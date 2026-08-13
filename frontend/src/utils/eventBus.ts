/**
 * Event bus for component communication.
 */

type EventHandler = (...args: unknown[]) => void

class EventBus {
  private handlers: Map<string, Set<EventHandler>> = new Map()
  
  /**
   * Subscribe to an event.
   *
   * @param event Event name
   * @param handler Event handler
   * @returns Unsubscribe function
   */
  on(event: string, handler: EventHandler): () => void {
    if (!this.handlers.has(event)) {
      this.handlers.set(event, new Set())
    }
    
    this.handlers.get(event)!.add(handler)
    
    // Return unsubscribe function
    return () => {
      this.off(event, handler)
    }
  }
  
  /**
   * Subscribe to an event once.
   *
   * @param event Event name
   * @param handler Event handler
   * @returns Unsubscribe function
   */
  once(event: string, handler: EventHandler): () => void {
    const wrappedHandler: EventHandler = (...args) => {
      handler(...args)
      this.off(event, wrappedHandler)
    }
    
    return this.on(event, wrappedHandler)
  }
  
  /**
   * Unsubscribe from an event.
   *
   * @param event Event name
   * @param handler Event handler
   */
  off(event: string, handler: EventHandler): void {
    const handlers = this.handlers.get(event)
    if (handlers) {
      handlers.delete(handler)
      if (handlers.size === 0) {
        this.handlers.delete(event)
      }
    }
  }
  
  /**
   * Emit an event.
   *
   * @param event Event name
   * @param args Event arguments
   */
  emit(event: string, ...args: unknown[]): void {
    const handlers = this.handlers.get(event)
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(...args)
        } catch (error) {
          console.error(`Error in event handler for "${event}":`, error)
        }
      })
    }
  }
  
  /**
   * Clear all event handlers.
   */
  clear(): void {
    this.handlers.clear()
  }
  
  /**
   * Get all event names.
   *
   * @returns Array of event names
   */
  getEvents(): string[] {
    return Array.from(this.handlers.keys())
  }
  
  /**
   * Check if event has handlers.
   *
   * @param event Event name
   * @returns true if event has handlers
   */
  has(event: string): boolean {
    return this.handlers.has(event) && this.handlers.get(event)!.size > 0
  }
}

// Export singleton instance
export const eventBus = new EventBus()

/**
 * Composable for event bus.
 */
export function useEventBus() {
  return {
    on: eventBus.on.bind(eventBus),
    once: eventBus.once.bind(eventBus),
    off: eventBus.off.bind(eventBus),
    emit: eventBus.emit.bind(eventBus),
    clear: eventBus.clear.bind(eventBus),
  }
}