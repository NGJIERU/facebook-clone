<script setup>
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import NotificationDropdown from './NotificationDropdown.vue';
import ThemeToggle from './ThemeToggle.vue';
import GlobalSearch from './GlobalSearch.vue';

const router = useRouter();
const authStore = useAuthStore();

const navItems = [
  { name: 'Home', path: '/', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
  { name: 'Friends', path: '/friends', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
  { name: 'Groups', path: '/groups', icon: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z' },
  { name: 'Events', path: '/events', icon: 'M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z' },
  { name: 'Saved', path: '/saved', icon: 'M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z' },
  { name: 'Messages', path: '/messages', icon: 'M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z' },
];

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<template>
  <nav class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 fixed top-0 left-0 right-0 z-50">
    <div class="max-w-7xl mx-auto px-2 sm:px-4 lg:px-6">
      <div class="flex justify-between h-16">
        <!-- Left section: Logo and Search -->
        <div class="flex items-center">
          <!-- Facebook Logo -->
          <div class="flex-shrink-0 flex items-center">
            <h1 class="text-2xl font-bold text-blue-600 cursor-pointer" @click="router.push('/')">Facebook</h1>
          </div>
          
          <!-- Search Bar -->
          <div class="hidden md:block ml-2">
            <GlobalSearch />
          </div>
        </div>
        
        <!-- Center section: Navigation Icons -->
        <div class="flex-1 flex justify-center max-w-2xl">
          <div class="flex space-x-1">
            <template v-for="item in navItems" :key="item.name">
              <router-link 
                :to="item.path"
                class="text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 px-4 py-3 rounded-md text-sm font-medium relative group"
                :class="{ 'text-blue-600 dark:text-blue-400': $route.path === item.path }"
              >
                <div class="flex flex-col items-center">
                  <svg 
                    class="h-6 w-6 mx-auto" 
                    fill="none" 
                    viewBox="0 0 24 24" 
                    stroke="currentColor"
                    :class="{ 'text-blue-600 dark:text-blue-400': $route.path === item.path }"
                  >
                    <path 
                      stroke-linecap="round" 
                      stroke-linejoin="round" 
                      stroke-width="2" 
                      :d="item.icon"
                    />
                  </svg>
                  <div 
                    class="absolute bottom-0 left-0 right-0 h-1 bg-blue-600 rounded-t transform scale-x-0 group-hover:scale-x-100 transition-transform duration-200"
                    :class="{ 'scale-x-100': $route.path === item.path }"
                  ></div>
                </div>
              </router-link>
            </template>
          </div>
        </div>
        
        <!-- Right section: User menu -->
        <div class="flex items-center">
          <div class="flex items-center space-x-2">
            <NotificationDropdown />
            <ThemeToggle />
            
            <!-- Profile dropdown -->
            <div class="ml-3 relative">
              <div>
                <button 
                  @click="router.push('/profile')"
                  class="flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-800 focus:ring-white"
                >
                  <span class="sr-only">Open user menu</span>
                  <div class="h-8 w-8 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center overflow-hidden">
                    <img 
                      v-if="authStore.user?.profilePicUrl" 
                      :src="authStore.user.profilePicUrl" 
                      class="h-full w-full object-cover"
                      alt="Profile"
                    />
                    <span v-else class="text-gray-700 dark:text-gray-300 font-medium text-sm">
                      {{ authStore.user?.username?.charAt(0).toUpperCase() }}
                    </span>
                  </div>
                  <span class="hidden md:inline-block ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                    {{ authStore.user?.username }}
                  </span>
                </button>
              </div>
            </div>
            
            <!-- Logout button -->
            <button 
              @click="handleLogout" 
              class="ml-2 p-1 rounded-full text-gray-500 hover:text-red-600 hover:bg-gray-100 dark:hover:bg-gray-700 focus:outline-none"
              title="Logout"
            >
              <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </nav>
  
  <!-- Spacer to push content below fixed navbar -->
  <div class="h-16"></div>
</template>

<style scoped>
.router-link-active {
  @apply text-blue-600 dark:text-blue-400;
}

.router-link-active svg {
  @apply text-blue-600 dark:text-blue-400;
}
</style>
