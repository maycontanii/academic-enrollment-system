<script setup lang="ts">
import type { NuxtError } from '#app'

defineProps<{ error: NuxtError }>()
</script>

<template>
  <v-app>
    <v-main class="d-flex align-center justify-center bg-background" style="min-height: 100vh">
      <v-card class="pa-8 text-center" max-width="420">
        <v-icon size="48" color="secondary" class="mb-3">mdi-alert-circle-outline</v-icon>
        <div class="text-h6 mb-1">Something went wrong</div>
        <div class="text-body-2 text-medium-emphasis mb-4">{{ error?.message || 'Unexpected error' }}</div>
        <v-btn color="primary" @click="clearError({ redirect: '/' })">Back to start</v-btn>
      </v-card>
    </v-main>
  </v-app>
</template>
