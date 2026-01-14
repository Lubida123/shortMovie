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

const fetchVideos = async () => {
  if (loading.value || finished.value) return
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await videoApi.getVideoList({
      pageNum: pageNum.value,
      pageSize,
    })
    if (data?.code === 200) {
      const list = data?.data?.list || data?.data?.records || data?.data || []
      const mapped = list.map(normalizeVideo).filter((item) => item && item.id)
      if (mapped.length === 0) {
        finished.value = true
      } else {
        videos.value.push(...mapped)
        pageNum.value += 1
        await nextTick()
      }
    } else {
      loadError.value = data?.message || '加载失败'
    }
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
    <header>
      <h2>推荐视频</h2>
      <p>基于推荐系统生成的个性化内容</p>
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
            <span class="likes">❤ {{ video.likeCount }}</span>
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
  gap: 16px;
}

.video-list header h2 {
  margin: 0;
  font-size: 18px;
}

.video-list header p {
  margin: 6px 0 0;
  color: var(--dy-text-tertiary);
  font-size: 12px;
}

.video-card {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
  background: rgba(16, 18, 26, 0.9);
  border-radius: 16px;
  padding: 16px;
  border: var(--dy-border-default);
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 16px;
}

.video-player {
  width: 100%;
  background: #0c0d14;
  border-radius: 14px;
  overflow: hidden;
}

.video-el {
  width: 100%;
  display: block;
  border-radius: 14px;
}

.video-meta {
  display: grid;
  gap: 6px;
  color: var(--dy-text-primary);
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
  color: var(--dy-text-primary);
}

.desc {
  margin: 0;
  color: #c9ccd6;
  font-size: 12px;
}

.state {
  text-align: center;
  font-size: 12px;
  color: var(--dy-text-tertiary);
  padding: 8px 0;
}

.load-more {
  border: none;
  background: rgba(255, 255, 255, 0.08);
  color: var(--dy-text-primary);
  padding: 10px 16px;
  border-radius: 999px;
  cursor: pointer;
  margin: 0 auto;
}
</style>
