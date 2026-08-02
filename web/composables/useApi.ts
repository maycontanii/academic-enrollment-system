import type Keycloak from 'keycloak-js'
import type { ApiError } from '~/types/models'

/**
 * Thin wrapper over $fetch that attaches the bearer token and normalizes backend errors into the
 * standardized envelope ({@link ApiError}), so callers can surface `message`/`code` uniformly.
 */
export function useApi() {
  const keycloak = useNuxtApp().$keycloak as Keycloak
  const baseURL = useRuntimeConfig().public.apiBase

  async function request<T>(path: string, opts: Record<string, unknown> = {}): Promise<T> {
    await keycloak.updateToken(30).catch(() => {})
    try {
      return await $fetch<T>(path, {
        baseURL,
        headers: { Authorization: `Bearer ${keycloak.token}` },
        ...opts,
      })
    } catch (e: unknown) {
      throw toApiError(e)
    }
  }

  return {
    get: <T>(path: string, query?: Record<string, unknown>) => request<T>(path, { method: 'GET', query }),
    post: <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body }),
    put: <T>(path: string, body?: unknown) => request<T>(path, { method: 'PUT', body }),
    del: (path: string) => request<void>(path, { method: 'DELETE' }),
  }
}

function toApiError(e: unknown): ApiError {
  const err = e as { data?: { error?: ApiError }; message?: string; status?: number }
  if (err?.data?.error) return err.data.error
  return {
    type: 'error',
    code: String(err?.status ?? 'error'),
    message: err?.message ?? 'Unexpected error',
    details: [],
  }
}
