import { apiGet, apiMutate } from '../../lib/api'

export type SchoolYearStatus = 'PLANNED' | 'ACTIVE' | 'ARCHIVED'
export type ClassGroupStatus = 'ACTIVE' | 'ARCHIVED'

export type SchoolYear = {
  id: string
  code: string
  startDate: string
  endDate: string
  status: SchoolYearStatus
  version: number
}

export type ClassGroup = {
  id: string
  schoolYearId: string
  schoolYearCode: string
  classCode: string
  displayName: string
  gradeLevel: number
  status: ClassGroupStatus
  version: number
}

export type SchoolYearInput = Pick<SchoolYear, 'code' | 'startDate' | 'endDate'> & {
  status: Exclude<SchoolYearStatus, 'ARCHIVED'>
  version?: number
}

export type ClassGroupInput = Pick<ClassGroup, 'schoolYearId' | 'classCode' | 'displayName' | 'gradeLevel'> & {
  version?: number
}

export const schoolYearsQueryKey = ['school-years'] as const
export const classGroupsQueryKey = ['class-groups'] as const

export function listSchoolYears() {
  return apiGet<SchoolYear[]>('/api/v1/school-years')
}

export function saveSchoolYear(input: SchoolYearInput, id?: string) {
  return id
    ? apiMutate<SchoolYear>(`/api/v1/school-years/${id}`, 'PUT', input)
    : apiMutate<SchoolYear>('/api/v1/school-years', 'POST', input)
}

export function archiveSchoolYear(schoolYear: SchoolYear) {
  return apiMutate<SchoolYear>(`/api/v1/school-years/${schoolYear.id}/archive`, 'POST', {
    version: schoolYear.version,
  })
}

export function listClassGroups(schoolYearId?: string) {
  const query = schoolYearId ? `?schoolYearId=${encodeURIComponent(schoolYearId)}` : ''
  return apiGet<ClassGroup[]>(`/api/v1/class-groups${query}`)
}

export function saveClassGroup(input: ClassGroupInput, id?: string) {
  return id
    ? apiMutate<ClassGroup>(`/api/v1/class-groups/${id}`, 'PUT', input)
    : apiMutate<ClassGroup>('/api/v1/class-groups', 'POST', input)
}

export function archiveClassGroup(classGroup: ClassGroup) {
  return apiMutate<ClassGroup>(`/api/v1/class-groups/${classGroup.id}/archive`, 'POST', {
    version: classGroup.version,
  })
}
