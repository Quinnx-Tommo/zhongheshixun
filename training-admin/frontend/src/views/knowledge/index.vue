<template>
  <div class="knowledge-page">
    <!-- 课程选择 + 筛选 -->
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
        <el-form-item label="知识点名称">
          <el-input
            v-model="queryForm.name"
            placeholder="请输入知识点名称"
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
          <el-icon><Plus /></el-icon>新增知识点
        </el-button>
      </div>

      <el-table v-loading="loading" :data="knowledgeList" stripe border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="courseId" label="课程ID" width="100" align="center" />
        <el-table-column prop="name" label="知识点名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="280" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
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
      :title="dialogMode === 'create' ? '新增知识点' : '编辑知识点'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="所属课程" prop="courseId">
          <el-select
            v-model="formData.courseId"
            placeholder="请选择课程"
            filterable
            :disabled="dialogMode === 'edit'"
            style="width: 100%"
          >
            <el-option
              v-for="item in courseOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入知识点名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入描述"
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
import { getCoursePage } from '@/api/course'
import {
  getKnowledgePage,
  createKnowledge,
  updateKnowledge,
  deleteKnowledge
} from '@/api/knowledge'

const loading = ref(false)
const submitting = ref(false)
const knowledgeList = ref<any[]>([])
const total = ref(0)

const courseOptions = ref<any[]>([])
const selectedCourseId = ref<number | undefined>(undefined)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  name: ''
})

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const formData = reactive({
  id: undefined as number | undefined,
  courseId: undefined as number | undefined,
  name: '',
  description: ''
})

const formRules: FormRules = {
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入知识点名称', trigger: 'blur' },
    { min: 2, max: 100, message: '名称长度在 2-100 字', trigger: 'blur' }
  ]
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
    knowledgeList.value = []
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
    if (queryForm.name) params.name = queryForm.name
    const res: any = await getKnowledgePage(params)
    if (res.data) {
      knowledgeList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      knowledgeList.value = res.records || []
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
  queryForm.name = ''
  queryForm.pageNum = 1
  fetchList()
}

function resetForm() {
  formData.id = undefined
  formData.courseId = selectedCourseId.value
  formData.name = ''
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
  formData.courseId = row.courseId
  formData.name = row.name
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
        await createKnowledge(data)
        ElMessage.success('新增成功')
      } else {
        await updateKnowledge(formData)
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

async function handleDelete(row: any) {
  await ElMessageBox.confirm(
    `确定删除知识点《${row.name}》？删除后不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  try {
    await deleteKnowledge(row.id)
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
.knowledge-page {
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
