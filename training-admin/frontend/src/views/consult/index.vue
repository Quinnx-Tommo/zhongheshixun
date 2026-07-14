<template>
  <div class="consult-page">
    <el-card shadow="never" class="table-card">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getConsultPage, replyConsult, getSlaAlert } from '@/api/consult'

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
