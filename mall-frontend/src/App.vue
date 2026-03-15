<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './store/modules/auth'

const authStore = useAuthStore()
const router = useRouter()

const menu = [
  { label: '首页', to: '/' },
  { label: '购物车', to: '/cart' },
  { label: '我的订单', to: '/orders' },
  { label: '个人中心', to: '/user' },
]

const profileOpen = ref(false)
const defaultAvatar = '/default-anime-avatar.svg'
const profileRef = ref(null)
const avatarInputRef = ref(null)
const isNicknameEditing = ref(false)
const nicknameDraft = ref('')
const isPhoneEditing = ref(false)
const phoneDraft = ref('')
const phoneCodeDraft = ref('')
const savingNickname = ref(false)
const bindingPhone = ref(false)
const uploadingAvatar = ref(false)
const toastMessage = ref('')
const toastType = ref('success')
let toastTimer = null

const displayName = computed(() => authStore.nickname || authStore.username || '游客')
const avatarSrc = computed(() => authStore.avatarUrl || defaultAvatar)
const phoneDisplay = computed(() => authStore.phone || '未绑定')
const canRequestCode = computed(() => /^1\d{10}$/.test(phoneDraft.value.trim()))

function goLogin() {
  router.push('/login')
}

function toggleProfile() {
  profileOpen.value = !profileOpen.value
}

function closeProfile(event) {
  const target = event.target
  if (!(target instanceof Node)) return
  if (profileRef.value && profileRef.value.contains(target)) return
  profileOpen.value = false
}

function logout() {
  authStore.logout()
  profileOpen.value = false
  router.push('/')
}

function openAvatarPicker() {
  avatarInputRef.value?.click()
}

function showToast(message, type = 'success') {
  toastMessage.value = message
  toastType.value = type
  if (toastTimer) {
    clearTimeout(toastTimer)
  }
  toastTimer = setTimeout(() => {
    toastMessage.value = ''
  }, 2000)
}

async function onAvatarChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  uploadingAvatar.value = true
  try {
    await authStore.uploadAvatar(file)
    showToast('头像更新成功', 'success')
  } catch (error) {
    showToast(error.message, 'error')
  } finally {
    uploadingAvatar.value = false
    event.target.value = ''
  }
}

function startNicknameEdit() {
  isNicknameEditing.value = true
  nicknameDraft.value = authStore.nickname || ''
}

function cancelNicknameEdit() {
  isNicknameEditing.value = false
  nicknameDraft.value = ''
}

async function confirmNicknameEdit() {
  const nextNickname = nicknameDraft.value.trim()
  if (!nextNickname) {
    showToast('昵称不能为空', 'error')
    return
  }
  savingNickname.value = true
  try {
    await authStore.updateNickname(nextNickname)
    isNicknameEditing.value = false
    nicknameDraft.value = ''
    showToast('昵称更新成功', 'success')
  } catch (error) {
    showToast(error.message, 'error')
  } finally {
    savingNickname.value = false
  }
}

function startPhoneEdit() {
  isPhoneEditing.value = true
  phoneDraft.value = ''
  phoneCodeDraft.value = ''
}

function cancelPhoneEdit() {
  isPhoneEditing.value = false
  phoneDraft.value = ''
  phoneCodeDraft.value = ''
}

function requestPhoneCode() {
  if (!canRequestCode.value) return
  showToast('验证码已发送，测试验证码为 1111', 'success')
}

async function confirmPhoneEdit() {
  if (!phoneDraft.value.trim()) {
    showToast('手机号不能为空', 'error')
    return
  }
  bindingPhone.value = true
  try {
    await authStore.bindPhone(phoneDraft.value.trim(), phoneCodeDraft.value.trim())
    isPhoneEditing.value = false
    phoneDraft.value = ''
    phoneCodeDraft.value = ''
    showToast('手机号已修改', 'success')
  } catch (error) {
    showToast(error.message, 'error')
  } finally {
    bindingPhone.value = false
  }
}

onMounted(async () => {
  try {
    await authStore.loadUserInfo()
  } catch {
    authStore.logout()
  }
  document.addEventListener('click', closeProfile)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeProfile)
  if (toastTimer) {
    clearTimeout(toastTimer)
  }
})
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand">Store</div>
      <nav class="menu">
        <RouterLink v-for="item in menu" :key="item.to" :to="item.to">{{ item.label }}</RouterLink>
      </nav>

      <div class="actions">
        <button v-if="!authStore.isLoggedIn" class="btn" @click="goLogin">登录</button>
        <div v-else ref="profileRef" class="profile">
          <button class="avatar-btn" @click="toggleProfile">
            <img :src="avatarSrc" alt="avatar" />
          </button>
          <div v-if="profileOpen" class="profile-card panel" @click.stop>
            <div class="profile-head">
              <button class="avatar-edit-btn" :disabled="uploadingAvatar" @click="openAvatarPicker">
                <img :src="avatarSrc" alt="avatar" />
              </button>
              <input
                ref="avatarInputRef"
                class="hidden-input"
                type="file"
                accept="image/*"
                @change="onAvatarChange"
              />
              <div>
                <p class="name">{{ displayName }}</p>
                <p class="muted">@{{ authStore.username }}</p>
                <p class="muted click-tip">点击头像可更换本地图片</p>
              </div>
            </div>
            <div class="profile-body">
              <div class="nickname-row">
                <strong>昵称：</strong>
                <template v-if="!isNicknameEditing">
                  <span>{{ authStore.nickname || '未设置' }}</span>
                  <button class="btn tiny ghost" @click="startNicknameEdit">修改</button>
                </template>
                <template v-else>
                  <input
                    v-model="nicknameDraft"
                    type="text"
                    maxlength="64"
                    placeholder="输入新昵称"
                  />
                  <button class="btn tiny" :disabled="savingNickname" @click="confirmNicknameEdit">
                    {{ savingNickname ? '确认中...' : '确认' }}
                  </button>
                  <button class="btn tiny ghost" :disabled="savingNickname" @click="cancelNicknameEdit">取消</button>
                </template>
              </div>
              <div class="info-row">
                <strong>用户名：</strong>
                <span>{{ authStore.username }}</span>
              </div>
              <div class="phone-row">
                <strong>手机号：</strong>
                <template v-if="!isPhoneEditing">
                  <span>{{ phoneDisplay }}</span>
                  <button class="btn tiny ghost" @click="startPhoneEdit">修改</button>
                </template>
                <template v-else>
                  <input
                    v-model="phoneDraft"
                    class="phone-edit-input"
                    type="text"
                    maxlength="11"
                    placeholder="输入新手机号"
                  />
                </template>
              </div>
              <div v-if="isPhoneEditing" class="phone-edit">
                <div class="code-row">
                  <input
                    v-model="phoneCodeDraft"
                    class="phone-edit-input"
                    type="text"
                    maxlength="4"
                    placeholder="输入验证码"
                  />
                  <button class="btn tiny ghost" :disabled="!canRequestCode" @click="requestPhoneCode">验证码</button>
                </div>
                <div class="confirm-row">
                  <button class="btn tiny" :disabled="bindingPhone" @click="confirmPhoneEdit">
                    {{ bindingPhone ? '确认中...' : '确认' }}
                  </button>
                  <button class="btn tiny ghost" :disabled="bindingPhone" @click="cancelPhoneEdit">取消</button>
                </div>
              </div>
            </div>
            <div v-if="toastMessage" class="card-toast" :class="toastType">{{ toastMessage }}</div>
            <button class="btn ghost" @click="logout">退出登录</button>
          </div>
        </div>
      </div>
    </header>

    <main class="content">
      <RouterView />
    </main>
  </div>
</template>
