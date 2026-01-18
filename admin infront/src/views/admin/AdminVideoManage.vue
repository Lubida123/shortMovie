<!-- src/views/admin/AdminVideoManage.vue -->
<template>
  <div class="admin-video-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>视频管理</h1>
      <p class="page-subtitle">管理平台视频内容，包括审核、编辑、删除等操作</p>
    </div>

    <!-- 筛选状态提示 -->
    <div class="filter-hint" v-if="hasActiveFilters">
      <div class="hint-content">
        <el-icon><Filter /></el-icon>
        <span class="hint-text">当前筛选条件：</span>
        
        <template v-if="filterCategory">
          <el-tag size="small" class="filter-tag">
            分类：{{ getCategoryName(filterCategory) }}
          </el-tag>
        </template>
        
        <template v-if="filterStatus">
          <el-tag size="small" class="filter-tag" :type="getStatusType(filterStatus)">
            {{ getStatusName(filterStatus) }}
          </el-tag>
        </template>
        
        <template v-if="searchKeyword">
          <el-tag size="small" class="filter-tag">
            关键词："{{ searchKeyword }}"
          </el-tag>
        </template>
        
        <div class="hint-stats">
          共找到 {{ pagination.total }} 个视频
        </div>
        
        <span class="hint-clear" @click="resetFilters">
          <el-icon><Close /></el-icon>
          清除筛选
        </span>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入视频标题或作者名称"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" :icon="Search" @click="handleSearch">
          搜索
        </el-button>
      </div>

      <div class="filter-box">
        <el-select
          v-model="filterStatus"
          placeholder="审核状态"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="全部状态" value="" />
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
          <el-option label="已下架" value="taken_down" />
        </el-select>

        <el-select
          v-model="filterCategory"
          placeholder="视频分类"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="全部分类" value="" />
          <el-option label="生活" value="life" />
          <el-option label="娱乐" value="entertainment" />
          <el-option label="知识" value="knowledge" />
          <el-option label="游戏" value="game" />
          <el-option label="音乐" value="music" />
        </el-select>

        <el-button :icon="Refresh" @click="resetFilters">
          重置
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6" :lg="3">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: rgba(64, 158, 255, 0.1);">
                <el-icon color="#409EFF"><VideoPlay /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalVideos }}</div>
                <div class="stat-label">总视频数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="12" :sm="6" :lg="3">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: rgba(230, 162, 60, 0.1);">
                <el-icon color="#E6A23C"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.pendingVideos }}</div>
                <div class="stat-label">待审核</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="12" :sm="6" :lg="3">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: rgba(103, 194, 58, 0.1);">
                <el-icon color="#67C23A"><Check /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.approvedVideos }}</div>
                <div class="stat-label">已通过</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="12" :sm="6" :lg="3">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: rgba(245, 108, 108, 0.1);">
                <el-icon color="#F56C6C"><Close /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.rejectedVideos }}</div>
                <div class="stat-label">已拒绝</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 批量操作栏 -->
    <div class="batch-operations" v-if="selectedVideos.length > 0">
      <div class="batch-info">
        <el-icon><Select /></el-icon>
        <span>已选择 {{ selectedVideos.length }} 个视频</span>
      </div>
      <div class="batch-actions">
        <el-button type="success" size="small" @click="batchApprove">
          <el-icon><Check /></el-icon>批量通过
        </el-button>
        <el-button type="danger" size="small" @click="batchReject">
          <el-icon><Close /></el-icon>批量拒绝
        </el-button>
        <el-button type="warning" size="small" @click="batchDelete">
          <el-icon><Delete /></el-icon>批量删除
        </el-button>
        <el-button type="text" size="small" @click="clearSelection">
          取消选择
        </el-button>
      </div>
    </div>

    <!-- 视频列表表格 -->
    <div class="table-container">
      <el-table
        :data="paginatedVideoList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        :row-key="row => row.id"
        stripe
        border
        class="video-table-responsive"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        
        <el-table-column label="视频封面" width="120" align="center">
          <template #default="scope">
            <div class="video-cover-container" @click="handleView(scope.row)">
              <img 
                :src="scope.row.coverUrl" 
                alt="封面" 
                class="video-cover"
              />
              <span class="video-duration">{{ formatDuration(scope.row.duration) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="title" label="视频标题" min-width="200" :show-overflow-tooltip="true">
          <template #default="scope">
            <div class="video-title-cell" @click="handleView(scope.row)">
              {{ scope.row.title }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="作者" width="120">
          <template #default="scope">
            <div class="author-info" @click="handleViewAuthor(scope.row)">
              <el-avatar :size="24" :src="scope.row.authorAvatar" />
              <span class="author-name">{{ scope.row.authorName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="category" label="分类" width="100" align="center">
          <template #default="scope">
            <el-tag 
              size="small" 
              class="category-tag"
              :data-category="scope.row.category"
            >
              {{ getCategoryName(scope.row.category) }}
            </el-tag>
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
        
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag 
              :type="getStatusType(scope.row.status)" 
              size="small"
              class="status-tag"
              @click="handleStatusClick(scope.row)"
            >
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="上传时间" width="160" />
        
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleView(scope.row)"
                :icon="View"
                class="action-btn"
              >
                查看
              </el-button>
              
              <div class="audit-buttons" v-if="scope.row.status === 'pending'">
                <el-button 
                  type="success" 
                  size="small" 
                  @click="handleApprove(scope.row)"
                  :icon="Check"
                  class="action-btn"
                >
                  通过
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleReject(scope.row)"
                  :icon="Close"
                  class="action-btn"
                >
                  拒绝
                </el-button>
              </div>
              
              <el-dropdown v-else>
                <el-button size="small" class="action-btn">
                  更多<el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleEdit(scope.row)">
                      <el-icon><Edit /></el-icon>编辑
                    </el-dropdown-item>
                    <el-dropdown-item 
                      @click="handleTakeDown(scope.row)" 
                      divided
                      v-if="scope.row.status === 'approved'"
                    >
                      <el-icon><Delete /></el-icon>下架
                    </el-dropdown-item>
                    <el-dropdown-item 
                      @click="handleRestore(scope.row)" 
                      v-if="scope.row.status === 'taken_down'"
                    >
                      <el-icon><RefreshRight /></el-icon>恢复
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  VideoPlay, 
  Clock, 
  Check, 
  Close, 
  ArrowDown,
  View,
  Edit,
  Delete,
  Refresh,
  Select,
  Filter,
  RefreshRight
} from '@element-plus/icons-vue'
import { getVideoList, getVideoStats } from '@/api/admin/userApi'

// 状态管理
const searchKeyword = ref('')
const filterStatus = ref('')
const filterCategory = ref('')
const loading = ref(false)
const selectedVideos = ref([])

// 分页参数
const pagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 统计数据
const stats = ref({
  totalVideos: 0,
  pendingVideos: 0,
  approvedVideos: 0,
  rejectedVideos: 0,
  todayUploads: 0,
  yesterdayUploads: 0
})

// 原始视频列表数据
const originalVideoList = ref([])

// 计算属性：是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return searchKeyword.value || filterStatus.value || filterCategory.value
})

// 计算属性：根据筛选条件过滤视频列表
const filteredVideoList = computed(() => {
  let result = [...originalVideoList.value]
  
  // 关键词搜索（标题或作者）
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(video => 
      video.title.toLowerCase().includes(keyword) || 
      video.authorName.toLowerCase().includes(keyword)
    )
  }
  
  // 状态筛选
  if (filterStatus.value) {
    result = result.filter(video => video.status === filterStatus.value)
  }
  
  // 分类筛选
  if (filterCategory.value) {
    result = result.filter(video => video.category === filterCategory.value)
  }
  
  return result
})

// 计算属性：分页后的视频列表
const paginatedVideoList = computed(() => {
  const startIndex = (pagination.value.pageNum - 1) * pagination.value.pageSize
  const endIndex = startIndex + pagination.value.pageSize
  return filteredVideoList.value.slice(startIndex, endIndex)
})

// 分类映射
const categoryMap = {
  'life': '生活',
  'entertainment': '娱乐',
  'knowledge': '知识',
  'game': '游戏',
  'music': '音乐'
}

// 状态映射
const statusMap = {
  'pending': { name: '待审核', type: 'warning' },
  'approved': { name: '已通过', type: 'success' },
  'rejected': { name: '已拒绝', type: 'danger' },
  'taken_down': { name: '已下架', type: 'info' }
}

// 获取分类名称
const getCategoryName = (category) => {
  return categoryMap[category] || category
}

// 获取状态名称和类型
const getStatusName = (status) => {
  return statusMap[status]?.name || status
}

const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

// 格式化时长
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 格式化数字
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 更新分页总数
const updatePaginationTotal = () => {
  pagination.value.total = filteredVideoList.value.length
  
  // 如果当前页没有数据，且不是第一页，自动回到第一页
  if (paginatedVideoList.value.length === 0 && pagination.value.pageNum > 1) {
    pagination.value.pageNum = 1
  }
}

// 搜索处理
const handleSearch = () => {
  loading.value = true
  pagination.value.pageNum = 1
  updatePaginationTotal()
  
  setTimeout(() => {
    ElMessage.success(`找到 ${filteredVideoList.value.length} 个视频`)
    loading.value = false
  }, 300)
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  filterCategory.value = ''
  pagination.value.pageNum = 1
  updatePaginationTotal()
  ElMessage.info('已重置所有筛选条件')
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  updatePaginationTotal()
}

const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
}

// 获取视频列表
const fetchVideoList = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value,
      status: filterStatus.value,
      category: filterCategory.value
    }
    
    const res = await getVideoList(params)
    originalVideoList.value = res.list || res || []
    pagination.value.total = res.total || originalVideoList.value.length
    
  } catch (error) {
    console.error('获取视频列表失败:', error)
    // 使用模拟数据作为后备
    originalVideoList.value = generateMockVideos()
    pagination.value.total = originalVideoList.value.length
  } finally {
    loading.value = false
  }
}

// 获取视频统计数据
const fetchVideoStats = async () => {
  try {
    const res = await getVideoStats()
    stats.value = res
  } catch (error) {
    console.error('获取视频统计数据失败:', error)
    stats.value = {
      totalVideos: 1560,
      pendingVideos: 42,
      approvedVideos: 1480,
      rejectedVideos: 38,
      todayUploads: 156,
      yesterdayUploads: 142
    }
  }
}

// 生成模拟视频数据
const generateMockVideos = () => {
  const videos = []
  const categories = ['life', 'entertainment', 'knowledge', 'game', 'music']
  const statuses = ['pending', 'approved', 'rejected', 'taken_down']
  const authors = ['张三', '李四', '王五', '赵六', '钱七']
  const titles = [
    '生活小技巧分享',
    '搞笑短视频合集',
    '编程入门教程',
    '游戏精彩集锦',
    '音乐现场录制',
    '旅行VLOG',
    '美食制作过程',
    '健身教学',
    '科技产品评测',
    '电影解说'
  ]
  
  for (let i = 1; i <= 50; i++) {
    const category = categories[Math.floor(Math.random() * categories.length)]
    videos.push({
      id: i,
      title: `${titles[Math.floor(Math.random() * titles.length)]} ${i}`,
      coverUrl: `https://via.placeholder.com/120x80?text=Video${i}`,
      duration: Math.floor(Math.random() * 600) + 60,
      authorName: authors[Math.floor(Math.random() * authors.length)],
      authorAvatar: `https://via.placeholder.com/40?text=User${Math.floor(Math.random() * 5) + 1}`,
      category: category,
      views: Math.floor(Math.random() * 1000000),
      likes: Math.floor(Math.random() * 100000),
      comments: Math.floor(Math.random() * 10000),
      shares: Math.floor(Math.random() * 5000),
      status: statuses[Math.floor(Math.random() * statuses.length)],
      createTime: `2024-${String(Math.floor(Math.random() * 12) + 1).padStart(2, '0')}-${String(Math.floor(Math.random() * 28) + 1).padStart(2, '0')} ${String(Math.floor(Math.random() * 24)).padStart(2, '0')}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}`
    })
  }
  return videos
}

// 选择处理
const handleSelectionChange = (selection) => {
  selectedVideos.value = selection
}

const clearSelection = () => {
  selectedVideos.value = []
}

// 批量操作
const batchApprove = async () => {
  if (selectedVideos.value.length === 0) {
    ElMessage.warning('请先选择视频')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要通过选中的 ${selectedVideos.value.length} 个视频吗？`,
      '批量通过',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    const ids = selectedVideos.value.map(video => video.id)
    selectedVideos.value.forEach(video => {
      video.status = 'approved'
    })
    
    ElMessage.success(`已成功通过 ${selectedVideos.value.length} 个视频`)
    clearSelection()
  } catch {
    ElMessage.info('已取消批量通过')
  }
}

const batchReject = async () => {
  if (selectedVideos.value.length === 0) {
    ElMessage.warning('请先选择视频')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要拒绝选中的 ${selectedVideos.value.length} 个视频吗？`,
      '批量拒绝',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    selectedVideos.value.forEach(video => {
      video.status = 'rejected'
    })
    
    ElMessage.success(`已成功拒绝 ${selectedVideos.value.length} 个视频`)
    clearSelection()
  } catch {
    ElMessage.info('已取消批量拒绝')
  }
}

const batchDelete = async () => {
  if (selectedVideos.value.length === 0) {
    ElMessage.warning('请先选择视频')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedVideos.value.length} 个视频吗？此操作不可撤销！`,
      '批量删除',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'danger' }
    )
    
    const idsToDelete = selectedVideos.value.map(v => v.id)
    originalVideoList.value = originalVideoList.value.filter(
      video => !idsToDelete.includes(video.id)
    )
    
    ElMessage.success(`已成功删除 ${selectedVideos.value.length} 个视频`)
    clearSelection()
    updatePaginationTotal()
    fetchVideoStats()
  } catch {
    ElMessage.info('已取消批量删除')
  }
}

// 单个操作
const handleView = (video) => {
  ElMessage.info(`查看视频: ${video.title}`)
}

const handleViewAuthor = (video) => {
  ElMessage.info(`查看作者: ${video.authorName}`)
}

const handleStatusClick = (video) => {
  ElMessage.info(`视频状态: ${getStatusName(video.status)}`)
}

const handleApprove = async (video) => {
  try {
    await ElMessageBox.confirm(
      '确定要通过这个视频吗？',
      '通过审核',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    video.status = 'approved'
    ElMessage.success('视频已通过审核')
    fetchVideoStats()
  } catch {
    ElMessage.info('已取消通过操作')
  }
}

const handleReject = async (video) => {
  try {
    await ElMessageBox.confirm(
      '确定要拒绝这个视频吗？',
      '拒绝审核',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    video.status = 'rejected'
    ElMessage.success('视频已拒绝审核')
    fetchVideoStats()
  } catch {
    ElMessage.info('已取消拒绝操作')
  }
}

const handleEdit = (video) => {
  ElMessage.info(`编辑视频: ${video.title}`)
}

const handleTakeDown = async (video) => {
  try {
    await ElMessageBox.confirm(
      '确定要下架这个视频吗？',
      '下架视频',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    video.status = 'taken_down'
    ElMessage.success('视频已下架')
    fetchVideoStats()
  } catch {
    ElMessage.info('已取消下架操作')
  }
}

const handleRestore = async (video) => {
  try {
    await ElMessageBox.confirm(
      '确定要恢复这个视频吗？',
      '恢复视频',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    video.status = 'approved'
    ElMessage.success('视频已恢复')
    fetchVideoStats()
  } catch {
    ElMessage.info('已取消恢复操作')
  }
}

// 监听筛选条件变化，自动更新分页总数
watch([searchKeyword, filterStatus, filterCategory], () => {
  updatePaginationTotal()
})

// 初始化
onMounted(() => {
  fetchVideoList()
  fetchVideoStats()
})
</script>

<style lang="less" scoped>
// Less变量定义（与tokens.css保持一致，但这里直接定义确保生效）
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

// 标签专用颜色（增强对比度）
@dy-tag-warning-bg: rgba(230, 162, 60, 0.3);
@dy-tag-warning-text: #E6C25C;
@dy-tag-success-bg: rgba(103, 194, 58, 0.3);
@dy-tag-success-text: #85D475;
@dy-tag-danger-bg: rgba(245, 108, 108, 0.3);
@dy-tag-danger-text: #FF8A8A;
@dy-tag-info-bg: rgba(144, 147, 153, 0.3);
@dy-tag-info-text: #A8ABB2;

// 分类标签颜色
@dy-tag-life-bg: rgba(64, 158, 255, 0.3);
@dy-tag-life-text: #80C5FF;
@dy-tag-entertainment-bg: rgba(156, 39, 176, 0.3);
@dy-tag-entertainment-text: #C678DD;
@dy-tag-knowledge-bg: rgba(255, 152, 0, 0.3);
@dy-tag-knowledge-text: #FFB74D;
@dy-tag-game-bg: rgba(233, 30, 99, 0.3);
@dy-tag-game-text: #F48FB1;
@dy-tag-music-bg: rgba(0, 188, 212, 0.3);
@dy-tag-music-text: #80DEEA;

.admin-video-manage-container {
  padding: 20px;
  min-height: 100vh;
  background: @dy-bg-body;
  color: @dy-text-primary;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.page-header {
  margin-bottom: 24px;
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px 0;
    color: @dy-brand-red;
  }
  
  .page-subtitle {
    color: @dy-text-tertiary;
    margin: 0;
    font-size: 14px;
  }
}

// 筛选状态提示
.filter-hint {
  background: linear-gradient(to right, rgba(254, 44, 85, 0.1), rgba(37, 244, 238, 0.1));
  border: 1px solid rgba(254, 44, 85, 0.2);
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
  
  .hint-content {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
    
    .el-icon {
      color: @dy-brand-cyan;
      font-size: 16px;
    }
    
    .hint-text {
      color: @dy-text-secondary;
      font-size: 14px;
      margin-right: 8px;
    }
    
    .filter-tag {
      height: 24px;
      line-height: 22px;
      font-size: 12px;
      background: rgba(255, 255, 255, 0.1);
      border: none;
      color: @dy-text-primary;
      
      &.el-tag--warning {
        background: @dy-tag-warning-bg;
        color: @dy-tag-warning-text;
      }
      
      &.el-tag--success {
        background: @dy-tag-success-bg;
        color: @dy-tag-success-text;
      }
      
      &.el-tag--danger {
        background: @dy-tag-danger-bg;
        color: @dy-tag-danger-text;
      }
    }
    
    .hint-stats {
      margin-left: auto;
      color: @dy-brand-cyan;
      font-weight: 500;
      font-size: 14px;
    }
    
    .hint-clear {
      display: flex;
      align-items: center;
      gap: 4px;
      color: @dy-brand-red;
      cursor: pointer;
      font-size: 13px;
      
      &:hover {
        text-decoration: underline;
      }
      
      .el-icon {
        color: @dy-brand-red;
        font-size: 14px;
      }
    }
  }
}

.search-filter-section {
  background: @dy-bg-container;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: @dy-border-default;
  
  .search-box {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    
    .search-input {
      flex: 1;
      
      :deep(.el-input__wrapper) {
        background: @dy-bg-elevated;
        border: @dy-border-default;
        box-shadow: none;
      }
      
      :deep(.el-input__inner) {
        color: @dy-text-primary;
      }
    }
  }
  
  .filter-box {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    
    .filter-select {
      min-width: 120px;
      
      :deep(.el-input__wrapper) {
        background: @dy-bg-elevated;
        border: @dy-border-default;
        box-shadow: none;
      }
    }
  }
}

.stats-cards {
  margin-bottom: 20px;
  
  .stat-card {
    background: @dy-bg-container;
    border: @dy-border-default;
    border-radius: 8px;
    
    :deep(.el-card__body) {
      padding: 16px;
    }
    
    .stat-content {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .el-icon {
          font-size: 24px;
        }
      }
      
      .stat-info {
        .stat-number {
          font-size: 20px;
          font-weight: 600;
          color: @dy-text-primary;
          line-height: 1.2;
          font-family: "DIN Condensed", sans-serif;
        }
        
        .stat-label {
          font-size: 13px;
          color: @dy-text-tertiary;
          margin-top: 4px;
        }
      }
    }
  }
}

.batch-operations {
  background: linear-gradient(to right, rgba(254, 44, 85, 0.1), rgba(37, 244, 238, 0.1));
  border: 1px solid rgba(254, 44, 85, 0.2);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(10px);
  
  .batch-info {
    display: flex;
    align-items: center;
    gap: 8px;
    color: @dy-brand-red;
    font-weight: 500;
  }
  
  .batch-actions {
    display: flex;
    gap: 8px;
  }
}

.table-container {
  background: @dy-bg-container;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: @dy-border-default;
  
  .video-table-responsive {
    // 增强表格行悬停效果
    :deep(.el-table__body) {
      .el-table__row {
        cursor: default;
        
        &:hover {
          background-color: rgba(37, 244, 238, 0.08) !important;
          
          // 悬停时增强文本对比度
          .el-table__cell {
            .cell, span, div, .video-title-cell, .author-name {
              color: rgba(255, 255, 255, 0.95) !important;
            }
            
            // 悬停时数字计数增强
            .views-count, .likes-count {
              color: @dy-brand-cyan !important;
              font-weight: 600;
            }
            
            // 悬停时标签增强
            .status-tag, .category-tag {
              opacity: 0.95;
              transform: translateY(-1px);
              transition: all 0.2s ease;
              box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
            }
          }
        }
        
        // 选中行样式
        &.current-row {
          background-color: rgba(254, 44, 85, 0.15) !important;
          
          .el-table__cell {
            .cell, span, div {
              color: rgba(255, 255, 255, 0.98) !important;
            }
          }
        }
      }
    }
    
    // 修正单元格样式
    :deep(.el-table__cell) {
      color: @dy-text-primary !important;
      background-color: transparent !important;
      padding: 12px 8px !important;
      
      .cell {
        color: @dy-text-primary !important;
      }
    }
    
    // 修正表头样式
    :deep(.el-table__header) {
      th {
        background: @dy-bg-elevated !important;
        border-color: @dy-border-default !important;
        color: @dy-text-primary !important;
        
        .cell {
          font-weight: 600;
          color: @dy-text-secondary !important;
        }
      }
    }
  }
  
  .video-cover-container {
    position: relative;
    width: 100px;
    height: 60px;
    border-radius: 4px;
    overflow: hidden;
    margin: 0 auto;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: scale(1.02);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
      
      .video-cover {
        transform: scale(1.05);
      }
      
      &::after {
        content: '▶';
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 36px;
        height: 36px;
        background: rgba(0, 0, 0, 0.7);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        color: white;
        opacity: 0;
        transition: opacity 0.3s ease;
      }
      
      &:hover::after {
        opacity: 1;
      }
    }
    
    .video-cover {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    
    .video-duration {
      position: absolute;
      bottom: 4px;
      right: 4px;
      background: rgba(0, 0, 0, 0.7);
      color: white;
      font-size: 10px;
      padding: 1px 4px;
      border-radius: 2px;
      font-family: "DIN Condensed", sans-serif;
    }
  }
  
  .video-title-cell {
    cursor: pointer;
    transition: color 0.2s ease;
    
    &:hover {
      color: @dy-brand-cyan !important;
    }
  }
  
  .author-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    
    &:hover {
      .el-avatar {
        transform: scale(1.1);
      }
      
      .author-name {
        color: @dy-brand-cyan !important;
      }
    }
    
    .el-avatar {
      transition: transform 0.2s ease;
    }
    
    .author-name {
      font-size: 13px;
      color: @dy-text-primary;
      transition: color 0.2s ease;
    }
  }
  
  // 分类标签样式（增强对比度）
  .category-tag {
    font-weight: 600 !important;
    font-size: 12px;
    height: 24px;
    line-height: 22px;
    border: none !important;
    min-width: 40px;
    text-align: center;
    transition: all 0.2s ease;
    cursor: default;
    
    // 根据不同分类应用不同颜色
    &[data-category="life"] {
      background: @dy-tag-life-bg !important;
      color: @dy-tag-life-text !important;
      border: 1px solid rgba(64, 158, 255, 0.4) !important;
    }
    
    &[data-category="entertainment"] {
      background: @dy-tag-entertainment-bg !important;
      color: @dy-tag-entertainment-text !important;
      border: 1px solid rgba(156, 39, 176, 0.4) !important;
    }
    
    &[data-category="knowledge"] {
      background: @dy-tag-knowledge-bg !important;
      color: @dy-tag-knowledge-text !important;
      border: 1px solid rgba(255, 152, 0, 0.4) !important;
    }
    
    &[data-category="game"] {
      background: @dy-tag-game-bg !important;
      color: @dy-tag-game-text !important;
      border: 1px solid rgba(233, 30, 99, 0.4) !important;
    }
    
    &[data-category="music"] {
      background: @dy-tag-music-bg !important;
      color: @dy-tag-music-text !important;
      border: 1px solid rgba(0, 188, 212, 0.4) !important;
    }
    
    &:hover {
      opacity: 0.9;
      transform: translateY(-1px);
    }
  }
  
  .views-count,
  .likes-count {
    font-family: "DIN Condensed", "DIN Alternate", sans-serif;
    color: @dy-brand-cyan;
    font-size: 13px;
    font-weight: 500;
    cursor: default;
    
    &:hover {
      color: @dy-brand-red !important;
    }
  }
  
  // 状态标签样式（增强对比度）
  .status-tag {
    font-weight: 600 !important;
    font-size: 12px;
    height: 24px;
    line-height: 22px;
    border: none !important;
    min-width: 60px;
    text-align: center;
    transition: all 0.2s ease;
    cursor: pointer;
    
    // 待审核
    &.el-tag--warning {
      background: @dy-tag-warning-bg !important;
      color: @dy-tag-warning-text !important;
      border: 1px solid rgba(230, 162, 60, 0.4) !important;
      
      &:hover {
        background: rgba(230, 162, 60, 0.4) !important;
        transform: translateY(-1px);
        box-shadow: 0 2px 6px rgba(230, 162, 60, 0.2);
      }
    }
    
    // 已通过
    &.el-tag--success {
      background: @dy-tag-success-bg !important;
      color: @dy-tag-success-text !important;
      border: 1px solid rgba(103, 194, 58, 0.4) !important;
      
      &:hover {
        background: rgba(103, 194, 58, 0.4) !important;
        transform: translateY(-1px);
        box-shadow: 0 2px 6px rgba(103, 194, 58, 0.2);
      }
    }
    
    // 已拒绝
    &.el-tag--danger {
      background: @dy-tag-danger-bg !important;
      color: @dy-tag-danger-text !important;
      border: 1px solid rgba(245, 108, 108, 0.4) !important;
      
      &:hover {
        background: rgba(245, 108, 108, 0.4) !important;
        transform: translateY(-1px);
        box-shadow: 0 2px 6px rgba(245, 108, 108, 0.2);
      }
    }
    
    // 已下架
    &.el-tag--info {
      background: @dy-tag-info-bg !important;
      color: @dy-tag-info-text !important;
      border: 1px solid rgba(144, 147, 153, 0.4) !important;
      
      &:hover {
        background: rgba(144, 147, 153, 0.4) !important;
        transform: translateY(-1px);
        box-shadow: 0 2px 6px rgba(144, 147, 153, 0.2);
      }
    }
  }
  
  .action-buttons {
    display: flex;
    align-items: center;
    gap: 4px;
    
    .audit-buttons {
      display: flex;
      gap: 4px;
    }
    
    .action-btn {
      transition: all 0.2s ease;
      cursor: pointer;
      
      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  
  :deep(.el-pagination) {
    --el-pagination-text-color: @dy-text-secondary;
    --el-pagination-button-disabled-bg-color: @dy-bg-elevated;
    --el-pagination-bg-color: @dy-bg-container;
    --el-pagination-button-bg-color: @dy-bg-elevated;
    
    .btn-prev, .btn-next {
      cursor: pointer;
      
      &:hover:not(:disabled) {
        background-color: rgba(37, 244, 238, 0.1) !important;
      }
    }
    
    .el-pager li {
      background: @dy-bg-elevated;
      color: @dy-text-secondary;
      cursor: pointer;
      
      &:hover {
        color: @dy-brand-cyan !important;
        background-color: rgba(37, 244, 238, 0.1) !important;
      }
      
      &.active {
        background: @dy-brand-red;
        color: white;
        cursor: default;
      }
    }
    
    .el-pagination__jump {
      .el-input__wrapper {
        background: @dy-bg-elevated;
        border: @dy-border-default;
        
        .el-input__inner {
          color: @dy-text-secondary;
        }
      }
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .admin-video-manage-container {
    padding: 12px;
  }
  
  .filter-hint {
    padding: 10px;
    
    .hint-content {
      flex-direction: column;
      align-items: flex-start;
      gap: 6px;
      
      .hint-stats {
        margin-left: 0;
      }
      
      .hint-clear {
        align-self: flex-end;
      }
    }
  }
  
  .search-filter-section {
    padding: 16px;
    
    .search-box {
      flex-direction: column;
      
      .search-input {
        width: 100%;
      }
    }
    
    .filter-box {
      flex-direction: column;
      
      .filter-select {
        min-width: 100%;
      }
    }
  }
  
  .table-container {
    padding: 12px;
    
    .video-cover-container {
      width: 80px;
      height: 48px;
    }
    
    .action-buttons {
      flex-wrap: wrap;
      justify-content: center;
      
      .audit-buttons {
        flex-direction: column;
        width: 100%;
        
        .el-button {
          width: 100%;
          margin: 2px 0;
        }
      }
    }
  }
  
  // 移动端标签样式调整
  .category-tag, .status-tag {
    font-size: 11px !important;
    height: 22px !important;
    line-height: 20px !important;
    min-width: 36px !important;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .table-container {
    .video-cover-container {
      width: 90px;
      height: 54px;
    }
  }
}
</style>