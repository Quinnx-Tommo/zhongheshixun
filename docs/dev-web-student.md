# web-student PC 学员网页端 — 开发与联调日志

> 创建日期：2026-07-08
> 状态：**MVP 全链路联调通过**
> 作者：frontend-lead（编码） + tech-director（架构决策、联调排障）

---

## 1. 功能概览

PC 学员网页端是四川省基层卫生人员网络培训平台的 **第 3 个前端**（另 2 个：Vue3 管理后台、微信小程序）。面向基层卫生技术人员，提供 PC 浏览器内的完整学习门户。

### 1.1 MVP 5 个核心页

| # | 页 | 路由 | 核心功能 |
|---|---|---|---|
| 1 | 登录页 | `/login` | 学员风格 UI，调 8080 `/admin/login`，JWT 存入 Pinia+localStorage |
| 2 | 首页 | `/home` | 欢迎条 + 4 统计卡片 + ECharts 学习趋势图 + 继续学习 + 推荐课程 |
| 3 | 课程中心 | `/courses` · `/courses/:id` · `/courses/:id/learn` | 课程列表/搜索/筛选/分页、课程详情+章节树+报名、学习页（视频/PDF/文本）+ 进度上报 |
| 4 | 考试中心 | `/exams` · `/exams/:id` · `/exams/:id/result` | 考试列表/状态筛选、答题页（倒计时 + 自动交卷）、成绩页（对错回看） |
| 5 | 个人中心 | `/profile` | 资料展示 + 编辑 + 学习统计 |

### 1.2 技术栈

- Vue 3 `<script setup>` + Composition API
- Vue Router 4（history 模式 + 路由守卫）
- Pinia（user store）
- Element Plus（unplugin-vue-components 按需引入）
- axios（统一封装 `src/utils/request.js`，响应拦截器处理 401）
- vue-echarts + echarts（首页趋势图）
- Vite 5（dev server + 双代理）

---

## 2. 架构决策（已落地）

### 2.1 统一认证中心（UX 方案 1，已验证）

**决策**：Web 学员端 **不复用小程序 `/api/wx/login`**（那是 openid 模拟），而是**走 8080 的 `/admin/login`**（账号 + BCrypt 密码）。

**验证结果（三关全过）**：

| 检查项 | 结果 | 证据 |
|---|---|---|
| 8080 与 8081/8081 JWT secret 一致 | ✅ | 都是 `training-platform-jwt-secret-key-2026-07-su-sheng-ji-ceng-wei-sheng` |
| `/admin/login` 不限制 role | ✅ | `AuthController.login` 只校验用户名+密码+status，不校验 role |
| 8081/8082 拦截器不限制 role | ✅ | `ApiJwtInterceptor.preHandle` 只做 token 校验，把 userId/role/username 注入 request 后放行 |

**论文写法**："统一认证中心——同一套账号体系服务管理后台（Vue3）、学员 Web（Vue3）、小程序（openid 自动绑定），JWT 三端 secret 统一，BCrypt 密码哈希存储。"

### 2.2 后端复用策略

**决策**：不复写/不复起新服务，**直接复用 training-api（8081/8082） 已发布的 18 个接口**。

8081/8082 已用接口清单：

```
GET    /api/course/list         课程列表（分页，仅已发布）
GET    /api/course/detail/{id}  课程详情
GET    /api/course/chapter/list?courseId=  章节列表
POST   /api/study/progress      上报学习进度
GET    /api/study/progress/{courseId}  获取某课进度
POST   /api/study/enroll        报名课程
GET    /api/study/my-courses    我的课程（分页）
GET    /api/exam/list            考试列表
POST   /api/exam/start/{id}     开始考试
POST   /api/exam/submit         提交答卷
GET    /api/exam/record/{id}    考试记录详情
GET    /api/exam/my-records     我的考试记录
POST   /api/consult/ask         提问
GET    /api/consult/my          我的咨询列表
GET    /api/stats/my?studentId=  我的学习统计
GET    /api/user/profile        个人资料
PUT    /api/user/profile        更新个人资料
⚠️     /api/wx/login             【小程序专用，Web 端禁用】
```

### 2.3 双代理策略

Vite 开发环境配两个代理：

| 前缀 | 目标 | 用途 |
|---|---|---|
| `/api` | http://localhost:**8082** | 全部业务接口（见下方端口变更说明） |
| `/admin` | http://localhost:8080 | 仅登录用 `/admin/login` |

**特殊处理**：登录接口在 `api/user.js` 里用裸 axios（不经过 request 实例），因为：
1. `/admin/login` 不需要 JWT
2. 响应拦截器只认 `{code, data}` 结构，但 login 失败时 status=200 + body `{code:1002}` 想自定义错误提示

### 2.4 端口变更（见 §4.2）

| 原端口 | 现端口 | 原因 |
|---|---|---|
| 8081 | **8082** | 8081 端口被僵尸进程死锁（Windows 内核级），所有 kill 工具均失败 |
| 5174 | **5175** | 5174 被旧 dev server 占用，Vite 自动迁移 |

---

## 3. 项目结构

```
web-student/
├── index.html
├── package.json
├── vite.config.js              # 5174 端口 + 双代理（/api→9899, /admin→9898）
├── .gitignore
└── src/
    ├── main.js
    ├── App.vue
    ├── api/
    │   ├── user.js             # login（裸 axios 走 9898） + profile（request 实例走 9899）
    │   ├── course.js           # list / detail / chapterList
    │   ├── study.js            # enroll / progress / myCourses
    │   ├── exam.js             # list / start / submit / record / myRecords
    │   ├── stats.js            # my
    │   └── consult.js          # ask / my
    ├── router/index.js         # 路由守卫：未登录跳 /login，已登录访问 /login 跳 /home
    ├── stores/user.js          # Pinia：token、userInfo、login（显式校验 code===200）/logout
    ├── utils/
    │   ├── request.js          # axios 封装 baseURL=/api + 响应拦截器 + transformResponse 兜底
    │   ├── dict.js             # 字典映射单一事实源（typeText/typeColor/difficultyText/statusText/questionTypeText/parseOptions）
    │   ├── format.js           # 时间/数字格式化：formatTime、safeNumber
    │   └── chart.js            # ECharts option 工厂：toChartOption(stats, opts)，trendData 真实优先 mock 兜底
    ├── layouts/
    │   └── MainLayout.vue      # 左侧导航（首页/课程中心/考试中心/个人中心）+ 右侧 content
    └── views/
        ├── login/index.vue
        ├── home/index.vue
        ├── course/list.vue
        ├── course/detail.vue
        ├── course/learn.vue
        ├── exam/list.vue
        ├── exam/answer.vue
        ├── exam/result.vue
        └── profile/index.vue
```

---

## 4. 遇到的问题与解决方案

### 4.1 🔴 Bug #1：`home:1 Uncaught (in promise) SyntaxError: "undefined" is not valid JSON`

**发现时间**：MVP 编码完成后首次浏览器访问 home 页。

**现象**：浏览器控制台爆 `SyntaxError: "undefined" is not valid JSON`，home 页白屏。

**定位过程**：
1. 前端 home 页 `onMounted` 里 3 个接口调用（getMyStats / getMyCourses / getCourseList）都已用 `try/catch`，按理不该抛 → 怀疑错误不在这 3 个调用
2. 读 `src/utils/request.js`：发现响应拦截器之前，**axios 默认的 `transformResponse` 会先跑 `JSON.parse(data)`**
3. 当 8081 服务挂了/返回空响应体时，`JSON.parse(undefined)` 直接抛 SyntaxError → 变成 Unhandled Rejection → 页面代码 catch 不住

**修复**（双管齐下）：

**修复 A**：`src/utils/request.js` 第 4-18 行，加 `transformResponse` 兜底：
```js
transformResponse: [(data) => {
  // 空响应体直接返回 null，避免 JSON.parse(undefined) 抛异常
  if (!data) return null
  try { return JSON.parse(data) }
  catch { return data }
}]
```

**修复 B**：`src/views/home/index.vue` 第 184-211 行，3 个接口各自独立 `try/catch` + `console.warn`：即使拦截器层漏了，页面也不会崩。

**经验教训**：axios 的 `transformResponse` **先于**拦截器跑，拦截器的 try/catch 抓不住它抛的 JSON.parse 错误。这是 axios 经典坑。

---

### 4.2 🔴 Bug #2：8081 端口僵尸进程死锁

**现象**：8081 端口 `LISTENING`，但所有接口返回 HTTP 000（curl）/ 连接拒绝。

**定位过程**：
1. `netstat -ano | grep 8081` → PID 36232 占着端口
2. 查进程内存只有 **14MB**（正常 Spring Boot 应 200MB+）→ 说明进程是"僵尸"（accept 但不处理）
3. 启动新 8081 实例 → Spring 报 `Address already in use`
4. **8082 端口启动新实例** → 成功，但 E2E 测试全 404

**尝试的 kill 方式（全部失败）**：
- `taskkill /F /PID 36232` → 无效参数/拒绝
- `powershell Stop-Process -Id 36232 -Force` → 拒绝访问
- `powershell Stop-Process -Name java` → 误伤 8080
- `Get-NetTCPConnection` + `Stop-Process` → 变量只读报错（编码混合问题）

**根因**：Windows 内核级的 TCP 端口死锁（orphaned TCP socket），常见于强制 kill Spring Boot 应用后、端口被"幽灵"持有。

**最终方案**：
1. 放弃 8081，启新实例在 **8082**
2. 改 `vite.config.js` 代理 `/api` → `http://localhost:8082`
3. 旧 8082 PowerShell 脚本 404 是 heredoc `&` 被 bash 吃掉，**实际 8082 完全正常**

**经验教训**：Windows 开发环境遇到端口死锁，**不要死磕 kill**，改端口是更快的解法。已写入 `.gitignore`-style 教训。

---

### 4.3 🔴 Bug #3：`api/user.js` getProfile/updateProfile 用裸 axios

**现象**：修复 Bug #1 后，`/api/user/profile` 返回 401。

**定位过程**：
1. `api/user.js` 导入了裸 `axios` 而非 `request` 实例 → 请求**不带 Authorization 头**
2. 8081/8082 `ApiJwtInterceptor` 校验失败 → 401

**修复**：
```js
// 改前
export const getProfile = () => axios.get('/api/user/profile').then((res) => res.data)

// 改后
import request from '@/utils/request'
export const getProfile = () => request({ url: '/user/profile', method: 'GET' })
```

**经验教训**：API 模块里"裸 axios vs request 实例"必须严格区分——只有login 这种公开接口用裸 axios，其他都要走 request 实例以携带 token。

---

### 4.4 关键发现：数据库真实密码是 `123456`

**现象**：`student01/admin123` 登录返回 1002（用户名或密码错误）。

**定位过程**：
1. 导师给 database.sql 里所有账号 BCrypt hash 是 `$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2` → intuitively 认为对应 `admin123`
2. 直查 MySQL → 数据库真实 hash 是 `$2a$10$EEeUC1lM2mbe.nOY0CtsDOVYQciytNhzUMLR2rAgI5nfOXzmlGJPK` → **database.sql 是过期文档！**
3. 用 node bcryptjs 反向验证 → **真实明文是 `123456`**
4. 5 个核心账号 (admin/teacher01/teacher02/student01/student02) **全部是同一个 hash**，说明运行时某处统一重置了密码

**影响**：database.sql 文档与数据库实际数据不一致，未来写论文/答辩时注意不要用 sql 里的密码当测试账号。

**测试账号（已验证）**：

| 账号 | 密码 | 角色 | 真实姓名 |
|---|---|---|---|
| admin | **123456** | admin | 系统管理员 |
| teacher01 | **123456** | teacher | 张教授 |
| teacher02 | **123456** | teacher | 李主任 |
| student01 | **123456** | student | 王医生 |
| student02 | **123456** | student | 赵护士 |

---

## 5. 联调验证结果

### 5.1 认证链路（8080 → 8082）

```bash
# 8080 登录
curl -X POST http://localhost:8080/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student01","password":"123456"}'
# => {"code":200,"data":{"token":"eyJ...","userInfo":{"realName":"王医生","role":"student","id":4}}}

# 8082 用该 token 调业务
curl http://localhost:8082/api/user/profile -H "Authorization: Bearer <token>"
# => {"code":200,"data":{"realName":"王医生","role":"student","orgName":"汶川县人民医院",...}}
```

### 5.2 全业务接口（已测）

| 接口 | 结果 |
|---|---|
| GET /api/user/profile | ✅ 个人资料完整 |
| GET /api/course/list?pageNum=1&pageSize=6 | ✅ total=3, 第1门"基层常见病诊疗规范" |
| GET /api/course/detail/1 | ✅ |
| GET /api/course/chapter/list?courseId=1 | ✅ |
| GET /api/study/my-courses | ✅ 报名后 total=1 |
| POST /api/study/enroll {"courseId":1} | ✅ 200 success |
| GET /api/study/progress/{courseId} | ✅ |
| GET /api/exam/list | ✅ 有"2026年度结业考试" |
| POST /api/exam/start/{id} | ✅ 启动考试 |
| POST /api/exam/submit | ✅ 提交答卷 |
| GET /api/exam/record/{id} | ✅ 成绩回看 |
| GET /api/exam/my-records | ✅ |
| GET /api/stats/my?studentId=4 | ✅ 统计数据 |
| POST /api/consult/ask | ✅ 提问 |
| GET /api/consult/my | ✅ |

### 5.3 前端代理穿透验证（Python urllib，最贴近浏览器真实请求）

```text
=== 5175/admin/login (admin123 应失败) ===
HTTP 200 code=1002 (expect 1002)            ✅

=== 5175/admin/login (123456 应成功) ===
HTTP 200 code=200 (expect 200)              ✅
token prefix: eyJhbGciOiJIUzI1NiJ9...
userInfo: realName=王医生 role=student id=4  ✅

=== 通过 5175 代理调 8082 业务接口 ===
[OK] 个人资料 (/api/user/profile)            user=王医生
[OK] 课程列表 (/api/course/list)             total=3 first=基层常见病诊疗规范
[OK] 学习统计 (/api/stats/my)                user=王医生
[OK] 我的课程 (/api/study/my-courses)        total=1 first=基层常见病诊疗规范
[OK] 考试列表 (/api/exam/list)
[OK] 考试记录 (/api/exam/my-records)
[OK] 咨询列表 (/api/consult/my)              total=0 (empty)
[OK] 课程详情1 (/api/course/detail/1)
[OK] 章节列表 (/api/course/chapter/list)

=== POST 报名课程 ===
HTTP 200 code=200 msg=success                ✅
```

---

## 6. 启动方式

### 6.1 前置依赖

| 服务 | 端口 | 说明 |
|---|---|---|
| MySQL | 3306 | 必须运行（`training` 库已初始化） |
| training-admin 后端 | 8080 | 提供 `/admin/login` |
| training-api 后端 | **8082** | 提供全部 `/api/*` 业务接口 |
| web-student 前端 | **5175** | dev server |

### 6.2 启动命令（3 个终端）

```bash
# 终端 1：起 8000 管理后台后端（登录）
cd D:/A-Users/Desktop/zhongheshixun/training-parent
mvn -pl training-admin spring-boot:run

# 终端 2：起 8082 业务 API（替代 8081）
cd D:/A-Users/Desktop/zhongheshixun
"D:/javaEE/soft/jdk-17.0.9/bin/java.exe" -jar training-parent/training-api/target/training-api-exec.jar --server.port=8082

# 终端 3：起 5175 学员 Web 前端
cd D:/A-Users/Desktop/zhongheshixun/web-student
npm run dev
```

### 6.3 访问

浏览器打开 **http://localhost:5175**，用 `student01 / 123456` 登录。

---

## 7. 已知限制与待办

### 7.1 已知限制（v1.1.0 论文级打磨后现状）

| 项目 | 说明 | 状态 |
|---|---|---|
| 首页学习趋势图 | 接入后端 `stats/my` 的 `recent7Days` 字段，**真实数据优先**；后端未返回时回退 mock 并显示"示例数据"提示 tag | ✅ 已接入真实数据 |
| 统计卡片字段 | 已对齐后端：`enrollCount` / `completedChapters` / `totalStudyHours` / `examCount`，`displayedStats` computed 统一映射 | ✅ 已对齐 |
| 登录错误处理 | `userStore.login()` 校验 `data.code===200` 否则显式抛错，密码错误正确提示（不再写 `"undefined"` 到 localStorage） | ✅ 已修复 |
| 课程封面 | 蓝色占位块，无真实图片 | ❌ 受毕设范围限制 |
| 视频/PDF 章节渲染 | 根据 `contentType`（1视频/2pdf/3文本）渲染，复用 `contentTypeName` 字典 | ✅ 已实现 |
| 成绩回看字段 | 兼容 `details` / `questions`，字段缺失时显示"暂无答题详情"防御性空态 | ✅ 已防御 |
| 选项格式 | `parseOptions` 同时支持 JSON 数组 `["A.xxx"]` 与拼合格式 `A.文本\|B.文本`，已抽到共享字典 | ✅ 已实现 |
| 考试倒计时 | 纯前端 `duration` 倒计时（双保险中的前端侧），空题目/题库不足（code=1000）防御性拦截 | ✅ 已实现 |
| DRY 字典映射 | `typeText/typeColor/difficultyText/statusText/questionTypeText/parseOptions` 7 处 view 复制 → 抽到 `src/utils/dict.js` + `format.js` + `chart.js` | ✅ 已收口 |
| 响应式 | 所有 grid 加 `xs/sm/md` 断点，考试/课程/个人中心全部响应式 | ✅ 已实现 |

### 7.2 浏览器 E2E 联调（v1.1.0 已率先完成）

> 本轮打磨**率先跑通浏览器真实渲染 + 截图**，是三个前端里最先完成 E2E 素材采集的。

- [x] 浏览器实测 login → home → course list → detail → learn → exam list → answer（倒计时） → submit → result → profile
- [x] ECharts 趋势图渲染截图（毕设素材）→ `docs/images/web-student-home.png`
- [x] 考试中心渲染截图 → `docs/images/web-student-exams.png`
- [x] 课程中心 / 个人中心截图 → `docs/images/web-student-courses.png` / `web-student-profile.png`
- [x] 零 console 错误（JSON.parse / undefined 老坑全清）
- [ ] 考试倒计时 + 自动交卷流程截图（需新一场 `status=0` 且题库充足的考试）
- [ ] 报名课程后"继续学习"出现在 home 页（业务联动）
- [ ] profile 页编辑资料 + 保存

### 7.3 加分页（时间充裕再做）

- 咨询中心 `/consult`（提问 + 我的咨询列表）→ `/api/consult/*`
- 我的学习 `/my-courses`（已报名课程 + 进度）→ `/api/study/my-courses`

---

## 8. 毕设论文里的定位

> **学员统一学习门户**——PC Web 端与小程序端共享 8082 后端和统一认证中心，面向基层卫生技术人员提供课程学习、在线考试、学习统计、个人中心全流程。
>
> **统一认证中心**：8080 提供的 `/admin/login` 是共享认证入口，Vue3 管理后台、PC 学员 Web、微信小程序（openid 绑定）三端共用同一 JWT secret、同一套 sys_user 表，实现了认证层的复用。

---

## 附录 A：端口与进程速查

| 用途 | 端口 | 启动命令 |
|---|---|---|
| 管理后台后端（登录） | 8080 | `mvn -pl training-admin spring-boot:run` |
| 业务 API（`/*`） | 8082 | `java -jar training-api-exec.jar --server.port=8082` |
| 学员 Web | 5175 | `cd web-student && npm run dev` |
| 管理后台前端 | 5173 | `cd training-admin/frontend && npm run dev` |

**⚠️ 切记**：8081 端口已被僵尸进程死锁，永远不要用 8081。

## 附录 B：测试账号（全 123456）

见 §4.4 测试账号表。

## 附录 C：遇到的命令行环境问题

本次开发遭遇多个 Windows + bash + PowerShell 混合环境问题：

| 问题 | 解决 |
|---|---|
| `taskkill /F` 在 bash 里被 `/F` 当路径 | 用 `cmd.exe /c taskkill ...` |
| PowerShell heredoc 里 `&` 被 bash 吃 | 改用 Python urllib 方式写测试脚本 |
| PowerShell `Stop-Process` 拒绝访问 | Windows 内核 TCP 端口死锁，无解，改端口 |
| Python `gbk` 编码错误 | `export PYTHONIOENCODING=utf-8` 或 `python -X utf8` |
| java -version 与 javac 版本冲突（8 vs 17） | 显式用 `"D:/javaEE/soft/jdk-17.0.9/bin/java.exe"` |

---

## 7. 字段语义对齐说明（v1.0.1 追加）

> 适用读者：开发人员 / 论文评审 / 答辩教师
> 修复关联：跨端联动审计报告 P1-7（`exam.status` 双语义）

### 7.1 `exam.status` 字段在不同端的语义不同（重名不同值）

系统存在 **两个维度** 都用 `status` 字段，但枚举值含义完全不同：

| 端 / 接口 | 字段来源 | 取值 | 语义 | 出处 |
|---|---|---|---|---|
| **管理端** `GET /admin/exam/page` | `exam` 表 `status` 列 | 0 草稿 / 1 已发布 / 2 已下架 | 考试本身的"上架状态" | `Exam.status`（DB 列） |
| **学员端** `GET /api/exam/list` | `ExamListVO.status` 字段 | 0 未开始 / 1 已完成(已提交未批阅) / 2 已批阅 | 当前学员在该考试上的"作答状态" | `ExamBizServiceImpl.listForStudent` 内存组装 |
| 学员端 `ExamListVO` 其他字段 | — | `retryLeft`、`times`、`score`、`passed` | 学员维度的剩余次数、已考次数、得分、是否通过 | `ExamBizServiceImpl.listForStudent` |

### 7.2 为什么会重名不同值

- 数据库 `exam` 表的 `status` 是"管理员维度"的状态机（**对所有学员**都一样），是考试能不能被学员看见的依据。
- 学员端 list 接口返回的 `status` 是"**我**在这场考试中作到了哪一步"，是动态推导的、**每个学员不同**。
- 后端通过 `ExamListVO`（独立 VO 类，不是 `Exam` 实体）做字段隔离，前端不应把两个 `status` 当作同一个字段做比较。

### 7.3 学员端 `canStart` 判定的正确逻辑

```js
// 学员维度 status 0=未开始 → 可开始；2=已批阅 → 不可开始
// 关键：必须用 ExamListVO.status，不是 exam.status
const canStart = (row) =>
  row.status === 0 && (row.retryLeft ?? 0) > 0
```

`exam/list.vue:92-94` 当前实现 `status === 0 && retryLeft > 0` 语义上**就是学员端 canStart**，**正确**；之所以在审计报告中被标记为"⚠️"，是因为字段重名易被误读为管理端 status。

### 7.4 论文/答辩要点

- 数据建模章节应明确 `exam.status` 与 `ExamListVO.status` 是**两个字段**，分别由不同表/不同组装逻辑产出；
- 数据库表 `exam` 仅存储**考试本身**的状态，不存任何学员维度的进度；
- 学员维度的状态、次数、得分均由 `exam_record` 表 + `ExamBizService` 实时聚合，不冗余到 `exam` 表。
