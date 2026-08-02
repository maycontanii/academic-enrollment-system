<script setup lang="ts">
import type { ApiError, Page, Student } from '~/types/models'

const api = useApi()
const items = ref<Student[]>([])
const loading = ref(true)
const error = ref<ApiError | null>(null)
const search = ref('')

const headers = [
  { title: 'Name', key: 'name' },
  { title: 'Email', key: 'email' },
  { title: 'Document', key: 'document' },
  { title: '', key: 'actions', sortable: false, align: 'end' as const, width: 110 },
]

async function load() {
  loading.value = true
  error.value = null
  try {
    const page = await api.get<Page<Student>>('/api/students', { size: 200 })
    items.value = page.content
  } catch (e) {
    error.value = e as ApiError
  } finally {
    loading.value = false
  }
}

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return items.value
  return items.value.filter((s) => s.name.toLowerCase().includes(q) || s.email.toLowerCase().includes(q))
})

async function remove(s: Student) {
  if (!confirm(`Delete ${s.name}?`)) return
  try {
    await api.del(`/api/students/${s.id}`)
    await load()
  } catch (e) {
    error.value = e as ApiError
  }
}

onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="Students" back="/admin">
      <template #actions>
        <v-btn color="primary" prepend-icon="mdi-plus" to="/admin/students/new">New student</v-btn>
      </template>
    </PageHeader>

    <ErrorAlert :error="error" />

    <v-text-field
      v-model="search"
      placeholder="Search name or email"
      prepend-inner-icon="mdi-magnify"
      hide-details
      clearable
      class="mb-3"
      style="max-width: 360px"
    />

    <v-card>
      <v-data-table :headers="headers" :items="filtered" :loading="loading" items-per-page="10" no-data-text="No students yet">
        <template #[`item.document`]="{ item }">{{ item.document || '—' }}</template>
        <template #[`item.actions`]="{ item }">
          <v-btn icon="mdi-pencil" variant="text" size="small" :to="`/admin/students/${item.id}`" />
          <v-btn icon="mdi-delete" variant="text" size="small" color="error" @click="remove(item)" />
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>
