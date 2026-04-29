import RecordPage from '@/pages/RecordPage.vue'
import HistoryPage from '@/pages/HistoryPage.vue'
import ProfilePage from '@/pages/ProfilePage.vue'
import { createRouter, createWebHistory } from 'vue-router'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/record',
    },
    {
      path: '/record',
      name: '记录',
      component: RecordPage,
    },
    {
      path: '/history',
      name: '历史',
      component: HistoryPage,
    },
    {
      path: '/profile',
      name: '我的',
      component: ProfilePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
  ],
})

export default router
