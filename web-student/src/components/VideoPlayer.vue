<template>
  <div class="video-player-wrapper">
    <video-player
      ref="playerRef"
      class="vjs-custom-skin"
      :sources="videoSources"
      :playsinline="true"
      :controls="true"
      :playback-rates="playbackRates"
      :options="playerOptions"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, computed, nextTick } from 'vue'
import { VideoPlayer } from 'vue-video-player'
import 'video.js/dist/video-js.css'
import { createSeekGuard } from '@/utils/video-guard'

const props = defineProps({
  // 视频地址
  src: { type: String, required: true },
  // 断点续播起始位置（秒）
  initialTime: { type: Number, default: 0 },
  // 是否启用防快进
  disableSeek: { type: Boolean, default: true },
  // 防快进容差（秒）
  seekThreshold: { type: Number, default: 5 },
  // 心跳间隔（秒）
  heartbeatInterval: { type: Number, default: 30 }
})

const emit = defineEmits([
  'heartbeat',    // { currentTime, duration, studyDuration, progress }
  'ended',        // { totalTime }
  'seek-blocked'  // { attemptTime, restoredTime }
])

// 倍速策略：禁用倍速（基层培训学时硬性要求，spec A3）
const playbackRates = []

const playerRef = ref(null)
let player = null
let seekGuardCleanup = null
let heartbeatTimer = null
let studyDurationAcc = 0  // 本次会话累计学习时长（秒），暂停不累加

const playerOptions = {
  preload: 'auto',
  fluid: true,
  language: 'zh-CN',
  controlBar: {
    playbackRateMenuButton: false  // 隐藏倍速菜单
  }
}

// vue-video-player@6（@videojs-player/vue）使用 sources prop（SourceObject[]），
// 不支持 src prop（会被当 fallthrough attr 透传到 div 而非 video 元素）。
// 章节切换时 props.src 变化 → videoSources 重算 → video.js 自动重载源。
const videoSources = computed(() => {
  if (!props.src) return []
  return [{ src: props.src, type: 'video/mp4' }]
})

function onPlay() {
  startHeartbeat()
}

function onPause() {
  stopHeartbeat()
  // 暂停时立即上报一次，保存 lastPosition
  emitHeartbeat()
}

function onEnded() {
  stopHeartbeat()
  const totalTime = Math.floor(player?.duration() || 0)
  emit('ended', { totalTime })
}

function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    studyDurationAcc += props.heartbeatInterval
    emitHeartbeat()
  }, props.heartbeatInterval * 1000)
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

function emitHeartbeat() {
  if (!player) return
  const currentTime = Math.floor(player.currentTime() || 0)
  const duration = Math.floor(player.duration() || 0)
  const progress = duration > 0 ? Math.min(100, Math.floor((currentTime / duration) * 100)) : 0
  emit('heartbeat', {
    currentTime,
    duration,
    studyDuration: studyDurationAcc,
    progress
  })
}

// 在 player 已 ready 后恢复断点续播位置。
// 必须等 loadedmetadata（duration 可用）后再设 currentTime，否则 video.js 会重置为 0。
function restorePosition() {
  if (!player || props.initialTime <= 0) return
  const doRestore = () => {
    const dur = player.duration()
    if (dur && !isNaN(dur) && props.initialTime < dur) {
      player.currentTime(props.initialTime)
      studyDurationAcc = Math.floor(props.initialTime)
    }
  }
  const dur = player.duration()
  if (dur && !isNaN(dur)) {
    doRestore()
  } else {
    player.one('loadedmetadata', doRestore)
  }
}

// 拿到 player 实例后绑定事件 + 防快进 + 断点续播。
// 不用 vue-video-player 的事件 prop（Vue3 把 onXxx 当事件监听器，组件未声明 emits 会丢弃），
// 改用 video.js 原生 player.on() 绑定，最可靠。
function bindPlayer(p) {
  if (!p || player === p) return
  player = p
  // player.ready：若已 ready 立即执行，否则等 ready 事件
  player.ready(function () {
    restorePosition()
    if (props.disableSeek) {
      seekGuardCleanup = createSeekGuard(player, {
        threshold: props.seekThreshold,
        initialTime: props.initialTime,
        onBlocked: (attemptTime, restoredTime) => {
          emit('seek-blocked', { attemptTime, restoredTime })
        }
      })
    }
  })
  // 事件绑定（video.js 原生 API）
  player.on('play', onPlay)
  player.on('pause', onPause)
  player.on('ended', onEnded)
}

function obtainPlayer() {
  // 优先 ref 暴露的 player，fallback 到 DOM 查询（.video-js 元素的 .player 属性）
  return playerRef.value?.player || document.querySelector('.video-js')?.player || null
}

function setupWhenReady(attempt = 0) {
  const p = obtainPlayer()
  if (p) {
    bindPlayer(p)
    return
  }
  // player 尚未创建，轮询等待（最多 1 秒）
  if (attempt < 10) {
    setTimeout(() => setupWhenReady(attempt + 1), 100)
  }
}

onMounted(() => {
  nextTick(() => setupWhenReady())
})

// src 变化（章节切换）时重置累计时长
watch(() => props.src, () => {
  stopHeartbeat()
  studyDurationAcc = 0
})

// initialTime 变化（章节切换到有断点的章节）时恢复位置
watch(() => props.initialTime, (newVal) => {
  if (!player || !newVal) return
  // 章节切换后 video.js 换源，等新源 loadedmetadata 再恢复
  player.one('loadedmetadata', () => {
    const dur = player.duration()
    if (dur && !isNaN(dur) && newVal < dur) {
      player.currentTime(newVal)
    }
  })
})

onBeforeUnmount(() => {
  stopHeartbeat()
  seekGuardCleanup?.()
  if (player) {
    player.off('play', onPlay)
    player.off('pause', onPause)
    player.off('ended', onEnded)
    player.dispose?.()
    player = null
  }
})
</script>

<style scoped>
.video-player-wrapper {
  width: 100%;
}
.video-player-wrapper :deep(.video-js) {
  width: 100%;
  max-height: 500px;
  border-radius: 4px;
  background: #000;
}
</style>
