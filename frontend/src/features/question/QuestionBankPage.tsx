import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Archive, ArrowLeft, BookOpenCheck, LoaderCircle, Pencil, Plus, Search } from 'lucide-react'
import { useState } from 'react'
import { useFieldArray, useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '../../components/ui/button'
import { Card } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { ApiError, type Session } from '../../lib/api'
import {
  archiveQuestion,
  getQuestion,
  questionsQueryKey,
  saveQuestion,
  searchQuestions,
  type QuestionSummary,
} from './question-api'

const questionSchema = z.object({
  gradeLevel: z.number().int().min(1).max(12),
  topic: z.string().trim().min(1, 'Nhập chủ đề/bài.').max(150),
  cognitiveLevel: z.enum(['RECOGNITION', 'COMPREHENSION', 'APPLICATION', 'HIGH_APPLICATION']),
  content: z.string().trim().min(1, 'Nhập nội dung câu hỏi.').max(10000),
  explanation: z.string().max(10000),
  suggestedScore: z.string().regex(/^$|^[0-9]{1,4}(\.[0-9]{1,3})$/, 'Nhập số dương, tối đa 3 số lẻ.'),
  status: z.enum(['DRAFT', 'ACTIVE']),
  options: z.array(z.object({ key: z.string(), content: z.string().trim().min(1, 'Nhập phương án.') })).min(2),
  correctKey: z.string().min(1),
})

type QuestionForm = z.infer<typeof questionSchema>

const defaults: QuestionForm = {
  gradeLevel: 10,
  topic: '',
  cognitiveLevel: 'RECOGNITION',
  content: '',
  explanation: '',
  suggestedScore: '',
  status: 'DRAFT',
  options: [
    { key: 'A', content: '' },
    { key: 'B', content: '' },
    { key: 'C', content: '' },
    { key: 'D', content: '' },
  ],
  correctKey: 'A',
}

function errorMessage(error: unknown) {
  return error instanceof ApiError ? error.message : 'Không thể lưu câu hỏi. Vui lòng thử lại.'
}

const levelLabels = {
  RECOGNITION: 'Nhận biết',
  COMPREHENSION: 'Thông hiểu',
  APPLICATION: 'Vận dụng',
  HIGH_APPLICATION: 'Vận dụng cao',
}

export function QuestionBankPage({ session }: { session: Session }) {
  const queryClient = useQueryClient()
  const [query, setQuery] = useState('')
  const [editing, setEditing] = useState<QuestionSummary>()

  const questionsQuery = useQuery({
    queryKey: [...questionsQueryKey, query],
    queryFn: () => searchQuestions(query),
  })
  const form = useForm<QuestionForm>({ resolver: zodResolver(questionSchema), defaultValues: defaults })
  const options = useFieldArray({ control: form.control, name: 'options' })

  const saveMutation = useMutation({
    mutationFn: (values: QuestionForm) => saveQuestion({
      type: 'SINGLE_CHOICE',
      gradeLevel: values.gradeLevel,
      topic: values.topic,
      cognitiveLevel: values.cognitiveLevel,
      content: values.content,
      explanation: values.explanation || undefined,
      suggestedScore: values.suggestedScore || undefined,
      status: values.status,
      options: values.options.map((option) => ({ ...option, correct: option.key === values.correctKey })),
      version: editing?.version,
    }, editing?.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: questionsQueryKey })
      setEditing(undefined)
      form.reset(defaults)
    },
  })
  const archiveMutation = useMutation({
    mutationFn: archiveQuestion,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: questionsQueryKey }),
  })

  async function editQuestion(question: QuestionSummary) {
    const detail = await getQuestion(question.id)
    setEditing(question)
    form.reset({
      gradeLevel: detail.gradeLevel,
      topic: detail.topic,
      cognitiveLevel: detail.cognitiveLevel,
      content: detail.content,
      explanation: detail.explanation ?? '',
      suggestedScore: detail.suggestedScore ?? '',
      status: detail.status === 'ACTIVE' ? 'ACTIVE' : 'DRAFT',
      options: detail.options.map((option) => ({ key: option.key, content: option.content })),
      correctKey: detail.options.find((option) => option.correct)?.key ?? 'A',
    })
  }

  return <main className="min-h-screen bg-[#f5f7f5] text-stone-900">
    <header className="border-b border-stone-200 bg-white">
      <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-5 sm:px-8">
        <div className="flex items-center gap-4"><Button asChild variant="ghost" size="icon"><Link to="/" aria-label="Về tổng quan"><ArrowLeft className="size-5" /></Link></Button><div><p className="font-semibold">Ngân hàng câu hỏi</p><p className="text-xs text-stone-400">{session.displayName}</p></div></div>
        <div className="rounded-full bg-violet-50 px-3 py-1 text-xs font-semibold text-violet-700">M03 · Question Bank</div>
      </div>
    </header>

    <div className="mx-auto grid max-w-7xl gap-6 px-5 py-8 sm:px-8 xl:grid-cols-[430px_1fr]">
      <Card className="h-fit p-5">
        <div className="flex items-center gap-3"><Plus className="size-5 text-emerald-700" /><h1 className="font-semibold">{editing ? 'Sửa câu hỏi' : 'Thêm câu trắc nghiệm'}</h1></div>
        <p className="mt-2 text-xs text-stone-500">Lát cắt đầu hỗ trợ trắc nghiệm một đáp án.</p>
        <form className="mt-5 space-y-4" onSubmit={form.handleSubmit((values) => saveMutation.mutate(values))}>
          <div className="grid grid-cols-2 gap-3">
            <label className="text-sm font-medium">Khối<Input className="mt-2" type="number" min="1" max="12" {...form.register('gradeLevel', { valueAsNumber: true })} /></label>
            <label className="text-sm font-medium">Mức độ<select className="mt-2 h-12 w-full rounded-xl border border-stone-200 bg-white px-3" {...form.register('cognitiveLevel')}><option value="RECOGNITION">Nhận biết</option><option value="COMPREHENSION">Thông hiểu</option><option value="APPLICATION">Vận dụng</option><option value="HIGH_APPLICATION">Vận dụng cao</option></select></label>
          </div>
          <label className="block text-sm font-medium">Chủ đề / bài<Input className="mt-2" placeholder="Địa lí dân cư" {...form.register('topic')} /></label>
          <label className="block text-sm font-medium">Nội dung<textarea className="mt-2 min-h-28 w-full rounded-xl border border-stone-200 bg-white p-3 text-sm" placeholder="Nhập câu hỏi…" {...form.register('content')} /></label>
          {options.fields.map((field, index) => <label key={field.id} className="grid grid-cols-[28px_1fr] items-center gap-2 text-sm font-medium"><input type="radio" value={field.key} {...form.register('correctKey')} /><Input placeholder={`Phương án ${field.key}`} {...form.register(`options.${index}.content`)} /><input type="hidden" {...form.register(`options.${index}.key`)} /></label>)}
          <label className="block text-sm font-medium">Giải thích đáp án<textarea className="mt-2 min-h-20 w-full rounded-xl border border-stone-200 bg-white p-3 text-sm" {...form.register('explanation')} /></label>
          <div className="grid grid-cols-2 gap-3"><label className="text-sm font-medium">Điểm gợi ý<Input className="mt-2" inputMode="decimal" placeholder="0.25" {...form.register('suggestedScore')} /></label><label className="text-sm font-medium">Trạng thái<select className="mt-2 h-12 w-full rounded-xl border border-stone-200 bg-white px-3" {...form.register('status')}><option value="DRAFT">Bản nháp</option><option value="ACTIVE">Hoạt động</option></select></label></div>
          {Object.keys(form.formState.errors).length > 0 && <p className="text-sm text-red-600">Kiểm tra lại nội dung và các phương án.</p>}
          {saveMutation.error && <p className="text-sm text-red-600">{errorMessage(saveMutation.error)}</p>}
          <div className="flex gap-2"><Button className="flex-1" type="submit" disabled={saveMutation.isPending}>{saveMutation.isPending && <LoaderCircle className="size-4 animate-spin" />}{editing ? 'Lưu thay đổi' : 'Thêm câu hỏi'}</Button>{editing && <Button type="button" variant="outline" onClick={() => { setEditing(undefined); form.reset(defaults) }}>Hủy</Button>}</div>
        </form>
      </Card>

      <section className="space-y-6">
      <Card className="border-amber-200 bg-amber-50/70 p-5">
        <h2 className="font-semibold text-amber-950">Tạo câu hỏi từ tài liệu bằng AI</h2>
        <p className="mt-2 text-sm leading-6 text-amber-900/70">Khu vực tải PDF/DOCX sẽ nằm trong “Giáo án & tài liệu”. Upload và AI quét chưa được bật vì nhà cung cấp AI, quyền riêng tư và OCR tài liệu scan vẫn cần chốt.</p>
      </Card>
      <Card className="overflow-hidden">
        <div className="flex flex-col gap-4 border-b border-stone-100 p-5 sm:flex-row sm:items-center sm:justify-between"><div><div className="flex items-center gap-3"><BookOpenCheck className="size-5 text-violet-700" /><h2 className="font-semibold">Câu hỏi đã lưu</h2></div><p className="mt-1 text-xs text-stone-500">Danh sách không tải kèm đáp án đúng.</p></div><label className="relative block sm:w-72"><Search className="absolute left-3 top-3.5 size-4 text-stone-400" /><Input className="pl-9" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Tìm nội dung hoặc chủ đề" /></label></div>
        <div className="divide-y divide-stone-100">
          {questionsQuery.isFetching && <p className="p-5 text-sm text-stone-500">Đang tải câu hỏi…</p>}
          {questionsQuery.data?.map((question) => <div key={question.id} className="flex gap-4 p-5"><div className="grid size-11 shrink-0 place-items-center rounded-xl bg-violet-50 text-sm font-bold text-violet-700">{question.gradeLevel}</div><div className="min-w-0 flex-1"><div className="flex flex-wrap gap-2 text-xs"><span className="font-semibold text-emerald-700">{question.topic}</span><span className="text-stone-400">{levelLabels[question.cognitiveLevel]}</span><span className="text-stone-400">{question.status === 'ACTIVE' ? 'Hoạt động' : question.status === 'DRAFT' ? 'Bản nháp' : 'Đã lưu trữ'}</span></div><p className="mt-2 line-clamp-3 text-sm leading-6">{question.content}</p></div>{question.status !== 'ARCHIVED' && <div className="flex shrink-0 gap-1"><Button variant="ghost" size="icon" onClick={() => editQuestion(question)} aria-label="Sửa câu hỏi"><Pencil className="size-4" /></Button><Button variant="ghost" size="icon" onClick={() => window.confirm('Lưu trữ câu hỏi này?') && archiveMutation.mutate(question)} aria-label="Lưu trữ câu hỏi"><Archive className="size-4" /></Button></div>}</div>)}
          {!questionsQuery.isFetching && !questionsQuery.data?.length && <p className="p-10 text-center text-sm text-stone-500">Chưa có câu hỏi. Hãy thêm câu đầu tiên.</p>}
        </div>
      </Card>
      </section>
    </div>
  </main>
}
