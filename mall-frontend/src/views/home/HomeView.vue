<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { productApi } from '../../api/modules/product'

const navLinks = [
  { name: '首页', active: true },
  { name: '运动健康', active: false },
  { name: '汽车用品', active: false },
  { name: '家用电器', active: false },
  { name: '食品生鲜', active: false },
  { name: '美妆个护', active: false },
]

const categoryList = [
  '数码办公',
  '时尚服饰',
  '家居家装',
  '个护清洁',
  '食品酒饮',
  '运动户外',
  '母婴玩具',
  '家用电器',
]

const newsLinks = ['资讯头条', '品牌专区', '关于我们', '联系我们', '售后服务']

const promoTags = ['手机通讯', '手机配件', '影音娱乐', '智能设备', '摄影摄像', '办公设备']

const products = ref([])
const loadingProducts = ref(false)
const productError = ref('')

const banners = [
  {
    image: 'https://placehold.co/760x360/f5d2b8/9a5030?text=Hero+Banner+1',
    kicker: 'TRENDING BRAND',
    title: '新品季 · 焕新科技生活',
    desc: '精选数码品牌联合活动，限时直降，低至 5 折起。',
  },
  {
    image: '/lunbao2.jpg',
    kicker: 'BRAND WEEK',
    title: '尖货上新 · 品牌狂欢周',
    desc: '热门品类爆款集结，爆款设备限时补贴进行中。',
  },
]

const bannerIndex = ref(0)
let bannerTimer = null
const currentBanner = computed(() => banners[bannerIndex.value])

function goBanner(index) {
  bannerIndex.value = index
}

function startBannerAutoPlay() {
  bannerTimer = setInterval(() => {
    bannerIndex.value = (bannerIndex.value + 1) % banners.length
  }, 3500)
}

async function fetchHomeProducts() {
  loadingProducts.value = true
  productError.value = ''
  try {
    const res = await productApi.getHome(8)
    products.value = res?.data?.products || []
  } catch (error) {
    products.value = []
    productError.value = error.message || '商品加载失败'
  } finally {
    loadingProducts.value = false
  }
}

onMounted(() => {
  startBannerAutoPlay()
  fetchHomeProducts()
})

onBeforeUnmount(() => {
  if (bannerTimer) clearInterval(bannerTimer)
})
</script>

<template>
  <div class="home-page">
    <section class="home-navbar">
      <div class="all-category">全部商品分类</div>
      <nav class="nav-links">
        <a
          v-for="link in navLinks"
          :key="link.name"
          href="javascript:void(0)"
          :class="{ active: link.active }"
        >
          {{ link.name }}
        </a>
      </nav>
    </section>

    <section class="hero-grid">
      <aside class="left-menu card">
        <a v-for="item in categoryList" :key="item" href="javascript:void(0)" class="menu-item">
          <span class="menu-dot"></span>
          <span class="menu-text">{{ item }}</span>
          <span class="menu-arrow">&gt;</span>
        </a>
      </aside>

      <section class="hero-banner card">
        <div class="banner-content">
          <p class="banner-kicker">{{ currentBanner.kicker }}</p>
          <h2>{{ currentBanner.title }}</h2>
          <p>{{ currentBanner.desc }}</p>
        </div>
        <img :src="currentBanner.image" :alt="currentBanner.title" />
        <div class="banner-dots">
          <button
            v-for="(item, index) in banners"
            :key="item.title"
            type="button"
            :class="{ active: index === bannerIndex }"
            @click="goBanner(index)"
          ></button>
        </div>
      </section>

      <aside class="right-panel card">
        <div class="news-box">
          <h3><span class="mark"></span> 资讯动态</h3>
          <a v-for="item in newsLinks" :key="item" href="javascript:void(0)">{{ item }}</a>
        </div>
      </aside>
    </section>

    <section class="floor-section">
      <header class="floor-head">
        <div class="title-group">
          <h2>数码办公</h2>
          <p>天天新品，科技带来快乐！</p>
        </div>
        <nav class="sub-nav">
          <a href="javascript:void(0)">手机</a>
          <a href="javascript:void(0)">电脑</a>
          <a href="javascript:void(0)">更多 &gt;</a>
        </nav>
      </header>

      <div class="floor-body">
        <aside class="floor-promo card">
          <h3>办公焕新季</h3>
          <p>高性能设备低价钜惠</p>
          <img src="https://placehold.co/220x220/e6e6e6/666666?text=Promo" alt="promo" />
          <div class="promo-tags">
            <a v-for="tag in promoTags" :key="tag" href="javascript:void(0)">{{ tag }}</a>
          </div>
        </aside>

        <section class="product-grid">
          <article v-for="item in products" :key="item.id" class="product-card card">
            <img
              :src="item.image || `https://placehold.co/180x180/f8f8f8/666666?text=Product+${item.id}`"
              :alt="item.name"
              class="product-img"
            />
            <p class="price">￥{{ item.price }}</p>
            <p class="old-price">￥{{ item.originalPrice }}</p>
            <h4>{{ item.name }}</h4>
            <p class="desc">{{ item.description }}</p>
            <RouterLink class="detail-link" :to="`/products/${item.id}`">查看详情</RouterLink>
          </article>

          <div v-if="loadingProducts" class="card product-state">商品加载中...</div>
          <div v-if="!loadingProducts && productError" class="card product-state">{{ productError }}</div>
          <div v-if="!loadingProducts && !productError && products.length === 0" class="card product-state">暂无商品数据</div>
        </section>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  display: grid;
  gap: 14px;
}

.card {
  background: #fff;
  border: 1px solid #ececec;
  border-radius: 10px;
}

.home-navbar {
  display: grid;
  grid-template-columns: 210px 1fr;
  align-items: center;
  min-height: 54px;
  border-bottom: 1px solid #efefef;
  background: #fff;
}

.all-category {
  background: #d93025;
  color: #fff;
  font-weight: 700;
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-links {
  display: flex;
  gap: 24px;
  padding: 0 20px;
}

.nav-links a {
  color: #333;
  font-size: 15px;
  height: 54px;
  display: inline-flex;
  align-items: center;
  border-bottom: 2px solid transparent;
}

.nav-links a.active {
  color: #d93025;
  border-bottom-color: #d93025;
  font-weight: 700;
}

.hero-grid {
  display: grid;
  grid-template-columns: 210px 1fr 280px;
  gap: 12px;
}

.left-menu {
  padding: 6px 0;
}

.menu-item {
  display: grid;
  grid-template-columns: 12px 1fr 16px;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  color: #333;
  font-size: 14px;
}

.menu-item:hover {
  background: #fff5f4;
}

.menu-dot {
  width: 6px;
  height: 6px;
  background: #d93025;
  border-radius: 999px;
  justify-self: center;
}

.menu-arrow {
  color: #999;
  text-align: right;
}

.hero-banner {
  position: relative;
  overflow: hidden;
  min-height: 360px;
  background: linear-gradient(135deg, #f9e6d8 0%, #f8d8bc 100%);
}

.hero-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-dots {
  position: absolute;
  left: 24px;
  bottom: 18px;
  z-index: 3;
  display: flex;
  gap: 8px;
}

.banner-dots button {
  width: 9px;
  height: 9px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  padding: 0;
}

.banner-dots button.active {
  background: #e43c31;
  width: 18px;
}

.banner-content {
  position: absolute;
  left: 24px;
  top: 24px;
  z-index: 2;
  max-width: 280px;
  color: #5c2a16;
}

.banner-content h2 {
  margin: 8px 0;
  font-size: 28px;
  line-height: 1.2;
}

.banner-content p {
  color: #7d4a35;
  font-size: 14px;
}

.banner-kicker {
  margin: 0;
  letter-spacing: 0.08em;
  font-weight: 700;
}

.right-panel {
  padding: 14px;
}

.news-box h3 {
  margin: 0 0 8px;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.mark {
  width: 3px;
  height: 14px;
  background: #e43c31;
  border-radius: 3px;
}

.news-box {
  display: grid;
  gap: 8px;
}

.news-box a {
  color: #444;
  font-size: 13px;
}

.floor-section {
  background: #fff;
  border: 1px solid #ececec;
  border-radius: 10px;
  padding: 14px;
}

.floor-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding-bottom: 10px;
  border-bottom: 1px solid #f1f1f1;
}

.title-group {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.title-group h2 {
  margin: 0;
  font-size: 24px;
  color: #2d2d2d;
}

.title-group p {
  margin: 0;
  color: #9a9a9a;
  font-size: 14px;
}

.sub-nav {
  display: flex;
  gap: 14px;
}

.sub-nav a {
  color: #666;
  font-size: 14px;
}

.floor-body {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 12px;
  margin-top: 12px;
}

.floor-promo {
  background: #f6f6f6;
  padding: 14px;
  display: grid;
  grid-template-rows: auto auto 1fr auto;
  gap: 10px;
}

.floor-promo h3 {
  margin: 0;
  font-size: 28px;
  line-height: 1;
  color: #333;
}

.floor-promo p {
  margin: 0;
  color: #8a8a8a;
  font-size: 13px;
}

.floor-promo img {
  width: 100%;
  border-radius: 8px;
}

.promo-tags {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
}

.promo-tags a {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  color: #666;
  font-size: 12px;
  text-align: center;
  padding: 6px 4px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.product-card {
  padding: 14px 12px;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.product-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.product-img {
  width: 100%;
  max-width: 180px;
  margin: 0 auto 10px;
  display: block;
}

.price {
  margin: 0;
  color: #e43c31;
  font-weight: 700;
  font-size: 22px;
}

.old-price {
  margin: 2px 0 8px;
  color: #9c9c9c;
  font-size: 13px;
  text-decoration: line-through;
}

.product-card h4 {
  margin: 0;
  color: #3b3b3b;
  font-size: 14px;
  line-height: 1.35;
  min-height: 38px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.desc {
  margin: 6px 0 0;
  color: #666;
  font-size: 13px;
  line-height: 1.35;
  min-height: 36px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.detail-link {
  display: inline-block;
  margin-top: 8px;
  color: #e43c31;
  font-size: 13px;
}

.product-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 170px;
  color: #888;
  font-size: 14px;
  grid-column: 1 / -1;
}

@media (max-width: 1200px) {
  .hero-grid {
    grid-template-columns: 210px 1fr;
  }

  .right-panel {
    grid-column: 1 / -1;
    grid-template-columns: 1fr 1fr;
    align-items: start;
  }

  .floor-body {
    grid-template-columns: 1fr;
  }

  .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .home-navbar {
    grid-template-columns: 1fr;
  }

  .all-category {
    height: 46px;
  }

  .nav-links {
    overflow-x: auto;
    gap: 16px;
  }

  .hero-grid {
    grid-template-columns: 1fr;
  }

  .right-panel {
    grid-template-columns: 1fr;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .title-group {
    display: grid;
    gap: 4px;
  }

  .floor-head {
    align-items: start;
    gap: 8px;
    flex-direction: column;
  }
}
</style>