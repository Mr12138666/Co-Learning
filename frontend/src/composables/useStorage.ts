import { ref, watch } from 'vue'
import type { Ref } from 'vue'

/**
 * Composable for localStorage with reactive state.
 *
 * @param key Storage key
 * @param defaultValue Default value
 * @returns Reactive value and setter
 */
export function useLocalStorage<T>(key: string, defaultValue: T): [Ref<T>, (value: T) => void] {
  // Get initial value from localStorage
  const stored = localStorage.getItem(key)
  const initial = stored ? JSON.parse(stored) : defaultValue
  
  // Create reactive ref
  const value = ref<T>(initial) as Ref<T>
  
  // Watch for changes and sync to localStorage
  watch(value, (newValue) => {
    localStorage.setItem(key, JSON.stringify(newValue))
  }, { deep: true })
  
  // Setter function
  function setValue(newValue: T) {
    value.value = newValue
  }
  
  return [value, setValue]
}

/**
 * Composable for sessionStorage with reactive state.
 *
 * @param key Storage key
 * @param defaultValue Default value
 * @returns Reactive value and setter
 */
export function useSessionStorage<T>(key: string, defaultValue: T): [Ref<T>, (value: T) => void] {
  // Get initial value from sessionStorage
  const stored = sessionStorage.getItem(key)
  const initial = stored ? JSON.parse(stored) : defaultValue
  
  // Create reactive ref
  const value = ref<T>(initial) as Ref<T>
  
  // Watch for changes and sync to sessionStorage
  watch(value, (newValue) => {
    sessionStorage.setItem(key, JSON.stringify(newValue))
  }, { deep: true })
  
  // Setter function
  function setValue(newValue: T) {
    value.value = newValue
  }
  
  return [value, setValue]
}