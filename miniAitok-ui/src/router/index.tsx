import React, { Suspense, lazy, useEffect, useRef, type JSX } from 'react'
import { createBrowserRouter, RouterProvider, Outlet, useLocation } from 'react-router-dom'
import Login from '@/pages/Login'
import Register from '@/pages/Register'
import Index from '@/layout/Index'
// Route change logger — logs previous and current pathname similar to Vue beforeEach
function RouteChangeLogger(): null {
  const location = useLocation()
  const prevRef = useRef<string | null>(null)

  useEffect(() => {
    const from = prevRef.current ?? ''
    const to = location.pathname
    // replicate the console log from the Vue router guard
    // eslint-disable-next-line no-console
    console.log(`${from} - ${to}`)
    prevRef.current = to
  }, [location])

  return null
}


/*
JSX.Element：TypeScript 针对 JSX 产物的返回类型，基本等同于 React.ReactElement<any, any>。在声明函数组件返回值时，通常用它（或直接让 TypeScript 推断）。
React.ReactElement：React 定义的元素类型，带泛型参数 (ReactElement<Props, Type>)，适合需要精确标注元素类型或 props 时使用（比如 React.ReactElement<ButtonProps, typeof Button>）。
实际用法：写组件时返回类型用 JSX.Element 或省略让 TS 推断即可；在手动构造、校验、限制某类元素时，用 React.ReactElement 及其泛型。两者底层形状一致，差异在命名空间和泛型控制
*/
export const AppRouter = (): JSX.Element => {

  // Root layout that renders the RouteChangeLogger and an Outlet for child routes
  const RootLayout = () => (

    <>
      <RouteChangeLogger />
      <Outlet />
    </>
  )


  {/*
    先渲染 RouteChangeLogger（记录路由切换），再渲染 <Outlet />，把后续子路由内容挂载到这里。
当某个路由被声明为 { path: '/', element: <RootLayout />, children: [...] } 时，所有 children 路由（包括 /, /login, /register 等）都会先经过 RootLayout，也就是说会共享这层布局/逻辑。
如果把某些路由（如 /login, /register）定义在根之外，它们就不会渲染 RootLayout（不会触发 RouteChangeLogger，也不会被这一层布局包裹）。如果放在 children 里，则会套在 RootLayout 下，共享同一布局和记录逻辑。
  */
    }

  const router = createBrowserRouter([

    {
      path: '/',
      element: 
      (
        <>
      <RootLayout />
        </>),
      children: [
      { path: '/login', element: <Login /> },
    { path: '/register', element: <Register /> },
        { path: '/', element: <Index /> },
      
        // Add more routes here. Example of protected/nested route:
        /*

 
          <Route path="/" element={<LayoutIndex />}>
            <Route index element={<Video />} />
            <Route path="discover" element={<Discover />} />

            <Route path="user" element={<User />}>
              <Route index element={<Navigate to="videoPost" replace />} />
              <Route path="videoPost" element={<UserVideoPost />} />
              <Route path="videoLike" element={<UserVideoLike />} />
              <Route path="videoFavorite" element={<UserVideoFavorite />} />
              <Route path="videoViewHistory" element={<UserVideoViewHistory />} />
            </Route>

            <Route path="publish" element={<Publish />} />

            <Route path="search" element={<Search />}>
              <Route index element={<Navigate to="video" replace />} />
              <Route path="video" element={<SearchVideo />} />
              <Route path="user" element={<SearchUser />} />
            </Route>

            <Route path="follow" element={<Follow />} />
            <Route path="channel" element={<Channel />} />
            <Route path="hotVideo" element={<HotVideo />} />

            <Route path="person/:userId" element={<Person />}>
              <Route path="videoPost" element={<PersonVideoPost />} />
              <Route path="videoLike" element={<PersonVideoLike />} />
              <Route path="videoFavorite" element={<PersonVideoFavorite />} />
            </Route>

            <Route path="category/:categoryId" element={<CategoryVideo />} />

            <Route path="cooperation" element={<Cooperation />} />
            <Route path="aitokGithub" element={<aitokGithub />} />

            <Route path="ai" element={<AICurrent />}>
              <Route index element={<Navigate to="chat" replace />} />
              <Route path="chat" element={<AICurrent />} />
              <Route path="image" element={<AICImage />} />
            </Route>
          </Route>
        */
        // 404 fallback
        { path: '*', element: <div>404 Not Found</div> },
         
      ],
    },
  ])

  return (
    <Suspense fallback={<div>Loading...</div>}>
      <RouterProvider router={router} />
    </Suspense>
  )
}


export default AppRouter
