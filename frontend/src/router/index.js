// router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from '../components/LoginPage.vue';
import IndexPage from '../components/IndexPage.vue';
import CartPage from '../components/CartPage.vue';
import LogoutPage from '../components/LogoutPage.vue';
import ProductsPage from '../components/ProductsPage.vue';
import ProfilePage from '../components/ProfilePage.vue';
import RegisterPage from '@/components/RegisterPage.vue';
import OrderPage from '@/components/OrderPage.vue';
import PromotionPage from '@/components/PromotionPage.vue';
const routes = [
  {
    path: '/', // 添加默认路由
    redirect: '/index'
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPage
  },
  {
    path: '/index',
    name: 'index',
    component: IndexPage
  },
  {
    path: '/cart',
    name: 'cart',
    component: CartPage
  },
  {
    path: '/logout',
    name: 'logout',
    component: LogoutPage
  },
  {
    path: '/products',
    name: 'products',
    component: ProductsPage
  },
  {
    path: '/profile',
    name: 'profile',
    component: ProfilePage
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterPage
  },
  {
    path: '/order',
    name:'order',
    component: OrderPage
  },
  {
    path: '/promotion',
    name: 'promotion',
    component: PromotionPage
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;