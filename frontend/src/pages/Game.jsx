import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiErrorMessage, getGameHistory, submitGuess } from '../services/api'
import GameBoard from '../components/GameBoard'
import MessageModal from '../components/MessageModal'

export default function Game() {
  const { gameId } = useParams()
  const navigate = useNavigate()
  const [game, setGame] = useState(null)
  const [input, setInput] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [modal, setModal] = useState(null)

  useEffect(() => {
    let active = true
    getGameHistory(gameId)
      .then((history) => { if (active) setGame(history) })
      .catch((requestError) => { if (active) setError(apiErrorMessage(requestError, 'Could not load this game.')) })
    return () => { active = false }
  }, [gameId])

  async function handleSubmit(event) {
    event.preventDefault()
    const guess = input.trim().toUpperCase()
    if (!/^[A-Z]{5}$/.test(guess)) {
      setError('Guess must contain exactly 5 letters.')
      return
    }
    setBusy(true)
    setError('')
    try {
      const result = await submitGuess(gameId, guess)
      setInput('')
      setGame((current) => ({
        ...current,
        guesses: [...current.guesses, {
          guessNumber: result.guessNumber,
          guess: result.guess,
          results: result.results,
        }],
        guessesRemaining: result.guessesRemaining,
        status: result.status,
        won: result.won,
        finished: result.finished,
      }))
      try {
        setGame(await getGameHistory(gameId))
      } catch {
        // Keep the authoritative guess response visible if the follow-up read fails.
      }
      if (result.finished) {
        setModal({
          title: result.won ? 'Well done!' : 'Game complete',
          message: result.message,
        })
      }
    } catch (requestError) {
      setError(apiErrorMessage(requestError))
    } finally {
      setBusy(false)
    }
  }

  if (error && !game) {
    return <main className="page-shell"><div className="alert alert-error" role="alert">{error}</div><Link className="text-link" to="/home">Back home</Link></main>
  }
  if (!game) return <div className="page-state">Loading game…</div>

  const finished = game.finished
  return (
    <main className="page-shell narrow-shell">
      <div className="page-heading">
        <div><div className="eyebrow">Game {game.gameId}</div><h1>Make your guess</h1><p className="muted">{game.playedDate} · {game.guessesRemaining} guesses remaining</p></div>
        <span className={`status-pill ${finished ? (game.won ? 'status-won' : 'status-lost') : 'status-active'}`}>{game.status.replace('_', ' ')}</span>
      </div>
      <section className="card game-card">
        <GameBoard guesses={game.guesses} />
        <div className="board-legend"><span><i className="legend-dot green-dot" />Right place</span><span><i className="legend-dot orange-dot" />In the word</span><span><i className="legend-dot grey-dot" />Not in the word</span></div>
        {!finished ? (
          <form className="guess-form" onSubmit={handleSubmit}>
            <label htmlFor="guess-input">Your 5-letter guess</label>
            <div className="guess-control">
              <input id="guess-input" autoComplete="off" autoCapitalize="characters" maxLength={5} placeholder="ABCDE" value={input} onChange={(event) => setInput(event.target.value.replace(/[^a-z]/gi, '').slice(0, 5).toUpperCase())} />
              <button className="button button-primary" disabled={busy} type="submit">{busy ? 'Checking…' : 'Submit guess'}</button>
            </div>
            {error && <div className="alert alert-error" role="alert">{error}</div>}
          </form>
        ) : (
          <div className="completed-actions"><p>{game.won ? 'You solved this game.' : 'This game is complete.'}</p><Link className="button button-primary" to="/home">Back to Home</Link></div>
        )}
        <Link className="text-link game-history-link" to="/history">View game history</Link>
      </section>
      <MessageModal open={Boolean(modal)} title={modal?.title} message={modal?.message} onClose={() => setModal(null)} />
    </main>
  )
}
