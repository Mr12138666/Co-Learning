/**
 * File utility functions.
 */

/**
 * Format file size.
 *
 * @param bytes File size in bytes
 * @returns Formatted file size
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  
  return `${(bytes / Math.pow(1024, i)).toFixed(2)} ${units[i]}`
}

/**
 * Get file extension.
 *
 * @param filename Filename
 * @returns File extension
 */
export function getFileExtension(filename: string): string {
  return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2)
}

/**
 * Get filename without extension.
 *
 * @param filename Filename
 * @returns Filename without extension
 */
export function getFilenameWithoutExtension(filename: string): string {
  return filename.replace(/.[^/.]+$/, '')
}

/**
 * Check if file type is image.
 *
 * @param filename Filename
 * @returns true if image
 */
export function isImage(filename: string): boolean {
  const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp']
  const extension = getFileExtension(filename).toLowerCase()
  return imageExtensions.includes(extension)
}

/**
 * Check if file type is video.
 *
 * @param filename Filename
 * @returns true if video
 */
export function isVideo(filename: string): boolean {
  const videoExtensions = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm']
  const extension = getFileExtension(filename).toLowerCase()
  return videoExtensions.includes(extension)
}

/**
 * Check if file type is audio.
 *
 * @param filename Filename
 * @returns true if audio
 */
export function isAudio(filename: string): boolean {
  const audioExtensions = ['mp3', 'wav', 'ogg', 'aac', 'flac']
  const extension = getFileExtension(filename).toLowerCase()
  return audioExtensions.includes(extension)
}

/**
 * Check if file type is document.
 *
 * @param filename Filename
 * @returns true if document
 */
export function isDocument(filename: string): boolean {
  const documentExtensions = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt']
  const extension = getFileExtension(filename).toLowerCase()
  return documentExtensions.includes(extension)
}

/**
 * Check if file type is archive.
 *
 * @param filename Filename
 * @returns true if archive
 */
export function isArchive(filename: string): boolean {
  const archiveExtensions = ['zip', 'rar', '7z', 'tar', 'gz']
  const extension = getFileExtension(filename).toLowerCase()
  return archiveExtensions.includes(extension)
}

/**
 * Generate unique filename.
 *
 * @param originalName Original filename
 * @returns Unique filename
 */
export function generateUniqueFilename(originalName: string): string {
  const extension = getFileExtension(originalName)
  const name = getFilenameWithoutExtension(originalName)
  const timestamp = Date.now()
  const random = Math.random().toString(36).substring(2, 8)
  
  return `${name}_${timestamp}_${random}${extension ? '.' + extension : ''}`
}

/**
 * Read file as text.
 *
 * @param file File to read
 * @returns Promise with file content
 */
export function readFileAsText(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = reject
    reader.readAsText(file)
  })
}

/**
 * Read file as data URL.
 *
 * @param file File to read
 * @returns Promise with data URL
 */
export function readFileAsDataURL(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

/**
 * Download file from URL.
 *
 * @param url File URL
 * @param filename Filename
 */
export function downloadFile(url: string, filename?: string): void {
  const link = document.createElement('a')
  link.href = url
  link.download = filename || ''
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

/**
 * Download text as file.
 *
 * @param text Text content
 * @param filename Filename
 * @param mimeType MIME type
 */
export function downloadTextAsFile(
  text: string,
  filename: string,
  mimeType: string = 'text/plain'
): void {
  const blob = new Blob([text], { type: mimeType })
  const url = URL.createObjectURL(blob)
  downloadFile(url, filename)
  URL.revokeObjectURL(url)
}