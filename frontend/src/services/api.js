class ApiError extends Error {
  constructor(status, message, payload) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.payload = payload
  }
}

async function request(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    credentials: 'include',
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
  })

  const rawBody = await response.text()
  let payload = null
  if (rawBody) {
    try {
      payload = JSON.parse(rawBody)
    } catch {
      payload = null
    }
  }

  if (!response.ok) {
    if (response.status === 401 && path !== '/api/auth/login' && path !== '/api/auth/me') {
      window.dispatchEvent(new Event('session-expired'))
    }
    const message = typeof payload?.message === 'string'
      ? payload.message
      : response.status >= 500
        ? 'The server could not complete the request. Please try again.'
        : `Request failed (${response.status}).`
    throw new ApiError(response.status, message, payload)
  }

  return payload
}

export const apiErrorMessage = (error, fallback = 'Something went wrong. Please try again.', forbiddenMessage = 'Access denied. You do not have permission to access this resource.') => {
  if (error instanceof ApiError) {
    if (error.status === 403) return forbiddenMessage
    if (error.status >= 500) return 'The server could not complete the request. Please try again.'
    return error.message
  }
  return error?.message || fallback
}

export const register = (username, password) => request('/api/auth/register', {
  method: 'POST',
  body: JSON.stringify({ username, password }),
})

export const login = (username, password) => request('/api/auth/login', {
  method: 'POST',
  body: JSON.stringify({ username, password }),
})

export const logout = () => request('/api/auth/logout', { method: 'POST' })

export const getCurrentUser = () => request('/api/auth/me')

export const startGame = () => request('/api/games/start', { method: 'POST' })

export const submitGuess = (gameId, guess) => request(`/api/games/${encodeURIComponent(gameId)}/guess`, {
  method: 'POST',
  body: JSON.stringify({ guess }),
})

export const getGameHistory = (gameId) => request(`/api/games/${encodeURIComponent(gameId)}`)

export const getDailyReport = (date) => request(`/api/admin/reports/daily?date=${encodeURIComponent(date)}`)

export const getUserReport = (userId) => request(`/api/admin/reports/user/${encodeURIComponent(userId)}`)

export { ApiError }
