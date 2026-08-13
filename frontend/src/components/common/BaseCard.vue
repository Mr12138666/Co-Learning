<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  /** Card title */
  title?: string
  /** Card subtitle */
  subtitle?: string
  /** Whether the card is hoverable */
  hoverable?: boolean
  /** Whether the card is clickable */
  clickable?: boolean
  /** Whether the card is loading */
  loading?: boolean
  /** Card variant */
  variant?: 'default' | 'outlined' | 'elevated' | 'flat'
  /** Card size */
  size?: 'small' | 'medium' | 'large'
  /** Custom class */
  class?: string
}>(), {
  hoverable: false,
  clickable: false,
  loading: false,
  variant: 'default',
  size: 'medium',
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const classes = computed(() => [
  'base-card',
  `base-card--${props.variant}`,
  `base-card--${props.size}`,
  {
    'base-card--hoverable': props.hoverable,
    'base-card--clickable': props.clickable,
    'base-card--loading': props.loading,
  },
  props.class,
])

function handleClick(event: MouseEvent) {
  if (props.clickable) {
    emit('click', event)
  }
}
</script>

<template>
  <div :class="classes" @click="handleClick">
    <div v-if="loading" class="base-card__loading">
      <slot name="loading">
        <div class="base-card__loading-content">
          <div class="base-card__loading-title skeleton" />
          <div class="base-card__loading-text skeleton" />
          <div class="base-card__loading-text skeleton base-card__loading-text--short" />
        </div>
      </slot>
    </div>
    <template v-else>
      <div v-if="title || subtitle || $slots.header" class="base-card__header">
        <slot name="header">
          <div v-if="title" class="base-card__title">{{ title }}</div>
          <div v-if="subtitle" class="base-card__subtitle">{{ subtitle }}</div>
        </slot>
      </div>
      <div v-if="$slots.default" class="base-card__body">
        <slot />
      </div>
      <div v-if="$slots.footer" class="base-card__footer">
        <slot name="footer" />
      </div>
    </template>
  </div>
</template>

<style scoped>
.base-card {
  background: var(--surface-1);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all 0.2s ease;
}

.base-card--default {
  border: 1px solid var(--border-color);
}

.base-card--outlined {
  border: 2px solid var(--border-color);
}

.base-card--elevated {
  box-shadow: var(--shadow-md);
  border: none;
}

.base-card--flat {
  background: var(--surface-2);
  border: none;
}

.base-card--hoverable:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.base-card--clickable {
  cursor: pointer;
}

.base-card--clickable:active {
  transform: translateY(0);
}

.base-card--loading {
  min-height: 120px;
}

.base-card--small {
  padding: var(--sp-3);
}

.base-card--medium {
  padding: var(--sp-4);
}

.base-card--large {
  padding: var(--sp-6);
}

.base-card__header {
  margin-bottom: var(--sp-3);
}

.base-card__title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-color);
  margin: 0;
}

.base-card__subtitle {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
  margin: var(--sp-1) 0 0;
}

.base-card__body {
  color: var(--text-color);
}

.base-card__footer {
  margin-top: var(--sp-3);
  padding-top: var(--sp-3);
  border-top: 1px solid var(--border-color);
}

.base-card__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.base-card__loading-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
}

.base-card__loading-title {
  height: 20px;
  width: 60%;
}

.base-card__loading-text {
  height: 12px;
  width: 100%;
}

.base-card__loading-text--short {
  width: 40%;
}

.skeleton {
  background: linear-gradient(90deg, var(--surface-2) 25%, var(--surface-3) 50%, var(--surface-2) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
  border-radius: var(--radius-xs);
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>