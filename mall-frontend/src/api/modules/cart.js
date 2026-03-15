import http from '../http'

export const cartApi = {
  add(payload) {
    return http.post('/cart/add', payload)
  },
  update(payload) {
    return http.put('/cart/update', payload)
  },
  list(params = {}) {
    return http.get('/cart/list', { params })
  },
}

