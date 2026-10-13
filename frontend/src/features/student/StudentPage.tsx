import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Archive, ArrowLeft, LoaderCircle, Pencil, Search, UserPlus, UsersRound } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '../../components/ui/button'
import { Card } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { listClassGroups, type ClassGroup } from '../classroom/classroom-api'
import { ApiError, type Session } from '../../lib/api'
import {
  archiveStudent,
  enrollmentsQueryKey,
  enrollStudent,
  leaveEnrollment,
  listClassEnrollments,
  saveStudent,
  searchStudents,
  studentsQueryKey,
  type Enrollment,
  type Student,
} from './student-api'

const studentSchema = z.object({
  studentCode: z.string().trim().min(1, 'Nhập mã học sinh.').max(50),
  fullName: z.string().trim().min(1, 'Nhập họ tên.').max(150),
  dateOfBirth: z.string(),
})

type StudentForm = z.infer<typeof studentSchema>

function errorMessage(error: unknown) {
  return error instanceof ApiError ? error.message : 'Không thể lưu dữ liệu. Vui lòng thử lại.'
}

function today() {
  return new Date().toISOString().slice(0, 10)
}

export function StudentPage({ session }: { session: Session }) {
  const queryClient = useQueryClient()
  const [query, setQuery] = useState('')
  const [editingStudent, setEditingStudent] = useState<Student>()
  const [selectedStudent, setSelectedStudent] = useState<Student>()
  const [selectedClassId, setSelectedClassId] = useState('')
  const [enrolledAt, setEnrolledAt] = useState(today())

  const studentsQuery = useQuery({
    queryKey: [...studentsQueryKey, query],
    queryFn: () => searchStudents(query),
  })
  const classesQuery = useQuery({ queryKey: ['class-groups', 'all'], queryFn: () => listClassGroups() })
  const activeClasses = classesQuery.data?.filter((item) => item.status === 'ACTIVE') ?? []
  const effectiveClassId = selectedClassId || activeClasses[0]?.id || ''
  const rosterQuery = useQuery({
    queryKey: [...enrollmentsQueryKey, effectiveClassId],
    queryFn: () => listClassEnrollments(effectiveClassId),
    enabled: Boolean(effectiveClassId),
  })

  const form = useForm<StudentForm>({
    resolver: zodResolver(studentSchema),
    defaultValues: { studentCode: '', fullName: '', dateOfBirth: '' },
  })

  const refreshStudents = () => queryClient.invalidateQueries({ queryKey: studentsQueryKey })
  const refreshRoster = () => queryClient.invalidateQueries({ queryKey: enrollmentsQueryKey })

  const studentMutation = useMutation({
    mutationFn: (values: StudentForm) => saveStudent(
      { ...values, version: editingStudent?.version },
      editingStudent?.id,
    ),
    onSuccess: (saved) => {
      refreshStudents()
      setSelectedStudent(saved)
      setEditingStudent(undefined)
      form.reset({ studentCode: '', fullName: '', dateOfBirth: '' })
    },
  })
  const archiveMutation = useMutation({
    mutationFn: archiveStudent,
    onSuccess: () => {
      refreshStudents()
      setSelectedStudent(undefined)
    },
  })
  const enrollMutation = useMutation({
    mutationFn: () => {
      if (!selectedStudent || !effectiveClassId) throw new Error('Chưa chọn học sinh hoặc lớp.')
      return enrollStudent(effectiveClassId, selectedStudent.id, enrolledAt)
    },
    onSuccess: refreshRoster,
  })
  const leaveMutation = useMutation({
    mutationFn: (enrollment: Enrollment) => leaveEnrollment(enrollment, today()),
    onSuccess: refreshRoster,
  })

  function editStudent(student: Student) {
    setEditingStudent(student)
    form.reset({
      studentCode: student.studentCode,
      fullName: student.fullName,
      dateOfBirth: student.dateOfBirth ?? '',
    })
  }

  return (
    <main className="min-h-screen bg-[#f5f7f5] text-stone-900">
      <header className="border-b border-stone-200 bg-white">
        <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-5 sm:px-8">
          <div className="flex items-center gap-4">
            <Button asChild variant="ghost" size="icon"><Link to="/classrooms" aria-label="Về lớp học"><ArrowLeft className="size-5" /></Link></Button>
            <div><p className="font-semibold">Học sinh & xếp lớp</p><p className="text-xs text-stone-400">{session.displayName}</p></div>
          </div>
          <div className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700">M02 · Student</div>
        </div>
      </header>

      <div className="mx-auto grid max-w-7xl gap-6 px-5 py-8 sm:px-8 xl:grid-cols-[390px_1fr]">
        <section className="space-y-6">
          <Card className="p-5">
            <div className="flex items-center gap-3"><UserPlus className="size-5 text-emerald-700" /><h1 className="font-semibold">{editingStudent ? 'Sửa học sinh' : 'Thêm học sinh'}</h1></div>
            <form className="mt-5 space-y-4" onSubmit={form.handleSubmit((values) => studentMutation.mutate(values))}>
              <label className="block text-sm font-medium">Mã học sinh<Input className="mt-2" placeholder="HS001" {...form.register('studentCode')} /></label>
              {form.formState.errors.studentCode && <p className="text-xs text-red-600">{form.formState.errors.studentCode.message}</p>}
              <label className="block text-sm font-medium">Họ và tên<Input className="mt-2" placeholder="Nguyễn Văn An" {...form.register('fullName')} /></label>
              {form.formState.errors.fullName && <p className="text-xs text-red-600">{form.formState.errors.fullName.message}</p>}
              <label className="block text-sm font-medium">Ngày sinh<Input className="mt-2" type="date" max={today()} {...form.register('dateOfBirth')} /></label>
              {studentMutation.error && <p className="text-sm text-red-600">{errorMessage(studentMutation.error)}</p>}
              <div className="flex gap-2">
                <Button className="flex-1" type="submit" disabled={studentMutation.isPending}>{studentMutation.isPending && <LoaderCircle className="size-4 animate-spin" />}{editingStudent ? 'Lưu thay đổi' : 'Thêm học sinh'}</Button>
                {editingStudent && <Button type="button" variant="outline" onClick={() => { setEditingStudent(undefined); form.reset() }}>Hủy</Button>}
              </div>
            </form>
          </Card>

          <Card className="p-5">
            <h2 className="font-semibold">Ghi danh vào lớp</h2>
            <p className="mt-1 text-xs text-stone-500">Chọn học sinh ở danh sách bên phải.</p>
            <label className="mt-4 block text-sm font-medium">Lớp học
              <select value={effectiveClassId} onChange={(event) => setSelectedClassId(event.target.value)} className="mt-2 h-12 w-full rounded-xl border border-stone-200 bg-white px-4">
                {!activeClasses.length && <option value="">Chưa có lớp hoạt động</option>}
                {activeClasses.map((item: ClassGroup) => <option key={item.id} value={item.id}>{item.schoolYearCode} · {item.displayName}</option>)}
              </select>
            </label>
            <label className="mt-4 block text-sm font-medium">Ngày ghi danh<Input className="mt-2" type="date" max={today()} value={enrolledAt} onChange={(event) => setEnrolledAt(event.target.value)} /></label>
            <p className="mt-4 rounded-xl bg-stone-50 p-3 text-sm text-stone-600">{selectedStudent ? `${selectedStudent.studentCode} · ${selectedStudent.fullName}` : 'Chưa chọn học sinh'}</p>
            {enrollMutation.error && <p className="mt-3 text-sm text-red-600">{errorMessage(enrollMutation.error)}</p>}
            <Button className="mt-4 w-full" disabled={!selectedStudent || !effectiveClassId || enrollMutation.isPending} onClick={() => enrollMutation.mutate()}><UserPlus className="size-4" /> Ghi danh</Button>
          </Card>
        </section>

        <section className="space-y-6">
          <Card className="overflow-hidden">
            <div className="border-b border-stone-100 p-5">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div><h2 className="font-semibold">Danh sách học sinh</h2><p className="mt-1 text-xs text-stone-500">Mã học sinh duy nhất trên toàn hệ thống.</p></div>
                <label className="relative block sm:w-72"><Search className="absolute left-3 top-3.5 size-4 text-stone-400" /><Input className="pl-9" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Tìm mã hoặc họ tên" /></label>
              </div>
            </div>
            <div className="divide-y divide-stone-100">
              {studentsQuery.isFetching && <p className="p-5 text-sm text-stone-500">Đang tải học sinh…</p>}
              {studentsQuery.data?.map((student) => (
                <div key={student.id} className={`flex items-center gap-4 p-5 ${selectedStudent?.id === student.id ? 'bg-emerald-50' : ''}`}>
                  <button type="button" className="min-w-0 flex-1 text-left" onClick={() => setSelectedStudent(student)}>
                    <p className="truncate font-semibold">{student.fullName}</p><p className="text-sm text-stone-400">{student.studentCode}{student.dateOfBirth ? ` · ${student.dateOfBirth}` : ''} · {student.status === 'ACTIVE' ? 'Đang học' : 'Đã lưu trữ'}</p>
                  </button>
                  {student.status === 'ACTIVE' && <div className="flex gap-1"><Button variant="ghost" size="icon" onClick={() => editStudent(student)} aria-label="Sửa học sinh"><Pencil className="size-4" /></Button><Button variant="ghost" size="icon" onClick={() => window.confirm('Lưu trữ học sinh này?') && archiveMutation.mutate(student)} aria-label="Lưu trữ học sinh"><Archive className="size-4" /></Button></div>}
                </div>
              ))}
              {!studentsQuery.isFetching && !studentsQuery.data?.length && <p className="p-8 text-center text-sm text-stone-500">Chưa tìm thấy học sinh.</p>}
            </div>
          </Card>

          <Card className="overflow-hidden">
            <div className="flex items-center gap-3 border-b border-stone-100 p-5"><UsersRound className="size-5 text-emerald-700" /><div><h2 className="font-semibold">Danh sách lớp</h2><p className="mt-1 text-xs text-stone-500">{activeClasses.find((item) => item.id === effectiveClassId)?.displayName ?? 'Chưa chọn lớp'}</p></div></div>
            <div className="divide-y divide-stone-100">
              {rosterQuery.isFetching && <p className="p-5 text-sm text-stone-500">Đang tải danh sách lớp…</p>}
              {rosterQuery.data?.map((enrollment) => (
                <div key={enrollment.id} className="flex items-center gap-4 p-5">
                  <div className="min-w-0 flex-1"><p className="truncate font-semibold">{enrollment.studentFullName}</p><p className="text-sm text-stone-400">{enrollment.studentCode} · vào lớp {enrollment.enrolledAt}{enrollment.leftAt ? ` · rời ${enrollment.leftAt}` : ''}</p></div>
                  {enrollment.status === 'ACTIVE' && <Button variant="outline" size="sm" disabled={leaveMutation.isPending} onClick={() => window.confirm('Xác nhận học sinh rời lớp hôm nay?') && leaveMutation.mutate(enrollment)}>Rời lớp</Button>}
                </div>
              ))}
              {!rosterQuery.isFetching && effectiveClassId && !rosterQuery.data?.length && <p className="p-8 text-center text-sm text-stone-500">Lớp chưa có học sinh.</p>}
              {!effectiveClassId && <p className="p-8 text-center text-sm text-stone-500">Tạo lớp học trước khi ghi danh.</p>}
            </div>
          </Card>
        </section>
      </div>
    </main>
  )
}
