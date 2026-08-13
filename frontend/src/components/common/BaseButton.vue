<script setup lang="ts">
import { computed } from 'vue'
import { NButton, NIcon } from 'naive-ui'
import type { ButtonProps } from 'naive-ui'

const props = withDefaults(defineProps<{
  /** Button variant */
  variant?: 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info' | 'text' | 'ghost'
  /** Button size */
  size?: 'tiny' | 'small' | 'medium' | 'large'
  /** Whether the button is loading */
  loading?: boolean
  /** Whether the button is disabled */
  disabled?: boolean
  /** Whether the button is block */
  block?: boolean
  /** Whether the button is round */
  round?: boolean
  /** Whether the button is circle */
  circle?: boolean
  /** Icon to display */
  icon?: Component
  /** Icon position */
  iconPosition?: 'left' | 'right'
  /** Button type */
  type?: 'default' | 'primary' | 'info' | 'success' | 'warning' | 'error'
  /** Native type */
  nativeType?: 'button' | 'submit' | 'reset'
  /** Custom class */
  class?: string
}>(), {
  variant: 'default',
  size: 'medium',
  loading: false,
  disabled: false,
  block: false,
  round: false,
  circle: false,
  iconPosition: 'left',
  type: 'default',
  nativeType: 'button',
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const naiveType = computed(() => {
  const typeMap: Record<string, ButtonProps['type']> = {
    default: 'default',
    primary: 'primary',
    secondary: 'default',
    success: 'success',
    warning: 'warning',
    error: 'error',
    info: 'info',
    text: 'default',
    ghost: 'default',
  }
  return typeMap[props.variant] || 'default'
})

const classes = computed(() => [
  'base-button',
  `base-button--${props.variant}`,
  {
    'base-button--block': props.block,
    'base-button--round': props.round,
    'base-button--circle': props.circle,
    'base-button--loading': props.loading,
  },
  props.class,
])

function handleClick(event: MouseEvent) {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<template>
  <n-button
    :type="naiveType"
    :size="size"
    :loading="loading"
    :disabled="disabled"
    :block="block"
    :round="round"
    :circle="circle"
    :native-type="nativeType"
    :class="classes"
    @click="handleClick"
  >
    <template v-if="icon && iconPosition === 'left'" #icon>
      <n-icon :component="icon" />
    </template>
    <slot />
    <template v-if="icon && iconPosition === 'right'" #icon>
      <n-icon :component="icon" />
    </template>
  </n-button>
</template>

<style scoped>
.base-button {
  transition: all 0.2s ease;
}

.base-button--secondary {
  background: var(--surface-2);
  border-color: var(--border-color);
  color: var(--text-color);
}

.base-button--secondary:hover {
  background: var(--surface-3);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.base-button--text {
  background: transparent;
  border: none;
  color: var(--primary-color);
  padding: 0;
  height: auto;
}

.base-button--text:hover {
  color: var(--primary-color-hover);
  text-decoration: underline;
}

.base-button--ghost {
  background: transparent;
  border-color: var(--border-color);
  color: var(--text-color);
}

.base-button--ghost:hover {
  background: var(--surface-2);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.base-button--block {
  width: 100%;
}

.base-button--round {
  border-radius: var(--radius-full);
}

.base-button--circle {
  border-radius: var(--radius-full);
  padding: 0;
}

.base-button--loading {
  opacity: 0.8;
  cursor: not-allowed;
}
</style>