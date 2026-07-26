import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  roomApi,
  type RoomResponse,
  type RoomMemberResponse,
  type RoomMessageResponse,
  type RoomStateResponse,
  type CreateRoomRequest,
} from '@/api/room'
import { useWebSocket } from '@/composables/useWebSocket'
import { useAuthStore } from '@/stores/authStore'

export const useRoomStore = defineStore('room', () => {
  // ===== State =====
  const rooms = ref<RoomResponse[]>([])
  const currentRoom = ref<RoomResponse | null>(null)
  const members = ref<RoomMemberResponse[]>([])
  const messages = ref<RoomMessageResponse[]>([])
  const onlineUserIds = ref<Set<number>>(new Set())
  const totalRooms = ref(0)
  const totalMessages = ref(0)
  const loading = ref(false)
  const connected = ref(false)

  // WebSocket composable (singleton-like)
  const ws = useWebSocket()

  // ===== Getters =====
  const onlineCount = computed(() => onlineUserIds.value.size)
  const studyingCount = computed(() =>
    members.value.filter((m) => m.focusStatus === 'STUDYING').length,
  )

  // ===== Actions =====

  async function loadRooms(page = 0, size = 20) {
    loading.value = true
    try {
      const res = await roomApi.listRooms(page, size)
      const data = res.data.data
      rooms.value = data.items
      totalRooms.value = data.total
    } finally {
      loading.value = false
    }
  }

  async function loadRoom(roomId: number) {
    const res = await roomApi.getRoom(roomId)
    currentRoom.value = res.data.data
    return res.data.data
  }

  async function createRoom(data: CreateRoomRequest) {
    const res = await roomApi.createRoom(data)
    await loadRooms()
    return res.data.data
  }

  async function deleteRoom(roomId: number) {
    await roomApi.deleteRoom(roomId)
    await loadRooms()
  }

  async function joinRoom(roomId: number, password?: string) {
    await roomApi.joinRoom(roomId, password ? { password } : undefined)
    await loadRoom(roomId)
    await loadMembers(roomId)
  }

  async function leaveRoom(roomId: number) {
    await roomApi.leaveRoom(roomId)
    currentRoom.value = null
    members.value = []
    messages.value = []
    onlineUserIds.value = new Set()
  }

  async function loadMembers(roomId: number) {
    const res = await roomApi.listMembers(roomId)
    members.value = res.data.data
    // Update online user IDs from member data
    onlineUserIds.value = new Set(
      res.data.data.filter((m: RoomMemberResponse) => m.isOnline).map((m: RoomMemberResponse) => m.userId),
    )
  }

  async function loadMessages(roomId: number, page = 0, size = 50) {
    const res = await roomApi.listMessages(roomId, page, size)
    const data = res.data.data
    // Messages come newest-first, reverse for display
    messages.value = [...data.items].reverse()
    totalMessages.value = data.total
  }

  async function loadMoreMessages(roomId: number, page: number, size = 20) {
    const res = await roomApi.listMessages(roomId, page, size)
    const data = res.data.data
    // Messages come newest-first, reverse for display
    const newMessages = [...data.items].reverse()
    // Prepend to existing messages (older messages go first)
    messages.value = [...newMessages, ...messages.value]
    totalMessages.value = data.total
  }

  async function sendMessage(roomId: number, content: string) {
    await roomApi.sendMessage(roomId, { content, messageType: 'TEXT' })
    // Message will also arrive via WebSocket, but we add it immediately for responsiveness
    // Actually, the REST API returns the message, and WebSocket will also broadcast it
    // To avoid duplicates, we rely on WebSocket broadcast for adding to the list
  }

  async function kickMember(roomId: number, targetUserId: number) {
    await roomApi.kickMember(roomId, targetUserId)
    await loadMembers(roomId)
  }

  async function muteMember(roomId: number, targetUserId: number, durationMinutes?: number) {
    await roomApi.muteMember(roomId, targetUserId, { durationMinutes })
    await loadMembers(roomId)
  }

  /**
   * Fetches the full room state snapshot (for initial load or reconnect).
   */
  async function fetchRoomState(roomId: number) {
    const res = await roomApi.getRoomState(roomId)
    const state: RoomStateResponse = res.data.data

    members.value = state.members
    messages.value = state.recentMessages
    onlineUserIds.value = new Set(state.onlineUserIds)

    return state
  }

  /**
   * Connects to the WebSocket and joins a room's real-time channel.
   */
  function connectRoom(roomId: number) {
    const authStore = useAuthStore()
    if (!authStore.accessToken) return

    // Connect WebSocket if not already connected
    ws.connect()

    // Join the room's WebSocket channel
    ws.joinRoomChannel(roomId, {
      onPresence: (data) => {
        // Update online user IDs
        const onlineSet = new Set(data.onlineUsers as number[])
        onlineUserIds.value = onlineSet

        // Update member online status
        for (const member of members.value) {
          member.isOnline = onlineSet.has(member.userId)
        }

        // If someone joined, reload members to get their info
        if (data.action === 'JOIN') {
          loadMembers(roomId).catch(() => {})
        }
      },
      onStatus: (data) => {
        // Update focus status for a user
        const member = members.value.find((m) => m.userId === data.userId)
        if (member) {
          member.focusStatus = data.focusStatus
        }
      },
      onMessage: (data: RoomMessageResponse) => {
        // Add new message if not already present (dedup by ID)
        if (!messages.value.find((m) => m.id === data.id)) {
          messages.value.push(data)
        }
      },
    })

    connected.value = true
  }

  /**
   * Disconnects from a room's WebSocket channel.
   */
  function disconnectRoom(roomId: number) {
    ws.leaveRoomChannel(roomId)
    connected.value = false
  }

  /**
   * Broadcasts focus status update to the current room.
   */
  function broadcastFocusStatus(roomId: number, focusStatus: string) {
    ws.updateFocusStatus(roomId, focusStatus)
  }

  /**
   * Resets the store state.
   */
  function reset() {
    rooms.value = []
    currentRoom.value = null
    members.value = []
    messages.value = []
    onlineUserIds.value = new Set()
    totalMessages.value = 0
    connected.value = false
  }

  return {
    // State
    rooms,
    currentRoom,
    members,
    messages,
    onlineUserIds,
    totalRooms,
    totalMessages,
    loading,
    connected,
    // Getters
    onlineCount,
    studyingCount,
    // Actions
    loadRooms,
    loadRoom,
    createRoom,
    deleteRoom,
    joinRoom,
    leaveRoom,
    loadMembers,
    loadMessages,
    loadMoreMessages,
    sendMessage,
    kickMember,
    muteMember,
    fetchRoomState,
    connectRoom,
    disconnectRoom,
    broadcastFocusStatus,
    reset,
  }
})
