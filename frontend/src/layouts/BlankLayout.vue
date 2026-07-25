<script setup lang="ts">
</script>

<template>
  <div class="blank-layout">
    <div class="blank-layout__content">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.blank-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.blank-layout__content {
  width: 100%;
  max-width: 420px;
  padding: 24px;
}
</style>
