# D - 学生丁 - 手册

> **对应分工文档**：`../综合实训分工方案/04-学生丁-咨询与小程序.md`  
> **目标**：丁（学生 D）跑完本测试，验证「咨询问答 + 小程序」模块 100% 可用（重点：关键词匹配 + SLA 告警 + 小程序 10 页）

---

## 一、测试前置条件

- [ ] 学生甲的登录认证已通过
- [ ] 学生乙的课程管理至少有 1 门课程在线
- [ ] 9898 training-admin 后端已启动
- [ ] 9899 training-api 后端已启动
- [ ] 微信开发者工具导入 miniprogram/ 项目
- [ ] 测试账号 student01/123456 可用

---

## 二、测试项（共 20 步）

### 2.1 知识库管理（管理后台侧）

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 1 | admin 登录 →「知识库管理」→ 列表 | 看到知识条目（含 keywords 字段） | | |
| 2 | 点「添加知识条目」→ title=感冒用药 / keywords=感冒,发烧,头痛 / answer=扑热息痛... → 保存 | 成功 | | |
| 3 | 再添加高血压用药指导 / keywords=高血压,降压药 | 成功 | | |
| 4 | 看 keywords 字段是逗号分隔（英文标点）| 是半角逗号 | | |
| 5 | 点「编辑知识条目」→ 改 keywords → 保存 | 成功 | | |

### 2.2 ⭐ 智能问答（小程序侧）

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 6 | 打开微信开发者工具 → 导入 miniprogram | 项目加载无报错 | | |
| 7 | 顶层 AppID 选「测试号」→ 编译 | 小程序跑起来 | | |
| 8 | 首页选择「登录」tab 或进入强制登录页 | 登录表单显示 | | |
| 9 | 输入 student01/123456 登录 | 登录成功，跳转首页 | | |
| 10 | ⚠️ 小程序 baseURL 是 9899 | 请求能发通 | | |
| 11 | 进入「咨询」tab | 聊天 UI 显示（输入框 + 发送按钮） | | |
| 12 | ⭐ 问"高血压怎么用药" | 显示打字机效果 1.5 秒 → 返回预设答案 | | ⭐ |
| 13 | F12（开发者工具 Network）看到 `POST /api/consult/ask` | 返回 isAuto=true | | |
| 14 | ⭐ 问刁钻问题（知识库没有的）| 返回"正在为您转接人工客服..." | | ⭐ |
| 15 | DB：`SELECT * FROM consult_record ORDER BY id DESC LIMIT 1` | 看到 is_auto=0 的记录 | | |

### 2.3 ⭐ SLA 超时告警（管理后台侧）

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 16 | student01 小程序问刁钻问题，记录 transfer_time=now | 这条记录入队 | | |
| 17 | 等 60 秒（演示时 sla_seconds=60）| 后端 sla 计算触发 | | |
| 18 | admin 管理后台 →「咨询管理」→ 列表 | 看到该条记录，**行底色变红**（sla_exceeded=1）| | ⭐ |

### 2.4 ⭐ 人工回复

| # | 操作 | 预期 | 实际 | ✅/❌ |
|---|------|------|------|------|
| 19 | 管理后台点该条记录「回复」→ 输入回复内容 → 发送 | 回复成功 | | |
| 20 | 看 DB：`SELECT reply_time, status, sla_exceeded FROM consult_record WHERE id=#{id}` | reply_time 非空，status=1 | | |
| 21 | 返回小程序「咨询」tab，刷新或重发 | 看到"人工客服：xxx" 回复 | | |

### 2.5 小程序全部 10 页走查

| # | 页面 | 路径 | 核心功能 | ✅/❌ |
|---|------|------|----------|------|
| 22 | 首页 | index | 推荐课程 + tab 切换 | |
| 23 | 课程列表 | course/list | 列表渲染 | |
| 24 | 课程详情 | course/detail | 章节 + video/pdf/text | |
| 25 | 考试列表 | exam/list | 待考/已考 | |
| 26 | 考试答题 | exam/do | 倒计时 + 答题 | |
| 27 | 考试成绩 | exam/result | 成绩 + 对错回看 | |
| 28 | 培训计划 | plan/list + detail | 计划列表 + 详情 | |
| 29 | 个人中心 | profile/index | 用户资料 + 学习统计 | |
| 30 | 我的课程 | profile/courses | 在学课程列表 | |
| 31 | 学习记录 | profile/records | 章节学习记录 | |
| 32 | 考试记录 | profile/exams | 历史考试成绩 | |
| 33 | 咨询页 | consult/index | 聊天式 UI | |

### 2.6 小程序技术点验证

| # | 技术点 | 验证 | ✅/❌ |
|---|--------|------|------|
| 34 | wx.request 自动带 token | F12 请求头有 Authorization | |
| 35 | 视频 `<video>` 组件播放 | 课程详情点视频播放 | |
| 36 | PDF 预览（wx.downloadFile + wx.openDocument）| PDF 章节能打开 | |
| 37 | 文本富文本渲染 | text 类型章节正常显示 | |
| 38 | 打字机效果（isTyping + setTimeout）| 咨询 UI 能出打字机 | |
| 39 | wx.pageScrollTo 自动滚到底部 | 新消息自动 scroll | |
| 40 | wx.setStorageSync 持久化 token | 关闭开发者工具再打开还登着 | |

### 2.7 数据库验证

```sql
-- 知识库
SELECT id, title, keywords, LEFT(answer,30) AS snippet FROM knowledge_base;

-- 咨询记录（⭐ 重点）
SELECT 
  id, user_id, question, 
  is_auto, status, 
  transfer_time, reply_time, 
  sla_seconds, sla_exceeded 
FROM consult_record ORDER BY id DESC LIMIT 10;

-- 预期：
-- knowledge_base: 2+ 条（感冒用药 + 高血压）
-- consult_record: 至少 1 条 is_auto=0（转人工）的记录，sla_exceeded=1
```

---

## 三、验收标准

### ✅ 全部通过：丁的模块是综合实训展示亮点

> 关键通过项：⭐12（关键词匹配命中）、⭐14（转人工）、⭐18（SLA 标红）、⭐20（回复成功）、⭐22-33（小程序 10 页走通）

### ❌ 部分失败：逐条记录 TODO

| 失败步骤 | 查阅章节 | 重点 |
|----------|----------|------|
| 1-5 | 4.2 知识库管理接口 | KnowledgeBaseController |
| 12-15 | 5.1 关键词匹配算法 | ConsultServiceImpl.ask() |
| 18 | 5.2 SLA 告警机制 | ConsultController / list.vue |
| 20 | 4.3 人工回复接口 | ConsultController.reply() |
| 22-33 | 小程序详解 | 各 page.js |
| 34-40 | 小程序技术点 | request.js / app.js |

---

## 四、⚠️ 特别检查项

| # | 检查 | 结果 |
|---|------|------|
| 1 | 基础 URL 是否改成 9899？ | |
| 2 | 开发工具是否勾选"不校验合法域名"？（测试用）| |
| 3 | 小程序登录是否通？ | |
| 4 | F12 看 Network 请求是否 200？ | |

---

## 五、测试产出

1. **测试结果截图**（每步一张，打 ✅/❌）
2. **录屏文件**（5 分钟，跑步骤 1-21）
3. **⭐ 关键词命中验证截图**（咨询聊天记录页）
4. **⭐ SLA 标红验证截图**（管理后台红色行）
5. **⭐ 小程序 10 页截图**（每页一张，标注功能）
6. **SQL 查询结果截图**（3 张）
7. **测试小结**（300 字 + Bug 记录 + 演示规避口径）
