import axios from 'axios';

const api = axios.create({
    baseURL: '/api/auth', // Relative path for Service Mesh / LoadBalancer
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
