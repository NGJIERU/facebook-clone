import { defineStore } from 'pinia';
import api from '../utils/api';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: JSON.parse(localStorage.getItem('user')) || null,
        token: localStorage.getItem('token') || null,
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
    },
    actions: {
        async login(email, password) {
            try {
                const response = await api.post('/auth/login', { email, password });
                const { token } = response.data;

                this.token = token;
                localStorage.setItem('token', token);

                // Fetch full profile
                try {
                    const profileRes = await api.get('/users/profile');
                    this.user = profileRes.data;
                } catch (e) {
                    console.warn('Failed to fetch profile', e);
                    this.user = { username: email }; // Fallback
                }

                localStorage.setItem('user', JSON.stringify(this.user));

                return { success: true };
            } catch (error) {
                console.error('Login failed', error);
                return {
                    success: false,
                    message: getErrorMessage(error)
                };
            }
        },
        async register(username, email, password) {
            try {
                await api.post('/auth/register', { username, email, password });
                return { success: true };
            } catch (error) {
                console.error('Registration failed', error);
                return {
                    success: false,
                    message: getErrorMessage(error)
                };
            }
        },
        logout() {
            this.token = null;
            this.user = null;
            localStorage.removeItem('token');
            localStorage.removeItem('user');
        }
    }
});

function getErrorMessage(error) {
    if (error.response) {
        // server responded with a status code that falls out of the range of 2xx
        const status = error.response.status;
        const data = error.response.data;

        if (data && data.message) {
            return data.message;
        }

        switch (status) {
            case 400: return 'Invalid input provided.';
            case 401: return 'Invalid username or password.';
            case 403: return 'Access denied.';
            case 404: return 'Resource not found.';
            case 409: return 'Username or email already exists.';
            case 500: return 'Internal Server Error. Please try again later.';
            default: return `Request failed with status ${status}.`;
        }
    } else if (error.request) {
        // The request was made but no response was received
        return 'Unable to reach the server. Please check your internet connection or try again later.';
    } else {
        // Something happened in setting up the request that triggered an Error
        return error.message || 'An unexpected error occurred.';
    }
}
