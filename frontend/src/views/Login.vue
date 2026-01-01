<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';

const email = ref('');
const password = ref('');
const error = ref('');
const authStore = useAuthStore();
const router = useRouter();

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
  <div class="flex items-center justify-center min-h-screen bg-gray-100">
    <div class="px-8 py-6 mt-4 text-left bg-white shadow-lg rounded-lg w-full max-w-sm">
      <h3 class="text-2xl font-bold text-center">Login to your account</h3>
      <form @submit.prevent="handleLogin">
        <div class="mt-4">
          <div>
            <label class="block" for="email">Email</label>
            <input type="email" placeholder="Email" id="email"
              class="w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-blue-600"
              v-model="email" @input="error = ''" required />
          </div>
          <div class="mt-4">
            <label class="block" for="password">Password</label>
            <input type="password" placeholder="Password" id="password"
              class="w-full px-4 py-2 mt-2 border rounded-md focus:outline-none focus:ring-1 focus:ring-blue-600"
              v-model="password" @input="error = ''" required />
          </div>
          <p v-if="error" class="text-red-500 text-sm mt-2">{{ error }}</p>
          <div class="flex items-baseline justify-between">
            <button class="px-6 py-2 mt-4 text-white bg-blue-600 rounded-lg hover:bg-blue-900">Login</button>
            <router-link to="/register" class="text-sm text-blue-600 hover:underline">Register</router-link>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>
