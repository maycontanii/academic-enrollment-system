<script setup lang="ts">
import type { ApiError, Course, Page, SchoolClass, Subject } from '~/types/models'

const api = useApi()
const cart = useCartStore()
const router = useRouter()

const courses = ref<Course[]>([])
const subjects = ref<Subject[]>([])
const openClasses = ref<SchoolClass[]>([])
const courseId = ref<string | null>(null)
const subjectId = ref<string | null>(null)
const error = ref<ApiError | null>(null)
const loading = ref(false)
const adding = ref<string | null>(null)

const courseOptions = computed(() => courses.value.map((c) => ({ title: c.name, value: c.id })))
const subjectOptions = computed(() =>
  subjects.value.filter((s) => s.courseId === courseId.value).map((s) => ({ title: s.name, value: s.id })),
)
const seatsFree = (c: SchoolClass) => c.seatLimit - c.seatsUsed

async function loadOpenClasses() {
  openClasses.value = []
  if (!subjectId.value) return
  loading.value = true
  error.value = null
  try {
    openClasses.value = (
      await api.get<Page<SchoolClass>>('/api/classes', { subjectId: subjectId.value, status: 'OPEN', size: 200 })
    ).content
  } catch (e) {
    error.value = e as ApiError
  } finally {
    loading.value = false
  }
}

async function add(c: SchoolClass) {
  adding.value = c.id
  error.value = null
  try {
    await cart.add(c.id)
    await loadOpenClasses() // reflect the new seat usage
  } catch (e) {
    error.value = e as ApiError
  } finally {
    adding.value = null
  }
}

watch(courseId, () => {
  subjectId.value = null
  openClasses.value = []
})
watch(subjectId, loadOpenClasses)

onMounted(async () => {
  try {
    const [c, s] = await Promise.all([
      api.get<Page<Course>>('/api/courses', { size: 200 }),
      api.get<Page<Subject>>('/api/subjects', { size: 500 }),
    ])
    courses.value = c.content
    subjects.value = s.content
    await cart.refresh()
  } catch (e) {
    error.value = e as ApiError
  }
})
</script>

<template>
  <div>
    <PageHeader title="Browse classes" />
    <ErrorAlert :error="error" />

    <div class="d-flex ga-3 mb-4 flex-wrap">
      <v-select v-model="courseId" :items="courseOptions" label="Course *" hide-details style="max-width: 300px" />
      <v-select
        v-model="subjectId"
        :items="subjectOptions"
        label="Subject *"
        hide-details
        :disabled="!courseId"
        style="max-width: 300px"
      />
    </div>

    <v-card v-if="subjectId">
      <v-list v-if="openClasses.length || loading">
        <v-list-item v-for="c in openClasses" :key="c.id">
          <template #title>{{ c.label }}</template>
          <template #subtitle>Seats free: {{ seatsFree(c) }}</template>
          <template #append>
            <v-btn
              v-if="seatsFree(c) > 0"
              color="primary"
              size="small"
              :loading="adding === c.id"
              @click="add(c)"
            >
              Add
            </v-btn>
            <v-chip v-else color="grey" size="small" label variant="tonal">Full</v-chip>
          </template>
        </v-list-item>
        <v-list-item v-if="loading">
          <v-progress-linear indeterminate color="primary" />
        </v-list-item>
      </v-list>
      <v-card-text v-else class="text-medium-emphasis">No open classes for this subject</v-card-text>
    </v-card>

    <div class="d-flex ga-2 mt-4">
      <v-btn variant="tonal" color="primary" prepend-icon="mdi-cart-outline" @click="router.push('/cart')">
        Go to cart
        <v-badge v-if="cart.count" :content="cart.count" color="primary" inline class="ml-1" />
      </v-btn>
      <v-btn variant="text" @click="router.push('/my-enrollments')">My enrollments</v-btn>
    </div>
  </div>
</template>
