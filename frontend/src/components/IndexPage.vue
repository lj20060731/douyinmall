<template>
  <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
    <el-tab-pane label="商品" name="products"></el-tab-pane>
    <el-tab-pane label="购物车" name="cart"></el-tab-pane>
    <el-tab-pane label="我的" name="profile"></el-tab-pane>
    <el-tab-pane label="退出登录" name="logout"></el-tab-pane>
  </el-tabs>
  <router-view></router-view>
</template>

<script lang="ts" setup>
import { ref ,onMounted,watch} from 'vue'
import type { TabsPaneContext } from 'element-plus'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus';

// 导航条相关代码
const activeName = ref('');
const router = useRouter();

// 根据当前路由动态设置 activeName
const setActiveName = () => {
  const currentRoute = router.currentRoute.value;
  if (currentRoute.name === 'products') {
    activeName.value = 'products';
  } else if (currentRoute.name === 'cart') {
    activeName.value = 'cart';
  } else if (currentRoute.name === 'profile') {
    activeName.value = 'profile';
  }
};

// 初始化时设置 activeName
onMounted(() => {
  setActiveName();
});

// 监听路由变化，更新 activeName
watch(() => router.currentRoute.value, () => {
  setActiveName();
});
const handleClick = (tab: TabsPaneContext, event: Event) => {
  const { name } = tab.props;
  if (name === 'products') {
    router.push({ name: 'products' });
  } else if (name === 'cart') {
    router.push({ name: 'cart' });
  } else if (name === 'profile') {
    router.push({ name: 'profile' });
  } else if (name === 'logout') {
    showLogoutConfirm();
  }
};

const showLogoutConfirm = () => {
  ElMessageBox.confirm('你确定要退出登录吗?', '确认退出', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'custom-message-box'
  })
  .then(() => {
    // 清除 localStorage 数据
    localStorage.clear();
    router.replace({ path: '/login' });
  })
  .catch(() => {
    router.push({ path: '/products' });
  });
};
</script>

<style>
.demo-tabs > .el-tabs__content {
  padding: 32px;
  color: #6b778c;
  font-size: 32px;
  font-weight: 600;
}

/* 让标签变大 */
.el-tabs__item {
  font-size: 30px;
  padding: 40px 40px;
}
</style>