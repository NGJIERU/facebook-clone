<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Navbar -->
    <nav class="bg-white shadow sticky top-0 z-50">
      <div class="container mx-auto px-4 h-16 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-blue-600 cursor-pointer" @click="router.push('/')">Facebook</h1>
        
        <div class="flex items-center gap-6">
          <button @click="router.push('/')" class="text-gray-600 hover:text-blue-600 font-medium">Home</button>
          <button @click="router.push('/friends')" class="text-gray-600 hover:text-blue-600 font-medium">Friends</button>
          <button @click="handleLogout" class="text-gray-500 hover:text-red-600 transition font-medium text-sm">Logout</button>
        </div>
      </div>
    </nav>

    <!-- Profile Content -->
    <div class="profile-container">
      <!-- Loading State -->
      <div v-if="loading" class="text-center py-20">
        <p class="text-gray-600">Loading profile...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class=" bg-red-50 border border-red-200 rounded-lg p-6 my-8">
        <p class="text-red-600">{{ error }}</p>
        <button @click="router.push('/')" class="mt-4 px-4 py-2 bg-blue-600 text-white rounded">Go to Feed</button>
      </div>

      <!-- Profile Content -->
      <div v-else>
        <!-- Profile Header -->
        <div v-if="profile" class="profile-header">
          <div class="cover-photo" :style="{ backgroundImage: profile.coverPicUrl ? `url(${profile.coverPicUrl})` : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }"></div>
          
          <div class="profile-info">
            <div class="profile-picture" @click="isOwnProfile && triggerProfilePicUpload()">
              <img v-if="profile.profilePicUrl" :src="profile.profilePicUrl" class="w-full h-full object-cover" />
              <div v-else class="bg-blue-500 w-full h-full flex items-center justify-center text-white text-6xl font-bold">
                {{ profile.username?.charAt(0).toUpperCase() }}
              </div>
              <div v-if="isOwnProfile" class="profile-pic-overlay">
                <span>📷</span>
              </div>
              <input type="file" ref="profilePicInput" @change="uploadProfilePic" accept="image/*" class="hidden" />
            </div>
            
            <div class="profile-details">
              <h1>{{ profile.username }}</h1>
              <p class="bio">{{ profile.bio || 'No bio yet' }}</p>
            </div>
            
            <button v-if="isOwnProfile" @click="showEditModal = true" class="edit-button">
              Edit Profile
            </button>
          </div>
        </div>

        <!-- Posts Feed -->
        <div class="posts-section">
          <h2>Posts</h2>
          <div v-if="posts.length === 0" class="no-posts">
            <p>{{ isOwnProfile ? 'You haven\'t posted anything yet' : 'No posts yet' }}</p>
          </div>
          <div v-else class="posts-list">
            <div v-for="post in posts" :key="post.id" class="post-card">
              <div class="post-header">
                <div class="post-avatar w-10 h-10 rounded-full overflow-hidden flex items-center justify-center text-white font-bold" :class="{ 'bg-blue-500': !profile.profilePicUrl }">
                  <img v-if="profile.profilePicUrl" :src="profile.profilePicUrl" class="w-full h-full object-cover" />
                  <span v-else>{{ profile.username?.charAt(0).toUpperCase() }}</span>
                </div>
                <div>
                  <h3>{{ profile.username }}</h3>
                  <p class="post-time">{{ formatDate(post.createdAt) }}</p>
                </div>
              </div>
              <p class="post-content">{{ post.content }}</p>
              <img v-if="post.imageUrl" :src="post.imageUrl" :alt="'Post image'" class="post-image">
              <div class="post-actions">
                <span>👍 {{ post.likesCount }} Likes</span>
                <span>💬 {{ post.commentsCount }} Comments</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Edit Profile Modal -->
      <div v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
        <div class="modal-content" @click.stop>
          <h2>Edit Profile</h2>
          <form @submit.prevent="saveProfile">
            <div class="form-group">
              <label>Username</label>
              <input v-model="editForm.username" type="text" required>
            </div>
            <div class="form-group">
              <label>Bio</label>
              <textarea v-model="editForm.bio" rows="4"></textarea>
            </div>
            <div class="form-actions">
              <button type="button" @click="showEditModal = false" class="cancel-btn">Cancel</button>
              <button type="submit" class="save-btn">Save Changes</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import api from '../utils/api';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const profile = ref(null);
const posts = ref([]);
const loading = ref(true);
const error = ref(null);
const showEditModal = ref(false);
const editForm = ref({
  username: '',
  bio: ''
});
const profilePicInput = ref(null);
const uploadingPic = ref(false);

const isOwnProfile = computed(() => {
  return profile.value && authStore.user && profile.value.id === authStore.user.id;
});

const fetchProfile = async () => {
  try {
    loading.value = true;
    error.value = null;
    
    // Get userId from route or auth store
    let userId = route.params.userId;
    
    // If no userId in route, use current user
    if (!userId) {
      // Try to get from auth store
      if (authStore.user?.id) {
        userId = authStore.user.id;
      } else {
        // Fetch current user profile
        console.log('Fetching current user profile from /users/profile');
        const response = await api.get('/users/profile');
        profile.value = response.data;
        editForm.value = {
          username: response.data.username,
          bio: response.data.bio || ''
        };
        loading.value = false;
        return;
      }
    }
    
    console.log('Fetching profile for userId:', userId);
    const response = await api.get(`/users/profile/${userId}`);
    profile.value = response.data;
    editForm.value = {
      username: response.data.username,
      bio: response.data.bio || ''
    };
  } catch (err) {
    console.error('Failed to fetch profile:', err);
    error.value = err.response?.data?.message || 'Failed to load profile. Please try again.';
  } finally{
    loading.value = false;
  }
};

const fetchPosts = async () => {
  try {
    let userId = route.params.userId;
    
    if (!userId) {
      userId = authStore.user?.id || profile.value?.id;
    }
    
    if (!userId) {
      console.log('No userId available for fetching posts');
      return;
    }
    
    console.log('Fetching posts for userId:', userId);
    const response = await api.get(`/feed/user/${userId}`);
    posts.value = response.data;
  } catch (err) {
    console.error('Failed to fetch posts:', err);
  }
};

const saveProfile = async () => {
  try {
    const response = await api.put('/users/profile', editForm.value);
    profile.value = response.data;
    authStore.user = response.data; // Update auth store
    showEditModal.value = false;
  } catch (err) {
    console.error('Failed to update profile:', err);
    alert('Failed to update profile');
  }
};

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return 'Just now';
  if (minutes < 60) return `${minutes}m ago`;
  if (hours < 24) return `${hours}h ago`;
  if (days < 7) return `${days}d ago`;
  return date.toLocaleDateString();
};

const triggerProfilePicUpload = () => {
  profilePicInput.value?.click();
};

const uploadProfilePic = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  uploadingPic.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', authStore.user.id);
    
    const uploadRes = await api.post('/media/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    
    const imageUrl = uploadRes.data.url;
    
    // Update profile with new picture URL
    const updateRes = await api.put('/users/profile', {
      ...editForm.value,
      profilePicUrl: imageUrl
    });
    
    profile.value = updateRes.data;
    authStore.user = updateRes.data;
  } catch (err) {
    console.error('Failed to upload profile picture:', err);
    alert('Failed to upload profile picture');
  } finally {
    uploadingPic.value = false;
  }
};

onMounted(async () => {
  await fetchProfile();
  await fetchPosts();
});
</script>

<style scoped>
.profile-container {
  max-width: 900px;
  margin: 0 auto;
}

.profile-header {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cover-photo {
  height: 250px;
  background-size: cover;
  background-position: center;
}

.profile-info {
  padding: 0 24px 24px;
  position: relative;
}

.profile-picture {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  border: 5px solid white;
  position: relative;
  top: -75px;
  margin-bottom: -75px;
  background: #f0f2f5;
}

.profile-picture img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-pic-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  text-align: center;
  padding: 8px;
  opacity: 0;
  transition: opacity 0.2s;
  cursor: pointer;
}

.profile-picture:hover .profile-pic-overlay {
  opacity: 1;
}

.profile-details {
  padding: 16px 0;
}

.profile-details h1 {
  font-size: 32px;
  margin: 0 0 8px;
  color: #1c1e21;
}

.bio {
  color: #65676b;
  font-size: 16px;
  margin: 0;
}

.edit-button {
  position: absolute;
  top: 16px;
  right: 24px;
  padding: 8px 16px;
  background: #1877f2;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

.edit-button:hover {
  background: #166fe5;
}

.posts-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.posts-section h2 {
  margin: 0 0 20px;
  font-size: 24px;
  color: #1c1e21;
}

.no-posts {
  text-align: center;
  padding: 40px 20px;
  color: #65676b;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  border: 1px solid #e4e6eb;
  border-radius: 8px;
  padding: 16px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.post-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.post-time {
  margin: 0;
  font-size: 13px;
  color: #65676b;
}

.post-content {
  margin: 0 0 12px;
  font-size: 15px;
  line-height: 1.5;
  color: #1c1e21;
}

.post-image {
  width: 100%;
  border-radius: 8px;
  margin-bottom: 12px;
}

.post-actions {
  display: flex;
  gap: 20px;
  padding-top: 12px;
  border-top: 1px solid #e4e6eb;
  font-size: 14px;
  color: #65676b;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 24px;
  width: 90%;
  max-width: 500px;
}

.modal-content h2 {
  margin: 0 0 20px;
  font-size: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #1c1e21;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ccd0d5;
  border-radius: 6px;
  font-size: 15px;
  font-family: inherit;
}

.form-group textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}

.cancel-btn,
.save-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.cancel-btn {
  background: #e4e6eb;
  color: #1c1e21;
}

.cancel-btn:hover {
  background: #d8dadf;
}

.save-btn {
  background: #1877f2;
  color: white;
}

.save-btn:hover {
  background: #166fe5;
}
</style>
