export type ClassStatus = 'OPEN' | 'CLOSED'
export type EnrollmentStatus = 'PENDING' | 'PROCESSING' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED'

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface Student {
  id: string
  name: string
  email: string
  document?: string
  /** Whether this student is tied to a Keycloak login (self-service enabled). */
  linked: boolean
}

export interface Course {
  id: string
  name: string
  description?: string
}

export interface Subject {
  id: string
  name: string
  courseId: string
  description?: string
}

export interface SchoolClass {
  id: string
  subjectId: string
  label: string
  seatLimit: number
  seatsUsed: number
  status: ClassStatus
}

export interface Enrollment {
  id: string
  studentId: string
  classId: string
  status: EnrollmentStatus
  createdAt: string
  updatedAt: string
}

/** The standardized error envelope from the backend: { error: { type, code, message, details } }. */
export interface ApiError {
  type: string
  code: string
  message: string
  details: string[]
}
