<script setup>
import { computed, ref } from 'vue';

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
});

const formattedDate = computed(() => {
  if (!props.post.createdAt) return '';
  const date = new Date(props.post.createdAt);
  // If invalid date (e.g. Number(iso_string) failed), try parsing directly
  return isNaN(date.getTime()) ? 'Just now' : date.toLocaleString();
});
const imageError = ref(false);
</script>

<template>
  <div class="bg-white p-4 rounded-lg shadow mb-4">
    <div class="flex items-center mb-4">
      <div class="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center mr-3">
        <span class="text-gray-600 font-bold">{{ post.authorId?.charAt(0) || 'U' }}</span>
      </div>
      <div>
        <h3 class="font-bold text-gray-800">{{ post.authorId || 'Unknown User' }}</h3>
        <p class="text-xs text-gray-500">{{ formattedDate }}</p>
      </div>
    </div>
    
    <p class="text-gray-800 mb-4">{{ post.content }}</p>
    
    <div v-if="post.imageUrl && !imageError" class="mb-4">
      <img :src="post.imageUrl" alt="Post content" class="rounded-lg max-h-96 w-full object-cover" @error="imageError = true">
    </div>
    
    <div class="flex items-center justify-between text-gray-500 text-sm border-t pt-3">
      <button class="flex items-center hover:text-blue-600 transition">
        <span>Like</span>
      </button>
      <button class="flex items-center hover:text-blue-600 transition">
        <span>Comment</span>
      </button>
      <button class="flex items-center hover:text-blue-600 transition">
        <span>Share</span>
      </button>
    </div>
  </div>
</template>
