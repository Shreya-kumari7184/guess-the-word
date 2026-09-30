const LEGACY_GAME_IDS_KEY = 'guess-the-word-game-ids'
const HISTORY_KEY_PREFIX = 'guessWordHistory_'
const MAX_SAVED_GAME_IDS = 30

export function getHistoryKey(username) {
  return `${HISTORY_KEY_PREFIX}${username}`
}

export function clearLegacyHistory() {
  try {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(LEGACY_GAME_IDS_KEY)
    }
  } catch {
    // Storage may be unavailable or disabled in the browser.
  }
}

export function readGameIds(username) {
  if (!username || typeof localStorage === 'undefined') return []

  try {
    const stored = JSON.parse(localStorage.getItem(getHistoryKey(username)) || '[]')
    return Array.isArray(stored)
      ? stored.map(String).filter((id) => /^\d+$/.test(id)).slice(0, MAX_SAVED_GAME_IDS)
      : []
  } catch {
    return []
  }
}

export function saveGameId(username, gameId) {
  clearLegacyHistory()
  if (!username || gameId === undefined || gameId === null || typeof localStorage === 'undefined') return []

  const normalizedId = String(gameId)
  if (!/^\d+$/.test(normalizedId)) return readGameIds(username)

  const next = [normalizedId, ...readGameIds(username).filter((id) => id !== normalizedId)]
    .slice(0, MAX_SAVED_GAME_IDS)
  try {
    localStorage.setItem(getHistoryKey(username), JSON.stringify(next))
  } catch {
    // Keep the game usable even if browser storage is unavailable.
  }
  return next
}
