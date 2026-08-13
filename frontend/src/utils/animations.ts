/**
 * Animation utility functions.
 */

/**
 * Fade in animation.
 *
 * @param element Element to animate
 * @param duration Duration in milliseconds
 * @returns Promise
 */
export function fadeIn(element: HTMLElement, duration: number = 300): Promise<void> {
  return new Promise((resolve) => {
    element.style.opacity = '0'
    element.style.display = ''
    
    const start = performance.now()
    
    function animate(currentTime: number) {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      
      element.style.opacity = String(progress)
      
      if (progress < 1) {
        requestAnimationFrame(animate)
      } else {
        resolve()
      }
    }
    
    requestAnimationFrame(animate)
  })
}

/**
 * Fade out animation.
 *
 * @param element Element to animate
 * @param duration Duration in milliseconds
 * @returns Promise
 */
export function fadeOut(element: HTMLElement, duration: number = 300): Promise<void> {
  return new Promise((resolve) => {
    const start = performance.now()
    const startOpacity = parseFloat(element.style.opacity) || 1
    
    function animate(currentTime: number) {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      
      element.style.opacity = String(startOpacity * (1 - progress))
      
      if (progress < 1) {
        requestAnimationFrame(animate)
      } else {
        element.style.display = 'none'
        resolve()
      }
    }
    
    requestAnimationFrame(animate)
  })
}

/**
 * Slide down animation.
 *
 * @param element Element to animate
 * @param duration Duration in milliseconds
 * @returns Promise
 */
export function slideDown(element: HTMLElement, duration: number = 300): Promise<void> {
  return new Promise((resolve) => {
    element.style.display = ''
    const height = element.scrollHeight
    element.style.height = '0'
    element.style.overflow = 'hidden'
    
    const start = performance.now()
    
    function animate(currentTime: number) {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      
      element.style.height = `${height * progress}px`
      
      if (progress < 1) {
        requestAnimationFrame(animate)
      } else {
        element.style.height = ''
        element.style.overflow = ''
        resolve()
      }
    }
    
    requestAnimationFrame(animate)
  })
}

/**
 * Slide up animation.
 *
 * @param element Element to animate
 * @param duration Duration in milliseconds
 * @returns Promise
 */
export function slideUp(element: HTMLElement, duration: number = 300): Promise<void> {
  return new Promise((resolve) => {
    const height = element.scrollHeight
    element.style.height = `${height}px`
    element.style.overflow = 'hidden'
    
    const start = performance.now()
    
    function animate(currentTime: number) {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      
      element.style.height = `${height * (1 - progress)}px`
      
      if (progress < 1) {
        requestAnimationFrame(animate)
      } else {
        element.style.display = 'none'
        element.style.height = ''
        element.style.overflow = ''
        resolve()
      }
    }
    
    requestAnimationFrame(animate)
  })
}

/**
 * Scale animation.
 *
 * @param element Element to animate
 * @param from Start scale
 * @param to End scale
 * @param duration Duration in milliseconds
 * @returns Promise
 */
export function scale(
  element: HTMLElement,
  from: number = 0,
  to: number = 1,
  duration: number = 300
): Promise<void> {
  return new Promise((resolve) => {
    element.style.transform = `scale(${from})`
    element.style.opacity = String(from)
    
    const start = performance.now()
    
    function animate(currentTime: number) {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      
      const currentScale = from + (to - from) * progress
      element.style.transform = `scale(${currentScale})`
      element.style.opacity = String(currentScale)
      
      if (progress < 1) {
        requestAnimationFrame(animate)
      } else {
        resolve()
      }
    }
    
    requestAnimationFrame(animate)
  })
}

/**
 * Bounce animation.
 *
 * @param element Element to animate
 * @param duration Duration in milliseconds
 * @returns Promise
 */
export function bounce(element: HTMLElement, duration: number = 600): Promise<void> {
  return new Promise((resolve) => {
    const start = performance.now()
    
    function animate(currentTime: number) {
      const elapsed = currentTime - start
      const progress = Math.min(elapsed / duration, 1)
      
      // Bounce easing
      const bounceProgress = progress < 0.5
        ? 4 * progress * progress * progress
        : 1 - Math.pow(-2 * progress + 2, 3) / 2
      
      element.style.transform = `scale(${1 + 0.1 * Math.sin(bounceProgress * Math.PI * 2)})`
      
      if (progress < 1) {
        requestAnimationFrame(animate)
      } else {
        element.style.transform = ''
        resolve()
      }
    }
    
    requestAnimationFrame(animate)
  })
}