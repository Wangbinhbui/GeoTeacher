import { apiGet, apiMutate, apiUpload } from '../../lib/api'

export type DocumentStatus = 'READY' | 'ARCHIVED'

export type SourceDocument = {
  id: string
  title: string
  gradeLevel?: number
  status: DocumentStatus
  originalName: string
  mediaType: string
  sizeBytes: number
  sha256: string
  extractedCharCount: number
  chunkCount: number
  version: number
}

export type DocumentChunk = {
  id: string
  chunkIndex: number
  pageNumber?: number
  sourceLabel: string
  content: string
  charCount: number
}

export type DocumentDetail = {
  document: SourceDocument
  chunks: DocumentChunk[]
}

export const documentsQueryKey = ['documents'] as const

export function listDocuments() {
  return apiGet<SourceDocument[]>('/api/v1/documents')
}

export function getDocument(id: string) {
  return apiGet<DocumentDetail>(`/api/v1/documents/${id}`)
}

export function uploadDocument(file: File, title: string, gradeLevel?: number) {
  const form = new FormData()
  form.append('file', file)
  const query = new URLSearchParams()
  if (title.trim()) query.set('title', title.trim())
  if (gradeLevel) query.set('gradeLevel', String(gradeLevel))
  return apiUpload<DocumentDetail>(`/api/v1/documents?${query.toString()}`, form)
}

export function archiveDocument(document: SourceDocument) {
  return apiMutate<DocumentDetail>(`/api/v1/documents/${document.id}/archive`, 'POST', {
    version: document.version,
  })
}
