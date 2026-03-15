<script setup>
const products = [
  { id: 1001, name: 'Store Phone X', price: '4999.00', stock: 36 },
  { id: 1002, name: 'Store Earbuds Pro', price: '899.00', stock: 128 },
  { id: 1003, name: 'Store Watch S', price: '1599.00', stock: 52 },
]
</script>

<template>
  <section>
    <h1 class="page-title">商品列表</h1>
    <div class="grid cards-3">
      <article v-for="item in products" :key="item.id" class="panel product-card">
        <h3>{{ item.name }}</h3>
        <p>¥ {{ item.price }}</p>
        <p class="muted">库存 {{ item.stock }}</p>
        <RouterLink class="btn ghost" :to="`/products/${item.id}`">查看详情</RouterLink>
      </article>
    </div>
  </section>
</template>
