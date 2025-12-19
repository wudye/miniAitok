import React, { useEffect, useState, useRef } from 'react'
import type { JSX } from 'react'
import { useNavigate } from 'react-router-dom'
import { FiSearch, FiX } from 'react-icons/fi'
import { searchHistoryLoad, delSearchHistory, searchHotLoad } from '@/api/search'

interface SearchHistory {
  id: number
  keyword: string
}

interface SearchItem {
  id: number
  keyword: string
}

export default function NavCenter(): JSX.Element {
  const navigate = useNavigate()
  const [searchData, setSearchData] = useState('')
  const [searchHistory, setSearchHistory] = useState<SearchHistory[]>([])
  const [searchDiscover, setSearchDiscover] = useState<SearchItem[]>([])
  const [hotSearch, setHotSearch] = useState<string[]>([])
  const [showPopover, setShowPopover] = useState(false)
  const popoverRef = useRef<HTMLDivElement>(null)

  const searchDefaults = '输入你感兴趣的内容'

  useEffect(() => {
    getSearchHistory()
    getHotSearch()
    getSearchDiscover()
  }, [])

  function getSearchHistory() {
    searchHistoryLoad()
      .then((res: any) => {
        if (res?.code === 200 && res.data?.length > 0) {
          setSearchHistory(res.data)
        }
      })
      .catch((err) => console.error('Failed to load search history:', err))
  }

  function getSearchDiscover() {
    setSearchDiscover([
      { id: 1, keyword: '你好' },
      { id: 2, keyword: '搞笑' },
      { id: 3, keyword: '美食' },
      { id: 4, keyword: '音乐' },
      { id: 5, keyword: '游戏' },
      { id: 6, keyword: '学习' },
      { id: 7, keyword: '娱乐' },
      { id: 8, keyword: '知识' },
    ])
  }

  function getHotSearch() {
    searchHotLoad({ pageNum: 1, pageSize: 10 })
      .then((res: any) => {
        if (res?.data?.length > 0) {
          setHotSearch(res.data)
        }
      })
      .catch((err) => console.error('Failed to load hot search:', err))
  }

  function handleSearchHistorySelect(keyword: string) {
    setSearchData(keyword)
    routerJump(keyword)
  }

  function handleSearchHotSelect(keyword: string) {
    setSearchData(keyword)
    routerJump(keyword)
  }

  function handleSearchDiscoverSelect(keyword: string) {
    setSearchData(keyword)
    routerJump(keyword)
  }

  function handleSearchHistoryClose(id: number) {
    delSearchHistory(id)
      .then((res: any) => {
        if (res?.code === 200) {
          getSearchHistory()
        }
      })
      .catch((err) => console.error('Failed to delete search history:', err))
  }

  function searchConfirm() {
    const query = searchData || searchDefaults
    setSearchData(query)
    routerJump(query)
    getSearchHistory()
  }

  function routerJump(keyword: string) {
    navigate(`/search/video?keyword=${encodeURIComponent(keyword)}`)
    setShowPopover(false)
  }

  return (
    <div className="flex-1 max-w-2xl px-4" ref={popoverRef}>
      <div
        className="relative"
        onMouseEnter={() => setShowPopover(true)}
        onMouseLeave={() => setShowPopover(false)}
      >
        {/* Search input */}
        <div className="flex items-center border-2 border-gray-300 dark:border-gray-600 rounded-xl overflow-hidden bg-gray-50 dark:bg-gray-700 hover:border-indigo-400 dark:hover:border-indigo-500 transition-all duration-300 shadow-sm hover:shadow-md">
          <input
            type="text"
            value={searchData}
            onChange={(e) => setSearchData(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') searchConfirm()
            }}
            placeholder={searchDefaults}
            className="flex-1 px-5 py-3 bg-transparent text-gray-900 dark:text-white outline-none text-base font-medium placeholder:text-gray-400"
          />
          <button
            onClick={searchConfirm}
            className="px-5 py-3 text-gray-600 dark:text-gray-300 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors duration-300 hover:scale-110"
          >
            <FiSearch size={22} />
          </button>
        </div>

        {/* Search popover with modern glassmorphism design */}
        {showPopover && (
          <div className="absolute top-full left-0 right-0 mt-3 backdrop-blur-xl bg-white/95 dark:bg-gray-800/95 border border-gray-200/50 dark:border-gray-600/50 rounded-2xl shadow-2xl p-6 w-[28rem] z-50 animate-fadeIn">
            {/* Search history */}
            {searchHistory.length > 0 && (
              <div className="mb-5">
                <div className="flex items-center gap-2 mb-3">
                  <div className="w-1 h-4 bg-gradient-to-b from-indigo-500 to-purple-500 rounded-full" />
                  <h5 className="text-sm font-bold text-gray-900 dark:text-white">搜索历史</h5>
                </div>
                <div className="flex flex-wrap gap-2">
                  {searchHistory.map((item) => (
                    <div
                      key={item.id}
                      className="group inline-flex items-center gap-2 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/30 dark:to-indigo-900/30 text-blue-700 dark:text-blue-300 px-4 py-2 rounded-xl text-sm cursor-pointer hover:from-blue-100 hover:to-indigo-100 dark:hover:from-blue-800/40 dark:hover:to-indigo-800/40 transition-all duration-300 hover:scale-105 border border-blue-200/50 dark:border-blue-700/30 shadow-sm"
                    >
                      <span onClick={() => handleSearchHistorySelect(item.keyword)} className="font-medium">{item.keyword}</span>
                      <button
                        onClick={() => handleSearchHistoryClose(item.id)}
                        className="ml-1 hover:text-blue-900 dark:hover:text-blue-100 transition-colors p-0.5 rounded-full hover:bg-blue-200/50 dark:hover:bg-blue-700/50"
                      >
                        <FiX size={16} />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Search discover */}
            {searchDiscover.length > 0 && (
              <div className="mb-5">
                <div className="flex items-center gap-2 mb-3">
                  <div className="w-1 h-4 bg-gradient-to-b from-purple-500 to-pink-500 rounded-full" />
                  <h5 className="text-sm font-bold text-gray-900 dark:text-white">猜你想搜</h5>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  {searchDiscover.map((item) => (
                    <div
                      key={item.id}
                      onClick={() => handleSearchDiscoverSelect(item.keyword)}
                      className="px-4 py-2.5 text-sm text-gray-700 dark:text-gray-300 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 truncate bg-gray-50 dark:bg-gray-700/50 rounded-xl hover:bg-gradient-to-r hover:from-indigo-50 hover:to-purple-50 dark:hover:from-indigo-900/20 dark:hover:to-purple-900/20 transition-all duration-300 hover:scale-105 border border-transparent hover:border-indigo-200 dark:hover:border-indigo-700/30 font-medium"
                    >
                      {item.keyword}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Hot search */}
            {hotSearch.length > 0 && (
              <div>
                <div className="flex items-center gap-2 mb-3">
                  <div className="w-1 h-4 bg-gradient-to-b from-orange-500 to-red-500 rounded-full" />
                  <h5 className="text-sm font-bold text-gray-900 dark:text-white">芝士热搜</h5>
                </div>
                <div className="space-y-1.5">
                  {hotSearch.map((item, index) => (
                    <div
                      key={index}
                      onClick={() => handleSearchHotSelect(item)}
                      className="flex items-center gap-3 px-3 py-2.5 text-sm text-gray-700 dark:text-gray-300 cursor-pointer hover:text-indigo-600 dark:hover:text-indigo-400 truncate rounded-xl hover:bg-gradient-to-r hover:from-gray-50 hover:to-indigo-50 dark:hover:from-gray-700/50 dark:hover:to-indigo-900/20 transition-all duration-300 hover:scale-102 group"
                    >
                      <div className="flex-shrink-0 w-6 h-6 flex items-center justify-center">
                        {index === 0 && <img src="/assets/images/rank/r1.png" alt="1" className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" />}
                        {index === 1 && <img src="/assets/images/rank/r2.png" alt="2" className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" />}
                        {index === 2 && <img src="/assets/images/rank/r3.png" alt="3" className="w-5 h-5 group-hover:scale-110 transition-transform duration-300" />}
                        {index > 2 && <span className="text-sm font-bold text-gray-400 dark:text-gray-500 group-hover:text-indigo-500 transition-colors">{index + 1}</span>}
                      </div>
                      <span className="truncate font-medium">{item}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
        
        <style>{`
          @keyframes fadeIn {
            from {
              opacity: 0;
              transform: translateY(-8px);
            }
            to {
              opacity: 1;
              transform: translateY(0);
            }
          }
          .animate-fadeIn {
            animation: fadeIn 0.2s ease-out;
          }
          .hover\\:scale-102:hover {
            transform: scale(1.02);
          }
        `}</style>
      </div>
    </div>
  )
}
