import '@arco-design/web-vue/dist/arco.css';
import ArcoVue from '@arco-design/web-vue';
import { createApp } from 'vue';
import { createPinia } from 'pinia';

import App from './App.vue';
import router from './router';

const pinia = createPinia()

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(ArcoVue);
app.use(pinia)

app.mount('#app');

