import { apiGet, apiMutate } from '../../lib/api'

export type StudentStatus = 'ACTIVE' | 'ARCHIVED'
export type EnrollmentStatus = 'ACTIVE' | 'LEFT'

export type Student = {
  id: string
  studentCode: string
  fullName: string
  dateOfBirth?: string
  status: StudentStatus
  version: number
}

export type Enrollment = {
  id: string
  classGroupId: string
  classCode: string
  classDisplayName: string
  schoolYearCode: string
  studentId: string
  studentCode: string
  studentFullName: string
  enrolledAt: string
  leftAt?: string
  status: EnrollmentStatus
  version: number
}

export type StudentInput = Pick<Student, 'studentCode' | 'fullName'> & {
  dateOfBirth?: string
  version?: number
}

export const studentsQueryKey = ['students'] as const
export const enrollmentsQueryKey = ['class-enrollments'] as const

export function searchStudents(query = '') {
  return apiGet<Student[]>(`/api/v1/students?query=${encodeURIComponent(query)}`)
}

export function saveStudent(input: StudentInput, id?: string) {
  const body = { ...input, dateOfBirth: input.dateOfBirth || null }
  return id
    ? apiMutate<Student>(`/api/v1/students/${id}`, 'PUT', body)
    : apiMutate<Student>('/api/v1/students', 'POST', body)
}

export function archiveStudent(student: Student) {
  return apiMutate<Student>(`/api/v1/students/${student.id}/archive`, 'POST', {
    version: student.version,
  })
}

export function listClassEnrollments(classGroupId: string) {
  return apiGet<Enrollment[]>(
    `/api/v1/class-enrollments?classGroupId=${encodeURIComponent(classGroupId)}`,
  )
}

export function enrollStudent(classGroupId: string, studentId: string, enrolledAt: string) {
  return apiMutate<Enrollment>('/api/v1/class-enrollments', 'POST', {
    classGroupId,
    studentId,
    enrolledAt,
  })
}

export function leaveEnrollment(enrollment: Enrollment, leftAt: string) {
  return apiMutate<Enrollment>(`/api/v1/class-enrollments/${enrollment.id}/leave`, 'POST', {
    leftAt,
    version: enrollment.version,
  })
}
