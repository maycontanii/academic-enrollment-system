<script setup lang="ts">
import type { ApiError, Course } from '~/types/models'

const api = useApi()
const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id as string)
const isNew = computed(() => id.value === 'new')

const form = reactive({ name: '', description: '' })
const error = ref<ApiError | null>(null)
const saving = ref(false)

onMounted(async () => {
  if (!isNew.value) {
    try {
      const c = await api.get<Course>(`/api/courses/${id.value}`)
      Object.assign(form, { name: c.name, description: c.description ?? '' })
    } catch (e) {
      error.value = e as ApiError
    }
  }
})

async function save() {
  saving.value = true
  error.value = null
  const body = { name: form.name, description: form.description || null }
  try {
    if (isNew.value) await api.post('/api/courses', body)
    else await api.put(`/api/courses/${id.value}`, body)
    router.push('/admin/courses')
  } catch (e) {
    error.value = e as ApiError
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div style="max-width: 560px">
    <PageHeader :title="isNew ? 'New course' : 'Edit course'" back="/admin/courses" />
    <ErrorAlert :error="error" />

    <v-card class="pa-6">
      <v-form @submit.prevent="save">
        <v-text-field v-model="form.name" label="Name *" autofocus />
        <v-text-field v-model="form.description" label="Description" />
        <div class="d-flex ga-2 mt-2">
          <v-btn color="primary" type="submit" :loading="saving">Save</v-btn>
          <v-btn variant="text" to="/admin/courses">Cancel</v-btn>
        </div>
      </v-form>
    </v-card>
  </div>
</template>
