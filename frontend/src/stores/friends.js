import { defineStore } from 'pinia';
import api from '../utils/api';

export const useFriendStore = defineStore('friend', {
    state: () => ({
        friends: [],
        pendingRequests: [],
        loading: false,
        error: null
    }),

    actions: {
        async fetchFriends() {
            this.loading = true;
            try {
                const response = await api.get('/friends');
                this.friends = response.data;
            } catch (err) {
                this.error = err.response?.data?.message || 'Failed to fetch friends';
            } finally {
                this.loading = false;
            }
        },

        async fetchPendingRequests() {
            try {
                const response = await api.get('/friends/requests');
                this.pendingRequests = response.data;
            } catch (err) {
                console.error('Failed to fetch pending requests', err);
            }
        },

        async sendFriendRequest(addresseeId) {
            try {
                await api.post(`/friends/request/${addresseeId}`);
                return { success: true };
            } catch (err) {
                return {
                    success: false,
                    message: err.response?.data?.message || 'Failed to send request'
                };
            }
        },

        async acceptFriendRequest(friendshipId) {
            try {
                await api.put(`/friends/accept/${friendshipId}`);
                // Refresh lists
                await this.fetchPendingRequests();
                await this.fetchFriends();
                return { success: true };
            } catch (err) {
                return { success: false, message: 'Failed to accept request' };
            }
        },

        async rejectFriendRequest(friendshipId) {
            try {
                await api.delete(`/friends/reject/${friendshipId}`);
                // Refresh list
                await this.fetchPendingRequests();
                return { success: true };
            } catch (err) {
                return { success: false, message: 'Failed to reject request' };
            }
        },

        async searchByUsername(query) {
            if (!query) return [];
            try {
                const response = await api.get(`/users/search?query=${encodeURIComponent(query)}`);
                return response.data;
            } catch (err) {
                console.error('Search failed', err);
                return [];
            }
        }
    }
});
