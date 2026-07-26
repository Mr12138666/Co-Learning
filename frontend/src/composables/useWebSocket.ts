import { ref, onScopeDispose, type Ref } from 'vue'
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuthStore } from '@/stores/authStore'

export type ConnectionState = 'disconnected' | 'connecting' | 'connected' | 'reconnecting'

interface SubscriptionCallbacks {
  onMessage: (message: any) => void
}

/**
 * WebSocket composable for STOMP-based real-time communication.
 *
 * Features:
 * - SockJS fallback for browser compatibility
 * - JWT authentication via CONNECT header
 * - Automatic token refresh on reconnection
 * - Topic subscription management
 * - Exponential backoff reconnection (handled by stompjs)
 * - Heartbeat support (15s in/out via STOMP, 25s business heartbeat)
 */
export function useWebSocket() {
  const authStore = useAuthStore()

  const connectionState: Ref<ConnectionState> = ref('disconnected')
  const error = ref<string | null>(null)

  let stompClient: Client | null = null
  const subscriptions = new Map<string, StompSubscription>()
  const subscriptionCallbacks = new Map<string, SubscriptionCallbacks>()

  // Heartbeat interval for room presence (25s)
  let heartbeatInterval: ReturnType<typeof setInterval> | null = null
  let currentRoomId: number | null = null

  /**
   * Connects to the WebSocket server with JWT authentication.
   */
  function connect() {
    if (stompClient?.active) return

    // Ensure we have a token
    if (!authStore.accessToken) {
      error.value = 'No access token available'
      return
    }

    connectionState.value = 'connecting'

    stompClient = new Client({
      // Use SockJS as the WebSocket factory
      webSocketFactory: () => new SockJS('/ws'),

      // Authentication via CONNECT header
      connectHeaders: {
        Authorization: `Bearer ${authStore.accessToken}`,
      },

      // Heartbeat: 15s in/out (matches server configuration)
      heartbeatIncoming: 15000,
      heartbeatOutgoing: 15000,

      // Reconnection delay (ms) - stompjs handles exponential backoff
      reconnectDelay: 3000,

      // Debug logging (disabled in production)
      debug: import.meta.env.DEV ? (msg) => console.debug('[STOMP]', msg) : () => {},

      // Called before each connection attempt - refresh token if needed.
      // Refresh when the token is missing OR expired (not merely absent): an
      // expired-but-present token would otherwise loop the STOMP reconnect forever.
      beforeConnect: async () => {
        if (!authStore.accessToken || authStore.isAccessTokenExpired) {
          try {
            await authStore.refresh()
          } catch {
            error.value = 'Authentication failed'
            connectionState.value = 'disconnected'
            // Stop the reconnect loop — there is nothing to authenticate with.
            stompClient?.deactivate()
            return
          }
        }
        // Update connect headers with fresh token
        if (stompClient) {
          stompClient.connectHeaders = {
            Authorization: `Bearer ${authStore.accessToken}`,
          }
        }
      },

      onConnect: () => {
        connectionState.value = 'connected'
        error.value = null

        // Re-subscribe to all active topics
        for (const [topic, callbacks] of subscriptionCallbacks) {
          subscribeToTopic(topic, callbacks)
        }

        // Start heartbeat if in a room
        startHeartbeat()
      },

      onDisconnect: () => {
        connectionState.value = 'disconnected'
        stopHeartbeat()
      },

      onStompError: (frame) => {
        console.error('[STOMP] Error:', frame.headers['message'])
        error.value = frame.headers['message'] || 'STOMP error'
        connectionState.value = 'reconnecting'
      },

      onWebSocketError: (event) => {
        console.error('[STOMP] WebSocket error:', event)
        error.value = 'WebSocket connection error'
        connectionState.value = 'reconnecting'
      },
    })

    stompClient.activate()
  }

  /**
   * Disconnects from the WebSocket server.
   */
  function disconnect() {
    stopHeartbeat()
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
    }
    subscriptions.clear()
    subscriptionCallbacks.clear()
    connectionState.value = 'disconnected'
    currentRoomId = null
  }

  /**
   * Subscribes to a STOMP topic.
   */
  function subscribe(topic: string, callbacks: SubscriptionCallbacks) {
    // Store callbacks for reconnection
    subscriptionCallbacks.set(topic, callbacks)

    if (stompClient?.connected) {
      subscribeToTopic(topic, callbacks)
    }
  }

  function subscribeToTopic(topic: string, callbacks: SubscriptionCallbacks) {
    // Unsubscribe if already subscribed
    const existing = subscriptions.get(topic)
    if (existing) {
      existing.unsubscribe()
    }

    const sub = stompClient?.subscribe(topic, (message: IMessage) => {
      try {
        const data = JSON.parse(message.body)
        callbacks.onMessage(data)
      } catch (e) {
        console.error('[STOMP] Failed to parse message:', e)
      }
    })

    if (sub) {
      subscriptions.set(topic, sub)
    }
  }

  /**
   * Unsubscribes from a STOMP topic.
   */
  function unsubscribe(topic: string) {
    const sub = subscriptions.get(topic)
    if (sub) {
      sub.unsubscribe()
      subscriptions.delete(topic)
    }
    subscriptionCallbacks.delete(topic)
  }

  /**
   * Sends a message to a STOMP destination.
   */
  function send(destination: string, body: any) {
    if (stompClient?.connected) {
      stompClient.publish({
        destination: `/app${destination}`,
        body: JSON.stringify(body),
      })
    } else {
      console.warn('[STOMP] Cannot send: not connected')
    }
  }

  /**
   * Joins a room's WebSocket presence channel.
   * Sets up subscriptions for presence, status, and messages.
   */
  function joinRoomChannel(roomId: number, callbacks: {
    onPresence?: (data: any) => void
    onStatus?: (data: any) => void
    onMessage?: (data: any) => void
  }) {
    currentRoomId = roomId

    // Subscribe to room topics
    subscribe(`/topic/rooms/${roomId}/presence`, {
      onMessage: (data) => callbacks.onPresence?.(data),
    })
    subscribe(`/topic/rooms/${roomId}/status`, {
      onMessage: (data) => callbacks.onStatus?.(data),
    })
    subscribe(`/topic/rooms/${roomId}/messages`, {
      onMessage: (data) => callbacks.onMessage?.(data),
    })

    // Send join message to register presence
    if (stompClient?.connected) {
      send(`/rooms/${roomId}/join`, {})
      startHeartbeat()
    }
  }

  /**
   * Leaves a room's WebSocket channel.
   */
  function leaveRoomChannel(roomId: number) {
    stopHeartbeat()
    currentRoomId = null

    unsubscribe(`/topic/rooms/${roomId}/presence`)
    unsubscribe(`/topic/rooms/${roomId}/status`)
    unsubscribe(`/topic/rooms/${roomId}/messages`)
  }

  /**
   * Starts the business heartbeat (every 25s) to refresh presence TTL.
   */
  function startHeartbeat() {
    stopHeartbeat()
    if (currentRoomId && stompClient?.connected) {
      heartbeatInterval = setInterval(() => {
        if (currentRoomId && stompClient?.connected) {
          send(`/rooms/${currentRoomId}/heartbeat`, {})
        }
      }, 25000)
    }
  }

  function stopHeartbeat() {
    if (heartbeatInterval) {
      clearInterval(heartbeatInterval)
      heartbeatInterval = null
    }
  }

  /**
   * Updates focus status in the current room.
   */
  function updateFocusStatus(roomId: number, focusStatus: string) {
    send(`/rooms/${roomId}/status`, { focusStatus })
  }

  /**
   * Sends a chat message via WebSocket.
   */
  function sendChatMessage(roomId: number, content: string, messageType = 'TEXT') {
    send(`/rooms/${roomId}/chat`, { content, messageType })
  }

  // Auto-cleanup when the using component/scope is disposed
  onScopeDispose(() => {
    disconnect()
  })

  return {
    connectionState,
    error,
    connect,
    disconnect,
    subscribe,
    unsubscribe,
    send,
    joinRoomChannel,
    leaveRoomChannel,
    updateFocusStatus,
    sendChatMessage,
  }
}
