<template>
  <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
    <el-tab-pane label="商品" name="products"></el-tab-pane>
    <el-tab-pane label="优惠券" name="promotion"></el-tab-pane>
    <el-tab-pane label="购物车" name="cart"></el-tab-pane>
    <el-tab-pane label="我的" name="profile"></el-tab-pane>
    <el-tab-pane label="订单" name="order"></el-tab-pane>
    <el-tab-pane label="退出登录" name="logout"></el-tab-pane>
  </el-tabs>
  <div class="search-container">
    <el-input 
      v-model="searchKeyword" 
      placeholder="输入商品描述搜索" 
      clearable 
      @keyup.enter="handleSearch"
      class="search-input"
    >
      <template #append>
        <el-button-group>
          <el-button 
            @click="handleSearch" 
            class="search-btn"
          >
            <font-awesome-icon :icon="['fas', 'magnifying-glass']" class="search-icon"/>
            <span class="search-text">搜索</span>
          </el-button>
          <el-button 
            @click="handleAIClick" 
            class="ai-btn"
            :disabled="isAILoading"
          >
            <el-icon v-if="!isAILoading" class="magic-icon"><MagicStick /></el-icon>
            <el-icon v-else class="loading-icon"><Loading /></el-icon>
            <span class="ai-text">
              {{ isAILoading ? '润色中...' : 'AI润色✨' }}
            </span>
          </el-button>
        </el-button-group>
      </template>
    </el-input>
  </div>
  <router-view></router-view>

  <!-- 商品展示部分 -->
  <div class="product-list">
    <div 
      v-for="product in showProducts" 
      :key="product.id" 
      class="product-item"
    >
      <img :src="product.image" alt="商品图片" class="product-image" />
      <div class="product-info">
        <h3>{{ product.name }}</h3>
        <p class="product-price">价格: {{ product.price }}</p>
        <p class="product-description">介绍: {{ product.description }}</p>
        <p class="product-stock">库存: {{ product.stock }}</p>
      </div>
      <!-- 添加加号按钮 -->
      <button @click="addProductToCart(product.id)" class="add-to-cart-button">
        <span>+ 添加到购物车</span>
      </button>
    </div>
    <!-- 加载动画，在加载时显示 -->
    <div v-if="isLoading" class="loading-spinner">
      <i class="el-icon-loading"></i>
    </div>
    <!-- 分页组件放在商品列表下方 -->
    <div class="pagination" v-if="!hasSearched">
      <button @click="prevPage" :disabled="currentPage === 1" class="pagination-button">上一页</button>
      <span>{{ currentPage }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage === totalPages" class="pagination-button">下一页</button>
    </div>
    <div v-if="showNoResults" class="no-results">
      没有找到相关商品
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import type { TabsPaneContext } from 'element-plus';
import { useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';

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
  } else if (currentRoute.name === 'order') {
    activeName.value = 'order';
  } else {
    activeName.value = 'promotion';
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
  } else if (name === 'order') {
    router.push({ name: 'order' });
  } else if (name === 'logout') {
    showLogoutConfirm();
  } else {
    router.push({ name: 'promotion' });
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

// 商品展示相关代码
// 每页显示的商品数量
const pageSize = ref(3);
// 当前页码
const currentPage = ref(1);
// 商品列表
const products = ref([]);
// 总商品数
const totalProducts = ref(0); // 初始值设为 0
// 加载状态
const isLoading = ref(false);

interface Product {
  id: number;
  name: string;
  price: number;
  description: string;
  stock: number;
  image: string;
}

const searchKeyword = ref('');
const searchResults = ref<Product[]>([]);
const isSearching = ref(false);

// 计算属性：显示的商品列表
const showProducts = computed(() => {
  return hasSearched.value ? searchResults.value : currentPageProducts.value;
});

const hasSearched = ref(false);

// 计算属性：显示无结果提示
const showNoResults = computed(() => {
  return hasSearched.value && !isLoading.value && searchResults.value.length === 0;
});


// 搜索处理方法
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = [];
    hasSearched.value = false;
    return;
  }
  try {
    hasSearched.value = true;
    isSearching.value = true;
    isLoading.value = true;
    
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      const confirmed = confirm('未找到令牌，请先登录');
      if (confirmed) router.push('/login');
      return;
    }

    const response = await axios.get('http://127.0.0.1:9997/shop/searchproduct', {
      headers: { Authorization: `Bearer ${token}` },
      params: { description: searchKeyword.value.trim() }
    });

    if (response.data.code === 200) {
      searchResults.value = response.data.data;
    } else {
      ElMessage.error('搜索失败: ' + (response.data.message || '未知错误'));
      searchResults.value = [];
    }
  } catch (error) {
    console.error('搜索请求出错:', error);
    ElMessage.error('搜索失败，请检查网络连接');
    searchResults.value = [];
  } finally {
    isSearching.value = false;
    isLoading.value = false;
  }
};

// 监听搜索关键字变化
watch(searchKeyword, (newVal) => {
  if (!newVal) {
    searchResults.value = [];
    hasSearched.value = false; // 重置搜索状态
  }
});

const isAILoading = ref(false);

const handleAIClick = async () => {
  try {
    isAILoading.value = true; 
    const text = searchKeyword.value.trim();
    if (!text) {
      ElMessage.warning('请输入需要润色的描述内容');
      return;
    }

    const token = localStorage.getItem('jwtToken');
    if (!token) {
      const confirmed = confirm('未找到令牌，请先登录');
      if (confirmed) router.push('/login');
      return;
    }
    
    const response = await axios.get('http://127.0.0.1:9997/shop/generate', {
      headers: { Authorization: `Bearer ${token}` },
      params: { message: text }
    });

    isLoading.value = true;

    if (response.data.code === 200) {
      searchKeyword.value = response.data.data;
      ElMessage.success('AI润色完成');
    } else {
      ElMessage.error('润色失败: ' + (response.data.message || '未知错误'));
    }
  } catch (error) {
    console.error('AI润色请求出错:', error);
    ElMessage.error('润色失败，请检查网络连接');
  } finally {
    isAILoading.value = false;
  }
};

// 获取当前页的商品
const currentPageProducts = computed(() => {
  if (!products.value) {
    return [];
  }
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return products.value.slice(start, end);
});

// 总页数
const totalPages = computed(() => {
  return Math.ceil(totalProducts.value / pageSize.value);
});

// 获取总商品数
const fetchTotalProductCount = async () => {
  try {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      const confirmed = confirm('未找到令牌，请先登录');
      if (confirmed) {
        router.push({ path: '/login' });
        return;
      }
    }
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const response = await instance.get('http://127.0.0.1:9997/shop/getproductsnum');
    if (response.data.code === 200) {
      totalProducts.value = response.data.data;
    } else {
      console.error('获取总商品数失败:', response.data.message);
    }
  } catch (error) {
    console.error('获取总商品数请求出错:', error);
  }
};

// 从后端获取商品数据
const fetchProducts = async () => {
  try {
    // 显示加载动画
    isLoading.value = true;
    // 先获取总商品数
    await fetchTotalProductCount();

    const token = localStorage.getItem('jwtToken');
    if (!token) {
      const confirmed = confirm('未找到令牌，请先登录');
      if (confirmed) {
        router.push({ path: '/login' });
        return;
      }
    }
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const queryParams = {
      page: currentPage.value,
      pageSize: pageSize.value
    };
    const response = await instance.post('http://127.0.0.1:9997/shop/getproductlist', queryParams);
    if (response.data.data && response.data.data.records) {
      if (currentPage.value === 1) {
        products.value = response.data.data.records;
      } else {
        products.value = products.value.concat(response.data.data.records);
      }
      console.log('更新后的 products 数据：', products.value);
    } else {
      console.error('后端返回的数据格式不正确');
    }
  } catch (error) {
    console.error('获取商品数据失败:', error);
    products.value = [];
  } finally {
    // 隐藏加载动画
    isLoading.value = false;
  }
};

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    fetchProducts();
  }
};

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    fetchProducts();
  }
};

// 添加商品到购物车
const addProductToCart = async (productId: number) => {
  try {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      const confirmed = confirm('未找到令牌，请先登录');
      if (confirmed) {
        router.push({ path: '/login' });
        return;
      }
    }
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const params = new URLSearchParams();
    params.append('productId', productId.toString());

    const response = await instance.post('http://127.0.0.1:9997/cart/addcart', params);
    console.log('添加商品到购物车的响应：', response.data);
    if (response.data.code === 200) {
      ElMessage.success('商品已成功添加到购物车');
    } else {
      ElMessage.error('添加商品到购物车失败，请稍后重试');
    }
  } catch (error) {
    console.error('添加商品到购物车失败:', error);
    ElMessage.error('添加商品到购物车失败，请稍后重试');
  }
};

onMounted(fetchProducts);
</script>


<style scoped>
/* 整体布局 */
.product-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
  padding: 20px;
}

/* 商品卡片 */
.product-item {
  width: 300px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
}

.product-item:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
}

/* 商品图片 */
.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

/* 商品信息 */
.product-info {
  padding: 15px;
}

.product-info h3 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.product-price {
  color: #e91e63;
  font-size: 16px;
  margin-bottom: 5px;
}

.product-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.product-stock {
  font-size: 14px;
  color: #888;
}

.search-container {
  margin: 20px auto;
  max-width: 600px;
  position: relative;
}

.search-input {
  width: 100%;
  border-radius: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.search-input:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}



/* 搜索按钮 */
.search-btn {
  padding: 0 25px !important;
  background: linear-gradient(135deg, #3a7afe, #2f5bff) !important;
  border: none !important;
  height: 40px;
  transition: all 0.3s ease;
  display: flex !important;
  align-items: center;
  gap: 10px;
  box-shadow: 0 3px 6px rgba(58, 122, 254, 0.2);
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 15px rgba(58, 122, 254, 0.3);
  opacity: 0.95;
}

.search-icon {
  font-size: 16px;
  color: white;
  transition: transform 0.2s ease;
}

.search-btn:hover .search-icon {
  transform: scale(1.1);
}

.search-text {
  color: white;
  font-weight: 600;
  letter-spacing: 0.8px;
  font-size: 14px;
}

/* 输入框聚焦效果优化 */
:deep(.el-input__inner) {
  border-radius: 30px !important;
  padding-left: 20px !important;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
  opacity: 0.95;
}

/* AI按钮 */
.ai-btn {
  padding: 0 ;
  margin-left: 0px;
  background: linear-gradient(135deg, #3dd2d2, #ae2cbf);
  border: none;
  border-radius: 25px;
  color: white;
  display: flex;
  height: 40px;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.ai-btn:hover {
  opacity: 0.9;
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.ai-btn::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transform: rotate(45deg);
  animation: shine 3s infinite;
}

@keyframes shine {
  0% { left: -50% }
  100% { left: 150% }
}

.magic-icon {
  font-size: 18px;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
}

.ai-text {
  font-weight: 500;
  letter-spacing: 0.5px;
  color: #333;
}

.el-button-group {
  display: flex;
}

.el-button {
  padding: 12px;
  border-radius: 0 4px 4px 0;
}

.el-button:first-child {
  border-radius: 0;
}

.el-button:last-child {
  border-radius: 0 4px 4px 0;
}

.el-button--success {
  background-color: #67c23a;
  border-color: #67c23a;
}

.el-button--success:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}

.no-results {
  font-size: 18px;
  padding: 30px;
  color: #909399;
  background: #f8f9fa;
  border-radius: 12px;
  margin: 40px auto;
  width: fit-content;
}

loading-icon {
  animation: spin 1s linear infinite;
  font-size: 16px;
}

.ai-btn[disabled] {
  opacity: 0.7;
  cursor: not-allowed;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 添加到购物车按钮 */
.add-to-cart-button {
  position: absolute;
  bottom: 15px;
  right: 15px;
  padding: 8px 15px;
  background-color: #409EFF;
  color: #fff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.add-to-cart-button:hover {
  background-color: #66b1ff;
}

/* 分页按钮 */
.pagination {
  margin: 40px 0;
  display: flex;
  justify-content: center;
  gap: 15px;
  align-items: center;
}

.pagination-button {
  padding: 8px 15px;
  background-color: #409EFF;
  color: #fff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
  margin: 0 5px;
}

.pagination-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.pagination-button:hover {
  background-color: #66b1ff;
}

/* 标签样式 */
.demo-tabs > .el-tabs__content {
  padding: 32px;
  color: #6b778c;
  font-size: 32px;
  font-weight: 600;
}

.el-tabs__item {
  font-size: 30px;
  padding: 40px 40px;
}

/* 美化确认窗口 */
.custom-message-box .el-message-box {
  width: 400px;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.custom-message-box .el-message-box__header {
  background-color: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
}

.custom-message-box .el-message-box__title {
  font-size: 18px;
  font-weight: bold;
}

.custom-message-box .el-message-box__content {
  font-size: 16px;
  color: #333;
}

.custom-message-box .el-message-box__btns {
  text-align: right;
}

.custom-message-box .el-button {
  padding: 8px 20px;
  border-radius: 5px;
}

.custom-message-box .el-button--primary {
  background-color: #409EFF;
  border-color: #409EFF;
  color: #fff;
}

.custom-message-box .el-button--default {
  background-color: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

/* 加载动画样式 */
.loading-spinner {
  text-align: center;
  margin: 20px 0;
}

.loading-spinner i {
  font-size: 32px;
  color: #67c23a;
  filter: drop-shadow(0 2px 4px rgba(103, 194, 58, 0.2));
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(180deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>