<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { cartApi } from '../../api/modules/cart'

const page = ref(1)
const size = ref(5)
const total = ref(0)
const totalPages = ref(0)
const list = ref([])
const loading = ref(false)
const pageError = ref('')
const actionMessage = ref('')
const actionError = ref('')
const updatingId = ref(0)
const selectedIds = ref([])

const allChecked = computed(() => {
  if (list.value.length === 0) return false
  return list.value.every((item) => selectedIds.value.includes(item.id))
})

const selectedItems = computed(() => list.value.filter((item) => selectedIds.value.includes(item.id)))

const selectedAmount = computed(() =>
  selectedItems.value.reduce((sum, item) => sum + Number(item.subtotalAmount || 0), 0).toFixed(2)
)

const selectedCount = computed(() =>
  selectedItems.value.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
)

async function fetchCartList(targetPage = page.value) {
  loading.value = true
  pageError.value = ''
  try {
    const res = await cartApi.list({ page: targetPage, size: size.value })
    const data = res?.data || {}
    page.value = data.page || targetPage
    size.value = data.size || size.value
    total.value = data.total || 0
    totalPages.value = data.totalPages || 0
    list.value = data.list || []
  } catch (error) {
    list.value = []
    total.value = 0
    totalPages.value = 0
    pageError.value = error.message || '购物车加载失败'
  } finally {
    loading.value = false
  }
}

async function updateQuantity(item, nextQuantity) {
  if (nextQuantity < 1) return
  actionMessage.value = ''
  actionError.value = ''
  updatingId.value = item.id
  try {
    await cartApi.update({
      cartItemId: item.id,
      quantity: nextQuantity,
    })
    await fetchCartList(page.value)
    actionMessage.value = '数量更新成功'
  } catch (error) {
    actionError.value = error.message || '数量更新失败'
  } finally {
    updatingId.value = 0
  }
}

function toggleAll(checked) {
  if (checked) {
    selectedIds.value = list.value.map((item) => item.id)
    return
  }
  selectedIds.value = []
}

function toggleOne(id, checked) {
  if (checked) {
    if (!selectedIds.value.includes(id)) {
      selectedIds.value = [...selectedIds.value, id]
    }
    return
  }
  selectedIds.value = selectedIds.value.filter((itemId) => itemId !== id)
}

function buyAll() {
  actionError.value = ''
  if (list.value.length === 0) {
    actionError.value = '购物车为空，无法购买'
    return
  }
  const quantity = list.value.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
  const amount = list.value.reduce((sum, item) => sum + Number(item.subtotalAmount || 0), 0).toFixed(2)
  actionMessage.value = `准备购买全部商品，共 ${quantity} 件，总额 ￥${amount}（订单接口待接入）`
}

function buySelected() {
  actionError.value = ''
  if (selectedItems.value.length === 0) {
    actionError.value = '请先勾选要购买的商品'
    return
  }
  actionMessage.value = `准备购买已选商品，共 ${selectedCount.value} 件，总额 ￥${selectedAmount.value}（订单接口待接入）`
}

function buySingle(item) {
  actionError.value = ''
  actionMessage.value = `准备购买「${item.name}」，数量 ${item.quantity}，金额 ￥${item.subtotalAmount}（订单接口待接入）`
}

function goPrevPage() {
  if (page.value <= 1) return
  fetchCartList(page.value - 1)
}

function goNextPage() {
  if (page.value >= totalPages.value) return
  fetchCartList(page.value + 1)
}

watch(
  () => list.value,
  (nextList) => {
    const currentIds = nextList.map((item) => item.id)
    selectedIds.value = selectedIds.value.filter((id) => currentIds.includes(id))
  },
  { deep: true }
)

onMounted(() => {
  fetchCartList(1)
})
</script>

<template>
  <section class="panel cart-page">
    <h1>购物车</h1>
    <p class="subtitle">5 个商品一页，支持单选、多选、全选购买与数量修改。</p>

    <div class="toolbar">
      <label class="check-all">
        <input type="checkbox" :checked="allChecked" @change="toggleAll($event.target.checked)" />
        全选当前页
      </label>
      <div class="toolbar-actions">
        <button class="btn buy-all" type="button" @click="buyAll">购买全部</button>
        <button class="btn ghost" type="button" @click="buySelected">购买已选</button>
      </div>
    </div>

    <div v-if="loading" class="state">购物车加载中...</div>
    <div v-else-if="pageError" class="state error">{{ pageError }}</div>
    <div v-else-if="list.length === 0" class="state">购物车为空，快去添加商品吧。</div>

    <div v-else class="cart-list">
      <article v-for="item in list" :key="item.id" class="cart-item">
        <label class="select-box">
          <input
            type="checkbox"
            :checked="selectedIds.includes(item.id)"
            @change="toggleOne(item.id, $event.target.checked)"
          />
        </label>

        <img
          class="cover"
          :src="item.image || `https://placehold.co/96x96/f3f3f3/666666?text=SKU+${item.skuId}`"
          :alt="item.name"
        />

        <div class="meta">
          <h3>{{ item.name }}</h3>
          <p class="desc">{{ item.description }}</p>
          <p class="stock">库存：{{ item.stock }}</p>
        </div>

        <div class="price-col">
          <p>当前价：<strong>￥{{ item.currentPrice }}</strong></p>
          <p>加入价：￥{{ item.addedPrice }}</p>
        </div>

        <div class="qty-col">
          <button
            type="button"
            :disabled="updatingId === item.id || item.quantity <= 1"
            @click="updateQuantity(item, item.quantity - 1)"
          >-</button>
          <span>{{ item.quantity }}</span>
          <button
            type="button"
            :disabled="updatingId === item.id || item.quantity >= item.stock"
            @click="updateQuantity(item, item.quantity + 1)"
          >+</button>
        </div>

        <div class="subtotal-col">
          <p>小计</p>
          <strong>￥{{ item.subtotalAmount }}</strong>
          <button class="buy-one" type="button" @click="buySingle(item)">购买单个</button>
        </div>
      </article>
    </div>

    <footer class="cart-footer">
      <div class="summary">
        <span>已选 {{ selectedCount }} 件</span>
        <strong>合计 ￥{{ selectedAmount }}</strong>
      </div>

      <div class="pagination">
        <button type="button" :disabled="page <= 1" @click="goPrevPage">上一页</button>
        <span>第 {{ page }} / {{ totalPages || 1 }} 页</span>
        <button type="button" :disabled="page >= totalPages" @click="goNextPage">下一页</button>
      </div>
    </footer>

    <p v-if="actionMessage" class="feedback success">{{ actionMessage }}</p>
    <p v-if="actionError" class="feedback error">{{ actionError }}</p>
  </section>
</template>

<style scoped>
.cart-page {
  display: grid;
  gap: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.check-all {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #555;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.toolbar-actions .btn {
  margin-top: 0;
}

.state {
  min-height: 180px;
  border: 1px dashed #ddd;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}

.cart-list {
  display: grid;
  gap: 10px;
}

.cart-item {
  display: grid;
  grid-template-columns: 30px 96px 1fr 150px 120px 120px;
  gap: 12px;
  border: 1px solid #ececec;
  border-radius: 10px;
  padding: 12px;
  align-items: center;
}

.select-box {
  display: flex;
  justify-content: center;
}

.cover {
  width: 96px;
  height: 96px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ececec;
}

.meta h3 {
  margin: 0;
  font-size: 16px;
  color: #222;
}

.desc {
  margin-top: 6px;
  font-size: 13px;
  color: #666;
}

.stock {
  margin-top: 6px;
  font-size: 12px;
  color: #888;
}

.price-col p {
  margin: 4px 0;
  font-size: 13px;
  color: #666;
}

.price-col strong {
  color: #d33a2c;
}

.qty-col {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qty-col button {
  width: 28px;
  height: 28px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
}

.qty-col span {
  min-width: 24px;
  text-align: center;
}

.subtotal-col {
  display: grid;
  gap: 6px;
  justify-items: end;
}

.subtotal-col p {
  margin: 0;
  color: #888;
  font-size: 12px;
}

.subtotal-col strong {
  color: #d33a2c;
}

.buy-one {
  border: 1px solid #f2b6ae;
  color: #d33a2c;
  background: #fff4f2;
  border-radius: 8px;
  height: 30px;
  padding: 0 10px;
  cursor: pointer;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  border-top: 1px solid #efefef;
  padding-top: 12px;
}

.summary {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #555;
}

.summary strong {
  color: #d33a2c;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination button {
  height: 32px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  padding: 0 10px;
  cursor: pointer;
}

.feedback {
  margin: 0;
  font-size: 14px;
}

.feedback.success {
  color: #1f8f5f;
}

.feedback.error {
  color: #d64545;
}

@media (max-width: 980px) {
  .cart-item {
    grid-template-columns: 26px 72px 1fr;
  }

  .price-col,
  .qty-col,
  .subtotal-col {
    grid-column: 3;
    justify-items: start;
  }
}
</style>

