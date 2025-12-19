export interface ApiClient {
  get: <T = any>(url: string) => Promise<T>;
  post: <T = any>(url: string, body?: unknown) => Promise<T>;
}

export const apiClient: ApiClient = {
  get: async (url) => {
    const res = await fetch(url);
    return (await res.json()) as any;
  },
  post: async (url, body) => {
    const res = await fetch(url, { method: 'POST', body: JSON.stringify(body), headers: { 'Content-Type': 'application/json' } });
    return (await res.json()) as any;
  },
};