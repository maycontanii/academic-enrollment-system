<script setup lang="ts">
const props = defineProps<{ status: string }>()

const map: Record<string, { color: string; hint?: string }> = {
  OPEN: { color: 'success' },
  CLOSED: { color: 'grey' },
  PENDING: { color: 'grey' },
  PROCESSING: { color: 'info', hint: 'Finalizing…' },
  CONFIRMED: { color: 'success', hint: 'Seat secured' },
  REJECTED: { color: 'error', hint: 'No seats were available' },
  CANCELLED: { color: 'grey' },
}

const style = computed(() => map[props.status] ?? { color: 'grey' })
</script>

<template>
  <v-chip :color="style.color" size="small" label variant="tonal">
    {{ status }}
    <v-tooltip v-if="style.hint" activator="parent" location="top">{{ style.hint }}</v-tooltip>
  </v-chip>
</template>
