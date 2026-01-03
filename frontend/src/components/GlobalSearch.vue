<script setup>
import { ref, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import api from '../utils/api';

const router = useRouter();
const searchQuery = ref('');
const showResults = ref(false);
const loading = ref(false);

const results = ref({
  users: [],
  posts: [],
  groups: [],
  events: []
});

const hasResults = computed(() => {
  return results.value.users.length > 0 || 
         results.value.posts.length > 0 || 
         results.value.groups.length > 0 || 
         results.value.events.length > 0;
});

// Combine all results into a single unified list
const unifiedResults = computed(() => {
  const combined = [];
  
  // Add users first (prioritize people)
  results.value.users.forEach(user => {
    combined.push({ type: 'user', data: user });
  });
  
  // Add groups
  results.value.groups.forEach(group => {
    combined.push({ type: 'group', data: group });
  });
  
  // Add events
  results.value.events.forEach(event => {
    combined.push({ type: 'event', data: event });
  });
  
  // Add posts last
  results.value.posts.forEach(post => {
    combined.push({ type: 'post', data: post });
  });
  
  return combined.slice(0, 10); // Limit to 10 results
});

let searchTimeout = null;

watch(searchQuery, (newVal) => {
  if (searchTimeout) clearTimeout(searchTimeout);
  
  if (!newVal.trim()) {
    results.value = { users: [], posts: [], groups: [], events: [] };
    showResults.value = false;
    return;
  }
  
  searchTimeout = setTimeout(() => {
    performSearch(newVal);
  }, 300);
});

const performSearch = async (query) => {
  if (!query.trim()) return;
  
  loading.value = true;
  showResults.value = true;
  
  try {
    const [usersRes, postsRes, groupsRes, eventsRes] = await Promise.allSettled([
      api.get(`/users/search?q=${encodeURIComponent(query)}`),
      api.get(`/feed/search?q=${encodeURIComponent(query)}`),
      api.get(`/groups/search?q=${encodeURIComponent(query)}`),
      api.get(`/events/search?q=${encodeURIComponent(query)}`)
    ]);
    
    results.value = {
      users: usersRes.status === 'fulfilled' ? usersRes.value.data : [],
      posts: postsRes.status === 'fulfilled' ? postsRes.value.data : [],
      groups: groupsRes.status === 'fulfilled' ? groupsRes.value.data : [],
      events: eventsRes.status === 'fulfilled' ? eventsRes.value.data : []
    };
  } catch (e) {
    console.error('Search failed', e);
  } finally {
    loading.value = false;
  }
};

const goToUser = (userId) => {
  router.push(`/profile/${userId}`);
  closeSearch();
};

const goToGroup = (groupId) => {
  router.push('/groups');
  closeSearch();
};

const goToEvent = (eventId) => {
  router.push('/events');
  closeSearch();
};

const closeSearch = () => {
  showResults.value = false;
  searchQuery.value = '';
};

const handleClickOutside = (e) => {
  if (!e.target.closest('.search-container')) {
    showResults.value = false;
  }
};
</script>

<template>
  <div class="search-container relative" @click.stop>
    <div class="search-input-wrapper bg-gray-100 dark:bg-gray-700">
      <span class="search-icon text-gray-500 dark:text-gray-400">🔍</span>
      <input 
        v-model="searchQuery"
        type="text"
        placeholder="Search Facebook..."
        class="search-input text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400"
        @focus="searchQuery.trim() && (showResults = true)"
      />
      <button v-if="searchQuery" @click="closeSearch" class="clear-btn">✕</button>
    </div>
    
    <!-- Search Results Dropdown -->
    <div v-if="showResults" class="search-results bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700" @click.stop>
      <!-- Loading -->
      <div v-if="loading" class="search-loading">
        <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
      </div>
      
      <!-- Unified Results List -->
      <div v-else-if="hasResults" class="results-list">
        <div 
          v-for="(item, index) in unifiedResults" 
          :key="`${item.type}-${item.data.id || index}`"
          @click="item.type === 'user' ? goToUser(item.data.id) : item.type === 'group' ? goToGroup(item.data.id) : item.type === 'event' ? goToEvent(item.data.id) : router.push('/')"
          class="result-item hover:bg-gray-100 dark:hover:bg-gray-700"
        >
          <!-- User Result -->
          <template v-if="item.type === 'user'">
            <div class="result-avatar bg-gray-200 dark:bg-gray-600 text-gray-600 dark:text-gray-200">
              <img v-if="item.data.profilePicUrl" :src="item.data.profilePicUrl" class="w-full h-full object-cover rounded-full" />
              <span v-else>{{ item.data.username?.charAt(0)?.toUpperCase() }}</span>
            </div>
            <div class="result-info">
              <span class="result-name text-gray-900 dark:text-white">{{ item.data.username }}</span>
              <span class="result-type text-gray-500 dark:text-gray-400">Person</span>
            </div>
          </template>
          
          <!-- Group Result -->
          <template v-else-if="item.type === 'group'">
            <div class="result-avatar bg-blue-100 dark:bg-blue-900 text-blue-600 dark:text-blue-400">👥</div>
            <div class="result-info">
              <span class="result-name text-gray-900 dark:text-white">{{ item.data.name }}</span>
              <span class="result-type text-gray-500 dark:text-gray-400">Group · {{ item.data.memberCount || 0 }} members</span>
            </div>
          </template>
          
          <!-- Event Result -->
          <template v-else-if="item.type === 'event'">
            <div class="result-avatar bg-purple-100 dark:bg-purple-900 text-purple-600 dark:text-purple-400">📅</div>
            <div class="result-info">
              <span class="result-name text-gray-900 dark:text-white">{{ item.data.title }}</span>
              <span class="result-type text-gray-500 dark:text-gray-400">Event</span>
            </div>
          </template>
          
          <!-- Post Result -->
          <template v-else-if="item.type === 'post'">
            <div class="result-avatar bg-green-100 dark:bg-green-900 text-green-600 dark:text-green-400">📝</div>
            <div class="result-info">
              <span class="result-name text-gray-900 dark:text-white">{{ item.data.content?.substring(0, 50) }}{{ item.data.content?.length > 50 ? '...' : '' }}</span>
              <span class="result-type text-gray-500 dark:text-gray-400">Post</span>
            </div>
          </template>
        </div>
      </div>
      
      <!-- No Results -->
      <div v-else-if="searchQuery.trim() && !loading" class="no-results text-gray-500 dark:text-gray-400">
        <p>No results found for "{{ searchQuery }}"</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-container {
  position: relative;
  width: 280px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  border-radius: 20px;
  padding: 0 12px;
}

.search-icon {
  font-size: 14px;
  margin-right: 8px;
}

.search-input {
  flex: 1;
  background: transparent;
  border: none;
  padding: 8px 0;
  font-size: 14px;
  outline: none;
}

:global(.dark) .search-input {
  color: #f3f4f6;
}

:global(.dark) .search-input::placeholder {
  color: #9ca3af;
}

.clear-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #65676b;
  font-size: 12px;
  padding: 4px;
}

:global(.dark) .clear-btn {
  color: #9ca3af;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  max-height: 400px;
  overflow-y: auto;
  z-index: 100;
}

.search-tabs {
  display: flex;
  padding: 8px;
  border-bottom: 1px solid #374151;
  gap: 4px;
}

.tab-btn {
  padding: 6px 12px;
  border: none;
  background: transparent;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
}

.search-loading {
  padding: 20px;
}

.results-list {
  padding: 8px;
}

.result-section {
  margin-bottom: 8px;
}

.result-section h4 {
  font-size: 13px;
  margin: 8px 12px;
  font-weight: 600;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}


.result-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #65676b;
  overflow: hidden;
}

.post-icon, .group-icon, .event-icon {
  font-size: 18px;
  background: #1e3a5f;
}

.result-info {
  flex: 1;
  min-width: 0;
}

.result-name {
  display: block;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-type {
  display: block;
  font-size: 12px;
}

.no-results {
  padding: 20px;
  text-align: center;
}
</style>
