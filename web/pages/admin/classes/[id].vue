<script setup lang="ts">
import type { ApiError, Page, SchoolClass, Subject } from '~/types/models'

const api = useApi()
const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id as string)
const isNew = computed(() => id.value === 'new')

const form = reactive({ subjectId: null as string | null, label: '', seatLimit: 30 })
const subjects = ref<Subject[]>([])
const error = ref<ApiError | null>(null)
const saving = ref(false)

const subjectOptions = computed(() => subjects.value.map((s) => ({ title: s.name, value: s.id })))

onMounted(async () => {
  try {
    subjects.value = (await api.get<Page<Subject>>('/api/subjects', { size: 500 })).content
    if (!isNew.value) {
      const c = await api.get<SchoolClass>(`/api/classes/${id.value}`)
      Object.assign(form, { subjectId: c.subjectId, label: c.label, seatLimit: c.seatLimit })
    }
  } catch (e) {
    error.value = e as ApiError
  }
})

async function save() {
  saving.value = true
  error.value = null
  const body = { subjectId: form.subjectId, label: form.label, seatLimit: Number(form.seatLimit) }
  try {
    if (isNew.value) await api.post('/api/classes', body)
    else await api.put(`/api/classes/${id.value}`, body)
    router.push('/admin/classes')
  } catch (e) {
    error.value = e as ApiError
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div style="max-width: 560px">
    <PageHeader :title="isNew ? 'New class' : 'Edit class'" back="/admin/classes" />
    <ErrorAlert :error="error" />

    <v-card class="pa-6">
      <v-form @submit.prevent="save">
        <v-select v-model="form.subjectId" :items="subjectOptions" label="Subject *" />
        <v-text-field v-model="form.label" label="Label *" placeholder="2026.1 - A" />
        <v-text-field v-model.number="form.seatLimit" label="Seat limit *" type="number" min="1" />
        <p class="text-caption text-medium-emphasis mb-4">New classes start Closed — open them from the list when ready.</p>
        <div class="d-flex ga-2">
          <v-btn color="primary" type="submit" :loading="saving">Save</v-btn>
          <v-btn variant="text" to="/admin/classes">Cancel</v-btn>
        </div>
      </v-form>
    </v-card>
  </div>
</template>
