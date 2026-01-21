<script setup>
import { computed, onMounted, defineAsyncComponent, ref } from 'vue'
import { useUserStore } from '../store/userStore'
const VideoFeed = defineAsyncComponent(() => import('../components/VideoFeed.vue'))

const userStore = useUserStore()
const displayName = computed(
  () => userStore.userInfo?.nickname || userStore.userInfo?.username || '未登录用户'
)
const avatarUrl = computed(
  () =>
    userStore.userInfo?.avatarUrl ||
    new URL('../assets/img/avatar.png', import.meta.url).href
)
const navItems = [
  {
    id: 'recommend',
    label: '推荐',
    icon: new URL('../assets/img/icon/home/new.webp', import.meta.url).href,
  },
  {
    id: 'hot',
    label: '热门',
    icon: new URL('../assets/img/icon/home/hot.webp', import.meta.url).href,
  },
]
const activeFeed = ref('recommend')

onMounted(() => {
  if (userStore.token) {
    userStore.fetchProfile()
  }
})
</script>

<template>
  <div class="dy-home bg-black">

    <aside class="dy-side">
      <div class="side-logo">
        <img src="../assets/log.png" alt="Spark Movie" class="logo-img" />
      </div>
      <nav class="side-nav">
        <button
          v-for="item in navItems"
          :key="item.id"
          class="nav-item"
          :class="{ active: activeFeed === item.id }"
          :aria-pressed="activeFeed === item.id"
          @click="activeFeed = item.id"
        >
          <img :src="item.icon" :alt="item.label" />
          <span class="nav-label">{{ item.label }}</span>
        </button>
      </nav>
    </aside>

    <main class="dy-main">
      <header class="dy-topbar">
        <div class="search">
          <img src="../assets/img/icon/search-light.png" alt="search" />
          <input type="text" placeholder="搜索视频 / 创作者 / 标签" />
        </div>
        <div class="top-right">
          <router-link
            v-if="userStore.token"
            to="/profile"
            class="user-profile-link"
          >
            <el-avatar
              :size="28"
              :src="userStore.userInfo?.avatar || avatarUrl"
              class="user-avatar"
            />
            <span>{{ displayName }}</span>
          </router-link>
          <span v-if="userStore.token" class="status-pill">在线</span>
          <button v-if="userStore.token" class="ghost">创作中心</button>
          <router-link
            v-if="!userStore.token"
            to="/login"
            class="login-link primary-action"
          >
            登录/注册
          </router-link>
          <router-link
            v-else
            to="/profile"
            class="login-link primary-action user-center"
          >
            用户中心
          </router-link>
        </div>
      </header>

      <section class="video-shell">
        <VideoFeed :feed-type="activeFeed" />
      </section>
    </main>

    <el-button
      circle
      size="large"
      class="upload-btn fixed bottom-20 right-4 z-50 shadow-lg border-none bg-gradient-to-r from-pink-500 to-red-500 hover:from-pink-600 hover:to-red-600 text-white"
    >
      <span class="plus-icon">+</span>
    </el-button>
  </div>
</template>

<style scoped lang="less">
.dy-home {
  height: 100vh;
  display: grid;
  grid-template-columns: 200rem 1fr;
  background: var(--dy-bg-body);
  color: var(--dy-text-primary);
  position: relative;
  overflow: hidden;
}

.dy-home::before,
.dy-home::after {
  content: '';
  position: fixed;
  inset: auto;
  width: 380rem;
  height: 380rem;
  filter: blur(80px);
  opacity: 0.55;
  z-index: 0;
  pointer-events: none;
}

.dy-home::before {
  top: -120rem;
  right: -120rem;
  background: radial-gradient(circle, rgba(34, 211, 238, 0.7), transparent 60%);
}

.dy-home::after {
  bottom: -160rem;
  left: 180rem;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.6), transparent 60%);
}

.dy-side {
  background: linear-gradient(180deg, rgba(15, 18, 29, 0.95) 0%, rgba(12, 14, 22, 0.95) 100%);
  padding: 18rem 14rem;
  display: flex;
  flex-direction: column;
  gap: 14rem;
  border-right: 1px solid rgba(148, 163, 184, 0.12);
  height: 100vh;
  position: sticky;
  top: 0;
  z-index: 1;
}

.side-logo img {
  width: 92rem;
}

.logo-img {
  border-radius: 18rem;
  padding: 6rem;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.35), rgba(34, 211, 238, 0.2));
  border: 1px solid rgba(148, 163, 184, 0.28);
  box-shadow: 0 10rem 24rem rgba(15, 23, 42, 0.6), 0 0 24rem rgba(34, 211, 238, 0.25);
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 8rem;
}

.nav-item {
  border: none;
  background: transparent;
  color: var(--dy-text-secondary);
  display: flex;
  align-items: center;
  gap: 12rem;
  padding: 8rem 10rem;
  border-radius: 12rem;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
  font-size: 12rem;
  min-height: 36rem;
  width: 100%;
}

.nav-item img {
  width: 16rem;
  height: 16rem;
  object-fit: contain;
  opacity: 0.9;
}

.nav-label {
  font-weight: 600;
}

.nav-item.active {
  background: linear-gradient(120deg, rgba(59, 130, 246, 0.22), rgba(34, 211, 238, 0.16));
  color: var(--dy-text-primary);
  box-shadow: 0 6rem 14rem rgba(59, 130, 246, 0.18);
}

.nav-item:hover {
  background: var(--dy-bg-hover);
  color: var(--dy-text-primary);
  transform: translateX(2rem);
}

.dy-main {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 20rem 36rem 40rem;
  gap: 18rem;
  background: radial-gradient(circle at top, rgba(35, 44, 80, 0.4), transparent 60%);
  position: relative;
  z-index: 1;
}

.dy-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(15, 18, 29, 0.86);
  padding: 12rem 18rem;
  border-radius: 18rem;
  box-shadow: var(--dy-shadow-soft);
  position: sticky;
  top: 12rem;
  z-index: 10;
  backdrop-filter: blur(20px);
  height: 64rem;
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.search {
  display: flex;
  align-items: center;
  gap: 10rem;
  background: rgba(255, 255, 255, 0.06);
  padding: 8rem 16rem;
  border-radius: 999rem;
  width: min(520rem, 100%);
  height: 42rem;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.search img {
  width: 18rem;
  opacity: 0.75;
}

.search input {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--dy-text-primary);
  outline: none;
  font-size: 14rem;
}

.top-right {
  display: flex;
  gap: 10rem;
  align-items: center;
}

.top-right button,
.top-right a {
  padding: 8rem 16rem;
  border-radius: 999rem;
  border: 1rem solid transparent;
  color: var(--dy-text-primary);
  background: transparent;
  font-weight: 600;
  font-size: 13rem;
  line-height: 1;
}

.top-right .ghost {
  border-color: rgba(148, 163, 184, 0.35);
}

.user-profile-link {
  display: inline-flex;
  align-items: center;
  gap: 8rem;
  padding: 6rem 12rem;
  border-radius: 999rem;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  text-decoration: none;
  font-weight: 600;
  text-shadow: 0 2rem 6rem rgba(0, 0, 0, 0.6);
}

.user-avatar {
  border: 1rem solid rgba(255, 255, 255, 0.5);
}

.status-pill {
  padding: 4rem 10rem;
  border-radius: 999rem;
  font-size: 11rem;
  font-weight: 600;
  color: #0f172a;
  background: linear-gradient(120deg, rgba(34, 211, 238, 0.95), rgba(56, 189, 248, 0.95));
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.login-link {
  padding: 8rem 18rem;
  border-radius: 999rem;
  border: 1px solid rgba(148, 163, 184, 0.2);
  color: var(--dy-text-primary);
  font-weight: 600;
  font-size: 13rem;
}

.primary-action {
  background: linear-gradient(120deg, var(--dy-brand-blue), var(--dy-brand-cyan));
  color: #0f172a;
  box-shadow: 0 10rem 20rem rgba(59, 130, 246, 0.25);
  border: none;
}

.user-center {
  background: linear-gradient(120deg, var(--dy-brand-cyan), var(--dy-brand-blue));
}

.video-shell {
  display: flex;
  justify-content: center;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  align-items: stretch;
}

.video-shell > :deep(.feed-container) {
  height: 100%;
  width: 100%;
}

.plus-icon {
  font-size: 24rem;
  font-weight: 600;
  line-height: 1;
}

@media (max-width: 980px) {
  .dy-home {
    grid-template-columns: 1fr;
  }

  .dy-side {
    grid-template-columns: repeat(auto-fit, minmax(120rem, 1fr));
    position: relative;
    height: auto;
    border-right: none;
  }

  .dy-main {
    padding: 16rem 20rem 32rem;
  }

  .dy-topbar {
    flex-direction: column;
    align-items: stretch;
    gap: 10rem;
    height: auto;
    padding: 12rem;
  }

  .search {
    width: 100%;
  }

  .top-right {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
  }
}
</style>
