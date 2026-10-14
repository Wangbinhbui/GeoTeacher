import { apiGet, apiMutate } from '../../lib/api'

export type AiExamType = 'FIFTEEN_MINUTE' | 'MIDTERM' | 'FINAL' | 'FULL_YEAR' | 'GRADUATION_EXAM'

export type AiQuestion = {
  type: 'SINGLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER' | 'ESSAY'
  content: string
  options: string[]
  answer: string
  explanation: string
  score: number
  sourceChunkIds: string[]
}

export type AiExamDraft = {
  id: string
  examType: AiExamType
  durationMinutes: number
  gradeLevel: number
  title: string
  teacherInstructions?: string
  status: 'DRAFT'
  provider: string
  model: string
  createdAt: string
  knowledgeDocumentIds: string[]
  sampleDocumentId?: string
  content: {
    title: string
    instructions: string
    sections: { title: string; description: string; questions: AiQuestion[] }[]
  }
}

export type AiStatus = { configured: boolean; provider: string; model: string; privacyNotice: string }

export type GenerateAiExamInput = {
  examType: AiExamType
  durationMinutes: 15 | 45 | 60
  gradeLevel: number
  title: string
  teacherInstructions?: string
  knowledgeDocumentIds: string[]
  sampleDocumentId?: string
  externalProcessingConsent: boolean
}

export const aiExamDraftsQueryKey = ['ai-exam-drafts'] as const
export const aiStatusQueryKey = ['ai-exam-status'] as const
export function getAiStatus() { return apiGet<AiStatus>('/api/v1/ai/exam-drafts/status') }
export function listAiExamDrafts() { return apiGet<AiExamDraft[]>('/api/v1/ai/exam-drafts') }
export function generateAiExam(input: GenerateAiExamInput) {
  return apiMutate<AiExamDraft>('/api/v1/ai/exam-drafts/generate', 'POST', input)
}
