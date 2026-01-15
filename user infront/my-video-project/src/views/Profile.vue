<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/userStore'
import * as userApi from '../api/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const passwordLoading = ref(false)
const logoutLoading = ref(false)
const avatarInput = ref(null)
const avatarFile = ref(null)
const avatarPreview = ref('')

const profile = ref(null)

const editForm = reactive({
  nickname: '',
  gender: '',
  description: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
})

const displayAvatar = computed(() => {
  if (avatarPreview.value) return avatarPreview.value
  if (profile.value?.avatar) return profile.value.avatar
  if (profile.value?.avatarUrl) return profile.value.avatarUrl
  return new URL('../assets/img/avatar.png', import.meta.url).href
})

const displayName = computed(
  () => profile.value?.nickname || profile.value?.username || '未命名用户'
)

const displayId = computed(() => profile.value?.id || profile.value?.userId || '--')
const followCount = computed(() => profile.value?.followCount ?? 0)
const likeCount = computed(() => profile.value?.likeCount ?? 0)
const signature = computed(() => profile.value?.description || profile.value?.signature || '暂无签名')

const fillProfile = (data) => {
  profile.value = data || null
  editForm.nickname = data?.nickname || data?.username || ''
  editForm.gender = data?.gender || ''
  editForm.description = data?.description || data?.signature || ''
  avatarPreview.value = ''
  avatarFile.value = null
}

const fetchProfile = async () => {
  try {
    const data = await userStore.fetchProfile()
    if (data) {
      fillProfile(data)
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

const triggerAvatar = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  avatarFile.value = file
  avatarPreview.value = URL.createObjectURL(file)
}

const handleSave = async () => {
  if (!editForm.nickname) {
    ElMessage.warning('请输入昵称')
    return
  }
  loading.value = true
  try {
    let payload = {
      nickname: editForm.nickname,
      gender: editForm.gender,
      description: editForm.description,
    }
    if (avatarFile.value) {
      const formData = new FormData()
      Object.entries(payload).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          formData.append(key, value)
        }
      })
      formData.append('avatar', avatarFile.value)
      payload = formData
    }
    const { data } = await userApi.updateUserInfo(payload)
    if (data?.code === 200) {
      ElMessage.success('修改成功')
      await fetchProfile()
    } else {
      ElMessage.error(data?.message || '修改失败')
    }
  } catch (error) {
    ElMessage.error('修改失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleUpdatePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.warning('请填写旧密码和新密码')
    return
  }
  passwordLoading.value = true
  try {
    const { data } = await userApi.updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    if (data?.code === 200) {
      ElMessage.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
    } else {
      ElMessage.error(data?.message || '密码修改失败')
    }
  } catch (error) {
    ElMessage.error('密码修改失败，请稍后重试')
  } finally {
    passwordLoading.value = false
  }
}

const handleLogout = async () => {
  logoutLoading.value = true
  try {
    await userStore.logout()
  } finally {
    logoutLoading.value = false
  }
}

const handleBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/')
}

onMounted(() => {
  if (userStore.token) {
    fetchProfile()
  }
})
</script>

<template>
  <main class="profile-page">
    <div class="profile-bg"></div>
    <div class="profile-overlay"></div>
    <button type="button" class="back-btn" @click="handleBack">←</button>

    <section class="profile-card">
      <div class="profile-header">
        <button type="button" class="avatar-btn" @click="triggerAvatar">
          <el-avatar :size="88" :src="displayAvatar" class="avatar" />
          <span class="avatar-tip">点击修改头像</span>
        </button>
        <div class="profile-meta">
          <h1>{{ displayName }}</h1>
          <p>ID: {{ displayId }}</p>
          <p class="signature">{{ signature }}</p>
        </div>
      </div>

      <div class="stats">
        <div>
          <strong>{{ likeCount }}</strong>
          <span>获赞</span>
        </div>
        <div>
          <strong>{{ followCount }}</strong>
          <span>关注</span>
        </div>
      </div>

      <input
        ref="avatarInput"
        type="file"
        accept="image/*"
        class="hidden"
        @change="handleAvatarChange"
      />

      <section class="section">
        <div class="section-title">
          <h2>资料编辑</h2>
          <el-button type="primary" :loading="loading" @click="handleSave">
            保存修改
          </el-button>
        </div>
        <el-form label-position="top" class="form-grid">
          <el-form-item label="昵称">
            <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="editForm.gender" placeholder="请选择性别">
              <el-option label="保密" value="" />
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
          <el-form-item label="简介" class="full">
            <el-input
              v-model="editForm.description"
              type="textarea"
              rows="3"
              placeholder="介绍一下你自己"
            />
          </el-form-item>
        </el-form>
      </section>

      <section class="section">
        <div class="section-title">
          <h2>账号安全</h2>
          <el-button :loading="passwordLoading" @click="handleUpdatePassword">
            修改密码
          </el-button>
        </div>
        <el-form label-position="top" class="form-grid">
          <el-form-item label="旧密码">
            <el-input v-model="passwordForm.oldPassword" show-password placeholder="请输入旧密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" show-password placeholder="请输入新密码" />
          </el-form-item>
        </el-form>
      </section>

      <div class="logout-row">
        <el-button type="danger" :loading="logoutLoading" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #0f1013;
  color: #fff;
  padding: 80px 20px 80px;
  position: relative;
  overflow: hidden;
}

.profile-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background-image: url('../assets/img/header-bg.png');
  background-size: cover;
  background-position: center;
}

.profile-overlay {
  position: fixed;
  inset: 0;
  z-index: 1;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(6px);
}


.back-btn {
  position: fixed;
  top: 18px;
  left: 18px;
  z-index: 5;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  cursor: pointer;
  display: grid;
  place-items: center;
}

.profile-card {
  max-width: 980px;
  margin: 0 auto;
  background: rgba(18, 20, 30, 0.95);
  border-radius: 20px;
  padding: 24px 28px 28px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 24px 50px rgba(0, 0, 0, 0.45);
  position: relative;
  z-index: 2;
  display: grid;
  justify-items: center;
}

.profile-header {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 20px;
  align-items: center;
  justify-items: center;
  text-align: center;
}

.avatar-btn {
  border: none;
  background: transparent;
  padding: 0;
  display: grid;
  justify-items: center;
  gap: 8px;
  cursor: pointer;
  color: inherit;
}

.avatar {
  border: 3px solid rgba(255, 255, 255, 0.7);
}

.avatar-tip {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
}

.profile-meta h1 {
  margin: 0 0 6px;
  font-size: 24px;
}

.profile-meta p {
  margin: 0;
  color: rgba(255, 255, 255, 0.65);
  font-size: 13px;
}

.signature {
  margin-top: 10px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin: 20px 0 12px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 14px;
  padding: 12px 16px;
  text-align: center;
  width: min(520px, 100%);
}

.stats strong {
  display: block;
  font-size: 18px;
}

.stats span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
}

.section {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  width: min(760px, 100%);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  width: 100%;
}

.section-title h2 {
  margin: 0;
  font-size: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  width: 100%;
}

.form-grid .full {
  grid-column: 1 / -1;
}

.logout-row {
  margin-top: 22px;
  display: flex;
  justify-content: center;
  width: 100%;
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.7);
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  box-shadow: none;
}

:deep(.el-input__inner) {
  color: #fff;
}

:deep(.el-button--primary) {
  background: linear-gradient(90deg, #ff4d7e, #ff8f4d);
  border: none;
}

@media (max-width: 960px) {
  .profile-card {
    margin-top: -40px;
    padding: 20px;
  }

  .profile-header {
    grid-template-columns: 1fr;
    text-align: center;
    justify-items: center;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .logout-row {
    justify-content: center;
  }
}
</style>
