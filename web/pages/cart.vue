<script setup lang="ts">
import type { ApiError, Enrollment } from '~/types/models'

const cart = useCartStore()
const catalog = useCatalog()
const router = useRouter()
const error = ref<ApiError | null>(null)
const removing = ref<string | null>(null)

const isFull = (classId: string) => {
  const c = catalog.classes.value[classId]
  return c ? c.seatsUsed >= c.seatLimit : false
}

async function remove(e: Enrollment) {
  removing.value = e.id
  error.value = null
  try {
    await cart.remove(e.id)
  } catch (err) {
    error.value = err as ApiError
  } finally {
    removing.value = null
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
  <div style="max-width: 760px">
    <PageHeader title="Cart — PENDING" back="/browse" />
    <ErrorAlert :error="error" />

    <v-card v-if="cart.items.length">
      <v-table>
        <thead>
          <tr>
            <th>Class</th>
            <th>Subject</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in cart.items" :key="e.id">
            <td>
              {{ catalog.classLabel(e.classId) }}
              <div v-if="isFull(e.classId)" class="text-caption text-warning">now full — may fail at checkout</div>
            </td>
            <td>{{ catalog.subjectNameForClass(e.classId) }}</td>
            <td><StatusChip :status="e.status" /></td>
            <td class="text-right">
              <v-btn variant="text" size="small" color="error" :loading="removing === e.id" @click="remove(e)">
                Remove
              </v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>
    </v-card>

    <v-card v-else class="pa-8 text-center text-medium-emphasis">Your cart is empty</v-card>

    <div class="d-flex ga-2 mt-4">
      <v-btn variant="text" @click="router.push('/browse')">Keep browsing</v-btn>
      <v-spacer />
      <v-btn color="primary" :disabled="!cart.items.length" @click="router.push('/checkout')">Checkout</v-btn>
    </div>
  </div>
</template>
