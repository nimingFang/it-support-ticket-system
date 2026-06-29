import { Outlet, useNavigate } from 'react-router-dom';
import { getUsername, getRole } from '../utils/jwt';
import { logout } from '../api/auth';

export default function Layout() {
  const navigate = useNavigate();
  const username = getUsername();
  const role = getRole();

  const handleLogout = async () => {
    try {
      await logout();
    } catch {
      // 即使登出失败也清除本地 Token
    }
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div className="app">
      <header className="topbar">
        <span className="topbar-title" onClick={() => navigate('/')}>
          IT Ticket System
        </span>
        <span className="topbar-user">
          {username} ({role}) &nbsp;
          <button onClick={handleLogout}>登出</button>
        </span>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
