<template>
  <Teleport to="body">
    <div 
      v-if="isOpen" 
      class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-90"
      @click="close"
    >
      <!-- Close button -->
      <button 
        class="absolute top-4 right-4 text-white text-3xl hover:text-gray-300 z-10"
        @click="close"
      >
        ✕
      </button>

      <!-- Image container -->
      <div class="relative max-w-[90vw] max-h-[90vh]" @click.stop>
        <img 
          :src="imageUrl" 
          :alt="alt"
          class="max-w-full max-h-[90vh] object-contain rounded-lg shadow-2xl"
          @error="handleError"
        />
        
        <!-- Caption -->
        <div v-if="caption" class="absolute bottom-0 left-0 right-0 bg-black bg-opacity-50 text-white p-4 rounded-b-lg">
          <p class="text-sm">{{ caption }}</p>
        </div>
      </div>

      <!-- Navigation arrows for gallery mode -->
      <button 
        v-if="hasPrev"
        class="absolute left-4 text-white text-4xl hover:text-gray-300 p-2"
        @click.stop="prev"
      >
        ‹
      </button>
      <button 
        v-if="hasNext"
        class="absolute right-4 text-white text-4xl hover:text-gray-300 p-2"
        @click.stop="next"
      >
        ›
      </button>

      <!-- Image counter -->
      <div v-if="total > 1" class="absolute bottom-4 left-1/2 transform -translate-x-1/2 text-white text-sm bg-black bg-opacity-50 px-3 py-1 rounded-full">
        {{ currentIndex + 1 }} / {{ total }}
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  imageUrl: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: 'Image'
  },
  caption: {
    type: String,
    default: ''
  },
  images: {
    type: Array,
    default: () => []
  },
  currentIndex: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['close', 'prev', 'next']);

const total = computed(() => props.images.length || 1);
const hasPrev = computed(() => props.images.length > 1 && props.currentIndex > 0);
const hasNext = computed(() => props.images.length > 1 && props.currentIndex < props.images.length - 1);

const close = () => emit('close');
const prev = () => emit('prev');
const next = () => emit('next');

const handleError = (e) => {
  console.error('Failed to load image:', e);
};

const handleKeydown = (e) => {
  if (!props.isOpen) return;
  if (e.key === 'Escape') close();
  if (e.key === 'ArrowLeft' && hasPrev.value) prev();
  if (e.key === 'ArrowRight' && hasNext.value) next();
};

onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
});
</script>
