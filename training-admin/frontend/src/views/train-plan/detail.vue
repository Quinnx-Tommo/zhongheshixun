<template>
  <div class="train-plan-detail-page">
    <el-page-header @back="goBack" content="计划详情" />

    <!-- 计划基本信息 -->
    <el-card shadow="never" class="info-card" v-loading="loading">
      <template #header>
        <span class="card-title">基本信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="计划ID">{{ planInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(planInfo.status)">{{ statusText(planInfo.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ planInfo.title }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ planInfo.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ planInfo.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 已关联课程 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">已关联课程</span>
          <el-button type="primary" @click="openLinkDialog">
            <el-icon><Plus /></el-icon>关联课程
          </el-button>
        </div>
      </template>

      <el-table v-loading="coursesLoading" :data="courseList" stripe border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column prop="isRequired" label="是否必修" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRequired ? 'danger' : 'info'">
              {{ row.isRequired ? '必修' : '选修' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleRemove(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="courseQuery.pageNum"
        v-model:page-size="courseQuery.pageSize"
        :total="courseTotal"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        class="pagination"
        @size-change="fetchCourses"
        @current-change="fetchCourses"
      />
    </el-card>

    <!-- 关联课程弹窗 -->
    <el-dialog
      v-model="linkDialogVisible"
      title="关联课程"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-input
        v-model="courseKeyword"
        placeholder="搜索课程标题"
        clearable
        style="margin-bottom: 16px"
        @input="filterCourseOptions"
      />
      <el-checkbox-group v-model="selectedCourseIds">
        <div
          v-for="item in filteredCourseOptions"
          :key="item.id"
          class="course-option"
        >
          <el-checkbox :value="item.id">{{ item.title }}</el-checkbox>
        </div>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="linkDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleLinkCourses">确定关联</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCoursePage } from '@/api/course'
import {
  getTrainPlanDetail,
  addPlanCourses,
  removePlanCourse
} from '@/api/train-plan'

const route = useRoute()
const router = useRouter()

const planId = Number(route.params.id)

const loading = ref(false)
const coursesLoading = ref(false)
const submitting = ref(false)

const planInfo = reactive({
  id: 0,
  title: '',
  description: '',
  status: 0,
  createTime: ''
})

const courseList = ref<any[]>([])
const courseTotal = ref(0)
const courseQuery = reactive({
  pageNum: 1,
  pageSize: 10
})

const linkDialogVisible = ref(false)
const courseKeyword = ref('')
const courseOptions = ref<any[]>([])
const filteredCourseOptions = ref<any[]>([])
const selectedCourseIds = ref<number[]>([])

const statusText = (status: number) => {
  const map: Record<number, string> = { 0: '草稿', 1: '已发布', 2: '已结束' }
  return map[status] || '-'
}

const statusTag = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || ''
}

function goBack() {
  router.back()
}

async function fetchPlanDetail() {
  loading.value = true
  try {
    const res: any = await getTrainPlanDetail(planId)
    const data = res.data || res
    Object.assign(planInfo, data.plan || data)
  } catch (e) {
    // 错误已在 request 拦截器处理
  } finally {
    loading.value = false
  }
}

async function fetchCourses() {
  coursesLoading.value = true
  try {
    const res: any = await getTrainPlanDetail(planId)
    const data = res.data || res
    const allCourses: any[] = data.courses || []
    // 前端分页
    const start = (courseQuery.pageNum - 1) * courseQuery.pageSize
    courseList.value = allCourses.slice(start, start + courseQuery.pageSize)
    courseTotal.value = allCourses.length
  } catch (e) {
    // 错误已在 request 拦截器处理
  } finally {
    coursesLoading.value = false
  }
}

async function openLinkDialog() {
  try {
    const res: any = await getCoursePage({ pageNum: 1, pageSize: 200 })
    const data = res.data || res
    courseOptions.value = data.records || []
    filteredCourseOptions.value = courseOptions.value
    selectedCourseIds.value = []
    courseKeyword.value = ''
    linkDialogVisible.value = true
  } catch (e) {
    // 错误已在 request 拦截器处理
  }
}

function filterCourseOptions() {
  const kw = courseKeyword.value.trim().toLowerCase()
  if (!kw) {
    filteredCourseOptions.value = courseOptions.value
  } else {
    filteredCourseOptions.value = courseOptions.value.filter((c: any) =>
      (c.title || '').toLowerCase().includes(kw)
    )
  }
}

async function handleLinkCourses() {
  if (selectedCourseIds.value.length === 0) {
    ElMessage.warning('请至少选择一个课程')
    return
  }
  submitting.value = true
  try {
    await addPlanCourses({ planId, courseIds: selectedCourseIds.value })
    ElMessage.success('关联成功')
    linkDialogVisible.value = false
    fetchCourses()
  } catch (e) {
    // 错误已在 request 拦截器处理
  } finally {
    submitting.value = false
  }
}

async function handleRemove(row: any) {
  await ElMessageBox.confirm(
    `确定移除课程《${row.courseName}》？`,
    '移除确认',
    {
      confirmButtonText: '确定移除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  try {
    await removePlanCourse(row.id)
    ElMessage.success('移除成功')
    fetchCourses()
  } catch (e) {
    // 错误已在 request 拦截器处理
  }
}

onMounted(() => {
  if (!planId) {
    ElMessage.error('缺少计划ID')
    goBack()
    return
  }
  fetchPlanDetail()
  fetchCourses()
})
</script>

<style scoped>
.train-plan-detail-page {
  padding: 16px;
}

.info-card {
  margin: 16px 0;
}

.table-card {
  margin-bottom: 16px;
}

.card-title {
  font-weight: bold;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.course-option {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.course-option:last-child {
  border-bottom: none;
}
</style>
