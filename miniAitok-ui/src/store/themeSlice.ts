import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface ThemeState {
  dark: boolean;
}

const initialState: ThemeState = {
  dark: false,
};

const themeSlice = createSlice({
  name: 'theme',
  initialState,
  reducers: {
    setDark: (state, action: PayloadAction<boolean>) => {
      state.dark = action.payload;
    },
    removeDark: (state) => {
      state.dark = false;
    },
  },
});

export const { setDark, removeDark } = themeSlice.actions;
export default themeSlice.reducer;