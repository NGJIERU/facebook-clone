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
import LoadingSpinner from '../components/LoadingSpinner.vue';
import StoriesBar from '../components/StoriesBar.vue';
import ThemeToggle from '../components/ThemeToggle.vue';
import GlobalSearch from '../components/GlobalSearch.vue';

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
    console.log('handlePostCreated called, refetching feed...');
    refetch();
};

const handlePostDeleted = () => {
    refetch();
};

const handlePostShared = () => {
    refetch();
};
</script>

<template>
  <div class="min-h-screen bg-gray-100 dark:bg-gray-900 pt-16">

    <!-- Main Content -->
    <main class="container mx-auto px-4 py-8 flex justify-center">
      <div class="w-full max-w-2xl">
        
        <!-- Stories -->
        <StoriesBar />
        
        <!-- Create Post -->
        <CreatePost @post-created="handlePostCreated" />

        <!-- Feed Controls -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 mb-6 flex items-center justify-between">
            <span class="font-medium text-gray-700 dark:text-gray-300">Feed Filter</span>
            <div class="flex gap-2">
                <button 
                    @click="showFriendsOnly = false"
                    :class="['px-3 py-1 rounded-full text-sm font-medium transition', !showFriendsOnly ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600']"
                >
                    All Posts
                </button>
                <button 
                    @click="showFriendsOnly = true"
                    :class="['px-3 py-1 rounded-full text-sm font-medium transition', showFriendsOnly ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600']"
                >
                    Friends Only
                </button>
            </div>
        </div>

        <!-- Feed Stream -->
        <div v-if="loading" class="py-10">
            <LoadingSpinner size="lg" text="Loading feed..." />
        </div>

        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
            <div class="text-red-500 text-4xl mb-2">⚠️</div>
            <h3 class="text-red-700 font-semibold mb-1">Unable to load feed</h3>
            <p class="text-red-600 text-sm mb-3">Please check if the backend is running.</p>
            <button @click="refetch()" class="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 text-sm">
                Try Again
            </button>
        </div>

        <div v-else-if="posts.length === 0" class="text-center text-gray-500 py-10">
            {{ showFriendsOnly ? 'No posts from friends yet.' : 'No posts yet. Be the first to share something!' }}
        </div>

        <div v-else class="feed-stream">
            <PostCard v-for="post in posts" :key="post.id" :post="post" @post-deleted="handlePostDeleted" @post-shared="handlePostShared" />
        </div>
      </div>
    </main>
  </div>
</template>
