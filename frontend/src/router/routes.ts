import type { RouteRecordRaw } from 'vue-router'

const BlankLayout = () => import('@/layouts/BlankLayout.vue')
const DefaultLayout = () => import('@/layouts/DefaultLayout.vue')

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: DefaultLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '首页', requiresAuth: true },
      },
    ],
  },
  {
    path: '/auth',
    component: BlankLayout,
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import('@/views/auth/LoginView.vue'),
        meta: { title: '登录' },
      },
      {
        path: 'register',
        name: 'register',
        component: () => import('@/views/auth/RegisterView.vue'),
        meta: { title: '注册' },
      },
      {
        path: 'verify-email',
        name: 'verify-email',
        component: () => import('@/views/auth/VerifyEmailView.vue'),
        meta: { title: '邮箱验证' },
      },
      {
        path: 'forgot-password',
        name: 'forgot-password',
        component: () => import('@/views/auth/ForgotPasswordView.vue'),
        meta: { title: '忘记密码' },
      },
      {
        path: 'reset-password',
        name: 'reset-password',
        component: () => import('@/views/auth/ResetPasswordView.vue'),
        meta: { title: '重置密码' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '404' },
  },
]
