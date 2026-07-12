<template>
  <div class="chapter-page">
    <!-- 课程选择 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="所属课程" required>
          <el-select
            v-model="selectedCourseId"
            placeholder="请先选择课程"
            filterable
            style="width: 300px"
            @change="onCourseChange"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="章节标题">
          <el-input
            v-model="queryForm.title"
            placeholder="请输入章节标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :disabled="!selectedCourseId">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作区 -->
    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate" :disabled="!selectedCourseId">
          <el-icon><Plus /></el-icon>新增章节
        </el-button>
      </div>

      <el-table v-loading="loading" :data="chapterList" stripe border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="章节标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="videoUrl" label="视频链接" min-width="200" show-overflow-tooltip />
        <el-table-column prop="duration" label="时长" width="120" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column label="操作" width="320" fixed="right" align="center">
          <template #default="{ row, $index }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              type="info"
              link
              :disabled="$index === 0"
              @click="handleMoveUp($index)"
            >
              上移
            </el-button>
            <el-button
              type="info"
              link
              :disabled="$index === chapterList.length - 1"
              @click="handleMoveDown($index)"
            >
              下移
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
      :title="dialogMode === 'create' ? '新增章节' : '编辑章节'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="章节标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入章节标题" maxlength="100" />
        </el-form-item>
        <el-form-item label="视频链接" prop="videoUrl">
          <el-input v-model="formData.videoUrl" placeholder="请输入视频 URL" />
        </el-form-item>
        <el-form-item label="时长(秒)" prop="duration">
          <el-input-number v-model="formData.duration" :min="0" :max="99999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="1" :max="999" style="width: 100%" />
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
import { getCoursePage } from '@/api/course'
import {
  getChapterPage,
  createChapter,
  updateChapter,
  deleteChapter,
  sortChapter
} from '@/api/chapter'

const loading = ref(false)
const submitting = ref(false)
const chapterList = ref<any[]>([])
const total = ref(0)

const courseOptions = ref<any[]>([])
const selectedCourseId = ref<number | undefined>(undefined)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  title: ''
})

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const formData = reactive({
  id: undefined as number | undefined,
  courseId: undefined as number | undefined,
  title: '',
  videoUrl: '',
  duration: 0,
  sortOrder: 1
})

const formRules: FormRules = {
  title: [
    { required: true, message: '请输入章节标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2-100 字', trigger: 'blur' }
  ],
  videoUrl: [
    { required: true, message: '请输入视频链接', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入时长', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ]
}

const formatDuration = (seconds: number) => {
  if (!seconds) return '0秒'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  if (m === 0) return `${s}秒`
  if (s === 0) return `${m}分`
  return `${m}分${s}秒`
}

async function fetchCourseOptions() {
  try {
    const res: any = await getCoursePage({ pageNum: 1, pageSize: 200 })
    if (res.data) {
      courseOptions.value = res.data.records || []
    } else {
      courseOptions.value = res.records || []
    }
  } catch (e) {
    // 错误已在 request 拦截器处理
  }
}

async function fetchList() {
  if (!selectedCourseId.value) {
    chapterList.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const params: any = {
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
      courseId: selectedCourseId.value
    }
    if (queryForm.title) params.title = queryForm.title
    const res: any = await getChapterPage(params)
    if (res.data) {
      chapterList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      chapterList.value = res.records || []
      total.value = res.total || 0
    }
  } catch (e) {
    // 错误已在 request 拦截器处理
  } finally {
    loading.value = false
  }
}

function onCourseChange() {
  queryForm.pageNum = 1
  fetchList()
}

function handleQuery() {
  queryForm.pageNum = 1
  fetchList()
}

function handleReset() {
  queryForm.title = ''
  queryForm.pageNum = 1
  fetchList()
}

function resetForm() {
  formData.id = undefined
  formData.courseId = selectedCourseId.value
  formData.title = ''
  formData.videoUrl = ''
  formData.duration = 0
  formData.sortOrder = (chapterList.value?.length || 0) + 1
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
  formData.courseId = row.courseId
  formData.title = row.title
  formData.videoUrl = row.videoUrl || ''
  formData.duration = row.duration || 0
  formData.sortOrder = row.sortOrder || 1
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
        await createChapter(data)
        ElMessage.success('新增成功')
      } else {
        await updateChapter(formData)
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

async function handleMoveUp(index: number) {
  if (index <= 0) return
  const list = [...chapterList.value]
  const temp = list[index]
  list[index] = list[index - 1]
  list[index - 1] = temp
  chapterList.value = list
  await saveSort()
}

async function handleMoveDown(index: number) {
  if (index >= chapterList.value.length - 1) return
  const list = [...chapterList.value]
  const temp = list[index]
  list[index] = list[index + 1]
  list[index + 1] = temp
  chapterList.value = list
  await saveSort()
}

async function saveSort() {
  try {
    const ids = chapterList.value.map((item) => item.id)
    await sortChapter(ids)
    ElMessage.success('排序已保存')
  } catch (e) {
    fetchList()
  }
}

async function handleDelete(row: any) {
  await ElMessageBox.confirm(
    `确定删除章节《${row.title}》？删除后不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  try {
    await deleteChapter(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    // 错误已在 request 拦截器处理
  }
}

onMounted(() => {
  fetchCourseOptions()
})
</script>

<style scoped>
.chapter-page {
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
