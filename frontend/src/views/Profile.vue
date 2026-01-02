<template>
  <div class="min-h-screen bg-gray-100 dark:bg-gray-900">
    <!-- Navbar -->
    <nav class="bg-white dark:bg-gray-800 shadow sticky top-0 z-50">
      <div class="container mx-auto px-4 h-16 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-blue-600 cursor-pointer" @click="router.push('/')">Facebook</h1>
        
        <div class="flex items-center gap-6">
          <button @click="router.push('/')" class="text-gray-600 dark:text-gray-300 hover:text-blue-600 font-medium">Home</button>
          <button @click="router.push('/friends')" class="text-gray-600 dark:text-gray-300 hover:text-blue-600 font-medium">Friends</button>
          <button @click="handleLogout" class="text-gray-500 dark:text-gray-400 hover:text-red-600 transition font-medium text-sm">Logout</button>
        </div>
      </div>
    </nav>

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
            <button v-if="isOwnProfile" @click="showSettingsModal = true" class="settings-button">
              ⚙️ Settings
            </button>
          </div>
        </div>

        <!-- Settings Section for own profile -->
        <div v-if="isOwnProfile" class="settings-section">
          <h3>Quick Settings</h3>
          <div class="settings-grid">
            <div class="setting-item" @click="router.push('/saved')">
              <span class="setting-icon">🔖</span>
              <span>Saved Posts</span>
            </div>
            <div class="setting-item" @click="router.push('/friends')">
              <span class="setting-icon">👥</span>
              <span>Friends</span>
            </div>
            <div class="setting-item" @click="router.push('/groups')">
              <span class="setting-icon">👨‍👩‍👧‍👦</span>
              <span>Groups</span>
            </div>
            <div class="setting-item" @click="router.push('/events')">
              <span class="setting-icon">📅</span>
              <span>Events</span>
            </div>
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
            <!-- Profile Picture -->
            <div class="form-group">
              <label>Profile Picture</label>
              <div class="flex items-center gap-4">
                <div class="w-16 h-16 rounded-full overflow-hidden bg-blue-500 flex items-center justify-center">
                  <img v-if="editForm.profilePicUrl" :src="editForm.profilePicUrl" class="w-full h-full object-cover" />
                  <span v-else class="text-white text-2xl font-bold">{{ editForm.username?.charAt(0)?.toUpperCase() }}</span>
                </div>
                <input type="file" ref="modalProfilePicInput" @change="handleModalProfilePic" accept="image/*" class="hidden" />
                <button type="button" @click="$refs.modalProfilePicInput.click()" class="px-3 py-1 bg-gray-200 rounded text-sm hover:bg-gray-300">
                  {{ uploadingModalPic ? 'Uploading...' : 'Change Photo' }}
                </button>
              </div>
            </div>
            <!-- Cover Picture -->
            <div class="form-group">
              <label>Cover Photo</label>
              <div class="w-full h-20 rounded overflow-hidden bg-gradient-to-r from-blue-500 to-purple-500">
                <img v-if="editForm.coverPicUrl" :src="editForm.coverPicUrl" class="w-full h-full object-cover" />
              </div>
              <input type="file" ref="modalCoverPicInput" @change="handleModalCoverPic" accept="image/*" class="hidden" />
              <button type="button" @click="$refs.modalCoverPicInput.click()" class="mt-2 px-3 py-1 bg-gray-200 rounded text-sm hover:bg-gray-300">
                {{ uploadingModalCover ? 'Uploading...' : 'Change Cover' }}
              </button>
            </div>
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

      <!-- Settings Modal -->
      <div v-if="showSettingsModal" class="modal-overlay" @click="showSettingsModal = false">
        <div class="modal-content settings-modal" @click.stop>
          <h2>Settings</h2>
          
          <div class="settings-list">
            <div class="settings-category">
              <h4>Account</h4>
              <div class="setting-row" @click="showEditModal = true; showSettingsModal = false">
                <span>✏️ Edit Profile</span>
                <span class="arrow">›</span>
              </div>
              <div class="setting-row" @click="router.push('/saved'); showSettingsModal = false">
                <span>🔖 Saved Posts</span>
                <span class="arrow">›</span>
              </div>
            </div>
            
            <div class="settings-category">
              <h4>Privacy</h4>
              <div class="setting-row">
                <span>🔒 Profile Visibility</span>
                <span class="setting-value">Public</span>
              </div>
              <div class="setting-row">
                <span>👁️ Who can see my posts</span>
                <span class="setting-value">Everyone</span>
              </div>
            </div>
            
            <div class="settings-category">
              <h4>Notifications</h4>
              <div class="setting-row">
                <span>🔔 Push Notifications</span>
                <label class="toggle">
                  <input type="checkbox" checked>
                  <span class="slider"></span>
                </label>
              </div>
              <div class="setting-row">
                <span>📧 Email Notifications</span>
                <label class="toggle">
                  <input type="checkbox">
                  <span class="slider"></span>
                </label>
              </div>
            </div>
            
            <div class="settings-category danger">
              <h4>Danger Zone</h4>
              <div class="setting-row danger" @click="handleLogout">
                <span>🚪 Logout</span>
                <span class="arrow">›</span>
              </div>
            </div>
          </div>
          
          <div class="form-actions">
            <button type="button" @click="showSettingsModal = false" class="cancel-btn">Close</button>
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
import api from '../utils/api';

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
  background: white;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:global(.dark) .profile-header {
  background: #1f2937;
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

:global(.dark) .profile-picture {
  border-color: #1f2937;
  background: #374151;
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

:global(.dark) .profile-details h1 {
  color: #f3f4f6;
}

.bio {
  color: #65676b;
  font-size: 16px;
  margin: 0;
}

:global(.dark) .bio {
  color: #9ca3af;
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

:global(.dark) .posts-section {
  background: #1f2937;
}

.posts-section h2 {
  margin: 0 0 20px;
  font-size: 24px;
  color: #1c1e21;
}

:global(.dark) .posts-section h2 {
  color: #f3f4f6;
}

.no-posts {
  text-align: center;
  padding: 40px 20px;
  color: #65676b;
}

:global(.dark) .no-posts {
  color: #9ca3af;
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

:global(.dark) .post-card {
  border-color: #374151;
  background: #111827;
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

:global(.dark) .post-time {
  color: #9ca3af;
}

.post-content {
  margin: 0 0 12px;
  font-size: 15px;
  line-height: 1.5;
  color: #1c1e21;
}

:global(.dark) .post-content {
  color: #e5e7eb;
}

:global(.dark) .post-header h3 {
  color: #f3f4f6;
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

:global(.dark) .post-actions {
  border-color: #374151;
  color: #9ca3af;
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

:global(.dark) .modal-content {
  background: #1f2937;
}

.modal-content h2 {
  margin: 0 0 20px;
  font-size: 20px;
}

:global(.dark) .modal-content h2 {
  color: #f3f4f6;
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

:global(.dark) .form-group label {
  color: #e5e7eb;
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

:global(.dark) .form-group input,
:global(.dark) .form-group textarea {
  background: #374151;
  border-color: #4b5563;
  color: #f3f4f6;
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

/* Settings Button */
.settings-button {
  position: absolute;
  top: 56px;
  right: 24px;
  padding: 8px 16px;
  background: #e4e6eb;
  color: #1c1e21;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

.settings-button:hover {
  background: #d8dadf;
}

:global(.dark) .settings-button {
  background: #374151;
  color: #e5e7eb;
}

:global(.dark) .settings-button:hover {
  background: #4b5563;
}

/* Settings Section */
.settings-section {
  background: white;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:global(.dark) .settings-section {
  background: #1f2937;
}

.settings-section h3 {
  margin: 0 0 16px;
  font-size: 18px;
  color: #1c1e21;
}

:global(.dark) .settings-section h3 {
  color: #f3f4f6;
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
  background: #f0f2f5;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.setting-item:hover {
  background: #e4e6eb;
}

:global(.dark) .setting-item {
  background: #374151;
  color: #e5e7eb;
}

:global(.dark) .setting-item:hover {
  background: #4b5563;
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

.settings-category h4 {
  margin: 0 0 10px;
  font-size: 14px;
  color: #65676b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

:global(.dark) .settings-category h4 {
  color: #9ca3af;
}

.settings-category.danger h4 {
  color: #dc2626;
}

.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f0f2f5;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.setting-row:hover {
  background: #e4e6eb;
}

:global(.dark) .setting-row {
  background: #374151;
  color: #e5e7eb;
}

:global(.dark) .setting-row:hover {
  background: #4b5563;
}

.setting-row.danger {
  color: #dc2626;
}

.setting-row.danger:hover {
  background: #fef2f2;
}

:global(.dark) .setting-row.danger:hover {
  background: #450a0a;
}

.setting-value {
  color: #65676b;
  font-size: 14px;
}

:global(.dark) .setting-value {
  color: #9ca3af;
}

.arrow {
  font-size: 18px;
  color: #65676b;
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
  background-color: #ccc;
  transition: 0.3s;
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

:global(.dark) .cancel-btn {
  background: #374151;
  color: #e5e7eb;
}

:global(.dark) .cancel-btn:hover {
  background: #4b5563;
}
</style>
