/**
 * Safe markdown rendering.
 *
 * Replaces the hand-rolled regex `renderMarkdown` that was copy-pasted into
 * JournalDetailView and JournalEditor and produced a stored/cross-user XSS
 * (it did not escape attribute quotes before injecting into `v-html`).
 *
 * - {@link renderMarkdown}: markdown -> sanitized HTML, for client-side live preview.
 * - {@link sanitizeHtml}: sanitize server-rendered HTML (defense in depth) before `v-html`.
 */
import { marked } from 'marked'
import DOMPurify from 'dompurify'

marked.setOptions({ gfm: true, breaks: true })

export function renderMarkdown(md: string | null | undefined): string {
  if (!md) return ''
  const raw = marked.parse(md, { async: false }) as string
  return DOMPurify.sanitize(raw)
}

export function sanitizeHtml(html: string | null | undefined): string {
  if (!html) return ''
  return DOMPurify.sanitize(html)
}
