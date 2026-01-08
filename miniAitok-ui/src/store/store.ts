// 简短说明：配置 Redux store 并使用 redux-persist 持久化指定的 slice（user、userInfo、token、theme）。
// 使用 RTK 的 `getDefaultMiddleware` 注入 thunk extraArgument，并追加自定义 logger 中间件。
import { configureStore, combineReducers } from "@reduxjs/toolkit";
import { persistStore, persistReducer } from 'redux-persist';
import storage from 'redux-persist/lib/storage';
import userReducer from './useUserSlice';
import userInfoReducer from './userInfoSlice';
import tokenReducer from './tokenSlice';
import themeReducer from './themeSlice';
import { apiClient } from './apiClient';    
import userTestReducer from './userSliceWithExtraReducers';
// (不再需要导入 `PersistedState`，因为 RootState 直接由未包裹的 rootReducer 推导)

/*
  中文说明（整体）：

  这段代码将 `redux-persist` 和 Redux Toolkit (RTK) 结合起来使用，目的是把重要的 state slice 持久化到浏览器存储
 （例如 `localStorage`），使用户在刷新页面或重启应用后仍能恢复这些数据。主要步骤如下：

  1. persistConfig：定义持久化配置。`key: 'root'` 表示在 storage 中使用的顶级键名，`storage` 指定具体存储引擎，
    `whitelist` 列出需要持久化的 slice 名称（只有在白名单中的 slice 会被写入和恢复）。

  2. rootReducer：使用 `combineReducers` 把多个 slice reducer 组合成根 reducer，键名会成为 store state 的顶层字段。

  3. persistedReducer：通过 `persistReducer(persistConfig, rootReducer)` 包裹根 reducer，返回的 reducer 会在内部
    处理持久化相关的 action（例如 rehydrate、purge 等）。

  4. configureStore：把 `persistedReducer` 传入 `configureStore` 创建 store。中间件使用 `getDefaultMiddleware` 并对
    `serializableCheck` 做了调整，忽略 redux-persist 在持久化生命周期中派发的某些 action，以避免 RTK 的可序列化检查
    发出不必要的警告或错误。

  5. persistor：`persistStore(store)` 返回一个 persistor 对象，用来控制 rehydration、flush、purge 等持久化生命周期操作，
    在 React 中常配合 `PersistGate` 使用，确保在恢复完持久化数据前不会渲染依赖这些数据的 UI。

  注意事项：
  - 白名单（`whitelist`）需要维护：新增或重命名 slice 时要同步更新配置。
  - 被持久化的数据应保持可序列化（避免函数、类实例、DOM 节点等），否则会在恢复时出现问题。
  - 若需要改变持久化结构（字段名、类型等），应考虑数据版本与迁移策略（redux-persist 支持 `migrate` 配置）。
  - 有时更推荐直接从 `rootReducer`（未包裹的）推导 `RootState` 类型，以避免把持久化内部元数据混入应用类型。
*/

const persistConfig = {
  key: 'root',
  storage,
  whitelist: ['user', 'userInfo', 'token', 'theme'], // Persist user, userInfo, token, and theme slices
};

const rootReducer = combineReducers({
  user: userReducer,
  userInfo: userInfoReducer,
  token: tokenReducer,
  theme: themeReducer,
  userTest: userTestReducer,
});

const persistedReducer = persistReducer(persistConfig, rootReducer);

// --- 自定义中间件与 thunk extraArgument 示例 ---
// 定义一个简单的 API 客户端接口（示例），并把具体实现注入到 thunk 的 extraArgument
// 这样在 `createAsyncThunk` 中可以通过 thunkAPI.extra 使用相同的类型，获得类型提示和校验。


// 简单 logger middleware：记录 action 与前后 state，演示中间件写法
import type { Middleware } from 'redux';
const loggerMiddleware: Middleware = (storeAPI) => (next) => (action: any) => {
  // 简单日志：打印 action 类型、前置 state、后置 state
  // 注意：为简单起见这里不区分开发/生产环境；如果需要可用构建时替换或环境变量控制。
  console.group?.(`action: ${action?.type}`);
  console.log('prev state', storeAPI.getState());
  console.log('action', action);

  const result = next(action);

  console.log('next state', storeAPI.getState());
  console.groupEnd?.();

  return result;
};

export const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      // 把 apiClient 注入到 thunk 的 extraArgument 中
      thunk: { extraArgument: apiClient },
      // 忽略 redux-persist 的生命周期 action，避免 RTK 的可序列化检查误报
      serializableCheck: {
        ignoredActions: [
          'persist/PERSIST',
          'persist/REHYDRATE',
          'persist/FLUSH',
          'persist/PAUSE',
          'persist/PURGE',
          'persist/REGISTER',
        ],
      },
    }).concat(loggerMiddleware),
});
export const persistor = persistStore(store);


export type RootState = ReturnType<typeof rootReducer>;
export type AppDispatch = typeof store.dispatch;

/*

. With Reducers and Middleware

import { configureStore, ConfigureStoreOptions } from "@reduxjs/toolkit";
import userReducer from "./slices/userSlice";
import postsReducer from "./slices/postsSlice";

const options: ConfigureStoreOptions = {
  reducer: {
    user: userReducer,
    posts: postsReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: false, // Disable serializable check
      immutableCheck: false,    // Disable immutable check
    }).concat(customMiddleware), // Add custom middleware
  devTools: process.env.NODE_ENV !== 'production', // Enable dev tools in development
  preloadedState: {
    user: { name: "Guest", isLoggedIn: false }
  }
};

export const store = configureStore(options);


3. Advanced Configuration
import { configureStore, ConfigureStoreOptions } from "@reduxjs/toolkit";
import { persistStore, persistReducer } from "redux-persist";

const options: ConfigureStoreOptions = {
  reducer: {
    user: persistReducer(persistConfig, userReducer),
    posts: postsReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      thunk: {
        extraArgument: api, // Pass extra argument to thunks
      },
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }),
  devTools: {
    name: 'My App Store',
    trace: true, // Enable stack traces
    traceLimit: 25,
  },
  enhancers: [customEnhancer],
  serialize: {
    ignoredActions: ['SOME_ACTION'],
    ignoredPaths: ['some.nested.path'],
  }
};

export const store = configureStore(options);
export const persistor = persistStore(store);

import type { PersistedState } from 'redux-persist';

export type RootState = ReturnType<typeof store.getState> & PersistedState;
export type AppDispatch = typeof store.dispatch;
*/