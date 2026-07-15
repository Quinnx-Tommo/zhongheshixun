# Spec: 11 步端到端测试清单（spec-studentB 验收门禁）

> **来源**：`defense-materials/综合实训分工方案/02-学生乙-课程与学习.md` §十 + `production/spec-studentB.md` 验收门禁
> **日期**：2026-07-14
> **范围**：11 步端到端测试，覆盖演示主流程「登录 → 报名 → 学习 → 进度 → 统计」
> **状态**：SPEC（待执行）

---

## 一、Objective

按分工文档 §十 的 11 步测试清单，端到端验证课程与学习模块的完整业务闭环，产出全绿测试报告，满足 spec-studentB 验收门禁「审计报告 §十 11 步测试清单全绿」。

**用户故事**：
- 作为答辩老师，我希望看到 11 步测试清单每一步都有明确的 PASS 证据（curl 输出 / DB 查询 / 浏览器 E2E 结果），证明模块可演示。
- 作为学生乙，我希望测试报告可直接作为答辩素材，回答"你的模块跑通了吗"。

---

## 二、测试环境

| 组件 | 端口 | 状态 |
|------|------|------|
| training-admin 后端 | 9898 | 运行中 |
| training-api 后端 | 9899 | 运行中 |
| web-student 前端 dev | 5174 | 运行中 |
| 浏览器自动化 | agent-browser | 可用 |

**测试账号**：
- admin / 123456（管理员）
- student01 / 123456（学员，userId=4，已报名课程1）

**数据库**：MySQL 8.0，database.sql 已初始化

---

## 三、11 步测试清单（验收标准）

> 来源：`02-学生乙-课程与学习.md` §十

| 步骤 | 操作 | 预期 | 验证手段 |
|------|------|------|---------|
| 1 | admin 登录 → 课程列表 | 看到 3+ 课程 | curl `/admin/login` 拿 token + curl `/admin/course/page` 验证 total ≥ 3 |
| 2 | admin 添加课程 | 新增成功 | curl `POST /admin/course` 返回 200 + DB course 表 +1 |
| 3 | admin 添加章节 | 新增成功 | curl `POST /admin/chapter` 返回 200 + DB course_chapter 表 +1 |
| 4 | student 登录 web-student | 登录成功 | curl `/admin/login`（student01）拿 token，返回 userInfo.role=STUDENT |
| 5 | student 报名课程 | 按钮状态变"开始学习" | curl `POST /api/study/enroll` 返回 200 + DB course_enroll +1 + 浏览器 detail.vue 按钮态 |
| 6 | student 进入学习页 | 看到章节列表 | curl `GET /api/course/detail/{id}` 返回 chapters 数组非空 + 浏览器 learn.vue 渲染章节 |
| 7 | 播放视频 / 停留 30s | Network 看到 progress 请求 | 浏览器 E2E：VideoPlayer 心跳上报（复用 V5.3 证据：onPause→POST，studyDuration 累加） |
| 8 | 刷新 - 查 DB | study_record 有 progress>0 | DB 查询 `SELECT progress,last_position FROM study_record WHERE student_id=4 AND course_id=1` |
| 9 | 点"学完"按钮 | completed=1 | curl `POST /api/study/complete-chapter` 返回 200 + DB study_record.completed=1（复用 V5.4 证据） |
| 10 | 课程详情看进度 | 进度条前进 | curl `GET /api/study/progress/{courseId}` 验证 progress 值 + 浏览器 detail.vue 进度条 |
| 11 | admin 看统计三图 | ECharts 正常渲染 | curl `/admin/stats/overview` + `/admin/stats/trend` + `/admin/stats/course` 返回数据 + 浏览器 stats 页 ECharts 渲染 |

---

## 四、验收门禁

全部 11 步 PASS 才算完成。每步需提供：
- **执行命令**（curl / agent-browser eval / DB SQL）
- **实际输出**（响应体 / DB 结果 / 浏览器状态）
- **判定**（✅ PASS / ❌ FAIL + 原因）

---

## 五、Boundaries

- **Always**：每步执行后立即记录证据；失败不跳过，记录原因继续
- **Never**：修改业务代码以通过测试（测试只验证，不改码）；使用脏数据污染生产表（步骤 2/3 新增的数据测试后清理）

---

## 六、与已有验证证据的复用

下列证据来自今日 V5.1-V5.5 验证，可直接复用，避免重复测试：

| 步骤 | 已有证据来源 | 复用判定 |
|------|-------------|---------|
| 7 | V5.3 心跳链路 PASS（studyDuration 1805→1810） | ✅ 复用 |
| 9 | V5.4 自动完成 PASS（chapter1 completed 0→1） | ✅ 复用 |
| 5（部分） | V5.5 T7 check-enrolled=true（已报名课程1） | ✅ 复用报名状态 |

步骤 1/2/3/4/6/8/10/11 需新执行。
