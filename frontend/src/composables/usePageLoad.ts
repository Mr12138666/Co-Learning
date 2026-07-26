import { ref } from 'vue'

/**
 * Composable for page-level loading and error state management.
 * Wraps async data loading with consistent error handling.
 */
export function usePageLoad() {
  const loading = ref(true)
  const error = ref<string | null>(null)

  async function load(fn: () => Promise<void>) {
    loading.value = true
    error.value = null
    try {
      await fn()
    } catch (e: any) {
      error.value = e?.response?.data?.message || e?.message || '加载失败，请稍后重试'
    } finally {
      loading.value = false
    }
  }

  function retry(fn: () => Promise<void>) {
    return load(fn)
  }

  return { loading, error, load, retry }
}
