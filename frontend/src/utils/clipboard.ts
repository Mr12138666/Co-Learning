/**
 * Clipboard utility.
 */

/**
 * Copy text to clipboard.
 *
 * @param text Text to copy
 * @returns Promise with success status
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch (error) {
    console.error('Failed to copy to clipboard:', error)
    
    // Fallback for older browsers
    try {
      const textArea = document.createElement('textarea')
      textArea.value = text
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      textArea.style.top = '-999999px'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      const result = document.execCommand('copy')
      document.body.removeChild(textArea)
      return result
    } catch (fallbackError) {
      console.error('Fallback copy failed:', fallbackError)
      return false
    }
  }
}

/**
 * Read text from clipboard.
 *
 * @returns Promise with clipboard text
 */
export async function readFromClipboard(): Promise<string> {
  try {
    return await navigator.clipboard.readText()
  } catch (error) {
    console.error('Failed to read from clipboard:', error)
    return ''
  }
}

/**
 * Copy element content to clipboard.
 *
 * @param element Element to copy
 * @returns Promise with success status
 */
export async function copyElementToClipboard(element: HTMLElement): Promise<boolean> {
  const text = element.innerText || element.textContent || ''
  return copyToClipboard(text)
}

/**
 * Copy HTML to clipboard.
 *
 * @param html HTML to copy
 * @returns Promise with success status
 */
export async function copyHtmlToClipboard(html: string): Promise<boolean> {
  try {
    const blob = new Blob([html], { type: 'text/html' })
    const item = new ClipboardItem({ 'text/html': blob })
    await navigator.clipboard.write([item])
    return true
  } catch (error) {
    console.error('Failed to copy HTML to clipboard:', error)
    return false
  }
}