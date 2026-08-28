import axios from 'axios';

const rawBaseUrl = import.meta.env.VITE_API_BASE_URL || '';
const cleanBaseUrl = rawBaseUrl.endsWith('/') ? rawBaseUrl.slice(0, -1) : rawBaseUrl;
const API_ROOT = cleanBaseUrl ? `${cleanBaseUrl}/api` : '/api';

const api = axios.create({
  baseURL: API_ROOT,
  withCredentials: true,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('gd_access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle token expiration and unwrap ApiResponse
api.interceptors.response.use(
  (response) => {
    if (response.data && typeof response.data.success === 'boolean') {
      return response.data;
    }
    return { success: true, data: response.data };
  },
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !originalRequest.url?.includes('/auth/')) {
      originalRequest._retry = true;
      const currentToken = localStorage.getItem('gd_access_token');
      if (!currentToken) {
        return Promise.reject(error.response?.data || { message: 'Unauthorized' });
      }
      try {
        const refreshUrl = `${API_ROOT}/auth/refresh`;
        const refreshResponse = await axios.post(refreshUrl, {}, { withCredentials: true, timeout: 15000 });
        if (refreshResponse.data?.data?.accessToken) {
          const newToken = refreshResponse.data.data.accessToken;
          localStorage.setItem('gd_access_token', newToken);
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return api(originalRequest);
        }
      } catch (refreshErr) {
        localStorage.removeItem('gd_access_token');
      }
    }

    // Network resilience error formatting
    let friendlyMessage = error.response?.data?.message || error.message || 'An unexpected network error occurred.';
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      friendlyMessage = 'Connection is slow. Please check your internet and retry.';
    } else if (error.code === 'ERR_NETWORK' || !error.response) {
      friendlyMessage = 'Unable to reach GramDrishti AI backend. Please verify your connection.';
    }

    return Promise.reject({
      success: false,
      message: friendlyMessage,
      status: error.response?.status || 0,
      raw: error
    });
  }
);

export default api;
