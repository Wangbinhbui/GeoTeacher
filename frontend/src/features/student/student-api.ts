import { apiGet, apiMutate, apiUpload } from '../../lib/api'

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

export type StudentImportRow = {
  rowNumber: number
  studentCode: string
  fullName: string
  dateOfBirth?: string
  action: 'CREATE_STUDENT_AND_ENROLL' | 'ENROLL_EXISTING' | 'INVALID'
  errors: string[]
}

export type StudentImportPreview = {
  valid: boolean
  totalRows: number
  newStudents: number
  existingStudents: number
  rows: StudentImportRow[]
}

export type StudentImportResult = {
  createdStudents: number
  enrolledStudents: number
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

function studentImportForm(file: File) {
  const form = new FormData()
  form.append('file', file)
  return form
}

export function previewStudentImport(classGroupId: string, file: File) {
  return apiUpload<StudentImportPreview>(
    `/api/v1/student-excel/preview?classGroupId=${encodeURIComponent(classGroupId)}`,
    studentImportForm(file),
  )
}

export function confirmStudentImport(classGroupId: string, enrolledAt: string, file: File) {
  const query = new URLSearchParams({ classGroupId, enrolledAt })
  return apiUpload<StudentImportResult>(
    `/api/v1/student-excel/confirm?${query.toString()}`,
    studentImportForm(file),
  )
}
