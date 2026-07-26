import 'vue-router'

/**
 * Strongly-typed route meta. Augments vue-router so `route.meta.title` /
 * `requiresAuth` / `requiresAdmin` are checked instead of `unknown`.
 */
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    requiresAdmin?: boolean
  }
}

export {}
