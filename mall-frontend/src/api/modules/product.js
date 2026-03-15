import http from '../http'

export const productApi = {
  getHome(limit = 8) {
    return http.get('/product/home', { params: { limit } })
  },
  getDetail(id) {
    return http.get(`/product/${id}`)
  },
}