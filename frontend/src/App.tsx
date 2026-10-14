import { useQuery } from '@tanstack/react-query'
import { LoaderCircle } from 'lucide-react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { LoginPage } from './features/auth/LoginPage'
import { ClassroomPage } from './features/classroom/ClassroomPage'
import { DashboardPage } from './features/dashboard/DashboardPage'
import { StudentPage } from './features/student/StudentPage'
import { QuestionBankPage } from './features/question/QuestionBankPage'
import { DocumentPage } from './features/document/DocumentPage'
import { AiExamPage } from './features/ai-exam/AiExamPage'
import { getSession, type Session } from './lib/api'
import { sessionQueryKey } from './lib/auth-queries'

function App() {
  const sessionQuery = useQuery({
    queryKey: sessionQueryKey,
    queryFn: getSession,
  })

  if (sessionQuery.isPending) {
    return (
      <main className="grid min-h-screen place-items-center bg-stone-50 text-emerald-800">
        <div className="flex items-center gap-3 text-sm font-medium">
          <LoaderCircle className="size-5 animate-spin" />
          Đang khởi động GeoTeacher…
        </div>
      </main>
    )
  }

  const session: Session = sessionQuery.data ?? { authenticated: false }

  return (
    <Routes>
      <Route
        path="/login"
        element={session.authenticated ? <Navigate to="/" replace /> : <LoginPage />}
      />
      <Route
        path="/"
        element={
          session.authenticated ? (
            <DashboardPage session={session} />
          ) : (
            <Navigate to="/login" replace />
          )
        }
      />
      <Route
        path="/classrooms"
        element={
          session.authenticated ? (
            <ClassroomPage session={session} />
          ) : (
            <Navigate to="/login" replace />
          )
        }
      />
      <Route
        path="/students"
        element={
          session.authenticated ? (
            <StudentPage session={session} />
          ) : (
            <Navigate to="/login" replace />
          )
        }
      />
      <Route
        path="/questions"
        element={
          session.authenticated ? (
            <QuestionBankPage session={session} />
          ) : (
            <Navigate to="/login" replace />
          )
        }
      />
      <Route
        path="/documents"
        element={
          session.authenticated ? (
            <DocumentPage session={session} />
          ) : (
            <Navigate to="/login" replace />
          )
        }
      />
      <Route
        path="/ai-exams"
        element={session.authenticated ? <AiExamPage session={session} /> : <Navigate to="/login" replace />}
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
