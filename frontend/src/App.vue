<script setup>
import { useAuthStore } from './stores/auth';
import { useNotificationStore } from './stores/notification';
import { watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import NavBar from './components/NavBar.vue';

const authStore = useAuthStore();
const notificationStore = useNotificationStore();
const route = useRoute();

// Connect if logged in on mount (only if user has id)
onMounted(() => {
  if (authStore.user && authStore.user.id) {
    notificationStore.connect();
  }
});

// Watch for login/logout - watch user instead of token since user is set after profile fetch
watch(() => authStore.user, (newUser) => {
  if (newUser && newUser.id) {
    notificationStore.connect();
  } else {
    notificationStore.disconnect();
  }
});

// Check if current route should show navbar
const showNavBar = () => {
  const publicRoutes = ['/login', '/register', '/forgot-password', '/reset-password'];
  return !publicRoutes.includes(route.path);
};
</script>

<template>
  <NavBar v-if="showNavBar()" />
  <router-view></router-view>
  
  <!-- Notification Toast Container -->
  <div class="fixed top-20 right-4 z-50 flex flex-col gap-2 pointer-events-none">
    <div 
      v-for="(notification, index) in notificationStore.notifications" 
      :key="index"
      class="bg-white dark:bg-gray-800 border-l-4 border-blue-500 text-gray-800 dark:text-gray-200 px-4 py-3 rounded shadow-lg max-w-sm pointer-events-auto flex items-start gap-3"
    >
        <div class="text-blue-500 mt-1">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z" />
            </svg>
        </div>
        <div>
           <div class="font-bold text-sm">{{ notification.senderId === 'SYSTEM' ? 'Notification' : notification.senderId }}</div>
           <div class="text-sm">{{ notification.message }}</div>
        </div>
    </div>
  </div>
</template>

<style>
/* Global styles if needed */
</style>
