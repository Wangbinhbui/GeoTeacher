export type Session = {
  authenticated: boolean
  username?: string | null
  displayName?: string | null
}

type CsrfToken = {
  token: string
  headerName: string
  parameterName: string
}

type ApiErrorBody = {
  code?: string
  message?: string
  fieldErrors?: Record<string, string>
}

export class ApiError extends Error {
  readonly status: number
  readonly code?: string
  readonly fieldErrors: Record<string, string>

  constructor(status: number, body?: ApiErrorBody) {
    super(body?.message ?? 'Không thể kết nối đến máy chủ.')
    this.name = 'ApiError'
    this.status = status
    this.code = body?.code
    this.fieldErrors = body?.fieldErrors ?? {}
  }
}

async function parseError(response: Response): Promise<never> {
  let body: ApiErrorBody | undefined
  try {
    body = (await response.json()) as ApiErrorBody
  } catch {
    body = undefined
  }
  throw new ApiError(response.status, body)
}

export async function getSession(): Promise<Session> {
  const response = await fetch('/api/v1/auth/session', { credentials: 'include' })
  if (!response.ok) return parseError(response)
  return response.json() as Promise<Session>
}

async function getCsrfToken(): Promise<CsrfToken> {
  const response = await fetch('/api/v1/auth/csrf', { credentials: 'include' })
  if (!response.ok) return parseError(response)
  return response.json() as Promise<CsrfToken>
}

export async function apiUpload<T>(path: string, formData: FormData): Promise<T> {
  const csrf = await getCsrfToken()
  const response = await fetch(path, {
    method: 'POST',
    credentials: 'include',
    headers: { [csrf.headerName]: csrf.token },
    body: formData,
  })
  if (!response.ok) return parseError(response)
  return response.json() as Promise<T>
}

export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(path, { credentials: 'include' })
  if (!response.ok) return parseError(response)
  return response.json() as Promise<T>
}

export async function apiMutate<T>(path: string, method: string, body?: unknown): Promise<T> {
  const csrf = await getCsrfToken()
  const response = await fetch(path, {
    method,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  if (!response.ok) return parseError(response)
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export function login(username: string, password: string) {
  return apiMutate<Session>('/api/v1/auth/login', 'POST', { username, password })
}

export function logout() {
  return apiMutate<void>('/api/v1/auth/logout', 'POST')
}
