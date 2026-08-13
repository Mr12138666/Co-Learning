<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  text?: string
  size?: 'small' | 'medium' | 'large'
}>()

const rowCount = computed(() => (props.size === 'small' ? 2 : props.size === 'large' ? 5 : 3))
</script>

<template>
  <div class="state-loading glass glass--subtle ambient-pulse" role="status" :aria-label="text || '加载中'">
    <div v-for="i in rowCount" :key="i" class="state-loading__row">
      <span class="state-loading__circle skeleton" />
      <span class="state-loading__lines">
        <span class="state-loading__line skeleton" :style="{ width: `${72 - i * 8}%` }" />
        <span class="state-loading__line state-loading__line--sub skeleton" :style="{ width: `${40 - i * 4}%` }" />
      </span>
    </div>
    <p v-if="text" class="state-loading__text">{{ text }}</p>
  </div>
</template>

<style scoped>
.state-loading {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
  padding: var(--sp-6) var(--sp-4);
  min-height: 160px;
  border-radius: var(--radius-lg);
}

.state-loading__row {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
}

.state-loading__circle {
  width: 18px;
  height: 18px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.state-loading__lines {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--sp-1);
  min-width: 0;
}

.state-loading__line {
  height: 12px;
  border-radius: var(--radius-xs);
}

.state-loading__line--sub {
  height: 8px;
  opacity: 0.7;
}

.state-loading__text {
  margin: var(--sp-2) 0 0;
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  text-align: center;
}

.skeleton {
  background: linear-gradient(90deg, var(--surface-2) 25%, var(--surface-3) 50%, var(--surface-2) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
