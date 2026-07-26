<script setup lang="ts">
import { NSpin, NText } from 'naive-ui'

defineProps<{
  text?: string
  size?: 'small' | 'medium' | 'large'
}>()
</script>

<template>
  <div class="state-loading">
    <NSpin :size="size || 'medium'" />
    <NText v-if="text" depth="3" class="loading-text">{{ text }}</NText>
  </div>
</template>

<style scoped>
.state-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--sp-3);
  padding: var(--sp-10) var(--sp-6);
  min-height: 200px;
}

.loading-text {
  font-size: var(--text-sm);
}
</style>
