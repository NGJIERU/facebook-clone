<template>
  <div class="min-h-screen bg-gray-100 dark:bg-gray-900">
    <!-- Navbar -->
    <nav class="bg-white dark:bg-gray-800 shadow sticky top-0 z-50">
      <div class="container mx-auto px-4 h-16 flex justify-between items-center">
        <h1 class="text-2xl font-bold text-blue-600 cursor-pointer" @click="router.push('/')">Facebook</h1>
        <div class="flex items-center gap-6">
          <button @click="router.push('/')" class="text-gray-600 dark:text-gray-300 hover:text-blue-600 font-medium">Home</button>
          <button @click="router.push('/friends')" class="text-gray-600 dark:text-gray-300 hover:text-blue-600 font-medium">Friends</button>
        </div>
      </div>
    </nav>

    <div class="container mx-auto px-4 py-8 max-w-4xl">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-3xl font-bold text-gray-800 dark:text-gray-100">Events</h2>
        <button 
          @click="showCreateModal = true"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium"
        >
          + Create Event
        </button>
      </div>

      <!-- Tabs -->
      <div class="flex gap-4 mb-6">
        <button 
          @click="activeTab = 'upcoming'"
          :class="['px-4 py-2 rounded-lg font-medium', activeTab === 'upcoming' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700']"
        >
          Upcoming
        </button>
        <button 
          @click="activeTab = 'my'"
          :class="['px-4 py-2 rounded-lg font-medium', activeTab === 'my' ? 'bg-blue-600 text-white' : 'bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700']"
        >
          My Events
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-10">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600 mx-auto"></div>
      </div>

      <!-- Events List -->
      <div v-else class="space-y-4">
        <div 
          v-for="event in displayedEvents" 
          :key="event.id"
          class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden hover:shadow-lg transition cursor-pointer"
          @click="viewEvent(event)"
        >
          <div class="flex">
            <div 
              class="w-48 h-32 bg-gradient-to-r from-purple-500 to-pink-500 flex-shrink-0"
              :style="event.coverImageUrl ? { backgroundImage: `url(${event.coverImageUrl})`, backgroundSize: 'cover' } : {}"
            ></div>
            <div class="p-4 flex-1">
              <div class="text-sm text-purple-600 font-medium mb-1">{{ formatEventDate(event.eventDate) }}</div>
              <h3 class="font-bold text-lg text-gray-800 dark:text-gray-100 mb-1">{{ event.title }}</h3>
              <p v-if="event.location" class="text-gray-500 dark:text-gray-400 text-sm mb-2">📍 {{ event.location }}</p>
              <div class="flex gap-4 text-sm text-gray-500 dark:text-gray-400">
                <span>{{ event.attendeesCount }} going</span>
                <span>{{ event.interestedCount }} interested</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && displayedEvents.length === 0" class="text-center py-10 text-gray-500 dark:text-gray-400">
        {{ activeTab === 'my' ? "You haven't RSVP'd to any events yet." : "No upcoming events." }}
      </div>
    </div>

    <!-- Create Event Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click="showCreateModal = false">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md mx-4 max-h-[90vh] overflow-y-auto" @click.stop>
        <h3 class="text-xl font-bold mb-4">Create New Event</h3>
        
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Event Title *</label>
            <input 
              v-model="newEvent.title"
              type="text"
              class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter event title"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Date & Time *</label>
            <input 
              v-model="newEvent.eventDate"
              type="datetime-local"
              class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Location</label>
            <input 
              v-model="newEvent.location"
              type="text"
              class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Where is this event?"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea 
              v-model="newEvent.description"
              class="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              rows="3"
              placeholder="What is this event about?"
            ></textarea>
          </div>
        </div>

        <div class="mt-6 flex justify-end gap-2">
          <button @click="showCreateModal = false" class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded">Cancel</button>
          <button 
            type="button"
            @click="createEvent"
            :disabled="creating || !newEvent.title || !newEvent.title.trim() || !newEvent.eventDate"
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            {{ creating ? 'Creating...' : 'Create Event' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Event Detail Modal -->
    <div v-if="selectedEvent" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click="selectedEvent = null">
      <div class="bg-white rounded-lg w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto" @click.stop>
        <div 
          class="h-48 bg-gradient-to-r from-purple-500 to-pink-500"
          :style="selectedEvent.coverImageUrl ? { backgroundImage: `url(${selectedEvent.coverImageUrl})`, backgroundSize: 'cover' } : {}"
        ></div>
        <div class="p-6">
          <div class="text-purple-600 font-medium mb-2">{{ formatEventDate(selectedEvent.eventDate) }}</div>
          <h3 class="text-2xl font-bold text-gray-800 mb-2">{{ selectedEvent.title }}</h3>
          <p v-if="selectedEvent.location" class="text-gray-500 mb-4">📍 {{ selectedEvent.location }}</p>
          
          <div class="flex gap-4 mb-4 text-sm">
            <span class="bg-purple-100 text-purple-700 px-3 py-1 rounded-full">{{ selectedEvent.attendeesCount }} going</span>
            <span class="bg-pink-100 text-pink-700 px-3 py-1 rounded-full">{{ selectedEvent.interestedCount }} interested</span>
          </div>
          
          <p v-if="selectedEvent.description" class="text-gray-600 mb-6">{{ selectedEvent.description }}</p>
          
          <!-- RSVP Buttons -->
          <div class="flex gap-3">
            <button 
              @click="rsvp('GOING')"
              :disabled="rsvping"
              :class="[
                'flex-1 py-2 rounded-lg font-medium transition',
                currentRsvpStatus === 'GOING' 
                  ? 'bg-purple-600 text-white' 
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              {{ currentRsvpStatus === 'GOING' ? '✓ Going' : 'Going' }}
            </button>
            <button 
              @click="rsvp('INTERESTED')"
              :disabled="rsvping"
              :class="[
                'flex-1 py-2 rounded-lg font-medium transition',
                currentRsvpStatus === 'INTERESTED' 
                  ? 'bg-pink-600 text-white' 
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              ]"
            >
              {{ currentRsvpStatus === 'INTERESTED' ? '✓ Interested' : 'Interested' }}
            </button>
            <button 
              v-if="currentRsvpStatus"
              @click="cancelRsvp"
              :disabled="rsvping"
              class="px-4 py-2 text-red-500 hover:bg-red-50 rounded-lg"
            >
              Cancel
            </button>
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

const activeTab = ref('upcoming');
const loading = ref(true);
const upcomingEvents = ref([]);
const myEvents = ref([]);
const showCreateModal = ref(false);
const creating = ref(false);
const selectedEvent = ref(null);
const currentRsvpStatus = ref(null);
const rsvping = ref(false);

const newEvent = ref({
  title: '',
  description: '',
  location: '',
  eventDate: ''
});

onMounted(async () => {
  await fetchEvents();
});

watch(activeTab, async () => {
  if (activeTab.value === 'my') {
    await fetchMyEvents();
  }
});

const fetchEvents = async () => {
  loading.value = true;
  try {
    const response = await api.get('/events');
    upcomingEvents.value = response.data;
  } catch (e) {
    console.error('Failed to fetch events', e);
  } finally {
    loading.value = false;
  }
};

const fetchMyEvents = async () => {
  loading.value = true;
  try {
    const response = await api.get('/events/my');
    myEvents.value = response.data;
  } catch (e) {
    console.error('Failed to fetch my events', e);
  } finally {
    loading.value = false;
  }
};

const displayedEvents = computed(() => {
  return activeTab.value === 'my' ? myEvents.value : upcomingEvents.value;
});

const createEvent = async () => {
  console.log('createEvent called', newEvent.value);
  if (!newEvent.value.title.trim() || !newEvent.value.eventDate) {
    console.log('Validation failed - title or date missing');
    return;
  }
  
  creating.value = true;
  try {
    const eventDate = new Date(newEvent.value.eventDate).toISOString();
    console.log('Sending request to create event', { ...newEvent.value, eventDate });
    await api.post('/events', {
      ...newEvent.value,
      eventDate
    });
    console.log('Event created successfully');
    showCreateModal.value = false;
    newEvent.value = { title: '', description: '', location: '', eventDate: '' };
    await fetchEvents();
  } catch (e) {
    console.error('Failed to create event', e);
    alert('Failed to create event: ' + (e.response?.data || e.message));
  } finally {
    creating.value = false;
  }
};

const viewEvent = async (event) => {
  selectedEvent.value = event;
  try {
    const response = await api.get(`/events/${event.id}/rsvp/status`);
    currentRsvpStatus.value = response.data.status;
  } catch (e) {
    currentRsvpStatus.value = null;
  }
};

const rsvp = async (status) => {
  if (!selectedEvent.value) return;
  // Don't do anything if clicking the same status
  if (currentRsvpStatus.value === status) return;
  
  const previousStatus = currentRsvpStatus.value;
  rsvping.value = true;
  try {
    await api.post(`/events/${selectedEvent.value.id}/rsvp`, { status });
    
    // Update counts locally based on previous and new status
    if (previousStatus === 'GOING') selectedEvent.value.attendeesCount--;
    if (previousStatus === 'INTERESTED') selectedEvent.value.interestedCount--;
    if (status === 'GOING') selectedEvent.value.attendeesCount++;
    if (status === 'INTERESTED') selectedEvent.value.interestedCount++;
    
    currentRsvpStatus.value = status;
    await fetchMyEvents();
  } catch (e) {
    console.error('Failed to RSVP', e);
  } finally {
    rsvping.value = false;
  }
};

const cancelRsvp = async () => {
  if (!selectedEvent.value) return;
  rsvping.value = true;
  try {
    await api.delete(`/events/${selectedEvent.value.id}/rsvp`);
    
    if (currentRsvpStatus.value === 'GOING') selectedEvent.value.attendeesCount--;
    if (currentRsvpStatus.value === 'INTERESTED') selectedEvent.value.interestedCount--;
    
    currentRsvpStatus.value = null;
    await fetchMyEvents();
  } catch (e) {
    console.error('Failed to cancel RSVP', e);
  } finally {
    rsvping.value = false;
  }
};

const formatEventDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', { 
    weekday: 'short', 
    month: 'short', 
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  });
};
</script>
