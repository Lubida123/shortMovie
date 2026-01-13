<script setup>
import VideoFeed from '../components/VideoFeed.vue'
</script>

<template>
  <div class="dy-home">
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
          <input type="text" placeholder="Search videos, creators, tags" />
        </div>
        <div class="top-right">
          <button class="ghost">Creator Center</button>
          <router-link to="/login" class="primary">Login</router-link>
        </div>
      </header>

      <VideoFeed />
    </main>
  </div>
</template>

<style scoped lang="less">
.dy-home {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 220rem 1fr;
  background: var(--main-bg);
  color: #fff;
}

.dy-side {
  background: var(--active-main-bg);
  padding: 18rem 16rem;
  display: grid;
  gap: 20rem;
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
  color: #cfd3e6;
  display: flex;
  align-items: center;
  gap: 12rem;
  padding: 10rem 12rem;
  border-radius: 12rem;
  cursor: pointer;
}

.nav-item img {
  width: 20rem;
}

.nav-item.active {
  background: rgba(252, 47, 86, 0.2);
  color: #fff;
}

.dy-main {
  display: grid;
  grid-template-rows: auto 1fr;
  padding: 16rem 24rem 30rem;
  gap: 16rem;
}

.dy-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--active-main-bg);
  padding: 10rem 16rem;
  border-radius: 16rem;
}

.search {
  display: flex;
  align-items: center;
  gap: 10rem;
  background: rgba(255, 255, 255, 0.1);
  padding: 8rem 14rem;
  border-radius: 999rem;
  width: min(520rem, 100%);
}

.search img {
  width: 18rem;
}

.search input {
  flex: 1;
  border: none;
  background: transparent;
  color: #fff;
  outline: none;
}

.top-right {
  display: flex;
  gap: 10rem;
}

.top-right button,
.top-right a {
  padding: 8rem 16rem;
  border-radius: 999rem;
  border: 1rem solid transparent;
  color: #fff;
  background: transparent;
  font-weight: 600;
}

.top-right .ghost {
  border-color: rgba(255, 255, 255, 0.3);
}

.top-right .primary {
  background: var(--primary-btn-color);
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
