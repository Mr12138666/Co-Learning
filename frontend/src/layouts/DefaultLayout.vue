<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useThemeStore } from '@/stores/themeStore'
import { NLayout, NLayoutHeader, NLayoutContent, NMenu, NButton, NSpace, NAvatar, NDropdown } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { h } from 'vue'
import { RouterLink } from 'vue-router'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

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
    label: () => h(RouterLink, { to: '/rooms' }, { default: () => '陪伴房' }),
    key: 'rooms',
  },
  {
    label: () => h(RouterLink, { to: '/leaderboard' }, { default: () => '排行' }),
    key: 'leaderboard',
  },
  {
    label: () => h(RouterLink, { to: '/pet' }, { default: () => '宠物' }),
    key: 'pet',
  },
  {
    label: () => h(RouterLink, { to: '/daily-tasks' }, { default: () => '每日任务' }),
    key: 'daily-tasks',
  },
  {
    label: () => h(RouterLink, { to: '/achievements' }, { default: () => '成就' }),
    key: 'achievements',
  },
  {
    label: '日志',
    key: 'journals',
    children: [
      { label: () => h(RouterLink, { to: '/journals' }, { default: () => '我的日志' }), key: 'my-journals' },
      { label: () => h(RouterLink, { to: '/journals/square' }, { default: () => '日志广场' }), key: 'journal-square' },
    ],
  },
]

function handleUserAction(key: string) {
  if (key === 'logout') {
    authStore.logout().then(() => router.push({ name: 'login' }))
  } else if (key === 'profile') {
    router.push({ name: 'profile' })
  }
}

const userOptions = [
  { label: '个人资料', key: 'profile' },
  { label: '退出登录', key: 'logout' },
]
</script>

<template>
  <NLayout position="absolute">
    <NLayoutHeader bordered class="layout-header">
      <div class="logo">Co-Learning</div>
      <NMenu mode="horizontal" :options="menuOptions" />
      <div class="header-right">
        <NButton text @click="themeStore.toggleTheme" class="theme-toggle">
          {{ themeStore.theme === 'light' ? '🌙 深色' : '☀️ 浅色' }}
        </NButton>
        <NDropdown :options="userOptions" @select="handleUserAction">
          <div style="display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 8px; border-radius: 8px;">
            <NAvatar round size="small" :src="authStore.user?.avatarUrl || undefined" style="--n-avatar-size-override: 36px;">
              {{ authStore.user?.displayName?.charAt(0) || authStore.user?.email?.charAt(0) }}
            </NAvatar>
            <span style="font-size: 14px; color: var(--text-primary);">{{ authStore.user?.displayName || authStore.user?.email?.split('@')[0] }}</span>
            <span style="font-size: 12px; color: var(--text-secondary);">▼</span>
          </div>
        </NDropdown>
      </div>
    </NLayoutHeader>
    <NLayoutContent class="layout-content">
      <RouterView />
    </NLayoutContent>
  </NLayout>
</template>

<style scoped>
.layout-header {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
}

.logo {
  font-weight: bold;
  font-size: 18px;
  color: var(--accent-primary);
  margin-right: 32px;
}

.header-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
}

.theme-toggle {
  font-size: 14px;
  padding: 6px 12px;
  border-radius: var(--radius-md);
  transition: all 0.2s ease;
}

.theme-toggle:hover {
  background-color: var(--bg-hover);
}

.layout-content {
  padding: 24px;
  min-height: 0;
  overflow: hidden;
}
</style>
