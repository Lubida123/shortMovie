<script setup>
import { computed, onMounted } from 'vue'
import { useUserStore } from '../store/userStore'
import VideoList from '../components/VideoList.vue'

const userStore = useUserStore()
const displayName = computed(
  () => userStore.userInfo?.nickname || userStore.userInfo?.username || '未登录用户'
)
const avatarUrl = computed(
  () =>
    userStore.userInfo?.avatarUrl ||
    new URL('../assets/img/avatar.png', import.meta.url).href
)

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
        <img src="../assets/logo.png" alt="Spark Movie" />
      </div>
      <nav class="side-nav">
        <button class="nav-item active">
          <img src="../assets/img/icon/home/hot.webp" alt="Recommend" />
          推荐
        </button>
        <button class="nav-item">
          <img src="../assets/img/icon/home/new.webp" alt="Featured" />
          精选
        </button>
        <button class="nav-item">
          <img src="../assets/img/icon/location.webp" alt="Nearby" />
          同城
        </button>
        <button class="nav-item">
          <img src="../assets/img/icon/home/followed.webp" alt="Following" />
          关注
        </button>
        <button class="nav-item">
          <img src="../assets/img/icon/live.webp" alt="Live" />
          直播
        </button>
        <button class="nav-item">
          <img src="../assets/img/icon/music.svg" alt="Drama" />
          短剧
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
          <router-link v-else to="/login" class="user-profile-link">登录/注册</router-link>
          <button class="ghost">创作中心</button>
          <router-link v-if="!userStore.token" to="/login" class="primary">登录</router-link>
        </div>
      </header>

      <section class="video-shell">
        <VideoList />
      </section>
    </main>

    <el-button
      circle
      size="large"
      class="upload-btn fixed bottom-20 right-4 z-50 shadow-lg border-none bg-gradient-to-r from-pink-500 to-red-500 hover:from-pink-600 hover:to-red-600 text-white"
    >
      <el-icon :size="24"><Plus /></el-icon>
    </el-button>
  </div>
</template>

<style scoped lang="less">
.dy-home {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240rem 1fr;
  background: var(--dy-bg-body);
  color: var(--dy-text-primary);
  position: relative;
}

.dy-side {
  background: var(--dy-bg-container);
  padding: 18rem 16rem;
  display: grid;
  gap: 20rem;
  border-right: var(--dy-border-default);
  height: 100vh;
  position: sticky;
  top: 0;
}

.side-logo img {
  width: 110rem;
}

.side-nav {
  display: grid;
  gap: 10rem;
}

.nav-item {
  border: none;
  background: transparent;
  color: var(--dy-text-tertiary);
  display: flex;
  align-items: center;
  gap: 12rem;
  padding: 10rem 12rem;
  border-radius: 12rem;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.nav-item img {
  width: 20rem;
}

.nav-item.active {
  background: rgba(254, 44, 85, 0.18);
  color: var(--dy-text-primary);
}

.nav-item:hover {
  background: var(--dy-bg-hover);
  color: var(--dy-text-primary);
  transform: translateX(4rem);
}

.dy-main {
  display: grid;
  grid-template-rows: auto 1fr;
  padding: 16rem 32rem 30rem;
  gap: 16rem;
  background: radial-gradient(circle at top, rgba(35, 40, 58, 0.35), transparent 60%);
}

.dy-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(22, 22, 24, 0.8);
  padding: 10rem 16rem;
  border-radius: 16rem;
  box-shadow: 0 12rem 30rem rgba(0, 0, 0, 0.35);
  position: sticky;
  top: 12rem;
  z-index: 10;
  backdrop-filter: blur(20px);
  height: 60rem;
}

.search {
  display: flex;
  align-items: center;
  gap: 10rem;
  background: rgba(255, 255, 255, 0.08);
  padding: 8rem 14rem;
  border-radius: 20rem;
  width: min(520rem, 100%);
  height: 40rem;
}

.search img {
  width: 18rem;
}

.search input {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--dy-text-primary);
  outline: none;
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
}

.top-right .ghost {
  border-color: rgba(255, 255, 255, 0.28);
}

.top-right .primary {
  background: var(--dy-brand-red);
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

.video-shell {
  display: flex;
  justify-content: center;
}

@media (max-width: 980px) {
  .dy-home {
    grid-template-columns: 1fr;
  }

  .dy-side {
    grid-template-columns: repeat(auto-fit, minmax(120rem, 1fr));
  }
}
</style>
