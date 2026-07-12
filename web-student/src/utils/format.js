/**
 * 时间格式化
 *
 * 后端时间字段可能是 ISO 字符串、带 T 的 UTC 字符串或时间戳，统一转为
 * 中文本地时间显示。空值显示占位符，避免 "Invalid Date" 出现在页面上。
 */
export function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  if (isNaN(d.getTime())) return '-'
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * 数字安全格式化：NaN / null / undefined 统一显示占位符
 */
export function safeNumber(v) {
  if (v === null || v === undefined || v === '') return '-'
  const n = Number(v)
  return isNaN(n) ? '-' : n
}
