import axios from './axiosInstance';

export function fetchTickets(params) {
  return axios.get('/tickets', { params });
}

export function getTicket(id) {
  return axios.get(`/tickets/${id}`);
}

export function createTicket(data) {
  return axios.post('/tickets', data);
}

export function assignTicket(id) {
  return axios.post(`/tickets/${id}/assign`);
}

export function updateStatus(id, status) {
  return axios.patch(`/tickets/${id}/status`, { status });
}
