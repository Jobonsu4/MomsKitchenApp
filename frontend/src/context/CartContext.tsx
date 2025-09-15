import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'

export type CartLine = { menuItemId: number; name: string; quantity: number; addonIds?: number[] }

type CartContextValue = {
  lines: CartLine[]
  add: (menuItemId: number, name: string, addonIds?: number[]) => void
  inc: (menuItemId: number, addonIds?: number[]) => void
  dec: (menuItemId: number, addonIds?: number[]) => void
  clear: () => void
  count: number
  toApiItems: () => { menuItemId: number; quantity: number; addons?: { addonId: number }[] }[]
}

const CartContext = createContext<CartContextValue | undefined>(undefined)

export function CartProvider({ children }: { children: ReactNode }) {
  const [lines, setLines] = useState<CartLine[]>([])

  // Load from localStorage on mount
  useEffect(() => {
    try {
      const raw = localStorage.getItem('cart:lines')
      if (raw) {
        const parsed = JSON.parse(raw)
        if (Array.isArray(parsed)) setLines(parsed as CartLine[])
      }
    } catch {}
  }, [])

  const sameAddons = (a?: number[], b?: number[]) => {
    if (!a && !b) return true
    if (!a || !b) return false
    if (a.length !== b.length) return false
    const as = [...a].sort((x,y)=>x-y)
    const bs = [...b].sort((x,y)=>x-y)
    return as.every((v,i)=>v===bs[i])
  }

  const add = (menuItemId: number, name: string, addonIds?: number[]) => {
    setLines(prev => {
      const i = prev.findIndex(x => x.menuItemId === menuItemId && sameAddons(x.addonIds, addonIds))
      if (i >= 0) {
        const n = [...prev]
        n[i] = { ...n[i], quantity: n[i].quantity + 1 }
        return n
      }
      return [...prev, { menuItemId, name, quantity: 1, addonIds: addonIds && addonIds.length ? addonIds : undefined }]
    })
  }

  const inc = (menuItemId: number, addonIds?: number[]) => {
    setLines(prev => prev.map(x => (x.menuItemId === menuItemId && sameAddons(x.addonIds, addonIds)) ? { ...x, quantity: x.quantity + 1 } : x))
  }

  const dec = (menuItemId: number, addonIds?: number[]) => {
    setLines(prev => prev
      .map(x => (x.menuItemId === menuItemId && sameAddons(x.addonIds, addonIds)) ? { ...x, quantity: x.quantity - 1 } : x)
      .filter(x => x.quantity > 0))
  }

  const clear = () => setLines([])

  const value = useMemo<CartContextValue>(() => ({
    lines,
    add,
    inc,
    dec,
    clear,
    count: lines.reduce((s, l) => s + l.quantity, 0),
    toApiItems: () => lines.map(l => ({
      menuItemId: l.menuItemId,
      quantity: l.quantity,
      addons: l.addonIds && l.addonIds.length ? l.addonIds.map(id => ({ addonId: id })) : undefined,
    })),
  }), [lines])

  // Persist to localStorage when lines change
  useEffect(() => {
    try { localStorage.setItem('cart:lines', JSON.stringify(lines)) } catch {}
  }, [lines])

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within CartProvider')
  return ctx
}
