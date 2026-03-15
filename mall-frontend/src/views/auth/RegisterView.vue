<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../../store/modules/auth'

const authStore = useAuthStore()
const router = useRouter()

const form = ref({ username: '', password: '', confirmPassword: '' })
const submitting = ref(false)
const errorMessage = ref('')

async function onSubmit() {
  errorMessage.value = ''
  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }
  submitting.value = true
  try {
    await authStore.register({ username: form.value.username, password: form.value.password })
    router.push({ name: 'login', query: { fromRegister: '1' } })
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="panel auth-panel">
    <h1>创建账号</h1>
    <p class="subtitle">注册后即可开始下单与管理订单。</p>
    <form class="form" @submit.prevent="onSubmit">
      <label>
        用户名
        <input v-model="form.username" type="text" required minlength="3" maxlength="64" />
      </label>
      <label>
        密码
        <input v-model="form.password" type="password" required minlength="6" maxlength="64" />
      </label>
      <label>
        确认密码
        <input v-model="form.confirmPassword" type="password" required minlength="6" maxlength="64" />
      </label>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      <button class="btn" :disabled="submitting" type="submit">{{ submitting ? '注册中...' : '注册' }}</button>
    </form>
    <p class="hint">已有账号？<RouterLink to="/login">去登录</RouterLink></p>
  </section>
</template>
