<template>
  <div class="stats-page">
    <!-- 数据概览卡片 -->
    <el-row :gutter="16" class="overview-row">
      <el-col :xs="12" :sm="8" :md="4" v-for="card in overviewCards" :key="card.label">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-label">{{ card.label }}</div>
          <div class="overview-value" :style="{ color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>考试统计（平均分 / 通过率）</span>
              <el-select
                v-model="examFilter"
                placeholder="全部考试"
                clearable
                style="width: 220px"
                @change="loadExamStats"
              >
                <el-option
                  v-for="e in examOptions"
                  :key="e.examId"
                  :label="e.examTitle"
                  :value="e.examId"
                />
              </el-select>
            </div>
          </template>
          <v-chart class="chart" :option="examBarOption" autoresize />
        </el-card>
      </el-col>
      <el-col :md="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>考试分数段分布</span>
            </div>
          </template>
          <v-chart class="chart" :option="scorePieOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :md="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>学习时间趋势</span>
              <el-radio-group v-model="granularity" size="small" @input="loadTrend">
                <el-radio-button label="day">按天</el-radio-button>
                <el-radio-button label="week">按周</el-radio-button>
                <el-radio-button label="month">按月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <v-chart class="chart" :option="trendLineOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据明细 tab -->
    <el-card shadow="never" class="table-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="学员学习统计" name="student">
          <el-form :inline="true" :model="studentQuery" @submit.prevent>
            <el-form-item label="关键字">
              <el-input v-model="studentQuery.keyword" placeholder="姓名/用户名" clearable />
            </el-form-item>
            <el-form-item label="机构">
              <el-input v-model="studentQuery.orgName" placeholder="机构名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadStudentStats">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="studentLoading" :data="studentRecords" stripe border>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="orgName" label="机构" min-width="160" show-overflow-tooltip />
            <el-table-column prop="jobType" label="岗位" width="100" />
            <el-table-column prop="enrollCount" label="报名数" width="100" align="center" />
            <el-table-column prop="totalStudyHours" label="学习时长(h)" width="120" align="center" />
            <el-table-column prop="completionRate" label="完成率(%)" width="110" align="center" />
            <el-table-column prop="examAvgScore" label="考试均分" width="110" align="center" />
          </el-table>
          <el-pagination
            v-model:current-page="studentQuery.pageNum"
            v-model:page-size="studentQuery.pageSize"
            :total="studentTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            class="pagination"
            @size-change="loadStudentStats"
            @current-change="loadStudentStats"
          />
        </el-tab-pane>

        <el-tab-pane label="课程热度统计" name="course">
          <el-form :inline="true" :model="courseQuery" @submit.prevent>
            <el-form-item label="课程标题">
              <el-input v-model="courseQuery.keyword" placeholder="课程标题" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadCourseStats">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="courseLoading" :data="courseRecords" stripe border>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="title" label="课程标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="teacherName" label="讲师" width="120" />
            <el-table-column prop="enrollCount" label="报名人数" width="110" align="center" />
            <el-table-column prop="studyCount" label="学习人数" width="110" align="center" />
            <el-table-column prop="avgProgress" label="平均进度(%)" width="120" align="center" />
            <el-table-column prop="completionRate" label="完课率(%)" width="110" align="center" />
          </el-table>
          <el-pagination
            v-model:current-page="courseQuery.pageNum"
            v-model:page-size="courseQuery.pageSize"
            :total="courseTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            class="pagination"
            @size-change="loadCourseStats"
            @current-change="loadCourseStats"
          />
        </el-tab-pane>

        <el-tab-pane label="机构维度统计" name="org">
          <el-table v-loading="orgLoading" :data="orgRecords" stripe border>
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="orgName" label="机构名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="studentCount" label="学员数" width="110" align="center" />
            <el-table-column prop="totalStudyHours" label="总学习时长(h)" width="140" align="center" />
            <el-table-column prop="avgStudyHours" label="人均时长(h)" width="130" align="center" />
            <el-table-column prop="examPassRate" label="考试通过率(%)" width="140" align="center" />
          </el-table>
          <el-pagination
            v-model:current-page="orgQuery.pageNum"
            v-model:page-size="orgQuery.pageSize"
            :total="orgTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            class="pagination"
            @size-change="loadOrgStats"
            @current-change="loadOrgStats"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
} from 'echarts/components'
import {
  getOverview,
  getStudentStats,
  getExamStats,
  getCourseStats,
  getOrgStats,
  getTrend
} from '@/api/stats'

// 按需注册 ECharts 组件（减小打包体积）
use([
  CanvasRenderer,
  BarChart,
  PieChart,
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent
])

// 数据概览
const overview = ref<any>({})
const overviewCards = computed(() => [
  { label: '学员总数', value: overview.value.totalStudents ?? 0, color: '#1677ff' },
  { label: '课程总数', value: overview.value.totalCourses ?? 0, color: '#52c41a' },
  { label: '报名人次', value: overview.value.totalEnrollments ?? 0, color: '#722ed1' },
  { label: '累计学习时长(h)', value: overview.value.totalStudyHours ?? 0, color: '#fa8c16' },
  { label: '考试场次', value: overview.value.totalExamRecords ?? 0, color: '#eb2f96' },
  { label: '今日活跃学员', value: overview.value.todayActiveStudents ?? 0, color: '#13c2c2' }
])

// 考试统计
const examFilter = ref<number | ''>('')
const examOptions = ref<any[]>([])
const examStats = ref<any[]>([])
const examBarOption = computed(() => {
  const list = examStats.value
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均分', '通过率(%)'] },
    xAxis: { type: 'category', data: list.map((e: any) => e.examTitle), axisLabel: { interval: 0, rotate: 20 } },
    yAxis: [
      { type: 'value', name: '分', max: 100 },
      { type: 'value', name: '%', max: 100 }
    ],
    series: [
      {
        name: '平均分',
        type: 'bar',
        data: list.map((e: any) => Number(e.avgScore?.toFixed(1))),
        itemStyle: { color: '#1677ff' }
      },
      {
        name: '通过率(%)',
        type: 'line',
        yAxisIndex: 1,
        data: list.map((e: any) => Number(e.passRate?.toFixed(1))),
        itemStyle: { color: '#52c41a' }
      }
    ]
  }
})

const scorePieOption = computed(() => {
  // 汇总所有考试分数段分布
  const total: Record<string, number> = {}
  for (const e of examStats.value) {
    for (const r of e.scoreRanges || []) {
      total[r.range] = (total[r.range] || 0) + r.count
    }
  }
  const data = Object.entries(total).map(([name, value]) => ({ name, value }))
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        name: '分数段分布',
        type: 'pie',
        radius: ['40%', '70%'],
        data,
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' }
        }
      }
    ]
  }
})

// 时间趋势
const granularity = ref('day')
const trendPoints = ref<any[]>([])
const trendLineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['学习时长(h)', '活跃学员', '新增报名'] },
  xAxis: { type: 'category', data: trendPoints.value.map((p: any) => p.label), boundaryGap: false },
  yAxis: { type: 'value' },
  series: [
    { name: '学习时长(h)', type: 'line', smooth: true, data: trendPoints.value.map((p: any) => p.studyHours), itemStyle: { color: '#1677ff' } },
    { name: '活跃学员', type: 'line', smooth: true, data: trendPoints.value.map((p: any) => p.activeStudents), itemStyle: { color: '#52c41a' } },
    { name: '新增报名', type: 'line', smooth: true, data: trendPoints.value.map((p: any) => p.newEnrollments), itemStyle: { color: '#fa8c16' } }
  ]
}))

// 学员统计
const studentLoading = ref(false)
const studentRecords = ref<any[]>([])
const studentTotal = ref(0)
const studentQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', orgName: '' })

// 课程统计
const courseLoading = ref(false)
const courseRecords = ref<any[]>([])
const courseTotal = ref(0)
const courseQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

// 机构统计
const orgLoading = ref(false)
const orgRecords = ref<any[]>([])
const orgTotal = ref(0)
const orgQuery = reactive({ pageNum: 1, pageSize: 20 })

const activeTab = ref('student')

async function loadOverview() {
  const res: any = await getOverview()
  overview.value = res.data || res
}

async function loadExamStats() {
  const res: any = await getExamStats(examFilter.value || undefined)
  const list = (res.data || res) || []
  examStats.value = list
  // 构建考试下拉选项（仅在首次）
  if (examOptions.value.length === 0) {
    examOptions.value = list.map((e: any) => ({ examId: e.examId, examTitle: e.examTitle }))
  }
}

async function loadTrend() {
  const res: any = await getTrend({ granularity: granularity.value, recentDays: 30 })
  trendPoints.value = (res.data?.points) || []
}

async function loadStudentStats() {
  studentLoading.value = true
  try {
    const params: any = {
      pageNum: studentQuery.pageNum,
      pageSize: studentQuery.pageSize
    }
    if (studentQuery.keyword) params.keyword = studentQuery.keyword
    if (studentQuery.orgName) params.orgName = studentQuery.orgName
    const res: any = await getStudentStats(params)
    studentRecords.value = res.data?.records || []
    studentTotal.value = res.data?.total || 0
  } finally {
    studentLoading.value = false
  }
}

async function loadCourseStats() {
  courseLoading.value = true
  try {
    const params: any = { pageNum: courseQuery.pageNum, pageSize: courseQuery.pageSize }
    if (courseQuery.keyword) params.keyword = courseQuery.keyword
    const res: any = await getCourseStats(params)
    courseRecords.value = res.data?.records || []
    courseTotal.value = res.data?.total || 0
  } finally {
    courseLoading.value = false
  }
}

async function loadOrgStats() {
  orgLoading.value = true
  try {
    const res: any = await getOrgStats({ pageNum: orgQuery.pageNum, pageSize: orgQuery.pageSize })
    orgRecords.value = res.data?.records || []
    orgTotal.value = res.data?.total || 0
  } finally {
    orgLoading.value = false
  }
}

onMounted(() => {
  loadOverview()
  loadExamStats()
  loadTrend()
  loadStudentStats()
})
</script>

<style scoped>
.stats-page {
  padding: 16px;
}

.overview-row {
  margin-bottom: 16px;
}
.overview-card {
  margin-bottom: 16px;
  text-align: center;
}
.overview-label {
  color: #999;
  font-size: 14px;
  margin-bottom: 8px;
}
.overview-value {
  font-size: 28px;
  font-weight: bold;
}

.chart-row {
  margin-bottom: 16px;
}
.chart-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chart {
  height: 320px;
}

.table-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
