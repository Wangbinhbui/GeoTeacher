import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Archive, ArrowLeft, CalendarDays, LoaderCircle, Pencil, Plus, School, UsersRound } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '../../components/ui/button'
import { Card } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { ApiError, type Session } from '../../lib/api'
import {
  archiveClassGroup,
  archiveSchoolYear,
  classGroupsQueryKey,
  listClassGroups,
  listSchoolYears,
  saveClassGroup,
  saveSchoolYear,
  schoolYearsQueryKey,
  type ClassGroup,
  type SchoolYear,
} from './classroom-api'

const schoolYearSchema = z
  .object({
    code: z.string().trim().min(1, 'Nhập mã năm học.').max(30),
    startDate: z.string().min(1, 'Chọn ngày bắt đầu.'),
    endDate: z.string().min(1, 'Chọn ngày kết thúc.'),
    status: z.enum(['PLANNED', 'ACTIVE']),
  })
  .refine((value) => value.startDate < value.endDate, {
    message: 'Ngày kết thúc phải sau ngày bắt đầu.',
    path: ['endDate'],
  })

const classGroupSchema = z.object({
  classCode: z.string().trim().min(1, 'Nhập mã lớp.').max(30),
  displayName: z.string().trim().min(1, 'Nhập tên hiển thị.').max(150),
  gradeLevel: z.number().int().min(1).max(12),
})

type SchoolYearForm = z.infer<typeof schoolYearSchema>
type ClassGroupForm = z.infer<typeof classGroupSchema>

function mutationMessage(error: unknown) {
  return error instanceof ApiError ? error.message : 'Không thể lưu dữ liệu. Vui lòng thử lại.'
}

export function ClassroomPage({ session }: { session: Session }) {
  const queryClient = useQueryClient()
  const [selectedYearOverride, setSelectedYearId] = useState<string>()
  const [editingYear, setEditingYear] = useState<SchoolYear>()
  const [editingClass, setEditingClass] = useState<ClassGroup>()

  const schoolYearsQuery = useQuery({ queryKey: schoolYearsQueryKey, queryFn: listSchoolYears })
  const defaultYear = schoolYearsQuery.data?.find((year) => year.status !== 'ARCHIVED') ?? schoolYearsQuery.data?.[0]
  const selectedYearId = selectedYearOverride ?? defaultYear?.id
  const classGroupsQuery = useQuery({
    queryKey: [...classGroupsQueryKey, selectedYearId],
    queryFn: () => listClassGroups(selectedYearId),
    enabled: Boolean(selectedYearId),
  })

  const schoolYearForm = useForm<SchoolYearForm>({
    resolver: zodResolver(schoolYearSchema),
    defaultValues: { code: '', startDate: '', endDate: '', status: 'PLANNED' },
  })
  const classGroupForm = useForm<ClassGroupForm>({
    resolver: zodResolver(classGroupSchema),
    defaultValues: { classCode: '', displayName: '', gradeLevel: 10 },
  })

  const refreshYears = () => queryClient.invalidateQueries({ queryKey: schoolYearsQueryKey })
  const refreshClasses = () => queryClient.invalidateQueries({ queryKey: classGroupsQueryKey })

  const schoolYearMutation = useMutation({
    mutationFn: (values: SchoolYearForm) =>
      saveSchoolYear(
        { ...values, version: editingYear?.version },
        editingYear?.id,
      ),
    onSuccess: (saved) => {
      refreshYears()
      setSelectedYearId(saved.id)
      setEditingYear(undefined)
      schoolYearForm.reset({ code: '', startDate: '', endDate: '', status: 'PLANNED' })
    },
  })

  const classGroupMutation = useMutation({
    mutationFn: (values: ClassGroupForm) => {
      if (!selectedYearId) throw new Error('Chưa chọn năm học.')
      return saveClassGroup(
        { ...values, schoolYearId: selectedYearId, version: editingClass?.version },
        editingClass?.id,
      )
    },
    onSuccess: () => {
      refreshClasses()
      setEditingClass(undefined)
      classGroupForm.reset({ classCode: '', displayName: '', gradeLevel: 10 })
    },
  })

  const archiveYearMutation = useMutation({
    mutationFn: archiveSchoolYear,
    onSuccess: () => {
      refreshYears()
      refreshClasses()
    },
  })
  const archiveClassMutation = useMutation({ mutationFn: archiveClassGroup, onSuccess: refreshClasses })

  const selectedYear = schoolYearsQuery.data?.find((year) => year.id === selectedYearId)
  const canEditClasses = selectedYear && selectedYear.status !== 'ARCHIVED'

  function editYear(year: SchoolYear) {
    setEditingYear(year)
    schoolYearForm.reset({
      code: year.code,
      startDate: year.startDate,
      endDate: year.endDate,
      status: year.status === 'ACTIVE' ? 'ACTIVE' : 'PLANNED',
    })
  }

  function editClass(classGroup: ClassGroup) {
    setEditingClass(classGroup)
    classGroupForm.reset({
      classCode: classGroup.classCode,
      displayName: classGroup.displayName,
      gradeLevel: classGroup.gradeLevel,
    })
  }

  return (
    <main className="min-h-screen bg-[#f5f7f5] text-stone-900">
      <header className="border-b border-stone-200 bg-white">
        <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-5 sm:px-8">
          <div className="flex items-center gap-4">
            <Button asChild variant="ghost" size="icon"><Link to="/" aria-label="Về tổng quan"><ArrowLeft className="size-5" /></Link></Button>
            <div>
              <p className="font-semibold">Năm học & lớp học</p>
              <p className="text-xs text-stone-400">{session.displayName}</p>
            </div>
          </div>
          <div className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">M02 · Classroom</div>
        </div>
      </header>

      <div className="mx-auto grid max-w-7xl gap-6 px-5 py-8 sm:px-8 xl:grid-cols-[360px_1fr]">
        <section className="space-y-6">
          <Card className="p-5">
            <div className="flex items-center gap-3"><CalendarDays className="size-5 text-emerald-700" /><h1 className="font-semibold">{editingYear ? 'Sửa năm học' : 'Tạo năm học'}</h1></div>
            <form className="mt-5 space-y-4" onSubmit={schoolYearForm.handleSubmit((values) => schoolYearMutation.mutate(values))}>
              <label className="block text-sm font-medium">Mã năm học<Input className="mt-2" placeholder="2026-2027" {...schoolYearForm.register('code')} /></label>
              {schoolYearForm.formState.errors.code && <p className="text-xs text-red-600">{schoolYearForm.formState.errors.code.message}</p>}
              <div className="grid grid-cols-2 gap-3">
                <label className="block text-sm font-medium">Bắt đầu<Input className="mt-2" type="date" {...schoolYearForm.register('startDate')} /></label>
                <label className="block text-sm font-medium">Kết thúc<Input className="mt-2" type="date" {...schoolYearForm.register('endDate')} /></label>
              </div>
              {schoolYearForm.formState.errors.endDate && <p className="text-xs text-red-600">{schoolYearForm.formState.errors.endDate.message}</p>}
              <label className="block text-sm font-medium">Trạng thái
                <select className="mt-2 h-12 w-full rounded-xl border border-stone-200 bg-white px-4" {...schoolYearForm.register('status')}>
                  <option value="PLANNED">Sắp tới</option><option value="ACTIVE">Đang hoạt động</option>
                </select>
              </label>
              {schoolYearMutation.error && <p className="text-sm text-red-600">{mutationMessage(schoolYearMutation.error)}</p>}
              <div className="flex gap-2">
                <Button type="submit" className="flex-1" disabled={schoolYearMutation.isPending}>{schoolYearMutation.isPending && <LoaderCircle className="size-4 animate-spin" />}{editingYear ? 'Lưu thay đổi' : 'Tạo năm học'}</Button>
                {editingYear && <Button type="button" variant="outline" onClick={() => { setEditingYear(undefined); schoolYearForm.reset() }}>Hủy</Button>}
              </div>
            </form>
          </Card>

          <Card className="overflow-hidden">
            <div className="border-b border-stone-100 p-5"><h2 className="font-semibold">Các năm học</h2></div>
            <div className="divide-y divide-stone-100">
              {schoolYearsQuery.isPending && <p className="p-5 text-sm text-stone-500">Đang tải…</p>}
              {schoolYearsQuery.data?.map((year) => (
                <button key={year.id} type="button" onClick={() => setSelectedYearId(year.id)} className={`flex w-full items-center gap-3 p-4 text-left ${selectedYearId === year.id ? 'bg-emerald-50' : 'hover:bg-stone-50'}`}>
                  <School className="size-5 text-emerald-700" />
                  <span className="flex-1"><span className="block text-sm font-semibold">{year.code}</span><span className="text-xs text-stone-400">{year.startDate} → {year.endDate}</span></span>
                  <span className="text-[11px] font-semibold text-stone-500">{year.status === 'ACTIVE' ? 'Hoạt động' : year.status === 'PLANNED' ? 'Sắp tới' : 'Đã lưu trữ'}</span>
                </button>
              ))}
              {!schoolYearsQuery.isPending && !schoolYearsQuery.data?.length && <p className="p-5 text-sm text-stone-500">Chưa có năm học.</p>}
            </div>
          </Card>
        </section>

        <section className="space-y-6">
          <Card className="p-6">
            <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
              <div><p className="text-sm font-semibold text-emerald-700">Năm học đang chọn</p><h2 className="mt-1 text-2xl font-semibold">{selectedYear?.code ?? 'Chưa có năm học'}</h2></div>
              {selectedYear && selectedYear.status !== 'ARCHIVED' && <div className="flex gap-2"><Button variant="outline" size="sm" onClick={() => editYear(selectedYear)}><Pencil className="size-4" /> Sửa</Button><Button variant="outline" size="sm" onClick={() => window.confirm('Lưu trữ năm học này?') && archiveYearMutation.mutate(selectedYear)}><Archive className="size-4" /> Lưu trữ</Button></div>}
            </div>
          </Card>

          {canEditClasses && <Card className="p-6">
            <div className="flex items-center gap-3"><Plus className="size-5 text-emerald-700" /><h2 className="font-semibold">{editingClass ? 'Sửa lớp' : 'Thêm lớp mới'}</h2></div>
            <form className="mt-5 grid gap-4 md:grid-cols-[0.7fr_1.3fr_0.55fr_auto] md:items-end" onSubmit={classGroupForm.handleSubmit((values) => classGroupMutation.mutate(values))}>
              <label className="text-sm font-medium">Mã lớp<Input className="mt-2" placeholder="10A1" {...classGroupForm.register('classCode')} /></label>
              <label className="text-sm font-medium">Tên hiển thị<Input className="mt-2" placeholder="Lớp 10A1" {...classGroupForm.register('displayName')} /></label>
              <label className="text-sm font-medium">Khối<Input className="mt-2" type="number" min="1" max="12" {...classGroupForm.register('gradeLevel', { valueAsNumber: true })} /></label>
              <div className="flex gap-2"><Button type="submit" disabled={classGroupMutation.isPending}>{editingClass ? 'Lưu' : 'Thêm lớp'}</Button>{editingClass && <Button type="button" variant="ghost" onClick={() => { setEditingClass(undefined); classGroupForm.reset() }}>Hủy</Button>}</div>
            </form>
            {(classGroupForm.formState.errors.classCode || classGroupForm.formState.errors.displayName || classGroupMutation.error) && <p className="mt-3 text-sm text-red-600">{classGroupMutation.error ? mutationMessage(classGroupMutation.error) : 'Kiểm tra lại thông tin lớp.'}</p>}
          </Card>}

          <Card className="overflow-hidden">
            <div className="flex items-center gap-3 border-b border-stone-100 p-5"><UsersRound className="size-5 text-emerald-700" /><h2 className="font-semibold">Danh sách lớp</h2></div>
            <div className="divide-y divide-stone-100">
              {classGroupsQuery.isFetching && <p className="p-5 text-sm text-stone-500">Đang tải lớp…</p>}
              {classGroupsQuery.data?.map((classGroup) => (
                <div key={classGroup.id} className="flex items-center gap-4 p-5">
                  <div className="grid size-11 place-items-center rounded-xl bg-sky-50 font-bold text-sky-700">{classGroup.gradeLevel}</div>
                  <div className="flex-1"><p className="font-semibold">{classGroup.displayName}</p><p className="text-sm text-stone-400">Mã {classGroup.classCode} · {classGroup.status === 'ACTIVE' ? 'Đang hoạt động' : 'Đã lưu trữ'}</p></div>
                  {classGroup.status === 'ACTIVE' && canEditClasses && <div className="flex gap-1"><Button variant="ghost" size="icon" onClick={() => editClass(classGroup)} aria-label="Sửa lớp"><Pencil className="size-4" /></Button><Button variant="ghost" size="icon" onClick={() => window.confirm('Lưu trữ lớp này?') && archiveClassMutation.mutate(classGroup)} aria-label="Lưu trữ lớp"><Archive className="size-4" /></Button></div>}
                </div>
              ))}
              {!classGroupsQuery.isFetching && selectedYear && !classGroupsQuery.data?.length && <p className="p-8 text-center text-sm text-stone-500">Năm học này chưa có lớp.</p>}
              {!selectedYear && <p className="p-8 text-center text-sm text-stone-500">Tạo năm học trước khi thêm lớp.</p>}
            </div>
          </Card>
        </section>
      </div>
    </main>
  )
}
