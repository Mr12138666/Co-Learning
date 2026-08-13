<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { NConfigProvider, NMessageProvider, NDialogProvider, NGlobalStyle, zhCN, dateZhCN, darkTheme, lightTheme } from 'naive-ui'
import type { GlobalThemeOverrides } from 'naive-ui'
import { useThemeStore } from '@/stores/themeStore'
import { useAuthStore } from '@/stores/authStore'
import { userApi } from '@/api/user'

const themeStore = useThemeStore()
const authStore = useAuthStore()

async function syncUserProfile() {
  if (!authStore.isAuthenticated) return
  try {
    const res = await userApi.getMyProfile()
    const profile = res.data.data
    if (profile && authStore.user) {
      authStore.user.avatarUrl = profile.avatarUrl
      authStore.user.displayName = profile.displayName
    }
  } catch {
    try {
      await authStore.refresh()
    } catch {
      authStore.clearAuth()
    }
  }
}

onMounted(() => {
  themeStore.initTheme()
  syncUserProfile()
})

// 基础主题（深色/浅色）
const baseTheme = computed(() => themeStore.isDark ? darkTheme : lightTheme)

// 自定义主题覆盖，让Naive UI使用我们的CSS变量
const themeOverrides = computed<GlobalThemeOverrides>(() => {
  const colors = themeStore.currentTheme.colors
  return {
    common: {
      primaryColor: colors.accent500,
      primaryColorHover: colors.accent400,
      primaryColorPressed: colors.accent600,
      primaryColorSuppl: colors.accent200,
    },
  }
})
</script>

<template>
  <NConfigProvider 
    :locale="zhCN" 
    :date-locale="dateZhCN" 
    :theme="baseTheme"
    :theme-overrides="themeOverrides"
  >
    <NGlobalStyle />
    <NMessageProvider>
      <NDialogProvider>
        <RouterView />
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>