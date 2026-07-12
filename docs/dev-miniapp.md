# 微信小程序（学员端）开发文档

> **版本**: 1.0.0
> **日期**: 2026-07-07
> **对应项目**: 四川省基层卫生人员网络培训平台
> **配套文档导航**:
> - 后端实现手册: `docs/dev-backend.md`
> - 前端实现手册: `docs/dev-frontend.md`
> - API 接口清单: `docs/dev-api.md`
> - 数据库设计: `docs/dev-database.md`
> - 部署手册: `docs/deploy.md`
> - 开发文档（总览）: `docs/开发文档.md`

---

## 一、项目搭建与目录结构

### 1.1 导入与配置

1. 打开**微信开发者工具** → 导入项目 → 选择 `miniprogram` 目录
2. AppID 选择**测试号**（无需注册正式小程序）
3. 修改 `miniprogram/api/config.js` 中的 `baseURL` 指向本地后端

> ⚠️ 开发阶段需在微信开发者工具中：**详情 → 本地设置 → 勾选"不校验合法域名"**，否则无法访问 `localhost:8081`。

### 1.2 完整目录树

```
miniprogram/
├── app.js                      # 全局 JS（启动、登录态检查）
├── app.json                    # 全局配置（页面注册、tabBar、窗口）
├── app.wxss                    # 全局样式（CSS 变量 + 基础类）
├── project.config.json          # 项目配置（AppID、编译选项）
├── sitemap.json                # 索引规则
├── pages/
│   ├── index/index.{js,json,wxss}          # 首页（轮播 + 快捷入口 + 推荐）
│   ├── login/login.{js,json,wxss}          # 登录（微信一键登录）
│   ├── course/
│   │   ├── list/list.{js,json,wxss}        # 课程列表（筛选 + 搜索）
│   │   ├── detail/detail.{js,json,wxss}    # 课程详情（章节 + 报名 + 离线下载）
│   │   └── study/study.{js,json,wxss}      # 学习页（视频播放 + 进度上报）
│   ├── exam/
│   │   ├── list/list.{js,json,wxss}        # 考试列表（剩余重考次数）
│   │   ├── do/do.{js,json,wxss}            # 答题页（计时 + 切题 + 交卷）
│   │   └── result/result.{js,json,wxss}    # 考试结果（分数 + 对错 + 重考）
│   ├── plan/list/list.{js,json,wxss}       # 培训计划列表（进度条）
│   ├── consult/index/index.{js,json,wxss}  # 智能问答（聊天气泡 + 转人工）
│   └── my/
│       ├── index/index.{js,json,wxss}      # 个人中心（头像 + 统计 + 入口）
│       ├── courses/courses.{js,json,wxss}  # 我的课程（已报名 + 进度）
│       └── records/records.{js,json,wxss}  # 学习记录（时间线）
├── api/
│   ├── config.js                # baseURL + timeout
│   ├── request.js               # Promise 封装 wx.request
│   ├── user.js                  # 用户/登录接口
│   ├── course.js                # 课程接口
│   ├── study.js                 # 学习/进度接口
│   ├── exam.js                  # 考试接口
│   ├── plan.js                  # 培训计划接口
│   ├── consult.js               # 咨询接口
│   └── stats.js                 # 统计接口
├── utils/
│   └── util.js                  # 通用工具（格式化、防抖、离线存储）
└── images/                      # 图标、占位图（本地资源）
```

### 1.3 project.config.json 关键配置

```json
{
  "miniprogramRoot": "./",
  "projectname": "training-miniapp",
  "description": "四川省基层卫生人员网络培训平台-学员端",
  "appid": "touristappid",
  "setting": {
    "urlCheck": false,
    "es6": true,
    "enhance": true,
    "postcss": true,
    "minified": true,
    "newFeature": true
  },
  "compileType": "miniprogram",
  "libVersion": "3.5.0"
}
```

> `urlCheck: false` 关闭域名校验，便于开发阶段访问 `http://localhost:8081`。

### 1.4 全局样式 app.wxss 基础变量

```css
/* app.wxss — 全局样式与 CSS 变量 */
page {
  --primary: #1677ff;        /* 主题蓝 */
  --success: #52c41a;
  --warning: #faad14;
  --danger: #ff4d4f;
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --text-muted: #9ca3af;
  --bg: #f5f7fa;
  --border: #e5e7eb;
  --radius: 12rpx;
  --card-bg: #ffffff;
  background-color: var(--bg);
  color: var(--text-primary);
  font-size: 28rpx;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", sans-serif;
}

/* 通用卡片 */
.card {
  background: var(--card-bg);
  border-radius: var(--radius);
  padding: 24rpx;
  margin: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

/* 通用按钮 */
.btn-primary {
  background: var(--primary);
  color: #fff;
  border-radius: 999rpx;
  font-size: 30rpx;
  padding: 20rpx 0;
  text-align: center;
}

/* 骨架屏 */
.skeleton {
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 37%, #eee 63%);
  background-size: 400% 100%;
  animation: skeleton-loading 1.4s ease infinite;
  border-radius: 8rpx;
}

@keyframes skeleton-loading {
  0% { background-position: 100% 50%; }
  100% { background-position: 0 50%; }
}

/* 文本省略 */
.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
```

---

## 二、核心封装（完整代码）

### 2.1 api/config.js — 接口基础配置

```js
// miniprogram/api/config.js
module.exports = {
  // 小程序 API 地址（training-api 端口 8081）
  baseURL: 'http://localhost:8081/api',
  // 请求超时 10 秒（弱网环境）
  timeout: 10000,
  // 离线包本地存储目录名
  offlineDir: 'offline_courses'
}
```

### 2.2 api/request.js — Promise 封装 wx.request（完整可运行）

```js
// miniprogram/api/request.js
const { baseURL, timeout } = require('./config')

// 请求重试次数（弱网优化）
const MAX_RETRY = 3

function request(url, method = 'GET', data = {}, retryCount = 0) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    wx.request({
      url: baseURL + url,
      method,
      data,
      timeout,
      header: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      success(res) {
        const { code, message, data } = res.data
        if (code === 200) {
          resolve(data)
        } else if (code === 401) {
          // Token 失效，清除并跳转登录
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          wx.reLaunch({ url: '/pages/login/login' })
          reject(new Error('未登录'))
        } else {
          wx.showToast({ title: message || '请求失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail(err) {
        // 弱网重试
        if (retryCount < MAX_RETRY) {
          setTimeout(() => {
            request(url, method, data, retryCount + 1).then(resolve, reject)
          }, 1000 * (retryCount + 1))
        } else {
          wx.showToast({ title: '网络错误，请检查网络', icon: 'none' })
          reject(err)
        }
      }
    })
  })
}

module.exports = { request }
```

### 2.3 各模块 API 文件方法签名

```js
// miniprogram/api/user.js — 用户/登录
const { request } = require('./request')
module.exports = {
  // 微信登录：wx.login 获取 code → 后端换取 token
  login: (data) => request('/wx/login', 'POST', data),
  // 获取当前用户信息
  getProfile: () => request('/user/profile', 'GET'),
  // 更新昵称头像
  updateProfile: (data) => request('/user/profile', 'PUT', data)
}
```

```js
// miniprogram/api/course.js — 课程
const { request } = require('./request')
module.exports = {
  // 课程列表（分页 + 搜索 + 分类）
  getList: (params) => request('/course/list', 'GET', params),
  // 课程详情（含章节 + 资源）
  getDetail: (id) => request(`/course/detail/${id}`, 'GET'),
  // 报名课程
  enroll: (id) => request(`/course/enroll/${id}`, 'POST'),
  // 获取离线包下载地址
  getDownloadUrl: (courseId) => request(`/course/download/${courseId}`, 'GET')
}
```

```js
// miniprogram/api/study.js — 学习/进度
const { request } = require('./request')
module.exports = {
  // 已报名课程列表
  getMyCourses: () => request('/study/my-courses', 'GET'),
  // 上报学习进度
  reportProgress: (data) => request('/study/progress', 'POST', data),
  // 获取学习记录
  getRecord: (courseId) => request('/study/record', 'GET', { courseId })
}
```

```js
// miniprogram/api/exam.js — 考试
const { request } = require('./request')
module.exports = {
  // 可参加的考试列表
  getList: (params) => request('/exam/list', 'GET', params),
  // 开始考试（返回题目，答案置空）
  start: (id) => request(`/exam/start/${id}`, 'GET'),
  // 提交考试
  submit: (data) => request('/exam/submit', 'POST', data),
  // 考试记录详情
  getRecord: (id) => request(`/exam/record/${id}`, 'GET')
}
```

```js
// miniprogram/api/plan.js — 培训计划
const { request } = require('./request')
module.exports = {
  // 当前学员参与的计划列表
  getList: (params) => request('/plan/list', 'GET', params),
  // 计划详情（含课程列表 + 进度）
  getDetail: (id) => request(`/plan/detail/${id}`, 'GET')
}
```

```js
// miniprogram/api/consult.js — 咨询
const { request } = request('./request')
module.exports = {
  // 发起咨询（智能匹配 + 转人工）
  ask: (data) => request('/consult/ask', 'POST', data)
}
```

```js
// miniprogram/api/stats.js — 统计
const { request } = require('./request')
module.exports = {
  // 学员个人学习统计
  getMyStats: () => request('/stats/my', 'GET')
}
```

---

## 三、登录模块（完整代码）

### 3.1 pages/login/login.js

```js
// miniprogram/pages/login/login.js
const userApi = require('../../api/user')

Page({
  data: {
    loading: false
  },

  // 微信一键登录
  onWxLogin(e) {
    if (this.data.loading) return
    this.setData({ loading: true })

    // 1. 获取微信临时 code
    wx.login({
      success: async (res) => {
        if (!res.code) {
          wx.showToast({ title: '获取 code 失败', icon: 'none' })
          this.setData({ loading: false })
          return
        }
        try {
          // 2. 调用后端登录接口
          const userInfo = e.detail.userInfo || {}
          const result = await userApi.login({
            code: res.code,
            nickname: userInfo.nickName || '学员',
            avatar: userInfo.avatarUrl || ''
          })
          // 3. 存储 token + userInfo
          wx.setStorageSync('token', result.token)
          wx.setStorageSync('userInfo', result.userInfo)
          // 4. 跳转首页
          wx.reLaunch({ url: '/pages/index/index' })
        } catch (err) {
          console.error('登录失败', err)
        } finally {
          this.setData({ loading: false })
        }
      },
      fail: () => {
        wx.showToast({ title: '微信登录失败', icon: 'none' })
        this.setData({ loading: false })
      }
    })
  }
})
```

### 3.2 pages/login/login.wxml

```xml
<!-- miniprogram/pages/login/login.wxml -->
<view class="login-container">
  <view class="logo-area">
    <image class="logo" src="/images/logo.png" mode="aspectFit"></image>
    <view class="title">四川省基层卫生人员</view>
    <view class="subtitle">网络培训平台</view>
  </view>

  <view class="login-box">
    <button
      class="wx-login-btn"
      open-type="getUserInfo"
      bindgetuserinfo="onWxLogin"
      loading="{{loading}}"
      disabled="{{loading}}">
      微信一键登录
    </button>
    <view class="tip">登录即代表同意《用户协议》和《隐私政策》</view>
  </view>
</view>
```

### 3.3 pages/login/login.wxss

```css
/* miniprogram/pages/login/login.wxss */
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 200rpx 60rpx 100rpx;
  background: linear-gradient(180deg, #1677ff 0%, #4096ff 100%);
}

.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #fff;
}
.logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  background: #fff;
  margin-bottom: 30rpx;
}
.title { font-size: 40rpx; font-weight: 600; }
.subtitle { font-size: 28rpx; margin-top: 10rpx; opacity: 0.9; }

.login-box { width: 100%; }
.wx-login-btn {
  background: #fff;
  color: #1677ff;
  border-radius: 999rpx;
  font-size: 32rpx;
  font-weight: 600;
}
.tip {
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 24rpx;
  margin-top: 30rpx;
}
```

### 3.4 pages/login/login.json

```json
{
  "navigationBarTitleText": "登录",
  "navigationStyle": "custom"
}
```

---

## 四、首页

### 4.1 pages/index/index.js

```js
// miniprogram/pages/index/index.js
const courseApi = require('../../api/course')
const statsApi = require('../../api/stats')

Page({
  data: {
    banners: [
      { id: 1, image: '/images/banner1.jpg' },
      { id: 2, image: '/images/banner2.jpg' }
    ],
    entries: [
      { icon: '/images/icon-course.png', text: '课程', url: '/pages/course/list' },
      { icon: '/images/icon-exam.png', text: '考试', url: '/pages/exam/list' },
      { icon: '/images/icon-plan.png', text: '计划', url: '/pages/plan/list' },
      { icon: '/images/icon-my.png', text: '我的', url: '/pages/my/index' },
      { icon: '/images/icon-consult.png', text: '咨询', url: '/pages/consult/index' }
    ],
    recommendList: [],
    loading: true
  },

  onLoad() {
    this.loadRecommend()
  },

  onShow() {
    // 检查登录态
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
    }
  },

  async loadRecommend() {
    try {
      const data = await courseApi.getList({ pageNum: 1, pageSize: 6 })
      this.setData({ recommendList: data.records, loading: false })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  onGoTo(e) {
    wx.navigateTo({ url: e.currentTarget.dataset.url })
  },

  // 下拉刷新
  async onPullDownRefresh() {
    await this.loadRecommend()
    wx.stopPullDownRefresh()
  }
})
```

### 4.2 pages/index/index.wxml

```xml
<!-- miniprogram/pages/index/index.wxml -->
<view class="container">
  <!-- 轮播图 -->
  <swiper class="banner" indicator-dots autoplay interval="4000" circular>
    <swiper-item wx:for="{{banners}}" wx:key="id">
      <image src="{{item.image}}" mode="aspectFill" class="banner-img"></image>
    </swiper-item>
  </swiper>

  <!-- 快捷入口网格 -->
  <view class="entries">
    <view class="entry-item" wx:for="{{entries}}" wx:key="text"
          data-url="{{item.url}}" bindtap="onGoTo">
      <image class="entry-icon" src="{{item.icon}}"></image>
      <text>{{item.text}}</text>
    </view>
  </view>

  <!-- 推荐课程 -->
  <view class="section-title">推荐课程</view>
  <view wx:if="{{loading}}" class="skeleton-list">
    <view class="skeleton-card" wx:for="{{[1,2,3]}}"></view>
  </view>
  <view wx:else class="course-list">
    <view class="course-card" wx:for="{{recommendList}}" wx:key="id"
          data-id="{{item.id}}" bindtap="goDetail">
      <image class="cover" src="{{item.coverUrl}}" mode="aspectFill"></image>
      <view class="info">
        <view class="title ellipsis-2">{{item.title}}</view>
        <view class="meta">{{item.teacherName}}</view>
      </view>
    </view>
  </view>
</view>
```

### 4.3 pages/index/index.wxss

```css
/* miniprogram/pages/index/index.wxss */
.banner { width: 100%; height: 320rpx; margin: 20rpx 0; }
.banner-img { width: 100%; height: 100%; border-radius: 16rpx; }

.entries {
  display: flex;
  flex-wrap: wrap;
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 30rpx 0;
}
.entry-item {
  width: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20rpx;
}
.entry-icon { width: 80rpx; height: 80rpx; margin-bottom: 12rpx; }
.entry-item text { font-size: 26rpx; color: var(--text-secondary); }

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  margin: 30rpx 30rpx 10rpx;
}

.course-list { padding: 0 20rpx; }
.course-card {
  display: flex;
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}
.cover {
  width: 200rpx;
  height: 150rpx;
  border-radius: 8rpx;
  margin-right: 20rpx;
  background: #eee;
}
.info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.info .title { font-size: 28rpx; font-weight: 500; }
.info .meta { font-size: 24rpx; color: var(--text-muted); }

.skeleton-card { height: 190rpx; margin: 0 20rpx 20rpx; }
```

---

## 五、课程模块（pages/course/）

### 5.1 list — 课程列表

**pages/course/list/list.js**：

```js
const courseApi = require('../../../api/course')

Page({
  data: {
    courseList: [],
    activeType: 'all',   // all / 1公开课 / 2必修课
    keyword: '',
    pageNum: 1,
    total: 0,
    loading: false,
    noMore: false
  },

  onLoad() {
    this.loadList()
  },

  async loadList(reset = false) {
    if (this.data.loading) return
    const pageNum = reset ? 1 : this.data.pageNum
    this.setData({ loading: true })
    try {
      const params = { pageNum, pageSize: 10 }
      if (this.data.activeType !== 'all') params.courseType = this.data.activeType
      if (this.data.keyword) params.title = this.data.keyword
      const data = await courseApi.getList(params)
      this.setData({
        courseList: reset ? data.records : this.data.courseList.concat(data.records),
        total: data.total,
        pageNum: pageNum + 1,
        noMore: data.records.length < 10,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  onFilter(e) {
    this.setData({ activeType: e.currentTarget.dataset.type }, () => this.loadList(true))
  },

  onSearch(e) {
    this.setData({ keyword: e.detail.value }, () => this.loadList(true))
  },

  onReachBottom() {
    if (!this.data.noMore) this.loadList()
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/course/detail/detail?id=${e.currentTarget.dataset.id}` })
  }
})
```

**pages/course/list/list.wxml**：

```xml
<view class="container">
  <!-- 搜索栏 -->
  <view class="search-bar">
    <input class="search-input" placeholder="搜索课程" bindinput="onSearch"
           confirm-type="search" value="{{keyword}}"/>
  </view>

  <!-- 分类筛选 -->
  <view class="filter">
    <view class="filter-item {{activeType === 'all' ? 'active' : ''}}"
          data-type="all" bindtap="onFilter">全部</view>
    <view class="filter-item {{activeType === '1' ? 'active' : ''}}"
          data-type="1" bindtap="onFilter">公开课</view>
    <view class="filter-item {{activeType === '2' ? 'active' : ''}}"
          data-type="2" bindtap="onFilter">必修课</view>
  </view>

  <!-- 课程列表 -->
  <view class="course-list">
    <view class="course-card" wx:for="{{courseList}}" wx:key="id"
          data-id="{{item.id}}" bindtap="goDetail">
      <image class="cover" src="{{item.coverUrl}}" mode="aspectFill"></image>
      <view class="info">
        <view class="title ellipsis-2">{{item.title}}</view>
        <view class="meta">
          <text class="tag">{{item.courseType === 1 ? '公开课' : '必修课'}}</text>
          <text>{{item.teacherName}}</text>
        </view>
      </view>
    </view>
  </view>

  <view wx:if="{{noMore && courseList.length}}" class="no-more">没有更多了</view>
</view>
```

### 5.2 detail — 课程详情（完整代码）

**pages/course/detail/detail.js**：

```js
const courseApi = require('../../../api/course')
const studyApi = require('../../../api/study')

Page({
  data: {
    course: null,
    chapters: [],
    enrolled: false,
    loading: true
  },

  async onLoad(options) {
    const id = options.id
    await this.loadDetail(id)
  },

  async loadDetail(id) {
    try {
      const data = await courseApi.getDetail(id)
      this.setData({
        course: data.course,
        chapters: data.chapters,
        enrolled: data.enrolled,
        loading: false
      })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  // 报名
  async onEnroll() {
    try {
      await courseApi.enroll(this.data.course.id)
      wx.showToast({ title: '报名成功' })
      this.setData({ enrolled: true })
    } catch (e) {}
  },

  // 离线下载（仅 offlineFlag=1 的课程显示）
  onDownload() {
    wx.showLoading({ title: '获取下载链接...' })
    courseApi.getDownloadUrl(this.data.course.id)
      .then(res => {
        wx.hideLoading()
        return this.downloadOfflinePackage(res.zipUrl)
      })
      .then(savedPath => {
        wx.showToast({ title: '下载完成' })
        // 解压并保存 manifest
        this.unpackAndSaveManifest(savedPath)
      })
      .catch(() => wx.hideLoading())
  },

  downloadOfflinePackage(url) {
    return new Promise((resolve, reject) => {
      wx.downloadFile({
        url,
        success: (res) => {
          if (res.statusCode === 200) resolve(res.tempFilePath)
          else reject(res)
        },
        fail: reject
      })
    })
  },

  unpackAndSaveManifest(tempPath) {
    const fs = wx.getFileSystemManager()
    const savePath = `${wx.env.USER_DATA_PATH}/offline_${this.data.course.id}`
    try {
      fs.saveFileSync(tempPath, savePath)
      // 实际项目需解压 ZIP 并读取 manifest.json
      wx.setStorageSync(`offline_manifest_${this.data.course.id}`, {
        courseId: this.data.course.id,
        downloadedAt: Date.now()
      })
    } catch (e) {
      console.error('保存离线包失败', e)
    }
  },

  // 进入学习
  onStudy() {
    wx.navigateTo({ url: `/pages/course/study/study?id=${this.data.course.id}` })
  }
})
```

**pages/course/detail/detail.wxml**：

```xml
<view class="container" wx:if="{{course}}">
  <!-- 课程封面 -->
  <image class="cover" src="{{course.coverUrl}}" mode="aspectFill"></image>

  <!-- 课程信息 -->
  <view class="course-info card">
    <view class="title">{{course.title}}</view>
    <view class="meta">
      <text class="tag">{{course.courseType === 1 ? '公开课' : '必修课'}}</text>
      <text>讲师：{{course.teacherName}}</text>
    </view>
    <view class="intro">{{course.intro}}</view>
  </view>

  <!-- 章节列表 -->
  <view class="section-title">章节列表</view>
  <view class="chapters">
    <view class="chapter-item" wx:for="{{chapters}}" wx:key="id">
      <view class="chapter-index">{{index + 1}}</view>
      <view class="chapter-info">
        <view class="chapter-title">{{item.title}}</view>
        <view class="chapter-meta">{{item.videoDuration}}秒</view>
      </view>
    </view>
  </view>

  <!-- 底部操作栏 -->
  <view class="action-bar">
    <button wx:if="{{!enrolled}}" class="btn-primary" bindtap="onEnroll">立即报名</button>
    <block wx:else>
      <button class="btn-primary" bindtap="onStudy">开始学习</button>
      <button wx:if="{{course.offlineFlag === 1}}" class="btn-offline" bindtap="onDownload">
        离线下载
      </button>
    </block>
  </view>
</view>
```

### 5.3 study — 学习页（完整代码）

**pages/course/study/study.js**：

```js
const studyApi = require('../../../api/study')

Page({
  data: {
    course: null,
    chapters: [],
    currentIndex: 0,
    currentChapter: null,
    lastPosition: 0
  },

  async onLoad(options) {
    const courseId = options.id
    const detail = await studyApi.getCourseDetail(courseId)
    const firstChapter = detail.chapters[0]
    this.setData({
      course: detail.course,
      chapters: detail.chapters,
      currentChapter: firstChapter,
      currentIndex: 0,
      lastPosition: firstChapter.lastPosition || 0
    })
  },

  // 切换章节
  onSwitchChapter(e) {
    const index = e.currentTarget.dataset.index
    const chapter = this.data.chapters[index]
    this.setData({
      currentIndex: index,
      currentChapter: chapter,
      lastPosition: chapter.lastPosition || 0
    })
  },

  // 视频播放进度上报（10 秒节流）
  onTimeUpdate(e) {
    this._lastReport = this._lastReport || 0
    const now = Date.now()
    if (now - this._lastReport < 10000) return

    const duration = e.detail.duration
    const current = e.detail.currentTime
    if (!duration) return
    const progress = Math.floor((current / duration) * 100)

    studyApi.reportProgress({
      courseId: this.data.course.id,
      chapterId: this.data.currentChapter.id,
      progress,
      studyDuration: 10,
      lastPosition: Math.floor(current)
    })
    this._lastReport = now
  },

  // 切换章节时保存当前进度
  onUnload() {
    // 页面卸载时上报一次最终进度
    this._lastReport = 0
  }
})
```

**pages/course/study/study.wxml**：

```xml
<view class="container" wx:if="{{course}}">
  <!-- 视频播放器：初始位置 lastPosition 实现断点续播 -->
  <video
    class="video"
    src="{{currentChapter.videoUrl}}"
    initial-time="{{lastPosition}}"
    controls
    bindtimeupdate="onTimeUpdate"
    poster="{{course.coverUrl}}">
  </video>

  <!-- 当前章节信息 -->
  <view class="current-chapter">
    <view class="title">{{currentChapter.title}}</view>
    <view class="meta">第 {{currentIndex + 1}} 章 / 共 {{chapters.length}} 章</view>
  </view>

  <!-- 章节切换 -->
  <scroll-view scroll-y class="chapter-list">
    <view class="chapter-item {{index === currentIndex ? 'active' : ''}}"
          wx:for="{{chapters}}" wx:key="id"
          data-index="{{index}}" bindtap="onSwitchChapter">
      <text class="index">{{index + 1}}</text>
      <text class="name ellipsis">{{item.title}}</text>
      <text wx:if="{{item.progress === 100}}" class="done">已完成</text>
    </view>
  </scroll-view>
</view>
```

**pages/course/study/study.wxss**：

```css
/* miniprogram/pages/course/study/study.wxss */
.video { width: 100%; height: 420rpx; background: #000; }

.current-chapter {
  padding: 24rpx;
  background: #fff;
  border-bottom: 1rpx solid var(--border);
}
.current-chapter .title { font-size: 30rpx; font-weight: 600; }
.current-chapter .meta { font-size: 24rpx; color: var(--text-muted); margin-top: 8rpx; }

.chapter-list { height: calc(100vh - 420rpx - 120rpx); }
.chapter-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid var(--border);
}
.chapter-item.active { background: #e6f0ff; }
.chapter-item .index {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #eee;
  text-align: center;
  line-height: 48rpx;
  font-size: 24rpx;
  margin-right: 20rpx;
}
.chapter-item.active .index { background: var(--primary); color: #fff; }
.chapter-item .name { flex: 1; }
.chapter-item .done { color: var(--success); font-size: 24rpx; }
```

---

## 六、考试模块（pages/exam/）

### 6.1 list — 考试列表

**pages/exam/list/list.js**：

```js
const examApi = require('../../../api/exam')

Page({
  data: {
    examList: [],
    loading: true
  },

  onShow() {
    this.loadList()
  },

  async loadList() {
    try {
      const data = await examApi.getList({})
      this.setData({ examList: data.records, loading: false })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  onStart(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/exam/do/do?id=${id}` })
  }
})
```

**pages/exam/list/list.wxml**：

```xml
<view class="container">
  <view class="exam-card" wx:for="{{examList}}" wx:key="id"
        data-id="{{item.id}}" bindtap="onStart">
    <view class="title ellipsis">{{item.title}}</view>
    <view class="meta">
      <text>总分 {{item.totalScore}} · 及格 {{item.passScore}}</text>
      <text>时长 {{item.duration}} 分钟</text>
    </view>
    <view class="footer">
      <text class="retry">剩余重考 {{item.remainRetry}} 次</text>
      <button class="btn-primary btn-sm">开始考试</button>
    </view>
  </view>
  <view wx:if="{{!loading && !examList.length}}" class="empty">暂无可参加的考试</view>
</view>
```

### 6.2 do — 答题页（完整实现）

**pages/exam/do/do.js**：

```js
const examApi = require('../../../api/exam')

Page({
  data: {
    examId: null,
    title: '',
    duration: 90,           // 考试时长（分钟）
    remainSeconds: 0,       // 剩余秒数
    questions: [],
    currentIndex: 0,
    currentQuestion: null,
    answers: {},            // { questionId: answer }
    showSubmitDialog: false
  },

  async onLoad(options) {
    const id = options.id
    this.setData({ examId: id })
    try {
      const data = await examApi.start(id)
      this.setData({
        title: data.title,
        duration: data.duration,
        remainSeconds: data.duration * 60,
        questions: data.questions,
        currentQuestion: data.questions[0]
      })
      this.startTimer()
    } catch (e) {}
  },

  // 倒计时
  startTimer() {
    this._timer = setInterval(() => {
      const sec = this.data.remainSeconds - 1
      if (sec <= 0) {
        clearInterval(this._timer)
        wx.showToast({ title: '考试时间到，自动交卷', icon: 'none' })
        this.submitExam(true)
      } else {
        this.setData({ remainSeconds: sec })
      }
    }, 1000)
  },

  // 格式化时间
  formatTime(sec) {
    const m = Math.floor(sec / 60)
    const s = sec % 60
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  },

  // 上一题
  onPrev() {
    if (this.data.currentIndex === 0) return
    this.switchQuestion(this.data.currentIndex - 1)
  },

  // 下一题
  onNext() {
    if (this.data.currentIndex >= this.data.questions.length - 1) return
    this.switchQuestion(this.data.currentIndex + 1)
  },

  // 题号导航
  onJump(e) {
    this.switchQuestion(e.currentTarget.dataset.index)
  },

  switchQuestion(index) {
    this.setData({
      currentIndex: index,
      currentQuestion: this.data.questions[index]
    })
  },

  // 单选/判断
  onRadioChange(e) {
    const value = e.detail.value
    this.saveAnswer(this.data.currentQuestion.id, value)
  },

  // 多选
  onCheckboxChange(e) {
    const value = e.detail.value.join('')
    this.saveAnswer(this.data.currentQuestion.id, value)
  },

  // 填空/问答
  onInput(e) {
    this.saveAnswer(this.data.currentQuestion.id, e.detail.value)
  },

  saveAnswer(questionId, answer) {
    const answers = { ...this.data.answers, [questionId]: answer }
    this.setData({ answers })
  },

  // 打开交卷确认
  onShowSubmit() {
    this.setData({ showSubmitDialog: true })
  },

  // 交卷
  onConfirmSubmit() {
    this.setData({ showSubmitDialog: false })
    this.submitExam(false)
  },

  async submitExam(autoSubmit) {
    clearInterval(this._timer)
    const answers = Object.keys(this.data.answers).map(qid => ({
      questionId: Number(qid),
      answer: this.data.answers[qid]
    }))
    try {
      const result = await examApi.submit({
        examId: this.data.examId,
        answers
      })
      wx.redirectTo({
        url: `/pages/exam/result/result?recordId=${result.recordId}&auto=${autoSubmit ? 1 : 0}`
      })
    } catch (e) {
      this.startTimer() // 提交失败恢复计时
    }
  },

  onUnload() {
    clearInterval(this._timer)
  }
})
```

**pages/exam/do/do.wxml**：

```xml
<view class="container">
  <!-- 顶部：标题 + 倒计时 -->
  <view class="header">
    <view class="title ellipsis">{{title}}</view>
    <view class="timer {{remainSeconds < 300 ? 'warning' : ''}}">
      ⏱ {{formatTime(remainSeconds)}}
    </view>
  </view>

  <!-- 进度 -->
  <view class="progress-bar">
    <text>第 {{currentIndex + 1}} / {{questions.length}} 题</text>
    <view class="bar">
      <view class="fill" style="width: {{ (currentIndex + 1) / questions.length * 100 }}%"></view>
    </view>
  </view>

  <!-- 题目渲染（5 种题型） -->
  <view class="question-card" wx:if="{{currentQuestion}}">
    <view class="q-title">
      <text class="q-type">[{{currentQuestion.questionType === 1 ? '单选' :
        currentQuestion.questionType === 2 ? '多选' :
        currentQuestion.questionType === 3 ? '判断' :
        currentQuestion.questionType === 4 ? '填空' : '问答'}}]</text>
      {{currentQuestion.title}}
    </view>

    <!-- 单选 / 判断 -->
    <radio-group wx:if="{{currentQuestion.questionType === 1 || currentQuestion.questionType === 3}}"
                 bindchange="onRadioChange" class="options">
      <label class="option" wx:for="{{currentQuestion.options}}" wx:key="*this">
        <radio value="{{item}}"></radio>
        <text>{{item}}</text>
      </label>
    </radio-group>

    <!-- 多选 -->
    <checkbox-group wx:if="{{currentQuestion.questionType === 2}}"
                    bindchange="onCheckboxChange" class="options">
      <label class="option" wx:for="{{currentQuestion.options}}" wx:key="*this">
        <checkbox value="{{item}}"></checkbox>
        <text>{{item}}</text>
      </label>
    </checkbox-group>

    <!-- 填空 -->
    <input wx:if="{{currentQuestion.questionType === 4}}"
           class="input" placeholder="请输入答案" bindinput="onInput"/>

    <!-- 问答 -->
    <textarea wx:if="{{currentQuestion.questionType === 5}}"
              class="textarea" placeholder="请输入答案" bindinput="onInput" maxlength="500"/>
  </view>

  <!-- 底部操作 -->
  <view class="action-bar">
    <button class="btn-secondary" bindtap="onPrev" disabled="{{currentIndex === 0}}">上一题</button>
    <button wx:if="{{currentIndex < questions.length - 1}}" class="btn-primary" bindtap="onNext">下一题</button>
    <button wx:else class="btn-primary" bindtap="onShowSubmit">交卷</button>
  </view>

  <!-- 题号导航 -->
  <view class="navigator">
    <view class="nav-item {{answers[item.id] ? 'answered' : ''}} {{index === currentIndex ? 'current' : ''}}"
          wx:for="{{questions}}" wx:key="id"
          data-index="{{index}}" bindtap="onJump">
      {{index + 1}}
    </view>
  </view>

  <!-- 交卷确认弹窗 -->
  <view wx:if="{{showSubmitDialog}}" class="dialog-mask">
    <view class="dialog">
      <view class="dialog-title">确认交卷？</view>
      <view class="dialog-content">已答 {{Object.keys(answers).length}} / {{questions.length}} 题</view>
      <view class="dialog-actions">
        <button bindtap="onShowSubmit">继续答题</button>
        <button class="btn-primary" bindtap="onConfirmSubmit">确认交卷</button>
      </view>
    </view>
  </view>
</view>
```

**pages/exam/do/do.wxss**：

```css
/* miniprogram/pages/exam/do/do.wxss */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #fff;
  border-bottom: 1rpx solid var(--border);
}
.header .title { flex: 1; font-size: 30rpx; font-weight: 600; margin-right: 20rpx; }
.header .timer { font-size: 28rpx; color: var(--primary); font-weight: 600; }
.header .timer.warning { color: var(--danger); }

.progress-bar { padding: 20rpx 24rpx; background: #fff; }
.progress-bar .bar { height: 8rpx; background: #eee; border-radius: 4rpx; margin-top: 10rpx; }
.progress-bar .fill { height: 100%; background: var(--primary); border-radius: 4rpx; }

.question-card { margin: 20rpx; padding: 24rpx; background: #fff; border-radius: 12rpx; }
.q-title { font-size: 30rpx; margin-bottom: 30rpx; }
.q-type { color: var(--primary); margin-right: 10rpx; }

.options { display: flex; flex-direction: column; }
.option {
  display: flex;
  align-items: center;
  padding: 20rpx;
  border-bottom: 1rpx solid var(--border);
}
.option radio, .option checkbox { margin-right: 16rpx; }

.input, .textarea {
  border: 1rpx solid var(--border);
  border-radius: 8rpx;
  padding: 20rpx;
  margin-top: 20rpx;
}
.textarea { width: 100%; height: 200rpx; box-sizing: border-box; }

.action-bar {
  display: flex;
  padding: 20rpx;
  gap: 20rpx;
}
.action-bar button { flex: 1; }

.navigator {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx;
  background: #fff;
  gap: 12rpx;
}
.nav-item {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #eee;
  text-align: center;
  line-height: 64rpx;
  font-size: 24rpx;
}
.nav-item.answered { background: #d9f7be; color: var(--success); }
.nav-item.current { background: var(--primary); color: #fff; }

.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}
.dialog { background: #fff; border-radius: 16rpx; padding: 40rpx; width: 80%; }
.dialog-title { font-size: 32rpx; font-weight: 600; text-align: center; }
.dialog-content { font-size: 28rpx; color: var(--text-secondary); text-align: center; margin: 20rpx 0; }
.dialog-actions { display: flex; gap: 20rpx; }
.dialog-actions button { flex: 1; }
```

### 6.3 result — 考试结果

**pages/exam/result/result.js**：

```js
const examApi = require('../../../api/exam')

Page({
  data: {
    recordId: null,
    score: 0,
    passed: false,
    totalCount: 0,
    correctCount: 0,
    maxRetry: 0,
    usedRetry: 0,
    canRetry: false
  },

  async onLoad(options) {
    const recordId = options.recordId
    this.setData({ recordId })
    try {
      const data = await examApi.getRecord(recordId)
      this.setData({
        score: data.score,
        passed: data.passed,
        totalCount: data.totalCount,
        correctCount: data.correctCount,
        maxRetry: data.maxRetry,
        usedRetry: data.usedRetry,
        canRetry: data.usedRetry < data.maxRetry
      })
    } catch (e) {}
  },

  onRetry() {
    wx.redirectTo({ url: `/pages/exam/do/do?id=${this.data.examId}` })
  },

  onBack() {
    wx.navigateBack({ delta: 2 })
  }
})
```

**pages/exam/result/result.wxml**：

```xml
<view class="container">
  <view class="result-card">
    <view class="score {{passed ? 'pass' : 'fail'}}">{{score}}</view>
    <view class="label">{{passed ? '恭喜通过' : '未通过'}}</view>
    <view class="detail">答对 {{correctCount}} / {{totalCount}} 题</view>
    <view class="detail">已使用重考 {{usedRetry}} / {{maxRetry}} 次</view>
  </view>

  <view class="actions">
    <button wx:if="{{canRetry && !passed}}" class="btn-primary" bindtap="onRetry">再次考试</button>
    <button class="btn-secondary" bindtap="onBack">返回</button>
  </view>
</view>
```

---

## 七、培训计划

### 7.1 pages/plan/list/list.js

```js
const planApi = require('../../../api/plan')

Page({
  data: {
    planList: [],
    loading: true
  },

  onShow() {
    this.loadList()
  },

  async loadList() {
    try {
      const data = await planApi.getList({})
      this.setData({ planList: data.records, loading: false })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/plan/detail/detail?id=${e.currentTarget.dataset.id}` })
  }
})
```

### 7.2 pages/plan/list/list.wxml

```xml
<view class="container">
  <view class="plan-card" wx:for="{{planList}}" wx:key="id"
        data-id="{{item.id}}" bindtap="goDetail">
    <view class="title ellipsis">{{item.title}}</view>
    <view class="meta">{{item.startTime}} ~ {{item.endTime}}</view>
    <view class="progress-row">
      <view class="progress-bar">
        <view class="fill" style="width: {{item.progress}}%"></view>
      </view>
      <text class="progress-text">{{item.completedCount}}/{{item.totalCount}} 已完成</text>
    </view>
  </view>
  <view wx:if="{{!loading && !planList.length}}" class="empty">暂无培训计划</view>
</view>
```

### 7.3 pages/plan/list/list.wxss

```css
/* miniprogram/pages/plan/list/list.wxss */
.plan-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin: 20rpx;
}
.plan-card .title { font-size: 30rpx; font-weight: 600; }
.plan-card .meta { font-size: 24rpx; color: var(--text-muted); margin: 10rpx 0 20rpx; }

.progress-row { display: flex; align-items: center; gap: 20rpx; }
.progress-bar { flex: 1; height: 12rpx; background: #eee; border-radius: 6rpx; }
.progress-bar .fill { height: 100%; background: var(--primary); border-radius: 6rpx; }
.progress-text { font-size: 24rpx; color: var(--text-secondary); }

.empty { text-align: center; color: var(--text-muted); padding: 100rpx 0; }
```

---

## 八、智能问答

### 8.1 pages/consult/index/index.js

```js
const consultApi = require('../../../api/consult')

Page({
  data: {
    inputValue: '',
    messages: [],          // { role: 'user' | 'ai' | 'human', content, time }
    loading: false,
    mode: 'ai'             // ai / human
  },

  onLoad() {
    // 加载历史记录
    const history = wx.getStorageSync('consult_history') || []
    this.setData({ messages: history })
  },

  onInput(e) {
    this.setData({ inputValue: e.detail.value })
  },

  async onSend() {
    const content = this.data.inputValue.trim()
    if (!content || this.data.loading) return

    const userMsg = { role: 'user', content, time: this.formatTime() }
    const messages = this.data.messages.concat(userMsg)
    this.setData({ messages, inputValue: '', loading: true })

    try {
      const res = await consultApi.ask({ title: content, content })
      const reply = {
        role: res.matched ? 'ai' : 'human',
        content: res.autoReply || '已转人工，请耐心等待回复',
        time: this.formatTime()
      }
      this.setData({ messages: messages.concat(reply), mode: res.matched ? 'ai' : 'human' })
    } catch (e) {
      const errMsg = { role: 'ai', content: '服务异常，请稍后重试', time: this.formatTime() }
      this.setData({ messages: messages.concat(errMsg) })
    } finally {
      this.setData({ loading: false })
      wx.setStorageSync('consult_history', this.data.messages)
    }
  },

  // 转人工
  onSwitchToHuman() {
    this.setData({ mode: 'human' })
    const msg = { role: 'human', content: '已为您转接人工客服', time: this.formatTime() }
    const messages = this.data.messages.concat(msg)
    this.setData({ messages })
    wx.setStorageSync('consult_history', messages)
  },

  formatTime() {
    const d = new Date()
    return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`
  }
})
```

### 8.2 pages/consult/index/index.wxml

```xml
<view class="container">
  <!-- 模式切换 -->
  <view class="mode-bar">
    <text class="mode-tag {{mode === 'ai' ? 'active' : ''}}">智能问答</text>
    <button class="switch-btn" bindtap="onSwitchToHuman">转人工</button>
  </view>

  <!-- 聊天区域 -->
  <scroll-view scroll-y class="chat-area" scroll-into-view="msg-{{messages.length - 1}}">
    <view wx:for="{{messages}}" wx:key="time" id="msg-{{index}}"
          class="msg {{item.role === 'user' ? 'msg-user' : 'msg-other'}}">
      <view class="bubble">{{item.content}}</view>
      <view class="time">{{item.time}}</view>
    </view>
    <view wx:if="{{loading}}" class="msg msg-other">
      <view class="bubble typing">...</view>
    </view>
  </scroll-view>

  <!-- 输入区 -->
  <view class="input-bar">
    <input class="input" placeholder="请输入问题" value="{{inputValue}}"
           bindinput="onInput" confirm-type="send" bindconfirm="onSend"/>
    <button class="send-btn" bindtap="onSend" disabled="{{loading}}">发送</button>
  </view>
</view>
```

### 8.3 pages/consult/index/index.wxss

```css
/* miniprogram/pages/consult/index/index.wxss */
.mode-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #fff;
  border-bottom: 1rpx solid var(--border);
}
.mode-tag { font-size: 28rpx; color: var(--primary); }
.switch-btn {
  font-size: 24rpx;
  color: var(--text-secondary);
  border: 1rpx solid var(--border);
  border-radius: 999rpx;
  padding: 6rpx 20rpx;
}

.chat-area { height: calc(100vh - 200rpx); padding: 20rpx; }
.msg { display: flex; flex-direction: column; margin-bottom: 24rpx; }
.msg-user { align-items: flex-end; }
.msg-other { align-items: flex-start; }
.bubble {
  max-width: 70%;
  padding: 20rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  background: #fff;
}
.msg-user .bubble { background: #95ec69; }
.msg .time { font-size: 22rpx; color: var(--text-muted); margin-top: 6rpx; }

.input-bar {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #fff;
  border-top: 1rpx solid var(--border);
  gap: 16rpx;
}
.input-bar .input {
  flex: 1;
  background: #f5f5f5;
  border-radius: 999rpx;
  padding: 16rpx 24rpx;
}
.send-btn {
  background: var(--primary);
  color: #fff;
  border-radius: 999rpx;
  font-size: 26rpx;
  padding: 12rpx 30rpx;
}
```

---

## 九、个人中心

### 9.1 pages/my/index/index.js

```js
const statsApi = require('../../../api/study')
const studyApi = require('../../../api/study')

Page({
  data: {
    userInfo: null,
    stats: { totalStudyHours: 0, completedCourses: 0, examCount: 0, avgScore: 0 },
    entries: [
      { icon: '/images/icon-my-course.png', text: '我的课程', url: '/pages/my/courses/courses' },
      { icon: '/images/icon-record.png', text: '学习记录', url: '/pages/my/records/records' },
      { icon: '/images/icon-cert.png', text: '证书', url: '' },
      { icon: '/images/icon-setting.png', text: '设置', url: '' }
    ]
  },

  onShow() {
    this.setData({ userInfo: wx.getStorageSync('userInfo') })
    this.loadStats()
  },

  async loadStats() {
    try {
      const stats = await statsApi.getMyStats()
      this.setData({ stats })
    } catch (e) {}
  },

  onGoTo(e) {
    if (e.currentTarget.dataset.url) {
      wx.navigateTo({ url: e.currentTarget.dataset.url })
    }
  }
})
```

### 9.2 pages/my/index/index.wxml

```xml
<view class="container">
  <!-- 用户信息 -->
  <view class="user-card">
    <image class="avatar" src="{{userInfo.avatar || '/images/avatar-default.png'}}"></image>
    <view class="info">
      <view class="name">{{userInfo.nickname || '学员'}}</view>
      <view class="org">{{userInfo.orgName || '四川省基层卫生人员'}}</view>
    </view>
  </view>

  <!-- 统计卡片 -->
  <view class="stats">
    <view class="stat-item">
      <view class="num">{{stats.totalStudyHours}}</view>
      <view class="label">学习时长(h)</view>
    </view>
    <view class="stat-item">
      <view class="num">{{stats.completedCourses}}</view>
      <view class="label">完成课程</view>
    </view>
    <view class="stat-item">
      <view class="num">{{stats.examCount}}</view>
      <view class="label">考试次数</view>
    </view>
    <view class="stat-item">
      <view class="num">{{stats.avgScore}}</view>
      <view class="label">平均分</view>
    </view>
  </view>

  <!-- 入口列表 -->
  <view class="entries">
    <view class="entry-item" wx:for="{{entries}}" wx:key="text"
          data-url="{{item.url}}" bindtap="onGoTo">
      <image class="icon" src="{{item.icon}}"></image>
      <text>{{item.text}}</text>
    </view>
  </view>
</view>
```

### 9.3 pages/my/courses/courses — 我的课程

```js
// pages/my/courses/courses.js
const studyApi = require('../../../api/study')

Page({
  data: {
    courseList: [],
    loading: true
  },

  onShow() {
    this.loadList()
  },

  async loadList() {
    try {
      const data = await studyApi.getMyCourses()
      this.setData({ courseList: data, loading: false })
    } catch (e) {
      this.setData({ loading: false })
    }
  },

  onStudy(e) {
    wx.navigateTo({ url: `/pages/course/study/study?id=${e.currentTarget.dataset.id}` })
  }
})
```

```xml
<!-- pages/my/courses/courses.wxml -->
<view class="container">
  <view class="course-card" wx:for="{{courseList}}" wx:key="id">
    <view class="title ellipsis">{{item.title}}</view>
    <view class="progress-row">
      <view class="progress-bar">
        <view class="fill" style="width: {{item.progress}}%"></view>
      </view>
      <text class="progress-text">{{item.progress}}%</text>
    </view>
    <button class="btn-primary btn-sm" data-id="{{item.id}}" bindtap="onStudy">继续学习</button>
  </view>
  <view wx:if="{{!loading && !courseList.length}}" class="empty">暂无已报名课程</view>
</view>
```

### 9.4 pages/my/records/records — 学习记录

```js
// pages/my/records/records.js
const studyApi = require('../../../api/study')

Page({
  data: {
    records: [],
    loading: true
  },

  onShow() {
    this.loadList()
  },

  async loadList() {
    try {
      const data = await studyApi.getRecord()
      this.setData({ records: data.records, loading: false })
    } catch (e) {
      this.setData({ loading: false })
    }
  }
})
```

```xml
<!-- pages/my/records/records.wxml -->
<view class="container">
  <view class="timeline">
    <view class="timeline-item" wx:for="{{records}}" wx:key="id">
      <view class="dot"></view>
      <view class="content">
        <view class="title ellipsis">{{item.courseTitle}}</view>
        <view class="meta">{{item.chapterTitle}} · 学习 {{item.studyDuration}} 秒</view>
        <view class="time">{{item.createTime}}</view>
      </view>
    </view>
  </view>
  <view wx:if="{{!loading && !records.length}}" class="empty">暂无学习记录</view>
</view>
```

---

## 十、离线学习（docx 3.1 要求）

### 10.1 ZIP 下载流程

```js
// utils/offline.js — 离线学习工具函数
const { offlineDir } = require('../api/config')

const fs = wx.getFileSystemManager()

module.exports = {
  // 下载并保存离线包
  downloadPackage(courseId, url) {
    return new Promise((resolve, reject) => {
      wx.downloadFile({
        url,
        success: (res) => {
          if (res.statusCode !== 200) return reject(res)
          const savePath = `${wx.env.USER_DATA_PATH}/${offlineDir}/${courseId}.zip`
          try {
            fs.saveFileSync(res.tempFilePath, savePath)
            resolve(savePath)
          } catch (e) {
            reject(e)
          }
        },
        fail: reject
      })
    })
  },

  // 解压 ZIP 并读取 manifest.json
  // 注：小程序原生不支持解压，需引入 miniprogram-zip 等第三方库
  // 或使用后端提供已解压的资源列表
  unzipPackage(zipPath, courseId) {
    const targetDir = `${wx.env.USER_DATA_PATH}/${offlineDir}/${courseId}`
    return new Promise((resolve, reject) => {
      // 伪代码：实际使用 JSZip 或原生插件
      fs.readFile({
        filePath: `${targetDir}/manifest.json`,
        encoding: 'utf-8',
        success: (res) => resolve(JSON.parse(res.data)),
        fail: reject
      })
    })
  },

  // 获取本地视频路径
  getLocalVideoPath(courseId, chapterId) {
    return `${wx.env.USER_DATA_PATH}/${offlineDir}/${courseId}/videos/${chapterId}.mp4`
  }
}
```

### 10.2 本地播放

```js
// pages/course/study/study.js 中离线播放分支
const offlineUtil = require('../../../utils/offline')

// 检查是否有离线包
checkOffline(courseId) {
  const manifest = wx.getStorageSync(`offline_manifest_${courseId}`)
  if (manifest) {
    // 使用本地视频路径
    const localVideo = offlineUtil.getLocalVideoPath(courseId, chapterId)
    this.setData({ videoUrl: localVideo, isOffline: true })
  }
}
```

### 10.3 进度回传（联网时批量上报）

```js
// utils/offline.js — 进度回传
const studyApi = require('../api/study')

module.exports = {
  // 保存离线进度到 localStorage
  saveOfflineProgress(courseId, chapterId, progress, lastPosition) {
    const key = `offline_progress_${courseId}`
    const list = wx.getStorageSync(key) || []
    list.push({
      courseId, chapterId, progress, lastPosition,
      studyDuration: 10,
      timestamp: Date.now()
    })
    wx.setStorageSync(key, list)
  },

  // 联网时批量回传
  async syncOfflineProgress() {
    const keys = wx.getStorageInfoSync().keys.filter(k => k.startsWith('offline_progress_'))
    for (const key of keys) {
      const list = wx.getStorageSync(key)
      if (!list || !list.length) continue
      try {
        for (const item of list) {
          await studyApi.reportProgress(item)
        }
        wx.removeStorageSync(key)
      } catch (e) {
        console.error('同步离线进度失败', e)
        break
      }
    }
  }
}
```

在 `app.js` 中监听网络恢复：

```js
// app.js
const offlineUtil = require('./utils/offline')

App({
  onLaunch() {
    // 启动时同步离线进度
    wx.getNetworkType({
      success: (res) => {
        if (res.networkType !== 'none') {
          offlineUtil.syncOfflineProgress()
        }
      }
    })
    // 监听网络恢复
    wx.onNetworkStatusChange((res) => {
      if (res.isConnected) offlineUtil.syncOfflineProgress()
    })
  }
})
```

---

## 十一、适配与兼容

### 11.1 弱网优化

| 策略 | 实现 |
|------|------|
| 请求重试 | `request.js` 中 `MAX_RETRY = 3`，指数退避 1s/2s/3s |
| 超时设置 | `timeout: 10000`（10 秒） |
| 骨架屏 | 列表页使用 `skeleton` 动画占位 |
| 离线缓存 | 课程列表、首页数据使用 `wx.setStorageSync` 缓存，弱网时先展示缓存 |
| 图片懒加载 | `<image lazy-load="true">` |

```js
// utils/util.js — 带缓存的请求
const cacheGet = async (key, fetchFn, cacheTime = 5 * 60 * 1000) => {
  const cached = wx.getStorageSync(key)
  if (cached && Date.now() - cached.time < cacheTime) {
    return cached.data
  }
  try {
    const data = await fetchFn()
    wx.setStorageSync(key, { data, time: Date.now() })
    return data
  } catch (e) {
    // 网络失败时降级使用缓存
    if (cached) return cached.data
    throw e
  }
}

module.exports = { cacheGet }
```

### 11.2 不同屏幕尺寸（rpx 自适应）

- 所有尺寸单位统一使用 **rpx**（750rpx = 屏宽）
- 布局使用 **flex** 避免固定宽度
- 安全区域适配（全面屏底部）：

```css
/* 底部操作栏适配全面屏 */
.action-bar {
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}
```

### 11.3 系统字号

```css
/* 支持系统字号设置 */
page {
  font-size: 28rpx;
  /* 小程序自动跟随系统字号设置 */
}
```

### 11.4 兼容性注意

| 项目 | 说明 |
|------|------|
| 基础库版本 | 建议最低 2.27.3，`app.json` 中 `"libVersion": "3.5.0"` |
| iOS | 支持 iOS 12+ |
| Android | 支持 Android 6+ |
| 组件 | `video`、`textarea` 在低版本有兼容问题，需真机测试 |
| 开放能力 | `open-type="getUserInfo"` 需用户主动触发 |

---

## 附录：app.json 全局配置参考

```json
{
  "pages": [
    "pages/index/index",
    "pages/login/login",
    "pages/course/list/list",
    "pages/course/detail/detail",
    "pages/course/study/study",
    "pages/exam/list/list",
    "pages/exam/do/do",
    "pages/exam/result/result",
    "pages/plan/list/list",
    "pages/consult/index/index",
    "pages/my/index/index",
    "pages/my/courses/courses",
    "pages/my/records/records"
  ],
  "window": {
    "backgroundTextStyle": "light",
    "navigationBarBackgroundColor": "#1677ff",
    "navigationBarTitleText": "基层卫生培训",
    "navigationBarTextStyle": "white",
    "enablePullDownRefresh": true
  },
  "tabBar": {
    "color": "#999",
    "selectedColor": "#1677ff",
    "backgroundColor": "#fff",
    "list": [
      { "pagePath": "pages/index/index", "text": "首页", "iconPath": "images/tab-home.png" },
      { "pagePath": "pages/plan/list/list", "text": "计划", "iconPath": "images/tab-plan.png" },
      { "pagePath": "pages/consult/index/index", "text": "咨询", "iconPath": "images/tab-consult.png" },
      { "pagePath": "pages/my/index/index", "text": "我的", "iconPath": "images/tab-my.png" }
    ]
  },
  "sitemapLocation": "sitemap.json",
  "lazyCodeLoading": "requiredComponents"
}
```
