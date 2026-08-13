/**
 * Performance monitoring utility.
 */

interface PerformanceMetric {
  name: string
  value: number
  rating: 'good' | 'needs-improvement' | 'poor'
  timestamp: number
}

interface PerformanceReport {
  metrics: PerformanceMetric[]
  summary: {
    totalMetrics: number
    goodCount: number
    needsImprovementCount: number
    poorCount: number
  }
  recommendations: string[]
}

class PerformanceMonitor {
  private metrics: PerformanceMetric[] = []
  private observers: Map<string, PerformanceObserver> = new Map()
  
  constructor() {
    this.initObservers()
  }
  
  private initObservers() {
    // Observe Largest Contentful Paint
    if ('PerformanceObserver' in window) {
      try {
        const lcpObserver = new PerformanceObserver((list) => {
          const entries = list.getEntries()
          const lastEntry = entries[entries.length - 1]
          this.recordMetric('LCP', lastEntry.startTime, this.getLCPRating(lastEntry.startTime))
        })
        lcpObserver.observe({ type: 'largest-contentful-paint', buffered: true })
        this.observers.set('lcp', lcpObserver)
      } catch (e) {
        console.warn('LCP observer not supported')
      }
      
      // Observe First Input Delay
      try {
        const fidObserver = new PerformanceObserver((list) => {
          const entries = list.getEntries()
          entries.forEach((entry: any) => {
            this.recordMetric('FID', entry.processingStart - entry.startTime, this.getFIDRating(entry.processingStart - entry.startTime))
          })
        })
        fidObserver.observe({ type: 'first-input', buffered: true })
        this.observers.set('fid', fidObserver)
      } catch (e) {
        console.warn('FID observer not supported')
      }
      
      // Observe Cumulative Layout Shift
      try {
        const clsObserver = new PerformanceObserver((list) => {
          let clsValue = 0
          list.getEntries().forEach((entry: any) => {
            if (!entry.hadRecentInput) {
              clsValue += entry.value
            }
          })
          this.recordMetric('CLS', clsValue, this.getCLSRating(clsValue))
        })
        clsObserver.observe({ type: 'layout-shift', buffered: true })
        this.observers.set('cls', clsObserver)
      } catch (e) {
        console.warn('CLS observer not supported')
      }
    }
  }
  
  private recordMetric(name: string, value: number, rating: 'good' | 'needs-improvement' | 'poor') {
    const metric: PerformanceMetric = {
      name,
      value,
      rating,
      timestamp: Date.now(),
    }
    this.metrics.push(metric)
    
    // Log to console in development
    if (import.meta.env.DEV) {
      console.log(`[Performance] ${name}: ${value.toFixed(2)}ms (${rating})`)
    }
  }
  
  private getLCPRating(value: number): 'good' | 'needs-improvement' | 'poor' {
    if (value <= 2500) return 'good'
    if (value <= 4000) return 'needs-improvement'
    return 'poor'
  }
  
  private getFIDRating(value: number): 'good' | 'needs-improvement' | 'poor' {
    if (value <= 100) return 'good'
    if (value <= 300) return 'needs-improvement'
    return 'poor'
  }
  
  private getCLSRating(value: number): 'good' | 'needs-improvement' | 'poor' {
    if (value <= 0.1) return 'good'
    if (value <= 0.25) return 'needs-improvement'
    return 'poor'
  }
  
  /**
   * Get all recorded metrics.
   */
  getMetrics(): PerformanceMetric[] {
    return [...this.metrics]
  }
  
  /**
   * Get performance report.
   */
  getReport(): PerformanceReport {
    const goodCount = this.metrics.filter((m) => m.rating === 'good').length
    const needsImprovementCount = this.metrics.filter((m) => m.rating === 'needs-improvement').length
    const poorCount = this.metrics.filter((m) => m.rating === 'poor').length
    
    const recommendations: string[] = []
    
    // Generate recommendations based on metrics
    const lcp = this.metrics.find((m) => m.name === 'LCP')
    if (lcp && lcp.rating !== 'good') {
      recommendations.push('优化 Largest Contentful Paint：减少服务器响应时间，优化关键资源加载')
    }
    
    const fid = this.metrics.find((m) => m.name === 'FID')
    if (fid && fid.rating !== 'good') {
      recommendations.push('优化 First Input Delay：减少 JavaScript 执行时间，使用 Web Workers')
    }
    
    const cls = this.metrics.find((m) => m.name === 'CLS')
    if (cls && cls.rating !== 'good') {
      recommendations.push('优化 Cumulative Layout Shift：为图片和视频设置尺寸，避免动态内容插入')
    }
    
    return {
      metrics: this.metrics,
      summary: {
        totalMetrics: this.metrics.length,
        goodCount,
        needsImprovementCount,
        poorCount,
      },
      recommendations,
    }
  }
  
  /**
   * Clear all metrics.
   */
  clearMetrics() {
    this.metrics = []
  }
  
  /**
   * Disconnect all observers.
   */
  disconnect() {
    this.observers.forEach((observer) => observer.disconnect())
    this.observers.clear()
  }
}

// Export singleton instance
export const performanceMonitor = new PerformanceMonitor()

/**
 * Composable for performance monitoring.
 */
export function usePerformanceMonitor() {
  return {
    getMetrics: () => performanceMonitor.getMetrics(),
    getReport: () => performanceMonitor.getReport(),
    clearMetrics: () => performanceMonitor.clearMetrics(),
  }
}