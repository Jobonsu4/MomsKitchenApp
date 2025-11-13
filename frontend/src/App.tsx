import { useState } from 'react'
import AdminOrdersPage from './pages/AdminOrdersPage'
import AdminLoginPage from './pages/AdminLoginPage'
import MenuPage from './pages/MenuPage'
import CheckoutPage from './pages/CheckoutPage'
import LookupPage from './pages/LookupPage'
import { CartProvider, useCart } from './context/CartContext'
import './App.css'

import { isAdminAuthed, clearAdminKey } from './api/base'

function App() {
  const [landing, setLanding] = useState(true)
  const [view, setView] = useState<'menu' | 'checkout' | 'lookup' | 'admin'>('menu')
  const [adminAuthed, setAdminAuthed] = useState<boolean>(isAdminAuthed())
  const landingBg = (import.meta as any).env.VITE_LANDING_BG as string | undefined

  if (landing) {
    return (
      <div className="landing-bg" style={landingBg ? { backgroundImage: `url(${landingBg})` } : { backgroundImage: 'url(/img/jollof.jpg)' }}>
        <div className="container landing">
          <h1>Mom&apos;s Kitchen</h1>
          <div className="hero">
            <p>Homestyle Ghanaian meals — fresh for pickup.</p>
            <button onClick={() => { setLanding(false); setView('menu') }}>View Menu</button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <CartProvider>
      <div className="container">
        <h1>Mom&apos;s Kitchen</h1>
        <Tabs value={view} onChange={setView} />

        {view === 'menu' && <MenuPage onViewCheckout={() => setView('checkout')} />}
        {view === 'checkout' && <CheckoutPage />}
        {view === 'lookup' && <LookupPage />}
        {view === 'admin' && (adminAuthed
          ? <AdminOrdersPage onLogout={() => { clearAdminKey(); setAdminAuthed(false); }} />
          : <AdminLoginPage onSuccess={() => setAdminAuthed(true)} />
        )}
      </div>
    </CartProvider>
  )
}

export default App

function Tabs({ value, onChange }: { value: 'menu'|'checkout'|'lookup'|'admin'; onChange: (v:any)=>void }) {
  const { count } = useCart()
  const btn = (v: typeof value, label: string) => (
    <button onClick={() => onChange(v)} disabled={value===v} style={{ marginRight: 12 }}>{label}</button>
  )
  return (
    <nav style={{ marginBottom: '1rem' }}>
      {btn('menu','Menu')}
      {btn('checkout',`Checkout (${count})`)}
      {btn('lookup','Lookup')}
      {btn('admin','Admin')}
    </nav>
  )
}
