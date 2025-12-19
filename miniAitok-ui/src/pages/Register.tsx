import React, { useState, useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { FiUser, FiLock, FiMail, FiEye, FiEyeOff } from 'react-icons/fi'
import { FaGithub, FaGoogle, FaTwitter } from 'react-icons/fa'

type RegisterForm = {
  username: string
  email: string
  password: string
  confirmPassword: string
}

export default function Register(): React.ReactElement {
  const [form, setForm] = useState<RegisterForm>({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  })
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [loginVisible, setLoginVisible] = useState(true)
  const [redirect, setRedirect] = useState<string | undefined>(undefined)

  const location = useLocation()
  const navigate = useNavigate()

  useEffect(() => {
    const params = new URLSearchParams(location.search)
    const r = params.get('redirect') || undefined
    setRedirect(r || undefined)
  }, [location.search])

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = e.target
    setForm((s) => ({ ...s, [name]: value }))
  }

  async function handleRegister(e?: React.FormEvent) {
    if (e) e.preventDefault()
    // basic validation
    if (!form.username.trim()) return alert('Please enter your username')
    if (!form.email.trim()) return alert('Please enter your email')
    if (!form.password) return alert('Please enter your password')
    if (!form.confirmPassword) return alert('Please confirm your password')
    if (form.password !== form.confirmPassword) return alert('Passwords do not match')
    if (form.password.length < 6) return alert('Password must be at least 6 characters')

    setLoading(true)
    try {
      const resp = await fetch('/api/member/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: form.username,
          password: form.password,
          confirmPassword: form.confirmPassword,
        }),
      })
      const data = await resp.json()
      // expecting { code, msg }
      if (data?.code !== 200) {
        setLoginVisible(false)
        alert(data?.msg || 'Registration failed')
      } else {
        setLoginVisible(true)
        alert(data?.msg || 'Registration successful')
        // redirect to login or provided redirect
        if (redirect) navigate(redirect)
        else navigate('/login')
      }
    } catch (err) {
      // network or other error
      // eslint-disable-next-line no-console
      console.error(err)
      alert('Registration request failed, please try again later')
    } finally {
      setLoading(false)
    }
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter') {
      handleRegister()
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-b from-gray-100 to-gray-50 px-4 py-8 sm:px-6 sm:py-12">
      <div className="w-full max-w-5xl rounded-xl shadow-xl overflow-hidden flex">
        {/* Left promo image */}
         <div className="hidden lg:flex w-1/2 items-center justify-center bg-gradient-to-br from-purple-600 to-pink-500">
          <img src="/assets/images/loginPic.jpg" alt="AITok Learn And Enjoy" className="w-full h-1/2 " />
        </div>

        {/* Right register panel - Ultra Modern Glassmorphism Design */}
        <div className="w-full lg:w-1/2 h-1/2 bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 p-8 sm:p-10 md:p-12 flex items-center justify-center relative overflow-hidden">
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
                <h1 className="text-4xl font-bold text-white mb-3 tracking-tight">Create Account</h1>
                <p className="text-white/70 text-sm font-light">Join our creative community today</p>
              </div>

              {/* Enhanced Form Fields */}
              <form onSubmit={handleRegister} className="space-y-6 mb-8">
                <div className="group relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <FiUser className="h-5 w-5 text-white/40 group-focus-within:text-purple-400 transition-colors duration-300" />
                  </div>
                  <input 
                    name="username" 
                    value={form.username} 
                    onChange={handleChange} 
                    placeholder="Username" 
                    className="w-full pl-12 pr-4 py-4 bg-white/10 border border-white/20 rounded-2xl text-white placeholder-white/40 focus:ring-2 focus:ring-purple-500 focus:border-purple-500 focus:bg-white/15 outline-none transition-all duration-300 backdrop-blur-sm"
                    autoComplete="username"
                  />
                  <div className="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transform scale-x-0 group-focus-within:scale-x-100 transition-transform duration-300"></div>
                </div>

                <div className="group relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <FiMail className="h-5 w-5 text-white/40 group-focus-within:text-purple-400 transition-colors duration-300" />
                  </div>
                  <input 
                    name="email" 
                    value={form.email} 
                    onChange={handleChange} 
                    type="email"
                    placeholder="Email address" 
                    className="w-full pl-12 pr-4 py-4 bg-white/10 border border-white/20 rounded-2xl text-white placeholder-white/40 focus:ring-2 focus:ring-purple-500 focus:border-purple-500 focus:bg-white/15 outline-none transition-all duration-300 backdrop-blur-sm"
                    autoComplete="email"
                  />
                  <div className="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transform scale-x-0 group-focus-within:scale-x-100 transition-transform duration-300"></div>
                </div>

                <div className="group relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <FiLock className="h-5 w-5 text-white/40 group-focus-within:text-purple-400 transition-colors duration-300" />
                  </div>
                  <input 
                    name="password" 
                    value={form.password} 
                    onChange={handleChange} 
                    type={showPassword ? "text" : "password"}
                    placeholder="Create password" 
                    className="w-full pl-12 pr-12 py-4 bg-white/10 border border-white/20 rounded-2xl text-white placeholder-white/40 focus:ring-2 focus:ring-purple-500 focus:border-purple-500 focus:bg-white/15 outline-none transition-all duration-300 backdrop-blur-sm"
                    autoComplete="new-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-4 flex items-center text-white/40 hover:text-white/60 transition-colors"
                  >
                    {showPassword ? <FiEyeOff className="h-5 w-5" /> : <FiEye className="h-5 w-5" />}
                  </button>
                  <div className="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transform scale-x-0 group-focus-within:scale-x-100 transition-transform duration-300"></div>
                </div>

                <div className="group relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <FiLock className="h-5 w-5 text-white/40 group-focus-within:text-purple-400 transition-colors duration-300" />
                  </div>
                  <input 
                    name="confirmPassword" 
                    value={form.confirmPassword} 
                    onChange={handleChange} 
                    type={showConfirmPassword ? "text" : "password"}
                    placeholder="Confirm password" 
                    className="w-full pl-12 pr-12 py-4 bg-white/10 border border-white/20 rounded-2xl text-white placeholder-white/40 focus:ring-2 focus:ring-purple-500 focus:border-purple-500 focus:bg-white/15 outline-none transition-all duration-300 backdrop-blur-sm"
                    autoComplete="new-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute inset-y-0 right-0 pr-4 flex items-center text-white/40 hover:text-white/60 transition-colors"
                  >
                    {showConfirmPassword ? <FiEyeOff className="h-5 w-5" /> : <FiEye className="h-5 w-5" />}
                  </button>
                  <div className="absolute inset-x-0 bottom-0 h-0.5 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transform scale-x-0 group-focus-within:scale-x-100 transition-transform duration-300"></div>
                </div>

                {/* Terms and Conditions */}
                <div className="flex items-start space-x-3">
                  <input 
                    type="checkbox" 
                    className="mt-1 w-4 h-4 bg-white/10 border-white/30 rounded focus:ring-purple-500 focus:ring-2 text-purple-600"
                    required
                  />
                  <p className="text-xs text-white/60 leading-relaxed">
                    I agree to the <a href="#" className="text-purple-400 hover:text-purple-300 transition-colors">Terms of Service</a> and <a href="#" className="text-purple-400 hover:text-purple-300 transition-colors">Privacy Policy</a>
                  </p>
                </div>

                {/* Premium Register Button */}
                <button 
                  type="submit"
                  disabled={loading}
                  className="w-full bg-gradient-to-r from-purple-600 via-pink-600 to-indigo-600 text-white py-4 rounded-2xl font-bold text-lg shadow-2xl hover:shadow-purple-500/25 focus:ring-4 focus:ring-purple-500/50 transition-all duration-300 transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed disabled:transform-none mb-6 relative overflow-hidden group"
                >
                  <span className="relative z-10">{loading ? 'Creating Account...' : 'Create Account'}</span>
                  <div className="absolute inset-0 bg-gradient-to-r from-purple-700 via-pink-700 to-indigo-700 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                </button>
              </form>

              {/* Modern Social Login */}
              <div className="text-center mb-6">
                <div className="relative mb-6">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-white/20"></div>
                  </div>
                  <div className="relative flex justify-center text-sm">
                    <span className="px-4 bg-transparent text-white/60 font-medium">Or sign up with</span>
                  </div>
                </div>
               
              </div>

              {/* Footer Links */}
              <div className="text-center text-sm">
                <p className="text-white/60 font-light">Already have an account? <Link to="/login" className="text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-pink-400 font-semibold hover:from-purple-300 hover:to-pink-300 transition-all">Sign in</Link></p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
