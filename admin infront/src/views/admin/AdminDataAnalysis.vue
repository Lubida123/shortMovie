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
              <el-select v-model="viewChartType" size="small" style="width: 120px;">
                <el-option label="日趋势" value="daily" />
                <el-option label="周趋势" value="weekly" />
                <el-option label="月趋势" value="monthly" />
              </el-select>
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
              <el-radio-group v-model="interactionType" size="small">
                <el-radio-button label="distribution">分布</el-radio-button>
                <el-radio-button label="comparison">对比</el-radio-button>
              </el-radio-group>
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
              <div class="chart-tabs">
                <el-radio-group v-model="userGrowthType" size="small">
                  <el-radio-button label="new">新增用户</el-radio-button>
                  <el-radio-button label="active">活跃用户</el-radio-button>
                  <el-radio-button label="total">累计用户</el-radio-button>
                </el-radio-group>
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
              <el-select v-model="categorySort" size="small" style="width: 120px;">
                <el-option label="按数量" value="count" />
                <el-option label="按播放量" value="views" />
                <el-option label="按互动量" value="interaction" />
              </el-select>
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
              <el-radio-group v-model="activePeriod" size="small">
                <el-radio-button label="weekday">工作日</el-radio-button>
                <el-radio-button label="weekend">周末</el-radio-button>
                <el-radio-button label="all">全部</el-radio-button>
              </el-radio-group>
            </div>
            <div class="chart-container">
              <div class="chart" ref="activePeriodChartRef" style="width: 100%; height: 100%;"></div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格：热门视频/用户排行榜 -->
    <div class="data-tables">
      <el-row :gutter="20">
        <!-- 热门视频排行榜 -->
        <el-col :xs="24" :lg="12">
          <div class="table-card">
            <div class="table-header">
              <h3><el-icon><Trophy /></el-icon> 热门视频排行榜</h3>
              <div class="table-actions">
                <el-select v-model="hotVideoSort" size="small" style="width: 120px;">
                  <el-option label="按播放量" value="views" />
                  <el-option label="按点赞数" value="likes" />
                  <el-option label="按评论数" value="comments" />
                </el-select>
              </div>
            </div>
            <div class="table-container">
              <el-table
                :data="hotVideos"
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
              </el-table>
            </div>
          </div>
        </el-col>
        
        <!-- 热门创作者排行榜 -->
        <el-col :xs="24" :lg="12">
          <div class="table-card">
            <div class="table-header">
              <h3><el-icon><StarFilled /></el-icon> 热门创作者排行榜</h3>
              <div class="table-actions">
                <el-select v-model="hotCreatorSort" size="small" style="width: 120px;">
                  <el-option label="按粉丝数" value="followers" />
                  <el-option label="按作品数" value="videos" />
                  <el-option label="按互动量" value="interaction" />
                </el-select>
              </div>
            </div>
            <div class="table-container">
              <el-table
                :data="hotCreators"
                style="width: 100%"
                :row-class-name="tableRowClassName"
                @row-click="handleCreatorClick"
              >
                <el-table-column label="排名" width="60" align="center">
                  <template #default="scope">
                    <div class="rank-cell" :class="getRankClass(scope.$index)">
                      {{ scope.$index + 1 }}
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="创作者" min-width="180">
                  <template #default="scope">
                    <div class="creator-info">
                      <el-avatar :size="36" :src="scope.row.avatar" />
                      <div class="creator-details">
                        <div class="creator-name">{{ scope.row.name }}</div>
                        <div class="creator-category">{{ scope.row.category }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column prop="followers" label="粉丝数" width="100" align="center">
                  <template #default="scope">
                    <span class="followers-count">{{ formatNumber(scope.row.followers) }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column prop="videos" label="作品数" width="100" align="center">
                  <template #default="scope">
                    <span class="videos-count">{{ scope.row.videos }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="总获赞" width="100" align="center">
                  <template #default="scope">
                    <span class="total-likes">{{ formatNumber(scope.row.totalLikes) }}</span>
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
  StarFilled,
  Refresh,
  Download,
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
const hotCreatorSort = ref('followers')
const lastUpdateTime = ref('')

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

// 表格数据
const hotVideos = ref([
  { id: 1, cover: 'https://via.placeholder.com/60x40?text=Video1', title: '生活小技巧分享', author: '张三', views: 154321, likes: 12345, comments: 2345, interactionRate: 0.095 },
  { id: 2, cover: 'https://via.placeholder.com/60x40?text=Video2', title: '搞笑短视频合集', author: '李四', views: 143210, likes: 13456, comments: 3456, interactionRate: 0.118 },
  { id: 3, cover: 'https://via.placeholder.com/60x40?text=Video3', title: '编程入门教程', author: '王五', views: 132109, likes: 14567, comments: 4567, interactionRate: 0.145 },
  { id: 4, cover: 'https://via.placeholder.com/60x40?text=Video4', title: '游戏精彩集锦', author: '赵六', views: 121098, likes: 15678, comments: 5678, interactionRate: 0.176 },
  { id: 5, cover: 'https://via.placeholder.com/60x40?text=Video5', title: '音乐现场录制', author: '钱七', views: 110987, likes: 16789, comments: 6789, interactionRate: 0.213 }
])

const hotCreators = ref([
  { id: 1, avatar: 'https://via.placeholder.com/40?text=ZS', name: '张三', category: '生活', followers: 154321, videos: 56, totalLikes: 234567 },
  { id: 2, avatar: 'https://via.placeholder.com/40?text=LS', name: '李四', category: '娱乐', followers: 143210, videos: 43, totalLikes: 198765 },
  { id: 3, avatar: 'https://via.placeholder.com/40?text=WW', name: '王五', category: '知识', followers: 132109, videos: 32, totalLikes: 176543 },
  { id: 4, avatar: 'https://via.placeholder.com/40?text=ZL', name: '赵六', category: '游戏', followers: 121098, videos: 67, totalLikes: 154321 },
  { id: 5, avatar: 'https://via.placeholder.com/40?text=QQ', name: '钱七', category: '音乐', followers: 110987, videos: 45, totalLikes: 143210 }
])

// 生成日期数据
const generateDateLabels = (days) => {
  const labels = []
  const now = new Date()
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    labels.push(date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }))
  }
  return labels
}

// 生成随机数据
const generateRandomData = (count, min, max) => {
  return Array.from({ length: count }, () => Math.floor(Math.random() * (max - min + 1)) + min)
}

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

// 获取播放量趋势图配置
const getViewChartOption = () => {
  const labels = timeRange.value === 'today' 
    ? ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
    : generateDateLabels(timeRange.value === 'week' ? 7 : 30)
  
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 22, 24, 0.9)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#fff' },
      formatter: (params) => {
        return `${params[0].name}<br/>播放量: ${formatNumber(params[0].value)}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { color: 'rgba(255, 255, 255, 0.7)' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
        formatter: (value) => {
          if (value >= 10000) return (value / 10000).toFixed(0) + 'w'
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
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
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
        data: generateRandomData(labels.length, 10000, 50000)
      }
    ]
  }
}

// 获取用户互动分析图配置
const getInteractionChartOption = () => {
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
        right: 10,
        top: 'center',
        textStyle: { color: 'rgba(255, 255, 255, 0.7)' }
      },
      series: [
        {
          name: '互动类型',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['40%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#161618',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {d}%',
            color: 'rgba(255, 255, 255, 0.9)'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
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
    const labels = generateDateLabels(7)
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
        textStyle: { color: 'rgba(255, 255, 255, 0.7)' },
        top: 10
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: labels,
        axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
        axisLabel: { color: 'rgba(255, 255, 255, 0.7)' }
      },
      yAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
        axisLabel: { color: 'rgba(255, 255, 255, 0.7)' },
        splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
      },
      series: [
        {
          name: '点赞',
          type: 'bar',
          stack: 'total',
          barWidth: '60%',
          itemStyle: { color: '#25F4EE' },
          data: generateRandomData(7, 1000, 5000)
        },
        {
          name: '评论',
          type: 'bar',
          stack: 'total',
          barWidth: '60%',
          itemStyle: { color: '#FF9500' },
          data: generateRandomData(7, 200, 1000)
        },
        {
          name: '分享',
          type: 'bar',
          stack: 'total',
          barWidth: '60%',
          itemStyle: { color: '#00C864' },
          data: generateRandomData(7, 50, 500)
        }
      ]
    }
  }
}

// 获取用户增长趋势图配置
const getUserGrowthChartOption = () => {
  const labels = generateDateLabels(30)
  const colors = {
    new: '#FE2C55',
    active: '#25F4EE',
    total: '#00C864'
  }
  
  let data = []
  if (userGrowthType.value === 'new') {
    data = generateRandomData(30, 100, 1000)
  } else if (userGrowthType.value === 'active') {
    data = generateRandomData(30, 5000, 20000)
  } else {
    data = Array.from({ length: 30 }, (_, i) => 10000 + i * 1000)
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
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels,
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
        interval: 4
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
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
        symbolSize: 6,
        lineStyle: {
          width: 3,
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
const getCategoryChartOption = () => {
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
      right: 10,
      top: 'center',
      textStyle: { color: 'rgba(255, 255, 255, 0.7)' }
    },
    series: [
      {
        name: '分类分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#161618',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c} ({d}%)',
          color: 'rgba(255, 255, 255, 0.9)',
          fontSize: 12
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
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

// 获取用户活跃时段分析图配置
const getActivePeriodChartOption = () => {
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  const weekdayData = generateRandomData(24, 1000, 5000)
  const weekendData = generateRandomData(24, 2000, 8000)
  
  let data = []
  if (activePeriod.value === 'weekday') {
    data = weekdayData
  } else if (activePeriod.value === 'weekend') {
    data = weekendData
  } else {
    data = weekdayData.map((val, idx) => (val + weekendData[idx]) / 2)
  }
  
  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(22, 22, 24, 0.9)',
      borderColor: 'rgba(255, 255, 255, 0.1)',
      textStyle: { color: '#fff' },
      formatter: (params) => {
        return `${params[0].name}<br/>活跃用户: ${formatNumber(params[0].value)}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
        interval: 2
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } },
      axisLabel: { 
        color: 'rgba(255, 255, 255, 0.7)',
        formatter: (value) => {
          if (value >= 1000) return (value / 1000).toFixed(0) + 'k'
          return value
        }
      },
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
    },
    series: [
      {
        name: '活跃用户',
        type: 'bar',
        barWidth: '80%',
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
        data: data
      }
    ]
  }
}

// 更新图表
const updateCharts = () => {
  if (viewChartInstance) {
    viewChartInstance.setOption(getViewChartOption())
  }
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
    ElMessage.success('数据导出成功！')
  })
}

// 视频点击
const handleVideoClick = (row) => {
  ElMessage.info(`查看视频: ${row.title}`)
}

// 创作者点击
const handleCreatorClick = (row) => {
  ElMessage.info(`查看创作者: ${row.name}`)
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
    
    // 更新图表数据
    updateCharts()
    
    loading.value = false
    chartLoading.value = false
    ElMessage.success('数据已刷新')
  }, 1500)
}

// 监听图表类型变化
watch([timeRange, viewChartType, interactionType, userGrowthType, categorySort, activePeriod], () => {
  updateCharts()
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
      
      .chart-tabs {
        display: flex;
        gap: 8px;
      }
    }
    
    .chart-container {
      height: 320px;
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
  
  // 创作者信息样式
  .creator-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .el-avatar {
      flex-shrink: 0;
    }
    
    .creator-details {
      flex: 1;
      min-width: 0;
      
      .creator-name {
        font-size: 14px;
        color: @dy-text-primary;
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .creator-category {
        font-size: 12px;
        color: @dy-text-tertiary;
      }
    }
  }
  
  // 计数样式
  .views-count,
  .likes-count,
  .followers-count,
  .videos-count,
  .total-likes {
    font-family: "DIN Condensed", sans-serif;
    font-weight: 500;
  }
  
  .interaction-rate {
    color: @dy-brand-cyan;
    font-weight: 600;
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
      
      h3 {
        width: 100%;
      }
    }
    
    .chart-container {
      height: 240px !important;
    }
  }
  
  .data-tables {
    .video-info,
    .creator-info {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .chart-container {
    height: 280px !important;
  }
}
</style>