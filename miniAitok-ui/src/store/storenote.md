# Redux Persist Guide

## Installation

```bash
npm install redux-persist
```

## Why Redux Persist?

### Without Redux Persist:
```
User logs in → token stored in Redux
User refreshes page → Redux store resets → token lost → user logged out
```

### With Redux Persist:
```
User logs in → token stored in Redux AND persisted to localStorage
User refreshes page → Redux store rehydrated from localStorage → token preserved → user stays logged in
```

## Core Concepts

### 1. Persistence Flow
```
State Change → Redux Store → Save to Storage
Page Load → Load from Storage → Rehydrate Redux Store
```

## Configuration Options

### TypeScript Configuration

```typescript
const persistConfig = {
  key: 'root',              // Storage key
  storage,                  // Storage engine (localStorage, etc.)
  whitelist: ['user'],      // Only persist these reducers
  blacklist: ['temp'],      // Don't persist these reducers
  transforms: [],           // Transform data before saving/loading
  throttle: 1000,          // Debounce saves (ms)
  debug: false,            // Enable debug logging
};
```

---

## Steps to Create Redux Store with Redux Persist

### Step 1: Install Dependencies

```bash
npm install @reduxjs/toolkit redux-persist
npm install react-redux @types/react-redux  # For React integration
```

### Step 2: Create Individual Slices

Create separate slice files for each piece of state:

```typescript
// src/store/userSlice.ts
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface UserState {
  token: string;
}

const initialState: UserState = { token: '' };

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setToken: (state, action: PayloadAction<string>) => {
      state.token = action.payload;
    },
    removeToken: (state) => {
      state.token = '';
    },
  },
});

export const { setToken, removeToken } = userSlice.actions;
export default userSlice.reducer;
```

### Step 3: Configure Persistence

Create your persistence configuration:

```typescript
import storage from 'redux-persist/lib/storage'; // localStorage
// import sessionStorage from 'redux-persist/lib/storage/session'; // sessionStorage

const persistConfig = {
  key: 'root',           // Storage key
  storage,               // Storage engine
  whitelist: ['user', 'theme'], // Only persist these slices
  // blacklist: ['temp'], // Don't persist these slices
};
```

### Step 4: Combine Reducers

Combine all your reducers:

```typescript
import { combineReducers } from '@reduxjs/toolkit';
import userReducer from './userSlice';
import themeReducer from './themeSlice';

const rootReducer = combineReducers({
  user: userReducer,
  theme: themeReducer,
  // other reducers...
});
```

### Step 5: Create Persisted Reducer

Wrap your root reducer with persistence:

```typescript
import { persistReducer } from 'redux-persist';

const persistedReducer = persistReducer(persistConfig, rootReducer);
```

### Step 6: Configure Store

Create store with proper middleware:

```typescript
import { configureStore } from '@reduxjs/toolkit';

export const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }),
});
```

### Step 7: Create Persistor

Create persistor instance:

```typescript
import { persistStore } from 'redux-persist';

export const persistor = persistStore(store);
```

### Step 8: Define TypeScript Types

Create proper type definitions:

```typescript
// Better: Use rootReducer for clean types
export type RootState = ReturnType<typeof rootReducer>;
export type AppDispatch = typeof store.dispatch;
```

### Step 9: Create Typed Hooks

Create typed hooks for React components:

```typescript
// src/store/hooks.ts
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from './store';

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
```

### Step 10: Integrate with React

Wrap your app with Provider and PersistGate:

```typescript
// src/App.tsx
import React from 'react';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';
import { store, persistor } from './store';
import YourAppComponent from './YourApp';

const App: React.FC = () => (
  <Provider store={store}>
    <PersistGate loading={null} persistor={persistor}>
      <YourAppComponent />
    </PersistGate>
  </Provider>
);

export default App;
```

---

## Complete Example File

Here's complete store setup in one file:

> 📁 `miniAitok-ui/src/store/complete-example.ts` (already created)

---

## Quick Reference Summary

### Essential Steps:
1. **Install**: `@reduxjs/toolkit`, `redux-persist`, `react-redux`
2. **Create Slices**: Individual state management files
3. **Configure Persistence**: Define what to persist and where
4. **Combine Reducers**: Merge all reducers into one
5. **Wrap with Persist**: Create persisted reducer
6. **Configure Store**: Set up store with proper middleware
7. **Create Persistor**: Handle persistence lifecycle
8. **Define Types**: TypeScript type safety
9. **Create Hooks**: Typed React hooks
10. **Wrap App**: Provider + PersistGate integration

### Key Configuration Points:
- **`whitelist`**: Only persist specified slices (recommended)
- **`ignoredActions`**: Prevent Redux Persist warnings
- **`RootState`**: Use `rootReducer` type, not `store.getState()`
- **`PersistGate`**: Prevents UI flash during rehydration

---

## Best Practices

### ✅ Do:
- Use `whitelist` to specify what to persist
- Ignore Redux Persist actions in middleware
- Use `rootReducer` for TypeScript types
- Wrap app with `PersistGate`

### ❌ Don't:
- Persist everything (use `whitelist` instead of `blacklist`)
- Forget to handle loading states
- Use `store.getState()` for types
- Ignore TypeScript warnings

---

## Common Issues & Solutions

### Issue: Serializable Check Warnings
```typescript
// Solution: Ignore persist actions
middleware: (getDefaultMiddleware) =>
  getDefaultMiddleware({
    serializableCheck: {
      ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
    },
  }),
```

### Issue: UI Flash on Load
```typescript
// Solution: Use PersistGate
<PersistGate loading={<div>Loading...</div>} persistor={persistor}>
  <App />
</PersistGate>
```

### Issue: Type Errors with Persisted State
```typescript
// Solution: Use rootReducer type instead of store.getState()
export type RootState = ReturnType<typeof rootReducer>;
```


extraReducers :

Handle actions created elsewhere (external actions)
Common use cases:
✅ Async thunks ( createAsyncThunk ) - most common
✅ Actions from other slices
✅ Third-party library actions
✅ Global/system actions