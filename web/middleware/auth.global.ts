/**
 * The Keycloak plugin already guarantees an authenticated user (login-required). This middleware
 * only routes by role: it lands "/" on the right home and keeps students out of /admin.
 */
export default defineNuxtRouteMiddleware((to) => {
  if (import.meta.server) return

  const { isAdmin, isStudent } = useAuth()

  if (to.path === '/') {
    return navigateTo(isAdmin.value ? '/admin' : '/browse')
  }

  if (to.path.startsWith('/admin') && !isAdmin.value) {
    return navigateTo('/browse')
  }

  const studentArea = ['/browse', '/cart', '/checkout', '/my-enrollments']
  if (studentArea.includes(to.path) && !isStudent.value && !isAdmin.value) {
    return navigateTo('/')
  }
})
