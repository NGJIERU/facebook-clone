<template>
  <div class="bg-white rounded-lg shadow p-4 mb-6">
    <div class="flex gap-4 overflow-x-auto pb-2">
      <!-- Create Story Button -->
      <div 
        @click="showCreateModal = true"
        class="flex-shrink-0 w-28 h-44 rounded-xl bg-gradient-to-b from-blue-500 to-blue-600 flex flex-col items-center justify-center cursor-pointer hover:opacity-90 transition"
      >
        <div class="w-10 h-10 bg-white rounded-full flex items-center justify-center mb-2">
          <span class="text-blue-600 text-2xl">+</span>
        </div>
        <span class="text-white text-sm font-medium">Create Story</span>
      </div>

      <!-- Stories -->
      <div 
        v-for="(userStories, index) in groupedStories" 
        :key="userStories.authorId"
        @click="openStoryViewer(index)"
        class="flex-shrink-0 w-28 h-44 rounded-xl overflow-hidden cursor-pointer relative group"
      >
        <img 
          v-if="userStories.stories[0].imageUrl" 
          :src="userStories.stories[0].imageUrl" 
          class="w-full h-full object-cover"
        />
        <div v-else class="w-full h-full bg-gradient-to-b from-purple-500 to-pink-500 flex items-center justify-center p-2">
          <p class="text-white text-xs text-center line-clamp-4">{{ userStories.stories[0].text }}</p>
        </div>
        
        <!-- Overlay -->
        <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
        
        <!-- Author Avatar -->
        <div class="absolute top-2 left-2 w-10 h-10 rounded-full border-4 border-blue-500 overflow-hidden bg-white">
          <img v-if="userStories.authorPic" :src="userStories.authorPic" class="w-full h-full object-cover" />
          <div v-else class="w-full h-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold">
            {{ userStories.authorName?.charAt(0)?.toUpperCase() }}
          </div>
        </div>
        
        <!-- Author Name -->
        <div class="absolute bottom-2 left-2 right-2">
          <p class="text-white text-xs font-medium truncate">{{ userStories.authorName }}</p>
        </div>

        <!-- Story count badge -->
        <div v-if="userStories.stories.length > 1" class="absolute top-2 right-2 bg-blue-600 text-white text-xs px-2 py-0.5 rounded-full">
          {{ userStories.stories.length }}
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="!loading && groupedStories.length === 0" class="flex items-center justify-center text-gray-400 text-sm px-4">
        No stories yet. Be the first to share!
      </div>
    </div>

    <!-- Create Story Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click="showCreateModal = false">
      <div class="bg-white rounded-lg p-6 w-full max-w-md mx-4" @click.stop>
        <h3 class="text-xl font-bold mb-4">Create Story</h3>
        
        <div class="space-y-4">
          <!-- Image Upload -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Add Photo</label>
            <input type="file" ref="fileInput" @change="handleFileSelect" accept="image/*" class="hidden" />
            <div 
              v-if="!previewUrl"
              @click="$refs.fileInput.click()"
              class="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center cursor-pointer hover:border-blue-500 transition"
            >
              <span class="text-gray-400">Click to upload an image</span>
            </div>
            <div v-else class="relative">
              <img :src="previewUrl" class="w-full h-48 object-cover rounded-lg" />
              <button @click="clearImage" class="absolute top-2 right-2 bg-red-500 text-white p-1 rounded-full">✕</button>
            </div>
          </div>

          <!-- Text -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Or add text</label>
            <textarea 
              v-model="storyText"
              placeholder="What's on your mind?"
              class="w-full border rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
              rows="3"
            ></textarea>
          </div>
        </div>

        <div class="mt-6 flex justify-end gap-2">
          <button @click="showCreateModal = false" class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded">Cancel</button>
          <button 
            @click="createStory"
            :disabled="creating || (!selectedFile && !storyText.trim())"
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            {{ creating ? 'Posting...' : 'Share Story' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Story Viewer Modal -->
    <div v-if="viewingStory" class="fixed inset-0 bg-black z-50 flex items-center justify-center">
      <!-- Close button -->
      <button @click="closeViewer" class="absolute top-4 right-4 text-white text-3xl z-10">✕</button>
      
      <!-- Progress bars -->
      <div class="absolute top-4 left-4 right-16 flex gap-1">
        <div 
          v-for="(story, idx) in currentUserStories" 
          :key="story.id"
          class="flex-1 h-1 bg-white/30 rounded-full overflow-hidden"
        >
          <div 
            :class="[
              'h-full bg-white transition-all',
              idx < currentStoryIndex ? 'w-full' : idx === currentStoryIndex ? 'w-full animate-progress' : 'w-0'
            ]"
          ></div>
        </div>
      </div>

      <!-- Author info -->
      <div class="absolute top-10 left-4 flex items-center gap-3">
        <div class="w-10 h-10 rounded-full overflow-hidden bg-white">
          <img v-if="currentAuthorPic" :src="currentAuthorPic" class="w-full h-full object-cover" />
          <div v-else class="w-full h-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold">
            {{ currentAuthorName?.charAt(0)?.toUpperCase() }}
          </div>
        </div>
        <div>
          <p class="text-white font-medium">{{ currentAuthorName }}</p>
          <p class="text-white/70 text-xs">{{ formatTime(currentStory?.createdAt) }}</p>
        </div>
      </div>

      <!-- Story Content -->
      <div class="w-full max-w-lg h-[80vh] flex flex-col items-center justify-center relative">
        <img 
          v-if="currentStory?.imageUrl" 
          :src="currentStory.imageUrl" 
          class="max-w-full max-h-full object-contain"
        />
        <div v-else class="bg-gradient-to-b from-purple-500 to-pink-500 w-full h-full flex items-center justify-center p-8">
          <p class="text-white text-2xl text-center">{{ currentStory?.text }}</p>
        </div>
        
        <!-- Show text caption if story has both image and text -->
        <div 
          v-if="currentStory?.imageUrl && currentStory?.text" 
          class="absolute bottom-4 left-4 right-4 bg-black/60 rounded-lg p-3"
        >
          <p class="text-white text-center">{{ currentStory.text }}</p>
        </div>
      </div>

      <!-- Navigation -->
      <button 
        v-if="canGoPrev"
        @click="prevStory"
        class="absolute left-4 top-1/2 -translate-y-1/2 text-white text-4xl p-2"
      >‹</button>
      <button 
        v-if="canGoNext"
        @click="nextStory"
        class="absolute right-4 top-1/2 -translate-y-1/2 text-white text-4xl p-2"
      >›</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useAuthStore } from '../stores/auth';
import api, { uploadMedia } from '../utils/api';

const authStore = useAuthStore();

const stories = ref([]);
const loading = ref(true);
const showCreateModal = ref(false);
const selectedFile = ref(null);
const previewUrl = ref('');
const storyText = ref('');
const creating = ref(false);
const fileInput = ref(null);

const viewingStory = ref(false);
const currentUserIndex = ref(0);
const currentStoryIndex = ref(0);
let storyTimer = null;

const STORY_DURATION = 5000; // 5 seconds per story

const startStoryTimer = () => {
  clearStoryTimer();
  storyTimer = setTimeout(() => {
    nextStory();
  }, STORY_DURATION);
};

const clearStoryTimer = () => {
  if (storyTimer) {
    clearTimeout(storyTimer);
    storyTimer = null;
  }
};

// Watch for story changes to restart timer
watch([currentUserIndex, currentStoryIndex, viewingStory], ([, , isViewing]) => {
  if (isViewing) {
    startStoryTimer();
  } else {
    clearStoryTimer();
  }
});

onMounted(async () => {
  await fetchStories();
});

onUnmounted(() => {
  clearStoryTimer();
});

const fetchStories = async () => {
  loading.value = true;
  try {
    const response = await api.get('/feed/stories');
    stories.value = response.data;
  } catch (e) {
    console.error('Failed to fetch stories', e);
  } finally {
    loading.value = false;
  }
};

const groupedStories = computed(() => {
  const groups = {};
  stories.value.forEach(story => {
    if (!groups[story.authorId]) {
      groups[story.authorId] = {
        authorId: story.authorId,
        authorName: null,
        authorPic: null,
        stories: []
      };
    }
    groups[story.authorId].stories.push(story);
  });
  
  // Fetch author info for each group
  Object.values(groups).forEach(async (group) => {
    try {
      const response = await api.get(`/users/profile/${group.authorId}`);
      group.authorName = response.data.username;
      group.authorPic = response.data.profilePicUrl;
    } catch (e) {
      group.authorName = 'Unknown';
    }
  });
  
  return Object.values(groups);
});

const currentUserStories = computed(() => {
  return groupedStories.value[currentUserIndex.value]?.stories || [];
});

const currentStory = computed(() => {
  return currentUserStories.value[currentStoryIndex.value];
});

const currentAuthorName = computed(() => {
  return groupedStories.value[currentUserIndex.value]?.authorName;
});

const currentAuthorPic = computed(() => {
  return groupedStories.value[currentUserIndex.value]?.authorPic;
});

const canGoPrev = computed(() => {
  return currentStoryIndex.value > 0 || currentUserIndex.value > 0;
});

const canGoNext = computed(() => {
  return currentStoryIndex.value < currentUserStories.value.length - 1 || 
         currentUserIndex.value < groupedStories.value.length - 1;
});

const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (file) {
    selectedFile.value = file;
    previewUrl.value = URL.createObjectURL(file);
  }
};

const clearImage = () => {
  selectedFile.value = null;
  previewUrl.value = '';
  if (fileInput.value) fileInput.value.value = '';
};

const createStory = async () => {
  if (!selectedFile.value && !storyText.value.trim()) return;
  
  creating.value = true;
  try {
    let imageUrl = null;
    
    if (selectedFile.value) {
      const uploadRes = await uploadMedia(selectedFile.value, authStore.user.id);
      imageUrl = uploadRes.data.url;
    }
    
    await api.post('/feed/stories', {
      imageUrl,
      text: storyText.value.trim() || null
    });
    
    showCreateModal.value = false;
    clearImage();
    storyText.value = '';
    await fetchStories();
  } catch (e) {
    console.error('Failed to create story', e);
    alert('Failed to create story');
  } finally {
    creating.value = false;
  }
};

const openStoryViewer = (userIndex) => {
  currentUserIndex.value = userIndex;
  currentStoryIndex.value = 0;
  viewingStory.value = true;
  
  // Mark as viewed
  if (currentStory.value) {
    api.post(`/feed/stories/${currentStory.value.id}/view`).catch(() => {});
  }
};

const closeViewer = () => {
  viewingStory.value = false;
};

const prevStory = () => {
  if (currentStoryIndex.value > 0) {
    currentStoryIndex.value--;
  } else if (currentUserIndex.value > 0) {
    currentUserIndex.value--;
    currentStoryIndex.value = groupedStories.value[currentUserIndex.value].stories.length - 1;
  }
};

const nextStory = () => {
  if (currentStoryIndex.value < currentUserStories.value.length - 1) {
    currentStoryIndex.value++;
  } else if (currentUserIndex.value < groupedStories.value.length - 1) {
    currentUserIndex.value++;
    currentStoryIndex.value = 0;
  } else {
    closeViewer();
  }
};

const formatTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  
  if (minutes < 1) return 'Just now';
  if (minutes < 60) return `${minutes}m ago`;
  if (hours < 24) return `${hours}h ago`;
  return date.toLocaleDateString();
};
</script>

<style scoped>
@keyframes progress {
  from { width: 0; }
  to { width: 100%; }
}
.animate-progress {
  animation: progress 5s linear forwards;
}
</style>
