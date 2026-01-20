import { defineStore } from 'pinia'
import router from '../router'
import * as userApi from '../api/user'

const STORAGE_KEY = 'shortmovie_user'

const readStorage = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : {}
  } catch (error) {
    return {}
  }
}

const writeStorage = (payload) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
}

const clearStorage = () => {
  localStorage.removeItem(STORAGE_KEY)
}

export const useUserStore = defineStore('user', {
  state: () => {
    const cached = readStorage()
    return {
      token: cached.token || '',
      userInfo: cached.userInfo || null,
    }
  },
  actions: {
    async login(form) {
      try {
        const { data } = await userApi.login(form)
        if (data?.code === 200) {
          this.token = data?.data?.token || ''
          writeStorage({ token: this.token, userInfo: this.userInfo })
          await this.fetchProfile()
          return data?.data || null
        }
      } catch (error) {
        const status = error?.response?.status
        if (status === 401 || status === 403) {
          this.clearAuth()
        }
      }
      return null
    },
    async fetchProfile() {
      if (!this.token) {
        return null
      }
      try {
        const { data } = await userApi.getProfile()
        if (data?.code === 200) {
          this.userInfo = data?.data || null
          writeStorage({ token: this.token, userInfo: this.userInfo })
          return data?.data || null
        }
      } catch (error) {
        const status = error?.response?.status
        if (status === 401 || status === 403) {
          this.clearAuth()
        }
      }
      return null
    },
    setAuth(loginVO) {
      this.token = loginVO?.token || ''
      this.userInfo = loginVO?.userInfo || null
      writeStorage({ token: this.token, userInfo: this.userInfo })
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo || null
      writeStorage({ token: this.token, userInfo: this.userInfo })
    },
    async fetchUserInfo() {
      return this.fetchProfile()
    },
    clearAuth() {
      this.token = ''
      this.userInfo = null
      clearStorage()
    },
    async logout() {
      try {
        await userApi.logout()
      } catch (error) {
        // ignore logout api errors
      } finally {
        this.clearAuth()
        router.push('/login')
      }
      return true
    },
  },
})
