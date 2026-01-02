<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../utils/api';
import PostCard from '../components/PostCard.vue';

const router = useRouter();
const savedPosts = ref([]);
const loading = ref(true);

onMounted(async () => {
  await fetchSavedPosts();
});

const fetchSavedPosts = async () => {
  loading.value = true;
  try {
    const response = await api.get('/feed/saved');
    savedPosts.value = response.data;
  } catch (e) {
    console.error('Failed to fetch saved posts', e);
  } finally {
    loading.value = false;
  }
};

const handlePostDeleted = (postId) => {
  savedPosts.value = savedPosts.value.filter(p => p.id !== postId);
};
</script>

<template>
  <div class="min-h-screen bg-gray-100 dark:bg-gray-900 pt-16">
    <div class="container mx-auto px-4 py-4 max-w-2xl">
      <h2 class="text-3xl font-bold mb-6 text-gray-800 dark:text-gray-100">Saved Posts</h2>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-10">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600 mx-auto"></div>
      </div>

      <!-- Saved Posts -->
      <div v-else-if="savedPosts.length > 0">
        <PostCard 
          v-for="post in savedPosts" 
          :key="post.id" 
          :post="post"
          @post-deleted="handlePostDeleted"
        />
      </div>

      <!-- Empty State -->
      <div v-else class="bg-white dark:bg-gray-800 rounded-lg shadow p-8 text-center">
        <div class="text-6xl mb-4">🔖</div>
        <h3 class="text-xl font-semibold text-gray-800 dark:text-gray-100 mb-2">No saved posts yet</h3>
        <p class="text-gray-500 dark:text-gray-400">Posts you save will appear here for easy access later.</p>
        <button 
          @click="router.push('/')"
          class="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          Browse Feed
        </button>
      </div>
    </div>
  </div>
</template>
