<template>
  <div class="admin-layout-container">
    <!-- 移动端菜单按钮 -->
    <div class="mobile-menu-toggle" v-if="isMobile" @click="toggleSidebar">
      <el-icon><Menu /></el-icon>
    </div>
    
    <!-- 侧边栏遮罩 -->
    <div 
      class="sidebar-mask" 
      v-if="isMobile && !sidebarCollapsed" 
      @click="sidebarCollapsed = true"
    ></div>
    
    <!-- 侧边栏 -->
    <aside 
      class="admin-sidebar" 
      :class="{
        'collapsed': isMobile && sidebarCollapsed,
        'mobile-sidebar': isMobile
      }"
    >
      <div class="sidebar-logo">
        <span v-if="!isMobile || !sidebarCollapsed">后台管理</span>
        <span v-else class="logo-collapsed">管</span>
      </div>
      <el-menu
        :default-active="activeIndex"
        class="sidebar-menu"
        background-color="transparent"
        text-color="#fff"
        active-text-color="#FE2C55"
        :router="true"
        :collapse="isMobile && sidebarCollapsed"
        :collapse-transition="false"
        @select="handleMenuSelect"
      >
        <el-menu-item index="1" :route="{ path: '/admin/home' }">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="2" :route="{ path: '/admin/user-manage' }">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="3" :route="{ path: '/admin/video-manage' }">
          <el-icon><VideoPlay /></el-icon>
          <span>视频管理</span>
        </el-menu-item>
        <el-menu-item index="4" :route="{ path: '/admin/permission-manage' }">
          <el-icon><Lock /></el-icon>
          <span>权限管理</span>
        </el-menu-item>
        <el-menu-item index="5" :route="{ path: '/admin/data-analysis' }">
          <el-icon><TrendCharts /></el-icon>
          <span>数据分析</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <main class="admin-main" :class="{ 'mobile-main': isMobile }">
      <!-- 顶部导航栏（退出登录） -->
      <header class="admin-header">
        <div class="header-title" v-if="isMobile">
          <span>{{ currentRouteTitle }}</span>
        </div>
        <div class="header-right">
          <el-button 
            type="text" 
            :icon="SwitchButton"
            @click="handleLogout"
            class="logout-btn"
          >
            <span v-if="!isMobile">退出登录</span>
          </el-button>
        </div>
      </header>

      <!-- 路由视图 -->
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { House, User, Lock, VideoPlay, SwitchButton, Menu, TrendCharts} from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { clearAdminStorage } from '@/utils/adminAuth'


const router = useRouter()
const route = useRoute()

// 响应式断点
const screenWidth = ref(window.innerWidth)
const isMobile = computed(() => screenWidth.value < 768)
const sidebarCollapsed = ref(true) // 移动端默认收起

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 更新屏幕宽度
const updateScreenWidth = () => {
  screenWidth.value = window.innerWidth
  // 桌面端自动展开侧边栏
  if (screenWidth.value >= 768) {
    sidebarCollapsed.value = false
  }
}

// 监听窗口大小变化
onMounted(() => {
  window.addEventListener('resize', updateScreenWidth)
  updateScreenWidth() // 初始化
})

onUnmounted(() => {
  window.removeEventListener('resize', updateScreenWidth)
})

// 当前路由标题
const currentRouteTitle = computed(() => {
  return route.meta?.title || '后台管理'
})

// 计算当前激活的菜单项
const activeIndex = ref('1')

watch(
  () => route.path,
  (newPath) => {
    if (newPath.includes('/admin/home')) {
      activeIndex.value = '1'
    } else if (newPath.includes('/admin/user-manage')) {
      activeIndex.value = '2'
    } else if (newPath.includes('/admin/video-manage')) {
      activeIndex.value = '3'
    } else if (newPath.includes('/admin/permission-manage')) {
      activeIndex.value = '4'
    }else if (newPath.includes('/admin/data-analysis')) {
      activeIndex.value = '5'
    }
    
    // 移动端自动关闭侧边栏
    if (isMobile.value) {
      sidebarCollapsed.value = true
    }
  },
  { immediate: true }
)

// 菜单选择处理
const handleMenuSelect = (index) => {
  console.log('菜单选择:', index)
  // 移动端选择菜单后自动收起
  if (isMobile.value) {
    sidebarCollapsed.value = true
  }
}

// 退出登录逻辑
const handleLogout = () => {
  clearAdminStorage()
  ElMessage.success('退出登录成功')
  router.push('/admin/login')
}
</script>

<style lang="less" scoped>
@dy-bg-body: #121212;
@dy-bg-sidebar: #161618;
@dy-bg-header: #161618;
@dy-bg-content: #121212;
@dy-text-primary: rgba(255, 255, 255, 1);
@dy-text-secondary: rgba(255, 255, 255, 0.88);
@dy-brand-red: #FE2C55;
@dy-border-default: 1px solid rgba(255, 255, 255, 0.08);

.admin-layout-container {
  display: flex;
  min-height: 100vh;
  background: @dy-bg-body;
  color: @dy-text-primary;
  font-family: "PingFang SC", "HarmonyOS Sans", "Microsoft YaHei", sans-serif;
  position: relative;

  // 移动端菜单按钮
  .mobile-menu-toggle {
    position: fixed;
    top: 12px;
    left: 12px;
    z-index: 1001;
    width: 36px;
    height: 36px;
    background: @dy-brand-red;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    
    .el-icon {
      font-size: 20px;
      color: white;
    }
  }

  // 侧边栏遮罩
  .sidebar-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 999;
    backdrop-filter: blur(2px);
  }

  // 侧边栏样式
  .admin-sidebar {
    width: 220px;
    background: @dy-bg-sidebar;
    border-right: @dy-border-default;
    height: 100vh;
    overflow: hidden;
    transition: transform 0.3s ease;
    z-index: 1000;

    &.mobile-sidebar {
      position: fixed;
      top: 0;
      left: 0;
      bottom: 0;
      box-shadow: 2px 0 12px rgba(0, 0, 0, 0.3);
      
      &.collapsed {
        transform: translateX(-100%);
      }
    }

    .sidebar-logo {
      height: 60px;
      line-height: 60px;
      text-align: center;
      font-size: 18px;
      font-weight: 600;
      color: @dy-brand-red;
      border-bottom: @dy-border-default;
      white-space: nowrap;
      overflow: hidden;
      
      .logo-collapsed {
        font-size: 20px;
      }
    }

    .sidebar-menu {
      border-right: none;
      --el-menu-text-color: @dy-text-secondary;
      --el-menu-active-text-color: @dy-brand-red;
      --el-menu-hover-bg-color: rgba(254, 44, 85, 0.1);
      --el-menu-item-height: 50px;
      font-size: 14px;
      margin-top: 10px;
      
      .el-menu-item {
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          background-color: rgba(254, 44, 85, 0.1);
        }
        
        &.is-active {
          background-color: rgba(254, 44, 85, 0.2);
        }
      }
    }
  }

  // 主内容区样式
  .admin-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100vh;
    transition: margin-left 0.3s ease;
    
    &.mobile-main {
      margin-left: 0;
    }

    .admin-header {
      height: 60px;
      background: @dy-bg-header;
      border-bottom: @dy-border-default;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 20px;
      
      .header-title {
        font-size: 18px;
        font-weight: 600;
        color: @dy-text-primary;
      }

      .logout-btn {
        color: @dy-text-secondary;
        font-size: 14px;
        &:hover {
          color: @dy-brand-red;
        }
      }
    }

    .admin-content {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      background: @dy-bg-content;
      
      // 移动端调整内边距
      @media (max-width: 768px) {
        padding: 12px;
      }
    }
  }
  
  // 响应式调整
  @media (max-width: 768px) {
    .admin-sidebar {
      width: 280px;
    }
    
    .admin-main {
      .admin-header {
        height: 50px;
        padding: 0 12px;
        
        .header-title {
          font-size: 16px;
        }
      }
    }
  }
  
  @media (min-width: 769px) and (max-width: 1024px) {
    .admin-sidebar {
      width: 200px;
    }
  }
}
</style>