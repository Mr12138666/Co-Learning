<script setup lang="ts">
import { computed, h } from 'vue'
import { NDropdown, NButton, NIcon } from 'naive-ui'
import { SunnyOutline, MoonOutline, ColorPaletteOutline } from '@vicons/ionicons5'
import { useThemeStore } from '@/stores/themeStore'
import { getThemeList } from '@/config/theme'
import type { ThemeName } from '@/config/theme'

const themeStore = useThemeStore()

// 获取主题列表并转换为下拉选项
const themeOptions = computed(() => {
  const themes = getThemeList()
  return themes.map(theme => ({
    label: theme.label,
    key: theme.name,
    icon: () => h(theme.isDark ? MoonOutline : SunnyOutline),
  }))
})

const currentIcon = computed(() => {
  return themeStore.isDark ? MoonOutline : ColorPaletteOutline
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
        <n-icon :component="currentIcon" />
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