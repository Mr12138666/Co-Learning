/**
 * Error handling utility.
 */

export interface AppError {
  code: string
  message: string
  details?: unknown
  timestamp: Date
}

export class ErrorHandler {
  private static instance: ErrorHandler
  private errors: AppError[] = []
  
  private constructor() {}
  
  /**
   * Get ErrorHandler instance.
   */
  static getInstance(): ErrorHandler {
    if (!ErrorHandler.instance) {
      ErrorHandler.instance = new ErrorHandler()
    }
    return ErrorHandler.instance
  }
  
  /**
   * Handle an error.
   *
   * @param error Error to handle
   * @param context Error context
   */
  handle(error: unknown, context?: string): AppError {
    const appError = this.normalize(error, context)
    this.errors.push(appError)
    
    // Log to console in development
    if (import.meta.env.DEV) {
      console.error('[ErrorHandler]', appError)
    }
    
    // TODO: Send to error reporting service in production
    // if (import.meta.env.PROD) {
    //   this.reportError(appError)
    // }
    
    return appError
  }
  
  /**
   * Normalize error to AppError.
   *
   * @param error Error to normalize
   * @param context Error context
   * @returns Normalized error
   */
  private normalize(error: unknown, context?: string): AppError {
    let code = 'UNKNOWN_ERROR'
    let message = '发生未知错误'
    let details = undefined
    
    if (error instanceof Error) {
      code = error.name || 'ERROR'
      message = error.message
      details = error.stack
    } else if (typeof error === 'string') {
      message = error
    } else if (typeof error === 'object' && error !== null) {
      const errorObj = error as Record<string, unknown>
      code = (errorObj.code as string) || 'ERROR'
      message = (errorObj.message as string) || '发生错误'
      details = errorObj.details
    }
    
    return {
      code,
      message: context ? `${context}: ${message}` : message,
      details,
      timestamp: new Date(),
    }
  }
  
  /**
   * Get all errors.
   */
  getErrors(): AppError[] {
    return [...this.errors]
  }
  
  /**
   * Get last error.
   */
  getLastError(): AppError | undefined {
    return this.errors[this.errors.length - 1]
  }
  
  /**
   * Clear all errors.
   */
  clearErrors(): void {
    this.errors = []
  }
  
  /**
   * Get error count.
   */
  getErrorCount(): number {
    return this.errors.length
  }
}

/**
 * Composable for error handling.
 */
export function useErrorHandler() {
  const errorHandler = ErrorHandler.getInstance()
  
  function handleError(error: unknown, context?: string): AppError {
    return errorHandler.handle(error, context)
  }
  
  function getErrors(): AppError[] {
    return errorHandler.getErrors()
  }
  
  function getLastError(): AppError | undefined {
    return errorHandler.getLastError()
  }
  
  function clearErrors(): void {
    errorHandler.clearErrors()
  }
  
  return {
    handleError,
    getErrors,
    getLastError,
    clearErrors,
  }
}

/**
 * Global error handler for unhandled errors.
 */
export function setupGlobalErrorHandlers(): void {
  const errorHandler = ErrorHandler.getInstance()
  
  // Handle unhandled promise rejections
  window.addEventListener('unhandledrejection', (event) => {
    errorHandler.handle(event.reason, 'Unhandled Promise Rejection')
    event.preventDefault()
  })
  
  // Handle uncaught errors
  window.addEventListener('error', (event) => {
    errorHandler.handle(event.error, 'Uncaught Error')
    event.preventDefault()
  })
}