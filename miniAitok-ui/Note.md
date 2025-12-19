npm install react react-dom react-router-dom
npm install -D typescript @types/react @types/react-dom
npm install @reduxjs/toolkit react-redux
npm install -D @types/react-redux
npm install redux-persist
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
npm install -D sass

npm install viewerjs react-masonry-css react-markdown remark-gfm

google map
npm install @googlemaps/js-api-loader
# 可选：安装 types 来获得 TypeScript 的 google.maps 类型
npm install -D @types/google.maps




js-cookie 是一个轻量的 JavaScript 库，用来在浏览器上读写 cookie。它封装了 cookie 的序列化/反序列化、过期、路径、域、secure、sameSite 等选项，使得设置和读取 cookie 比直接操作 document.cookie 更方便和可靠。
核心功能与常用 API

安装：
npm: npm install js-cookie
TypeScript 类型（可选）：npm install -D @types/js-cookie
常用方法（示例）：
import Cookies from 'js-cookie';

// 设置 cookie（value 会自动序列化为字符串）
Cookies.set('token', 'abc123', { expires: 3 /* 天 */, path: '/', secure: true, sameSite: 'Lax' });

// 读取 cookie
const t = Cookies.get('token'); // 返回 string | undefined

// 删除 cookie
Cookies.remove('token', { path: '/' });

当需要在前端读写 cookie：js-cookie 很方便（浏览器端）。
当你想完全避免前端可读 token，使用服务器端 HttpOnly cookie + CSRF token（或 SameSite 策略）。
若你要做 cookie 解析/序列化在服务器端：使用 cookie 或 cookie-parser（Node 库）。
js-cookie 方便，但如果 token 是高敏感凭证，优先考虑由后端设置 HttpOnly cookie；若必须放在前端 cookie，设置安全选项：Cookies.set(TokenKey, token, { expires: 3, path: '/', secure: true, sameSite: 'Lax' })。



JSX.Element：TypeScript 针对 JSX 产物的返回类型，基本等同于 React.ReactElement<any, any>。在声明函数组件返回值时，通常用它（或直接让 TypeScript 推断）。
React.ReactElement：React 定义的元素类型，带泛型参数 (ReactElement<Props, Type>)，适合需要精确标注元素类型或 props 时使用（比如 React.ReactElement<ButtonProps, typeof Button>）。
实际用法：写组件时返回类型用 JSX.Element 或省略让 TS 推断即可；在手动构造、校验、限制某类元素时，用 React.ReactElement 及其泛型。两者底层形状一致，差异在命名空间和泛型控制。


两者都很流行，但定位不同：

受众与生态：Next.js（基于 React）在“生产级全栈/SSR/SEO”场景更主流，社区和招聘面更大；Vite 是通用前端构建工具（多框架），在“纯前端/SPA 开发体验”里极流行。
功能侧重点：Next 内置路由、SSR/SSG、API 路由、Edge/Server Actions、图片优化、国际化等“站点级”能力；Vite 主要是极快的开发服务器和打包工具，框架层功能（路由、数据获取、SSR 等）需自行选库或配套（如 React Router、TanStack、Vite SSR 方案）。
体验与性能：Vite 开发时冷/热启动极快；Next 近版本（基于 Turbopack/Next 14/15）也在提升，但传统 Webpack 路径下启动/构建略慢。
迁移/可组合性：想保持框架自由或已有路由/状态方案，Vite 更轻；想“一站式约束+最佳实践”，Next 更合适。
部署：Next 有现成托管模式（Vercel 等），SSR/Edge 友好；Vite SPA 可静态托管最简单，SSR 需要自己搭配方案。
学习/团队协作：团队成员偏“React 全栈/SEO/内容站/复杂路由” → 选 Next；偏“前端单页/快速原型/组件库/多框架” → 选 Vite。
热度趋势：GitHub stars/npm 下载都很高；Next 在 Web 应用和招聘市场更“默认”，Vite 在工具/体验上口碑极佳。
简单决策：

做内容站、SEO、SSR/SSG、电商、博客、文档、需要内置路由/数据获取 → 选 Next。
做纯前端 SPA、组件库、后台管理、需要最快开发体验/打包灵活 → 选 Vite