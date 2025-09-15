import { useMemo, useState } from 'react';
import { OrdersApi } from '../api';
import { useCart } from '../context/CartContext';
import type { QuoteResponse, OrderSummaryDTO } from '../api';

export default function CheckoutPage() {
  const cart = useCart();
  const [name, setName] = useState('Test');
  const [email, setEmail] = useState('test@example.com');
  const [phone, setPhone] = useState('302-555-0123');
  const [pickupDay, setPickupDay] = useState<number>(5); // 0=Sun..6=Sat (default Fri)
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [quote, setQuote] = useState<QuoteResponse | null>(null);
  const [order, setOrder] = useState<OrderSummaryDTO | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<'CASH'|'CASHAPP'>('CASH');

  const cashTag = (import.meta as any).env.VITE_CASHAPP_TAG as string | undefined;
  const buildCashAppUrl = useMemo(() => {
    return (amount: number, note?: string) => {
      if (!cashTag) return undefined;
      const amt = amount.toFixed(2);
      // Common deep-link patterns used by Cash App
      // Try amount in path first; add note via query if desired
      const base = `https://cash.app/${cashTag.startsWith('$') ? cashTag : '$'+cashTag}`;
      const url = `${base}/${amt}`;
      if (note && note.length) return `${url}?note=${encodeURIComponent(note)}`;
      return url;
    };
  }, [cashTag]);

  function buildBody() {
    return {
      customerName: name,
      customerEmail: email,
      customerPhone: phone,
      // We only send pickupDay — backend validates against active slots
      pickupDay,
      paymentMethod,
      items: cart.toApiItems().length ? cart.toApiItems() : [{ menuItemId: 1, quantity: 1 }],
    };
  }

  function doQuote() {
    setLoading(true); setError(null); setOrder(null);
    OrdersApi.quote(buildBody())
      .then(setQuote)
      .catch((e: any) => setError(e?.body?.message || e.message || 'Quote failed'))
      .finally(() => setLoading(false));
  }

  function doCreate() {
    setLoading(true); setError(null);
    OrdersApi.create(buildBody())
      .then(o => { setOrder(o); cart.clear(); })
      .catch((e: any) => setError(e?.body?.message || e.message || 'Create failed'))
      .finally(() => setLoading(false));
  }

  function doClear() {
    cart.clear();
    setQuote(null);
    setOrder(null);
  }

  return (
    <div>
      <h2>Checkout</h2>
      {loading && <div className="notice">Loading…</div>}
      {error && <div className="error">{error}</div>}

      <div style={{ display: 'grid', gap: 8, maxWidth: 420 }}>
        <input placeholder="Name" value={name} onChange={e => setName(e.target.value)} />
        <input placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
        <input placeholder="Phone" value={phone} onChange={e => setPhone(e.target.value)} />
        <div>
          <label style={{ marginRight: 8 }}>Pickup Day:</label>
          <label style={{ marginRight: 12 }}>
            <input type="radio" name="pickup-day" checked={pickupDay===5} onChange={()=>setPickupDay(5)} /> Fri (4–7 PM)
          </label>
          <label style={{ marginRight: 12 }}>
            <input type="radio" name="pickup-day" checked={pickupDay===6} onChange={()=>setPickupDay(6)} /> Sat (12–3 PM)
          </label>
          <label>
            <input type="radio" name="pickup-day" checked={pickupDay===0} onChange={()=>setPickupDay(0)} /> Sun (12–3 PM)
          </label>
        </div>
        <div>
          <label style={{ marginRight: 8 }}>Payment:</label>
          <label style={{ marginRight: 12 }}>
            <input type="radio" name="pay" checked={paymentMethod==='CASH'} onChange={()=>setPaymentMethod('CASH')} /> Cash
          </label>
          <label>
            <input type="radio" name="pay" checked={paymentMethod==='CASHAPP'} onChange={()=>setPaymentMethod('CASHAPP')} /> Cash App
          </label>
        </div>
      </div>
      <div style={{ marginTop: 8 }}>
        <strong>Pickup day:</strong> {['Sun','Mon','Tue','Wed','Thu','Fri','Sat'][pickupDay]} • Slots as listed above
      </div>
      <section style={{ marginTop: 8 }}>
        <strong>Cart</strong>
        {cart.lines.length === 0 ? (
          <div className="notice" style={{ marginTop: 6 }}>Your cart is empty.</div>
        ) : (
          <ul style={{ listStyle: 'none', padding: 0, marginTop: 6 }}>
            {cart.lines.map((l, idx) => (
              <li key={idx} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '6px 0', borderTop: '1px solid rgba(127,127,127,.2)' }}>
                <div style={{ flex: 1, textAlign: 'left' }}>{l.name}</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <button onClick={() => cart.dec(l.menuItemId, l.addonIds)}>-</button>
                  <span>{l.quantity}</span>
                  <button onClick={() => cart.inc(l.menuItemId, l.addonIds)}>+</button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
      <div style={{ marginTop: 12, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <button onClick={doQuote} disabled={loading || cart.lines.length===0}>Quote</button>
        <button onClick={doCreate} disabled={loading || cart.lines.length===0}>Create Order</button>
        <button onClick={doClear} disabled={loading || cart.lines.length===0} style={{ marginLeft: 'auto' }}>Clear Cart</button>
      </div>

      {quote && (
        <div className="quote">
          <div>Subtotal: ${quote.subtotal.toFixed(2)}</div>
          <div>Tax: ${quote.tax.toFixed(2)}</div>
          <div>Total: ${quote.total.toFixed(2)}</div>
          {paymentMethod==='CASHAPP' && cashTag && (
            <div style={{ marginTop: 8 }}>
              <a href={buildCashAppUrl(quote.total, 'Order Quote')} target="_blank" rel="noreferrer">Pay with Cash App (Quote)</a>
            </div>
          )}
          {paymentMethod==='CASHAPP' && !cashTag && (
            <div className="notice" style={{ marginTop: 8 }}>Set VITE_CASHAPP_TAG in frontend/.env (e.g., $momsKitchen) to show a Cash App link.</div>
          )}
        </div>
      )}

      {order && (
        <div className="quote">
          <div><strong>Order created</strong></div>
          <div>Code: {order.orderCode}</div>
          <div>Total: ${order.totalAmount.toFixed(2)}</div>
          {paymentMethod==='CASHAPP' && cashTag && (
            <div style={{ marginTop: 8 }}>
              <a href={buildCashAppUrl(order.totalAmount, `Order ${order.orderCode}`)} target="_blank" rel="noreferrer">Pay with Cash App</a>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
