import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import Feed from '../views/Feed.vue';
import Friends from '../views/Friends.vue';
import Profile from '../views/Profile.vue';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: Login
        },
        {
            path: '/register',
            name: 'register',
            component: Register
        },
        {
            path: '/',
            name: 'feed',
            component: Feed,
            meta: { requiresAuth: true }
        },
        {
            path: '/friends',
            name: 'friends',
            component: Friends,
            meta: { requiresAuth: true }
        },
        {
            path: '/profile/:userId?',
            name: 'profile',
            component: Profile,
            meta: { requiresAuth: true }
        }
    ]
});

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login');
    } else {
        next();
    }
});

export default router;
