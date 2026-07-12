<template>
  <router-view />
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/user'
useUserStore()
</script>
