<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '../../store/modules/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = ref({ username: '', password: '' })
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = computed(() => route.query.fromRegister === '1' ? '注册成功，请登录后继续。' : '')

async function onSubmit() {
  errorMessage.value = ''
  submitting.value = true
  try {
    await authStore.login(form.value)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="panel auth-panel">
    <h1>欢迎回来</h1>
    <p class="subtitle">登录后可查看购物车、订单与个人信息。</p>
    <p v-if="successMessage" class="success">{{ successMessage }}</p>
    <form class="form" @submit.prevent="onSubmit">
      <label>
        用户名
        <input v-model="form.username" type="text" required minlength="3" maxlength="64" />
      </label>
      <label>
        密码
        <input v-model="form.password" type="password" required minlength="6" maxlength="64" />
      </label>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      <button class="btn" :disabled="submitting" type="submit">{{ submitting ? '登录中...' : '登录' }}</button>
    </form>
    <p class="hint">还没有账号？<RouterLink to="/register">去注册</RouterLink></p>
  </section>
</template>
