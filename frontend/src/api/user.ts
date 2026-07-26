import http from './http'

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
  getProfile: (userId: number) => http.get(`/users/${userId}/profile`),
  getMyProfile: () => http.get('/me/profile'),
  updateProfile: (data: UpdateProfileRequest) => http.put('/me/profile', data),
  updateSettings: (data: UpdateSettingsRequest) => http.put('/me/settings', data),
  uploadAvatar: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return http.post('/me/avatar', formData)
  },

  // Block management
  listBlocks: () => http.get('/me/blocks'),
  blockUser: (targetUserId: number) => http.post(`/me/blocks/${targetUserId}`),
  unblockUser: (targetUserId: number) => http.delete(`/me/blocks/${targetUserId}`),
}
