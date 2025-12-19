import {createSlice, type PayloadAction} from '@reduxjs/toolkit'


interface UserState {
    accessToken: string;
    refreshToken?: string;
}
const initialState: UserState = {

    accessToken : '' ,
    refreshToken : ''
}   

const userSlice = createSlice({ 
    name : 'user' ,
    initialState ,
    reducers: {
        setToken: (state,action: PayloadAction<{accessToken: string, refreshToken?: string}>) => {
            state.accessToken = action.payload.accessToken;
            if (action.payload.refreshToken) {
                state.refreshToken = action.payload.refreshToken;
            }
        },
        removeToken: (state) => {   
            state.accessToken = '';
            state.refreshToken = '';
        }
     },
});

export const { setToken, removeToken } = userSlice.actions;
export default userSlice.reducer;