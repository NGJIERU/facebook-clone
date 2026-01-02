<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useNotificationStore } from '../stores/notification';
import { formatDistanceToNow } from 'date-fns';
import api from '../utils/api';
import { usePushNotifications } from '../composables/usePushNotifications';

const notificationStore = useNotificationStore();
const { requestPermission, showNotification, permission } = usePushNotifications();
const isOpen = ref(false);
const dropdownRef = ref(null);
const senderNames = ref({});
const lastNotificationCount = ref(0);

const toggleDropdown = () => {
  isOpen.value = !isOpen.value;
};

const handleMarkAsRead = (notification) => {
    if (!notification.read) {
        notificationStore.markAsRead(notification.id);
    }
};

const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isOpen.value = false;
  }
};

const fetchSenderName = async (senderId) => {
  if (senderId === 'SYSTEM' || senderNames.value[senderId]) return;
  try {
    const response = await api.get(`/users/profile/${senderId}`);
    senderNames.value[senderId] = response.data.username;
  } catch (e) {
    senderNames.value[senderId] = senderId; // fallback to ID
  }
};

const getSenderDisplayName = (senderId) => {
  if (senderId === 'SYSTEM') return 'SYSTEM';
  return senderNames.value[senderId] || senderId;
};

// Watch for new notifications and fetch sender names
watch(() => notificationStore.history, async (notifications) => {
  for (const notification of notifications) {
    if (notification.senderId && notification.senderId !== 'SYSTEM') {
      await fetchSenderName(notification.senderId);
    }
  }
}, { immediate: true, deep: true });

// Watch for new notifications and show browser push notification
watch(() => notificationStore.history.length, (newCount) => {
  if (newCount > lastNotificationCount.value && lastNotificationCount.value > 0) {
    const latestNotification = notificationStore.history[0];
    if (latestNotification && !latestNotification.read) {
      const senderName = getSenderDisplayName(latestNotification.senderId);
      showNotification('New Notification', {
        body: `${senderName}: ${latestNotification.message}`,
        tag: 'app-notification',
      });
    }
  }
  lastNotificationCount.value = newCount;
});

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
  // Request notification permission on mount
  if (permission.value === 'default') {
    requestPermission();
  }
  lastNotificationCount.value = notificationStore.history.length;
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});

const formatDate = (dateString) => {
    if (!dateString) return '';
    return formatDistanceToNow(new Date(dateString), { addSuffix: true });
};
</script>

<template>
  <div class="relative" ref="dropdownRef">
    <!-- Bell Icon Button -->
    <button 
      @click="toggleDropdown" 
      class="p-2 text-gray-600 dark:text-gray-300 hover:text-blue-600 transition relative focus:outline-none"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
      </svg>
      
      <!-- Unread Badge -->
      <span 
        v-if="notificationStore.unreadCount > 0" 
        class="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/4 -translate-y-1/4"
      >
        {{ notificationStore.unreadCount }}
      </span>
    </button>

    <!-- Dropdown Menu -->
    <div 
      v-if="isOpen" 
      class="absolute right-0 mt-2 w-80 bg-white dark:bg-gray-800 rounded-md shadow-lg overflow-hidden z-20 border border-gray-200 dark:border-gray-700"
    >
        <div class="px-4 py-2 border-b border-gray-100 dark:border-gray-700 font-semibold text-gray-700 dark:text-gray-200 flex justify-between items-center">
            <span>Notifications</span>
            <span class="text-xs text-gray-400 dark:text-gray-500 font-normal">History</span>
        </div>

        <div class="max-h-96 overflow-y-auto">
            <div v-if="notificationStore.history.length === 0" class="px-4 py-6 text-center text-gray-500 dark:text-gray-400 text-sm">
                No notifications yet.
            </div>

            <div 
                v-for="notification in notificationStore.history" 
                :key="notification.id"
                @click="handleMarkAsRead(notification)"
                class="px-4 py-3 border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer transition flex gap-3 items-start"
                :class="{ 'bg-blue-50': !notification.read }"
            >
                <div class="flex-shrink-0 mt-1 text-blue-500">
                     <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                        <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z" />
                    </svg>
                </div>
                <div class="flex-1">
                     <p class="text-sm text-gray-800 dark:text-gray-200" :class="{ 'font-semibold': !notification.read }">
                        <span class="font-bold">{{ getSenderDisplayName(notification.senderId) }}</span> {{ notification.message }}
                     </p>
                     <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                        {{ formatDate(notification.createdAt) }}
                     </p>
                </div>
                <div v-if="!notification.read" class="flex-shrink-0 mt-2">
                    <span class="block w-2 h-2 bg-blue-600 rounded-full"></span>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>
