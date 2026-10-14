import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, BookOpenCheck, FileCheck2, LoaderCircle, ShieldCheck, Sparkles } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../../components/ui/button'
import { Card } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { ApiError, type Session } from '../../lib/api'
import { documentsQueryKey, listDocuments } from '../document/document-api'
import { aiExamDraftsQueryKey, aiStatusQueryKey, generateAiExam, getAiStatus, listAiExamDrafts, type AiExamDraft, type AiExamType } from './ai-exam-api'

const examTypes: { value: AiExamType; label: string }[] = [
  { value: 'FIFTEEN_MINUTE', label: 'Kiểm tra 15 phút' },
  { value: 'MIDTERM', label: 'Kiểm tra giữa kỳ' },
  { value: 'FINAL', label: 'Kiểm tra cuối kỳ' },
  { value: 'FULL_YEAR', label: 'Tổng hợp cả năm' },
  { value: 'GRADUATION_EXAM', label: 'Ôn/thi tốt nghiệp THPT' },
]

const questionTypeLabel: Record<string, string> = { SINGLE_CHOICE: 'Trắc nghiệm', TRUE_FALSE: 'Đúng/Sai', SHORT_ANSWER: 'Trả lời ngắn', ESSAY: 'Tự luận' }
function errorMessage(error: unknown) { return error instanceof ApiError ? error.message : 'Không thể tạo đề. Vui lòng thử lại.' }

export function AiExamPage({ session }: { session: Session }) {
  const client = useQueryClient()
  const [examType, setExamType] = useState<AiExamType>('FIFTEEN_MINUTE')
  const [duration, setDuration] = useState<15 | 45 | 60>(15)
  const [grade, setGrade] = useState(12)
  const [title, setTitle] = useState('Đề kiểm tra Địa lý')
  const [instructions, setInstructions] = useState('')
  const [knowledgeIds, setKnowledgeIds] = useState<string[]>([])
  const [sampleId, setSampleId] = useState('')
  const [consent, setConsent] = useState(false)
  const [selectedDraft, setSelectedDraft] = useState<AiExamDraft>()

  const statusQuery = useQuery({ queryKey: aiStatusQueryKey, queryFn: getAiStatus })
  const documentsQuery = useQuery({ queryKey: documentsQueryKey, queryFn: listDocuments })
  const draftsQuery = useQuery({ queryKey: aiExamDraftsQueryKey, queryFn: listAiExamDrafts })
  const generation = useMutation({
    mutationFn: () => generateAiExam({ examType, durationMinutes: duration, gradeLevel: grade, title, teacherInstructions: instructions || undefined, knowledgeDocumentIds: knowledgeIds, sampleDocumentId: sampleId || undefined, externalProcessingConsent: consent }),
    onSuccess: (draft) => { setSelectedDraft(draft); client.invalidateQueries({ queryKey: aiExamDraftsQueryKey }) },
  })

  const knowledgeDocuments = documentsQuery.data?.filter((item) => item.status === 'READY' && item.documentKind === 'KNOWLEDGE') ?? []
  const samples = documentsQuery.data?.filter((item) => item.status === 'READY' && item.documentKind === 'SAMPLE_EXAM') ?? []
  const visibleDraft = selectedDraft ?? draftsQuery.data?.[0]
  const toggleKnowledge = (id: string) => setKnowledgeIds((current) => current.includes(id) ? current.filter((value) => value !== id) : [...current, id])

  return <main className="min-h-screen bg-[#f5f7f5] text-stone-900">
    <header className="border-b border-stone-200 bg-white"><div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-5 sm:px-8"><div className="flex items-center gap-4"><Button asChild variant="ghost" size="icon"><Link to="/" aria-label="Về tổng quan"><ArrowLeft className="size-5" /></Link></Button><div><p className="font-semibold">Tạo đề bằng AI</p><p className="text-xs text-stone-400">{session.displayName}</p></div></div><span className="rounded-full bg-violet-50 px-3 py-1 text-xs font-semibold text-violet-700">Bản nháp có nguồn</span></div></header>

    <div className="mx-auto grid max-w-7xl gap-6 px-5 py-8 sm:px-8 xl:grid-cols-[410px_1fr]">
      <section className="space-y-5">
        <Card className="p-5">
          <div className="flex items-center gap-3"><Sparkles className="size-5 text-violet-700" /><h1 className="font-semibold">Cấu hình đề</h1></div>
          <div className="mt-5 space-y-4">
            <label className="block text-sm font-medium">Loại kỳ kiểm tra<select className="mt-2 h-10 w-full rounded-md border border-stone-200 bg-white px-3 text-sm" value={examType} onChange={(event) => setExamType(event.target.value as AiExamType)}>{examTypes.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></label>
            <div className="grid grid-cols-2 gap-3"><label className="block text-sm font-medium">Thời lượng<select className="mt-2 h-10 w-full rounded-md border border-stone-200 bg-white px-3 text-sm" value={duration} onChange={(event) => setDuration(Number(event.target.value) as 15 | 45 | 60)}><option value={15}>15 phút</option><option value={45}>45 phút</option><option value={60}>60 phút</option></select></label><label className="block text-sm font-medium">Khối<Input className="mt-2" type="number" min={1} max={12} value={grade} onChange={(event) => setGrade(Number(event.target.value))} /></label></div>
            <label className="block text-sm font-medium">Tên đề<Input className="mt-2" value={title} maxLength={255} onChange={(event) => setTitle(event.target.value)} /></label>
            <label className="block text-sm font-medium">Yêu cầu thêm<textarea className="mt-2 min-h-24 w-full rounded-md border border-stone-200 bg-white p-3 text-sm" maxLength={2000} value={instructions} onChange={(event) => setInstructions(event.target.value)} placeholder="Chủ đề, mức độ, số câu mong muốn…" /></label>
          </div>
        </Card>

        <Card className="p-5"><h2 className="font-semibold">Nguồn kiến thức</h2><p className="mt-1 text-xs text-stone-500">Chọn tối đa 8 giáo trình. AI chỉ được dùng các đoạn này.</p><div className="mt-4 space-y-2">{knowledgeDocuments.map((document) => <label key={document.id} className="flex cursor-pointer items-start gap-3 rounded-xl border border-stone-100 p-3 text-sm"><input className="mt-1" type="checkbox" checked={knowledgeIds.includes(document.id)} disabled={!knowledgeIds.includes(document.id) && knowledgeIds.length >= 8} onChange={() => toggleKnowledge(document.id)} /><span><span className="font-medium">{document.title}</span><span className="mt-1 block text-xs text-stone-400">{document.chunkCount} đoạn{document.gradeLevel ? ` · khối ${document.gradeLevel}` : ''}</span></span></label>)}{!knowledgeDocuments.length && <p className="text-sm text-amber-700">Chưa có giáo trình. <Link className="underline" to="/documents">Tải tài liệu lên</Link>.</p>}</div></Card>

        <Card className="p-5"><h2 className="font-semibold">Đề mẫu (không bắt buộc)</h2><p className="mt-1 text-xs text-stone-500">AI học bố cục và phong cách, không sao chép kiến thức.</p><select className="mt-4 h-10 w-full rounded-md border border-stone-200 bg-white px-3 text-sm" value={sampleId} onChange={(event) => setSampleId(event.target.value)}><option value="">Không dùng đề mẫu</option>{samples.map((sample) => <option key={sample.id} value={sample.id}>{sample.title}</option>)}</select></Card>

        <Card className="border-sky-200 bg-sky-50/70 p-5"><div className="flex gap-3"><ShieldCheck className="mt-0.5 size-5 shrink-0 text-sky-700" /><div><p className="font-semibold text-sky-950">Xác nhận xử lý bên ngoài</p><p className="mt-1 text-xs leading-5 text-sky-900/70">{statusQuery.data?.privacyNotice}</p></div></div><label className="mt-4 flex gap-3 text-sm text-sky-950"><input type="checkbox" checked={consent} onChange={(event) => setConsent(event.target.checked)} /><span>Tôi đồng ý gửi các đoạn giáo trình/đề mẫu đã chọn tới {statusQuery.data?.provider ?? 'dịch vụ AI'} cho lần tạo đề này.</span></label></Card>

        {!statusQuery.data?.configured && <p className="rounded-xl bg-amber-50 p-4 text-sm text-amber-800">AI chưa được cấu hình. Thêm <code>GEMINI_API_KEY</code> và đặt <code>AI_ENABLED=true</code> trong <code>.env</code>, rồi khởi động lại backend.</p>}
        {generation.error && <p className="rounded-xl bg-red-50 p-4 text-sm text-red-700">{errorMessage(generation.error)}</p>}
        <Button className="w-full" disabled={!statusQuery.data?.configured || !consent || !title.trim() || knowledgeIds.length === 0 || generation.isPending} onClick={() => generation.mutate()}>{generation.isPending ? <LoaderCircle className="size-4 animate-spin" /> : <Sparkles className="size-4" />} {generation.isPending ? 'AI đang tạo và kiểm tra đề…' : 'Tạo bản nháp đề'}</Button>
      </section>

      <section className="space-y-5">
        {!visibleDraft && <Card className="grid min-h-80 place-items-center p-10 text-center"><div><FileCheck2 className="mx-auto size-10 text-stone-300" /><p className="mt-4 font-semibold">Chưa có bản nháp AI</p><p className="mt-2 text-sm text-stone-500">Chọn giáo trình, tùy chọn đề mẫu và tạo đề đầu tiên.</p></div></Card>}
        {visibleDraft && <><Card className="p-6"><div className="flex flex-wrap items-start justify-between gap-4"><div><p className="text-xs font-semibold uppercase tracking-wide text-violet-600">Bản nháp — cần giáo viên duyệt</p><h2 className="mt-2 text-2xl font-semibold">{visibleDraft.content.title}</h2><p className="mt-2 text-sm text-stone-500">{visibleDraft.durationMinutes} phút · Khối {visibleDraft.gradeLevel} · {visibleDraft.model}</p></div><Button asChild variant="outline" size="sm"><Link to="/documents"><BookOpenCheck className="size-4" /> Quản lý nguồn</Link></Button></div><p className="mt-5 rounded-xl bg-stone-50 p-4 text-sm leading-6 text-stone-600">{visibleDraft.content.instructions}</p></Card>
          {visibleDraft.content.sections.map((section, sectionIndex) => <Card key={`${section.title}-${sectionIndex}`} className="overflow-hidden"><div className="border-b border-stone-100 p-5"><h3 className="font-semibold">Phần {sectionIndex + 1}. {section.title}</h3><p className="mt-1 text-sm text-stone-500">{section.description}</p></div><div className="divide-y divide-stone-100">{section.questions.map((question, questionIndex) => <article key={questionIndex} className="p-5"><div className="flex flex-wrap justify-between gap-2"><p className="font-medium">Câu {questionIndex + 1}. {question.content}</p><span className="text-xs font-semibold text-violet-700">{questionTypeLabel[question.type]} · {question.score} điểm</span></div>{question.options.length > 0 && <ol className="mt-3 grid gap-2 text-sm text-stone-600 sm:grid-cols-2">{question.options.map((option, index) => <li key={index}>{String.fromCharCode(65 + index)}. {option}</li>)}</ol>}<details className="mt-4 rounded-lg bg-emerald-50 p-3 text-sm"><summary className="cursor-pointer font-semibold text-emerald-800">Đáp án và giải thích</summary><p className="mt-2 text-emerald-900">{question.answer}</p><p className="mt-1 text-emerald-800/70">{question.explanation}</p></details><p className="mt-3 text-xs text-sky-700">Nguồn: {question.sourceChunkIds.join(', ')}</p></article>)}</div></Card>)}
        </>}
      </section>
    </div>
  </main>
}
