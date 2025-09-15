import { get } from './base';
import type { MenuSummaryDTO, MenuTreeDTO, ItemDTO } from './types';

export function listMenus(): Promise<MenuSummaryDTO[]> {
  return get<MenuSummaryDTO[]>('/api/menu/menus');
}

export function getMenuTree(menuId: number): Promise<MenuTreeDTO> {
  return get<MenuTreeDTO>(`/api/menu/${menuId}/tree`);
}

export function getItemsByCategory(categoryId: number): Promise<ItemDTO[]> {
  return get<ItemDTO[]>(`/api/menu/categories/${categoryId}/items`);
}

