<template>
  <div class="customer-layout">
    <div class="content-wrapper">
      <router-view />
    </div>
  </div>
</template>

<script setup>
// 客户布局组件
</script>

<style scoped>
.customer-layout {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  /* 确保布局容器不阻止滚动 */
  overflow: visible;
}

.content-wrapper {
  flex: 1;
  width: 100%;
  /* 移除高度限制，让内容自然扩展 */
  height: auto;
  min-height: auto;
  overflow: visible;
}
</style>

