# 后端开发文档（dev-backend.md）

> **版本**: 1.0.0
> **日期**: 2026-07-07
> **对应项目**: 四川省基层卫生人员网络培训平台
> **技术栈**: Spring Boot 2.7.18 + MyBatis-Plus 3.5.3 + MySQL 8.0 + JJWT 0.11.5
> **配套文档导航**:
> - API 接口文档: `docs/dev-api.md`
> - 数据库设计: `docs/dev-database.md`
> - 前端实现手册: `docs/dev-frontend.md`
> - 小程序实现手册: `docs/dev-miniapp.md`
> - 部署手册: `docs/dev-deploy.md`
> - 开发文档（编码/联调/部署权威参考）: `docs/开发文档.md`
> - 设计文档（架构视角）: `docs/设计文档.md`

---

## 目录

- [第 1 章：公共层（training-common）](#第-1-章公共层training-common)
- [第 2 章：用户模块（完整代码）](#第-2-章用户模块完整代码)
- [第 3 章：课程模块（完整代码）](#第-3-章课程模块完整代码)
- [第 4 章：学习模块](#第-4-章学习模块)
- [第 5 章：考试模块（完整代码）](#第-5-章考试模块完整代码)
- [第 6 章：培训计划模块](#第-6-章培训计划模块)
- [第 7 章：智能问答模块（SLA 机制完整代码）](#第-7-章智能问答模块sla-机制完整代码)
- [第 8 章：统计模块](#第-8-章统计模块)
- [第 9 章：高并发技术方案（代码级概念验证）](#第-9-章高并发技术方案代码级概念验证重点章)
- [第 10 章：云服务三层架构（IAAS/PAAS/SAAS）](#第-10-章云服务三层架构iaaspaassaas)
- [第 11 章：配置文件速查](#第-11-章配置文件速查)

---

## 第 1 章：公共层（training-common）

> 【负责成员】成员A（架构）+ 成员B（业务）
> 模块路径：`training-common/src/main/java/com/training/common/`

公共层是所有模块的依赖基础，包含统一响应、异常处理、JWT 工具、实体类、DTO/VO。

### 1.1 Result.java — 泛型响应包装

```java
package com.training.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
```

### 1.2 ResultCode 枚举

```java
package com.training.common.result;

public enum ResultCode {
    SUCCESS(200, "success"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(1000, "业务异常"),
    USERNAME_EXISTS(1001, "用户名已存在"),
    LOGIN_FAIL(1002, "用户名或密码错误"),
    COURSE_NOT_FOUND(1003, "课程不存在"),
    EXAM_NOT_FOUND(1004, "考试不存在"),
    ENROLL_EXISTS(1005, "已报名该课程"),
    EXAM_TIME_OVER(1006, "考试时间已结束"),
    RETRY_LIMIT(1007, "已达到最大重考次数");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() { return code; }
    public String getMessage() { return message; }
}
```

### 1.3 BusinessException + GlobalExceptionHandler

```java
package com.training.common.exception;

import com.training.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 1000;
    }
}
```

```java
package com.training.common.exception;

import com.training.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return Result.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
```

### 1.4 JwtUtils — HMAC-SHA256 完整实现

```java
package com.training.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${training.jwt.secret}")
    private String secret;

    @Value("${training.jwt.expire}")
    private long expire;

    /**
     * 由 secret 字符串生成 HMAC-SHA256 密钥
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色 admin/teacher/student
     */
    public String generate(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 获取用户ID
     */
    public Long getUserId(String token) {
        return parse(token).get("userId", Long.class);
    }

    /**
     * 从 Token 获取角色
     */
    public String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validate(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

### 1.5 SysUser 实体（Lombok + @TableName）

```java
package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String name;

    private String phone;

    private String email;

    /** 角色: admin/teacher/student */
    private String role;

    private String avatar;

    /** 所属机构（乡镇卫生院/社区卫生服务中心） */
    private String orgName;

    /** 岗位类型: 临床/公卫/护理/医技 */
    private String jobType;

    /** 状态: 0禁用 1启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### 1.6 其他公共组件

| 组件 | 路径 | 说明 |
|------|------|------|
| `PageQuery.java` | `common/dto/` | 分页请求基类（pageNum / pageSize） |
| `LoginDTO.java` | `common/dto/` | 登录请求体（username / password） |
| `LoginVO.java` | `common/vo/` | 登录响应体（token / userInfo） |
| `CommonConstants.java` | `common/constants/` | 常量（角色枚举、状态值） |
| `MetaObjectFiller.java` | `common/handler/` | MyBatis-Plus 自动填充 createTime/updateTime |

---

## 第 2 章：用户模块 ⭐（完整代码）

> 【负责成员】成员A
> 模块路径：`training-service/src/main/java/com/training/service/impl/UserServiceImpl.java`

### 2.1 业务规则

| 规则 | 说明 |
|------|------|
| 密码加密 | 使用 `BCryptPasswordEncoder`（强度 10），禁止明文存储 |
| 登录失败锁定 | 连续 5 次失败锁定 15 分钟（Redis 计数，key=`login_fail:{username}`） |
| 注册默认角色 | 新注册用户默认 `role=student`，管理员可通过后台调整 |
| 用户名唯一 | `sys_user.username` 有唯一约束，重复返回 `1001` |
| 状态校验 | `status=0` 的账号禁止登录 |

### 2.2 UserServiceImpl.login() 完整代码

```java
@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int MAX_LOGIN_FAIL = 5;
    private static final long LOCK_MINUTES = 15;

    @Override
    public LoginVO login(LoginDTO dto) {
        // ===== 步骤1：检查是否被锁定 =====
        String lockKey = "login_fail:" + dto.getUsername();
        String failCountStr = redisTemplate.opsForValue().get(lockKey);
        if (failCountStr != null && Integer.parseInt(failCountStr) >= MAX_LOGIN_FAIL) {
            throw new BusinessException("登录失败次数过多，请 " + LOCK_MINUTES + " 分钟后再试");
        }

        // ===== 步骤2：查询用户 =====
        SysUser user = lambdaQuery().eq(SysUser::getUsername, dto.getUsername()).one();
        if (user == null) {
            recordFail(lockKey);
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }

        // ===== 步骤3：校验密码（bcrypt）=====
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            recordFail(lockKey);
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }

        // ===== 步骤4：校验状态 =====
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // ===== 步骤5：登录成功，清除失败计数，签发 Token =====
        redisTemplate.delete(lockKey);
        String token = jwtUtils.generate(user.getId(), user.getUsername(), user.getRole());

        user.setPassword(null);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserInfo(user);
        return vo;
    }

    private void recordFail(String lockKey) {
        Long count = redisTemplate.opsForValue().increment(lockKey);
        if (count != null && count == 1) {
            redisTemplate.expire(lockKey, LOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    public void register(RegisterDTO dto) {
        // 1. 用户名唯一校验
        if (lambdaQuery().eq(SysUser::getUsername, dto.getUsername()).count() > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        // 2. 加密密码，默认 student
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("student");
        user.setStatus(1);
        save(user);
    }
}
```

### 2.3 PasswordEncoder 配置

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 2.4 JwtInterceptor（preHandle）+ InterceptorConfig 完整代码

```java
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // ===== 步骤1：放行登录等白名单接口 =====
        String uri = request.getRequestURI();
        if (uri.contains("/login") || uri.contains("/wx/login")) {
            return true;
        }

        // ===== 步骤2：解析 Authorization Header =====
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // ===== 步骤3：校验 Token 有效性 =====
        if (!jwtUtils.validate(token)) {
            writeError(response, 401, "未登录或登录已过期");
            return false;
        }

        // ===== 步骤4：将用户信息注入 request attribute，供 Controller 使用 =====
        Claims claims = jwtUtils.parse(token);
        request.setAttribute("userId", claims.get("userId", Long.class));
        request.setAttribute("role", claims.get("role", String.class));
        request.setAttribute("username", claims.get("username", String.class));
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + msg + "\"}");
    }
}
```

```java
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/admin/**", "/api/**")
                .excludePathPatterns("/admin/login", "/api/wx/login", "/error");
    }
}
```

### 2.5 /admin/login 接口

```java
@RestController
@RequestMapping("/admin")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(userService.login(dto));
    }
}
```

---

## 第 3 章：课程模块 ⭐（完整代码）

> 【负责成员】成员A
> 模块路径：`training-service/src/main/java/com/training/service/impl/CourseServiceImpl.java`

### 3.1 业务规则

| 规则 | 说明 |
|------|------|
| CRUD | 标题必填、学时 ≥ 0、封面可选 |
| 发布状态机 | 草稿(0) → 已发布(1) → 已下架(2)，不可逆向 |
| 章节排序 | `sort_order` 控制播放顺序，同一课程下唯一 |
| 药柜式课件 | 通过 `resource_file` 灵活组合视频/PPT/PDF 形成不同课程 |
| 离线属性 | `offline_flag=1` 标记支持离线，启用后异步打包 ZIP |
| 发布校验 | 发布前至少 1 个章节，否则拒绝 |

### 3.2 CourseServiceImpl 关键方法（含发布状态机校验）

```java
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Autowired
    private CourseChapterMapper chapterMapper;

    @Autowired
    private OfflinePackageService offlinePackageService;

    @Override
    public Page<CourseVO> pageCourse(PageQuery query) {
        return baseMapper.selectCoursePage(
            new Page<>(query.getPageNum(), query.getPageSize()),
            query.getKeyword(), query.getCourseType(), query.getStatus()
        );
    }

    @Override
    public CourseVO detail(Long id) {
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);

        // 章节列表（按 sort_order 升序）
        vo.setChapters(chapterMapper.selectList(
            new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, id)
                .orderByAsc(CourseChapter::getSortOrder)
        ));
        // 资源文件列表
        vo.setResources(resourceFileMapper.selectList(
            new LambdaQueryWrapper<ResourceFile>().eq(ResourceFile::getCourseId, id)
        ));
        return vo;
    }

    @Override
    public void publish(Long id, Integer status) {
        // ===== 步骤1：状态机校验 =====
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        // 只允许 0→1（发布）或 1→2（下架）
        if (course.getStatus() == 0 && status != 1) {
            throw new BusinessException("草稿状态只能发布");
        }
        if (course.getStatus() == 1 && status != 2) {
            throw new BusinessException("已发布状态只能下架");
        }
        if (course.getStatus() == 2) {
            throw new BusinessException("已下架课程不可操作");
        }

        // ===== 步骤2：发布前校验章节完整性 =====
        if (status == 1) {
            long chapterCount = chapterMapper.selectCount(
                new LambdaQueryWrapper<CourseChapter>().eq(CourseChapter::getCourseId, id)
            );
            if (chapterCount == 0) {
                throw new BusinessException("发布前至少添加一个章节");
            }
        }

        // ===== 步骤3：更新状态 =====
        course.setStatus(status);
        updateById(course);
    }

    @Override
    public void enableOffline(Long id) {
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        // 异步打包 ZIP
        offlinePackageService.packageCourse(id);
        course.setOfflineFlag(1);
        updateById(course);
    }
}
```

### 3.3 离线打包服务 OfflinePackageService（核心打包逻辑完整）

```java
@Slf4j
@Service
public class OfflinePackageService {

    @Value("${training.offline.path}")
    private String offlineBasePath;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseChapterMapper chapterMapper;

    @Autowired
    private ResourceFileMapper resourceFileMapper;

    /**
     * 为课程生成离线 ZIP 包
     * 包含：章节视频 + 课件资源 + manifest.json（课程元数据）
     */
    @Async("taskExecutor")
    public void packageCourse(Long courseId) {
        // ===== 步骤1：查询课程元数据 =====
        Course course = courseMapper.selectById(courseId);
        List<CourseChapter> chapters = chapterMapper.selectList(
            new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId)
                .orderByAsc(CourseChapter::getSortOrder)
        );
        List<ResourceFile> resources = resourceFileMapper.selectList(
            new LambdaQueryWrapper<ResourceFile>().eq(ResourceFile::getCourseId, courseId)
        );

        // ===== 步骤2：构造 ZIP 文件名 =====
        String zipName = courseId + "_" + course.getTitle().replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "") + ".zip";
        Path zipPath = Paths.get(offlineBasePath, zipName);

        // ===== 步骤3：打包（manifest + 视频 + 资源）=====
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {

            // 3.1 写入 manifest.json
            zos.putNextEntry(new ZipEntry("manifest.json"));
            String manifest = buildManifest(course, chapters);
            zos.write(manifest.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 3.2 写入章节视频
            for (CourseChapter ch : chapters) {
                if (StringUtils.hasText(ch.getVideoUrl())) {
                    Path videoFile = resolveVideoFile(ch.getVideoUrl());
                    if (Files.exists(videoFile)) {
                        zos.putNextEntry(new ZipEntry("video/" + ch.getSortOrder() + "_" + ch.getTitle() + ".mp4"));
                        Files.copy(videoFile, zos);
                        zos.closeEntry();
                    }
                }
            }

            // 3.3 写入课件资源（Word/PPT/PDF）
            for (ResourceFile res : resources) {
                Path resFile = resolveResourceFile(res.getFileUrl());
                if (Files.exists(resFile)) {
                    zos.putNextEntry(new ZipEntry("resources/" + res.getFileName()));
                    Files.copy(resFile, zos);
                    zos.closeEntry();
                }
            }

        } catch (IOException e) {
            log.error("打包课程 {} 离线包失败", courseId, e);
            throw new BusinessException("离线包生成失败");
        }

        // ===== 步骤4：更新 course.zip_url =====
        course.setZipUrl("/offline/" + zipName);
        courseMapper.updateById(course);
        log.info("课程 {} 离线包生成成功：{}", courseId, zipName);
    }

    private String buildManifest(Course course, List<CourseChapter> chapters) {
        return String.format(
            "{\"title\":\"%s\",\"description\":\"%s\",\"chapters\":%d,\"generatedAt\":\"%s\"}",
            course.getTitle(), course.getDescription(), chapters.size(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    private Path resolveVideoFile(String videoUrl) {
        // videoUrl 形如 /video/xxx.mp4，映射到本地存储目录
        return Paths.get(offlineBasePath, "../video", Paths.get(videoUrl).getFileName().toString());
    }

    private Path resolveResourceFile(String fileUrl) {
        return Paths.get(offlineBasePath, "../upload", Paths.get(fileUrl).getFileName().toString());
    }
}
```

---

## 第 4 章：学习模块

> 【负责成员】成员A
> 模块路径：`training-service/src/main/java/com/training/service/impl/StudyServiceImpl.java`

### 4.1 业务规则

| 规则 | 说明 |
|------|------|
| 报名 | 学员可报名公开课/必修课，同一课程不可重复报名（`uk_student_course`） |
| 进度上报 | 每次播放进度更新上报，记录 progress / study_duration / last_position |
| 断点续播 | 下次进入返回 `last_position`，从该位置起播 |
| 进度回传 | 离线学习进度记录在 localStorage，联网后回传同一接口 |
| 累加逻辑 | `study_duration` 累加，`progress` 取最大值，`last_position` 覆盖 |

### 4.2 关键实现：saveOrUpdate + 累加逻辑（完整代码片段）

```java
@Service
public class StudyServiceImpl extends ServiceImpl<StudyRecordMapper, StudyRecord> implements StudyService {

    @Autowired
    private CourseEnrollMapper enrollMapper;

    @Override
    public void enroll(Long userId, Long courseId) {
        // ===== 步骤1：校验课程存在且已发布 =====
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != 1) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        // ===== 步骤2：唯一约束防重复报名 =====
        long exist = enrollMapper.selectCount(new LambdaQueryWrapper<CourseEnroll>()
            .eq(CourseEnroll::getStudentId, userId)
            .eq(CourseEnroll::getCourseId, courseId)
        );
        if (exist > 0) {
            throw new BusinessException(ResultCode.ENROLL_EXISTS);
        }
        CourseEnroll enroll = new CourseEnroll();
        enroll.setStudentId(userId);
        enroll.setCourseId(courseId);
        enrollMapper.insert(enroll);
    }

    @Override
    public void reportProgress(Long userId, ProgressDTO dto) {
        // ===== 步骤1：查询是否已有学习记录（利用唯一约束 uk_study）=====
        StudyRecord record = lambdaQuery()
            .eq(StudyRecord::getStudentId, userId)
            .eq(StudyRecord::getCourseId, dto.getCourseId())
            .eq(StudyRecord::getChapterId, dto.getChapterId())
            .one();

        // ===== 步骤2：不存在则新建 =====
        if (record == null) {
            record = new StudyRecord();
            record.setStudentId(userId);
            record.setCourseId(dto.getCourseId());
            record.setChapterId(dto.getChapterId());
            record.setProgress(dto.getProgress());
            record.setLastPosition(dto.getLastPosition());
            record.setStudyDuration(dto.getStudyDuration());
            save(record);
            return;
        }

        // ===== 步骤3：存在则累加/覆盖 =====
        // progress 取最大值（避免回退）
        if (dto.getProgress() != null && dto.getProgress() > record.getProgress()) {
            record.setProgress(dto.getProgress());
        }
        // last_position 覆盖（取最新播放位置）
        if (dto.getLastPosition() != null) {
            record.setLastPosition(dto.getLastPosition());
        }
        // study_duration 累加（每次上报增加 N 秒）
        if (dto.getStudyDuration() != null && dto.getStudyDuration() > 0) {
            record.setStudyDuration(
                (record.getStudyDuration() == null ? 0 : record.getStudyDuration())
                + dto.getStudyDuration()
            );
        }
        updateById(record);
    }

    @Override
    public StudyRecord getLastPosition(Long userId, Long chapterId) {
        return lambdaQuery()
            .eq(StudyRecord::getStudentId, userId)
            .eq(StudyRecord::getChapterId, chapterId)
            .one();
    }
}
```

### 4.3 实现要点

- 唯一约束 `uk_study(student_id, course_id, chapter_id)` 保证每个学员每个章节一条记录，重复上报走 UPDATE。
- `progress` 取最大值避免用户回退视频导致进度回退。
- `study_duration` 累加，前端每 10 秒上报一次（防抖 + 节流）。
- 进度回传：离线期间记录在 localStorage，联网后批量调用同一接口，后端无感知。

---

## 第 5 章：考试模块 ⭐（完整代码）

> 【负责成员】成员B
> 模块路径：`training-service/src/main/java/com/training/service/impl/ExamServiceImpl.java`

### 5.1 业务规则

| 规则 | 说明 |
|------|------|
| 3 种考试类型 | 课程考试(1) / 计划考试(2) / 单独考试(3)，通过 `exam_type` 区分 |
| 5 种题型 | 单选(1) / 多选(2) / 判断(3) / 填空(4) / 问答(5) |
| 难度比例组卷 | 简单 30% : 普通 50% : 困难 20%，随机抽题 |
| 客观题自动阅卷 | 单选/多选/判断/填空自动判分，问答题(5)跳过 |
| 重考次数 | 超过 `max_retry` 返回 `1007` |
| 及格判定 | `score >= passScore` 则 `passed=true` |
| 答案防泄露 | 开始考试接口不返回 `answer` 字段 |

### 5.2 ⭐ 自动组卷算法 generatePaper() 完整代码

```java
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamPaperMapper paperMapper;

    @Autowired
    private ExamRecordMapper recordMapper;

    @Autowired
    private ExamAnswerMapper answerMapper;

    /**
     * 自动组卷：按难度比例 30:50:20（易:中:难）随机抽题
     * @param examId 考试ID
     * @param userId 学员ID
     * @return 试卷ID
     */
    @Override
    public Long generatePaper(Long examId, Long userId) {
        // ===== 步骤1：校验考试存在且已发布 =====
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException(ResultCode.EXAM_NOT_FOUND);
        }
        if (exam.getStatus() != 1) {
            throw new BusinessException("考试未发布");
        }

        // ===== 步骤2：校验时间窗口 =====
        // 如有 startTime/endTime 字段，校验当前时间在其间（此处简化）

        // ===== 步骤3：校验重考次数 =====
        long attemptCount = recordMapper.selectCount(new LambdaQueryWrapper<ExamRecord>()
            .eq(ExamRecord::getExamId, examId)
            .eq(ExamRecord::getStudentId, userId)
        );
        if (attemptCount >= exam.getMaxRetry()) {
            throw new BusinessException(ResultCode.RETRY_LIMIT);
        }

        // ===== 步骤4：按难度比例计算各难度题目数量 =====
        int count = exam.getQuestionCount();
        int easy = count * 30 / 100;
        int medium = count * 50 / 100;
        int hard = count - easy - easy;   // 余数给困难，避免舍入误差
        // 注：上面是演示，正确写法：
        hard = count - easy - medium;

        // ===== 步骤5：按难度随机抽题 =====
        List<Question> questions = new ArrayList<>();
        questions.addAll(questionMapper.randomByDifficulty(exam.getCourseId(), 1, easy));
        questions.addAll(questionMapper.randomByDifficulty(exam.getCourseId(), 2, medium));
        questions.addAll(questionMapper.randomByDifficulty(exam.getCourseId(), 3, hard));

        // ===== 步骤6：打乱顺序（保证公平性）=====
        Collections.shuffle(questions);

        // ===== 步骤7：写入试卷表（题目ID快照）=====
        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        ExamPaper paper = new ExamPaper();
        paper.setExamId(examId);
        paper.setStudentId(userId);
        paper.setQuestions(JSON.toJSONString(questionIds));
        paperMapper.insert(paper);

        return paper.getId();
    }
}
```

### 5.3 Mapper SQL（randomByDifficulty ORDER BY RAND()）

```xml
<!-- QuestionMapper.xml -->
<select id="randomByDifficulty" resultType="com.training.common.entity.Question">
    SELECT * FROM question
    WHERE course_id = #{courseId}
      AND difficulty = #{difficulty}
    ORDER BY RAND()
    LIMIT #{count}
</select>
```

```java
// QuestionMapper.java
public interface QuestionMapper extends BaseMapper<Question> {
    List<Question> randomByDifficulty(@Param("courseId") Long courseId,
                                     @Param("difficulty") Integer difficulty,
                                     @Param("count") Integer count);
}
```

### 5.4 自动阅卷完整代码（for 循环判分 + 跳过问答题 type=5）

```java
    @Override
    @Transactional
    public SubmitVO submit(Long userId, SubmitDTO dto) {
        // ===== 步骤1：获取考试记录和试卷 =====
        ExamRecord record = recordMapper.selectById(dto.getRecordId());
        if (record == null || !record.getStudentId().equals(userId)) {
            throw new BusinessException(ResultCode.EXAM_NOT_FOUND);
        }
        ExamPaper paper = paperMapper.selectById(record.getPaperId());
        Exam exam = getById(record.getExamId());

        // ===== 步骤2：解析试卷题目列表 =====
        List<Long> questionIds = JSON.parseArray(paper.getQuestions(), Long.class);
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // ===== 步骤3：客观题自动阅卷 =====
        BigDecimal totalScore = BigDecimal.ZERO;
        List<SubmitVO.Result> results = new ArrayList<>();

        for (AnswerDTO ans : dto.getAnswers()) {
            Question q = questionMap.get(ans.getQuestionId());
            if (q == null) continue;

            ExamAnswer ea = new ExamAnswer();
            ea.setRecordId(record.getId());
            ea.setQuestionId(ans.getQuestionId());
            ea.setStudentAnswer(ans.getAnswer());

            // 客观题判分（问答题 type=5 跳过，需人工批阅）
            if (q.getQuestionType() != 5 && q.getAnswer() != null
                    && q.getAnswer().equalsIgnoreCase(ans.getAnswer().trim())) {
                ea.setIsCorrect(1);
                ea.setScore(q.getScore());
                totalScore = totalScore.add(q.getScore());
            } else if (q.getQuestionType() != 5) {
                ea.setIsCorrect(0);
                ea.setScore(BigDecimal.ZERO);
            }
            answerMapper.insert(ea);

            // 组装返回结果
            SubmitVO.Result r = new SubmitVO.Result();
            r.setQuestionId(q.getId());
            r.setCorrect(ea.getIsCorrect() != null && ea.getIsCorrect() == 1);
            r.setCorrectAnswer(q.getAnswer());
            results.add(r);
        }

        // ===== 步骤4：更新考试记录 =====
        record.setScore(totalScore);
        record.setStatus(2);  // 已批阅
        record.setSubmitTime(LocalDateTime.now());
        recordMapper.updateById(record);

        // ===== 步骤5：组装返回 =====
        SubmitVO vo = new SubmitVO();
        vo.setScore(totalScore);
        vo.setPassed(totalScore.compareTo(exam.getPassScore()) >= 0);
        vo.setResults(results);
        return vo;
    }
```

### 5.5 开始考试 + 提交答卷接口流程

**开始考试 `GET /api/exam/start/{id}`**：
1. 校验考试存在且已发布
2. 校验时间窗口（如有）
3. 校验重考次数（超限返回 1007）
4. 调用 `generatePaper()` 抽题组卷
5. 创建 `exam_record`（status=0 进行中）
6. 返回题目列表（**不返回 answer 字段**）

**提交答卷 `POST /api/exam/submit`**：
1. 校验考试记录归属当前学员
2. 客观题自动判分，问答题跳过
3. 计算总分，更新 `exam_record`
4. 返回成绩 + 每题对错 + 正确答案

---

## 第 6 章：培训计划模块

> 【负责成员】成员B
> 模块路径：`training-service/src/main/java/com/training/service/impl/TrainPlanServiceImpl.java`

### 6.1 业务规则

| 规则 | 说明 |
|------|------|
| 多课程关联 | 一个计划包含多个课程，通过 `plan_course` 关联 |
| sort_order 流程控制 | 控制学习顺序，学员按顺序完成 |
| is_required | 标记必修/选修，必修课程必须完成 |
| 状态机 | 草稿(0) → 已发布(1) → 已结束(2) |
| 学员授权 | 计划发布后，指定学员自动获得计划内课程学习权限 |

### 6.2 三种学习模式映射

| 学习模式 | 映射方式 | 说明 |
|---------|---------|------|
| 公开课 | `course.course_type = 1` | 学员自主选择学习 |
| 必修课 | `course.course_type = 2` | 管理员分配，针对机构/岗位定制 |
| 计划流程性培训 | `train_plan` + `plan_course` | 多课程按顺序组合，含考试 |

### 6.3 关键方法（伪代码为主）

```java
@Service
public class TrainPlanServiceImpl extends ServiceImpl<TrainPlanMapper, TrainPlan> implements TrainPlanService {

    @Autowired
    private PlanCourseMapper planCourseMapper;

    @Override
    public void addPlan(PlanDTO dto) {
        // ===== 步骤1：创建计划 =====
        TrainPlan plan = new TrainPlan();
        plan.setTitle(dto.getTitle());
        plan.setDescription(dto.getDescription());
        plan.setStatus(0);  // 草稿
        save(plan);

        // ===== 步骤2：关联课程（按 sort_order 排序）=====
        if (dto.getCourses() != null) {
            for (PlanCourse pc : dto.getCourses()) {
                pc.setPlanId(plan.getId());
                planCourseMapper.insert(pc);
            }
        }

        // ===== 步骤3：授权学员（可选）=====
        if (dto.getStudentIds() != null) {
            // 为每个学员对每个计划内课程创建 course_enroll
            grantCourseAccess(plan.getId(), dto.getStudentIds());
        }
    }

    @Override
    public PlanVO detail(Long planId, Long userId) {
        TrainPlan plan = getById(planId);
        PlanVO vo = new PlanVO();
        BeanUtils.copyProperties(plan, vo);

        // 查询计划课程列表（含每课程学习进度）
        List<PlanCourseVO> courses = planCourseMapper.selectWithProgress(planId, userId);
        vo.setCourses(courses);
        return vo;
    }

    @Override
    public void publishPlan(Long planId) {
        TrainPlan plan = getById(planId);
        // 校验至少一门课程
        long courseCount = planCourseMapper.selectCount(
            new LambdaQueryWrapper<PlanCourse>().eq(PlanCourse::getPlanId, planId)
        );
        if (courseCount == 0) {
            throw new BusinessException("计划至少包含一门课程");
        }
        plan.setStatus(1);
        updateById(plan);
    }
}
```

### 6.4 实现要点

- `plan_course` 表联合索引 `idx_plan_sort(plan_id, sort_order)` 支持按顺序查询。
- 学员查看计划详情时，LEFT JOIN `study_record` 计算每课程学习进度。
- 流程控制：学员必须按 `sort_order` 顺序完成课程（前端校验，后端可选）。

---

## 第 7 章：智能问答模块 ⭐（SLA 机制完整代码）

> 【负责成员】成员B
> 模块路径：`training-service/src/main/java/com/training/service/impl/ConsultServiceImpl.java`

### 7.1 业务规则

| 规则 | 说明 |
|------|------|
| 智能应答 | LongCat AI 自动回复（启用时）；转人工关键词检测命中直接转人工 |
| 人工转接 | 学员主动要求或 AI 未启用/失败时，转人工客服队列 |
| **SLA < 1 分钟** | docx 3.5 明确要求：人工应答时间 < 1 分钟 |
| SLA 计时起点 | 转人工时记录 `transfer_time` |
| SLA 告警 | 回复时计算耗时，超时写 `sla_alert` 表 + 日志告警 |

### 7.2 转人工关键词检测

```java
private static final List<String> TRANSFER_KEYWORDS =
    Arrays.asList("转人工", "找老师", "人工客服", "联系客服", "找人工", "客服");

/**
 * 检测问题中是否包含转人工关键词。
 * 命中则直接转人工工单，跳过 LongCat AI 自动回复。
 * 关键词来源：consult_keyword 表（v1.3.0 新增，可后台扩展）。
 */
private boolean containsTransferKeyword(String question) {
    if (StringUtils.isBlank(question)) {
        return false;
    }
    String lowerQuestion = question.toLowerCase();
    return TRANSFER_KEYWORDS.stream()
        .anyMatch(lowerQuestion::contains);
}
```

### 7.3 ⭐ SLA <1 分钟机制完整实现

```java
@Service
public class ConsultServiceImpl extends ServiceImpl<ConsultRecordMapper, ConsultRecord> implements ConsultService {

    @Autowired
    private LongCatAiService longCatAiService;

    @Autowired
    private SlaAlertMapper slaAlertMapper;

    private static final long SLA_THRESHOLD_SECONDS = 60;  // docx 要求 < 1 分钟

    /**
     * 学员提问：转人工关键词检测 → LongCat AI 自动回复 → 兜底转人工（记录 transfer_time）
     */
    @Override
    public AskVO ask(Long studentId, AskDTO dto) {
        ConsultRecord record = new ConsultRecord();
        record.setStudentId(studentId);
        record.setQuestion(dto.getQuestion());

        AskVO vo = new AskVO();

        // ===== 步骤1：检测转人工关键词，命中直接转人工 =====
        if (containsTransferKeyword(dto.getQuestion())) {
            record.setAnswer("已转人工客服");
            record.setIsAuto(0);
            record.setTransferTime(LocalDateTime.now());   // SLA 计时起点
            save(record);

            vo.setConsultId(record.getId());
            vo.setAutoReply("正在为您转接人工客服...");
            vo.setMatched(false);
            return vo;
        }

        // ===== 步骤2：调用 LongCat AI 自动回复（启用时） =====
        String aiReply = longCatAiService.ask(dto.getQuestion());

        if (StringUtils.hasText(aiReply)) {
            // ===== 步骤3a：AI 命中，自动回复 =====
            record.setAnswer(aiReply);
            record.setIsAuto(1);
            save(record);

            vo.setConsultId(record.getId());
            vo.setAutoReply(aiReply);
            vo.setMatched(true);
            return vo;
        }

        // ===== 步骤3b：AI 未启用或失败 → 兜底转人工，记录 SLA 计时起点 =====
        record.setAnswer("AI 暂不可用，已转人工客服");
        record.setIsAuto(0);
        record.setTransferTime(LocalDateTime.now());   // SLA 计时起点
        save(record);

        vo.setConsultId(record.getId());
        vo.setAutoReply("正在为您转接人工客服...");
        vo.setMatched(false);
        return vo;
    }

    /**
     * 人工回复：计算耗时、超时写 sla_alert 表、日志告警
     */
    @Override
    public void manualReply(Long consultId, String answer) {
        // ===== 步骤1：查询咨询记录 =====
        ConsultRecord record = getById(consultId);
        if (record == null) {
            throw new BusinessException("咨询记录不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setAnswer(answer);
        record.setReplyTime(now);

        // ===== 步骤2：SLA 校验 =====
        if (record.getTransferTime() != null) {
            long seconds = ChronoUnit.SECONDS.between(record.getTransferTime(), now);
            boolean exceeded = seconds > SLA_THRESHOLD_SECONDS;
            record.setSlaExceeded(exceeded ? 1 : 0);

            if (exceeded) {
                // 写入 SLA 告警表
                SlaAlert alert = new SlaAlert();
                alert.setConsultId(consultId);
                alert.setStudentId(record.getStudentId());
                alert.setQuestion(record.getQuestion());
                alert.setTransferTime(record.getTransferTime());
                alert.setReplyTime(now);
                alert.setDurationSeconds(seconds);
                slaAlertMapper.insert(alert);

                // 日志告警
                log.warn("咨询#{} 人工应答耗时 {} 秒，超过 1 分钟 SLA 阈值", consultId, seconds);
            } else {
                log.info("咨询#{} 人工应答耗时 {} 秒，SLA 达标", consultId, seconds);
            }
        }

        // ===== 步骤3：更新记录 =====
        updateById(record);
    }

    /**
     * 关键词提取（简化版：按空格/标点分词）
     */
    private List<String> extractKeywords(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        // 去除标点，按空格分词，过滤停用词（简化）
        return Arrays.stream(text.replaceAll("[，。？！,?!\\s]+", " ").trim().split("\\s+"))
                .filter(s -> s.length() > 1)
                .distinct()
                .collect(Collectors.toList());
    }
}
```

### 7.4 consult_record 表补充字段说明

```sql
-- SLA 相关字段（v2.0 新增）
ALTER TABLE consult_record ADD COLUMN transfer_time  DATETIME     DEFAULT NULL COMMENT '转人工时间（SLA 计时起点）';
ALTER TABLE consult_record ADD COLUMN reply_time    DATETIME     DEFAULT NULL COMMENT '人工回复时间';
ALTER TABLE consult_record ADD COLUMN sla_exceeded  TINYINT      DEFAULT 0     COMMENT '是否 SLA 超时：0否 1是';

-- SLA 告警表（独立存储超时记录，便于统计）
CREATE TABLE sla_alert (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    consult_id        BIGINT       NOT NULL,
    student_id        BIGINT       NOT NULL,
    question          VARCHAR(500) NOT NULL,
    transfer_time     DATETIME     NOT NULL,
    reply_time        DATETIME     NOT NULL,
    duration_seconds  INT          NOT NULL COMMENT '应答耗时（秒）',
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_consult (consult_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SLA 超时告警表';
```

### 7.5 实现要点

- `transfer_time` 是 SLA 计时起点，在转人工时写入。
- `manualReply()` 计算 `reply_time - transfer_time`，超过 60 秒写 `sla_alert`。
- 后台 `/admin/consult/sla-list` 返回超时记录，统计页展示"SLA 达标率"。
- 生产环境可接入定时任务扫描超时未回复工单，主动推送告警。

---

## 第 8 章：统计模块

> 【负责成员】成员B
> 模块路径：`training-service/src/main/java/com/training/service/impl/StatsServiceImpl.java`

### 8.1 6 个统计接口映射

| 接口 | 视角 | 返回核心字段 |
|------|------|-------------|
| `/admin/stats/overview` | 平台总览 | userCount / courseCount / examCount / todayStudyCount |
| `/admin/stats/student` | 微观（学员） | 各学员学习时长、完成率；支持 orgName/jobType 筛选 |
| `/admin/stats/exam` | 成果监控 | 平均分、通过率、分数段分布 |
| `/admin/stats/course` | 宏观（课程） | 报名人数、学习人数、完成率 |
| `/admin/stats/org` | 机构维度 | 各机构学习时长、通过率横向对比 |
| `/admin/stats/trend` | 时间趋势 | 近 N 天学习/考试人数折线图数据 |

### 8.2 ⭐ 多角度查询 XML 示例（按机构+岗位交叉，动态 SQL）

```xml
<!-- StatsMapper.xml -->
<select id="selectStudentStats" resultType="com.training.common.vo.StudentStatVO">
    SELECT u.id AS studentId,
           u.name AS realName,
           u.org_name AS orgName,
           u.job_type AS jobType,
           IFNULL(SUM(s.study_duration), 0) AS totalStudySeconds,
           IFNULL(AVG(s.progress), 0) AS avgProgress,
           (SELECT COUNT(*) FROM exam_record er WHERE er.student_id = u.id) AS examCount,
           (SELECT AVG(er.score) FROM exam_record er WHERE er.student_id = u.id) AS examAvgScore
    FROM sys_user u
    LEFT JOIN study_record s ON u.id = s.student_id
    WHERE u.role = 'student'
    <if test="orgName != null and orgName != ''">
        AND u.org_name = #{orgName}
    </if>
    <if test="jobType != null and jobType != ''">
        AND u.job_type = #{jobType}
    </if>
    GROUP BY u.id
    ORDER BY totalStudySeconds DESC
</select>
```

### 8.3 overview 实现完整代码

```java
@Service
public class StatsServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements StatsService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private StudyRecordMapper studyRecordMapper;

    @Override
    public StatsVO overview() {
        StatsVO vo = new StatsVO();

        // ===== 步骤1：用户总数 =====
        vo.setUserCount(selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "student")));

        // ===== 步骤2：已发布课程数 =====
        vo.setCourseCount(courseMapper.selectCount(
            new LambdaQueryWrapper<Course>().eq(Course::getStatus, 1)
        ));

        // ===== 步骤3：考试总数 =====
        vo.setExamCount(examMapper.selectCount(null));

        // ===== 步骤4：今日学习人数（去重）=====
        vo.setTodayStudyCount(studyRecordMapper.countTodayDistinctStudents());

        return vo;
    }

    @Override
    public Page<StudentStatVO> studentStats(PageQuery query) {
        return baseMapper.selectStudentStats(
            new Page<>(query.getPageNum(), query.getPageSize()),
            query.getOrgName(), query.getJobType(), query.getRealName()
        );
    }

    @Override
    public ExamStatVO examStat(Long examId) {
        // 平均分、通过率、分数段分布
        return baseMapper.selectExamStat(examId);
    }

    @Override
    public List<TrendVO> trend(Integer days, String type) {
        // 近 N 天学习/考试人数趋势
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);
        return baseMapper.selectTrend(start, end, type);
    }
}
```

### 8.4 实现要点

- 多角度查询：通过 `<if test>` 动态 SQL 实现机构/岗位/课程/时间交叉筛选。
- 今日活跃人数：按 `update_time` 统计当天有学习行为的去重学员数。
- 分数段分布：CASE WHEN 分组统计 0-59/60-69/70-79/80-89/90-100 各段人数。
- 前端使用 ECharts 渲染柱状图、饼图、折线图。

---

## 第 9 章：高并发技术方案（代码级概念验证）⭐ 重点章

> 【负责成员】成员A（存储层/流媒体）+ 成员B（计算层/传输层）
> 对应 docx 原始需求第 4 节"项目特点"（17 项高并发技术）

docx 第 4 节明确列出传输层/计算层/存储层共 17 项技术。本节给出 **6 项核心技术的可运行代码演示**，其余做配置级或文档级落地。

### 9.1 CDN 模拟 — CdnCacheService 完整代码

> **docx 原文引用**："网络链路出口进行压力分载，通过CDN让用户访问最近的数据缓存。"

```java
@Slf4j
@Service
public class CdnCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CourseMapper courseMapper;

    /** CDN 缓存 TTL：30 分钟 */
    private static final long CDN_TTL_MINUTES = 30;

    /** 缓存空对象 TTL：2 分钟（防穿透）*/
    private static final long NULL_TTL_MINUTES = 2;

    private static final String NULL_VALUE = "NULL";

    /**
     * 读取课程详情（带 CDN 缓存层）
     * 生产环境 CDN 节点回源到 Nginx → 本服务；这里用 Redis 模拟 CDN 节点缓存
     */
    public CourseVO getCourseDetail(Long courseId) {
        String cacheKey = "cdn:course:" + courseId;

        // ===== 步骤1：查 CDN 缓存（模拟）=====
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (NULL_VALUE.equals(cached)) {
                return null;  // 缓存空对象，防穿透
            }
            return (CourseVO) cached;  // CDN 命中，直接返回
        }

        // ===== 步骤2：CDN 未命中，回源到数据库 =====
        CourseVO detail = courseMapper.selectDetailById(courseId);

        // ===== 步骤3：写入 CDN 缓存（含空对象防穿透）=====
        if (detail != null) {
            redisTemplate.opsForValue().set(cacheKey, detail, CDN_TTL_MINUTES, TimeUnit.MINUTES);
        } else {
            // 缓存空对象，防止缓存穿透
            redisTemplate.opsForValue().set(cacheKey, NULL_VALUE, NULL_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return detail;
    }

    /**
     * 课程下架/更新时，主动清除 CDN 缓存（缓存一致性）
     */
    public void invalidateCourse(Long courseId) {
        redisTemplate.delete("cdn:course:" + courseId);
        log.info("课程 {} CDN 缓存已清除", courseId);
    }
}
```

### 9.2 MapReduce / BSP — MapReduceDemo 完整代码

> **docx 原文引用**："采用最经典的分布式算法对海量数据进行处理，将计算进行分载。" / "BSP(Bulk Synchronous Parallel-大型同步模型)算法是基于MPI算法的基础进行演化，运用在系统中并行计算的部分。"

```java
/**
 * 模拟 MapReduce：海量学员学习时长聚合统计
 * docx 第 4 节明确要求 MapReduce 做分布式计算，这里用 parallelStream 模拟
 */
@Slf4j
@Component
public class MapReduceDemo {

    @Autowired
    private StudyRecordMapper studyRecordMapper;

    /**
     * Map 阶段：将学员分片，每片并行计算局部学习时长
     * Reduce 阶段：汇总各片结果
     * BSP 同步点：parallelStream 的 collect 天然是同步屏障
     */
    public StudyStatsVO calcTotalStudyTime(List<Long> studentIds, LocalDate start, LocalDate end) {
        // ===== Map 阶段 =====
        // parallelStream 将数据集拆分为多个分片（Fork/Join 线程池），类似 Map 任务的分布执行
        List<StudyStatsVO.PartialResult> partialResults = studentIds.parallelStream()
            .map(studentId -> {
                // 每个 Map 任务：计算单个学员的学习时长（局部聚合）
                Integer total = studyRecordMapper.sumDuration(studentId, start, end);
                return new StudyStatsVO.PartialResult(studentId, total != null ? total : 0);
            })
            .collect(Collectors.toList());

        // ===== BSP 同步点 =====
        // parallelStream 的 collect 是同步屏障，所有 Map 任务完成后才继续（BSP 思想）

        // ===== Reduce 阶段 =====
        // 汇总各 Map 任务结果（类似 Reduce 端聚合）
        int totalSeconds = partialResults.stream()
            .mapToInt(StudyStatsVO.PartialResult::getDuration)
            .sum();
        int avgSeconds = studentIds.isEmpty() ? 0 : totalSeconds / studentIds.size();

        StudyStatsVO vo = new StudyStatsVO();
        vo.setStudentCount(studentIds.size());
        vo.setTotalStudySeconds(totalSeconds);
        vo.setAvgStudySeconds(avgSeconds);
        vo.setMapTaskCount(partialResults.size());   // 展示"分布式"执行过程
        log.info("MapReduce 统计完成：{} 个学员，Map 任务数 {}，总学习时长 {} 秒",
                studentIds.size(), partialResults.size(), totalSeconds);
        return vo;
    }
}
```

### 9.3 读写分离 — DataSourceConfig 完整代码

> **docx 原文引用**："由于系统的读大于写的频率，数据库架构采用了1主/多从，双主多从的策略，所以我们将会将读和写进行分离，并且将大量的读请求分散给多台不同的(Slave)服务器。"

```java
/**
 * 读写分离数据源配置
 * docx 第 4 节要求"1 主/多从，双主多从，读写分离"
 */
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();   // 主库：读写
    }

    @Bean
    @ConfigurationProperties("spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();   // 从库：只读
    }

    @Primary
    @Bean
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource master,
            @Qualifier("slaveDataSource") DataSource slave) {
        DynamicRoutingDataSource routing = new DynamicRoutingDataSource();
        Map<Object, Object> target = new HashMap<>();
        target.put("master", master);
        target.put("slave", slave);
        routing.setTargetDataSources(target);
        routing.setDefaultTargetDataSource(master);
        return routing;
    }
}
```

配合 AOP 按方法名路由（读操作走 slave）：

```java
@Aspect
@Component
public class DataSourceRouteAspect {

    @Around("execution(* com.training.service.*.get*(..)) || " +
            "execution(* com.training.service.*.list*(..)) || " +
            "execution(* com.training.service.*.query*(..)) || " +
            "execution(* com.training.service.*.page*(..))")
    public Object routeToSlave(ProceedingJoinPoint pjp) throws Throwable {
        DynamicDataSourceContextHolder.set("slave");
        try {
            return pjp.proceed();
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }
}
```

对应 `application.yml` 配置：

```yaml
spring:
  datasource:
    master:
      url: jdbc:mysql://master-host:3306/training?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
      username: writer
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver
    slave:
      url: jdbc:mysql://slave-host:3306/training?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
      username: reader
      password: 654321
      driver-class-name: com.mysql.cj.jdbc.Driver
```

### 9.4 分库分表 — ShardingSphere 配置 + DDL 分区策略

> **docx 原文引用**："一台数据库将很快无法满足大量并发，需要使用库表散列，将数据库中的数据进行分散存储。我们在应用程序中安装业务和应用或者功能模块将数据库进行分离...搜狐的论坛就是采用了这样的架构，将论坛的用户、设置、帖子等信息进行数据库分离，然后对帖子、用户按照板块和ID进行散列数据库和表..."

```yaml
# application-sharding.yml —— 分库分表策略（按 docx 搜狐论坛架构思想）
spring:
  shardingsphere:
    datasource:
      names: ds0, ds1
      ds0:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://host1:3306/training_0
        username: root
        password: 123456
      ds1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        jdbc-url: jdbc:mysql://host2:3306/training_1
        username: root
        password: 123456
    rules:
      sharding:
        tables:
          sys_user:                                         # 用户表按 ID 散列
            actual-data-nodes: ds$0..1.sys_user_$0..3       # 2 库 × 4 表 = 8 张分表
            database-strategy:
              standard:
                sharding-column: id
                sharding-algorithm-name: db-mod
            table-strategy:
              standard:
                sharding-column: id
                sharding-algorithm-name: table-mod
          study_record:                                    # 学习记录按时间+ID 散列
            actual-data-nodes: ds$0..1.study_record_$0..7
            database-strategy:
              standard:
                sharding-column: student_id
                sharding-algorithm-name: db-mod
            table-strategy:
              standard:
                sharding-column: student_id
                sharding-algorithm-name: table-mod
        sharding-algorithms:
          db-mod:
            type: MOD
            props:
              sharding-count: 2
          table-mod:
            type: MOD
            props:
              sharding-count: 4
    props:
      sql-show: true
```

**分库分表映射表**：

| 原表 | 分片策略 | 分片数 | 映射结果 |
|------|---------|--------|---------|
| `sys_user` | 按 id MOD 散列 | 2 库 × 4 表 | `ds0.sys_user_0` ~ `ds1.sys_user_3` |
| `study_record` | 按 student_id MOD 散列 | 2 库 × 8 表 | `ds0.study_record_0` ~ `ds1.study_record_7` |
| `exam_record` | 按 student_id MOD 散列 | 2 库 × 4 表 | `ds0.exam_record_0` ~ `ds1.exam_record_3` |

**时间分区 DDL（docx 要求"按时间段作为分区主要策略"）**：

```sql
-- 学习记录表按月 RANGE 分区
ALTER TABLE study_record
  PARTITION BY RANGE (YEAR(create_time) * 100 + MONTH(create_time)) (
    PARTITION p202601 VALUES LESS THAN (202602),
    PARTITION p202602 VALUES LESS THAN (202603),
    PARTITION p202603 VALUES LESS THAN (202604),
    PARTITION p202604 VALUES LESS THAN (202605),
    PARTITION p202605 VALUES LESS THAN (202606),
    PARTITION p202606 VALUES LESS THAN (202607),
    PARTITION p202607 VALUES LESS THAN (202608),
    PARTITION p_future VALUES LESS THAN MAXVALUE
  );
```

### 9.5 流媒体分离 — VideoController 完整代码

> **docx 原文引用**："针对最消耗资源的流媒体文件，我们将流媒体与页面进行分离，建立独立的流媒体服务器来降低提供页面访问请求的服务器系统压力，保证系统不会因为流媒体问题而崩溃..."

```java
@Slf4j
@RestController
@RequestMapping("/video")
public class VideoController {

    @Value("${training.video.path}")
    private String videoBasePath;   // D:/training/video/

    /**
     * 视频流式播放，支持 HTTP Range 请求（断点续传/拖动进度条）
     * 生产环境应由独立流媒体服务器（Nginx-RTMP / SRS）处理，这里演示流媒体分离思想
     */
    @GetMapping("/play/{filename}")
    public ResponseEntity<StreamingResponseBody> play(
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

        Path videoPath = Paths.get(videoBasePath, filename).normalize();
        // 安全校验：防止路径穿越
        if (!videoPath.startsWith(Paths.get(videoBasePath).normalize())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!Files.exists(videoPath)) {
            return ResponseEntity.notFound().build();
        }

        long fileSize = Files.size(videoPath);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("video/mp4"));
        headers.set("Accept-Ranges", "bytes");

        // ===== 处理 Range 请求（206 Partial Content）=====
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] range = rangeHeader.substring(6).split("-");
            long start = Long.parseLong(range[0]);
            long end = range[1].isEmpty() ? fileSize - 1 : Long.parseLong(range[1]);
            long contentLength = end - start + 1;

            headers.set("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
            headers.setContentLength(contentLength);

            StreamingResponseBody stream = output -> {
                try (InputStream is = Files.newInputStream(videoPath)) {
                    is.skip(start);
                    byte[] buffer = new byte[4096];
                    long remaining = contentLength;
                    int bytesRead;
                    while (remaining > 0 && (bytesRead = is.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                        output.write(buffer, 0, bytesRead);
                        remaining -= bytesRead;
                    }
                }
            };
            return new ResponseEntity<>(stream, headers, HttpStatus.PARTIAL_CONTENT);
        }

        // ===== 无 Range：返回完整流 =====
        headers.setContentLength(fileSize);
        StreamingResponseBody stream = output -> {
            try (InputStream is = Files.newInputStream(videoPath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
        };
        return new ResponseEntity<>(stream, headers, HttpStatus.OK);
    }
}
```

### 9.6 LVS/HA-Proxy — Nginx upstream + keepalive 配置

> **docx 原文引用**："对用户的请求进行压力分载，并且实现多种负载均衡的策略，也可以选择使用HA-Proxy实现。" / "针对Web服务器进行方向代理，通过HA-Proxy将用户的请求分发到不同的Web服务器上。"

```nginx
# nginx.conf —— 负载均衡（模拟 LVS/HA-Proxy）+ keepalive 高可用

# 后端集群：加权轮询（weight）+ 健康检查（max_fails/fail_timeout）
upstream admin_backend {
    # 加权轮询：weight 越大分配越多
    server 127.0.0.1:8080 weight=3 max_fails=2 fail_timeout=10s;
    server 127.0.0.1:8082 weight=2 max_fails=2 fail_timeout=10s;
    server 127.0.0.1:8083 weight=1 max_fails=2 fail_timeout=10s backup;  # 备份节点

    # keepalive：长连接池，减少 TCP 握手开销
    keepalive 32;
}

upstream api_backend {
    least_conn;                          # 最少连接策略（适合长请求）
    server 127.0.0.1:8081 weight=1 max_fails=2 fail_timeout=10s;
    server 127.0.0.1:8084 weight=1 max_fails=2 fail_timeout=10s;
    keepalive 32;
}

# 流媒体独立集群（流媒体分离）
upstream video_backend {
    server 127.0.0.1:8090 weight=1;
    keepalive 64;
}

server {
    listen 80;
    server_name training.example.com;

    # 视频/流媒体独立路径 —— 流媒体分离
    location /video/ {
        proxy_pass http://video_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header Range $http_range;
        proxy_set_header If-Range $http_if_range;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }

    # 后台管理 API 代理
    location /admin/ {
        proxy_pass http://admin_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }

    # 小程序 API 代理
    location /api/ {
        proxy_pass http://api_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }
}
```

**负载均衡策略说明**：

| 策略 | Nginx 指令 | 适用场景 |
|------|-----------|---------|
| 加权轮询 | `weight=N` | 后端性能不均（admin 集群） |
| 最少连接 | `least_conn` | 长请求/连接（api 集群） |
| IP 哈希 | `ip_hash` | 会话保持 |
| 备份节点 | `backup` | 高可用（主节点全挂时启用） |

### 9.7 高并发方案汇总表

| 层 | docx 技术项 | 本项目落地方式 | 代码文件 |
|----|------------|---------------|---------|
| 传输 | CDN | Redis 30min TTL 热点缓存 + 空对象防穿透 | `CdnCacheService.java` |
| 传输 | 智能双路 | 文档说明，Nginx 智能 DNS（生产） | `docs/dev-deploy.md` |
| 传输 | LVS | Nginx upstream 多实例负载 | `nginx.conf` |
| 传输 | HA-Proxy | Nginx 反向代理 + keepalive | `nginx.conf` |
| 传输 | 流媒体分离 | HTTP Range 流式响应 + 独立视频路径 | `VideoController.java` + `nginx.conf` |
| 传输 | HTML 静态化 | 首页公告 Redis TTL 缓存 | 与 CDN 缓存共用 |
| 计算 | MapReduce | parallelStream 分布式统计 | `MapReduceDemo.java` |
| 计算 | BSP | parallelStream 同步屏障 | `MapReduceDemo.java` |
| 计算 | Result Cache | 复用 CDN 缓存 | `CdnCacheService.java` |
| 计算 | Scatter/Gather | 前端并发调用多统计接口聚合 | 前端 ECharts |
| 存储 | 读写分离 | 双数据源 + AOP 按方法名路由 | `DataSourceConfig.java` + `DataSourceRouteAspect.java` |
| 存储 | 分区策略 | 月度 RANGE 分区 DDL | `docs/database.sql` |
| 存储 | Sharding | ShardingSphere 配置（2 库 × N 表） | `application-sharding.yml` |
| 存储 | 列式存储 | 覆盖索引 + SQL 只查必要字段 | 统计 XML |
| 存储 | 数据库集群 | 文档说明 + 读写分离演示 | `DataSourceConfig.java` |
| 存储 | 数据库镜像 | 文档说明（关键数据镜像） | `docs/dev-deploy.md` |

---

## 第 10 章：云服务三层架构（IAAS/PAAS/SAAS）

> 【负责成员】成员A（架构）+ 全员（落地）
> 对应 docx 原始需求第 3 节"软件即服务"（IAAS/PAAS/SAAS 三层）

### 10.1 三层架构图（文本）

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    SAAS 层（软件即服务）                                  │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    业务提供层（用户访问层）                        │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │   │
│  │  │ 微信小程序│  │ 后台管理 │  │ PC 端    │  │ 移动 APP │       │   │
│  │  │ (学员端) │  │ (管理端) │  │ (离线下载)│  │ (预留)   │       │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    业务服务层（业务逻辑层）                        │   │
│  │  ┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐ │   │
│  │  │培训学习││考试测评││资源管理││统计分析││在线咨询││系统管理│ │   │
│  │  │ 服务   ││ 服务   ││ 服务   ││ 服务   ││ 服务   ││ 服务   │ │   │
│  │  └────────┘└────────┘└────────┘└────────┘└────────┘└────────┘ │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    基础服务层（通用抽象层）                        │   │
│  │  ┌──────────┐┌──────────┐┌──────────┐┌──────────┐┌──────────┐ │   │
│  │  │用户认证  ││报表服务  ││搜索服务  ││接口服务  ││文件服务  │ │   │
│  │  │ 服务     ││(预留)    ││(预留)    ││(预留)    ││          │ │   │
│  │  └──────────┘└──────────┘└──────────┘└──────────┘└──────────┘ │   │
│  └─────────────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────────────┤
│                    PAAS 层（平台即服务）                                  │
│  ┌──────────┐┌───────────┐┌───────────┐┌───────────┐┌───────────┐      │
│  │ MySQL    ││ Redis     ││ Web 容器  ││ 消息队列  ││ 流媒体    │      │
│  │ (数据库) ││ (分布式   ││ (Undertow)││ (RabbitMQ/││ 服务器    │      │
│  │          ││  缓存)    ││           ││  Kafka)   ││ (流媒体   │      │
│  │          ││           ││           ││           ││  分离)    │      │
│  └──────────┘└───────────┘└───────────┘└───────────┘└───────────┘      │
├─────────────────────────────────────────────────────────────────────────┤
│                    IAAS 层（基础设施即服务）                              │
│  ┌──────────┐┌───────────┐┌───────────┐┌───────────┐┌───────────┐      │
│  │ 网络资源 ││ 硬件服务器 ││ 存储设备  ││ 负载均衡  ││ CDN 加速  │      │
│  │ (带宽/   ││ (CPU/内存) ││ (SSD/HDD) ││ (LVS/     ││ (内容     │      │
│  │   VPC)   ││           ││           ││ HA-Proxy) ││  分发)    │      │
│  └──────────┘└───────────┘└───────────┘└───────────┘└───────────┘      │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                     统一安全（跨层控制体系）                        │  │
│  │  内容安全  │  应用认证（JWT/OAuth）│  数据交互加密  │  审计日志   │  │
│  │  网络安全  │  权限控制（RBAC）     │  传输加密(HTTPS)│  安全监控   │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 10.2 三层与本项目代码的映射表

| 云服务层 | docx 含义 | 本项目落地位置 | 落地方式 |
|---------|---------|----------------|---------|
| **IAAS** | 网络、服务器、存储、负载均衡、CDN | 部署章节 | Nginx + 单机服务器 + 本地存储 + Nginx 负载均衡演示 |
| **PAAS** | 数据库、缓存、Web 容器、消息队列、流媒体 | training-common / training-dao | MySQL 8.0 + Redis + Undertow + Nginx-RTMP（预留） |
| **SAAS - 基础服务层** | 用户认证、报表、搜索、接口、文件服务 | training-common | `JwtUtils.java` + `Result.java` + `GlobalExceptionHandler.java` |
| **SAAS - 业务服务层** | 培训学习、考试测评、资源管理、统计分析、在线咨询、系统管理 | training-service | 7 个 Service |
| **SAAS - 业务提供层** | 前端用户界面（Web/小程序/APP） | training-admin + training-api + miniprogram | Vue3 后台 + 微信小程序 |
| **统一安全** | 内容/应用/数据/传输安全 | 全局 | JwtInterceptor + CorsConfig + BCrypt + 审计日志 |

### 10.3 统一安全体系（7 维度 → 对应代码）

| 安全维度 | 实现点 | 对应代码/配置 |
|---------|-------|-------------|
| **应用认证** | JWT Token + 角色拦截 | `JwtInterceptor`、`InterceptorConfig` |
| **权限控制** | RBAC（admin/teacher/student 三角色） | Controller 层 `request.getAttribute("role")` 校验 |
| **跨域安全** | 跨域白名单 + 参数化查询防注入 | `CorsConfig` + MyBatis-Plus `#{}` |
| **密码安全** | bcrypt 加盐哈希 | `BCryptPasswordEncoder` |
| **传输加密** | HTTPS（生产环境） | Nginx SSL 配置 |
| **内容安全** | 视频/课件审核（预留） | `resource_file` 预留 `audit_status` 字段 |
| **审计日志** | 敏感操作记录 | `audit_log` 表（登录/关键操作写入） |

---

## 第 11 章：配置文件速查

> 【负责成员】成员A

### 11.1 application.yml（含 master/slave 双数据源、mybatis-plus、jwt、file 配置）

```yaml
# application.yml —— training-admin（端口 8080）
server:
  port: 8080
  undertow:
    threads:
      io: 16
      worker: 256
    buffer-size: 1024

spring:
  profiles:
    active: dev
  datasource:
    # 主库（写）
    master:
      url: jdbc:mysql://localhost:3306/training?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
    # 从库（读，生产环境指向只读实例）
    slave:
      url: jdbc:mysql://localhost:3306/training?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 5000
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
  servlet:
    multipart:
      max-file-size: 500MB      # 视频文件上传
      max-request-size: 500MB

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.training.common.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl   # 开发期打印 SQL
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# 自定义配置
training:
  jwt:
    secret: training-platform-jwt-secret-key-2026-07
    expire: 604800000          # 7天（毫秒）
  file:
    upload-path: D:/training/upload/    # Windows
    # upload-path: /home/training/upload/  # Linux
  video:
    path: D:/training/video/
    # path: /home/training/video/
  offline:
    path: D:/training/offline/
    # path: /home/training/offline/

logging:
  level:
    com.training: debug
    com.training.dao: debug
```

### 11.2 CorsConfig + MybatisPlusConfig 完整代码

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

```java
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件（可选）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
```

### 11.3 其他配置类速查

| 配置类 | 路径 | 说明 |
|--------|------|------|
| `SecurityConfig` | `admin/config/` | PasswordEncoder Bean |
| `InterceptorConfig` | `admin/config/` | JWT 拦截器注册 |
| `DataSourceConfig` | `admin/config/` | 读写分离双数据源 |
| `RedisConfig` | `common/config/` | RedisTemplate 序列化 |
| `AsyncConfig` | `admin/config/` | @Async 线程池（离线打包用） |
| `WebMvcConfig` | `admin/config/` | 静态资源映射 |

```java
// AsyncConfig：离线打包异步线程池
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("offline-pack-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

---

## 附录：配套文档导航

| 文档 | 用途 |
|------|------|
| `docs/dev-api.md` | API 接口文档（接口清单 + 统一响应格式） |
| `docs/dev-database.md` | 数据库设计文档（17 张表 + ER 图 + 分区策略） |
| `docs/dev-frontend.md` | 前端实现手册（Vue3 + Element Plus） |
| `docs/dev-miniapp.md` | 小程序实现手册（微信小程序） |
| `docs/dev-deploy.md` | 部署手册（打包 + Nginx + 启动） |
| `docs/开发文档.md` | 开发者实现手册（编码/联调/部署权威参考） |
| `docs/设计文档.md` | 单一事实源（架构 + 数据库 + API + 分工 + 计划） |
| `docs/需求偏差说明.md` | docx 需求对照 + 简化理由 |

---

> **版本**：v1.0.0 | **维护人**：Backend Lead | **更新日期**：2026-07-07
