import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'

/**
 * Generic store state interface.
 */
export interface StoreState<T> {
  items: Ref<T[]>
  loading: Ref<boolean>
  error: Ref<string | null>
  selectedId: Ref<number | string | null>
}

/**
 * Generic store getters interface.
 */
export interface StoreGetters<T> {
  selectedItem: ComputedRef<T | null>
  isEmpty: ComputedRef<boolean>
  count: ComputedRef<number>
}

/**
 * Generic store actions interface.
 */
export interface StoreActions<T, C, U> {
  fetchAll: () => Promise<void>
  fetchById: (id: number | string) => Promise<T>
  create: (data: C) => Promise<T>
  update: (id: number | string, data: U) => Promise<T>
  remove: (id: number | string) => Promise<void>
  select: (id: number | string | null) => void
  clearError: () => void
}

/**
 * Create a generic store for a resource.
 *
 * @param storeId   The store ID
 * @param apiClient The API client
 * @returns The store
 */
export function createResourceStore<T extends { id: number | string }, C, U>(
  storeId: string,
  apiClient: {
    list: (params?: Record<string, unknown>) => Promise<T[]>
    getById: (id: number | string) => Promise<T>
    create: (data: C) => Promise<T>
    update: (id: number | string, data: U) => Promise<T>
    delete: (id: number | string) => Promise<void>
  }
) {
  return defineStore(storeId, () => {
    // State
    const items = ref<T[]>([]) as Ref<T[]>
    const loading = ref(false)
    const error = ref<string | null>(null)
    const selectedId = ref<number | string | null>(null)

    // Getters
    const selectedItem = computed(() => {
      if (selectedId.value === null) return null
      return items.value.find((item) => item.id === selectedId.value) || null
    })

    const isEmpty = computed(() => items.value.length === 0)
    const count = computed(() => items.value.length)

    // Actions
    async function fetchAll() {
      loading.value = true
      error.value = null
      try {
        items.value = await apiClient.list()
      } catch (e) {
        error.value = e instanceof Error ? e.message : 'Failed to fetch items'
        throw e
      } finally {
        loading.value = false
      }
    }

    async function fetchById(id: number | string) {
      loading.value = true
      error.value = null
      try {
        const item = await apiClient.getById(id)
        const index = items.value.findIndex((i) => i.id === id)
        if (index !== -1) {
          items.value[index] = item
        } else {
          items.value.push(item)
        }
        return item
      } catch (e) {
        error.value = e instanceof Error ? e.message : 'Failed to fetch item'
        throw e
      } finally {
        loading.value = false
      }
    }

    async function create(data: C) {
      loading.value = true
      error.value = null
      try {
        const item = await apiClient.create(data)
        items.value.push(item)
        return item
      } catch (e) {
        error.value = e instanceof Error ? e.message : 'Failed to create item'
        throw e
      } finally {
        loading.value = false
      }
    }

    async function update(id: number | string, data: U) {
      loading.value = true
      error.value = null
      try {
        const item = await apiClient.update(id, data)
        const index = items.value.findIndex((i) => i.id === id)
        if (index !== -1) {
          items.value[index] = item
        }
        return item
      } catch (e) {
        error.value = e instanceof Error ? e.message : 'Failed to update item'
        throw e
      } finally {
        loading.value = false
      }
    }

    async function remove(id: number | string) {
      loading.value = true
      error.value = null
      try {
        await apiClient.delete(id)
        items.value = items.value.filter((i) => i.id !== id)
        if (selectedId.value === id) {
          selectedId.value = null
        }
      } catch (e) {
        error.value = e instanceof Error ? e.message : 'Failed to delete item'
        throw e
      } finally {
        loading.value = false
      }
    }

    function select(id: number | string | null) {
      selectedId.value = id
    }

    function clearError() {
      error.value = null
    }

    return {
      // State
      items,
      loading,
      error,
      selectedId,
      // Getters
      selectedItem,
      isEmpty,
      count,
      // Actions
      fetchAll,
      fetchById,
      create,
      update,
      remove,
      select,
      clearError,
    }
  })
}