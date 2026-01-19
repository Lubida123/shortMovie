<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getVideoList } from '../api/video'
import { useUserStore } from '../store/userStore'
import * as userApi from '../api/user'

const route = useRoute()
const router = useRouter()
const defaultAvatar = new URL('../assets/img/avatar.png', import.meta.url).href
const userStore = useUserStore()

const loadingWorks = ref(false)

const creator = ref({
  id: '--',
  name: '未命名用户',
  account: '',
  avatar: defaultAvatar,
  works: [],
})

const works = computed(() => creator.value.works || [])
const displayName = computed(() => creator.value.name || '未命名用户')
const displayId = computed(() => creator.value.id || '--')
const displayAccount = computed(() => creator.value.account || '')
const displayAvatar = computed(() => creator.value.avatar || defaultAvatar)

const formatDuration = (value) => {
  if (typeof value === 'string') return value
  const safe = Number.isFinite(value) ? value : 0
  const m = Math.floor(safe / 60).toString().padStart(2, '0')
  const s = Math.floor(safe % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

const normalizeWork = (item) => {
  if (!item) return null
  return {
    id: item.videoId || item.id,
    title: item.title || item.videoTitle || '未命名视频',
    desc: item.description || item.desc || '',
    duration: item.duration || 0,
    cover: item.coverUrl || item.cover || '',
    url: item.videoUrl || item.url || '',
  }
}

const extractPageList = (payload) => {
  const pageData = payload?.data ?? payload
  const list = pageData?.records ?? pageData?.list ?? []
  return Array.isArray(list) ? list : []
}

const syncCreatorProfile = (profile, fallbackId) => {
  if (!profile) return
  creator.value = {
    ...creator.value,
    id: profile.id ?? profile.userId ?? fallbackId ?? creator.value.id,
    name: profile.nickname || profile.username || creator.value.name || '未命名用户',
    account: profile.username ? `@${profile.username}` : creator.value.account,
    avatar: profile.avatar || creator.value.avatar || defaultAvatar,
  }
}

const fetchSelfWorks = async (userId) => {
  const pageSize = 20
  let pageNum = 1
  let hasMore = true
  const worksMap = new Map()
  while (hasMore) {
    const { data } = await userApi.getMyVideos({ pageNum, pageSize })
    if (data?.code !== 200) break
    const list = extractPageList(data)
    if (list.length === 0) break
    list
      .map(normalizeWork)
      .filter(Boolean)
      .forEach((item) => {
        if (item?.id !== undefined && item?.id !== null) {
          worksMap.set(item.id, item)
        }
      })
    const pageData = data?.data ?? data
    if (typeof pageData?.pages === 'number') {
      hasMore = pageNum < pageData.pages
    } else {
      hasMore = list.length >= pageSize
    }
    pageNum += 1
  }
  creator.value.works = Array.from(worksMap.values())
}

const loadCreator = () => {
  const userId = route.params.userId ? String(route.params.userId) : ''
  const raw = sessionStorage.getItem('creator_profile')
  if (raw) {
    try {
      const parsed = JSON.parse(raw)
      const cachedId = parsed?.id ? String(parsed.id) : ''
      if (!userId || cachedId === userId) {
        creator.value = {
          id: parsed.id || route.params.userId || '--',
          name: parsed.name || '未命名用户',
          account: parsed.account || '',
          avatar: parsed.avatar || defaultAvatar,
          works: Array.isArray(parsed.works) ? parsed.works : [],
        }
        return
      }
    } catch (error) {
      // fall through to fallback
    }
  }
  creator.value = {
    id: route.params.userId || '--',
    name: route.query.name || '未命名用户',
    account: route.query.account || '',
    avatar: defaultAvatar,
    works: [],
  }
}

const fetchUserWorks = async (userId) => {
  if (!userId) return
  loadingWorks.value = true
  try {
    const currentId = userStore.userInfo?.id ?? userStore.userInfo?.userId
    if (currentId && String(currentId) === String(userId)) {
      syncCreatorProfile(userStore.userInfo, userId)
      await fetchSelfWorks(userId)
      return
    }
    if (userStore.token && !currentId) {
      const profile = await userStore.fetchProfile()
      const profileId = profile?.id ?? profile?.userId
      if (profileId && String(profileId) === String(userId)) {
        syncCreatorProfile(profile, userId)
        await fetchSelfWorks(userId)
        return
      }
    }
    const pageSize = 20
    let pageNum = 1
    let hasMore = true
    const worksMap = new Map()
    while (hasMore) {
      const { data } = await getVideoList({
        pageNum,
        pageSize,
        authorId: userId,
      })
      if (data?.code !== 200) break
      const list = extractPageList(data)
      if (list.length === 0) break
      list
        .map(normalizeWork)
        .filter(Boolean)
        .forEach((item) => {
        if (item?.id !== undefined && item?.id !== null) {
          worksMap.set(item.id, item)
        }
      })
      const pageData = data?.data ?? data
      if (typeof pageData?.pages === 'number') {
        hasMore = pageNum < pageData.pages
      } else {
        hasMore = list.length >= pageSize
      }
      pageNum += 1
    }
    creator.value.works = Array.from(worksMap.values())
  } finally {
    loadingWorks.value = false
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

onMounted(async () => {
  loadCreator()
  if (route.params.userId) {
    await fetchUserWorks(route.params.userId)
  }
})

watch(
  () => route.params.userId,
  async (value) => {
    loadCreator()
    if (value) {
      await fetchUserWorks(value)
    }
  }
)
</script>

<template>
  <main class="creator-page">
    <div class="creator-bg"></div>
    <div class="creator-overlay"></div>
    <button type="button" class="back-btn" @click="handleBack">←</button>

    <section class="creator-card">
      <div class="creator-header">
        <el-avatar :size="86" :src="displayAvatar" class="creator-avatar" />
        <div class="creator-meta">
          <h1>{{ displayName }}</h1>
          <p>ID: {{ displayId }}</p>
          <p v-if="displayAccount" class="creator-account">{{ displayAccount }}</p>
        </div>
      </div>

      <section class="creator-works">
        <div class="section-title">
          <h2>我的作品</h2>
          <span class="section-tip">{{ works.length }} 条</span>
        </div>
        <div v-if="loadingWorks" class="works-empty">加载中...</div>
        <div v-else-if="works.length === 0" class="works-empty">暂无作品</div>
        <div v-else class="works-grid">
          <article v-for="item in works" :key="item.id" class="work-card" @click="openWork(item)">
            <div class="work-thumb">
              <video
                v-if="item.url || item.videoUrl"
                :src="item.url || item.videoUrl"
                muted
                playsinline
                preload="metadata"
              ></video>
              <img v-else-if="item.cover" :src="item.cover" alt="cover" />
              <div v-else class="work-placeholder">暂无预览</div>
              <span class="work-duration">{{ formatDuration(item.duration || 0) }}</span>
            </div>
            <div class="work-info">
              <div class="work-title">{{ item.title }}</div>
              <div class="work-desc">{{ item.desc || '暂无简介' }}</div>
            </div>
          </article>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.creator-page {
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

.creator-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background: radial-gradient(circle at 15% 20%, rgba(34, 211, 238, 0.18), transparent 45%),
    radial-gradient(circle at 80% 10%, rgba(59, 130, 246, 0.2), transparent 50%),
    radial-gradient(circle at 60% 80%, rgba(249, 115, 22, 0.16), transparent 45%);
}

.creator-overlay {
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

.creator-card {
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
  gap: 24px;
}

.creator-header {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 20px;
  align-items: center;
}

.creator-avatar {
  border: 3px solid rgba(59, 130, 246, 0.65);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.6);
}

.creator-meta h1 {
  margin: 0 0 6px;
  font-size: 26px;
  font-family: var(--font-heading);
}

.creator-meta p {
  margin: 0;
  color: var(--dy-text-tertiary);
  font-size: 13px;
}

.creator-account {
  margin-top: 6px;
}

.creator-works {
  border-top: 1px solid rgba(148, 163, 184, 0.16);
  padding-top: 16px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 12px;
}

.section-title h2 {
  margin: 0;
  font-size: 16px;
  font-family: var(--font-heading);
}

.section-tip {
  font-size: 12px;
  color: var(--dy-text-tertiary);
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
  position: relative;
  display: grid;
  place-items: center;
}

.work-thumb video,
.work-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.work-placeholder {
  color: rgba(226, 232, 240, 0.7);
  font-size: 12px;
}

.work-duration {
  position: absolute;
  right: 10px;
  bottom: 10px;
  font-size: 11px;
  color: #fff;
  background: rgba(0, 0, 0, 0.6);
  padding: 2px 8px;
  border-radius: 999px;
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

@media (max-width: 960px) {
  .creator-card {
    margin-top: -40px;
    padding: 20px;
  }

  .creator-header {
    grid-template-columns: 1fr;
    text-align: center;
    justify-items: center;
  }
}
</style>
