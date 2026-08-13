<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { NConfigProvider, NMessageProvider, NDialogProvider, NGlobalStyle, zhCN, dateZhCN, darkTheme, lightTheme } from 'naive-ui'
import { useThemeStore } from '@/stores/themeStore'
import { useAuthStore } from '@/stores/authStore'

const themeStore = useThemeStore()
const authStore = useAuthStore()

onMounted(() => {
  themeStore.initTheme()
  // Refresh user data on app load to sync avatarUrl and other profile changes
  if (authStore.isAuthenticated) {
    authStore.refresh().catch(() => {
      // Token invalid/expired — clear stale auth state
      authStore.clearAuth()
    })
  }
})

const naiveTheme = computed(() => themeStore.theme === 'dark' ? darkTheme : lightTheme)
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
