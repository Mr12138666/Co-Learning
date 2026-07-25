<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { NLayout, NLayoutHeader, NLayoutContent, NMenu, NButton, NSpace, NAvatar, NDropdown } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { h } from 'vue'
import { RouterLink } from 'vue-router'

const router = useRouter()
const authStore = useAuthStore()

const menuOptions: MenuOption[] = [
  {
    label: () => h(RouterLink, { to: '/dashboard' }, { default: () => '首页' }),
    key: 'dashboard',
  },
  {
    label: () => h(RouterLink, { to: '/tasks' }, { default: () => '任务' }),
    key: 'tasks',
  },
  {
    label: () => h(RouterLink, { to: '/stats' }, { default: () => '统计' }),
    key: 'stats',
  },
  {
    label: () => h(RouterLink, { to: '/journals' }, { default: () => '日志' }),
    key: 'journals',
  },
]

function handleUserAction(key: string) {
  if (key === 'logout') {
    authStore.logout().then(() => router.push({ name: 'login' }))
  } else if (key === 'profile') {
    // Will be implemented in later phases
  }
}

const userOptions = [
  { label: '个人资料', key: 'profile' },
  { label: '退出登录', key: 'logout' },
]
</script>

<template>
  <NLayout position="absolute">
    <NLayoutHeader bordered style="height: 56px; display: flex; align-items: center; padding: 0 24px; background: #fff;">
      <div style="font-weight: bold; font-size: 18px; color: #2080F0; margin-right: 32px;">
        Co-Learning
      </div>
      <NMenu mode="horizontal" :options="menuOptions" />
      <div style="flex: 1;" />
      <NDropdown :options="userOptions" @select="handleUserAction">
        <NSpace align="center" style="cursor: pointer;">
          <NAvatar round size="small" :src="authStore.user?.email ? `https://api.dicebear.com/7.x/avataaars/svg?seed=${authStore.user.email}` : undefined" />
          <span>{{ authStore.user?.email }}</span>
        </NSpace>
      </NDropdown>
    </NLayoutHeader>
    <NLayoutContent style="padding: 24px;">
      <RouterView />
    </NLayoutContent>
  </NLayout>
</template>
