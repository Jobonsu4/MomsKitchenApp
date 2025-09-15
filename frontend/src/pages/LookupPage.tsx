import { useState } from 'react'
import { OrdersApi } from '../api'
import type { OrderSummaryDTO } from '../api'

export default function LookupPage() {
  const [code, setCode] = useState('')
  const [phone, setPhone] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [order, setOrder] = useState<OrderSummaryDTO | null>(null)

  function doLookup() {
    setLoading(true); setError(null); setOrder(null)
    OrdersApi.lookup(code, phone)
      .then(setOrder)
      .catch((e: any) => setError(e?.body?.message || e.message || 'Not found'))
      .finally(() => setLoading(false))
  }

  return (
    <div>
      <h2>Order Lookup</h2>
      {loading && <div className="notice">Loading…</div>}
      {error && <div className="error">{error}</div>}

      <div style={{ display: 'grid', gap: 8, maxWidth: 420 }}>
        <input placeholder="Order Code" value={code} onChange={e=>setCode(e.target.value)} />
        <input placeholder="Phone (digits or formatted)" value={phone} onChange={e=>setPhone(e.target.value)} />
      </div>
      <div style={{ marginTop: 12 }}>
        <button onClick={doLookup} disabled={loading || !code || !phone}>Lookup</button>
      </div>

      {order && (
        <div className="quote" style={{ marginTop: 12 }}>
          <div><strong>{order.customerName}</strong> — {order.customerPhone}</div>
          <div>Code: {order.orderCode}</div>
          <div>Status: {order.status} · Payment: {order.paymentStatus}</div>
          <div>Total: ${order.totalAmount.toFixed(2)}</div>
        </div>
      )}
    </div>
  )
}

