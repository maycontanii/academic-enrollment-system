<script setup lang="ts">
const groups = [
  {
    heading: 'Manage',
    items: [
      { label: 'Students', to: '/admin/students', icon: 'mdi-account-school' },
      { label: 'Courses', to: '/admin/courses', icon: 'mdi-book-open-variant' },
      { label: 'Subjects', to: '/admin/subjects', icon: 'mdi-bookshelf' },
      { label: 'Classes', to: '/admin/classes', icon: 'mdi-calendar-clock' },
    ],
  },
  {
    heading: 'Consult',
    items: [
      { label: 'Enrollments by class', to: '/admin/enrollments-by-class', icon: 'mdi-clipboard-list' },
      { label: 'Enrollments by student', to: '/admin/enrollments-by-student', icon: 'mdi-account-details' },
    ],
  },
  {
    heading: 'Access',
    items: [{ label: 'Users & access', to: '/admin/users', icon: 'mdi-shield-account' }],
  },
]
</script>

<template>
  <div>
    <PageHeader title="Administration" />
    <div v-for="g in groups" :key="g.heading" class="mb-6">
      <div class="text-overline text-medium-emphasis mb-2">{{ g.heading }}</div>
      <v-row>
        <v-col v-for="item in g.items" :key="item.to" cols="12" sm="6" md="4">
          <v-card :to="item.to" hover class="pa-4 d-flex align-center" height="88">
            <v-icon :icon="item.icon" size="28" color="primary" class="mr-3" />
            <span class="text-subtitle-1">{{ item.label }}</span>
          </v-card>
        </v-col>
      </v-row>
    </div>
  </div>
</template>
