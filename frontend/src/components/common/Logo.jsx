import { Workflow } from "lucide-react";

/**
 * Fwork Logo Component
 * Uses Lucide's `Workflow` icon with chromatic depth, layered glass / gradient surfaces.
 */
export default function Logo({
  size = "md",
  showText = true,
  className = "",
  textClassName = "",
}) {
  const sizeMap = {
    sm: { box: "h-8 w-8 rounded-xl", icon: "h-4 w-4", text: "text-base" },
    md: { box: "h-9 w-9 rounded-xl", icon: "h-4.5 w-4.5", text: "text-lg" },
    lg: { box: "h-10 w-10 rounded-2xl", icon: "h-5 w-5", text: "text-[17px]" },
    xl: { box: "h-11 w-11 rounded-2xl", icon: "h-6 w-6", text: "text-xl" },
  };

  const currentSize = sizeMap[size] || sizeMap.md;

  return (
    <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      <div
        className={`brand-gradient grid shrink-0 place-items-center ${currentSize.box} shadow-[var(--shadow-brand)] transition-transform duration-200 hover:scale-105 active:scale-95`}
      >
        <Workflow className={`${currentSize.icon} text-white stroke-[2.3]`} />
      </div>
      {showText && (
        <span
          className={`font-display font-bold tracking-tight text-ink ${currentSize.text} ${textClassName}`}
        >
          Fwork
        </span>
      )}
    </div>
  );
}
