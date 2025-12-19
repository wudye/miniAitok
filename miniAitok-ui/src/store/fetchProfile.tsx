import { createAsyncThunk } from '@reduxjs/toolkit';
import type { ApiClient } from './apiClient';

export const fetchProfile = createAsyncThunk<
  /* Returned */ any,
  /* Arg */ string,
  /* ThunkApiConfig */ { extra: ApiClient }
>(
  'user/fetchProfile',
  async (userId, { extra: api }) => {
    const data = await api.get(`/api/users/${userId}`);
    return data;
  }
);