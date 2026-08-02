import type { Course, Page, SchoolClass, Subject } from '~/types/models'

/** Caches catalog lookups so enrollment/class rows can show class labels and subject names by id. */
export function useCatalog() {
  const courses = useState<Record<string, Course>>('cat.courses', () => ({}))
  const subjects = useState<Record<string, Subject>>('cat.subjects', () => ({}))
  const classes = useState<Record<string, SchoolClass>>('cat.classes', () => ({}))

  async function load(force = false) {
    if (!force && Object.keys(classes.value).length) return
    const api = useApi()
    const [c, s, cl] = await Promise.all([
      api.get<Page<Course>>('/api/courses', { size: 200 }),
      api.get<Page<Subject>>('/api/subjects', { size: 500 }),
      api.get<Page<SchoolClass>>('/api/classes', { size: 500 }),
    ])
    courses.value = byId(c.content)
    subjects.value = byId(s.content)
    classes.value = byId(cl.content)
  }

  const classLabel = (classId: string) => classes.value[classId]?.label ?? '—'
  const subjectName = (subjectId: string) => subjects.value[subjectId]?.name ?? '—'
  const subjectNameForClass = (classId: string) => {
    const subjectId = classes.value[classId]?.subjectId
    return subjectId ? subjectName(subjectId) : '—'
  }

  return { courses, subjects, classes, load, classLabel, subjectName, subjectNameForClass }
}

function byId<T extends { id: string }>(items: T[]): Record<string, T> {
  return Object.fromEntries(items.map((i) => [i.id, i]))
}
