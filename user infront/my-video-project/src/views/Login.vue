<script setup>
import { reactive, ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/userStore'
import * as userApi from '../api/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const agree = ref(true)
const loginLoading = ref(false)
const registerLoading = ref(false)
const codeLoading = ref(false)
const isAuthed = ref(false)
const codeCountdown = ref(0)
let codeTimer = null

const loginForm = reactive({
  account: '',
  password: '',
})

const registerForm = reactive({
  username: '',
  password: '',
  phone: '',
  email: '',
  emailCode: '',
})

const handleLogin = async () => {
  if (!loginForm.account || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  if (!agree.value) {
    ElMessage.warning('请先同意用户协议与隐私政策')
    return
  }

  loginLoading.value = true
  try {
    const { data } = await userApi.login({
      account: loginForm.account,
      password: loginForm.password,
    })
    isAuthed.value = data?.code === 200
    if (isAuthed.value) {
      userStore.setAuth(data?.data)
    }
    ElMessage.success(isAuthed.value ? '登录成功' : data?.message || '登录失败')
    if (isAuthed.value) {
      router.push('/home')
    }
  } catch (error) {
    ElMessage.error('登录失败，请检查后端服务')
  } finally {
    loginLoading.value = false
  }
}

const handleSendCode = async () => {
  if (codeCountdown.value > 0) {
    return
  }
  if (!registerForm.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  codeLoading.value = true
  try {
    const { data } = await userApi.sendEmailCode({ email: registerForm.email })
    if (data?.code === 200) {
      ElMessage.success('验证码已发送')
      codeCountdown.value = 60
      codeTimer = window.setInterval(() => {
        codeCountdown.value -= 1
        if (codeCountdown.value <= 0) {
          codeCountdown.value = 0
          window.clearInterval(codeTimer)
          codeTimer = null
        }
      }, 1000)
    } else {
      ElMessage.error(data?.message || '发送失败')
    }
  } catch (error) {
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    codeLoading.value = false
  }
}

const handleRegister = async () => {
  const { username, password, phone, email, emailCode } = registerForm
  if (!username || !password || !phone || !email || !emailCode) {
    ElMessage.warning('请完整填写注册信息')
    return
  }
  if (!agree.value) {
    ElMessage.warning('请先同意用户协议与隐私政策')
    return
  }

  registerLoading.value = true
  try {
    const { data } = await userApi.register({
      username,
      password,
      phone,
      email,
      emailCode,
    })
    if (data?.code === 200) {
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      loginForm.account = email || username
      loginForm.password = password
      router.push('/login')
    } else {
      ElMessage.error(data?.message || '注册失败')
    }
  } catch (error) {
    ElMessage.error('注册失败，请检查后端服务')
  } finally {
    registerLoading.value = false
  }
}

onBeforeUnmount(() => {
  if (codeTimer) {
    window.clearInterval(codeTimer)
    codeTimer = null
  }
})
</script>

<template>
  <main class="login-page">
    <div class="login-bg"></div>
    <div class="login-overlay"></div>

    <section class="login-card">
      <h1 class="text-3xl font-bold text-white text-center mb-6">短视频 Demo</h1>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form label-position="top" class="form-block">
            <el-form-item label="账号（用户名/邮箱/手机号）">
              <el-input v-model="loginForm.account" placeholder="请输入账号" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" placeholder="请输入密码" show-password />
            </el-form-item>
            <div class="agree text-white/70">
              <input id="agree-login" v-model="agree" type="checkbox" />
              <label for="agree-login">我已阅读并同意用户协议与隐私政策</label>
            </div>
            <el-button
              class="submit w-full"
              type="primary"
              :loading="loginLoading"
              @click="handleLogin"
            >
              一键登录
            </el-button>
            <div class="status" :class="{ active: isAuthed }">
              {{ isAuthed ? '已连接后端服务' : '等待后端响应' }}
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form label-position="top" class="form-block">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" placeholder="请输入密码" show-password />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="邮箱验证码">
              <el-input v-model="registerForm.emailCode" placeholder="请输入验证码">
                <template #append>
                  <el-button
                    :loading="codeLoading"
                    :disabled="codeCountdown > 0"
                    class="code-btn"
                    @click="handleSendCode"
                  >
                    {{ codeCountdown > 0 ? `${codeCountdown}s` : '发送验证码' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <div class="agree text-white/70">
              <input id="agree-register" v-model="agree" type="checkbox" />
              <label for="agree-register">我已阅读并同意用户协议与隐私政策</label>
            </div>
            <el-button
              class="submit w-full"
              type="primary"
              :loading="registerLoading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </section>
  </main>
</template>

<style scoped lang="less">
.login-page {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rem;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background-image: url('../assets/img/header-bg.png');
  background-size: cover;
  background-position: center;
}

.login-overlay {
  position: absolute;
  inset: 0;
  z-index: 10;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(6px);
}

.login-card {
  position: relative;
  z-index: 20;
  width: min(460rem, 100%);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(18px);
  padding: 32rem 36rem;
  border-radius: 28rem;
  border: 1rem solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 30rem 70rem rgba(0, 0, 0, 0.45);
}

.form-block {
  display: grid;
  gap: 10rem;
}

.agree {
  display: flex;
  align-items: center;
  gap: 8rem;
  font-size: 12rem;
}

.submit {
  border-radius: 999rem;
  box-shadow: 0 14rem 30rem rgba(255, 77, 120, 0.35);
}

.status {
  margin-top: 10rem;
  font-size: 12rem;
  color: #cfc4b6;
  text-align: center;
}

.status.active {
  color: #7ce0b3;
  font-weight: 600;
}

:deep(.el-tabs__item) {
  color: rgba(255, 255, 255, 0.7);
}

:deep(.el-tabs__item.is-active) {
  color: #ffffff;
}

:deep(.el-tabs__active-bar) {
  background-color: #ffffff;
}

:deep(.el-tabs__nav-wrap::after) {
  background: rgba(255, 255, 255, 0.2);
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.75);
}

:deep(.el-input__wrapper) {
  border-radius: 12rem;
  background: rgba(255, 255, 255, 0.08);
  box-shadow: none;
}

:deep(.el-input__inner) {
  color: #fff;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px rgba(255, 120, 140, 0.7);
}

:deep(.el-button--primary) {
  background: linear-gradient(90deg, #ff4d7e, #ff8f4d);
  border: none;
}

.code-btn {
  color: #ff8f4d;
}
</style>
