<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  src?: string | null
  size?: number
  round?: boolean
}>(), {
  src: null,
  size: 30,
  round: true,
})

const absoluteSrc = computed(() => {
  if (!props.src) return null
  if (props.src.startsWith('http://') || props.src.startsWith('https://')) return props.src
  return window.location.origin + props.src
})

const sizeStyle = computed(() => ({
  width: props.size + 'px',
  height: props.size + 'px',
  borderRadius: props.round ? '50%' : 'var(--radius-md)',
}))

const imgError = ref(false)

function onError() {
  imgError.value = true
}

import { ref } from 'vue'
</script>

<template>
  <div class="app-avatar" :style="sizeStyle">
    <img
      v-if="absoluteSrc && !imgError"
      :src="absoluteSrc"
      :style="sizeStyle"
      class="app-avatar__img"
      @error="onError"
    />
    <span v-else class="app-avatar__fallback">
      <slot />
    </span>
  </div>
</template>

<style scoped>
.app-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--surface-3);
}

.app-avatar__img {
  object-fit: cover;
  display: block;
}

.app-avatar__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-weight: var(--weight-semibold);
  color: var(--text-color-muted);
  background: var(--surface-3);
}
</style>
