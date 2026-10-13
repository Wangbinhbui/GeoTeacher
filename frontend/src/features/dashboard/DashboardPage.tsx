import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  BookOpenCheck,
  ChevronRight,
  ClipboardCheck,
  FileStack,
  GraduationCap,
  LayoutDashboard,
  LibraryBig,
  LogOut,
  Map,
  MapPinned,
  Settings,
  Sparkles,
  UsersRound,
} from 'lucide-react'
import { Button } from '../../components/ui/button'
import { Card } from '../../components/ui/card'
import { logout, type Session } from '../../lib/api'
import { sessionQueryKey } from '../../lib/auth-queries'
import { useNavigate } from 'react-router-dom'

const navigation = [
  { label: 'Tổng quan', icon: LayoutDashboard, active: true },
  { label: 'Lớp học', icon: UsersRound },
  { label: 'Học sinh', icon: GraduationCap },
  { label: 'Ngân hàng câu hỏi', icon: LibraryBig },
  { label: 'Đề kiểm tra', icon: FileStack },
  { label: 'Chấm bài', icon: ClipboardCheck },
  { label: 'Sổ điểm', icon: GraduationCap },
  { label: 'Giáo án & tài liệu', icon: BookOpenCheck },
  { label: 'Công cụ Địa lý', icon: Map },
  { label: 'Cài đặt', icon: Settings },
]

const overviewCards = [
  { label: 'Lớp đang quản lý', value: '—', icon: UsersRound, accent: 'bg-sky-50 text-sky-700' },
  { label: 'Học sinh', value: '—', icon: GraduationCap, accent: 'bg-amber-50 text-amber-700' },
  { label: 'Câu hỏi', value: '—', icon: LibraryBig, accent: 'bg-violet-50 text-violet-700' },
  { label: 'Đề kiểm tra', value: '—', icon: FileStack, accent: 'bg-emerald-50 text-emerald-700' },
]

export function DashboardPage({ session }: { session: Session }) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const logoutMutation = useMutation({
    mutationFn: logout,
    onSuccess: () => queryClient.setQueryData(sessionQueryKey, { authenticated: false }),
  })

  return (
    <div className="min-h-screen bg-[#f5f7f5] text-stone-900 lg:grid lg:grid-cols-[260px_1fr]">
      <aside className="hidden border-r border-stone-200 bg-white lg:flex lg:min-h-screen lg:flex-col">
        <div className="flex h-20 items-center gap-3 border-b border-stone-100 px-6">
          <div className="grid size-10 place-items-center rounded-xl bg-emerald-800 text-white"><MapPinned className="size-5" /></div>
          <div>
            <p className="font-bold tracking-tight">GeoTeacher</p>
            <p className="text-[11px] text-stone-400">Không gian giảng dạy</p>
          </div>
        </div>

        <nav className="flex-1 space-y-1 px-3 py-5" aria-label="Điều hướng chính">
          {navigation.map((item) => (
            <button
              key={item.label}
              type="button"
              disabled={!item.active && item.label !== 'Lớp học' && item.label !== 'Học sinh'}
              onClick={() => {
                if (item.label === 'Lớp học') navigate('/classrooms')
                if (item.label === 'Học sinh') navigate('/students')
              }}
              className={`flex h-11 w-full items-center gap-3 rounded-xl px-3 text-left text-sm font-medium transition ${
                item.active
                  ? 'bg-emerald-50 text-emerald-900'
                  : 'text-stone-500 hover:bg-stone-50 disabled:cursor-not-allowed disabled:opacity-70'
              }`}
            >
              <item.icon className="size-[18px]" />
              {item.label}
            </button>
          ))}
        </nav>

        <div className="border-t border-stone-100 p-4">
          <div className="mb-3 flex items-center gap-3 px-2">
            <div className="grid size-9 place-items-center rounded-full bg-emerald-100 text-sm font-bold text-emerald-800">
              {(session.displayName ?? 'G').charAt(0).toUpperCase()}
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-semibold">{session.displayName}</p>
              <p className="truncate text-xs text-stone-400">@{session.username}</p>
            </div>
          </div>
          <Button variant="ghost" className="w-full justify-start" onClick={() => logoutMutation.mutate()} disabled={logoutMutation.isPending}>
            <LogOut className="size-4" /> Đăng xuất
          </Button>
        </div>
      </aside>

      <main className="min-w-0">
        <header className="flex h-20 items-center justify-between border-b border-stone-200 bg-white/80 px-5 backdrop-blur sm:px-8">
          <div className="flex items-center gap-3 lg:hidden">
            <div className="grid size-9 place-items-center rounded-xl bg-emerald-800 text-white"><MapPinned className="size-5" /></div>
            <span className="font-bold">GeoTeacher</span>
          </div>
          <p className="hidden text-sm text-stone-500 lg:block">Không gian làm việc của giáo viên</p>
          <Button variant="outline" size="sm" className="lg:hidden" onClick={() => logoutMutation.mutate()}>
            <LogOut className="size-4" /> Đăng xuất
          </Button>
        </header>

        <div className="mx-auto max-w-7xl px-5 py-8 sm:px-8 lg:py-10">
          <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
            <div>
              <p className="text-sm font-semibold text-emerald-700">Tổng quan</p>
              <h1 className="mt-2 text-3xl font-semibold tracking-[-0.035em] text-stone-950">Xin chào, {session.displayName}</h1>
              <p className="mt-2 text-sm text-stone-500">Nền tảng đã sẵn sàng. Hãy bắt đầu bằng việc thiết lập năm học và lớp.</p>
            </div>
            <Button disabled><Sparkles className="size-4" /> Thao tác nhanh</Button>
          </div>

          <section className="mt-8 grid gap-4 sm:grid-cols-2 xl:grid-cols-4" aria-label="Số liệu tổng quan">
            {overviewCards.map((item) => (
              <Card key={item.label} className="p-5">
                <div className={`grid size-10 place-items-center rounded-xl ${item.accent}`}><item.icon className="size-5" /></div>
                <p className="mt-5 text-3xl font-semibold tracking-tight">{item.value}</p>
                <p className="mt-1 text-sm text-stone-500">{item.label}</p>
              </Card>
            ))}
          </section>

          <section className="mt-6 grid gap-6 xl:grid-cols-[1.35fr_0.65fr]">
            <Card className="p-6 sm:p-7">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="text-lg font-semibold">Thiết lập ban đầu</h2>
                  <p className="mt-1 text-sm text-stone-500">Các bước tiếp theo trong kế hoạch MVP.</p>
                </div>
                <span className="rounded-full bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-700">Đang phát triển</span>
              </div>
              <div className="mt-6 space-y-3">
                {['Tạo năm học hiện tại', 'Thêm lớp học', 'Nhập danh sách học sinh'].map((label, index) => (
                  <div key={label} className="flex items-center gap-4 rounded-xl border border-stone-100 bg-stone-50/70 p-4">
                    <div className="grid size-8 place-items-center rounded-full bg-white text-xs font-bold text-stone-500 shadow-sm">{index + 1}</div>
                    <span className="flex-1 text-sm font-medium text-stone-700">{label}</span>
                    <ChevronRight className="size-4 text-stone-300" />
                  </div>
                ))}
              </div>
            </Card>

            <Card className="overflow-hidden bg-emerald-900 p-6 text-white sm:p-7">
              <div className="grid size-11 place-items-center rounded-2xl bg-white/10 ring-1 ring-white/10"><Map className="size-5" /></div>
              <h2 className="mt-8 text-xl font-semibold tracking-tight">Foundation đã hoạt động</h2>
              <p className="mt-3 text-sm leading-6 text-emerald-100/70">Xác thực, bảo vệ CSRF, database migration và giao diện nền đã được kết nối.</p>
              <div className="mt-8 h-px bg-white/10" />
              <p className="mt-5 text-xs text-emerald-100/55">Bước kế tiếp: Classroom & Student Management</p>
            </Card>
          </section>
        </div>
      </main>
    </div>
  )
}
