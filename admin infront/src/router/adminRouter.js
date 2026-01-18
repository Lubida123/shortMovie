// src/router/adminRouter.js
import { createRouter, createWebHistory } from 'vue-router'
import { setupAdminGuard } from './guard/adminGuard'
// 导入布局组件（确保路径正确）
import AdminLayout from '@/layouts/AdminLayout.vue'

// 管理员路由规则
const adminRoutes = [
  {
    path: '/',
    redirect: '/admin/login'
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue'),
    meta: { title: '管理员登录', noAuth: true }
  },
  // 布局路由：嵌套子路由
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '', // 空路径，访问 /admin 时跳首页
        redirect: 'home'
      },
      {
        path: 'home', // 路由：/admin/home
        name: 'AdminHome',
        component: () => import('@/views/admin/AdminHome.vue'),
        meta: { title: '管理员仪表盘' }
      },
      {
        path: 'user-manage', // 路由：/admin/user-manage
        name: 'AdminUserManage',
        component: () => import('@/views/admin/AdminUserManage.vue'),
        meta: { title: '用户管理', requiredPermission: 'user:manage' }
      },
      {
        path: 'permission-manage', // 路由：/admin/permission-manage
        name: 'AdminPermissionManage',
        component: () => import('@/views/admin/AdminPermissionManage.vue'),
        meta: { title: '权限管理', requiredPermission: 'permission:manage' }
      },
      {
        path: 'video-manage', // 路由：/admin/video-manage
        name: 'AdminVideoManage',
        component: () => import('@/views/admin/AdminVideoManage.vue'),
        meta: { title: '视频管理', requiredPermission: 'video:manage' }
      },

      {
        path: 'data-analysis', // 路由：/admin/data-analysis
        name: 'AdminDataAnalysis',
        component: () => import('@/views/admin/AdminDataAnalysis.vue'),
        meta: { title: '数据分析', requiredPermission: 'data:analysis' }
      }
    ]
  },
  // 404页面
  {
    path: '/admin/:pathMatch(.*)*',
    name: 'Admin404',
    component: () => import('@/views/admin/Admin404.vue'),
    meta: { noAuth: true }
  }
]

// 创建路由实例
const adminRouter = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: adminRoutes,
  scrollBehavior: () => ({ top: 0 })
})

// 注册路由守卫
setupAdminGuard(adminRouter)

export default adminRouter