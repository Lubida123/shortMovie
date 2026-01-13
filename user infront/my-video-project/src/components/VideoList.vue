<script setup>
import { ref } from 'vue'

const videos = ref([
  { id: 'r-01', title: '雾港来信', author: '许言', duration: '00:32' },
  { id: 'r-02', title: '地铁尽头', author: '陈望', duration: '00:18' },
  { id: 'r-03', title: '海边夏夜', author: '沈夏', duration: '00:28' },
])
</script>

<template>
  <section class="video-list">
    <header>
      <h2>推荐视频</h2>
      <p>占位数据，用于后续接入推荐接口。</p>
    </header>
    <ul>
      <li v-for="video in videos" :key="video.id">
        <div>
          <strong>{{ video.title }}</strong>
          <span>@{{ video.author }}</span>
        </div>
        <span class="duration">{{ video.duration }}</span>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.video-list {
  display: grid;
  gap: 12px;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 10px;
}

li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
}

strong {
  display: block;
}

.duration {
  font-size: 0.85rem;
  color: #6b6b6b;
}
</style>
