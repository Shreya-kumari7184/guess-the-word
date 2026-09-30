import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../App'
import { logout } from '../services/api'

export default function Navbar() {
  const { user, setUser } = useAuth()
  const navigate = useNavigate()

  if (!user) return null

  async function handleLogout() {
    try {
      await logout()
    } catch {
      // Clear local state even if the server session has already expired.
    } finally {
      setUser(null)
      navigate('/login', { replace: true })
    }
  }

  return (
    <header className="topbar">
      <Link className="brand" to={user.role === 'ADMIN' ? '/admin' : '/home'}>
        <span className="brand-mark">GW</span>
        <span>Guess the Word</span>
      </Link>
      <nav className="nav-links" aria-label="Main navigation">
        {user.role === 'ADMIN' ? (
          <Link to="/admin">Admin dashboard</Link>
        ) : (
          <>
            <Link to="/home">Home</Link>
            <Link to="/history">History</Link>
          </>
        )}
      </nav>
      <div className="nav-account">
        <span className="nav-username">{user.username}</span>
        <button className="button button-quiet button-small" onClick={handleLogout} type="button">Log out</button>
      </div>
    </header>
  )
}
