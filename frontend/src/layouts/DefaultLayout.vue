<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, h, type Component } from 'vue'
import { useRouter, useRoute, RouterLink, RouterView } from 'vue-router'
import { NAvatar, NDropdown, NDrawer, NDrawerContent, NInput, NTooltip, NPopconfirm, useMessage } from 'naive-ui'
import {
  Sun, Moon, Inbox, CalendarRange, Clock, LayoutGrid, ClipboardCheck,
  BarChart3, Users, BookOpen, Trophy, PawPrint, Award, ListChecks,
  Plus, PanelLeftClose, PanelLeftOpen, Menu, ChevronRight, LogOut, User,
  Hash, BookMarked, Search, X,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/authStore'
import { useThemeStore } from '@/stores/themeStore'
import { useStudyStore } from '@/stores/studyStore'
import { useTaskStore } from '@/stores/taskStore'
import { getErrorMessage } from '@/utils/http-error'
import FocusMiniPlayer from '@/components/focus/FocusMiniPlayer.vue'
import QuickAddTask from '@/components/task/QuickAddTask.vue'
import CommandPalette from '@/components/common/CommandPalette.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const studyStore = useStudyStore()
const taskStore = useTaskStore()
const message = useMessage()

const STORAGE_COLLAPSED = 'sidebar-collapsed'
const collapsed = ref(localStorage.getItem(STORAGE_COLLAPSED) === '1')
const mobileDrawer = ref(false)
const isMobile = ref(window.innerWidth <= 768)

interface NavItem { key: string; to: string; label: string; icon: Component }

const primaryNav: NavItem[] = [
  { key: 'today', to: '/today', label: '今日', icon: Sun },
  { key: 'inbox', to: '/inbox', label: '收件箱', icon: Inbox },
  { key: 'planner', to: '/planner', label: '规划', icon: CalendarRange },
  { key: 'schedule', to: '/schedule', label: '日程', icon: Clock },
  { key: 'boards', to: '/boards', label: '看板', icon: LayoutGrid },
]

const secondaryNav: NavItem[] = [
  { key: 'checkin', to: '/checkin', label: '每日复盘', icon: ClipboardCheck },
  { key: 'stats', to: '/stats', label: '学习统计', icon: BarChart3 },
  { key: 'rooms', to: '/rooms', label: '自习室', icon: Users },
  { key: 'journals', to: '/journals', label: '日志', icon: BookOpen },
  { key: 'leaderboard', to: '/leaderboard', label: '排行榜', icon: Trophy },
  { key: 'pet', to: '/pet', label: '宠物', icon: PawPrint },
  { key: 'achievements', to: '/achievements', label: '成就', icon: Award },
  { key: 'daily-tasks', to: '/daily-tasks', label: '每日任务', icon: ListChecks },
]

const pageTitle = computed(() => route.meta.title ?? '')
const inboxCount = ref(0)
const paletteShow = ref(false)

function onGlobalKeydown(e: KeyboardEvent) {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    paletteShow.value = true
  }
}

function isActive(to: string): boolean {
  return route.path === to || route.path.startsWith(to + '/')
}

// ===== Sidebar Subjects / Tags sections =====
const subjectsOpen = ref(true)
const tagsOpen = ref(true)
const addingSubject = ref(false)
const addingTag = ref(false)
const newSubject = ref('')
const newTag = ref('')

async function loadSidebarData() {
  await Promise.all([
    studyStore.subjects.length ? Promise.resolve() : studyStore.fetchSubjects().catch(() => {}),
    taskStore.tags.length ? Promise.resolve() : taskStore.loadTags().catch(() => {}),
    refreshInboxCount(),
  ])
}

async function refreshInboxCount() {
  try {
    inboxCount.value = (await taskStore.fetchInbox()).length
  } catch { /* ignore */ }
}

let subjectBusy = false
let tagBusy = false

async function submitSubject() {
  const name = newSubject.value.trim()
  if (!name) { addingSubject.value = false; return }
  if (subjectBusy) return
  subjectBusy = true
  newSubject.value = ''
  addingSubject.value = false
  try {
    await studyStore.createSubject({ name })
  } catch (e) { message.error(getErrorMessage(e, '创建科目失败')) }
  finally { subjectBusy = false }
}

async function submitTag() {
  const name = newTag.value.trim()
  if (!name) { addingTag.value = false; return }
  if (tagBusy) return
  tagBusy = true
  newTag.value = ''
  addingTag.value = false
  try {
    await taskStore.createTag(name)
  } catch (e) { message.error(getErrorMessage(e, '创建标签失败')) }
  finally { tagBusy = false }
}

// ===== User / theme / responsive =====
const userOptions = [
  { label: '个人资料', key: 'profile', icon: () => h(User, { size: 15 }) },
  { label: '退出登录', key: 'logout', icon: () => h(LogOut, { size: 15 }) },
]

function handleUserAction(key: string) {
  if (key === 'logout') authStore.logout().then(() => router.push({ name: 'login' }))
  else if (key === 'profile') router.push({ name: 'profile' })
}

function toggleCollapsed() {
  collapsed.value = !collapsed.value
  localStorage.setItem(STORAGE_COLLAPSED, collapsed.value ? '1' : '0')
}

function handleResize() {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) mobileDrawer.value = false
}

watch(() => route.path, () => {
  if (isMobile.value) mobileDrawer.value = false
  if (route.path === '/inbox' || route.path === '/today') refreshInboxCount()
})

const displayName = computed(
  () => authStore.user?.displayName || authStore.user?.email?.split('@')[0] || '用户',
)

onMounted(() => {
  window.addEventListener('resize', handleResize)
  window.addEventListener('keydown', onGlobalKeydown)
  loadSidebarData()
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('keydown', onGlobalKeydown)
})

defineExpose({ refreshInboxCount })
</script>

<template>
  <div class="app-shell">
    <!-- ===== Desktop Sidebar ===== -->
    <aside v-if="!isMobile" class="sidebar" :class="{ 'sidebar--collapsed': collapsed }">
      <div class="sidebar__brand">
        <div class="brand">
          <div class="brand__mark">CL</div>
          <span v-if="!collapsed" class="brand__name">Co-Learning</span>
        </div>
        <button class="icon-btn" type="button" :aria-label="collapsed ? '展开侧栏' : '折叠侧栏'" @click="toggleCollapsed">
          <component :is="collapsed ? PanelLeftOpen : PanelLeftClose" :size="17" />
        </button>
      </div>

      <nav class="sidebar__nav" aria-label="主导航">
        <RouterLink
          v-for="item in primaryNav"
          :key="item.key"
          :to="item.to"
          class="nav-item"
          :class="{ 'is-active': isActive(item.to) }"
          :aria-current="isActive(item.to) ? 'page' : undefined"
          :title="collapsed ? item.label : undefined"
        >
          <component :is="item.icon" :size="17" class="nav-item__icon" />
          <span v-if="!collapsed" class="nav-item__label">{{ item.label }}</span>
          <span v-if="!collapsed && item.key === 'inbox' && inboxCount > 0" class="nav-item__badge">{{ inboxCount }}</span>
        </RouterLink>

        <div class="nav-divider" />

        <!-- Subjects (Projects) -->
        <template v-if="!collapsed">
          <div class="nav-section">
            <button class="nav-section__head" type="button" @click="subjectsOpen = !subjectsOpen">
              <ChevronRight :size="14" class="nav-section__chev" :class="{ 'is-open': subjectsOpen }" />
              <BookMarked :size="14" />
              <span class="nav-section__title">科目</span>
              <span class="nav-section__add" role="button" aria-label="新增科目" @click.stop="addingSubject = true; subjectsOpen = true">
                <Plus :size="14" />
              </span>
            </button>
            <div v-show="subjectsOpen" class="nav-section__body">
              <NInput
                v-if="addingSubject"
                v-model:value="newSubject"
                size="tiny"
                placeholder="科目名称，回车创建"
                autofocus
                @keyup.enter="submitSubject"
                @blur="submitSubject"
              />
              <RouterLink
                v-for="s in studyStore.subjects"
                :key="s.id"
                :to="`/tasks?subjectId=${s.id}`"
                class="nav-leaf"
              >
                <span class="nav-leaf__dot" :style="{ background: s.color }" />
                <span class="nav-leaf__label">{{ s.name }}</span>
                <NPopconfirm @positive-click="studyStore.deleteSubject(s.id)">
                  <template #trigger>
                    <span class="nav-leaf__action" @click.stop.prevent><X :size="12" /></span>
                  </template>
                  删除科目？
                </NPopconfirm>
              </RouterLink>
              <p v-if="!studyStore.subjects.length && !addingSubject" class="nav-empty">暂无科目</p>
            </div>
          </div>

          <!-- Tags -->
          <div class="nav-section">
            <button class="nav-section__head" type="button" @click="tagsOpen = !tagsOpen">
              <ChevronRight :size="14" class="nav-section__chev" :class="{ 'is-open': tagsOpen }" />
              <Hash :size="14" />
              <span class="nav-section__title">标签</span>
              <span class="nav-section__add" role="button" aria-label="新增标签" @click.stop="addingTag = true; tagsOpen = true">
                <Plus :size="14" />
              </span>
            </button>
            <div v-show="tagsOpen" class="nav-section__body">
              <NInput
                v-if="addingTag"
                v-model:value="newTag"
                size="tiny"
                placeholder="标签名称，回车创建"
                autofocus
                @keyup.enter="submitTag"
                @blur="submitTag"
              />
              <div v-for="t in taskStore.tags" :key="t.id" class="nav-leaf">
                <span class="nav-leaf__dot" :style="{ background: t.color }" />
                <span class="nav-leaf__label">{{ t.name }}</span>
                <NPopconfirm @positive-click="taskStore.deleteTag(t.id)">
                  <template #trigger>
                    <span class="nav-leaf__action"><X :size="12" /></span>
                  </template>
                  删除标签？
                </NPopconfirm>
              </div>
              <p v-if="!taskStore.tags.length && !addingTag" class="nav-empty">暂无标签</p>
            </div>
          </div>

          <div class="nav-divider" />
        </template>

        <RouterLink
          v-for="item in secondaryNav"
          :key="item.key"
          :to="item.to"
          class="nav-item"
          :class="{ 'is-active': isActive(item.to) }"
          :aria-current="isActive(item.to) ? 'page' : undefined"
          :title="collapsed ? item.label : undefined"
        >
          <component :is="item.icon" :size="17" class="nav-item__icon" />
          <span v-if="!collapsed" class="nav-item__label">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="sidebar__footer">
        <button class="icon-btn footer-theme" type="button" :aria-label="themeStore.theme === 'dark' ? '切换浅色模式' : '切换深色模式'" @click="themeStore.toggleTheme()">
          <component :is="themeStore.theme === 'dark' ? Sun : Moon" :size="17" />
          <span v-if="!collapsed" class="footer-theme__label">{{ themeStore.theme === 'dark' ? '浅色模式' : '深色模式' }}</span>
        </button>
        <NDropdown :options="userOptions" placement="top-start" @select="handleUserAction">
          <button class="footer-user" :class="{ 'footer-user--collapsed': collapsed }" type="button">
            <NAvatar round :size="collapsed ? 26 : 30" :src="authStore.user?.avatarUrl || undefined">
              {{ displayName.charAt(0) }}
            </NAvatar>
            <div v-if="!collapsed" class="footer-user__info">
              <div class="footer-user__name">{{ displayName }}</div>
              <div class="footer-user__email">{{ authStore.user?.email }}</div>
            </div>
          </button>
        </NDropdown>
      </div>
    </aside>

    <!-- ===== Main ===== -->
    <main class="main-area">
      <!-- Top toolbar -->
      <header class="topbar">
        <button v-if="isMobile" class="icon-btn" type="button" aria-label="打开菜单" @click="mobileDrawer = true">
          <Menu :size="20" />
        </button>
        <h1 class="topbar__title">{{ pageTitle }}</h1>
        <div class="topbar__spacer" />
        <FocusMiniPlayer />
        <NTooltip placement="bottom">
          <template #trigger>
            <button class="topbar-search" type="button" aria-label="搜索 (Ctrl/⌘ K)" @click="paletteShow = true">
              <Search :size="15" />
              <span class="topbar-search__hint">搜索</span>
              <kbd class="topbar-search__kbd">⌘K</kbd>
            </button>
          </template>
          全局搜索（Ctrl/⌘ K）
        </NTooltip>
        <QuickAddTask @created="refreshInboxCount" />
        <NTooltip v-if="!isMobile" placement="bottom">
          <template #trigger>
            <button class="topbar-icon-btn" type="button" :aria-label="themeStore.theme === 'dark' ? '切换浅色模式' : '切换深色模式'" @click="themeStore.toggleTheme()">
              <component :is="themeStore.theme === 'dark' ? Sun : Moon" :size="18" />
            </button>
          </template>
          {{ themeStore.theme === 'dark' ? '切换浅色模式' : '切换深色模式' }}
        </NTooltip>
        <NDropdown :options="userOptions" placement="bottom-end" @select="handleUserAction">
          <button class="topbar-avatar" type="button" aria-label="用户菜单">
            <NAvatar round :size="30" :src="authStore.user?.avatarUrl || undefined">{{ displayName.charAt(0) }}</NAvatar>
          </button>
        </NDropdown>
      </header>

      <div class="page-content" :class="{ 'page-content--mobile': isMobile }">
        <RouterView />
      </div>

      <!-- Mobile bottom nav -->
      <nav v-if="isMobile" class="bottom-nav" aria-label="底部导航">
        <RouterLink v-for="item in primaryNav" :key="item.key" :to="item.to" class="bottom-nav__item" :class="{ 'is-active': isActive(item.to) }">
          <component :is="item.icon" :size="20" />
          <span class="bottom-nav__label">{{ item.label }}</span>
        </RouterLink>
      </nav>
    </main>

    <!-- Mobile drawer -->
    <NDrawer v-model:show="mobileDrawer" placement="left" :width="264">
      <NDrawerContent :native-scrollbar="false">
        <template #header>
          <div class="brand"><div class="brand__mark">CL</div><span class="brand__name">Co-Learning</span></div>
        </template>
        <nav class="drawer-nav" aria-label="移动导航">
          <RouterLink v-for="item in [...primaryNav, ...secondaryNav]" :key="item.key" :to="item.to" class="nav-item" :class="{ 'is-active': isActive(item.to) }">
            <component :is="item.icon" :size="17" class="nav-item__icon" />
            <span class="nav-item__label">{{ item.label }}</span>
          </RouterLink>
        </nav>
        <template #footer>
          <button class="icon-btn footer-theme" type="button" @click="themeStore.toggleTheme()">
            <component :is="themeStore.theme === 'dark' ? Sun : Moon" :size="17" />
            <span class="footer-theme__label">{{ themeStore.theme === 'dark' ? '浅色模式' : '深色模式' }}</span>
          </button>
        </template>
      </NDrawerContent>
    </NDrawer>

    <CommandPalette v-model:show="paletteShow" />
  </div>
</template>

<style scoped>
.app-shell { display: flex; height: 100vh; overflow: hidden; background: var(--bg-page); }

/* Sidebar */
.sidebar {
  width: var(--sidebar-width); min-width: var(--sidebar-width);
  background: var(--sidebar-bg);
  border-right: 1px solid var(--sidebar-border);
  display: flex; flex-direction: column;
  transition: width var(--duration-md) var(--ease-standard), min-width var(--duration-md) var(--ease-standard);
  overflow: hidden;
  will-change: width;
}
.sidebar--collapsed { width: var(--sidebar-collapsed-width); min-width: var(--sidebar-collapsed-width); }

.sidebar__brand {
  display: flex; align-items: center; justify-content: space-between;
  height: var(--header-height); padding: 0 var(--sp-3); flex-shrink: 0;
  border-bottom: 1px solid var(--separator);
}
.brand { display: flex; align-items: center; gap: var(--sp-2); overflow: hidden; }
.brand__mark {
  width: 28px; height: 28px; border-radius: var(--radius-sm);
  background: var(--brand); color: var(--ink-on-accent);
  display: flex; align-items: center; justify-content: center;
  font-weight: var(--weight-bold); font-size: var(--text-xs); flex-shrink: 0;
}
.brand__name { font-size: var(--text-base); font-weight: var(--weight-semibold); color: var(--text-color-strong); white-space: nowrap; }

.icon-btn {
  display: flex; align-items: center; justify-content: center;
  min-width: 30px; height: 30px; padding: 0 6px; gap: var(--sp-2);
  border: none; background: transparent; border-radius: var(--radius-sm);
  color: var(--text-color-muted); cursor: pointer;
  transition: background-color var(--transition-fast), color var(--transition-fast);
  font-family: inherit; font-size: var(--text-sm);
}
.icon-btn:hover { background: var(--state-hover); color: var(--text-color); }

.sidebar__nav { flex: 1; overflow-y: auto; overflow-x: hidden; padding: var(--sp-2) var(--sp-2); }

.nav-item {
  position: relative;
  display: flex; align-items: center; gap: var(--sp-2);
  height: 34px; padding: 0 var(--sp-2); border-radius: var(--radius-sm);
  color: var(--nav-text); text-decoration: none;
  transition: background-color var(--transition-fast), color var(--transition-fast);
}
.nav-item::before {
  content: '';
  position: absolute; left: -2px; top: 50%;
  width: 3px; height: 0; transform: translateY(-50%);
  border-radius: var(--radius-pill);
  background: var(--brand);
  opacity: 0;
  transition: height var(--transition-fast), opacity var(--transition-fast);
}
.nav-item:hover { background: var(--state-hover); color: var(--text-color); }
.nav-item.is-active { background: var(--state-selected); color: var(--nav-text-active); font-weight: var(--weight-medium); }
.nav-item.is-active::before { height: 16px; opacity: 1; }
.nav-item__icon { flex-shrink: 0; }
.nav-item__label { flex: 1; font-size: var(--text-base); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.nav-item__badge {
  font-size: var(--text-xs); font-weight: var(--weight-semibold);
  background: var(--state-hover); color: var(--text-color-muted);
  border-radius: var(--radius-pill); padding: 0 7px; min-width: 20px; text-align: center;
}
.sidebar--collapsed .nav-item { justify-content: center; padding: 0; }

.nav-divider { height: 1px; background: var(--separator); margin: var(--sp-2) var(--sp-1); }

.nav-section { margin-bottom: var(--sp-1); }
.nav-section__head {
  display: flex; align-items: center; gap: 6px; width: 100%;
  height: 30px; padding: 0 var(--sp-2); border: none; background: transparent;
  color: var(--text-color-muted); cursor: pointer; border-radius: var(--radius-sm);
  font-family: inherit;
}
.nav-section__head:hover { background: var(--state-hover); }
.nav-section__chev { transition: transform var(--transition-fast); }
.nav-section__chev.is-open { transform: rotate(90deg); }
.nav-section__title { flex: 1; text-align: left; font-size: var(--text-xs); font-weight: var(--weight-semibold); text-transform: uppercase; letter-spacing: var(--tracking-wide); }
.nav-section__add { display: flex; align-items: center; opacity: 0; transition: opacity var(--transition-fast); }
.nav-section__head:hover .nav-section__add { opacity: 0.8; }
.nav-section__body { display: flex; flex-direction: column; gap: 1px; padding-left: var(--sp-3); }

.nav-leaf {
  display: flex; align-items: center; gap: var(--sp-2);
  height: 30px; padding: 0 var(--sp-2); border-radius: var(--radius-sm);
  color: var(--nav-text); text-decoration: none; font-size: var(--text-sm);
  transition: background-color var(--transition-fast), color var(--transition-fast);
}
.nav-leaf:hover { background: var(--state-hover); color: var(--text-color); }
.nav-leaf__dot { width: 8px; height: 8px; border-radius: var(--radius-full); flex-shrink: 0; }
.nav-leaf__label { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.nav-leaf__action {
  display: flex; align-items: center; justify-content: center;
  width: 18px; height: 18px; border-radius: var(--radius-xs);
  color: var(--text-color-muted); cursor: pointer; flex-shrink: 0;
  opacity: 0; transition: opacity var(--transition-fast), background-color var(--transition-fast);
}
.nav-leaf:hover .nav-leaf__action { opacity: 0.7; }
.nav-leaf__action:hover { opacity: 1 !important; background: var(--state-hover); }
.nav-empty { font-size: var(--text-xs); color: var(--text-color-muted); padding: var(--sp-1) var(--sp-2); }

.sidebar__footer { border-top: 1px solid var(--separator); padding: var(--sp-2); display: flex; flex-direction: column; gap: var(--sp-1); flex-shrink: 0; }
.footer-theme { width: 100%; justify-content: flex-start; height: 34px; }
.sidebar--collapsed .footer-theme { justify-content: center; }
.footer-theme__label { font-size: var(--text-sm); }
.footer-user {
  display: flex; align-items: center; gap: var(--sp-2); width: 100%;
  padding: var(--sp-1); border: none; background: transparent; cursor: pointer;
  border-radius: var(--radius-sm); transition: background-color var(--transition-fast); min-height: 40px;
}
.footer-user:hover { background: var(--state-hover); }
.footer-user--collapsed { justify-content: center; }
.footer-user__info { overflow: hidden; text-align: left; }
.footer-user__name { font-size: var(--text-sm); font-weight: var(--weight-medium); color: var(--text-color); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.footer-user__email { font-size: var(--text-xs); color: var(--text-color-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* Main */
.main-area { flex: 1; display: flex; flex-direction: column; min-width: 0; overflow: hidden; }
.topbar {
  display: flex; align-items: center; gap: var(--sp-2);
  height: var(--header-height); flex-shrink: 0; padding: 0 var(--sp-4);
  background: var(--header-bg); border-bottom: 1px solid var(--header-border);
}
.topbar__title { font-size: var(--text-lg); font-weight: var(--weight-semibold); color: var(--text-color-strong); margin: 0; }
.topbar__spacer { flex: 1; }
.topbar-icon-btn {
  display: flex; align-items: center; justify-content: center;
  width: 34px; height: 34px; border: none; background: transparent;
  border-radius: var(--radius-sm); color: var(--text-color-muted); cursor: pointer;
  transition: background-color var(--transition-fast), color var(--transition-fast);
}
.topbar-icon-btn:hover { background: var(--state-hover); color: var(--text-color); }
.topbar-avatar { border: none; background: transparent; cursor: pointer; padding: 0; display: flex; }
.topbar-search {
  display: flex; align-items: center; gap: var(--sp-2);
  height: 34px; padding: 0 var(--sp-2) 0 var(--sp-3);
  border: 1px solid var(--border-color); background: var(--surface-1);
  border-radius: var(--radius-sm); color: var(--text-color-muted);
  cursor: pointer; font-family: inherit; font-size: var(--text-sm);
  transition: background-color var(--transition-fast), border-color var(--transition-fast);
}
.topbar-search:hover { background: var(--state-hover); border-color: var(--brand); }
.topbar-search__kbd { font-size: var(--text-xs); border: 1px solid var(--border-color); border-radius: var(--radius-xs); padding: 0 4px; }
@media (max-width: 900px) { .topbar-search__hint, .topbar-search__kbd { display: none; } }

.page-content { flex: 1; overflow-y: auto; overflow-x: hidden; }
.page-content--mobile { padding-bottom: calc(var(--bottom-nav-height) + var(--sp-4)); }

/* Bottom nav (mobile) */
.bottom-nav {
  position: fixed; bottom: 0; left: 0; right: 0; height: var(--bottom-nav-height);
  background: var(--header-bg); border-top: 1px solid var(--header-border);
  display: flex; align-items: center; justify-content: space-around;
  z-index: var(--z-bottom-nav); padding-bottom: env(safe-area-inset-bottom, 0);
}
.bottom-nav__item { display: flex; flex-direction: column; align-items: center; gap: 2px; padding: var(--sp-1); color: var(--text-color-muted); text-decoration: none; min-width: 48px; }
.bottom-nav__item.is-active { color: var(--brand); }
.bottom-nav__label { font-size: 10px; font-weight: var(--weight-medium); }

.drawer-nav { display: flex; flex-direction: column; gap: 1px; }
</style>
