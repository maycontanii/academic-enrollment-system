import type { Student } from '~/types/models'

/** Loads and caches the logged-in student's own profile (needed to create enrollments). */
export function useMe() {
  const me = useState<Student | null>('me', () => null)

  async function load(): Promise<Student> {
    if (!me.value) {
      me.value = await useApi().get<Student>('/api/students/me')
    }
    return me.value
  }

  return { me, load }
}
