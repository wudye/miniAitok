
import React, { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { FiUser, FiLock, FiPhone, FiKey } from 'react-icons/fi'
import { FaGithub, FaGoogle, FaTwitter } from 'react-icons/fa'
import { setToken as setAuthToken } from '@/utils/auth'
import { login } from '@/api/member'
import api, { setAccessToken, setRefreshToken } from '@/utils/request'
import { useDispatch } from 'react-redux'
import { setToken } from '@/store/useUserSlice'

type LoginForm = {
  username: string
  password: string
  telephone: string
  smsCode: string
}

export default function LoginNew(): React.ReactElement {
  const [loginType, setLoginType] = useState<'up' | 'sms'>('up')
  const [loginForm, setLoginForm] = useState<LoginForm>({ username: '', password: '', telephone: '', smsCode: '' })
  const [loading, setLoading] = useState(false)
  const [mobileCodeTimer, setMobileCodeTimer] = useState<number>(0)
  const timerRef = useRef<number | null>(null)
  const navigate = useNavigate()

  const [remember, setRemember] = useState<boolean>(false);

  const dispatch = useDispatch();

  useEffect(() => {

    try {
    const saved = localStorage.getItem('remember_username');
    if (saved) {
      setLoginForm((s) => ({ ...s, username: saved }));
      setRemember(true);
    }
  } catch (e) {}
    return () => {
      if (timerRef.current) window.clearInterval(timerRef.current)
    }
  }, [])

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = e.target
    setLoginForm((s) => ({ ...s, [name]: value }))
  }

  function validatePhone(phone: string) {
    return /^(?:(?:\+|00)86)?1(?:3[\d]|4[5-79]|5[0-35-9]|6[5-7]|7[0-8]|8[\d]|9[189])\d{8}$/.test(phone)
  }


  {
  /*
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
  */}




  async function handleLogin(e?: React.FormEvent) {
    // if called from form submit, prevent default navigation
    try { e?.preventDefault() } catch (err) {}
    // client-side simple validation
    if (loginType === 'up') {
      if (!loginForm.username.trim()) return alert('Please enter your account')
      if (!loginForm.password) return alert('Please enter your password')
    } else {
      if (!loginForm.telephone) return alert('Please enter your phone number')
      if (!validatePhone(loginForm.telephone)) return alert('Invalid phone number format')
      if (!loginForm.smsCode) return alert('Please enter SMS verification code')
    }

    // set loading locally
    // (setLoading is available but unused by lint until referenced here)
    // @ts-ignore
    setLoading(true)
    try {
      if (loginType === 'up') {
        console.log('Logging in with username/password:', api.defaults.baseURL, loginForm.username);

        const res = await login(loginForm.username, loginForm.password);
        console.log('Login response data:', res.data);
        const body = res.data;
        if (body.code === 200) {
          // Read tokens from common response shapes and persist to localStorage
          const payload = body.data || {};
          const access = payload.accessToken || payload.access_token || payload.token || payload['access-token'];
          const refresh = payload.refreshToken || payload.refresh_token || payload.refresh || payload['refresh-token'];

          if (access) {
            try { dispatch(setToken({ accessToken: access, refreshToken: refresh })); } catch (e) {}
            try { setAuthToken(access); } catch (e) { try { localStorage.setItem('accesstoken', access) } catch (ee) {} }
          } else {
            console.warn('Login response did not include an access token:', body)
          }

          if (refresh) {
            try { setRefreshToken(refresh); } catch (e) {}
          }

          try {
            if (remember) {
              localStorage.setItem('remember_username', loginForm.username);
            } else {
              localStorage.removeItem('remember_username');
            }
          } catch (e) {}

          alert(body.msg || 'Login successful')
          navigate('/')
        } else {
          alert(body.msg || 'Login failed')
        }
      } else {
        console.log('Logging in with SMS:', loginForm.telephone);
        const resp = await fetch('/api/member/sms-login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ telephone: loginForm.telephone, smsCode: loginForm.smsCode }),
        })
        const data = await resp.json()
        if (data?.code === 200) {
          // Read tokens from SMS-login response shape
          const payload = data.data || {};
          const access = payload.accessToken || payload.access_token || payload.token || payload['access-token'];
          const refresh = payload.refreshToken || payload.refresh_token || payload.refresh || payload['refresh-token'];

          if (access) {
            try{ dispatch(setToken({ accessToken: access, refreshToken: refresh })); } catch (e) {}
          
            try { setAccessToken(access); } catch (e) {}
            try { setAuthToken(access) } catch (e) { try { localStorage.setItem('accesstoken', access) } catch (ee) {} }
          } else {
            console.warn('SMS login response did not include an access token:', data)
          }

          if (refresh) {
            try { setRefreshToken(refresh); } catch (e) {}
          }

          try {
            if (remember) {
              localStorage.setItem('remember_username', loginForm.username);
            } else {
              localStorage.removeItem('remember_username');
            }
          } catch (e) {}

          // Log readable (non-HttpOnly) cookies for debugging.
          // Note: HttpOnly cookies (e.g. refreshToken) will NOT appear here.
          try {
            const allCookies = document.cookie || '';
            console.log('Readable cookies after SMS login:', allCookies);
            const xsrf = allCookies.split('; ').find((c) => c.startsWith('XSRF-TOKEN='));
            console.log('XSRF-TOKEN (readable):', xsrf ? decodeURIComponent(xsrf.split('=')[1]) : 'not found or HttpOnly');
          } catch (e) {
            console.warn('Could not read document.cookie in this context', e);
          }

          alert(data.msg || 'Login successful')
          navigate('/')
        } else {
          alert(data.msg || 'Login failed')
        }
      }
    } catch (err) {
      // eslint-disable-next-line no-console
      console.error(err)
      alert('Request failed, please try again later')
    } finally {
      // @ts-ignore
      setLoading(false)
    }
  }

  function startMobileTimer() {
    setMobileCodeTimer(60)
    timerRef.current = window.setInterval(() => {
      setMobileCodeTimer((t) => {
        if (t <= 1) {
          if (timerRef.current) window.clearInterval(timerRef.current)
          timerRef.current = null
          return 0
        }
        return t - 1
      })
    }, 1000)
  }

  function getSmsCode() {
    if (mobileCodeTimer > 0) return
    if (!loginForm.telephone) return alert('Please enter your phone number')
    if (!validatePhone(loginForm.telephone)) return alert('Invalid phone number format')

    // simulate API call to send SMS code
    alert('Verification code sent successfully')
    startMobileTimer()
  }

  return (
    <div className="min-h-screen  flex items-center justify-center bg-gradient-to-b from-gray-100 to-gray-50 px-4 py-8 sm:px-6 sm:py-12">
      <div className="w-full max-w-6xl rounded-xl shadow-xl overflow-hidden flex">
        {/* Left promo image */}
        <div className="hidden lg:flex w-1/2 items-center justify-center bg-gradient-to-br from-purple-600 to-pink-500">
          <img src="/assets/images/loginPic1.jpg" alt="AITok Learn And Enjoy" className="w-full " />
        </div>

        {/* Right login panel - Ultra Modern Glassmorphism Design */}
        <div className="w-full lg:w-1/2 bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 p-8 sm:p-10 md:p-12 flex items-center justify-center relative overflow-hidden">
          {/* Animated background elements */}
          <div className="absolute inset-0">
            <div className="absolute top-0 right-0 w-96 h-96 bg-gradient-to-br from-purple-600 to-pink-600 rounded-full blur-3xl opacity-20 animate-pulse"></div>
            <div className="absolute bottom-0 left-0 w-80 h-80 bg-gradient-to-tr from-blue-600 to-cyan-600 rounded-full blur-3xl opacity-20 animate-pulse delay-1000"></div>
            <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-gradient-to-r from-indigo-600 to-purple-600 rounded-full blur-3xl opacity-10 animate-pulse delay-500"></div>
          </div>
          
          {/* Glass morphism card */}
          <div className="w-full max-w-md relative z-10">
            <div className="bg-white/10 backdrop-blur-xl rounded-3xl p-8 border border-white/20 shadow-2xl">
              {/* Logo/Brand Section */}
              <div className="text-center mb-10">
                <div className="inline-flex items-center justify-center w-30 h-20 bg-gradient-to-br from-purple-500 via-pink-500 to-indigo-600 rounded-3xl shadow-2xl mb-6 transform hover:rotate-6 transition-transform duration-300">
                  <span className="text-white text-3xl font-bold">AITok</span>
                </div>
                <h1 className="text-4xl font-bold text-white mb-3 tracking-tight">Welcome Back</h1>
                <p className="text-white/70 text-sm font-light">Step into your creative universe</p>
              </div>

              {/* Modern Tab Navigation */}
              <div className="mb-10">
                <div className="flex bg-white/5 rounded-2xl p-1 backdrop-blur-sm border border-white/10">
                  <button 
                    onClick={() => setLoginType('up')} 
                    className={`flex-1 py-3 px-6 rounded-xl text-sm font-semibold transition-all duration-300 ${
                      loginType === 'up' 
                        ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-lg transform scale-105' 
                        : 'text-white/60 hover:text-white hover:bg-white/5'
                    }`}
                  >
                    Account Login
                  </button>
                  <button 
                    onClick={() => setLoginType('sms')} 
                    className={`flex-1 py-3 px-6 rounded-xl text-sm font-semibold transition-all duration-300 ${
                      loginType === 'sms' 
                        ? 'bg-gradient-to-r from-purple-600 to-pink-600 text-white shadow-lg transform scale-105' 
                        : 'text-white/60 hover:text-white hover:bg-white/5'
                    }`}
                  >
                    Quick Login
                  </button>
                </div>
              </div>

              {/* Enhanced Form Fields */}
              <form onSubmit={handleLogin} className="mb-8">
                <div className="space-y-6 mb-4">
                {loginType === 'up' ? (
                  <>
                    <div className="group relative">
                      <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                        <FiUser className="h-5 w-5 text-white/40 group-focus-within:text-purple-400 transition-colors duration-300" />
                      </div>
                      <input 
                        name="username" 
                        value={loginForm.username} 
                        onChange={handleChange} 
                        placeholder="Email or username" 
                        autoComplete="username"
                        className="w-full pl-12 pr-4 py-4 bg-white/10 border border-white/20 rounded-2xl text-white placeholder-white/40 focus:ring-2 focus:ring-purple-500 focus:border-purple-500 focus:bg-white/15 outline-none transition-all duration-300 backdrop-blur-sm"
                      />
                      <div className="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transform scale-x-0 group-focus-within:scale-x-100 transition-transform duration-300"></div>
                    </div>
                    <div className="group relative">
                      <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                        <FiLock className="h-5 w-5 text-white/40 group-focus-within:text-purple-400 transition-colors duration-300" />
                      </div>
                      <input 
                        name="password" 
                        value={loginForm.password} 
                        onChange={handleChange} 
                        type="password" 
                        placeholder="Enter your password" 
                        autoComplete="current-password"
                        className="w-full pl-12 pr-4 py-4 bg-white/10 border border-white/20 rounded-2xl text-white placeholder-white/40 focus:ring-2 focus:ring-purple-500 focus:border-purple-500 focus:bg-white/15 outline-none transition-all duration-300 backdrop-blur-sm"
                      />
                      <div className="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transform scale-x-0 group-focus-within:scale-x-100 transition-transform duration-300"></div>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="group relative">
                      <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                        <FiPhone className="h-5 w-5 text-white/40 group-focus-within:text-purple-400 transition-colors duration-300" />
                      </div>
                      <input 
                        name="telephone" 
                        value={loginForm.telephone} 
                        onChange={handleChange} 
                        placeholder="Mobile number" 
                        autoComplete="tel"
                        className="w-full pl-12 pr-4 py-4 bg-white/10 border border-white/20 rounded-2xl text-white placeholder-white/40 focus:ring-2 focus:ring-purple-500 focus:border-purple-500 focus:bg-white/15 outline-none transition-all duration-300 backdrop-blur-sm"
                      />
                      <div className="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transform scale-x-0 group-focus-within:scale-x-100 transition-transform duration-300"></div>
                    </div>
                    <div className="group relative">
                      <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                        <FiKey className="h-5 w-5 text-white/40 group-focus-within:text-purple-400 transition-colors duration-300" />
                      </div>
                      <input 
                        name="smsCode" 
                        value={loginForm.smsCode} 
                        onChange={handleChange} 
                        placeholder="Verification code" 
                        autoComplete="one-time-code"
                        className="w-full pl-12 pr-28 py-4 bg-white/10 border border-white/20 rounded-2xl text-white placeholder-white/40 focus:ring-2 focus:ring-purple-500 focus:border-purple-500 focus:bg-white/15 outline-none transition-all duration-300 backdrop-blur-sm"
                      />
                      <button 
                        type="button"
                        onClick={getSmsCode}
                        disabled={mobileCodeTimer > 0}
                        className={`absolute right-2 top-1/2 transform -translate-y-1/2 px-4 py-2 text-xs font-semibold rounded-xl transition-all duration-300 backdrop-blur-sm ${
                          mobileCodeTimer > 0 
                            ? 'bg-white/10 text-white/40 cursor-not-allowed border border-white/10' 
                            : 'bg-gradient-to-r from-purple-600 to-pink-600 text-white hover:from-purple-700 hover:to-pink-700 shadow-lg hover:shadow-xl transform hover:scale-105'
                        }`}
                      >
                        {mobileCodeTimer > 0 ? `${mobileCodeTimer}s` : 'Send Code'}
                      </button>
                      <div className="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transform scale-x-0 group-focus-within:scale-x-100 transition-transform duration-300"></div>
                    </div>
                  </>
                )}
              </div>

                {/* Remember Me & Forgot Password */}
                <div className="flex items-center justify-between mt-6 mb-6">
                  <label htmlFor="remember" className="flex items-center text-sm text-white/70 cursor-pointer hover:text-white/90 transition-colors">
                    <input
                      id="remember"
                      type="checkbox"
                      checked={remember}
                      onChange={(e) => setRemember(e.target.checked)}
                      className="mr-2 w-4 h-4 bg-white/10 border-white/30 rounded focus:ring-purple-500 focus:ring-2 accent-purple-600"
                      aria-label="remember my login information"
                    />
                    remember me
                  </label>
                  <a href="#" className="text-sm text-purple-400 hover:text-purple-300 transition-colors">Forgot password?</a>
                </div>

                {/* Premium Login Button */}
                <button 
                  type="submit"
                  disabled={loading}
                  className="w-full bg-gradient-to-r from-purple-600 via-pink-600 to-indigo-600 text-white py-4 rounded-2xl font-bold text-lg shadow-2xl hover:shadow-purple-500/25 focus:ring-4 focus:ring-purple-500/50 transition-all duration-300 transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed disabled:transform-none mb-8 relative overflow-hidden group"
                >
                  <span className="relative z-10">{loading ? 'Authenticating...' : 'Sign In Securely'}</span>
                  <div className="absolute inset-0 bg-gradient-to-r from-purple-700 via-pink-700 to-indigo-700 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                </button>
              </form>

              {/* Modern Social Login */}
              <div className="text-center mb-8">
                <div className="relative mb-6">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-white/20"></div>
                  </div>
                  <div className="relative flex justify-center text-sm">
                    <span className="px-4 bg-transparent text-white/60 font-medium">Or continue with</span>
                  </div>
                </div>
                <div className="flex justify-center space-x-3">
                  <a href="/api/oauth/github" aria-label="Sign in with GitHub" className="w-14 h-14 bg-white/10 backdrop-blur-sm border border-white/20 rounded-2xl flex items-center justify-center hover:bg-white/20 hover:border-white/30 transition-all duration-300 hover:scale-110 hover:shadow-xl group">
                    <FaGithub className="text-gray-300 text-xl group-hover:text-white transition-colors" />
                  </a>
                  <a href="/api/oauth/google" aria-label="Sign in with Google" className="w-14 h-14 bg-white/10 backdrop-blur-sm border border-white/20 rounded-2xl flex items-center justify-center hover:bg-white/20 hover:border-white/30 transition-all duration-300 hover:scale-110 hover:shadow-xl group">
                    <FaGoogle className="text-red-400 text-xl group-hover:text-red-300 transition-colors" />
                  </a>
                  <a href="/api/oauth/twitter" aria-label="Sign in with Twitter" className="w-14 h-14 bg-white/10 backdrop-blur-sm border border-white/20 rounded-2xl flex items-center justify-center hover:bg-white/20 hover:border-white/30 transition-all duration-300 hover:scale-110 hover:shadow-xl group">
                    <FaTwitter className="text-gray-300 text-xl group-hover:text-white transition-colors" />
                  </a>
                </div>
              </div>

              {/* Footer Links */}
              <div className="text-center text-sm">
                <p className="text-white/60 font-light">New to our platform? <a href="/register" className="text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-pink-400 font-semibold hover:from-purple-300 hover:to-pink-300 transition-all">Create account</a></p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
