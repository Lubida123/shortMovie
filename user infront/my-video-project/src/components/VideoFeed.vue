<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { getVideoList } from '../api/video'

const emit = defineEmits(['load-more'])

const videos = ref([])
const currentIndex = ref(0)
const isSwitching = ref(false)
const loading = ref(false)
const hasMore = ref(true)
const loadError = ref('')
const pageNum = ref(1)
const pageSize = 8
const videoRefs = ref([])
let switchTimer = null

const wrapperStyle = computed(() => ({
  transform: `translate3d(0, -${currentIndex.value * 100}%, 0)`,
}))

const formatDuration = (seconds) => {
  const safe = Number.isFinite(seconds) ? seconds : 0
  const m = Math.floor(safe / 60).toString().padStart(2, '0')
  const s = Math.floor(safe % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

const normalizeVideo = (item) => {
  if (!item) return null
  const authorName = item.authorName || item.author || '匿名'
  const cover = item.coverUrl || item.cover || ''
  const url = item.videoUrl || item.url || ''
  if (!cover && !url) return null
  return {
    id: item.videoId || item.id,
    title: item.title || item.videoTitle || '未命名视频',
    author: authorName.startsWith('@') ? authorName : `@${authorName}`,
    desc: item.description || item.desc || '',
    duration: formatDuration(item.duration || 0),
    cover,
    url,
    likeCount: item.likeCount ?? 0,
    commentCount: item.commentCount ?? 0,
    collectCount: item.collectCount ?? 0,
  }
}

const extractPageList = (payload) => {
  const pageData = payload?.data ?? payload
  const list = pageData?.records ?? pageData?.list ?? []
  return Array.isArray(list) ? list : []
}

const fetchVideos = async () => {
  if (loading.value || !hasMore.value) return
  loading.value = true
  loadError.value = ''
  try {
    const { data } = await getVideoList({
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
      if (videos.value.length === 0) {
        loadError.value = '暂无视频数据'
      }
      hasMore.value = false
      return
    }
    videos.value.push(...mapped)
    pageNum.value += 1
  } catch (error) {
    const status = error?.response?.status
    if (status === 401) {
      loadError.value = '请先登录后查看推荐'
    } else {
      loadError.value = '加载失败，请检查后端服务'
    }
  } finally {
    loading.value = false
  }
}

const syncPlayback = async (index) => {
  await nextTick()
  videoRefs.value.forEach((video, idx) => {
    if (!video) return
    if (idx === index) {
      const result = video.play()
      if (result && typeof result.catch === 'function') {
        result.catch(() => {})
      }
    } else {
      video.pause()
    }
  })
}

const setVideoRef = (el, index) => {
  if (el) {
    videoRefs.value[index] = el
  }
}

const loadMoreIfNeeded = async () => {
  if (!hasMore.value || loading.value) return
  emit('load-more')
  await fetchVideos()
}

const goNext = () => {
  if (currentIndex.value < videos.value.length - 1) {
    currentIndex.value += 1
    if (currentIndex.value >= videos.value.length - 2) {
      loadMoreIfNeeded()
    }
    return
  }
  loadMoreIfNeeded()
}

const goPrev = () => {
  if (currentIndex.value <= 0) return
  currentIndex.value -= 1
}

const handleWheel = (e) => {
  if (isSwitching.value) return
  if (!videos.value.length) return
  if (e.deltaY === 0) return
  isSwitching.value = true
  switchTimer = window.setTimeout(() => {
    isSwitching.value = false
  }, 800)

  if (e.deltaY > 0) {
    goNext()
  } else if (e.deltaY < 0) {
    goPrev()
  }
}

onMounted(() => {
  fetchVideos()
})

watch(
  () => currentIndex.value,
  (index) => {
    if (videos.value.length) {
      syncPlayback(index)
    }
  }
)

watch(
  () => videos.value.length,
  (length) => {
    if (length) {
      syncPlayback(currentIndex.value)
    }
  }
)

onBeforeUnmount(() => {
  if (switchTimer) {
    window.clearTimeout(switchTimer)
    switchTimer = null
  }
})
</script>

<template>
  <section class="feed-container" tabindex="0" @wheel.passive.capture="handleWheel">
    <div v-if="loadError" class="empty-state">{{ loadError }}</div>
    <div v-else-if="videos.length === 0" class="empty-state">暂无视频</div>
    <div v-else class="video-wrapper" :style="wrapperStyle">
      <article v-for="(video, index) in videos" :key="video.id" class="video-item">
        <div class="player-frame">
          <div class="player-cover" :style="{ backgroundImage: `url(${video.cover})` }">
            <video
              v-if="video.url"
              class="video-el"
              :src="video.url"
              :poster="video.cover"
              muted
              autoplay
              loop
              playsinline
              preload="metadata"
              :ref="(el) => setVideoRef(el, index)"
            ></video>
            <div class="player-info">
              <div class="player-author">
                <img class="avatar" src="../assets/img/avatar.png" alt="avatar" />
                <span>{{ video.author }}</span>
              </div>
              <h1>{{ video.title }}</h1>
              <p v-if="video.desc" class="desc">{{ video.desc }}</p>
            </div>
          </div>

          <div class="player-actions">
            <button class="action">
              <img src="../assets/img/icon/love.svg" alt="like" />
              <span>{{ video.likeCount }}</span>
            </button>
            <button class="action">
              <img src="../assets/img/icon/message.svg" alt="comment" />
              <span>{{ video.commentCount }}</span>
            </button>
            <button class="action">
              <img src="../assets/img/icon/star-white.png" alt="collect" />
              <span>{{ video.collectCount }}</span>
            </button>
            <button class="action">
              <img src="../assets/img/icon/share-white.png" alt="share" />
              <span>分享</span>
            </button>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped lang="less">
.feed-container {
  height: 100%;
  width: 100%;
  overflow: hidden;
  position: relative;
  overscroll-behavior: contain;
}

.video-wrapper {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  transition: transform 0.6s ease;
  will-change: transform;
}

.video-item {
  height: 100%;
  width: 100%;
  flex: 0 0 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  box-sizing: border-box;
}

.empty-state {
  height: 100%;
  width: 100%;
  display: grid;
  place-items: center;
  color: var(--dy-text-tertiary);
}

.player-frame {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28rem;
}

.player-cover {
  position: relative;
  border-radius: 16rem;
  overflow: hidden;
  height: 100%;
  min-height: 0;
  flex: 1;
  max-width: 1100rem;
  background-position: center;
  background-size: cover;
  background-color: #0b0d16;
  box-shadow: 0 24rem 60rem rgba(0, 0, 0, 0.35);
  border: 1rem solid rgba(148, 163, 184, 0.12);
}

.video-el {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
}

.player-cover::before {
  content: none;
}

.player-actions {
  display: grid;
  gap: 14rem;
  z-index: 2;
  min-width: 72rem;
}

.action {
  border: none;
  background: rgba(18, 20, 30, 0.8);
  color: #fff;
  border-radius: 16rem;
  padding: 10rem 12rem;
  display: grid;
  justify-items: center;
  gap: 6rem;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease;
}

.action:hover {
  transform: translateY(-2rem);
  background: rgba(28, 32, 48, 0.9);
}

.action img {
  width: 20rem;
}

.action span {
  font-size: 12rem;
}

.player-info {
  position: absolute;
  left: 18rem;
  bottom: 18rem;
  z-index: 2;
  color: #fff;
  text-shadow: 0 6rem 14rem rgba(0, 0, 0, 0.7);
  max-width: 70%;
}

.avatar {
  width: 38rem;
  height: 38rem;
  border-radius: 999rem;
  border: 2rem solid #fff;
}

.player-author {
  display: inline-flex;
  align-items: center;
  gap: 10rem;
  font-weight: 600;
  margin-bottom: 6rem;
}

.player-info h1 {
  margin: 0 0 6rem;
  font-size: 26rem;
  font-weight: 600;
}

.desc {
  font-size: 12rem;
  color: #e2e8f0;
  opacity: 0.9;
}

@media (max-width: 980px) {
  .player-actions {
    grid-template-columns: repeat(4, minmax(80rem, 1fr));
    background: rgba(18, 20, 30, 0.7);
    padding: 10rem;
    border-radius: 14rem;
    margin-top: 12rem;
  }

  .player-frame {
    flex-direction: column;
    gap: 16rem;
  }

  .player-cover {
    max-width: 100%;
  }
}
</style>
