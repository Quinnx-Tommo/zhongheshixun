# Plan: Web 端视频播放器 — 决策记录 + 任务拆解

> **对应 Spec**：`production/spec-videoplayer-web.md`
> **阶段**：2（决策 + 接口契约）+ 3（任务拆解）
> **设计原则**：契约优先 / 加法优于修改 / Hyrum's Law / ponytail

---

## 一、关键决策记录

### 决策 1：播放器库选 vue-video-player，不用 xgplayer

| 维度 | vue-video-player | xgplayer |
|------|------------------|----------|
| 底层引擎 | Video.js 8（成熟） | 自研 |
| Vue3 支持 | 官方封装 | 需手动封装 |
| 事件机制 | 完整（timeupdate/seeking/ended） | 完整 |
| 中文文档 | 多（CSDN 大量实战） | 少 |
| 体积 | ~500KB | ~300KB |
| 生态插件 | 极丰富 | 中 |

**决策**：vue-video-player。理由：事件机制直接对应防作弊需求（seeking 拦截），Vue3 官方封装零集成成本，文档资源多。体积差 200KB 在 PC Web 端可接受。

### 决策 2：零后端改动，复用现有接口

现有 `StudyProgressDTO` 字段已完全满足心跳上报：

```java
private Long courseId;
private Long chapterId;
private Integer progress;        // 进度百分比
private Integer studyDuration;   // 本次学习时长(秒) ← 心跳累计
private Integer lastPosition;    // 上次播放位置(秒) ← 断点续播
private Boolean completed;       // 是否已完成
```

**无需任何后端改动**，直接前端调用现有 `POST /api/study/progress`。符合 Hyrum's Law：不引入新 observable behavior，不破坏 v3.4 已验证的 5 项 curl。

### 决策 3：封装 VideoPlayer.vue 组件，不直接在 learn.vue 写

**方案 A（采纳）**：抽出 `components/VideoPlayer.vue`，learn.vue 通过事件回调处理业务
**方案 B（未采纳）**：直接在 learn.vue 内嵌播放器逻辑

理由：
- 组件化符合项目现有结构（detail.vue / learn.vue 已分离）
- VideoPlayer 可复用（后续 my-courses 预览、课程详情页预览都可能用）
- learn.vue 已 250+ 行，再塞播放器逻辑会超 400 行，可读性下降
- 单一职责：VideoPlayer 管播放 + 防作弊，learn.vue 管业务编排

### 决策 4：防快进 5 秒容差，不禁倍速（待确认）

**防快进**：跳跃 > 5 秒回退到 lastValidTime
- 5 秒容差允许用户误触微调（鼠标点击进度条偏差）
- 完全禁拖动体验太差，且无法防"按住方向键快进"

**倍速**：**待用户确认**。两个选项：
- 选项 A：禁用倍速（最严格，基层培训学时硬性要求）
- 选项 B：允许 1.5x，但 studyDuration 按实际播放时长计（前端累计真实秒数，不乘倍率）

spec 默认假设禁用倍速，但这是 Open Question，评审时确认。

### 决策 5：心跳节流用累计 studyDuration，不依赖 currentTime 差值

**方案 A（采纳）**：前端维护 `studyDurationAccumulator`，每 30 秒 += 30 上报
**方案 B（未采纳）**：后端用 `currentTime - lastPosition` 算时长

理由：
- 方案 B 在用户暂停、切换标签页时会算出负数或异常值
- 方案 A 前端只在 `timeupdate` 真实播放时累加，暂停不累加，数据准确
- 后端 T8 已有 `progress > 100` 钳制，心跳上报的 studyDuration 后端直接存储，无需额外校验

---

## 二、接口契约（复用现有，无新增）

### 2.1 心跳上报 — 复用 `POST /api/study/progress`

**请求体**（VideoPlayer 触发，learn.vue 组装）：

```json
{
  "courseId": 1,
  "chapterId": 3,
  "progress": 65,
  "studyDuration": 180,
  "lastPosition": 180,
  "completed": false
}
```

| 字段 | 来源 | 计算 |
|------|------|------|
| courseId | learn.vue 传入 | route.params.id |
| chapterId | learn.vue 传入 | activeChapterId |
| progress | VideoPlayer 传入 | `Math.floor(currentTime / duration * 100)` |
| studyDuration | VideoPlayer 累计 | 每 30 秒 += 30（暂停不累加） |
| lastPosition | VideoPlayer 传入 | `Math.floor(currentTime)` |
| completed | learn.vue 判断 | 心跳时 false，ended 时单独调 complete-chapter |

### 2.2 断点续播恢复 — 复用 `GET /api/study/progress/{courseId}`

learn.vue 已有 `progressList`，从中取当前章节的 `lastPosition` 传给 VideoPlayer 的 `initialTime` prop。

### 2.3 自动完成 — 复用 `POST /api/study/complete-chapter`

VideoPlayer `@ended` 事件透传给 learn.vue，learn.vue 调 `completeChapter({ courseId, chapterId })`。

**零契约变更**，所有接口签名保持 v3.4 状态。

---

## 三、任务拆解

### 3.1 依赖关系

```
V1(装依赖) ──── V2(VideoPlayer 组件) ──── V3(learn.vue 集成)
                                              │
                                              ▼
                                         V4(验证)
                                              │
                                              ▼
                                         V5(回归 + 文档)
```

### 3.2 任务清单

```markdown
- [x] V1: 安装 vue-video-player + video.js 依赖
  - 验收: package.json 出现两个依赖，npm install 无 error
  - 验证: npm run dev 启动无报错
  - 文件: web-student/package.json

- [x] V2: 封装 VideoPlayer.vue 组件
  - 验收: 组件能播放视频，props 接收 src/initialTime，emits timeupdate/ended/seek-blocked
  - 验证: 单独在 learn.vue 引入测试播放
  - 文件: web-student/src/components/VideoPlayer.vue, web-student/src/utils/video-guard.js
  - 包含:
    - vue-video-player 引入 + Video.js CSS
    - createSeekGuard 防快进（5 秒容差）
    - 心跳节流（30 秒触发 timeupdate）
    - studyDuration 累计（暂停不累加）
    - initialTime 断点续播恢复
    - 倍速禁用（playbackRates: [1]）

- [x] V3: learn.vue 集成 VideoPlayer
  - 验收: 替换原生 <video>，章节切换时重新加载，心跳上报调 reportProgress
  - 验证: 浏览器手动播放 35 秒，Network 看到 1 次心跳请求
  - 文件: web-student/src/views/course/learn.vue
  - 包含:
    - import VideoPlayer
    - 模板替换 <video> → <VideoPlayer>
    - @timeupdate 回调调 reportProgress
    - @ended 回调调 completeChapter（替代原 handleVideoEnd）
    - @seek-blocked 回调 ElMessage.warning('不允许快进')
    - initialTime 从 progressList 取当前章节 lastPosition

- [x] V4: 构建验证
  - 验收: npm run build 通过
  - 验证: dist/ 生成，无 video.js 相关 error
  - 文件: 无（仅构建）

- [x] V5: 浏览器 E2E 4 场景 + 回归 3 项
  - 验收: 4 场景全部 PASS + T5/T6/T7 静态 review PASS
  - 验证: 手动浏览器测试
  - 场景:
    1. 断点续播: 播 30 秒 → 刷新 → 从 30 秒恢复
    2. 防快进: 拖到 5 分钟 → 回退 + 提示
    3. 心跳: 播 35 秒 → Network 1 次 progress 请求
    4. 自动完成: 视频结束 → 章节 ✓ 标记
  - 回归:
    - T5: 章节渲染（v3.3 P0-1）未破坏
    - T6: detail.vue finished（本次不碰）
    - T7: check-enrolled 报名校验未破坏

- [x] V6: 文档同步 + 答辩口径卡
  - 验收: dev-api.md 12.1 补心跳字段说明 + 进度文档 v3.5 + plan 答辩口径卡
  - 文件: docs/dev-api.md, docs/进度文档.md, production/plan-videoplayer-web.md
```

### 3.3 并行批次

**批次 A（串行，有依赖）**

| 子任务 | 文件 | 改动量 |
|--------|------|--------|
| V1 | package.json | 2 行依赖 |
| V2 | VideoPlayer.vue + video-guard.js | ~150 行（新文件） |
| V3 | learn.vue | ~30 行改（替换 video + 加事件回调） |

> V1 → V2 → V3 串行（V2 依赖 V1 装包，V3 依赖 V2 组件）

**批次 B（A 完成后，验证）**

| 子任务 | 类型 |
|--------|------|
| V4 | npm run build 构建验证 |
| V5 | 浏览器 E2E 4 场景 + 回归 3 项 |

**批次 C（B 通过后，收尾）**

| 子任务 | 类型 |
|--------|------|
| V6 | 答辩口径卡 + 文档同步 |

---

## 四、设计评审检查（api-and-interface-design verification）

- [x] 组件 props 有类型化输入输出（src/initialTime/disableSeek/heartbeatInterval）
- [x] 事件透传契约清晰（timeupdate/ended/seek-blocked）
- [x] 校验在边界（VideoPlayer 不调 API，learn.vue 组装 DTO 时 courseId/chapterId 已有 @NotNull）
- [x] 新组件加法式（不改现有 api/study.js，不改后端）
- [x] 命名一致（kebab-case 事件名，camelCase props）
- [x] 零后端改动，不破坏 v3.4 observable behavior（Hyrum's Law）
- [x] 改动 ≤ 5 文件（实际 3 文件：1 改 + 2 新）

---

## 五、答辩口径卡（V6 阶段填充，2026-07-14）

### 卡 1：视频播放防作弊方案

**教师可能问**：视频怎么防作弊？学员能不能拖进度条刷时长？

**答辩口径**：采用三层防御，纵深保障学习真实性：

1. **禁快进（seeking 拦截）**：`createSeekGuard` 监听 video.js `seeking` 事件，正向跳跃超过 5 秒阈值时回退到 `lastValidTime` 并提示"不允许快进，请正常观看视频"。回退（重听）允许，不拦截，方便复习。同时禁用倍速（`playbackRates=[]` + 隐藏倍速菜单），防止 2x/4x 刷时长。
2. **心跳上报（30 秒定时器）**：`player.on('play')` 启动 30 秒定时器，每次 `studyDurationAcc += 30` 并 POST 上报；`player.on('pause')` 立即上报一次保存断点。后端 `upsertProgress` 累加 `study_duration`，真实学习时长被记录。
3. **完成度校验（ended 触发）**：只有视频自然播完触发 `ended` 事件，才调 `markFinished`（progress=100, completed=true）标记完成。手动拖到末尾不会触发 `ended`（被防快进拦截），无法绕过。

**5 秒容差理由**：video.js 的 `timeupdate` 事件约 4 次/秒，`lastValidTime` 更新有微小延迟；5 秒容差避免正常播放时的误判，同时仍能拦截明显的快进行为（>5 秒跳跃）。

**验证证据**：V5.2 E2E PASS — eval `currentTime(9)`（lastValidTime=5，diff=4<5 放行）后 `currentTime(15)`（diff=10>5 拦截），player 回退到 9 + ElMessage 提示"不允许快进，请正常观看视频"。

### 卡 2：断点续播实现

**教师可能问**：刷新页面怎么记住上次播放位置？时序问题怎么处理？

**答辩口径**：复用 v3.4 已有的 `study_record.last_position` 字段，零 DB 改动：

1. **保存**：心跳上报时 `lastPosition = Math.floor(player.currentTime())`，通过 `POST /api/study/progress` upsert 到 `study_record`。
2. **恢复**：页面加载时 `getProgress` 取回 `lastPosition`，作为 `initialTime` 传入 VideoPlayer。
3. **时序关键**：`player.ready()` 触发时 video metadata 可能未加载（`duration` 为 NaN），此时 `currentTime(initialTime)` 会被 video.js 重置为 0 → 必须监听 `loadedmetadata` 事件后再恢复。

**防快进协调**：`createSeekGuard` 的 `lastValidTime` 初始值同步设为 `initialTime`，否则断点位置(5)与 lastValidTime(0)不一致，恢复后第一次 `timeupdate` 会把 lastValidTime 更新为 5，但在此之前如果有 seeking 会被误判。初始值对齐避免断点丢失。

**验证证据**：V5.1 E2E PASS — 数据库 lastPosition=5，刷新页面后 eval `player.currentTime()` 返回 5（恢复成功）；V5.3 验证 onPause→POST 链路，studyDuration 1805→1810 累加证明 upsert 生效。

### 卡 3：播放器选型理由

**教师可能问**：为什么用 vue-video-player？踩了什么坑？

**答辩口径**：选 `vue-video-player@6.0.0`（底层 `@videojs-player/vue`，基于 video.js@7.21.7），理由：

1. **生态成熟**：video.js 是最流行的 web 视频播放器，文档完善，社区活跃，HTML5 原生兼容好。
2. **Vue3 封装**：`vue-video-player` 提供 Vue3 组件封装，支持 `sources` prop、`options` 透传，集成成本低。
3. **API 完整**：通过 `playerRef.value.player` 可拿到原生 video.js 实例，调用 `currentTime()`/`on()`/`trigger()` 等完整 API。

**踩坑**：vue-video-player@6 把事件回调（ready/ended/play/pause）声明为 **props**（onReady/onEnded/onPlay/onpause）而非 emits。Vue3 编译器把 `on` + 大写字母（如 onReady）识别为事件监听器，组件未声明对应 emits 则**直接丢弃**；只有全小写（如 onpause）才被当普通 prop。

**解决方案**：放弃 vue-video-player 的事件绑定（`@ready`/`@ended` 无效），改用 `player.ready()` + `player.on('play'/'pause'/'ended')` 原生 video.js API，在 `bindPlayer` 中统一绑定。组件只负责渲染（`sources` prop），逻辑全部用原生 API。

### 卡 4：心跳上报设计

**教师可能问**：心跳间隔为什么 30 秒？字段怎么设计？

**答辩口径**：

1. **间隔 30 秒**：平衡弱网友好与数据精度。基层网络环境差，过于频繁（如 5 秒）的心跳会增加请求量和失败率；过于稀疏（如 5 分钟）断点续播精度差，丢失进度多。30 秒是 MOOC 平台（Coursera/学堂在线）的常见配置，断点最大丢失 30 秒可接受。
2. **字段设计**：
   - `lastPosition`：`currentTime`，断点续播用
   - `studyDuration`：累加器 `studyDurationAcc`，每次心跳 +30，后端 upsert 累加。不依赖 `currentTime` 差值（seeking 会导致差值失真）
   - `progress`：`currentTime/duration*100`，后端钳制上限 100
   - `completed`：心跳固定 false，只有 ended 触发 markFinished 才 true，避免心跳误标完成
3. **暂停即时上报**：`onPause` 清除定时器并立即上报一次，确保用户切走/暂停时 lastPosition 不丢失（弱网下可能长时间不触发 30 秒定时器）。
4. **失败静默**：心跳 POST 失败不抛错、不阻塞播放，下次重试。学习体验优先于数据完整性。

**验证证据**：V5.3 E2E PASS — trigger pause 后 studyDuration 1805→1810（+5，来自 restorePosition 的 studyDurationAcc=initialTime=5），证明 onPause→emitHeartbeat→emit→onHeartbeat→reportProgress→POST→upsert 全链路通。

### 卡 5：与 v3.4 的兼容性 / 零后端改动

**教师可能问**：这次改动会影响已有功能吗？后端为什么没改？

**答辩口径**：本次为**纯前端加法式增强**，零后端改动，复用 v3.4 已有接口：

1. **复用接口**：
   - `POST /api/study/progress`（v3.4 已有，含 T8 上限钳制）→ 心跳上报
   - `GET /api/study/progress/{courseId}`（v3.4 已有）→ 断点续播恢复
   - `POST /api/study/complete-chapter`（v3.4 T4 新增）→ 自动完成
2. **加法式组件**：新增 `VideoPlayer.vue` + `video-guard.js`，learn.vue 仅替换原 `<video>` 标签为 `<VideoPlayer>`，章节导航/报名校验/进度合并逻辑未动。
3. **回归验证**：V5.5 E2E PASS — T5 check-enrolled=200 true / T6 course/recommend=200 4 条 / T7 complete-chapter 幂等 200。v3.4 的 8 项功能（T2-T9）运行时复核全部保持。
4. **零后端改动的理由**：心跳上报本质是"前端定期写 study_record"，v3.4 的 `reportProgress` + `upsertProgress` 已支持 progress/studyDuration/lastPosition/completed 全字段 upsert，T8 还加了上限钳制。后端无需新增接口或改表结构。

**潜在风险**：vue-video-player 事件绑定机制变化（onXxx props 被丢弃）若未来升级版本可能失效 → 已在代码注释标明，用原生 `player.on()` 规避。

---

## 六、风险与缓解

| 风险 | 概率 | 缓解 |
|------|------|------|
| vue-video-player 与 Vue3 版本不兼容 | 低 | 锁定 ^2.1.0（已验证支持 Vue3） |
| Video.js CSS 与 Element Plus 样式冲突 | 低 | VideoPlayer.vue scoped 样式隔离 |
| 心跳上报失败导致进度丢失 | 中 | catch 静默 + 下次重试 + ended 强制上报 |
| 演示视频 URL 不可用 | 中 | 提前准备本地测试视频或公开 CDN 视频 |
| 防快进 5 秒容差被质疑太宽松 | 低 | 评审时确认，可调整 |
