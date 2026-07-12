<template>
  <div class="consult-page">
    <el-card shadow="never" class="table-card">
      <el-tabs v-model="activeTab">
        <!-- 咨询工单 -->
        <el-tab-pane label="咨询工单" name="ticket">
          <el-form :inline="true" :model="query" @submit.prevent>
            <el-form-item label="关键字">
              <el-input v-model="query.keyword" placeholder="问题内容" clearable />
            </el-form-item>
            <el-form-item label="回复类型">
              <el-select v-model="query.isAuto" placeholder="全部" clearable style="width: 140px">
                <el-option label="智能回答" :value="1" />
                <el-option label="人工回答" :value="2" />
                <el-option label="待回复" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadPage">查询</el-button>
              <el-button type="warning" @click="loadSlaAlert">SLA 超时告警</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="loading" :data="records" stripe border>
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="studentId" label="学员ID" width="90" align="center" />
            <el-table-column prop="question" label="问题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="answer" label="回答" min-width="240" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.answer">{{ row.answer }}</span>
                <el-tag v-else type="warning" size="small">待回复</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="回复类型" width="110" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isAuto === 1" type="success" size="small">智能回答</el-tag>
                <el-tag v-else-if="row.isAuto === 2" type="primary" size="small">人工回答</el-tag>
                <el-tag v-else type="warning" size="small">待回复</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提问时间" width="170" align="center" />
            <el-table-column prop="replyTime" label="回复时间" width="170" align="center">
              <template #default="{ row }">
                <span :style="{ color: isOverdue(row) ? '#f5222d' : '' }">
                  {{ row.replyTime || '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="!row.answer"
                  type="primary"
                  link
                  @click="openReply(row)"
                >回复</el-button>
                <el-button v-else type="info" link disabled>已回复</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            class="pagination"
            @size-change="loadPage"
            @current-change="loadPage"
          />
        </el-tab-pane>

        <!-- 知识库管理 -->
        <el-tab-pane label="知识库" name="kb">
          <el-form :inline="true" :model="kbQuery" @submit.prevent>
            <el-form-item label="关键字">
              <el-input v-model="kbQuery.keyword" placeholder="问题/关键词" clearable />
            </el-form-item>
            <el-form-item label="分类">
              <el-input v-model="kbQuery.category" placeholder="分类" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadKbPage">查询</el-button>
              <el-button type="success" @click="openKbCreate">新增条目</el-button>
            </el-form-item>
          </el-form>
          <el-table v-loading="kbLoading" :data="kbRecords" stripe border>
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column prop="question" label="问题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="answer" label="答案" min-width="260" show-overflow-tooltip />
            <el-table-column prop="keywords" label="关键词" min-width="160" show-overflow-tooltip />
            <el-table-column prop="category" label="分类" width="120" align="center" />
            <el-table-column label="操作" width="160" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="openKbEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="handleKbDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="kbQuery.pageNum"
            v-model:page-size="kbQuery.pageSize"
            :total="kbTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            class="pagination"
            @size-change="loadKbPage"
            @current-change="loadKbPage"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog
      v-model="replyVisible"
      title="人工回复"
      width="520px"
      :close-on-click-modal="false"
    >
      <div class="reply-question">
        <span class="label">问题：</span>{{ replyRow?.question }}
      </div>
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="80px">
        <el-form-item label="回复内容" prop="reply">
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="5"
            placeholder="请输入回复内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleReply">确定</el-button>
      </template>
    </el-dialog>

    <!-- 知识库新增/编辑弹窗 -->
    <el-dialog
      v-model="kbDialogVisible"
      :title="kbMode === 'create' ? '新增知识库条目' : '编辑知识库条目'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="kbFormRef" :model="kbForm" :rules="kbRules" label-width="100px">
        <el-form-item label="问题" prop="question">
          <el-input v-model="kbForm.question" placeholder="请输入问题" maxlength="200" />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input
            v-model="kbForm.answer"
            type="textarea"
            :rows="4"
            placeholder="请输入答案"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="关键词" prop="keywords">
          <el-input v-model="kbForm.keywords" placeholder="逗号分隔，用于智能匹配" maxlength="200" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="kbForm.category" placeholder="如：平台使用/考试相关" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="kbDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="kbSubmitting" @click="handleKbSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getConsultPage,
  replyConsult,
  getSlaAlert,
  getKnowledgePage,
  createKnowledge,
  updateKnowledge,
  deleteKnowledge
} from '@/api/consult'

const activeTab = ref('ticket')

// 咨询工单
const loading = ref(false)
const records = ref<any[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  isAuto: undefined as number | undefined
})

// 知识库
const kbLoading = ref(false)
const kbRecords = ref<any[]>([])
const kbTotal = ref(0)
const kbQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '', category: '' })

// 回复弹窗
const replyVisible = ref(false)
const submitting = ref(false)
const replyFormRef = ref<FormInstance>()
const replyRow = ref<any>({})
const replyForm = reactive({ reply: '' })
const replyRules: FormRules = {
  reply: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 2, max: 500, message: '回复长度在 2-500 字', trigger: 'blur' }
  ]
}

// 知识库弹窗
const kbDialogVisible = ref(false)
const kbMode = ref<'create' | 'edit'>('create')
const kbSubmitting = ref(false)
const kbFormRef = ref<FormInstance>()
const kbForm = reactive<any>({ id: undefined, question: '', answer: '', keywords: '', category: '' })
const kbRules: FormRules = {
  question: [{ required: true, message: '请输入问题', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入答案', trigger: 'blur' }]
}

// SLA 超时判断（未回复超过 24 小时）
function isOverdue(row: any) {
  if (row.replyTime || row.answer) return false
  if (!row.createTime) return false
  const create = new Date(row.createTime).getTime()
  return Date.now() - create > 24 * 3600 * 1000
}

async function loadPage() {
  loading.value = true
  try {
    const params: any = {
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
    if (query.keyword) params.keyword = query.keyword
    if (query.isAuto !== undefined) params.isAuto = query.isAuto
    const res: any = await getConsultPage(params)
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadSlaAlert() {
  loading.value = true
  try {
    const res: any = await getSlaAlert(24)
    records.value = (res.data || res) || []
    total.value = records.value.length
    if (records.value.length === 0) {
      ElMessage.success('当前无超时工单')
    } else {
      ElMessage.warning(`发现 ${records.value.length} 条超时工单`)
    }
  } finally {
    loading.value = false
  }
}

function openReply(row: any) {
  replyRow.value = row
  replyForm.reply = ''
  replyVisible.value = true
  replyFormRef.value?.clearValidate()
}

async function handleReply() {
  if (!replyFormRef.value) return
  await replyFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await replyConsult({ id: replyRow.value.id, reply: replyForm.reply })
      ElMessage.success('回复成功')
      replyVisible.value = false
      loadPage()
    } finally {
      submitting.value = false
    }
  })
}

async function loadKbPage() {
  kbLoading.value = true
  try {
    const params: any = { pageNum: kbQuery.pageNum, pageSize: kbQuery.pageSize }
    if (kbQuery.keyword) params.keyword = kbQuery.keyword
    if (kbQuery.category) params.category = kbQuery.category
    const res: any = await getKnowledgePage(params)
    kbRecords.value = res.data?.records || []
    kbTotal.value = res.data?.total || 0
  } finally {
    kbLoading.value = false
  }
}

function resetKbForm() {
  kbForm.id = undefined
  kbForm.question = ''
  kbForm.answer = ''
  kbForm.keywords = ''
  kbForm.category = ''
  kbFormRef.value?.clearValidate()
}

function openKbCreate() {
  resetKbForm()
  kbMode.value = 'create'
  kbDialogVisible.value = true
}

function openKbEdit(row: any) {
  resetKbForm()
  kbMode.value = 'edit'
  kbForm.id = row.id
  kbForm.question = row.question || ''
  kbForm.answer = row.answer || ''
  kbForm.keywords = row.keywords || ''
  kbForm.category = row.category || ''
  kbDialogVisible.value = true
}

async function handleKbSubmit() {
  if (!kbFormRef.value) return
  await kbFormRef.value.validate(async (valid) => {
    if (!valid) return
    kbSubmitting.value = true
    try {
      if (kbMode.value === 'create') {
        const { id, ...data } = kbForm
        await createKnowledge(data)
        ElMessage.success('新增成功')
      } else {
        await updateKnowledge(kbForm)
        ElMessage.success('更新成功')
      }
      kbDialogVisible.value = false
      loadKbPage()
    } finally {
      kbSubmitting.value = false
    }
  })
}

async function handleKbDelete(row: any) {
  await ElMessageBox.confirm(`确定删除知识库条目「${row.question}」？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    await deleteKnowledge(row.id)
    ElMessage.success('删除成功')
    loadKbPage()
  } catch (e) {
    // handled by interceptor
  }
}

onMounted(() => {
  loadPage()
})
</script>

<style scoped>
.consult-page {
  padding: 16px;
}
.table-card {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
.reply-question {
  background: #f5f6f8;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
  line-height: 1.6;
}
.reply-question .label {
  color: #999;
}
</style>
