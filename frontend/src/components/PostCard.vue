<script setup>
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import api from '../utils/api';
import ImageLightbox from './ImageLightbox.vue';

const router = useRouter();
const authStore = useAuthStore();
const emit = defineEmits(['post-deleted', 'post-shared']);

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
const sharesCount = ref(props.post.sharesCount || 0);
const showComments = ref(false);
const comments = ref([]);
const newComment = ref('');
const loadingComments = ref(false);
const commentAuthors = ref({});
const isOwnPost = computed(() => authStore.user?.id === props.post.authorId);
const showShareModal = ref(false);
const shareComment = ref('');
const sharing = ref(false);
const originalAuthorName = ref(null);
const originalAuthorPic = ref(null);
const showLightbox = ref(false);

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

  // Fetch original author profile for shared posts
  if (props.post.originalAuthorId) {
    try {
      const response = await api.get(`/users/profile/${props.post.originalAuthorId}`);
      originalAuthorName.value = response.data.username;
      originalAuthorPic.value = response.data.profilePicUrl;
    } catch (e) {
      console.warn('Failed to fetch original author profile', e);
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

const deletePost = async () => {
  if (!confirm('Are you sure you want to delete this post?')) return;
  try {
    await api.delete(`/feed/posts/${props.post.id}`);
    emit('post-deleted', props.post.id);
  } catch (e) {
    console.error('Failed to delete post', e);
    alert('Failed to delete post');
  }
};

const goToAuthorProfile = () => {
  router.push(`/profile/${props.post.authorId}`);
};

const openShareModal = () => {
  showShareModal.value = true;
  shareComment.value = '';
};

const closeShareModal = () => {
  showShareModal.value = false;
  shareComment.value = '';
};

const sharePost = async () => {
  sharing.value = true;
  try {
    await api.post(`/feed/posts/${props.post.id}/share`, { comment: shareComment.value });
    sharesCount.value++;
    closeShareModal();
    emit('post-shared');
  } catch (e) {
    console.error('Failed to share post', e);
    alert('Failed to share post');
  } finally {
    sharing.value = false;
  }
};

const isSharedPost = computed(() => !!props.post.originalPostId);
</script>

<template>
  <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow mb-4">
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center">
        <div 
          @click="goToAuthorProfile" 
          class="w-10 h-10 rounded-full overflow-hidden flex items-center justify-center mr-3 cursor-pointer hover:opacity-80" 
          :class="{ 'bg-gray-300': !authorProfilePic }"
        >
          <img v-if="authorProfilePic" :src="authorProfilePic" class="w-full h-full object-cover" />
          <span v-else class="text-gray-600 font-bold">{{ displayName?.charAt(0)?.toUpperCase() || 'U' }}</span>
        </div>
        <div>
          <h3 @click="goToAuthorProfile" class="font-bold text-gray-800 dark:text-gray-100 cursor-pointer hover:underline">{{ displayName }}</h3>
          <p class="text-xs text-gray-500 dark:text-gray-400">{{ formattedDate }}</p>
        </div>
      </div>
      <button 
        v-if="isOwnPost" 
        @click="deletePost" 
        class="text-gray-400 hover:text-red-500 p-2 rounded-full hover:bg-gray-100 transition"
        title="Delete post"
      >
        🗑️
      </button>
    </div>

    <!-- Shared post indicator -->
    <div v-if="isSharedPost" class="text-xs text-gray-500 mb-2 flex items-center gap-1">
      <span>↗️</span>
      <span>Shared a post</span>
    </div>
    
    <p v-if="post.content" class="text-gray-800 dark:text-gray-200 mb-4">{{ post.content }}</p>

    <!-- Original Post (for shared posts) -->
    <div v-if="isSharedPost" class="border dark:border-gray-600 rounded-lg p-4 mb-4 bg-gray-50 dark:bg-gray-700">
      <div class="flex items-center gap-2 mb-2">
        <div class="w-8 h-8 rounded-full overflow-hidden bg-gray-300 flex items-center justify-center">
          <img v-if="originalAuthorPic" :src="originalAuthorPic" class="w-full h-full object-cover" />
          <span v-else class="text-gray-600 font-bold text-sm">{{ originalAuthorName?.charAt(0)?.toUpperCase() || 'U' }}</span>
        </div>
        <span class="font-medium text-sm text-gray-700">{{ originalAuthorName || 'Unknown User' }}</span>
      </div>
      <div v-if="post.imageUrl && !imageError">
        <img 
          :src="post.imageUrl" 
          alt="Shared content" 
          class="rounded-lg max-h-64 w-full object-cover cursor-pointer hover:opacity-90 transition" 
          @click="showLightbox = true"
          @error="imageError = true"
        >
      </div>
    </div>
    
    <!-- Regular post image -->
    <div v-else-if="post.imageUrl && !imageError" class="mb-4">
      <img 
        :src="post.imageUrl" 
        alt="Post content" 
        class="rounded-lg max-h-96 w-full object-cover cursor-pointer hover:opacity-90 transition" 
        @click="showLightbox = true"
        @error="imageError = true"
      >
    </div>

    <!-- Image Lightbox -->
    <ImageLightbox 
      :is-open="showLightbox" 
      :image-url="post.imageUrl" 
      :caption="post.content"
      @close="showLightbox = false"
    />

    <!-- Like/Comment counts -->
    <div class="flex items-center gap-4 text-sm text-gray-500 dark:text-gray-400 mb-2">
      <span v-if="likesCount > 0">👍 {{ likesCount }}</span>
      <span v-if="post.commentsCount > 0 || comments.length > 0">💬 {{ comments.length || post.commentsCount }}</span>
    </div>
    
    <div class="flex items-center justify-between text-gray-500 dark:text-gray-400 text-sm border-t dark:border-gray-600 pt-3">
      <button 
        @click="toggleLike"
        :class="['flex items-center gap-1 px-4 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition', liked ? 'text-blue-600 font-semibold' : '']"
      >
        <span>{{ liked ? '👍' : '👍' }}</span>
        <span>{{ liked ? 'Liked' : 'Like' }}</span>
      </button>
      <button 
        @click="toggleComments"
        class="flex items-center gap-1 px-4 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition"
      >
        <span>💬</span>
        <span>Comment</span>
      </button>
      <button 
        @click="openShareModal"
        class="flex items-center gap-1 px-4 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 transition"
      >
        <span>↗️</span>
        <span>Share{{ sharesCount > 0 ? ` (${sharesCount})` : '' }}</span>
      </button>
    </div>

    <!-- Share Modal -->
    <div v-if="showShareModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click="closeShareModal">
      <div class="bg-white rounded-lg p-6 w-full max-w-lg mx-4" @click.stop>
        <h3 class="text-xl font-bold mb-4">Share this post</h3>
        
        <!-- Original Post Preview -->
        <div class="bg-gray-50 rounded-lg p-4 mb-4 border">
          <div class="flex items-center gap-2 mb-2">
            <div class="w-8 h-8 rounded-full overflow-hidden bg-gray-300 flex items-center justify-center">
              <img v-if="authorProfilePic" :src="authorProfilePic" class="w-full h-full object-cover" />
              <span v-else class="text-gray-600 font-bold text-sm">{{ displayName?.charAt(0)?.toUpperCase() }}</span>
            </div>
            <span class="font-medium text-sm">{{ displayName }}</span>
          </div>
          <p class="text-gray-700 text-sm">{{ post.content }}</p>
          <img v-if="post.imageUrl" :src="post.imageUrl" class="mt-2 rounded max-h-32 object-cover" />
        </div>

        <!-- Share Comment -->
        <textarea 
          v-model="shareComment"
          placeholder="Add a comment (optional)..."
          class="w-full border rounded-lg p-3 mb-4 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          rows="3"
        ></textarea>

        <div class="flex justify-end gap-2">
          <button @click="closeShareModal" class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded">Cancel</button>
          <button 
            @click="sharePost" 
            :disabled="sharing"
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            {{ sharing ? 'Sharing...' : 'Share Now' }}
          </button>
        </div>
      </div>
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
