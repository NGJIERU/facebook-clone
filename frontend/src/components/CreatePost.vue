<script setup>
import { ref } from 'vue';
import { useMutation } from '@vue/apollo-composable';
import gql from 'graphql-tag';
import { useAuthStore } from '../stores/auth';

const content = ref('');
const authStore = useAuthStore();
const emit = defineEmits(['post-created']);

const CREATE_POST_MUTATION = gql`
  mutation CreatePost($content: String!) {
    createPost(content: $content) {
      id
      content
      authorId
      createdAt
    }
  }
`;

const { mutate: createPost, loading, error } = useMutation(CREATE_POST_MUTATION);

const handleSubmit = async () => {
    if (!content.value.trim()) return;

    try {
        await createPost({
            content: content.value
        });
        content.value = '';
        emit('post-created');
    } catch (e) {
        console.error("Failed to create post:", e);
    }
};
</script>

<template>
  <div class="bg-white p-4 rounded-lg shadow mb-6">
    <div class="flex gap-4">
      <div class="w-10 h-10 bg-gray-300 rounded-full flex-shrink-0 flex items-center justify-center">
         <span class="text-gray-600 font-bold">{{ authStore.user?.username?.charAt(0) || 'U' }}</span>
      </div>
      <div class="flex-grow">
        <textarea 
            v-model="content"
            placeholder="What's on your mind?"
            class="w-full bg-gray-100 rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none h-24"
        ></textarea>
        <div v-if="error" class="text-red-500 text-sm mt-2">Error creating post.</div>
        <div class="flex justify-end mt-2">
            <button 
                @click="handleSubmit"
                :disabled="loading || !content.trim()"
                class="bg-blue-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
                Post
            </button>
        </div>
      </div>
    </div>
  </div>
</template>
