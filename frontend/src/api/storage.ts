import http from './http'

export interface UploadResult {
  url: string
  objectKey: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export const storageApi = {
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return http.post<ApiResponse<UploadResult>>('/storage/upload', formData)
  },

  getUrl: (bucket: string, objectKey: string) =>
    http.get<string>('/storage/url', { params: { bucket, objectKey } }),

  deleteFile: (bucket: string, objectKey: string) =>
    http.delete('/storage/delete', { params: { bucket, objectKey } }),
}
