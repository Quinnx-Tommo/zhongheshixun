# RBAC 权限重构设计方案（v1.0）

> **版本**：1.0
> **日期**：2026-07-08
> **作者**：tech-director
> **背景**：教师 7.8 讲解文档要求 RBAC 权限模型（user/role/permission/user_role/role_permission 三张表），项目现状仅 sys_user.role 字段，Controller 零角色校验。本方案补齐 RBAC 三表 + Spring Security + @PreAuthorize 细粒度鉴权，覆盖讲解文档要求。

---

## 一、设计参数确认

| 参数 | 选定方案 | 说明 |
|------|---------|------|
| 方案路径 | A. 标准 RBAC 三表 | 最完整，答辩最强 |
| 权限颗粒度 | `module:action`（16 个预设） | 如 course:read / course:write |
| 审计/审批表 | 不涉及 | 项目无工作流 |
| 用户-角色关联 | 单 `role_id` FK | 用户单角色（讲解文档中的 user_role 表等价），毕业设计标准够用 |
| API 端（小程序后台） | **不做 RBAC**，保持现状 userId 校验 | 小程序纯学员端，强加 RBAC 多此一举 |
| RBAC 表部署位置 | training-admin 8080 库统一管理 | 移动端不再重复建表 |
| 集成方式 | Spring Security + `UserDetailsService` + `OncePerRequestFilter` | 替代旧 `JwtInterceptor` |
| 注解策略 | `@PreAuthorize("hasAuthority('xxx:write')")` + SpEL | 方法细粒度 |
| URL 粗粒度 | `SecurityConfig`：`/admin/**` 要求 `ROLE_ADMIN` | 整个 admin 后台锁定管理员 |
| 鉴权分层 | URL 粗（ROLE_ADMIN）+ 方法细（authority） | 双保险 |
| 数据初始化 | `DataInitializer` @PostConstruct | 启动时保证角色/权限字典已就位 |
| 现有 role 字段处理 | **物理删除**，迁移至 role_id + 外键 | 不保留冗余 |

---

## 二、数据库改造

### 2.1 新建 3 张表

```sql
-- 角色字典表
CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'ADMIN/TEACHER/STUDENT',
  role_name VARCHAR(50) NOT NULL COMMENT '系统管理员/讲师/学员',
  description VARCHAR(200),
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色字典表';

-- 权限字典表（module:action 格式）
CREATE TABLE sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  perm_code VARCHAR(100) NOT NULL UNIQUE COMMENT '如 course:write',
  perm_name VARCHAR(100) NOT NULL COMMENT '权限中文名',
  description VARCHAR(200),
  module VARCHAR(50) COMMENT '所属模块分组（用于前端分组显示）',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限字典表';

-- 角色-权限关联表
CREATE TABLE sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_perm (role_id, permission_id),
  KEY idx_role_id (role_id),
  KEY idx_perm_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';
```

### 2.2 改造 sys_user

```sql
-- 加 role_id 列（允许 NULL 便于平滑迁移）
ALTER TABLE sys_user ADD COLUMN role_id BIGINT COMMENT 'FK -> sys_role.id' AFTER role;
ALTER TABLE sys_user ADD KEY idx_role_id (role_id);

-- 数据迁移：role='admin' → 1, 'teacher' → 2, 'student' → 3
-- （具体值见 DataInitializer）
UPDATE sys_user SET role_id = CASE role
  WHEN 'admin'   THEN 1
  WHEN 'teacher' THEN 2
  WHEN 'student'  THEN 3
  ELSE 3
END;

-- 验证迁移结果
SELECT COUNT(*) AS missing_role_id FROM sys_user WHERE role_id IS NULL;
-- 必须返回 0

-- 删除旧 role 列（须确认上一条返回 0 再执行）
ALTER TABLE sys_user DROP COLUMN role;
```

### 2.3 权限字典预设（共 16 个）

| module | perm_code | perm_name | 说明 |
|--------|-----------|-----------|------|
| course | course:read | 课程查看 | 列表/详情 |
| course | course:write | 课程编辑 | 新增/编辑/发布/删除 |
| chapter | chapter:read | 章节查看 | |
| chapter | chapter:write | 章节编辑 | 含排序 |
| knowledge | knowledge:read | 知识点查看 | |
| knowledge | knowledge:write | 知识点编辑 | |
| question | question:read | 试题查看 | |
| question | question:write | 试题编辑 | |
| exam | exam:read | 考试查看 | |
| exam | exam:write | 考试编辑 | 含组卷 |
| consult | consult:read | 咨询查看 | 工单列表 |
| consult | consult:write | 咨询处理 | 知识库管理 |
| stats | stats:read | 统计查看 | 报表接口 |
| stats | stats:write | 统计配置 | |
| user | user:read | 用户查看 | |
| user | user:write | 用户编辑 | 启用/禁用/删除 |

### 2.4 角色初始化

| role_code | role_name | 权限集 |
|-----------|-----------|--------|
| ADMIN | 系统管理员 | 全部 16 个 |
| TEACHER | 讲师 | course:read/write,knowledge:read/write,question:read/write,exam:read/write,consult:read/write,stats:read,student:read |
| STUDENT | 学员 | course:read（后台实际不使用，仅保留扩展） |

### 2.5 迁移顺序（必须按此顺序）

1. 执行 2.1 新建 3 张表
2. 执行 2.2 加 role_id 列（此行**先不加外键约束**）
3. 执行 2.3 DataInitializer 初始化 3 角色 + 16 权限
4. 执行 2.2 数据迁移 UPDATE（CASE 映射）
5. 执行 2.2 验证（count NULL = 0）
6. 执行 2.2 DROP COLUMN role
7. （可选）加 `FOREIGN KEY (role_id) REFERENCES sys_role(id)`

---

## 三、Java 代码改造

### 3.1 新增文件（约 12 个）

| 模块 | 文件 | 作用 |
|------|------|------|
| common/entity | SysRole.java | 角色实体 |
| common/entity | SysPermission.java | 权限实体 |
| common/entity | SysRolePermission.java | 关联实体 |
| common/mapper | SysRoleMapper.java | |
| common/mapper | SysPermissionMapper.java | |
| common/mapper | SysRolePermissionMapper.java | |
| common/mapper/xml | SysRoleMapper.xml | |
| common/mapper/xml | SysPermissionMapper.xml | |
| common/mapper/xml | SysRolePermissionMapper.xml | |
| service | RoleService.java / impl | 用户-角色-权限查询 |
| security | LoginUser.java | UserDetails 实现 |
| security | CustomUserDetailsService.java | 实现 UserDetailsService |
| security | JwtAuthenticationFilter.java | OncePerRequestFilter，替代旧拦截器 |
| security | CustomAuthenticationEntryPoint.java | 401 JSON 返回 |
| security | CustomAccessDeniedHandler.java | 403 JSON 返回 |
| config | SecurityConfig.java (mod) | 启用 Security + URL 规则 |
| config | DataInitializer.java (new) | 启动初始化字典 |
| vo | role/RoleDTO.java | 前端用户管理用 |

### 3.2 修改现有文件

| 文件 | 改动 |
|------|------|
| AuthController | 登录成功后返回 `role_code`，用于前端角色显示 |
| JwtInterceptor.java | **废弃**（由 JwtAuthenticationFilter 替代但不直接删，保留备查） |
| 所有 11 个 Controller | 补 `@PreAuthorize` 注解 |
| application.yml | 加 `training.rbac.enabled=true` 开关 |

### 3.3 架构图

```
请求
  │
  ▼
JwtAuthenticationFilter（OncePerRequestFilter）
  │ 解析 Authorization Header → 调 UserDetailsService
  │ 构建 UsernamePasswordAuthenticationToken 写入 SecurityContext
  ▼
Spring Security Filter Chain
  │ FilterSecurityInterceptor（URL 粗粒度：/admin/** requires ROLE_ADMIN）
  ▼
Method Security Interceptor（@PreAuthorize hasAuthority 细粒度）
  │
  ▼
Controller（业务逻辑）
```

### 3.4 权限编码规则

- Role → Spring Security 默认前缀 `ROLE_`（ADMIN 存为 `ROLE_ADMIN`）
- Permission → authority string 直接存 `course:write`
- UserDetails.authorities = merged list = `[ROLE_ADMIN, course:read, course:write, ...]`

### 3.5 Controller 注解分配

| Controller | 走读 | 写入 |
|---|---|---|
| AuthController | — | — | 登录公开放行 |
| CourseController | `course:read` | `course:write` |
| ChapterController | `chapter:read` | `chapter:write` |
| KnowledgeController | `knowledge:read` | `knowledge:write` |
| QuestionController | `question:read` | `question:write` |
| ExamController | `exam:read` | `exam:write` |
| ConsultController | `consult:read` | `consult:write` |
| StatsController | `stats:read` | `stats:write` |
| UserManagementController | `user:read` | `user:write` |
| TeacherController | `teacher:read` | `teacher:write` |
| TrainPlanController | `plan:read` | `plan:write` |

> 未列模块 teacher:read/write 实际未预置，需补入 DataInitializer 或改用 course 模块。

### 3.6 数据初始化器（DataInitializer）

```java
@Component
public class RbacDataInitializer {
    @PostConstruct
    public void init() {
        // 1. 查 sys_role 是否有 ADMIN；无则插入 3 角色
        // 2. 查 sys_permission 是否有 16 条；无则插入
        // 3. 查 sys_role_permission 是否有 20+ 条；无则按角色分配
        // 4. 打 INFO 日志确认
    }
}
```

---

## 四、前端适配

- 登录响应：加 `role_code` 字段；前端 Layout 读取显示角色 Badge
- 用户管理表：`role` 列改成 `roleName`，来自后端查询 `sys_role` 关联
- 其他页面无需改（因为 RBAC 在后端实施）

---

## 五、风险与回滚

| 风险 | 缓解 |
|------|------|
| role 字段删除影响现有数据 | DataInitializer + CASE 迁移 + 验证 |
| Spring Security 破坏现有接口 | 单测覆盖 + 全 Controller 冒烟 |
| JwtUtils 被改 | **不改 JwtUtils**，仅加 Filter 包装 |
| RBAC 导致现有 admin 账号失效 | 启动打印 role_id 初始化日志 |

### 回滚方案
- SecurityConfig 临时 `anyRequest().permitAll()`
- 改回旧 JwtInterceptor（保留旧文件）
- 软删除 RBAC 表（RENAME TABLE）

---

## 六、与教师讲解文档对照

| 讲解点 | 本方案覆盖 |
|--------|-----------|
| PC/移动端统一接口 | 不涉及（本期不做）。毕业设为简化设计，双端分离。 |
| JSON 序列化 | 已通过 Jackson 全局日期格式配置（-section 2） |
| Token 统一身份认证 | **↑ 已实现**，强化为 Filter + SecurityContext |
| 分页参数 | 已通过 MyBatis-Plus IPage 实现 |
| **RBAC 权限控制** | ✅ **本期重点补齐**（三表 + 拦截器鉴权） |

---

## 七、实施计划（约 1.5 人天）

| 阶段 | 内容 | 时间 |
|------|------|-----|
| Phase 1 | 数据库 3 表 + 改 sys_user + DataInitializer | 4h |
| Phase 2 | 新增 Entity + Mapper + XML | 2h |
| Phase 3 | CustomUserDetailsService + LoginUser + Filter | 3h |
| Phase 4 | SecurityConfig + Entry/Exit Handler | 2h |
| Phase 5 | 11 个 Controller 加 @PreAuthorize | 2h |
| Phase 6 | 前端 Layout/用户管理适配 + 冒烟测试 | 2h |

---

## 八、验收标准

1. ✅ `mvn compile` 通过
2. ✅ 登录接口返回 JWT + role_code
3. ✅ 未登录访问 `/admin/course/page` → 401 JSON
4. ✅ 学员（role=3）访问 `/admin/course` POST → 403 JSON
5. ✅ admin 用户访问全接口 → 200
6. ✅ DataInitializer 启动日志显示角色/权限初始化成功
7. ✅ 现有 5+ 冒烟 case 通过

---

## 九、待用户确认点

（已通过 5 轮 AskUserQuestion 完成收口，无剩余歧义）

---

**批准状态**：✅ 已批准（2026-07-08）
**下一步**：进入实现阶段（建议 invoke writing-plans skill 或分 Phase 逐步编码）
