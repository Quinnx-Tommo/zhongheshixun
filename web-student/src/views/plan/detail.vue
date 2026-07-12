<template>
  <div class="plan-detail-page" v-loading="loading">
    <el-button @click="router.push('/plans')" class="plan-detail-page__back">
      <el-icon><ArrowLeft /></el-icon> 返回计划列表
    </el-button>

    <el-alert
      v-if="loadError"
      type="error"
      title="计划加载失败"
      show-icon
      class="plan-detail-page__alert"
    />

    <template v-else>
      <el-row :gutter="20">
        <el-col :xs="24" :md="16">
          <el-card shadow="never" class="plan-detail-page__main">
            <h1 class="plan-detail-page__title">{{ plan.title || '培训计划' }}</h1>
            <div class="plan-detail-page__desc">
              <h3>计划简介</h3>
              <p>{{ plan.description || '暂无简介' }}</p>
            </div>

            <div class="plan-detail-page__progress">
              <h3>学习进度</h3>
              <el-progress
                :percentage="plan.progress || 0"
                :stroke-width="14"
                :text-inside="true"
              />
              <div class="plan-detail-page__progress-meta">
                已完成 <strong>{{ plan.completedCount || 0 }}</strong> / 共
                <strong>{{ plan.totalCount || 0 }}</strong> 门课程
              </div>
            </div>

            <div class="plan-detail-page__courses" v-if="courseList.length > 0">
              <h3>计划课程（{{ courseList.length }} 门）</h3>
              <el-row :gutter="12">
                <el-col
                  :xs="24"
                  :sm="12"
                  v-for="course in courseList"
                  :key="course.id"
                >
                  <el-card
                    class="course-item"
                    shadow="hover"
                    @click="router.push(`/courses/${course.id}`)"
                  >
                    <div class="course-item__title">{{ course.title }}</div>
                    <div class="course-item__meta">
                      <el-tag v-if="course.courseType" size="small" :type="typeColor(course.courseType)">
                        {{ typeText(course.courseType) }}
                      </el-tag>
                      <el-tag v-if="course.difficulty" size="small" type="info">
                        {{ course.difficulty }}
                      </el-tag>
                      <span v-if="course.totalHours">📚 {{ course.totalHours }} 学时</span>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
            <el-empty v-else description="该计划暂未关联课程" />
          </el-card>
        </el-col>

        <el-col :xs="24" :md="8">
          <el-card shadow="never" class="plan-detail-page__side">
            <div class="info-row">
              <span class="info-row__label">计划名称</span>
              <span class="info-row__value">{{ plan.title || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-row__label">开始时间</span>
              <span class="info-row__value">{{ formatTime(plan.startTime) || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-row__label">结束时间</span>
              <span class="info-row__value">{{ formatTime(plan.endTime) || '长期有效' }}</span>
            </div>
            <div class="info-row">
              <span class="info-row__label">课程数量</span>
              <span class="info-row__value">{{ plan.totalCount || 0 }} 门</span>
            </div>
            <div class="info-row">
              <span class="info-row__label">已完成</span>
              <span class="info-row__value">{{ plan.completedCount || 0 }} 门</span>
            </div>
            <div class="info-row">
              <span class="info-row__label">完成进度</span>
              <span class="info-row__value">{{ plan.progress || 0 }}%</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPlanDetail } from '@/api/plan'
import { formatTime } from '@/utils/format'
import { typeText, typeColor } from '@/utils/dict'

const route = useRoute()
const router = useRouter()
const planId = computed(() => route.params.id)

const loading = ref(false)
const loadError = ref(false)
const plan = ref({})
const courseList = ref([])

async function fetchDetail() {
  loading.value = true
  loadError.value = false
  try {
    const data = await getPlanDetail(planId.value)
    plan.value = data || {}
    courseList.value = Array.isArray(data?.courseList) ? data.courseList : []
  } catch (e) {
    loadError.value = true
    ElMessage.warning('计划详情加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.plan-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.plan-detail-page__back {
  align-self: flex-start;
  margin-bottom: 8px;
}
.plan-detail-page__alert {
  margin-bottom: 16px;
}
.plan-detail-page__main {
  background: #fff;
  padding: 24px;
}
.plan-detail-page__title {
  margin: 0 0 16px;
  font-size: 22px;
  color: #303133;
}
.plan-detail-page__desc h3,
.plan-detail-page__progress h3,
.plan-detail-page__courses h3 {
  font-size: 16px;
  margin: 0 0 8px;
  color: #303133;
}
.plan-detail-page__desc p {
  color: #606266;
  line-height: 1.6;
  margin: 0 0 16px;
}
.plan-detail-page__progress {
  margin-bottom: 24px;
}
.plan-detail-page__progress-meta {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}
.plan-detail-page__courses {
  margin-top: 16px;
}
.course-item {
  margin-bottom: 12px;
  cursor: pointer;
}
.course-item__title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
}
.course-item__meta {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 12px;
  color: #909399;
}
.plan-detail-page__side {
  background: #fff;
  padding: 20px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}
.info-row:last-of-type {
  border-bottom: none;
}
.info-row__label {
  color: #909399;
}
.info-row__value {
  color: #303133;
  font-weight: 500;
}
</style>
