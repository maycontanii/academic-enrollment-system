<script setup lang="ts">
const consoleUrl = useKeycloakLinks().users
</script>

<template>
  <div style="max-width: 720px">
    <PageHeader title="Users & access" back="/admin" />

    <v-card class="pa-6">
      <div class="d-flex align-center mb-3">
        <v-icon color="primary" class="mr-2">mdi-shield-account</v-icon>
        <span class="text-subtitle-1">Access is managed in Keycloak</span>
      </div>
      <p class="text-body-2 text-medium-emphasis mb-4">
        User accounts and role assignments (Administrator / Student) live in the identity provider, not
        in this application. The backend only reads the roles carried in each request's token. Manage
        users, reset passwords, and assign roles from the Keycloak admin console.
      </p>

      <v-btn color="primary" :href="consoleUrl" target="_blank" prepend-icon="mdi-open-in-new">
        Open Keycloak console
      </v-btn>

      <v-divider class="my-5" />

      <div class="text-overline text-medium-emphasis mb-2">Seeded demo users</div>
      <v-table density="comfortable">
        <thead>
          <tr><th>User</th><th>Role</th></tr>
        </thead>
        <tbody>
          <tr><td>admin</td><td>Administrator</td></tr>
          <tr><td>ana</td><td>Student</td></tr>
        </tbody>
      </v-table>
    </v-card>
  </div>
</template>
