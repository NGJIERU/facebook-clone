<script setup>
import { ref, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import api from '../utils/api';

const router = useRouter();
const searchQuery = ref('');
const showResults = ref(false);
const loading = ref(false);
const activeTab = ref('all');

const results = ref({
  users: [],
  posts: [],
  groups: [],
  events: []
});

const tabs = [
  { id: 'all', label: 'All' },
  { id: 'users', label: 'People' },
  { id: 'posts', label: 'Posts' },
  { id: 'groups', label: 'Groups' },
  { id: 'events', label: 'Events' }
];

const hasResults = computed(() => {
  return results.value.users.length > 0 || 
         results.value.posts.length > 0 || 
         results.value.groups.length > 0 || 
         results.value.events.length > 0;
});

const filteredResults = computed(() => {
  if (activeTab.value === 'all') {
    return {
      users: results.value.users.slice(0, 3),
      posts: results.value.posts.slice(0, 3),
      groups: results.value.groups.slice(0, 3),
      events: results.value.events.slice(0, 3)
    };
  }
  return {
    users: activeTab.value === 'users' ? results.value.users : [],
    posts: activeTab.value === 'posts' ? results.value.posts : [],
    groups: activeTab.value === 'groups' ? results.value.groups : [],
    events: activeTab.value === 'events' ? results.value.events : []
  };
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
    <div class="search-input-wrapper">
      <span class="search-icon">🔍</span>
      <input 
        v-model="searchQuery"
        type="text"
        placeholder="Search Facebook..."
        class="search-input"
        @focus="searchQuery.trim() && (showResults = true)"
      />
      <button v-if="searchQuery" @click="closeSearch" class="clear-btn">✕</button>
    </div>
    
    <!-- Search Results Dropdown -->
    <div v-if="showResults" class="search-results" @click.stop>
      <!-- Tabs -->
      <div class="search-tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.id"
          @click="activeTab = tab.id"
          :class="['tab-btn', activeTab === tab.id ? 'active' : '']"
        >
          {{ tab.label }}
        </button>
      </div>
      
      <!-- Loading -->
      <div v-if="loading" class="search-loading">
        <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
      </div>
      
      <!-- Results -->
      <div v-else-if="hasResults" class="results-list">
        <!-- Users -->
        <div v-if="filteredResults.users.length > 0" class="result-section">
          <h4 v-if="activeTab === 'all'">People</h4>
          <div 
            v-for="user in filteredResults.users" 
            :key="user.id"
            @click="goToUser(user.id)"
            class="result-item"
          >
            <div class="result-avatar">
              <img v-if="user.profilePicUrl" :src="user.profilePicUrl" class="w-full h-full object-cover" />
              <span v-else>{{ user.username?.charAt(0)?.toUpperCase() }}</span>
            </div>
            <div class="result-info">
              <span class="result-name">{{ user.username }}</span>
              <span class="result-type">Person</span>
            </div>
          </div>
        </div>
        
        <!-- Posts -->
        <div v-if="filteredResults.posts.length > 0" class="result-section">
          <h4 v-if="activeTab === 'all'">Posts</h4>
          <div 
            v-for="post in filteredResults.posts" 
            :key="post.id"
            @click="router.push('/')"
            class="result-item"
          >
            <div class="result-avatar post-icon">📝</div>
            <div class="result-info">
              <span class="result-name">{{ post.content?.substring(0, 50) }}{{ post.content?.length > 50 ? '...' : '' }}</span>
              <span class="result-type">Post</span>
            </div>
          </div>
        </div>
        
        <!-- Groups -->
        <div v-if="filteredResults.groups.length > 0" class="result-section">
          <h4 v-if="activeTab === 'all'">Groups</h4>
          <div 
            v-for="group in filteredResults.groups" 
            :key="group.id"
            @click="goToGroup(group.id)"
            class="result-item"
          >
            <div class="result-avatar group-icon">👥</div>
            <div class="result-info">
              <span class="result-name">{{ group.name }}</span>
              <span class="result-type">Group · {{ group.memberCount }} members</span>
            </div>
          </div>
        </div>
        
        <!-- Events -->
        <div v-if="filteredResults.events.length > 0" class="result-section">
          <h4 v-if="activeTab === 'all'">Events</h4>
          <div 
            v-for="event in filteredResults.events" 
            :key="event.id"
            @click="goToEvent(event.id)"
            class="result-item"
          >
            <div class="result-avatar event-icon">📅</div>
            <div class="result-info">
              <span class="result-name">{{ event.title }}</span>
              <span class="result-type">Event</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- No Results -->
      <div v-else-if="searchQuery.trim() && !loading" class="no-results">
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
  background: #f0f2f5;
  border-radius: 20px;
  padding: 0 12px;
}

:global(.dark) .search-input-wrapper {
  background: #374151;
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
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-height: 400px;
  overflow-y: auto;
  z-index: 100;
}

:global(.dark) .search-results {
  background: #1f2937;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

.search-tabs {
  display: flex;
  padding: 8px;
  border-bottom: 1px solid #e4e6eb;
  gap: 4px;
}

:global(.dark) .search-tabs {
  border-color: #374151;
}

.tab-btn {
  padding: 6px 12px;
  border: none;
  background: transparent;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  color: #65676b;
}

.tab-btn:hover {
  background: #f0f2f5;
}

.tab-btn.active {
  background: #e7f3ff;
  color: #1877f2;
}

:global(.dark) .tab-btn {
  color: #9ca3af;
}

:global(.dark) .tab-btn:hover {
  background: #374151;
}

:global(.dark) .tab-btn.active {
  background: #1e3a5f;
  color: #60a5fa;
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
  color: #65676b;
  margin: 8px 12px;
  font-weight: 600;
}

:global(.dark) .result-section h4 {
  color: #9ca3af;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.result-item:hover {
  background: #f0f2f5;
}

:global(.dark) .result-item:hover {
  background: #374151;
}

.result-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e4e6eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #65676b;
  overflow: hidden;
}

:global(.dark) .result-avatar {
  background: #4b5563;
  color: #e5e7eb;
}

.post-icon, .group-icon, .event-icon {
  font-size: 18px;
  background: #e7f3ff;
}

:global(.dark) .post-icon,
:global(.dark) .group-icon,
:global(.dark) .event-icon {
  background: #1e3a5f;
}

.result-info {
  flex: 1;
  min-width: 0;
}

.result-name {
  display: block;
  font-weight: 500;
  color: #1c1e21;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:global(.dark) .result-name {
  color: #f3f4f6;
}

.result-type {
  display: block;
  font-size: 12px;
  color: #65676b;
}

:global(.dark) .result-type {
  color: #9ca3af;
}

.no-results {
  padding: 20px;
  text-align: center;
  color: #65676b;
}

:global(.dark) .no-results {
  color: #9ca3af;
}
</style>
