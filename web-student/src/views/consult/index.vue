<template>
  <div class="consult-page" v-loading="loading">
    <el-row :gutter="20">
      <!-- 左侧聊天区 -->
      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="consult-page__chat">
          <template #header>
            <div class="consult-page__chat-header">
              <span class="consult-page__chat-title">智能咨询助手</span>
              <el-tag size="small" type="success">在线</el-tag>
            </div>
          </template>

          <!-- 消息列表 -->
          <div class="consult-page__messages" ref="messagesRef">
            <div
              v-for="(msg, idx) in messages"
              :key="idx"
              class="consult-page__msg"
              :class="consult-page__msg--"
            >
              <el-avatar
                :size="36"
                :class="msg.role === 'user' ? 'consult-page__avatar-user' : 'consult-page__avatar-bot'"
              >
                {{ msg.role === 'user' ? '我' : 'AI' }}
              </el-avatar>
              <div class="consult-page__bubble">
                <span v-if="msg.role === 'bot' && msg.source === 'ai'" class="consult-page__source-tag">AI</span>
                <span v-if="msg.role === 'bot' && msg.source === 'kb'" class="consult-page__source-tag consult-page__source-tag--kb">知识库</span>
                {{ msg.text }}
              </div>
            </div>
            <el-empty v-if="messages.length === 0" description="开始提问吧" :image-size="80" />
          </div>

          <!-- 输入区 -->
          <div class="consult-page__input">
            <el-input
              v-model="question"
              type="textarea"
              :rows="3"
              placeholder="请输入您的问题（Ctrl+Enter 发送）"
              :disabled="asking"
              @keydown.ctrl.enter="handleAsk"
            />
            <div class="consult-page__input-actions">
              <span class="consult-page__hint">提示：知识库未命中时将调用 AI 智能回答</span>
              <el-button
                type="primary"
                :loading="asking"
                :disabled="!question.trim()"
                @click="handleAsk"
              >
                发送
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧历史记录 -->
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="consult-page__history">
          <template #header>
            <div class="consult-page__history-header">
              <span>我的咨询历史</span>
              <el-button link size="small" @click="fetchHistory">刷新</el-button>
            </div>
          </template>
          <div v-if="historyList.length > 0" class="consult-page__history-list">
            <div
              v-for="item in historyList"
              :key="item.id"
              class="consult-page__history-item"
              @click="viewHistory(item)"
            >
              <div class="consult-page__history-q">Q: {{ item.question }}</div>
              <div class="consult-page__history-a">
                <template v-if="item.answer">A: {{ item.answer }}</template>
                <template v-else>等待人工回复...</template>
              </div>
              <div class="consult-page__history-meta">
                <el-tag v-if="item.isAuto === 1" size="small" type="success">自动回复</el-tag>
                <el-tag v-else-if="item.isAuto === 2" size="small" type="primary">人工回复</el-tag>
                <el-tag v-else-if="item.slaExceeded === 1" size="small" type="danger" effect="dark">超时</el-tag>
                <el-tag v-else size="small" type="warning">待回复</el-tag>
                <span class="consult-page__history-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div v-if="!item.answer && item.slaExceeded === 1" class="consult-page__history-sla">
                已超时，老师正在加急处理...
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无历史咨询" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { askConsult, getMyConsults } from '@/api/consult'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const asking = ref(false)
const question = ref('')
const messages = ref([]) // { role: 'user' | 'bot', text, source }
const historyList = ref([])
const messagesRef = ref(null)

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function handleAsk() {
  const text = question.value.trim()
  if (!text) return
  // 插入用户消息
  messages.value.push({ role: 'user', text })
  question.value = ''
  scrollToBottom()

  asking.value = true
  try {
    const result = await askConsult({ question: text })
    // 后端返回 { consultId, autoReply, matched, source }
    const reply = result?.autoReply || '已收到您的问题，工作人员将尽快回复。'
    const source = result?.source || (result?.matched ? 'kb' : 'human')
    messages.value.push({ role: 'bot', text: reply, source })
    if (result?.matched) {
      ElMessage.success(source === 'kb' ? '已为您匹配到知识库答案' : 'AI 已为您生成回答')
    } else {
      ElMessage.info('已转人工服务')
    }
    fetchHistory()
  } catch (e) {
    messages.value.push({ role: 'bot', text: '咨询失败，请稍后重试', source: 'system' })
  } finally {
    asking.value = false
    scrollToBottom()
  }
}

async function fetchHistory() {
  loading.value = true
  try {
    const res = await getMyConsults({ pageNum: 1, pageSize: 20 })
    historyList.value = res?.records || res?.list || res || []
  } catch (e) {
    historyList.value = []
  } finally {
    loading.value = false
  }
}

function viewHistory(item) {
  // 点击历史记录，在聊天区展示该条对话
  messages.value = [
    { role: 'user', text: item.question },
    { role: 'bot', text: item.answer || '等待人工回复...', source: item.isAuto === 1 ? 'kb' : (item.isAuto === 2 ? 'human' : 'pending') }
  ]
  scrollToBottom()
}

onMounted(() => {
  messages.value.push({
    role: 'bot',
    text: '您好！我是基层卫生培训平台的智能助手，可以为您解答课程学习、考试报名、培训安排等问题。',
    source: 'system'
  })
  fetchHistory()
})
</script>

<style scoped>
.consult-page {
  display: flex;
  flex-direction: column;
}
.consult-page__chat {
  background: #fff;
  height: calc(100vh - 180px);
  display: flex;
  flex-direction: column;
}
.consult-page__chat-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.consult-page__chat-title {
  font-size: 16px;
  font-weight: 500;
}
.consult-page__messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.consult-page__msg {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
.consult-page__msg--user {
  flex-direction: row-reverse;
}
.consult-page__bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 8px;
  line-height: 1.5;
  word-break: break-word;
}
.consult-page__msg--user .consult-page__bubble {
  background: #1677ff;
  color: #fff;
}
.consult-page__msg--bot .consult-page__bubble {
  background: #f5f7fa;
  color: #303133;
}
.consult-page__avatar-user {
  background: #1677ff;
  color: #fff;
}
.consult-page__avatar-bot {
  background: #67c23a;
  color: #fff;
}
.consult-page__source-tag {
  display: inline-block;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
  background: #e6f7ff;
  color: #1677ff;
  margin-right: 4px;
  vertical-align: middle;
}
.consult-page__source-tag--kb {
  background: #f6ffed;
  color: #52c41a;
}
.consult-page__input {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}
.consult-page__input-actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.consult-page__hint {
  color: #909399;
  font-size: 12px;
}
.consult-page__history {
  background: #fff;
  height: calc(100vh - 180px);
  overflow-y: auto;
}
.consult-page__history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.consult-page__history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.consult-page__history-item {
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.consult-page__history-item:hover {
  background: #ebeef5;
}
.consult-page__history-q {
  font-size: 13px;
  color: #303133;
  margin-bottom: 4px;
}
.consult-page__history-a {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}
.consult-page__history-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}
.consult-page__history-time {
  margin-left: auto;
}
.consult-page__history-sla {
  margin-top: 6px;
  font-size: 12px;
  color: #f5222d;
  font-weight: 500;
}
</style>