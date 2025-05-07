<template>
  <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
    <el-tab-pane label="商品" name="products"></el-tab-pane>
    <el-tab-pane label="优惠券" name="promotion"></el-tab-pane>
    <el-tab-pane label="购物车" name="cart"></el-tab-pane>
    <el-tab-pane label="我的" name="profile"></el-tab-pane>
    <el-tab-pane label="订单" name="order"></el-tab-pane>
    <el-tab-pane label="退出登录" name="logout"></el-tab-pane>
  </el-tabs>
  <router-view></router-view>

  <!-- 购物车内容部分 -->
  <div v-if="activeName === 'cart'" class="cart-container">
    <!-- 添加判断，如果 cartItems 为空则显示提示信息 -->
    <p v-if="cartItems.length === 0" class="no-products-message">当前暂无商品</p>
    <div class="cart-items-container">
      <div v-for="item in cartItems" :key="item.productId" class="cart-item">
        <img :src="item.image" alt="商品图片" class="product-image" />
        <div class="item-info">
          <h3>{{ item.name }}</h3>
          <p>单价: {{ item.price.toFixed(2) }}</p>
          <p>小计: {{ (item.price * item.number).toFixed(2) }}</p>
        </div>
        <div class="quantity-control">
          <span style="font-size: 18px;margin-right: 6px;">数量:</span>
          <el-button @click="decreaseQuantity(item.productId)" :disabled="item.number === 0">
            <font-awesome-icon icon="fa-minus" style="font-size: 20px; font-weight: bold;"></font-awesome-icon>
          </el-button>
          <span style="margin-left: 4px;margin-right: 4px;">{{ item.number }}</span>
          <el-button @click="increaseQuantity(item.productId)">
            <font-awesome-icon icon="fa-plus" style="font-size: 20px; font-weight: bold;"></font-awesome-icon>
          </el-button>
        </div>
      </div>
    </div>
    <!-- 当有商品时才显示总金额和结算按钮 -->
    <div v-if="cartItems.length > 0" class="total-amount">
      <p>总金额: {{ totalCartAmount.toFixed(2) }}</p>
    </div>
    <el-button v-if="cartItems.length > 0" @click="onCheckoutClick" type="primary" class="checkout-btn">结算</el-button>
  </div>

  <!-- 背景遮罩层 -->
  <div v-if="showCheckout" class="overlay" @click="showCheckout = false"></div>

  <!-- 结算页面-->
  <div v-if="showCheckout" class="checkout-container">
    <div class="checkout-header">
      <h2>结算页面</h2>
      <el-button @click="showCheckout = false" class="close-btn">关闭</el-button>
    </div>
    <!-- 地址信息 -->
    <div v-if="address" class="address-info">
      <h3>地址:</h3>
      <p>收货人: {{ address.consignee }}</p>
      <p>手机号: {{ address.phone }}</p>
      <p>性别: {{ address.sex === 0 ? '女' : '男' }}</p>
      <p>地址: {{ address.provinceName }} {{ address.cityName }} {{ address.districtName }} {{ address.detail }}</p>
      <el-button type="primary" @click="showAddressList = true" class="select-address-btn">选择其它地址</el-button>
    </div>
    <!-- 地址列表弹窗 -->
    <div v-if="showAddressList" class="address-list-modal">
      <div class="address-list-header">
        <h3>已有地址列表</h3>
        <el-button @click="showAddressList = false" class="close-address-list-btn">关闭</el-button>
      </div>
      <div class="address-list-container">
        <div v-for="(addr, index) in addressList" :key="index" class="address-item"  @click="selectAddress(addr)">
          <div class="address-info">
            <p>收货人: {{ addr.consignee }}</p>
            <p>手机号: {{ addr.phone }}</p>
            <p>性别: {{ addr.sex === 0 ? '女' : '男' }}</p>
            <p>地址: {{ addr.provinceName }} {{ addr.cityName }} {{ addr.districtName }} {{ addr.detail }}</p>
            <span v-if="addr.isDefault" class="default-tag">默认地址</span>
          </div>
        </div>
      </div>
    </div>
    <div class="checkout-items-container">
      <div v-for="item in cartItems" :key="item.productId" class="checkout-item">
        <img :src="item.image" alt="商品图片" class="product-image" />
        <div class="item-info">
          <h3>{{ item.name }}</h3>
          <p>单价: {{ item.price.toFixed(2) }}</p>
          <p>数量: {{ item.number }}</p>
          <p>小计: {{ (item.price * item.number).toFixed(2) }}</p>
        </div>
      </div>
    </div>
     <!-- 新增优惠券选择区域 -->
     <div class="coupon-selection">
      <h3>可用优惠券:</h3>
      <div class="coupon-groups">
        <div v-for="(group, index) in availableCouponGroups" 
            :key="index" 
            class="coupon-group-item"
            :class="{ 
              'selected-coupon': selectedCouponGroup === index,
              'unselected-coupon': selectedCouponGroup !== index
            }"
            @click="toggleCouponGroup(index)">
          <div class="coupon-group-info">
            <div class="coupon-header">
              <span class="coupon-count">{{ group.ids.length }}张可用</span>
              <span class="discount-amount">立减 ¥{{ group.discountAmount }}</span>
            </div>
            <div class="coupon-rules">
              <div v-for="(rule, i) in group.rules" :key="i" class="rule-item">
                <i class="el-icon-circle-check"></i>
                <span>{{ rule }}</span>
              </div>
            </div>
            <div class="checkmark" v-if="selectedCouponGroup === index">
              <i class="el-icon-circle-check"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="amount-summary">
      <p class="amount-line">
        <span>商品总价：</span>
        <span>¥{{ originalTotalAmount.toFixed(2) }}</span>
      </p>
      <p class="amount-line">
        <span>优惠金额：</span>
        <span class="discount">-¥{{ discountAmount.toFixed(2) }}</span>
      </p>
      <p class="amount-line total-amount">
        <span>应付总额：</span>
        <span>¥{{ (originalTotalAmount - discountAmount).toFixed(2) }}</span>
      </p>
    </div>
    <div class="total-amount">
      <p>总金额: ¥{{ (originalTotalAmount - discountAmount).toFixed(2) }}</p>
    </div>
    <el-button @click="confirmCheckout" type="primary" class="confirm-btn">确认结算</el-button>
  </div>

  <div v-if="showPaymentDialog" class="payment-dialog-overlay">
    <!-- 添加背景模糊效果 -->
    <div class="payment-dialog-background"></div>
    <div class="payment-dialog">
      <!-- 添加标题图标 -->
      <div class="payment-dialog-title">
        <i class="fa fa-credit-card"></i>
        <h3>请确认支付</h3>
      </div>
      <p class="payment-amount">支付金额: 
        <span class="amount">
          ¥{{ (originalTotalAmount - discountAmount).toFixed(2) }}
        </span>
      </p>
      <div class="payment-buttons">
        <el-button @click="handlePaymentCancel" type="danger" class="payment-button">取消支付</el-button>
        <el-button @click="handlePaymentConfirm" type="primary" class="payment-button">确认支付</el-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import type { TabsPaneContext} from 'element-plus';
import { useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

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
  } else{
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

// 获取令牌的函数
const getToken = () => {
  const token = localStorage.getItem('jwtToken');
  if (!token) {
    const confirmed = confirm('未找到令牌，请先登录');
    if (confirmed) {
      router.push({ path: '/login' });
    }
    return null;
  }
  return token;
};


// 类型定义部分
type ProductType = 1 | 2 | 3; // 使用 PascalCase 命名类型

interface ProductTypeItem {
  value: ProductType;
  desc: string;
}

// 值定义部分保持 camelCase
const ProductType: Record<string, ProductTypeItem> = {
  DIGIT: { value: 1, desc: "数码型" },
  FOOD: { value: 2, desc: "食品型" },
  CLOTHING: { value: 3, desc: "服装型" }
} as const;

// 购物车项定义
const cartItems = ref<{
  id: number,
  productId: number;
  name: string;
  image: string;
  number: number;
  price: number;
  amount: number;
  productType: ProductType
}[]>([]);


// 从后端获取购物车数据
const fetchCartItems = async () => {
  try {
    const token = getToken();
    if (!token) return;
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const response = await instance.get('http://127.0.0.1:9997/cart/getcart');
    cartItems.value = response.data.data;
  } catch (error) {
    ElMessage.error('获取购物车数据失败');
  }
};

// 计算购物车总金额
const totalCartAmount = computed(() => {
  let total = 0;
  cartItems.value.forEach(item => {
    total += item.price * item.number;
  });
  return total;
});

// 增加商品数量
const increaseQuantity = async (productId: number) => {
  try {
    const item = cartItems.value.find(item => item.productId === productId);
    if (item) {
      item.number++;
      // 向后端发送请求
      const token = getToken();
      if (!token) return;
      const instance = axios.create({
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const params = new URLSearchParams();
      params.append('productId', productId.toString());
      const response = await instance.post('http://127.0.0.1:9997/cart/addcart', params);
      if (response.status === 200) {
        ElMessage.success('增加商品数量成功');
      }
    }
  } catch (error) {
    ElMessage.error('增加商品数量失败');
  }
};

// 减少商品数量
const decreaseQuantity = async (productId: number) => {
  try {
    const item = cartItems.value.find(item => item.productId === productId);
    if (item && item.number > 0) {
      item.number--;
      if (item.number === 0) {
        // 数量为 0 时，从购物车列表中移除该商品
        cartItems.value = cartItems.value.filter(i => i.productId !== productId);
      }
      // 向后端发送请求
      const token = getToken();
      if (!token) return;
      const instance = axios.create({
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      const params = new URLSearchParams();
      params.append('productId', productId.toString());
      const response = await instance.post('http://127.0.0.1:9997/cart/subcart', params);
      if (response.status === 200) {
        ElMessage.success('减少商品数量成功');
      }
    }
  } catch (error) {
    ElMessage.error('减少商品数量失败');
  }
};

// 控制结算页面显示隐藏的状态变量
const showCheckout = ref(false);

const address = ref<Record<string, any>>({
  id:0,
  consignee: '',
  phone: '',
  sex: 0,
  address: [],
  detail: '',
  provinceName: '',
  cityName: '',
  districtName: ''
});

// 从后端获取默认地址
const fetchAddress = async () => {
  try {
    const token = getToken();
    if (!token) return;
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const response = await instance.get('http://127.0.0.1:9997/address/getdefault');
    if (response.data && response.data.data) {
      const addressStr = response.data.data.address;
      // 解析地址字符串
      const parsedAddress = parseAddressString(addressStr);
      address.value = parsedAddress;
    } 
  } catch (error) {
    ElMessage.error('获取默认地址失败');
  }
};

// 解析地址字符串的函数
const parseAddressString = (addressStr: string) => {
  const result: { [key: string]: string | number } = {};
  const startIndex = addressStr.indexOf('(') + 1;
  const endIndex = addressStr.lastIndexOf(')');
  const fields = addressStr.slice(startIndex, endIndex).split(', ');
  fields.forEach(field => {
    const [key, value] = field.split('=');
    result[key] = isNaN(Number(value)) ? value : Number(value);
  });
  return result;
};

// 控制地址列表弹窗显示隐藏的状态变量
const showAddressList = ref(false);
// 存储地址列表的响应式变量
const addressList = ref<Array<{
  id: number;
  consignee: string;
  phone: string;
  sex: number;
  provinceName: string;
  cityName: string;
  districtName: string;
  detail: string;
  isDefault: boolean;
}>>([]);

// 从后端获取地址列表
const fetchAddressList = async () => {
  try {
    const token = getToken();
    if (!token) return;
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const response = await instance.get('http://127.0.0.1:9997/address/get');
    if (response.data.code === 200) {
      addressList.value = response.data.data.map((addr: any) => ({
        ...addr,
        isDefault: addr.isDefault || false
      }));
    } else {
      ElMessage.error('获取地址失败，请稍后重试');
    }
  } catch (error) {
    ElMessage.error('获取地址失败，请稍后重试');
  }
};

// 选择地址的函数
const selectAddress = (addr: any) => {
  address.value = addr;
  showAddressList.value = false;
};

// 修改点击结算按钮的处理函数，添加获取地址列表逻辑
const onCheckoutClick = async () => {
  showCheckout.value = true;
  await fetchAddress();
  await fetchAddressList();
  await fetchAvailableCoupons(); 
};

// 新增支付相关状态
const showPaymentDialog = ref(false);
const payStatus = ref(0); // 0-未支付 1-取消 2-确认
const tempOrderData = ref<TempOrderData | null>(null);

// 确保数据结构正确
interface CouponGroup {
  ids: string[];
  rules: string[];
  discountAmount: number;
  discountDetail: Record<number, number>;
}

interface TempOrderData {
  addressId: number;
  cartId: number[];
  totalAmount: number;
  couponIds: string[];
}


// 新增响应式数据
const availableCouponGroups = ref<CouponGroup[]>([
  {
    ids: [],
    rules: [],
    discountAmount: 0,
    discountDetail: {}
  }
]);
const selectedCouponGroup = ref<number>(-1);
const originalTotalAmount = ref(0);
const discountAmount = ref(0);


// 确认结算函数
const confirmCheckout = async () => {
  try {
    const token = getToken();
    if (!token) return;
    
    if (!address.value.id) {
      ElMessage.warning('请选择配送地址');
      return;
    }

    // 计算实际支付金额
    const finalAmount = originalTotalAmount.value - discountAmount.value;
    const selectedCouponIds = selectedCouponGroup.value !== -1 
      ? availableCouponGroups.value[selectedCouponGroup.value]?.ids || []
      : [];

    tempOrderData.value = {
      addressId: address.value.id,
      cartId: cartItems.value.map(item => item.id),
      totalAmount: Math.round(finalAmount),
      couponIds: selectedCouponIds // 保持为字符串数组
    };

    console.log('提交数据:', JSON.stringify(tempOrderData.value, (_, value) =>
      typeof value === 'bigint' ? value.toString() : value
    ));
       showPaymentDialog.value = true;
  } catch (error) {
    ElMessage.error('订单提交失败');
  }
};

const fetchAvailableCoupons = async () => {
  try {
    const token = getToken();
    if (!token) return;

    // 添加数据校验
    const products = cartItems.value
      .filter(item => 
        item.productId && 
        item.productType && 
        !isNaN(item.amount)
      )
      .map(item => ({
        id: Number(item.productId),
        productType: Number(item.productType),
        price: Number(item.amount)
      }));

    const response = await axios.post(
      'http://127.0.0.1:9997/promotion/available',
      products,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        timeout: 5000 // 添加超时设置
      }
    );

    if (response.data?.code === 200) {
      availableCouponGroups.value = response.data.data.map(group => ({
        ids: group.ids.map(id => String(id)),
        rules: Array.isArray(group.rules) ? group.rules : [],
        discountAmount: Number(group.discountAmount) || 0,
        discountDetail: group.discountDetail || {}
      }));
      originalTotalAmount.value = cartItems.value.reduce((acc, item) => acc + (item.price * item.number), 0);
      selectedCouponGroup.value = -1; // 重置选择状态
    }
  } catch (error) {
    console.error('优惠券获取错误详情:', error);
    ElMessage.error('获取优惠券失败');
  }
};

const toggleCouponGroup = (index: number) => {
  if (index >= 0 && index < availableCouponGroups.value.length) {
    selectedCouponGroup.value = selectedCouponGroup.value === index ? -1 : index;
  }
};

// 监听优惠券选择
watch(selectedCouponGroup, (newVal) => {
  if (newVal === -1) {
    discountAmount.value = 0;
  } else {
    discountAmount.value = availableCouponGroups.value[newVal].discountAmount;
  }
});


// 处理支付确认
const handlePaymentConfirm = async () => {
  if (!tempOrderData.value) return;
  
  try {
    const token = getToken();
    if (!token) return;

    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    // 添加支付状态
    const data = {
      ...tempOrderData.value,
      payStatus: 2 // 确认支付
    };

    console.log(data);
    const response = await instance.post('http://127.0.0.1:9997/order/submit', data);
    if (response.status === 200) {
      ElMessage.success('支付成功');
      cartItems.value = [];
      showCheckout.value = false;
      // 可以在这里跳转到订单页面
      router.push({ name: 'order' });
    }
  } catch (error) {
    ElMessage.error('支付失败');
  } finally {
    showPaymentDialog.value = false;
    tempOrderData.value = null;
  }
};

// 处理支付取消
const handlePaymentCancel = async () => {
  if (!tempOrderData.value) return;

  try {
    const token = getToken();
    if (!token) return;

    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    // 添加支付状态
    const data = {
      ...tempOrderData.value,
      payStatus: 1 // 取消支付
    };

    const response = await instance.post('http://127.0.0.1:9997/order/submit', data);
    if (response.status === 200) {
      ElMessage.warning('已取消支付');
      router.push({ name: 'order' });
    }
  } catch (error) {
    ElMessage.error('取消支付操作失败');
  } finally {
    showPaymentDialog.value = false;
    tempOrderData.value = null;
  }
};

onMounted(fetchCartItems);
</script>

<style scoped>
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

.cart-container {
  padding: 20px;
}

.cart-item {
  display: flex;
  align-items: flex-start;
  border-bottom: 1px solid #ccc;
  padding: 10px 0;
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  margin-right: 20px;
}

.item-info {
  flex: 0.5;
}

.quantity-control {
  margin-top: 80px;
}

.quantity-control el-button {
  margin: 0 5px;
}

.total-amount {
  margin-top: 20px;
  font-size: 20px;
  font-weight: bold;
  text-align: right;
}

.checkout-btn {
  display: block;
  width: auto; 
  margin-top: 10px; 
  padding: 8px 16px; 
  font-size: 20px; 
  text-align: center;
  float: right; 
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.checkout-container {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
  z-index: 1000;
  width: 600px;
  max-width: 90%;
  max-height: 90vh; 
  overflow-y: auto;
}

.checkout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.close-btn {
  background-color: transparent;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

.checkout-items-container {
  max-height: 40vh; 
  overflow-y: auto; 
}

.checkout-item {
  display: flex;
  align-items: flex-start;
  border-bottom: 1px solid #eee;
  padding: 15px 0;
}

.confirm-btn {
  display: block;
  width: 100%;
  margin-top: 20px;
  padding: 10px;
  font-size: 18px;
}

.address-info {
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 15px;
  width: 90%;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.select-address-btn {
  float: right;
  margin-top: -30px; /* 根据实际布局调整 */
}

.address-list-modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
  z-index: 1001;
  width: 600px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.address-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.close-address-list-btn {
  background-color: transparent;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

.default-tag {
  color: #1890ff;
  font-weight: bold;
  float: right; 
  margin-top: -20px; 
}

.payment-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.payment-dialog {
  background: white;
  padding: 30px;
  border-radius: 8px;
  width: 400px;
  text-align: center;
}

.payment-dialog h3 {
  margin-bottom: 20px;
  font-size: 24px;
  color: #333;
}

.payment-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: space-around;
}

.payment-buttons .el-button {
  width: 120px;
}

.payment-dialog-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px); /* 添加模糊效果 */
  z-index: 2000;
}
/* 支付窗口容器 */
.payment-dialog {
  background: white;
  padding: 30px;
  border-radius: 16px; /* 增大圆角 */
  width: 450px; /* 适当增大宽度 */
  text-align: center;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); /* 增强阴影效果 */
  z-index: 2001;
  position: relative;
}
/* 支付窗口标题 */
.payment-dialog-title {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}
.payment-dialog-title i {
  font-size: 28px;
  margin-right: 10px;
  color: #1890ff; /* 图标颜色 */
}
.payment-dialog h3 {
  font-size: 28px; /* 增大标题字体大小 */
  color: #333;
  margin: 0;
}
/* 支付金额显示 */
.payment-amount {
  font-size: 22px; /* 增大金额字体大小 */
  margin-bottom: 30px;
}

.payment-amount .amount {
  color: #ff6600; /* 金额颜色 */
  font-weight: bold;
}

/* 支付按钮 */
.payment-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: space-around;
}

.payment-button {
  padding: 12px 24px; /* 增大按钮内边距 */
  font-size: 18px; /* 增大按钮字体大小 */
  border-radius: 8px; /* 按钮圆角 */
}

.payment-button.el-button--danger {
  background-color: #ff4d4f; /* 取消按钮背景颜色 */
  border-color: #ff4d4f;
}

.payment-button.el-button--danger:hover {
  background-color: #ff7875; /* 取消按钮悬停背景颜色 */
  border-color: #ff7875;
}

.payment-button.el-button--primary {
  background-color: #1890ff; /* 确认按钮背景颜色 */
  border-color: #1890ff;
}

.payment-button.el-button--primary:hover {
  background-color: #40a9ff; /* 确认按钮悬停背景颜色 */
  border-color: #40a9ff;
}

.no-products-message {
  text-align: center;
  font-size: 20px;
  color: #999;
  margin-top: 50px;
}

/* 新增优惠券样式 */
.coupon-selection {
  margin: 20px 0;
}

.coupon-groups {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-group-item {
  position: relative;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #ebeef5;
  background-color: #f8fafc;
}

.unselected-coupon:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.selected-coupon {
  border: 2px solid #409eff !important;
  background-color: #f5faff !important;
}

.coupon-group-info {
  position: relative;
}

.checkmark {
  position: absolute;
  right: 12px;
  top: 12px;
  color: #67C23A;
  font-size: 20px;
}

.no-coupon {
  text-align: center;
  padding: 12px;
  color: #606266;
  font-weight: 500;
}

.no-coupon.selected-coupon {
  color: #409eff;
  background-color: #f5faff;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.discount-amount {
  color: #F56C6C;
  font-size: 18px;
  font-weight: bold;
}

.rule-item {
  display: flex;
  align-items: center;
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
}

.rule-item i {
  color: #67C23A;
  margin-right: 8px;
  font-size: 16px;
}

/* 金额区域优化 */
.amount-summary {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin: 20px 0;
}

.amount-line {
  display: flex;
  justify-content: space-between;
  margin: 8px 0;
  font-size: 16px;
}

.total-amount {
  font-weight: bold;
  color: #303133;
}
</style>