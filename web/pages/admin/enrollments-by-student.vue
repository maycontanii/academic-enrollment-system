<script setup lang="ts">
import type { ApiError, Enrollment, Page, SchoolClass, Student, Subject } from '~/types/models'

const api = useApi()
const students = ref<Student[]>([])
const classes = ref<SchoolClass[]>([])
const subjects = ref<Subject[]>([])
const selectedStudentId = ref<string | null>(null)
const enrollments = ref<Enrollment[]>([])
const error = ref<ApiError | null>(null)
const loading = ref(false)

const headers = [
  { title: 'Class', key: 'class', sortable: false },
  { title: 'Subject', key: 'subject', sortable: false },
  { title: 'Status', key: 'status' },
  { title: 'Date', key: 'date', sortable: false },
  { title: '', key: 'actions', sortable: false, align: 'end' as const, width: 110 },
]

const studentOptions = computed(() => students.value.map((s) => ({ title: `${s.name} (${s.email})`, value: s.id })))
const classById = (id: string) => classes.value.find((c) => c.id === id)
const classLabel = (id: string) => classById(id)?.label ?? '—'
const subjectForClass = (id: string) => {
  const subjectId = classById(id)?.subjectId
  return subjects.value.find((s) => s.id === subjectId)?.name ?? '—'
}
const isActive = (s: string) => ['PENDING', 'PROCESSING', 'CONFIRMED'].includes(s)
const fmtDate = (iso: string) => new Date(iso).toLocaleDateString()

async function loadStudentEnrollments() {
  if (!selectedStudentId.value) return
  loading.value = true
  error.value = null
  try {
    enrollments.value = (
      await api.get<Page<Enrollment>>('/api/enrollments', { studentId: selectedStudentId.value, size: 200 })
    ).content
  } catch (e) {
    error.value = e as ApiError
  } finally {
    loading.value = false
  }
}

async function cancel(e: Enrollment) {
  error.value = null
  try {
    await api.post(`/api/enrollments/${e.id}/cancel`)
    await loadStudentEnrollments()
  } catch (err) {
    error.value = err as ApiError
  }
}

watch(selectedStudentId, loadStudentEnrollments)

onMounted(async () => {
  try {
    const [s, c, subj] = await Promise.all([
      api.get<Page<Student>>('/api/students', { size: 500 }),
      api.get<Page<SchoolClass>>('/api/classes', { size: 500 }),
      api.get<Page<Subject>>('/api/subjects', { size: 500 }),
    ])
    students.value = s.content
    classes.value = c.content
    subjects.value = subj.content
  } catch (e) {
    error.value = e as ApiError
  }
})
</script>

<template>
  <div>
    <PageHeader title="Enrollments by student" back="/admin" />
    <ErrorAlert :error="error" />

    <v-autocomplete
      v-model="selectedStudentId"
      :items="studentOptions"
      label="Student *"
      hide-details
      style="max-width: 360px"
      class="mb-4"
    />

    <v-card v-if="selectedStudentId">
      <v-data-table
        :headers="headers"
        :items="enrollments"
        :loading="loading"
        items-per-page="10"
        no-data-text="This student has no enrollments"
      >
        <template #[`item.class`]="{ item }">{{ classLabel(item.classId) }}</template>
        <template #[`item.subject`]="{ item }">{{ subjectForClass(item.classId) }}</template>
        <template #[`item.status`]="{ item }"><StatusChip :status="item.status" /></template>
        <template #[`item.date`]="{ item }">{{ fmtDate(item.createdAt) }}</template>
        <template #[`item.actions`]="{ item }">
          <v-btn v-if="isActive(item.status)" variant="text" size="small" color="error" @click="cancel(item)">
            Cancel
          </v-btn>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>
