// src/utils/authClient.js
import axios from 'axios';
import Cookies from 'js-cookie';

// Resolve API base once and reuse
// 优先使用 Vite 环境变量 VITE_API_BASE；如果未设置，默认使用 http://localhost:8080
// 之前的回退是 window.location.origin（会导致在 dev server 下走 5173），
// 导致请求被发到 Vite 而非后端。将默认改为 8080 以匹配后端预期。
const API_BASE = (import.meta.env.VITE_API_BASE || 'http://localhost:8008').replace(/\/$/, '');

const API_ORIGIN = new URL(API_BASE).origin;

// 公共实例（无 auth/refresh 拦截器）
export const publicApi = axios.create({ baseURL: API_BASE, withCredentials: true });

// 用 publicApi 发登录请求
//await publicApi.post('/api/auth/login', { username, password });


export const api = axios.create({ baseURL: API_BASE, withCredentials: true });
let accessToken = localStorage.getItem('token') || null;

export function setAccessToken(token) { accessToken = token; }
export function clearAccessToken() { accessToken = null; }

// --- Refresh / rate-limit / backoff configuration ---
const MAX_REFRESH_ATTEMPTS = 3; // consecutive failed refreshes before forcing logout
const MIN_REFRESH_INTERVAL_MS = 500; // minimum ms between refresh requests
const LOCKOUT_BASE_MS = 1000; // base backoff in ms
const LOCKOUT_MAX_MS = 30_000; // max backoff

let lastRefreshTime = 0;
let refreshAttempts = 0;
let refreshLockUntil = 0; // timestamp until which refreshes are blocked

// persistent storage keys (use localStorage so attempts persist across reloads)
const STORAGE_ATTEMPTS = 'auth_refreshAttempts';
const STORAGE_LOCKUNTIL = 'auth_refreshLockUntil';

// initialize from storage if present
try {
  const sa = localStorage.getItem(STORAGE_ATTEMPTS);
  const sl = localStorage.getItem(STORAGE_LOCKUNTIL);
  refreshAttempts = sa ? parseInt(sa, 10) || 0 : 0;
  refreshLockUntil = sl ? parseInt(sl, 10) || 0 : 0;
} catch (e) {
  // localStorage may be unavailable in some environments; ignore
}

/**
 * 将刷新状态（尝试次数和锁定时间）持久化到本地存储
 * @param {number} refreshAttempts - 当前刷新尝试次数
 * @param {number} refreshLockUntil - 锁定截止时间戳
 * @throws {Error} 如果本地存储不可用可能会抛出错误（但会被静默处理）
 */
function persistRefreshState() {
  try {
    localStorage.setItem(STORAGE_ATTEMPTS, String(refreshAttempts));
    localStorage.setItem(STORAGE_LOCKUNTIL, String(refreshLockUntil));
  } catch (e) {
    // ignore storage errors
  }
}

function logoutAndRedirect() {
  // best-effort notify server and clear client state, then redirect to login
  try {
    // fire-and-forget; ignore errors
    api.post('/api/auth/logout').catch(() => {});
  } catch (e) {
    // ignore
  }
  clearAccessToken();
  const loginPath = import.meta.env.VITE_LOGIN_PATH || '/login';
  // use replace so back button doesn't go back to protected page
  window.location.replace(loginPath);
}

function showLoginExpiredPrompt() {
  // Dispatch a custom event so app UI can show a custom modal if desired
  try {
    window.dispatchEvent(new CustomEvent('auth:expired'));
  } catch (e) {
    // ignore
  }

  // Fallback: simple confirm dialog if no app handles the event
  const msg = '登录已过期，需要重新登录。点击确定跳转到登录页。';
  // Use setTimeout to avoid blocking during interceptor flow
  setTimeout(() => {
    const handled = false; // apps can listen to 'auth:expired' and prevent default behaviour if desired
    // If no custom handler redirected the user, show a confirm dialog
    // Note: we cannot detect if a handler already performed redirect, so always show as a fallback
    if (window.confirm(msg)) {
      logoutAndRedirect();
    } else {
      // user chose to stay; apply a longer temporary lockout
      refreshLockUntil = Date.now() + LOCKOUT_MAX_MS;
      persistRefreshState();
    }
  }, 50);
}

// Add CSRF header automatically (Spring uses X-XSRF-TOKEN)
/*
{
  url: '/api/users',
  baseURL: 'https://api.example.com',
  method: 'post',
  headers: { 'Content-Type': 'application/json' },
  data: { name: 'alice' },
  withCredentials: true
}

{data: {…}, status: 200, statusText: 'OK', headers: AxiosHeaders, config: {…}, …}
config
: 
{transitional: {…}, adapter: Array(3), transformRequest: Array(1), transformResponse: Array(1), timeout: 0, …}
data
: 
{code: 200, msg: 'ok', data: {…}}
headers
: 
AxiosHeaders {cache-control: 'no-cache, no-store, max-age=0, must-revalidate', content-type: 'application/json', expires: '0', pragma: 'no-cache'}
request
: 
XMLHttpRequest {onreadystatechange: null, readyState: 4, timeout: 0, withCredentials: true, upload: XMLHttpRequestUpload, …}
status
: 
200
statusText
: 
"OK"
[[Prototype]]
: 
Object
*/
api.interceptors.request.use(cfg => {
  const xsrf = Cookies.get('XSRF-TOKEN');
  if (xsrf) cfg.headers['X-XSRF-TOKEN'] = xsrf;
  console.log('Request config:', cfg);

  // Only add Authorization for requests targeting our API origin.
  // This prevents leaking Authorization to third-party URLs.
  try {
    const target = new URL(cfg.url, cfg.baseURL || API_BASE);

    if (accessToken && target.origin === API_ORIGIN) {
      cfg.headers = cfg.headers || {};
      cfg.headers['Authorization'] = `Bearer ${accessToken}`;
      console.log('Attaching Authorization header to request for', target.href);
    }
  } catch (e) {
    // fallback: if URL parsing fails, do not attach Authorization
  }

  return cfg;
});

// Refresh queue
let isRefreshing = false;
let subscribers = [];

function onRefreshed(newToken) {
  subscribers.forEach(cb => cb(newToken));
  subscribers = [];
}

function addSubscriber(cb) {
  subscribers.push(cb);
}

api.interceptors.response.use(res => res, async err => {
  const { config, response } = err;
  if (!response) return Promise.reject(err);
  if (response.status !== 401) return Promise.reject(err);

  if (config._retry) return Promise.reject(err);
  config._retry = true;
  // If refreshes are temporarily locked due to recent failures, bail out
  const now = Date.now();
  if (refreshLockUntil && now < refreshLockUntil) {
    // force logout if we've hit max attempts and are in lockout
    if (refreshAttempts >= MAX_REFRESH_ATTEMPTS) {
      // If we've reached max attempts, prefer showing UI prompt rather than immediate redirect
      showLoginExpiredPrompt();
      return Promise.reject(new Error('refresh locked out - prompt shown'));
    }
    return Promise.reject(new Error('refresh temporarily blocked'));
  }

  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      addSubscriber(token => {
        if (!token) return reject(new Error('refresh failed'));
        config.headers = config.headers || {};
        config.headers['Authorization'] = `Bearer ${token}`;
        resolve(api.request(config));
      });
    });
  }

  // throttle rapid refresh attempts
  if (now - lastRefreshTime < MIN_REFRESH_INTERVAL_MS) {
    return Promise.reject(new Error('refresh rate limited'));
  }

  isRefreshing = true;
  try {
    lastRefreshTime = now;
    // Call refresh endpoint (refresh cookie sent automatically)
    const r = await api.post('/member/api/v1/refresh');
    console.log('Refresh response:', r);  
    const newAccess = r.data && r.data.accessToken;
    if (!newAccess) throw new Error('no access token returned');
    setAccessToken(newAccess);
    // reset backoff/attempts on success
    refreshAttempts = 0;
    refreshLockUntil = 0;
    onRefreshed(newAccess);
    return api.request(config);
  } catch (e) {
    // increment attempts and apply exponential backoff lockout
    refreshAttempts = Math.min(refreshAttempts + 1, MAX_REFRESH_ATTEMPTS);
    persistRefreshState();
    const backoff = Math.min(LOCKOUT_BASE_MS * Math.pow(2, refreshAttempts - 1), LOCKOUT_MAX_MS);
    refreshLockUntil = Date.now() + backoff;
    persistRefreshState();
    onRefreshed(null);
    clearAccessToken();
    // if we've exhausted attempts, show expired prompt (UI) instead of immediate redirect
    if (refreshAttempts >= MAX_REFRESH_ATTEMPTS) {
      showLoginExpiredPrompt();
      return Promise.reject(new Error('refresh failed, showing expired prompt'));
    }
    return Promise.reject(e);
  } finally {
    isRefreshing = false;
  }
});

// For compatibility: default export is the authenticated API instance
export default api;

