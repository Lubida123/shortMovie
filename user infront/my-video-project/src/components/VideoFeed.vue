<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getVideoList, getVideoDetail } from '../api/video'

const emit = defineEmits(['load-more', 'comment-like', 'comment-reply'])
const router = useRouter()

const CACHE_KEY = 'video_feed_cache_v1'
const CACHE_TTL = 5 * 60 * 1000

const videos = ref([])
const currentIndex = ref(0)
const isSwitching = ref(false)
const loading = ref(false)
const hasMore = ref(true)
const loadError = ref('')
const isFullscreen = ref(false)
const feedRef = ref(null)
const pageNum = ref(1)
const pageSize = 8
const videoRefs = ref([])
const progressRefs = ref([])
const currentTimes = ref({})
const durations = ref({})
const isSeeking = ref(false)
const seekingIndex = ref(null)
const seekWasPlaying = ref(false)
let pendingSeekTime = 0
const retryLoading = ref({})
const showComments = ref(false)
const newComment = ref('')
const replyTo = ref(null)
const defaultAvatar = new URL('../assets/img/avatar.png', import.meta.url).href
const mockComments = ref([
  {
    id: 1,
    user: '梦玉',
    content: '谁需要流星？我去炸。',
    time: '3周前 · 福建',
    likes: 17,
    liked: false,
    avatar: defaultAvatar,
  },
  {
    id: 2,
    user: '方圆脸看了一直在哭',
    content: '今天的风也太温柔了。',
    time: '3周前 · 湖南',
    likes: 3971,
    liked: false,
    avatar: defaultAvatar,
  },
  {
    id: 3,
    user: '小唐总划水',
    content: '我要去新疆滑雪了。',
    time: '3周前 · 上海',
    likes: 48,
    liked: false,
    avatar: defaultAvatar,
  },
  {
    id: 4,
    user: '顿哥哥',
    content: '看到这里就会想起以前。',
    time: '4天前 · 河南',
    likes: 1,
    liked: false,
    avatar: defaultAvatar,
  },
  {
    id: 5,
    user: '远没醒醒',
    content: '漂亮的眼睛清澈幸福。',
    time: '1天前 · 广东',
    likes: 6,
    liked: false,
    avatar: defaultAvatar,
  },
])
let switchTimer = null

const wrapperStyle = computed(() => ({
  transform: `translate3d(0, -${currentIndex.value * 100}%, 0)`,
}))

const currentVideo = computed(() => videos.value[currentIndex.value] || null)
const currentComments = computed(() => mockComments.value)
const commentCount = computed(() => currentVideo.value?.commentCount ?? currentComments.value.length)

const formatDuration = (seconds) => {
  const value = Number(seconds)
  const safe = Number.isFinite(value) ? value : 0
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
    authorName,
    authorId: item.authorId ?? item.userId ?? item.uid ?? item.userID,
    authorAvatar: item.authorAvatar || item.avatar || '',
    desc: item.description || item.desc || '',
    duration: formatDuration(item.duration || 0),
    cover,
    url,
    likeCount: item.likeCount ?? 0,
    commentCount: item.commentCount ?? 0,
    collectCount: item.collectCount ?? 0,
    retryCount: 0,
    error: false,
  }
}

const extractPageList = (payload) => {
  const pageData = payload?.data ?? payload
  const list = pageData?.records ?? pageData?.list ?? []
  return Array.isArray(list) ? list : []
}

const hasSignedUrl = (list) =>
  list.some((item) => typeof item?.url === 'string' && item.url.includes('?'))

const hydrateFromCache = () => {
  try {
    const raw = sessionStorage.getItem(CACHE_KEY)
    if (!raw) return false
    const cached = JSON.parse(raw)
    if (!cached?.videos || !Array.isArray(cached.videos)) return false
    if (hasSignedUrl(cached.videos)) {
      sessionStorage.removeItem(CACHE_KEY)
      return false
    }
    if (Date.now() - (cached.ts || 0) > CACHE_TTL) return false
    videos.value = cached.videos
    pageNum.value = cached.pageNum || 1
    hasMore.value = cached.hasMore ?? true
    currentIndex.value = Math.min(cached.currentIndex || 0, Math.max(cached.videos.length - 1, 0))
    loadError.value = ''
    return true
  } catch (error) {
    return false
  }
}

const persistCache = () => {
  try {
    if (hasSignedUrl(videos.value)) {
      sessionStorage.removeItem(CACHE_KEY)
      return
    }
    sessionStorage.setItem(
      CACHE_KEY,
      JSON.stringify({
        ts: Date.now(),
        videos: videos.value,
        pageNum: pageNum.value,
        hasMore: hasMore.value,
        currentIndex: currentIndex.value,
      })
    )
  } catch (error) {
    // ignore cache errors
  }
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
    persistCache()
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

const updateTimeFromVideo = (index) => {
  const video = videoRefs.value[index]
  if (!video) return
  const duration = Number.isFinite(video.duration) ? video.duration : 0
  durations.value[index] = duration
  currentTimes.value[index] = Number.isFinite(video.currentTime) ? video.currentTime : 0
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
  updateTimeFromVideo(index)
}

const refreshVideoSource = async (video) => {
  if (!video?.id) return false
  try {
    const { data } = await getVideoDetail(video.id)
    if (data?.code !== 200) return false
    const detail = data?.data || {}
    const nextUrl = detail.videoUrl || detail.url || video.url
    const nextCover = detail.coverUrl || detail.cover || video.cover
    if (nextUrl) {
      video.url = nextUrl
    }
    if (nextCover) {
      video.cover = nextCover
    }
    video.error = false
    return true
  } catch (error) {
    return false
  }
}

const handleVideoError = async (video) => {
  if (!video) return
  if (!Number.isFinite(video.retryCount)) {
    video.retryCount = 0
  }
  if (video.retryCount >= 1) {
    video.error = true
    return
  }
  video.retryCount += 1
  const refreshed = await refreshVideoSource(video)
  if (!refreshed) {
    video.error = true
  }
}

const retryVideo = async (video) => {
  if (!video || !video.id) return
  retryLoading.value[video.id] = true
  video.retryCount = 0
  const refreshed = await refreshVideoSource(video)
  if (!refreshed) {
    video.error = true
  }
  retryLoading.value[video.id] = false
}

const setVideoRef = (el, index) => {
  if (el) {
    videoRefs.value[index] = el
  }
}

const setProgressRef = (el, index) => {
  if (el) {
    progressRefs.value[index] = el
  }
}

const getProgress = (index) => {
  const duration = durations.value[index] || 0
  if (!duration) return 0
  const current = currentTimes.value[index] || 0
  return Math.min(100, Math.max(0, (current / duration) * 100))
}

const handleTimeUpdate = (index, event) => {
  const el = event?.target
  if (!el) return
  currentTimes.value[index] = el.currentTime || 0
  if (Number.isFinite(el.duration)) {
    durations.value[index] = el.duration
  }
}

const handleLoadedMetadata = (index, event) => {
  const el = event?.target
  if (!el) return
  durations.value[index] = Number.isFinite(el.duration) ? el.duration : 0
}

const togglePlay = (index) => {
  const video = videoRefs.value[index]
  if (!video) return
  if (video.paused) {
    const result = video.play()
    if (result && typeof result.catch === 'function') {
      result.catch(() => {})
    }
  } else {
    video.pause()
  }
}

const updateSeekPreview = (index, event) => {
  const bar = progressRefs.value[index]
  const video = videoRefs.value[index]
  if (!bar || !video) return
  const rect = bar.getBoundingClientRect()
  if (!rect.width) return
  const percent = Math.min(1, Math.max(0, (event.clientX - rect.left) / rect.width))
  const duration = Number.isFinite(video.duration) ? video.duration : durations.value[index] || 0
  pendingSeekTime = duration > 0 ? duration * percent : 0
  currentTimes.value[index] = pendingSeekTime
}

const handleSeekMove = (event) => {
  if (!isSeeking.value || seekingIndex.value === null) return
  updateSeekPreview(seekingIndex.value, event)
}

const handleSeekEnd = () => {
  if (!isSeeking.value) return
  const index = seekingIndex.value
  const video = index !== null ? videoRefs.value[index] : null
  if (video && Number.isFinite(pendingSeekTime)) {
    if (typeof video.fastSeek === 'function') {
      video.fastSeek(pendingSeekTime)
    } else {
      video.currentTime = pendingSeekTime
    }
    currentTimes.value[index] = video.currentTime || pendingSeekTime
    if (seekWasPlaying.value) {
      const result = video.play()
      if (result && typeof result.catch === 'function') {
        result.catch(() => {})
      }
    }
  }
  isSeeking.value = false
  seekingIndex.value = null
  seekWasPlaying.value = false
  window.removeEventListener('pointermove', handleSeekMove)
  window.removeEventListener('pointerup', handleSeekEnd)
}

const handleSeekStart = (index, event) => {
  if (!progressRefs.value[index]) return
  const video = videoRefs.value[index]
  isSeeking.value = true
  seekingIndex.value = index
  pendingSeekTime = currentTimes.value[index] || 0
  seekWasPlaying.value = video ? !video.paused : false
  if (video && !video.paused) {
    video.pause()
  }
  updateSeekPreview(index, event)
  window.addEventListener('pointermove', handleSeekMove)
  window.addEventListener('pointerup', handleSeekEnd)
}

const toggleComments = () => {
  showComments.value = !showComments.value
}

const closeComments = () => {
  showComments.value = false
}

const clearReply = () => {
  replyTo.value = null
}

const handleReply = (comment) => {
  replyTo.value = comment
  newComment.value = `@${comment.user} `
  emit('comment-reply', {
    commentId: comment.id,
    videoId: currentVideo.value?.id,
  })
}

const toggleCommentLike = (comment) => {
  const next = !comment.liked
  comment.liked = next
  comment.likes = Math.max(0, comment.likes + (next ? 1 : -1))
  emit('comment-like', {
    commentId: comment.id,
    videoId: currentVideo.value?.id,
    liked: next,
  })
}

const canSend = computed(() => newComment.value.trim().length > 0)

const sendComment = () => {
  if (!canSend.value) return
  mockComments.value.unshift({
    id: Date.now(),
    user: '我',
    content: newComment.value.trim(),
    time: '刚刚',
    likes: 0,
    liked: false,
    avatar: defaultAvatar,
  })
  if (currentVideo.value) {
    if (Number.isFinite(currentVideo.value.commentCount)) {
      currentVideo.value.commentCount += 1
    } else {
      currentVideo.value.commentCount = currentComments.value.length
    }
  }
  newComment.value = ''
  replyTo.value = null
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
  if (isSwitching.value || isSeeking.value) return
  if (showComments.value) return
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

const getAuthorKey = (video) => video.authorId ?? video.authorName ?? video.author ?? 'unknown'

const openCreatorProfile = (video) => {
  const authorKey = getAuthorKey(video)
  const works = videos.value.filter((item) => getAuthorKey(item) === authorKey)
  const payload = {
    id: video.authorId ?? authorKey,
    name: video.authorName || video.author || '匿名',
    account: video.author || `@${video.authorName || 'unknown'}`,
    avatar: video.authorAvatar || defaultAvatar,
    works,
  }
  sessionStorage.setItem('creator_profile', JSON.stringify(payload))
  router.push({
    name: 'creator',
    params: { userId: String(payload.id || 'unknown') },
  })
}

const handleFullscreenChange = () => {
  isFullscreen.value = Boolean(document.fullscreenElement)
}

const toggleFullscreen = async () => {
  const target = feedRef.value
  if (!target) return
  try {
    if (document.fullscreenElement) {
      await document.exitFullscreen()
      return
    }
    if (target.requestFullscreen) {
      await target.requestFullscreen()
      target.focus?.()
    }
  } catch (error) {
    return
  }
}

onMounted(() => {
  const restored = hydrateFromCache()
  if (!restored || videos.value.length === 0) {
    fetchVideos()
  }
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

watch(
  () => currentIndex.value,
  (index) => {
    if (videos.value.length) {
      syncPlayback(index)
    }
    persistCache()
    showComments.value = false
    replyTo.value = null
    newComment.value = ''
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
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  window.removeEventListener('pointermove', handleSeekMove)
  window.removeEventListener('pointerup', handleSeekEnd)
})
</script>

<template>
  <section
    ref="feedRef"
    class="feed-container"
    :class="{ 'is-fullscreen': isFullscreen }"
    tabindex="0"
    @wheel.passive.capture="handleWheel"
  >
    <div v-if="loadError" class="empty-state">{{ loadError }}</div>
    <div v-else-if="videos.length === 0" class="empty-state">暂无视频</div>
    <div v-else class="video-wrapper" :style="wrapperStyle">
      <article v-for="(video, index) in videos" :key="video.id" class="video-item">
        <div class="player-frame">
          <div
            class="player-cover"
            :style="{ backgroundImage: `url(${video.cover})` }"
            @click="togglePlay(index)"
          >
            <video
              v-if="video.url"
              class="video-el"
              :src="video.url"
              :poster="video.cover"
              muted
              autoplay
              loop
              playsinline
              :preload="index === currentIndex ? 'auto' : 'metadata'"
              :ref="(el) => setVideoRef(el, index)"
              @timeupdate="handleTimeUpdate(index, $event)"
              @loadedmetadata="handleLoadedMetadata(index, $event)"
              @error="handleVideoError(video)"
            ></video>
            <div v-if="video.error" class="video-error">
              <div class="video-error-title">Video unavailable</div>
              <div class="video-error-sub">请稍后再试</div>
              <button
                class="video-error-btn"
                :disabled="retryLoading[video.id]"
                @click.stop="retryVideo(video)"
              >
                {{ retryLoading[video.id] ? '重试中...' : '重试' }}
              </button>
            </div>
            <div class="player-info">
              <div class="player-author" @click.stop="openCreatorProfile(video)">
                <img class="avatar" :src="video.authorAvatar || defaultAvatar" alt="avatar" />
                <span>{{ video.author }}</span>
              </div>
              <h1>{{ video.title }}</h1>
              <p v-if="video.desc" class="desc">{{ video.desc }}</p>
            </div>
            <div class="player-progress">
              <span class="progress-time">{{ formatDuration(currentTimes[index] || 0) }}</span>
              <div
                class="progress-bar"
                :ref="(el) => setProgressRef(el, index)"
                @pointerdown.stop.prevent="handleSeekStart(index, $event)"
                @click.stop
              >
                <div class="progress-fill" :style="{ width: `${getProgress(index)}%` }">
                  <span class="progress-thumb"></span>
                </div>
              </div>
              <span class="progress-time">{{ formatDuration(durations[index] || 0) }}</span>
            </div>
          </div>

          <div class="player-actions">
            <button class="action" @click.stop>
              <img src="../assets/img/icon/love.svg" alt="like" />
              <span>{{ video.likeCount }}</span>
            </button>
            <button class="action" @click.stop="toggleComments">
              <img src="../assets/img/icon/message.svg" alt="comment" />
              <span>{{ video.commentCount }}</span>
            </button>
            <button class="action" @click.stop>
              <img src="../assets/img/icon/star-white.png" alt="collect" />
              <span>{{ video.collectCount }}</span>
            </button>
            <button class="action" @click.stop>
              <img src="../assets/img/icon/share-white.png" alt="share" />
              <span>分享</span>
            </button>
            <button class="action action-fullscreen" @click.stop="toggleFullscreen">
              <span class="fullscreen-icon">⛶</span>
              <span>{{ isFullscreen ? '退出' : '全屏' }}</span>
            </button>
          </div>
        </div>
      </article>
    </div>

    <aside class="comment-panel" :class="{ 'is-open': showComments }" @wheel.stop>
      <div class="comment-header">
        <div>
          <div class="comment-title">评论</div>
          <div class="comment-count">全部评论 {{ commentCount }}</div>
        </div>
        <button class="comment-close" @click="closeComments">×</button>
      </div>
      <div class="comment-list">
        <div v-for="item in currentComments" :key="item.id" class="comment-item">
          <img class="comment-avatar" :src="item.avatar" alt="avatar" />
          <div class="comment-body">
            <div class="comment-name">{{ item.user }}</div>
            <div class="comment-content">{{ item.content }}</div>
            <div class="comment-meta">
              <span>{{ item.time }}</span>
              <button class="comment-action" @click="handleReply(item)">回复</button>
              <button
                class="comment-like"
                :class="{ active: item.liked }"
                @click="toggleCommentLike(item)"
              >
                <img src="../assets/img/icon/love.svg" alt="like" />
                <span>{{ item.likes }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="comment-compose">
        <div v-if="replyTo" class="comment-replying">
          回复 @{{ replyTo.user }}
          <button class="comment-cancel" @click="clearReply">取消</button>
        </div>
        <div class="comment-input-row">
          <input
            v-model="newComment"
            type="text"
            class="comment-input"
            placeholder="说点什么…"
            @keydown.enter.exact.prevent="sendComment"
          />
          <button class="comment-send" :disabled="!canSend" @click="sendComment">发送</button>
        </div>
      </div>
    </aside>
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
  cursor: pointer;
}

.video-el {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
}

.video-error {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  z-index: 2;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  text-align: center;
}

.video-error-title {
  font-size: 16rem;
  font-weight: 600;
}

.video-error-sub {
  font-size: 12rem;
  color: rgba(226, 232, 240, 0.8);
  margin-top: 6rem;
}

.video-error-btn {
  margin-top: 14rem;
  border: none;
  border-radius: 999rem;
  padding: 8rem 16rem;
  background: linear-gradient(120deg, #f43f5e, #fb7185);
  color: #0b0d16;
  font-weight: 600;
  cursor: pointer;
}

.video-error-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.action-fullscreen .fullscreen-icon {
  font-size: 16rem;
  line-height: 1;
}

.player-info {
  position: absolute;
  left: 18rem;
  bottom: 52rem;
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
  cursor: pointer;
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

.player-progress {
  position: absolute;
  left: 18rem;
  right: 18rem;
  bottom: 16rem;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 10rem;
  font-size: 11rem;
  color: #e5e7eb;
}

.progress-time {
  min-width: 36rem;
  text-align: center;
}

.progress-bar {
  flex: 1;
  height: 4rem;
  background: rgba(255, 255, 255, 0.35);
  border-radius: 999rem;
  position: relative;
  cursor: pointer;
}

.progress-fill {
  height: 100%;
  background: #fff;
  border-radius: inherit;
  position: relative;
}

.progress-thumb {
  position: absolute;
  right: 0;
  top: 50%;
  width: 10rem;
  height: 10rem;
  border-radius: 999rem;
  background: #fff;
  transform: translate(50%, -50%);
  box-shadow: 0 2rem 8rem rgba(0, 0, 0, 0.35);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.progress-bar:hover .progress-thumb {
  opacity: 1;
}

.comment-panel {
  position: absolute;
  top: 0;
  right: 0;
  height: 100%;
  width: 360rem;
  background: linear-gradient(180deg, rgba(15, 18, 30, 0.98), rgba(10, 12, 20, 0.98));
  border-left: 1rem solid rgba(148, 163, 184, 0.2);
  transform: translateX(100%);
  transition: transform 0.25s ease;
  z-index: 4;
  display: flex;
  flex-direction: column;
}

.comment-panel.is-open {
  transform: translateX(0);
  box-shadow: -20rem 0 40rem rgba(0, 0, 0, 0.45);
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rem 20rem 12rem;
  border-bottom: 1rem solid rgba(148, 163, 184, 0.18);
}

.comment-title {
  font-size: 16rem;
  font-weight: 600;
}

.comment-count {
  font-size: 12rem;
  color: rgba(226, 232, 240, 0.7);
  margin-top: 4rem;
}

.comment-close {
  border: none;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  width: 28rem;
  height: 28rem;
  border-radius: 999rem;
  cursor: pointer;
  font-size: 18rem;
  line-height: 1;
}

.comment-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 14rem 20rem;
  display: grid;
  gap: 16rem;
}

.comment-item {
  display: flex;
  gap: 12rem;
  padding-bottom: 12rem;
  border-bottom: 1rem solid rgba(148, 163, 184, 0.12);
}

.comment-avatar {
  width: 36rem;
  height: 36rem;
  border-radius: 999rem;
}

.comment-body {
  display: grid;
  gap: 6rem;
}

.comment-name {
  font-weight: 600;
  font-size: 13rem;
}

.comment-content {
  font-size: 13rem;
  color: #e5e7eb;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 12rem;
  font-size: 11rem;
  color: rgba(226, 232, 240, 0.6);
}

.comment-action {
  border: none;
  background: transparent;
  color: rgba(226, 232, 240, 0.7);
  cursor: pointer;
  padding: 0;
}

.comment-action:hover {
  color: #fff;
}

.comment-like {
  border: none;
  background: transparent;
  color: rgba(226, 232, 240, 0.7);
  cursor: pointer;
  padding: 0;
  display: inline-flex;
  align-items: center;
  gap: 6rem;
}

.comment-like img {
  width: 14rem;
  opacity: 0.8;
}

.comment-like.active {
  color: #f43f5e;
}

.comment-like.active img {
  filter: drop-shadow(0 0 6rem rgba(244, 63, 94, 0.6));
  opacity: 1;
}

.comment-compose {
  border-top: 1rem solid rgba(148, 163, 184, 0.14);
  padding: 12rem 20rem 16rem;
  display: flex;
  flex-direction: column;
  gap: 10rem;
}

.comment-replying {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6rem 12rem;
  border-radius: 999rem;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(226, 232, 240, 0.8);
  font-size: 12rem;
}

.comment-cancel {
  border: none;
  background: transparent;
  color: rgba(226, 232, 240, 0.8);
  cursor: pointer;
}

.comment-input-row {
  display: flex;
  gap: 10rem;
}

.comment-input {
  flex: 1;
  background: rgba(255, 255, 255, 0.08);
  border: 1rem solid rgba(148, 163, 184, 0.2);
  border-radius: 999rem;
  padding: 8rem 14rem;
  color: #fff;
  font-size: 12rem;
  outline: none;
}

.comment-send {
  border: none;
  border-radius: 999rem;
  padding: 8rem 16rem;
  background: linear-gradient(120deg, #f43f5e, #fb7185);
  color: #0b0d16;
  font-weight: 600;
  cursor: pointer;
}

.comment-send:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.feed-container.is-fullscreen {
  background: #000;
}

.feed-container.is-fullscreen .player-frame {
  gap: 0;
}

.feed-container.is-fullscreen .player-cover {
  max-width: none;
  border-radius: 0;
  width: 100%;
  height: 100%;
}

.feed-container.is-fullscreen .player-actions {
  position: absolute;
  right: 24rem;
  bottom: 120rem;
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

  .comment-panel {
    width: 100%;
  }
}
</style>
