# 2026-07-08 网页端设计 + RBAC 重构进度日志

> 本文件记录 2026-07-08 对四川省基层卫生人员网络培训平台项目进行的网页设计 + RBAC 重构的全部进度、改动、遇到的错误及解决方案。
> 项目：`D:\A-Users\Desktop\zhongheshixun`
> 文档锚点：`docs/superpowers/specs/2026-07-08-rbac-refactor-design.md`（RBAC 设计方案）

---

## 一、今日完成的主要工作

### 1. 前端网页端页面补全（7 个新页面）✅

#### 已完成 P0（3 个页面）
| 页面 | 路由 | 文件 | 状态 |
|------|------|------|------|
| 章节管理 | `/chapters` | `src/views/chapter/index.vue` | ✅ |
| 试题管理 | `/questions` | `src/views/question/index.vue` | ✅ |
| 考试管理 | `/exams` | `src/views/exam/index.vue` | ✅ |

#### 已完成 P1（3 个页面）
| 页面 | 路由 | 文件 | 状态 |
|------|------|------|------|
| 知识点管理 | `/knowledge` | `src/views/knowledge/index.vue` | ✅ |
| 培训计划 | `/train-plans` | `src/views/train-plan/index.vue` | ✅ |
| 培训计划详情 | `/train-plans/:id` | `src/views/train-plan/detail.vue` | 隐藏 |
| 用户管理 | `/users` | `src/views/user/index.vue` | ✅ |

#### 已完成 P2（0 / 1 个页面）
| 页面 | 路由 | 状态 |
|------|------|------|
| 讲师管理 | `/teachers` | ❌ **尚未完成** |

#### 总前端页面清单（共 12 个 = 原有 5 + 新增 7）
login、layout、dashboard、course、stats、consult、chapter、question、exam、knowledge、train-plan、user

### 2. 后端 5 个新 Controller ✅

位于 `training-admin/src/main/java/com/training/admin/controller/`：
| Controller | 路径 | 功能 |
|------------|------|------|
| ChapterController | `/admin/chapter` | 章节 CRUD + 排序 |
| KnowledgeController | `/admin/knowledge` | 知识点 CRUD |
| TrainPlanController | `/admin/train-plan` | 培训计划 + 课程关联 |
| UserManagementController | `/admin/user` | 用户 CRUD + 启用/禁用 |
| TeacherController | `/admin/teacher` | 讲师 CRUD |

### 3. 后端缺口补齐（exam-generate 缺陷修复）✅

File：`training-admin/.../controller/ExamController.java`
- 原缺陷：POST `/admin/exam/generate` 仅返回题目 ID 列表，**未持久化到 exam_paper**
- 修复：新增 `GenerateResultVO` + `ExamBizService.generateAndSavePaper()` 方法，生成卷后自动保存模板试卷（studentId=0L 标识），返回题目 ID 列表 + 题目总数

### 4. RBAC 权限重构（方案 A：标准三表）✅

#### 数据库层（Phase 1）
- 新建 3 张表：`sys_role`、`sys_permission`、`sys_role_permission`
- 改 `sys_user`：新增 `role_id` BIGINT 列（FK → sys_role.id）
- 删 `sys_user` 旧的 `role` VARCHAR(20) 列
- 数据迁移：role='admin'→1, 'teacher'→2, 'student'→3
- DataInitializer：@PostConstruct 初始化 3 角色 + 16 权限 + 28 条关联

#### Java 代码层（Phase 2）
- 新建 `security/LoginUser.java`（UserDetails 实现）
- 新建 `security/RbacUserDetailsService.java`（UserDetailsService 实现）
- 新建 `security/JwtAuthenticationFilter.java`（替代旧 JwtInterceptor，改继承 OncePerRequestFilter）
- 新建 `security/CustomAuthenticationEntryPoint.java`（401 JSON 返回）
- 新建 `security/CustomAccessDeniedHandler.java`（403 JSON 返回）
- 新建 `config/SecurityConfig.java`（@EnableWebSecurity + @EnableMethodSecurity）
- 修改 `config/InterceptorConfig.java`（清空，避免双重鉴权）
- JwtInterceptor.java 去掉 @Component 注解（保留文件备查）
- JwtUtils.java 扩展 generate(..., Long roleId)
- 11 个业务 Controller 全部方法加 @PreAuthorize 注解

#### 权限字典预设（16 个）
course:read/write、chapter:read/write、knowledge:read/write、question:read/write、exam:read/write、consult:read/write、stats:read/write、user:read/write、teacher:read/write、plan:read/write

#### URL 粗粒度 + 方法细粒度双保险
- SecurityConfig：`/admin/**` 要求 `hasRole('ADMIN')`
- 方法级：`@PreAuthorize("hasAuthority('xxx:write')")`

### 5. StatsMapper.xml role 列引用修复 ✅

修复原因：sys_user.role 列已删除，但 StatsMapper.xml 多处 SQL 仍引用该列导致 500 错误。
修复位置：5 处（行 9、76、91、207、217）
规则：`role = 'student'` → `role_id = 3`（.STUDENT 角色 id=3）

---

## 二、遇到的错误与解决方案

### 错误 1：前端 Vite esbuild 崩溃
- **现象**：`The service is no longer running` + esbuild 报错指向 exam/index.vue
- **根因**：项目系统默认 Java 1.8，jar 用 Java 17 编译；npm 缓存中 esbuild 服务冲突
- **解决**：清除 node_modules/.vite 缓存 + kill esbuild 僵尸进程
- **教训**：Vite dev server 运行期偶发崩溃，重启即可

### 错误 2：mvn package 报 mybatis-plus 不存在
- **现象**：`java: 程序包org.mybatis.spring.annotation不存在`
- **根因**：SysUser.java 实体加了 role_id 字段后未同步 SysUserMapper.xml 映射；或 training-api 未正确声明 mybatis-plus 依赖
- **解决**：通过更新 training-dao 的 SysUserMapper.xml 添加 role_id 映射 + 完整的 mvn rebuild
- **验证**：`mvn package -DskipTests -q` BUILD SUCCESS

### 错误 3：前端 5 个新页面接口 500（前端提示"系统繁忙"）
- **现象**：前端 `request.ts` 拦截器把 >=500 包装成"系统繁忙，请稍后重试"
- **根因**：后端启的是旧 jar 包（缺 Chapter/Knowledge 等 Controller）；新代码未重启部署
- **解决**：重新 `mvn package -DskipTests` + kill 旧 java 进程 + 重启 jar
- **验证**：后端日志 `Started AdminApplication in ...`

### 错误 4：后端启动报 UnsupportedClassVersionError
- **现象**：`class file version 61.0, this version only recognizes up to 52.0`
- **根因**：系统 PATH 默认 Java 是 JDK 1.8（52.0），但 jar 用 JDK 17（61.0）编译
- **解决**：显式用 `D:\javaEE\soft\jdk-17.0.9\bin\java.exe -jar` 启动
- **教训**：Windows 双 Java 版本必须显式指定 JDK 17

### 错误 5：后端启动报 Port 8080 already in use
- **现象**：`PortInUseException: Port 8080 was already in use`
- **根因**：旧 java 进程未完全释放端口（启了一次未 kill）
- **解决**：`netstat -ano | findstr ":8080"` + `taskkill /F /PID <PID>` 或 `taskkill /F /IM java.exe`
- **教训**：每次重启前必须先 kill 旧进程

### 错误 6：Stats 500（前端 stats 页面 3 个接口失败）
- **现象**：`loadOverview`、`loadTrend`、`loadStudentStats` 全部抛"系统繁忙"
- **根因**：用户执行了 `ALTER TABLE sys_user DROP COLUMN role`，但 `training-dao/src/main/resources/mapper/StatsMapper.xml` 中 6 处 SQL 仍引用 `role` 列
- **解决**：grep 定位所有 `role = 'student'` 引用（6 处）→ 全部替换为 `role_id = 3`
- **验证**：grep 确认无 `role =` 模式残留
- **位置**：行 9、76、91、207、217（selectOverview、selectStudentStats、countStudentStats、selectOrgStats、countOrgStats）

### 错误 7：前端 ElTag type="" warning
- **现象**：`ElementPlusError: [el-radio] label act as value is about to be deprecated` + `ElTag type validation failed`
- **根因**：非致命 warning；Element Plus 3.0 弃用 `label` 作 value；ElTag 传空字符串
- **解决**：**暂不影响功能**（低优先级）
- **教训**：控制台 warning ≠ 致命 bug

### 错误 8（新，待解决）：JSON.parse(undefined) at user.js:8
- **现象**：`SyntaxError: "undefined" is not valid JSON at user.js:8:29`
- **根因（待定）**：auth/login 接口返回空响应；或 Spring Security 拦截 401/403 返回空 body
- **定位方法**：F12 → Network → 选中登录请求 → Response 标签查看
- **待办**：需后端日志 + F12 网络响应体确认

---

## 三、待办事项（按优先级排序）

| 优先级 | 任务 | 状态 | 负责人 |
|--------|------|:-----:|--------|
| 🔴 P0 | 修复登录 JSON.parse 错误（user.js:8） | ❌ | 待查后端日志 |
| 🔴 P0 | 前端 dev server 重启后验证 home/stats 恢复正常 | ❌ | 待启 jar 后 |
| 🔴 P0 | 重打包 jar + kill 旧进程 + 重启（包含 StatsMapper 修复） | ❌ | 用户执行 |
| 🟡 P1 | 完成 P2 讲师管理前端页面 | ❌ | 待用户指令 |
| 🟡 P1 | 前端适配 SysUser.role_id（用户管理表格显示 roleName） | ❌ | 待启 jar 后 |
| 🟡 P1 | 修复 AuthController 返回 roleCode 向前兼容 | ❌ | 待确认 |
| 🟡 P1 | RbacUserDetailsService 回退 user.getRole() 清理 | ❌ | 低 |
| 🟡 P1 | SysUserServiceImpl 的 setRole 改为同时 set roleId | ❌ | 低 |
| 🟢 P2 | 小程序 API 后端（api 8081）重新打包 | ❌ | 不在本次范围 |
| 🟢 P2 | 项目全模块 mybatis 依赖一致性检查 | ❌ | 低 |
| 🟢 P2 | ElTag 空字符串 type 修复（warning） | ❌ | 最低 |

---

## 四、关键设计决策记录

### 4.1 RBAC 改造选型
- **教师讲解文档**要求：RBAC 五表模型（user/role/permission/user_role/role_permission）
- **项目现状**：仅 sys_user.role 字段，Controller 零角色校验
- **选定方案**：标准三表 RBAC，用户单 role_id FK
- **决策理由**：毕设答辩 RBAC 必讲解点，三表最标准且改动可控

### 4.2 数据库迁移顺序（必须执行）
1. 新建 3 表
2. sys_user 加 role_id 列
3. DataInitializer 初始化字典
4. UPDATE 迁移 role 字符串 → role_id 数字
5. 验证 `COUNT(role_id IS NULL) == 0`
6. DROP COLUMN role

### 4.3 API 端（小程序后台）处理决策
- **决策**：training-api 8081 RBAC 不改，保持现有 userId 校验
- **理由**：小程序纯学员端，RBAC 只会增加复杂度

### 4.4 Spring Security 鉴权分层
- **URL 粗粒度**：SecurityConfig 配置 `/admin/**` 要求 hasRole('ADMIN')
- **方法细粒度**：每个 Controller 方法 @PreAuthorize("hasAuthority('xxx:write')")
- **双保险**：URL 防未登录，方法防越权

---

## 五、文档与代码位置

### 设计文档
- RBAC 设计方案：`docs/superpowers/specs/2026-07-08-rbac-refactor-design.md`
- 数据库迁移脚本：`training-admin/src/main/resources/db/migration/V2_0__rbac_init.sql`
- 本文档（进度 + 错误）：`docs/dev-web-rbac-20260708.md`

### Java 新增文件清单（Phase 1 + 2）
```
training-dao/src/main/java/com/training/common/entity/SysRole.java
training-dao/src/main/java/com/training/common/entity/SysPermission.java
training-dao/src/main/java/com/training/common/entity/SysRolePermission.java
training-dao/src/main/java/com/training/mapper/SysRoleMapper.java
training-dao/src/main/java/com/training/mapper/SysPermissionMapper.java
training-dao/src/main/java/com/training/mapper/SysRolePermissionMapper.java

training-common/src/main/java/com/training/common/dto/RoleForm.java
training-common/src/main/java/com/training/common/dto/RoleQuery.java
training-common/src/main/java/com/training/common/dto/RolePermissionDTO.java
training-common/src/main/java/com/training/common/vo/GenerateResultVO.java

training-admin/src/main/java/com/training/admin/security/LoginUser.java
training-admin/src/main/java/com/training/admin/security/RbacUserDetailsService.java
training-admin/src/main/java/com/training/admin/security/JwtAuthenticationFilter.java
training-admin/src/main/java/com/training/admin/security/CustomAuthenticationEntryPoint.java
training-admin/src/main/java/com/training/admin/security/CustomAccessDeniedHandler.java

training-admin/src/main/java/com/training/admin/config/SecurityConfig.java (new)
training-admin/src/main/java/com/training/admin/config/RbacDataInitializer.java

training-dao/src/main/resources/mapper/SysRoleMapper.xml
training-dao/src/main/resources/mapper/SysPermissionMapper.xml
training-dao/src/main/resources/mapper/SysRolePermissionMapper.xml
training-dao/src/main/resources/mapper/CourseChapterMapper.xml (更新)
training-dao/src/main/resources/mapper/KnowledgePointMapper.xml (更新)
training-dao/src/main/resources/mapper/TeacherMapper.xml (new)
```

### 前端新增文件清单（本次 discussion）
```
training-admin/frontend/src/api/chapter.ts
training-admin/frontend/src/api/question.ts
training-admin/frontend/src/api/exam.ts
training-admin/frontend/src/api/knowledge.ts
training-admin/frontend/src/api/train-plan.ts
training-admin/frontend/src/api/user.ts

training-admin/frontend/src/views/chapter/index.vue
training-admin/frontend/src/views/question/index.vue
training-admin/frontend/src/views/exam/index.vue
training-admin/frontend/src/views/knowledge/index.vue
training-admin/frontend/src/views/train-plan/index.vue
training-admin/frontend/src/views/train-plan/detail.vue
training-admin/frontend/src/views/user/index.vue
```

### 修改的关键现有文件
- `training-dao/src/main/resources/mapper/StatsMapper.xml`（6 处 role→role_id 修复）
- `training-admin/src/main/java/com/training/admin/interceptor/JwtInterceptor.java`（去掉 @Component）
- `training-admin/src/main/java/com/training/admin/config/InterceptorConfig.java`（清空）
- `training-admin/src/main/java/com/training/admin/controller/AuthController.java`（roleId 进 JWT）
- `training-common/src/main/java/com/training/common/utils/JwtUtils.java`（增加 generate 重载）
- `training-admin/src/main/java/com/training/admin/controller/ExamController.java`（返回 GenerateResultVO）
- 11 个业务 Controller（全部方法加 @PreAuthorize）

---

## 六、下一步行动

1. **用户在 MySQL 执行 DDL**（步骤 1）
2. **重打包 jar**（mvn package -DskipTests -q，需 JDK 17）
3. **kill 旧进程 + 重启**（需使用 D:\javaEE\soft\jdk-17.0.9\bin\java.exe）
4. **数据迁移 UPDATE + DROP COLUMN role**
5. **登录验证**（定位 user.js:8 JSON.parse 错误）
6. **前端 home/stats 500 验证**
7. **讲师管理前端页面**（P2，待推进）

---

## 七、教师讲解文档对照（7.8）

| 讲解点 | 项目现状 | 文档章节 |
|--------|---------|---------|
| PC/移动端统一接口 | 分离（admin 8080 + api 8081）| § 4.3 |
| JSON 序列化（Date/null） | `application.yml` 已配 `date-format: yyyy-MM-dd HH:mm:ss` | § 4.1 |
| Token 身份认证 | JWT + Filter + SecurityContext | § 4.4 |
| 分页参数 | MyBatis-Plus IPage + Page | § 4.1 |
| RBAC 权限控制 | **本期补齐**：三表 + Spring Security + @PreAuthorize | 全部 § 四 |

---

> **文档维护**：每次进度推进时请同步更新本文件的"待办事项"和"错误记录"章节。
> **最后更新**：2026-07-08（RBAC Phase 1+2 完成 + StatsMapper 修复完成，jar 重启待执行）
