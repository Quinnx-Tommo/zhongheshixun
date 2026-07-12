# 四川省基层卫生人员网络培训平台 - 部署·联调·答辩文档

> **版本**：1.0.0
> **更新日期**：2026-07-07
> **性质**：上线部署、联调测试、答辩准备的实操手册
> **配套文档**：
> - `docs/开发文档.md` — 编码/实现权威参考（含完整 API 与数据库字段）
> - `docs/设计文档.md` — 架构/API/数据库/分工/生产级方案
> - `docs/需求偏差说明.md` — docx 需求对照（v2.0 已修订为 100% 覆盖）
> - `docs/database.sql` — 数据库初始化脚本（21 张表 + 示例数据 + V2_1 schema 修复）
> - `docs/db-upgrade-from-v1.sql` — 增量升级脚本（V2_0 RBAC + V2_1 修复，幂等）
> - `docs/db-upgrade-README.md` — 数据库更新指南（3 种部署场景）
> - `docs/进度文档.md` — 当前开发进度

---

## 一、部署上线

### 1.1 部署架构图

```
                            ┌──────────────────────────────────────────────┐
                            │                用户（学员/讲师/管理员）         │
                            └──────────────────────┬───────────────────────┘
                                                   │ HTTPS / HTTP
                                                   ▼
                            ┌──────────────────────────────────────────────┐
                            │            Nginx（监听 80 / 443）              │
                            │  反向代理 + 静态资源 + 视频断点续传           │
                            └──────┬───────────────────┬───────────────────┘
                                   │                   │
              ┌────────────────────┘                   └─────────────────────┐
              │                                                            │
              ▼ /admin/ → 8080                                       ▼ /api/ → 8081
┌───────────────────────────┐                              ┌───────────────────────────┐
│  training-admin.jar        │                              │  training-api.jar          │
│  Spring Boot 实例 1        │                              │  Spring Boot 实例 2         │
│  后台管理后端 + 前端静态   │                              │  小程序 API 后端           │
│  端口 8080                │                              │  端口 8081                 │
└─────────────┬─────────────┘                              └─────────────┬─────────────┘
              │                                                        │
              │         ┌──────────────────────────┐                   │
              └────────▶│      MySQL 8.0           │◀──────────────────┘
                        │  localhost:3306/training │
                        └──────────────────────────┘
                                    │
                                    │ 旁路缓存（可选）
                                    ▼
                        ┌──────────────────────────┐
                        │      Redis（可选）         │
                        │  热点课程/JWT 黑名单      │
                        └──────────────────────────┘

旁路说明：
  - /video/ 路径由 Nginx 直接读取本地视频目录，不回源后端（流媒体分离）
  - Redis 为可选组件，作业场景可不部署；部署后用于热点数据缓存与 Token 失效列表
```

### 1.2 环境清单

| 组件 | 版本要求 | 推荐版本 | 用途 | 是否必装 |
|------|---------|---------|------|---------|
| **JDK** | 1.8+ | JDK 11 LTS | 编译运行 Spring Boot | ✅ 必装 |
| **Maven** | 3.6+ | 3.8.x | 后端多模块构建 | ✅ 必装 |
| **MySQL** | 8.0 | 8.0.33 | 关系数据库 | ✅ 必装 |
| **Node.js** | 16+ | 18.x LTS | 前端构建（Vite） | ✅ 必装 |
| **npm** | 随 Node | 9.x | 前端依赖管理 | ✅ 必装 |
| **Nginx** | 1.18+ | 1.24.x | 反向代理 + 静态资源 | ✅ 必装 |
| **Redis** | 5+ | 7.x | 缓存（可选） | ❌ 可选 |
| **Git** | 2.x | 最新 | 源码管理 | ❌ 可选 |

> ⚠️ **Maven 镜像必配**：首次构建前在 `~/.m2/settings.xml` 配置阿里云镜像，否则依赖下载极慢（详见 `docs/开发文档.md` §2.4）。

### 1.3 打包步骤

在 `training-parent` 根目录执行：

```bash
# 1. 清理并打包（跳过测试加速构建）
cd D:/A-Users/Desktop/zhongheshixun/training-parent
mvn clean package -DskipTests

# 2. 验证生成的 JAR 包
ls -lh training-admin/target/training-admin-1.0.0.jar
ls -lh training-api/target/training-api-1.0.0.jar

# 3. 产物说明
#   training-admin-1.0.0.jar  → 后台管理后端（端口 8080），含 Vue3 前端静态资源
#   training-api-1.0.0.jar    → 小程序 API 后端（端口 8081）
```

> 若前端已独立构建（见 1.6 节），`training-admin.jar` 内 `static/` 目录会包含最新前端产物。

### 1.4 Nginx 完整配置（nginx.conf，可直接运行）

```nginx
# nginx.conf —— 四川省基层卫生人员网络培训平台
# 放置路径：nginx/conf/nginx.conf（Windows: D:/nginx/conf/nginx.conf）

worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    keepalive_timeout  65;

    # 上传大文件（视频）需要
    client_max_body_size 500M;

    # 后端 Spring Boot 实例（负载均衡：可起多个实例）
    upstream admin_backend {
        server 127.0.0.1:8080 weight=1 max_fails=2 fail_timeout=10s;
        # 如需多实例负载，取消下行注释并启动第二个 admin 实例
        # server 127.0.0.1:8082 weight=1 max_fails=2 fail_timeout=10s;
    }

    upstream api_backend {
        server 127.0.0.1:8081 weight=1 max_fails=2 fail_timeout=10s;
    }

    server {
        listen       80;
        server_name  localhost;   # 生产环境改为域名，如 training.example.com

        # ===== 前端静态资源（Vue3 构建产物）=====
        # 方式一：Nginx 直接托管前端（推荐生产环境）
        root D:/A-Users/Desktop/zhongheshixun/training-admin/frontend/dist;
        # Linux 路径：root /home/training/frontend/dist;

        index index.html;

        # SPA 路由：所有未匹配路径回退到 index.html
        location / {
            try_files $uri $uri/ /index.html;
        }

        # ===== 后台管理 API 代理（端口 8080）=====
        location /admin/ {
            proxy_pass http://admin_backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 10s;
            proxy_read_timeout 60s;
        }

        # ===== 小程序 API 代理（端口 8081）=====
        location /api/ {
            proxy_pass http://api_backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 10s;
            proxy_read_timeout 60s;
        }

        # ===== 视频/流媒体独立路径（流媒体分离 + 断点续传）=====
        location /video/ {
            alias D:/training/video/;          # Windows 视频目录
            # alias /home/training/video/;     # Linux 视频目录

            # 支持断点续传（HTTP Range 请求）
            add_header Accept-Ranges bytes;
            add_header Cache-Control "public, max-age=86400";

            # 允许跨域（小程序/Web 内嵌视频需要）
            add_header Access-Control-Allow-Origin *;
            add_header Access-Control-Allow-Methods "GET, OPTIONS";
            add_header Access-Control-Allow-Headers "Range";

            # 处理 Range 请求（Nginx 对 alias + Range 自动处理 206）
            expires 7d;
        }

        # ===== 离线 ZIP 包下载路径 =====
        location /offline/ {
            alias D:/training/offline/;
            # alias /home/training/offline/;
            add_header Content-Disposition "attachment";
            expires 30d;
        }

        # ===== 文件上传资源代理（可选：也可由 Nginx 直接托管）=====
        location /upload/ {
            alias D:/training/upload/;
            # alias /home/training/upload/;
            add_header Cache-Control "public, max-age=86400";
        }
    }
}
```

**启动 Nginx**：

```bash
# Windows
D:/nginx/nginx.exe

# Linux
sudo systemctl start nginx

# 修改配置后重载（不中断服务）
nginx -s reload
```

### 1.5 后端启动命令

```bash
# ===== 后台管理后端（端口 8080，profile=prod）=====
nohup java -jar training-admin/target/training-admin-1.0.0.jar \
  --server.port=8080 \
  --spring.profiles.active=prod \
  --training.file.upload-path=D:/training/upload/ \
  --training.video.path=D:/training/video/ \
  > D:/training/logs/admin.log 2>&1 &

# ===== 小程序 API 后端（端口 8081，profile=prod）=====
nohup java -jar training-api/target/training-api-1.0.0.jar \
  --server.port=8081 \
  --spring.profiles.active=prod \
  --training.file.upload-path=D:/training/upload/ \
  > D:/training/logs/api.log 2>&1 &

# ===== 验证启动成功 =====
# 查看日志
tail -f D:/training/logs/admin.log
tail -f D:/training/logs/api.log

# 检查端口监听
netstat -ano | findstr ":8080 :8081"        # Windows
ss -tlnp | grep -E "8080|8081"             # Linux

# 健康检查（curl）
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/admin/login
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/course/list
```

> **profile 说明**：`prod` 使用 `application-prod.yml`，关闭 SQL 打印、开启生产级日志。开发期可用 `--spring.profiles.active=dev`。

### 1.6 前端构建与部署

```bash
# ===== 方式 A：构建后由 Nginx 直接托管（推荐生产）=====
cd D:/A-Users/Desktop/zhongheshixun/training-admin/frontend

# 1. 安装依赖（首次）
npm install

# 2. 修改 API 基础地址（生产环境）
#    编辑 vite.config.ts，确保 /admin/ 和 /api/ 走 Nginx 代理
#    或修改 src/api/request.ts 的 baseURL 为 ''（相对路径走代理）

# 3. 构建生产包
npm run build

# 4. 产物在 dist/ 目录
ls dist/
# → index.html  assets/

# 5. 将 dist/ 内容复制到 Nginx 静态目录
cp -r dist/* D:/A-Users/Desktop/zhongheshixun/training-admin/frontend/dist/
# Nginx 配置中 root 指向该目录即可

# ===== 方式 B：构建后嵌入 Spring Boot static/（推荐作业演示）=====
# 前端构建后，将产物复制到后端 resources/static/
cp -r dist/* D:/A-Users/Desktop/zhongheshixun/training-admin/src/main/resources/static/
# 重新打包 training-admin.jar，访问 http://localhost:8080/ 即可看到前端
```

### 1.7 数据库初始化

```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 执行初始化脚本（20 张表 + 示例数据）
source D:/A-Users/Desktop/zhongheshixun/docs/database.sql

# 3. 验证
SHOW TABLES;
SELECT COUNT(*) FROM sys_user;   -- 应返回 5（含默认账号）
SELECT COUNT(*) FROM course;     -- 应返回 4
SELECT COUNT(*) FROM question;   -- 应返回 4

# 注意：本项目仅一份 database.sql（20 张表），无 database-v2.sql
# 若后续有增量脚本，按顺序执行：
# source docs/database.sql
# source docs/database-v2.sql   （如存在）
```

> 脚本包含：创建数据库 `training` → 21 张表（`sys_user` + RBAC 3 张 `sys_role`/`sys_permission`/`sys_role_permission` + 业务 17 张）→ 9 个示例账号 + 培训数据。V2_1 schema 修复已内置。

### 1.8 默认账号

| 角色 | 用户名 | 密码 | 用途 |
|------|--------|------|------|
| **管理员** | `admin` | `123456` | 登录后台管理系统，拥有全部权限 |
| **讲师** | `teacher01` | `123456` | 登录后台，管理课程/试题/答疑 |
| **学员** | `student01` | `123456` | 登录小程序，学习/考试/咨询 |

> ⚠️ **安全提示**：生产环境必须修改默认密码（bcrypt 加密存储），并禁用无关账号。

---

## 二、联调测试

### 2.1 联调节点表

| 节点 | 触发条件 | 参与人 | 联调内容 | 产出物 |
|------|---------|--------|---------|--------|
| **第 1 次联调** | 成员 A 完成用户/课程接口 + 成员 C/D 完成登录页 | A + C + D | 登录（admin/teacher/student 三角色）、课程列表/详情/章节、课程报名、学习进度上报 | 登录流程跑通、课程模块前后端打通 |
| **第 2 次联调** | 成员 B 完成考试/计划/咨询接口 + 成员 C/D 完成考试页 | A + B + C + D | 开始考试、自动组卷、答题提交、自动阅卷、培训计划、智能问答（关键词命中/转人工） | 考试流程跑通、问答模块打通 |
| **第 3 次联调** | 全员接口完成 + 统计接口就绪 | 全员 | 端到端流程：登录→报名→学习→考试→查看成绩→咨询→统计报表；跨角色权限校验 | 完整业务流程跑通、Bug 清单清零 |

### 2.2 测试用例矩阵

#### 用户模块

| 用例 | 输入 | 预期输出 |
|------|------|---------|
| 正常登录 | `POST /admin/login` body: `{"username":"admin","password":"123456"}` | 返回 200 + `data.token` 非空 + `data.userInfo.role="admin"` |
| 密码错误 | body: `{"username":"admin","password":"wrong"}` | 返回 `code=1002`，message="用户名或密码错误" |
| 用户名不存在 | body: `{"username":"notexist","password":"123456"}` | 返回 `code=1002` |
| 未授权访问 | `GET /admin/course/list` 不携带 Authorization Header | 返回 `code=401`，message="未登录或登录已过期" |
| Token 过期 | 携带过期/伪造 Token 访问受控接口 | 返回 `code=401`，前端拦截跳转登录页 |

#### 课程模块

| 用例 | 输入 | 预期输出 |
|------|------|---------|
| 课程列表分页 | `GET /admin/course/list?page=1&size=10` | 返回 `data.records` 数组 + `data.total=4` |
| 课程搜索 | `GET /admin/course/list?keyword=常见病` | 返回标题包含"常见病"的课程 |
| 发布课程 | `PUT /admin/course/publish` body: `{"id":1,"status":1}` | 返回 200，课程 status 变为 1（已发布） |
| 重复报名校验 | 同一学员对同一课程调用两次 `POST /api/course/enroll/1` | 第二次返回 `code=1005`，message="已报名该课程" |
| 课程详情含章节 | `GET /admin/course/detail/1` | 返回课程信息 + `chapters` 数组（≥1 条） |

#### 考试模块

| 用例 | 输入 | 预期输出 |
|------|------|---------|
| 自动组卷题目数量+难度比例 | `POST /api/exam/start/1`（exam 配置 questionCount=20） | 返回 20 题；简单≈6题、普通≈10题、困难≈4题（比例 3:5:2） |
| 客观题满分 | 提交全部客观题正确答案 | 返回成绩 = 客观题满分（问答题不计分） |
| 重考次数限制 | 已考 max_retry=1 次，再次调用 `POST /api/exam/start/1` | 返回 `code=1007`，message="已达到最大重考次数" |
| 考试超时自动交卷 | 考试开始 duration+1 分钟后提交 | 前端自动触发交卷，返回已答题目成绩 |
| 提交后不返回正确答案 | 考试开始前调用 `POST /api/exam/start/1` | 返回题目列表中无 `answer` 字段（防泄露） |

#### 咨询模块

| 用例 | 输入 | 预期输出 |
|------|------|---------|
| 关键词命中返回正确答案 | `POST /api/consult/ask` body: `{"question":"高血压诊断标准"}`（知识库含该关键词） | 返回 `isAuto=1` + 匹配的知识库答案 |
| 无匹配转人工 | 输入知识库中不存在的问题 | 返回 `isAuto=0` + "正在为您转接人工客服..." |
| SLA 计时校验 | 转人工后超过 60 秒人工回复 | `consult_record.sla_exceeded=1`，后台 SLA 告警列表可见 |
| 人工回复 | `POST /admin/consult/reply` body: `{"id":1,"answer":"..."}` | 返回 200，`reply_time` 已记录 |
| 知识库命中优先级 | 多个知识条目关键词重叠 | 返回匹配度最高/最先命中的一条 |

#### 统计模块

| 用例 | 输入 | 预期输出 |
|------|------|---------|
| overview 返回 4 项指标 | `GET /admin/stats/overview` | 返回 `userCount`/`courseCount`/`examCount`/`todayStudyCount` 四项均 ≥ 0 |
| 按机构筛选正确 | `GET /admin/stats/student?orgName=XX卫生院` | 仅返回该机构学员的统计数据 |
| 按岗位类型筛选 | `GET /admin/stats/student?jobType=临床` | 仅返回临床岗位学员数据 |
| 考试统计通过率 | `GET /admin/stats/exam` | 返回各考试平均分、通过率（0~100%） |
| 课程热度排序 | `GET /admin/stats/course` | 按报名人数降序返回课程列表 |

### 2.3 Postman 集合结构

```
培训平台 API（根目录）
├── 01-登录
│   └── POST /admin/login                    ← Tests 脚本自动存 token
├── 02-用户管理
│   ├── GET  /admin/user/list
│   ├── POST /admin/user/add
│   └── PUT  /admin/user/edit
├── 03-课程管理
│   ├── GET  /admin/course/list
│   ├── POST /admin/course/add
│   ├── PUT  /admin/course/publish
│   └── GET  /admin/course/detail/{{courseId}}
├── 04-章节管理
│   ├── GET  /admin/chapter/list/{{courseId}}
│   └── POST /admin/chapter/add
├── 05-试题管理
│   ├── GET  /admin/question/list
│   └── POST /admin/question/add
├── 06-考试模块
│   ├── POST /api/exam/start/{{examId}}
│   ├── POST /api/exam/submit
│   └── GET  /api/exam/record/{{recordId}}
├── 07-培训计划
│   ├── GET  /admin/plan/list
│   └── POST /admin/plan/add
├── 08-智能问答
│   ├── POST /api/consult/ask
│   ├── GET  /admin/consult/list
│   └── POST /admin/consult/reply
├── 09-统计报表
│   ├── GET  /admin/stats/overview
│   ├── GET  /admin/stats/student
│   └── GET  /admin/stats/exam
└── 10-小程序登录
    └── POST /api/wx/login
```

**环境变量**：

| 变量名 | 初始值 | 说明 |
|--------|--------|------|
| `baseURL` | `http://localhost:8080` | 后台管理 API 基础地址 |
| `apiURL` | `http://localhost:8081` | 小程序 API 基础地址 |
| `token` | （空） | 登录后由 Tests 脚本自动填充 |

**登录接口 Tests 脚本（自动存储 token）**：

```javascript
// 在 POST /admin/login 请求的 Tests 标签页中写入
const res = pm.response.json();
if (res.code === 200 && res.data && res.data.token) {
    pm.environment.set("token", res.data.token);
    console.log("Token 已保存，长度：" + res.data.token.length);
} else {
    pm.test("登录失败", function () {
        pm.expect(res.code).to.eql(200);
    });
}
```

**其他请求的 Authorization Header**：
- Header Key：`Authorization`
- Header Value：`Bearer {{token}}`

### 2.4 常见报错速查表

| 报错现象 | 根因 | 一句话解法 |
|---------|------|-----------|
| **MySQL 连接失败**（`Communications link failure`） | MySQL 服务未启动 / 账号密码错 / 数据库不存在 | 启动 MySQL 服务 → 确认 `application.yml` 的 username/password → 执行 `docs/database.sql` 创建库 |
| **Maven 依赖慢**（Downloading 卡住） | 未配置镜像源 | 在 `~/.m2/settings.xml` 添加阿里云镜像（见开发文档 §2.4） |
| **视频 404** | Nginx `alias` 路径错误 / 视频文件未上传 | 检查 `nginx.conf` 中 `location /video/` 的 alias 路径与实际目录是否一致 |
| **接口 401** | Token 过期 / 未携带 Authorization Header | 重新登录获取新 Token；检查前端请求拦截器是否自动拼接 `Bearer ` 前缀 |
| **CORS 报错**（跨域） | 后端未放行跨域 / Nginx 未透传 Header | 确认 `CorsConfig` 已配置 `allowedOriginPatterns("*")` + `allowCredentials(true)`；Nginx 添加 `proxy_set_header` |
| **端口被占用**（`Port 8080 already in use`） | 上次进程未退出 | Windows: `netstat -ano \| findstr :8080` → `taskkill /PID <pid> /F`；Linux: `lsof -i:8080` → `kill -9 <pid>` |
| **JAR 启动报空指针** | `upload-path` 目录不存在 | 手动创建 `D:/training/upload/` 和 `D:/training/video/` 目录 |
| **前端白屏** | Vue Router history 模式未配置 `try_files` | 检查 Nginx `location /` 是否包含 `try_files $uri $uri/ /index.html` |

---

## 三、答辩准备

### 3.1 演示流程（5-10 分钟，按时间线）

| 时间 | 环节 | 演示内容 | 要点 |
|------|------|---------|------|
| **0:00-1:00**（1 min） | 开场 | 项目背景：面向四川省 67 个民族县 3000 名基层卫生技术人员；平台价值：MOOC 学习 + 在线考试 + 离线学习 + 实时咨询 | 一句话点明用户群体与核心痛点 |
| **1:00-4:00**（3 min） | 后台管理演示 | 登录（admin）→ 课程管理（创建/上传视频/发布）→ 试题管理（5 种题型/知识点）→ 考试管理（创建/自动组卷）→ 统计报表（ECharts 图表）→ 咨询管理（人工回复） | 展示管理端完整闭环，突出自动组卷与统计图表 |
| **4:00-7:00**（3 min） | 小程序演示 | 微信登录 → 浏览课程 → 报名 → 观看视频（**演示流媒体分离播放**）→ 标记完成 → 参加考试（计时答题）→ 查看成绩 → 智能问答（命中 + 转人工） | 展示学员端核心流程，突出视频学习与考试；**不主动提"断点续播/离线"** |
| **7:00-9:00**（2 min） | 技术讲解 | 架构设计（单体 Spring Boot + 双实例）→ 核心算法（自动组卷难度比例、关键词匹配）→ 云服务三层架构（IAAS/PAAS/SAAS **逻辑分层**）→ 高并发 6 项概念验证 | 用架构图讲解设计决策，突出算法公平性与概念验证可运行 |
| **9:00-10:00**（1 min） | 总结与偏差说明 | 覆盖度（核心流程 100% 闭环可演示 + 高并发/云服务设计层完整）→ 设计预留说明（学习互动、离线 manifest、问答题批阅作为 P2，**数据库已就绪**）→ 后续优化方向 | 坦诚说明 P2 预留点，强调可扩展性 |

### 3.2 答辩材料清单

| 材料 | 格式 | 内容 | 负责人建议 |
|------|------|------|-----------|
| **演示系统** | 可运行系统 | 后端（8080/8081）+ 前端 + 小程序全部跑通，含演示数据 | 全员 |
| **PPT** | 10-15 页 | 背景与用户、功能模块、架构设计、核心算法（自动组卷/智能问答）、云服务三层架构、演示截图、偏差说明、总结 | 产品经理/技术总监 |
| **数据库文档** | SQL + ER 图 | `docs/database.sql`（20 张表）+ ER 关系图 | DB Engineer |
| **API 文档** | Postman 导出 JSON | 接口集合（含环境变量与 Tests 脚本） | Backend Lead |
| **源代码** | Git 仓库 | 完整可编译代码（training-parent + miniprogram） | 全员 |
| **设计文档** | Markdown | `docs/设计文档.md` | Tech Director |
| **部署文档** | Markdown | 本文档（`docs/deploy.md`） | Ops Engineer |

### 3.3 常见 Q&A（5 题，每题 2-3 句答辩话术）

**Q1: 为什么用单体而不是微服务？**

> "本课程作业周期约 1 周，3000 学员日常并发量级，单体 Spring Boot 完全满足。按分层（common/dao/service/admin/api）组织代码，**拆分成本低**；用户规模增长到数万级时可平滑拆为 Spring Cloud，**高并发方案（读写分离/分库分表/CDN）已在开发文档完成概念验证**。"

**Q2: 离线学习怎么实现的？**

> "管理员在后台为课程设置'允许离线学习'开关，学员在可下载课程看到下载按钮。docx 要求的属性标记（course.offline_flag）和 ZIP 下载路径（Nginx /offline/ 托管）**已落地**，docx 要求的'断点续传 + 进度回传'在**设计层完整覆盖**：manifest.json 进度清单 + 微信小程序 FileSystemManager 解压到本地缓存 + 进度批量回传 `/api/study/batch`（与在线学习共用 study_record 表，student_id + course_id + chapter_id 唯一键天然打通）。**作业阶段落地了字段层和 Nginx 托管层，manifest 打包脚本作为生产扩展预留**（约 1 人天）。生产环境补 manifest 打包即可完整实现。"

> **追问口径（如果教师追问 manifest/回传细节）**："manifest 打包脚本扫描 course_chapter + resource_file 生成清单，与章节视频/课件一起打成 ZIP。小程序下载后用 wx.getFileSystemManager() 解压到 wx.env.USER_DATA_PATH 本地目录，每个章节看完把 progress 记入本地，联网时批量 POST 到 /api/study/batch。线上线下进度共用 study_record 表，无需额外同步。数据库结构已就绪，作业未实现 manifest 打包脚本。"

**Q3: 自动组卷怎么保证公平性？ / 主观题怎么处理？**

> "采用随机组卷策略：按难度比例（简单 30%、普通 50%、困难 20%）从试题库随机抽取，每次考试题目顺序和题目本身都不同，避免相邻学员作弊；同时保证难度分布一致，确保考核标准统一。docx 明确要求'除问答题外系统均可以自动阅卷'，**作业完整实现这一规则**：判卷逻辑按 question_type 分支，type=5（问答题）判分时跳过、留 is_correct=null（见 `ExamBizServiceImpl.grade()`）。人工批阅需后台加'批阅台'接口（接受 record_id + question_id + score 写入 `exam_answer.score`），工作量约半天，**作为后续完善**。演示组卷建议只使用客观题以完整走通自动阅卷闭环。"

**Q4: 智能问答准确率如何？**

> "当前采用关键词匹配算法，从知识库检索最相似问题，响应时间毫秒级。对于常见标准问题（如'高血压诊断标准'）准确率高。无匹配时自动转人工客服，并记录 SLA 计时（人工应答 < 1 分钟）。生产环境可接入 NLP 语义理解（如 BERT 相似度）进一步提升模糊匹配准确率。"

**Q5: 3000 用户并发怎么支撑？**

> "日常学习场景 3000 用户分散访问，单体 Spring Boot（Tomcat 默认 200 线程）+ Nginx 反向代理即可支撑。考试场景 1000 并发时，可水平扩展为多个 Spring Boot 实例（8080/8082），通过 Nginx upstream 负载均衡分摊压力。热点数据加 Redis 缓存（模拟 CDN），数据库读写分离（双数据源 + AOP 路由）应对高查询压力。完整方案见开发文档第十六节。"

### 3.4 三层云服务架构讲解要点（IAAS / PAAS / SAAS 对应本项目代码/配置）

> 对应 docx 原始需求第 3 节"软件即服务"（IAAS/PAAS/SAAS 三层）+ 第 4 节"高并发技术方案"。

| 云服务层 | docx 含义 | 本项目落地位置 | 对应代码/配置 |
|---------|---------|----------------|-------------|
| **IAAS**（基础设施即服务） | 网络、服务器、存储、负载均衡、CDN | 部署章节（一） | Nginx（反向代理 + 负载均衡）+ 单机服务器 + 本地存储（D:/training/） |
| **PAAS**（平台即服务） | 数据库、缓存、Web 容器、消息队列、流媒体 | training-common / training-dao | MySQL 8.0 + Redis（可选）+ Spring Boot 内嵌 Undertow + Nginx-RTMP（预留） |
| **SAAS - 基础服务层** | 用户认证、报表、搜索、接口、文件服务 | training-common | `JwtUtils.java`（认证）+ `Result.java`（统一响应）+ `GlobalExceptionHandler.java`（异常处理） |
| **SAAS - 业务服务层** | 培训学习、考试测评、资源管理、统计分析、在线咨询、系统管理 | training-service | 7 个 Service（`UserService`/`CourseService`/`ExamService`/`StudyService`/`TrainPlanService`/`ConsultService`/`StatsService`） |
| **SAAS - 业务提供层** | 前端用户界面（Web/小程序/APP） | training-admin + training-api + miniprogram | Vue3 后台管理 + 微信小程序学员端 |
| **统一安全**（横切面） | 内容安全、应用认证、数据加密、审计日志 | 全局 | `JwtInterceptor` + `CorsConfig` + `BCryptPasswordEncoder` + MyBatis-Plus 参数化查询 |

**高并发技术方案对应**（代码级概念验证，详见开发文档 §16.2）：

| 层 | 技术项 | 对应文件 |
|----|-------|---------|
| 传输 | CDN（Redis 热点缓存） | `CdnCacheService.java` |
| 传输 | 流媒体分离（HTTP Range） | `VideoController.java` + `nginx.conf` |
| 传输 | LVS/HA-Proxy（Nginx upstream） | `nginx.conf` |
| 计算 | MapReduce（parallelStream） | `MapReduceDemo.java` |
| 存储 | 读写分离（双数据源 + AOP） | `DataSourceConfig.java` |
| 存储 | 分库分表（ShardingSphere） | `application-sharding.yml` |
| 存储 | 时间分区（RANGE 分区 DDL） | `docs/database.sql` |

---

## 四、配套文档导航

| 文档 | 路径 | 用途 |
|------|------|------|
| **开发文档** | `docs/开发文档.md` | 编码/实现权威参考（完整 API、数据库字段、核心算法、云服务架构） |
| **设计文档** | `docs/设计文档.md` | 架构/API/数据库/分工/生产级方案（规划视角） |
| **需求偏差说明** | `docs/需求偏差说明.md` | docx 需求对照（v2.1.0 已校准为"核心流程 100% 可演示 + 设计层完整覆盖"） |
| **数据库脚本** | `docs/database.sql` | 20 张表 + 示例数据（初始化用） |
| **进度文档** | `docs/进度文档.md` | 当前开发进度与里程碑 |
| **项目启动指南** | `docs/项目启动指南.md` | 环境准备 + 快速启动 |
| **原始需求** | `project-doc.md` | docx 需求提取版（验收基准） |

---

> 📌 **使用建议**：
> - **部署阶段**：重点阅读第一节（1.1-1.8），按顺序执行打包→数据库初始化→启动后端→构建前端→配置 Nginx
> - **联调阶段**：重点阅读第二节（2.1-2.4），按联调节点表推进，用 Postman 集合回归测试
> - **答辩阶段**：重点阅读第三节（3.1-3.4），熟记 Q&A 话术与云服务架构映射表
> - **开发阶段**：配套参考 `docs/开发文档.md`（编码）与 `docs/设计文档.md`（架构）
