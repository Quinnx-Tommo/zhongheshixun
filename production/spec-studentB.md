# Spec: 学生乙 — 课程与学习模块完善

> **来源**：`02-学生乙-课程与学习.md` 分工 + `docs/跨端联动审计报告.md` 遗留 + 完成度检查
> **日期**：2026-07-14
> **范围**：T2–T9（T1 离线下载已移除）
> **状态**：SPEC（待评审）

---

## 一、Objective

补齐学生乙负责的「课程与学习」模块在分工清单、跨端审计、docx 强制要求三方面的缺口，使模块达到答辩可演示、审计清单全绿、文档与代码零偏差。

**用户故事**：
- 作为管理员，我希望在统计报表看到「平台运行情况（在线人数/并发考试）」，以体现 docx 高并发亮点。
- 作为学员，我希望看到「推荐课程」，快速发现热门内容。
- 作为学员，我希望点「学完本章」时进度正确标记完成。
- 作为答辩老师，我希望学习页不能通过 URL 直跳绕过报名。

---

## 二、Tech Stack（沿用项目现有，不新增）

| 层 | 技术 | 版本 |
|----|------|------|
| 后端 | Spring Boot | 2.7.18 |
| ORM | MyBatis-Plus | 3.5.3 |
| DB | MySQL | 8.0 |
| 认证 | JWT（jjwt 0.11.5） | — |
| 前端 | Vue3 + Element Plus | 3.x |
| 缓存 | **不使用 Redis**（运行环境未部署） | — |

> ⚠️ T2 在线人数采用纯 SQL 方案，不引入 Redis。

---

## 三、Commands

| 用途 | 命令 |
|------|------|
| 后端 admin 构建 | `mvn -pl training-admin -am package -DskipTests`（产物 training-admin-exec.jar，端口 9898） |
| 后端 api 构建 | `mvn -pl training-api -am package -DskipTests`（产物 training-api-exec.jar，端口 9899） |
| 前端 web-student 构建 | `cd web-student && npm run build`（端口 5174 开发 / dist 产物） |
| 接口验证 | `curl` + admin/student token |
| 浏览器 E2E | 手动 + webapp-testing skill |

---

## 四、Project Structure（本次涉及文件）

```
training-parent/
├── training-admin/src/main/java/com/training/admin/controller/
│   └── StatsController.java              ← T2 加 /platform
├── training-api/src/main/java/com/training/api/controller/
│   ├── CourseApiController.java          ← T3 加 /recommend
│   └── StudyApiController.java           ← T4 加 /complete-chapter
├── training-service/src/main/java/com/training/service/
│   ├── StatsService.java                 ← T2 加 platform()
│   ├── StudyService.java                 ← T4 加 completeChapter()
│   ├── CourseService.java                ← T3 加 recommend()
│   └── impl/
│       ├── StatsServiceImpl.java         ← T2 实现
│       ├── StudyServiceImpl.java         ← T4 实现 + T8 上限校验
│       └── CourseServiceImpl.java        ← T3 实现
├── training-dao/src/main/java/com/training/mapper/
│   ├── StatsMapper.java                  ← T2 加 SQL 方法
│   ├── CourseMapper.java                 ← T3 加 recommend 查询
│   └── resources/mapper/StatsMapper.xml  ← T2 SQL
└── training-common/src/main/java/com/training/common/vo/
    └── PlatformStatVO.java               ← T2 新建返回对象

web-student/src/views/course/
├── learn.vue                              ← T7 加报名校验
└── detail.vue                             ← T6 回归验证
```

---

## 五、Code Style（遵循现有约定）

- Controller 统一返回 `Result<T>` / `PageResult<T>`
- 权限注解：后台用 `@PreAuthorize("hasAuthority('xxx:read')")`
- 学员侧接口用 `@RequestAttribute("userId") Long userId` 取登录用户
- MyBatis XML：`IFNULL` 处理空值，`deleted = 0` 过滤逻辑删除，`&gt;` / `&lt;` 转义
- 字段命名：DB 下划线 ↔ Java 驼峰（`map-underscore-to-camel-case: true`）
- 前端：Element Plus 组件 + `<script setup>` + axios api 封装

---

## 六、Testing Strategy

| 层级 | 方法 | 工具 |
|------|------|------|
| 后端单元 | 现有无单测框架，不新增 | — |
| 接口验证 | curl 跑每个新端点（admin/student token） | curl |
| 构建 | `mvn package` + `npm run build` 通过 | Maven/npm |
| E2E | 浏览器手动跑审计 11 步清单 + webapp-testing | 浏览器 |
| 回归 | T5/T6 验证 v3.3 修复仍生效 | 浏览器 + DB 查询 |

---

## 七、Boundaries

- **Always**：改完即编译；接口遵循现有 Result 包装；前端改完跑 build；不动 v3.3 已修复的进度保存核心分支
- **Ask first**：新建数据库表/列；引入新依赖；改 application.yml
- **Never**：引入 Redis；重写 StudyServiceImpl 进度计算为查 chapter.duration；拆回 publish/unpublish；提交含硬编码密码/调试残留

---

## 八、Success Criteria（T2–T9 验收标准）

### 🔴 T2 — `/admin/stats/platform` 接口

```
Given 管理员已登录（持有 stats:read 权限的 token）
When  GET /admin/stats/platform
Then  返回 200，body:
      {
        "code": 200,
        "data": {
          "onlineCount": <近5分钟有 study_record 上报的去重学员数>,
          "todayStudyCount": <今日活跃学员数>,
          "concurrentExamCount": <exam_record.status=0 进行中的数量>,
          "totalOnlineToday": <今日登录用户数，若 sys_user 无登录字段则同 todayStudyCount>
        }
      }
And   无 stats:read 权限的账号调用返回 403
And   接口已加入 StatsController，遵循 @PreAuthorize("hasAuthority('stats:read')")
```

**SQL 口径**：
- onlineCount = `COUNT(DISTINCT student_id) FROM study_record WHERE deleted=0 AND update_time >= NOW()-INTERVAL 5 MINUTE`
- todayStudyCount = `COUNT(DISTINCT student_id) FROM study_record WHERE deleted=0 AND update_time >= CURDATE()`
- concurrentExamCount = `COUNT(*) FROM exam_record WHERE deleted=0 AND status=0`

### 🟡 T3 — `/api/course/recommend` 推荐接口

```
Given 任意访问者（公开接口，无需登录）
When  GET /api/course/recommend?limit=5
Then  返回 200，body.data 为 List<Course>，按报名数降序
      仅含 status=1（已发布）课程
      limit 缺省 5，上限 20
And   无报名数据的课程排在最后（enroll_count=0）
And   返回字段与 /api/course/list 一致（含 teacherName 回填）
```

### 🟡 T4 — `/api/study/complete-chapter` 端点

```
Given 学员已登录（持有 token）
When  POST /api/study/complete-chapter
      body: {"courseId": 1, "chapterId": 2}
Then  返回 200
And   study_record 表中 (student_id, course_id, chapter_id) 记录
      progress=100, completed=1
And   实现复用 reportProgress（传入 completed=true, progress=100），不重复造逻辑
And   未登录调用返回 401
```

### 🟡 T5 — 回归验证：章节字段修复（v3.3 P0-1）

```
Given study_record 有数据，student01 已报名课程1
When  登录 student01 → 进入课程1学习页（/courses/1/learn）
Then  章节列表正常渲染（视频/PDF/文本任一类型）
And   每个章节显示 contentType（1视频/2PDF/3文本）与 content
And   控制台无 "Cannot read property of undefined" 报错
```

### 🟡 T6 — 回归验证：detail.vue finished 显示（v3.3 P1-6）

```
Given study_record 存在 completed=1 的章节
When  学员打开课程详情页
Then  已完成章节显示「已学完成」标签
And   未完成章节不显示该标签
And   course.detail VO 返回的 chapters 含 completed 字段，前端正确 merge
```

### 🟡 T7 — learn.vue 报名状态校验

```
Given student01 未报名课程2
When  直接访问 /courses/2/learn
Then  onMounted 调用 /api/study/check-enrolled?courseId=2
And   返回 false → 跳转回 /courses/2（详情页）并提示「请先报名」
And   已报名则正常加载学习页
```

### 🟢 T8 — StudyServiceImpl 进度上限校验（最小改）

```
Given 前端上报 progress=150（异常/恶意）
When  POST /api/study/progress
Then  study_record.progress 被钳制为 100
And   StudyServiceImpl.reportProgress 仅增加 1 行：if(progress>100) progress=100
And   不查 chapter 表，不改 completed 阈值（保持 >=100）
And   不影响 v3.3 已修复的 finished/completed 字段对齐
```

### 🟢 T9 — publish 合并口径（不改码）

```
Given 答辩老师问"为什么 publish/unpublish 合并为一个接口"
Then  产出答辩口径卡（1 段话）：
      "通过 status 参数（1发布/2下架）复用同一 PUT /publish 端点，
       符合 REST 资源状态转移语义，减少重复代码。
       与拆分为 /publish + /unpublish 两个端点功能等价。"
And   代码不改
```

---

## 九、Assumptions（阶段2设计时验证）

1. `study_record.update_time` 存在且 `ON UPDATE CURRENT_TIMESTAMP` ✅ 已验证（database.sql:211）
2. `exam_record.status` 取值：0进行中/1已提交/2已批阅 ✅ 已验证（database.sql:284）
3. `course_enroll.student_id` + `course_id` 字段存在 ✅ 已验证
4. `sys_user` 无 `last_login_time` 字段 → T2 的 totalOnlineToday 降级用 todayStudyCount 口径（阶段2确认）
5. CourseApiController 为公开接口（无需登录）→ T3 recommend 也公开 ✅ 已验证（注释 line 29）
6. StudyApiController 用 `@RequestAttribute("userId")` → T4/T7 需登录 ✅ 已验证
7. `CourseMapper` 有 `selectEnrolledCourses`（已用于 my-courses）→ T3 可加 `selectRecommend` 方法（阶段2确认签名）
8. web-student 已有 `/api/study/check-enrolled` 前端封装 → T7 直接调用（阶段2确认 api/study.js）

---

## 十、Open Questions

1. T2 的 `totalOnlineToday` 字段是否保留？若 sys_user 无登录时间字段，建议改为只返回 3 个核心字段（onlineCount/todayStudyCount/concurrentExamCount），避免凑数。（倾向：精简为 3 字段）
2. T3 recommend 是否需要排除学员已报名的课程？分工文档未要求，倾向不排除（公开热门榜）。
3. T7 跳转提示用 ElMessage 还是详情页内 banner？（倾向 ElMessage.warning）

---

## 十一、验收门禁（全部通过才算完成）

- [x] T2 platform 接口 curl 返回 3 字段且 SQL 口径正确
- [x] T3 recommend 接口 curl 返回按报名数降序列表
- [x] T4 complete-chapter 接口 curl 后 DB study_record completed=1
- [x] T5 learn.vue 章节渲染正常（浏览器验证）← e2e-test-11steps-report.md 步骤6
- [x] T6 detail.vue 已完成标签显示（浏览器验证）← e2e-test-11steps-report.md 步骤10
- [x] T7 未报名访问 learn 被拦截跳转（浏览器验证）← V5.5 + 步骤5报名校验
- [x] T8 progress=150 被钳制为 100（curl + DB 验证）
- [x] T9 答辩口径卡产出
- [x] `mvn package` 两个 jar 构建通过
- [x] `npm run build` 通过
- [x] 审计报告 §十 11 步测试清单全绿 ← `production/e2e-test-11steps-report.md`（2026-07-14，11 步全 PASS）
- [x] docs/dev-api.md + 进度文档同步更新
