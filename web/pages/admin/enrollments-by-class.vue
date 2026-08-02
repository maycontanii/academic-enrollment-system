<script setup lang="ts">
import type { ApiError, Enrollment, Page, SchoolClass, Student } from '~/types/models'

const api = useApi()
const classes = ref<SchoolClass[]>([])
const students = ref<Student[]>([])
const selectedClassId = ref<string | null>(null)
const enrollments = ref<Enrollment[]>([])
const enrollStudentId = ref<string | null>(null)
const error = ref<ApiError | null>(null)
const loading = ref(false)

const headers = [
  { title: 'Student', key: 'student', sortable: false },
  { title: 'Status', key: 'status' },
  { title: 'Date', key: 'date', sortable: false },
  { title: '', key: 'actions', sortable: false, align: 'end' as const, width: 110 },
]

const classOptions = computed(() => classes.value.map((c) => ({ title: c.label, value: c.id })))
const studentOptions = computed(() => students.value.map((s) => ({ title: `${s.name} (${s.email})`, value: s.id })))
const selectedClass = computed(() => classes.value.find((c) => c.id === selectedClassId.value) || null)

const studentName = (studentId: string) => students.value.find((s) => s.id === studentId)?.name ?? '—'
const isActive = (s: string) => ['PENDING', 'PROCESSING', 'CONFIRMED'].includes(s)
const fmtDate = (iso: string) => new Date(iso).toLocaleDateString()

async function loadClassEnrollments() {
  if (!selectedClassId.value) return
  loading.value = true
  error.value = null
  try {
    enrollments.value = (
      await api.get<Page<Enrollment>>('/api/enrollments', { classId: selectedClassId.value, size: 200 })
    ).content
  } catch (e) {
    error.value = e as ApiError
  } finally {
    loading.value = false
  }
}

async function enroll() {
  if (!enrollStudentId.value || !selectedClassId.value) return
  error.value = null
  try {
    await api.post('/api/enrollments', { studentId: enrollStudentId.value, classId: selectedClassId.value })
    enrollStudentId.value = null
    await refresh()
  } catch (e) {
    error.value = e as ApiError
  }
}

async function cancel(e: Enrollment) {
  error.value = null
  try {
    await api.post(`/api/enrollments/${e.id}/cancel`)
    await refresh()
  } catch (err) {
    error.value = err as ApiError
  }
}

async function refresh() {
  // Reload the class too, so the seat counters reflect confirms/cancels.
  classes.value = (await api.get<Page<SchoolClass>>('/api/classes', { size: 500 })).content
  await loadClassEnrollments()
}

watch(selectedClassId, loadClassEnrollments)

onMounted(async () => {
  try {
    const [c, s] = await Promise.all([
      api.get<Page<SchoolClass>>('/api/classes', { size: 500 }),
      api.get<Page<Student>>('/api/students', { size: 500 }),
    ])
    classes.value = c.content
    students.value = s.content
  } catch (e) {
    error.value = e as ApiError
  }
})
</script>

<template>
  <div>
    <PageHeader title="Enrollments by class" back="/admin" />
    <ErrorAlert :error="error" />

    <v-select v-model="selectedClassId" :items="classOptions" label="Class *" hide-details style="max-width: 320px" class="mb-4" />

    <template v-if="selectedClass">
      <div class="d-flex align-center mb-4 flex-wrap ga-4">
        <div class="text-body-2">
          Seats: <strong>{{ selectedClass.seatsUsed }}</strong> used ·
          <strong>{{ selectedClass.seatLimit - selectedClass.seatsUsed }}</strong> free ·
          limit {{ selectedClass.seatLimit }}
        </div>
        <v-spacer />
        <div class="d-flex ga-2" style="min-width: 340px">
          <v-autocomplete
            v-model="enrollStudentId"
            :items="studentOptions"
            label="Enroll a student"
            hide-details
            density="comfortable"
          />
          <v-btn color="primary" :disabled="!enrollStudentId" @click="enroll">Enroll</v-btn>
        </div>
      </div>

      <v-card>
        <v-data-table
          :headers="headers"
          :items="enrollments"
          :loading="loading"
          items-per-page="10"
          no-data-text="No enrollments for this class"
        >
          <template #[`item.student`]="{ item }">{{ studentName(item.studentId) }}</template>
          <template #[`item.status`]="{ item }"><StatusChip :status="item.status" /></template>
          <template #[`item.date`]="{ item }">{{ fmtDate(item.createdAt) }}</template>
          <template #[`item.actions`]="{ item }">
            <v-btn v-if="isActive(item.status)" variant="text" size="small" color="error" @click="cancel(item)">
              Cancel
            </v-btn>
          </template>
        </v-data-table>
      </v-card>
    </template>
  </div>
</template>
