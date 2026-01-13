<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const loginForm = reactive({
  username: '',
  password: '',
})
const loginLoading = ref(false)
const isAuthed = ref(false)

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  loginLoading.value = true
  try {
    const { data } = await http.get('/login', {
      params: {
        username: loginForm.username,
        password: loginForm.password,
      },
    })
    isAuthed.value = data?.code === 200
    ElMessage.success(isAuthed.value ? '登录成功' : data?.msg || '登录失败，请稍后重试')
  } catch (error) {
    ElMessage.error('登录失败，请检查后端服务')
  } finally {
    loginLoading.value = false
  }
}
</script>

<template>
  <section class="login-panel">
    <header>
      <h2>登录</h2>
      <p>对接后端 UserInfoController 的登录接口。</p>
    </header>
    <el-form label-position="top">
      <el-form-item label="账号">
        <el-input v-model="loginForm.username" placeholder="请输入账号" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input
          v-model="loginForm.password"
          placeholder="请输入密码"
          show-password
        />
      </el-form-item>
      <div class="login-actions">
        <el-button type="primary" :loading="loginLoading" @click="handleLogin">
          登录
        </el-button>
        <span class="status" :class="{ active: isAuthed }">
          {{ isAuthed ? '已连接后端' : '等待后端响应' }}
        </span>
      </div>
    </el-form>
    <footer>
      <span>GET /user/login</span>
      <span>baseURL: /api/user</span>
    </footer>
  </section>
</template>

<style scoped>
.login-panel {
  display: grid;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
}

.login-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status {
  color: #8a6b4e;
  font-size: 0.85rem;
}

.status.active {
  color: #1c7c54;
  font-weight: 600;
}

footer {
  display: flex;
  gap: 12px;
  font-size: 0.8rem;
  color: #6b6b6b;
}
</style>
