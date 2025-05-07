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


  <h2 class="section-title">领取优惠券</h2>

  <!-- 兑换码输入 -->
  <div class="exchange-container">
    <el-input 
      v-model="exchangeCode"
      placeholder="请输入兑换码"
      class="exchange-input"
      clearable
      @keyup.enter="handleExchange"
    >
      <template #append>
        <el-button 
          type="primary" 
          :loading="exchanging"
          @click="handleExchange"
        >
          立即兑换
        </el-button>
      </template>
    </el-input>
  </div>

  <!-- 优惠券领取区 -->
  <div class="coupon-section">
    <div class="coupon-list">
      <div 
        v-for="coupon in availableCoupons" 
        :key="coupon.id" 
        class="coupon-item"
        :class="couponClass(coupon)"
      >
        <div class="coupon-corner">
          <i class="coupon-icon" :class="couponIcon(coupon)"></i>
        </div>
        <div class="coupon-header">
          <h3>{{ coupon.name }}</h3>
          <span class="coupon-type-tag">{{ formatDiscountType(coupon.discountType) }}</span>
        </div>
        <div class="coupon-body">
          <div class="coupon-main">
            <!-- 金额/折扣显示 -->
            <template v-if="coupon.discountType === DiscountType.DISCOUNT">
              <div class="discount-display">
                <span class="discount-ratio">{{ coupon.discountValue / 10 }}</span>
                <span class="discount-unit">折</span>
              </div>
            </template>
            <template v-else>
              <div class="discount-amount">
                <span class="symbol">¥</span>
                <span class="value">{{ coupon.discountValue }}</span>
              </div>
            </template>

            <!-- 新增适用类型显示 -->
            <div class="condition-info">
              <p v-if="coupon.thresholdAmount > 0" class="threshold">
                满{{ coupon.thresholdAmount }}元可用
              </p>
              <p class="product-scopes">
                <template v-if="coupon.specific">
                  适用品类：{{ formatProductScopes(coupon.scopes) }}
                </template>
                <template v-else>
                  适用类型：不限类型
                </template>
              </p>
              <p v-if="coupon.maxDiscountAmount > 0" class="max-discount">
                最高优惠 ¥{{ coupon.maxDiscountAmount }}
              </p>
            </div>
          </div>
          
          <div class="coupon-footer">
            <p class="valid-time">
              {{ formatValidTime(coupon) }}
            </p>
            <button 
              @click="receiveCoupon(coupon.id)"
              :class="['receive-btn', statusClass(coupon)]"
              :disabled="!coupon.available"
            >
              {{ getButtonText(coupon) }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <h2 class="section-title">我的优惠券</h2>
  <!-- 我的优惠券区 -->
  <div class="coupon-section">
    <!-- 我的优惠券列表 -->
    <div class="coupon-list">
      <div 
        v-for="coupon in myCoupons"
        :key="coupon.couponId"
        class="coupon-item"
        :class="myCouponClass(coupon)"
      >
        <div class="coupon-header">
          <h3>{{ coupon.name }}</h3>
          <span class="coupon-type-tag">{{ formatDiscountType(coupon.discountType) }}</span>
          <!--<span class="coupon-status-tag">{{ formatCouponStatus(coupon.userCouponStatus) }}</span>-->
        </div>
      
        <div class="coupon-body">
          <div class="coupon-main">
            <!-- 金额/折扣显示 -->
            <template v-if="coupon.discountType === DiscountType.DISCOUNT">
              <div class="discount-display">
                <span class="discount-ratio">{{ coupon.discountValue / 10 }}</span>
                <span class="discount-unit">折</span>
              </div>
            </template>
            <template v-else>
              <div class="discount-amount">
                <span class="symbol">¥</span>
                <span class="value">{{ coupon.discountValue }}</span>
              </div>
            </template>

            <!-- 使用条件 -->
            <div class="condition-info">
              <p v-if="coupon.thresholdAmount > 0" class="threshold">
                满{{ coupon.thresholdAmount }}元可用
              </p>
              <p class="product-scopes">
                <template v-if="coupon.specific">
                  适用品类：{{ formatProductScopes(coupon.scopes) }}
                </template>
                <template v-else>
                  适用类型：不限类型
                </template>
              </p>
              <p v-if="coupon.maxDiscountAmount > 0" class="max-discount">
                最高优惠 ¥{{ coupon.maxDiscountAmount }}
              </p>
            </div>
          </div>
          <!-- 有效期 -->
          <div class="coupon-footer">
            <p class="valid-time">
              有效期：{{ formatDateTime(coupon.termBeginTime) }} 至 {{ formatDateTime(coupon.termEndTime) }}
            </p>
            <div class="coupon-status">
              <span class="status-text">{{ getStatusText(coupon.userCouponStatus) }}</span>
              <span v-if="coupon.usedTime" class="used-time">
                使用时间：{{ formatDateTime(coupon.usedTime) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
  
<script lang="ts" setup>
import { ref, onMounted, watch, computed } from 'vue';
import axios from 'axios';
import type { TabsPaneContext } from 'element-plus';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';

// ==================== 类型定义 ====================
enum UserCouponStatus {
  UNUSED = 1,
  USED = 2,
  EXPIRED = 3
}

enum DiscountType {
  FULL_REDUCTION = 1,
  DISCOUNT = 2,
  NO_THRESHOLD = 3,
  NORMAL_FULL = 4
}

type ProductTypeValue = 1 | 2 | 3;
interface ProductTypeItem {
  value: ProductTypeValue;
  desc: string;
}

interface Coupon {
  id: number;
  name: string;
  specific: boolean;
  scopes: number[];
  discountType: DiscountType;
  thresholdAmount: number;
  discountValue: number;
  maxDiscountAmount: number;
  termDays: number;
  termEndTime: string;
  available: boolean;
}


interface UserCoupon {
  couponId: number;
  name: string;
  specific: boolean;
  scopes: number[];
  discountType: DiscountType;
  thresholdAmount: number;
  discountValue: number;
  maxDiscountAmount: number;
  termBeginTime: string;
  termEndTime: string;
  usedTime: string | null;
  userCouponStatus: UserCouponStatus;
}

// ==================== 常量定义 ====================
const ProductType: Record<string, ProductTypeItem> = {
  DIGIT: { value: 1, desc: "数码型" },
  FOOD: { value: 2, desc: "食品型" },
  CLOTHING: { value: 3, desc: "服装型" }
} as const;

// ==================== 响应式数据 ====================
const activeName = ref('promotion');
const router = useRouter();
const coupons = ref<Coupon[]>([]);
const myCoupons = ref<UserCoupon[]>([]);
const isLoading = ref(false);
const exchangeCode = ref('');
const exchanging = ref(false);

// ==================== 计算属性 ====================
const availableCoupons = computed(() => coupons.value);

// ==================== 生命周期钩子 ====================
onMounted(() => {
  setActiveName();
  fetchCoupons();
  fetchMyCoupons();
});

// ==================== 事件处理 ====================
const handleClick = (tab: TabsPaneContext) => {
  const routeMap: Record<string, string> = {
    products: '/products',
    promotion: '/promotion',
    cart: '/cart',
    profile: '/profile',
    order: '/order',
    logout: '/logout'
  };

  if (tab.paneName === 'logout') {
    handleLogout();
  } else if (routeMap[tab.paneName]) {
    router.push(routeMap[tab.paneName]);
  }
};

const handleLogout = () => {
  localStorage.clear();
  router.replace('/login');
};

// ==================== 数据获取 ====================
const fetchCoupons = async () => {
  try {
    isLoading.value = true;
    const token = localStorage.getItem('jwtToken');
    if (!token) return router.push('/login');

    const response = await axios.get('http://127.0.0.1:9997/promotion/list', {
      headers: { Authorization: `Bearer ${token}` }
    });


    console.log(response.data.data);
    if (response.data.code === 200) {
      coupons.value = response.data.data.map((c: any) => ({
        ...c,
        discountType: Number(c.discountType) as DiscountType,
        scopes: c.scopes || [] // 确保有默认值
      }));
    }
  } catch (error) {
    console.error('获取优惠券失败:', error);
  } finally {
    isLoading.value = false;
  }
};

const fetchMyCoupons = async () => {
  try {
    const token = localStorage.getItem('jwtToken');
    if (!token) return router.push('/login');

    const response = await axios.get('http://127.0.0.1:9997/promotion/mycoupon', {
      headers: { Authorization: `Bearer ${token}` }
    });

    if (response.data.code === 200) {
      myCoupons.value = response.data.data;
      console.log(myCoupons.value);
    }
  } catch (error) {
    console.error('获取我的优惠券失败:', error);
  }
};

// ==================== 业务逻辑 ====================
const receiveCoupon = async (couponId: number) => {
  try {
    const token = localStorage.getItem('jwtToken');
    if (!token) return router.push('/login');

    const response = await axios.post(
      `http://127.0.0.1:9997/promotion/${couponId}/receive`,
      null,
      { headers: { Authorization: `Bearer ${token}` } }
    );

    if (response.data.code === 200) {
      ElMessage.success('领取成功');
      fetchCoupons();
    } else {
      ElMessage.error(response.data.data || '领取失败');
    }
  } catch (error) {
    console.error('领取优惠券失败:', error);
    ElMessage.error('领取失败');
  }
};

const handleExchange = async () => {
  if (!exchangeCode.value.trim()) {
    ElMessage.warning('请输入兑换码');
    return;
  }

  try {
    exchanging.value = true;
    const token = localStorage.getItem('jwtToken');
    if (!token) return router.push('/login');

    const response = await axios.post(
      `http://127.0.0.1:9997/promotion/${exchangeCode.value}/exchange`,
      null,
      { headers: { Authorization: `Bearer ${token}` } }
    );

    if (response.data.code === 200) {
      ElMessage.success({ message: `兑换成功！${response.data.data}`, duration: 3000 });
      exchangeCode.value = '';
      fetchCoupons();
    } else {
      ElMessage.error(response.data.data || '兑换失败');
    }
  } catch (error) {
    console.error('兑换失败:', error);
    const message = axios.isAxiosError(error) 
      ? error.response?.data?.data || '兑换失败'
      : '兑换失败';
    ElMessage.error(message);
  } finally {
    exchanging.value = false;
  }
};

// ==================== 工具函数 ====================
const setActiveName = () => {
  activeName.value = router.currentRoute.value.name?.toString() || '';
};

const formatDiscountType = (type: DiscountType) => {
  const types = {
    [DiscountType.FULL_REDUCTION]: '每满减',
    [DiscountType.DISCOUNT]: '折扣',
    [DiscountType.NO_THRESHOLD]: '无门槛',
    [DiscountType.NORMAL_FULL]: '普通满减'
  };
  return types[type] || '未知类型';
};

const formatValidTime = (coupon: Coupon) => {
  return coupon.termDays 
    ? `领取后${coupon.termDays}天内有效`
    : `有效期至：${dayjs(coupon.termEndTime).format('YYYY-MM-DD HH:mm')}`;
};

const formatDateTime = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm');
};

const formatProductScopes = (scopes: number[]) => {
  return scopes.map(value => {
    const found = Object.values(ProductType).find(t => t.value === value);
    return found ? found.desc : '未知类型';
  }).join('、');
};

// ==================== 样式类处理 ====================
const couponClass = (coupon: Coupon) => {
  const typeMap = {
    [DiscountType.FULL_REDUCTION]: 'full-reduction',
    [DiscountType.DISCOUNT]: 'discount',
    [DiscountType.NO_THRESHOLD]: 'no-threshold',
    [DiscountType.NORMAL_FULL]: 'normal-full'
  };
  return typeMap[coupon.discountType] || 'default';
};

const couponIcon = (coupon: Coupon) => {
  const icons = {
    [DiscountType.FULL_REDUCTION]: 'el-icon-money',
    [DiscountType.DISCOUNT]: 'el-icon-sold-out',
    [DiscountType.NO_THRESHOLD]: 'el-icon-magic-stick',
    [DiscountType.NORMAL_FULL]: 'el-icon-price-tag'
  };
  return icons[coupon.discountType] || 'el-icon-tickets';
};

const statusClass = (coupon: Coupon) => {
  return coupon.available ? 'available' : 'received';
};

const getButtonText = (coupon: Coupon) => {
  return coupon.available ? '立即领取' : '已领取';
};

const myCouponClass = (coupon: UserCoupon) => ({
  'used-coupon': coupon.userCouponStatus === UserCouponStatus.USED,
  'expired-coupon': coupon.userCouponStatus === UserCouponStatus.EXPIRED,
  'specific-coupon': coupon.specific
});

const formatCouponStatus = (status: UserCouponStatus) => {
  const statusMap = {
    [UserCouponStatus.UNUSED]: '未使用',
    [UserCouponStatus.USED]: '已使用',
    [UserCouponStatus.EXPIRED]: '已失效'
  };
  return statusMap[status] || '未知状态';
};

const getStatusText = (status: UserCouponStatus) => {
  const texts = {
    [UserCouponStatus.UNUSED]: '可使用',
    [UserCouponStatus.USED]: '已使用',
    [UserCouponStatus.EXPIRED]: '已过期'
  };
  return texts[status] || '';
};

// ==================== 监听器 ====================
watch(() => router.currentRoute.value, setActiveName);
</script>
  
<style scoped>
/* 布局优化 */
.coupon-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 15px;
  padding: 15px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 卡片基础样式 */
.coupon-item {
  position: relative;
  border-radius: 8px;
  padding: 15px;
  margin: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
  transition: transform 0.3s ease;
  background: white;
}

.coupon-section {
  margin: 20px auto;
  max-width: 1200px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.section-title {
  color: #303133;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
  margin: 0 0 20px 0;
}

.coupon-item:hover {
  transform: translateY(-3px);
}

/* 类型标识角标 */
.coupon-corner {
  position: absolute;
  top: 10px;
  right: 10px;
  opacity: 0.2;
}

.coupon-icon {
  font-size: 20px;
}

/* 头部区域 */
.coupon-header {
  margin-bottom: 12px;
}

.coupon-header h3 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 8px 0;
}

.coupon-type-tag {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
  background: rgba(64,158,255,0.1);
  color: #409EFF;
}

/* 主体内容 */
.coupon-main {
  margin: 12px 0;
}

.used-coupon {
  opacity: 0.6;
  background: #f5f7fa;
  border-left-color: #909399;
}

.expired-coupon {
  position: relative;
  overflow: hidden;
  &::after {
    content: "已过期";
    position: absolute;
    top: 10px;
    right: -28px;
    background: #f56c6c;
    color: white;
    padding: 2px 30px;
    transform: rotate(45deg);
    font-size: 12px;
  }
}

.specific-coupon {
  border-right: 4px solid #67C23A;
}

.coupon-status-tag {
  background: rgba(64,158,255,0.1);
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.product-scopes {
  color: #606266;
  font-size: 12px;
  margin: 4px 0;
}

.used-time {
  color: #909399;
  font-size: 12px;
}

/* 折扣显示样式 */
.discount-display {
  text-align: center;
  margin: 12px 0;
}

.discount-ratio {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
  color: #409EFF;
}

.discount-unit {
  font-size: 18px;
  color: #606266;
  margin-left: 4px;
}

/* 金额样式 */
.discount-amount {
  text-align: center;
  margin: 12px 0;
}

.discount-amount .symbol {
  font-size: 18px;
  vertical-align: super;
  margin-right: 2px;
}

.discount-amount .value {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

/* 条件信息 */
.condition-info {
  border-top: 1px dashed #EBEEF5;
  padding-top: 8px;
  margin-top: 12px;
}

.condition-info p {
  margin: 4px 0;
  font-size: 12px;
  line-height: 1.4;
}

.threshold {
  color: #606266;
}

.no-threshold {
  color: #67C23A;
}

.max-discount {
  color: #F56C6C;
}

/* 底部区域 */
.coupon-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 12px;
}

.valid-time {
  color: #909399;
  font-size: 11px;
  flex: 1;
  margin-right: 10px;
}

/* 按钮样式 */
.receive-btn {
  padding: 6px 15px;
  border-radius: 16px;
  border: none;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
  white-space: nowrap;
}

/* 按钮状态 */
.available {
  background: #409EFF;
  color: white;
}

.available:hover {
  background: #66B1FF;
  box-shadow: 0 2px 8px rgba(64,158,255,0.3);
}

.received {
  background: #EBEEF5;
  color: #C0C4CC;
  cursor: not-allowed;
}

.disabled {
  background: #F5F7FA;
  color: #909399;
  cursor: not-allowed;
}

/* 类型颜色方案 */
.full-reduction {
  border-left: 4px solid #F56C6C;
}

.full-reduction .discount-amount .value {
  color: #F56C6C;
}

.discount {
  border-left: 4px solid #409EFF;
}

.no-threshold {
  border-left: 4px solid #67C23A;
}

.no-threshold .coupon-type-tag {
  background: rgba(103,194,58,0.1);
  color: #67C23A;
}

.normal-full {
  border-left: 4px solid #E6A23C;
}

/* 加载动画 */
.loading-spinner {
  text-align: center;
  padding: 20px 0;
}

.loading-spinner i {
  font-size: 24px;
  color: #409EFF;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  100% { transform: rotate(360deg); }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .coupon-list {
    grid-template-columns: 1fr;
    padding: 10px;
  }
  
  .coupon-item {
    margin: 8px;
  }
  
  .discount-ratio {
    font-size: 32px;
  }
  
  .discount-amount .value {
    font-size: 32px;
  }
  
  .coupon-footer {
    flex-direction: column;
    align-items: stretch;
  }
  
  .receive-btn {
    margin-top: 8px;
    width: 100%;
  }
}

/* 保持导航条一致 */
.el-tabs__item {
  font-size: 16px !important;
  padding: 0 20px !important;
  height: 50px !important;
  line-height: 50px !important;
}

.el-tabs__nav-wrap::after {
  height: 1px !important;
}

.exchange-container {
  max-width: 600px;
  margin: 20px auto;
  padding: 0 15px;
}

.exchange-input {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

:deep(.el-input-group__append) {
  background: #409EFF;
  border: none;
  border-radius: 0 8px 8px 0;
}

:deep(.el-input-group__append button) {
  color: white;
  font-weight: bold;
}

.coupon-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.coupon-type-tag {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
  background: rgba(103,194,58,0.1);
  color: #67C23A;
  order: 1;
}

.coupon-status-tag {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
  background: rgba(64,158,255,0.1);
  color: #409EFF;
  order: 2;
}

.product-scopes {
  color: #606266;
  font-size: 12px;
  margin: 4px 0;
}
</style>