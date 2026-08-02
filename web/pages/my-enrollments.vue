<script setup lang="ts">
import type { ApiError, Enrollment, Page } from '~/types/models'

const api = useApi()
const catalog = useCatalog()
const router = useRouter()

const items = ref<Enrollment[]>([])
const error = ref<ApiError | null>(null)
const loading = ref(true)
let timer: ReturnType<typeof setInterval> | null = null

const canCancel = (s: string) => ['PENDING', 'PROCESSING', 'CONFIRMED'].includes(s)
const anyProcessing = computed(() => items.value.some((e) => e.status === 'PROCESSING'))

async function load(showSpinner = false) {
  if (showSpinner) loading.value = true
  try {
    const page = await api.get<Page<Enrollment>>('/api/enrollments', { size: 200 })
    items.value = page.content.filter((e) => e.status !== 'CANCELLED')
  } catch (e) {
    error.value = e as ApiError
  } finally {
    loading.value = false
  }
}

// Short-poll only while something is still finalizing, then stop.
function ensurePolling() {
  if (anyProcessing.value && !timer) {
    timer = setInterval(async () => {
      await load()
      if (!anyProcessing.value && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 2000)
  }
}

async function cancel(e: Enrollment) {
  error.value = null
  try {
    await api.post(`/api/enrollments/${e.id}/cancel`)
    await load()
  } catch (err) {
    error.value = err as ApiError
  }
}

watch(anyProcessing, ensurePolling)

onMounted(async () => {
  await catalog.load(true)
  await load(true)
  ensurePolling()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div style="max-width: 760px">
    <PageHeader title="My enrollments">
      <template #actions>
        <v-btn variant="text" prepend-icon="mdi-refresh" @click="load(true)">Refresh</v-btn>
      </template>
    </PageHeader>

    <ErrorAlert :error="error" />

    <v-alert v-if="anyProcessing" type="info" variant="tonal" density="comfortable" class="mb-4">
      Finalizing your request… this updates automatically.
    </v-alert>

    <v-card v-if="items.length || loading">
      <v-data-table
        :headers="[
          { title: 'Class', key: 'class', sortable: false },
          { title: 'Subject', key: 'subject', sortable: false },
          { title: 'Status', key: 'status' },
          { title: '', key: 'actions', sortable: false, align: 'end', width: 110 },
        ]"
        :items="items"
        :loading="loading"
        items-per-page="20"
      >
        <template #[`item.class`]="{ item }">{{ catalog.classLabel(item.classId) }}</template>
        <template #[`item.subject`]="{ item }">{{ catalog.subjectNameForClass(item.classId) }}</template>
        <template #[`item.status`]="{ item }"><StatusChip :status="item.status" /></template>
        <template #[`item.actions`]="{ item }">
          <v-btn v-if="canCancel(item.status)" variant="text" size="small" color="error" @click="cancel(item)">
            Cancel
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <v-card v-else class="pa-8 text-center text-medium-emphasis">You have no enrollments yet</v-card>

    <div class="mt-4">
      <v-btn variant="text" prepend-icon="mdi-arrow-left" @click="router.push('/browse')">Browse more</v-btn>
    </div>
  </div>
</template>
