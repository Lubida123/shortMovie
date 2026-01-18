<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as videoApi from '../api/video'

const videos = ref([])
const pageNum = ref(1)
const pageSize = 8
const loading = ref(false)
const finished = ref(false)
const loadError = ref('')
const activeId = ref(null)

const playStates = new Map()
const videoElements = new Map()
let observer = null

const normalizeVideo = (item) => {
  if (!item) return null
  return {
    id: item.videoId || item.id,
    title: item.title || item.videoTitle || '未命名视频',
    description: item.description || item.desc || '',
    videoUrl: item.videoUrl || '',
    coverUrl: item.coverUrl || '',
    authorName: item.authorName || item.author || '匿名',
    likeCount: item.likeCount ?? 0,
  }
}

const ensureState = (id) => {
  if (!playStates.has(id)) {
    playStates.set(id, { total: 0, lastStart: null, reported: false })
  }
  return playStates.get(id)
}

const addPlayTime = (id) => {
  const state = playStates.get(id)
  if (!state || state.lastStart === null) return
  const delta = (Date.now() - state.lastStart) / 1000
  state.total += delta
  state.lastStart = null
}

const reportPlay = async (id, isCompleted) => {
  const state = playStates.get(id)
  if (!state || state.reported) return
  state.reported = true
  const playDuration = Math.max(0, Math.round(state.total * 100) / 100)
  try {
    await videoApi.recordPlay(id, {
      playDuration,
      isCompleted,
    })
  } catch (error) {
    // ignore report errors to avoid blocking UI
  }
}

const handleActiveChange = (nextId) => {
  if (!nextId || nextId === activeId.value) return
  const prevId = activeId.value
  activeId.value = nextId
  if (prevId) {
    addPlayTime(prevId)
    reportPlay(prevId, false)
  }
}

const setVideoRef = (el) => {
  if (!el) return
  const id = el.dataset?.id
  if (!id || videoElements.has(id)) return
  videoElements.set(id, el)
  if (observer) {
    observer.observe(el)
  }
}

const onPlay = (id) => {
  const state = ensureState(id)
  if (state.lastStart === null) {
    state.lastStart = Date.now()
  }
}

const onPause = (id) => {
  addPlayTime(id)
}

const onEnded = (id) => {
  addPlayTime(id)
  reportPlay(id, true)
}

const extractPageList = (payload) => {
  const pageData = payload?.data ?? payload
  const list = pageData?.records ?? pageData?.list ?? []
  return Array.isArray(list) ? list : []
}

const fetchVideos = async () => {
  if (loading.value || finished.value) return
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await videoApi.getVideoList({
      pageNum: pageNum.value,
      pageSize,
    })
    if (data?.code !== undefined && data?.code !== 200) {
      loadError.value = data?.message || '加载失败'
      return
    }
    const list = extractPageList(data)
    const mapped = list.map(normalizeVideo).filter((item) => item && item.id)
    if (mapped.length === 0) {
      finished.value = true
      return
    }
    videos.value.push(...mapped)
    pageNum.value += 1
    await nextTick()
  } catch (error) {
    loadError.value = '加载失败，请检查后端服务'
  } finally {
    loading.value = false
  }
}

const handleScroll = () => {
  if (finished.value || loading.value) return
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const viewHeight = window.innerHeight
  const docHeight = document.documentElement.scrollHeight
  if (scrollTop + viewHeight >= docHeight - 200) {
    fetchVideos()
  }
}

onMounted(async () => {
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && entry.intersectionRatio >= 0.6) {
          handleActiveChange(entry.target.dataset?.id)
        }
      })
    },
    { threshold: [0.6] }
  )
  await fetchVideos()
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
  if (observer) {
    observer.disconnect()
    observer = null
  }
})
</script>

<template>
  <section class="video-list">
    <header class="section-head">
      <div>
        <h2>推荐视频</h2>
        <p>基于推荐系统生成的个性化内容</p>
      </div>
      <span class="head-count">已加载 {{ videos.length }} 条</span>
    </header>

    <div v-if="loadError" class="state">{{ loadError }}</div>
    <div v-else-if="videos.length === 0 && !loading" class="state">暂无视频</div>

    <ul>
      <li v-for="video in videos" :key="video.id" class="video-card">
        <div class="video-player">
          <video
            :data-id="video.id"
            :src="video.videoUrl"
            :poster="video.coverUrl"
            preload="metadata"
            controls
            class="video-el"
            @play="onPlay(video.id)"
            @pause="onPause(video.id)"
            @ended="onEnded(video.id)"
            :ref="setVideoRef"
          />
        </div>
        <div class="video-meta">
          <div class="meta-head">
            <div>
              <strong>{{ video.title }}</strong>
              <span class="author">@{{ video.authorName }}</span>
            </div>
            <span class="likes">赞 {{ video.likeCount }}</span>
          </div>
          <p class="desc">{{ video.description }}</p>
        </div>
      </li>
    </ul>

    <div v-if="loading" class="state">加载中...</div>
    <button v-if="!finished && !loading" class="load-more" @click="fetchVideos">加载更多</button>
    <div v-if="finished && videos.length > 0" class="state">没有更多了</div>
  </section>
</template>

<style scoped>
.video-list {
  display: grid;
  gap: 18px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.section-head h2 {
  margin: 0;
  font-size: 20px;
  font-family: var(--font-heading);
}

.section-head p {
  margin: 6px 0 0;
  color: var(--dy-text-tertiary);
  font-size: 12px;
}

.head-count {
  font-size: 12px;
  color: var(--dy-text-secondary);
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.18);
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.video-card {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: minmax(280px, 1.2fr) minmax(200px, 0.8fr);
  gap: 16px;
  background: linear-gradient(135deg, rgba(18, 22, 36, 0.92), rgba(12, 14, 24, 0.92));
  border-radius: 18px;
  padding: 18px;
  border: var(--dy-border-default);
  box-shadow: var(--dy-shadow-card);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 18px;
}

.video-player {
  width: 100%;
  background: #0c0d14;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.18);
  aspect-ratio: 16 / 9;
}

.video-el {
  width: 100%;
  display: block;
  border-radius: 16px;
  height: 100%;
  object-fit: cover;
}

.video-meta {
  display: grid;
  gap: 8px;
  color: var(--dy-text-primary);
  align-content: center;
}

.meta-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.author {
  display: block;
  font-size: 12px;
  color: var(--dy-text-tertiary);
  margin-top: 2px;
}

.likes {
  font-size: 12px;
  color: var(--dy-text-secondary);
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(249, 115, 22, 0.15);
  border: 1px solid rgba(249, 115, 22, 0.3);
}

.desc {
  margin: 0;
  color: var(--dy-text-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.state {
  text-align: center;
  font-size: 12px;
  color: var(--dy-text-tertiary);
  padding: 8px 0;
}

.load-more {
  border: none;
  background: rgba(59, 130, 246, 0.2);
  color: var(--dy-text-primary);
  padding: 10px 18px;
  border-radius: 999px;
  cursor: pointer;
  margin: 0 auto;
  border: 1px solid rgba(59, 130, 246, 0.35);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.video-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 40px rgba(2, 6, 23, 0.55);
}

.load-more:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.3);
}

@media (max-width: 980px) {
  .video-card {
    grid-template-columns: 1fr;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
