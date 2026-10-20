/**
 * Fwork Logo Component
 * Uses the official Fwork stylized logomark icon.
 */
export default function Logo({
  size = "md",
  showText = true,
  className = "",
  textClassName = "",
}) {
  const sizeMap = {
    sm: { box: "h-7 w-7 rounded-lg", text: "text-base" },
    md: { box: "h-8.5 w-8.5 rounded-xl", text: "text-lg" },
    lg: { box: "h-10 w-10 rounded-xl", text: "text-[17px]" },
    xl: { box: "h-12 w-12 rounded-2xl", text: "text-xl" },
  };

  const currentSize = sizeMap[size] || sizeMap.md;

  return (
    <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      <div
        className={`shrink-0 overflow-hidden ${currentSize.box} shadow-[var(--shadow-brand)] transition-transform duration-200 hover:scale-105 active:scale-95`}
      >
        <img
          src="/icon.png"
          alt="Fwork"
          className="h-full w-full object-cover select-none pointer-events-none"
        />
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
