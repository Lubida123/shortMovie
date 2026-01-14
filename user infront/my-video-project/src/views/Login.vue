<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const router = useRouter()
const activeTab = ref('login')
const agree = ref(true)
const loginLoading = ref(false)
const registerLoading = ref(false)
const codeLoading = ref(false)
const isAuthed = ref(false)

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
    const { data } = await http.post('/login', null, {
      params: {
        account: loginForm.account,
        password: loginForm.password,
      },
    })
    isAuthed.value = data?.code === 200
    ElMessage.success(isAuthed.value ? '登录成功' : data?.msg || '登录失败')
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
  if (!registerForm.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  codeLoading.value = true
  try {
    const { data } = await http.post('/send-code', null, {
      params: {
        email: registerForm.email,
      },
    })
    ElMessage.success(data?.code === 200 ? '验证码已发送' : data?.msg || '发送失败')
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
    const { data } = await http.post('/register', null, {
      params: {
        username,
        password,
        phone,
        email,
        emailCode,
      },
    })
    if (data?.code === 200) {
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      router.push('/login')
    } else {
      ElMessage.error(data?.msg || '注册失败')
    }
  } catch (error) {
    ElMessage.error('注册失败，请检查后端服务')
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <main class="login-shell">
    <header class="login-header">
      <router-link to="/home" class="close">
        <img src="../assets/img/icon/close-white.png" alt="close" />
      </router-link>
      <div class="header-content">
        <img class="logo" src="../assets/logo.png" alt="Spark Movie" />
        <h1>登录 / 注册</h1>
        <p>认证服务由中国移动提供</p>
      </div>
    </header>

    <section class="login-content">
      <div class="login-card">
        <div class="tabs">
          <button
            class="tab"
            :class="{ active: activeTab === 'login' }"
            @click="activeTab = 'login'"
          >
            登录
          </button>
          <button
            class="tab"
            :class="{ active: activeTab === 'register' }"
            @click="activeTab = 'register'"
          >
            注册
          </button>
        </div>

        <div v-if="activeTab === 'login'" class="form-block">
          <div class="phone">138****8000</div>
          <el-form label-position="top">
            <el-form-item label="账号（用户名/邮箱/手机号）">
              <el-input v-model="loginForm.account" placeholder="请输入账号" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" placeholder="请输入密码" show-password />
            </el-form-item>
            <div class="agree">
              <input id="agree-login" v-model="agree" type="checkbox" />
              <label for="agree-login">我已阅读并同意用户协议与隐私政策</label>
            </div>
            <el-button
              class="submit"
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
        </div>

        <div v-else class="form-block">
          <el-form label-position="top">
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
                  <el-button :loading="codeLoading" @click="handleSendCode">
                    发送验证码
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <div class="agree">
              <input id="agree-register" v-model="agree" type="checkbox" />
              <label for="agree-register">我已阅读并同意用户协议与隐私政策</label>
            </div>
            <el-button
              class="submit"
              type="primary"
              :loading="registerLoading"
              @click="handleRegister"
            >
              注册并登录
            </el-button>
          </el-form>
        </div>

        <div class="alt-login">
          <span>其他登录方式</span>
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
  width: min(440rem, 100%);
  background: #ffffff;
  color: #12131a;
  border-radius: 20rem;
  padding: 24rem;
  box-shadow: 0 26rem 60rem rgba(0, 0, 0, 0.35);
}

.tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10rem;
  margin-bottom: 16rem;
}

.tab {
  border: none;
  padding: 10rem 0;
  border-radius: 999rem;
  background: #f3f4f8;
  color: #666;
  font-weight: 600;
  cursor: pointer;
}

.tab.active {
  background: var(--primary-btn-color);
  color: #fff;
}

.form-block {
  display: grid;
  gap: 8rem;
}

.phone {
  text-align: center;
  font-size: 18rem;
  letter-spacing: 2rem;
  margin-bottom: 8rem;
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
