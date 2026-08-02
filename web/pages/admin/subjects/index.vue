<script setup lang="ts">
import type { ApiError, Course, Page, Subject } from '~/types/models'

const api = useApi()
const subjects = ref<Subject[]>([])
const courses = ref<Course[]>([])
const loading = ref(true)
const error = ref<ApiError | null>(null)
const courseFilter = ref<string | null>(null)

const headers = [
  { title: 'Subject', key: 'name' },
  { title: 'Course', key: 'course', sortable: false },
  { title: '', key: 'actions', sortable: false, align: 'end' as const, width: 110 },
]

const courseName = (courseId: string) => courses.value.find((c) => c.id === courseId)?.name ?? '—'

const courseOptions = computed(() => [
  { title: 'All', value: null },
  ...courses.value.map((c) => ({ title: c.name, value: c.id })),
])

const filtered = computed(() =>
  courseFilter.value ? subjects.value.filter((s) => s.courseId === courseFilter.value) : subjects.value,
)

async function load() {
  loading.value = true
  error.value = null
  try {
    const [s, c] = await Promise.all([
      api.get<Page<Subject>>('/api/subjects', { size: 500 }),
      api.get<Page<Course>>('/api/courses', { size: 200 }),
    ])
    subjects.value = s.content
    courses.value = c.content
  } catch (e) {
    error.value = e as ApiError
  } finally {
    loading.value = false
  }
}

async function remove(s: Subject) {
  if (!confirm(`Delete ${s.name}?`)) return
  try {
    await api.del(`/api/subjects/${s.id}`)
    await load()
  } catch (e) {
    error.value = e as ApiError
  }
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="Subjects" back="/admin">
      <template #actions>
        <v-btn color="primary" prepend-icon="mdi-plus" to="/admin/subjects/new">New subject</v-btn>
      </template>
    </PageHeader>

    <ErrorAlert :error="error" />

    <v-select
      v-model="courseFilter"
      :items="courseOptions"
      label="Course"
      hide-details
      class="mb-3"
      style="max-width: 320px"
    />

    <v-card>
      <v-data-table :headers="headers" :items="filtered" :loading="loading" items-per-page="10" no-data-text="No subjects yet">
        <template #[`item.course`]="{ item }">{{ courseName(item.courseId) }}</template>
        <template #[`item.actions`]="{ item }">
          <v-btn icon="mdi-pencil" variant="text" size="small" :to="`/admin/subjects/${item.id}`" />
          <v-btn icon="mdi-delete" variant="text" size="small" color="error" @click="remove(item)" />
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>
