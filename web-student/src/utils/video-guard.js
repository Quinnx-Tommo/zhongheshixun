/**
 * 视频播放防作弊工具函数
 * 单一职责：seeking 拦截（防快进），无状态，可单测
 */

/**
 * 创建防快进守卫
 * @param {Object} player - video.js player 实例
 * @param {Object} opts
 * @param {number} opts.threshold - 允许的跳跃秒数容差（默认 5 秒）
 * @param {Function} opts.onBlocked - 被拦截回调 (attemptTime, restoredTime) => void
 * @returns {Function} cleanup - 注销监听
 */
export function createSeekGuard(player, opts = {}) {
  const threshold = opts.threshold ?? 5
  // 初始为断点续播位置，避免防快进误回退到 0 丢失断点
  let lastValidTime = opts.initialTime ?? 0
  let isSeekingBlocked = false

  const onTimeUpdate = () => {
    // 拦截回退期间不更新 lastValidTime，避免回退后被新的 seeking 误判
    if (!isSeekingBlocked) {
      lastValidTime = player.currentTime() || 0
    }
  }

  const onSeeking = () => {
    if (isSeekingBlocked) return
    const current = player.currentTime() || 0
    const diff = current - lastValidTime
    // 跳跃超过容差（仅正向快进视为作弊；回退允许，方便重听）
    if (diff > threshold) {
      isSeekingBlocked = true
      player.currentTime(lastValidTime)
      opts.onBlocked?.(current, lastValidTime)
      // 下一帧解除锁，允许后续正常 seeking
      setTimeout(() => { isSeekingBlocked = false }, 0)
    } else {
      lastValidTime = current
    }
  }

  player.on('timeupdate', onTimeUpdate)
  player.on('seeking', onSeeking)

  return () => {
    player.off('timeupdate', onTimeUpdate)
    player.off('seeking', onSeeking)
  }
}
