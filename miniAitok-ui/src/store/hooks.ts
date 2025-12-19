import { useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from './store';
import type { TypedUseSelectorHook } from 'react-redux';

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

/*
Practical notes: import and use these hooks across your Reac
t components instead of the untyped useDispatch/useSelector.
 Also keep RootState and AppDispatch up to date in your store 
 definitions — if RootState includes redux-persist metadata or
  other wrapper types, consider deriving it from the unwrapped r
  oot reducer for cleaner selector types.
*/