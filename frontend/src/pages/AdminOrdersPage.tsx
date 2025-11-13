import { useEffect, useState } from 'react'
import { AdminApi } from '../api'
import type { Page, OrderListItemDTO, OrderSummaryDTO } from '../api'

export default function AdminOrdersPage({ onLogout }: { onLogout?: () => void }) {
  const [page, setPage] = useState<Page<OrderListItemDTO> | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selected, setSelected] = useState<OrderSummaryDTO | null>(null)

  function load(p = 0) {
    setLoading(true)
    setError(null)
    setSelected(null)
    AdminApi.listOrders({ page: p, size: 20, sort: 'createdAt,desc' })
      .then(setPage)
      .catch((e: any) => setError(e?.body?.message || e.message || 'Failed to load orders'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load(0) }, [])

  function openDetails(id: number) {
    setLoading(true)
    setError(null)
    AdminApi.getOrder(id)
      .then(setSelected)
      .catch((e: any) => setError(e?.body?.message || e.message || 'Failed to load order'))
      .finally(() => setLoading(false))
  }

  function markStatus(id: number, status: string) {
    setLoading(true)
    setError(null)
    AdminApi.updateStatus(id, status)
      .then(() => load(page?.number || 0))
      .catch((e: any) => setError(e?.body?.message || e.message || 'Update failed'))
      .finally(() => setLoading(false))
  }

  function markPayment(id: number, pay: string) {
    setLoading(true)
    setError(null)
    AdminApi.updatePayment(id, pay)
      .then(() => load(page?.number || 0))
      .catch((e: any) => setError(e?.body?.message || e.message || 'Update failed'))
      .finally(() => setLoading(false))
  }

  return (
    <div>
      <h2>Admin — Orders</h2>
      <div className="notice">Authenticated as admin; requests send X-Admin-Key.</div>
      {onLogout && (
        <button style={{ float: 'right', marginTop: -36 }} onClick={onLogout}>Sign out</button>
      )}
      {loading && <div className="notice">Loading…</div>}
      {error && <div className="error">{error}</div>}

      {page && (
        <div>
          <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem' }}>
            <thead>
              <tr>
                <th align="left">ID</th>
                <th align="left">Code</th>
                <th align="left">Customer</th>
                <th align="left">Pickup Day</th>
                <th align="left">Status</th>
                <th align="left">Payment</th>
                <th align="right">Total</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {page.content.map((o) => (
                <tr key={o.orderId} style={{ borderTop: '1px solid rgba(127,127,127,.3)' }}>
                  <td>{o.orderId}</td>
                  <td>{o.orderCode}</td>
                  <td>{o.customerName}</td>
                  <td>{formatDay(o.pickupTime)}</td>
                  <td>{o.pickupStatus}</td>
                  <td>{o.paymentStatus}</td>
                  <td align="right">${o.total?.toFixed ? o.total.toFixed(2) : o.total}</td>
                  <td>
                    <button onClick={() => openDetails(o.orderId)}>View</button>{' '}
                    <button onClick={() => markStatus(o.orderId, 'CONFIRMED')}>Confirm</button>{' '}
                    <button onClick={() => markStatus(o.orderId, 'READY')}>Ready</button>{' '}
                    <button onClick={() => markStatus(o.orderId, 'COMPLETED')}>Complete</button>{' '}
                    <button onClick={() => markPayment(o.orderId, 'PAID')}>Mark Paid</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {selected && (
        <div className="quote" style={{ marginTop: '1rem' }}>
          <h3>Order #{selected.id} — {selected.orderCode}</h3>
          <div>{selected.customerName} · {selected.customerPhone}</div>
          <div>Pickup: {formatDay(selected.pickupAt)} {formatTime(selected.pickupAt)}</div>
          <div>Status: {selected.status} · Payment: {selected.paymentStatus}</div>
          <div>Total: ${selected.totalAmount.toFixed(2)}</div>
          <ul>
            {selected.items.map((it, idx) => (
              <li key={idx}>
                {it.quantity} × {it.itemName} — ${it.lineSubtotal.toFixed(2)}
              </li>
            ))}
          </ul>
        </div>
      )}

      {!page && !loading && <button onClick={() => load(0)}>Load Orders</button>}
    </div>
  )
}

function parseMaybeDate(v: any): Date | null {
  if (!v) return null;
  // String ISO
  if (typeof v === 'string') {
    const d = new Date(v);
    return isNaN(d.getTime()) ? null : d;
  }
  // Jackson array form: [yyyy, M, d, H, m, s, ns]
  if (Array.isArray(v)) {
    const [y, M, d, h = 0, m = 0, s = 0] = v;
    const dt = new Date(y, (M ?? 1) - 1, d ?? 1, h, m, s);
    return isNaN(dt.getTime()) ? null : dt;
  }
  return null;
}

function formatDay(v: any) {
  const d = parseMaybeDate(v);
  return d ? d.toLocaleDateString(undefined, { weekday: 'short' }) : '-';
}

function formatTime(v: any) {
  const d = parseMaybeDate(v);
  return d ? d.toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' }) : '';
}
