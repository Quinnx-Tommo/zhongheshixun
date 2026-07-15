# Plan: 11 步端到端测试执行计划

> **对应 spec**：`production/spec-e2e-test-11steps.md`
> **日期**：2026-07-14
> **目标**：执行 11 步测试，产出全绿报告

---

## 一、执行批次（串行，步骤间有数据依赖）

### 批次 A：管理端基础（步骤 1-3）

| 步骤 | 命令 | 预期 | 清理 |
|------|------|------|------|
| 1 | curl admin login → curl `/admin/course/page` | total ≥ 3 | 无 |
| 2 | curl `POST /admin/course`（新建测试课程） | 200 + DB +1 | 测试后逻辑删除该课程 |
| 3 | curl `POST /admin/chapter`（为测试课程加章节） | 200 + DB +1 | 随课程清理 |

### 批次 B：学员端登录 + 报名（步骤 4-5）

| 步骤 | 命令 | 预期 |
|------|------|------|
| 4 | curl admin login（student01） | token + role=STUDENT |
| 5 | curl `POST /api/study/enroll`（student01 报名课程2，未报名的） | 200 + DB course_enroll +1 |

### 批次 C：学习页 + 进度（步骤 6-9，复用 V5 证据）

| 步骤 | 命令/证据 | 预期 |
|------|----------|------|
| 6 | curl `GET /api/course/detail/1` + agent-browser learn.vue | chapters 非空 + 页面渲染 |
| 7 | 复用 V5.3（onPause→POST，studyDuration +5） | 心跳链路通 |
| 8 | DB 查询 study_record（student_id=4, course_id=1） | progress > 0 |
| 9 | 复用 V5.4（trigger ended → completed 0→1） | completed=1 |

### 批次 D：进度 + 统计（步骤 10-11）

| 步骤 | 命令 | 预期 |
|------|------|------|
| 10 | curl `GET /api/study/progress/1` + agent-browser detail.vue | progress 值 + 进度条 |
| 11 | curl `/admin/stats/overview`+`/trend`+`/course` + agent-browser stats 页 | 三接口返回数据 + ECharts 渲染 |

---

## 二、数据清理策略

- 步骤 2 新建的测试课程：记录其 id，测试完成后 `PUT /admin/course/publish` 设 status 或逻辑删除（`DELETE /admin/course/{id}`）
- 步骤 3 新建章节：随课程删除
- 步骤 5 新增报名（student01-课程2）：保留（演示数据有益无害，不清理）

---

## 三、风险与缓解

| 风险 | 缓解 |
|------|------|
| curl PowerShell JSON 转义 | 用 `'{\"k\":\"v\"}'` 单引号外层 |
| Clash 代理 502 | 全部加 `--noproxy localhost` |
| 浏览器 autoplay 拒绝 | 步骤 7 复用 V5.3 已有证据，不重新触发 play |
| 步骤 2/3 写入脏数据影响演示 | 测试后逻辑删除，记录清理结果 |
| DB 查询无 mysql cli | 用后端已有接口反查（study_record 通过 `/api/study/progress` 返回值验证） |

---

## 四、验收检查（执行后自检）

- [ ] 11 步全部有执行命令 + 实际输出 + 判定
- [ ] 失败步骤有原因分析
- [ ] 测试数据已清理（步骤 2/3 的测试课程）
- [ ] 报告生成 `production/e2e-test-11steps-report.md`
- [ ] 进度文档升 v3.6
- [ ] spec-studentB 验收门禁勾选
