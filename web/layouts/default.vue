<script setup lang="ts">
const { username, role, isAdmin, isStudent, logout } = useAuth()
const cart = useCartStore()
const router = useRouter()

// Keep the cart badge live for students.
onMounted(() => {
  if (isStudent.value && !isAdmin.value) cart.refresh().catch(() => {})
})

const home = computed(() => (isAdmin.value ? '/admin' : '/browse'))
</script>

<template>
  <v-app>
    <v-app-bar flat border color="surface">
      <v-app-bar-title>
        <NuxtLink :to="home" class="text-decoration-none text-high-emphasis font-weight-medium">
          Academic Enrollment
        </NuxtLink>
      </v-app-bar-title>

      <template #append>
        <v-btn
          v-if="isStudent && !isAdmin"
          variant="text"
          class="mr-2"
          prepend-icon="mdi-cart-outline"
          @click="router.push('/cart')"
        >
          Cart
          <v-badge v-if="cart.count" :content="cart.count" color="primary" inline class="ml-1" />
        </v-btn>

        <v-menu>
          <template #activator="{ props }">
            <v-btn variant="text" append-icon="mdi-chevron-down" v-bind="props">
              {{ username || 'account' }}
            </v-btn>
          </template>
          <v-list density="compact" min-width="200">
            <v-list-item :subtitle="role" :title="username" />
            <v-divider />
            <v-list-item prepend-icon="mdi-logout" title="Sign out" @click="logout" />
          </v-list>
        </v-menu>
      </template>
    </v-app-bar>

    <v-main class="bg-background">
      <v-container class="py-6" style="max-width: 1100px">
        <slot />
      </v-container>
    </v-main>
  </v-app>
</template>
