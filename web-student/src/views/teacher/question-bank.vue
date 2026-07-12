<template>
  <div class="question-bank-page">
    <el-card shadow="never" class="question-bank-page__header">
      <div class="question-bank-page__header-text">
        <h2>题库管理</h2>
        <p class="question-bank-page__subtitle">
          登录身份：<el-tag size="small" type="warning">{{ userStore.realName }}</el-tag>
          <span style="margin-left: 8px">讲师工作台</span>
        </p>
      </div>
    </el-card>

    <!-- 题库使用情况（基于学员端 API，演示教师视角的"题库相关"） -->
    <el-row :gutter="16" class="question-bank-page__stats" v-if="!loadingStats">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card stat-card--primary">
          <div class="stat-card__label">考试记录总数</div>
          <div class="stat-card__value">{{ stats.total }}</div>
          <div class="stat-card__hint">学员端 /api/exam/my-records</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card stat-card--success">
          <div class="stat-card__label">已通过</div>
          <div class="stat-card__value">{{ stats.passed }}</div>
          <div class="stat-card__hint">分数 ≥ 60 分</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card stat-card--warning">
          <div class="stat-card__label">待批阅</div>
          <div class="stat-card__value">{{ stats.pending }}</div>
          <div class="stat-card__hint">含主观题未出分</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="question-bank-page__list">
      <template #header>
        <div class="question-bank-page__list-header">
          <span><el-icon><Reading /></el-icon> 考试记录（{{ recordList.length }}）</span>
          <el-button text type="primary" @click="fetchData" :loading="loading">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && recordList.length === 0" description="暂无考试记录">
          <el-button type="primary" @click="goExamCenter">前往考试中心</el-button>
        </el-empty>

        <el-table v-else :data="recordList" stripe>
          <el-table-column label="考试名称" min-width="200">
            <template #default="{ row }">
              <span class="link" @click="goExamDetail(row)">{{ row.examTitle || row.title || `考试 #${row.examId || row.id}` }}</span>
            </template>
          </el-table-column>
          <el-table-column label="得分" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.score != null" :type="(row.score >= 60) ? 'success' : 'danger'" size="small">
                {{ row.score }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag v-if="row.status === 1" type="success" size="small">已通过</el-tag>
              <el-tag v-else-if="row.status === 2" type="danger" size="small">未通过</el-tag>
              <el-tag v-else-if="row.status === 0" type="warning" size="small">待批阅</el-tag>
              <el-tag v-else type="info" size="small">未知</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="180">
            <template #default="{ row }">
              <span v-if="row.submitTime">{{ formatTime(row.submitTime) }}</span>
              <span v-else-if="row.createTime">{{ formatTime(row.createTime) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="goExamDetail(row)">查看答卷</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-card shadow="never" class="question-bank-page__tip">
      <el-alert
        type="info"
        :closable="false"
        title="题库管理完整功能（题目增删改查、批量导入、组卷配置）在管理后台（5176）提供。"
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
import { getMyRecords } from '@/api/exam'
import { formatTime } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const loadingStats = ref(false)
const recordList = ref([])
const stats = ref({ total: 0, passed: 0, pending: 0 })

async function fetchData() {
  loading.value = true
  loadingStats.value = true
  try {
    // 教师工作台 MVP：用学员端 /api/exam/my-records 拉取考试记录，
    // 展示教师视角的"题库使用情况"（教师也参加培训，可作为数据源）
    const res = await getMyRecords({ pageNum: 1, pageSize: 50 })
    const list = res?.records || res?.list || res || []
    recordList.value = Array.isArray(list) ? list : []

    const total = recordList.value.length
    const passed = recordList.value.filter((r) => r.status === 1 || (r.score != null && r.score >= 60)).length
    const pending = recordList.value.filter((r) => r.status === 0 || r.score == null).length
    stats.value = { total, passed, pending }
  } catch (e) {
    ElMessage.warning('考试记录加载失败，已为您展示空列表')
    recordList.value = []
    stats.value = { total: 0, passed: 0, pending: 0 }
  } finally {
    loading.value = false
    loadingStats.value = false
  }
}

function goExamDetail(row) {
  // 跳到考试结果页（M10 联调：如果没有 examId 就不跳）
  if (row?.examId) {
    router.push(`/exams/${row.examId}/result`)
  } else if (row?.id) {
    router.push(`/exams/${row.id}/result`)
  }
}

function goExamCenter() {
  router.push('/exams')
}

function goAdmin() {
  window.open('http://localhost:5176/#/question', '_blank')
}

function goHome() {
  router.push('/home')
}

onMounted(fetchData)
</script>

<style scoped>
.question-bank-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.question-bank-page__header {
  background: linear-gradient(90deg, #fff7e6 0%, #ffffff 100%);
}
.question-bank-page__header h2 {
  margin: 0 0 4px;
  font-size: 18px;
  color: #303133;
}
.question-bank-page__subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
.question-bank-page__stats {
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
.question-bank-page__list-header {
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
