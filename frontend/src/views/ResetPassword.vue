<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useRoute, useRouter } from 'vue-router';

const newPassword = ref('');
const message = ref('');
const error = ref('');
const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();
const token = ref('');

onMounted(() => {
  token.value = route.query.token;
  if (!token.value) {
    error.value = 'Invalid or missing token.';
  }
});

const handleSubmit = async () => {
  if (!token.value) return;
  message.value = '';
  error.value = '';
  const result = await authStore.resetPassword(token.value, newPassword.value);
  if (result.success) {
      message.value = result.message;
      setTimeout(() => {
          router.push('/login');
      }, 3000);
  } else {
    error.value = result.message;
  }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-100 dark:bg-gray-900">
    <div class="px-8 py-6 mt-4 text-left bg-white dark:bg-gray-800 shadow-lg rounded-lg w-full max-w-sm">
      <h3 class="text-2xl font-bold text-center text-gray-900 dark:text-white">New Password</h3>
        <div v-if="!token && error" class="text-red-500 text-center mt-4">
            {{ error }}
            <div class="mt-4">
                <router-link to="/forgot-password" class="text-blue-600 hover:underline">Request new link</router-link>
            </div>
        </div>
      <form v-else @submit.prevent="handleSubmit">
        <div class="mt-4">
          <div>
            <label class="block text-gray-700 dark:text-gray-300" for="password">New Password</label>
            <input type="password" placeholder="Enter new password" id="password"
              class="w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-blue-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white border-gray-300 dark:border-gray-600"
              v-model="newPassword" required />
          </div>
          <p v-if="message" class="text-green-500 text-sm mt-2">{{ message }}</p>
          <p v-if="error && token" class="text-red-500 text-sm mt-2">{{ error }}</p>
          <div class="flex items-baseline justify-between">
            <button class="px-6 py-2 mt-4 text-white bg-blue-600 rounded-lg hover:bg-blue-900 transition duration-200">Reset Password</button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>
