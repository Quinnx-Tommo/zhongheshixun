# API 接口清单

> **版本**: 1.0.0
> **日期**: 2026-07-07
> **对应项目**: 四川省基层卫生人员网络培训平台
> **配套文档导航**:
> - 后端实现手册: `docs/dev-backend.md`
> - 前端实现手册: `docs/dev-frontend.md`
> - 小程序实现手册: `docs/dev-miniapp.md`
> - 数据库设计: `docs/dev-database.md`
> - 部署手册: `docs/dev-deploy.md`

---

## 1. 通用约定

### 1.1 服务端口与路径前缀

| 服务 | 基础 URL | 路径前缀 | 说明 |
|------|----------|----------|------|
| 后台管理后端 | `http://localhost:9898` | `/admin` | Vue3 后台管理调用 |
| 小程序 API | `http://localhost:9899` | `/api` | 微信小程序调用 |

### 1.2 认证方式

除登录接口外，所有接口需在请求头携带 JWT Token：

```
Authorization: Bearer <token>
```

未登录或 Token 失效返回 `401`，无权限返回 `403`。

### 1.3 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

**分页接口** `data` 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [ ... ],
    "total": 100
  }
}
```

### 1.4 错误码

| code | 含义 |
|------|------|
| 200 | 成功 |
| 401 | 未登录或 Token 失效 |
| 403 | 无权限 |
| 5xx | 服务端错误 |
| 1001 | 用户名已存在 |
| 1002 | 登录失败（用户名或密码错误） |
| 1005 | 已报名 |
| 1007 | 重考次数超限 |

### 1.5 公共请求参数约定

- 分页接口统一使用 Query 参数：`pageNum`（默认 1）、`pageSize`（默认 10）。
- 路径参数使用 RESTful 风格：`/{id}`。
- 时间字段统一使用 `yyyy-MM-dd HH:mm:ss` 格式。

---

## 2. 公共 - 登录

### 2.1 后台管理登录

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/login` |
| 请求参数 (JSON) | `{ "username": "string", "password": "string" }` |
| 响应 (JSON) | `{ "token": "string", "userInfo": { "id": 1, "username": "admin", "realName": "管理员", "role": "admin" } }` |
| 说明 | 用户名密码校验通过后签发 JWT Token。连续 5 次失败锁定 15 分钟。 |

**请求示例：**

```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "role": "admin"
    }
  }
}
```

### 2.2 微信小程序登录

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /api/wx/login` |
| 请求参数 (JSON) | `{ "code": "string", "nickname": "string", "avatar": "string" }` |
| 响应 (JSON) | `{ "token": "string", "userInfo": { "id": 1, "nickname": "string", "avatar": "string", "role": "student" } }` |
| 说明 | 通过微信 `code` 换取 `openid`，自动注册/绑定学员账号。 |

**请求示例：**

```json
{
  "code": "081ZJZ000Wn2hv1aQp210x",
  "nickname": "张医生",
  "avatar": "https://..."
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 10001,
      "nickname": "张医生",
      "avatar": "https://...",
      "role": "student"
    }
  }
}
```

---

## 3. 用户模块

### 3.1 用户列表（分页 + 搜索）

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/user/list` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`role`（可选：admin/teacher/student）、`realName`（可选，模糊搜索） |
| 响应 (JSON) | 分页 `data.records` 为用户列表，含 `id, username, realName, role, orgName, jobType, status, createTime` |
| 说明 | 按角色或姓名搜索用户。 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "role": "admin",
        "orgName": null,
        "jobType": null,
        "status": 1,
        "createTime": "2026-07-07 10:00:00"
      }
    ],
    "total": 1
  }
}
```

### 3.2 新增用户

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/user/add` |
| 请求参数 (JSON) | `{ "username": "string", "password": "string", "realName": "string", "role": "string", "orgName": "string", "jobType": "string", "phone": "string" }` |
| 响应 (JSON) | 新建用户信息 |
| 说明 | 用户名唯一，重复返回 1001。密码 bcrypt 加盐存储。 |

**请求示例：**

```json
{
  "username": "teacher01",
  "password": "Teacher@123",
  "realName": "李老师",
  "role": "teacher",
  "orgName": "XX县人民医院",
  "jobType": "全科医生",
  "phone": "13800000000"
}
```

### 3.3 编辑用户

| 项目 | 说明 |
|------|------|
| 接口路径 | `PUT /admin/user/edit` |
| 请求参数 (JSON) | `{ "id": 1, "realName": "string", "orgName": "string", "jobType": "string", "phone": "string", "status": 1 }` |
| 响应 (JSON) | 更新后用户信息 |
| 说明 | 不可修改 username，密码通过单独接口重置。 |

### 3.4 删除用户

| 项目 | 说明 |
|------|------|
| 接口路径 | `DELETE /admin/user/{id}` |
| 请求参数 | 路径参数 `id` |
| 响应 (JSON) | `{ "code": 200, "message": "success" }` |
| 说明 | 逻辑删除，保留学习记录。 |

---

## 4. 讲师模块

### 4.1 讲师列表

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/teacher/list` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`realName`（可选） |
| 响应 (JSON) | 分页讲师列表，含 `id, realName, orgName, jobType, title, intro, createTime` |

### 4.2 新增讲师

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/teacher/add` |
| 请求参数 (JSON) | `{ "userId": 1, "realName": "string", "orgName": "string", "title": "string", "intro": "string" }` |
| 响应 (JSON) | 新建讲师信息 |
| 说明 | 关联已有用户账号，设置讲师角色。 |

### 4.3 编辑讲师

| 项目 | 说明 |
|------|------|
| 接口路径 | `PUT /admin/teacher/edit` |
| 请求参数 (JSON) | `{ "id": 1, "realName": "string", "orgName": "string", "title": "string", "intro": "string" }` |
| 响应 (JSON) | 更新后讲师信息 |

---

## 5. 课程模块

### 5.1 课程列表（分页 + 搜索）

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/course/list` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`title`（可选，模糊搜索）、`courseType`（可选：1公开课 2必修课）、`status`（可选：0下架 1上架） |
| 响应 (JSON) | 分页课程列表，含 `id, title, courseType, coverUrl, teacherName, offlineFlag, status, createTime` |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "常见病诊疗规范",
        "courseType": 1,
        "coverUrl": "https://...",
        "teacherName": "李老师",
        "offlineFlag": 1,
        "status": 1,
        "createTime": "2026-07-07 10:00:00"
      }
    ],
    "total": 1
  }
}
```

### 5.2 新增课程

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/course/add` |
| 请求参数 (JSON) | `{ "title": "string", "courseType": 1, "coverUrl": "string", "teacherId": 1, "intro": "string", "offlineFlag": 0 }` |
| 响应 (JSON) | 新建课程信息 |
| 说明 | `offlineFlag=1` 标记为支持离线学习课程。 |

### 5.3 编辑课程

| 项目 | 说明 |
|------|------|
| 接口路径 | `PUT /admin/course/edit` |
| 请求参数 (JSON) | `{ "id": 1, "title": "string", "courseType": 1, "coverUrl": "string", "intro": "string", "offlineFlag": 1 }` |
| 响应 (JSON) | 更新后课程信息 |

### 5.4 发布/下架课程

| 项目 | 说明 |
|------|------|
| 接口路径 | `PUT /admin/course/publish` |
| 请求参数 (JSON) | `{ "id": 1, "status": 1 }` |
| 响应 (JSON) | 更新后课程信息 |
| 说明 | `status=1` 发布，`status=0` 下架。发布前需校验章节完整性。 |

### 5.5 课程详情（含章节 + 资源）

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/course/detail/{id}` |
| 请求参数 | 路径参数 `id` |
| 响应 (JSON) | 课程详情 + `chapters[]`（章节列表）+ `resources[]`（资源文件列表） |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "常见病诊疗规范",
    "courseType": 1,
    "coverUrl": "https://...",
    "teacherName": "李老师",
    "offlineFlag": 1,
    "status": 1,
    "chapters": [
      { "id": 1, "title": "第一章 总论", "sortOrder": 1 }
    ],
    "resources": [
      { "id": 1, "fileName": "第一章课件.mp4", "fileUrl": "https://...", "fileType": "video" }
    ]
  }
}
```

### 5.6 启用离线学习（触发打包）

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/course/offline/enable` |
| 请求参数 (JSON) | `{ "id": 1 }` |
| 响应 (JSON) | `{ "packageUrl": "https://...", "packageVersion": "v1" }` |
| 说明 | 异步打包课程资源为加密 ZIP 离线包，生成后更新 `offlineFlag=1` 并返回下载地址。 |

### 5.7 小程序 - 课程列表

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/course/list` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`title`（可选）、`courseType`（可选） |
| 响应 (JSON) | 分页课程列表（仅上架课程） |

### 5.8 小程序 - 课程详情

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/course/detail/{id}` |
| 请求参数 | 路径参数 `id` |
| 响应 (JSON) | 课程详情 + 章节列表 + 资源列表（含视频播放地址） |

### 5.9 小程序 - 离线包下载

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/course/download/{courseId}` |
| 请求参数 | 路径参数 `courseId` |
| 响应 | 文件流（加密 ZIP 离线包），支持断点续传（Range 请求） |
| 说明 | 仅 `offlineFlag=1` 的课程可下载。记录下载日志用于进度回传。 |

### 5.10 小程序 - 推荐课程（按报名数 Top-N）

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/course/recommend` |
| 请求参数 (Query) | `limit`（可选，默认 5，上限 20） |
| 响应 (JSON) | `[{ "id": 1, "title": "...", "teacherName": "...", "courseType": 2, "totalHours": 40, "status": 1, "offlineFlag": 0, ... }, ...]` |
| 鉴权 | 需登录（学员 Token） |
| 说明 | 仅返回 `status=1` 已发布且 `deleted=0` 的课程，按 `course_enroll` 报名数降序、报名数相同时按 `create_time` 降序，取 Top-N。后端对 `limit` 做边界钳制（<=0 取 5，>20 取 20）。响应字段含 `teacherName`（后端 JOIN `sys_user` 填充）。 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": 1, "title": "基层常见疾病诊治规范", "teacherName": "张医生", "courseType": 2, "totalHours": 40, "status": 1 },
    { "id": 2, "title": "公共卫生服务规范", "teacherName": "李主任", "courseType": 2, "totalHours": 30, "status": 1 }
  ]
}
```

---

## 6. 章节模块

### 6.1 章节列表

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/chapter/list/{courseId}` |
| 请求参数 | 路径参数 `courseId` |
| 响应 (JSON) | 章节列表，含 `id, title, sortOrder, videoDuration, resourceCount` |

### 6.2 新增章节

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/chapter/add` |
| 请求参数 (JSON) | `{ "courseId": 1, "title": "string", "sortOrder": 1, "videoUrl": "string", "videoDuration": 600 }` |
| 响应 (JSON) | 新建章节信息 |

### 6.3 编辑章节

| 项目 | 说明 |
|------|------|
| 接口路径 | `PUT /admin/chapter/edit` |
| 请求参数 (JSON) | `{ "id": 1, "title": "string", "sortOrder": 1, "videoUrl": "string", "videoDuration": 600 }` |
| 响应 (JSON) | 更新后章节信息 |

---

## 7. 知识点模块

### 7.1 知识点列表

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/knowledge-point/list` |
| 请求参数 (Query) | `chapterId`（可选）、`keyword`（可选） |
| 响应 (JSON) | 知识点列表，含 `id, title, content, chapterId, chapterTitle` |

### 7.2 新增知识点

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/knowledge-point/add` |
| 请求参数 (JSON) | `{ "chapterId": 1, "title": "string", "content": "string" }` |
| 响应 (JSON) | 新建知识点信息 |

---

## 8. 试题模块

### 8.1 试题列表（多条件筛选）

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/question/list` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`knowledgePointId`（可选）、`questionType`（可选：1单选 2多选 3判断 4填空 5问答）、`difficulty`（可选：1易 2中 3难） |
| 响应 (JSON) | 分页试题列表，含 `id, questionType, title, options, answer, difficulty, knowledgePointId` |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "questionType": 1,
        "title": "高血压的诊断标准是？",
        "options": ["A. ≥140/90", "B. ≥130/80", "C. ≥120/80"],
        "answer": "A",
        "difficulty": 2,
        "knowledgePointId": 1
      }
    ],
    "total": 1
  }
}
```

### 8.2 新增试题

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/question/add` |
| 请求参数 (JSON) | `{ "questionType": 1, "title": "string", "options": ["A.xx", "B.xx"], "answer": "A", "difficulty": 2, "knowledgePointId": 1 }` |
| 响应 (JSON) | 新建试题信息 |
| 说明 | 5 种题型：1单选 2多选 3判断 4填空 5问答。多选答案格式如 `"AB"`。 |

### 8.3 批量导入试题

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/question/import` |
| 请求参数 (Multipart) | `file`（Excel 文件） |
| 响应 (JSON) | `{ "total": 100, "success": 98, "fail": 2, "failReasons": [...] }` |
| 说明 | 解析 Excel 批量写入试题库，返回成功/失败统计。 |

---

## 9. 考试模块

### 9.1 考试列表

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/exam/list` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`examType`（可选：1课程考试 2计划考试 3单独考试）、`title`（可选） |
| 响应 (JSON) | 分页考试列表，含 `id, title, examType, courseId, planId, totalScore, passScore, retakeLimit, startTime, endTime` |

### 9.2 新增考试

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/exam/add` |
| 请求参数 (JSON) | `{ "title": "string", "examType": 1, "courseId": 1, "planId": null, "totalScore": 100, "passScore": 60, "retakeLimit": 3, "duration": 90, "startTime": "2026-07-08 09:00:00", "endTime": "2026-07-08 18:00:00" }` |
| 响应 (JSON) | 新建考试信息 |
| 说明 | 3 种考试类型通过 `exam_type` 区分：1课程考试 2计划考试 3单独考试。`retakeLimit` 为重考次数上限。 |

### 9.3 自动组卷

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/exam/generate` |
| 请求参数 (JSON) | `{ "examId": 1, "knowledgePointIds": [1,2,3], "totalScore": 100, "difficultyRatio": "30:50:20" }` |
| 响应 (JSON) | `{ "questionIds": [1,2,3,...], "generatedCount": 50 }` |
| 说明 | 按难度比例 30:50:20（易:中:难）自动抽题组卷。 |

### 9.4 小程序 - 考试列表

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/exam/list` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`examType`（可选） |
| 响应 (JSON) | 分页考试列表（仅当前学员可参加的考试） |

### 9.5 小程序 - 开始考试

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/exam/start/{id}` |
| 请求参数 | 路径参数 `id`（考试 ID） |
| 响应 (JSON) | `{ "examId": 1, "title": "string", "duration": 90, "questions": [{ "id": 1, "questionType": 1, "title": "string", "options": [...] }] }` |
| 说明 | 校验重考次数（超限返回 1007）、时间窗口。返回试题时答案字段置空。 |

### 9.6 小程序 - 提交考试（含自动阅卷）

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /api/exam/submit` |
| 请求参数 (JSON) | `{ "examId": 1, "answers": [{ "questionId": 1, "answer": "A" }, ...] }` |
| 响应 (JSON) | `{ "score": 85, "passed": true, "result": [{ "questionId": 1, "correct": true, "correctAnswer": "A" }] }` |
| 说明 | 客观题（单选/多选/判断/填空）自动阅卷，主观题（问答）标记待人工批阅。`passed` 由 `score >= passScore` 判定。 |

**请求示例：**

```json
{
  "examId": 1,
  "answers": [
    { "questionId": 1, "answer": "A" },
    { "questionId": 2, "answer": "BC" }
  ]
}
```

### 9.7 小程序 - 考试记录详情

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/exam/record/{id}` |
| 请求参数 | 路径参数 `id`（考试记录 ID） |
| 响应 (JSON) | 考试记录详情，含 `score, passed, submitTime, answers[], correctCount, totalCount` |

---

## 10. 培训计划模块

> 三种学习模式：
> - 公开课：`course.courseType=1`
> - 必修课：`course.courseType=2`
> - 计划流程性培训：`train_plan`（多课程按顺序完成）

### 10.1 计划列表

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/plan/list` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`title`（可选）、`status`（可选） |
| 响应 (JSON) | 分页计划列表，含 `id, title, startTime, endTime, status, courseCount, studentCount` |

### 10.2 新增计划

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/plan/add` |
| 请求参数 (JSON) | `{ "title": "string", "intro": "string", "startTime": "2026-07-08", "endTime": "2026-08-08", "studentIds": [1,2,3] }` |
| 响应 (JSON) | 新建计划信息 |
| 说明 | 可指定参与学员，学员自动获得计划内课程学习权限。 |

### 10.3 计划关联课程

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/plan/course` |
| 请求参数 (JSON) | `{ "planId": 1, "courses": [{ "courseId": 1, "sortOrder": 1, "isRequired": 1 }, ...] }` |
| 响应 (JSON) | 更新后计划课程列表 |
| 说明 | `sortOrder` 控制学习顺序，`isRequired=1` 标记为必修。 |

### 10.4 小程序 - 计划列表

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/plan/list` |
| 请求参数 (Query) | `pageNum`、`pageSize` |
| 响应 (JSON) | 分页计划列表（当前学员参与的计划） |

### 10.5 小程序 - 计划详情

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/plan/detail/{id}` |
| 请求参数 | 路径参数 `id` |
| 响应 (JSON) | 计划详情 + 课程列表（含每课程学习进度、完成状态） |

---

## 11. 咨询模块

### 11.1 小程序 - 发起咨询（LongCat AI + 转人工）

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /api/consult/ask` |
| 请求参数 (JSON) | `{ "question": "string" }` |
| 响应 (JSON) | `{ "consultId": 1, "autoReply": "string", "matched": true }` |
| 说明 | 优先检测转人工关键词（命中则直接转人工工单）；未命中则调用 LongCat AI 自动回复；AI 未启用或失败时兜底转人工工单。 |

**请求示例：**

```json
{
  "question": "高血压用药咨询：一线用药有哪些？"
}
```

### 11.2 咨询列表

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/consult/list` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`status`（可选：0待回复 1已回复 2已关闭）、`keyword`（可选） |
| 响应 (JSON) | 分页咨询列表，含 `id, title, studentName, status, createTime, replyTime` |

### 11.3 人工回复咨询

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /admin/consult/reply` |
| 请求参数 (JSON) | `{ "id": 1, "reply": "string" }` |
| 响应 (JSON) | 更新后咨询信息 |
| 说明 | 回复后状态变为已回复，记录回复时间用于 SLA 统计。 |

### 11.4 SLA 超时告警列表

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/consult/sla-list` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`slaHours`（可选，默认 24） |
| 响应 (JSON) | 超时未回复咨询列表，含 `id, title, studentName, createTime, overdueHours` |
| 说明 | 超过 `slaHours` 未回复的工单进入告警列表。 |

---

## 12. 学习进度模块

> 学员学习过程中实时上报进度，支持断点续传与进度回传。

### 12.1 小程序 - 上报学习进度

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /api/study/progress` |
| 请求参数 (JSON) | `{ "courseId": 1, "chapterId": 1, "progress": 80, "studyDuration": 600, "lastPosition": 580, "completed": false }` |
| 响应 (JSON) | `{ "code": 200, "message": "success" }` |
| 说明 | 前端视频播放中每 30 秒或进度变化 >= 5% 时上报一次；`completed=true` 表示章节学完；后端 upsert 到 `study_record` 表。 |
| 上限钳制 | 后端对 `progress` 做防御性钳制：`progress > 100` 时强制改为 `100`；`progress >= 100` 时强制 `completed=1`。避免前端心跳上报异常值导致进度超过 100% 或已完成章节被回退。不查 `chapter.duration` 表，避免每次心跳额外的 DB 开销。 |

**请求示例：**

```json
{
  "courseId": 1,
  "chapterId": 1,
  "progress": 80,
  "studyDuration": 600,
  "lastPosition": 580,
  "completed": false
}
```

**web 端心跳上报机制（VideoPlayer 组件，2026-07-14 新增）：**

web 学员端（`web-student/`）的视频播放器 `VideoPlayer.vue` 在播放过程中通过本接口上报进度，字段映射与触发时机如下：

| 字段 | 来源 | 说明 |
|------|------|------|
| `lastPosition` | `Math.floor(player.currentTime())` | 视频当前播放位置（秒），用于断点续播 |
| `studyDuration` | `studyDurationAcc`（累加器） | 本次会话累计学习时长（秒），后端 upsert 累加到 `study_record.study_duration` |
| `progress` | `Math.min(100, Math.floor(currentTime / duration * 100))` | 章节完成百分比，后端钳制上限 100 |
| `completed` | 心跳固定 `false`；`ended` 事件触发时由 `markFinished` 上报 `true` | 避免心跳误标完成 |

**触发时机（三层）：**

1. **30 秒定时器心跳**：`player.on('play')` 启动 `setInterval(30s)`，每次 `studyDurationAcc += 30` 并上报；`player.on('pause')` 清除定时器并立即上报一次（保存断点）。
2. **暂停即时上报**：`onPause` 调用 `emitHeartbeat()` 立即 POST，确保用户切走/暂停时 `lastPosition` 不丢失。
3. **播放结束自动完成**：`player.on('ended')` → `emit('ended')` → `learn.vue onVideoEnded` → `markFinished`（上报 `progress=100, completed=true`），章节自动标记 ✓。

**断点续播联动**：页面加载时 `getProgress` 取回 `lastPosition`，作为 `initialTime` 传入 VideoPlayer；`player.ready` 后监听 `loadedmetadata` 事件（避免 metadata 未加载时 `currentTime` 被重置），再 `player.currentTime(initialTime)` 恢复。防快进守卫 `lastValidTime` 初始值同步设为 `initialTime`，避免断点位置被误判为快进而回退到 0。

**防快进联动**：`createSeekGuard` 监听 `seeking` 事件，正向跳跃超过 5 秒阈值时回退到 `lastValidTime` 并 emit `seek-blocked`（前端 ElMessage 提示"不允许快进，请正常观看视频"）。回退（重听）允许，不拦截。

### 12.2 小程序 - 查询章节学习进度

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/study/progress/{courseId}` |
| 请求参数 | 路径参数 `courseId` |
| 响应 (JSON) | `[{ "chapterId": 1, "progress": 100, "studyDuration": 1800, "lastPosition": 0, "completed": true }, ...]` |
| 说明 | 返回当前学员在某课程下所有章节的学习进度，用于学习页恢复播放位置与章节完成状态。 |

### 12.3 小程序 - 标记章节完成

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /api/study/complete-chapter` |
| 请求参数 (JSON) | `{ "courseId": 1, "chapterId": 3 }` |
| 响应 (JSON) | `{ "code": 200, "message": "success" }` |
| 鉴权 | 需登录（学员 Token） |
| 说明 | 学员点击"标记为已学完"或视频播放结束时调用。等价于 `POST /api/study/progress` 传 `{ progress: 100, completed: true }`，后端通过组合复用 `reportProgress` 实现，保证 upsert + 上限校验逻辑一致。 |
| 校验 | `courseId` 和 `chapterId` 均为 `@NotNull`。 |

**请求示例：**

```json
{
  "courseId": 1,
  "chapterId": 3
}
```

### 12.4 小程序 - 检查是否已报名课程（轻量接口）

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/study/check-enrolled` |
| 请求参数 (Query) | `courseId`（必填） |
| 响应 (JSON) | `{ "code": 200, "data": true }` 或 `{ "code": 200, "data": false }` |
| 鉴权 | 需登录（学员 Token） |
| 说明 | 轻量报名状态查询，底层走 `course_enroll` 表 `COUNT`，仅返回 boolean。用于学习页 `learn.vue` 进入时校验当前学员是否已报名，替代旧版 `getMyCourses({pageSize: 200})` 拉取整页列表的兜底逻辑，避免无谓的数据传输。 |

---

## 13. 统计模块

> 多角度交叉分析：机构 / 岗位 / 课程 / 时间维度。

### 12.1 概览统计

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/stats/overview` |
| 请求参数 | 无 |
| 响应 (JSON) | `{ "userCount": 3000, "courseCount": 50, "examCount": 100, "todayActive": 800 }` |
| 说明 | 用户数 / 课程数 / 考试数 / 今日活跃数。 |

### 12.2 学员学习统计（微观）

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/stats/student` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`orgName`（可选）、`jobType`（可选）、`realName`（可选） |
| 响应 (JSON) | 分页学员学习统计，含 `studentId, realName, orgName, jobType, totalStudyHours, completionRate, examAvgScore` |
| 说明 | 支持按机构 / 岗位筛选，微观视角分析学员学习时长与完成率。 |

### 12.3 考试成果统计

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/stats/exam` |
| 请求参数 (Query) | `examId`（可选）、`orgName`（可选）、`startTime`（可选）、`endTime`（可选） |
| 响应 (JSON) | `{ "totalCount": 500, "avgScore": 78.5, "passRate": 0.85, "scoreDistribution": [{ "range": "0-59", "count": 75 }, ...] }` |
| 说明 | 平均分 / 通过率 / 分数段分布。 |

### 12.4 课程统计（宏观）

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/stats/course` |
| 请求参数 (Query) | `pageNum`、`pageSize`、`courseType`（可选）、`title`（可选） |
| 响应 (JSON) | 分页课程统计，含 `courseId, title, enrollCount, studyCount, completionRate, avgScore` |
| 说明 | 报名人数 / 学习人数 / 完成率 / 平均分。 |

### 12.5 机构维度统计

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/stats/org` |
| 请求参数 (Query) | `pageNum`、`pageSize` |
| 响应 (JSON) | 分页机构统计，含 `orgName, studentCount, totalStudyHours, avgStudyHours, passRate` |
| 说明 | 各机构学习时长 / 通过率横向对比。 |

### 12.6 时间趋势统计

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/stats/trend` |
| 请求参数 (Query) | `days`（可选，默认 30）、`type`（可选：study/exam） |
| 响应 (JSON) | `{ "dates": ["2026-06-07", ...], "studyCounts": [120, ...], "examCounts": [30, ...] }` |
| 说明 | 近 N 天学习 / 考试人数趋势。 |

### 12.7 小程序 - 学员个人学习统计

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /api/stats/my` |
| 请求参数 | 无（从 Token 获取当前学员） |
| 响应 (JSON) | `{ "totalStudyHours": 120, "completedCourses": 8, "examCount": 5, "avgScore": 82, "rankPercent": 0.15 }` |
| 说明 | 学员个人学习时长 / 完成课程数 / 考试数 / 平均分 / 排名百分位。 |

### 12.8 平台实时统计（在线 / 今日活跃 / 考试并发）

| 项目 | 说明 |
|------|------|
| 接口路径 | `GET /admin/stats/platform` |
| 请求参数 | 无 |
| 响应 (JSON) | `{ "onlineCount": 12, "todayStudyCount": 156, "concurrentExamCount": 1 }` |
| 鉴权 | 需登录（管理员 Token，`stats:read` 权限） |
| 说明 | 用于管理后台 Dashboard 顶部实时统计卡片。三个字段均通过单条 SQL 子查询聚合，避免多次往返。 |
| 字段语义 | `onlineCount` = 近 5 分钟内有 `study_record.update_time` 活跃的去重学员数（5 分钟活跃用户口径，替代 Redis 在线集合）；`todayStudyCount` = 当天 0 点至今有学习记录的去重学员数；`concurrentExamCount` = `exam_record.status=0`（进行中）的考试记录数。 |
| 兜底 | 三个字段在空表时 SQL `COUNT` 可能返回 `null`，Service 层统一兜底为 `0`。 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "onlineCount": 12,
    "todayStudyCount": 156,
    "concurrentExamCount": 1
  }
}
```

---

## 14. 附录：按角色权限过滤的接口矩阵

> 标注各接口可被哪些角色调用：**A** = admin（管理员）、**T** = teacher（讲师）、**S** = student（学员/小程序用户）。

### 13.1 登录

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/admin/login` | POST | ✓ | ✓ | - |
| `/api/wx/login` | POST | - | - | ✓ |

### 13.2 用户模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/admin/user/list` | GET | ✓ | - | - |
| `/admin/user/add` | POST | ✓ | - | - |
| `/admin/user/edit` | PUT | ✓ | - | - |
| `/admin/user/{id}` | DELETE | ✓ | - | - |

### 13.3 讲师模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/admin/teacher/list` | GET | ✓ | ✓ | - |
| `/admin/teacher/add` | POST | ✓ | - | - |
| `/admin/teacher/edit` | PUT | ✓ | - | - |

### 13.4 课程模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/admin/course/list` | GET | ✓ | ✓ | - |
| `/admin/course/add` | POST | ✓ | - | - |
| `/admin/course/edit` | PUT | ✓ | - | - |
| `/admin/course/publish` | PUT | ✓ | - | - |
| `/admin/course/detail/{id}` | GET | ✓ | ✓ | - |
| `/admin/course/offline/enable` | POST | ✓ | - | - |
| `/api/course/list` | GET | - | - | ✓ |
| `/api/course/detail/{id}` | GET | - | - | ✓ |
| `/api/course/download/{courseId}` | GET | - | - | ✓ |

### 13.5 章节模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/admin/chapter/list/{courseId}` | GET | ✓ | ✓ | - |
| `/admin/chapter/add` | POST | ✓ | - | - |
| `/admin/chapter/edit` | PUT | ✓ | - | - |

### 13.6 知识点模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/admin/knowledge-point/list` | GET | ✓ | ✓ | - |
| `/admin/knowledge-point/add` | POST | ✓ | - | - |

### 13.7 试题模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/admin/question/list` | GET | ✓ | ✓ | - |
| `/admin/question/add` | POST | ✓ | - | - |
| `/admin/question/import` | POST | ✓ | - | - |

### 13.8 考试模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/admin/exam/list` | GET | ✓ | ✓ | - |
| `/admin/exam/add` | POST | ✓ | - | - |
| `/admin/exam/generate` | POST | ✓ | - | - |
| `/api/exam/list` | GET | - | - | ✓ |
| `/api/exam/start/{id}` | GET | - | - | ✓ |
| `/api/exam/submit` | POST | - | - | ✓ |
| `/api/exam/record/{id}` | GET | - | - | ✓ |

### 13.9 培训计划模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/admin/plan/list` | GET | ✓ | ✓ | - |
| `/admin/plan/add` | POST | ✓ | - | - |
| `/admin/plan/course` | POST | ✓ | - | - |
| `/api/plan/list` | GET | - | - | ✓ |
| `/api/plan/detail/{id}` | GET | - | - | ✓ |

### 13.10 咨询模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/api/consult/ask` | POST | - | - | ✓ |
| `/admin/consult/list` | GET | ✓ | ✓ | - |
| `/admin/consult/reply` | POST | ✓ | ✓ | - |
| `/admin/consult/sla-list` | GET | ✓ | - | - |

### 13.11 统计模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/admin/stats/overview` | GET | ✓ | - | - |
| `/admin/stats/student` | GET | ✓ | - | - |
| `/admin/stats/exam` | GET | ✓ | - | - |
| `/admin/stats/course` | GET | ✓ | - | - |
| `/admin/stats/org` | GET | ✓ | - | - |
| `/admin/stats/trend` | GET | ✓ | - | - |
| `/api/stats/my` | GET | - | - | ✓ |

### 14.12 学习进度模块

| 接口路径 | 方法 | A | T | S |
|----------|------|---|---|---|
| `/api/study/progress` | POST | - | - | ✓ |
| `/api/study/progress/{courseId}` | GET | - | - | ✓ |

### 14.13 权限说明

- **admin**：拥有全部后台管理接口权限。
- **teacher**：可查看课程、章节、知识点、试题、考试、计划列表及咨询回复，但不可新增/编辑/删除（咨询回复除外）。
- **student**：仅可访问 `/api` 前缀的小程序接口，且数据范围限定为自身（如个人考试记录、个人学习统计）。

---

## 15. 附录：核心接口 Java Controller 方法签名示例

### 14.1 后台登录接口

```java
@RestController
@RequestMapping("/admin")
public class AuthController {

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        // 校验用户名密码，连续5次失败锁定15分钟
        // 签发 JWT Token（Access Token 30分钟）
        // 返回 token + userInfo
    }
}
```

### 14.2 自动组卷接口

```java
@RestController
@RequestMapping("/admin/exam")
public class ExamController {

    @PostMapping("/generate")
    public Result<GenerateVO> generate(@RequestBody GenerateDTO dto) {
        // 按难度比例 30:50:20（易:中:难）从知识点对应试题库抽题
        // 校验总分是否与 totalScore 一致
        // 返回抽中的 questionIds
    }
}
```

### 14.3 考试提交（自动阅卷）接口

```java
@RestController
@RequestMapping("/api/exam")
public class ExamApiController {

    @PostMapping("/submit")
    public Result<SubmitVO> submit(@RequestBody SubmitDTO dto,
                                   @RequestAttribute("userId") Long userId) {
        // 校验考试是否在时间窗口内
        // 校验重考次数（超限返回业务错误码 1007）
        // 客观题（单选/多选/判断/填空）自动比对答案
        // 主观题（问答）标记待人工批阅
        // 计算 score，score >= passScore 则 passed=true
        // 保存考试记录与答题明细
    }
}
```

### 15.4 学习进度上报接口

```java
@RestController
@RequestMapping("/api/study")
public class StudyApiController {

    @PostMapping("/progress")
    public Result<Void> reportProgress(@RequestBody StudyProgressDTO dto,
                                       @RequestAttribute("userId") Long userId) {
        // upsert 到 study_record 表（student_id, course_id, chapter_id 唯一）
        // 累加 study_duration，更新 progress、last_position、completed
    }

    @GetMapping("/progress/{courseId}")
    public Result<List<StudyProgressVO>> getProgress(@PathVariable Long courseId,
                                                     @RequestAttribute("userId") Long userId) {
        // 查询当前学员在指定课程下所有章节的学习进度
    }
}
```

### 15.5 智能咨询接口

```java
@RestController
@RequestMapping("/api/consult")
public class ConsultApiController {

    @PostMapping("/ask")
    public Result<AskVO> ask(@RequestBody AskDTO dto,
                             @RequestAttribute("userId") Long userId) {
        // 1. 检测转人工关键词（命中"转人工/找老师"等 → 直接人工工单）
        // 2. 调用 LongCat AI 自动回复（启用时），成功则返回 autoReply（matched=true）
        // 3. AI 未启用或调用失败 → 兜底创建人工工单（matched=false），等待 admin 回复
    }
}
```

---

## 15.6 附录：字段命名对照表

> 数据库字段与 API 响应字段对照，防止后端开发时命名不一致。

| 表 | 数据库字段 | API 字段 | 备注 |
|----|-----------|---------|------|
| sys_user | real_name | realName | 驼峰转换 |
| teacher | real_name | realName | 驼峰转换 |
| course | offline_flag | offlineFlag | 驼峰转换 |
| exam | start_time | startTime | 驼峰转换 |
| exam | end_time | endTime | 驼钩转换 |

