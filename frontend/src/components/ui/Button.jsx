import { cn } from "../../lib/utils";
import { Loader2 } from "lucide-react";

const variants = {
  primary:
    "brand-gradient text-white shadow-[var(--shadow-brand)] hover:brightness-[1.05] hover:shadow-[0_12px_28px_rgba(103,80,164,0.32)] active:scale-[0.98]",
  secondary:
    "bg-surface hover:bg-surface-2 text-ink border border-line shadow-[var(--shadow-card)] hover:shadow-[var(--shadow-soft)] hover:border-brand-500/30 active:scale-[0.98]",
  ghost: "hover:bg-surface-2 text-muted hover:text-ink active:scale-[0.98]",
  danger:
    "bg-priority-urgent text-white shadow-[0_8px_20px_rgba(225,29,72,0.28)] hover:brightness-[1.06] active:scale-[0.98]",
  outline: "border border-line bg-surface hover:border-brand-300 dark:hover:border-brand-400 hover:bg-surface-2 text-ink shadow-[var(--shadow-card)] active:scale-[0.98]",
  soft: "bg-brand-50 text-brand-700 dark:text-brand-300 hover:bg-brand-100 dark:hover:bg-brand-100 border border-brand-100/60 dark:border-brand-300/20 active:scale-[0.98]",
};

const sizes = {
  sm: "h-8 px-4 text-xs gap-1.5",
  md: "h-10 px-5 text-sm gap-2",
  lg: "h-12 px-7 text-[15px] gap-2.5",
  icon: "h-10 w-10",
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
      "inline-flex select-none items-center justify-center whitespace-nowrap rounded-full font-semibold transition-all duration-200 ease-[var(--ease-spring)] focus-ring disabled:opacity-50 disabled:pointer-events-none active:scale-[0.97]",
      variants[variant],
      sizes[size],
      className
    )}
    disabled={disabled || loading}
    {...props}
  >
    {loading && <Loader2 className="h-4 w-4 animate-spin" />}
    {children}
  </button>
);

export default Button;
