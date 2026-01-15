<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/userStore'
import * as userApi from '../api/user'

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

onMounted(() => {
  if (userStore.token) {
    fetchProfile()
  }
})
</script>

<template>
  <main class="profile-shell">
    <section class="profile-card">
      <header class="profile-header">
        <button class="avatar-btn" type="button" @click="triggerAvatar">
          <img class="avatar" :src="displayAvatar" alt="avatar" />
          <span class="avatar-tip">点击修改头像</span>
        </button>
        <div class="profile-title">
          <h1>{{ displayName }}</h1>
          <p>ID: {{ displayId }}</p>
          <div class="stats">
            <span>获赞 {{ likeCount }}</span>
            <span>关注 {{ followCount }}</span>
          </div>
        </div>
      </header>

      <p class="signature">{{ signature }}</p>

      <input
        ref="avatarInput"
        type="file"
        accept="image/*"
        class="hidden-input"
        @change="handleAvatarChange"
      />

      <section class="profile-section">
        <h2>资料编辑</h2>
        <el-form label-position="top" class="profile-form">
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
          <el-form-item label="简介">
            <el-input
              v-model="editForm.description"
              type="textarea"
              rows="3"
              placeholder="介绍一下你自己"
            />
          </el-form-item>
          <div class="profile-actions">
            <el-button type="primary" :loading="loading" @click="handleSave">
              保存修改
            </el-button>
          </div>
        </el-form>
      </section>

      <section class="profile-section">
        <h2>账号安全</h2>
        <el-form label-position="top" class="profile-form">
          <el-form-item label="旧密码">
            <el-input v-model="passwordForm.oldPassword" show-password placeholder="请输入旧密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" show-password placeholder="请输入新密码" />
          </el-form-item>
          <div class="profile-actions">
            <el-button :loading="passwordLoading" @click="handleUpdatePassword">
              修改密码
            </el-button>
          </div>
        </el-form>
      </section>

      <div class="logout-block">
        <el-button class="logout" :loading="logoutLoading" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </section>
  </main>
</template>

<style scoped lang="less">
.profile-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24rem 5vw 60rem;
  background: var(--dy-bg-body);
  color: var(--dy-text-primary);
}

.profile-card {
  width: min(720rem, 100%);
  background: rgba(22, 24, 34, 0.92);
  border-radius: 24rem;
  padding: 24rem;
  border: var(--dy-border-default);
  box-shadow: 0 24rem 60rem rgba(0, 0, 0, 0.35);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20rem;
}

.avatar-btn {
  border: none;
  background: transparent;
  padding: 0;
  display: grid;
  justify-items: center;
  gap: 6rem;
  cursor: pointer;
  color: inherit;
}

.avatar {
  width: 84rem;
  height: 84rem;
  border-radius: 50%;
  border: 2rem solid rgba(255, 255, 255, 0.7);
  object-fit: cover;
}

.avatar-tip {
  font-size: 12rem;
  color: var(--dy-text-tertiary);
}

.profile-title h1 {
  margin: 0 0 6rem;
  font-size: 24rem;
}

.profile-title p {
  margin: 0;
  color: var(--dy-text-tertiary);
  font-size: 12rem;
}

.stats {
  display: flex;
  gap: 12rem;
  margin-top: 8rem;
  font-size: 12rem;
  color: #fff;
}

.signature {
  margin: 16rem 0 8rem;
  color: #d4d7e3;
  font-size: 13rem;
}

.profile-section {
  margin-top: 18rem;
  padding-top: 14rem;
  border-top: 1rem solid rgba(255, 255, 255, 0.08);
}

.profile-section h2 {
  margin: 0 0 12rem;
  font-size: 16rem;
}

.profile-form {
  display: grid;
  gap: 8rem;
}

.profile-actions {
  display: flex;
  justify-content: flex-end;
}

.logout-block {
  margin-top: 20rem;
  display: flex;
  justify-content: center;
}

.logout {
  width: 100%;
  background: rgba(254, 44, 85, 0.2);
  color: #fff;
  border: 1rem solid rgba(254, 44, 85, 0.5);
}

.hidden-input {
  display: none;
}

:deep(.el-input__wrapper) {
  border-radius: 12rem;
}

:deep(.el-textarea__inner) {
  border-radius: 12rem;
}

:deep(.el-select__wrapper) {
  border-radius: 12rem;
}

:deep(.el-button--primary) {
  background: var(--dy-brand-red);
  border-color: var(--dy-brand-red);
}
</style>
