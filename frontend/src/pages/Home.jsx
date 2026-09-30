import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../App'
import { apiErrorMessage, startGame } from '../services/api'
import { saveGameId } from '../services/historyStorage'

export default function Home() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function handleStart() {
    setError('')
    setBusy(true)
    try {
      const game = await startGame()
      saveGameId(user.username, game.gameId)
      navigate(`/game/${game.gameId}`)
    } catch (requestError) {
      setError(apiErrorMessage(requestError))
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="page-shell">
      <section className="hero-card">
        <div className="hero-copy">
          <div className="eyebrow">A little word challenge</div>
          <h1>Guess the Word</h1>
          <p>Find the hidden five-letter word using the clues from each guess.</p>
          <button className="button button-primary" disabled={busy} onClick={handleStart} type="button">
            {busy ? 'Starting game…' : 'Start New Game'}
          </button>
          {error && <div className="alert alert-error home-error" role="alert">{error}</div>}
        </div>
        <div className="hero-tiles" aria-hidden="true">
          {['G', 'U', 'E', 'S', 'S'].map((letter, index) => <span className={`letter-cell ${index === 0 ? 'green' : index === 2 ? 'orange' : ''}`} key={index}>{letter}</span>)}
        </div>
      </section>

      <section className="info-grid" aria-label="How to play">
        <article className="info-card"><span className="info-number">01</span><h2>Three games a day</h2><p>You can play a maximum of 3 games per day.</p></article>
        <article className="info-card"><span className="info-number">02</span><h2>Five guesses</h2><p>Each game gives you up to 5 guesses to find the word.</p></article>
        <article className="info-card"><span className="info-number">03</span><h2>Five letters</h2><p>Every guess must contain exactly 5 letters.</p></article>
      </section>
      <p className="legend-note"><span className="legend-dot green-dot" /> Correct letter and position <span className="legend-dot orange-dot" /> Letter is in the word <span className="legend-dot grey-dot" /> Letter is not available</p>
    </main>
  )
}
