<script setup>
import { useAuthStore } from '../stores/auth';
import { useFriendStore } from '../stores/friends';
import { useRouter } from 'vue-router';
import { useQuery } from '@vue/apollo-composable';
import gql from 'graphql-tag';
import { computed, ref, onMounted } from 'vue';
import PostCard from '../components/PostCard.vue';
import CreatePost from '../components/CreatePost.vue';
import NotificationDropdown from '../components/NotificationDropdown.vue';

const authStore = useAuthStore();
const friendStore = useFriendStore();
const router = useRouter();
const showFriendsOnly = ref(false);

onMounted(() => {
    friendStore.fetchFriends();
});

const GET_FEED_QUERY = gql`
  query GetFeed {
    getFeed {
      id
      content
      authorId
      createdAt
      imageUrl
    }
  }
`;

const { result, loading, error, refetch } = useQuery(GET_FEED_QUERY);

const posts = computed(() => {
    const allPosts = result.value?.getFeed || [];
    if (!showFriendsOnly.value) {
        return allPosts;
    }
    // Filter posts from friends (and self?)
    // friendStore.friends contains UserProfile objects (id, username)
    // We also want to see our OWN posts usually.
    return allPosts.filter(post => {
        const isFriend = friendStore.friends.some(f => f.id === post.authorId);
        const isSelf = post.authorId === authStore.user?.id;
        return isFriend || isSelf;
    });
});

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};

const handlePostCreated = () => {
    refetch();
};
</script>

<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Navbar -->
    <nav class="bg-white shadow sticky top-0 z-50">
      <div class="container mx-auto px-4 h-16 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-blue-600 cursor-pointer" @click="router.push('/')">Facebook</h1>
        
        <div class="flex items-center gap-6">
          <button @click="router.push('/friends')" class="text-gray-600 hover:text-blue-600 font-medium">Friends</button>
          <NotificationDropdown />
          
          <div class="flex items-center gap-2">
              <div class="bg-blue-100 w-8 h-8 rounded-full flex items-center justify-center text-blue-600 font-bold text-sm">
                  {{ authStore.user?.username?.charAt(0).toUpperCase() }}
              </div>
              <span class="font-medium text-gray-700 hidden sm:block">{{ authStore.user?.username }}</span>
          </div>
          
          <button @click="handleLogout" class="text-gray-500 hover:text-red-600 transition font-medium text-sm">Logout</button>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="container mx-auto px-4 py-8 flex justify-center">
      <div class="w-full max-w-2xl">
        
        <!-- Create Post -->
        <CreatePost @post-created="handlePostCreated" />

        <!-- Feed Controls -->
        <div class="bg-white rounded-lg shadow p-4 mb-6 flex items-center justify-between">
            <span class="font-medium text-gray-700">Feed Filter</span>
            <div class="flex gap-2">
                <button 
                    @click="showFriendsOnly = false"
                    :class="['px-3 py-1 rounded-full text-sm font-medium transition', !showFriendsOnly ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200']"
                >
                    All Posts
                </button>
                <button 
                    @click="showFriendsOnly = true"
                    :class="['px-3 py-1 rounded-full text-sm font-medium transition', showFriendsOnly ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200']"
                >
                    Friends Only
                </button>
            </div>
        </div>

        <!-- Feed Stream -->
        <div v-if="loading" class="text-center py-10">
            <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600 mx-auto"></div>
        </div>

        <div v-else-if="error" class="text-center text-red-500 py-10">
            Error loading feed. Is the backend running?
        </div>

        <div v-else-if="posts.length === 0" class="text-center text-gray-500 py-10">
            {{ showFriendsOnly ? 'No posts from friends yet.' : 'No posts yet. Be the first to share something!' }}
        </div>

        <div v-else class="feed-stream">
            <PostCard v-for="post in posts" :key="post.id" :post="post" />
        </div>
      </div>
    </main>
  </div>
</template>
