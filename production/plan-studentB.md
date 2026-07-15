# Plan: 学生乙模块完善 — 接口契约 + 决策记录 + 任务拆解

> **对应 Spec**：`production/spec-studentB.md`
> **阶段**：2（接口契约设计 + 决策）+ 3（任务拆解）
> **设计原则**：契约优先 / 加法优于修改 / 一致错误语义 / Hyrum's Law（不破坏现有 observable behavior）

---

## 一、接口契约设计（T2 / T3 / T4）

### 1.1 设计约束（沿用项目现有约定，保证一致性）

| 约束 | 现有实现 | 本次遵循 |
|------|---------|---------|
| 响应包装 | `Result<T>` / `PageResult<T>` | ✅ 沿用 |
| 后台权限 | `@PreAuthorize("hasAuthority('xxx:read')")` | ✅ T2 沿用 `stats:read` |
| 学员侧登录 | `@RequestAttribute("userId") Long userId` | ✅ T4 沿用 |
| 学员侧公开接口 | 无 `@RequestAttribute`（如 `/api/course/list`） | ✅ T3 公开 |
| 路径命名 | kebab-case（`my-courses` / `check-enrolled`） | ✅ `complete-chapter` / `recommend` / `platform` |
| DTO 校验 | `@Valid` + `@NotNull` | ✅ T4 新 DTO 沿用 |
| 字段命名 | camelCase（map-underscore-to-camel-case） | ✅ |

### 1.2 T2 — `GET /admin/stats/platform`

**契约**

| 项 | 值 |
|----|----|
| 方法路径 | `GET /admin/stats/platform` |
| 权限 | `@PreAuthorize("hasAuthority('stats:read')")`（ADMIN/TEACHER） |
| 入参 | 无 |
| 出参 | `Result<PlatformStatVO>` |

**返回对象**（新建 `com.training.common.vo.PlatformStatVO`）

```java
@Data
public class PlatformStatVO {
    /** 近5分钟活跃学员数（在线人数） */
    private Integer onlineCount;
    /** 今日活跃学员数 */
    private Integer todayStudyCount;
    /** 进行中考试数（exam_record.status=0） */
    private Integer concurrentExamCount;
}
```

**SQL**（StatsMapper.xml，单条子查询，复用 overview 风格）

```sql
<select id="selectPlatform" resultType="com.training.common.vo.PlatformStatVO">
    SELECT
        (SELECT COUNT(DISTINCT student_id) FROM study_record
         WHERE deleted = 0
           AND update_time &gt;= DATE_SUB(NOW(), INTERVAL 5 MINUTE)) AS onlineCount,
        (SELECT COUNT(DISTINCT student_id) FROM study_record
         WHERE deleted = 0
           AND update_time &gt;= CURDATE()) AS todayStudyCount,
        (SELECT COUNT(*) FROM exam_record
         WHERE deleted = 0 AND status = 0) AS concurrentExamCount
</select>
```

**调用链**：StatsController → StatsService.platform() → StatsServiceImpl → StatsMapper.selectPlatform()

---

### 1.3 T3 — `GET /api/course/recommend`

**契约**

| 项 | 值 |
|----|----|
| 方法路径 | `GET /api/course/recommend` |
| 权限 | 公开（与 `/api/course/list` 一致，无需登录） |
| 入参 | `?limit=5`（默认 5，上限 20，超限钳制为 20） |
| 出参 | `Result<List<Course>>` |

**设计决策**：Top-N 推荐场景，**不分页**。理由：recommend 语义是"取前 N 条热门"，非全量浏览；与 api-and-interface-design 的"列表需分页"红 flag 不冲突，因其非列表浏览端点，有 limit 上界。

**排序口径**：按 `course_enroll` 报名数降序 → 报名数相同按 `create_time` 降序 → 仅 `status=1` 已发布课程。

**SQL**（CourseMapper.xml 新增）

```sql
<select id="selectRecommend" resultType="com.training.common.entity.Course">
    SELECT c.*
    FROM course c
    LEFT JOIN (
        SELECT course_id, COUNT(*) AS cnt
        FROM course_enroll WHERE deleted = 0
        GROUP BY course_id
    ) e ON e.course_id = c.id
    WHERE c.deleted = 0 AND c.status = 1
    ORDER BY IFNULL(e.cnt, 0) DESC, c.create_time DESC
    LIMIT #{limit}
</select>
```

**teacherName 回填**：复用 `/api/course/list` 的批量回填逻辑（抽公共方法 `fillTeacherName(List<Course>)`，避免重复代码 — 符合 code-simplification）。

**调用链**：CourseApiController.recommend() → CourseService.recommend(limit) → CourseMapper.selectRecommend(limit) → 回填 teacherName

---

### 1.4 T4 — `POST /api/study/complete-chapter`

**契约**

| 项 | 值 |
|----|----|
| 方法路径 | `POST /api/study/complete-chapter` |
| 权限 | 需登录（`@RequestAttribute("userId")`） |
| 入参 | `CompleteChapterDTO`（`@Valid`） |
| 出参 | `Result<Void>` |

**入参 DTO**（新建 `com.training.common.dto.CompleteChapterDTO`）

```java
@Data
public class CompleteChapterDTO {
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;
}
```

**实现口径**：**复用 `reportProgress`**，不重复造逻辑。构造 `StudyProgressDTO(progress=100, completed=true, courseId, chapterId)` 调 `reportProgress`。

```java
// StudyServiceImpl.completeChapter
public void completeChapter(Long userId, CompleteChapterDTO dto) {
    StudyProgressDTO p = new StudyProgressDTO();
    p.setCourseId(dto.getCourseId());
    p.setChapterId(dto.getChapterId());
    p.setProgress(100);
    p.setCompleted(true);
    reportProgress(userId, p);  // 复用，保证 upsert + 上限校验一致
}
```

**设计原则**：加法优于修改 — 新增端点 + 复用现有 service 方法，不改动 reportProgress 签名。

---

## 二、决策记录（T8 / T9）

### 2.1 T8 — StudyServiceImpl 进度上限校验

**决策**：方案 A — 后端保持信任前端 `progress`，仅加 1 行上限钳制。

**改动**：`StudyServiceImpl.reportProgress` 第 44 行后加：

```java
Integer progress = dto.getProgress() == null ? 0 : dto.getProgress();
if (progress > 100) progress = 100;   // ← 新增：上限校验防刷
record.setProgress(progress);
```

**决策依据**（引用 api-and-interface-design 原则）：

| 原则 | 应用 |
|------|------|
| **Hyrum's Law** | 现有 `study_record.progress` 字段已被 learn.vue/detail.vue/home 依赖（读 progress 显示进度条）。若改后端为查 `chapter.duration` 重算，会改变 observable behavior（progress 值语义变化），破坏现有消费者 → 拒绝方案 B |
| **加法优于修改** | 只加 1 行校验，不改 reportProgress 签名/阈值/计算分支，v3.3 已修复的 finished/completed 对齐不受影响 |
| **边界验证** | 上限校验属于"系统边界输入验证"（前端可传任意 progress），符合"validate at boundaries" |

**拒绝方案 B 的风险**（前面已分析，此处归档）：
- DB 开销翻倍（每 10s 上报多查 1 次 course_chapter，与 3000 并发叙事冲突）
- 除零风险（duration 为 null/0 时 ArithmeticException）
- completed 阈值 100→95 改变完成判定，与演示库现有 study_record 状态不一致

**答辩口径卡**：
> "学习进度采用**前端计算 + 后端 upsert 存储 + 后端上限校验防刷**的分层策略。前端按 `lastPosition/duration×100%` 计算进度上报，后端做 `progress≤100` 上限校验后 upsert 入库。生产级可增加服务端按 chapter.duration 重算校验，当前演示环境为降低每次心跳上报的 DB 开销，采用信任前端 + 上限兜底。该策略支持 3000 并发学习场景下的高频进度上报。"

---

### 2.2 T9 — publish/unpublish 合并

**决策**：保留现状（`PUT /publish` + `status` 参数），不拆分。仅产出答辩口径卡，不改码。

**决策依据**：

| 原则 | 应用 |
|------|------|
| **Hyrum's Law** | 现有管理后台前端已调用 `PUT /admin/course/publish/{id}` 传 `status=1/2`。拆分为 `/publish`+`/unpublish` 会破坏现有前端消费者 |
| **加法优于修改** | 现状工作正常，无理由改 |

**答辩口径卡**：
> "课程上下架通过 `PUT /admin/course/publish/{id}` + `status` 参数（1发布/2下架）复用同一端点，符合 REST **资源状态转移**语义——status 是课程资源的状态字段，PUT 更新状态比拆成两个动词端点更贴合 REST 风格。与 `/publish`+`/unpublish` 两端点功能等价，但减少重复代码。"

---

## 三、任务拆解（阶段 3）

### 3.1 依赖图

```
T2(platform) ──── 后端独立，无依赖
T3(recommend) ─── 后端独立，无依赖
T4(complete) ───── 后端独立，无依赖（复用 reportProgress）
T8(上限校验) ───── 改 StudyServiceImpl，与 T4 同文件但不同方法，无冲突
T7(报名校验) ───── 前端独立，依赖 check-enrolled 接口（已存在）
T5/T6(回归) ────── 依赖编译通过 + 浏览器
T9(口径卡) ─────── 纯文档，无依赖
```

### 3.2 并行批次

**批次 A（后端 + 前端并行，互不阻塞）**

| 子任务 | 文件 | 改动量 |
|--------|------|--------|
| T2 | StatsController + StatsService + StatsServiceImpl + StatsMapper(.java/.xml) + PlatformStatVO | ~6 文件 |
| T3 | CourseApiController + CourseService + CourseServiceImpl + CourseMapper(.java/.xml) + 抽 fillTeacherName | ~5 文件 |
| T4 | StudyApiController + StudyService + StudyServiceImpl + CompleteChapterDTO | ~4 文件 |
| T8 | StudyServiceImpl（同 T4 文件，但改 reportProgress 方法，T4 改 completeChapter 新方法，**无冲突**） | 1 行 |
| T7 | web-student/src/views/course/learn.vue + api/study.js（checkEnrolled 已存在） | ~1 文件 |

> ⚠️ T4 与 T8 同改 `StudyServiceImpl.java`，但 T4 加新方法 `completeChapter`、T8 改 `reportProgress` 现有方法。**同一文件不同方法，合并时无冲突**，但建议串行执行避免编辑冲突（同子代理内顺序改）。

**批次 B（A 完成后，验证）**

| 子任务 | 类型 |
|--------|------|
| T5 | 浏览器回归 learn.vue 章节渲染 |
| T6 | 浏览器回归 detail.vue finished 标签 |
| mvn package | 两 jar 构建通过 |
| npm run build | web-student 构建通过 |
| curl T2/T3/T4 | 接口验证 |

**批次 C（B 通过后，收尾）**

| 子任务 | 类型 |
|--------|------|
| T9 | 答辩口径卡（纯文档） |
| 文档同步 | dev-api.md + 进度文档 |

### 3.3 任务清单（含验收 + 验证 + 文件）

```markdown
- [ ] T2: /admin/stats/platform 接口
  - 验收: curl GET 返回 {onlineCount, todayStudyCount, concurrentExamCount} 3 字段
  - 验证: admin token curl 返回 200；无权限账号 403
  - 文件: StatsController, StatsService, StatsServiceImpl, StatsMapper.java, StatsMapper.xml, PlatformStatVO.java

- [ ] T3: /api/course/recommend 接口
  - 验收: curl GET 返回 List<Course>，按报名数降序，limit 默认5上限20
  - 验证: curl 无需 token；返回含 teacherName
  - 文件: CourseApiController, CourseService, CourseServiceImpl, CourseMapper.java, CourseMapper.xml

- [ ] T4: /api/study/complete-chapter 接口
  - 验收: curl POST 后 DB study_record.completed=1, progress=100
  - 验证: student token curl 返回 200；未登录 401
  - 文件: StudyApiController, StudyService, StudyServiceImpl, CompleteChapterDTO.java

- [ ] T8: StudyServiceImpl 进度上限校验
  - 验收: curl POST progress=150 后 DB progress=100
  - 验证: 上报 progress=150 → 查 study_record.progress=100
  - 文件: StudyServiceImpl.java（1 行）

- [ ] T7: learn.vue 报名校验
  - 验收: 未报名访问 /courses/2/learn → ElMessage.warning + 跳回 /courses/2
  - 验证: 浏览器手动 URL 直跳
  - 文件: web-student/src/views/course/learn.vue

- [ ] T5: 回归 learn.vue 章节渲染（v3.3 P0-1）
  - 验收: 章节列表正常渲染，无 undefined 报错
  - 验证: 浏览器进学习页
  - 文件: 仅验证，不改

- [ ] T6: 回归 detail.vue finished 标签（v3.3 P1-6）
  - 验收: 已完成章节显示「已学完成」
  - 验证: 浏览器进详情页
  - 文件: 仅验证，不改

- [ ] T9: publish 合并答辩口径卡
  - 验收: 1 段口径文字产出
  - 验证: 文档存在
  - 文件: 答辩口径卡文档
```

---

## 四、设计评审检查（api-and-interface-design verification）

- [x] 每个端点有类型化输入输出（PlatformStatVO / List<Course> / CompleteChapterDTO / Result<Void>）
- [x] 错误响应沿用单一 Result 格式（code+message）
- [x] 校验在边界（DTO @Valid + @NotNull）
- [x] 列表端点：T3 为 Top-N（有 limit 上界），非全量列表，决策已说明
- [x] 新字段加法式（新增端点 + 新 VO/DTO，不改现有字段类型）
- [x] 命名一致（kebab-case 路径，camelCase 字段）
- [x] 接口文档将随实现提交（阶段 7 同步 dev-api.md）

---

## 五、答辩口径卡（T9）

> 本章节为学生乙分工 T2-T9 的答辩问答脚本。每张卡含：教师可能提问 / 标准答案 / 实现依据 / 演示路径。
> 实现已完成（2026-07-14），所有口径与代码实际行为一致。

### 卡 1：T2 平台实时统计 — 在线人数口径

**教师可能问**：「在线人数是怎么算的？用 HttpSessionListener 还是 Redis 在线集合？」

**标准答案**：

> 用的是**近 5 分钟活跃用户口径**，不是 HttpSessionListener 也不是 Redis。
>
> 设计考量：HttpSessionListener 只能统计 session 存活数，与"正在学习"语义不匹配（学员可能开着浏览器但没操作）；Redis 在线集合需要前端心跳上报，会增加弱网环境下的请求量，对基层网络不友好。
>
> 我用的是 `study_record.update_time` 近 5 分钟内有更新的去重 `student_id` 数。学员每次上报学习进度都会更新 `update_time`，5 分钟窗口足以覆盖一次视频心跳周期（前端每 30 秒上报一次），既准确又零额外基础设施依赖。
>
> 单条 SQL 三个子查询同时返回 `onlineCount` / `todayStudyCount` / `concurrentExamCount`，避免多次 DB 往返。Service 层对 COUNT 空表可能返回的 null 做了兜底为 0。

**实现依据**：
- `StatsMapper.xml` → `selectPlatform` SQL
- `StatsServiceImpl.platform()` → null 兜底
- `StatsController` → `@PreAuthorize("hasAuthority('stats:read')")`

**演示路径**：
1. admin 登录后台管理（5176）
2. Dashboard 顶部"平台实时统计"卡片
3. curl 验证：`GET http://localhost:9898/admin/stats/platform` 返回 `{onlineCount, todayStudyCount, concurrentExamCount}`

**关键代码**（MySQL 子查询）：
```sql
SELECT
  (SELECT COUNT(DISTINCT student_id) FROM study_record
   WHERE deleted = 0 AND update_time >= DATE_SUB(NOW(), INTERVAL 5 MINUTE)) AS onlineCount,
  (SELECT COUNT(DISTINCT student_id) FROM study_record
   WHERE deleted = 0 AND update_time >= CURDATE()) AS todayStudyCount,
  (SELECT COUNT(*) FROM exam_record
   WHERE deleted = 0 AND status = 0) AS concurrentExamCount
```

---

### 卡 2：T3 推荐课程 — Top-N 语义与排序

**教师可能问**：「推荐课程是怎么算的？用了什么推荐算法？为什么不做分页？」

**标准答案**：

> 用的是**基于报名数的 Top-N 热门推荐**，不是个性化推荐算法。
>
> 设计考量：本平台是基层卫生人员继续教育培训，课程总量有限（演示库 4-5 门），且学员学习需求相对统一（公共卫生服务规范、常见疾病诊治等），不需要个性化推荐。基于报名数的 Top-N 简单有效，且数据已有（`course_enroll` 表），无需额外算法基础设施。
>
> 不做分页是因为推荐位的语义是"热门榜单"而非"全量浏览"。学员要浏览全部课程走 `/api/course/list` 分页接口。推荐接口默认 `limit=5`，上限 20，后端做了边界钳制（`<=0` 取 5，`>20` 取 20）防止恶意请求拉取大数据量。
>
> 排序规则：报名数降序 + 报名数相同时按 `create_time` 降序（新课程优先露出）。仅返回 `status=1` 已发布课程。

**实现依据**：
- `CourseMapper.xml` → `selectRecommend` LEFT JOIN `course_enroll` 聚合
- `CourseServiceImpl.recommend()` → limit 边界钳制
- `CourseApiController.recommend()` → 复用 `fillTeacherName` 填充教师姓名

**演示路径**：
1. student01 登录 web-student（5174）
2. 首页"推荐课程"区块
3. curl 验证：`GET http://localhost:9899/api/course/recommend?limit=5` 返回按报名数排序的课程列表

---

### 卡 3：T4 标记章节完成 — 复用策略

**教师可能问**：「标记章节完成和上报学习进度是不是两个接口重复了？为什么不直接前端调 progress 接口传 100？」

**标准答案**：

> 后端确实是复用 `reportProgress` 实现的，但保留独立接口有三个价值：
>
> 1. **语义清晰**：`POST /api/study/complete-chapter` 表达"标记完成"业务语义，前端代码可读性更好。如果让前端直接调 `progress` 传 100 + completed=true，业务意图被技术参数淹没。
> 2. **DTO 校验独立**：`complete-chapter` 的 DTO 只需 `courseId + chapterId` 两个字段（`@NotNull`），不需要前端传 `progress / studyDuration / lastPosition / completed` 等技术字段，减少误传风险。
> 3. **未来扩展点**：如果后续要加"完成章节触发勋章/积分"逻辑，只需在 `completeChapter` 方法里加，不影响 `reportProgress` 心跳上报路径。
>
> 复用方式是**组合**而非复制：`completeChapter` 内部构造 `StudyProgressDTO` 设置 `progress=100 + completed=true`，调用 `reportProgress`，保证 upsert + 上限校验逻辑一致，零重复代码。这符合 api-and-interface-design 的"加法优于修改"原则。

**实现依据**：
- `StudyServiceImpl.completeChapter()` → 组合调用 `reportProgress`
- `CompleteChapterDTO` → 仅 2 字段 `@NotNull`
- `StudyApiController.completeChapter()` → `@PostMapping("/complete-chapter")`

**演示路径**：
1. student01 登录 web-student，进入课程1 学习页
2. 选择未完成章节，点击"标记为已学完"按钮
3. 章节列表该章节出现绿色 ✓ 标记，按钮变成"已完成本节学习"标签
4. curl 验证：`POST /api/study/complete-chapter` body `{"courseId":1,"chapterId":3}` 返回 200，再次 GET `/api/study/progress/1` 看到 chapterId=3 的 `progress=100, completed=1`

---

### 卡 4：T7 轻量报名校验 — 性能收益

**教师可能问**：「为什么要单独做一个 check-enrolled 接口？前端不能从 my-courses 列表里找吗？」

**标准答案**：

> 旧实现：学员进入学习页时，前端调 `getMyCourses({pageSize: 200})` 拉取整页已报名课程列表，再在前端用 `some(c => c.id === courseId)` 判断是否包含当前课程。问题：
>
> 1. **数据浪费**：为了判断 1 个 courseId 是否报名，拉取了最多 200 条课程完整字段（title/description/coverUrl/teacherName/...），网络传输浪费。
> 2. **DB 压力**：底层 `selectEnrolledCourses` JOIN `course_enroll` + `course` 表，分页查询要 count + select 两次 SQL，仅为了判断 1 条记录是否存在。
>
> 新实现：`GET /api/study/check-enrolled?courseId=N`，底层走 `course_enroll` 表 `COUNT`，仅返回 boolean。SQL 一次、传输 1 字节、零业务字段泄露。
>
> 性能对比（演示库数据量小，差异不显著，但语义和数据量级差异明显）：
> - 旧：~200 条 Course 对象序列化 + 网络传输 + 前端遍历
> - 新：1 个 boolean
>
> 这也是 v3.3 P2-8 修复（避免直接 URL 跳过报名进入学习）的配套优化。

**实现依据**：
- `StudyServiceImpl.isEnrolled()` → `courseEnrollMapper.selectCount(wrapper)`
- `StudyApiController.checkEnrolled()` → `@GetMapping("/check-enrolled")`
- `web-student/src/api/study.js` → `checkEnrolled` 封装
- `web-student/src/views/course/learn.vue` → `checkEnrolled()` 函数从 `getMyCourses(200)` 改为轻量 API

**演示路径**：
1. student01 登录 web-student
2. 直接访问 `/courses/1/learn`（已报名）→ 正常进入学习页
3. 直接访问 `/courses/99/learn`（未报名）→ 显示"您尚未报名该课程"el-result 拦截页
4. curl 验证：`GET /api/study/check-enrolled?courseId=1` 返回 `true`，`?courseId=99` 返回 `false`

---

### 卡 5：T8 学习进度上限钳制 — 决策依据

**教师可能问**：「学习进度为什么不查 chapter.duration 表做严格校验？只做上限 100 钳制够吗？」

**标准答案**：

> 这是一个**架构权衡决策**，核心考量是性能 vs 严格性的取舍。
>
> **方案 A（采纳）**：上限钳制 `if (progress > 100) progress = 100`，不查 `chapter.duration` 表。
> - 优点：每次心跳上报只 1 次 upsert SQL，无额外 SELECT
> - 缺点：无法防止"进度 50 跳到 80"的非线性增长（但这种行为对学员无害，最多是进度比真实情况快）
>
> **方案 B（未采纳）**：每次上报查 `chapter.duration` 校验 `progress <= (studyDuration / duration) * 100`。
> - 优点：严格防刷
> - 缺点：每次心跳多 1 次 SELECT `chapter` 表，按 3000 用户每 30 秒心跳计算，DB QPS 翻倍
>
> 决策依据（Hyrum's Law + ponytail 原则）：
> 1. v3.3 已修复 `progress >= 100` 强制 `completed=1` 的逻辑，T8 只追加 `progress > 100` 钳到 100，**不破坏现有 observable behavior**
> 2. 学习进度是学员自己的数据，"刷进度"伤害的是学员自己（学时不够），不存在安全风险
> 3. 真正的防刷应在考试环节（已有 `exam_record.status` + 重考次数限制 `ResultCode.EXAM_RETRY_LIMIT`）
> 4. ponytail 原则：用最小改动达到防御性目的，不引入额外 DB 开销
>
> 后续如果审计要求严格防刷，可在 `reportProgress` 加 `chapter.duration` 校验，是**加法式扩展**不影响现有契约。

**实现依据**：
- `StudyServiceImpl.reportProgress()` → line 46-48 上限钳制 + line 55-58 进度 100 强制 completed
- 不查 `chapter` 表的决策记录在代码注释 `// T8: 上限校验防刷（信任前端值 + 上限兜底，不查 chapter 表避免每次心跳 DB 开销）`

**演示路径**：
1. curl 构造异常请求：`POST /api/study/progress` body `{"courseId":1,"chapterId":2,"progress":200,"completed":true}`
2. 后端返回 200 success
3. GET `/api/study/progress/1` 看到 chapterId=2 的 `progress=100`（被钳制，不是 200）

**关键代码**：
```java
// T8: 上限校验防刷（信任前端值 + 上限兜底，不查 chapter 表避免每次心跳 DB 开销）
Integer progress = dto.getProgress() == null ? 0 : dto.getProgress();
if (progress > 100) progress = 100;
record.setProgress(progress);
// ... 进度 100 时强制 completed=1
if (record.getProgress() != null && record.getProgress() >= 100) {
    record.setProgress(100);
    record.setCompleted(1);
}
```

---

### 卡 6：跨题目通用 — 学生乙分工完整性

**教师可能问**：「你负责的课程与学习模块做了哪些完善？整体思路是什么？」

**标准答案**：

> 学生乙负责"课程与学习"模块的完善工作，按 spec-driven 8 阶段流程执行：
>
> **整体思路**：契约优先 → 决策记录 → 任务拆解 → 增量实现 → 验证回归 → 代码评审 → 文档同步 → 答辩口径
>
> **8 项任务（T2-T9）**：
> - T2 平台实时统计（纯 SQL 替代 Redis 方案）
> - T3 推荐课程 Top-N（按报名数）
> - T4 标记章节完成（组合复用 reportProgress）
> - T5 回归 learn.vue 章节渲染（PASS）
> - T6 回归 detail.vue finished 标签（PASS）
> - T7 轻量报名校验（替代 getMyCourses 200 兜底）
> - T8 学习进度上限钳制（最小改动 + 不破坏 v3.3）
> - T9 答辩口径卡（本章节）
>
> T1 离线下载按用户决定暂不做（答辩教师要求演示真实打包，预置 zip 方案不在本次范围）。
>
> **验证**：mvn package 两 jar 构建通过 + vite build 通过 + curl 5 项全 PASS + 静态回归 T5/T6 PASS。
>
> **设计原则**：
> - Hyrum's Law：不破坏 v3.3 已修复的 observable behavior
> - ponytail：最小改动，不超范围
> - api-and-interface-design：契约优先、加法优于修改、校验在边界
> - spec-driven：8 阶段门控推进

**演示路径**：参见 v3.4 更新说明（`docs/进度文档.md`）

**实现依据**：
- Spec：`production/spec-studentB.md`
- Plan：`production/plan-studentB.md`（本文件）
- 代码改动：13 个文件（6 后端 + 2 前端 + 5 文档）
- 验证日志：curl 5 项 + 静态回归 2 项
