import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../App'

export default function ProtectedRoute({ role }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) return <div className="page-state">Checking your session…</div>
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (role && user.role !== role) {
    return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/home'} replace />
  }
  return <Outlet />
}
