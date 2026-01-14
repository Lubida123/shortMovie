import { defineStore } from 'pinia'

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

export const useUserStore = defineStore('user', {
  state: () => {
    const cached = readStorage()
    return {
      token: cached.token || '',
      userInfo: cached.userInfo || null,
    }
  },
  actions: {
    setAuth(loginVO) {
      this.token = loginVO?.token || ''
      this.userInfo = loginVO?.userInfo || null
      writeStorage({ token: this.token, userInfo: this.userInfo })
    },
    clearAuth() {
      this.token = ''
      this.userInfo = null
      writeStorage({ token: '', userInfo: null })
    },
  },
})
