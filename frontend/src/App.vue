<script setup>
</script>

<template>
  <div class="min-h-screen bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-gray-100">
    <nav class="p-4 bg-white dark:bg-gray-800 shadow flex justify-between items-center">
      <h1 class="text-2xl font-bold text-blue-600">Facebook Clone</h1>
      <div class="space-x-4">
        <router-link to="/" class="hover:text-blue-500">Home</router-link>
        <router-link to="/about" class="hover:text-blue-500">About</router-link>
      </div>
    </nav>
    <main class="p-4">
      <router-view></router-view>
    </main>
  </div>
</template>
