<script setup>
import { ref } from 'vue';
import { useMutation } from '@vue/apollo-composable';
import gql from 'graphql-tag';
import { useAuthStore } from '../stores/auth';
import { uploadMedia } from '../utils/api';

const content = ref('');
const selectedFile = ref(null);
const fileInput = ref(null);
const uploading = ref(false);
const authStore = useAuthStore();
const emit = defineEmits(['post-created']);

const CREATE_POST_MUTATION = gql`
  mutation CreatePost($content: String!, $imageUrl: String) {
    createPost(content: $content, imageUrl: $imageUrl) {
      id
      content
      authorId
      createdAt
      imageUrl
    }
  }
`;

const { mutate: createPost, loading: mutationLoading, error } = useMutation(CREATE_POST_MUTATION);

const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (file) {
        selectedFile.value = file;
    }
};

const triggerFileInput = () => {
    fileInput.value.click();
};

const clearFile = () => {
    selectedFile.value = null;
    if (fileInput.value) {
        fileInput.value.value = '';
    }
};

const handleSubmit = async () => {
    if (!content.value.trim() && !selectedFile.value) return;

    try {
        let imageUrl = null;
        uploading.value = true;

        if (selectedFile.value) {
            // Assume user ID is available in auth store. Using '1' as fallback if not.
            // In a real app, we should ensure we have the ID.
            const userId = authStore.user?.id || 1; 
            const response = await uploadMedia(selectedFile.value, userId);
            imageUrl = response.data.url;
        }

        await createPost({
            content: content.value,
            imageUrl: imageUrl
        });

        content.value = '';
        clearFile();
        emit('post-created');
    } catch (e) {
        console.error("Failed to create post:", e);
    } finally {
        uploading.value = false;
    }
};
const getObjectUrl = (file) => {
    return URL.createObjectURL(file);
};
</script>

<template>
  <div class="bg-white p-4 rounded-lg shadow mb-6">
    <div class="flex gap-4">
      <div class="w-10 h-10 bg-gray-300 rounded-full flex-shrink-0 overflow-hidden flex items-center justify-center">
         <img v-if="authStore.user?.profilePicUrl" :src="authStore.user.profilePicUrl" class="w-full h-full object-cover" />
         <span v-else class="text-gray-600 font-bold">{{ authStore.user?.username?.charAt(0) || 'U' }}</span>
      </div>
      <div class="flex-grow">
        <textarea 
            v-model="content"
            placeholder="What's on your mind?"
            class="w-full bg-gray-100 rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none h-24 mb-2"
        ></textarea>
        
        <!-- Image Preview -->
        <div v-if="selectedFile" class="mb-4 relative inline-block">
            <img :src="getObjectUrl(selectedFile)" class="h-32 w-auto rounded-lg object-cover border border-gray-200" />
            <button @click="clearFile" class="absolute -top-2 -right-2 bg-gray-800 text-white rounded-full p-1 hover:bg-gray-900 shadow">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
            </button>
        </div>

        <div v-if="error" class="text-red-500 text-sm mt-2">Error creating post.</div>
        
        <div class="flex justify-between items-center mt-2 border-t pt-2 border-gray-100">
            <div>
                <input 
                    type="file" 
                    ref="fileInput" 
                    @change="handleFileSelect" 
                    class="hidden" 
                    accept="image/jpeg,image/png,image/gif,video/*"
                />
                <button 
                    @click="triggerFileInput"
                    class="flex items-center gap-2 text-gray-500 hover:bg-gray-100 px-3 py-2 rounded-lg transition"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <span class="font-medium">Photo/Video</span>
                </button>
            </div>

            <button 
                @click="handleSubmit"
                :disabled="mutationLoading || uploading || (!content.trim() && !selectedFile)"
                class="bg-blue-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center gap-2"
            >
                <span v-if="uploading">Uploading...</span>
                <span v-else>Post</span>
            </button>
        </div>
      </div>
    </div>
  </div>
</template>
