/**
 * ECharts 学习趋势图 option 工厂
 *
 * 单一事实源：首页 / 统计页的趋势图 option 都从这里生成。
 *
 * 数据优先级（论文级要求：真实数据优先，无数据 graceful fallback）：
 *   1. stats.trendData —— 后端返回的真实趋势（数组 of 数字 或 of {date,value}）
 *   2. mock 占位 —— 7 天假数据，避免白屏
 *
 * @param {Object} stats 后端 /stats/my 的 data 对象
 * @param {Object} [opts] 覆盖项
 * @param {string} [opts.unit='分钟'] y 轴单位
 * @param {string} [opts.color='#1677ff'] 主题色
 * @returns {Object} ECharts option
 */
export function toChartOption(stats, opts = {}) {
  const unit = opts.unit || '分钟'
  const color = opts.color || '#1677ff'

  // 默认 x 轴：近 7 天
  const defaultXData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const defaultSeries = [30, 45, 60, 20, 80, 55, 70]

  let xData = defaultXData
  let seriesData = defaultSeries
  let isMock = true

  if (stats && Array.isArray(stats.trendData) && stats.trendData.length > 0) {
    isMock = false
    const raw = stats.trendData
    // 数组 of {date, value}
    if (raw.length > 0 && typeof raw[0] === 'object' && raw[0] !== null) {
      xData = raw.map((it) => it.date ?? it.name ?? it.label ?? '')
      seriesData = raw.map((it) => Number(it.value ?? it.count ?? it.minutes ?? 0) || 0)
    } else {
      // 数组 of 数字（如后端 recent7Days）
      seriesData = raw.map((v) => (isNaN(Number(v)) ? 0 : Number(v)))
      // 按实际数据点生成"近 N 天"日期标签（以今天结尾），论文级真实感
      xData = seriesData.map((_, i) => {
        const d = new Date()
        d.setDate(d.getDate() - (seriesData.length - 1 - i))
        return `${d.getMonth() + 1}/${d.getDate()}`
      })
    }
  }

  return {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.name}<br/>学习时长：${p.value} ${unit}`
      },
    },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: xData, boundaryGap: false },
    yAxis: { type: 'value', name: `学习时长 (${unit})` },
    series: [
      {
        name: '学习时长',
        type: 'line',
        smooth: true,
        data: seriesData,
        areaStyle: { color: hexToRgba(color, 0.15) },
        lineStyle: { color },
        itemStyle: { color },
      },
    ],
    // 暴露给调用方判断是否 mock，便于 UI 可选标注
    __isMock: isMock,
  }
}

// 把 #rrggbb 转为 rgba(r,g,b,a)
function hexToRgba(hex, alpha) {
  const m = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  if (!m) return `rgba(22,119,255,${alpha})`
  const r = parseInt(m[1], 16)
  const g = parseInt(m[2], 16)
  const b = parseInt(m[3], 16)
  return `rgba(${r},${g},${b},${alpha})`
}
