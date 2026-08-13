/**
 * Drag and drop utility.
 */

export interface DragItem {
  id: string
  type: string
  data: unknown
}

export interface DropTarget {
  id: string
  accepts: string[]
  onDrop: (item: DragItem) => void
}

export interface DragState {
  isDragging: boolean
  dragItem: DragItem | null
  dropTarget: DropTarget | null
}

class DragAndDropManager {
  private dragItem: DragItem | null = null
  private dropTargets: Map<string, DropTarget> = new Map()
  private listeners: Set<(state: DragState) => void> = new Set()
  
  /**
   * Register a drop target.
   *
   * @param target Drop target
   */
  registerDropTarget(target: DropTarget): void {
    this.dropTargets.set(target.id, target)
  }
  
  /**
   * Unregister a drop target.
   *
   * @param id Drop target ID
   */
  unregisterDropTarget(id: string): void {
    this.dropTargets.delete(id)
  }
  
  /**
   * Start dragging.
   *
   * @param item Drag item
   */
  startDrag(item: DragItem): void {
    this.dragItem = item
    this.notifyListeners()
  }
  
  /**
   * End dragging.
   */
  endDrag(): void {
    this.dragItem = null
    this.notifyListeners()
  }
  
  /**
   * Handle drop.
   *
   * @param targetId Drop target ID
   * @returns true if drop succeeded
   */
  drop(targetId: string): boolean {
    if (!this.dragItem) {
      return false
    }
    
    const target = this.dropTargets.get(targetId)
    if (!target) {
      return false
    }
    
    // Check if target accepts this item type
    if (!target.accepts.includes(this.dragItem.type)) {
      return false
    }
    
    // Execute drop handler
    target.onDrop(this.dragItem)
    
    // End drag
    this.endDrag()
    
    return true
  }
  
  /**
   * Get current drag state.
   *
   * @returns Drag state
   */
  getState(): DragState {
    return {
      isDragging: this.dragItem !== null,
      dragItem: this.dragItem,
      dropTarget: null,
    }
  }
  
  /**
   * Subscribe to state changes.
   *
   * @param listener Listener function
   * @returns Unsubscribe function
   */
  subscribe(listener: (state: DragState) => void): () => void {
    this.listeners.add(listener)
    
    return () => {
      this.listeners.delete(listener)
    }
  }
  
  /**
   * Notify listeners of state change.
   */
  private notifyListeners(): void {
    const state = this.getState()
    this.listeners.forEach(listener => {
      try {
        listener(state)
      } catch (error) {
        console.error('Error in drag and drop listener:', error)
      }
    })
  }
}

// Create singleton instance
export const dragAndDrop = new DragAndDropManager()

/**
 * Composable for drag and drop.
 */
export function useDragAndDrop() {
  return {
    registerDropTarget: dragAndDrop.registerDropTarget.bind(dragAndDrop),
    unregisterDropTarget: dragAndDrop.unregisterDropTarget.bind(dragAndDrop),
    startDrag: dragAndDrop.startDrag.bind(dragAndDrop),
    endDrag: dragAndDrop.endDrag.bind(dragAndDrop),
    drop: dragAndDrop.drop.bind(dragAndDrop),
    getState: dragAndDrop.getState.bind(dragAndDrop),
    subscribe: dragAndDrop.subscribe.bind(dragAndDrop),
  }
}