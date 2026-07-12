# B - 学生乙 - 手册

> **对应分工文档**：`../综合实训分工方案/02-学生乙-课程与学习.md`  
> **目标**：乙（学生 B）跑完本测试，验证「课程 + 学习」模块 100% 可用

---

## 一、测试前置条件

- [ ] 学生甲的登录认证已通过测试（拿到 token）
- [ ] 9898 training-admin 后端已启动
- [ ] 9899 training-api 后端已启动
- [ ] 5176 管理后台前端已启动
- [ ] 5174 PC web-student 前端已启动
- [ ] 测试账号 student01/123456 可用

---

## 二、测试项（共 15 步）

### 2.1 课程管理（管理后台侧）

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 1 | admin 登录管理后台 →「课程管理」→ 列表 | 看到 3+ 课程 | | |
| 2 | 看字段：title, type(公开课/必修课), hours, offlineFlag | 字义清楚 | | |
| 3 | 点「添加课程」→ title=高血压诊疗规范 / type=1(公开课)/ hours=20 / offlineFlag=0 → 保存 | 新增成功 | | |
| 4 | 点「编辑课程」→ 改 description → 保存 | 成功 | | |
| 5 | 点「发布/下架」切换 | 状态切正确 | | |
| 6 | 点「章节管理」→ 该课程的章节列表 | 看到章节（按 sortOrder 排序） | | |
| 7 | 点「添加章节」→ title=第一章 高血压诊断 / contentType=video / duration=1800 / sortOrder=1 → 保存 | 成功 | | |
| 8 | contentType 选 text 并存 contentText | 文本内容存下 | | |

### 2.2 PC web-student 报名与学习（核心）⭐

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 9 | 打开 http://localhost:5174 → student01/123456 登录 | 登录成功，跳首页 | | |
| 10 | 首页看到欢迎条 + 统计卡片（我学的课程数、已报名数、在学考试、总学时）| 卡片数据准确 | | |
| 11 | ⚠️ 首页 ECharts 趋势图 | **当前显示 mock 静态数据**，不是真实数据 | | ❌ mock 数据 |
| 12 | 点「课程中心」→ 课程列表 | 分页展示课程 | | |
| 13 | 点某课程 → 详情页（章节列表 + 报名按钮） | 看到元信息 + 报名按钮 | | |
| 14 | 点「报名」按钮 | 变为"已报名 / 直接学习"，DB course_enroll 表 insert 1 条 | | ⭐ |
| 15 | 点「开始学习」→ 进入学习页（章节区 + 媒体区） | 章节列表 + 视频播放器 / PDF / 文本 | | |
| 16 | 播放视频 / 停留 10s → 看 F12 Network | 看到 `/api/study/progress` 请求（POST 200） | | ⭐ |
| 17 | 请求体：`{courseId, chapterId, lastPosition, duration, completed}` | 字段齐全 | | |
| 18 | 等 30s 后刷新 - 查 DB：`SELECT * FROM study_record WHERE user_id=2` | 看到 progress>0 的记录 | | |
| 19 | 点「学完本章节」按钮 | completed=1 | | |
| 20 | 在课程详情看进度条 | 进度前进 | | |

### 2.3 我学的课程 & 个人中心

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 21 | 点侧边栏「个人中心」→「我的课程」 | 看到刚报名的课程，显示进度百分比 | | |
| 22 | 看 profile 页 | 看到用户资料 + 学习统计 | | |

### 2.4 统计报表（多角度）⭐

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 23 | 管理后台 →「统计报表」→ 数据概览 | 看到 4 统计卡片 | | |
| 24 | ECharts 柱状图：各机构考试通过率 | 正常渲染 | | |
| 25 | ECharts 饼图：课程类型分布 | 正常渲染 | | |
| 26 | ECharts 折线图：近 7 日学习时长 | 正常渲染（⚠️ 若 mock data 注明） | | |
| 27 | 点「学员学习统计」表格 | 看到按 org 分组统计 | | |
| 28 | 点「考试统计」+「课程热度」+「机构统计」 | 各页能正常加载 | | |

### 2.5 数据库验证

```sql
-- 课程 + 章节
SELECT id, title, type, hours, offline_flag, status FROM course ORDER BY id;
SELECT course_id, title, content_type, duration, sort_order FROM course_chapter ORDER BY course_id, sort_order;

-- 报名
SELECT id, user_id, course_id, enroll_time, status FROM course_enroll;

-- 学习进度
SELECT user_id, course_id, chapter_id, progress, completed, study_duration 
FROM study_record 
ORDER BY user_id, course_id, chapter_id;

-- offline 字段
SELECT id, title, offline_flag, zip_url FROM course;

-- 预期能看到：
-- course 3+ 条（含 offline_flag=0）
-- course_chapter 共 5+ 条（每课程至少 1 章）
-- course_enroll 看到 student01(user_id=3) 报名了课程
-- study_record 看到 student01 的章节进度 progress > 0
-- offline_flag 全为 0（规避路径）
```

---

## 三、验收标准

### ✅ 全部通过：乙的模块可上线演示

> 关键通过项：14（报名成功）、16（进度上报）、19（完成章节）、24-26（ECharts 三图）

### ⚠️ mock 数据标注

> 步骤 11（首页 ECharts 趋势图）当前显示静态 mock 数据 [30,45,60,20,80,55,70]，后端 trendData 接口未对齐。演示时口径：「当前 UI 用静态 mock 数据填充展示，后端 trendData 接口已规划，待对接真实数据」。属于**已知待完善项**，不影响核心流程。

### ❌ 部分失败：逐条记录 TODO

| 失败步骤 | 查阅章节 | 重点 |
|----------|----------|------|
| 1-8 | 课程管理相关 Controller | CourseController / ChapterController |
| 9-10 | PC web-student 入口 | 多端 token 复用 |
| 14/18/19 | 5.1 进度计算公式 | StudyApiController |
| 24-26 | 5.5 统计接口 | StatsController |

---

## 四、测试产出

1. **测试结果截图**（每步一张，打 ✅/❌）
2. **录屏文件**（5 分钟，跑步骤 1-28）
3. **F12 Network 截图**（捕获 study/progress 请求）
4. **SQL 查询结果截图**（5 张）
5. **测试小结**（200 字 + Bug 记录 + mock 标注）
