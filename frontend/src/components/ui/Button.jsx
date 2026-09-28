import { cn } from "../../lib/utils";
import { Loader2 } from "lucide-react";

const variants = {
  primary:
    "bg-zinc-900 text-zinc-50 hover:bg-zinc-800 dark:bg-zinc-50 dark:text-zinc-900 dark:hover:bg-zinc-200 shadow-xs font-medium",
  secondary:
    "bg-zinc-100 text-zinc-900 hover:bg-zinc-200 dark:bg-zinc-800 dark:text-zinc-50 dark:hover:bg-zinc-700 border border-zinc-200 dark:border-zinc-700/60 font-medium",
  ghost:
    "hover:bg-zinc-100 text-zinc-600 hover:text-zinc-900 dark:text-zinc-400 dark:hover:bg-zinc-800 dark:hover:text-zinc-50 font-medium",
  danger:
    "bg-red-600 text-white hover:bg-red-700 dark:bg-red-900 dark:text-red-100 dark:hover:bg-red-800 shadow-xs font-medium",
  outline:
    "border border-zinc-200 bg-white hover:bg-zinc-100 dark:border-zinc-800 dark:bg-zinc-900 dark:hover:bg-zinc-800 text-zinc-900 dark:text-zinc-50 font-medium shadow-xs",
  soft:
    "bg-indigo-50 text-indigo-700 hover:bg-indigo-100 dark:bg-indigo-950/60 dark:text-indigo-300 dark:hover:bg-indigo-900/80 font-medium",
};

const sizes = {
  sm: "h-8 px-3 text-xs gap-1.5",
  md: "h-9 px-4 text-xs font-medium gap-2",
  lg: "h-11 px-6 text-sm font-medium gap-2",
  icon: "h-9 w-9",
  iconSm: "h-8 w-8",
};

const Button = ({
  children,
  variant = "primary",
  size = "md",
  loading = false,
  className,
  disabled,
  ...props
}) => (
  <button
    className={cn(
      "inline-flex select-none items-center justify-center whitespace-nowrap rounded-md font-sans transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-950 dark:focus-visible:ring-zinc-300 disabled:opacity-50 disabled:pointer-events-none active:scale-[0.98]",
      variants[variant],
      sizes[size],
      className
    )}
    disabled={disabled || loading}
    {...props}
  >
    {loading && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
    {children}
  </button>
);

export default Button;
