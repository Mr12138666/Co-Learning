/**
 * Performance optimization configuration.
 */

export const performanceConfig = {
  // Lazy loading
  lazyLoading: {
    // Enable lazy loading for routes
    routes: true,
    // Enable lazy loading for components
    components: true,
    // Enable lazy loading for images
    images: true,
  },
  
  // Caching
  caching: {
    // Enable API response caching
    api: true,
    // Cache duration in milliseconds (5 minutes)
    apiDuration: 5 * 60 * 1000,
    // Enable component caching
    components: true,
    // Enable state persistence
    state: true,
  },
  
  // Compression
  compression: {
    // Enable gzip compression
    gzip: true,
    // Enable brotli compression
    brotli: true,
    // Minimum file size for compression (bytes)
    minSize: 1024,
  },
  
  // Bundle optimization
  bundle: {
    // Enable code splitting
    codeSplitting: true,
    // Enable tree shaking
    treeShaking: true,
    // Enable chunk splitting
    chunkSplitting: true,
    // Maximum chunk size (bytes)
    maxChunkSize: 250000,
  },
  
  // Image optimization
  images: {
    // Enable WebP format
    webp: true,
    // Enable lazy loading
    lazy: true,
    // Enable responsive images
    responsive: true,
    // Image quality (0-100)
    quality: 80,
  },
  
  // Font optimization
  fonts: {
    // Enable font subsetting
    subsetting: true,
    // Enable font preloading
    preloading: true,
    // Enable font display swap
    displaySwap: true,
  },
  
  // Preloading
  preloading: {
    // Enable critical resource preloading
    critical: true,
    // Enable route preloading
    routes: true,
    // Enable component preloading
    components: true,
  },
}

/**
 * Performance monitoring configuration.
 */
export const monitoringConfig = {
  // Enable performance monitoring
  enabled: true,
  
  // Metrics to collect
  metrics: {
    // First Contentful Paint
    fcp: true,
    // Largest Contentful Paint
    lcp: true,
    // First Input Delay
    fid: true,
    // Cumulative Layout Shift
    cls: true,
    // Time to First Byte
    ttfb: true,
    // Time to Interactive
    tti: true,
  },
  
  // Reporting
  reporting: {
    // Enable console logging
    console: true,
    // Enable remote reporting
    remote: false,
    // Remote endpoint
    endpoint: '/api/performance',
    // Reporting interval (milliseconds)
    interval: 30000,
  },
}