import http from './http'
import type { ApiResponse } from '@/types/api'

export interface RegisterRequest {
  email: string
  password: string
  displayName: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface TokenResponse {
  accessToken: string
  accessTokenExpiresIn: number
  accessTokenExpiresAt: string
  userId: number
  email: string
  role: string
  emailVerified: boolean
  displayName: string
  avatarUrl: string | null
}

export const authApi = {
  register: (data: RegisterRequest) => http.post<ApiResponse<void>>('/auth/register', data),
  verifyEmail: (token: string) => http.post<ApiResponse<void>>('/auth/verify-email', { token }),
  login: (data: LoginRequest) => http.post<ApiResponse<TokenResponse>>('/auth/login', data),
  refresh: () => http.post<ApiResponse<TokenResponse>>('/auth/refresh'),
  logout: () => http.post<ApiResponse<void>>('/auth/logout'),
  forgotPassword: (email: string) => http.post<ApiResponse<void>>('/auth/forgot-password', { email }),
  resetPassword: (token: string, newPassword: string) =>
    http.post<ApiResponse<void>>('/auth/reset-password', { token, newPassword }),
}
