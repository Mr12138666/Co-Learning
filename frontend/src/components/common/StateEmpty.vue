<script setup lang="ts">
import { NEmpty, NButton } from 'naive-ui'

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
    <NEmpty :description="title || '暂无数据'" size="large">
      <template #extra>
        <p v-if="description" class="empty-description">{{ description }}</p>
        <NButton v-if="actionText" type="primary" @click="emit('action')">
          {{ actionText }}
        </NButton>
      </template>
    </NEmpty>
  </div>
</template>

<style scoped>
.state-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--sp-10) var(--sp-6);
  min-height: 200px;
}

.empty-description {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin-bottom: var(--sp-4);
  text-align: center;
  max-width: 280px;
}
</style>
