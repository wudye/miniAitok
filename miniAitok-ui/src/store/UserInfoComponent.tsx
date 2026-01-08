import React from 'react';
import { useAppSelector, useAppDispatch } from './hooks';
import { setUserInfo, removeUserInfo } from './userInfoSlice';

const UserInfoComponent: React.FC = () => {
  const userInfo = useAppSelector((state) => state.userInfo?.userInfo || '');
  const dispatch = useAppDispatch();

  const handleSetUserInfo = (newUserInfo: string) => {
    dispatch(setUserInfo(newUserInfo));
  };

  const handleRemoveUserInfo = () => {
    dispatch(removeUserInfo());
  };

  return (
    <div>
      <p>User Info: {userInfo || 'No user info'}</p>
      <button onClick={() => handleSetUserInfo('John Doe - john@example.com')}>
        Set User Info
      </button>
      <button onClick={() => handleSetUserInfo('Jane Smith - jane@example.com')}>
        Set Different User
      </button>
      <button onClick={handleRemoveUserInfo}>Clear User Info</button>
    </div>
  );
};

export default UserInfoComponent;