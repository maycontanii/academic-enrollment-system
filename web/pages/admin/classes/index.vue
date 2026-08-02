<script setup lang="ts">
import type { ApiError, Page, SchoolClass, Subject } from '~/types/models'

const api = useApi()
const classes = ref<SchoolClass[]>([])
const subjects = ref<Subject[]>([])
const loading = ref(true)
const error = ref<ApiError | null>(null)
const subjectFilter = ref<string | null>(null)
const statusFilter = ref<string | null>(null)

const headers = [
  { title: 'Class', key: 'label' },
  { title: 'Subject', key: 'subject', sortable: false },
  { title: 'Seats', key: 'seats', sortable: false, width: 100 },
  { title: 'Status', key: 'status', width: 110 },
  { title: '', key: 'actions', sortable: false, align: 'end' as const, width: 190 },
]

const subjectName = (subjectId: string) => subjects.value.find((s) => s.id === subjectId)?.name ?? '—'

const subjectOptions = computed(() => [
  { title: 'All', value: null },
  ...subjects.value.map((s) => ({ title: s.name, value: s.id })),
])
const statusOptions = [
  { title: 'All', value: null },
  { title: 'Open', value: 'OPEN' },
  { title: 'Closed', value: 'CLOSED' },
]

async function load() {
  loading.value = true
  error.value = null
  try {
    if (!subjects.value.length) {
      subjects.value = (await api.get<Page<Subject>>('/api/subjects', { size: 500 })).content
    }
    const query: Record<string, unknown> = { size: 500 }
    if (subjectFilter.value) query.subjectId = subjectFilter.value
    if (statusFilter.value) query.status = statusFilter.value
    classes.value = (await api.get<Page<SchoolClass>>('/api/classes', query)).content
  } catch (e) {
    error.value = e as ApiError
  } finally {
    loading.value = false
  }
}

async function toggle(c: SchoolClass) {
  const action = c.status === 'OPEN' ? 'close' : 'open'
  try {
    await api.post(`/api/classes/${c.id}/${action}`)
    await load()
  } catch (e) {
    error.value = e as ApiError
  }
}

async function remove(c: SchoolClass) {
  if (!confirm(`Delete ${c.label}?`)) return
  try {
    await api.del(`/api/classes/${c.id}`)
    await load()
  } catch (e) {
    error.value = e as ApiError
  }
}

watch([subjectFilter, statusFilter], load)
onMounted(load)
</script>

<template>
  <div>
    <PageHeader title="Classes" back="/admin">
      <template #actions>
        <v-btn color="primary" prepend-icon="mdi-plus" to="/admin/classes/new">New class</v-btn>
      </template>
    </PageHeader>

    <ErrorAlert :error="error" />

    <div class="d-flex ga-3 mb-3 flex-wrap">
      <v-select v-model="subjectFilter" :items="subjectOptions" label="Subject" hide-details style="max-width: 280px" />
      <v-select v-model="statusFilter" :items="statusOptions" label="Status" hide-details style="max-width: 180px" />
    </div>

    <v-card>
      <v-data-table :headers="headers" :items="classes" :loading="loading" items-per-page="10" no-data-text="No classes yet">
        <template #[`item.subject`]="{ item }">{{ subjectName(item.subjectId) }}</template>
        <template #[`item.seats`]="{ item }">{{ item.seatsUsed }}/{{ item.seatLimit }}</template>
        <template #[`item.status`]="{ item }"><StatusChip :status="item.status" /></template>
        <template #[`item.actions`]="{ item }">
          <v-btn
            :color="item.status === 'OPEN' ? 'grey' : 'success'"
            variant="text"
            size="small"
            @click="toggle(item)"
          >
            {{ item.status === 'OPEN' ? 'Close' : 'Open' }}
          </v-btn>
          <v-btn icon="mdi-pencil" variant="text" size="small" :to="`/admin/classes/${item.id}`" />
          <v-btn icon="mdi-delete" variant="text" size="small" color="error" @click="remove(item)" />
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>
