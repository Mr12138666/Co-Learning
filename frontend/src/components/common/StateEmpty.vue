<script setup lang="ts">
import { NButton } from 'naive-ui'
import { Inbox } from 'lucide-vue-next'

defineProps<{
  title?: string
  description?: string
  actionText?: string
}>()

const emit = defineEmits<{
  action: []
}>()
</script>

<template>
  <div class="state-empty">
    <div class="state-empty__icon">
      <Inbox :size="18" />
    </div>
    <p class="state-empty__title">{{ title || '暂无数据' }}</p>
    <p v-if="description" class="state-empty__desc">{{ description }}</p>
    <NButton v-if="actionText" type="primary" size="small" @click="emit('action')">
      {{ actionText }}
    </NButton>
  </div>
</template>

<style scoped>
.state-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--sp-2);
  padding: var(--sp-8) var(--sp-4);
  min-height: 160px;
  animation: fadeIn var(--duration-md) var(--ease-enter);
}

.state-empty__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: var(--bg-page);
  border: 1px solid var(--separator);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-color-muted);
  margin-bottom: var(--sp-1);
}

.state-empty__title {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-color);
}

.state-empty__desc {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  text-align: center;
  max-width: 240px;
}
</style>
