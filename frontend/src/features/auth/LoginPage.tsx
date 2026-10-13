import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { BookOpenText, LoaderCircle, MapPinned } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { Button } from '../../components/ui/button'
import { Card } from '../../components/ui/card'
import { Input } from '../../components/ui/input'
import { ApiError, login } from '../../lib/api'
import { sessionQueryKey } from '../../lib/auth-queries'

const loginSchema = z.object({
  username: z.string().trim().min(1, 'Vui lòng nhập tên đăng nhập.').max(100),
  password: z.string().min(1, 'Vui lòng nhập mật khẩu.').max(128),
})

type LoginValues = z.infer<typeof loginSchema>

export function LoginPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const form = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { username: '', password: '' },
  })

  const loginMutation = useMutation({
    mutationFn: (values: LoginValues) => login(values.username, values.password),
    onSuccess: (session) => {
      queryClient.setQueryData(sessionQueryKey, session)
      navigate('/', { replace: true })
    },
  })

  const errorMessage =
    loginMutation.error instanceof ApiError
      ? loginMutation.error.message
      : loginMutation.isError
        ? 'Không thể kết nối đến hệ thống. Vui lòng thử lại.'
        : null

  return (
    <main className="relative min-h-screen overflow-hidden bg-[#f3f5f1] px-5 py-8 sm:px-8">
      <div className="pointer-events-none absolute inset-0 opacity-60 [background-image:radial-gradient(#8fa99a_0.8px,transparent_0.8px)] [background-size:22px_22px]" />
      <div className="relative mx-auto grid min-h-[calc(100vh-4rem)] max-w-6xl overflow-hidden rounded-[2rem] border border-white/80 bg-white shadow-[0_30px_80px_rgba(29,53,43,0.13)] lg:grid-cols-[1.08fr_0.92fr]">
        <section className="relative hidden overflow-hidden bg-emerald-900 p-12 text-white lg:flex lg:flex-col lg:justify-between">
          <div className="absolute -right-24 -top-24 size-80 rounded-full border border-white/10" />
          <div className="absolute -bottom-20 -left-20 size-72 rounded-full bg-emerald-700/35 blur-2xl" />
          <div className="relative flex items-center gap-3">
            <div className="grid size-11 place-items-center rounded-2xl bg-white/12 ring-1 ring-white/15">
              <MapPinned className="size-6" />
            </div>
            <div>
              <p className="text-lg font-bold tracking-tight">GeoTeacher</p>
              <p className="text-xs text-emerald-100/70">Geography Teaching Management</p>
            </div>
          </div>

          <div className="relative max-w-md">
            <span className="mb-6 inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-3 py-1.5 text-xs font-medium text-emerald-50">
              <BookOpenText className="size-4" />
              Trợ lý công việc dành cho giáo viên Địa lý
            </span>
            <h1 className="text-4xl font-semibold leading-tight tracking-[-0.04em] xl:text-5xl">
              Tổ chức lớp học và đề kiểm tra trong một không gian rõ ràng.
            </h1>
            <p className="mt-6 max-w-sm text-[15px] leading-7 text-emerald-100/75">
              Quản lý học sinh, xây ngân hàng câu hỏi, tạo nhiều mã đề và chấm bài trên giấy — nhất quán từ đầu đến cuối.
            </p>
          </div>

          <p className="relative text-xs text-emerald-100/50">Dữ liệu học sinh và đáp án được bảo vệ trong tài khoản của giáo viên.</p>
        </section>

        <section className="flex items-center justify-center px-6 py-12 sm:px-12 lg:px-16">
          <div className="w-full max-w-md">
            <div className="mb-10 flex items-center gap-3 lg:hidden">
              <div className="grid size-10 place-items-center rounded-xl bg-emerald-800 text-white">
                <MapPinned className="size-5" />
              </div>
              <span className="text-lg font-bold text-stone-900">GeoTeacher</span>
            </div>

            <p className="text-sm font-semibold text-emerald-800">Chào mừng trở lại</p>
            <h2 className="mt-2 text-3xl font-semibold tracking-[-0.035em] text-stone-950">Đăng nhập hệ thống</h2>
            <p className="mt-3 text-sm leading-6 text-stone-500">Sử dụng tài khoản giáo viên đã được cấu hình để tiếp tục.</p>

            <Card className="mt-8 border-0 p-0 shadow-none">
              <form
                className="space-y-5"
                onSubmit={form.handleSubmit((values) => loginMutation.mutate(values))}
                noValidate
              >
                <div>
                  <label htmlFor="username" className="mb-2 block text-sm font-semibold text-stone-700">Tên đăng nhập</label>
                  <Input
                    id="username"
                    autoComplete="username"
                    autoFocus
                    placeholder="Nhập tên đăng nhập"
                    aria-invalid={Boolean(form.formState.errors.username)}
                    {...form.register('username')}
                  />
                  {form.formState.errors.username && (
                    <p className="mt-1.5 text-xs text-red-600">{form.formState.errors.username.message}</p>
                  )}
                </div>

                <div>
                  <label htmlFor="password" className="mb-2 block text-sm font-semibold text-stone-700">Mật khẩu</label>
                  <Input
                    id="password"
                    type="password"
                    autoComplete="current-password"
                    placeholder="Nhập mật khẩu"
                    aria-invalid={Boolean(form.formState.errors.password)}
                    {...form.register('password')}
                  />
                  {form.formState.errors.password && (
                    <p className="mt-1.5 text-xs text-red-600">{form.formState.errors.password.message}</p>
                  )}
                </div>

                {errorMessage && (
                  <div role="alert" className="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700">
                    {errorMessage}
                  </div>
                )}

                <Button className="h-12 w-full" disabled={loginMutation.isPending} type="submit">
                  {loginMutation.isPending && <LoaderCircle className="size-4 animate-spin" />}
                  Đăng nhập
                </Button>
              </form>
            </Card>
          </div>
        </section>
      </div>
    </main>
  )
}
