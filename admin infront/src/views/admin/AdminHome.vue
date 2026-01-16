<template>
  <div class="admin-home-container">
    <div class="home-card">
      <h2 class="home-title">欢迎进入后台管理系统</h2>
      <p class="admin-info">当前登录管理员：<span>{{ adminName }}</span></p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminUserInfo } from '@/utils/adminAuth'

const adminName = ref('')

// 获取登录管理员名称
const getAdminInfo = () => {
  const userInfo = getAdminUserInfo()
  if (userInfo) {
    adminName.value = userInfo.username
  }
}

onMounted(() => {
  getAdminInfo()
})
</script>

<style lang="less" scoped>
@dy-bg-card: rgba(22, 22, 24, 0.8);
@dy-text-primary: rgba(255, 255, 255, 1);
@dy-text-secondary: rgba(255, 255, 255, 0.88);
@dy-brand-red: #FE2C55;
@dy-border-default: 1px solid rgba(255, 255, 255, 0.08);
@dy-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);

.admin-home-container {
  width: 100%;
  min-height: calc(100vh - 40px);
  display: flex;
  align-items: center;
  justify-content: center;

  .home-card {
    width: 500px;
    padding: 40px 30px;
    background: @dy-bg-card;
    backdrop-filter: blur(12px);
    border: @dy-border-default;
    border-radius: 16px;
    box-shadow: @dy-shadow;
    text-align: center;

    .home-title {
      color: @dy-brand-red;
      font-size: 24px;
      margin-bottom: 20px;
      font-weight: 600;
    }

    .admin-info {
      color: @dy-text-secondary;
      font-size: 16px;
      span {
        color: @dy-text-primary;
        font-weight: 500;
      }
    }
  }
}
</style>