import axios from 'axios'

const http = axios.create({
  baseURL: '/api/user',
  timeout: 10000,
})

export default http
