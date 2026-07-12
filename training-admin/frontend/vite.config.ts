import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-vue-components/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',
    port: 5176,  // 5174 已分配给 web-student，管理后台用 5176 避免冲突
    open: false,
    proxy: {
      '/admin': {
        target: 'http://localhost:9898',
        changeOrigin: true
      },
      '/upload': {
        target: 'http://localhost:9898',
        changeOrigin: true
      }
    }
  }
})
