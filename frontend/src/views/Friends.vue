<script setup>
import { ref, onMounted } from 'vue';
import { useFriendStore } from '../stores/friends';
import { useRouter } from 'vue-router';

const friendStore = useFriendStore();
const router = useRouter();

const searchQuery = ref('');
const searchResults = ref([]);
const searched = ref(false);
const message = ref('');
const error = ref('');

onMounted(() => {
    friendStore.fetchFriends();
    friendStore.fetchPendingRequests();
});

const handleSearch = async () => {
    if (!searchQuery.value.trim()) return;
    console.log('Searching for:', searchQuery.value);
    searchResults.value = await friendStore.searchByUsername(searchQuery.value);
    searched.value = true;
};

const handleSendRequest = async (userId) => {
    error.value = '';
    message.value = '';

    const result = await friendStore.sendFriendRequest(userId);
    
    if (result.success) {
        message.value = 'Friend request sent!';
        // Optional: remove from search results logic
    } else {
        error.value = result.message;
    }
};

const handleAccept = async (friendshipId) => {
    await friendStore.acceptFriendRequest(friendshipId);
};

const handleReject = async (friendshipId) => {
    await friendStore.rejectFriendRequest(friendshipId);
};

const handleUnfriend = async (friendId) => {
    if (!confirm('Are you sure you want to unfriend this person?')) return;
    await friendStore.unfriend(friendId);
};
</script>

<template>
    <div class="min-h-screen bg-gray-100 pb-10">
        <!-- Navbar -->
        <nav class="bg-white shadow sticky top-0 z-50">
            <div class="container mx-auto px-4 h-16 flex justify-between items-center">
                <h1 class="text-2xl font-bold text-blue-600 cursor-pointer" @click="router.push('/')">Facebook</h1>
                <button @click="router.push('/')" class="text-gray-600 hover:text-blue-600 font-medium">Back to Feed</button>
            </div>
        </nav>

        <div class="container mx-auto px-4 py-8 max-w-2xl">
            <h2 class="text-3xl font-bold mb-6 text-gray-800">Friends</h2>

            <!-- Add Friend Section -->
            <!-- Search/Add Friend Section -->
            <div class="bg-white rounded-lg shadow p-6 mb-6">
                <h3 class="text-xl font-semibold mb-4">Find Friends</h3>
                <div class="flex gap-2 mb-4">
                    <input 
                        v-model="searchQuery" 
                        @keyup.enter="handleSearch"
                        type="text" 
                        placeholder="Search by username..." 
                        class="flex-1 border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button 
                        @click="handleSearch" 
                        class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                    >
                        Search
                    </button>
                </div>

                <!-- Search Results -->
                <div v-if="searchResults.length > 0" class="space-y-3">
                    <h4 class="text-sm font-semibold text-gray-500 uppercase">Results</h4>
                    <div v-for="user in searchResults" :key="user.id" class="flex items-center justify-between p-3 border rounded hover:bg-gray-50">
                        <div class="flex items-center gap-3">
                            <div class="bg-gray-200 w-8 h-8 rounded-full flex items-center justify-center text-gray-600 font-bold text-sm">
                                {{ user.username?.charAt(0).toUpperCase() }}
                            </div>
                            <span class="font-medium">{{ user.username }}</span>
                        </div>
                        <button 
                            @click="handleSendRequest(user.id)" 
                            class="text-blue-600 hover:text-blue-800 text-sm font-medium disabled:opacity-50"
                            :disabled="friendStore.loading"
                        >
                            Add Friend
                        </button>
                    </div>
                </div>
                <div v-else-if="searched && searchResults.length === 0" class="text-gray-500 text-sm italic">
                    No users found matching "{{ searchQuery }}"
                </div>

                <p v-if="message" class="text-green-600 mt-2 text-sm">{{ message }}</p>
                <p v-if="error" class="text-red-500 mt-2 text-sm">{{ error }}</p>
            </div>

            <!-- Pending Requests -->
            <div v-if="friendStore.pendingRequests.length > 0" class="bg-white rounded-lg shadow p-6 mb-6">
                <h3 class="text-xl font-semibold mb-4 text-orange-600">Pending Requests</h3>
                <div class="space-y-4">
                    <div v-for="request in friendStore.pendingRequests" :key="request.id" class="flex justify-between items-center border-b pb-3 last:border-0 last:pb-0">
                        <div>
                            <span class="font-medium text-gray-800">{{ request.requesterName }}</span>
                            <span class="text-gray-500 text-sm ml-2">sent you a request</span>
                        </div>
                        <div class="flex gap-2">
                            <button @click="handleAccept(request.id)" class="bg-green-500 text-white px-3 py-1 rounded hover:bg-green-600 text-sm">Accept</button>
                            <button @click="handleReject(request.id)" class="bg-gray-300 text-gray-700 px-3 py-1 rounded hover:bg-gray-400 text-sm">Reject</button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Friends List -->
            <div class="bg-white rounded-lg shadow p-6">
                <h3 class="text-xl font-semibold mb-4">My Friends ({{ friendStore.friends.length }})</h3>
                
                <div v-if="friendStore.loading" class="text-center py-4">
                    <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                </div>
                
                <div v-else-if="friendStore.friends.length === 0" class="text-gray-500 text-center py-4">
                    You have no friends yet. Add someone above!
                </div>

                <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div v-for="friend in friendStore.friends" :key="friend.id" class="flex items-center justify-between p-3 border rounded hover:bg-gray-50">
                        <div class="flex items-center gap-3">
                            <div class="bg-blue-100 w-10 h-10 rounded-full overflow-hidden flex items-center justify-center text-blue-600 font-bold">
                                <img v-if="friend.profilePicUrl" :src="friend.profilePicUrl" class="w-full h-full object-cover" />
                                <span v-else>{{ friend.username?.charAt(0).toUpperCase() }}</span>
                            </div>
                            <div>
                                <div class="font-medium text-gray-900">{{ friend.username }}</div>
                            </div>
                        </div>
                        <button 
                            @click="handleUnfriend(friend.id)" 
                            class="text-red-500 hover:text-red-700 text-sm px-3 py-1 border border-red-300 rounded hover:bg-red-50"
                        >
                            Unfriend
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
