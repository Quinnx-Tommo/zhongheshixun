<template>
  <div class="teacher-page">
    <!-- 筛选区 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item label="姓名">
          <el-input
            v-model="queryForm.realName"
            placeholder="请输入姓名"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="教学方向">
          <el-input
            v-model="queryForm.direction"
            placeholder="请输入教学方向"
            clearable
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="姓名/简介"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
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
          <el-icon><Plus /></el-icon>新增讲师
        </el-button>
      </div>

      <el-table v-loading="loading" :data="teacherList" stripe border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="realName" label="姓名" min-width="120" align="center" />
        <el-table-column prop="title" label="职称" width="140" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.title" type="primary">{{ row.title }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="education" label="学历" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.education || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="direction" label="教学方向" min-width="160" show-overflow-tooltip />
        <el-table-column prop="intro" label="简介" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
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
      :title="dialogMode === 'create' ? '新增讲师' : '编辑讲师'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入姓名" maxlength="50" />
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-select v-model="formData.title" placeholder="请选择职称" clearable style="width: 100%">
            <el-option label="主任医师" value="主任医师" />
            <el-option label="副主任医师" value="副主任医师" />
            <el-option label="主治医师" value="主治医师" />
            <el-option label="住院医师" value="住院医师" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="学历" prop="education">
          <el-select v-model="formData.education" placeholder="请选择学历" clearable style="width: 100%">
            <el-option label="博士" value="博士" />
            <el-option label="硕士" value="硕士" />
            <el-option label="本科" value="本科" />
            <el-option label="大专" value="大专" />
            <el-option label="中专" value="中专" />
          </el-select>
        </el-form-item>
        <el-form-item label="教学方向" prop="direction">
          <el-input v-model="formData.direction" placeholder="请输入教学方向" maxlength="100" />
        </el-form-item>
        <el-form-item label="讲师简介" prop="intro">
          <el-input
            v-model="formData.intro"
            type="textarea"
            :rows="4"
            placeholder="请输入讲师简介"
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
  getTeacherPage,
  createTeacher,
  updateTeacher,
  deleteTeacher,
  type TeacherDTO
} from '@/api/teacher'

const loading = ref(false)
const submitting = ref(false)
const teacherList = ref<TeacherDTO[]>([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  realName: '',
  direction: '',
  keyword: ''
})

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const formData = reactive({
  id: undefined as number | undefined,
  realName: '',
  title: '',
  education: '',
  direction: '',
  intro: ''
})

const formRules: FormRules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在 2-50 字', trigger: 'blur' }
  ]
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = {
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    }
    if (queryForm.realName) params.realName = queryForm.realName
    if (queryForm.direction) params.direction = queryForm.direction
    if (queryForm.keyword) params.keyword = queryForm.keyword

    const res: any = await getTeacherPage(params)
    if (res.data) {
      teacherList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      teacherList.value = res.records || []
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
  queryForm.realName = ''
  queryForm.direction = ''
  queryForm.keyword = ''
  queryForm.pageNum = 1
  fetchList()
}

function resetForm() {
  formData.id = undefined
  formData.realName = ''
  formData.title = ''
  formData.education = ''
  formData.direction = ''
  formData.intro = ''
  formRef.value?.clearValidate()
}

function handleCreate() {
  resetForm()
  dialogMode.value = 'create'
  dialogVisible.value = true
}

function handleEdit(row: TeacherDTO) {
  resetForm()
  dialogMode.value = 'edit'
  formData.id = row.id
  formData.realName = row.realName
  formData.title = row.title || ''
  formData.education = row.education || ''
  formData.direction = row.direction || ''
  formData.intro = row.intro || ''
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
        await createTeacher(data)
        ElMessage.success('新增成功')
      } else {
        await updateTeacher(formData)
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

async function handleDelete(row: TeacherDTO) {
  await ElMessageBox.confirm(
    `确定删除讲师「${row.realName}」？删除后不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  try {
    await deleteTeacher(row.id as number)
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
.teacher-page {
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
