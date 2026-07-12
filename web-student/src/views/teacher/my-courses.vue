<template>
  <div class="my-courses-page">
    <el-card shadow="never" class="my-courses-page__header">
      <div class="my-courses-page__header-text">
        <h2>我的课程</h2>
        <p class="my-courses-page__subtitle">
          登录身份：<el-tag size="small" type="warning">{{ userStore.realName }}</el-tag>
          <span style="margin-left: 8px">讲师工作台</span>
        </p>
      </div>
    </el-card>

    <!-- 教学概览统计（基于学员端 API，演示教师视角的"我的教学"） -->
    <el-row :gutter="16" class="my-courses-page__stats" v-if="!loadingStats">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card stat-card--primary">
          <div class="stat-card__label">已报名课程</div>
          <div class="stat-card__value">{{ stats.enrolledCount }}</div>
          <div class="stat-card__hint">学员端 /api/study/my-courses</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card stat-card--success">
          <div class="stat-card__label">已完成课程</div>
          <div class="stat-card__value">{{ stats.completedCount }}</div>
          <div class="stat-card__hint">已 100% 学完的课程</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card stat-card--warning">
          <div class="stat-card__label">学习中</div>
          <div class="stat-card__value">{{ stats.inProgressCount }}</div>
          <div class="stat-card__hint">已报名但未完成</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="my-courses-page__list">
      <template #header>
        <div class="my-courses-page__list-header">
          <span><el-icon><Reading /></el-icon> 课程列表（{{ courseList.length }}）</span>
          <el-button text type="primary" @click="fetchData" :loading="loading">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && courseList.length === 0" description="暂无已报名课程">
          <el-button type="primary" @click="goCourseCenter">前往课程中心</el-button>
        </el-empty>

        <el-table v-else :data="courseList" stripe>
          <el-table-column label="课程名称" min-width="200">
            <template #default="{ row }">
              <a class="link" @click.prevent="goDetail(row)">{{ row.title || row.courseName || '-' }}</a>
            </template>
          </el-table-column>
          <el-table-column label="课程类型" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="typeColor(row.courseType)">
                {{ typeText(row.courseType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="难度" width="90">
            <template #default="{ row }">
              <span v-if="row.difficulty">{{ difficultyText(row.difficulty) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="学时" width="80">
            <template #default="{ row }">
              <span v-if="row.totalHours">{{ row.totalHours }}h</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="进度" width="180">
            <template #default="{ row }">
              <el-progress
                :percentage="row.progress ?? 0"
                :stroke-width="10"
                :text-inside="true"
              />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="(row.progress ?? 0) >= 100" type="success" size="small">已完成</el-tag>
              <el-tag v-else-if="(row.progress ?? 0) > 0" type="warning" size="small">学习中</el-tag>
              <el-tag v-else type="info" size="small">未开始</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="goDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-card shadow="never" class="my-courses-page__tip">
      <el-alert
        type="info"
        :closable="false"
        title="教学事务的完整功能（课程创建、章节编辑、查看学员）在管理后台（5176）提供。"
      >
        <template #default>
          <div style="margin-top: 8px">
            <el-button type="primary" size="small" @click="goAdmin">前往管理后台（5176）</el-button>
            <el-button size="small" @click="goHome">返回学员首页</el-button>
          </div>
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMyCourses } from '@/api/study'
import { typeText, typeColor, difficultyText } from '@/utils/dict'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const loadingStats = ref(false)
const courseList = ref([])
const stats = ref({
  enrolledCount: 0,
  completedCount: 0,
  inProgressCount: 0,
})

async function fetchData() {
  loading.value = true
  loadingStats.value = true
  try {
    // 教师工作台 MVP：用学员端 /api/study/my-courses 拉取已报名课程，
    // 展示教师自己的学习与教学情况（演示教师视角数据）
    const res = await getMyCourses({ pageNum: 1, pageSize: 50 })
    const list = res?.records || res?.list || res || []
    courseList.value = Array.isArray(list) ? list : []

    // 统计
    const enrolled = courseList.value.length
    const completed = courseList.value.filter((c) => (c.progress ?? 0) >= 100).length
    const inProgress = enrolled - completed
    stats.value = { enrolledCount: enrolled, completedCount: completed, inProgressCount: inProgress }
  } catch (e) {
    // 失败时回退到空列表，不阻塞页面
    ElMessage.warning('课程数据加载失败，已为您展示空列表')
    courseList.value = []
    stats.value = { enrolledCount: 0, completedCount: 0, inProgressCount: 0 }
  } finally {
    loading.value = false
    loadingStats.value = false
  }
}

function goDetail(row) {
  if (row?.id) {
    router.push(`/courses/${row.id}`)
  }
}

function goCourseCenter() {
  router.push('/courses')
}

function goAdmin() {
  window.open('http://localhost:5176/#/course', '_blank')
}

function goHome() {
  router.push('/home')
}

onMounted(fetchData)
</script>

<style scoped>
.my-courses-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.my-courses-page__header {
  background: linear-gradient(90deg, #fff7e6 0%, #ffffff 100%);
}
.my-courses-page__header h2 {
  margin: 0 0 4px;
  font-size: 18px;
  color: #303133;
}
.my-courses-page__subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
.my-courses-page__stats {
  margin: 0;
}
.stat-card {
  text-align: center;
  border-left: 4px solid #1677ff;
}
.stat-card--success {
  border-left-color: #67c23a;
}
.stat-card--warning {
  border-left-color: #e6a23c;
}
.stat-card__label {
  font-size: 13px;
  color: #909399;
}
.stat-card__value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 4px 0;
}
.stat-card__hint {
  font-size: 12px;
  color: #c0c4cc;
}
.my-courses-page__list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.link {
  color: #1677ff;
  cursor: pointer;
}
.link:hover {
  text-decoration: underline;
}
</style>
