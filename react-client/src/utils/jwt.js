import { jwtDecode } from 'jwt-decode';

export function getPayload() {
  const token = localStorage.getItem('token');
  if (!token) return null;
  try {
    return jwtDecode(token);
  } catch {
    return null;
  }
}

export function getUsername() {
  const p = getPayload();
  return p ? p.sub : null;
}

export function getRole() {
  const p = getPayload();
  return p && p.authorities ? p.authorities[0] : null;
}

export function isExpired() {
  const p = getPayload();
  return p ? p.exp * 1000 < Date.now() : true;
}
