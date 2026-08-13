/**
 * Cache management utility.
 */

export interface CacheConfig {
  /** Cache prefix */
  prefix?: string
  /** Default TTL in milliseconds */
  defaultTTL?: number
  /** Maximum cache size */
  maxSize?: number
}

interface CacheItem<T> {
  value: T
  expiry: number
  createdAt: number
}

class CacheManager {
  private cache: Map<string, CacheItem<unknown>> = new Map()
  private prefix: string
  private defaultTTL: number
  private maxSize: number
  
  constructor(config: CacheConfig = {}) {
    this.prefix = config.prefix || 'cache'
    this.defaultTTL = config.defaultTTL || 5 * 60 * 1000 // 5 minutes
    this.maxSize = config.maxSize || 100
  }
  
  /**
   * Generate cache key with prefix.
   *
   * @param key Original key
   * @returns Prefixed key
   */
  private getKey(key: string): string {
    return `${this.prefix}:${key}`
  }
  
  /**
   * Check if cache item is expired.
   *
   * @param item Cache item
   * @returns true if expired
   */
  private isExpired(item: CacheItem<unknown>): boolean {
    return Date.now() > item.expiry
  }
  
  /**
   * Evict expired items.
   */
  private evictExpired(): void {
    for (const [key, item] of this.cache.entries()) {
      if (this.isExpired(item)) {
        this.cache.delete(key)
      }
    }
  }
  
  /**
   * Evict oldest items if cache is full.
   */
  private evictOldest(): void {
    if (this.cache.size >= this.maxSize) {
      const oldestKey = this.cache.keys().next().value
      if (oldestKey) {
        this.cache.delete(oldestKey)
      }
    }
  }
  
  /**
   * Set a value in cache.
   *
   * @param key Cache key
   * @param value Value to cache
   * @param ttl Time to live in milliseconds
   */
  set<T>(key: string, value: T, ttl?: number): void {
    this.evictExpired()
    this.evictOldest()
    
    const fullKey = this.getKey(key)
    const expiry = Date.now() + (ttl || this.defaultTTL)
    
    this.cache.set(fullKey, {
      value,
      expiry,
      createdAt: Date.now(),
    })
  }
  
  /**
   * Get a value from cache.
   *
   * @param key Cache key
   * @returns Cached value or undefined
   */
  get<T>(key: string): T | undefined {
    const fullKey = this.getKey(key)
    const item = this.cache.get(fullKey) as CacheItem<T> | undefined
    
    if (!item) {
      return undefined
    }
    
    if (this.isExpired(item)) {
      this.cache.delete(fullKey)
      return undefined
    }
    
    return item.value
  }
  
  /**
   * Get or set pattern.
   *
   * @param key Cache key
   * @param factory Value factory
   * @param ttl Time to live
   * @returns Cached or computed value
   */
  async getOrSet<T>(key: string, factory: () => Promise<T>, ttl?: number): Promise<T> {
    const cached = this.get<T>(key)
    if (cached !== undefined) {
      return cached
    }
    
    const value = await factory()
    this.set(key, value, ttl)
    return value
  }
  
  /**
   * Delete a value from cache.
   *
   * @param key Cache key
   * @returns true if deleted
   */
  delete(key: string): boolean {
    const fullKey = this.getKey(key)
    return this.cache.delete(fullKey)
  }
  
  /**
   * Check if key exists in cache.
   *
   * @param key Cache key
   * @returns true if exists
   */
  has(key: string): boolean {
    const fullKey = this.getKey(key)
    const item = this.cache.get(fullKey)
    
    if (!item) {
      return false
    }
    
    if (this.isExpired(item)) {
      this.cache.delete(fullKey)
      return false
    }
    
    return true
  }
  
  /**
   * Clear all cache.
   */
  clear(): void {
    this.cache.clear()
  }
  
  /**
   * Get cache size.
   *
   * @returns Cache size
   */
  size(): number {
    this.evictExpired()
    return this.cache.size
  }
  
  /**
   * Get all cache keys.
   *
   * @returns Array of keys
   */
  keys(): string[] {
    this.evictExpired()
    return Array.from(this.cache.keys()).map(key => 
      key.replace(`${this.prefix}:`, '')
    )
  }
  
  /**
   * Get cache statistics.
   *
   * @returns Cache statistics
   */
  stats(): { size: number; maxSize: number; prefix: string; defaultTTL: number } {
    this.evictExpired()
    return {
      size: this.cache.size,
      maxSize: this.maxSize,
      prefix: this.prefix,
      defaultTTL: this.defaultTTL,
    }
  }
}

// Create default cache instance
export const cache = new CacheManager()

/**
 * Create a new cache instance.
 *
 * @param config Cache configuration
 * @returns Cache instance
 */
export function createCache(config: CacheConfig): CacheManager {
  return new CacheManager(config)
}

/**
 * Composable for cache management.
 */
export function useCache(config?: CacheConfig) {
  const cacheInstance = config ? new CacheManager(config) : cache
  
  return {
    set: cacheInstance.set.bind(cacheInstance),
    get: cacheInstance.get.bind(cacheInstance),
    getOrSet: cacheInstance.getOrSet.bind(cacheInstance),
    delete: cacheInstance.delete.bind(cacheInstance),
    has: cacheInstance.has.bind(cacheInstance),
    clear: cacheInstance.clear.bind(cacheInstance),
    size: cacheInstance.size.bind(cacheInstance),
    keys: cacheInstance.keys.bind(cacheInstance),
    stats: cacheInstance.stats.bind(cacheInstance),
  }
}