import React, { useEffect, useState } from 'react'
import type { JSX } from 'react'
import { FiSearch, FiBell, FiSettings, FiSun, FiMoon, FiUser } from 'react-icons/fi'
import { useAppDispatch } from '@/store/hooks'
import { useAppSelector } from '@/store/hooks'
import { getInfo } from '@/api/member'
import { getToken } from '@/utils/auth'
import NavCenter from '@/components/nav/NavCenter'
import NavRight from '@/components/nav/NavRight'

interface HeaderProps {
  onThemeChange?: (dark: boolean) => void
}

export default function Header({ onThemeChange }: HeaderProps): JSX.Element {
  const dispatch = useAppDispatch()
  const user = useAppSelector((state) => state.userInfo.user)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    getUserInfo()
  }, [])

  function getUserInfo() {
    if (getToken()) {
      if (!user) {
        setLoading(true)
        getInfo()
          .then((res: any) => {
            if (res?.code === 200) {
              dispatch({ type: 'userInfo/setUserInfo', payload: res.data })
            }
          })
          .catch((err) => console.error('Failed to load user info:', err))
          .finally(() => setLoading(false))
      }
    }
  }

  function handleThemeChange(dark: boolean) {
    onThemeChange?.(dark)
  }

  return (
    <header className="h-20 backdrop-blur-xl bg-white/80 dark:bg-gray-900/80 border-b border-gray-200/50 dark:border-gray-700/50 flex items-center px-6 shadow-lg relative">
      {/* Gradient accent line */}
      <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 opacity-70" />
      
      <div className="flex-1" />
      <NavCenter />
      <NavRight user={user} onThemeChange={handleThemeChange} />
    </header>
  )
}
