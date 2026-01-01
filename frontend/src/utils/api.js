import axios from 'axios';

const api = axios.create({
    baseURL: '/api/auth', // Service Mesh Routing
    // CACHE_BUST_ID: 12345
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;
