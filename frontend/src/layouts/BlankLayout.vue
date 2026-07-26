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
  background: var(--bg-page);
}

.blank-layout__content {
  width: 100%;
  max-width: 400px;
  padding: var(--sp-6) var(--sp-4);
}

@media (max-width: 480px) {
  .blank-layout__content {
    padding: var(--sp-4) var(--sp-3);
    max-width: 100%;
  }
}
</style>
