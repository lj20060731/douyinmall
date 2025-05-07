<template>
  <div id="app" :style="backgroundStyle">
    <router-view></router-view>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router';
import { ref, watch } from 'vue';

// 获取当前路由信息
const route = useRoute();
// 用于存储背景样式的响应式变量
const backgroundStyle = ref({});

// 监听路由名称的变化
watch(() => route.name, (newRouteName) => {
  if (newRouteName === 'login') {
    // 如果当前路由名称是 'login'，则设置背景样式
    backgroundStyle.value = {
      backgroundImage: `url('/image/beijing.jpg')`,
      backgroundRepeat: 'no-repeat',
      backgroundSize: 'cover'
    };
  } else {
    // 如果不是 'login' 路由，则清空背景样式
    backgroundStyle.value = {};
  }
}, { immediate: true }); // immediate: true 表示在监听开始时立即执行一次回调函数
</script>

<style>
#app {
  height: 100vh;
}
</style>