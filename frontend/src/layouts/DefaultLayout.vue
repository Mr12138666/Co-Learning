<script setup lang="ts">
import { ref, computed, h, watch } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useThemeStore } from '@/stores/themeStore'
import {
  NLayout,
  NLayoutSider,
  NLayoutContent,
  NMenu,
  NButton,
  NAvatar,
  NDropdown,
  NIcon,
  NDrawer,
  NDrawerContent,
  NSwitch,
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import {
  HomeOutline,
  TimerOutline,
  ListOutline,
  BarChartOutline,
  PeopleOutline,
  TrophyOutline,
  PawOutline,
  TodayOutline,
  RibbonOutline,
  BookOutline,
  PersonOutline,
  MoonOutline,
  SunnyOutline,
  MenuOutline,
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const collapsed = ref(false)
const mobileDrawer = ref(false)
const isMobile = ref(window.innerWidth <= 768)

// Track active menu key from route
const activeKey = computed(() => {
  const path = route.path
  if (path === '/dashboard') return 'dashboard'
  if (path.startsWith('/tasks')) return 'tasks'
  if (path.startsWith('/goals')) return 'goals'
  if (path.startsWith('/subjects')) return 'subjects'
  if (path.startsWith('/stats')) return 'stats'
  if (path.startsWith('/rooms')) return 'rooms'
  if (path.startsWith('/leaderboard')) return 'leaderboard'
  if (path.startsWith('/pet')) return 'pet'
  if (path.startsWith('/daily-tasks')) return 'daily-tasks'
  if (path.startsWith('/achievements')) return 'achievements'
  if (path.startsWith('/journals')) return 'journals'
  if (path.startsWith('/profile')) return 'profile'
  return 'dashboard'
})

// Expanded keys for submenus
const expandedKeys = computed(() => {
  const keys: string[] = []
  if (route.path.startsWith('/goals') || route.path.startsWith('/subjects') || route.path.startsWith('/tasks')) {
    keys.push('study')
  }
  if (route.path.startsWith('/journals')) {
    keys.push('journals')
  }
  return keys
})

function renderIcon(icon: any) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions: MenuOption[] = [
  {
    label: () => h(RouterLink, { to: '/dashboard' }, { default: () => '首页' }),
    key: 'dashboard',
    icon: renderIcon(HomeOutline),
  },
  {
    label: '学习',
    key: 'study',
    icon: renderIcon(BookOutline),
    children: [
      { label: () => h(RouterLink, { to: '/tasks' }, { default: () => '任务清单' }), key: 'tasks' },
      { label: () => h(RouterLink, { to: '/goals' }, { default: () => '考试目标' }), key: 'goals' },
      { label: () => h(RouterLink, { to: '/subjects' }, { default: () => '科目管理' }), key: 'subjects' },
    ],
  },
  {
    label: () => h(RouterLink, { to: '/stats' }, { default: () => '学习统计' }),
    key: 'stats',
    icon: renderIcon(BarChartOutline),
  },
  {
    label: () => h(RouterLink, { to: '/rooms' }, { default: () => '自习室' }),
    key: 'rooms',
    icon: renderIcon(PeopleOutline),
  },
  {
    label: () => h(RouterLink, { to: '/leaderboard' }, { default: () => '排行榜' }),
    key: 'leaderboard',
    icon: renderIcon(TrophyOutline),
  },
  {
    label: () => h(RouterLink, { to: '/pet' }, { default: () => '宠物' }),
    key: 'pet',
    icon: renderIcon(PawOutline),
  },
  {
    label: () => h(RouterLink, { to: '/daily-tasks' }, { default: () => '每日任务' }),
    key: 'daily-tasks',
    icon: renderIcon(TodayOutline),
  },
  {
    label: () => h(RouterLink, { to: '/achievements' }, { default: () => '成就' }),
    key: 'achievements',
    icon: renderIcon(RibbonOutline),
  },
  {
    label: '日志',
    key: 'journals',
    icon: renderIcon(BookOutline),
    children: [
      { label: () => h(RouterLink, { to: '/journals' }, { default: () => '我的日志' }), key: 'my-journals' },
      { label: () => h(RouterLink, { to: '/journals/square' }, { default: () => '日志广场' }), key: 'journal-square' },
    ],
  },
]

// Bottom nav items for mobile
const bottomNavItems = [
  { key: 'dashboard', path: '/dashboard', label: '首页', icon: HomeOutline },
  { key: 'tasks', path: '/tasks', label: '任务', icon: ListOutline },
  { key: 'rooms', path: '/rooms', label: '自习室', icon: PeopleOutline },
  { key: 'stats', path: '/stats', label: '统计', icon: BarChartOutline },
  { key: 'profile', path: '/profile', label: '我的', icon: PersonOutline },
]

const userOptions = [
  { label: '个人资料', key: 'profile' },
  { label: '退出登录', key: 'logout' },
]

function handleUserAction(key: string) {
  if (key === 'logout') {
    authStore.logout().then(() => router.push({ name: 'login' }))
  } else if (key === 'profile') {
    router.push({ name: 'profile' })
  }
}

function handleMenuUpdate(key: string) {
  if (isMobile.value) {
    mobileDrawer.value = false
  }
}

// Handle resize
function handleResize() {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    mobileDrawer.value = false
  }
}

// Watch for route changes to close mobile drawer
watch(() => route.path, () => {
  if (isMobile.value) {
    mobileDrawer.value = false
  }
})

// Setup resize listener
if (typeof window !== 'undefined') {
  window.addEventListener('resize', handleResize)
}
</script>

<template>
  <NLayout has-sider class="app-layout" :style="{ height: '100vh' }">
    <!-- Desktop Sidebar -->
    <NLayoutSider
      v-if="!isMobile"
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      class="sidebar"
      :native-scrollbar="false"
    >
      <!-- Logo -->
      <div class="sidebar-logo" :class="{ 'sidebar-logo--collapsed': collapsed }">
        <div class="logo-icon">C</div>
        <span v-if="!collapsed" class="logo-text">Co-Learning</span>
      </div>

      <!-- Navigation -->
      <NMenu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="20"
        :options="menuOptions"
        :value="activeKey"
        :default-expanded-keys="expandedKeys"
        @update:value="handleMenuUpdate"
        class="sidebar-menu"
      />

      <!-- Bottom section -->
      <div class="sidebar-footer">
        <!-- Theme toggle -->
        <div class="sidebar-theme-toggle" :title="themeStore.theme === 'light' ? '切换深色模式' : '切换浅色模式'">
          <NSwitch
            :value="themeStore.theme === 'dark'"
            @update:value="themeStore.toggleTheme()"
            size="small"
          >
            <template #checked-icon>
              <NIcon :component="MoonOutline" :size="14" />
            </template>
            <template #unchecked-icon>
              <NIcon :component="SunnyOutline" :size="14" />
            </template>
          </NSwitch>
          <span v-if="!collapsed" class="theme-label">
            {{ themeStore.theme === 'dark' ? '深色模式' : '浅色模式' }}
          </span>
        </div>

        <!-- User -->
        <NDropdown :options="userOptions" @select="handleUserAction" placement="top-start">
          <div class="sidebar-user" :class="{ 'sidebar-user--collapsed': collapsed }">
            <NAvatar round :size="collapsed ? 32 : 36" :src="authStore.user?.avatarUrl || undefined">
              {{ authStore.user?.displayName?.charAt(0) || authStore.user?.email?.charAt(0) }}
            </NAvatar>
            <div v-if="!collapsed" class="user-info">
              <div class="user-name">{{ authStore.user?.displayName || authStore.user?.email?.split('@')[0] }}</div>
              <div class="user-email">{{ authStore.user?.email }}</div>
            </div>
          </div>
        </NDropdown>
      </div>
    </NLayoutSider>

    <!-- Main Content -->
    <NLayoutContent class="main-content" :native-scrollbar="false">
      <!-- Mobile Header -->
      <div v-if="isMobile" class="mobile-header">
        <NButton text @click="mobileDrawer = true" class="mobile-menu-btn">
          <NIcon :component="MenuOutline" :size="24" />
        </NButton>
        <div class="mobile-logo">Co-Learning</div>
        <NButton text @click="themeStore.toggleTheme()" class="mobile-theme-btn">
          <NIcon :component="themeStore.theme === 'dark' ? SunnyOutline : MoonOutline" :size="20" />
        </NButton>
      </div>

      <!-- Page Content -->
      <div class="page-content" :class="{ 'page-content--mobile': isMobile }">
        <RouterView />
      </div>

      <!-- Mobile Bottom Navigation -->
      <div v-if="isMobile" class="bottom-nav">
        <RouterLink
          v-for="item in bottomNavItems"
          :key="item.key"
          :to="item.path"
          class="bottom-nav-item"
          :class="{ 'bottom-nav-item--active': activeKey === item.key || (item.key === 'tasks' && activeKey === 'tasks') }"
        >
          <NIcon :component="item.icon" :size="20" />
          <span class="bottom-nav-label">{{ item.label }}</span>
        </RouterLink>
      </div>
    </NLayoutContent>

    <!-- Mobile Drawer -->
    <NDrawer
      v-model:show="mobileDrawer"
      placement="left"
      :width="280"
      class="mobile-drawer"
    >
      <NDrawerContent :native-scrollbar="false">
        <template #header>
          <div class="drawer-header">
            <div class="logo-icon">C</div>
            <span class="logo-text">Co-Learning</span>
          </div>
        </template>

        <NMenu
          :options="menuOptions"
          :value="activeKey"
          :default-expanded-keys="expandedKeys"
          @update:value="handleMenuUpdate"
        />

        <template #footer>
          <div class="drawer-footer">
            <div class="sidebar-theme-toggle">
              <NSwitch
                :value="themeStore.theme === 'dark'"
                @update:value="themeStore.toggleTheme()"
                size="small"
              >
                <template #checked-icon>
                  <NIcon :component="MoonOutline" :size="14" />
                </template>
                <template #unchecked-icon>
                  <NIcon :component="SunnyOutline" :size="14" />
                </template>
              </NSwitch>
              <span class="theme-label">
                {{ themeStore.theme === 'dark' ? '深色模式' : '浅色模式' }}
              </span>
            </div>

            <NDropdown :options="userOptions" @select="handleUserAction">
              <div class="sidebar-user">
                <NAvatar round :size="36" :src="authStore.user?.avatarUrl || undefined">
                  {{ authStore.user?.displayName?.charAt(0) || authStore.user?.email?.charAt(0) }}
                </NAvatar>
                <div class="user-info">
                  <div class="user-name">{{ authStore.user?.displayName || authStore.user?.email?.split('@')[0] }}</div>
                  <div class="user-email">{{ authStore.user?.email }}</div>
                </div>
              </div>
            </NDropdown>
          </div>
        </template>
      </NDrawerContent>
    </NDrawer>
  </NLayout>
</template>

<style scoped>
/* ===== Desktop Sidebar ===== */
.sidebar {
  background: var(--bg-card);
  transition: width var(--duration-normal) var(--ease-default);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-4) var(--sp-5);
  height: var(--header-height);
  border-bottom: 1px solid var(--border-default);
}

.sidebar-logo--collapsed {
  justify-content: center;
  padding: var(--sp-4) 0;
}

.logo-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--accent);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: var(--weight-bold);
  font-size: var(--text-lg);
  flex-shrink: 0;
}

.logo-text {
  font-size: var(--text-lg);
  font-weight: var(--weight-bold);
  color: var(--text-primary);
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  padding: var(--sp-2) 0;
}

.sidebar-footer {
  border-top: 1px solid var(--border-default);
  padding: var(--sp-3) var(--sp-4);
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
}

.sidebar-theme-toggle {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  padding: var(--sp-1) 0;
}

.theme-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-2);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color var(--duration-fast) var(--ease-default);
}

.sidebar-user:hover {
  background: var(--state-hover);
}

.sidebar-user--collapsed {
  justify-content: center;
  padding: var(--sp-1);
}

.user-info {
  overflow: hidden;
}

.user-name {
  font-size: var(--text-md);
  font-weight: var(--weight-medium);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-email {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== Main Content ===== */
.main-content {
  background: var(--bg-page);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.page-content {
  flex: 1;
  padding: var(--sp-6);
  max-width: var(--content-max-width);
  width: 100%;
  margin: 0 auto;
}

.page-content--mobile {
  padding: var(--sp-4);
  padding-top: calc(var(--header-height) + var(--sp-4));
  padding-bottom: calc(var(--bottom-nav-height) + var(--sp-4));
}

/* ===== Mobile Header ===== */
.mobile-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-height);
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-default);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--sp-4);
  z-index: var(--z-sticky);
}

.mobile-menu-btn,
.mobile-theme-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mobile-logo {
  font-size: var(--text-lg);
  font-weight: var(--weight-bold);
  color: var(--accent);
}

/* ===== Bottom Navigation ===== */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: var(--bottom-nav-height);
  background: var(--bg-card);
  border-top: 1px solid var(--border-default);
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: var(--z-sticky);
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.bottom-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: var(--sp-1) var(--sp-2);
  color: var(--text-tertiary);
  text-decoration: none;
  transition: color var(--duration-fast) var(--ease-default);
  border-radius: var(--radius-md);
  min-width: 56px;
}

.bottom-nav-item:active {
  background: var(--state-active);
}

.bottom-nav-item--active {
  color: var(--accent);
}

.bottom-nav-label {
  font-size: var(--text-xs);
  font-weight: var(--weight-medium);
}

/* ===== Mobile Drawer ===== */
.drawer-header {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
}

.drawer-footer {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
  padding: var(--sp-2) 0;
}
</style>
