/**
 * Route-query normalization.
 *
 * `route.query.x` is `string | null | (string | null)[]` — a duplicated
 * `?redirect=a&redirect=b` yields an array, so blindly casting `as string`
 * lies. `firstQuery` collapses any of those shapes to a single string.
 */
import type { LocationQueryValue } from 'vue-router'

export function firstQuery(
  value: LocationQueryValue | LocationQueryValue[] | undefined,
): string {
  if (Array.isArray(value)) return value[0] ?? ''
  return value ?? ''
}
