/**
 * Shared formatting helpers.
 *
 * Consolidates the divergent `formatSeconds` / `formatMinutes` /
 * `formatTimeFromMinutes` / `formatDate` copies that were scattered across
 * Dashboard, Stats, Checkin and the journal views into a single unit
 * convention: durations render as `1h30min` / `45min`.
 */
import dayjs from 'dayjs'

/** Format a duration given in seconds, e.g. `5400 -> "1h30min"`, `2700 -> "45min"`. */
export function formatDuration(seconds: number): string {
  const totalMinutes = Math.floor(Math.max(0, seconds) / 60)
  const hours = Math.floor(totalMinutes / 60)
  const minutes = totalMinutes % 60
  if (hours > 0 && minutes > 0) return `${hours}h${minutes}min`
  if (hours > 0) return `${hours}h`
  return `${minutes}min`
}

/** Same as {@link formatDuration} but the input is expressed in minutes. */
export function formatDurationFromMinutes(minutes: number): string {
  return formatDuration(Math.round(Math.max(0, minutes) * 60))
}

/** Format an ISO date/timestamp; returns '' for nullish input. */
export function formatDate(
  date: string | number | Date | null | undefined,
  format = 'YYYY-MM-DD HH:mm',
): string {
  if (!date) return ''
  return dayjs(date).format(format)
}

/** Date only, `YYYY-MM-DD`. */
export function formatDay(date: string | number | Date | null | undefined): string {
  return formatDate(date, 'YYYY-MM-DD')
}
