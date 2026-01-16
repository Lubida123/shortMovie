<template>
  <div class="admin-layout-container">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar">
      <div class="sidebar-logo">
        <span>后台管理</span>
      </div>
      <el-menu
        default-active="1"
        class="sidebar-menu"
        background-color="transparent"
        text-color="#fff"
        active-text-color="#FE2C55"
        router
      >
        <el-menu-item index="1" route="/admin/home">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="2" route="/admin/user-manage">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="3" route="/admin/permission-manage">
          <el-icon><Lock /></el-icon>
          <span>权限管理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <main class="admin-main">
      <!-- 顶部导航栏（退出登录） -->
      <header class="admin-header">
        <div class="header-right">
          <el-button 
            type="text" 
            :icon="SwitchButton"
            @click="handleLogout"
            class="logout-btn"
          >
            退出登录
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
// 修复：使用正确的图标名称
import { House, User, Lock, SwitchButton } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { clearAdminStorage } from '@/utils/adminAuth'

const router = useRouter()

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

  // 侧边栏样式
  .admin-sidebar {
    width: 220px;
    background: @dy-bg-sidebar;
    border-right: @dy-border-default;
    height: 100vh;
    overflow: hidden;

    .sidebar-logo {
      height: 60px;
      line-height: 60px;
      text-align: center;
      font-size: 18px;
      font-weight: 600;
      color: @dy-brand-red;
      border-bottom: @dy-border-default;
    }

    .sidebar-menu {
      border-right: none;
      --el-menu-text-color: @dy-text-secondary;
      --el-menu-active-text-color: @dy-brand-red;
      --el-menu-hover-bg-color: rgba(254, 44, 85, 0.1);
      --el-menu-item-height: 50px;
      font-size: 14px;
      margin-top: 10px;
    }
  }

  // 主内容区样式
  .admin-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100vh;

    .admin-header {
      height: 60px;
      background: @dy-bg-header;
      border-bottom: @dy-border-default;
      display: flex;
      justify-content: flex-end;
      align-items: center;
      padding: 0 20px;

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
    }
  }
}
</style>