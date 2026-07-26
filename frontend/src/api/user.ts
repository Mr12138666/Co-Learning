import http from './http'
import type { ApiResponse } from '@/types/api'

export interface PublicUserProfileResponse {
  userId: number
  displayName: string
  avatarUrl: string | null
  bio: string | null
}

export interface UserProfileResponse {
  userId: number
  email: string
  displayName: string
  avatarUrl: string
  bio: string
  privacyLevel: string
  notifEmailEnabled: boolean
  notifPushEnabled: boolean
  timezone: string
  dailyFocusGoalMinutes: number
  role: string
  emailVerified: boolean
  createdAt: string
}

export interface UpdateProfileRequest {
  displayName?: string
  bio?: string
}

export interface UpdateSettingsRequest {
  privacyLevel?: string
  notifEmailEnabled?: boolean
  notifPushEnabled?: boolean
  timezone?: string
  dailyFocusGoalMinutes?: number
}

export interface BlockedUserResponse {
  userId: number
  displayName: string
  avatarUrl: string | null
  blockedAt: string
}

export const userApi = {
  getProfile: (userId: number) => http.get<ApiResponse<PublicUserProfileResponse>>(`/users/${userId}/profile`),
  getMyProfile: () => http.get<ApiResponse<UserProfileResponse>>('/me/profile'),
  updateProfile: (data: UpdateProfileRequest) => http.put<ApiResponse<UserProfileResponse>>('/me/profile', data),
  updateSettings: (data: UpdateSettingsRequest) => http.put<ApiResponse<UserProfileResponse>>('/me/settings', data),
  uploadAvatar: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return http.post<ApiResponse<UserProfileResponse>>('/me/avatar', formData)
  },

  // Block management
  listBlocks: () => http.get<ApiResponse<BlockedUserResponse[]>>('/me/blocks'),
  blockUser: (targetUserId: number) => http.post<ApiResponse<void>>(`/me/blocks/${targetUserId}`),
  unblockUser: (targetUserId: number) => http.delete<ApiResponse<void>>(`/me/blocks/${targetUserId}`),
}
