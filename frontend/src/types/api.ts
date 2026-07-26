/**
 * Shared API envelope types.
 *
 * Mirrors the backend `ApiResponse<T>` (com.colearning.common.dto.ApiResponse)
 * and `PageResponse<T>` records — the single source of truth for the response
 * shape every endpoint returns.
 */

/** Unified response envelope. `code === '0'` means success. */
export interface ApiResponse<T> {
  code: string
  message: string
  data: T
  /** Optional — omitted by the backend (`@JsonInclude(NON_NULL)`) when absent. */
  traceId?: string
  timestamp?: string
}

/** Paginated payload wrapper. */
export interface Page<T> {
  items: T[]
  page: number
  size: number
  total: number
  totalPages: number
}
