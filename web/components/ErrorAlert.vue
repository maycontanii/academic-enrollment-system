<script setup lang="ts">
import type { ApiError } from '~/types/models'

defineProps<{ error: ApiError | null }>()
</script>

<template>
  <v-alert v-if="error" type="error" variant="tonal" density="comfortable" class="mb-4" :text="error.message">
    <template v-if="error.details?.length" #append>
      <ul class="text-caption">
        <li v-for="(d, i) in error.details" :key="i">{{ d }}</li>
      </ul>
    </template>
  </v-alert>
</template>
