// miniprogram/api/config.js
module.exports = {
  // 小程序 API 地址（training-api 端口 9899）
  // ⚠️ 为避开 8081 僵尸死锁，已迁移至 9899（2026-07-09）；测试包/本地都用这个
  baseURL: 'http://localhost:9899/api',
  // 请求超时 10 秒（弱网环境）
  timeout: 10000,
  // 离线包本地存储目录名
  offlineDir: 'offline_courses'
}
