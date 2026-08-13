<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import { useRoomStore } from '@/stores/roomStore'
import { useFocusStore } from '@/stores/focusStore'
import { useAuthStore } from '@/stores/authStore'
import RoomChatBox from '@/components/room/RoomChatBox.vue'
import RoomMemberList from '@/components/room/RoomMemberList.vue'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const roomStore = useRoomStore()
const focusStore = useFocusStore()
const authStore = useAuthStore()

const roomId = computed(() => Number(route.params.roomId))
const isOwner = computed(() => roomStore.currentRoom?.ownerId === authStore.user?.userId)
const showMembers = ref(true)

// ===== Lifecycle =====

onMounted(async () => {
  try {
    await roomStore.loadRoom(roomId.value)

    if (!roomStore.currentRoom?.isMember) {
      await roomStore.joinRoom(roomId.value)
    }

    await roomStore.loadMembers(roomId.value)
    await roomStore.loadMessages(roomId.value)

    roomStore.connectRoom(roomId.value)

    if (focusStore.isActive) {
      roomStore.broadcastFocusStatus(roomId.value, 'STUDYING')
    } else if (focusStore.isPaused) {
      roomStore.broadcastFocusStatus(roomId.value, 'PAUSED')
    } else {
      roomStore.broadcastFocusStatus(roomId.value, 'IDLE')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || 'Failed to load room')
    router.push('/rooms')
  }
})

onUnmounted(() => {
  roomStore.disconnectRoom(roomId.value)
  roomStore.reset()
})

// ===== Focus status broadcasting =====

watch(() => focusStore.isActive, (active) => {
  if (roomStore.connected) {
    if (active) {
      roomStore.broadcastFocusStatus(roomId.value, 'STUDYING')
    } else if (focusStore.isPaused) {
      roomStore.broadcastFocusStatus(roomId.value, 'PAUSED')
    } else {
      roomStore.broadcastFocusStatus(roomId.value, 'IDLE')
    }
  }
})

watch(() => focusStore.isPaused, (paused) => {
  if (roomStore.connected) {
    if (paused) {
      roomStore.broadcastFocusStatus(roomId.value, 'PAUSED')
    } else if (focusStore.isActive) {
      roomStore.broadcastFocusStatus(roomId.value, 'STUDYING')
    }
  }
})

// ===== Actions =====

async function handleSend(content: string) {
  try {
    await roomStore.sendMessage(roomId.value, content)
  } catch {
    message.error('发送失败')
  }
}

function handleLeave() {
  dialog.warning({
    title: '离开房间',
    content: '确定要离开这个房间吗？',
    positiveText: '离开',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await roomStore.leaveRoom(roomId.value)
        message.success('已离开房间')
        router.push('/rooms')
      } catch {
        message.error('操作失败')
      }
    },
  })
}

function handleDelete() {
  dialog.warning({
    title: '删除房间',
    content: '确定要永久删除这个房间吗？此操作不可恢复，所有聊天记录将被删除。',
    positiveText: '删除',
    negativeText: '取消',
    positiveButtonProps: { type: 'error' },
    onPositiveClick: async () => {
      try {
        await roomStore.deleteRoom(roomId.value)
        message.success('房间已删除')
        router.push('/rooms')
      } catch {
        message.error('操作失败')
      }
    },
  })
}

function handleKick(userId: number) {
  dialog.warning({
    title: '踢出成员',
    content: `确定要踢出该成员吗？`,
    positiveText: '踢出',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await roomStore.kickMember(roomId.value, userId)
        message.success('成员已踢出')
      } catch {
        message.error('操作失败')
      }
    },
  })
}

function handleMute(userId: number) {
  dialog.warning({
    title: '禁言成员',
    content: '禁言时长（分钟）？留空为永久禁言',
    positiveText: '禁言',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await roomStore.muteMember(roomId.value, userId, 60)
        message.success('成员已禁言')
      } catch {
        message.error('操作失败')
      }
    },
  })
}

function handleSendImage(imageUrl: string) {
  roomStore.sendMessage(roomId.value, `![图片](${imageUrl})`)
}

function goBack() {
  router.push('/rooms')
}
</script>

<template>
  <div class="room-detail-view gradient-mesh" v-if="roomStore.currentRoom">
    <!-- Room Header -->
    <div class="room-header glass">
      <div class="header-left">
        <button class="back-btn" @click="goBack">←</button>
        <div class="room-info">
          <div class="room-name-row">
            <h3 class="room-title">{{ roomStore.currentRoom.name }}</h3>
            <n-tag v-if="roomStore.currentRoom.visibility === 'PRIVATE'" type="warning" size="small" :bordered="false">
              私密
            </n-tag>
            <n-tag v-if="roomStore.currentRoom.topic" size="small" :bordered="false">{{ roomStore.currentRoom.topic }}</n-tag>
          </div>
          <p class="room-subtitle" v-if="roomStore.currentRoom.description">
            {{ roomStore.currentRoom.description }}
          </p>
        </div>
      </div>

      <div class="header-right">
        <div class="header-stats">
          <div class="stat-chip">
            <span class="stat-num">{{ roomStore.onlineCount }}</span>
            <span class="stat-lbl">在线</span>
          </div>
          <div class="stat-chip">
            <span class="stat-num studying">{{ roomStore.studyingCount }}</span>
            <span class="stat-lbl">学习中</span>
          </div>
          <div class="stat-chip">
            <span class="stat-num">{{ roomStore.currentRoom.memberCount }}</span>
            <span class="stat-lbl">总人数</span>
          </div>
        </div>

        <div class="header-actions">
          <n-button quaternary size="small" @click="showMembers = !showMembers">
            {{ showMembers ? '隐藏成员' : '显示成员' }}
          </n-button>
          <n-button quaternary size="small" type="error" @click="handleDelete" v-if="isOwner">
            删除房间
          </n-button>
          <n-button quaternary size="small" type="error" @click="handleLeave" v-if="!isOwner">
            离开房间
          </n-button>
        </div>
      </div>
    </div>

    <!-- Room Body -->
    <div class="room-body">
      <!-- Chat Area -->
      <div class="chat-area glass">
        <RoomChatBox
          :messages="roomStore.messages"
          @send="handleSend"
          @send-image="handleSendImage"
        />
      </div>

      <!-- Member Sidebar -->
      <div class="member-sidebar glass" v-if="showMembers">
        <RoomMemberList
          :members="roomStore.members"
          :online-user-ids="roomStore.onlineUserIds"
          @kick="handleKick"
          @mute="handleMute"
        />
      </div>
    </div>
  </div>

  <div v-else class="loading-state">
    <n-spin size="large" />
  </div>
</template>

<style scoped>
.room-detail-view {
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--header-height) - var(--bottom-nav-height));
  min-height: 400px;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-4);
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-lg);
  margin: var(--sp-2);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  min-width: 0;
  flex: 1;
}

.back-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: var(--text-lg);
  color: var(--text-color-muted);
  padding: var(--sp-1) var(--sp-2);
  border-radius: var(--radius-sm);
  transition: background-color var(--transition-fast);
  flex-shrink: 0;
}

.back-btn:hover {
  background: var(--state-hover);
}

.room-info {
  min-width: 0;
}

.room-name-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.room-title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.room-subtitle {
  margin: var(--sp-1) 0 0 0;
  font-size: var(--text-sm);
  color: var(--text-color-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  flex-shrink: 0;
}

.header-stats {
  display: flex;
  gap: var(--sp-4);
}

.stat-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-num {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.stat-num.studying {
  color: var(--success);
}

.stat-lbl {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
}

.room-body {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  gap: var(--sp-2);
  padding: 0 var(--sp-2) var(--sp-2);
}

.chat-area {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: var(--radius-lg);
}

.member-sidebar {
  width: 260px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: var(--radius-lg);
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;
}

@media (max-width: 768px) {
  .room-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .room-body {
    flex-direction: column;
  }

  .chat-area {
    border-right: none;
  }

  .member-sidebar {
    width: 100%;
    max-height: 200px;
    border-left: none;
  }
}
</style>
