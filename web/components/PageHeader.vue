<script setup lang="ts">
defineProps<{ title: string; back?: string }>()
const router = useRouter()
</script>

<template>
  <div class="d-flex align-center mb-4">
    <v-btn v-if="back" icon="mdi-arrow-left" variant="text" density="comfortable" class="mr-2" @click="router.push(back)" />
    <h1 class="text-h5 font-weight-medium">{{ title }}</h1>
    <v-spacer />
    <slot name="actions" />
  </div>
</template>
