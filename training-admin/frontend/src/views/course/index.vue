<template>
  <div class="course-page">
    <!-- 筛选区 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item label="课程标题">
          <el-input
            v-model="queryForm.title"
            placeholder="请输入课程标题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select
            v-model="queryForm.courseType"
            placeholder="全部"
            clearable
            style="width: 140px"
          >
            <el-option label="公开课" :value="1" />
            <el-option label="必修课" :value="2" />
            <el-option label="选修课" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作区 -->
    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>新增课程
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="courseList" stripe border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="课程标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="courseType" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="courseTypeTag(row.courseType)">
              {{ courseTypeText(row.courseType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalHours" label="学时" width="100" align="center" />
        <el-table-column prop="teacherName" label="讲师" width="120" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status !== 1"
              type="success"
              link
              @click="handlePublish(row, 1)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              @click="handlePublish(row, 2)"
            >
              下架
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        class="pagination"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </el-card>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增课程' : '编辑课程'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="课程标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入课程标题" maxlength="100" />
        </el-form-item>
        <el-form-item label="课程类型" prop="courseType">
          <el-select v-model="formData.courseType" placeholder="请选择课程类型" style="width: 100%">
            <el-option label="公开课" :value="1" />
            <el-option label="必修课" :value="2" />
            <el-option label="选修课" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="学时" prop="totalHours">
          <el-input-number v-model="formData.totalHours" :min="1" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getCoursePage,
  createCourse,
  updateCourse,
  publishCourse,
  deleteCourse
} from '@/api/course'

const loading = ref(false)
const submitting = ref(false)
const courseList = ref<any[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  courseType: undefined as number | undefined,
  status: undefined as number | undefined
})

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const formData = reactive({
  id: undefined as number | undefined,
  title: '',
  courseType: undefined as number | undefined,
  totalHours: 1,
  description: ''
})

const formRules: FormRules = {
  title: [
    { required: true, message: '请输入课程标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2-100 字', trigger: 'blur' }
  ],
  courseType: [
    { required: true, message: '请选择课程类型', trigger: 'change' }
  ],
  totalHours: [
    { required: true, message: '请输入学时', trigger: 'blur' }
  ]
}

const courseTypeText = (type: number) => {
  const map: Record<number, string> = { 1: '公开课', 2: '必修课', 3: '选修课' }
  return map[type] || '-'
}

const courseTypeTag = (type: number) => {
  const map: Record<number, string> = { 1: 'primary', 2: 'danger', 3: 'warning' }
  return map[type] || ''
}

const statusText = (status: number) => {
  const map: Record<number, string> = { 0: '草稿', 1: '已发布', 2: '已下架' }
  return map[status] || '-'
}

const statusTag = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || ''
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = {
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    }
    if (queryForm.title) params.title = queryForm.title
    if (queryForm.courseType !== undefined) params.courseType = queryForm.courseType
    if (queryForm.status !== undefined) params.status = queryForm.status

    const res: any = await getCoursePage(params)
    // 后端返回: { code: 200, data: { records, total } }
    if (res.data) {
      courseList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      courseList.value = res.records || []
      total.value = res.total || 0
    }
  } catch (e) {
    // 错误已在 request 拦截器处理
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryForm.pageNum = 1
  fetchList()
}

function handleReset() {
  queryForm.title = ''
  queryForm.courseType = undefined
  queryForm.status = undefined
  queryForm.pageNum = 1
  fetchList()
}

function resetForm() {
  formData.id = undefined
  formData.title = ''
  formData.courseType = undefined
  formData.totalHours = 1
  formData.description = ''
  formRef.value?.clearValidate()
}

function handleCreate() {
  resetForm()
  dialogMode.value = 'create'
  dialogVisible.value = true
}

function handleEdit(row: any) {
  resetForm()
  dialogMode.value = 'edit'
  formData.id = row.id
  formData.title = row.title
  formData.courseType = row.courseType
  formData.totalHours = row.totalHours || 1
  formData.description = row.description || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (dialogMode.value === 'create') {
        const { id, ...data } = formData
        await createCourse(data)
        ElMessage.success('新增成功')
      } else {
        await updateCourse(formData)
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      fetchList()
    } catch (e) {
      // 错误已在 request 拦截器处理
    } finally {
      submitting.value = false
    }
  })
}

async function handlePublish(row: any, status: number) {
  const actionText = status === 1 ? '发布' : '下架'
  try {
    await publishCourse({ id: row.id, status })
    ElMessage.success(`${actionText}成功`)
    fetchList()
  } catch (e) {
    // 错误已在 request 拦截器处理
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(
    `确定删除课程《${row.title}》？删除后不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  try {
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    // 错误已在 request 拦截器处理
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.course-page {
  padding: 16px;
}

.filter-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
