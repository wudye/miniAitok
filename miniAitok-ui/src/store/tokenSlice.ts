import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface TokenState {
  token: string;
}

const initialState: TokenState = {
  token: '',
};

const tokenSlice = createSlice({
  name: 'token',
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

export const { setToken, removeToken } = tokenSlice.actions;
export default tokenSlice.reducer;