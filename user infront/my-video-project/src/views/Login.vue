<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const loginForm = reactive({
  username: '',
  password: '',
})
const agree = ref(true)
const loginLoading = ref(false)
const isAuthed = ref(false)

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('Please enter username and password')
    return
  }
  if (!agree.value) {
    ElMessage.warning('Please accept the agreement first')
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
    ElMessage.success(isAuthed.value ? 'Login success' : data?.msg || 'Login failed')
  } catch (error) {
    ElMessage.error('Login failed, please check backend')
  } finally {
    loginLoading.value = false
  }
}
</script>

<template>
  <main class="login-shell">
    <header class="login-header">
      <router-link to="/" class="close">
        <img src="../assets/img/icon/close-white.png" alt="close" />
      </router-link>
      <div class="header-content">
        <img class="logo" src="../assets/logo.png" alt="Spark Movie" />
        <h1>Login to view friends</h1>
        <p>Verified by China Mobile</p>
      </div>
    </header>

    <section class="login-content">
      <div class="login-card">
        <div class="phone">138****8000</div>
        <el-form label-position="top">
          <el-form-item label="Username">
            <el-input v-model="loginForm.username" placeholder="Enter username" />
          </el-form-item>
          <el-form-item label="Password">
            <el-input v-model="loginForm.password" placeholder="Enter password" show-password />
          </el-form-item>
          <div class="agree">
            <input id="agree" v-model="agree" type="checkbox" />
            <label for="agree">I agree to the user agreement and privacy policy</label>
          </div>
          <el-button
            class="submit"
            type="primary"
            :loading="loginLoading"
            @click="handleLogin"
          >
            One-tap login
          </el-button>
          <div class="status" :class="{ active: isAuthed }">
            {{ isAuthed ? 'Backend connected' : 'Waiting for backend response' }}
          </div>
        </el-form>
        <div class="alt-login">
          <span>Other login methods</span>
          <div class="icons">
            <img src="../assets/img/icon/login/toutiao-round.png" alt="toutiao" />
            <img src="../assets/img/icon/login/qq-round.webp" alt="qq" />
            <img src="../assets/img/icon/login/wechat-round.png" alt="wechat" />
            <img src="../assets/img/icon/login/weibo-round.webp" alt="weibo" />
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped lang="less">
.login-shell {
  min-height: 100vh;
  display: grid;
  grid-template-rows: auto 1fr;
  background: var(--main-bg);
  color: #fff;
  animation: fadeIn 0.6s ease;
}

.login-header {
  background: url("../assets/img/header-bg.png") center/cover no-repeat;
  padding: 18rem 5vw 48rem;
  position: relative;
  min-height: 220rem;
}

.close {
  position: absolute;
  right: 20rem;
  top: 20rem;
  width: 32rem;
  height: 32rem;
  border-radius: 999rem;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.35);
}

.close img {
  width: 14rem;
}

.header-content {
  display: grid;
  gap: 10rem;
  margin-top: 40rem;
}

.logo {
  width: 90rem;
}

.header-content h1 {
  margin: 0;
  font-size: 22rem;
}

.header-content p {
  margin: 0;
  color: #d2d4e1;
}

.login-content {
  display: grid;
  place-items: center;
  padding: 20rem 5vw 60rem;
}

.login-card {
  width: min(420rem, 100%);
  background: #ffffff;
  color: #12131a;
  border-radius: 20rem;
  padding: 24rem;
  box-shadow: 0 26rem 60rem rgba(0, 0, 0, 0.35);
}

.phone {
  text-align: center;
  font-size: 18rem;
  letter-spacing: 2rem;
  margin-bottom: 16rem;
}

.agree {
  display: flex;
  align-items: center;
  gap: 8rem;
  margin-bottom: 16rem;
  font-size: 12rem;
  color: #666;
}

.submit {
  width: 100%;
}

.status {
  margin-top: 10rem;
  font-size: 12rem;
  color: #8a6b4e;
  text-align: center;
}

.status.active {
  color: #1c7c54;
  font-weight: 600;
}

.alt-login {
  margin-top: 18rem;
  text-align: center;
  font-size: 12rem;
  color: #666;
}

.icons {
  margin-top: 12rem;
  display: flex;
  justify-content: center;
  gap: 16rem;
}

.icons img {
  width: 36rem;
  height: 36rem;
}

:deep(.el-input__wrapper) {
  border-radius: 12rem;
}

:deep(.el-button--primary) {
  background: var(--primary-btn-color);
  border-color: var(--primary-btn-color);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8rem);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
