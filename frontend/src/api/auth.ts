import http from './http'

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
  register: (data: RegisterRequest) => http.post('/auth/register', data),
  verifyEmail: (token: string) => http.post('/auth/verify-email', { token }),
  login: (data: LoginRequest) => http.post('/auth/login', data),
  refresh: () => http.post('/auth/refresh'),
  logout: () => http.post('/auth/logout'),
  forgotPassword: (email: string) => http.post('/auth/forgot-password', { email }),
  resetPassword: (token: string, newPassword: string) =>
    http.post('/auth/reset-password', { token, newPassword }),
}
