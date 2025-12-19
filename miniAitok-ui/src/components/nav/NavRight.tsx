import React, { useEffect, useState } from 'react'
import type { JSX } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  FiSmartphone,
  FiBell,
  FiUpload,
  FiUser,
  FiSun,
  FiLogOut,
  FiArrowRight,
  FiMessageSquare,
} from 'react-icons/fi'
import type { AppDispatch, RootState } from '@/store/store'
import { useDispatch, useSelector } from 'react-redux'
import {  myLikeCount, myFavoriteCount } from '@/api/behave'
import {myVideoCount} from '@/api/video'
import { noticeCount } from '@/api/notice'
import { getToken, removeToken } from '@/utils/auth'

interface User {
  avatar?: string
  nickName?: string
}

interface UserPostInfo {
  id: number
  icon: React.ReactNode
  num: number
  title: string
  url: string
}

interface NavRightProps {
  user?: User
  onThemeChange?: (dark: boolean) => void
}
export default function NavRight({ user, onThemeChange }: NavRightProps): JSX.Element {
  const dispatch = useDispatch<AppDispatch>()
  const isDark = useSelector((state: RootState) => state.theme.dark)
  const navigate = useNavigate()
  
  const [saveLogin, setSaveLogin] = useState(true)
  const [showUserPopover, setShowUserPopover] = useState(false)
  const [notice, setNotice] = useState<number | undefined>(undefined)
  const [userPostInfo, setUserPostInfo] = useState<UserPostInfo[]>([
    { id: 1, icon: <FiUpload size={20} />, num: 0, title: '我的作品', url: '/user/videoPost' },
    { id: 2, icon: <FiBell size={20} />, num: 0, title: '我的喜欢', url: '/user/videoLike' },
    { id: 3, icon: <FiMessageSquare size={20} />, num: 0, title: '我的收藏', url: '/user/videoFavorite' },
    { id: 4, icon: <FiSmartphone size={20} />, num: 0, title: '观看历史', url: '/user/videoViewHistory' },
  ])

  useEffect(() => {
    initNotice()
  }, [])

  function initNotice() {
    if (getToken()) {
      noticeCount({ receiveFlag: '0' })
        .then((res: any) => {
          if (res?.code === 200) {
            setNotice(res.data)
          }
        })
        .catch((err) => console.error('Failed to load notice count:', err))
    }
  }

  function handlePopoverShow() {
    if (getToken()) {
      myVideoCount()
        .then((res: any) => {
          if (res?.code === 200) {
            setUserPostInfo((prev) =>
              prev.map((item) => (item.id === 1 ? { ...item, num: res.data } : item))
            )
          }
        })
        .catch((err) => console.error('Failed to load video count:', err))

      myLikeCount()
        .then((res: any) => {
          if (res?.code === 200) {
            setUserPostInfo((prev) =>
              prev.map((item) => (item.id === 2 ? { ...item, num: res.data } : item))
            )
          }
        })
        .catch((err) => console.error('Failed to load like count:', err))

      myFavoriteCount()
        .then((res: any) => {
          if (res?.code === 200) {
            setUserPostInfo((prev) =>
              prev.map((item) => (item.id === 3 ? { ...item, num: res.data } : item))
            )
          }
        })
        .catch((err) => console.error('Failed to load favorite count:', err))
    }
  }

  function handleThemeSwitch() {
    const newDark = !isDark
    dispatch({ type: 'theme/setDark', payload: newDark })
    onThemeChange?.(newDark)
    const html = document.documentElement
    if (newDark) {
      html.classList.add('dark')
    } else {
      html.classList.remove('dark')
    }
  }

  function handleLogout() {
    removeToken()
    dispatch({ type: 'token/clearToken' })
    dispatch({ type: 'userInfo/clearUserInfo' })
    navigate('/login')
  }

  function openTargetLink(url: string) {
    window.open(url, '_blank')
  }

  return (
    <div className="flex items-center gap-6">
      {/* Mobile link */}
      <button
        onClick={() => openTargetLink('#')}
        className="flex flex-col items-center gap-1.5 text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-all duration-300 hover:scale-110 group"
        title="移动端"
      >
        <div className="p-2 rounded-lg group-hover:bg-indigo-50 dark:group-hover:bg-indigo-900/30 transition-colors duration-300">
          <FiSmartphone size={24} />
        </div>
        <span className="text-sm font-medium">移动端</span>
      </button>

      {/* Notice */}
      <div className="relative">
        <button className="flex flex-col items-center gap-1.5 text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-all duration-300 hover:scale-110 relative group" title="通知">
          <div className="relative p-2 rounded-lg group-hover:bg-indigo-50 dark:group-hover:bg-indigo-900/30 transition-colors duration-300">
            <FiBell size={24} />
            {notice && notice > 0 && (
              <span className="absolute -top-1 -right-1 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white bg-red-600 rounded-full animate-pulse">
                {notice}
              </span>
            )}
          </div>
          <span className="text-sm font-medium">通知</span>
        </button>
      </div>

      {/* Upload */}
      <button
        onClick={() => openTargetLink('http://43.240.221.8:5273')}
        className="flex flex-col items-center gap-1.5 text-gray-600 dark:text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-all duration-300 hover:scale-110 group"
        title="投稿"
      >
        <div className="p-2 rounded-lg group-hover:bg-indigo-50 dark:group-hover:bg-indigo-900/30 transition-colors duration-300">
          <FiUpload size={24} />
        </div>
        <span className="text-sm font-medium">投稿</span>
      </button>

      {/* User popover */}
      <div className="relative">
        <button
          onClick={() => setShowUserPopover(!showUserPopover)}
          onMouseEnter={() => setShowUserPopover(true)}
          onMouseLeave={() => setShowUserPopover(false)}
          className="flex items-center gap-2 hover:scale-110 transition-all duration-300 group"
          title="用户菜单"
        >
          {user?.avatar ? (
            <img src={user.avatar} alt="avatar" className="w-11 h-11 rounded-full object-cover border-2 border-transparent group-hover:border-indigo-500 transition-all duration-300 shadow-md" />
          ) : (
            <div className="w-11 h-11 bg-gradient-to-br from-gray-300 to-gray-400 dark:from-gray-600 dark:to-gray-700 rounded-full flex items-center justify-center border-2 border-transparent group-hover:border-indigo-500 transition-all duration-300 shadow-md">
              <FiUser size={22} className="text-gray-600 dark:text-gray-300" />
            </div>
          )}
        </button>

        {/* User menu popover with modern design */}
        {showUserPopover && (
          <div
            className="absolute right-0 top-full mt-3 w-[26rem] backdrop-blur-xl bg-white/95 dark:bg-gray-800/95 border border-gray-200/50 dark:border-gray-600/50 rounded-2xl shadow-2xl p-6 z-50 animate-slideDown"
            onMouseEnter={() => {
              setShowUserPopover(true)
              handlePopoverShow()
            }}
            onMouseLeave={() => setShowUserPopover(false)}
          >
            {/* User header with gradient background */}
            <div className="flex items-center justify-between mb-5 p-4 rounded-xl bg-gradient-to-r from-indigo-50 to-purple-50 dark:from-indigo-900/20 dark:to-purple-900/20 border border-indigo-100 dark:border-indigo-800/30">
              <Link
                to="/user/videoPost"
                className="flex items-center gap-2 text-gray-900 dark:text-white font-bold hover:text-indigo-600 dark:hover:text-indigo-400 no-underline group transition-all duration-300 text-base"
              >
                <span>{user?.nickName || 'User'}</span>
                <FiArrowRight size={18} className="group-hover:translate-x-1 transition-transform duration-300" />
              </Link>
              <div className="flex items-center gap-3">
                <span className="text-xs text-gray-600 dark:text-gray-400 font-medium">保存登录</span>
                <button
                  onClick={() => setSaveLogin(!saveLogin)}
                  className={`relative inline-flex h-7 w-12 items-center rounded-full transition-all duration-300 shadow-sm ${
                    saveLogin ? 'bg-gradient-to-r from-green-500 to-emerald-500' : 'bg-gray-300 dark:bg-gray-600'
                  }`}
                >
                  <span
                    className={`inline-block h-5 w-5 transform rounded-full bg-white shadow-md transition-transform duration-300 ${
                      saveLogin ? 'translate-x-6' : 'translate-x-1'
                    }`}
                  />
                </button>
              </div>
            </div>

            {/* User stats with modern card design */}
            <div className="grid grid-cols-4 gap-3 mb-5">
              {userPostInfo.map((item) => (
                <Link
                  key={item.id}
                  to={item.url}
                  className="flex flex-col items-center gap-2 p-4 rounded-xl bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-700/50 dark:to-gray-800/50 hover:from-indigo-50 hover:to-purple-50 dark:hover:from-indigo-900/30 dark:hover:to-purple-900/30 border border-gray-200/50 dark:border-gray-600/30 hover:border-indigo-300 dark:hover:border-indigo-600/50 transition-all duration-300 hover:scale-105 hover:shadow-lg group no-underline"
                >
                  <div className="text-2xl text-gray-600 dark:text-gray-400 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors duration-300">{item.icon}</div>
                  <div className="text-xl font-bold text-gray-900 dark:text-white group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors duration-300">{item.num}</div>
                  <p className="text-xs text-gray-600 dark:text-gray-400 font-medium text-center leading-tight">{item.title}</p>
                </Link>
              ))}
            </div>

            {/* Gradient divider */}
            <div className="relative my-5">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-200 dark:border-gray-700" />
              </div>
              <div className="relative flex justify-center">
                <span className="px-3 bg-white dark:bg-gray-800 text-xs text-gray-500 dark:text-gray-400">更多选项</span>
              </div>
            </div>

            {/* Footer actions with enhanced buttons */}
            <div className="flex items-center justify-between">
              <div className="px-4 py-2 rounded-lg bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 border border-blue-200/50 dark:border-blue-700/30">
                <span className="text-sm text-gray-700 dark:text-gray-300 font-medium">客服</span>
              </div>
              <div className="flex items-center gap-3">
                <button
                  onClick={handleThemeSwitch}
                  className="flex items-center gap-2 px-4 py-2 rounded-xl text-gray-700 dark:text-gray-300 hover:text-indigo-600 dark:hover:text-indigo-400 transition-all duration-300 text-sm font-medium hover:bg-indigo-50 dark:hover:bg-indigo-900/30 hover:scale-105 group"
                >
                  <FiSun size={18} className="group-hover:rotate-45 transition-transform duration-300" />
                  <span>换肤</span>
                </button>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-2 px-4 py-2 rounded-xl text-gray-700 dark:text-gray-300 hover:text-red-600 dark:hover:text-red-400 transition-all duration-300 text-sm font-medium hover:bg-red-50 dark:hover:bg-red-900/30 hover:scale-105 group"
                >
                  <FiLogOut size={18} className="group-hover:translate-x-0.5 transition-transform duration-300" />
                  <span>退出登录</span>
                </button>
              </div>
            </div>
          </div>
        )}
        
        <style>{`
          @keyframes slideDown {
            from {
              opacity: 0;
              transform: translateY(-12px);
            }
            to {
              opacity: 1;
              transform: translateY(0);
            }
          }
          .animate-slideDown {
            animation: slideDown 0.25s ease-out;
          }
        `}</style>
      </div>
    </div>
  )
}
