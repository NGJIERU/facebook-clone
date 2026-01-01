import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue'
import router from './router'

import { setupApollo } from './utils/apollo'

const app = createApp(App)

app.use(createPinia())
app.use(router)
setupApollo()

app.mount('#app')
