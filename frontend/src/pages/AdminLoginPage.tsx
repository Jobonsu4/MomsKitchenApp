import { useState, type FormEvent } from 'react'
import { setAdminKey, clearAdminKey } from '../api/base'
import { AdminApi } from '../api'

export default function AdminLoginPage({ onSuccess }: { onSuccess: () => void }) {
  const [pwd, setPwd] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function submit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    // Save and attempt a quick call to validate the key
    setAdminKey(pwd)
    AdminApi.listOrders({ size: 1 })
      .then(() => { setLoading(false); onSuccess() })
      .catch((err: any) => {
        clearAdminKey()
        setLoading(false)
        setError(err?.body?.message || 'Invalid password')
      })
  }

  return (
    <div>
      <h2>Admin — Sign In</h2>
      <p className="notice">Enter the admin password to manage orders.</p>
      <form onSubmit={submit} style={{ maxWidth: 360 }}>
        <label htmlFor="admin-password">Password</label>
        <input id="admin-password" type="password" value={pwd} onChange={(e) => setPwd(e.target.value)}
               placeholder="Enter admin password" required style={{ display: 'block', width: '100%', margin: '8px 0' }} />
        <button type="submit" disabled={loading}>
          {loading ? 'Signing in…' : 'Sign In'}
        </button>
      </form>
      {error && <div className="error" style={{ marginTop: 8 }}>{error}</div>}
    </div>
  )
}
