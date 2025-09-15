import { get, put, adminHeaders } from './base';
import type { OrderListItemDTO, OrderSummaryDTO, Page } from './types';

type ListParams = {
  status?: string;
  paymentStatus?: string;
  page?: number;
  size?: number;
  sort?: string; // e.g., 'createdAt,desc'
};

export function listOrders(params: ListParams = {}): Promise<Page<OrderListItemDTO>> {
  const q = new URLSearchParams();
  if (params.status) q.set('status', params.status);
  if (params.paymentStatus) q.set('paymentStatus', params.paymentStatus);
  if (params.page != null) q.set('page', String(params.page));
  if (params.size != null) q.set('size', String(params.size));
  if (params.sort) q.set('sort', params.sort);
  const hdrs = adminHeaders();
  return get<Page<OrderListItemDTO>>(`/api/admin/orders?${q.toString()}`, hdrs);
}

export function getOrder(id: number): Promise<OrderSummaryDTO> {
  return get<OrderSummaryDTO>(`/api/admin/orders/${id}`, adminHeaders());
}

export function updateStatus(id: number, newStatus: string): Promise<OrderListItemDTO> {
  return put<OrderListItemDTO>(`/api/admin/orders/${id}/status/${encodeURIComponent(newStatus)}`, undefined, adminHeaders());
}

export function updatePayment(id: number, newPayment: string): Promise<OrderListItemDTO> {
  return put<OrderListItemDTO>(`/api/admin/orders/${id}/payment/${encodeURIComponent(newPayment)}`, undefined, adminHeaders());
}

