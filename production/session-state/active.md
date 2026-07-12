# Active Session State

## Project

四川省基层卫生人员网络培训平台

## Phase

S3 集成演练阶段（后端+三端编码完成 → 演示前 E2E 联调 + 规避路径演练）

## Status

**文档与编码状态**
✅ 需求澄清（9 项决策）
✅ 架构设计（单体 Spring Boot + 设计文档逻辑分层）
✅ 数据库设计（17 张表 + 初始化脚本）
✅ API 设计（学员/管理/web 三端）
✅ 后端编码（training-admin 11 controller + training-api 6 controller；jar 41 MB + 39 MB，2026-07-08 15:16 构建）
✅ Vue 管理后台前端（12 页面 + ECharts 三图 + 咨询管理，位于 training-admin/frontend/）
✅ Web-Student 学员 PC 端（5 核心页 + MainLayout + API 穿透验证 9 项通过，web-student/）
✅ 微信小程序（10 页面 + 咨询页完整，miniprogram/）
✅ 需求偏差说明 v2.1.0、deploy.md 修订、双版本答辩口径
✅ 项目入口 CLAUDE.md & 多代理 .claude/ 配置
✅ 进度文档重写（docs/进度文档.md 与代码实际同步）

**待完成**
❌ 演示前密码 hash 决策（password vs "123456" 二选一）
❌ 三端口澄清（8080/8081/8082 + 5173/5174）
❌ 浏览器 E2E 全流程演练（管理后台 + web-student + 小程序三端）
❌ 演示素材采集（6 项：ECharts 三图/SLA 标红/offline_flag/组卷 JSON/高并发设计代码/小程序咨询命中+转人工）
❌ 演示流程演练 + 答辩 PPT

**整体完成度：~82%**
（规划+文档+编码均基本完成；端口/密码统一 + E2E 演练是当前瓶颈）

## Next Steps（按优先级）

### P0（演示必修）
1. **【必修】密码决策**：当前 DB hash `$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2` 是明文 "password" 的 BCrypt（不是 "123456"）。选择其一：
   - 方案 A：生成真正的 "123456" BCrypt hash，替换 database.sql 第 318-322 行
   - 方案 B：统一文档声明真实测试密码 = `password`（本项目当前状态）
2. **【必修】端口确认**：演示前 `netstat -ano` 确认 8080/8081/8082/5173/5174 真实监听端口；小程序 baseURL 硬编码 8081、web-student 代理 `/api`→8082
3. **【必修】E2E 三端演练**：登录 → 首页 → 课程 → 报名 → 学习 → 考试（倒计时+自动交卷）→ 成绩回看 → 个人中心 → 咨询 → 统计
4. **【必修】数据库准备**：`UPDATE course SET offline_flag=0`；确认默认考试题目仅 type 1/2/3（客观题）

### P1（演示前 1 天）
5. 演示素材 6 项截图采集
6. 双版本答辩口径演练（Q1/Q2/Q3）
7. PPT 禁用词自查

### P2（答辩准备）
8. 答辩文档 / 演示 PPT
9. 学员互动三件套（笔记/评分/提问）数据库预留 → 如需补做约 1.5 人天
10. 问答题人工批阅界面 → 如需补做约 0.5 人天
11. 离线学习 manifest + /api/study/batch 进度回传 → 如需补做约 2 人天

## Notes

- **原始需求唯一来源**：`11.基层卫生人员网络培训平台.docx`（已提取为 `project-doc.md`）
- **整体目标**：3000 名基层卫生技术人员 / 67 个民族县
- **核心功能**：MOOC 学习、在线考试、离线学习、实时咨询、多角度统计
- **技术栈**：Spring Boot 2.7 + MyBatis-Plus 3.5 + MySQL 8.0 + Vue3 + Element Plus + 微信小程序原生 + Redis（可选）
- **架构**：单体应用（训练管理后台 /admin 8080 + 训练 API /api 8081/8082 + Vue 5173 + Web-Student 5174 + 小程序）
- **JWT 统一认证中心**：8080/8081/8082 JWT secret 完全一致（`training-platform-jwt-secret-key-2026-07-su-sheng-ji-ceng-wei-sheng`）；AuthController 与 ApiJwtInterceptor 均不限制 role，学员 token 可调业务接口
- **关键架构决策**：
  - /admin/login 不限制 role（AuthController）
  - ApiInterceptorConfig 放行 /api/wx/login 与 /api/course/**；拦截 /api/user/**、/api/study/**、/api/exam/**、/api/consult/**、/api/stats/**
  - web-student 双代理：/api→8082（业务），/admin→8080（登录）
  - 小程序 baseURL 硬编码 8081（miniprogram/api/config.js）
- **诚实披露现状（演示避免踩坑）**：
  - 当前真实演示密码是 **`password`**（不是 "123456"，database.sql 注释有误）
  - web-student 首页 ECharts 趋势图当前用 **mock 硬编码** [周一~周日 30/45/60/20/80/55/70]（后端 trendData 待对接）
  - 小程序首页 **index 是静态壳**（banner+入口图片占位，第 24 行 `TODO: 加载推荐课程`）
  - 学习互动三件套 / 问答题人工批阅 / 离线断点续传 是 **P2 未实现** 但演示会绕开
  - SLA 默认告警阈值 24 小时，演示可设 slaHours=1（1 分钟级）
</parameter>
</invoke>
</invoke>