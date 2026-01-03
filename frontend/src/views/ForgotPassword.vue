<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';

const email = ref('');
const message = ref('');
const error = ref('');
const authStore = useAuthStore();

const handleSubmit = async () => {
  message.value = '';
  error.value = '';
  const result = await authStore.forgotPassword(email.value);
  if (result.success) {
    message.value = result.message;
  } else {
    error.value = result.message;
  }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-100 dark:bg-gray-900">
    <div class="px-8 py-6 mt-4 text-left bg-white dark:bg-gray-800 shadow-lg rounded-lg w-full max-w-sm">
      <h3 class="text-2xl font-bold text-center text-gray-900 dark:text-white">Reset Password</h3>
      <form @submit.prevent="handleSubmit">
        <div class="mt-4">
          <div>
            <label class="block text-gray-700 dark:text-gray-300" for="email">Email</label>
            <input type="email" placeholder="Enter your email" id="email"
              class="w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-blue-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white border-gray-300 dark:border-gray-600"
              v-model="email" required />
          </div>
          <p v-if="message" class="text-green-500 text-sm mt-2">{{ message }}</p>
          <p v-if="error" class="text-red-500 text-sm mt-2">{{ error }}</p>
          <div class="flex items-baseline justify-between">
            <button class="px-6 py-2 mt-4 text-white bg-blue-600 rounded-lg hover:bg-blue-900 transition duration-200">Send Reset Link</button>
            <router-link to="/login" class="text-sm text-blue-600 hover:underline dark:text-blue-400">Back to Login</router-link>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>
