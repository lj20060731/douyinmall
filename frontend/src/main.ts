// main.ts
import { createApp } from 'vue';
import App from './App.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import 'font-awesome/css/font-awesome.min.css';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faMinus, faPlus } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import router from './router';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import axios from 'axios';

dayjs.locale('zh-cn');

library.add(faMinus, faPlus);
const app = createApp(App);
app.use(router);
app.component('font-awesome-icon', FontAwesomeIcon);
app.use(ElementPlus, { locale: zhCn }); 
app.mount('#app');

// main.ts或axios配置文件
axios.defaults.transformResponse = [function (data) {
    return JSON.parse(data, (key, value) => {
      if ((key === 'ids' || key === 'couponIds') && Array.isArray(value)) {
        return value.map(v => v.toString());
      }
      return value;
    });
  }];