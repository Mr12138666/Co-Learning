import http from './http'
import type { ApiResponse, Page } from '@/types/api'

/**
 * Generic API response handler.
 * Provides common methods for API calls with proper error handling.
 */
export class ApiHelper {
  /**
   * Make a GET request.
   *
   * @param url    The URL
   * @param params Query parameters
   * @returns The response data
   */
  static async get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
    const response = await http.get<ApiResponse<T>>(url, { params })
    return response.data.data
  }

  /**
   * Make a POST request.
   *
   * @param url  The URL
   * @param data Request body
   * @returns The response data
   */
  static async post<T>(url: string, data?: unknown): Promise<T> {
    const response = await http.post<ApiResponse<T>>(url, data)
    return response.data.data
  }

  /**
   * Make a PUT request.
   *
   * @param url  The URL
   * @param data Request body
   * @returns The response data
   */
  static async put<T>(url: string, data?: unknown): Promise<T> {
    const response = await http.put<ApiResponse<T>>(url, data)
    return response.data.data
  }

  /**
   * Make a PATCH request.
   *
   * @param url  The URL
   * @param data Request body
   * @returns The response data
   */
  static async patch<T>(url: string, data?: unknown): Promise<T> {
    const response = await http.patch<ApiResponse<T>>(url, data)
    return response.data.data
  }

  /**
   * Make a DELETE request.
   *
   * @param url The URL
   */
  static async delete(url: string): Promise<void> {
    await http.delete(url)
  }

  /**
   * Make a GET request for paginated data.
   *
   * @param url    The URL
   * @param params Query parameters
   * @returns The paginated response
   */
  static async getPage<T>(url: string, params?: Record<string, unknown>): Promise<Page<T>> {
    const response = await http.get<ApiResponse<Page<T>>>(url, { params })
    return response.data.data
  }
}

/**
 * Create a typed API client for a specific resource.
 *
 * @param baseUrl The base URL for the resource
 * @returns API client with common CRUD operations
 */
export function createApiClient<T, C, U>(baseUrl: string) {
  return {
    /**
     * List all resources.
     *
     * @param params Query parameters
     * @returns List of resources
     */
    list: (params?: Record<string, unknown>) => ApiHelper.get<T[]>(baseUrl, params),

    /**
     * Get a resource by ID.
     *
     * @param id Resource ID
     * @returns The resource
     */
    getById: (id: number | string) => ApiHelper.get<T>(`${baseUrl}/${id}`),

    /**
     * Create a new resource.
     *
     * @param data Create request
     * @returns The created resource
     */
    create: (data: C) => ApiHelper.post<T>(baseUrl, data),

    /**
     * Update a resource.
     *
     * @param id   Resource ID
     * @param data Update request
     * @returns The updated resource
     */
    update: (id: number | string, data: U) => ApiHelper.put<T>(`${baseUrl}/${id}`, data),

    /**
     * Delete a resource.
     *
     * @param id Resource ID
     */
    delete: (id: number | string) => ApiHelper.delete(`${baseUrl}/${id}`),
  }
}