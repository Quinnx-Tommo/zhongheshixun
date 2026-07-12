<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <el-card shadow="never" class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2 class="welcome-title">欢迎回来，{{ welcomeName }}</h2>
          <p class="welcome-subtitle">{{ welcomeSubtitle }}</p>
        </div>
        <div class="welcome-extra">
          <el-tag size="large" type="info" effect="plain">后台管理</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 核心指标卡片 -->
    <el-row :gutter="16" class="card-row">
      <el-col :xs="12" :sm="8" :md="4" v-for="card in overviewCards" :key="card.label">
        <el-card shadow="hover" class="metric-card" @click="handleMetricClick(card.key)">
          <div class="metric-label">{{ card.label }}</div>
          <div class="metric-value" :style="{ color: card.color }">{{ card.value }}</div>
          <div class="metric-icon" :style="{ background: card.bg }">
            <el-icon :size="20" :color="card.color"><component :is="card.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图 + 快捷入口 -->
    <el-row :gutter="16" class="row">
      <el-col :md="16">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>近 30 天学习趋势</span>
              <el-radio-group v-model="granularity" size="small" @input="loadTrend">
                <el-radio-button label="day">按天</el-radio-button>
                <el-radio-button label="week">按周</el-radio-button>
                <el-radio-button label="month">按月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <v-chart class="chart" :option="trendOption" autoresize />
        </el-card>
      </el-col>
      <el-col :md="8">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-header"><span>快捷入口</span></div>
          </template>
          <div class="quick-entry">
            <el-button
              v-for="entry in quickEntries"
              :key="entry.route"
              class="quick-btn"
              :type="entry.type"
              plain
              @click="router.push(entry.route)"
            >
              <el-icon><component :is="entry.icon" /></el-icon>
              <span>{{ entry.label }}</span>
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待回复咨询 -->
    <el-row :gutter="16" class="row">
      <el-col :span="24">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="panel-header">
              <span>待回复咨询</span>
              <el-button type="primary" link @click="router.push('/consult')">查看全部</el-button>
            </div>
          </template>
          <el-table v-loading="consultLoading" :data="pendingConsults" stripe border>
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="question" label="问题" min-width="300" show-overflow-tooltip />
            <el-table-column prop="studentId" label="学员ID" width="100" align="center" />
            <el-table-column prop="createTime" label="提问时间" width="180" align="center" />
            <el-table-column label="等待时长" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="waitTagType(row)" size="small">{{ waitTimeText(row) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty
            v-if="!consultLoading && pendingConsults.length === 0"
            description="暂无待回复咨询"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import { getOverview, getTrend } from '@/api/stats'
import { getConsultPage } from '@/api/consult'
import { useUserStore } from '@/store/user'

// 按需注册 ECharts 组件
use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const router = useRouter()
const userStore = useUserStore()

// 欢迎信息
const welcomeName = computed(() => {
  const info = userStore.userInfo
  return info?.realName || info?.username || '管理员'
})
const welcomeSubtitle = computed(() => {
  const hour = new Date().getHours()
  const period = hour < 12 ? '上午好' : hour < 18 ? '下午好' : '晚上好'
  return `${period}，以下是今日平台运营数据概览`
})

// 指标卡片
const overview = ref<any>({})
const overviewCards = computed(() => [
  { key: 'students', label: '学员总数', value: overview.value.totalStudents ?? 0, color: '#1677ff', bg: '#e8f3ff', icon: 'User' },
  { key: 'courses', label: '课程总数', value: overview.value.totalCourses ?? 0, color: '#52c41a', bg: '#f0fae5', icon: 'Reading' },
  { key: 'enrollments', label: '报名人次', value: overview.value.totalEnrollments ?? 0, color: '#722ed1', bg: '#f3e8ff', icon: 'Tickets' },
  { key: 'studyHours', label: '累计学习(h)', value: formatHours(overview.value.totalStudyHours), color: '#fa8c16', bg: '#fff4e6', icon: 'Clock' },
  { key: 'exams', label: '考试场次', value: overview.value.totalExamRecords ?? 0, color: '#eb2f96', bg: '#ffe8f0', icon: 'DocumentChecked' },
  { key: 'active', label: '今日活跃', value: overview.value.todayActiveStudents ?? 0, color: '#13c2c2', bg: '#e6fffb', icon: 'Odometer' }
])

function formatHours(v: any) {
  const n = Number(v || 0)
  return n >= 1000 ? (n / 1000).toFixed(1) + 'k' : n.toFixed(0)
}

function handleMetricClick(key: string) {
  if (key === 'students' || key === 'courses' || key === 'exams') {
    router.push('/stats')
  } else if (key === 'active' || key === 'studyHours' || key === 'enrollments') {
    router.push('/stats')
  }
}

// 趋势图
const granularity = ref('day')
const trendPoints = ref<any[]>([])
const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['学习时长(h)', '活跃学员', '新增报名'] },
  grid: { left: 40, right: 20, top: 30, bottom: 30 },
  xAxis: {
    type: 'category',
    data: trendPoints.value.map((p: any) => p.label),
    boundaryGap: false
  },
  yAxis: { type: 'value' },
  series: [
    {
      name: '学习时长(h)',
      type: 'line',
      smooth: true,
      data: trendPoints.value.map((p: any) => p.studyHours),
      itemStyle: { color: '#1677ff' },
      areaStyle: { color: 'rgba(23,119,255,0.1)' }
    },
    {
      name: '活跃学员',
      type: 'line',
      smooth: true,
      data: trendPoints.value.map((p: any) => p.activeStudents),
      itemStyle: { color: '#52c41a' }
    },
    {
      name: '新增报名',
      type: 'line',
      smooth: true,
      data: trendPoints.value.map((p: any) => p.newEnrollments),
      itemStyle: { color: '#fa8c16' }
    }
  ]
}))

// 快捷入口
const quickEntries = [
  { label: '课程管理', route: '/courses', icon: 'Reading', type: 'primary' as const },
  { label: '统计报表', route: '/stats', icon: 'DataAnalysis', type: 'success' as const },
  { label: '咨询管理', route: '/consult', icon: 'ChatDotRound', type: 'warning' as const }
]

// 待回复咨询
const consultLoading = ref(false)
const pendingConsults = ref<any[]>([])
const consultQuery = reactive({ pageNum: 1, pageSize: 5, isAuto: 0 })

function waitTimeText(row: any) {
  if (!row.createTime) return '-'
  const diff = Date.now() - new Date(row.createTime).getTime()
  const hours = Math.floor(diff / 3600000)
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时`
  return `${Math.floor(hours / 24)}天`
}
function waitTagType(row: any) {
  if (!row.createTime) return 'info'
  const hours = (Date.now() - new Date(row.createTime).getTime()) / 3600000
  if (hours > 24) return 'danger'
  if (hours > 12) return 'warning'
  return 'info'
}

async function loadOverview() {
  const res: any = await getOverview()
  overview.value = res.data || {}
}

async function loadTrend() {
  const res: any = await getTrend({ granularity: granularity.value, recentDays: 30 })
  trendPoints.value = res.data?.points || []
}

async function loadPendingConsults() {
  consultLoading.value = true
  try {
    const res: any = await getConsultPage(consultQuery)
    pendingConsults.value = res.data?.records || []
  } finally {
    consultLoading.value = false
  }
}

onMounted(() => {
  loadOverview()
  loadTrend()
  loadPendingConsults()
})
</script>

<style scoped>
.dashboard {
  padding: 16px;
}

.welcome-banner {
  margin-bottom: 16px;
  background: linear-gradient(90deg, #1677ff 0%, #4096ff 100%);
  color: #fff;
}
.welcome-banner :deep(.el-card__body) {
  padding: 20px 24px;
}
.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.welcome-title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #fff;
}
.welcome-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}
.welcome-extra :deep(.el-tag) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: none;
}

.card-row {
  margin-bottom: 16px;
}
.metric-card {
  position: relative;
  margin-bottom: 16px;
  cursor: pointer;
  overflow: hidden;
}
.metric-card:hover .metric-icon {
  transform: scale(1.1);
}
.metric-label {
  color: #999;
  font-size: 14px;
  margin-bottom: 8px;
}
.metric-value {
  font-size: 28px;
  font-weight: bold;
}
.metric-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}

.row {
  margin-bottom: 16px;
}
.panel-card {
  margin-bottom: 16px;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chart {
  height: 320px;
}

.quick-entry {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.quick-btn {
  height: 48px;
  justify-content: flex-start;
  gap: 8px;
  font-size: 15px;
}
</style>
