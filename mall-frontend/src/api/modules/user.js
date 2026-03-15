import http from '../http'

export const userApi = {
  register(payload) {
    return http.post('/user/register', payload)
  },
  login(payload) {
    return http.post('/user/login', payload)
  },
  info() {
    return http.get('/user/info')
  },
  updateNickname(payload) {
    return http.post('/user/nickname', payload)
  },
  bindPhone(payload) {
    return http.post('/user/phone/bind', payload)
  },
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return http.post('/user/avatar/upload', formData)
  },
  listAddress(params = {}) {
    return http.get('/user/address/list', { params })
  },
  addAddress(payload) {
    return http.post('/user/address/add', payload)
  },
  updateAddress(payload) {
    return http.put('/user/address/update', payload)
  },
  deleteAddress(id) {
    return http.delete(`/user/address/delete/${id}`)
  },
  deleteAddressBatch(ids) {
    return http.delete('/user/address/delete_batch', { data: { ids } })
  },
  deleteAllAddress() {
    return http.delete('/user/address/delete_all')
  },
}
