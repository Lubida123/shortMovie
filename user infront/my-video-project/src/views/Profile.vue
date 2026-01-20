<script setup>
import { ref, reactive, onMounted, computed, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/userStore'
import * as userApi from '../api/user'
import * as videoApi from '../api/video'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const passwordLoading = ref(false)
const logoutLoading = ref(false)
const editDialogVisible = ref(false)
const avatarInput = ref(null)
const avatarFile = ref(null)
const avatarPreview = ref('')
const uploadInput = ref(null)
const videoFile = ref(null)
const videoPreview = ref('')
const videoDuration = ref(0)
const uploadLoading = ref(false)
const myUploads = ref([])
const myUploadsLoading = ref(false)
const uploadsPageNum = ref(1)
const uploadsHasMore = ref(true)
const uploadsPageSize = 12
const likeVideos = ref([])
const collectVideos = ref([])
const likesLoading = ref(false)
const collectsLoading = ref(false)
const likesPageNum = ref(1)
const collectsPageNum = ref(1)
const likesHasMore = ref(true)
const collectsHasMore = ref(true)
const likesPageSize = 12
const collectsPageSize = 12
const activeLibraryTab = ref('likes')

const profile = ref(null)

const editForm = reactive({
  nickname: '',
  phone: '',
  email: '',
  avatar: '',
})

const uploadForm = reactive({
  title: '',
  description: '',
  category: '',
  tags: '',
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

const videoFileName = computed(() => videoFile.value?.name || '未选择视频')
const videoDurationLabel = computed(() =>
  videoDuration.value ? formatDuration(videoDuration.value) : '--'
)

const formatDuration = (seconds) => {
  const value = Number(seconds)
  const safe = Number.isFinite(value) ? value : 0
  const m = Math.floor(safe / 60).toString().padStart(2, '0')
  const s = Math.floor(safe % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

const syncEditForm = (data) => {
  editForm.nickname = data?.nickname || data?.username || ''
  editForm.phone = data?.phone || ''
  editForm.email = data?.email || ''
  editForm.avatar = data?.avatar || data?.avatarUrl || ''
  avatarPreview.value = ''
  avatarFile.value = null
}

const resetUpload = () => {
  videoFile.value = null
  if (videoPreview.value) {
    URL.revokeObjectURL(videoPreview.value)
  }
  videoPreview.value = ''
  videoDuration.value = 0
  uploadForm.title = ''
  uploadForm.description = ''
  uploadForm.category = ''
  uploadForm.tags = ''
}

const extractPageList = (payload) => {
  const pageData = payload?.data ?? payload
  const list = pageData?.records ?? pageData?.list ?? []
  return Array.isArray(list) ? list : []
}

const normalizeLibraryVideo = (item) => {
  if (!item) return null
  return {
    id: item.videoId || item.id,
    title: item.title || item.videoTitle || '未命名视频',
    description: item.description || item.desc || '',
    videoUrl: item.videoUrl || item.url || '',
    coverUrl: item.coverUrl || item.cover || '',
    duration: item.duration || 0,
    authorName: item.authorName || item.author || '',
  }
}

const normalizeMyVideo = (item) => {
  if (!item) return null
  return {
    id: item.videoId || item.id,
    title: item.title || item.videoTitle || '未命名视频',
    description: item.description || item.desc || '',
    videoUrl: item.videoUrl || item.url || '',
    coverUrl: item.coverUrl || item.cover || '',
    duration: item.duration || 0,
    status: item.auditStatus ?? item.status ?? null,
    createdAt: item.createTime || item.createdAt,
  }
}

const fetchMyUploads = async (append = false) => {
  if (!userStore.token) {
    myUploads.value = []
    uploadsHasMore.value = false
    return
  }
  if (!append) {
    uploadsPageNum.value = 1
    uploadsHasMore.value = true
    myUploads.value = []
  }
  if (myUploadsLoading.value || !uploadsHasMore.value) return
  myUploadsLoading.value = true
  try {
    const { data } = await userApi.getMyVideos({
      pageNum: uploadsPageNum.value,
      pageSize: uploadsPageSize,
    })
    if (data?.code === 200) {
      const list = extractPageList(data)
      const mapped = list.map(normalizeMyVideo).filter(Boolean)
      if (append) {
        myUploads.value.push(...mapped)
      } else {
        myUploads.value = mapped
      }
      const pageData = data?.data ?? data
      if (typeof pageData?.pages === 'number') {
        uploadsHasMore.value = uploadsPageNum.value < pageData.pages
      } else {
        uploadsHasMore.value = list.length >= uploadsPageSize
      }
      if (uploadsHasMore.value) {
        uploadsPageNum.value += 1
      }
      return
    }
    ElMessage.error(data?.message || '获取作品失败')
  } catch (error) {
    ElMessage.error('获取作品失败，请稍后重试')
  } finally {
    myUploadsLoading.value = false
  }
}

const loadMoreUploads = () => fetchMyUploads(true)

const fetchLikes = async (append = false) => {
  if (!userStore.token) {
    likeVideos.value = []
    likesHasMore.value = false
    return
  }
  if (!append) {
    likesPageNum.value = 1
    likesHasMore.value = true
    likeVideos.value = []
  }
  if (likesLoading.value || !likesHasMore.value) return
  likesLoading.value = true
  try {
    const { data } = await userApi.getMyLikes({
      pageNum: likesPageNum.value,
      pageSize: likesPageSize,
    })
    if (data?.code === 200) {
      const list = extractPageList(data)
      const mapped = list.map(normalizeLibraryVideo).filter(Boolean)
      if (append) {
        likeVideos.value.push(...mapped)
      } else {
        likeVideos.value = mapped
      }
      const pageData = data?.data ?? data
      if (typeof pageData?.pages === 'number') {
        likesHasMore.value = likesPageNum.value < pageData.pages
      } else {
        likesHasMore.value = list.length >= likesPageSize
      }
      if (likesHasMore.value) {
        likesPageNum.value += 1
      }
      return
    }
    ElMessage.error(data?.message || '获取喜欢失败')
  } catch (error) {
    ElMessage.error('获取喜欢失败，请稍后重试')
  } finally {
    likesLoading.value = false
  }
}

const fetchCollects = async (append = false) => {
  if (!userStore.token) {
    collectVideos.value = []
    collectsHasMore.value = false
    return
  }
  if (!append) {
    collectsPageNum.value = 1
    collectsHasMore.value = true
    collectVideos.value = []
  }
  if (collectsLoading.value || !collectsHasMore.value) return
  collectsLoading.value = true
  try {
    const { data } = await userApi.getMyCollects({
      pageNum: collectsPageNum.value,
      pageSize: collectsPageSize,
    })
    if (data?.code === 200) {
      const list = extractPageList(data)
      const mapped = list.map(normalizeLibraryVideo).filter(Boolean)
      if (append) {
        collectVideos.value.push(...mapped)
      } else {
        collectVideos.value = mapped
      }
      const pageData = data?.data ?? data
      if (typeof pageData?.pages === 'number') {
        collectsHasMore.value = collectsPageNum.value < pageData.pages
      } else {
        collectsHasMore.value = list.length >= collectsPageSize
      }
      if (collectsHasMore.value) {
        collectsPageNum.value += 1
      }
      return
    }
    ElMessage.error(data?.message || '获取收藏失败')
  } catch (error) {
    ElMessage.error('获取收藏失败，请稍后重试')
  } finally {
    collectsLoading.value = false
  }
}

const loadMoreLikes = () => fetchLikes(true)
const loadMoreCollects = () => fetchCollects(true)

const libraryList = computed(() =>
  activeLibraryTab.value === 'likes' ? likeVideos.value : collectVideos.value
)
const libraryLoading = computed(() =>
  activeLibraryTab.value === 'likes' ? likesLoading.value : collectsLoading.value
)
const libraryHasMore = computed(() =>
  activeLibraryTab.value === 'likes' ? likesHasMore.value : collectsHasMore.value
)

const ensureLibraryData = (tab) => {
  if (tab === 'likes' && likeVideos.value.length === 0 && !likesLoading.value) {
    fetchLikes()
  }
  if (tab === 'collects' && collectVideos.value.length === 0 && !collectsLoading.value) {
    fetchCollects()
  }
}

const loadMoreLibrary = () => {
  if (activeLibraryTab.value === 'likes') {
    loadMoreLikes()
  } else {
    loadMoreCollects()
  }
}

const fetchProfile = async () => {
  try {
    const { data } = await userApi.getProfile()
    if (data?.code === 200) {
      profile.value = data?.data || null
      syncEditForm(profile.value)
      fetchMyUploads()
      if (userStore.setUserInfo) {
        userStore.setUserInfo(profile.value)
      }
      return
    }
    ElMessage.error(data?.message || '获取用户信息失败')
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

const openEditDialog = () => {
  syncEditForm(profile.value)
  editDialogVisible.value = true
}

const triggerAvatar = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  avatarFile.value = file
  avatarPreview.value = URL.createObjectURL(file)
  event.target.value = ''
}

const triggerVideoSelect = () => {
  uploadInput.value?.click()
}

const handleVideoChange = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  if (videoPreview.value) {
    URL.revokeObjectURL(videoPreview.value)
  }
  videoFile.value = file
  videoPreview.value = URL.createObjectURL(file)
  const tempVideo = document.createElement('video')
  tempVideo.preload = 'metadata'
  tempVideo.src = videoPreview.value
  tempVideo.onloadedmetadata = () => {
    videoDuration.value = Math.round(tempVideo.duration || 0)
    tempVideo.remove()
  }
  event.target.value = ''
}

const handleSave = async () => {
  if (!editForm.nickname) {
    ElMessage.warning('请输入昵称')
    return
  }
  loading.value = true
  try {
    let avatarUrl = editForm.avatar
    if (avatarFile.value) {
      const { data: uploadRes } = await userApi.uploadAvatar(avatarFile.value)
      if (uploadRes?.code === 200 && uploadRes?.data) {
        avatarUrl = uploadRes.data
      } else {
        ElMessage.error(uploadRes?.msg || '头像上传失败')
        return
      }
    }
    const payload = {
      nickname: editForm.nickname,
      phone: editForm.phone,
      email: editForm.email,
      avatar: avatarUrl,
    }
    Object.keys(payload).forEach((key) => {
      if (payload[key] === '' || payload[key] === undefined || payload[key] === null) {
        delete payload[key]
      }
    })
    const { data } = await userApi.updateUserInfo(payload)
    if (data?.code === 200) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      avatarFile.value = null
      avatarPreview.value = ''
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

const handleUploadVideo = async () => {
  if (!videoFile.value) {
    ElMessage.warning('请选择视频文件')
    return
  }
  if (!uploadForm.title) {
    ElMessage.warning('请输入视频标题')
    return
  }
  uploadLoading.value = true
  try {
    const payload = {
      file: videoFile.value,
      title: uploadForm.title,
      description: uploadForm.description,
      category: uploadForm.category,
      tags: uploadForm.tags,
      duration: videoDuration.value || undefined,
    }
    const { data } = await videoApi.uploadVideo(payload)
    if (data?.code === 200) {
      ElMessage.success('上传成功，等待审核')
      resetUpload()
      fetchMyUploads()
    } else {
      ElMessage.error(data?.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败，请稍后重试')
  } finally {
    uploadLoading.value = false
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
      ElMessage.success('密码修改成功，请重新登录')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      await userStore.logout()
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
  router.push('/home')
}

const openWork = (item) => {
  if (!item?.id) return
  sessionStorage.setItem('video_feed_target_id', String(item.id))
  router.push('/home')
}

onMounted(() => {
  if (userStore.token) {
    fetchProfile()
    ensureLibraryData(activeLibraryTab.value)
  } else {
    fetchMyUploads()
  }
})

watch(
  () => profile.value?.id,
  () => {
    fetchMyUploads()
  }
)

watch(
  () => activeLibraryTab.value,
  (tab) => {
    ensureLibraryData(tab)
  }
)

onBeforeUnmount(() => {
  if (videoPreview.value) {
    URL.revokeObjectURL(videoPreview.value)
  }
  if (avatarPreview.value) {
    URL.revokeObjectURL(avatarPreview.value)
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
        <button type="button" class="avatar-btn" @click="openEditDialog">
          <el-avatar :size="88" :src="displayAvatar" class="avatar" />
          <span class="avatar-tip">修改个人资料</span>
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

      <section class="section upload-section">
        <div class="section-title">
          <h2>视频上传</h2>
          <div class="upload-toolbar">
            <el-button @click="triggerVideoSelect">选择视频</el-button>
            <el-button
              type="primary"
              :loading="uploadLoading"
              :disabled="!videoFile"
              @click="handleUploadVideo"
            >
              发布视频
            </el-button>
          </div>
        </div>
        <p class="section-tip">上传你的作品，支持 MP4/MOV，完成后进入审核。</p>
        <div class="upload-card">
          <div class="upload-preview">
            <div class="preview-box">
              <video v-if="videoPreview" :src="videoPreview" muted playsinline controls></video>
              <div v-else class="preview-placeholder" @click="triggerVideoSelect">
                <p>点击上传视频</p>
                <span>16:9 或 9:16 均可</span>
                <el-button size="small" @click.stop="triggerVideoSelect">选择视频</el-button>
              </div>
            </div>
            <div class="upload-meta">
              <span>{{ videoFileName }}</span>
              <span>时长 {{ videoDurationLabel }}</span>
            </div>
            <el-button v-if="videoPreview" class="change-btn" @click="triggerVideoSelect">
              更换视频
            </el-button>
          </div>
          <div class="upload-form">
            <el-form label-position="top">
              <el-form-item label="标题">
                <el-input v-model="uploadForm.title" placeholder="给你的视频起个标题" />
              </el-form-item>
              <el-form-item label="简介">
                <el-input
                  v-model="uploadForm.description"
                  type="textarea"
                  :rows="3"
                  placeholder="描述一下视频内容"
                />
              </el-form-item>
              <el-form-item label="分类">
                <el-input v-model="uploadForm.category" placeholder="例如：旅行/游戏/生活" />
              </el-form-item>
              <el-form-item label="标签">
                <el-input v-model="uploadForm.tags" placeholder="用逗号分隔标签" />
              </el-form-item>
            </el-form>
          </div>
        </div>
        <input
          ref="uploadInput"
          type="file"
          accept="video/*"
          class="hidden"
          @change="handleVideoChange"
        />
      </section>

      <section class="section works-section">
        <div class="section-title">
          <h2>我的作品</h2>
          <span class="section-tip">已上传 {{ myUploads.length }} 条</span>
        </div>
        <div v-if="myUploadsLoading" class="works-empty">加载中...</div>
        <div v-else-if="myUploads.length === 0" class="works-empty">
          暂无作品，上传你的第一支视频吧。
        </div>
        <div v-else class="works-grid">
          <article v-for="item in myUploads" :key="item.id" class="work-card" @click="openWork(item)">
            <div class="work-thumb">
              <video
                v-if="item.videoUrl"
                :src="item.videoUrl"
                muted
                playsinline
                preload="metadata"
              ></video>
              <img v-else-if="item.coverUrl" :src="item.coverUrl" alt="cover" />
              <div v-else class="work-placeholder">暂无预览</div>
            </div>
            <div class="work-info">
              <div class="work-title">{{ item.title }}</div>
              <div class="work-desc">{{ item.description || '暂无简介' }}</div>
              <div class="work-meta">
                <span>{{ formatDuration(item.duration || 0) }}</span>
                <span class="work-status">{{ item.status ?? '已发布' }}</span>
              </div>
            </div>
          </article>
        </div>
        <div v-if="uploadsHasMore && myUploads.length" class="works-more">
          <el-button :loading="myUploadsLoading" @click="loadMoreUploads">加载更多</el-button>
        </div>
        <div v-else-if="!uploadsHasMore && myUploads.length" class="works-end">没有更多了</div>
      </section>

      <section class="section library-section">
        <div class="section-title">
          <h2>我的互动</h2>
          <div class="library-tabs">
            <button
              type="button"
              class="library-tab"
              :class="{ active: activeLibraryTab === 'likes' }"
              @click="activeLibraryTab = 'likes'"
            >
              喜欢
            </button>
            <button
              type="button"
              class="library-tab"
              :class="{ active: activeLibraryTab === 'collects' }"
              @click="activeLibraryTab = 'collects'"
            >
              收藏
            </button>
          </div>
        </div>
        <p class="section-tip">
          共 {{ activeLibraryTab === 'likes' ? likeVideos.length : collectVideos.length }} 条
        </p>
        <div v-if="libraryLoading && libraryList.length === 0" class="works-empty">加载中...</div>
        <div v-else-if="libraryList.length === 0" class="works-empty">暂无数据</div>
        <div v-else class="works-grid">
          <article v-for="item in libraryList" :key="item.id" class="work-card" @click="openWork(item)">
            <div class="work-thumb">
              <video
                v-if="item.videoUrl"
                :src="item.videoUrl"
                muted
                playsinline
                preload="metadata"
              ></video>
              <img v-else-if="item.coverUrl" :src="item.coverUrl" alt="cover" />
              <div v-else class="work-placeholder">暂无预览</div>
            </div>
            <div class="work-info">
              <div class="work-title">{{ item.title }}</div>
              <div class="work-desc">{{ item.description || '暂无简介' }}</div>
              <div class="work-meta">
                <span>{{ formatDuration(item.duration || 0) }}</span>
                <span v-if="item.authorName">@{{ item.authorName }}</span>
              </div>
            </div>
          </article>
        </div>
        <div v-if="libraryHasMore && libraryList.length" class="works-more">
          <el-button :loading="libraryLoading" @click="loadMoreLibrary">加载更多</el-button>
        </div>
        <div v-else-if="!libraryHasMore && libraryList.length" class="works-end">没有更多了</div>
      </section>

      <section class="section">
        <div class="section-title">
          <h2>资料编辑</h2>
          <el-button type="primary" @click="openEditDialog">编辑资料</el-button>
        </div>
        <p class="section-tip">更新昵称、手机号、邮箱和头像地址。</p>
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
        <el-button class="logout-btn" :loading="logoutLoading" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </section>

    <el-dialog v-model="editDialogVisible" title="编辑资料" width="420px">
      <el-form label-position="top" class="dialog-form">
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :size="44" :src="avatarPreview || editForm.avatar || displayAvatar" />
            <div class="upload-actions">
              <el-button @click="triggerAvatar">上传文件</el-button>
              <span class="upload-hint">
                {{ avatarFile ? avatarFile.name : '未选择文件' }}
              </span>
            </div>
          </div>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            class="hidden"
            @change="handleAvatarChange"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="头像地址">
          <el-input v-model="editForm.avatar" placeholder="请输入头像 URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSave">
          保存
        </el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.profile-page {
  height: 100vh;
  min-height: 100vh;
  background: var(--dy-bg-body);
  color: var(--dy-text-primary);
  padding: 88px 20px 80px;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
}

.profile-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background: radial-gradient(circle at 15% 20%, rgba(34, 211, 238, 0.18), transparent 45%),
    radial-gradient(circle at 80% 10%, rgba(59, 130, 246, 0.2), transparent 50%),
    radial-gradient(circle at 60% 80%, rgba(249, 115, 22, 0.16), transparent 45%);
}

.profile-overlay {
  position: fixed;
  inset: 0;
  z-index: 1;
  background: rgba(8, 10, 18, 0.65);
  backdrop-filter: blur(14px);
}

.back-btn {
  position: fixed;
  top: 18px;
  left: 18px;
  z-index: 5;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: rgba(15, 18, 29, 0.75);
  color: var(--dy-text-primary);
  cursor: pointer;
  display: grid;
  place-items: center;
}

.profile-card {
  max-width: 980px;
  margin: 0 auto;
  background: linear-gradient(140deg, rgba(16, 20, 34, 0.95), rgba(12, 15, 26, 0.96));
  border-radius: 24px;
  padding: 28px 32px 32px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: var(--dy-shadow-card);
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
  border: 3px solid rgba(59, 130, 246, 0.65);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.6);
}

.avatar-tip {
  font-size: 12px;
  color: var(--dy-text-tertiary);
}

.profile-meta h1 {
  margin: 0 0 6px;
  font-size: 26px;
  font-family: var(--font-heading);
}

.profile-meta p {
  margin: 0;
  color: var(--dy-text-tertiary);
  font-size: 13px;
}

.signature {
  margin-top: 10px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin: 20px 0 12px;
  background: rgba(15, 18, 29, 0.7);
  border-radius: 16px;
  padding: 14px 18px;
  text-align: center;
  width: min(520px, 100%);
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.stats strong {
  display: block;
  font-size: 20px;
  font-family: var(--font-heading);
}

.stats span {
  font-size: 12px;
  color: var(--dy-text-tertiary);
}

.section {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
  width: min(760px, 100%);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  width: 100%;
  gap: 12px;
}

.section-title h2 {
  margin: 0;
  font-size: 16px;
  font-family: var(--font-heading);
}

.section-tip {
  margin: 0;
  color: var(--dy-text-tertiary);
  font-size: 12px;
}

.upload-section {
  width: min(860px, 100%);
}

.upload-toolbar {
  display: flex;
  gap: 10px;
}

.works-section {
  width: min(860px, 100%);
}

.works-empty {
  margin-top: 12px;
  padding: 16px;
  border-radius: 16px;
  text-align: center;
  color: var(--dy-text-tertiary);
  background: rgba(15, 18, 29, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.works-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.library-tabs {
  display: flex;
  gap: 8px;
}

.library-tab {
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: transparent;
  color: var(--dy-text-secondary);
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.library-tab.active {
  background: linear-gradient(120deg, var(--dy-brand-blue), var(--dy-brand-cyan));
  color: #0f172a;
  border-color: transparent;
}

.works-more,
.works-end {
  margin-top: 14px;
  display: flex;
  justify-content: center;
  color: var(--dy-text-tertiary);
  font-size: 12px;
}

.work-card {
  border-radius: 16px;
  overflow: hidden;
  background: rgba(12, 15, 24, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.16);
  display: grid;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.work-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(2, 6, 23, 0.45);
}

.work-thumb {
  width: 100%;
  aspect-ratio: 9 / 16;
  background: #0b0d16;
}

.work-thumb video,
.work-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.work-placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: rgba(226, 232, 240, 0.7);
  font-size: 12px;
}

.work-info {
  padding: 12px;
  display: grid;
  gap: 6px;
}

.work-title {
  font-size: 14px;
  font-weight: 600;
}

.work-desc {
  font-size: 12px;
  color: var(--dy-text-tertiary);
}

.work-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 11px;
  color: rgba(226, 232, 240, 0.7);
}

.work-status {
  color: #38bdf8;
}

.upload-card {
  margin-top: 16px;
  display: grid;
  grid-template-columns: minmax(220px, 280px) 1fr;
  gap: 24px;
  align-items: start;
  background: rgba(15, 18, 29, 0.6);
  border-radius: 18px;
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.upload-preview {
  display: grid;
  gap: 12px;
  justify-items: center;
}

.preview-box {
  width: 100%;
  aspect-ratio: 9 / 16;
  border-radius: 16px;
  border: 1px dashed rgba(148, 163, 184, 0.4);
  background: rgba(9, 12, 22, 0.6);
  overflow: hidden;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.preview-box video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-placeholder {
  display: grid;
  gap: 8px;
  text-align: center;
  color: rgba(226, 232, 240, 0.8);
  font-size: 12px;
}

.preview-placeholder p {
  margin: 0;
  font-weight: 600;
  color: #fff;
}

.upload-meta {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-size: 12px;
  color: rgba(226, 232, 240, 0.7);
}

.change-btn {
  width: 100%;
}

.upload-form {
  width: 100%;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-actions {
  display: grid;
  gap: 6px;
}

.upload-hint {
  font-size: 12px;
  color: var(--dy-text-tertiary);
}

.hidden {
  display: none;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  width: 100%;
}

.logout-row {
  margin-top: 22px;
  display: flex;
  justify-content: center;
  width: 100%;
}

:deep(.el-form-item__label) {
  color: var(--dy-text-tertiary);
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  border-radius: 14px;
  background: rgba(15, 18, 29, 0.7);
  box-shadow: none;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

:deep(.el-input__inner) {
  color: var(--dy-text-primary);
}

:deep(.el-button--primary) {
  background: linear-gradient(120deg, var(--dy-brand-blue), var(--dy-brand-cyan));
  border: none;
}

:deep(.logout-btn) {
  background: linear-gradient(120deg, var(--dy-brand-blue), var(--dy-brand-cyan));
  border: none;
  color: #0f172a;
  box-shadow: 0 10rem 20rem rgba(59, 130, 246, 0.25);
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

  .upload-card {
    grid-template-columns: 1fr;
  }

  .logout-row {
    justify-content: center;
  }
}
</style>
