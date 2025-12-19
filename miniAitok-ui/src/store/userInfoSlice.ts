import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface UserInfoState {
  userInfo: string;
}

const initialState: UserInfoState = {
  userInfo: '',
};

const userInfoSlice = createSlice({
  name: 'userInfo',
  initialState,
  reducers: {
    setUserInfo: (state, action: PayloadAction<string>) => {
      state.userInfo = action.payload;
    },
    removeUserInfo: (state) => {
      state.userInfo = '';
    },
  },
});

export const { setUserInfo, removeUserInfo } = userInfoSlice.actions;
export default userInfoSlice.reducer;