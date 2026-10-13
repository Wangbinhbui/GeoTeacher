import type { InputHTMLAttributes } from 'react'
import { cn } from '../../lib/utils'

export function Input({ className, ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className={cn(
        'h-12 w-full rounded-xl border border-stone-200 bg-white px-4 text-[15px] text-stone-900 shadow-sm transition placeholder:text-stone-400 hover:border-stone-300 focus:border-emerald-700 focus:ring-4 focus:ring-emerald-800/10 focus:outline-none',
        className,
      )}
      {...props}
    />
  )
}
