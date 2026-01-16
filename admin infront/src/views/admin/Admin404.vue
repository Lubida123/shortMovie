<template>
  <div class="admin-404-container">
    <el-card class="admin-404-card">
      <div class="content">
        <h1 class="code">404</h1>
        <p class="desc">抱歉，该管理员页面不存在</p>
        <el-button type="primary" @click="goToLogin">返回登录页</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const goToLogin = () => router.push('/admin/login')
</script>

<style lang="less" scoped>
.admin-404-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;

  .admin-404-card {
    width: 400px;
    text-align: center;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    .content {
      padding: 40px;

      .code {
        font-size: 72px;
        color: #e54d42;
        margin: 20px 0;
      }

      .desc {
        font-size: 16px;
        color: #666;
        margin-bottom: 30px;
      }
    }
  }
}
</style>