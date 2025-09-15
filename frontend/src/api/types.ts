// Shared API types matching the backend DTOs

export type MenuSummaryDTO = {
  id: number;
  name: string;
  description: string | null;
  active: boolean;
};

export type AddonDTO = {
  id: number;
  name: string;
  description: string | null;
  priceDelta: number;
  active: boolean;
};

export type ItemDTO = {
  id: number;
  categoryId: number | null;
  name: string;
  description: string | null;
  price: number;
  available: boolean;
  imageUrl: string | null;
  displayOrder: number;
  allowedAddons: AddonDTO[];
};

export type CategoryDTO = {
  id: number;
  name: string;
  description: string | null;
  displayOrder: number;
  active: boolean;
  items: ItemDTO[];
};

export type MenuTreeDTO = {
  id: number;
  name: string;
  description: string | null;
  active: boolean;
  categories: CategoryDTO[];
};

export type OrderListItemDTO = {
  orderId: number;
  customerName: string;
  customerPhone: string;
  pickupTime: string; // ISO string from backend
  pickupStatus: string;
  paymentStatus: string;
  total: number;
  createdAt: string; // ISO
  orderCode: string;
};

export type OrderSummaryItemAddon = {
  addonName: string;
  priceDelta: number;
};

export type OrderSummaryItem = {
  itemName: string;
  unitPrice: number;
  quantity: number;
  lineSubtotal: number;
  addons: OrderSummaryItemAddon[];
};

export type OrderSummaryDTO = {
  id: number;
  orderCode: string;
  status: string;
  paymentStatus: string;
  pickupAt: string; // ISO
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  notes: string | null;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  items: OrderSummaryItem[];
};

export type CartAddon = { addonId: number };
export type CartItem = { menuItemId: number; quantity: number; addons?: CartAddon[] };

export type CreateOrderRequest = {
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  pickupSlotId?: number;
  pickupDay?: number; // 0..6
  pickupTime?: string; // ISO
  paymentMethod?: string;
  paymentStatus?: string;
  items: CartItem[];
};

export type QuoteResponse = {
  items?: unknown[] | null;
  subtotal: number;
  tax: number;
  fees?: number | null;
  discount?: number | null;
  total: number;
  message?: string | null;
};

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // current page index
};

