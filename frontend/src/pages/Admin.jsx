import { useState } from 'react'
import { apiErrorMessage, getDailyReport, getUserReport } from '../services/api'

function today() {
  const now = new Date()
  const offset = now.getTimezoneOffset()
  return new Date(now.getTime() - offset * 60_000).toISOString().slice(0, 10)
}

export default function Admin() {
  const [date, setDate] = useState(today)
  const [daily, setDaily] = useState(null)
  const [dailyError, setDailyError] = useState('')
  const [dailyBusy, setDailyBusy] = useState(false)
  const [userId, setUserId] = useState('')
  const [userReport, setUserReport] = useState(null)
  const [userError, setUserError] = useState('')
  const [userBusy, setUserBusy] = useState(false)

  async function submitDaily(event) {
    event.preventDefault()
    setDailyError('')
    setDailyBusy(true)
    try {
      setDaily(await getDailyReport(date))
    } catch (error) {
      setDaily(null)
      setDailyError(apiErrorMessage(error, undefined, 'Access denied. Admin privileges are required.'))
    } finally {
      setDailyBusy(false)
    }
  }

  async function submitUser(event) {
    event.preventDefault()
    setUserError('')
    if (!/^\d+$/.test(userId.trim())) {
      setUserError('Enter a valid user ID.')
      return
    }
    setUserBusy(true)
    try {
      setUserReport(await getUserReport(userId.trim()))
    } catch (error) {
      setUserReport(null)
      setUserError(apiErrorMessage(error, undefined, 'Access denied. Admin privileges are required.'))
    } finally {
      setUserBusy(false)
    }
  }

  return (
    <main className="page-shell">
      <div className="page-heading"><div><div className="eyebrow">Administration</div><h1>Reports</h1><p className="muted">Review player activity and correct guesses.</p></div></div>
      <div className="report-grid">
        <section className="card report-card">
          <div className="report-icon">◷</div><h2>Daily report</h2><p className="muted">See distinct players and correct guesses for a date.</p>
          <form className="inline-form report-form" onSubmit={submitDaily}>
            <label htmlFor="daily-date">Date</label><input id="daily-date" type="date" required value={date} onChange={(event) => setDate(event.target.value)} />
            <button className="button button-primary" disabled={dailyBusy} type="submit">{dailyBusy ? 'Loading…' : 'Generate Daily Report'}</button>
          </form>
          {dailyError && <div className="alert alert-error" role="alert">{dailyError}</div>}
          {daily && <div className="report-result"><h3>Daily Report</h3><dl className="metric-list"><div><dt>Date</dt><dd>{daily.date}</dd></div><div><dt>Number of Users</dt><dd>{daily.numberOfUsers}</dd></div><div><dt>Number of Correct Guesses</dt><dd>{daily.numberOfCorrectGuesses}</dd></div></dl></div>}
        </section>

        <section className="card report-card">
          <div className="report-icon">◎</div><h2>User report</h2><p className="muted">See games tried and correct guesses by date.</p>
          <form className="inline-form report-form" onSubmit={submitUser}>
            <label htmlFor="report-user-id">User ID</label><input id="report-user-id" inputMode="numeric" placeholder="e.g. 12" required value={userId} onChange={(event) => setUserId(event.target.value.replace(/\D/g, ''))} />
            <button className="button button-primary" disabled={userBusy} type="submit">{userBusy ? 'Loading…' : 'Generate User Report'}</button>
          </form>
          {userError && <div className="alert alert-error" role="alert">{userError}</div>}
          {userReport && <div className="report-result"><h3>{userReport.username}</h3><p className="muted">User ID: {userReport.userId}</p>
            {userReport.reports.length === 0 ? <p className="empty-inline">No game activity found.</p> : (
              <div className="table-wrap"><table><thead><tr><th>Date</th><th>Words tried</th><th>Correct guesses</th></tr></thead><tbody>
                {userReport.reports.map((entry) => <tr key={entry.date}><td>{entry.date}</td><td>{entry.numberOfWordsTried}</td><td>{entry.numberOfCorrectGuesses}</td></tr>)}
              </tbody></table></div>
            )}
          </div>}
        </section>
      </div>
    </main>
  )
}
