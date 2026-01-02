<template>
  <div class="flex items-center justify-center" :class="containerClass">
    <div 
      class="animate-spin rounded-full border-b-2 border-blue-600"
      :class="sizeClass"
    ></div>
    <span v-if="text" class="ml-2 text-gray-600">{{ text }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg', 'xl'].includes(value)
  },
  text: {
    type: String,
    default: ''
  },
  fullPage: {
    type: Boolean,
    default: false
  }
});

const sizeClass = computed(() => {
  const sizes = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
    xl: 'h-16 w-16'
  };
  return sizes[props.size];
});

const containerClass = computed(() => {
  return props.fullPage ? 'min-h-screen' : 'py-4';
});
</script>
