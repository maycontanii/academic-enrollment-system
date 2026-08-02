<script setup lang="ts">
import type { ApiError, Student } from '~/types/models'

const api = useApi()
const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id as string)
const isNew = computed(() => id.value === 'new')

const form = reactive({ name: '', email: '', document: '' })
const error = ref<ApiError | null>(null)
const saving = ref(false)

onMounted(async () => {
  if (!isNew.value) {
    try {
      const s = await api.get<Student>(`/api/students/${id.value}`)
      Object.assign(form, { name: s.name, email: s.email, document: s.document ?? '' })
    } catch (e) {
      error.value = e as ApiError
    }
  }
})

async function save() {
  saving.value = true
  error.value = null
  const body = { name: form.name, email: form.email, document: form.document || null }
  try {
    if (isNew.value) await api.post('/api/students', body)
    else await api.put(`/api/students/${id.value}`, body)
    router.push('/admin/students')
  } catch (e) {
    error.value = e as ApiError
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div style="max-width: 560px">
    <PageHeader :title="isNew ? 'New student' : 'Edit student'" back="/admin/students" />
    <ErrorAlert :error="error" />

    <v-card class="pa-6">
      <v-form @submit.prevent="save">
        <v-text-field v-model="form.name" label="Name *" autofocus />
        <v-text-field v-model="form.email" label="Email *" type="email" />
        <v-text-field v-model="form.document" label="Document" />
        <div class="d-flex ga-2 mt-2">
          <v-btn color="primary" type="submit" :loading="saving">Save</v-btn>
          <v-btn variant="text" to="/admin/students">Cancel</v-btn>
        </div>
      </v-form>
    </v-card>
  </div>
</template>
