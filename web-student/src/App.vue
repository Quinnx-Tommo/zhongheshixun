<template>
  <!-- login 等 meta.layout===false 的页面：全窗口裸渲染 -->
  <router-view v-if="!withLayout" />
  <!-- 其他页面：用 MainLayout 包裹（左侧导航 + 顶部 header + 退出登录） -->
  <MainLayout v-else>
    <router-view />
  </MainLayout>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const route = useRoute()
// 默认需要布局；仅当路由显式声明 meta.layout === false 时才裸渲染
const withLayout = computed(() => route.meta.layout !== false)
</script>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
