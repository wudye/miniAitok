import { createSlice, createAsyncThunk, type PayloadAction } from '@reduxjs/toolkit';
import { apiClient } from './apiClient';

// Define the async thunk for user login
export const loginUser = createAsyncThunk(
  'user/loginUser',
  async (credentials: { username: string; password: string }, { rejectWithValue }) => {
    try {
      // Use the apiClient for API call
      const response = await apiClient.post('/auth/login', credentials);
      
      // Set auth token in apiClient for future requests
      if (response.token) {
        apiClient.setAuth(response.token);
      }
      
      return response.token; // This becomes the action payload
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Login failed');
    }
  }
);

// Define the async thunk for fetching user profile
export const fetchUserProfile = createAsyncThunk(
  'user/fetchProfile',
  async (_, { rejectWithValue }) => {
    try {
      const response = await apiClient.get('/user/profile');
      return response;
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Failed to fetch profile');
    }
  }
);

interface UserState {
  token: string;
  isLoading: boolean;
  error: string | null;
  profile: {
    id: string;
    username: string;
    email: string;
  } | null;
}

const initialState: UserState = {
  token: '',
  isLoading: false,
  error: null,
  profile: null,
};

const userSliceTest = createSlice({
  name: 'userTest',
  initialState,
  reducers: {
    // Local actions (synchronous)
    setToken: (state, action: PayloadAction<string>) => {
      state.token = action.payload;
      // Set auth token in apiClient
      apiClient.setAuth(action.payload);
    },
    removeToken: (state) => {
      state.token = '';
      state.profile = null;
      // Clear auth from apiClient
      apiClient.clearAuth();
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    /* Handle actions created elsewhere (external actions)
Common use cases:
✅ Async thunks ( createAsyncThunk ) - most common
✅ Actions from other slices
✅ Third-party library actions
✅ Global/system action
    */
    builder
    /*
    he Three Lifecycle States:
pending (line 84): Request started

Set isLoading: true
Clear previous errors
Show loading spinner
fulfilled (line 88): Request succeeded

Set isLoading: false
Update state with response data
Clear errors
rejected (line 93): Request failed

Set isLoading: false
Store error message
Keep previous data
Call dispatch(loginUser(...)) → once
Auto dispatches pending → UI shows loading
API call completes → either:
fulfilled → UI updates with data
rejected → UI shows error
    */
      .addCase(loginUser.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action: PayloadAction<string>) => {
        state.isLoading = false;
        state.token = action.payload;
        state.error = null;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });

    // Handle fetchUserProfile async thunk
    builder
      .addCase(fetchUserProfile.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchUserProfile.fulfilled, (state, action) => {
        state.isLoading = false;
        state.profile = action.payload;
        state.error = null;
      })
      .addCase(fetchUserProfile.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const { setToken, removeToken, clearError } = userSliceTest.actions;
export default userSliceTest.reducer;