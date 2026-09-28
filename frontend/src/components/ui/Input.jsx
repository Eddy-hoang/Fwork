import { ChevronDown } from "lucide-react";
import { cn } from "../../lib/utils";

const labelCls = "block text-xs font-medium tracking-tight text-zinc-700 dark:text-zinc-300";
const inputCls =
  "w-full rounded-md border border-zinc-200 bg-white px-3 py-2 text-xs text-zinc-900 shadow-xs placeholder:text-zinc-400 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-950 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-50 dark:placeholder:text-zinc-500 dark:focus-visible:ring-zinc-300 transition-colors";

export const Input = ({ label, error, className, id, ...props }) => (
  <div className="space-y-1.5">
    {label && (
      <label htmlFor={id} className={labelCls}>
        {label}
      </label>
    )}
    <input
      id={id}
      className={cn(inputCls, error && "!border-red-500 focus-visible:!ring-red-500", className)}
      {...props}
    />
    {error && <p className="text-xs text-red-500">{error}</p>}
  </div>
);

export const Textarea = ({ label, error, className, id, rows = 4, ...props }) => (
  <div className="space-y-1.5">
    {label && (
      <label htmlFor={id} className={labelCls}>
        {label}
      </label>
    )}
    <textarea
      id={id}
      rows={rows}
      className={cn(inputCls, "resize-none", error && "!border-red-500 focus-visible:!ring-red-500", className)}
      {...props}
    />
    {error && <p className="text-xs text-red-500">{error}</p>}
  </div>
);

export const Select = ({ label, className, id, children, ...props }) => (
  <div className="space-y-1.5">
    {label && (
      <label htmlFor={id} className={labelCls}>
        {label}
      </label>
    )}
    <div className="relative">
      <select
        id={id}
        className={cn(inputCls, "cursor-pointer appearance-none pr-9", className)}
        {...props}
      >
        {children}
      </select>
      <ChevronDown className="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-zinc-400 dark:text-zinc-500" />
    </div>
  </div>
);

export const FilterSelect = ({ className, children, ...props }) => (
  <div className="relative">
    <select
      className={cn(
        "h-8 cursor-pointer appearance-none rounded-md border border-zinc-200 bg-white pl-3 pr-8 text-xs font-medium text-zinc-700 shadow-xs outline-none transition-colors hover:bg-zinc-50 focus:border-zinc-400 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-300 dark:hover:bg-zinc-850 dark:focus:border-zinc-700",
        className
      )}
      {...props}
    >
      {children}
    </select>
    <ChevronDown className="pointer-events-none absolute right-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-zinc-400 dark:text-zinc-500" />
  </div>
);
