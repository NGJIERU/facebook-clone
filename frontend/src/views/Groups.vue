<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Navbar -->
    <nav class="bg-white shadow sticky top-0 z-50">
      <div class="container mx-auto px-4 h-16 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-blue-600 cursor-pointer" @click="router.push('/')">Facebook</h1>
        <div class="flex items-center gap-6">
          <button @click="router.push('/')" class="text-gray-600 hover:text-blue-600 font-medium">Home</button>
          <button @click="router.push('/friends')" class="text-gray-600 hover:text-blue-600 font-medium">Friends</button>
        </div>
      </div>
    </nav>

    <div class="container mx-auto px-4 py-8 max-w-4xl">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-3xl font-bold text-gray-800">Groups</h2>
        <button 
          @click="showCreateModal = true"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
        >
          + Create Group
        </button>
      </div>

      <!-- Tabs -->
      <div class="flex gap-4 mb-6">
        <button 
          @click="activeTab = 'discover'"
          :class="['px-4 py-2 rounded-lg font-medium', activeTab === 'discover' ? 'bg-blue-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-100']"
        >
          Discover
        </button>
        <button 
          @click="activeTab = 'my'"
          :class="['px-4 py-2 rounded-lg font-medium', activeTab === 'my' ? 'bg-blue-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-100']"
        >
          My Groups
        </button>
      </div>

      <!-- Search -->
      <div class="mb-6">
        <input 
          v-model="searchQuery"
          @input="handleSearch"
          type="text"
          placeholder="Search groups..."
          class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-10">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600 mx-auto"></div>
      </div>

      <!-- Groups Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div 
          v-for="group in displayedGroups" 
          :key="group.id"
          class="bg-white rounded-lg shadow overflow-hidden hover:shadow-lg transition cursor-pointer"
          @click="viewGroup(group)"
        >
          <div 
            class="h-32 bg-gradient-to-r from-blue-500 to-purple-500"
            :style="group.coverImageUrl ? { backgroundImage: `url(${group.coverImageUrl})`, backgroundSize: 'cover' } : {}"
          ></div>
          <div class="p-4">
            <h3 class="font-bold text-lg text-gray-800">{{ group.name }}</h3>
            <p class="text-gray-500 text-sm mb-2">{{ group.membersCount }} members</p>
            <p v-if="group.description" class="text-gray-600 text-sm line-clamp-2">{{ group.description }}</p>
            <div class="mt-3 flex gap-2">
              <span v-if="group.public" class="text-xs bg-green-100 text-green-700 px-2 py-1 rounded">Public</span>
              <span v-else class="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded">Private</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && displayedGroups.length === 0" class="text-center py-10 text-gray-500">
        {{ activeTab === 'my' ? "You haven't joined any groups yet." : "No groups found." }}
      </div>
    </div>

    <!-- Create Group Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click="showCreateModal = false">
      <div class="bg-white rounded-lg p-6 w-full max-w-md mx-4" @click.stop>
        <h3 class="text-xl font-bold mb-4">Create New Group</h3>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Group Name *</label>
            <input 
              v-model="newGroup.name"
              type="text"
              class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter group name"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea 
              v-model="newGroup.description"
              class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              rows="3"
              placeholder="What is this group about?"
            ></textarea>
          </div>

          <div class="flex items-center gap-2">
            <input type="checkbox" v-model="newGroup.isPublic" id="isPublic" class="rounded" />
            <label for="isPublic" class="text-sm text-gray-700">Public group (anyone can join)</label>
          </div>
        </div>

        <div class="mt-6 flex justify-end gap-2">
          <button @click="showCreateModal = false" class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded">Cancel</button>
          <button 
            type="button"
            @click="createGroup"
            :disabled="creating || !newGroup.name || !newGroup.name.trim()"
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            {{ creating ? 'Creating...' : 'Create Group' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Group Detail Modal -->
    <div v-if="selectedGroup" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click="selectedGroup = null">
      <div class="bg-white rounded-lg w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto" @click.stop>
        <div 
          class="h-40 bg-gradient-to-r from-blue-500 to-purple-500"
          :style="selectedGroup.coverImageUrl ? { backgroundImage: `url(${selectedGroup.coverImageUrl})`, backgroundSize: 'cover' } : {}"
        ></div>
        <div class="p-6">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h3 class="text-2xl font-bold text-gray-800">{{ selectedGroup.name }}</h3>
              <p class="text-gray-500">{{ selectedGroup.membersCount }} members</p>
            </div>
            <button 
              v-if="!isMemberOfSelected"
              @click="joinGroup(selectedGroup.id)"
              :disabled="joining"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              {{ joining ? 'Joining...' : 'Join Group' }}
            </button>
            <button 
              v-else
              @click="leaveGroup(selectedGroup.id)"
              :disabled="leaving"
              class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-100 disabled:opacity-50"
            >
              {{ leaving ? 'Leaving...' : 'Leave Group' }}
            </button>
          </div>
          
          <p v-if="selectedGroup.description" class="text-gray-600 mb-4">{{ selectedGroup.description }}</p>
          
          <div class="flex gap-2">
            <span v-if="selectedGroup.public" class="text-sm bg-green-100 text-green-700 px-3 py-1 rounded">Public</span>
            <span v-else class="text-sm bg-gray-100 text-gray-700 px-3 py-1 rounded">Private</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import api from '../utils/api';

const router = useRouter();

const activeTab = ref('discover');
const loading = ref(true);
const publicGroups = ref([]);
const myGroups = ref([]);
const searchQuery = ref('');
const searchResults = ref([]);
const showCreateModal = ref(false);
const creating = ref(false);
const selectedGroup = ref(null);
const isMemberOfSelected = ref(false);
const joining = ref(false);
const leaving = ref(false);

const newGroup = ref({
  name: '',
  description: '',
  isPublic: true
});

onMounted(async () => {
  await fetchGroups();
});

watch(activeTab, async () => {
  if (activeTab.value === 'my') {
    await fetchMyGroups();
  }
});

const fetchGroups = async () => {
  loading.value = true;
  try {
    const response = await api.get('/groups');
    publicGroups.value = response.data;
  } catch (e) {
    console.error('Failed to fetch groups', e);
  } finally {
    loading.value = false;
  }
};

const fetchMyGroups = async () => {
  loading.value = true;
  try {
    const response = await api.get('/groups/my');
    myGroups.value = response.data;
  } catch (e) {
    console.error('Failed to fetch my groups', e);
  } finally {
    loading.value = false;
  }
};

const displayedGroups = computed(() => {
  if (searchQuery.value.trim()) {
    return searchResults.value;
  }
  return activeTab.value === 'my' ? myGroups.value : publicGroups.value;
});

const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = [];
    return;
  }
  try {
    const response = await api.get(`/groups/search?query=${encodeURIComponent(searchQuery.value)}`);
    searchResults.value = response.data;
  } catch (e) {
    console.error('Search failed', e);
  }
};

const createGroup = async () => {
  console.log('createGroup called', newGroup.value);
  if (!newGroup.value.name.trim()) {
    console.log('Name is empty, returning');
    return;
  }
  
  creating.value = true;
  try {
    console.log('Sending request to create group');
    await api.post('/groups', newGroup.value);
    console.log('Group created successfully');
    showCreateModal.value = false;
    newGroup.value = { name: '', description: '', isPublic: true };
    await fetchGroups();
    await fetchMyGroups();
  } catch (e) {
    console.error('Failed to create group', e);
    alert('Failed to create group: ' + (e.response?.data || e.message));
  } finally {
    creating.value = false;
  }
};

const viewGroup = async (group) => {
  selectedGroup.value = group;
  try {
    const response = await api.get(`/groups/${group.id}/membership`);
    isMemberOfSelected.value = response.data.isMember;
  } catch (e) {
    isMemberOfSelected.value = false;
  }
};

const joinGroup = async (groupId) => {
  joining.value = true;
  try {
    await api.post(`/groups/${groupId}/join`);
    isMemberOfSelected.value = true;
    selectedGroup.value.membersCount++;
    await fetchMyGroups();
  } catch (e) {
    console.error('Failed to join group', e);
    alert('Failed to join group');
  } finally {
    joining.value = false;
  }
};

const leaveGroup = async (groupId) => {
  leaving.value = true;
  try {
    await api.delete(`/groups/${groupId}/leave`);
    isMemberOfSelected.value = false;
    selectedGroup.value.membersCount--;
    await fetchMyGroups();
  } catch (e) {
    console.error('Failed to leave group', e);
    alert('Failed to leave group');
  } finally {
    leaving.value = false;
  }
};
</script>
