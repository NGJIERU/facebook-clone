import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/auth/, ''),
      },
      '/api/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/friends': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/feed/graphql': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/feed\/graphql/, '/graphql'),
      },
      '/api/feed': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/media': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/api/notifications': {
        target: 'http://localhost:8084',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/notifications/, '/api/notifications'), // No rewrite needed if controller matches, but ensuring consistency
      },
      '/ws': {
        target: 'http://localhost:8084',
        ws: true,
        changeOrigin: true,
      },
    },
  },
})
