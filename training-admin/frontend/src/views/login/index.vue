<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">{{ title }}</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="btn-login" @click="onLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/api/request'

const title = import.meta.env.VITE_APP_TITLE || '基层卫生培训平台'
const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await request({ url: '/admin/login', method: 'POST', data: form })
    userStore.login({ token: res.data.token, userInfo: res.data.userInfo })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // 错误信息已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%; height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
}
.login-card { width: 380px; padding: 20px; }
.title { text-align: center; margin-bottom: 24px; color: #1677ff; }
.btn-login { width: 100%; }
</style>
