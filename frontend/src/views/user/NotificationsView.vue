<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NList, NListItem, NThing, NButton, NTag, NEmpty, NTabs, NTabPane, NBadge, NIcon, NPopconfirm } from 'naive-ui'
import { NotificationsOutline, CheckmarkDoneOutline, TrashOutline } from '@vicons/ionicons5'

interface Notification {
  id: number
  type: 'info' | 'success' | 'warning' | 'error'
  title: string
  message: string
  time: string
  read: boolean
  action?: {
    label: string
    handler: () => void
  }
}

const router = useRouter()

// 默认通知数据
const defaultNotifications: Notification[] = [
  {
    id: 1,
    type: 'success',
    title: '学习目标完成',
    message: '恭喜！你已经完成了今天的数学学习目标。',
    time: '2分钟前',
    read: false,
  },
  {
    id: 2,
    type: 'info',
    title: '新消息',
    message: '你有一条来自学习伙伴的新消息。',
    time: '10分钟前',
    read: false,
    action: {
      label: '查看',
      handler: () => router.push('/rooms'),
    },
  },
  {
    id: 3,
    type: 'warning',
    title: '任务即将到期',
    message: '你的英语作文任务将在明天到期。',
    time: '1小时前',
    read: true,
  },
  {
    id: 4,
    type: 'error',
    title: '系统通知',
    message: '系统将在今晚进行维护，届时可能无法访问。',
    time: '2小时前',
    read: true,
  },
]

// 从localStorage加载通知状态
function loadNotifications(): Notification[] {
  const stored = localStorage.getItem('notifications')
  if (stored) {
    try {
      return JSON.parse(stored)
    } catch {
      return [...defaultNotifications]
    }
  }
  return [...defaultNotifications]
}

// 保存通知状态到localStorage
function saveNotifications(notifs: Notification[]) {
  localStorage.setItem('notifications', JSON.stringify(notifs))
}

const notifications = ref<Notification[]>(loadNotifications())

const activeTab = ref('all')

const filteredNotifications = computed(() => {
  if (activeTab.value === 'unread') {
    return notifications.value.filter((n) => !n.read)
  }
  return notifications.value
})

const unreadCount = computed(() => notifications.value.filter((n) => !n.read).length)

function markAsRead(id: number) {
  const notification = notifications.value.find((n) => n.id === id)
  if (notification) {
    notification.read = true
    saveNotifications(notifications.value)
  }
}

function markAllAsRead() {
  notifications.value.forEach((n) => {
    n.read = true
  })
  saveNotifications(notifications.value)
}

function deleteNotification(id: number) {
  notifications.value = notifications.value.filter((n) => n.id !== id)
  saveNotifications(notifications.value)
}

function clearAll() {
  notifications.value = []
  saveNotifications([])
}

function getTypeColor(type: string): 'info' | 'success' | 'warning' | 'error' {
  const colors: Record<string, 'info' | 'success' | 'warning' | 'error'> = {
    info: 'info',
    success: 'success',
    warning: 'warning',
    error: 'error',
  }
  return colors[type] || 'info'
}

function getTypeIcon(type: string) {
  const icons: Record<string, Component> = {
    info: NotificationsOutline,
    success: CheckmarkDoneOutline,
    warning: NotificationsOutline,
    error: NotificationsOutline,
  }
  return icons[type] || NotificationsOutline
}
</script>

<template>
  <div class="notifications-view">
    <div class="notifications-view__header">
      <h1 class="notifications-view__title">
        <n-icon :component="NotificationsOutline" />
        通知中心
        <n-badge :value="unreadCount" :max="99" />
      </h1>
      <div class="notifications-view__actions">
        <n-button quaternary @click="markAllAsRead">全部已读</n-button>
        <n-popconfirm @positive-click="clearAll">
          <template #trigger>
            <n-button quaternary type="error">清空</n-button>
          </template>
          确定要清空所有通知吗？
        </n-popconfirm>
      </div>
    </div>
    
    <n-tabs v-model:value="activeTab" type="line" animated>
      <n-tab-pane name="all" tab="全部">
        <n-card>
          <n-list v-if="filteredNotifications.length > 0" hoverable clickable>
            <n-list-item
              v-for="notification in filteredNotifications"
              :key="notification.id"
              :class="{ 'notifications-view__item--unread': !notification.read }"
              @click="markAsRead(notification.id)"
            >
              <n-thing>
                <template #avatar>
                  <n-icon
                    :component="getTypeIcon(notification.type)"
                    :color="notification.type === 'success' ? '#18a058' : notification.type === 'error' ? '#d03050' : notification.type === 'warning' ? '#f0a020' : '#2080f0'"
                    size="24"
                  />
                </template>
                <template #header>
                  <div class="notifications-view__item-header">
                    <span>{{ notification.title }}</span>
                    <n-tag :type="getTypeColor(notification.type)" size="small">
                      {{ notification.type === 'info' ? '信息' : notification.type === 'success' ? '成功' : notification.type === 'warning' ? '警告' : '错误' }}
                    </n-tag>
                  </div>
                </template>
                <template #description>
                  <div class="notifications-view__item-content">
                    <p>{{ notification.message }}</p>
                    <span class="notifications-view__item-time">{{ notification.time }}</span>
                  </div>
                </template>
                <template #header-extra>
                  <div class="notifications-view__item-actions">
                    <n-button
                      v-if="notification.action"
                      size="small"
                      type="primary"
                      @click.stop="notification.action.handler"
                    >
                      {{ notification.action.label }}
                    </n-button>
                    <n-button
                      size="small"
                      quaternary
                      type="error"
                      @click.stop="deleteNotification(notification.id)"
                    >
                      <template #icon>
                        <n-icon :component="TrashOutline" />
                      </template>
                    </n-button>
                  </div>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
          <n-empty v-else description="暂无通知" />
        </n-card>
      </n-tab-pane>
      
      <n-tab-pane name="unread" tab="未读">
        <n-card>
          <n-list v-if="filteredNotifications.length > 0" hoverable clickable>
            <n-list-item
              v-for="notification in filteredNotifications"
              :key="notification.id"
              class="notifications-view__item--unread"
              @click="markAsRead(notification.id)"
            >
              <n-thing>
                <template #avatar>
                  <n-icon
                    :component="getTypeIcon(notification.type)"
                    :color="notification.type === 'success' ? '#18a058' : notification.type === 'error' ? '#d03050' : notification.type === 'warning' ? '#f0a020' : '#2080f0'"
                    size="24"
                  />
                </template>
                <template #header>
                  <div class="notifications-view__item-header">
                    <span>{{ notification.title }}</span>
                    <n-tag :type="getTypeColor(notification.type)" size="small">
                      {{ notification.type === 'info' ? '信息' : notification.type === 'success' ? '成功' : notification.type === 'warning' ? '警告' : '错误' }}
                    </n-tag>
                  </div>
                </template>
                <template #description>
                  <div class="notifications-view__item-content">
                    <p>{{ notification.message }}</p>
                    <span class="notifications-view__item-time">{{ notification.time }}</span>
                  </div>
                </template>
                <template #header-extra>
                  <div class="notifications-view__item-actions">
                    <n-button
                      v-if="notification.action"
                      size="small"
                      type="primary"
                      @click.stop="notification.action.handler"
                    >
                      {{ notification.action.label }}
                    </n-button>
                    <n-button
                      size="small"
                      quaternary
                      type="error"
                      @click.stop="deleteNotification(notification.id)"
                    >
                      <template #icon>
                        <n-icon :component="TrashOutline" />
                      </template>
                    </n-button>
                  </div>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
          <n-empty v-else description="暂无未读通知" />
        </n-card>
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<style scoped>
.notifications-view {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--sp-6);
}

.notifications-view__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-6);
}

.notifications-view__title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-color);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.notifications-view__actions {
  display: flex;
  gap: var(--sp-2);
}

.notifications-view__item--unread {
  background: var(--primary-suppl);
}

.notifications-view__item-header {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.notifications-view__item-content {
  display: flex;
  flex-direction: column;
  gap: var(--sp-1);
}

.notifications-view__item-content p {
  margin: 0;
  color: var(--text-color);
}

.notifications-view__item-time {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}

.notifications-view__item-actions {
  display: flex;
  gap: var(--sp-2);
}
</style>