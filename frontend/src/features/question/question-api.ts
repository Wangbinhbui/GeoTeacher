import { apiGet, apiMutate } from '../../lib/api'

export type QuestionType = 'SINGLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER' | 'ESSAY'
export type QuestionStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED'
export type CognitiveLevel = 'RECOGNITION' | 'COMPREHENSION' | 'APPLICATION' | 'HIGH_APPLICATION'

export type QuestionSummary = {
  id: string
  type: QuestionType
  gradeLevel: number
  topic: string
  cognitiveLevel: CognitiveLevel
  content: string
  suggestedScore?: string
  status: QuestionStatus
  version: number
}

export type QuestionOption = {
  id?: string
  key: string
  content: string
  correct: boolean
  position?: number
}

export type QuestionDetail = QuestionSummary & {
  explanation?: string
  options: QuestionOption[]
}

export type QuestionInput = Omit<QuestionDetail, 'id' | 'version'> & { version?: number }

export const questionsQueryKey = ['questions'] as const

export function searchQuestions(query = '') {
  return apiGet<QuestionSummary[]>(`/api/v1/questions?query=${encodeURIComponent(query)}`)
}

export function getQuestion(id: string) {
  return apiGet<QuestionDetail>(`/api/v1/questions/${id}`)
}

export function saveQuestion(input: QuestionInput, id?: string) {
  return id
    ? apiMutate<QuestionDetail>(`/api/v1/questions/${id}`, 'PUT', input)
    : apiMutate<QuestionDetail>('/api/v1/questions', 'POST', input)
}

export function archiveQuestion(question: QuestionSummary) {
  return apiMutate<QuestionDetail>(`/api/v1/questions/${question.id}/archive`, 'POST', {
    version: question.version,
  })
}
