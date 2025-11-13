// Base fetch helpers using promise chains (no async/await)

const API: string = (import.meta as any).env.VITE_API_URL as string;

export type HeadersMap = Record<string, string>;

function parseResponse<T>(res: Response): Promise<T> {
  if (res.status === 204) {
    return Promise.resolve(undefined as unknown as T);
  }
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('application/json')) {
    return res.json() as Promise<T>;
  }
  return res.text().then((txt) => txt as unknown as T);
}

function buildError(res: Response): Promise<never> {
  return res.text().then((txt) => {
    let body: unknown = txt;
    try {
      body = txt ? JSON.parse(txt) : undefined;
    } catch {}
    const err: any = new Error(`HTTP ${res.status}`);
    err.status = res.status;
    err.body = body;
    throw err;
  });
}

function request<T>(path: string, init?: RequestInit): Promise<T> {
  return fetch(`${API}${path}`, init).then((res) => {
    if (!res.ok) return buildError(res);
    return parseResponse<T>(res);
  });
}

export function get<T>(path: string, headers: HeadersMap = {}): Promise<T> {
  return request<T>(path, { method: 'GET', headers });
}

export function post<T>(path: string, body?: unknown, headers: HeadersMap = {}): Promise<T> {
  return request<T>(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...headers },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
}

export function put<T>(path: string, body?: unknown, headers: HeadersMap = {}): Promise<T> {
  return request<T>(path, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...headers },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
}

export function del<T>(path: string, headers: HeadersMap = {}): Promise<T> {
  return request<T>(path, { method: 'DELETE', headers });
}

// Admin auth helpers: store/get admin key (password) from localStorage, fallback to env
export function getAdminKey(): string | undefined {
  try {
    const ls = typeof localStorage !== 'undefined' ? localStorage.getItem('adminKey') : null;
    if (ls && ls.trim()) return ls.trim();
  } catch {}
  const envKey = (import.meta as any).env.VITE_ADMIN_KEY as string | undefined;
  return envKey && String(envKey).trim() ? String(envKey).trim() : undefined;
}

export function setAdminKey(value: string) {
  try { if (typeof localStorage !== 'undefined') localStorage.setItem('adminKey', value || ''); } catch {}
}

export function clearAdminKey() {
  try { if (typeof localStorage !== 'undefined') localStorage.removeItem('adminKey'); } catch {}
}

export function isAdminAuthed(): boolean { return !!getAdminKey(); }

// Helper for admin API header
export function adminHeaders(): HeadersMap {
  const key = getAdminKey();
  return key ? { 'X-Admin-Key': key } : {};
}
