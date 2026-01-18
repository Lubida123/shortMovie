<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const defaultAvatar = new URL('../assets/img/avatar.png', import.meta.url).href

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

const loadCreator = () => {
  const raw = sessionStorage.getItem('creator_profile')
  if (raw) {
    try {
      const parsed = JSON.parse(raw)
      creator.value = {
        id: parsed.id || route.params.userId || '--',
        name: parsed.name || '未命名用户',
        account: parsed.account || '',
        avatar: parsed.avatar || defaultAvatar,
        works: Array.isArray(parsed.works) ? parsed.works : [],
      }
      return
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

const handleBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/home')
}

onMounted(() => {
  loadCreator()
})

watch(
  () => route.params.userId,
  () => {
    loadCreator()
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
        <div v-if="works.length === 0" class="works-empty">
          暂无作品
        </div>
        <div v-else class="works-grid">
          <article v-for="item in works" :key="item.id" class="work-card">
            <div class="work-thumb">
              <video
                v-if="item.url"
                :src="item.url"
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
