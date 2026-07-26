import http from './http'
import type { ApiResponse } from '@/types/api'

export interface UploadResult {
  url: string
  objectKey: string
}

export const storageApi = {
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return http.post<ApiResponse<UploadResult>>('/storage/upload', formData)
  },

  getUrl: (bucket: string, objectKey: string) =>
    http.get<ApiResponse<string>>('/storage/url', { params: { bucket, objectKey } }),

  deleteFile: (bucket: string, objectKey: string) =>
    http.delete<ApiResponse<void>>('/storage/delete', { params: { bucket, objectKey } }),
}
