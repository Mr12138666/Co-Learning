<script setup lang="ts">
import { computed } from 'vue'
import { NDropdown, NButton, NIcon } from 'naive-ui'
import { SunnyOutline, MoonOutline, ColorPaletteOutline } from '@vicons/ionicons5'
import { useThemeStore } from '@/stores/themeStore'
import type { ThemeName } from '@/config/theme'

const themeStore = useThemeStore()

const themeOptions: any[] = [
  {
    label: '浅色模式',
    key: 'light',
    icon: SunnyOutline,
  },
  {
    label: '深色模式',
    key: 'dark',
    icon: MoonOutline,
  },
  {
    type: 'divider',
    key: 'd1',
  },
  {
    label: '蓝色主题',
    key: 'blue',
  },
  {
    label: '绿色主题',
    key: 'green',
  },
  {
    label: '紫色主题',
    key: 'purple',
  },
]

const currentIcon = computed(() => {
  return themeStore.isDark ? MoonOutline : SunnyOutline
})

function handleSelect(key: string) {
  themeStore.setTheme(key as ThemeName)
}
</script>

<template>
  <n-dropdown
    :options="themeOptions"
    trigger="click"
    @select="handleSelect"
  >
    <n-button quaternary circle size="small">
      <template #icon>
        <n-icon :component="ColorPaletteOutline" />
      </template>
    </n-button>
  </n-dropdown>
</template>

<style scoped>
.theme-switcher {
  display: inline-flex;
  align-items: center;
}
</style>