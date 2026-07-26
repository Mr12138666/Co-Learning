<script setup lang="ts">
import { NButton } from 'naive-ui'
import { TriangleAlert } from 'lucide-vue-next'

defineProps<{
  title?: string
  description?: string
}>()

const emit = defineEmits<{
  retry: []
}>()
</script>

<template>
  <div class="state-error">
    <div class="state-error__icon">
      <TriangleAlert :size="18" />
    </div>
    <p class="state-error__title">{{ title || '加载失败' }}</p>
    <p class="state-error__desc">{{ description || '请检查网络连接后重试' }}</p>
    <NButton type="primary" size="small" @click="emit('retry')">重新加载</NButton>
  </div>
</template>

<style scoped>
.state-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--sp-2);
  padding: var(--sp-8) var(--sp-4);
  min-height: 160px;
  animation: fadeIn var(--duration-md) var(--ease-enter);
}

.state-error__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: var(--danger-muted);
  border: 1px solid var(--danger);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--danger);
  margin-bottom: var(--sp-1);
}

.state-error__title {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-color);
}

.state-error__desc {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  text-align: center;
  max-width: 240px;
}
</style>
