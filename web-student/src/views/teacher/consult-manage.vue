<template>
  <div class="consult-manage-page">
    <el-card shadow="never" class="consult-manage-page__header">
      <div class="consult-manage-page__header-text">
        <h2>咨询回复</h2>
        <p class="consult-manage-page__subtitle">
          登录身份：<el-tag size="small" type="warning">{{ userStore.realName }}</el-tag>
          <span style="margin-left: 8px">讲师工作台</span>
        </p>
      </div>
    </el-card>

    <!-- 咨询概览统计（基于学员端 API，演示教师视角的"咨询回复"） -->
    <el-row :gutter="16" class="consult-manage-page__stats" v-if="!loadingStats">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card stat-card--primary">
          <div class="stat-card__label">咨询总数</div>
          <div class="stat-card__value">{{ stats.total }}</div>
          <div class="stat-card__hint">学员端 /api/consult/my</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card stat-card--success">
          <div class="stat-card__label">已回复</div>
          <div class="stat-card__value">{{ stats.replied }}</div>
          <div class="stat-card__hint">含智能匹配 + 人工回复</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card stat-card--warning">
          <div class="stat-card__label">待回复</div>
          <div class="stat-card__value">{{ stats.pending }}</div>
          <div class="stat-card__hint">SLA &lt; 1min 告警</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="consult-manage-page__list">
      <template #header>
        <div class="consult-manage-page__list-header">
          <span><el-icon><ChatDotRound /></el-icon> 咨询记录（{{ consultList.length }}）</span>
          <el-button text type="primary" @click="fetchData" :loading="loading">刷新</el-button>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!loading && consultList.length === 0" description="暂无咨询记录">
          <el-button type="primary" @click="goConsult">发起咨询</el-button>
        </el-empty>

        <el-table v-else :data="consultList" stripe>
          <el-table-column label="问题" min-width="280" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="link" @click="goConsult">{{ row.question || row.content || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="回复" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.reply || row.answer" class="reply-text">{{ row.reply || row.answer }}</span>
              <span v-else class="pending">— 暂无回复 —</span>
            </template>
          </el-table-column>
          <el-table-column label="匹配方式" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.matched || row.autoReply" type="success" size="small">智能匹配</el-tag>
              <el-tag v-else type="warning" size="small">人工</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag v-if="row.status === 1 || row.reply" type="success" size="small">已回复</el-tag>
              <el-tag v-else type="warning" size="small">待回复</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="180">
            <template #default="{ row }">
              <span v-if="row.createTime">{{ formatTime(row.createTime) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-card shadow="never" class="consult-manage-page__tip">
      <el-alert
        type="info"
        :closable="false"
        title="咨询接单 / 回复 / 闭环的完整能力在管理后台（5176）提供，这里是教师侧的快速概览。"
      >
        <template #default>
          <div style="margin-top: 8px">
            <el-button type="primary" size="small" @click="goAdmin">前往管理后台（5176）</el-button>
            <el-button size="small" @click="goConsult">查看我的咨询</el-button>
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
import { ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMyConsults } from '@/api/consult'
import { formatTime } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const loadingStats = ref(false)
const consultList = ref([])
const stats = ref({ total: 0, replied: 0, pending: 0 })

async function fetchData() {
  loading.value = true
  loadingStats.value = true
  try {
    // 教师工作台 MVP：用学员端 /api/consult/my 拉取咨询历史，
    // 展示教师视角的"咨询回复概览"（教师也作为学员可发起咨询，作为数据源）
    const res = await getMyConsults({ pageNum: 1, pageSize: 50 })
    const list = res?.records || res?.list || res || []
    consultList.value = Array.isArray(list) ? list : []

    const total = consultList.value.length
    const replied = consultList.value.filter((r) => r.status === 1 || r.reply || r.answer).length
    const pending = total - replied
    stats.value = { total, replied, pending }
  } catch (e) {
    ElMessage.warning('咨询记录加载失败，已为您展示空列表')
    consultList.value = []
    stats.value = { total: 0, replied: 0, pending: 0 }
  } finally {
    loading.value = false
    loadingStats.value = false
  }
}

function goConsult() {
  router.push('/consult')
}

function goAdmin() {
  window.open('http://localhost:5176/#/consult', '_blank')
}

function goHome() {
  router.push('/home')
}

onMounted(fetchData)
</script>

<style scoped>
.consult-manage-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.consult-manage-page__header {
  background: linear-gradient(90deg, #fff7e6 0%, #ffffff 100%);
}
.consult-manage-page__header h2 {
  margin: 0 0 4px;
  font-size: 18px;
  color: #303133;
}
.consult-manage-page__subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
.consult-manage-page__stats {
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
.consult-manage-page__list-header {
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
.reply-text {
  color: #67c23a;
}
.pending {
  color: #c0c4cc;
  font-style: italic;
}
</style>
