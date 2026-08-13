<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { NConfigProvider, NMessageProvider, NDialogProvider, NGlobalStyle, zhCN, dateZhCN, darkTheme, lightTheme } from 'naive-ui'
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
      console.log('[Auth] Profile synced, avatarUrl:', profile.avatarUrl)
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

const naiveTheme = computed(() => themeStore.isDark ? darkTheme : lightTheme)
</script>

<template>
  <NConfigProvider :locale="zhCN" :date-locale="dateZhCN" :theme="naiveTheme">
    <NGlobalStyle />
    <NMessageProvider>
      <NDialogProvider>
        <RouterView />
      </NDialogProvider>
    </NMessageProvider>
  </NConfigProvider>
</template>