import type Keycloak from 'keycloak-js'

/** Reads roles/identity from the Keycloak token and exposes sign-out. */
export function useAuth() {
  const keycloak = useNuxtApp().$keycloak as Keycloak

  const roles = computed<string[]>(() => keycloak?.tokenParsed?.realm_access?.roles ?? [])
  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const isStudent = computed(() => roles.value.includes('STUDENT'))
  const username = computed<string>(() => keycloak?.tokenParsed?.preferred_username ?? '')
  const role = computed(() => (isAdmin.value ? 'Administrator' : isStudent.value ? 'Student' : 'User'))

  function logout() {
    keycloak.logout({ redirectUri: window.location.origin })
  }

  return { roles, isAdmin, isStudent, username, role, logout }
}
