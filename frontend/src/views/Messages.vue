<template>
  <div class="min-h-screen bg-gray-100 dark:bg-gray-900 pt-16">
    <div class="container mx-auto px-4 py-4 max-w-5xl">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden" style="height: calc(100vh - 140px);">
        <div class="flex h-full">
          <!-- Conversations List -->
          <div class="w-1/3 border-r dark:border-gray-700 flex flex-col">
            <div class="p-4 border-b dark:border-gray-700">
              <h2 class="text-xl font-bold dark:text-gray-100">Messages</h2>
            </div>
            
            <!-- New Message Button -->
            <div class="p-3 border-b dark:border-gray-700">
              <button 
                @click="showNewMessageModal = true"
                class="w-full py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm font-medium"
              >
                + New Message
              </button>
            </div>

            <!-- Conversations -->
            <div class="flex-1 overflow-y-auto">
              <div v-if="loadingConversations" class="p-4 text-center">
                <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
              </div>
              <div v-else-if="conversations.length === 0" class="p-4 text-center text-gray-500 dark:text-gray-400">
                No conversations yet
              </div>
              <div 
                v-else
                v-for="conv in conversations" 
                :key="conv.partnerId"
                @click="selectConversation(conv)"
                :class="[
                  'p-4 border-b dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition',
                  selectedPartnerId === conv.partnerId ? 'bg-blue-50 dark:bg-blue-900' : ''
                ]"
              >
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 rounded-full overflow-hidden bg-blue-100 flex items-center justify-center flex-shrink-0">
                    <img v-if="conv.partnerPic" :src="conv.partnerPic" class="w-full h-full object-cover" />
                    <span v-else class="text-blue-600 font-bold">{{ conv.partnerName?.charAt(0)?.toUpperCase() }}</span>
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex justify-between items-center">
                      <span class="font-semibold text-gray-800 dark:text-gray-100">{{ conv.partnerName }}</span>
                      <span class="text-xs text-gray-400">{{ formatTime(conv.lastMessageTime) }}</span>
                    </div>
                    <p :class="['text-sm truncate', conv.unread ? 'font-semibold text-gray-800 dark:text-gray-100' : 'text-gray-500 dark:text-gray-400']">
                      {{ conv.isFromMe ? 'You: ' : '' }}{{ conv.lastMessage }}
                    </p>
                  </div>
                  <div v-if="conv.unread" class="w-3 h-3 bg-blue-600 rounded-full"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Chat Area -->
          <div class="flex-1 flex flex-col">
            <div v-if="!selectedPartnerId" class="flex-1 flex items-center justify-center text-gray-400 dark:text-gray-500">
              <div class="text-center">
                <div class="text-6xl mb-4">💬</div>
                <p>Select a conversation or start a new one</p>
              </div>
            </div>

            <template v-else>
              <!-- Chat Header -->
              <div class="p-4 border-b dark:border-gray-700 flex items-center gap-3">
                <div class="w-10 h-10 rounded-full overflow-hidden bg-blue-100 flex items-center justify-center">
                  <img v-if="selectedPartnerPic" :src="selectedPartnerPic" class="w-full h-full object-cover" />
                  <span v-else class="text-blue-600 font-bold">{{ selectedPartnerName?.charAt(0)?.toUpperCase() }}</span>
                </div>
                <span class="font-semibold dark:text-gray-100">{{ selectedPartnerName }}</span>
              </div>

              <!-- Messages -->
              <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 space-y-3">
                <div v-if="loadingMessages" class="text-center py-4">
                  <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
                </div>
                <div 
                  v-else
                  v-for="msg in messages" 
                  :key="msg.id"
                  :class="['flex', msg.senderId === authStore.user?.id ? 'justify-end' : 'justify-start']"
                >
                  <div 
                    :class="[
                      'max-w-[70%] px-4 py-2 rounded-2xl',
                      msg.senderId === authStore.user?.id 
                        ? 'bg-blue-600 text-white rounded-br-md' 
                        : 'bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-100 rounded-bl-md'
                    ]"
                  >
                    <p>{{ msg.content }}</p>
                    <p :class="['text-xs mt-1', msg.senderId === authStore.user?.id ? 'text-blue-200' : 'text-gray-400']">
                      {{ formatTime(msg.createdAt) }}
                    </p>
                  </div>
                </div>
              </div>

              <!-- Message Input -->
              <div class="p-4 border-t dark:border-gray-700">
                <div class="flex gap-2">
                  <input 
                    v-model="newMessage"
                    @keyup.enter="sendMessage"
                    type="text"
                    placeholder="Type a message..."
                    class="flex-1 px-4 py-2 border dark:border-gray-600 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white dark:bg-gray-700 dark:text-gray-100 dark:placeholder-gray-400"
                  />
                  <button 
                    @click="sendMessage"
                    :disabled="!newMessage.trim() || sending"
                    class="px-6 py-2 bg-blue-600 text-white rounded-full hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {{ sending ? '...' : 'Send' }}
                  </button>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- New Message Modal -->
    <div v-if="showNewMessageModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click="showNewMessageModal = false">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md mx-4" @click.stop>
        <h3 class="text-xl font-bold mb-4 dark:text-gray-100">New Message</h3>
        <p class="text-gray-600 dark:text-gray-400 mb-4">Select a friend to message:</p>
        
        <div v-if="friendStore.loading" class="text-center py-4">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        </div>
        
        <div v-else-if="friendStore.friends.length === 0" class="text-gray-500 dark:text-gray-400 text-center py-4">
          No friends yet. Add friends to start messaging!
        </div>
        
        <div v-else class="max-h-64 overflow-y-auto space-y-2">
          <div 
            v-for="friend in friendStore.friends" 
            :key="friend.id"
            @click="startConversation(friend)"
            class="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer"
          >
            <div class="w-10 h-10 rounded-full overflow-hidden bg-blue-100 flex items-center justify-center">
              <img v-if="friend.profilePicUrl" :src="friend.profilePicUrl" class="w-full h-full object-cover" />
              <span v-else class="text-blue-600 font-bold">{{ friend.username?.charAt(0)?.toUpperCase() }}</span>
            </div>
            <span class="font-medium">{{ friend.username }}</span>
          </div>
        </div>
        
        <div class="mt-4 flex justify-end">
          <button @click="showNewMessageModal = false" class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useFriendStore } from '../stores/friends';
import api from '../utils/api';

const router = useRouter();
const authStore = useAuthStore();
const friendStore = useFriendStore();

const conversations = ref([]);
const messages = ref([]);
const loadingConversations = ref(true);
const loadingMessages = ref(false);
const selectedPartnerId = ref(null);
const selectedPartnerName = ref('');
const selectedPartnerPic = ref('');
const newMessage = ref('');
const sending = ref(false);
const showNewMessageModal = ref(false);
const messagesContainer = ref(null);

onMounted(async () => {
  await fetchConversations();
  friendStore.fetchFriends();
});

const fetchConversations = async () => {
  loadingConversations.value = true;
  try {
    const response = await api.get('/messages/conversations');
    conversations.value = response.data;
  } catch (e) {
    console.error('Failed to fetch conversations', e);
  } finally {
    loadingConversations.value = false;
  }
};

const selectConversation = async (conv) => {
  selectedPartnerId.value = conv.partnerId;
  selectedPartnerName.value = conv.partnerName;
  selectedPartnerPic.value = conv.partnerPic;
  await fetchMessages();
};

const startConversation = async (friend) => {
  selectedPartnerId.value = friend.id;
  selectedPartnerName.value = friend.username;
  selectedPartnerPic.value = friend.profilePicUrl || '';
  showNewMessageModal.value = false;
  await fetchMessages();
};

const fetchMessages = async () => {
  if (!selectedPartnerId.value) return;
  loadingMessages.value = true;
  try {
    const response = await api.get(`/messages/conversation/${selectedPartnerId.value}`);
    messages.value = response.data;
    await nextTick();
    scrollToBottom();
  } catch (e) {
    console.error('Failed to fetch messages', e);
  } finally {
    loadingMessages.value = false;
  }
};

const sendMessage = async () => {
  if (!newMessage.value.trim() || !selectedPartnerId.value) return;
  
  sending.value = true;
  try {
    const response = await api.post('/messages/send', {
      receiverId: selectedPartnerId.value,
      content: newMessage.value.trim()
    });
    messages.value.push(response.data);
    newMessage.value = '';
    await nextTick();
    scrollToBottom();
    await fetchConversations();
  } catch (e) {
    console.error('Failed to send message', e);
    alert('Failed to send message');
  } finally {
    sending.value = false;
  }
};

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

const formatTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return 'Just now';
  if (minutes < 60) return `${minutes}m`;
  if (hours < 24) return `${hours}h`;
  if (days < 7) return `${days}d`;
  return date.toLocaleDateString();
};
</script>
