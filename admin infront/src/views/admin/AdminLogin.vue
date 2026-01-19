<template>
  <!-- 外层容器：复用用户端深色背景+居中布局 -->
  <div class="dy-admin-login-container">
    <!-- 登录卡片：毛玻璃效果+圆角+阴影，对齐用户端卡片风格 -->
    <div class="dy-admin-login-card">
      <div class="login-header">
        <h2 class="login-title">后台管理系统</h2>
        <p class="login-desc">欢迎管理员，登录后可进行系统管理操作</p>
      </div>

      <!-- 登录表单：对齐用户端输入框样式 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入管理员账号"
            class="dy-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入管理员密码"
            class="dy-input"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

       

        <el-form-item class="form-actions">
          <el-checkbox v-model="loginForm.remember" class="remember-checkbox">
            记住密码
          </el-checkbox>
          <a href="#" class="forgot-pwd">忘记密码？</a>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            @click="handleLogin"
            :loading="isLoading"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { User, Lock, ChatDotSquare } from '@element-plus/icons-vue'
import { adminLogin } from '@/api/admin/userApi'
import { setAdminToken, setAdminUserInfo } from '@/utils/adminAuth'

const router = useRouter()
const loginFormRef = ref(null)
const isLoading = ref(false)

// 登录表单数据
const loginForm = ref({
  username: '',
  password: '',
  remember: false
})

// 验证码按钮相关（仅展示，暂不实现倒计时）
const sendBtnDisabled = ref(false)

// 表单校验规则（保留验证码校验，仅改提示）
const loginRules = ref({
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入管理员密码', trigger: 'blur' }]
})

// 初始化
onMounted(() => {
  // 记住密码逻辑（可选）
  const savedUser = localStorage.getItem('admin_saved_user')
  if (savedUser) {
    loginForm.value.username = JSON.parse(savedUser).username
    loginForm.value.remember = true
  }
})

// 发送验证码（仅提示，暂不实现实际发送）
const handleSendCaptcha = () => {
  // 校验账号是否输入（可选）
  if (!loginForm.value.username) {
    ElMessage.warning('请先输入管理员账号')
    return
  }
  
}

// 登录处理（逻辑不变，仅验证码改为短信验证码）
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    isLoading.value = true

    // 调用登录接口（实际项目中替换为真实参数）
    const res = await adminLogin({
      username: loginForm.value.username,
      password: loginForm.value.password,
    })

    // 存储Token和用户信息
    setAdminToken(res.token)
    setAdminUserInfo(res.userInfo)

    // 记住密码
    if (loginForm.value.remember) {
      localStorage.setItem('admin_saved_user', JSON.stringify({
        username: loginForm.value.username
      }))
    } else {
      localStorage.removeItem('admin_saved_user')
    }

    ElMessage.success('登录成功')
    router.push('/admin/home')
  } catch (error) {
    ElMessage.error(error.msg || '登录失败，请检查账号密码')
  } finally {
    isLoading.value = false
  }
}
</script>

<style lang="less" scoped>
// 核心：直接定义Less固定色值
@dy-bg-body: #121212;
@dy-bg-container: #161618;
@dy-bg-elevated: #252526;
@dy-bg-hover: #2D2D2D;
@dy-brand-red: #FE2C55;
@dy-brand-red-hover: #ff476f; 
@dy-brand-red-active: #e8284d;
@dy-text-primary: rgba(255, 255, 255, 1);
@dy-text-secondary: rgba(255, 255, 255, 0.88);
@dy-text-tertiary: rgba(255, 255, 255, 0.55);
@dy-border-default: 1px solid rgba(255, 255, 255, 0.08);

// 登录页外层容器
.dy-admin-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: @dy-bg-body;
  padding: 20px;
}

// 登录卡片
.dy-admin-login-card {
  width: 100%;
  max-width: 420px;
  background: @dy-bg-container;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  padding: 40px;
  backdrop-filter: blur(20px);

  .login-header {
    text-align: center;
    margin-bottom: 32px;

    .login-title {
      font-size: 24px;
      color: @dy-text-primary;
      margin-bottom: 8px;
      font-weight: 600;
    }

    .login-desc {
      font-size: 14px;
      color: @dy-text-tertiary;
    }
  }

  .login-form {
    width: 100%;

    // 输入框样式
    .dy-input {
      height: 48px;
      background: @dy-bg-elevated;
      border: @dy-border-default;
      border-radius: 8px;
      color: @dy-text-primary;

      ::v-deep(.el-input__wrapper) {
        background: transparent;
        border: none;
        box-shadow: none;
      }

      ::v-deep(.el-input__placeholder) {
        color: @dy-text-tertiary;
      }

      ::v-deep(.el-input__prefix) {
        color: @dy-text-secondary;
      }
    }

    // 验证码行布局（适配按钮）
    .captcha-wrap {
      display: flex;
      gap: 12px;

      .captcha-input {
        flex: 1;
      }

      // 发送验证码按钮样式
      .send-captcha-btn {
        width: 120px;
        height: 48px;
        border-radius: 8px;
        background: @dy-bg-elevated;
        border: @dy-border-default;
        color: @dy-text-secondary;
        font-size: 14px;
        cursor: pointer;

        &:hover {
          background: @dy-bg-hover;
          color: @dy-text-primary;
        }

        &:disabled {
          background: @dy-bg-elevated;
          color: @dy-text-tertiary;
          cursor: not-allowed;
        }
      }
    }

    // 记住密码+忘记密码
    .form-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .remember-checkbox {
        ::v-deep(.el-checkbox__label) {
          color: @dy-text-secondary;
          font-size: 14px;
        }
      }

      .forgot-pwd {
        color: @dy-brand-red;
        font-size: 14px;
        text-decoration: none;

        &:hover {
          color: @dy-brand-red-hover;
        }
      }
    }

    // 登录按钮
    .login-btn {
      width: 100%;
      height: 48px;
      background: @dy-brand-red;
      border: none;
      border-radius: 8px;
      font-size: 16px;
      font-weight: 600;
      transition: all 0.2s ease;

      &:hover {
        background: @dy-brand-red-hover;
      }

      &:active {
        background: @dy-brand-red-active;
        transform: scale(0.98);
      }
    }
  }
}
</style>