<template>
  <div class="min-h-screen bg-gray-100 dark:bg-gray-900">
    <!-- Profile Content -->
    <div class="profile-container">
      <!-- Loading State -->
      <div v-if="loading" class="text-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p class="text-gray-600 dark:text-gray-400">Loading profile...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6 my-8 text-center">
        <div class="text-red-500 text-4xl mb-2">⚠️</div>
        <h3 class="text-red-700 font-semibold mb-1">Unable to load profile</h3>
        <p class="text-red-600 text-sm mb-3">{{ error }}</p>
        <button @click="router.push('/')" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Go to Feed</button>
      </div>

      <!-- Profile Content -->
      <div v-else>
        <!-- Profile Header -->
        <div v-if="profile" class="profile-header bg-white dark:bg-gray-800">
          <div class="cover-photo" :style="{ backgroundImage: profile.coverPicUrl ? `url(${profile.coverPicUrl})` : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }"></div>
          
          <div class="profile-info">
            <div class="profile-picture border-white dark:border-gray-800 bg-gray-100 dark:bg-gray-700" @click="isOwnProfile && triggerProfilePicUpload()">
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
              <h1 class="text-gray-900 dark:text-white">{{ profile.username }}</h1>
              <p class="bio text-gray-500 dark:text-gray-400">{{ profile.bio || 'No bio yet' }}</p>
            </div>
            
            <button v-if="isOwnProfile" @click="showEditModal = true" class="edit-button absolute top-4 right-6 bg-blue-600 text-white hover:bg-blue-700 px-4 py-2 rounded-md font-semibold transition-colors">
              Edit Profile
            </button>
            <button v-if="isOwnProfile" @click="showSettingsModal = true" class="settings-button absolute top-16 right-6 bg-gray-200 dark:bg-gray-700 text-gray-900 dark:text-gray-100 hover:bg-gray-300 dark:hover:bg-gray-600 px-4 py-2 rounded-md font-semibold transition-colors">
              ⚙️ Settings
            </button>
          </div>
        </div>

        <!-- Settings Section for own profile -->
        <div v-if="isOwnProfile" class="settings-section bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
          <h3 class="text-gray-900 dark:text-white">Quick Settings</h3>
          <div class="settings-grid">
            <div class="setting-item bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-gray-600" @click="router.push('/saved')">
              <span class="setting-icon">🔖</span>
              <span>Saved Posts</span>
            </div>
            <div class="setting-item bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-gray-600" @click="router.push('/friends')">
              <span class="setting-icon">👥</span>
              <span>Friends</span>
            </div>
            <div class="setting-item bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-gray-600" @click="router.push('/groups')">
              <span class="setting-icon">👨‍👩‍👧‍👦</span>
              <span>Groups</span>
            </div>
            <div class="setting-item bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-gray-600" @click="router.push('/events')">
              <span class="setting-icon">📅</span>
              <span>Events</span>
            </div>
          </div>
        </div>

        <!-- Posts Feed -->
        <div class="posts-section bg-white dark:bg-gray-800">
          <h2 class="text-gray-900 dark:text-white">Posts</h2>
          <div v-if="posts.length === 0" class="no-posts text-gray-500 dark:text-gray-400">
            <p>{{ isOwnProfile ? 'You haven\'t posted anything yet' : 'No posts yet' }}</p>
          </div>
          <div v-else class="posts-list">
            <div v-for="post in posts" :key="post.id" class="post-card bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600">
              <div class="post-header">
                <div class="post-avatar w-10 h-10 rounded-full overflow-hidden flex items-center justify-center text-white font-bold" :class="{ 'bg-blue-500': !profile.profilePicUrl }">
                  <img v-if="profile.profilePicUrl" :src="profile.profilePicUrl" class="w-full h-full object-cover" />
                  <span v-else>{{ profile.username?.charAt(0).toUpperCase() }}</span>
                </div>
                <div>
                  <h3 class="text-gray-900 dark:text-white">{{ profile.username }}</h3>
                  <p class="post-time text-gray-500 dark:text-gray-400">{{ formatDate(post.createdAt) }}</p>
                </div>
              </div>
              <p class="post-content text-gray-900 dark:text-gray-200">{{ post.content }}</p>
              <img v-if="post.imageUrl" :src="post.imageUrl" :alt="'Post image'" class="post-image">
              <div class="post-actions border-t border-gray-200 dark:border-gray-600 text-gray-500 dark:text-gray-400">
                <span>👍 {{ post.likesCount }} Likes</span>
                <span>💬 {{ post.commentsCount }} Comments</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Edit Profile Modal -->
      <div v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
        <div class="modal-content dark:bg-gray-800" @click.stop>
          <h2 class="dark:text-white">Edit Profile</h2>
          <form @submit.prevent="saveProfile">
            <!-- Profile Picture -->
            <div class="form-group">
              <label class="text-gray-900 dark:text-gray-100">Profile Picture</label>
              <div class="flex items-center gap-4">
                <div class="w-16 h-16 rounded-full overflow-hidden bg-blue-500 flex items-center justify-center">
                  <img v-if="editForm.profilePicUrl" :src="editForm.profilePicUrl" class="w-full h-full object-cover" />
                  <span v-else class="text-white text-2xl font-bold">{{ editForm.username?.charAt(0)?.toUpperCase() }}</span>
                </div>
                <input type="file" ref="modalProfilePicInput" @change="handleModalProfilePic" accept="image/*" class="hidden" />
                <button type="button" @click="$refs.modalProfilePicInput.click()" class="px-3 py-1 bg-gray-200 dark:bg-gray-600 rounded text-sm hover:bg-gray-300 dark:hover:bg-gray-500 text-gray-800 dark:text-gray-100 font-medium">
                  {{ uploadingModalPic ? 'Uploading...' : 'Change Photo' }}
                </button>
              </div>
            </div>
            <!-- Cover Picture -->
            <div class="form-group">
              <label class="text-gray-900 dark:text-gray-100">Cover Photo</label>
              <div class="w-full h-20 rounded overflow-hidden bg-gradient-to-r from-blue-500 to-purple-500">
                <img v-if="editForm.coverPicUrl" :src="editForm.coverPicUrl" class="w-full h-full object-cover" />
              </div>
              <input type="file" ref="modalCoverPicInput" @change="handleModalCoverPic" accept="image/*" class="hidden" />
              <button type="button" @click="$refs.modalCoverPicInput.click()" class="mt-2 px-3 py-1 bg-gray-200 dark:bg-gray-600 rounded text-sm hover:bg-gray-300 dark:hover:bg-gray-500 text-gray-800 dark:text-gray-100 font-medium">
                {{ uploadingModalCover ? 'Uploading...' : 'Change Cover' }}
              </button>
            </div>
            <div class="form-group">
              <label class="text-gray-900 dark:text-gray-100">Username</label>
              <input v-model="editForm.username" type="text" required class="bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent">
            </div>
            <div class="form-group">
              <label class="text-gray-900 dark:text-gray-100">Bio</label>
              <textarea v-model="editForm.bio" rows="4" class="bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"></textarea>
            </div>
            <div class="form-actions">
              <button type="button" @click="showEditModal = false" class="cancel-btn bg-gray-200 dark:bg-gray-600 hover:bg-gray-300 dark:hover:bg-gray-500 text-gray-800 dark:text-gray-100">Cancel</button>
              <button type="submit" class="save-btn bg-blue-600 text-white hover:bg-blue-700">Save Changes</button>
            </div>
          </form>
        </div>
      </div>

      <!-- Settings Modal -->
      <div v-if="showSettingsModal" class="modal-overlay" @click="showSettingsModal = false">
        <div class="modal-content settings-modal bg-white dark:bg-gray-800" @click.stop>
          <h2 class="dark:text-white">Settings</h2>
          
          <div class="settings-list">
            <div class="settings-category">
              <h4 class="text-gray-500 dark:text-gray-400 text-sm font-semibold uppercase tracking-wide mb-2">Account</h4>
              <div class="setting-row flex justify-between items-center p-3 rounded-lg mb-2 cursor-pointer transition-colors bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-900 dark:text-gray-100" @click="showEditModal = true; showSettingsModal = false">
                <span>✏️ Edit Profile</span>
                <span class="arrow text-gray-400">›</span>
              </div>
              <div class="setting-row flex justify-between items-center p-3 rounded-lg mb-2 cursor-pointer transition-colors bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-900 dark:text-gray-100" @click="router.push('/saved'); showSettingsModal = false">
                <span>🔖 Saved Posts</span>
                <span class="arrow text-gray-400">›</span>
              </div>
            </div>
            
            <div class="settings-category">
              <h4 class="text-gray-500 dark:text-gray-400 text-sm font-semibold uppercase tracking-wide mb-2">Privacy</h4>
              <div class="setting-row flex justify-between items-center p-3 rounded-lg mb-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-900 dark:text-gray-100">
                <span>🔒 Profile Visibility</span>
                <span class="setting-value text-gray-500 dark:text-gray-400 text-sm">Public</span>
              </div>
              <div class="setting-row flex justify-between items-center p-3 rounded-lg mb-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-900 dark:text-gray-100">
                <span>👁️ Who can see my posts</span>
                <span class="setting-value text-gray-500 dark:text-gray-400 text-sm">Everyone</span>
              </div>
            </div>
            
            <div class="settings-category">
              <h4 class="text-gray-500 dark:text-gray-400 text-sm font-semibold uppercase tracking-wide mb-2">Notifications</h4>
              <div class="setting-row flex justify-between items-center p-3 rounded-lg mb-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-900 dark:text-gray-100">
                <span>🔔 Push Notifications</span>
                <label class="toggle">
                  <input type="checkbox" checked>
                  <span class="slider bg-gray-300 dark:bg-gray-600"></span>
                </label>
              </div>
              <div class="setting-row flex justify-between items-center p-3 rounded-lg mb-2 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-900 dark:text-gray-100">
                <span>📧 Email Notifications</span>
                <label class="toggle">
                  <input type="checkbox">
                  <span class="slider bg-gray-300 dark:bg-gray-600"></span>
                </label>
              </div>
            </div>
            
            <div class="settings-category danger">
              <h4 class="text-red-600 dark:text-red-400 text-sm font-semibold uppercase tracking-wide mb-2">Danger Zone</h4>
              <div class="setting-row danger flex justify-between items-center p-3 rounded-lg mb-2 cursor-pointer transition-colors bg-red-50 dark:bg-red-900/20 hover:bg-red-100 dark:hover:bg-red-900/40 text-red-600 dark:text-red-400" @click="handleLogout">
                <span>🚪 Logout</span>
                <span class="arrow text-gray-400">›</span>
              </div>
            </div>
          </div>
          
          <div class="form-actions">
            <button type="button" @click="showSettingsModal = false" class="cancel-btn dark:bg-gray-600 dark:text-white">Close</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useThemeStore } from '../stores/theme';
import api from '../utils/api';

// Initialize theme store to ensure dark mode is applied
const themeStore = useThemeStore();

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const profile = ref(null);
const posts = ref([]);
const loading = ref(true);
const error = ref(null);
const showEditModal = ref(false);
const showSettingsModal = ref(false);
const editForm = ref({
  username: '',
  bio: '',
  profilePicUrl: '',
  coverPicUrl: ''
});
const profilePicInput = ref(null);
const uploadingPic = ref(false);
const uploadingModalPic = ref(false);
const uploadingModalCover = ref(false);
const modalProfilePicInput = ref(null);
const modalCoverPicInput = ref(null);

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
          bio: response.data.bio || '',
          profilePicUrl: response.data.profilePicUrl || '',
          coverPicUrl: response.data.coverPicUrl || ''
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
      bio: response.data.bio || '',
      profilePicUrl: response.data.profilePicUrl || '',
      coverPicUrl: response.data.coverPicUrl || ''
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
    authStore.updateUser(response.data); // Update auth store and persist to localStorage
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
    authStore.updateUser(updateRes.data);
    editForm.value.profilePicUrl = imageUrl;
  } catch (err) {
    console.error('Failed to upload profile picture:', err);
    alert('Failed to upload profile picture');
  } finally {
    uploadingPic.value = false;
  }
};

const handleModalProfilePic = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  uploadingModalPic.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', authStore.user.id);
    
    const uploadRes = await api.post('/media/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    
    editForm.value.profilePicUrl = uploadRes.data.url;
  } catch (err) {
    console.error('Failed to upload profile picture:', err);
    alert('Failed to upload profile picture');
  } finally {
    uploadingModalPic.value = false;
  }
};

const handleModalCoverPic = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  uploadingModalCover.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', authStore.user.id);
    
    const uploadRes = await api.post('/media/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    
    editForm.value.coverPicUrl = uploadRes.data.url;
  } catch (err) {
    console.error('Failed to upload cover picture:', err);
    alert('Failed to upload cover picture');
  } finally {
    uploadingModalCover.value = false;
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
  border-width: 5px;
  border-style: solid;
  position: relative;
  top: -75px;
  margin-bottom: -75px;
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
}

.bio {
  font-size: 16px;
  margin: 0;
}



.posts-section {
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.posts-section h2 {
  margin: 0 0 20px;
  font-size: 24px;
}

.no-posts {
  text-align: center;
  padding: 40px 20px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
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
}

.post-content {
  margin: 0 0 12px;
  font-size: 15px;
  line-height: 1.5;
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
  font-size: 14px;
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
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 15px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
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



/* Settings Button */


/* Settings Section */
.settings-section {
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.settings-section h3 {
  margin: 0 0 16px;
  font-size: 18px;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.setting-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.setting-icon {
  font-size: 24px;
}

/* Settings Modal */
.settings-modal {
  max-width: 450px;
}

.settings-list {
  max-height: 400px;
  overflow-y: auto;
}

.settings-category {
  margin-bottom: 20px;
}













.arrow {
  font-size: 18px;
}

/* Toggle Switch */
.toggle {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

.toggle input:checked + .slider {
  background-color: #1877f2;
}

.toggle input:checked + .slider:before {
  transform: translateX(20px);
}


</style>
