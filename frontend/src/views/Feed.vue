<script setup>
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';
import { useQuery } from '@vue/apollo-composable';
import gql from 'graphql-tag';
import { computed } from 'vue';
import PostCard from '../components/PostCard.vue';
import CreatePost from '../components/CreatePost.vue';

const authStore = useAuthStore();
const router = useRouter();

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

const posts = computed(() => result.value?.getFeed || []);

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
        <h1 class="text-2xl font-bold text-blue-600">Facebook</h1>
        <div class="flex items-center gap-4">
          <span class="font-medium text-gray-700">Hello, {{ authStore.user?.username }}</span>
          <button @click="handleLogout" class="text-gray-600 hover:text-red-600 transition font-medium">Logout</button>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="container mx-auto px-4 py-8 flex justify-center">
      <div class="w-full max-w-2xl">
        
        <!-- Create Post -->
        <CreatePost @post-created="handlePostCreated" />

        <!-- Feed Stream -->
        <div v-if="loading" class="text-center py-10">
            <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600 mx-auto"></div>
        </div>

        <div v-else-if="error" class="text-center text-red-500 py-10">
            Error loading feed. Is the backend running?
        </div>

        <div v-else-if="posts.length === 0" class="text-center text-gray-500 py-10">
            No posts yet. Be the first to share something!
        </div>

        <div v-else class="feed-stream">
            <PostCard v-for="post in posts" :key="post.id" :post="post" />
        </div>
      </div>
    </main>
  </div>
</template>
