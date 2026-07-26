import http from './http'
import type { ApiResponse, Page } from '@/types/api'

// ===== Types =====

export interface RoomResponse {
  id: number
  name: string
  description: string | null
  maxMembers: number
  visibility: string
  status: string
  topic: string | null
  ownerId: number
  ownerName: string
  ownerAvatar: string | null
  memberCount: number
  isMember: boolean
  createdAt: string
  updatedAt: string
}

export interface RoomMemberResponse {
  memberId: number
  userId: number
  displayName: string
  avatarUrl: string | null
  role: string
  isMuted: boolean
  mutedUntil: string | null
  joinedAt: string
  isOnline: boolean
  focusStatus: string | null
}

export interface RoomMessageResponse {
  id: number
  roomId: number
  userId: number
  displayName: string
  avatarUrl: string | null
  content: string
  messageType: string
  focusStatus: string | null
  createdAt: string
}

export interface RoomStateResponse {
  roomId: number
  roomName: string
  status: string
  members: RoomMemberResponse[]
  onlineUserIds: number[]
  recentMessages: RoomMessageResponse[]
  snapshotAt: string
}

export interface CreateRoomRequest {
  name: string
  description?: string
  maxMembers?: number
  visibility?: string
  password?: string
  topic?: string
}

export interface UpdateRoomRequest {
  name?: string
  description?: string
  maxMembers?: number
  status?: string
  password?: string
  topic?: string
}

export interface JoinRoomRequest {
  password?: string
}

export interface SendRoomMessageRequest {
  content: string
  messageType?: string
  focusStatus?: string
}

export interface MuteMemberRequest {
  durationMinutes?: number
  reason?: string
}

// ===== API =====

export const roomApi = {
  listRooms(page = 0, size = 20) {
    return http.get<ApiResponse<Page<RoomResponse>>>('/rooms', { params: { page, size } })
  },

  getRoom(roomId: number) {
    return http.get<ApiResponse<RoomResponse>>(`/rooms/${roomId}`)
  },

  createRoom(data: CreateRoomRequest) {
    return http.post<ApiResponse<RoomResponse>>('/rooms', data)
  },

  updateRoom(roomId: number, data: UpdateRoomRequest) {
    return http.put<ApiResponse<RoomResponse>>(`/rooms/${roomId}`, data)
  },

  deleteRoom(roomId: number) {
    return http.delete<ApiResponse<void>>(`/rooms/${roomId}`)
  },

  joinRoom(roomId: number, data?: JoinRoomRequest) {
    return http.post<ApiResponse<void>>(`/rooms/${roomId}/join`, data || {})
  },

  leaveRoom(roomId: number) {
    return http.delete<ApiResponse<void>>(`/rooms/${roomId}/leave`)
  },

  listMembers(roomId: number) {
    return http.get<ApiResponse<RoomMemberResponse[]>>(`/rooms/${roomId}/members`)
  },

  kickMember(roomId: number, targetUserId: number) {
    return http.post<ApiResponse<void>>(`/rooms/${roomId}/kick/${targetUserId}`)
  },

  muteMember(roomId: number, targetUserId: number, data?: MuteMemberRequest) {
    return http.post<ApiResponse<void>>(`/rooms/${roomId}/mute/${targetUserId}`, data || {})
  },

  listMessages(roomId: number, page = 0, size = 50) {
    return http.get<ApiResponse<Page<RoomMessageResponse>>>(`/rooms/${roomId}/messages`, {
      params: { page, size },
    })
  },

  sendMessage(roomId: number, data: SendRoomMessageRequest) {
    return http.post<ApiResponse<RoomMessageResponse>>(`/rooms/${roomId}/messages`, data)
  },

  getRoomState(roomId: number) {
    return http.get<ApiResponse<RoomStateResponse>>(`/rooms/${roomId}/state`)
  },
}
