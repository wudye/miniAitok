import React, { useEffect, useState } from 'react'
import type { JSX } from 'react'
import { Link, useLocation } from 'react-router-dom'
import {
  FiHome,
  FiCompass,
  FiTrendingUp,
  FiUsers,
  FiUser,
  FiMessageSquare,
  FiCornerUpRight,
  FiGithub,
} from 'react-icons/fi'
import { videoCategoryParentList } from '@/api/video'

interface TabItem {
  id: number
  name: string
  class?: string
  link: string
  icon: React.ReactNode
}

interface Category {
  id: number
  name: string
  categoryImage: string
}

export default function Aside({ siteTitle = '芝士学爆' }: { siteTitle?: string }): JSX.Element {
  const location = useLocation()
  const [videoCategoryList, setVideoCategoryList] = useState<Category[]>([])
  const [loading, setLoading] = useState(false)

  const tabsTopList: TabItem[] = [
    { id: 0, name: '首页', link: '/', icon: <FiHome size={24} /> },
    { id: 1, name: '推荐', link: '/discover', icon: <FiCompass size={24} /> },
    { id: 2, name: '热门', link: '/hotVideo', icon: <FiTrendingUp size={24} /> },
    { id: 3, name: '关注', link: '/follow', icon: <FiUsers size={24} /> },
    { id: 5, name: '我的', link: '/user', icon: <FiUser size={24} /> },
  ]

  const tabsBottomList: TabItem[] = [
    { id: 1, name: 'AI', link: '/ai/chat', icon: <FiMessageSquare size={24} /> },
    { id: 2, name: '商务合作', link: '/cooperation', icon: <FiCornerUpRight size={24} /> },
    { id: 3, name: '源码地址', link: '/niuyinGithub', icon: <FiGithub size={24} /> },
  ]

  useEffect(() => {
    initVideoCategoryParentList()
  }, [])

  function initVideoCategoryParentList() {
    const cached = localStorage.getItem('videoCategoryParentList')
    if (cached) {
      setVideoCategoryList(JSON.parse(cached))
    }

    setLoading(true)
    videoCategoryParentList()
      .then((res: any) => {
        if (res?.code === 200) {
          setVideoCategoryList(res.data)
          localStorage.setItem('videoCategoryParentList', JSON.stringify(res.data))
        }
      })
      .catch((err) => console.error('Failed to load categories:', err))
      .finally(() => setLoading(false))
  }

  function isActive(link: string): boolean {
    return location.pathname === link
  }

  return (
    <aside className="w-fit overflow-hidden flex flex-col h-screen">
      {/* Logo with gradient background */}
      <div className="h-20 sticky top-0 z-10 backdrop-blur-xl bg-gradient-to-r from-indigo-600 to-purple-600 dark:from-indigo-700 dark:to-purple-700 border-b border-white/20 flex items-center px-4 shadow-lg">
        <Link to="/" className="flex items-center gap-2 text-lg font-bold text-white no-underline hover:scale-105 transition-transform duration-300 group">
          <div className="w-9 h-9 bg-white/20 backdrop-blur-sm rounded-xl flex items-center justify-center group-hover:rotate-12 transition-transform duration-300">
            <img src="/assets/logo/logo-cheese.png" alt="logo" className="w-6 h-6" />
          </div>
          <span className="hidden sm:inline font-bold text-base drop-shadow-lg" style={{ fontFamily: 'DouyinSansBold' }}>
            {siteTitle}
          </span>
        </Link>
      </div>

      {/* Scrollable tabs area with custom scrollbar - using flexbox to separate top and bottom */}
      <div className="flex-1 overflow-y-auto px-3 py-4 custom-sidebar-scroll flex flex-col">
        {/* Top section with main navigation and categories */}
        <div className="flex-1">
          {/* Top tabs */}
          <ul className="space-y-2 mb-6">
            {tabsTopList.map((item) => (
              <li key={item.id}>
                <Link
                  to={item.link}
                  className={`group flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300 no-underline relative overflow-hidden ${
                    isActive(item.link)
                      ? 'bg-gradient-to-r from-indigo-500 to-purple-500 dark:from-indigo-600 dark:to-purple-600 text-white font-semibold shadow-lg shadow-indigo-500/30 scale-105'
                      : 'text-gray-700 dark:text-gray-300 hover:bg-gradient-to-r hover:from-gray-100 hover:to-gray-50 dark:hover:from-gray-700 dark:hover:to-gray-600 hover:scale-105'
                  }`}
                >
                  {isActive(item.link) && (
                    <div className="absolute inset-0 bg-gradient-to-r from-white/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                  )}
                  <div className={`w-12 h-12 flex items-center justify-center rounded-lg transition-all duration-300 ${
                    isActive(item.link) ? 'bg-white/20' : 'bg-gray-100 dark:bg-gray-700 group-hover:bg-indigo-100 dark:group-hover:bg-indigo-900'
                  }`}>
                    {item.icon}
                  </div>
                  <span className="hidden sm:inline text-base font-semibold relative z-10">{item.name}</span>
                </Link>
              </li>
            ))}
          </ul>

          {/* Divider with gradient */}
          <div className="relative my-4">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-200 dark:border-gray-700" />
            </div>
            <div className="relative flex justify-center">
              <span className="px-2 text-xs text-gray-500 dark:text-gray-400 bg-transparent">分类</span>
            </div>
          </div>

          {/* Video categories */}
          {videoCategoryList && videoCategoryList.length > 0 && (
            <>
              <ul className="space-y-2 mb-6">
                {videoCategoryList.map((item) => (
                  <li key={item.id}>
                    <Link
                      to={`/category/${item.id}`}
                      className={`group flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300 no-underline relative overflow-hidden ${
                        isActive(`/category/${item.id}`)
                          ? 'bg-gradient-to-r from-indigo-500 to-purple-500 dark:from-indigo-600 dark:to-purple-600 text-white font-semibold shadow-lg shadow-indigo-500/30 scale-105'
                          : 'text-gray-700 dark:text-gray-300 hover:bg-gradient-to-r hover:from-gray-100 hover:to-gray-50 dark:hover:from-gray-700 dark:hover:to-gray-600 hover:scale-105'
                      }`}
                    >
                      {isActive(`/category/${item.id}`) && (
                        <div className="absolute inset-0 bg-gradient-to-r from-white/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                      )}
                    <div className={`w-12 h-12 flex items-center justify-center rounded-lg transition-all duration-300 ${
                      isActive(`/category/${item.id}`) ? 'bg-white/20' : 'bg-gray-100 dark:bg-gray-700 group-hover:bg-indigo-100 dark:group-hover:bg-indigo-900'
                    }`}>
                      <img src={item.categoryImage} alt={item.name} className="w-7 h-7 object-contain" />
                    </div>
                    <span className="hidden sm:inline text-base font-semibold relative z-10">{item.name}</span>
                    </Link>
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>

        {/* Bottom section - pushed to the end */}
        <div>
          {/* Divider before bottom tabs */}
          <div className="relative my-4">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-200 dark:border-gray-700" />
            </div>
          </div>

          {/* Bottom tabs */}
          <ul className="space-y-2">
            {tabsBottomList.map((item) => (
              <li key={item.id}>
                <Link
                  to={item.link}
                  className={`group flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300 no-underline relative overflow-hidden ${
                    isActive(item.link)
                      ? 'bg-gradient-to-r from-indigo-500 to-purple-500 dark:from-indigo-600 dark:to-purple-600 text-white font-semibold shadow-lg shadow-indigo-500/30 scale-105'
                      : 'text-gray-700 dark:text-gray-300 hover:bg-gradient-to-r hover:from-gray-100 hover:to-gray-50 dark:hover:from-gray-700 dark:hover:to-gray-600 hover:scale-105'
                  }`}
                >
                  {isActive(item.link) && (
                    <div className="absolute inset-0 bg-gradient-to-r from-white/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                  )}
                  <div className={`w-12 h-12 flex items-center justify-center rounded-lg transition-all duration-300 ${
                    isActive(item.link) ? 'bg-white/20' : 'bg-gray-100 dark:bg-gray-700 group-hover:bg-indigo-100 dark:group-hover:bg-indigo-900'
                  }`}>
                    {item.icon}
                  </div>
                  <span className="hidden sm:inline text-base font-semibold relative z-10">{item.name}</span>
                </Link>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {/* Custom scrollbar styles */}
      <style>{`
        .custom-sidebar-scroll::-webkit-scrollbar {
          width: 6px;
        }
        .custom-sidebar-scroll::-webkit-scrollbar-track {
          background: transparent;
        }
        .custom-sidebar-scroll::-webkit-scrollbar-thumb {
          background: rgba(99, 102, 241, 0.3);
          border-radius: 10px;
          transition: background 0.3s ease;
        }
        .custom-sidebar-scroll::-webkit-scrollbar-thumb:hover {
          background: rgba(99, 102, 241, 0.6);
        }
      `}</style>
    </aside>
  )
}
