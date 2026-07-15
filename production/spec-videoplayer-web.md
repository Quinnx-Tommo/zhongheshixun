# Spec: Web 端视频播放器（防作弊 + 断点续播 + 心跳上报）

> **对应分工**：学生乙 - 课程与学习模块 - 视频播放增强
> **阶段**：1（Specify）
> **设计原则**：契约优先 / 加法优于修改 / Hyrum's Law（不破坏 v3.4 observable behavior）/ ponytail（最小改动）

---

## Objective

### 目标
将 web-student 学习页 [learn.vue](file:///d:/javaEE/code/Project/project02/zhongheshixun/web-student/src/views/course/learn.vue) 的原生 `<video>` 标签升级为 vue-video-player（Video.js 封装），实现三层防作弊 + 断点续播 + 心跳上报，让学习进度数据可信。

### 用户故事
- 作为**学员**：刷新页面或切换章节后能从上次播放位置继续，不用重头看
- 作为**管理员**：学员的学习进度数据真实反映观看行为，不能拖进度条作弊
- 作为**答辩教师**：能看到视频播放有防作弊机制，学习进度统计有可信数据来源

### 成功画面
1. 学员进入学习页，视频自动从上次位置恢复播放
2. 学员拖动进度条跳跃 > 5 秒，被强制回退并提示"不允许快进"
3. 学员正常观看，每 30 秒自动上报一次进度（无感知）
4. 视频播放结束，自动触发 `complete-chapter` 标记章节完成
5. 管理后台统计的"今日活跃/在线人数"数据来自真实心跳

---

## Assumptions（关键假设，评审时确认）

| # | 假设 | 默认值 | 备选 |
|---|------|--------|------|
| A1 | 防快进严格度 | 跳跃 > 5 秒回退（允许 5 秒内微调） | 完全禁拖动 / > 10 秒回退 |
| A2 | 心跳上报间隔 | 30 秒（在线教育行业标准） | 15 秒（更严格）/ 60 秒（省流量） |
| A3 | 倍速策略 | 禁用倍速（基层培训学时是硬性要求） | 允许 1.5x（studyDuration 按实际时长计） |
| A4 | 视频源格式 | 演示用 MP4 直链（chapter.content 字段已是 URL） | 生产级 HLS m3u8（本次不做） |
| A5 | 播放器库 | vue-video-player@2.x（Video.js 8 封装） | xgplayer / ArtPlayer |
| A6 | 仅改 web-student | 小程序端不在本次范围 | — |

> ⚠️ 如果以上任一假设不成立，请在评审阶段指出，我会调整 spec。

---

## Tech Stack

| 组件 | 版本 | 用途 |
|------|------|------|
| vue-video-player | ^2.1.0 | Video.js 的 Vue3 封装组件 |
| video.js | ^8.10.0 | 底层播放器引擎 |
| hls.js | ^1.5.0（可选） | 未来 HLS 支持，本次 MP4 不需要 |
| Vue | 3.x | 现有框架 |
| Element Plus | 现有 | ElMessage 提示 |

**零后端改动**：复用现有接口
- `POST /api/study/progress`（reportProgress，含 lastPosition/studyDuration/completed）
- `GET /api/study/progress/{courseId}`（拿 lastPosition 恢复）
- `POST /api/study/complete-chapter`（视频结束触发）

---

## Commands

```bash
# 安装依赖
cd web-student
npm install vue-video-player video.js --save

# 开发调试
npm run dev

# 构建验证
npm run build
```

---

## Project Structure

```
web-student/
├── src/
│   ├── views/course/
│   │   └── learn.vue              # 【改】替换原生 video → vue-video-player
│   ├── components/
│   │   └── VideoPlayer.vue        # 【新】封装播放器组件（防快进+心跳+断点续播）
│   ├── api/
│   │   └── study.js               # 【无改动】已有 reportProgress/getProgress/completeChapter
│   └── utils/
│       └── video-guard.js         # 【新】防作弊工具函数（seeking 拦截 + 心跳节流）
```

**改动文件**：1 改 + 2 新（共 3 文件，符合 ponytail "改动 ≤ 5 文件"）

---

## Code Style

### 封装原则
- VideoPlayer.vue 是**纯展示 + 事件透传**组件，不直接调 API
- 业务逻辑（心跳上报、完成触发）由 learn.vue 通过事件回调处理
- video-guard.js 是无状态工具函数，可单测

### 组件 props 契约

```javascript
// VideoPlayer.vue props
{
  src: String,           // 视频地址（chapter.content）
  initialTime: Number,   // 断点续播起始位置（秒，来自 lastPosition）
  disableSeek: {         // 防快进配置
    type: Boolean,
    default: true
  },
  heartbeatInterval: {   // 心跳间隔（秒）
    type: Number,
    default: 30
  }
}

// VideoPlayer.vue emits
emit('timeupdate', { currentTime, duration, studyDuration })  // 心跳触发
emit('ended', { totalTime })                                    // 播放结束
emit('seek-blocked', { attemptTime, restoredTime })            // 防快进触发（用于提示）
```

### 防快进实现示例

```javascript
// video-guard.js
let lastValidTime = 0

export function createSeekGuard(player, onBlocked) {
  player.on('timeupdate', () => {
    lastValidTime = player.currentTime()
  })
  player.on('seeking', () => {
    const diff = player.currentTime() - lastValidTime
    if (diff > 5) {
      player.currentTime(lastValidTime)
      onBlocked?.(player.currentTime(), lastValidTime)
    }
  })
}
```

---

## Testing Strategy

### 验证层级

| 层级 | 方法 | 验收点 |
|------|------|--------|
| 构建 | `npm run build` | vite build 通过，无依赖错误 |
| 接口 | curl 复用现有接口 | 心跳上报后 GET progress 看到 lastPosition 更新 |
| 浏览器 E2E | 手动测 4 场景 | 见下方 |
| 回归 | 静态 review | 章节切换/标记完成/未报名拦截 3 项不破坏 |

### 浏览器 E2E 4 场景

1. **断点续播**：播放视频 30 秒 → 刷新页面 → 视频从 30 秒位置恢复
2. **防快进**：播放 10 秒 → 拖进度条到 5 分钟 → 被回退到 10 秒 + 提示
3. **心跳上报**：播放 35 秒 → Network 面板看到 1 次 `/api/study/progress` 请求
4. **自动完成**：让视频自然播放结束 → 章节出现 ✓ 标记 + 按钮"已完成"

### 回归 3 项（v3.4 不能破坏）

- T5: learn.vue 章节渲染（v3.3 P0-1 修复保持）
- T6: detail.vue finished 标签（本次不碰 detail.vue）
- T7: check-enrolled 报名校验（VideoPlayer 不影响 enrolled 逻辑）

---

## Boundaries

### Always do
- 复用现有 `reportProgress` / `getProgress` / `completeChapter` 接口，零后端改动
- 防快进回退后必须给用户提示（ElMessage.warning）
- 心跳上报失败不阻塞播放（catch 静默，下次重试）
- VideoPlayer 组件 props 加法式扩展，不改现有 api/study.js

### Ask first
- 是否引入 hls.js（本次 MP4 不需要，但生产级 HLS 需要）
- 是否调整心跳间隔（默认 30 秒，可改）
- 防快进严格度（默认 5 秒容差，可改）

### Never do
- 不改后端任何 Java 代码（StudyServiceImpl / StudyApiController 保持 v3.4）
- 不改 api/study.js 已有函数签名（加法式，可加新函数但不改旧的）
- 不破坏 v3.4 已通过的 5 项 curl 验证
- 不碰小程序端视频（本次仅 web-student）

---

## Success Criteria

| # | 条件 | 验证方法 |
|---|------|---------|
| S1 | `npm run build` 通过 | 构建无 error |
| S2 | 断点续播生效 | 刷新后视频从 lastPosition 恢复 |
| S3 | 防快进生效 | 拖动 > 5 秒被回退 + 提示 |
| S4 | 心跳上报生效 | 30 秒触发 1 次 reportProgress |
| S5 | 自动完成生效 | 视频结束触发 completeChapter |
| S6 | v3.4 回归 PASS | T5/T6/T7 静态 review 无破坏 |
| S7 | 零后端改动 | git diff training-parent/ 无变更 |

---

## Open Questions

1. **A1 防快进容差**：5 秒是否合理？完全禁拖动会不会影响用户体验？
2. **A3 倍速**：禁用倍速是否影响学员体验？（基层卫生人员可能希望 1.5x 加速学习）
3. **视频源**：演示用 chapter.content 现有 URL 是否可用？是否需要准备测试视频？
4. **VideoPlayer 组件复用**：是否考虑后续在 my-courses 或其他页面也用？影响封装设计

---

## Out of Scope（本次不做）

- 小程序端视频播放（用户明确仅做 web）
- HLS m3u8 切片加密（生产级，答辩口径提及即可）
- URL 签名防盗链（生产级）
- 视频弹幕、倍速记忆、画质切换等高级功能
- 视频上传/转码后台管理
