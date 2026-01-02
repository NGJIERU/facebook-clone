<script setup>
import { computed, ref, onMounted } from 'vue';
import api from '../utils/api';

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
});

const authorName = ref(null);
const authorProfilePic = ref(null);
const liked = ref(false);
const likesCount = ref(props.post.likesCount || 0);
const showComments = ref(false);
const comments = ref([]);
const newComment = ref('');
const loadingComments = ref(false);
const commentAuthors = ref({});

onMounted(async () => {
  // Fetch author profile (name and picture)
  if (props.post.authorId) {
    try {
      const response = await api.get(`/users/profile/${props.post.authorId}`);
      authorName.value = response.data.username;
      authorProfilePic.value = response.data.profilePicUrl;
    } catch (e) {
      console.warn('Failed to fetch author profile', e);
      authorName.value = null;
      authorProfilePic.value = null;
    }
  }
  
  // Fetch like status
  try {
    const response = await api.get(`/feed/posts/${props.post.id}/like/status`);
    liked.value = response.data.liked;
    likesCount.value = response.data.likesCount;
  } catch (e) {
    console.warn('Failed to fetch like status', e);
  }
});

const displayName = computed(() => authorName.value || props.post.authorId || 'Unknown User');

const formattedDate = computed(() => {
  if (!props.post.createdAt) return '';
  const date = new Date(props.post.createdAt);
  return isNaN(date.getTime()) ? 'Just now' : date.toLocaleString();
});

const imageError = ref(false);

const toggleLike = async () => {
  try {
    if (liked.value) {
      const response = await api.delete(`/feed/posts/${props.post.id}/like`);
      liked.value = false;
      likesCount.value = response.data.likesCount;
    } else {
      const response = await api.post(`/feed/posts/${props.post.id}/like`);
      liked.value = true;
      likesCount.value = response.data.likesCount;
    }
  } catch (e) {
    console.error('Failed to toggle like', e);
  }
};

const toggleComments = async () => {
  showComments.value = !showComments.value;
  if (showComments.value && comments.value.length === 0) {
    await fetchComments();
  }
};

const fetchComments = async () => {
  loadingComments.value = true;
  try {
    const response = await api.get(`/feed/posts/${props.post.id}/comments`);
    comments.value = response.data;
    // Fetch author names for comments
    for (const comment of comments.value) {
      if (!commentAuthors.value[comment.authorId]) {
        try {
          const authorRes = await api.get(`/users/profile/${comment.authorId}`);
          commentAuthors.value[comment.authorId] = authorRes.data.username;
        } catch (e) {
          commentAuthors.value[comment.authorId] = comment.authorId;
        }
      }
    }
  } catch (e) {
    console.error('Failed to fetch comments', e);
  } finally {
    loadingComments.value = false;
  }
};

const submitComment = async () => {
  if (!newComment.value.trim()) return;
  try {
    const response = await api.post(`/feed/posts/${props.post.id}/comments`, {
      content: newComment.value.trim()
    });
    comments.value.push(response.data);
    // Fetch author name for new comment
    if (!commentAuthors.value[response.data.authorId]) {
      try {
        const authorRes = await api.get(`/users/profile/${response.data.authorId}`);
        commentAuthors.value[response.data.authorId] = authorRes.data.username;
      } catch (e) {
        commentAuthors.value[response.data.authorId] = response.data.authorId;
      }
    }
    newComment.value = '';
  } catch (e) {
    console.error('Failed to submit comment', e);
  }
};

const formatCommentDate = (dateString) => {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  if (minutes < 1) return 'Just now';
  if (minutes < 60) return `${minutes}m`;
  if (hours < 24) return `${hours}h`;
  return date.toLocaleDateString();
};
</script>

<template>
  <div class="bg-white p-4 rounded-lg shadow mb-4">
    <div class="flex items-center mb-4">
      <div class="w-10 h-10 rounded-full overflow-hidden flex items-center justify-center mr-3" :class="{ 'bg-gray-300': !authorProfilePic }">
        <img v-if="authorProfilePic" :src="authorProfilePic" class="w-full h-full object-cover" />
        <span v-else class="text-gray-600 font-bold">{{ displayName?.charAt(0)?.toUpperCase() || 'U' }}</span>
      </div>
      <div>
        <h3 class="font-bold text-gray-800">{{ displayName }}</h3>
        <p class="text-xs text-gray-500">{{ formattedDate }}</p>
      </div>
    </div>
    
    <p class="text-gray-800 mb-4">{{ post.content }}</p>
    
    <div v-if="post.imageUrl && !imageError" class="mb-4">
      <img :src="post.imageUrl" alt="Post content" class="rounded-lg max-h-96 w-full object-cover" @error="imageError = true">
    </div>

    <!-- Like/Comment counts -->
    <div class="flex items-center gap-4 text-sm text-gray-500 mb-2">
      <span v-if="likesCount > 0">👍 {{ likesCount }}</span>
      <span v-if="post.commentsCount > 0 || comments.length > 0">💬 {{ comments.length || post.commentsCount }}</span>
    </div>
    
    <div class="flex items-center justify-between text-gray-500 text-sm border-t pt-3">
      <button 
        @click="toggleLike"
        :class="['flex items-center gap-1 px-4 py-2 rounded hover:bg-gray-100 transition', liked ? 'text-blue-600 font-semibold' : '']"
      >
        <span>{{ liked ? '👍' : '👍' }}</span>
        <span>{{ liked ? 'Liked' : 'Like' }}</span>
      </button>
      <button 
        @click="toggleComments"
        class="flex items-center gap-1 px-4 py-2 rounded hover:bg-gray-100 transition"
      >
        <span>💬</span>
        <span>Comment</span>
      </button>
      <button class="flex items-center gap-1 px-4 py-2 rounded hover:bg-gray-100 transition">
        <span>↗️</span>
        <span>Share</span>
      </button>
    </div>

    <!-- Comments Section -->
    <div v-if="showComments" class="mt-4 border-t pt-4">
      <!-- Comment Input -->
      <div class="flex gap-2 mb-4">
        <input 
          v-model="newComment"
          @keyup.enter="submitComment"
          type="text" 
          placeholder="Write a comment..."
          class="flex-1 px-3 py-2 bg-gray-100 rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button 
          @click="submitComment"
          :disabled="!newComment.trim()"
          class="px-4 py-2 bg-blue-600 text-white rounded-full text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-blue-700 transition"
        >
          Post
        </button>
      </div>

      <!-- Comments List -->
      <div v-if="loadingComments" class="text-center text-gray-500 text-sm py-2">
        Loading comments...
      </div>
      <div v-else-if="comments.length === 0" class="text-center text-gray-500 text-sm py-2">
        No comments yet. Be the first to comment!
      </div>
      <div v-else class="space-y-3">
        <div v-for="comment in comments" :key="comment.id" class="flex gap-2">
          <div class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center flex-shrink-0">
            <span class="text-gray-600 font-bold text-xs">{{ (commentAuthors[comment.authorId] || comment.authorId)?.charAt(0)?.toUpperCase() }}</span>
          </div>
          <div class="bg-gray-100 rounded-2xl px-3 py-2 flex-1">
            <p class="font-semibold text-sm text-gray-800">{{ commentAuthors[comment.authorId] || comment.authorId }}</p>
            <p class="text-sm text-gray-700">{{ comment.content }}</p>
            <p class="text-xs text-gray-500 mt-1">{{ formatCommentDate(comment.createdAt) }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
