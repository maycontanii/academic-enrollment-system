<script setup lang="ts">
import type { ApiError } from '~/types/models'

const cart = useCartStore()
const catalog = useCatalog()
const api = useApi()
const router = useRouter()

const error = ref<ApiError | null>(null)
const submitting = ref(false)
const done = ref(false)

async function confirm() {
  submitting.value = true
  error.value = null
  const results = await Promise.allSettled(
    cart.items.map((e) => api.post(`/api/enrollments/${e.id}/confirm`)),
  )
  submitting.value = false

  const firstError = results.find((r) => r.status === 'rejected') as PromiseRejectedResult | undefined
  if (firstError) {
    error.value = firstError.reason as ApiError
  }
  // Whatever was accepted is now PROCESSING; the outcomes show on My enrollments.
  done.value = true
  await cart.refresh()
  if (!firstError) {
    setTimeout(() => router.push('/my-enrollments'), 800)
  }
}

onMounted(async () => {
  try {
    await Promise.all([cart.refresh(), catalog.load(true)])
  } catch (e) {
    error.value = e as ApiError
  }
})
</script>

<template>
  <div style="max-width: 640px">
    <PageHeader title="Review & Confirm" back="/cart" />
    <ErrorAlert :error="error" />

    <v-card class="pa-6">
      <v-list v-if="cart.items.length" lines="two">
        <v-list-item v-for="e in cart.items" :key="e.id" :title="catalog.classLabel(e.classId)" :subtitle="catalog.subjectNameForClass(e.classId)">
          <template #prepend><v-icon color="primary">mdi-circle-small</v-icon></template>
        </v-list-item>
      </v-list>
      <div v-else class="text-medium-emphasis">Nothing to confirm.</div>

      <v-alert v-if="done && !error" type="success" variant="tonal" density="comfortable" class="mt-4">
        Request received — finalizing…
      </v-alert>

      <div class="d-flex ga-2 mt-4">
        <v-btn variant="text" to="/cart">Back to cart</v-btn>
        <v-spacer />
        <v-btn v-if="error" variant="tonal" color="primary" to="/my-enrollments">Go to my enrollments</v-btn>
        <v-btn
          v-else
          color="primary"
          :loading="submitting"
          :disabled="!cart.items.length || done"
          @click="confirm"
        >
          Confirm
        </v-btn>
      </div>
    </v-card>
  </div>
</template>
