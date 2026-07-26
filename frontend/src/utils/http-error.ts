/**
 * Centralized error-message extraction.
 *
 * Replaces the `catch (e: any) { e?.response?.data?.message || '...' }` idiom
 * that was duplicated ~10 times across the app. Always catch `unknown` and run
 * the value through `getErrorMessage`.
 */
import { isAxiosError } from 'axios'
import type { ApiResponse } from '@/types/api'

export function getErrorMessage(err: unknown, fallback = '操作失败，请稍后重试'): string {
  if (isAxiosError(err)) {
    const data = err.response?.data as Partial<ApiResponse<unknown>> | undefined
    if (data?.message) return data.message
    if (err.message) return err.message
  }
  if (err instanceof Error && err.message) return err.message
  if (typeof err === 'string' && err) return err
  return fallback
}
