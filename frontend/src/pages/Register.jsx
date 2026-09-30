import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { apiErrorMessage, register } from '../services/api'

export default function Register() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    if ((username.match(/[A-Za-z]/g) || []).length < 5) {
      setError('Username must contain at least 5 letters.')
      return
    }
    if (password.length < 5 || !/[A-Za-z]/.test(password) || !/\d/.test(password) || !/[$%*]/.test(password)) {
      setError('Password must be at least 5 characters and include a letter, a number, and one of $, %, or *.')
      return
    }
    if (password !== confirmation) {
      setError('Passwords do not match.')
      return
    }

    setBusy(true)
    try {
      await register(username.trim(), password)
      navigate('/login', { replace: true, state: { message: 'Account created. You can now sign in.' } })
    } catch (requestError) {
      setError(apiErrorMessage(requestError))
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-card">
        <div className="eyebrow">Join the challenge</div>
        <h1>Create account</h1>
        <p className="muted">New accounts are created as players.</p>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <form className="form-stack" onSubmit={handleSubmit}>
          <label>Username<input autoComplete="username" required value={username} onChange={(event) => setUsername(event.target.value)} /></label>
          <label>Password<input autoComplete="new-password" required type="password" value={password} onChange={(event) => setPassword(event.target.value)} /><small>At least 5 characters, with a letter, number, and $, % or *.</small></label>
          <label>Confirm password<input autoComplete="new-password" required type="password" value={confirmation} onChange={(event) => setConfirmation(event.target.value)} /></label>
          <button className="button button-primary button-wide" disabled={busy} type="submit">{busy ? 'Creating account…' : 'Register'}</button>
        </form>
        <p className="auth-switch">Already registered? <Link to="/login">Log in</Link></p>
      </section>
    </main>
  )
}
