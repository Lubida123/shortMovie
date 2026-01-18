<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
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
let switchTimer = null

const wrapperStyle = computed(() => ({
  transform: `translateY(-${currentIndex.value * 100}%)`,
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
  return {
    id: item.videoId || item.id,
    title: item.title || item.videoTitle || '未命名视频',
    author: authorName.startsWith('@') ? authorName : `@${authorName}`,
    desc: item.description || item.desc || '',
    duration: formatDuration(item.duration || 0),
    cover: item.coverUrl || '',
    url: item.videoUrl || '',
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

onBeforeUnmount(() => {
  if (switchTimer) {
    window.clearTimeout(switchTimer)
    switchTimer = null
  }
})
</script>

<template>
  <section class="feed-container" @wheel="handleWheel">
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
            ></video>
            <div class="player-overlay">
              <div class="player-meta">
                <span>#city-story</span>
                <span>{{ video.duration }}</span>
              </div>
              <div class="player-info">
                <h1>{{ video.title }}</h1>
                <p>{{ video.author }}</p>
                <p class="desc">{{ video.desc }}</p>
              </div>
              <div class="player-progress">
                <span>00:00</span>
                <div class="progress-bar">
                  <div
                    class="progress-fill"
                    :style="{ width: index === currentIndex ? '35%' : '0%' }"
                  ></div>
                </div>
                <span>{{ video.duration }}</span>
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

            <div class="player-user">
              <img class="avatar" src="../assets/img/avatar.png" alt="avatar" />
              <div>
                <div class="author">{{ video.author }}</div>
                <div class="caption">{{ video.title }}</div>
              </div>
            </div>
          </div>
          <div class="player-footer">
            <span>Reason: recommended</span>
            <span>下一条：{{ videos[index + 1]?.title || '暂无' }}</span>
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
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16rem;
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
  display: grid;
  gap: 12rem;
}

.player-cover {
  position: relative;
  border-radius: 18rem;
  overflow: hidden;
  flex: 1;
  background-position: center;
  background-size: cover;
  box-shadow: 0 24rem 60rem rgba(0, 0, 0, 0.45);
  border: var(--dy-border-default);
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
  content: "";
  position: absolute;
  inset: -20rem;
  background: inherit;
  filter: blur(30px);
  transform: scale(1.1);
  opacity: 0.6;
  z-index: 0;
}

.player-overlay {
  position: relative;
  height: 100%;
  padding: 20rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(180deg, rgba(12, 13, 18, 0.2), rgba(12, 13, 18, 0.8));
  z-index: 1;
}

.player-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12rem;
}

.player-info h1 {
  margin: 0 0 6rem;
  font-size: 32rem;
}

.player-info p {
  margin: 0 0 6rem;
  opacity: 0.9;
}

.desc {
  font-size: 12rem;
  color: #e3e3e3;
}

.player-progress {
  display: flex;
  align-items: center;
  gap: 10rem;
  font-size: 12rem;
}

.progress-bar {
  flex: 1;
  height: 2rem;
  background: rgba(255, 255, 255, 0.35);
  border-radius: 999rem;
  transition: height 0.2s ease;
}

.player-progress:hover .progress-bar {
  height: 4rem;
}

.progress-fill {
  height: 100%;
  background: #fff;
  border-radius: inherit;
  transition: width 0.4s ease;
}

.player-actions {
  position: absolute;
  right: 18rem;
  bottom: 120rem;
  display: grid;
  gap: 14rem;
  z-index: 2;
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

.player-user {
  position: absolute;
  left: 16rem;
  bottom: 18rem;
  display: flex;
  align-items: center;
  gap: 12rem;
  background: rgba(0, 0, 0, 0.45);
  padding: 10rem 12rem;
  border-radius: 14rem;
  backdrop-filter: blur(6px);
  z-index: 2;
}

.avatar {
  width: 38rem;
  height: 38rem;
  border-radius: 999rem;
  border: 2rem solid #fff;
}

.author {
  font-weight: 600;
}

.caption {
  font-size: 12rem;
  color: #d6d6d6;
}

.player-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12rem;
  color: var(--dy-text-tertiary);
}

@media (max-width: 980px) {
  .player-actions {
    position: static;
    grid-template-columns: repeat(4, minmax(80rem, 1fr));
    background: rgba(18, 20, 30, 0.7);
    padding: 10rem;
    border-radius: 14rem;
    margin-top: 12rem;
  }
}
</style>
