import { createContext, useContext, useEffect, useState } from 'react'
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import Admin from './pages/Admin'
import Game from './pages/Game'
import History from './pages/History'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import { getCurrentUser } from './services/api'

const AuthContext = createContext(null)
export const useAuth = () => useContext(AuthContext)

function LandingRedirect() {
  const { user, loading } = useAuth()
  if (loading) return <div className="page-state">Loading Guess the Word…</div>
  return <Navigate to={user ? (user.role === 'ADMIN' ? '/admin' : '/home') : '/login'} replace />
}

export default function App() {
  const navigate = useNavigate()
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    getCurrentUser()
      .then((currentUser) => { if (active) setUser(currentUser) })
      .catch(() => { if (active) setUser(null) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  useEffect(() => {
    function handleExpiredSession() {
      setUser(null)
      navigate('/login', { replace: true })
    }
    window.addEventListener('session-expired', handleExpiredSession)
    return () => window.removeEventListener('session-expired', handleExpiredSession)
  }, [navigate])

  return (
    <AuthContext.Provider value={{ user, setUser, loading }}>
      <div className="app-frame">
        <Navbar />
        <Routes>
          <Route path="/" element={<LandingRedirect />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route element={<ProtectedRoute role="PLAYER" />}>
            <Route path="/home" element={<Home />} />
            <Route path="/game/:gameId" element={<Game />} />
            <Route path="/history" element={<History />} />
          </Route>
          <Route element={<ProtectedRoute role="ADMIN" />}>
            <Route path="/admin" element={<Admin />} />
          </Route>
          <Route path="*" element={<LandingRedirect />} />
        </Routes>
      </div>
    </AuthContext.Provider>
  )
}
