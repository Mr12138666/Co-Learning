/**
 * State machine utility.
 */

export interface State {
  name: string
  onEnter?: () => void
  onExit?: () => void
}

export interface Transition {
  from: string
  to: string
  guard?: () => boolean
  action?: () => void
}

export interface StateMachineConfig {
  initial: string
  states: State[]
  transitions: Transition[]
}

class StateMachine {
  private currentState: string
  private states: Map<string, State> = new Map()
  private transitions: Map<string, Transition[]> = new Map()
  private listeners: Map<string, Set<() => void>> = new Map()
  
  constructor(config: StateMachineConfig) {
    this.currentState = config.initial
    
    // Register states
    for (const state of config.states) {
      this.states.set(state.name, state)
    }
    
    // Register transitions
    for (const transition of config.transitions) {
      if (!this.transitions.has(transition.from)) {
        this.transitions.set(transition.from, [])
      }
      this.transitions.get(transition.from)!.push(transition)
    }
  }
  
  /**
   * Get current state.
   *
   * @returns Current state name
   */
  getCurrentState(): string {
    return this.currentState
  }
  
  /**
   * Check if transition is valid.
   *
   * @param to Target state
   * @returns true if transition is valid
   */
  canTransition(to: string): boolean {
    const transitions = this.transitions.get(this.currentState) || []
    return transitions.some(t => t.to === to)
  }
  
  /**
   * Transition to new state.
   *
   * @param to Target state
   * @returns true if transition succeeded
   */
  transition(to: string): boolean {
    const transitions = this.transitions.get(this.currentState) || []
    const transition = transitions.find(t => t.to === to)
    
    if (!transition) {
      console.warn(`Invalid transition from ${this.currentState} to ${to}`)
      return false
    }
    
    // Check guard
    if (transition.guard && !transition.guard()) {
      console.warn(`Guard failed for transition from ${this.currentState} to ${to}`)
      return false
    }
    
    // Execute exit action
    const currentStateObj = this.states.get(this.currentState)
    if (currentStateObj?.onExit) {
      currentStateObj.onExit()
    }
    
    // Execute transition action
    if (transition.action) {
      transition.action()
    }
    
    // Update state
    const previousState = this.currentState
    this.currentState = to
    
    // Execute enter action
    const newStateObj = this.states.get(to)
    if (newStateObj?.onEnter) {
      newStateObj.onEnter()
    }
    
    // Notify listeners
    this.notifyListeners(previousState, to)
    
    return true
  }
  
  /**
   * Get available transitions from current state.
   *
   * @returns Array of available target states
   */
  getAvailableTransitions(): string[] {
    const transitions = this.transitions.get(this.currentState) || []
    return transitions.map(t => t.to)
  }
  
  /**
   * Subscribe to state changes.
   *
   * @param state State to watch
   * @param callback Callback function
   * @returns Unsubscribe function
   */
  on(state: string, callback: () => void): () => void {
    if (!this.listeners.has(state)) {
      this.listeners.set(state, new Set())
    }
    
    this.listeners.get(state)!.add(callback)
    
    return () => {
      this.off(state, callback)
    }
  }
  
  /**
   * Unsubscribe from state changes.
   *
   * @param state State to watch
   * @param callback Callback function
   */
  off(state: string, callback: () => void): void {
    const listeners = this.listeners.get(state)
    if (listeners) {
      listeners.delete(callback)
    }
  }
  
  /**
   * Notify listeners of state change.
   *
   * @param from Previous state
   * @param to New state
   */
  private notifyListeners(from: string, to: string): void {
    const listeners = this.listeners.get(to)
    if (listeners) {
      listeners.forEach(callback => {
        try {
          callback()
        } catch (error) {
          console.error(`Error in state listener for ${to}:`, error)
        }
      })
    }
  }
  
  /**
   * Reset to initial state.
   */
  reset(): void {
    this.currentState = this.states.keys().next().value || ''
  }
  
  /**
   * Get all states.
   *
   * @returns Array of state names
   */
  getStates(): string[] {
    return Array.from(this.states.keys())
  }
  
  /**
   * Check if in specific state.
   *
   * @param state State to check
   * @returns true if in state
   */
  is(state: string): boolean {
    return this.currentState === state
  }
}

/**
 * Create a state machine.
 *
 * @param config State machine configuration
 * @returns State machine instance
 */
export function createStateMachine(config: StateMachineConfig): StateMachine {
  return new StateMachine(config)
}