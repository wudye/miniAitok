import React, { useEffect, useState } from 'react'
import type { JSX } from 'react'
import { FiArrowUp } from 'react-icons/fi'
import { useDispatch, useSelector } from 'react-redux'
import Aside from '@/components/Aside'
import Header from '@/components/Header'



const SITE_TITLE = 'AITOK_FORM_'
const CACHE_COMPONENTS = ['Video', 'Discover', 'HotVideo', 'Follow', 'User', 'CategoryVideo2']

export default function Index(): JSX.Element {

  const dispatch = useDispatch()
  const accessToken = useSelector((state: any) => state.user.accessToken)
  const refreshToken = useSelector((state: any) => state.user.refreshToken)
  console.log('accessToken:', accessToken)
  const isDark = useSelector((state: any) => state.theme.dark)
  const [showBackTop, setShowBackTop] = useState(false)
  const mainRef = React.useRef<HTMLDivElement>(null)

  useEffect(() => {
    roydonLog()
    initTheme()
  }, [])

  useEffect(() => {
    const handleScroll = () => {
      if (mainRef.current) {
        setShowBackTop(mainRef.current.scrollTop > 100)
      }
    }
    const container = mainRef.current
    if (container) {
      container.addEventListener('scroll', handleScroll)
      return () => container.removeEventListener('scroll', handleScroll)
    }
  }, [])

  function initTheme() {
    const themeClass = isDark ? 'niuyin-dark' : 'niuyin-light'
    document.documentElement.className = themeClass
  }

  function handleThemeChange(dark: boolean) {
    const themeClass = dark ? 'niuyin-dark' : 'niuyin-light'
    document.documentElement.className = themeClass
  }

  function handleBackTop() {
    if (mainRef.current) {
      mainRef.current.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }

  function roydonLog() {
    console.log(
      '%croydon',
      'background-color: gold ; color: pink ;text-shadow: 3px 1px 3px black ; font-weight: bolder ; border-radius: 10px;font-size: 30px ;padding: 10px 100px;'
    )
    console.log(
      '%chttps://github.com/niuyin-server',
      'background-color: pink ; color: white ; font-weight: bold ; border-radius: 6px;padding:5px 10px;font-size: 16px ; font-style: italic ; text-decoration: underline ; font-family: \'american typewriter\' ; text-shadow: 1px 2px 3px black ;'
    )
    console.group('%cniuyin产品列表', 'background-color: #e0005a ; color: #ffffff ; font-weight: bold ; padding: 4px ;')
    console.log('niuyin-web')
    console.log('niuyin-creator')
    console.log('niuyin-android')
    console.groupEnd()
  }

  return (
    <div className={`min-h-screen flex flex-col relative ${isDark ? 'niuyin-dark bg-gradient-to-br from-gray-950 via-slate-900 to-gray-900 text-white' : 'niuyin-light bg-gradient-to-br from-gray-50 via-blue-50 to-indigo-50 text-gray-900'}`}>
      {/* Enhanced background with animated blobs */}
      <div className="fixed inset-0 -z-10 overflow-hidden">
        <div className="absolute inset-0 w-full h-full bg-cover bg-center opacity-30" style={{ backgroundImage: 'var(--global-bg-img)' }} />
        <div className={`absolute top-0 -left-4 w-96 h-96 ${isDark ? 'bg-purple-600/30' : 'bg-blue-400/20'} rounded-full mix-blend-multiply filter blur-3xl animate-blob`} />
        <div className={`absolute top-0 -right-4 w-96 h-96 ${isDark ? 'bg-cyan-600/30' : 'bg-pink-400/20'} rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-2000`} />
        <div className={`absolute -bottom-8 left-20 w-96 h-96 ${isDark ? 'bg-pink-600/30' : 'bg-indigo-400/20'} rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-4000`} />
      </div>

      {/* Layout container */}
      <div className="flex h-screen relative">
        {/* Sidebar with glassmorphism */}
        <div className={`backdrop-blur-xl ${isDark ? 'bg-gray-900/70' : 'bg-white/70'} border-r ${isDark ? 'border-gray-700/50' : 'border-gray-200/50'} shadow-2xl transition-all duration-300`}>
          <Aside siteTitle={SITE_TITLE} />
        </div>

        {/* Main content area */}
        <div className="flex flex-col flex-1 overflow-hidden">
          {/* Header with enhanced glassmorphism and gradient accent */}
          <div className={`backdrop-blur-xl ${isDark ? 'bg-gradient-to-r from-gray-900/80 via-gray-800/80 to-gray-900/80' : 'bg-gradient-to-r from-white/90 via-blue-50/90 to-white/90'} border-b ${isDark ? 'border-gray-700/50' : 'border-gray-200/50'} shadow-2xl transition-all duration-300 relative`}>
            {/* Animated gradient border at bottom */}
            <div className="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 opacity-80 animate-gradient" />
            <Header onThemeChange={handleThemeChange} />
          </div>

          {/* Main scrollable area with enhanced padding */}
          <main
            ref={mainRef}
            className="flex-1 overflow-y-auto overflow-x-hidden custom-scrollbar px-6 py-6"
            style={{ height: 'calc(100vh - 80px)' }}
          >
            {/* Content wrapper with modern card design */}
            <div className={`min-h-full rounded-3xl ${isDark ? 'bg-gray-800/30' : 'bg-white/40'} backdrop-blur-sm border ${isDark ? 'border-gray-700/30' : 'border-gray-200/30'} shadow-xl p-8 transition-all duration-300`}>
              {/* Router view will be rendered here */}
              {/* In React Router context, children components will be rendered via Outlet */}
            </div>

            {/* Enhanced back to top button with pulse effect */}
            {showBackTop && (
              <button
                onClick={handleBackTop}
                className={`fixed bottom-10 right-10 p-5 ${isDark ? 'bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-600 hover:from-indigo-500 hover:via-purple-500 hover:to-pink-500' : 'bg-gradient-to-br from-blue-500 via-indigo-600 to-purple-600 hover:from-blue-400 hover:via-indigo-500 hover:to-purple-500'} text-white rounded-2xl shadow-2xl hover:shadow-indigo-500/50 hover:scale-110 transition-all duration-300 z-50 group backdrop-blur-md border-2 ${isDark ? 'border-indigo-400/30' : 'border-white/60'} animate-pulse-slow`}
                title="回到顶部"
              >
                <FiArrowUp size={28} className="group-hover:-translate-y-1 transition-transform duration-300" />
              </button>
            )}
          </main>
        </div>
      </div>

      {/* Custom scrollbar and animation styles */}
      <style>{`
        .custom-scrollbar::-webkit-scrollbar {
          width: 10px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
          background: ${isDark ? 'rgba(15, 23, 42, 0.3)' : 'rgba(241, 245, 249, 0.3)'};
          border-radius: 10px;
          margin: 8px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: ${isDark ? 'linear-gradient(180deg, rgba(99, 102, 241, 0.6), rgba(139, 92, 246, 0.6))' : 'linear-gradient(180deg, rgba(79, 70, 229, 0.6), rgba(99, 102, 241, 0.6))'};
          border-radius: 10px;
          transition: background 0.3s ease;
          border: 2px solid transparent;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: ${isDark ? 'linear-gradient(180deg, rgba(99, 102, 241, 0.9), rgba(139, 92, 246, 0.9))' : 'linear-gradient(180deg, rgba(79, 70, 229, 0.9), rgba(99, 102, 241, 0.9))'};
        }
        @keyframes blob {
          0%, 100% { transform: translate(0px, 0px) scale(1); }
          33% { transform: translate(30px, -50px) scale(1.1); }
          66% { transform: translate(-20px, 20px) scale(0.9); }
        }
        .animate-blob {
          animation: blob 7s infinite;
        }
        .animation-delay-2000 {
          animation-delay: 2s;
        }
        .animation-delay-4000 {
          animation-delay: 4s;
        }
        @keyframes gradient {
          0%, 100% { background-position: 0% 50%; }
          50% { background-position: 100% 50%; }
        }
        .animate-gradient {
          background-size: 200% 200%;
          animation: gradient 3s ease infinite;
        }
        @keyframes pulse-slow {
          0%, 100% { opacity: 1; }
          50% { opacity: 0.85; }
        }
        .animate-pulse-slow {
          animation: pulse-slow 3s cubic-bezier(0.4, 0, 0.6, 1) infinite;
        }
      `}</style>
    </div>
  )
}
