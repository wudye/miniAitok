import React, { useState, useEffect } from 'react';
import { useAppSelector, useAppDispatch } from './hooks';
import { 
  loginUser, 
  fetchUserProfile, 
  setToken, 
  removeToken, 
  clearError 
} from './userSliceWithExtraReducers';

const EnhancedUserComponent: React.FC = () => {
  const dispatch = useAppDispatch();
  const { 
    token, 
    isLoading, 
    error, 
    profile 
  } = useAppSelector((state) => state.userTest);

  const [credentials, setCredentials] = useState({
    username: '',
    password: '',
  });

  const [manualToken, setManualToken] = useState('');

  // Auto-fetch profile when token changes
  useEffect(() => {
    if (token && !profile) {
      dispatch(fetchUserProfile());
    }
  }, [token, profile, dispatch]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (credentials.username && credentials.password) {
      dispatch(loginUser(credentials));
    }
  };

  const handleLogout = () => {
    dispatch(removeToken());
    setCredentials({ username: '', password: '' });
    setManualToken('');
  };

  const handleSetManualToken = () => {
    if (manualToken.trim()) {
      dispatch(setToken(manualToken.trim()));
    }
  };

  const handleClearError = () => {
    dispatch(clearError());
  };

  return (
    <div style={{ 
      padding: '20px', 
      maxWidth: '500px', 
      margin: '0 auto',
      fontFamily: 'Arial, sans-serif'
    }}>
      <h2>Enhanced User Authentication</h2>
      
      {/* Login Form */}
      {!token ? (
        <div style={{ marginBottom: '20px' }}>
          <h3>Login with Credentials</h3>
          <form onSubmit={handleLogin}>
            <div style={{ marginBottom: '10px' }}>
              <input
                type="text"
                placeholder="Username"
                value={credentials.username}
                onChange={(e) => setCredentials(prev => ({
                  ...prev,
                  username: e.target.value
                }))}
                style={{ 
                  width: '100%', 
                  padding: '8px',
                  border: '1px solid #ddd',
                  borderRadius: '4px'
                }}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <input
                type="password"
                placeholder="Password"
                value={credentials.password}
                onChange={(e) => setCredentials(prev => ({
                  ...prev,
                  password: e.target.value
                }))}
                style={{ 
                  width: '100%', 
                  padding: '8px',
                  border: '1px solid #ddd',
                  borderRadius: '4px'
                }}
              />
            </div>
            <button 
              type="submit" 
              disabled={isLoading || !credentials.username || !credentials.password}
              style={{ 
                width: '100%', 
                padding: '10px', 
                backgroundColor: isLoading ? '#ccc' : '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: isLoading ? 'not-allowed' : 'pointer'
              }}
            >
              {isLoading ? 'Logging in...' : 'Login'}
            </button>
          </form>

          <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
            <h4>Or Set Token Manually</h4>
            <div style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
              <input
                type="text"
                placeholder="Enter JWT token"
                value={manualToken}
                onChange={(e) => setManualToken(e.target.value)}
                style={{ 
                  flex: 1, 
                  padding: '8px',
                  border: '1px solid #ddd',
                  borderRadius: '4px'
                }}
              />
              <button 
                onClick={handleSetManualToken}
                disabled={!manualToken.trim()}
                style={{ 
                  padding: '8px 16px',
                  backgroundColor: !manualToken.trim() ? '#ccc' : '#28a745',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: !manualToken.trim() ? 'not-allowed' : 'pointer'
                }}
              >
                Set Token
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div>
          <h3>Welcome! 🎉</h3>
          {profile ? (
            <div style={{ 
              padding: '15px', 
              backgroundColor: '#d4edda',
              border: '1px solid #c3e6cb',
              borderRadius: '4px',
              marginBottom: '20px'
            }}>
              <h4>Profile Loaded:</h4>
              <p><strong>ID:</strong> {profile.id}</p>
              <p><strong>Username:</strong> {profile.username}</p>
              <p><strong>Email:</strong> {profile.email}</p>
            </div>
          ) : (
            <div style={{ 
              padding: '15px', 
              backgroundColor: '#fff3cd',
              border: '1px solid #ffeaa7',
              borderRadius: '4px',
              marginBottom: '20px'
            }}>
              <p>Loading profile...</p>
            </div>
          )}
          
          <button 
            onClick={handleLogout}
            style={{ 
              padding: '10px 20px', 
              backgroundColor: '#dc3545',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Logout
          </button>
        </div>
      )}

      {/* Error Display */}
      {error && (
        <div style={{ 
          marginTop: '20px', 
          padding: '15px', 
          backgroundColor: '#f8d7da',
          color: '#721c24',
          border: '1px solid #f5c6cb',
          borderRadius: '4px'
        }}>
          <h4>Error:</h4>
          <p>{error}</p>
          <button 
            onClick={handleClearError}
            style={{ 
              marginTop: '10px',
              padding: '5px 10px',
              backgroundColor: '#721c24',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Clear Error
          </button>
        </div>
      )}

      {/* Debug Info */}
      <div style={{ 
        marginTop: '20px', 
        padding: '15px', 
        backgroundColor: '#f8f9fa',
        border: '1px solid #dee2e6',
        borderRadius: '4px',
        fontSize: '14px'
      }}>
        <h4>Debug Info:</h4>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
          <div><strong>Token:</strong> {token ? '✅ Present' : '❌ None'}</div>
          <div><strong>Loading:</strong> {isLoading ? '🔄 Yes' : '✅ No'}</div>
          <div><strong>Error:</strong> {error ? '❌ ' + error : '✅ None'}</div>
          <div><strong>Profile:</strong> {profile ? '✅ Loaded' : '❌ Not loaded'}</div>
        </div>
        {token && (
          <div style={{ marginTop: '10px', wordBreak: 'break-all' }}>
            <strong>Token Preview:</strong> {token.substring(0, 50)}...
          </div>
        )}
      </div>
    </div>
  );
};

export default EnhancedUserComponent;