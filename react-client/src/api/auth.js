import axios from './axiosInstance';

export function login(username, password) {
  return axios.post('/user/authenticate', { username, password });
}

export function logout() {
  return axios.post('/user/logout');
}
