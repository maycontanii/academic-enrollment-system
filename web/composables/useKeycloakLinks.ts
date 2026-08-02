/** Deep links into the Keycloak admin console — the app doesn't provision users, it points there. */
export function useKeycloakLinks() {
  const cfg = useRuntimeConfig().public
  const realmBase = `${cfg.keycloakUrl}/admin/master/console/#/${cfg.keycloakRealm}`
  return {
    users: `${realmBase}/users`,
    addUser: `${realmBase}/users/add-user`,
  }
}
