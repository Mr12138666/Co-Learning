import { ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'

/**
 * API request state interface.
 */
export interface ApiState<T> {
  data: Ref<T | null>
  loading: Ref<boolean>
  error: Ref<string | null>
  success: Ref<boolean>
}

/**
 * API request options interface.
 */
export interface ApiOptions {
  /** Show loading state */
  showLoading?: boolean
  /** Show error state */
  showError?: boolean
  /** Auto reset on success */
  autoReset?: boolean
  /** Initial data */
  initialData?: any
}

/**
 * Composable for API requests with loading and error handling.
 *
 * @param options API options
 * @returns API state and methods
 */
export function useApi<T>(options: ApiOptions = {}) {
  const {
    showLoading = true,
    showError = true,
    autoReset = false,
    initialData = null,
  } = options
  
  // State
  const data = ref<T | null>(initialData) as Ref<T | null>
  const loading = ref(false)
  const error = ref<string | null>(null)
  const success = ref(false)
  
  // Computed
  const isIdle = computed(() => !loading.value && !error.value && !success.value)
  const isLoading = computed(() => loading.value)
  const isError = computed(() => !!error.value)
  const isSuccess = computed(() => success.value)
  
  // Methods
  async function execute<R>(apiCall: () => Promise<R>): Promise<R | null> {
    // Reset state
    loading.value = showLoading
    error.value = null
    success.value = false
    
    try {
      const result = await apiCall()
      data.value = result as unknown as T
      success.value = true
      
      if (autoReset) {
        setTimeout(() => {
          success.value = false
        }, 3000)
      }
      
      return result
    } catch (e) {
      if (showError) {
        error.value = e instanceof Error ? e.message : '请求失败'
      }
      return null
    } finally {
      loading.value = false
    }
  }
  
  function reset() {
    data.value = initialData
    loading.value = false
    error.value = null
    success.value = false
  }
  
  function clearError() {
    error.value = null
  }
  
  function setData(newData: T) {
    data.value = newData
  }
  
  return {
    // State
    data,
    loading,
    error,
    success,
    
    // Computed
    isIdle,
    isLoading,
    isError,
    isSuccess,
    
    // Methods
    execute,
    reset,
    clearError,
    setData,
  }
}

/**
 * Composable for paginated API requests.
 *
 * @param options API options
 * @returns Paginated API state and methods
 */
export function usePaginatedApi<T>(options: ApiOptions = {}) {
  const api = useApi<T[]>(options)
  
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  
  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
  const hasNext = computed(() => page.value < totalPages.value)
  const hasPrev = computed(() => page.value > 1)
  
  function setPage(newPage: number) {
    page.value = newPage
  }
  
  function setPageSize(newPageSize: number) {
    pageSize.value = newPageSize
    page.value = 1
  }
  
  function setTotal(newTotal: number) {
    total.value = newTotal
  }
  
  function nextPage() {
    if (hasNext.value) {
      page.value++
    }
  }
  
  function prevPage() {
    if (hasPrev.value) {
      page.value--
    }
  }
  
  function firstPage() {
    page.value = 1
  }
  
  function lastPage() {
    page.value = totalPages.value
  }
  
  return {
    ...api,
    
    // Pagination state
    page,
    pageSize,
    total,
    
    // Pagination computed
    totalPages,
    hasNext,
    hasPrev,
    
    // Pagination methods
    setPage,
    setPageSize,
    setTotal,
    nextPage,
    prevPage,
    firstPage,
    lastPage,
  }
}