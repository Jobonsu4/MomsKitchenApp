import { useEffect, useState } from 'react'
import { MenuApi } from '../api'
import type { MenuTreeDTO } from '../api'
import { useCart } from '../context/CartContext'

type Props = { onViewCheckout?: () => void }

export default function MenuPage({ onViewCheckout }: Props) {
  const [menu, setMenu] = useState<MenuTreeDTO | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [category, setCategory] = useState<number | 'all'>('all')
  // Track selected add-ons per item id
  const [sel, setSel] = useState<Record<number, number[]>>({})
  const cart = useCart()

  useEffect(() => {
    setLoading(true)
    MenuApi.getMenuTree(1)
      .then(setMenu)
      .catch((e:any)=>setError(e?.body?.message||e.message))
      .finally(()=>setLoading(false))
  }, [])

  function add(menuItemId:number, name:string, addonIds?: number[]) {
    cart.add(menuItemId, name, addonIds)
  }

  function resolveImageSrc(u?: string | null): string {
    if (!u) return '/img/placeholder.svg'
    if (/^https?:\/\//i.test(u) || u.startsWith('/')) return u
    return `/img/${u}`
  }

  function toggleAddon(itemId: number, addonId: number) {
    setSel(prev => {
      const cur = prev[itemId] || []
      const exists = cur.includes(addonId)
      const next = exists ? cur.filter(id => id !== addonId) : [...cur, addonId]
      return { ...prev, [itemId]: next }
    })
  }

  function addSelected(item: MenuTreeDTO['categories'][number]['items'][number]) {
    const ids = sel[item.id] || []
    if (!ids.length) return
    const names = item.allowedAddons
      .filter(a => ids.includes(a.id))
      .map(a => a.name)
    const label = `${item.name} + ${names.join(' + ')}`
    add(item.id, label, ids)
    // clear selection for that item
    setSel(prev => ({ ...prev, [item.id]: [] }))
  }

  return (
    <div>
      {loading && <div className="notice">Loading…</div>}
      {error && <div className="error">{error}</div>}

      <section>
        <h2>Menu</h2>
        {menu && (
          <nav style={{ margin: '0.5rem 0 1rem', display: 'flex', flexWrap: 'wrap', gap: 8, justifyContent: 'center' }}>
            <button
              onClick={() => setCategory('all')}
              disabled={category==='all'}
              style={{ padding: '.35rem .75rem', borderRadius: 999, border: '1px solid rgba(127,127,127,.35)' }}
            >All</button>
            {menu.categories.map(c => (
              <button
                key={c.id}
                onClick={() => setCategory(c.id)}
                disabled={category===c.id}
                style={{ padding: '.35rem .75rem', borderRadius: 999, border: '1px solid rgba(127,127,127,.35)' }}
              >{c.name}</button>
            ))}
          </nav>
        )}
        {!menu && !loading && <div>No menu</div>}
        {menu && (
          <div className="menu">
            {(category==='all' ? menu.categories : menu.categories.filter(c => c.id === category)).map(cat=> (
              <div key={cat.id} className="category">
                <h3>{cat.name}</h3>
                <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
                  {cat.items.map(it=> (
                    <li key={it.id} className="menu-item" style={{ marginBottom: 12, display: 'grid', gridTemplateColumns: '100px 1fr', gap: 12, alignItems: 'start' }}>
                      <div className="thumb-wrap" style={{ width: 100, height: 100, overflow: 'hidden', borderRadius: 8, background: 'rgba(0,0,0,.06)' }}>
                        <img
                          src={resolveImageSrc(it.imageUrl)}
                          alt={it.name}
                          style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
                          onError={(e)=>{ (e.currentTarget as HTMLImageElement).src = '/img/placeholder.svg' }}
                        />
                      </div>
                      <div className="details">
                        <div className="title" style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                          <strong style={{ fontSize: '1.05rem' }}>{it.name}</strong>
                          <span>— ${it.price.toFixed(2)}</span>
                          <button onClick={()=>add(it.id,it.name)} style={{ marginLeft: 'auto' }}>Add</button>
                        </div>
                        {it.description && (
                          <div style={{ fontSize: '.9rem', opacity: .85, marginTop: 4 }}>{it.description}</div>
                        )}
                        {it.allowedAddons && it.allowedAddons.length > 0 && (
                          <div style={{ marginTop: 6 }}>
                            <div style={{ fontWeight: 600, fontSize: '.9rem', opacity: .95 }}>Add-ons</div>
                            <ul style={{ margin: '6px 0 0', paddingLeft: 0, listStyle: 'none' }}>
                              {it.allowedAddons.map(a => {
                                const checked = (sel[it.id] || []).includes(a.id)
                                return (
                                  <li key={a.id} style={{ fontSize: '.9rem', opacity: .95, display: 'flex', alignItems: 'flex-start', gap: 8, padding: '2px 0' }}>
                                    <input type="checkbox" checked={checked} onChange={()=>toggleAddon(it.id, a.id)} />
                                    <label style={{ textAlign: 'left', cursor: 'pointer' }} onClick={()=>toggleAddon(it.id, a.id)}>
                                      <span style={{ fontWeight: 500 }}>{a.name}</span>
                                      {a.priceDelta !== 0 && (
                                        <span> (+${a.priceDelta.toFixed(2)})</span>
                                      )}
                                      {a.description && (
                                        <span style={{ opacity: .8 }}> — {a.description}</span>
                                      )}
                                    </label>
                                  </li>
                                )
                              })}
                            </ul>
                            <div style={{ marginTop: 6 }}>
                              <button onClick={()=>addSelected(it)} disabled={!(sel[it.id]||[]).length}>
                                Add Selected ({(sel[it.id]||[]).length})
                              </button>{' '}
                              <button onClick={()=>setSel(p=>({ ...p, [it.id]: [] }))} disabled={!(sel[it.id]||[]).length}>Clear</button>
                            </div>
                          </div>
                        )}
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        )}
      </section>

      {onViewCheckout && cart.count > 0 && (
        <section>
          <button onClick={onViewCheckout}>View Cart ({cart.count})</button>
        </section>
      )}
    </div>
  )
}
