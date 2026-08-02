import { defineStore } from 'pinia'
import type { Enrollment, Page } from '~/types/models'

/**
 * The cart is server-side truth: a student's PENDING enrollments. Adding creates a PENDING row;
 * removing cancels it. The badge count and cart page both read from here.
 */
export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [] as Enrollment[],
    loaded: false,
  }),
  getters: {
    count: (s) => s.items.length,
  },
  actions: {
    async refresh() {
      const page = await useApi().get<Page<Enrollment>>('/api/enrollments', { status: 'PENDING', size: 100 })
      this.items = page.content
      this.loaded = true
    },
    async add(classId: string) {
      const me = await useMe().load()
      await useApi().post('/api/enrollments', { studentId: me.id, classId })
      await this.refresh()
    },
    async remove(enrollmentId: string) {
      await useApi().post(`/api/enrollments/${enrollmentId}/cancel`)
      await this.refresh()
    },
  },
})
