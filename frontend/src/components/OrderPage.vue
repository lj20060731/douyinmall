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

  <div class="dashboard-container">
      <!-- 搜索区域 -->
      <div class="search-bar">
          <el-select
                  v-model="activeStatus"
                  placeholder="订单状态"
                  style="width: 120px; margin-right: 15px"
                  @change="handleStatusChange"
          >
              <el-option label="全部订单" value="0" />
              <el-option label="待付款" value="1" />
              <el-option label="派送中" value="2" />
              <el-option label="已完成" value="3" />
              <el-option label="已取消" value="4" />
          </el-select>
          <el-input
                  v-model="searchParams.number"
                  placeholder="订单号"
                  style="width: 200px; margin-right: 15px"
                  clearable
          />
          <el-input
                  v-model="searchParams.phone"
                  placeholder="手机号"
                  style="width: 200px; margin-right: 15px"
                  clearable
          />
          <el-date-picker
                  v-model="searchParams.timeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  format="YYYY-MM-DD HH:mm:ss"
                  :locale="locale"
                  style="width: 350px; margin-right: 15px"
          />
      </div>

      <!-- 订单表格 -->
      <el-table :data="orderList" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="number" label="订单号" width="180" />
          <el-table-column label="商品信息">
            <template #default="{ row }">
              <!-- 添加一个容器来包裹商品信息和按钮 -->
              <div class="product-info-wrapper"> 
                <el-tooltip 
                  effect="dark" 
                  placement="top" 
                  :disabled="row.items.length <= 2"
                  raw-content
                >
                  <template #content>
                    <div class="tooltip-content">
                      <div v-for="item in row.items" :key="item.id" class="tooltip-item">
                        <span class="item-name">{{ item.name }}</span>
                        <span class="item-quantity"> x{{ item.number }}</span>
                      </div>
                    </div>
                  </template>
                  <div class="product-list-trigger" style="height: 70px;">
                    <div v-for="item in row.items.slice(0, 2)" :key="item.id">
                      {{ item.name }} x{{ item.number }}
                    </div>
                    <div v-if="row.items.length > 2" class="ellipsis">...</div>
                  </div>
                </el-tooltip>
                <el-button 
                  size="small" 
                  @click.stop="showDetail(row.id)"
                  class="view-detail-btn"
                >
                  查看详情
                </el-button>
              </div>
            </template>
          </el-table-column>
          <!-- 订单表格状态列 -->
          <el-table-column prop="status" label="状态" width="140">
            <template #default="{ row }">
              <el-tag :type="statusTagType[row.status]">
                {{ statusMap[row.status] }}
                <span v-if="row.status === 1"> ({{ formatCountdown(row.countdown) }})</span>
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="consignee" label="收货人" width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="address" label="地址" show-overflow-tooltip />
          <el-table-column prop="orderTime" label="下单时间" width="180" >
              <template #default="{ row }">
                  {{ dayjs(row.orderTime).format('YYYY-MM-DD HH:mm:ss') }}
              </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="金额" width="120">
              <template #default="{ row }">￥{{ row.totalAmount.toFixed(2) }}</template>
          </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
              v-model:current-page="pagination.current"
              v-model:page-size="pagination.size"
              :total="pagination.total"
              :page-sizes="[10, 20, 30]"
              layout="total, sizes, prev, pager, next"
              style="margin-top: 20px"
              @size-change="fetchOrders"
              @current-change="fetchOrders"
      />

      <!-- 订单详情弹窗 -->
      <el-dialog v-model="detailVisible" title="订单详情" width="55%">
        <div v-if="currentOrder" class="order-detail-container">
          <!-- 状态卡片 -->
          <div class="status-card" :style="{borderLeft: `4px solid ${statusColors[currentOrder.status]}`}" :data-status="currentOrder.status">
            <div class="status-header">
              <el-tag :type="statusTagType[currentOrder.status]" effect="dark">
                {{ statusMap[currentOrder.status] }}
                <span v-if="currentOrder.status === 1"> ({{ formatCountdown(currentOrder.countdown) }})</span>
              </el-tag>
              <div class="order-meta">
                <div class="order-number">订单号：{{ currentOrder.number }}</div>
                <div class="order-time">
                  <el-icon><Clock /></el-icon>
                  下单时间：{{ dayjs(currentOrder.orderTime).format('YYYY-MM-DD HH:mm:ss') }}
                </div>
              </div>
            </div>
          </div>

          <!-- 信息分割 -->
          <el-divider content-position="left"><el-icon><info-filled /></el-icon> 基本信息</el-divider>

          <!-- 收货信息 -->
          <el-descriptions :column="2" border>
            <el-descriptions-item label="收货人">
              <el-text tag="b">{{ currentOrder.consignee }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              <el-link type="primary" :href="`tel:${currentOrder.phone}`">
                {{ currentOrder.phone }}
              </el-link>
            </el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">
              {{ currentOrder.address }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 商品清单 -->
          <el-divider content-position="left"><el-icon><goods /></el-icon> 商品清单</el-divider>
          <el-table :data="currentOrder.items" stripe size="small">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="number" label="数量" width="100" align="center">
              <template #default="{row}">x{{ row.number }}</template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="120" align="right">
              <template #default="{row}">￥{{ row.amount.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
          <!-- 优惠券信息 -->
          <el-divider 
            v-if="currentOrder.userCoupons?.length" 
            content-position="left"
          >
            <el-icon><ticket /></el-icon> 优惠券信息
          </el-divider>
          <el-table 
            v-if="currentOrder.userCoupons?.length"
            :data="currentOrder.userCoupons" 
            stripe 
            size="small"
            class="coupon-table"
          >
            <el-table-column prop="name" label="优惠券名称" />
            <el-table-column label="折扣类型" width="120">
              <template #default="{row}">
                {{ formatDiscountType(row.discountType) }}
              </template>
            </el-table-column>
            <el-table-column label="优惠规则">
            <template #default="{row}">
              {{ formatCouponRule(row) }}
            </template>
          </el-table-column>
          </el-table>
          <!-- 金额汇总 -->
          <div class="amount-summary">
            <div class="total-line">
              <span>订单总额：</span>
              <span class="total-amount">￥{{ currentOrder.totalAmount.toFixed(2) }}</span>
            </div>
            <div v-if="currentOrder.status === 4" class="cancel-info">
              <el-icon><WarningFilled /></el-icon>
              <span>取消原因：{{ currentOrder.cancelReason || '无' }}</span>
            </div>
            <el-button v-if="currentOrder.status === 1" @click="confirmPayOrder(currentOrder.id)">立即支付</el-button>
            <el-button v-if="currentOrder.status ===3 || currentOrder.status === 2" @click="confirmRefund(currentOrder.id)">退款</el-button>
            <el-dialog 
              v-model="refundConfirmVisible" 
              title="退款申请确认"
              width="450px"
              align-center
              class="refund-dialog"
            >
              <div class="refund-content">
                <el-icon :size="36" color="#e6a23c" class="warning-icon">
                  <WarningFilled />
                </el-icon>
                <h3 class="refund-title">确认要申请退款吗？</h3>
                <div class="refund-tips">
                  <p>• 退款申请提交后我们将尽快处理</p>
                  <p>• 退款金额将原路退回支付账户</p>
                  <p>• 请确保商品符合退款政策要求</p>
                </div>
              </div>
              <template #footer>
                <div class="dialog-footer">
                  <el-button 
                    @click="refundConfirmVisible = false" 
                    class="cancel-btn"
                    size="large"
                  >
                    再想想
                  </el-button>
                  <el-button 
                    type="primary" 
                    @click="handleRefund(currentOrder.id)"
                    class="confirm-btn"
                    size="large"
                  >
                    确认退款
                  </el-button>
                </div>
              </template>
            </el-dialog>
          </div>
        </div>
      </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch, toRefs } from 'vue';
import axios from 'axios';
import type { TabsPaneContext } from 'element-plus';
import { useRouter } from 'vue-router';
import { ElMessageBox,ElMessage} from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import dayjs from 'dayjs';
import { Clock, InfoFilled, Goods, WarningFilled, Ticket } from '@element-plus/icons-vue';
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

enum DiscountType {
  FULL_REDUCTION = 1,
  DISCOUNT = 2,
  NO_THRESHOLD = 3,
  NORMAL_FULL = 4
}

interface Order {
  id: string;
  number: string;
  status: number;
  consignee: string;
  phone: string;
  address: string;
  orderTime: string;
  cancelTime?: string;
  cancelReason?: string;
  totalAmount: number;
  items: Array<{
      id: string;
      name: string;
      amount: number;
      number: number;
  }>;
  userCoupons: Array<{
    couponId: string | null;
    name: string;
    discountType: number;
    thresholdAmount: number;
    discountValue: number;
    maxDiscountAmount: number;
  }>;
  countdown: number;
}

const formatDiscountType = (type: DiscountType) => {
  const types = {
    [DiscountType.FULL_REDUCTION]: '每满减',
    [DiscountType.DISCOUNT]: '折扣',
    [DiscountType.NO_THRESHOLD]: '无门槛',
    [DiscountType.NORMAL_FULL]: '普通满减'
  };
  return types[type] || '未知类型';
};

const formatCouponRule = (coupon: {
  discountType: number;
  thresholdAmount: number;
  discountValue: number;
  maxDiscountAmount: number;
}) => {
  const { discountType, thresholdAmount, discountValue, maxDiscountAmount } = coupon;
  
  switch (discountType) {
    case DiscountType.FULL_REDUCTION: // 每满减
      return `每满${thresholdAmount}减${discountValue}${maxDiscountAmount ? `，最高减${maxDiscountAmount}` : ''}`;
      
    case DiscountType.DISCOUNT: // 折扣
      return `满${thresholdAmount}打${(discountValue / 10).toFixed(1)}折${
        maxDiscountAmount ? `，最高减${maxDiscountAmount}` : ''
      }`;
      
    case DiscountType.NO_THRESHOLD: // 无门槛
      return `立减${discountValue}元${maxDiscountAmount ? `，最高减${maxDiscountAmount}` : ''}`;
      
    case DiscountType.NORMAL_FULL: // 普通满减
      return `满${thresholdAmount}减${discountValue}${maxDiscountAmount ? `，最高减${maxDiscountAmount}` : ''}`;
      
    default:
      return '未知优惠规则';
  }
};

// 状态映射
const statusMap: { [key: number]: string } = {
  1: '待付款',
  2: '派送中',
  3: '已完成',
  4: '已取消'
}
const statusTagType: { [key: number]: string } = {
  1: 'warning',
  2: 'primary',
  3: 'success',
  4: 'danger'
}

const statusColors: { [key: number]: string } = {
  1: '#e6a23c',  // 待付款
  2: '#409eff',  // 派送中
  3: '#67c23a',  // 已完成
  4: '#f56c6c'   // 已取消
}

// 响应式数据
const activeStatus = ref('0')
const loading = ref(false)
const orderList = ref<Order[]>([])
const detailVisible = ref(false)
const currentOrder = ref<Order | null>(null)

const searchParams = reactive({
  number: '',
  phone: '',
  timeRange: []
})

const locale = zhCn;

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取订单列表
const fetchOrders = async () => {
  try {
      loading.value = true;
      const token = getToken();
      if (!token) return;
      const instance = axios.create({
          headers: {
              Authorization: `Bearer ${token}`
          }
      });
      const params = {
          page: pagination.current,
          pageSize: pagination.size,
          status: activeStatus.value === '0' ? undefined : parseInt(activeStatus.value),
          number: searchParams.number || undefined,
          phone: searchParams.phone || undefined,
          beginTime: searchParams.timeRange?.[0],
          endTime: searchParams.timeRange?.[1]
      };
      console.log('请求参数:', params); 
      const response = await instance.post('http://127.0.0.1:9997/order/getorderlist', params);
      orderList.value = response.data.data.records.map((order: any) => {
          if (order.status === 1) {
              const orderTime = dayjs(order.orderTime);
              const expireTime = orderTime.add(30, 'minutes');
              const now = dayjs();
              const countdown = expireTime.diff(now, 'seconds');
              order.countdown = Math.max(0, countdown);
              startCountdown(order); // 启动倒计时
          } else {
              order.countdown = 0;
          }
          return order;
      });
      pagination.total = response.data.data.total;
  } catch (error) {
    ElMessage.error('获取订单失败');
  } finally {
      loading.value = false;
  }
};

// 开始倒计时
const startCountdown = (order: Order) => {
  const reactiveOrder = reactive(order); // 将 order 转为响应式对象
  const intervalId = setInterval(() => {
      if (reactiveOrder.countdown > 0) {
          reactiveOrder.countdown--;
      } else {
          clearInterval(intervalId);
          cancelOrder(reactiveOrder.id);
      }
  }, 1000);
};

watch([activeStatus, () => searchParams.number, () => searchParams.phone, () => searchParams.timeRange], () => {
  pagination.current = 1; // 每次搜索条件变化时，重置为第一页
  fetchOrders();
}, { immediate: false });

// 查看详情
const showDetail = (orderId: string) => {
  const order = orderList.value.find(order => order.id === orderId);
  if (order) {
      currentOrder.value = order;
      detailVisible.value = true;
  }
};

// 切换状态
const handleStatusChange = () => {
  pagination.current = 1
  fetchOrders()
}

// 格式化倒计时
const formatCountdown = (seconds: number) => {
  const minutes = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 取消订单
const cancelOrder = async (orderId: string) => {
  try {
      const token = getToken();
      if (!token) return;
      const instance = axios.create({
          headers: {
              Authorization: `Bearer ${token}`
          }
      });
      const params = {
          id: orderId,
          cancelReason: '超时未支付',
          status: 4
      };
      await instance.post('http://127.0.0.1:9997/order/cancel', params);
      ElMessage.success('订单已取消');
      fetchOrders(); // 重新获取订单列表
  } catch (error) {
      ElMessage.error('取消订单失败');
  }
};

// 确认支付订单
const confirmPayOrder = async (orderId: string) => {
  try {
    const result = await ElMessageBox.confirm(
      '你确定要支付该订单吗？',
      '确认支付',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    if (result === 'confirm') {
      await payOrder(orderId); // 等待支付请求完成
      detailVisible.value = false; // 关闭订单详情弹窗
      currentOrder.value = null; // 清空当前订单信息
    }
  } catch (error) {
    // 用户取消支付，无需额外操作，可回到订单页面
  }
};

// 支付订单
const payOrder = async (orderId: string) => {
  try {
    const token = getToken();
    if (!token) return;
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const params = {
      id: orderId,
      status: 2,
      payStatus: 2,
      deliveryStatus: 1
    };
    await instance.post('http://127.0.0.1:9997/order/confirm', params);
    ElMessage.success('支付成功');
    fetchOrders(); // 重新获取订单列表
  } catch (error) {
    ElMessage.error('支付失败');
  }
};

// 添加退款确认弹窗的显示控制
const refundConfirmVisible = ref(false);

// 确认退款
const confirmRefund = (orderId: string) => {
  refundConfirmVisible.value = true;
};

// 处理退款请求
const handleRefund = async (orderId: string) => {
  try {
    const token = getToken();
    if (!token) return;
    const instance = axios.create({
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const params = {
      id: orderId,
      cancelReason: '退货退款',
      status: 4,
      payStatus: 3
    };
    await instance.post('http://127.0.0.1:9997/order/drawback', params);
    ElMessage.success('退款申请已提交');
    fetchOrders(); // 重新获取订单列表
    refundConfirmVisible.value = false; // 关闭确认弹窗
    detailVisible.value = false; // 关闭订单详情弹窗
    currentOrder.value = null; // 清空当前订单信息
  } catch (error) {
    ElMessage.error('退款申请失败');
    refundConfirmVisible.value = false; // 关闭确认弹窗
  }
};

// 初始化加载
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  margin-top: -80px;
}

.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #6c7681;
  border-radius: 8px;
}

.el-select {
  margin-right: 15px;
}

.order-info div,
.user-info div {
  margin: 8px 0;
  font-size: 14px;
}

.items-info .item {
  display: flex;
  justify-content: space-between;
  margin: 8px 0;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.total-amount {
  text-align: right;
  font-weight: bold;
  margin-top: 15px;
  color: #f56c6c;
}

.el-divider {
  margin: 20px 0;
}

.product-list-trigger {
  position: relative;
  display: flex; /* 使用 Flexbox 布局 */
  flex-direction: column; /* 垂直排列子元素 */
  justify-content: center; /* 垂直居中子元素 */
  align-items: flex-start; /* 水平靠左对齐子元素 */
  cursor: default;
}

/* 调整 tooltip 样式 */
.el-popper {
  white-space: pre-wrap;
  max-width: 300px;
  line-height: 1.6;
  .ellipsis {
      color: #999;
  }
}

/* 商品信息和按钮的包裹容器样式 */
.product-info-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between; 
}

/* 查看详情按钮样式 */
.view-detail-btn {
  margin-right: 80px;
  background-color: #409eff; 
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px; 
  cursor: pointer;
  transition: background-color 0.3s ease; 
}

.view-detail-btn:hover {
  background-color: #3a8ee6; 
}

.order-detail-container {
  padding: 0 20px;
}

.status-card {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 20px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 20px;
}

.order-number {
  color: #666;
  font-size: 15px;
}

.order-time {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #888;
  font-size: 15px;
}

.status-card {
  /* 修改背景颜色 */
  background: rgba(0, 0, 0, 0.1); /* 默认的半透明背景 */
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 20px;
  /* 使用动态样式绑定 */
  &[data-status="1"] {
    background: rgba(230, 162, 60, 0.1); /* 待付款 */
    border-left: 4px solid #e6a23c;
  }
  &[data-status="2"] {
    background: rgba(64, 158, 255, 0.1); /* 派送中 */
    border-left: 4px solid #409eff;
  }
  &[data-status="3"] {
    background: rgba(103, 194, 58, 0.1); /* 已完成 */
    border-left: 4px solid #67c23a;
  }
  &[data-status="4"] {
    background: rgba(245, 108, 108, 0.1); /* 已取消 */
    border-left: 4px solid #f56c6c;
  }
}

.el-divider {
  margin: 24px 0;
}

.el-divider__text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.amount-summary {
  margin-top: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
}

.total-line {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 16px;
  font-size: 14px;
}

.total-amount {
  font-size: 18px;
  color: var(--el-color-primary);
  font-weight: 600;
}

.cancel-info {
  margin-top: 12px;
  padding: 12px;
  background: #fef0f0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f56c6c;
}

:deep(.el-descriptions__body) {
  background: #fff;
}

:deep(.el-descriptions__label) {
  width: 100px;
  color: #666;
}

.refund-dialog {
  :deep(.el-dialog__header) {
    border-bottom: 1px solid #eee;
    margin-bottom: 20px;
  }

  .refund-content {
    text-align: center;
    padding: 0 20px;

    .warning-icon {
      margin-bottom: 16px;
      animation: pulse 1.5s infinite;
    }

    .refund-title {
      color: #303133;
      margin: 0 0 16px 0;
      font-size: 18px;
    }

    .refund-tips {
      text-align: left;
      background: #f8f9fa;
      border-radius: 8px;
      padding: 16px;
      margin: 0 auto;
      width: 80%;

      p {
        color: #606266;
        margin: 8px 0;
        font-size: 14px;
        line-height: 1.5;
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: center;
    gap: 24px;
    padding-top: 20px;

    .cancel-btn {
      width: 120px;
      background: #f0f2f5;
      border-color: #dcdfe6;
      color: #606266;

      &:hover {
        background: #e4e6eb;
      }
    }

    .confirm-btn {
      width: 120px;
      background: #f56c6c;
      border-color: #f56c6c;

      &:hover {
        background: #e65050;
      }
    }
  }
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.coupon-table {
  margin: 16px 0;
  
  :deep(.el-table__cell) {
    padding: 8px 0;
  }
  
  :deep(th) {
    background-color: #fafafa;
    font-weight: 500;
  }
  
  :deep(td) {
    .cell {
      white-space: pre-wrap;
      line-height: 1.5;
    }
  }
}
</style>