<script setup lang="ts">
import type { ApiError, Course, Page, Subject } from '~/types/models'

const api = useApi()
const items = ref<Course[]>([])
const subjectCount = ref<Record<string, number>>({})
const loading = ref(true)
const error = ref<ApiError | null>(null)
const search = ref('')

const headers = [
  { title: 'Name', key: 'name' },
  { title: 'Description', key: 'description' },
  { title: 'Subjects', key: 'subjects', sortable: false, width: 100 },
  { title: '', key: 'actions', sortable: false, align: 'end' as const, width: 110 },
]

async function load() {
  loading.value = true
  error.value = null
  try {
    const [c, s] = await Promise.all([
      api.get<Page<Course>>('/api/courses', { size: 200 }),
      api.get<Page<Subject>>('/api/subjects', { size: 500 }),
    ])
    items.value = c.content
    subjectCount.value = s.content.reduce((acc, subj) => {
      acc[subj.courseId] = (acc[subj.courseId] ?? 0) + 1
      return acc
    }, {} as Record<string, number>)
  } catch (e) {
    error.value = e as ApiError
  } finally {
    loading.value = false
  }
}

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return items.value
  return items.value.filter((c) => c.name.toLowerCase().includes(q))
})

async function remove(c: Course) {
  if (!confirm(`Delete ${c.name}?`)) return
  try {
    await api.del(`/api/courses/${c.id}`)
    await load()
  } catch (e) {
    error.value = e as ApiError
  }
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="Courses" back="/admin">
      <template #actions>
        <v-btn color="primary" prepend-icon="mdi-plus" to="/admin/courses/new">New course</v-btn>
      </template>
    </PageHeader>

    <ErrorAlert :error="error" />

    <v-text-field
      v-model="search"
      placeholder="Search"
      prepend-inner-icon="mdi-magnify"
      hide-details
      clearable
      class="mb-3"
      style="max-width: 360px"
    />

    <v-card>
      <v-data-table :headers="headers" :items="filtered" :loading="loading" items-per-page="10" no-data-text="No courses yet">
        <template #[`item.description`]="{ item }">{{ item.description || '—' }}</template>
        <template #[`item.subjects`]="{ item }">{{ subjectCount[item.id] ?? 0 }}</template>
        <template #[`item.actions`]="{ item }">
          <v-btn icon="mdi-pencil" variant="text" size="small" :to="`/admin/courses/${item.id}`" />
          <v-btn icon="mdi-delete" variant="text" size="small" color="error" @click="remove(item)" />
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>
