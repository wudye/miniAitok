// API Client for use with Redux Thunk extraArgument
export interface ApiClient {
  get: (url: string) => Promise<any>;
  post: (url: string, data?: any) => Promise<any>;
  put: (url: string, data?: any) => Promise<any>;
  delete: (url: string) => Promise<any>;
  setAuth: (token: string) => void;
  clearAuth: () => void;
}

class ApiClientImpl implements ApiClient {
  private baseUrl: string = '';
  private authToken: string = '';

  constructor(baseUrl: string = '') {
    // Use provided baseUrl, environment variable, or default
    const envUrl = this.getEnvVar('VITE_API_BASE_URL');
    this.baseUrl = baseUrl || envUrl || 'http://localhost:3001/api';
  }

  private getEnvVar(name: string): string | undefined {
    // Safe environment variable access
    try {
      if (typeof window !== 'undefined' && (window as any).__ENV__) {
        return (window as any).__ENV__[name];
      }
      return undefined;
    } catch {
      return undefined;
    }
  }

  private async request(endpoint: string, options: RequestInit = {}): Promise<any> {
    const url = `${this.baseUrl}${endpoint}`;
    
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    // Add auth token if available
    if (this.authToken) {
      config.headers = {
        ...config.headers,
        Authorization: `Bearer ${this.authToken}`,
      };
    }

    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      // Handle 204 No Content
      if (response.status === 204) {
        return null;
      }

      return await response.json();
    } catch (error) {
      if (error instanceof Error) {
        throw error;
      }
      throw new Error('Network error occurred');
    }
  }

  async get(endpoint: string): Promise<any> {
    return this.request(endpoint, { method: 'GET' });
  }

  async post(endpoint: string, data?: any): Promise<any> {
    return this.request(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async put(endpoint: string, data?: any): Promise<any> {
    return this.request(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async delete(endpoint: string): Promise<any> {
    return this.request(endpoint, { method: 'DELETE' });
  }

  setAuth(token: string): void {
    this.authToken = token;
  }

  clearAuth(): void {
    this.authToken = '';
  }
}

// Create and export a singleton instance
export const apiClient: ApiClient = new ApiClientImpl();

// Export the class for testing or custom instances
export { ApiClientImpl };