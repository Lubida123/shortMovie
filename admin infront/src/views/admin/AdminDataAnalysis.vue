<!-- src/views/admin/AdminDataAnalysis.vue -->
<template>
  <div class="admin-data-analysis-container">
    <!-- 页面标题和日期选择 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1><el-icon><TrendCharts /></el-icon> 数据分析中心</h1>
          <p class="page-subtitle">实时监控平台数据趋势与用户行为</p>
        </div>
        <div class="header-right">
          <div class="date-selector">
            <el-radio-group v-model="timeRange" size="small" @change="handleTimeRangeChange">
              <el-radio-button label="today">今日</el-radio-button>
              <el-radio-button label="week">近7天</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="custom">自定义</el-radio-button>
            </el-radio-group>
            
            <el-date-picker
              v-if="timeRange === 'custom'"
              v-model="customDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              style="width: 240px; margin-left: 12px;"
              @change="handleCustomDateChange"
            />
          </div>
          <el-button type="primary" :icon="Download" size="small" @click="exportData">
            导出数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 核心数据指标 -->
    <div class="core-metrics">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="metric-card" style="border-left-color: #FE2C55;">
            <div class="metric-icon" style="background: linear-gradient(135deg, #FE2C55, #FF6B8B);">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ formatNumber(coreMetrics.totalViews) }}</div>
              <div class="metric-label">总播放量</div>
              <div class="metric-trend" :class="coreMetrics.viewTrend >= 0 ? 'up' : 'down'">
                <el-icon :size="12"><TrendingUp v-if="coreMetrics.viewTrend >= 0" /><TrendingDown v-else /></el-icon>
                <span>{{ Math.abs(coreMetrics.viewTrend) }}%</span>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="metric-card" style="border-left-color: #25F4EE;">
            <div class="metric-icon" style="background: linear-gradient(135deg, #25F4EE, #6CEBFF);">
              <el-icon><Star /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ formatNumber(coreMetrics.totalLikes) }}</div>
              <div class="metric-label">总点赞数</div>
              <div class="metric-trend" :class="coreMetrics.likeTrend >= 0 ? 'up' : 'down'">
                <el-icon :size="12"><TrendingUp v-if="coreMetrics.likeTrend >= 0" /><TrendingDown v-else /></el-icon>
                <span>{{ Math.abs(coreMetrics.likeTrend) }}%</span>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="metric-card" style="border-left-color: #FF9500;">
            <div class="metric-icon" style="background: linear-gradient(135deg, #FF9500, #FFB74D);">
              <el-icon><ChatLineRound /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ formatNumber(coreMetrics.totalComments) }}</div>
              <div class="metric-label">总评论数</div>
              <div class="metric-trend" :class="coreMetrics.commentTrend >= 0 ? 'up' : 'down'">
                <el-icon :size="12"><TrendingUp v-if="coreMetrics.commentTrend >= 0" /><TrendingDown v-else /></el-icon>
                <span>{{ Math.abs(coreMetrics.commentTrend) }}%</span>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="metric-card" style="border-left-color: #00C864;">
            <div class="metric-icon" style="background: linear-gradient(135deg, #00C864, #85D475);">
              <el-icon><Share /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ formatNumber(coreMetrics.totalShares) }}</div>
              <div class="metric-label">总分享数</div>
              <div class="metric-trend" :class="coreMetrics.shareTrend >= 0 ? 'up' : 'down'">
                <el-icon :size="12"><TrendingUp v-if="coreMetrics.shareTrend >= 0" /><TrendingDown v-else /></el-icon>
                <span>{{ Math.abs(coreMetrics.shareTrend) }}%</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 数据概览图表 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 播放量趋势图 -->
        <el-col :xs="24" :lg="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3><el-icon><TrendCharts /></el-icon> 播放量趋势</h3>
              <div class="chart-actions">
                <el-select v-model="viewChartType" size="small" style="width: 120px;" @change="handleViewChartTypeChange">
                  <el-option label="日趋势" value="daily" />
                  <el-option label="周趋势" value="weekly" />
                  <el-option label="月趋势" value="monthly" />
                </el-select>
                <el-button 
                  type="text" 
                  :icon="FullScreen" 
                  size="small"
                  @click="openChartZoom('viewChart')"
                  title="全屏查看"
                />
              </div>
            </div>
            <div class="chart-container">
              <div class="chart" ref="viewChartRef" style="width: 100%; height: 100%;"></div>
            </div>
          </div>
        </el-col>
        
        <!-- 用户互动分析 -->
        <el-col :xs="24" :lg="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3><el-icon><PieChart /></el-icon> 用户互动分析</h3>
              <div class="chart-actions">
                <el-radio-group v-model="interactionType" size="small" @change="handleInteractionTypeChange">
                  <el-radio-button label="distribution">分布</el-radio-button>
                  <el-radio-button label="comparison">对比</el-radio-button>
                </el-radio-group>
                <el-button 
                  type="text" 
                  :icon="FullScreen" 
                  size="small"
                  @click="openChartZoom('interactionChart')"
                  title="全屏查看"
                />
              </div>
            </div>
            <div class="chart-container">
              <div class="chart" ref="interactionChartRef" style="width: 100%; height: 100%;"></div>
            </div>
          </div>
        </el-col>
        
        <!-- 用户增长趋势 -->
        <el-col :xs="24" :lg="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3><el-icon><User /></el-icon> 用户增长趋势</h3>
              <div class="chart-actions">
                <div class="chart-tabs">
                  <el-radio-group v-model="userGrowthType" size="small" @change="handleUserGrowthTypeChange">
                    <el-radio-button label="new">新增用户</el-radio-button>
                    <el-radio-button label="active">活跃用户</el-radio-button>
                    <el-radio-button label="total">累计用户</el-radio-button>
                  </el-radio-group>
                </div>
                <el-button 
                  type="text" 
                  :icon="FullScreen" 
                  size="small"
                  @click="openChartZoom('userGrowthChart')"
                  title="全屏查看"
                />
              </div>
            </div>
            <div class="chart-container">
              <div class="chart" ref="userGrowthChartRef" style="width: 100%; height: 100%;"></div>
            </div>
          </div>
        </el-col>
        
        <!-- 视频分类分布 -->
        <el-col :xs="24" :lg="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3><el-icon><DataAnalysis /></el-icon> 视频分类分布</h3>
              <div class="chart-actions">
                <el-select v-model="categorySort" size="small" style="width: 120px;" @change="handleCategorySortChange">
                  <el-option label="按数量" value="count" />
                  <el-option label="按播放量" value="views" />
                  <el-option label="按互动量" value="interaction" />
                </el-select>
                <el-button 
                  type="text" 
                  :icon="FullScreen" 
                  size="small"
                  @click="openChartZoom('categoryChart')"
                  title="全屏查看"
                />
              </div>
            </div>
            <div class="chart-container">
              <div class="chart" ref="categoryChartRef" style="width: 100%; height: 100%;"></div>
            </div>
          </div>
        </el-col>
        
        <!-- 热门时段分析 -->
        <el-col :xs="24" :lg="24">
          <div class="chart-card">
            <div class="chart-header">
              <h3><el-icon><Clock /></el-icon> 用户活跃时段分析</h3>
              <div class="chart-actions">
                <el-radio-group v-model="activePeriod" size="small" @change="handleActivePeriodChange">
                  <el-radio-button label="weekday">工作日</el-radio-button>
                  <el-radio-button label="weekend">周末</el-radio-button>
                  <el-radio-button label="all">全部</el-radio-button>
                </el-radio-group>
                <el-button 
                  type="text" 
                  :icon="FullScreen" 
                  size="small"
                  @click="openChartZoom('activePeriodChart')"
                  title="全屏查看"
                />
              </div>
            </div>
            <div class="chart-container">
              <div class="chart" ref="activePeriodChartRef" style="width: 100%; height: 100%;"></div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格：热门视频排行榜 -->
    <div class="data-tables">
      <el-row :gutter="20">
        <!-- 热门视频排行榜 -->
        <el-col :xs="24" :lg="24">
          <div class="table-card">
            <div class="table-header">
              <h3><el-icon><Trophy /></el-icon> 热门视频排行榜</h3>
              <div class="table-actions">
                <el-select v-model="hotVideoSort" size="small" style="width: 120px;" @change="handleHotVideoSortChange">
                  <el-option label="按播放量" value="views" />
                  <el-option label="按点赞数" value="likes" />
                  <el-option label="按评论数" value="comments" />
                  <el-option label="按互动率" value="interactionRate" />
                </el-select>
              </div>
            </div>
            <div class="table-container">
              <el-table
                :data="sortedHotVideos"
                style="width: 100%"
                :row-class-name="tableRowClassName"
                @row-click="handleVideoClick"
              >
                <el-table-column label="排名" width="60" align="center">
                  <template #default="scope">
                    <div class="rank-cell" :class="getRankClass(scope.$index)">
                      {{ scope.$index + 1 }}
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="视频标题" min-width="180">
                  <template #default="scope">
                    <div class="video-info">
                      <img :src="scope.row.cover" alt="封面" class="video-cover">
                      <div class="video-details">
                        <div class="video-title">{{ scope.row.title }}</div>
                        <div class="video-author">{{ scope.row.author }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column prop="views" label="播放量" width="100" align="center">
                  <template #default="scope">
                    <span class="views-count">{{ formatNumber(scope.row.views) }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column prop="likes" label="点赞数" width="100" align="center">
                  <template #default="scope">
                    <span class="likes-count">{{ formatNumber(scope.row.likes) }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="互动率" width="100" align="center">
                  <template #default="scope">
                    <span class="interaction-rate">{{ (scope.row.interactionRate * 100).toFixed(1) }}%</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="趋势" width="80" align="center">
                  <template #default="scope">
                    <div class="video-trend" :class="scope.row.trend >= 0 ? 'up' : 'down'">
                      <el-icon :size="12"><TrendingUp v-if="scope.row.trend >= 0" /><TrendingDown v-else /></el-icon>
                      <span>{{ Math.abs(scope.row.trend) }}%</span>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 数据刷新按钮 -->
    <div class="refresh-section">
      <el-button 
        type="primary" 
        :icon="Refresh" 
        :loading="loading"
        @click="refreshData"
      >
        刷新数据
      </el-button>
      <span class="last-update">最后更新: {{ lastUpdateTime }}</span>
    </div>

    <!-- 图表放大弹窗 -->
    <el-dialog
      v-model="chartZoomVisible"
      :title="zoomChartTitle"
      width="95%"
      top="5vh"
      destroy-on-close
      @close="handleZoomDialogClose"
      class="chart-zoom-dialog"
    >
      <div class="zoom-chart-container" ref="zoomChartRef"></div>
      <div class="zoom-chart-actions">
        <el-button 
          type="primary" 
          :icon="Download" 
          @click="downloadZoomChart"
          size="small"
        >
          下载图片
        </el-button>
        <el-button 
          :icon="FullScreen" 
          @click="toggleFullScreen"
          size="small"
        >
          {{ isFullScreen ? '退出全屏' : '全屏模式' }}
        </el-button>
        <el-button 
          :icon="Refresh" 
          @click="refreshZoomChart"
          size="small"
        >
          刷新数据
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  VideoPlay,
  Star,
  ChatLineRound,
  Share,
  TrendCharts,
  PieChart,
  User,
  DataAnalysis,
  Clock,
  Trophy,
  Refresh,
  Download,
  FullScreen,
  ArrowUp,
  ArrowDown
} from '@element-plus/icons-vue'

// 状态管理
const loading = ref(false)
const chartLoading = ref(false)
const timeRange = ref('week')
const customDateRange = ref([])
const viewChartType = ref('daily')
const interactionType = ref('distribution')
const userGrowthType = ref('new')
const categorySort = ref('count')
const activePeriod = ref('all')
const hotVideoSort = ref('views')
const lastUpdateTime = ref('')

// 图表放大相关状态
const chartZoomVisible = ref(false)
const currentZoomChartType = ref('')
const zoomChartTitle = ref('')
const zoomChartRef = ref(null)
let zoomChartInstance = null
const isFullScreen = ref(false)

// 图表实例引用
const viewChartRef = ref(null)
const interactionChartRef = ref(null)
const userGrowthChartRef = ref(null)
const categoryChartRef = ref(null)
const activePeriodChartRef = ref(null)

// 图表实例
let viewChartInstance = null
let interactionChartInstance = null
let userGrowthChartInstance = null
let categoryChartInstance = null
let activePeriodChartInstance = null

// 核心指标数据
const coreMetrics = ref({
  totalViews: 1567890,
  viewTrend: 12.5,
  totalLikes: 234567,
  likeTrend: 8.3,
  totalComments: 45678,
  commentTrend: 5.7,
  totalShares: 12345,
  shareTrend: 15.2
})

// 原始表格数据（保持原始顺序）
const rawHotVideos = ref([
  { 
    id: 1, 
    cover: 'https://via.placeholder.com/60x40?text=Video1', 
    title: '生活小技巧分享', 
    author: '张三', 
    views: 154321, 
    likes: 12345, 
    comments: 2345, 
    interactionRate: 0.095,
    trend: 12.5,
    publishDate: '2024-01-15',
    duration: '3:45'
  },
  { 
    id: 2, 
    cover: 'https://via.placeholder.com/60x40?text=Video2', 
    title: '搞笑短视频合集', 
    author: '李四', 
    views: 143210, 
    likes: 13456, 
    comments: 3456, 
    interactionRate: 0.118,
    trend: 8.3,
    publishDate: '2024-01-14',
    duration: '5:20'
  },
  { 
    id: 3, 
    cover: 'https://via.placeholder.com/60x40?text=Video3', 
    title: '编程入门教程', 
    author: '王五', 
    views: 132109, 
    likes: 14567, 
    comments: 4567, 
    interactionRate: 0.145,
    trend: 15.2,
    publishDate: '2024-01-13',
    duration: '15:30'
  },
  { 
    id: 4, 
    cover: 'https://via.placeholder.com/60x40?text=Video4', 
    title: '游戏精彩集锦', 
    author: '赵六', 
    views: 121098, 
    likes: 15678, 
    comments: 5678, 
    interactionRate: 0.176,
    trend: 5.7,
    publishDate: '2024-01-12',
    duration: '8:15'
  },
  { 
    id: 5, 
    cover: 'https://via.placeholder.com/60x40?text=Video5', 
    title: '音乐现场录制', 
    author: '钱七', 
    views: 110987, 
    likes: 16789, 
    comments: 6789, 
    interactionRate: 0.213,
    trend: 22.1,
    publishDate: '2024-01-11',
    duration: '4:50'
  },
  { 
    id: 6, 
    cover: 'https://via.placeholder.com/60x40?text=Video6', 
    title: '美食制作教程', 
    author: '孙八', 
    views: 109876, 
    likes: 17890, 
    comments: 7890, 
    interactionRate: 0.198,
    trend: 7.4,
    publishDate: '2024-01-10',
    duration: '12:30'
  },
  { 
    id: 7, 
    cover: 'https://via.placeholder.com/60x40?text=Video7', 
    title: '旅行vlog分享', 
    author: '周九', 
    views: 98765, 
    likes: 18901, 
    comments: 8901, 
    interactionRate: 0.245,
    trend: 18.9,
    publishDate: '2024-01-09',
    duration: '7:45'
  },
  { 
    id: 8, 
    cover: 'https://via.placeholder.com/60x40?text=Video8', 
    title: '健身教学视频', 
    author: '吴十', 
    views: 87654, 
    likes: 19012, 
    comments: 9012, 
    interactionRate: 0.167,
    trend: 3.2,
    publishDate: '2024-01-08',
    duration: '10:20'
  },
  { 
    id: 9, 
    cover: 'https://via.placeholder.com/60x40?text=Video9', 
    title: '科技产品评测', 
    author: '郑十一', 
    views: 76543, 
    likes: 20123, 
    comments: 10123, 
    interactionRate: 0.189,
    trend: 14.5,
    publishDate: '2024-01-07',
    duration: '18:15'
  },
  { 
    id: 10, 
    cover: 'https://via.placeholder.com/60x40?text=Video10', 
    title: '宠物日常分享', 
    author: '王十二', 
    views: 65432, 
    likes: 21234, 
    comments: 11234, 
    interactionRate: 0.256,
    trend: 25.3,
    publishDate: '2024-01-06',
    duration: '2:30'
  }
])

// 固定活跃时段数据（每个时段不超过1000）
const fixedActivePeriodData = {
  // 工作日数据 - 早晨和晚上高峰
  weekday: [
    50,   // 0:00
    30,   // 1:00
    20,   // 2:00
    15,   // 3:00
    20,   // 4:00
    50,   // 5:00
    180,  // 6:00
    350,  // 7:00
    450,  // 8:00
    420,  // 9:00
    400,  // 10:00
    380,  // 11:00
    450,  // 12:00 (午休时间)
    420,  // 13:00
    400,  // 14:00
    380,  // 15:00
    420,  // 16:00
    500,  // 17:00 (下班时间开始)
    750,  // 18:00
    850,  // 19:00
    950,  // 20:00 (晚间高峰)
    800,  // 21:00
    400,  // 22:00
    150   // 23:00
  ],
  // 周末数据 - 白天活跃时间更长
  weekend: [
    80,   // 0:00
    60,   // 1:00
    40,   // 2:00
    30,   // 3:00
    40,   // 4:00
    60,   // 5:00
    120,  // 6:00
    200,  // 7:00
    350,  // 8:00
    550,  // 9:00
    700,  // 10:00
    850,  // 11:00
    950,  // 12:00 (午间高峰)
    900,  // 13:00
    850,  // 14:00
    800,  // 15:00
    850,  // 16:00
    900,  // 17:00
    950,  // 18:00 (晚间高峰)
    980,  // 19:00
    900,  // 20:00
    650,  // 21:00
    350,  // 22:00
    120   // 23:00
  ],
  // 平均数据
  all: []
}

// 计算平均数据
fixedActivePeriodData.all = fixedActivePeriodData.weekday.map((weekdayValue, index) => {
  return Math.round((weekdayValue + fixedActivePeriodData.weekend[index]) / 2)
})

// ==================== 计算属性：排序后的表格数据 ====================

// 排序后的热门视频
const sortedHotVideos = computed(() => {
  const videos = [...rawHotVideos.value]
  
  // 根据排序条件排序
  switch (hotVideoSort.value) {
    case 'views':
      return videos.sort((a, b) => b.views - a.views)
    case 'likes':
      return videos.sort((a, b) => b.likes - a.likes)
    case 'comments':
      return videos.sort((a, b) => b.comments - a.comments)
    case 'interactionRate':
      return videos.sort((a, b) => b.interactionRate - a.interactionRate)
    default:
      return videos.sort((a, b) => b.views - a.views)
  }
})

// ==================== 生成不同粒度的时间标签函数 ====================

// 生成日趋势标签
const generateDailyLabels = () => {
  const labels = []
  const now = new Date()
  let days = 7
  
  // 根据时间范围确定天数
  switch (timeRange.value) {
    case 'today':
      days = 1
      // 如果是今天，显示小时数据
      for (let i = 0; i < 24; i++) {
        labels.push(`${i.toString().padStart(2, '0')}:00`)
      }
      return labels
    case 'week':
      days = 7
      break
    case 'month':
      days = 30
      break
    case 'custom':
      if (customDateRange.value && customDateRange.value.length === 2) {
        const start = new Date(customDateRange.value[0])
        const end = new Date(customDateRange.value[1])
        const diffTime = Math.abs(end - start)
        days = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
      } else {
        days = 7
      }
      break
  }
  
  // 生成日期标签
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    labels.push(date.toLocaleDateString('zh-CN', { 
      month: '2-digit', 
      day: '2-digit' 
    }))
  }
  
  return labels
}

// 生成周趋势标签
const generateWeeklyLabels = () => {
  const labels = []
  const now = new Date()
  let weeks = 4
  
  // 根据时间范围确定周数
  switch (timeRange.value) {
    case 'today':
    case 'week':
      weeks = 1
      break
    case 'month':
      weeks = 4
      break
    case 'custom':
      if (customDateRange.value && customDateRange.value.length === 2) {
        const start = new Date(customDateRange.value[0])
        const end = new Date(customDateRange.value[1])
        const diffTime = Math.abs(end - start)
        weeks = Math.ceil(diffTime / (1000 * 60 * 60 * 24 * 7))
        weeks = Math.max(1, Math.min(weeks, 12)) // 限制在1-12周
      } else {
        weeks = 4
      }
      break
  }
  
  // 生成周标签
  for (let i = weeks - 1; i >= 0; i--) {
    const weekStart = new Date(now)
    weekStart.setDate(now.getDate() - (i * 7))
    const weekEnd = new Date(weekStart)
    weekEnd.setDate(weekStart.getDate() + 6)
    
    const startStr = weekStart.toLocaleDateString('zh-CN', { 
      month: '2-digit', 
      day: '2-digit' 
    })
    const endStr = weekEnd.toLocaleDateString('zh-CN', { 
      month: '2-digit', 
      day: '2-digit' 
    })
    
    labels.push(`${startStr}-${endStr}`)
  }
  
  return labels
}

// 生成月趋势标签
const generateMonthlyLabels = () => {
  const labels = []
  const now = new Date()
  let months = 6 // 默认显示6个月
  
  // 根据时间范围确定月数
  switch (timeRange.value) {
    case 'today':
    case 'week':
      months = 1
      break
    case 'month':
      months = 1
      break
    case 'custom':
      if (customDateRange.value && customDateRange.value.length === 2) {
        const start = new Date(customDateRange.value[0])
        const end = new Date(customDateRange.value[1])
        const diffMonths = (end.getFullYear() - start.getFullYear()) * 12 + (end.getMonth() - start.getMonth())
        months = Math.max(1, Math.min(diffMonths + 1, 12)) // 限制在1-12个月
      } else {
        months = 6
      }
      break
  }
  
  // 生成月标签
  for (let i = months - 1; i >= 0; i--) {
    const month = new Date(now)
    month.setMonth(now.getMonth() - i)
    labels.push(month.toLocaleDateString('zh-CN', { 
      year: 'numeric',
      month: '2-digit' 
    }))
  }
  
  return labels
}

// 获取播放量趋势图的时间轴标签
const getViewChartLabels = () => {
  switch (viewChartType.value) {
    case 'daily':
      return generateDailyLabels()
    case 'weekly':
      return generateWeeklyLabels()
    case 'monthly':
      return generateMonthlyLabels()
    default:
      return generateDailyLabels()
  }
}

// 生成不同粒度的播放量数据
const generateViewChartData = (count) => {
  const data = []
  
  // 根据图表类型生成不同量级的数据
  let min = 10000
  let max = 50000
  
  switch (viewChartType.value) {
    case 'daily':
      min = 10000
      max = 50000
      break
    case 'weekly':
      min = 70000
      max = 350000
      break
    case 'monthly':
      min = 300000
      max = 1500000
      break
  }
  
  for (let i = 0; i < count; i++) {
    // 添加一些趋势，让数据看起来更真实
    const trend = Math.sin(i * 0.5) * (max - min) * 0.2
    const baseValue = min + (max - min) * 0.5
    const randomFactor = (Math.random() - 0.5) * (max - min) * 0.3
    const value = Math.floor(baseValue + trend + randomFactor)
    data.push(Math.max(min * 0.5, Math.min(max * 1.5, value)))
  }
  
  return data
}

// ==================== 图表初始化函数 ====================

// 初始化图表
const initChart = (chartRef, option) => {
  if (!chartRef.value) return null
  
  const chart = echarts.init(chartRef.value)
  chart.setOption(option)
  return chart
}

// 初始化所有图表
const initAllCharts = () => {
  // 播放量趋势图
  viewChartInstance = initChart(viewChartRef, getViewChartOption())
  
  // 用户互动分析图
  interactionChartInstance = initChart(interactionChartRef, getInteractionChartOption())
  
  // 用户增长趋势图
  userGrowthChartInstance = initChart(userGrowthChartRef, getUserGrowthChartOption())
  
  // 视频分类分布图
  categoryChartInstance = initChart(categoryChartRef, getCategoryChartOption())
  
  // 用户活跃时段图
  activePeriodChartInstance = initChart(activePeriodChartRef, getActivePeriodChartOption())
}

// ==================== 图表配置函数 ====================

// 获取播放量趋势图配置（已修复时间轴问题）
const getViewChartOption = (isZoom = false) => {
  const labels = getViewChartLabels()
  const data = generateViewChartData(labels.length)
  
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 22, 24, 0.9)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#fff' },
      formatter: (params) => {
        const name = params[0].name
        const value = params[0].value
        let unit = '日'
        
        if (viewChartType.value === 'weekly') unit = '周'
        if (viewChartType.value === 'monthly') unit = '月'
        
        return `${unit}度数据: ${name}<br/>播放量: ${formatNumber(value)}`
      }
    },
    grid: {
      left: isZoom ? '5%' : '3%',
      right: isZoom ? '5%' : '4%',
      bottom: isZoom ? '12%' : '3%',
      top: isZoom ? '15%' : '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
        interval: isZoom ? 0 : (index) => {
          const total = labels.length
          if (total <= 7) return 0  // 7个以内全部显示
          if (total <= 15) return Math.floor(total / 3) - 1
          return Math.floor(total / 5)
        },
        // 旋转标签避免重叠
        rotate: labels.length > 10 ? (isZoom ? 30 : 45) : 0,
        fontSize: isZoom ? 12 : 10
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
        fontSize: isZoom ? 12 : 10,
        formatter: (value) => {
          if (value >= 1000000) return (value / 1000000).toFixed(1) + 'M'
          if (value >= 10000) return (value / 10000).toFixed(1) + 'w'
          if (value >= 1000) return (value / 1000).toFixed(0) + 'k'
          return value
        }
      },
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
    },
    series: [
      {
        name: '播放量',
        type: 'line',
        smooth: true,
        symbol: viewChartType.value === 'monthly' ? 'none' : 'circle',
        symbolSize: isZoom ? 8 : (viewChartType.value === 'monthly' ? 0 : 6),
        lineStyle: {
          width: isZoom ? 4 : 3,
          color: '#FE2C55'
        },
        itemStyle: {
          color: '#FE2C55'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(254, 44, 85, 0.3)'
            }, {
              offset: 1, color: 'rgba(254, 44, 85, 0.05)'
            }]
          }
        },
        data: data
      }
    ]
  }
}

// 获取用户互动分析图配置
const getInteractionChartOption = (isZoom = false) => {
  if (interactionType.value === 'distribution') {
    return {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(22, 22, 24, 0.9)',
        borderColor: 'rgba(255, 255, 255, 0.1)',
        textStyle: { color: '#fff' },
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: isZoom ? '5%' : 10,
        top: 'center',
        textStyle: { 
          color: 'rgba(255, 255, 255, 0.7)',
          fontSize: isZoom ? 12 : 10
        }
      },
      series: [
        {
          name: '互动类型',
          type: 'pie',
          radius: isZoom ? ['30%', '70%'] : ['40%', '70%'],
          center: isZoom ? ['35%', '50%'] : ['40%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#161618',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {d}%',
            color: 'rgba(255, 255, 255, 0.9)',
            fontSize: isZoom ? 12 : 10
          },
          emphasis: {
            label: {
              show: true,
              fontSize: isZoom ? 16 : 14,
              fontWeight: 'bold'
            }
          },
          data: [
            { value: 45678, name: '评论', itemStyle: { color: '#FF9500' } },
            { value: 234567, name: '点赞', itemStyle: { color: '#25F4EE' } },
            { value: 12345, name: '分享', itemStyle: { color: '#00C864' } },
            { value: 3456, name: '收藏', itemStyle: { color: '#409EFF' } }
          ]
        }
      ]
    }
  } else {
    const labels = generateDailyLabels().slice(-7) // 最近7天
    return {
      backgroundColor: 'transparent',
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: 'rgba(22, 22, 24, 0.9)',
        borderColor: 'rgba(255, 255, 255, 0.1)',
        textStyle: { color: '#fff' }
      },
      legend: {
        data: ['点赞', '评论', '分享'],
        textStyle: { 
          color: 'rgba(255, 255, 255, 0.7)',
          fontSize: isZoom ? 12 : 10
        },
        top: 10
      },
      grid: {
        left: isZoom ? '5%' : '3%',
        right: isZoom ? '5%' : '4%',
        bottom: isZoom ? '12%' : '3%',
        top: isZoom ? '15%' : '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: labels,
        axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
        axisLabel: { 
          color: 'rgba(255, 255, 255, 0.7)',
          fontSize: isZoom ? 12 : 10
        }
      },
      yAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
        axisLabel: { 
          color: 'rgba(255, 255, 255, 0.7)',
          fontSize: isZoom ? 12 : 10
        },
        splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
      },
      series: [
        {
          name: '点赞',
          type: 'bar',
          stack: 'total',
          barWidth: isZoom ? '70%' : '60%',
          itemStyle: { color: '#25F4EE' },
          data: generateRandomData(7, 1000, 5000)
        },
        {
          name: '评论',
          type: 'bar',
          stack: 'total',
          barWidth: isZoom ? '70%' : '60%',
          itemStyle: { color: '#FF9500' },
          data: generateRandomData(7, 200, 1000)
        },
        {
          name: '分享',
          type: 'bar',
          stack: 'total',
          barWidth: isZoom ? '70%' : '60%',
          itemStyle: { color: '#00C864' },
          data: generateRandomData(7, 50, 500)
        }
      ]
    }
  }
}

// 获取用户增长趋势图配置
const getUserGrowthChartOption = (isZoom = false) => {
  const labels = generateDailyLabels()
  const colors = {
    new: '#FE2C55',
    active: '#25F4EE',
    total: '#00C864'
  }
  
  let data = []
  if (userGrowthType.value === 'new') {
    data = generateRandomData(labels.length, 100, 1000)
  } else if (userGrowthType.value === 'active') {
    data = generateRandomData(labels.length, 5000, 20000)
  } else {
    data = Array.from({ length: labels.length }, (_, i) => 10000 + i * 1000)
  }
  
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 22, 24, 0.9)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#fff' },
      formatter: (params) => {
        return `${params[0].name}<br/>${params[0].seriesName}: ${formatNumber(params[0].value)}`
      }
    },
    grid: {
      left: isZoom ? '5%' : '3%',
      right: isZoom ? '5%' : '4%',
      bottom: isZoom ? '12%' : '3%',
      top: isZoom ? '15%' : '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
        interval: isZoom ? 0 : 4,
        fontSize: isZoom ? 12 : 10
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
        fontSize: isZoom ? 12 : 10,
        formatter: (value) => {
          if (value >= 10000) return (value / 10000).toFixed(1) + 'w'
          return value
        }
      },
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
    },
    series: [
      {
        name: userGrowthType.value === 'new' ? '新增用户' : 
              userGrowthType.value === 'active' ? '活跃用户' : '累计用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: isZoom ? 8 : 6,
        lineStyle: {
          width: isZoom ? 4 : 3,
          color: colors[userGrowthType.value]
        },
        itemStyle: {
          color: colors[userGrowthType.value]
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: colors[userGrowthType.value] + '40'
            }, {
              offset: 1, color: colors[userGrowthType.value] + '05'
            }]
          }
        },
        data: data
      }
    ]
  }
}

// 获取视频分类分布图配置
const getCategoryChartOption = (isZoom = false) => {
  const categories = ['生活', '娱乐', '知识', '游戏', '音乐']
  const data = [
    { value: 1560, name: '生活' },
    { value: 1240, name: '娱乐' },
    { value: 980, name: '知识' },
    { value: 760, name: '游戏' },
    { value: 540, name: '音乐' }
  ]
  
  const colors = ['#FE2C55', '#25F4EE', '#FF9500', '#00C864', '#AF52DE']
  
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(22, 22, 24, 0.9)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#fff' },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: isZoom ? '5%' : 10,
      top: 'center',
      textStyle: { 
        color: 'rgba(255, 255, 255, 0.7)',
        fontSize: isZoom ? 12 : 10
      }
    },
    series: [
      {
        name: '分类分布',
        type: 'pie',
        radius: isZoom ? ['30%', '70%'] : ['40%', '70%'],
        center: isZoom ? ['35%', '50%'] : ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#161618',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: isZoom ? '{b}\n{c} ({d}%)' : '{b}\n{c} ({d}%)',
          color: 'rgba(255, 255, 255, 0.9)',
          fontSize: isZoom ? 12 : 10
        },
        emphasis: {
          label: {
            show: true,
            fontSize: isZoom ? 16 : 14,
            fontWeight: 'bold'
          }
        },
        data: data.map((item, index) => ({
          ...item,
          itemStyle: { color: colors[index] }
        }))
      }
    ]
  }
}

// 获取用户活跃时段分析图配置（使用固定数据）
const getActivePeriodChartOption = (isZoom = false) => {
  const hours = Array.from({ length: 24 }, (_, i) => `${i.toString().padStart(2, '0')}:00`)
  
  let data = []
  switch (activePeriod.value) {
    case 'weekday':
      data = fixedActivePeriodData.weekday
      break
    case 'weekend':
      data = fixedActivePeriodData.weekend
      break
    case 'all':
      data = fixedActivePeriodData.all
      break
    default:
      data = fixedActivePeriodData.all
  }
  
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 22, 24, 0.9)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#fff' },
      formatter: (params) => {
        const hour = params[0].name
        const value = params[0].value
        return `时间: ${hour}<br/>活跃用户: ${value}人`
      }
    },
    grid: {
      left: isZoom ? '5%' : '3%',
      right: isZoom ? '5%' : '4%',
      bottom: isZoom ? '12%' : '3%',
      top: isZoom ? '15%' : '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
        interval: isZoom ? 0 : 2, // 显示所有小时标签
        fontSize: isZoom ? 11 : 9
      }
    },
    yAxis: {
      type: 'value',
      max: 1000, // 固定Y轴最大值为1000
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
        fontSize: isZoom ? 12 : 10
      },
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
    },
    series: [
      {
        name: '活跃用户',
        type: 'bar',
        barWidth: isZoom ? '85%' : '80%',
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: '#25F4EE'
            }, {
              offset: 1, color: '#FE2C55'
            }]
          }
        },
        data: data,
        markLine: {
          silent: true,
          lineStyle: {
            color: '#FF9500',
            type: 'dashed'
          },
          data: [{
            yAxis: 800,
            label: {
              formatter: '高峰阈值',
              position: 'middle'
            }
          }]
        }
      }
    ]
  }
}

// ==================== 工具函数 ====================

// 生成随机数据
const generateRandomData = (count, min, max) => {
  return Array.from({ length: count }, () => Math.floor(Math.random() * (max - min + 1)) + min)
}

// 更新图表
const updateCharts = () => {
  // 重新初始化播放量趋势图
  if (viewChartInstance) {
    viewChartInstance.dispose()
    viewChartInstance = initChart(viewChartRef, getViewChartOption())
  }
  
  // 更新其他图表
  if (interactionChartInstance) {
    interactionChartInstance.setOption(getInteractionChartOption())
  }
  if (userGrowthChartInstance) {
    userGrowthChartInstance.setOption(getUserGrowthChartOption())
  }
  if (categoryChartInstance) {
    categoryChartInstance.setOption(getCategoryChartOption())
  }
  if (activePeriodChartInstance) {
    activePeriodChartInstance.setOption(getActivePeriodChartOption())
  }
}

// 格式化数字
const formatNumber = (num) => {
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  }
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 获取排名样式
const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return ''
}

// 表格行样式
const tableRowClassName = ({ rowIndex }) => {
  return rowIndex % 2 === 1 ? 'even-row' : ''
}

// ==================== 图表放大功能 ====================

// 打开图表放大弹窗
const openChartZoom = (chartType) => {
  currentZoomChartType.value = chartType
  chartZoomVisible.value = true
  
  // 设置弹窗标题
  const titleMap = {
    viewChart: '播放量趋势',
    interactionChart: '用户互动分析',
    userGrowthChart: '用户增长趋势',
    categoryChart: '视频分类分布',
    activePeriodChart: '用户活跃时段分析'
  }
  zoomChartTitle.value = titleMap[chartType] || '图表详情'
  
  // 等待DOM渲染完成后初始化放大图表
  nextTick(() => {
    initZoomChart()
  })
}

// 初始化放大图表
const initZoomChart = () => {
  if (!zoomChartRef.value) return
  
  // 销毁现有实例
  if (zoomChartInstance) {
    zoomChartInstance.dispose()
  }
  
  // 创建新的图表实例
  zoomChartInstance = echarts.init(zoomChartRef.value)
  
  // 根据图表类型获取配置
  let option = {}
  switch (currentZoomChartType.value) {
    case 'viewChart':
      option = getViewChartOption(true) // true表示是放大模式
      break
    case 'interactionChart':
      option = getInteractionChartOption(true)
      break
    case 'userGrowthChart':
      option = getUserGrowthChartOption(true)
      break
    case 'categoryChart':
      option = getCategoryChartOption(true)
      break
    case 'activePeriodChart':
      option = getActivePeriodChartOption(true)
      break
  }
  
  // 调整一些配置以适应放大模式
  if (option.legend) {
    option.legend.right = '5%'
    option.legend.top = '5%'
  }
  
  if (option.grid) {
    option.grid.top = '15%'
    option.grid.bottom = '15%'
    option.grid.left = '5%'
    option.grid.right = '5%'
  }
  
  if (option.series && option.series[0]) {
    // 放大模式下增加线条宽度和符号大小
    if (option.series[0].type === 'line') {
      option.series[0].lineStyle.width = 4
      option.series[0].symbolSize = 10
    }
    // 放大模式下增加柱状图宽度
    if (option.series[0].type === 'bar') {
      option.series[0].barWidth = '70%'
    }
    // 放大模式下增加饼图半径
    if (option.series[0].type === 'pie') {
      option.series[0].radius = ['30%', '80%']
    }
  }
  
  // 设置配置
  zoomChartInstance.setOption(option)
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleZoomChartResize)
}

// 处理放大图表窗口大小变化
const handleZoomChartResize = () => {
  if (zoomChartInstance) {
    zoomChartInstance.resize()
  }
}

// 下载放大图表为图片
const downloadZoomChart = () => {
  if (!zoomChartInstance) {
    ElMessage.warning('图表未加载完成')
    return
  }
  
  try {
    const base64 = zoomChartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#161618'
    })
    
    const link = document.createElement('a')
    link.href = base64
    link.download = `${zoomChartTitle.value}_${new Date().getTime()}.png`
    link.click()
    
    ElMessage.success('图表已下载')
  } catch (error) {
    console.error('下载图表失败:', error)
    ElMessage.error('下载失败')
  }
}

// 切换全屏模式
const toggleFullScreen = () => {
  const dialogElement = document.querySelector('.chart-zoom-dialog .el-dialog')
  
  if (!dialogElement) return
  
  if (!isFullScreen.value) {
    // 进入全屏
    if (dialogElement.requestFullscreen) {
      dialogElement.requestFullscreen()
    } else if (dialogElement.mozRequestFullScreen) {
      dialogElement.mozRequestFullScreen()
    } else if (dialogElement.webkitRequestFullscreen) {
      dialogElement.webkitRequestFullscreen()
    } else if (dialogElement.msRequestFullscreen) {
      dialogElement.msRequestFullscreen()
    }
    isFullScreen.value = true
  } else {
    // 退出全屏
    if (document.exitFullscreen) {
      document.exitFullscreen()
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen()
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen()
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen()
    }
    isFullScreen.value = false
  }
  
  // 监听全屏变化
  document.addEventListener('fullscreenchange', handleFullScreenChange)
  document.addEventListener('webkitfullscreenchange', handleFullScreenChange)
  document.addEventListener('mozfullscreenchange', handleFullScreenChange)
  document.addEventListener('MSFullscreenChange', handleFullScreenChange)
}

// 处理全屏变化
const handleFullScreenChange = () => {
  const isFull = document.fullscreenElement || 
                 document.webkitFullscreenElement || 
                 document.mozFullScreenElement || 
                 document.msFullscreenElement
  
  isFullScreen.value = !!isFull
  
  // 全屏变化时重新调整图表大小
  if (zoomChartInstance) {
    setTimeout(() => {
      zoomChartInstance.resize()
    }, 100)
  }
}

// 刷新放大图表数据
const refreshZoomChart = () => {
  if (!zoomChartInstance) return
  
  // 重新获取数据并更新图表
  switch (currentZoomChartType.value) {
    case 'viewChart':
      zoomChartInstance.setOption(getViewChartOption(true))
      break
    case 'interactionChart':
      zoomChartInstance.setOption(getInteractionChartOption(true))
      break
    case 'userGrowthChart':
      zoomChartInstance.setOption(getUserGrowthChartOption(true))
      break
    case 'categoryChart':
      zoomChartInstance.setOption(getCategoryChartOption(true))
      break
    case 'activePeriodChart':
      zoomChartInstance.setOption(getActivePeriodChartOption(true))
      break
  }
  
  ElMessage.success('图表数据已刷新')
}

// 处理放大弹窗关闭
const handleZoomDialogClose = () => {
  // 移除全屏事件监听
  document.removeEventListener('fullscreenchange', handleFullScreenChange)
  document.removeEventListener('webkitfullscreenchange', handleFullScreenChange)
  document.removeEventListener('mozfullscreenchange', handleFullScreenChange)
  document.removeEventListener('MSFullscreenChange', handleFullScreenChange)
  
  // 如果还在全屏模式，退出全屏
  if (isFullScreen.value) {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    }
    isFullScreen.value = false
  }
  
  // 移除窗口大小变化监听
  window.removeEventListener('resize', handleZoomChartResize)
  
  // 销毁图表实例
  if (zoomChartInstance) {
    zoomChartInstance.dispose()
    zoomChartInstance = null
  }
}

// ==================== 事件处理函数 ====================

// 时间范围改变
const handleTimeRangeChange = () => {
  refreshData()
}

// 自定义日期改变
const handleCustomDateChange = () => {
  if (customDateRange.value) {
    refreshData()
  }
}

// 播放量趋势类型改变
const handleViewChartTypeChange = () => {
  updateCharts()
}

// 互动分析类型改变
const handleInteractionTypeChange = () => {
  updateCharts()
}

// 用户增长类型改变
const handleUserGrowthTypeChange = () => {
  updateCharts()
}

// 分类排序改变
const handleCategorySortChange = () => {
  updateCharts()
}

// 活跃时段改变
const handleActivePeriodChange = () => {
  updateCharts()
}

// 热门视频排序改变
const handleHotVideoSortChange = () => {
  // 排序逻辑已通过计算属性实现
  const sortLabels = {
    views: '播放量',
    likes: '点赞数',
    comments: '评论数',
    interactionRate: '互动率'
  }
  ElMessage.success(`已按${sortLabels[hotVideoSort.value]}排序`)
}

// 导出数据
const exportData = () => {
  ElMessageBox.confirm(
    '确定要导出当前数据吗？',
    '导出数据',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    // 导出排序后的数据
    const exportData = {
      hotVideos: sortedHotVideos.value,
      coreMetrics: coreMetrics.value,
      activePeriodData: fixedActivePeriodData,
      exportTime: new Date().toLocaleString('zh-CN')
    }
    
    // 创建下载链接
    const dataStr = JSON.stringify(exportData, null, 2)
    const dataBlob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(dataBlob)
    
    const link = document.createElement('a')
    link.href = url
    link.download = `数据分析_${new Date().getTime()}.json`
    link.click()
    
    URL.revokeObjectURL(url)
    ElMessage.success('数据导出成功！')
  })
}

// 视频点击
const handleVideoClick = (row) => {
  ElMessageBox.confirm(
    `视频详情：${row.title}<br/>作者：${row.author}<br/>播放量：${formatNumber(row.views)}<br/>发布时间：${row.publishDate}`,
    '视频详情',
    {
      confirmButtonText: '查看详情',
      cancelButtonText: '关闭',
      dangerouslyUseHTMLString: true,
      type: 'info'
    }
  )
}

// 刷新数据
const refreshData = () => {
  loading.value = true
  chartLoading.value = true
  
  // 模拟API调用
  setTimeout(() => {
    // 更新最后更新时间
    const now = new Date()
    lastUpdateTime.value = now.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
    
    // 更新核心指标数据（模拟变化）
    coreMetrics.value = {
      totalViews: coreMetrics.value.totalViews + Math.floor(Math.random() * 10000),
      viewTrend: (Math.random() * 20 - 5).toFixed(1),
      totalLikes: coreMetrics.value.totalLikes + Math.floor(Math.random() * 1000),
      likeTrend: (Math.random() * 15 - 3).toFixed(1),
      totalComments: coreMetrics.value.totalComments + Math.floor(Math.random() * 100),
      commentTrend: (Math.random() * 10 - 2).toFixed(1),
      totalShares: coreMetrics.value.totalShares + Math.floor(Math.random() * 50),
      shareTrend: (Math.random() * 25 - 5).toFixed(1)
    }
    
    // 更新视频数据（模拟变化）
    rawHotVideos.value = rawHotVideos.value.map(video => ({
      ...video,
      views: video.views + Math.floor(Math.random() * 1000),
      likes: video.likes + Math.floor(Math.random() * 100),
      comments: video.comments + Math.floor(Math.random() * 50),
      trend: (Math.random() * 30 - 10).toFixed(1)
    }))
    
    // 更新图表数据
    updateCharts()
    
    loading.value = false
    chartLoading.value = false
    ElMessage.success('数据已刷新')
  }, 1500)
}

// ==================== 生命周期和监听器 ====================

// 监听图表类型变化
watch([viewChartType, timeRange, interactionType, userGrowthType, categorySort, activePeriod], () => {
  // 当这些值变化时，我们已经通过单独的事件处理函数更新图表
  // 这里可以添加其他逻辑
})

// 初始化
onMounted(() => {
  nextTick(() => {
    initAllCharts()
    refreshData()
  })
})

// 销毁图表实例
onUnmounted(() => {
  if (viewChartInstance) {
    viewChartInstance.dispose()
  }
  if (interactionChartInstance) {
    interactionChartInstance.dispose()
  }
  if (userGrowthChartInstance) {
    userGrowthChartInstance.dispose()
  }
  if (categoryChartInstance) {
    categoryChartInstance.dispose()
  }
  if (activePeriodChartInstance) {
    activePeriodChartInstance.dispose()
  }
  
  // 销毁放大图表实例
  if (zoomChartInstance) {
    zoomChartInstance.dispose()
  }
  
  // 移除事件监听
  document.removeEventListener('fullscreenchange', handleFullScreenChange)
  document.removeEventListener('webkitfullscreenchange', handleFullScreenChange)
  document.removeEventListener('mozfullscreenchange', handleFullScreenChange)
  document.removeEventListener('MSFullscreenChange', handleFullScreenChange)
  window.removeEventListener('resize', handleZoomChartResize)
})
</script>

<style lang="less" scoped>
// Less变量定义
@dy-bg-body: #121212;
@dy-bg-container: #161618;
@dy-bg-elevated: #252526;
@dy-bg-hover: #2D2D2D;
@dy-brand-red: #FE2C55;
@dy-brand-cyan: #25F4EE;
@dy-text-primary: rgba(255, 255, 255, 1);
@dy-text-secondary: rgba(255, 255, 255, 0.88);
@dy-text-tertiary: rgba(255, 255, 255, 0.55);
@dy-border-default: 1px solid rgba(255, 255, 255, 0.08);

.admin-data-analysis-container {
  padding: 20px;
  min-height: 100vh;
  background: @dy-bg-body;
  color: @dy-text-primary;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

// 页面头部
.page-header {
  margin-bottom: 24px;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
    
    .header-left {
      h1 {
        font-size: 24px;
        font-weight: 600;
        margin: 0 0 8px 0;
        color: @dy-text-primary;
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-icon {
          color: @dy-brand-red;
        }
      }
      
      .page-subtitle {
        color: @dy-text-tertiary;
        margin: 0;
        font-size: 14px;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
      flex-wrap: wrap;
      
      .date-selector {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }
  }
}

// 核心指标卡片
.core-metrics {
  margin-bottom: 24px;
  
  .metric-card {
    background: @dy-bg-container;
    border: @dy-border-default;
    border-radius: 12px;
    padding: 20px;
    border-left-width: 4px;
    border-left-style: solid;
    transition: all 0.3s ease;
    height: 100%;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
      border-color: @dy-brand-cyan;
    }
    
    .metric-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16px;
      
      .el-icon {
        font-size: 28px;
        color: white;
      }
    }
    
    .metric-info {
      .metric-value {
        font-size: 28px;
        font-weight: 700;
        color: @dy-text-primary;
        margin-bottom: 4px;
        font-family: "DIN Condensed", sans-serif;
      }
      
      .metric-label {
        font-size: 14px;
        color: @dy-text-tertiary;
        margin-bottom: 8px;
      }
      
      .metric-trend {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        font-weight: 500;
        padding: 4px 8px;
        border-radius: 4px;
        
        &.up {
          background: rgba(0, 200, 100, 0.1);
          color: #00C864;
        }
        
        &.down {
          background: rgba(254, 44, 85, 0.1);
          color: #FE2C55;
        }
      }
    }
  }
}

// 图表区域
.charts-section {
  margin-bottom: 24px;
  
  .chart-card {
    background: @dy-bg-container;
    border: @dy-border-default;
    border-radius: 12px;
    padding: 20px;
    height: 100%;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    }
    
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: @dy-text-primary;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-icon {
          color: @dy-brand-cyan;
        }
      }
      
      .chart-actions {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .el-button {
          color: @dy-text-tertiary;
          transition: color 0.2s ease;
          
          &:hover {
            color: @dy-brand-cyan;
          }
        }
      }
      
      .chart-tabs {
        display: flex;
        gap: 8px;
      }
    }
    
    .chart-container {
      height: 320px;
      min-height: 240px;
      
      .chart {
        width: 100% !important;
        height: 100% !important;
        min-height: 200px;
      }
    }
  }
}

// 数据表格
.data-tables {
  margin-bottom: 24px;
  
  .table-card {
    background: @dy-bg-container;
    border: @dy-border-default;
    border-radius: 12px;
    padding: 20px;
    height: 100%;
    
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: @dy-text-primary;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
        
        .el-icon {
          color: #FF9500;
        }
      }
    }
    
    .table-container {
      :deep(.el-table) {
        background: transparent;
        
        .el-table__header-wrapper {
          th {
            background: @dy-bg-elevated;
            border-bottom: @dy-border-default;
            
            .cell {
              color: @dy-text-secondary;
              font-weight: 600;
            }
          }
        }
        
        .el-table__body-wrapper {
          .el-table__row {
            cursor: pointer;
            transition: background-color 0.2s ease;
            
            &:hover {
              background-color: rgba(37, 244, 238, 0.08) !important;
            }
            
            &.even-row {
              background-color: rgba(255, 255, 255, 0.02);
            }
            
            .el-table__cell {
              border-bottom: @dy-border-default;
              
              .cell {
                color: @dy-text-primary;
              }
            }
          }
        }
      }
    }
  }
  
  // 排名单元格样式
  .rank-cell {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 16px;
    margin: 0 auto;
    background: rgba(255, 255, 255, 0.05);
    color: @dy-text-secondary;
    
    &.rank-gold {
      background: linear-gradient(135deg, #FFD700, #FFC107);
      color: #333;
    }
    
    &.rank-silver {
      background: linear-gradient(135deg, #C0C0C0, #A8A8A8);
      color: #333;
    }
    
    &.rank-bronze {
      background: linear-gradient(135deg, #CD7F32, #B06F2E);
      color: white;
    }
  }
  
  // 视频信息样式
  .video-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .video-cover {
      width: 60px;
      height: 40px;
      border-radius: 4px;
      object-fit: cover;
      flex-shrink: 0;
    }
    
    .video-details {
      flex: 1;
      min-width: 0;
      
      .video-title {
        font-size: 14px;
        color: @dy-text-primary;
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .video-author {
        font-size: 12px;
        color: @dy-text-tertiary;
      }
    }
  }
  
  // 计数样式
  .views-count,
  .likes-count {
    font-family: "DIN Condensed", sans-serif;
    font-weight: 500;
  }
  
  .interaction-rate {
    color: @dy-brand-cyan;
    font-weight: 600;
  }
  
  // 视频趋势样式
  .video-trend {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 500;
    padding: 2px 6px;
    border-radius: 4px;
    
    &.up {
      background: rgba(0, 200, 100, 0.1);
      color: #00C864;
    }
    
    &.down {
      background: rgba(254, 44, 85, 0.1);
      color: #FE2C55;
    }
  }
}

// 刷新区域
.refresh-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: @dy-bg-container;
  border: @dy-border-default;
  border-radius: 12px;
  
  .last-update {
    color: @dy-text-tertiary;
    font-size: 14px;
  }
}

// 图表放大弹窗样式
:deep(.chart-zoom-dialog) {
  .el-dialog {
    background: @dy-bg-container;
    border-radius: 12px;
    border: @dy-border-default;
    
    .el-dialog__header {
      background: @dy-bg-elevated;
      border-bottom: @dy-border-default;
      border-radius: 12px 12px 0 0;
      margin-right: 0;
      padding: 20px;
      
      .el-dialog__title {
        color: @dy-text-primary;
        font-weight: 600;
      }
      
      .el-dialog__headerbtn {
        top: 20px;
        right: 20px;
        
        .el-dialog__close {
          color: @dy-text-tertiary;
          
          &:hover {
            color: @dy-text-primary;
          }
        }
      }
    }
    
    .el-dialog__body {
      padding: 0;
    }
  }
}

.zoom-chart-container {
  width: 100%;
  height: 65vh;
  min-height: 400px;
  background: @dy-bg-container;
}

.zoom-chart-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: @dy-bg-elevated;
  border-top: @dy-border-default;
}

// 响应式适配
@media (max-width: 768px) {
  .admin-data-analysis-container {
    padding: 12px;
  }
  
  .page-header {
    .header-content {
      flex-direction: column;
      align-items: stretch;
      
      .header-left,
      .header-right {
        width: 100%;
      }
      
      .header-right {
        flex-direction: column;
        align-items: stretch;
        
        .date-selector {
          flex-direction: column;
          align-items: stretch;
          
          .el-date-picker {
            width: 100% !important;
          }
        }
      }
    }
  }
  
  .chart-card {
    .chart-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
      
      .chart-actions {
        width: 100%;
        justify-content: space-between;
      }
    }
    
    .chart-container {
      height: 240px !important;
    }
  }
  
  .data-tables {
    .video-info {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
    
    .video-trend {
      font-size: 10px;
      padding: 1px 4px;
    }
  }
  
  .zoom-chart-container {
    height: 50vh;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .chart-container {
    height: 280px !important;
  }
  
  .zoom-chart-container {
    height: 55vh;
  }
}
</style>