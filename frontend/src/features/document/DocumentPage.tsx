import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Archive, ArrowLeft, BookOpenCheck, FileText, LoaderCircle, ScanText, Upload } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../../components/ui/button'
import { Card } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { ApiError, type Session } from '../../lib/api'
import {
  archiveDocument,
  documentsQueryKey,
  getDocument,
  listDocuments,
  uploadDocument,
} from './document-api'

function errorMessage(error: unknown) {
  return error instanceof ApiError ? error.message : 'Không thể xử lý tài liệu. Vui lòng thử lại.'
}

function formatBytes(value: number) {
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

export function DocumentPage({ session }: { session: Session }) {
  const queryClient = useQueryClient()
  const [file, setFile] = useState<File>()
  const [title, setTitle] = useState('')
  const [gradeLevel, setGradeLevel] = useState<number>()
  const [selectedId, setSelectedId] = useState<string>()

  const documentsQuery = useQuery({ queryKey: documentsQueryKey, queryFn: listDocuments })
  const detailQuery = useQuery({
    queryKey: [...documentsQueryKey, selectedId, 'detail'],
    queryFn: () => getDocument(selectedId!),
    enabled: Boolean(selectedId),
  })
  const uploadMutation = useMutation({
    mutationFn: () => {
      if (!file) throw new Error('Chưa chọn tài liệu.')
      return uploadDocument(file, title, gradeLevel)
    },
    onSuccess: (detail) => {
      queryClient.invalidateQueries({ queryKey: documentsQueryKey })
      setSelectedId(detail.document.id)
      setFile(undefined)
      setTitle('')
      setGradeLevel(undefined)
    },
  })
  const archiveMutation = useMutation({
    mutationFn: archiveDocument,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: documentsQueryKey })
      setSelectedId(undefined)
    },
  })

  return <main className="min-h-screen bg-[#f5f7f5] text-stone-900">
    <header className="border-b border-stone-200 bg-white">
      <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-5 sm:px-8">
        <div className="flex items-center gap-4"><Button asChild variant="ghost" size="icon"><Link to="/" aria-label="Về tổng quan"><ArrowLeft className="size-5" /></Link></Button><div><p className="font-semibold">Giáo án & tài liệu</p><p className="text-xs text-stone-400">{session.displayName}</p></div></div>
        <div className="rounded-full bg-sky-50 px-3 py-1 text-xs font-semibold text-sky-700">Document ingestion</div>
      </div>
    </header>

    <div className="mx-auto grid max-w-7xl gap-6 px-5 py-8 sm:px-8 xl:grid-cols-[390px_1fr]">
      <section className="space-y-6">
        <Card className="p-5">
          <div className="flex items-center gap-3"><Upload className="size-5 text-emerald-700" /><h1 className="font-semibold">Tải tài liệu nguồn</h1></div>
          <p className="mt-2 text-xs leading-5 text-stone-500">PDF/DOCX có thể chọn được chữ, tối đa 20 MB. File scan chưa có OCR.</p>
          <div className="mt-5 space-y-4">
            <label className="block text-sm font-medium">Tiêu đề<Input className="mt-2" value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Để trống để dùng tên file" maxLength={255} /></label>
            <label className="block text-sm font-medium">Khối lớp<Input className="mt-2" type="number" min="1" max="12" value={gradeLevel ?? ''} onChange={(event) => setGradeLevel(event.target.value ? Number(event.target.value) : undefined)} placeholder="Không bắt buộc" /></label>
            <label className="block text-sm font-medium">Tệp tài liệu<Input className="mt-2" type="file" accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document" onChange={(event) => setFile(event.target.files?.[0])} /></label>
            {file && <p className="rounded-xl bg-stone-50 p-3 text-xs text-stone-600">{file.name} · {formatBytes(file.size)}</p>}
            {uploadMutation.error && <p className="text-sm text-red-600">{errorMessage(uploadMutation.error)}</p>}
            <Button className="w-full" disabled={!file || uploadMutation.isPending} onClick={() => uploadMutation.mutate()}>{uploadMutation.isPending ? <LoaderCircle className="size-4 animate-spin" /> : <ScanText className="size-4" />} Tải lên và đọc nội dung</Button>
          </div>
        </Card>

        <Card className="border-amber-200 bg-amber-50/70 p-5">
          <h2 className="font-semibold text-amber-950">Bước AI kế tiếp</h2>
          <p className="mt-2 text-sm leading-6 text-amber-900/70">Các đoạn nguồn đã sẵn sàng cho retrieval và citation. Chức năng tạo câu hỏi 15/45/60 phút chưa gọi model cho tới khi chốt provider và quyền riêng tư.</p>
          <Button asChild variant="outline" size="sm" className="mt-4"><Link to="/questions"><BookOpenCheck className="size-4" /> Mở ngân hàng câu hỏi</Link></Button>
        </Card>
      </section>

      <section className="space-y-6">
        <Card className="overflow-hidden">
          <div className="border-b border-stone-100 p-5"><h2 className="font-semibold">Tài liệu đã tải</h2><p className="mt-1 text-xs text-stone-500">Checksum giúp phát hiện tải trùng; binary không được phục vụ công khai.</p></div>
          <div className="divide-y divide-stone-100">
            {documentsQuery.isFetching && <p className="p-5 text-sm text-stone-500">Đang tải tài liệu…</p>}
            {documentsQuery.data?.map((document) => <div key={document.id} className={`flex items-center gap-4 p-5 ${selectedId === document.id ? 'bg-sky-50' : ''}`}>
              <button type="button" className="min-w-0 flex-1 text-left" onClick={() => setSelectedId(document.id)}><div className="flex items-center gap-2"><FileText className="size-4 shrink-0 text-sky-700" /><p className="truncate font-semibold">{document.title}</p></div><p className="mt-1 truncate text-xs text-stone-400">{document.originalName} · {formatBytes(document.sizeBytes)} · {document.chunkCount} đoạn{document.gradeLevel ? ` · khối ${document.gradeLevel}` : ''}</p></button>
              {document.status === 'READY' && <Button variant="ghost" size="icon" aria-label="Lưu trữ tài liệu" onClick={() => window.confirm('Lưu trữ tài liệu này? File vẫn được giữ để bảo toàn nguồn.') && archiveMutation.mutate(document)}><Archive className="size-4" /></Button>}
            </div>)}
            {!documentsQuery.isFetching && !documentsQuery.data?.length && <p className="p-10 text-center text-sm text-stone-500">Chưa có tài liệu nguồn.</p>}
          </div>
        </Card>

        {selectedId && <Card className="overflow-hidden">
          <div className="border-b border-stone-100 p-5"><h2 className="font-semibold">Nội dung đã trích xuất</h2><p className="mt-1 text-xs text-stone-500">Mỗi đoạn giữ nhãn trang/nguồn để gắn citation khi AI tạo câu hỏi.</p></div>
          <div className="max-h-[620px] divide-y divide-stone-100 overflow-auto">
            {detailQuery.isFetching && <p className="p-5 text-sm text-stone-500">Đang đọc các đoạn nguồn…</p>}
            {detailQuery.data?.chunks.map((chunk) => <article key={chunk.id} className="p-5"><div className="flex justify-between gap-3 text-xs font-semibold text-sky-700"><span>Đoạn {chunk.chunkIndex + 1} · {chunk.sourceLabel}</span><span className="text-stone-400">{chunk.charCount} ký tự</span></div><p className="mt-3 whitespace-pre-line text-sm leading-6 text-stone-700">{chunk.content}</p></article>)}
          </div>
        </Card>}
      </section>
    </div>
  </main>
}
