import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

export function setupGuards(router: Router) {
  router.beforeEach((to, _from, next) => {
    // Set page title
    const title = to.meta.title as string
    document.title = title ? `${title} - Co-Learning` : 'Co-Learning 伴学平台'

    const authStore = useAuthStore()

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
      next({ name: 'login', query: { redirect: to.fullPath } })
    } else if ((to.name === 'login' || to.name === 'register') && authStore.isAuthenticated) {
      next({ name: 'dashboard' })
    } else {
      next()
    }
  })
}
