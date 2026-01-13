<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Mousewheel, Pagination } from 'swiper/modules'

const modules = [Mousewheel, Pagination]

const videos = ref([
  {
    id: 'v-1',
    title: 'Harbor Letter',
    author: '@luxiao',
    desc: 'Street light and voices at the end of the road.',
    duration: '00:28',
    cover: new URL('../assets/img/poster/1.jpg', import.meta.url).href,
  },
  {
    id: 'v-2',
    title: 'Metro End',
    author: '@chen',
    desc: 'Ambient sound and human whispers underground.',
    duration: '00:24',
    cover: new URL('../assets/img/poster/3.jpg', import.meta.url).href,
  },
])

const activeIndex = ref(0)
const isFullscreen = ref(false)

const handleSlideChange = (swiper) => {
  activeIndex.value = swiper.activeIndex
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

watch(isFullscreen, (value) => {
  document.body.classList.toggle('dy-fullscreen', value)
})

onBeforeUnmount(() => {
  document.body.classList.remove('dy-fullscreen')
})
</script>

<template>
  <section class="dy-player" :class="{ fullscreen: isFullscreen }">
    <swiper
      class="dy-swiper"
      :modules="modules"
      direction="vertical"
      :mousewheel="true"
      :pagination="{ clickable: true }"
      @slide-change="handleSlideChange"
    >
      <swiper-slide v-for="(video, index) in videos" :key="video.id">
        <div class="player-frame">
          <div
            class="player-cover"
            :style="{ backgroundImage: `url(${video.cover})` }"
            @dblclick="toggleFullscreen"
          >
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
                  <div class="progress-fill" :style="{ width: index === activeIndex ? '35%' : '0%' }"></div>
                </div>
                <span>{{ video.duration }}</span>
              </div>
            </div>

            <div class="player-actions">
              <button class="action">
                <img src="../assets/img/icon/love.svg" alt="like" />
                <span>446.9K</span>
              </button>
              <button class="action">
                <img src="../assets/img/icon/message.svg" alt="comment" />
                <span>59K</span>
              </button>
              <button class="action">
                <img src="../assets/img/icon/star-white.png" alt="collect" />
                <span>144K</span>
              </button>
              <button class="action">
                <img src="../assets/img/icon/share-white.png" alt="share" />
                <span>385K</span>
              </button>
              <button class="action" @click="toggleFullscreen">
                <img src="../assets/img/icon/rotate.svg" alt="fullscreen" />
                <span>{{ isFullscreen ? 'Exit' : 'Full' }}</span>
              </button>
            </div>

            <div class="player-user">
              <img class="avatar" src="../assets/img/avatar.png" alt="avatar" />
              <div>
                <div class="author">@luxiao</div>
                <div class="caption">what is your name · #2026</div>
              </div>
            </div>
          </div>
          <div class="player-footer">
            <span>Reason: city mood + blue tone</span>
            <span>Next: Sea Night · 00:24</span>
          </div>
        </div>
      </swiper-slide>
    </swiper>

    <div class="float-buttons">
      <button title="Up">
        <img src="../assets/img/icon/arrow-up.png" alt="up" />
      </button>
      <button title="Down">
        <img src="../assets/img/icon/next.svg" alt="down" />
      </button>
    </div>
  </section>
</template>

<style scoped lang="less">
.dy-player {
  position: relative;
  display: grid;
  place-items: center;
}

.dy-swiper {
  width: min(1200rem, 100%);
  height: 680rem;
}

.player-frame {
  width: 100%;
  display: grid;
  gap: 12rem;
}

.player-cover {
  position: relative;
  border-radius: 18rem;
  overflow: hidden;
  min-height: 560rem;
  background-position: center;
  background-size: cover;
  box-shadow: 0 24rem 60rem rgba(0, 0, 0, 0.45);
  cursor: pointer;
}

.player-overlay {
  height: 100%;
  padding: 20rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(180deg, rgba(12, 13, 18, 0.2), rgba(12, 13, 18, 0.8));
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
  height: 6rem;
  background: rgba(255, 255, 255, 0.35);
  border-radius: 999rem;
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
  color: var(--second-text-color);
}

.float-buttons {
  position: absolute;
  right: 18rem;
  top: 50%;
  transform: translateY(-50%);
  display: grid;
  gap: 10rem;
}

.float-buttons button {
  border: none;
  width: 36rem;
  height: 36rem;
  border-radius: 50%;
  background: rgba(18, 20, 30, 0.9);
  display: grid;
  place-items: center;
  cursor: pointer;
}

.float-buttons img {
  width: 16rem;
}

.dy-player.fullscreen {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.96);
  z-index: 999;
  padding: 20rem;
}

.dy-player.fullscreen .dy-swiper {
  height: calc(100vh - 40rem);
  width: min(1400rem, 100%);
}

.dy-player.fullscreen .player-cover {
  min-height: calc(100vh - 140rem);
}

@media (max-width: 980px) {
  .dy-swiper {
    height: 520rem;
  }

  .player-cover {
    min-height: 420rem;
  }

  .player-actions {
    position: static;
    grid-template-columns: repeat(5, minmax(80rem, 1fr));
    background: rgba(18, 20, 30, 0.7);
    padding: 10rem;
    border-radius: 14rem;
    margin-top: 12rem;
  }

  .float-buttons {
    display: none;
  }
}
</style>
