<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../store/modules/auth'
import { cartApi } from '../../api/modules/cart'
import { productApi } from '../../api/modules/product'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const errorMessage = ref('')
const detail = ref(null)
const activeTab = ref('detail')
const activeImageIndex = ref(0)
const quantity = ref(1)
const addingToCart = ref(false)
const actionMessage = ref('')
const actionError = ref('')

const gallery = computed(() => {
  const list = detail.value?.gallery || []
  if (list.length > 0) return list
  const fallback = detail.value?.image || 'https://placehold.co/520x520/f0f0f0/777777?text=Product'
  return [fallback]
})

const currentImage = computed(() => gallery.value[activeImageIndex.value] || gallery.value[0])

async function fetchDetail() {
  loading.value = true
  errorMessage.value = ''
  try {
    const res = await productApi.getDetail(route.params.id)
    detail.value = res?.data || null
    activeImageIndex.value = 0
    quantity.value = 1
  } catch (error) {
    detail.value = null
    errorMessage.value = error.message || '商品详情加载失败'
  } finally {
    loading.value = false
  }
}

function setActiveImage(index) {
  activeImageIndex.value = index
}

function minus() {
  quantity.value = Math.max(1, quantity.value - 1)
}

function plus() {
  quantity.value += 1
}

async function addToCart() {
  actionMessage.value = ''
  actionError.value = ''
  if (!authStore.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (!detail.value) return

  addingToCart.value = true
  try {
    await cartApi.add({ skuId: detail.value.id, quantity: quantity.value })
    actionMessage.value = '已加入购物车'
  } catch (error) {
    actionError.value = error.message || '加入购物车失败'
  } finally {
    addingToCart.value = false
  }
}

onMounted(fetchDetail)
watch(() => route.params.id, fetchDetail)
</script>

<template>
  <section class="detail-page">
    <div v-if="loading" class="panel state">商品详情加载中...</div>
    <div v-else-if="errorMessage" class="panel state">{{ errorMessage }}</div>

    <template v-else-if="detail">
      <article class="panel top-card">
        <div class="gallery-col">
          <div class="main-image-wrap">
            <img :src="currentImage" :alt="detail.name" class="main-image" />
          </div>

          <div class="thumb-list">
            <button
              v-for="(img, index) in gallery"
              :key="`${img}-${index}`"
              type="button"
              class="thumb-item"
              :class="{ active: activeImageIndex === index }"
              @click="setActiveImage(index)"
            >
              <img :src="img" alt="thumb" />
            </button>
          </div>

          <button type="button" class="fav-btn">♡ 收藏</button>
        </div>

        <div class="info-col">
          <h1 class="title">{{ detail.name }}</h1>
          <p class="selling-point">{{ detail.shortDesc }}</p>

          <div class="price-panel">
            <div class="price-lines">
              <div class="price-row"><span>原价</span><strong class="old-price">￥{{ detail.originalPrice }}</strong></div>
              <div class="price-row"><span>售价</span><strong class="sale-price">￥{{ detail.price }}</strong></div>
            </div>
            <div class="price-meta">{{ detail.sales }} 销量 ｜ 手机购买 📱</div>
          </div>

          <div class="quantity-row">
            <span class="label">数量</span>
            <div class="stepper">
              <button type="button" @click="minus">-</button>
              <input v-model="quantity" type="text" readonly />
              <button type="button" @click="plus">+</button>
            </div>
            <small>(库存{{ detail.stock }}台)</small>
          </div>

          <div class="action-row">
            <button type="button" class="btn-buy">立即购买</button>
            <button type="button" class="btn-cart" :disabled="addingToCart || detail.stock <= 0" @click="addToCart">
              {{ addingToCart ? '加入中...' : '🛒 加入购物车' }}
            </button>
          </div>
          <p v-if="actionMessage" class="success-msg">{{ actionMessage }}</p>
          <p v-if="actionError" class="error-msg">{{ actionError }}</p>
        </div>
      </article>

      <article class="panel bottom-card">
        <header class="tabs-bar">
          <div class="tabs-left">
            <button type="button" :class="{ active: activeTab === 'detail' }" @click="activeTab = 'detail'">详情</button>
            <button type="button" :class="{ active: activeTab === 'comment' }" @click="activeTab = 'comment'">评价(0)</button>
          </div>
          <button type="button" class="btn-mini-cart" :disabled="addingToCart || detail.stock <= 0" @click="addToCart">
            {{ addingToCart ? '加入中...' : '🛒 加入购物车' }}
          </button>
        </header>

        <div v-if="activeTab === 'detail'" class="detail-long-images">
          <img src="https://placehold.co/1120x800/e9edff/6572c0?text=Detail+Long+Image+1" alt="detail-1" />
          <img src="https://placehold.co/1120x800/f4f7ff/4d7cc3?text=Detail+Long+Image+2" alt="detail-2" />
          <img src="https://placehold.co/1120x800/eff9ff/3a8cb2?text=Detail+Long+Image+3" alt="detail-3" />
          <div v-if="detail.detailHtml" class="detail-html" v-html="detail.detailHtml"></div>
        </div>

        <div v-else class="comment-empty">暂无评价，欢迎购买后留下第一条评价。</div>
      </article>
    </template>
  </section>
</template>

<style scoped>
.detail-page {
  display: grid;
  gap: 14px;
  background: #f7f7f7;
}

.state {
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}

.top-card,
.bottom-card {
  background: #fff;
  border: 1px solid #ececec;
  border-radius: 8px;
  padding: 16px;
}

.top-card {
  display: grid;
  grid-template-columns: 38% 1fr;
  gap: 18px;
}

.gallery-col {
  display: grid;
  gap: 10px;
  align-content: start;
}

.main-image-wrap {
  border: 1px solid #e9e9e9;
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.main-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: contain;
  display: block;
}

.thumb-list {
  display: flex;
  gap: 8px;
}

.thumb-item {
  border: 1px solid #e2e2e2;
  border-radius: 4px;
  background: #fff;
  padding: 2px;
  width: 50px;
  height: 50px;
  cursor: pointer;
}

.thumb-item.active {
  border-color: #e60012;
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.fav-btn {
  justify-self: center;
  border: none;
  background: transparent;
  color: #666;
  cursor: pointer;
  font-size: 14px;
}

.title {
  margin: 0;
  color: #222;
  font-size: 26px;
  line-height: 1.3;
}

.selling-point {
  margin: 8px 0 12px;
  color: #e34b2f;
  font-size: 18px;
}

.price-panel {
  background: linear-gradient(135deg, #f8f8f8 0%, #f4f4f4 100%);
  border: 1px solid #ececec;
  border-radius: 6px;
  padding: 12px 14px;
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: end;
  gap: 10px;
}

.price-lines {
  display: grid;
  gap: 8px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 22px;
}

.price-row span {
  color: #555;
  width: 44px;
}

.old-price {
  color: #8f8f8f;
  font-size: 34px;
  text-decoration: line-through;
}

.sale-price {
  color: #e60012;
  font-size: 50px;
  line-height: 1;
  font-weight: 700;
}

.price-meta {
  color: #666;
  font-size: 13px;
  padding-bottom: 6px;
}

.quantity-row {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
  color: #444;
}

.stepper {
  display: grid;
  grid-template-columns: 32px 42px 32px;
  align-items: center;
}

.stepper button {
  height: 32px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
}

.stepper input {
  height: 32px;
  border: 1px solid #ddd;
  border-left: none;
  border-right: none;
  text-align: center;
}

.quantity-row small {
  color: #888;
}

.action-row {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.btn-buy,
.btn-cart,
.btn-mini-cart {
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 700;
}

.btn-buy {
  min-width: 110px;
  border: none;
  color: #fff;
  background: #e60012;
}

.btn-cart,
.btn-mini-cart {
  min-width: 130px;
  border: 1px solid #ffc3b7;
  color: #e55334;
  background: #fff3ef;
}

.tabs-bar {
  border: 1px solid #ececec;
  border-radius: 6px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 8px;
}

.tabs-left {
  display: flex;
}

.tabs-left button {
  width: 120px;
  height: 44px;
  border: none;
  background: #fff;
  color: #333;
  cursor: pointer;
  position: relative;
}

.tabs-left button.active {
  color: #e60012;
}

.tabs-left button.active::after {
  content: '';
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 0;
  height: 2px;
  background: #e60012;
}

.btn-mini-cart {
  height: 34px;
  min-width: 120px;
}

.success-msg {
  margin-top: 10px;
  color: #1f8f5f;
  font-size: 14px;
}

.error-msg {
  margin-top: 10px;
  color: #d64545;
  font-size: 14px;
}

.detail-long-images {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}

.detail-long-images img {
  width: 100%;
  display: block;
  border-radius: 8px;
}

.detail-html {
  border: 1px solid #ececec;
  border-radius: 8px;
  padding: 12px;
  color: #444;
}

.comment-empty {
  margin-top: 14px;
  padding: 20px;
  border: 1px dashed #ddd;
  border-radius: 8px;
  text-align: center;
  color: #999;
}

@media (max-width: 960px) {
  .top-card {
    grid-template-columns: 1fr;
  }

  .sale-price {
    font-size: 40px;
  }

  .old-price {
    font-size: 28px;
  }

  .tabs-left button {
    width: 100px;
  }
}
</style>
