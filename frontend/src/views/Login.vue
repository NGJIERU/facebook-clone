<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useRouter, useRoute } from 'vue-router';

const email = ref('');
const password = ref('');
const error = ref('');
const authStore = useAuthStore();
const router = useRouter();

const route = useRoute();

onMounted(async () => {
  const token = route.query.token;
  const refreshToken = route.query.refreshToken;
  
  if (token) {
    authStore.setToken(token);
    if (refreshToken) {
      authStore.setRefreshToken(refreshToken);
    }
    // Fetch user profile or just redirect
    await authStore.fetchUser();
    router.push('/');
  }
});

const handleLogin = async () => {
  const result = await authStore.login(email.value, password.value);
  if (result.success) {
    router.push('/');
  } else {
    error.value = result.message;
  }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-100 dark:bg-gray-900">
    <div class="px-8 py-6 mt-4 text-left bg-white dark:bg-gray-800 shadow-lg rounded-lg w-full max-w-sm">
      <h3 class="text-2xl font-bold text-center text-gray-900 dark:text-white">Login to your account</h3>
      <form @submit.prevent="handleLogin">
        <div class="mt-4">
          <div>
            <label class="block text-gray-700 dark:text-gray-300" for="email">Email</label>
            <input type="email" placeholder="Email" id="email"
              class="w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-blue-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white border-gray-300 dark:border-gray-600"
              v-model="email" @input="error = ''" required />
          </div>
          <div class="mt-4">
            <label class="block text-gray-700 dark:text-gray-300" for="password">Password</label>
            <input type="password" placeholder="Password" id="password"
              class="w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-blue-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white border-gray-300 dark:border-gray-600"
              v-model="password" @input="error = ''" required />
          </div>
          <p v-if="error" class="text-red-500 text-sm mt-2">{{ error }}</p>
          <div class="flex items-baseline justify-between">
            <button class="px-6 py-2 mt-4 text-white bg-blue-600 rounded-lg hover:bg-blue-900 transition duration-200">Login</button>
            <div class="flex flex-col items-end">
                <router-link to="/forgot-password" class="text-sm text-blue-600 hover:underline dark:text-blue-400 mb-2">Forgot Password?</router-link>
                <router-link to="/register" class="text-sm text-blue-600 hover:underline dark:text-blue-400">Register</router-link>
            </div>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>
