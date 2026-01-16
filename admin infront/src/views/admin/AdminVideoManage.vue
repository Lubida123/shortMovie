<!-- src/views/admin/AdminVideoManage.vue -->
<template>
  <div class="admin-video-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>视频管理</h1>
      <p class="page-subtitle">管理平台视频内容，包括审核、编辑、删除等操作</p>
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
                <div class="stat-number">1,560</div>
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
                <div class="stat-number">42</div>
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
                <div class="stat-number">1,480</div>
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
                <div class="stat-number">38</div>
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
        :data="videoList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        :row-key="row => row.id"
        stripe
        border
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        
        <el-table-column label="视频封面" width="120" align="center">
          <template #default="scope">
            <div class="video-cover-container">
              <img 
                :src="scope.row.coverUrl" 
                alt="封面" 
                class="video-cover"
              />
              <span class="video-duration">{{ formatDuration(scope.row.duration) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="title" label="视频标题" min-width="200" :show-overflow-tooltip="true" />
        
        <el-table-column label="作者" width="120">
          <template #default="scope">
            <div class="author-info">
              <el-avatar :size="24" :src="scope.row.authorAvatar" />
              <span class="author-name">{{ scope.row.authorName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="category" label="分类" width="100" align="center">
          <template #default="scope">
            <el-tag size="small">{{ getCategoryName(scope.row.category) }}</el-tag>
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
            <el-tag :type="getStatusType(scope.row.status)" size="small">
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
              >
                查看
              </el-button>
              
              <div class="audit-buttons" v-if="scope.row.status === 'pending'">
                <el-button 
                  type="success" 
                  size="small" 
                  @click="handleApprove(scope.row)"
                  :icon="Check"
                >
                  通过
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleReject(scope.row)"
                  :icon="Close"
                >
                  拒绝
                </el-button>
              </div>
              
              <el-dropdown v-else>
                <el-button size="small">
                  更多<el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleEdit(scope.row)">
                      <el-icon><Edit /></el-icon>编辑
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleTakeDown(scope.row)" divided>
                      <el-icon><Delete /></el-icon>删除
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
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
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
  Select
} from '@element-plus/icons-vue'

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

// 视频列表数据
const videoList = ref([
  {
    id: 1,
    title: '测试视频标题 1 - 生活',
    coverUrl: 'https://via.placeholder.com/120x80?text=Video1',
    duration: 125,
    authorName: '张三',
    authorAvatar: 'https://via.placeholder.com/40?text=ZS',
    category: 'life',
    views: 15432,
    likes: 1234,
    status: 'pending',
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    title: '测试视频标题 2 - 娱乐',
    coverUrl: 'https://via.placeholder.com/120x80?text=Video2',
    duration: 240,
    authorName: '李四',
    authorAvatar: 'https://via.placeholder.com/40?text=LS',
    category: 'entertainment',
    views: 23456,
    likes: 2345,
    status: 'approved',
    createTime: '2024-01-02 11:00:00'
  },
  {
    id: 3,
    title: '测试视频标题 3 - 知识',
    coverUrl: 'https://via.placeholder.com/120x80?text=Video3',
    duration: 180,
    authorName: '王五',
    authorAvatar: 'https://via.placeholder.com/40?text=WW',
    category: 'knowledge',
    views: 8765,
    likes: 876,
    status: 'rejected',
    createTime: '2024-01-03 12:00:00'
  },
  {
    id: 4,
    title: '测试视频标题 4 - 游戏',
    coverUrl: 'https://via.placeholder.com/120x80?text=Video4',
    duration: 360,
    authorName: '赵六',
    authorAvatar: 'https://via.placeholder.com/40?text=ZL',
    category: 'game',
    views: 45678,
    likes: 4567,
    status: 'approved',
    createTime: '2024-01-04 13:00:00'
  },
  {
    id: 5,
    title: '测试视频标题 5 - 音乐',
    coverUrl: 'https://via.placeholder.com/120x80?text=Video5',
    duration: 210,
    authorName: '钱七',
    authorAvatar: 'https://via.placeholder.com/40?text=QQ',
    category: 'music',
    views: 32145,
    likes: 3214,
    status: 'pending',
    createTime: '2024-01-05 14:00:00'
  }
])

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

// 搜索处理
const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    ElMessage.success('搜索完成')
    loading.value = false
  }, 500)
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  filterCategory.value = ''
  handleSearch()
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  fetchVideoList()
}

const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
  fetchVideoList()
}

// 获取视频列表
const fetchVideoList = () => {
  loading.value = true
  setTimeout(() => {
    pagination.value.total = videoList.value.length
    loading.value = false
  }, 300)
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
  try {
    await ElMessageBox.confirm(
      `确定要通过选中的 ${selectedVideos.value.length} 个视频吗？`,
      '批量通过',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
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
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedVideos.value.length} 个视频吗？此操作不可撤销！`,
      '批量删除',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'danger' }
    )
    ElMessage.success(`已成功删除 ${selectedVideos.value.length} 个视频`)
    clearSelection()
  } catch {
    ElMessage.info('已取消批量删除')
  }
}

// 单个操作
const handleView = (video) => {
  ElMessage.info(`查看视频: ${video.title}`)
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
  } catch {
    ElMessage.info('已取消下架操作')
  }
}

// 初始化
onMounted(() => {
  fetchVideoList()
})
</script>

<style lang="less" scoped>
.admin-video-manage-container {
  padding: 20px;
  min-height: 100vh;
  background: #121212;
  color: rgba(255, 255, 255, 0.88);
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

.page-header {
  margin-bottom: 24px;
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px 0;
    color: #FE2C55;
  }
  
  .page-subtitle {
    color: rgba(255, 255, 255, 0.55);
    margin: 0;
    font-size: 14px;
  }
}

.search-filter-section {
  background: #161618;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  
  .search-box {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    
    .search-input {
      flex: 1;
      
      :deep(.el-input__wrapper) {
        background: #252526;
        border: 1px solid rgba(255, 255, 255, 0.08);
        box-shadow: none;
      }
      
      :deep(.el-input__inner) {
        color: rgba(255, 255, 255, 0.88);
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
        background: #252526;
        border: 1px solid rgba(255, 255, 255, 0.08);
        box-shadow: none;
      }
    }
  }
}

.stats-cards {
  margin-bottom: 20px;
  
  .stat-card {
    background: #161618;
    border: 1px solid rgba(255, 255, 255, 0.08);
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
          color: rgba(255, 255, 255, 0.88);
          line-height: 1.2;
        }
        
        .stat-label {
          font-size: 13px;
          color: rgba(255, 255, 255, 0.55);
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
    color: #FE2C55;
    font-weight: 500;
  }
  
  .batch-actions {
    display: flex;
    gap: 8px;
  }
}

.table-container {
  background: #161618;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  
  :deep(.el-table) {
    background: transparent;
    
    th {
      background: #252526 !important;
      border-color: rgba(255, 255, 255, 0.08) !important;
      color: rgba(255, 255, 255, 0.88) !important;
    }
    
    tr {
      background: transparent !important;
      color: rgba(255, 255, 255, 0.88) !important;
      
      &:hover {
        background: rgba(255, 255, 255, 0.05) !important;
      }
    }
    
    td {
      border-color: rgba(255, 255, 255, 0.08) !important;
    }
  }
  
  .video-cover-container {
    position: relative;
    width: 100px;
    height: 60px;
    border-radius: 4px;
    overflow: hidden;
    margin: 0 auto;
    
    .video-cover {
      width: 100%;
      height: 100%;
      object-fit: cover;
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
  
  .author-info {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .author-name {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.88);
    }
  }
  
  .views-count,
  .likes-count {
    font-family: "DIN Condensed", "DIN Alternate", sans-serif;
    color: #25F4EE;
    font-size: 13px;
  }
  
  .action-buttons {
    display: flex;
    align-items: center;
    gap: 4px;
    
    .audit-buttons {
      display: flex;
      gap: 4px;
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  
  :deep(.el-pagination) {
    --el-pagination-text-color: rgba(255, 255, 255, 0.88);
    --el-pagination-button-disabled-bg-color: #252526;
    --el-pagination-bg-color: #161618;
    --el-pagination-button-bg-color: #252526;
    
    .el-pager li {
      background: #252526;
      color: rgba(255, 255, 255, 0.88);
      
      &:hover {
        color: #FE2C55;
      }
      
      &.active {
        background: #FE2C55;
        color: white;
      }
    }
  }
}
</style>