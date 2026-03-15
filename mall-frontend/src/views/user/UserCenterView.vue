<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '../../store/modules/auth'
import { userApi } from '../../api/modules/user'

const authStore = useAuthStore()
const defaultAvatar = '/default-anime-avatar.svg'

const activeMenu = ref('profile')
const displayName = computed(() => authStore.nickname || authStore.username || '游客')
const avatarSrc = computed(() => authStore.avatarUrl || defaultAvatar)
const phoneDisplay = computed(() => authStore.phone || '未绑定')

const nicknameForm = ref({ nickname: '' })
const bindPhoneForm = ref({ phone: '', verificationCode: '' })
const savingNickname = ref(false)
const bindingPhone = ref(false)
const uploadingAvatar = ref(false)
const avatarInputRef = ref(null)

const page = ref(1)
const size = ref(5)
const total = ref(0)
const totalPages = ref(0)
const addressList = ref([])
const addressLoading = ref(false)
const addressError = ref('')
const selectedAddressIds = ref([])
const submittingAddress = ref(false)
const showAddressForm = ref(false)
const editingAddressId = ref(0)
const addressForm = ref({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  detailAddress: '',
  isDefault: 0,
})

const actionMessage = ref('')
const actionError = ref('')

const allAddressChecked = computed(() => {
  if (addressList.value.length === 0) return false
  return addressList.value.every((item) => selectedAddressIds.value.includes(item.id))
})

function switchMenu(menu) {
  activeMenu.value = menu
  actionMessage.value = ''
  actionError.value = ''
  if (menu === 'address') {
    fetchAddressList(1)
  }
}

function openAvatarPicker() {
  avatarInputRef.value?.click()
}

async function onAvatarFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  actionMessage.value = ''
  actionError.value = ''
  uploadingAvatar.value = true
  try {
    await authStore.uploadAvatar(file)
    actionMessage.value = '头像更新成功'
  } catch (error) {
    actionError.value = error.message
  } finally {
    uploadingAvatar.value = false
    event.target.value = ''
  }
}

async function saveNickname() {
  actionMessage.value = ''
  actionError.value = ''
  const nextNickname = nicknameForm.value.nickname.trim()
  if (!nextNickname) {
    actionError.value = '昵称不能为空'
    return
  }

  savingNickname.value = true
  try {
    await authStore.updateNickname(nextNickname)
    nicknameForm.value.nickname = ''
    actionMessage.value = '昵称更新成功'
  } catch (error) {
    actionError.value = error.message
  } finally {
    savingNickname.value = false
  }
}

async function bindPhone() {
  actionMessage.value = ''
  actionError.value = ''
  bindingPhone.value = true
  try {
    await authStore.bindPhone(bindPhoneForm.value.phone, bindPhoneForm.value.verificationCode)
    bindPhoneForm.value.verificationCode = ''
    actionMessage.value = '手机号绑定成功'
  } catch (error) {
    actionError.value = error.message
  } finally {
    bindingPhone.value = false
  }
}

async function fetchAddressList(targetPage = page.value) {
  addressLoading.value = true
  addressError.value = ''
  try {
    const res = await userApi.listAddress({ page: targetPage, size: size.value })
    const data = res?.data || {}
    page.value = data.page || targetPage
    size.value = data.size || size.value
    total.value = data.total || 0
    totalPages.value = data.totalPages || 0
    addressList.value = data.list || []
    const currentIds = addressList.value.map((item) => item.id)
    selectedAddressIds.value = selectedAddressIds.value.filter((id) => currentIds.includes(id))
  } catch (error) {
    addressList.value = []
    total.value = 0
    totalPages.value = 0
    addressError.value = error.message || '地址列表加载失败'
  } finally {
    addressLoading.value = false
  }
}

function resetAddressForm() {
  addressForm.value = {
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    detailAddress: '',
    isDefault: 0,
  }
}

function openAddAddress() {
  editingAddressId.value = 0
  showAddressForm.value = true
  resetAddressForm()
}

function openEditAddress(item) {
  editingAddressId.value = item.id
  showAddressForm.value = true
  addressForm.value = {
    receiverName: item.receiverName || '',
    receiverPhone: item.receiverPhone || '',
    province: item.province || '',
    city: item.city || '',
    detailAddress: item.detailAddress || '',
    isDefault: item.isDefault === 1 ? 1 : 0,
  }
}

function closeAddressForm() {
  showAddressForm.value = false
  editingAddressId.value = 0
}

async function submitAddress() {
  actionMessage.value = ''
  actionError.value = ''
  submittingAddress.value = true
  try {
    const payload = { ...addressForm.value }
    if (editingAddressId.value > 0) {
      await userApi.updateAddress({ id: editingAddressId.value, ...payload })
      actionMessage.value = '地址修改成功'
    } else {
      await userApi.addAddress(payload)
      actionMessage.value = '地址新增成功'
    }
    closeAddressForm()
    await fetchAddressList(page.value)
  } catch (error) {
    actionError.value = error.message || '地址保存失败'
  } finally {
    submittingAddress.value = false
  }
}

async function deleteOneAddress(id) {
  actionMessage.value = ''
  actionError.value = ''
  try {
    await userApi.deleteAddress(id)
    actionMessage.value = '地址删除成功'
    await fetchAddressList(page.value)
  } catch (error) {
    actionError.value = error.message || '地址删除失败'
  }
}

async function deleteBatchAddress() {
  actionMessage.value = ''
  actionError.value = ''
  if (selectedAddressIds.value.length === 0) {
    actionError.value = '请先选择要删除的地址'
    return
  }
  try {
    await userApi.deleteAddressBatch(selectedAddressIds.value)
    actionMessage.value = '批量删除成功'
    await fetchAddressList(page.value)
  } catch (error) {
    actionError.value = error.message || '批量删除失败'
  }
}

async function deleteAllAddress() {
  actionMessage.value = ''
  actionError.value = ''
  try {
    await userApi.deleteAllAddress()
    actionMessage.value = '已清空全部地址'
    await fetchAddressList(1)
  } catch (error) {
    actionError.value = error.message || '清空地址失败'
  }
}

function toggleAllAddress(checked) {
  if (checked) {
    selectedAddressIds.value = addressList.value.map((item) => item.id)
    return
  }
  selectedAddressIds.value = []
}

function toggleAddress(id, checked) {
  if (checked) {
    if (!selectedAddressIds.value.includes(id)) {
      selectedAddressIds.value = [...selectedAddressIds.value, id]
    }
    return
  }
  selectedAddressIds.value = selectedAddressIds.value.filter((itemId) => itemId !== id)
}

function goPrevPage() {
  if (page.value <= 1) return
  fetchAddressList(page.value - 1)
}

function goNextPage() {
  if (page.value >= totalPages.value) return
  fetchAddressList(page.value + 1)
}

onMounted(() => {
  if (activeMenu.value === 'address') {
    fetchAddressList(1)
  }
})
</script>

<template>
  <section class="panel user-center-page">
    <div class="left-menu">
      <button
        type="button"
        class="menu-item"
        :class="{ active: activeMenu === 'profile' }"
        @click="switchMenu('profile')"
      >
        个人资料
      </button>
      <button
        type="button"
        class="menu-item"
        :class="{ active: activeMenu === 'address' }"
        @click="switchMenu('address')"
      >
        查看地址
      </button>
    </div>

    <div class="right-content">
      <template v-if="activeMenu === 'profile'">
        <h1>个人中心</h1>
        <p class="subtitle">查看并管理你的账号信息。</p>

        <div class="user-card">
          <button class="user-avatar-btn" :disabled="uploadingAvatar" @click="openAvatarPicker">
            <img :src="avatarSrc" alt="avatar" />
          </button>
          <input
            ref="avatarInputRef"
            class="hidden-input"
            type="file"
            accept="image/*"
            @change="onAvatarFileChange"
          />
          <div class="user-meta">
            <h2>{{ displayName }}</h2>
            <p class="muted">@{{ authStore.username }}</p>
            <p class="hint">点击头像可更换本地图片</p>
          </div>
        </div>

        <div class="profile-grid">
          <p><strong>用户名：</strong>{{ authStore.username }}</p>
          <p><strong>手机号：</strong>{{ phoneDisplay }}</p>
          <p><strong>昵称：</strong>{{ authStore.nickname || '未设置' }}</p>
        </div>

        <div class="profile-actions">
          <section class="panel form-card">
            <h3>修改昵称</h3>
            <div class="form-inline">
              <input
                v-model="nicknameForm.nickname"
                type="text"
                maxlength="64"
                placeholder="请输入新的昵称"
              />
              <button class="btn" :disabled="savingNickname" @click="saveNickname">
                {{ savingNickname ? '保存中...' : '保存昵称' }}
              </button>
            </div>
          </section>

          <section class="panel form-card">
            <h3>绑定手机号</h3>
            <div class="form-inline">
              <input
                v-model="bindPhoneForm.phone"
                type="text"
                maxlength="11"
                placeholder="请输入手机号"
              />
              <input
                v-model="bindPhoneForm.verificationCode"
                type="text"
                maxlength="4"
                placeholder="验证码(1111)"
              />
              <button class="btn" :disabled="bindingPhone" @click="bindPhone">
                {{ bindingPhone ? '绑定中...' : '绑定手机号' }}
              </button>
            </div>
          </section>
        </div>
      </template>

      <template v-else>
        <div class="address-head">
          <div>
            <h1>地址管理</h1>
            <p class="subtitle">每页 5 条，支持新增、修改、单删、批删、清空全部。</p>
          </div>
          <button class="btn" type="button" @click="openAddAddress">添加地址</button>
        </div>

        <div class="address-toolbar">
          <label>
            <input type="checkbox" :checked="allAddressChecked" @change="toggleAllAddress($event.target.checked)" />
            全选当前页
          </label>
          <div class="toolbar-actions">
            <button class="btn ghost" type="button" @click="deleteBatchAddress">删除多个</button>
            <button class="btn ghost danger" type="button" @click="deleteAllAddress">删除全部</button>
          </div>
        </div>

        <div v-if="addressLoading" class="address-state">地址加载中...</div>
        <div v-else-if="addressError" class="address-state error">{{ addressError }}</div>
        <div v-else-if="addressList.length === 0" class="address-state">暂无地址，请先添加。</div>

        <div v-else class="address-list">
          <article v-for="item in addressList" :key="item.id" class="address-item">
            <label>
              <input
                type="checkbox"
                :checked="selectedAddressIds.includes(item.id)"
                @change="toggleAddress(item.id, $event.target.checked)"
              />
            </label>
            <div class="address-main">
              <p><strong>{{ item.receiverName }}</strong> {{ item.receiverPhone }}</p>
              <p>{{ item.province || '' }} {{ item.city || '' }} {{ item.detailAddress }}</p>
              <p v-if="item.isDefault === 1" class="default-tag">默认地址</p>
            </div>
            <div class="address-actions">
              <button type="button" @click="openEditAddress(item)">修改地址</button>
              <button type="button" class="danger" @click="deleteOneAddress(item.id)">删除一个</button>
            </div>
          </article>
        </div>

        <div class="address-pagination">
          <button type="button" :disabled="page <= 1" @click="goPrevPage">上一页</button>
          <span>第 {{ page }} / {{ totalPages || 1 }} 页，共 {{ total }} 条</span>
          <button type="button" :disabled="page >= totalPages" @click="goNextPage">下一页</button>
        </div>

        <div v-if="showAddressForm" class="address-form-wrap">
          <h3>{{ editingAddressId > 0 ? '修改地址' : '添加地址' }}</h3>
          <div class="address-form">
            <input v-model="addressForm.receiverName" type="text" placeholder="收货人姓名" />
            <input v-model="addressForm.receiverPhone" type="text" maxlength="11" placeholder="收货人手机号" />
            <input v-model="addressForm.province" type="text" placeholder="省份（可选）" />
            <input v-model="addressForm.city" type="text" placeholder="城市（可选）" />
            <input v-model="addressForm.detailAddress" type="text" placeholder="详细地址" />
            <label class="default-switch">
              <input v-model="addressForm.isDefault" type="checkbox" :true-value="1" :false-value="0" />
              设为默认地址
            </label>
          </div>
          <div class="form-actions">
            <button class="btn" :disabled="submittingAddress" @click="submitAddress">
              {{ submittingAddress ? '提交中...' : '确认保存' }}
            </button>
            <button class="btn ghost" :disabled="submittingAddress" @click="closeAddressForm">取消</button>
          </div>
        </div>
      </template>

      <p v-if="actionMessage" class="success">{{ actionMessage }}</p>
      <p v-if="actionError" class="error">{{ actionError }}</p>
    </div>
  </section>
</template>

<style scoped>
.user-center-page {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 16px;
}

.left-menu {
  border-right: 1px solid #ececec;
  padding-right: 12px;
  display: grid;
  gap: 8px;
  align-content: start;
}

.menu-item {
  height: 38px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
}

.menu-item.active {
  border-color: #e04a3a;
  color: #e04a3a;
  background: #fff4f1;
}

.right-content {
  display: grid;
  gap: 12px;
}

.user-card {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid #ececec;
  background: linear-gradient(135deg, #fffdf8 0%, #f4fbff 100%);
}

.user-card img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #ececec;
}

.user-avatar-btn {
  border: none;
  background: transparent;
  padding: 0;
  margin: 0;
  cursor: pointer;
}

.hidden-input {
  display: none;
}

.profile-grid {
  display: grid;
  gap: 10px;
}

.profile-grid p {
  margin: 0;
  border: 1px solid #ececec;
  border-radius: 10px;
  padding: 10px 12px;
  background: #fff;
}

.profile-actions {
  display: grid;
  gap: 12px;
}

.form-card h3 {
  margin: 0 0 10px;
}

.form-inline {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.form-inline input {
  min-width: 180px;
}

.address-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.address-head .btn {
  margin-top: 0;
}

.address-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.toolbar-actions .btn {
  margin-top: 0;
}

.btn.danger,
.toolbar-actions .danger {
  color: #c4372f;
  border-color: #e9b3af;
}

.address-state {
  min-height: 120px;
  border: 1px dashed #ddd;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}

.address-list {
  display: grid;
  gap: 10px;
}

.address-item {
  border: 1px solid #ececec;
  border-radius: 10px;
  padding: 12px;
  display: grid;
  grid-template-columns: 24px 1fr auto;
  gap: 12px;
  align-items: start;
}

.address-main p {
  margin: 0;
  color: #555;
}

.address-main p + p {
  margin-top: 6px;
}

.default-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #e04a3a;
  border: 1px solid #efc5bf;
  margin-top: 8px;
}

.address-actions {
  display: grid;
  gap: 8px;
}

.address-actions button {
  height: 30px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  padding: 0 10px;
}

.address-actions .danger {
  color: #c4372f;
  border-color: #e9b3af;
}

.address-pagination {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  align-items: center;
}

.address-pagination button {
  height: 30px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  padding: 0 10px;
}

.address-form-wrap {
  border: 1px solid #ececec;
  border-radius: 10px;
  padding: 12px;
  background: #fafafa;
}

.address-form-wrap h3 {
  margin: 0 0 10px;
}

.address-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.address-form input {
  width: 100%;
}

.default-switch {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #555;
}

.form-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.form-actions .btn {
  margin-top: 0;
}

@media (max-width: 960px) {
  .user-center-page {
    grid-template-columns: 1fr;
  }

  .left-menu {
    border-right: none;
    border-bottom: 1px solid #ececec;
    padding-right: 0;
    padding-bottom: 10px;
    grid-template-columns: 1fr 1fr;
  }

  .address-item {
    grid-template-columns: 1fr;
  }

  .address-form {
    grid-template-columns: 1fr;
  }
}
</style>

