<script setup lang="ts">
import type { ApiError, Course, Page, Subject } from '~/types/models'

const api = useApi()
const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id as string)
const isNew = computed(() => id.value === 'new')

const form = reactive({ name: '', courseId: null as string | null, description: '' })
const courses = ref<Course[]>([])
const error = ref<ApiError | null>(null)
const saving = ref(false)

const courseOptions = computed(() => courses.value.map((c) => ({ title: c.name, value: c.id })))

onMounted(async () => {
  try {
    courses.value = (await api.get<Page<Course>>('/api/courses', { size: 200 })).content
    if (!isNew.value) {
      const s = await api.get<Subject>(`/api/subjects/${id.value}`)
      Object.assign(form, { name: s.name, courseId: s.courseId, description: s.description ?? '' })
    }
  } catch (e) {
    error.value = e as ApiError
  }
})

async function save() {
  saving.value = true
  error.value = null
  const body = { name: form.name, courseId: form.courseId, description: form.description || null }
  try {
    if (isNew.value) await api.post('/api/subjects', body)
    else await api.put(`/api/subjects/${id.value}`, body)
    router.push('/admin/subjects')
  } catch (e) {
    error.value = e as ApiError
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div style="max-width: 560px">
    <PageHeader :title="isNew ? 'New subject' : 'Edit subject'" back="/admin/subjects" />
    <ErrorAlert :error="error" />

    <v-card class="pa-6">
      <v-form @submit.prevent="save">
        <v-text-field v-model="form.name" label="Name *" autofocus />
        <v-select v-model="form.courseId" :items="courseOptions" label="Course *" />
        <v-text-field v-model="form.description" label="Description" />
        <div class="d-flex ga-2 mt-2">
          <v-btn color="primary" type="submit" :loading="saving">Save</v-btn>
          <v-btn variant="text" to="/admin/subjects">Cancel</v-btn>
        </div>
      </v-form>
    </v-card>
  </div>
</template>
