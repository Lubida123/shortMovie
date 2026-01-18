import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Profile from '../views/Profile.vue'
import CreatorProfile from '../views/CreatorProfile.vue'
import { useUserStore } from '../store/userStore'

const routes = [
  { path: '/', redirect: '/login' },
  {
    path: '/home',
    name: 'home',
    component: Home,
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
  },
  {
    path: '/profile',
    name: 'profile',
    component: Profile,
  },
  {
    path: '/user/:userId',
    name: 'creator',
    component: CreatorProfile,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  if (to.path === '/profile') {
    const userStore = useUserStore()
    if (!userStore.token) {
      next('/login')
      return
    }
  }
  next()
})

export default router
