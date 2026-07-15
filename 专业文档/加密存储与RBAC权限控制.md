# 加密存储与 RBAC 权限控制

## 一、概述

本平台面向四川省基层卫生技术人员，涉及用户密码、手机号等敏感信息。系统在代码层面实现了以下安全措施：

1. **密码 BCrypt 加密存储**：用户密码不以明文存储，采用 BCrypt 自适应哈希算法单向加密
2. **RBAC 权限控制模型**：基于"用户-角色-权限"三层模型，结合 Spring Security 实现粗粒度 URL 鉴权 + 细粒度方法级鉴权
3. **合规风险规避**：登录响应过滤敏感字段、JWT 无状态认证避免 Session 劫持、登录失败锁定机制

---

## 二、敏感信息加密存储

### 2.1 密码 BCrypt 加密

#### 数据库设计

```sql
-- sys_user 表密码字段定义
CREATE TABLE sys_user (
  ...
  password VARCHAR(100) NOT NULL COMMENT '密码(bcrypt加密)',
  ...
);
```

数据库中存储的是 BCrypt 哈希值（60 字符），而非明文密码：

```sql
-- 示例数据：密码明文为 123456，存储为 BCrypt 哈希
INSERT INTO sys_user (..., password, ...) VALUES
(1, 'admin', '$2a$10$EEeUC1lM2mbe.nOY0CtsDOVYQciytNhzUMLR2rAgI5nfOXzmlGJPK', ...);
```

#### BCryptPasswordEncoder 注入与使用

```java
// SysUserServiceImpl.java — 密码加密器注入
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    /** 校验密码：明文 vs BCrypt 密文 */
    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /** 创建用户 — 密码加密后存储 */
    @Override
    public boolean createUser(UserForm form) {
        SysUser user = new SysUser();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));  // BCrypt 加密
        // ... 其他字段
        return save(user);
    }

    /** 更新用户 — 仅在传入新密码时才加密更新 */
    @Override
    public boolean updateUser(UserForm form) {
        SysUser exist = getById(form.getId());
        if (StringUtils.hasText(form.getPassword())) {
            exist.setPassword(passwordEncoder.encode(form.getPassword()));  // BCrypt 加密
        }
        // ... 其他字段
        return updateById(exist);
    }
}
```

#### 登录校验流程

```java
// AuthController.java — 后台登录
@PostMapping("/admin/login")
public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
    // 1. 查询用户
    SysUser user = userService.getByUsername(dto.getUsername());
    if (user == null) {
        return Result.error(1002, "用户名或密码错误");  // 不暴露具体原因
    }

    // 2. 校验密码（BCrypt 明文 vs 密文比对）
    if (!userService.checkPassword(dto.getPassword(), user.getPassword())) {
        return Result.error(1002, "用户名或密码错误");  // 统一错误提示，防枚举
    }

    // 3. 校验状态
    if (user.getStatus() == null || user.getStatus() != 1) {
        return Result.error(403, "账号已被禁用");
    }

    // 4. 生成 JWT
    String token = jwtUtils.generate(user.getId(), user.getUsername(),
                                      user.getRole(), user.getRoleId());

    // 5. 过滤敏感字段后返回
    user.setPassword(null);  // 关键：移除密码字段，防止泄露
    LoginVO vo = new LoginVO();
    vo.setToken(token);
    vo.setUserInfo(userInfo);
    return Result.success(vo);
}
```

#### BCrypt 安全特性

| 特性 | 说明 |
|------|------|
| 自适应哈希 | 默认 cost=10（2^10=1024 轮），抗 GPU/ASIC 暴力破解 |
| 内置盐值 | 每次加密自动生成随机盐，相同明文产生不同密文 |
| 单向不可逆 | 无法从密文反推明文，只能通过 matches() 比对 |
| 兼容性 | Spring Security 原生支持，无需额外配置 |

---

### 2.2 登录响应敏感字段过滤

登录成功后，响应体中**不含密码字段**：

```java
// AuthController.java — 过滤敏感字段
user.setPassword(null);  // 清空密码

// 响应体结构（LoginVO）
{
    "code": 200,
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userInfo": {
            "id": 1,
            "username": "admin",
            "realName": "系统管理员",
            "role": "admin",
            "roleCode": "admin",
            "avatar": null,
            "orgName": "四川省卫健委"
            // 注意：password、phone 等敏感字段不在响应中
        }
    }
}
```

小程序端登录（`WxAuthController`）同样执行 `user.setPassword(null)` 过滤。

---

### 2.3 登录失败锁定机制

```java
// CommonConstants.java — 登录安全策略常量
public class CommonConstants {
    /** 登录失败锁定前缀 */
    public static final String LOGIN_FAIL_PREFIX = "login_fail:";
    /** 最大登录失败次数 */
    public static final int MAX_LOGIN_FAIL = 5;
    /** 登录失败锁定时长（分钟） */
    public static final long LOCK_MINUTES = 15;
}
```

连续 5 次登录失败后，账号被锁定 15 分钟（基于 Redis 计数器实现），有效防止暴力破解。

---

## 三、RBAC 权限控制模型

### 3.1 模型设计

本系统采用经典 RBAC0（基于角色的访问控制）模型，数据结构为"用户→角色→权限"三层：

```
用户(sys_user) ──FK(role_id)──> 角色(sys_role) ──关联表──> 权限(sys_permission)
```

#### 数据库表结构

```sql
-- 用户表（含角色外键）
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL COMMENT '密码(bcrypt加密)',
  real_name VARCHAR(50),
  phone VARCHAR(20),
  role_id BIGINT DEFAULT NULL COMMENT '角色ID,FK->sys_role.id(RBAC细粒度权限)',
  status TINYINT DEFAULT 1 COMMENT '状态:0禁用 1启用',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  ...
);

-- 角色字典表
CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(30) NOT NULL COMMENT 'ADMIN/TEACHER/STUDENT',
  role_name VARCHAR(50) NOT NULL COMMENT '系统管理员/讲师/学员',
  description VARCHAR(200),
  status TINYINT DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_role_code (role_code)
);

-- 权限字典表
CREATE TABLE sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  perm_code VARCHAR(100) NOT NULL COMMENT '权限编码(如course:read)',
  perm_name VARCHAR(100) NOT NULL COMMENT '权限显示名',
  module VARCHAR(50) COMMENT '归属模块',
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_perm_code (perm_code)
);

-- 角色-权限关联表（多对多）
CREATE TABLE sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_perm (role_id, permission_id)
);
```

#### 预设数据

**角色（3 个）**：

| ID | role_code | role_name | 说明 |
|----|-----------|-----------|------|
| 1 | ADMIN | 系统管理员 | 全部权限 |
| 2 | TEACHER | 培训讲师 | 课程/试题/考试/咨询管理 |
| 3 | STUDENT | 学员 | 学习/考试/咨询 |

**权限（20 个）**：

| ID | perm_code | perm_name | 模块 |
|----|-----------|-----------|------|
| 1 | course:read | 课程查看 | course |
| 2 | course:write | 课程编辑 | course |
| 3 | chapter:read | 章节查看 | chapter |
| 4 | chapter:write | 章节编辑 | chapter |
| 5 | knowledge:read | 知识点查看 | knowledge |
| 6 | knowledge:write | 知识点编辑 | knowledge |
| 7 | question:read | 题目查看 | question |
| 8 | question:write | 题目编辑 | question |
| 9 | exam:read | 考试查看 | exam |
| 10 | exam:write | 考试编辑 | exam |
| 11 | consult:read | 咨询查看 | consult |
| 12 | consult:write | 咨询处理 | consult |
| 13 | stats:read | 统计查看 | stats |
| 14 | user:read | 用户查看 | user |
| 15 | user:write | 用户编辑 | user |
| 16 | plan:read | 计划查看 | plan |
| 17 | plan:write | 计划编辑 | plan |
| 18 | teacher:read | 讲师查看 | teacher |
| 19 | teacher:write | 讲师编辑 | teacher |
| 20 | resource:read | 资源查看 | resource |

**角色-权限绑定（47 条）**：

| 角色 | 权限数 | 权限编码 |
|------|--------|----------|
| ADMIN | 20 | 全部权限 |
| TEACHER | 17 | 1-13 + 16,17,18,20（无 user 管理） |
| STUDENT | 10 | 1,3,5,7,9,11,12,13,16,20（只读 + 咨询提问） |

---

### 3.2 Spring Security 鉴权架构

#### SecurityConfig — 安全过滤器链

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // 启用方法级 @PreAuthorize
public class SecurityConfig {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RbacUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（JWT 天然防 CSRF）
            .csrf(csrf -> csrf.disable())
            // 无状态 session
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // URL 粗粒度鉴权规则
            .authorizeHttpRequests(auth -> auth
                // 白名单放行
                .antMatchers("/admin/login", "/api/wx/login",
                             "/error", "/swagger-ui/**", "/v3/api-docs/**")
                .permitAll()
                // TEACHER 可访问课程/章节/知识/试题/考试/计划/咨询/统计
                .antMatchers("/admin/course/**").hasAnyRole("ADMIN", "TEACHER")
                .antMatchers("/admin/chapter/**").hasAnyRole("ADMIN", "TEACHER")
                .antMatchers("/admin/knowledge/**").hasAnyRole("ADMIN", "TEACHER")
                .antMatchers("/admin/question/**").hasAnyRole("ADMIN", "TEACHER")
                .antMatchers("/admin/exam/**").hasAnyRole("ADMIN", "TEACHER")
                .antMatchers("/admin/train-plan/**").hasAnyRole("ADMIN", "TEACHER")
                .antMatchers("/admin/consult/**").hasAnyRole("ADMIN", "TEACHER")
                .antMatchers("/admin/stats/**").hasAnyRole("ADMIN", "TEACHER")
                // 其余后台路径仅 ADMIN
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // 异常处理
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler(new CustomAccessDeniedHandler())
            )
            // JWT 过滤器
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtUtils, userDetailsService),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

#### 鉴权层级说明

| 层级 | 机制 | 粒度 | 示例 |
|------|------|------|------|
| **L1 URL 鉴权** | SecurityConfig antMatchers | 粗粒度 | `/admin/course/**` → ADMIN/TEACHER |
| **L2 方法鉴权** | @PreAuthorize | 细粒度 | `@PreAuthorize("hasAuthority('course:write')")` |
| **L3 数据鉴权** | 业务逻辑 | 行级 | 仅操作本人/本机构数据 |

---

### 3.3 JWT 认证流程

#### JWT 工具类

```java
@Component
public class JwtUtils {

    @Value("${training.jwt.secret}")
    private String secret;

    @Value("${training.jwt.expire}")
    private long expire;  // 604800000ms = 7 天

    /** 生成 Token（含 userId, username, role, roleId） */
    public String generate(Long userId, String username,
                           String role, Long roleId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        if (roleId != null) {
            claims.put("roleId", roleId);
        }
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 解析 Token */
    public Claims parse(String token) { ... }

    /** 验证 Token 有效性 */
    public boolean validate(String token) { ... }
}
```

#### JWT 配置

```yaml
training:
  jwt:
    secret: training-platform-jwt-secret-key-2026-07-su-sheng-ji-ceng-wei-sheng
    expire: 604800000  # 7 天
```

#### JWT 过滤器（Admin 模块）

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> WHITELIST = Arrays.asList(
        "/admin/login", "/api/wx/login", "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) {
        // 1. 白名单放行
        // 2. 取 Authorization Header (Bearer xxx)
        // 3. 校验 token 有效性
        // 4. 解析 token 取 username
        // 5. 加载 UserDetails（含角色+权限）
        // 6. 构建 Authentication 写入 SecurityContext
    }
}
```

#### JWT 拦截器（API 模块 — 小程序端）

```java
@Component
public class ApiJwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 放行登录接口
        // 解析 Authorization Header
        // 校验 token → 提取 userId/role/username 存入 request attribute
    }
}
```

---

### 3.4 RbacUserDetailsService — 用户权限加载

```java
@Service
public class RbacUserDetailsService implements UserDetailsService {

    @Resource private SysUserMapper sysUserMapper;
    @Resource private SysRoleMapper sysRoleMapper;
    @Resource private SysPermissionMapper sysPermissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1. 查用户（未删除）
        SysUser user = sysUserMapper.selectByUsername(username);

        // 2. 校验状态（禁用则拒绝）
        if (user.getStatus() != 1) throw new UsernameNotFoundException("账号已被禁用");

        // 3. 优先按 roleId 查角色，回退到 role 字符串（兼容旧数据）
        String roleCode = null;
        if (user.getRoleId() != null) {
            SysRole role = sysRoleMapper.selectById(user.getRoleId());
            roleCode = role.getRoleCode();
        }

        // 4. 按 roleId 查权限列表
        List<SysPermission> perms = sysPermissionMapper.selectByRoleId(user.getRoleId());
        List<String> permissionCodes = perms.stream()
                .map(SysPermission::getPermCode)
                .collect(Collectors.toList());

        // 5. 组装 LoginUser（含 ROLE_xxx + 业务权限码）
        return new LoginUser(user, roleCode, permissionCodes);
    }
}
```

---

### 3.5 LoginUser — UserDetails 实现

```java
public class LoginUser implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private String roleCode;
    private List<SimpleGrantedAuthority> authorities;
    private boolean enabled;

    public LoginUser(SysUser user, String roleCode, List<String> permissionCodes) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.roleCode = roleCode;
        this.enabled = user.getStatus() != null && user.getStatus() == 1;

        // 合并角色（加 ROLE_ 前缀）+ 业务权限码
        this.authorities = new ArrayList<>();
        if (roleCode != null) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
            // 如 ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT
        }
        if (permissionCodes != null) {
            for (String code : permissionCodes) {
                this.authorities.add(new SimpleGrantedAuthority(code));
                // 如 course:read, course:write, exam:write
            }
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    // ... 其他 UserDetails 方法
}
```

**权限码格式说明**：

| 格式 | 示例 | 用途 |
|------|------|------|
| `ROLE_XXX` | ROLE_ADMIN, ROLE_TEACHER | Spring Security 内置角色前缀，用于 hasRole() |
| `module:action` | course:read, course:write | 自定义权限码，用于 hasAuthority() |

---

### 3.6 RBAC 数据自动初始化

```java
@Configuration
@ConditionalOnProperty(name = "training.rbac.init", havingValue = "true",
                       matchIfMissing = true)
public class RbacDataInitializer {

    @PostConstruct
    @Transactional(rollbackFor = Exception.class)
    public void init() {
        // 1. 初始化 3 个角色（ADMIN=1, TEACHER=2, STUDENT=3）
        // 2. 初始化 16 个权限字典
        // 3. 初始化角色-权限关联
        //    - ADMIN → 全部 16 个权限
        //    - TEACHER → 10 个选定权限
        //    - STUDENT → 仅 course:read
    }
}
```

应用启动时自动执行（幂等设计，重复启动不会重复插入）。

---

## 四、合规风险规避措施

### 4.1 敏感数据处理规范

| 风险点 | 规避措施 | 代码实现 |
|--------|----------|----------|
| 密码明文存储 | BCrypt 单向加密 | `passwordEncoder.encode()` |
| 密码泄露到前端 | 登录响应过滤 | `user.setPassword(null)` |
| 错误提示枚举用户 | 统一错误提示 | "用户名或密码错误" |
| 暴力破解 | 登录失败锁定 | Redis 计数器，5 次锁 15 分钟 |
| Session 劫持 | JWT 无状态认证 | `SessionCreationPolicy.STATELESS` |
| CSRF 攻击 | 禁用 Session + CSRF | `csrf.disable()` + Bearer Token |
| Token 伪造 | HMAC-SHA256 签名 | `signWith(getKey(), HS256)` |
| Token 过期 | 7 天有效期 | `expire: 604800000` |
| 越权访问 | RBAC 双层鉴权 | URL 鉴权 + 方法级 @PreAuthorize |
| 软删除数据泄露 | 逻辑删除字段 | `@TableLogic deleted` |

### 4.2 401/403 统一处理

```java
// 401 未认证 — 返回 JSON 而非重定向到登录页
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(...) {
        response.setStatus(401);
        body.put("code", 401);
        body.put("message", "未登录或登录已过期");
    }
}

// 403 无权限 — 已登录但访问未授权资源
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(...) {
        response.setStatus(403);
        body.put("code", 403);
        body.put("message", "无权限访问");
    }
}
```

### 4.3 请求参数校验

```java
// 登录请求体 — @Valid 参数校验
@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

全局异常处理器捕获 `MethodArgumentNotValidException`，返回 400 错误。

---

## 五、权限矩阵

### 5.1 后台管理端功能权限

| 功能模块 | ADMIN | TEACHER | STUDENT |
|----------|-------|---------|---------|
| 课程管理 | 读写 | 读写 | — |
| 章节管理 | 读写 | 读写 | — |
| 知识点管理 | 读写 | 读写 | — |
| 试题管理 | 读写 | 读写 | — |
| 考试管理 | 读写 | 读写 | — |
| 咨询管理 | 读写 | 读写 | — |
| 统计报表 | 读 | 读 | — |
| 用户管理 | 读写 | — | — |
| 培训计划 | 读写 | 读/写 | — |
| 讲师管理 | 读写 | 读 | — |
| 资源管理 | 读 | 读 | — |

### 5.2 小程序端功能权限

| 功能模块 | 学员 | 讲师 |
|----------|------|------|
| 课程浏览/报名 | ✅ | ✅ |
| 在线学习 | ✅ | ✅ |
| 考试 | ✅ | ✅ |
| 在线咨询 | ✅ | ✅ |
| 个人统计 | ✅ | ✅ |
| 个人中心 | ✅ | ✅ |

---

## 六、安全架构总览

```
                        客户端请求
                            │
                    ┌───────▼───────┐
                    │ Authorization │
                    │ Header: Bearer│
                    └───────┬───────┘
                            │
                ┌───────────▼───────────┐
                │  JwtAuthenticationFilter │  ← Admin 模块
                │  ApiJwtInterceptor      │  ← API 模块
                └───────────┬───────────┘
                            │
                  ┌─────────▼─────────┐
                  │  Token 校验(JWT)  │
                  │  解析 userId/role │
                  └─────────┬─────────┘
                            │
              ┌─────────────▼─────────────┐
              │  RbacUserDetailsService   │
              │  加载用户 + 角色 + 权限    │
              └─────────────┬─────────────┘
                            │
         ┌──────────────────▼──────────────────┐
         │         Spring Security             │
         │  ┌────────────┬───────────────────┐ │
         │  │ L1 URL鉴权 │ L2 方法鉴权       │ │
         │  │ hasRole()  │ hasAuthority()    │ │
         │  │ 粗粒度     │ 细粒度            │ │
         │  └────────────┴───────────────────┘ │
         └──────────────────┬──────────────────┘
                            │
                    ┌───────▼───────┐
                    │  Controller   │
                    │  业务处理     │
                    └───────────────┘
```
