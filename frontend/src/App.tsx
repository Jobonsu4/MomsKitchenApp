import { useEffect, useState } from 'react'
import './App.css'
import { MenuApi, OrdersApi } from './api'
import type { MenuTreeDTO, QuoteResponse, OrderSummaryDTO } from './api'
import AdminOrdersPage from './pages/AdminOrdersPage'

function App() {
  const [menu, setMenu] = useState<MenuTreeDTO | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [quote, setQuote] = useState<QuoteResponse | null>(null)
  const [created, setCreated] = useState<OrderSummaryDTO | null>(null)
  const [view, setView] = useState<'customer' | 'admin'>('customer')
  const [landing, setLanding] = useState<boolean>(true)

  // Load menu tree on mount
  useEffect(() => {
    setLoading(true)
    setError(null)
    MenuApi.getMenuTree(1)
      .then(setMenu)
      .catch((e: any) => setError(e?.body?.message || e.message || 'Failed to load menu'))
      .finally(() => setLoading(false))
  }, [])

  function quoteSample() {
    setLoading(true)
    setError(null)
    setQuote(null)
    setCreated(null)
    // Sample: minimal cart (item 1 qty 1), dev-friendly pickup (no time)
    OrdersApi.quote({
      customerName: 'Test',
      customerEmail: 'test@example.com',
      customerPhone: '302-555-0123',
      pickupSlotId: 1,
      pickupDay: 5,
      items: [{ menuItemId: 1, quantity: 1 }],
    })
      .then(setQuote)
      .catch((e: any) => setError(e?.body?.message || e.message || 'Quote failed'))
      .finally(() => setLoading(false))
  }

  function createSample() {
    setLoading(true)
    setError(null)
    setCreated(null)
    OrdersApi.create({
      customerName: 'Test',
      customerEmail: 'test@example.com',
      customerPhone: '302-555-0123',
      pickupSlotId: 1,
      pickupDay: 5,
      items: [{ menuItemId: 1, quantity: 1 }],
    })
      .then(setCreated)
      .catch((e: any) => setError(e?.body?.message || e.message || 'Create failed'))
      .finally(() => setLoading(false))
  }

  // Landing page with a clear CTA to view meals
  if (landing) {
    return (
      <div className="container">
        <h1>Mom&apos;s Kitchen</h1>
        <div className="hero">
          <p>Homestyle Ghanaian meals — fresh for pickup.</p>
          <button onClick={() => setLanding(false)}>View Meals</button>
        </div>
      </div>
    )
  }

  return (
    <div className="container">
      <h1>Mom&apos;s Kitchen</h1>
      <div style={{ marginBottom: '1rem' }}>
        <button onClick={() => setView('customer')} disabled={view==='customer'}>Customer</button>{' '}
        <button onClick={() => setView('admin')} disabled={view==='admin'}>Admin</button>
      </div>
      {loading && <div className="notice">Loading…</div>}
      {error && <div className="error">{error}</div>}

      {view === 'customer' && (
      <section>
        <h2>Menu</h2>
        {!menu && !loading && <div>No menu loaded</div>}
        {menu && (
          <div className="menu">
            {menu.categories.map((cat) => (
              <div key={cat.id} className="category">
                <h3>{cat.name}</h3>
                <ul>
                  {cat.items.map((it) => (
                    <li key={it.id}>
                      <strong>{it.name}</strong> — ${it.price.toFixed(2)}
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}
      </section>
      )}

      {view === 'customer' && (
      <section>
        <h2>Quick Quote (Sample)</h2>
        <button onClick={quoteSample} disabled={loading}>
          Quote item #1 (qty 1)
        </button>
        {' '}
        <button onClick={createSample} disabled={loading}>
          Create sample order
        </button>
        {quote && (
          <div className="quote">
            <div>Subtotal: ${quote.subtotal.toFixed(2)}</div>
            <div>Tax: ${quote.tax.toFixed(2)}</div>
            <div>Total: ${quote.total.toFixed(2)}</div>
          </div>
        )}
        {created && (
          <div className="quote">
            <div><strong>Order created</strong></div>
            <div>Order Code: {created.orderCode}</div>
            <div>Total: ${created.totalAmount.toFixed(2)}</div>
          </div>
        )}
      </section>
      )}

      {view === 'admin' && (
        <AdminOrdersPage />
      )}
    </div>
  )
}

export default App
