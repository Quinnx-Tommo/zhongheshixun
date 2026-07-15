<template>
  <div class="profile-page" v-loading="loading">
    <el-row :gutter="20">
      <!-- 左侧基本信息 -->
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="profile-page__card">
          <div class="profile-page__avatar">
            <el-avatar :size="80" class="profile-page__avatar-img">
              {{ userStore.realName.charAt(0) }}
            </el-avatar>
            <h2 class="profile-page__name">{{ profile.realName || userStore.realName }}</h2>
            <el-tag>{{ roleDisplay }}</el-tag>
          </div>
          <el-divider />
          <div class="profile-page__info">
            <div class="info-row">
              <span class="info-row__label">用户名</span>
              <span class="info-row__value">{{ profile.username || userStore.userInfo?.username || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-row__label">所属机构</span>
              <span class="info-row__value">{{ profile.orgName || userStore.userInfo?.orgName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-row__label">注册时间</span>
              <span class="info-row__value">{{ formatTime(profile.createTime) || userInfoCreateTime }}</span>
            </div>
            <div class="info-row">
              <span class="info-row__label">手机号</span>
              <span class="info-row__value">{{ profile.phone || userStore.userInfo?.phone || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-row__label">邮箱</span>
              <span class="info-row__value">{{ profile.email || userStore.userInfo?.email || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧编辑 + 统计 -->
      <el-col :xs="24" :md="16">
        <!-- 编辑表单 -->
        <el-card shadow="never" class="profile-page__card">
          <template #header>
            <span class="profile-page__section-title">编辑个人信息</span>
          </template>
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="80px"
          >
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">
                保存修改
              </el-button>
              <el-button :disabled="saving" @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 学习统计 -->
        <el-card shadow="never" class="profile-page__card">
          <template #header>
            <span class="profile-page__section-title">学习统计</span>
          </template>
          <el-row :gutter="20">
            <el-col :xs="12" :sm="6">
              <div class="stat-item">
                <div class="stat-item__value">{{ stats.enrolledCount }}</div>
                <div class="stat-item__label">已报名课程</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6">
              <div class="stat-item">
                <div class="stat-item__value">{{ stats.completedCount }}</div>
                <div class="stat-item__label">已完成章节</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6">
              <div class="stat-item">
                <div class="stat-item__value">{{ stats.totalHours }}</div>
                <div class="stat-item__label">总学习时长 (h)</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6">
              <div class="stat-item">
                <div class="stat-item__value">{{ stats.examCount }}</div>
                <div class="stat-item__label">参加考试数</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile } from '@/api/user'
import { getMyStats } from '@/api/stats'
// P3-12 修复：从 utils/format 导入 safeNumber（与 home/index.vue 共享），避免重复定义
import { formatTime, safeNumber } from '@/utils/format'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const formRef = ref(null)
const profile = ref({})

// 后端 stats 字段 → 显示字段适配
const rawStats = ref({})
const stats = computed(() => ({
  enrolledCount: safeNumber(rawStats.value.enrollCount),
  completedCount: safeNumber(rawStats.value.completedChapters),
  totalHours: safeNumber(rawStats.value.totalStudyHours),
  examCount: safeNumber(rawStats.value.examCount),
}))

// 角色显示：后端 /user/profile 返回 role(roleId)，但前端原代码用 roleName
// 兼容两种：优先 profile.roleName，否则按 role/roleId 字典映射
const roleDisplay = computed(() => {
  if (profile.value?.roleName) return profile.value.roleName
  const r = profile.value?.role || userStore.userInfo?.role
  if (r === 'admin' || r === 'ADMIN') return '系统管理员'
  if (r === 'teacher' || r === 'TEACHER') return '培训讲师'
  if (r === 'student' || r === 'STUDENT') return '学员'
  return '学员'
})

// 兼容旧 userInfo 中的注册时间字段（某些早期版本可能含 createdAt）
const userInfoCreateTime = computed(() => {
  const ui = userStore.userInfo
  return formatTime(ui?.createTime) || formatTime(ui?.createdAt) || '-'
})

const form = reactive({
  phone: '',
  email: '',
})

const rules = {
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
}

async function fetchProfile() {
  loading.value = true
  try {
    profile.value = await getProfile()
    form.phone = profile.value.phone || ''
    form.email = profile.value.email || ''
    // 同步更新 userStore，让右上角用户名/头像即时刷新
    userStore.userInfo = { ...userStore.userInfo, ...profile.value }
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
  } catch (e) {
    ElMessage.warning('个人资料加载失败')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  form.phone = profile.value.phone || ''
  form.email = profile.value.email || ''
  formRef.value?.clearValidate()
}

async function fetchStats() {
  if (!userStore.userId) return
  try {
    // P2-11 修复：不再传 studentId，后端从 request attribute 取 userId
    rawStats.value = await getMyStats()
  } catch (e) {
    rawStats.value = {}
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateProfile({ phone: form.phone, email: form.email })
    ElMessage.success('保存成功')
    await fetchProfile()
  } catch (e) {
    ElMessage.warning('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchProfile()
  fetchStats()
})
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.profile-page__card {
  background: #fff;
  margin-bottom: 16px;
}
.profile-page__avatar {
  text-align: center;
  padding: 16px 0;
}
.profile-page__avatar-img {
  background-color: #1677ff;
  color: #fff;
  font-size: 32px;
  font-weight: bold;
}
.profile-page__name {
  margin: 12px 0 8px;
  font-size: 18px;
  color: #303133;
}
.profile-page__info {
  padding: 0 8px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}
.info-row:last-child {
  border-bottom: none;
}
.info-row__label {
  color: #909399;
}
.info-row__value {
  color: #303133;
}
.profile-page__section-title {
  font-size: 16px;
  font-weight: 500;
}
.stat-item {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
}
.stat-item__value {
  font-size: 24px;
  font-weight: bold;
  color: #1677ff;
}
.stat-item__label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>
