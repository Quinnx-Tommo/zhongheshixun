# 四川省基层卫生人员网络培训平台

基于 Spring Boot + Vue 3 + 微信小程序的基层卫生人员网络在线培训平台。面向四川省 67 个民族县 3000 名基层卫生技术人员，提供 MOOC 学习、在线考试、离线学习、实时咨询等服务。

> 📄 **原始需求唯一参考文档**：`11.基层卫生人员网络培训平台.docx`（已提取为 `project-doc.md`）
> 📊 **当前进度**：`docs/进度文档.md` | **Session 状态**：`production/session-state/active.md`

## Domain

医疗在线教育 - 基层卫生人员继续教育培训

## Technology Stack

| 模块 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **后端** | Spring Boot | 2.7.18 | 单体应用（非微服务） |
| **ORM** | MyBatis-Plus | 3.5.3 | 数据访问 |
| **数据库** | MySQL | 8.0 | 单机 + 读写分离概念验证 |
| **缓存** | Redis | 7.x | 模拟 CDN 热点缓存（可选） |
| **认证** | JWT | jjwt 0.11.5 | Token 认证 |
| **Web 前端** | Vue3 + TypeScript + Element Plus | — | 后台管理 |
| **移动端** | 微信小程序原生 | — | 学员端（替代原生 APP） |
| **部署** | Docker（可选） | — | 作业演示可单机 |

## Project Structure

```
zhongheshixun/
├── 11.基层卫生人员网络培训平台.docx   # 原始选题文档
├── project-doc.md                     # 原始需求提取（可读版）
├── AGENTS.md                          # 本文件（项目入口）
├── training-parent/                   # Maven 父工程（待创建）
│   ├── training-common/               # 公共层（实体、工具、统一响应）
│   ├── training-dao/                  # 数据访问层（Mapper）
│   ├── training-service/              # 业务逻辑层
│   ├── training-admin/                # 后台管理后端 + Vue3 前端
│   └── training-api/                  # 小程序 API 后端
├── miniprogram/                       # 微信小程序（待创建）
├── docs/                              # 开发文档（权威）
├── 专业文档/                          # 教师要求的 11 份正式文档
├── powerDesigner/                     # PowerDesigner 建模工具
├── .Codex/                           # 多代理开发环境
└── production/                        # 生产/部署相关
```

## Multi-Agent Team

本项目使用 4 个协调代理进行开发。
Collaboration mode: **Hierarchical**

### Agents

| Agent | 角色 | 模型 | 职责 |
|-------|------|------|------|
| `tech-director` | 技术负责人 | Opus | 架构评审、API 契约、论文统稿、跨模块集成 |
| `backend-lead` | 后端负责人 | Sonnet | Spring Boot + MyBatis-Plus 全部后端实现 |
| `frontend-lead` | 前端负责人 | Sonnet | Vue3 + Element Plus 后台管理前端 |
| `miniapp-lead` | 小程序负责人 | Sonnet | 微信小程序（学员端）全部实现 |

### Available Skills

| Skill | 描述 | 何时用 |
|-------|------|--------|
| `/team-design` | 启动设计方案工作流 | 需要调整设计时 |
| `/team-code` | 启动编码实现工作流 | **编码阶段** |
| `/code-review` | 代码质量评审 | 提交代码时 |
| `/gen-report` | 生成毕设论文 | 项目收尾时 |
| `/team-status` | 查看团队状态 | 任何时候 |

## Coordination Rules

@.Codex/docs/coordination-rules.md

## Collaboration Protocol

每个任务遵循：**需求对齐 → 设计方案 → 评审 → 编码 → 集成验收**

- 代理必须按阶段门控推进，不要跳级
- 接口契约变更必须走 CR 流程
- 每个阶段结束提交评审，通过后才进入下一阶段

## Hooks

@.Codex/hooks/
- `session-start.sh` — 会话开始时加载项目上下文（显示结构 + 进度 + 下一步建议）
- `validate-commit.sh` — 提交前验证（无硬编码密码、无调试代码残留、无大文件）

## Current Progress (进度记录)

> 详细进度见 `docs/进度文档.md`。每次完成里程碑后更新此章节和进度文档。

### Milestones

| 阶段 | 状态 | 更新日期 |
|------|------|---------|
| 需求澄清与决策 | ✅ 完成 | 2026-07-07 |
| 技术栈选型 | ✅ 完成 | 2026-07-07 |
| 架构设计（单体 Spring Boot） | ✅ 完成 | 2026-07-07 |
| 数据库设计（17张表 + 初始化脚本） | ✅ 完成 | 2026-07-07 |
| API 接口设计 | ✅ 完成 | 2026-07-07 |
| 4人团队分工 | ✅ 完成 | 2026-07-07 |
| 文档合并为3份权威文档 | ✅ 完成（后扩充为：设计/开发/偏差/启动指南/database.sql） | 2026-07-07 |
| 需求偏差说明（v1.0 85%覆盖）| ✅ 完成 → **v2.0 修订为 100% 覆盖（教师要求的 5 项已纳入）** | 2026-07-07 |
| 开发文档（实现级手册） | ✅ 完成（v1.1.0 含高并发/IAAS-PAAS-SAAS/离线学习） | 2026-07-07 |
| 多代理开发环境配置 | ✅ 完成（v2.0 升级：4 代理 + 5 Skills） | 2026-07-07 |
| 设计方案评审（S1） | ✅ 通过（附修正：deleted/offline_flag/start_time/end_time/real_name + 学习进度接口） | 2026-07-07 |
| 后端编码（training-parent M1-M8 全部模块） | ✅ 完成（登录/课程/学习/考试/咨询/统计，jar 已构建：training-admin-exec.jar + training-api-exec.jar） | 2026-07-08 |
| 前端编码（Vue3 后台管理） | ✅ 完成（12 页面含 dashboard/course/chapter/exam/question/stats/consult/knowledge/train-plan/user/teacher，build 通过，dist/ 已生成） | 2026-07-08 |
| 小程序编码（微信小程序） | ✅ 完成（7 页：login/index/course/exam/plan/profile/consult，含关键词匹配+转人工咨询） | 2026-07-08 |
| **PC 学员网页端（web-student/）** | ✅ **完成**（5 核心页：登录/首页/课程中心/考试中心/个人中心，build 通过 + **接口穿透验证 9 项通过**） | 2026-07-08 |
| **web-student 混合 RBAC 菜单差异化（P0-P3 全部）** | ✅ **完成**（学生 6 菜单 / 讲师 9 菜单 / 管理员 6+顶部按钮；角色 Badge + 路由守卫 + P2/P3 页面 + ADMIN 跳转 5176；build + 9 项接口穿透 + 6 项浏览器 E2E + 6 张截图均通过） | 2026-07-10 |
| 联调测试（三端） | 🟡 **web-student 接口穿透已通过**，管理后台/小程序 **浏览器 E2E 未联调** | — |
| 部署与答辩准备 | ❌ 未开始 | — |

### Current Phase

**S3 全链路联调验收阶段**（M1-M9 全部完成 → 进入 MVP 端到端验收）

整体项目完成度约 **85%**（规划+文档+编码 100%、**web-student 接口穿透 9 项通过（E2E 待联调）**、其余两端联调 0%、部署/答辩 0%）。

> ⚠️ **web-student 已可用** <http://localhost:5174>，测试账号 `student01 / 123456`（5 个核心账号 `admin / teacher01 / teacher02 / student01-06` 真实密码统一为 **`123456`**，对应 `database.sql` 头部声明与 INSERT 实际 hash `$2a$10$EEeUC1lM2mbe.nOY0CtsDOVYQciytNhzUMLR2rAgI5nfOXzmlGJPK`。2026-07-10 实测：curl 9898 `/admin/login` 接受 `123456`，拒绝 `password`）。
> ⚠️ **当前端口拓扑（2026-07-09 更新）**：web-student 前端 **5174**；training-admin 后端 **9898**；training-api（小程序/业务后端） **9899**（**原 8081 已被僵尸进程死锁、原 8082 已弃用**）。

### 权威文档清单

| 文档 | 用途 |
|------|------|
| `docs/设计文档.md` | 单一事实源（架构+数据库+API+分工+计划+生产级架构设计） |
| `docs/开发文档.md` | **开发文档总览（导航+公共层+分工+构建）** |
| `docs/dev-backend.md` | 后端实现（7 模块 + 高并发 + 三层架构，~2150 行） |
| `docs/dev-frontend.md` | 后台管理前端（Vue3 + Element Plus，~1820 行） |
| `docs/dev-web-student.md` | 🆕 **PC 学员网页端（web-student/）— MVP 上线 + 联调日志** |
| `docs/dev-miniapp.md` | 微信小程序（学员端，~1000 行） |
| `docs/dev-database.md` | 数据库（17 表 + 分区 + 约束，~800 行） |
| `docs/dev-api.md` | API 接口清单（小程序端 + 后台管理端，~600 行） |
| `docs/deploy.md` | 部署 + 联调测试 + 答辩（~700 行） |
| `docs/需求偏差说明.md` | docx 需求对照（v2.0 已修订为 100% 覆盖） |
| `docs/项目启动指南.md` | 环境准备 + 快速启动 |
| `docs/database.sql` | 数据库初始化脚本（17张表+示例数据） |
| `专业文档/` | 教师要求的 11 份正式文档（需求规约/数据库设计/架构设计/测试计划等） |
| `project-doc.md` | 原始需求文档（docx 提取） |

### Next Milestone

M10 全链路联调验收：
1. 启动三个服务（后端 admin 9898 + api 9899 + 前端 5174）
2. 跑通 MVP 完整业务流程（登录 → 课程浏览 → 报名 → 学习 → 考试 → 咨询 → 统计）
3. 截图素材采集（ECharts 三图、咨询管理 SLA 告警、小程序咨询页）
4. 准备演示脚本（docs/deploy.md）
5. **密码决策**：✅ **已确认（2026-07-10 实测）**：演示账号密码统一为 `123456`，与 `database.sql` 头部注释一致。AGENTS.md 旧版"密码是 password"的描述错误，已修正
6. **演示规避路径**：offline_flag=0、默认考试仅含客观题（单选/多选/判断）、禁用词检查

### Notes for Continuation

- 新 session 启动时，首先 Read `docs/设计文档.md`、`docs/开发文档.md`（总览）和 `docs/需求偏差说明.md`
- ⚠️ **M10 联调重点**：全链路业务流程、ECharts 截图素材、答辩演示脚本
- ⚠️ **教师要求的 5 项必须功能**（高并发/IAAS-PAAS-SAAS/离线属性标记/咨询 SLA<1min/多角度统计）已纳入设计+开发文档，答辩必备

## Key Requirements

1. **性能要求**: 支持3000用户日常并发学习，1000并发考试，响应时间<2s
2. **离线支持**: 移动端离线学习包，支持断点续传和进度回传
3. **安全合规**: 医疗数据隐私保护，传输加密，敏感数据脱敏
4. **多语言支持**: 界面简洁易用，支持少数民族语言切换准备
5. **弱网优化**: 适配基层网络环境，优化弱网下的学习体验

## Development Rules

@.Codex/rules/common.md
@.Codex/rules/backend.md
@.Codex/rules/frontend.md
