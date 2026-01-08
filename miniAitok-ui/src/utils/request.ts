// src/utils/request.ts
import axios, { type AxiosInstance, type AxiosResponse, type AxiosError, type InternalAxiosRequestConfig } from 'axios'

// Types
interface RefreshResponse {
  accessToken?: string
  access_token?: string
  token?: string
  'access-token'?: string
  refreshToken?: string
  refresh_token?: string
  refresh?: string
  'refresh-token'?: string
  data?: {
    accessToken?: string
    access_token?: string
    token?: string
    refreshToken?: string
    refresh_token?: string
    refresh?: string
  }
}

interface RefreshRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

type SubscriberCallback = (token: string | null) => void

// API base config
const API_BASE = (import.meta.env.VITE_API_BASE || 'http://localhost:8008').replace(/\/$/, '')

// Public API instance (no auth/refresh interceptors)
export const publicApi: AxiosInstance = axios.create({ baseURL: API_BASE, withCredentials: true })

// Auth API instance (with interceptors)
export const api: AxiosInstance = axios.create({ baseURL: API_BASE, withCredentials: true })

// Token state (localStorage only, no Redux hooks in util files)
let accessToken: string | null = null
let refreshToken: string | null = null

// Initialize tokens from localStorage
function initTokens() {
  try {
    accessToken = localStorage.getItem('accesstoken')
    refreshToken = localStorage.getItem('refreshtoken')
  } catch {}
}
initTokens()

// Token management functions
export function setAccessToken(token: string | null): void {
  accessToken = token
  try {
    if (token) {
      localStorage.setItem('accesstoken', token)
    } else {
      localStorage.removeItem('accesstoken')
    }
  } catch {}
}

export function clearAccessToken(): void {
  accessToken = null
  try {
    localStorage.removeItem('accesstoken')
  } catch {}
}

export function setRefreshToken(token: string | null): void {
  refreshToken = token
  try {
    if (token) {
      localStorage.setItem('refreshtoken', token)
    } else {
      localStorage.removeItem('refreshtoken')
    }
  } catch {}
}

export function clearRefreshToken(): void {
  refreshToken = null
  try {
    localStorage.removeItem('refreshtoken')
  } catch {}
}

// --- Refresh / rate-limit / backoff configuration ---
const MAX_REFRESH_ATTEMPTS = 3
const MIN_REFRESH_INTERVAL_MS = 500
const LOCKOUT_BASE_MS = 1000
const LOCKOUT_MAX_MS = 30000

let lastRefreshTime = 0
let refreshAttempts = 0
let refreshLockUntil = 0

const STORAGE_ATTEMPTS = 'auth_refreshAttempts'
const STORAGE_LOCKUNTIL = 'auth_refreshLockUntil'

try {
  const sa = localStorage.getItem(STORAGE_ATTEMPTS)
  const sl = localStorage.getItem(STORAGE_LOCKUNTIL)
  refreshAttempts = sa ? parseInt(sa, 10) || 0 : 0
  refreshLockUntil = sl ? parseInt(sl, 10) || 0 : 0
} catch {}

function persistRefreshState(): void {
  try {
    localStorage.setItem(STORAGE_ATTEMPTS, String(refreshAttempts))
    localStorage.setItem(STORAGE_LOCKUNTIL, String(refreshLockUntil))
  } catch {}
}

function logoutAndRedirect(): void {
  try {
    api.post('/api/auth/logout').catch(() => {})
  } catch {}
  clearAccessToken()
  const loginPath = import.meta.env.VITE_LOGIN_PATH || '/login'
  window.location.replace(loginPath)
}

function showLoginExpiredPrompt(): void {
  try {
    window.dispatchEvent(new CustomEvent('auth:expired'))
  } catch {}

  const msg = '登录已过期，需要重新登录。点击确定跳转到登录页。'
  setTimeout(() => {
    if (window.confirm(msg)) {
      logoutAndRedirect()
    } else {
      refreshLockUntil = Date.now() + LOCKOUT_MAX_MS
      persistRefreshState()
    }
  }, 50)
}

// Request interceptor
api.interceptors.request.use((cfg: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
  if (!cfg || !accessToken) return cfg
  cfg.headers = cfg.headers || {}
  cfg.headers['Authorization'] = `Bearer ${accessToken}`
  return cfg
})

// Refresh queue
let isRefreshing = false
let subscribers: SubscriberCallback[] = []

function onRefreshed(newToken: string | null): void {
  subscribers.forEach(cb => cb(newToken))
  subscribers = []
}

function addSubscriber(cb: SubscriberCallback): void {
  subscribers.push(cb)
}

// Response interceptor
api.interceptors.response.use(
  (res: AxiosResponse): AxiosResponse => res,
  async (err: AxiosError): Promise<AxiosResponse> => {
    const { config, response } = err

    if (!config || !response) return Promise.reject(err)
    if (response.status !== 401) return Promise.reject(err)

    const requestConfig = config as RefreshRequestConfig

    if (requestConfig._retry) return Promise.reject(err)
    requestConfig._retry = true

    const now = Date.now()
    if (refreshLockUntil && now < refreshLockUntil) {
      if (refreshAttempts >= MAX_REFRESH_ATTEMPTS) {
        showLoginExpiredPrompt()
        return Promise.reject(new Error('refresh locked out - prompt shown'))
      }
      return Promise.reject(new Error('refresh temporarily blocked'))
    }

    if (isRefreshing) {
      return new Promise<AxiosResponse>((resolve, reject) => {
        addSubscriber((token: string | null) => {
          if (!token) return reject(new Error('refresh failed'))
          requestConfig.headers = requestConfig.headers || {}
          requestConfig.headers['Authorization'] = `Bearer ${token}`
          resolve(api.request(requestConfig))
        })
      })
    }

    if (now - lastRefreshTime < MIN_REFRESH_INTERVAL_MS) {
      return Promise.reject(new Error('refresh rate limited'))
    }

    isRefreshing = true
    try {
      lastRefreshTime = now
      const currentRefresh = refreshToken || (() => {
        try {
          return localStorage.getItem('refreshtoken')
        } catch {
          return null
        }
      })()
      if (!currentRefresh) throw new Error('no refresh token available')

      const r = await api.post<RefreshResponse>('/member/api/v1/refresh', { refreshToken: currentRefresh })
      const resp = r && r.data ? r.data : {}
      const newAccess = resp.accessToken || resp.access_token || resp.token || resp['access-token'] || (resp.data && (resp.data.accessToken || resp.data.access_token || resp.data.token))
      const newRefresh = resp.refreshToken || resp.refresh_token || resp.refresh || resp['refresh-token'] || (resp.data && (resp.data.refreshToken || resp.data.refresh_token || resp.data.refresh))
      if (!newAccess) throw new Error('no access token returned')
      setAccessToken(newAccess)
      if (newRefresh) setRefreshToken(newRefresh)
      refreshAttempts = 0
      refreshLockUntil = 0
      onRefreshed(newAccess)
      return api.request(requestConfig)
    } catch (e) {
      refreshAttempts = Math.min(refreshAttempts + 1, MAX_REFRESH_ATTEMPTS)
      persistRefreshState()
      const backoff = Math.min(LOCKOUT_BASE_MS * Math.pow(2, refreshAttempts - 1), LOCKOUT_MAX_MS)
      refreshLockUntil = Date.now() + backoff
      persistRefreshState()
      onRefreshed(null)
      clearAccessToken()
      if (refreshAttempts >= MAX_REFRESH_ATTEMPTS) {
        showLoginExpiredPrompt()
        return Promise.reject(new Error('refresh failed, showing expired prompt'))
      }
      return Promise.reject(e)
    } finally {
      isRefreshing = false
    }
  }
)

// For compatibility: default export is the authenticated API instance
export default api
