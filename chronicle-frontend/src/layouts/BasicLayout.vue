<template>
  <div class="basic-layout">
    <div class="main-content">
      <router-view />
    </div>

    <!-- 底部版权信息 -->
    <GlobalFooter />
  </div>
</template>

<script setup lang="ts">
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  background: none;
}

.main-content {
  width: 100%;
  padding: 0;
  background: none;
  margin: 0;
}
</style>
