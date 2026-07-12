# 四川省基层卫生人员网络培训平台

基于 Spring Boot 2.7 + Vue 3 + 微信小程序的基层卫生人员继续教育培训平台，面向四川省 67 个民族县 3000 名基层卫生技术人员。

## 技术栈

- **后端**：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3 + MySQL 8.0 + Redis + JWT
- **后台管理前端**：Vue 3 + TypeScript + Element Plus（端口 5176）
- **PC 学员端**：Vue 3 + Vite（端口 5174）
- **学员移动端**：微信小程序原生
- **构建**：Maven + Vite

## 快速开始

```bash
# 1. 初始化数据库
mysql -u root -p < docs/database.sql

# 2. 启动后端
cd training-parent
mvn clean install -DskipTests
java -jar training-admin/training-admin-exec.jar   # 后台 API 9898
java -jar training-api/training-api-exec.jar       # 业务 API 9899

# 3. 启动后台管理
cd training-admin-ui
npm install && npm run dev                        # 5176

# 4. 启动学员端
cd web-student
npm install && npm run dev                        # 5174
```

测试账号：`admin / teacher01 / teacher02 / student01-06`，统一密码 `123456`

## 文档

- [设计文档](docs/设计文档.md)
- [开发文档](docs/开发文档.md)
- [需求偏差说明](docs/需求偏差说明.md)
- [部署文档](docs/deploy.md)
- [完整文档索引](docs/)

## 目录结构

```
zhongheshixun/
├── training-parent/       # Maven 后端父工程
├── training-admin-ui/     # 后台管理前端
├── web-student/           # PC 学员端
├── miniprogram/           # 微信小程序
├── docs/                  # 权威开发文档
├── 专业文档/              # 正式交付文档
└── database.sql           # 数据库脚本
```

## 许可证

仅用于毕设演示，未经授权不得商用。
