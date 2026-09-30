import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../App'
import { apiErrorMessage, getCurrentUser, login } from '../services/api'

export default function Login() {
  const { user, setUser } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  if (user) return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/home'} replace />

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setBusy(true)
    try {
      await login(username.trim(), password)
      const currentUser = await getCurrentUser()
      setUser(currentUser)
      const destination = currentUser.role === 'ADMIN' ? '/admin' : (location.state?.from || '/home')
      navigate(destination, { replace: true })
    } catch (requestError) {
      setError(apiErrorMessage(requestError, 'Invalid username or password.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-card">
        <div className="eyebrow">Welcome back</div>
        <h1>Sign in</h1>
        <p className="muted">Sign in to continue to Guess the Word.</p>
        {location.state?.message && <div className="alert alert-success" role="status">{location.state.message}</div>}
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <form className="form-stack" onSubmit={handleSubmit}>
          <label>Username<input autoComplete="username" required value={username} onChange={(event) => setUsername(event.target.value)} /></label>
          <label>Password<input autoComplete="current-password" required type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
          <button className="button button-primary button-wide" disabled={busy} type="submit">{busy ? 'Signing in…' : 'Login'}</button>
        </form>
        <p className="auth-switch">New here? <Link to="/register">Create a player account</Link></p>
      </section>
    </main>
  )
}
