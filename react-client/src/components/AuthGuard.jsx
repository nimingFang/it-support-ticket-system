import { Navigate, Outlet } from 'react-router-dom';
import { isExpired } from '../utils/jwt';

export default function AuthGuard() {
  const token = localStorage.getItem('token');

  if (!token || isExpired()) {
    localStorage.removeItem('token');
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
