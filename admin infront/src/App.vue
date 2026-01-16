<template>
  <div id="app">
    <!-- 仅管理员路由出口 -->
    <router-view></router-view>
  </div>
</template>

<script setup>
// 无需额外逻辑，仅承载路由出口
</script>

<style lang="less">
#app {
  width: 100%;
  height: 100%;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>

