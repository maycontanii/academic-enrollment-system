// SPA (ssr: false): a dashboard-style console where OIDC login is a client-side redirect to
// Keycloak (PKCE). No server runtime is needed — the app talks to academic-service directly.
export default defineNuxtConfig({
  ssr: false,
  devtools: { enabled: false },
  modules: ['vuetify-nuxt-module', '@pinia/nuxt'],
  css: ['@mdi/font/css/materialdesignicons.css'],

  runtimeConfig: {
    public: {
      // Overridable at runtime via NUXT_PUBLIC_* env vars (set in the Compose stack, T14).
      apiBase: 'http://localhost:8081',
      keycloakUrl: 'http://localhost:8080',
      keycloakRealm: 'academic-enrollment',
      keycloakClient: 'academic-web',
    },
  },

  vuetify: {
    vuetifyOptions: {
      // Grey-white Material palette, matching the approved UI skeleton/prototype.
      theme: {
        defaultTheme: 'light',
        themes: {
          light: {
            colors: {
              primary: '#455A64',
              secondary: '#607D8B',
              surface: '#FFFFFF',
              background: '#F5F5F5',
            },
          },
        },
      },
      defaults: {
        VTextField: { variant: 'outlined', density: 'comfortable' },
        VSelect: { variant: 'outlined', density: 'comfortable' },
        VCard: { rounded: 'lg' },
      },
    },
  },

  app: {
    head: {
      title: 'Academic Enrollment',
      meta: [{ name: 'viewport', content: 'width=device-width, initial-scale=1' }],
    },
  },
})
