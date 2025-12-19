import React, { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '../store/hooks';
import { fetchUserProfile } from '../store/userSliceWithExtraReducers';

export function Profile({ userId }: { userId: string }) {
    console.log('Profile component rendered with userId:', userId);
  const dispatch = useAppDispatch();
  const { token, isLoading, error } = useAppSelector((s) => s.userTest);

  useEffect(() => {
    dispatch(fetchUserProfile());
  }, [dispatch]);

  if (isLoading) return <div>加载中...</div>;
  if (error) return <div>错误：{error}</div>;
  return <div>{token ? token : '无数据'}</div>;
}