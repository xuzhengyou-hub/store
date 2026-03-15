import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { userApi } from '../../api/modules/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('store_token') || '')
  const username = ref('')
  const phone = ref('')
  const nickname = ref('')
  const avatar = ref('')

  const isLoggedIn = computed(() => Boolean(token.value))
  const avatarUrl = computed(() => normalizeAvatarUrl(avatar.value))

  function normalizeAvatarUrl(value) {
    if (!value) return ''

    const raw = String(value).trim()
    if (!raw) return ''
    if (/^https?:\/\//i.test(raw) || raw.startsWith('data:')) return raw

    let normalized = raw.replace(/\\/g, '/')
    const projectPrefix = 'mall-frontend/'
    const publicPrefix = 'public/'

    const projectIndex = normalized.indexOf(projectPrefix)
    if (projectIndex >= 0) {
      normalized = normalized.slice(projectIndex + projectPrefix.length)
    }
    if (normalized.startsWith(publicPrefix)) {
      normalized = normalized.slice(publicPrefix.length)
    }

    return normalized.startsWith('/') ? normalized : `/${normalized}`
  }

  function setToken(nextToken) {
    token.value = nextToken || ''
    if (nextToken) {
      localStorage.setItem('store_token', nextToken)
    } else {
      localStorage.removeItem('store_token')
    }
  }

  function applyUserInfo(data) {
    username.value = data?.username || ''
    phone.value = data?.phone || ''
    nickname.value = data?.nickname || ''
    avatar.value = data?.avatar || ''
  }

  async function register(payload) {
    return userApi.register(payload)
  }

  async function login(payload) {
    const res = await userApi.login(payload)
    const nextToken = res?.data?.token || ''
    if (!nextToken) {
      throw new Error('登录失败，未获取到有效令牌')
    }
    setToken(nextToken)
    await loadUserInfo()
    return res
  }

  async function loadUserInfo() {
    if (!token.value) return null
    const res = await userApi.info()
    applyUserInfo(res?.data || {})
    return res
  }

  async function updateNickname(nicknameValue) {
    const res = await userApi.updateNickname({ nickname: nicknameValue })
    applyUserInfo(res?.data || {})
    return res
  }

  async function bindPhone(phoneValue, verificationCode) {
    const res = await userApi.bindPhone({ phone: phoneValue, verificationCode })
    applyUserInfo(res?.data || {})
    return res
  }

  async function uploadAvatar(file) {
    const res = await userApi.uploadAvatar(file)
    applyUserInfo(res?.data || {})
    return res
  }

  function logout() {
    setToken('')
    username.value = ''
    phone.value = ''
    nickname.value = ''
    avatar.value = ''
  }

  return {
    token,
    username,
    phone,
    nickname,
    avatar,
    avatarUrl,
    isLoggedIn,
    register,
    login,
    loadUserInfo,
    updateNickname,
    bindPhone,
    uploadAvatar,
    logout,
  }
})
