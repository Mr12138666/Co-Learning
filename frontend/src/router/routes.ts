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
      {
        path: 'goals',
        name: 'goals',
        component: () => import('@/views/study/GoalsView.vue'),
        meta: { title: '考试目标', requiresAuth: true },
      },
      {
        path: 'subjects',
        name: 'subjects',
        component: () => import('@/views/study/SubjectsView.vue'),
        meta: { title: '科目管理', requiresAuth: true },
      },
      {
        path: 'tasks',
        name: 'tasks',
        component: () => import('@/views/study/TasksView.vue'),
        meta: { title: '任务清单', requiresAuth: true },
      },
      {
        path: 'stats',
        name: 'stats',
        component: () => import('@/views/study/StatsView.vue'),
        meta: { title: '学习统计', requiresAuth: true },
      },
      {
        path: 'checkin',
        name: 'checkin',
        component: () => import('@/views/CheckinView.vue'),
        meta: { title: '每日复盘', requiresAuth: true },
      },
      {
        path: 'journals',
        name: 'journals',
        component: () => import('@/views/JournalView.vue'),
        meta: { title: '学习日志', requiresAuth: true },
      },
      {
        path: 'journals/new',
        name: 'journal-new',
        component: () => import('@/views/JournalEditView.vue'),
        meta: { title: '写日志', requiresAuth: true },
      },
      {
        path: 'journals/:id/edit',
        name: 'journal-edit',
        component: () => import('@/views/JournalEditView.vue'),
        meta: { title: '编辑日志', requiresAuth: true },
      },
      {
        path: 'rooms',
        name: 'rooms',
        component: () => import('@/views/rooms/RoomListView.vue'),
        meta: { title: '陪伴房', requiresAuth: true },
      },
      {
        path: 'rooms/:roomId',
        name: 'room-detail',
        component: () => import('@/views/rooms/RoomDetailView.vue'),
        meta: { title: '房间', requiresAuth: true },
      },
      {
        path: 'leaderboard',
        name: 'leaderboard',
        component: () => import('@/views/gamification/LeaderboardView.vue'),
        meta: { title: '排行榜', requiresAuth: true },
      },
      {
        path: 'pet',
        name: 'pet',
        component: () => import('@/views/gamification/PetView.vue'),
        meta: { title: '宠物', requiresAuth: true },
      },
      {
        path: 'achievements',
        name: 'achievements',
        component: () => import('@/views/gamification/AchievementsView.vue'),
        meta: { title: '成就', requiresAuth: true },
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
