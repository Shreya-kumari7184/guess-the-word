import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { apiErrorMessage, getGameHistory } from '../services/api'
import GameBoard from '../components/GameBoard'
import { useAuth } from '../App'
import { clearLegacyHistory, readGameIds, saveGameId } from '../services/historyStorage'

export default function History() {
  const { user } = useAuth()
  const username = user.username
  const [savedHistory, setSavedHistory] = useState(() => ({ username, ids: readGameIds(username) }))
  const [loadedGames, setLoadedGames] = useState(() => ({ username, items: [] }))
  const gameIds = savedHistory.username === username ? savedHistory.ids : []
  const games = loadedGames.username === username ? loadedGames.items : []
  const [gameId, setGameId] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    clearLegacyHistory()
    setSavedHistory({ username, ids: readGameIds(username) })
    setLoadedGames({ username, items: [] })
  }, [username])

  useEffect(() => {
    let active = true
    Promise.all(gameIds.map(async (id) => {
      try {
        const history = await getGameHistory(id)
        return { history }
      } catch (requestError) {
        return { id, error: apiErrorMessage(requestError, 'Could not load this game.') }
      }
    })).then((items) => {
      if (active) setLoadedGames({ username, items })
    })
    return () => { active = false }
  }, [gameIds, username])

  async function loadGame(event) {
    event.preventDefault()
    if (!/^\d+$/.test(gameId.trim())) {
      setError('Enter a valid game ID.')
      return
    }
    setBusy(true)
    setError('')
    try {
      const history = await getGameHistory(gameId.trim())
      const nextIds = saveGameId(username, history.gameId)
      setSavedHistory({ username, ids: nextIds })
      setLoadedGames((current) => ({
        username,
        items: [{ history }, ...current.items.filter((item) => String(item.history?.gameId) !== String(history.gameId))],
      }))
      setGameId('')
    } catch (requestError) {
      setError(apiErrorMessage(requestError, 'Could not load this game.'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="page-shell">
      <div className="page-heading"><div><div className="eyebrow">Your saved games</div><h1>Game history</h1><p className="muted">Review the guesses and clues for games you have played.</p></div></div>
      <section className="card history-search">
        <h2>Open a game</h2>
        <p className="muted">Your recently started games appear below. You can also enter a game ID.</p>
        <form className="inline-form" onSubmit={loadGame}>
          <label className="sr-only" htmlFor="history-game-id">Game ID</label>
          <input id="history-game-id" inputMode="numeric" placeholder="Game ID" value={gameId} onChange={(event) => setGameId(event.target.value.replace(/\D/g, ''))} />
          <button className="button button-secondary" disabled={busy} type="submit">{busy ? 'Loading…' : 'Open game'}</button>
        </form>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
      </section>
      {games.length === 0 ? (
        <section className="empty-state card"><div className="empty-icon">⌁</div><h2>No saved games yet</h2><p>Start a game to see it here.</p><Link className="button button-primary" to="/home">Start a game</Link></section>
      ) : (
        <div className="history-list">
          {games.map((item, index) => {
            if (item.error) return <div className="alert alert-error" key={`${item.id}-${index}`}>Game {item.id}: {item.error}</div>
            const game = item.history
            return (
              <article className="card history-card" key={`${game.gameId}-${index}`}>
                <div className="history-card-heading"><div><span className="eyebrow">Game {game.gameId}</span><h2>{game.playedDate}</h2></div><span className={`status-pill ${game.finished ? (game.won ? 'status-won' : 'status-lost') : 'status-active'}`}>{game.status.replace('_', ' ')}</span></div>
                <p className="muted">{game.guesses.length} guess{game.guesses.length === 1 ? '' : 'es'} · {game.guessesRemaining} remaining</p>
                <GameBoard guesses={game.guesses} />
              </article>
            )
          })}
        </div>
      )}
    </main>
  )
}
