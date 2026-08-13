/**
 * General utility functions.
 */

/**
 * Debounce function execution.
 *
 * @param fn Function to debounce
 * @param delay Delay in milliseconds
 * @returns Debounced function
 */
export function debounce<T extends (...args: unknown[]) => unknown>(
  fn: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: ReturnType<typeof setTimeout>
  
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => fn(...args), delay)
  }
}

/**
 * Throttle function execution.
 *
 * @param fn Function to throttle
 * @param limit Limit in milliseconds
 * @returns Throttled function
 */
export function throttle<T extends (...args: unknown[]) => unknown>(
  fn: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle = false
  
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      fn(...args)
      inThrottle = true
      setTimeout(() => {
        inThrottle = false
      }, limit)
    }
  }
}

/**
 * Deep clone an object.
 *
 * @param obj Object to clone
 * @returns Cloned object
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') {
    return obj
  }
  
  if (obj instanceof Date) {
    return new Date(obj.getTime()) as unknown as T
  }
  
  if (obj instanceof Array) {
    return obj.map(item => deepClone(item)) as unknown as T
  }
  
  if (obj instanceof Object) {
    const clonedObj = {} as T
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
  
  return obj
}

/**
 * Check if object is empty.
 *
 * @param obj Object to check
 * @returns true if empty
 */
export function isEmpty(obj: unknown): boolean {
  if (obj === null || obj === undefined) {
    return true
  }
  
  if (typeof obj === 'string' || Array.isArray(obj)) {
    return obj.length === 0
  }
  
  if (typeof obj === 'object') {
    return Object.keys(obj).length === 0
  }
  
  return false
}

/**
 * Format number with commas.
 *
 * @param num Number to format
 * @returns Formatted number
 */
export function formatNumber(num: number): string {
  return num.toString().replace(/B(?=(d{3})+(?!d))/g, ',')
}

/**
 * Format date to string.
 *
 * @param date Date to format
 * @param format Format string
 * @returns Formatted date
 */
export function formatDate(date: Date | string, format: string = 'YYYY-MM-DD'): string {
  const d = typeof date === 'string' ? new Date(date) : date
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * Generate unique ID.
 *
 * @returns Unique ID
 */
export function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

/**
 * Sleep for specified milliseconds.
 *
 * @param ms Milliseconds to sleep
 * @returns Promise
 */
export function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * Retry function execution.
 *
 * @param fn Function to retry
 * @param maxAttempts Maximum attempts
 * @param delay Delay between attempts
 * @returns Promise with result
 */
export async function retry<T>(
  fn: () => Promise<T>,
  maxAttempts: number = 3,
  delay: number = 1000
): Promise<T> {
  let lastError: Error | null = null
  
  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      return await fn()
    } catch (error) {
      lastError = error instanceof Error ? error : new Error(String(error))
      
      if (attempt < maxAttempts) {
        await sleep(delay * attempt)
      }
    }
  }
  
  throw lastError
}

/**
 * Group array by key.
 *
 * @param array Array to group
 * @param keyFn Key function
 * @returns Grouped object
 */
export function groupBy<T>(array: T[], keyFn: (item: T) => string): Record<string, T[]> {
  return array.reduce((groups, item) => {
    const key = keyFn(item)
    if (!groups[key]) {
      groups[key] = []
    }
    groups[key].push(item)
    return groups
  }, {} as Record<string, T[]>)
}

/**
 * Sort array by key.
 *
 * @param array Array to sort
 * @param keyFn Key function
 * @param order Sort order ('asc' or 'desc')
 * @returns Sorted array
 */
export function sortBy<T>(
  array: T[],
  keyFn: (item: T) => string | number,
  order: 'asc' | 'desc' = 'asc'
): T[] {
  return [...array].sort((a, b) => {
    const keyA = keyFn(a)
    const keyB = keyFn(b)
    
    if (keyA < keyB) return order === 'asc' ? -1 : 1
    if (keyA > keyB) return order === 'asc' ? 1 : -1
    return 0
  })
}

/**
 * Remove duplicates from array.
 *
 * @param array Array with duplicates
 * @param keyFn Key function
 * @returns Array without duplicates
 */
export function uniqueBy<T>(array: T[], keyFn: (item: T) => string | number): T[] {
  const seen = new Set<string | number>()
  return array.filter(item => {
    const key = keyFn(item)
    if (seen.has(key)) {
      return false
    }
    seen.add(key)
    return true
  })
}

/**
 * Chunk array into smaller arrays.
 *
 * @param array Array to chunk
 * @param size Chunk size
 * @returns Array of chunks
 */
export function chunk<T>(array: T[], size: number): T[][] {
  const chunks: T[][] = []
  for (let i = 0; i < array.length; i += size) {
    chunks.push(array.slice(i, i + size))
  }
  return chunks
}