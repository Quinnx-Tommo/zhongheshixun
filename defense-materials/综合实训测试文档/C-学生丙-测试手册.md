# C - 学生丙 - 手册

> **对应分工文档**：`../综合实训分工方案/03-学生丙-考试与统计.md`
> **目标**：丙（学生 C）跑完本测试，验证「考试 + 统计」模块 100% 可用（重点：双核心算法）
> **文档版本**：v1.1（2026-07-10 修订：与实现完全对齐）
> **修订要点**：
> 1. 知识点管理实际为平铺列表（按 courseId 过滤），非 parent_id 树形
> 2. ExamStartVO 已补 startTime + serverTime 字段（双保险计时）
> 3. exam_paper 实体不含 score/is_pass/start_time/end_time，这些字段在 exam_record 表
> 4. 多选答案实际格式 "A,B,D"（带逗号），判分算法为字符集合比较（效果等价于 sortChar）
> 5. ECharts 三图主题：考试统计混合图 / 分数段饼 / 时间趋势折线
> 6. 数据概览为 6 卡片（非 4 卡片）
> 7. isPass 按「得分百分比 ≥ passScore」判定，不是 score≥60

---

## 一、测试前置条件

- [ ] 学生甲的登录认证已通过
- [ ] 学生乙的课程管理至少有 1 门课程在线
- [ ] 9898 training-admin 后端已启动
- [ ] 9899 training-api 后端已启动
- [ ] 管理后台前端已启动（端口 5176）
- [ ] 5174 PC web-student 前端已启动
- [ ] 测试账号 admin + student01 可用（密码统一为 `123456`）

---

## 二、测试项（共 18 步）

### 2.1 试题管理（核心）

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 1 | admin 登录 →「试题管理」→ 列表 | 看到 5 题型（单选/多选/判断/填空/问答）不同颜色标识（primary/warning/success/info/空） | | |
| 2 | 点「添加试题」→ 选单选 → 选项 A/B/C/D → 答案=C → 难度=2(中等) → 保存 | 新增成功 | | |
| 3 | 添加多选题 → 答案 A,B,D（前端 join 逗号）→ 难度=1(简单) → 保存 | 成功 | | |
| 4 | 添加判断题 → 答案 正确/错误 → 成功 | 成功 | | |
| 5 | 添加填空题 → 答案"收缩压≥140\|140mmHg"（多个用 \| 分隔）→ 保存 | 成功 | | |
| 6 | 添加问答题（type=5）| 本期不判分，仅占位（exam_answer.is_correct=null，score=0） | | |
| 7 | 查 DB 确认 type/difficulty 分布 | 简单 4 题 / 中等 4 题 / 困难 2 题（或其他 3:5:2 附近比例） | | ⭐ |

### 2.2 知识点管理（**实现简化版**）

> ⚠️ **本期实现说明**：知识点管理采用**平铺列表**方案（按 courseId 过滤），未做 parent_id 自关联的树形渲染，也未做"关联试题"功能。该模块的核心功能是 CRUD 题目分类，不影响考试与统计的端到端跑通。

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 8 | admin →「知识点管理」→ 选择课程 → 列表 | 看到平铺列表（每行：ID / 课程ID / 名称 / 描述 / 创建时间） | | |
| 9 | 添加知识点 + 编辑 + 删除 | CRUD 三个操作均成功（关联试题功能本期未做） | | |

### 2.3 考试管理 + ⭐ 自动组卷（核心）

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 10 | 管理后台 →「考试管理」→「创建考试」| 字段齐全：title, examType, courseId, passScore, maxRetry, duration, questionCount | | |
| 11 | 创建 title=内科基础测试 / examType=1(课程) / courseId=1 / passScore=60 / duration=30 / questionCount=10 → 保存 | 成功 | | |
| 12 | ⭐ 点「自动组卷」→ 选考试 / 知识点 | 系统按 30:50:20 抽题（`ExamBizServiceImpl.autoGeneratePaper`） | | ⭐ |
| 13 | 读 DB：`SELECT questions FROM exam_paper WHERE exam_id=#{id} AND student_id=0` | 看到 10 道题 JSON（管理员模板卷 student_id=0；学员卷存各自 student_id） | | |
| 14 | 手工数：⭐ 简单 3 题、中等 5 题、困难 2 题（合计 10）| 符合 30:50:20 比例（**注**：若某难度桶题库不足，会自动缩桶，可能偏离 3:5:2） | | ⭐ |
| 15 | 截图 exam_paper 表 questions 字段 | 保留作为验收证据 | | |

### 2.4 ⭐ 双保险计时 + 自动阅卷（核心）

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 16 | student01 登录 web-student →「考试中心」→ 找到刚才那场 | 显示"待考"状态 | | |
| 17 | 点「开始考试」| 题目加载 + 倒计时（30min），F12 看 9899 返回 `startTime` + `serverTime`（**v1.1 已加字段**） | | ⭐ |
| 18 | 回答全部客观题（单选/多选/判断/填空），避开问答题 | 能正常选/勾/填 | | |
| 19 | ⭐ 点「交卷」 | `POST /api/exam/submit` → 自动阅卷 → 返回 score/totalScore/passed/correctCount/wrongCount | | ⭐ |
| 20 | 成绩页：看每题对错 + 标准答案 + 学员选项 | 回看功能完整 | | |
| 21 | 故意答错 7 道（passScore=60）→ 预期 passed=false | earnedScore/totalScore*100 < 60 → passed=false | | |
| 22 | 再答对 7 道 → passed=true | earnedScore/totalScore*100 ≥ 60 → passed=true | | |

**判分公式**（v1.1 修订）：
```
percentScore = earnedScore / totalScore * 100
passed = (percentScore >= exam.passScore)
```
- exam.passScore 单位是**百分比**（60 表示 60%），不是固定 60 分
- isPass 不存数据库，由 result 接口按 score+passScore 实时推导

### 2.5 ⭐ 自动阅卷算法验证（手抄必会）

测一道多选题是否启用顺序无关比较：

```sql
-- 准备 1 道多选题（标准答案存为 "A,B,D" 注意带逗号）
-- DB standard answer: "A,B,D"
-- 学员答: "D,B,A"（顺序打乱，但字符集合相同）
-- Set<Character> 比较：{A,B,D} == {A,B,D} → 判对

SELECT 
  id, question_id, student_answer, correct_answer,
  is_correct, score
FROM exam_answer 
WHERE record_id = <刚才交卷的记录>;
-- 预期 student_answer="D,B,A" 的记录 is_correct=1（字符集合比较生效）
-- 如果 is_correct=0 说明 Set<Character>.equals() 没生效
```

**算法对照**（[ExamBizServiceImpl.checkAnswer L405-434](file:///D:/A-Users/Desktop/zhongheshixun/training-parent/training-service/src/main/java/com/training/service/impl/ExamBizServiceImpl.java#L405-L434)）：

| 题型 | type | 比较方式 | 备注 |
|------|------|---------|------|
| 单选 | 1 | `equalsIgnoreCase` | 大小写不敏感 |
| 多选 | 2 | `Set<Character>.equals()` | **顺序无关**，效果等价 sortChar |
| 判断 | 3 | `equalsIgnoreCase` | 大小写不敏感 |
| 填空 | 4 | `split("\\|")` 任一命中 | 多答案用 \| 分隔 |
| 问答 | 5 | 不判分 | is_correct=null, score=0 |

### 2.6 ⭐ 多角度统计 + ECharts 三图（亮点）

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 23 | admin →「统计报表」→ 数据概览 | **6 个统计卡片**（学员总数 / 课程总数 / 报名人次 / 累计学习时长 / 考试场次 / 今日活跃学员） | | |
| 24 | ⭐ 混合图：考试统计（平均分柱+通过率线）| ECharts BarChart + LineChart 双轴混合正常渲染 | | ⭐ |
| 25 | ⭐ 饼图：考试分数段分布（60-69/70-79/80-89/90-100）| ECharts PieChart 正常渲染 | | ⭐ |
| 26 | ⭐ 折线图：学习时间趋势（3 条线：学习时长/活跃学员/新增报名）| ECharts LineChart 正常渲染，支持 day/week/month 切换 | | ⭐ |
| 27 | 点「学员学习统计」tab | 表格按姓名/机构筛选，分页 | | |
| 28 | 点「课程热度统计」+「机构维度统计」tab | 各页加载正常 | | |
| 29 | 跑 6 个统计接口（curl/Postman）| 都返回 data：`/admin/stats/overview` `/student` `/exam` `/course` `/org` `/trend` | | |

**ECharts 三图主题对照**（v1.1 修订）：

| 手册原描述 | 实际实现 | 实现位置 |
|----------|---------|---------|
| 柱状图：各机构考试通过率 | 混合图：考试统计（平均分柱+通过率线） | [stats/index.vue L36](file:///D:/A-Users/Desktop/zhongheshixun/training-admin/frontend/src/views/stats/index.vue#L36) |
| 饼图：课程类型分布 | 饼图：考试分数段分布 | [stats/index.vue L46](file:///D:/A-Users/Desktop/zhongheshixun/training-admin/frontend/src/views/stats/index.vue#L46) |
| 折线图：近 7 日学习时长 | 折线图：学习时间趋势（30 天粒度，3 条线）| [stats/index.vue L64](file:///D:/A-Users/Desktop/zhongheshixun/training-admin/frontend/src/views/stats/index.vue#L64) |

### 2.7 数据库验证

```sql
-- 试题库（注意：题库分布是题目本身分布，与组卷比例 30:50:20 不同）
SELECT id, type, difficulty, LEFT(content,20) AS snippet FROM question ORDER BY id;

-- 知识点（平铺列表，按 courseId 过滤；entity 有 parent_id 字段但本期未使用）
SELECT id, parent_id, course_id, name FROM knowledge_point ORDER BY course_id, id;

-- 考试
SELECT id, title, exam_type, course_id, pass_score, duration, question_count FROM exam;

-- 试卷（⭐ 重点；注意字段名是 student_id 不是 user_id）
-- exam_paper 仅存：id/exam_id/student_id/questions/create_time
-- score/is_pass/start_time/end_time 都在 exam_record 表
SELECT id, exam_id, student_id, questions, create_time 
FROM exam_paper ORDER BY id DESC LIMIT 5;

-- 答题详情（⭐ 重点；多选答案格式为 "A,B,D" 带逗号）
SELECT record_id, question_id, student_answer, correct_answer, is_correct, score 
FROM exam_answer ORDER BY id DESC LIMIT 30;

-- 考试记录（含分数与提交时间，isPass 由 score+passScore 实时推导）
SELECT id, exam_id, student_id, score, status, start_time, submit_time 
FROM exam_record ORDER BY id DESC LIMIT 5;

-- 预期：
-- question: 10+ 条（按需分布：单选/多选/判断/填空/问答 + 简单/中等/困难）
-- exam: 1+ 条（exam_type=1, course_id=1, pass_score=60）
-- exam_paper: 含 questions JSON（管理员模板卷 student_id=0；学员卷存各自 student_id）
-- exam_answer: 客观题 is_correct 正确标记；问答 is_correct=null
-- exam_record: score 数值正确，submit_time 在 start_time 之后
```

---

## 三、验收标准

### ✅ 全部通过：丙的模块是综合实训亮点

> 关键通过项：
> - ⭐12（组 10 题符合 30:50:20）
> - ⭐17（startExam 返回 startTime + serverTime）
> - ⭐19（交卷自动出分）
> - ⭐20（成绩回看）
> - ⭐24-26（ECharts 三图渲染）

### ❌ 部分失败：逐条记录 TODO

| 失败步骤 | 查阅章节 | 重点 |
|----------|----------|------|
| 1-6 | 4.1 试题管理接口 | QuestionController |
| 8-9 | 4.2 知识点管理（简化版） | KnowledgeController（平铺 CRUD，无树形/无关联试题） |
| 12-15 | 5.1 自动组卷算法 | ExamBizServiceImpl.autoGeneratePaper() |
| 17 | 双保险计时 | ExamBizServiceImpl.startExam() 已返回 startTime + serverTime |
| 19-22 | 5.2 自动阅卷算法 | ExamBizServiceImpl.checkAnswer() + submitExam() |
| 24-29 | 5.3 多角度统计 | StatsController + stats/index.vue |

---

## 四、测试产出

1. **测试结果截图**（每步一张，打 ✅/❌）
2. **录屏文件**（5 分钟，跑步骤 1-29）
3. **⭐ 自动组卷验证截图**（exam_paper.questions JSON）
4. **⭐ 自动阅卷验证截图**（exam_answer 表，注意 student_answer 含逗号）
5. **⭐ ECharts 三图截图**（混合图+饼图+折线图）
6. **SQL 查询结果截图**（6 张：question/knowledge_point/exam/exam_paper/exam_answer/exam_record）
7. **测试小结**（300 字 + Bug 记录 + 算法走查心得）

---

## 五、版本变更记录

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-07-08 | 初版 |
| v1.1 | 2026-07-10 | 修订项见文档开头修订要点；ExamStartVO 加 startTime/serverTime；统计三图主题校正；SQL 字段名校正（student_id 替代 user_id）；新增 exam_record 表 SQL；多选答案格式"带逗号"说明；判分公式按百分比 |
