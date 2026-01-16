import { isAdminLogin, hasAdminPermission } from '@/utils/adminAuth'
import { ElMessage } from 'element-plus'

/**
 * 管理员路由守卫（仅处理管理员路由）
 * @param {import('vue-router').Router} adminRouter 管理员路由实例
 */
export const setupAdminGuard = (adminRouter) => {
  adminRouter.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title || '后台管理系统'

    // 校验登录状态
    if (to.meta.requiresAuth && !isAdminLogin()) {
      ElMessage.warning('请先登录管理员账号')
      next({ path: '/admin/login', query: { redirect: to.fullPath } })
    } else if (to.path === '/admin/login' && isAdminLogin()) {
      // 已登录则跳首页
      next(to.query.redirect || '/admin/home')
    } else if (to.meta.requiresAuth && to.meta.requiredPermission) {
      // 校验权限（适配新的admin角色逻辑）
      if (!hasAdminPermission(to.meta.requiredPermission)) {
        ElMessage.error('您没有该页面的访问权限')
        next({ path: '/admin/home' })
      } else {
        next()
      }
    } else {
      next()
    }
  })
}