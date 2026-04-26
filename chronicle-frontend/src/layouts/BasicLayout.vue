<template>
  <div class="basic-layout">
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts"></script>

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

