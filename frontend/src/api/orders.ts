import { get, post } from './base';
import type { CreateOrderRequest, OrderSummaryDTO, QuoteResponse } from './types';

// Normalize phone to digits only (matches backend behavior)
const digitsOnly = (v: string) => v ? v.replace(/\D/g, '') : v;

export function quote(body: CreateOrderRequest): Promise<QuoteResponse> {
  const payload: CreateOrderRequest = {
    ...body,
    customerPhone: digitsOnly(body.customerPhone),
  };
  return post<QuoteResponse>('/api/orders/quote', payload);
}

export function create(body: CreateOrderRequest): Promise<OrderSummaryDTO> {
  const payload: CreateOrderRequest = {
    ...body,
    customerPhone: digitsOnly(body.customerPhone),
  };
  return post<OrderSummaryDTO>('/api/orders', payload);
}

export function lookup(orderCode: string, phone: string): Promise<OrderSummaryDTO> {
  const q = new URLSearchParams({ phone: digitsOnly(phone) }).toString();
  return get<OrderSummaryDTO>(`/api/orders/${encodeURIComponent(orderCode)}?${q}`);
}

