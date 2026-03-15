import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/home/HomeView.vue'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import ProductDetailView from '../views/product/ProductDetailView.vue'
import CartView from '../views/cart/CartView.vue'
import OrderConfirmView from '../views/order/OrderConfirmView.vue'
import OrderListView from '../views/order/OrderListView.vue'
import UserCenterView from '../views/user/UserCenterView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
  { path: '/register', name: 'register', component: RegisterView, meta: { guestOnly: true } },
  { path: '/products/:id', name: 'product-detail', component: ProductDetailView },
  { path: '/cart', name: 'cart', component: CartView, meta: { requiresAuth: true } },
  { path: '/order/confirm', name: 'order-confirm', component: OrderConfirmView, meta: { requiresAuth: true } },
  { path: '/orders', name: 'orders', component: OrderListView, meta: { requiresAuth: true } },
  { path: '/user', name: 'user', component: UserCenterView, meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = localStorage.getItem('store_token')

  if (to.meta.requiresAuth && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.guestOnly && token) {
    return { name: 'home' }
  }

  return true
})

export default router
