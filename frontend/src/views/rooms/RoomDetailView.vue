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
    // Load room details
    await roomStore.loadRoom(roomId.value)

    // If not a member, try to join
    if (!roomStore.currentRoom?.isMember) {
      await roomStore.joinRoom(roomId.value)
    }

    // Fetch full room state (members, messages, online users)
    await roomStore.fetchRoomState(roomId.value)

    // Connect WebSocket and join real-time channel
    roomStore.connectRoom(roomId.value)

    // Broadcast initial focus status
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

function goBack() {
  router.push('/rooms')
}
</script>

<template>
  <div class="room-detail-view" v-if="roomStore.currentRoom">
    <!-- Room Header -->
    <div class="room-header">
      <n-space align="center" justify="space-between">
        <n-space align="center">
          <n-button quaternary circle @click="goBack">
            <template #icon><span>←</span></template>
          </n-button>
          <div>
            <n-space align="center" size="small">
              <h3 class="room-title">{{ roomStore.currentRoom.name }}</h3>
              <n-tag v-if="roomStore.currentRoom.visibility === 'PRIVATE'" type="warning" size="small" round>
                私密
              </n-tag>
              <n-tag v-if="roomStore.currentRoom.topic" size="small">{{ roomStore.currentRoom.topic }}</n-tag>
            </n-space>
            <p class="room-subtitle" v-if="roomStore.currentRoom.description">
              {{ roomStore.currentRoom.description }}
            </p>
          </div>
        </n-space>

        <n-space align="center" size="small">
          <!-- Online stats -->
          <n-statistic label="在线" :value="roomStore.onlineCount" />
          <n-divider vertical />
          <n-statistic label="学习中" :value="roomStore.studyingCount" />
          <n-divider vertical />
          <n-statistic label="总人数" :value="roomStore.currentRoom.memberCount" />

          <n-button quaternary @click="showMembers = !showMembers">
            {{ showMembers ? '隐藏成员' : '显示成员' }}
          </n-button>
          <n-button quaternary type="error" @click="handleLeave" v-if="!isOwner">
            离开房间
          </n-button>
        </n-space>
      </n-space>
    </div>

    <!-- Room Body -->
    <div class="room-body">
      <!-- Chat Area -->
      <div class="chat-area">
        <RoomChatBox
          :messages="roomStore.messages"
          @send="handleSend"
        />
      </div>

      <!-- Member Sidebar -->
      <div class="member-sidebar" v-if="showMembers">
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
  height: 100%;
  min-height: 0;
}

.room-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--divider-color);
  flex-shrink: 0;
}

.room-title {
  margin: 0;
  font-size: 18px;
}

.room-subtitle {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: var(--text-color-3);
}

.room-body {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.chat-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--divider-color);
}

.member-sidebar {
  width: 280px;
  flex-shrink: 0;
  border-left: 1px solid var(--divider-color);
  overflow: hidden;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;
}
</style>
