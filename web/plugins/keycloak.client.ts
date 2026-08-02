import Keycloak from 'keycloak-js'

/**
 * Client-side OIDC. `login-required` redirects any unauthenticated visitor to Keycloak (PKCE), so
 * by the time the app renders we always have a token. The instance is provided as `$keycloak`.
 */
export default defineNuxtPlugin(async (nuxtApp) => {
  const cfg = useRuntimeConfig().public

  const keycloak = new Keycloak({
    url: cfg.keycloakUrl,
    realm: cfg.keycloakRealm,
    clientId: cfg.keycloakClient,
  })

  try {
    await keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    })
  } catch (err) {
    // A failed init means the browser is mid-redirect to Keycloak, or Keycloak is unreachable.
    console.error('Keycloak initialization failed', err)
  }

  // Keep the access token fresh; if the refresh fails, bounce back through login.
  keycloak.onTokenExpired = () => {
    keycloak.updateToken(30).catch(() => keycloak.login())
  }

  return { provide: { keycloak } }
})
