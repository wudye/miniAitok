import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { Provider } from 'react-redux'
import { persistor, store } from './store/store.ts'
import { PersistGate } from 'redux-persist/lib/integration/react'


/*
Viewer.js 的样式表就是库自带的一份 CSS 文件（在你的项目里是 viewerjs/dist/viewer.css），它定义了图片查看器的所有视觉与交互动效。
 Viewer.js 的样式表引入到应用中，确保使用 Viewer.js（图片预览器）时其组件/DOM 按作者设计呈现正确的外观和交互样式。
打包行为：在 Vite/webpack 等现代打包器里从 JS/TS 导入 CSS，会被构建流程处理——要么注入到页面（development），要么按配置抽取成单独的 CSS 文件（production）。
作用域：这是全局样式（不是 CSS module），会影响整个页面上匹配的选择器，可能与项目其他样式发生冲突。
*/

import 'viewerjs/dist/viewer.css'



 /*
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeHighlight from 'rehype-highlight'
 */

createRoot(document.getElementById('root')!).render(
  <Provider store={store}>
    <PersistGate loading={null} persistor={persistor}>

     <App />
    </PersistGate>
  </Provider>,
)


