import React from "react";

/**
 * Fwork Logo Component
 * - Automatically switches between Light and Dark theme logomarks
 * - Typography designed with Taste-Skill geometric grotesque font system (Outfit / Plus Jakarta Sans)
 * - Single authoritative, cohesive wordmark in solid ink / crisp white
 */
export default function Logo({
  size = "md",
  showText = true,
  theme = "auto", // "auto" | "light" | "dark"
  className = "",
  textClassName = "",
}) {
  const sizeMap = {
    sm: { box: "h-7 w-7", text: "text-base" },
    md: { box: "h-8.5 w-8.5", text: "text-lg" },
    lg: { box: "h-10 w-10", text: "text-[19px]" },
    xl: { box: "h-12 w-12", text: "text-2xl" },
  };

  const currentSize = sizeMap[size] || sizeMap.md;

  const renderIcon = () => {
    if (theme === "dark") {
      return (
        <img
          src="/icon-dark.png"
          alt="Fwork"
          className="h-full w-full object-contain select-none pointer-events-none drop-shadow-[0_2px_8px_rgba(139,92,246,0.3)]"
        />
      );
    }
    if (theme === "light") {
      return (
        <img
          src="/icon-light.png"
          alt="Fwork"
          className="h-full w-full object-contain select-none pointer-events-none drop-shadow-sm"
        />
      );
    }
    // auto theme: responsive via Tailwind dark: variant
    return (
      <>
        <img
          src="/icon-light.png"
          alt="Fwork"
          className="h-full w-full object-contain select-none pointer-events-none dark:hidden drop-shadow-sm"
        />
        <img
          src="/icon-dark.png"
          alt="Fwork"
          className="h-full w-full object-contain select-none pointer-events-none hidden dark:block drop-shadow-[0_2px_8px_rgba(139,92,246,0.3)]"
        />
      </>
    );
  };

  const textColor =
    theme === "dark"
      ? "text-slate-50"
      : theme === "light"
      ? "text-slate-900"
      : "text-ink dark:text-slate-50";

  return (
    <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      <div
        className={`relative shrink-0 ${currentSize.box} flex items-center justify-center transition-transform duration-200 hover:scale-105 active:scale-95`}
      >
        {renderIcon()}
      </div>
      {showText && (
        <span
          className={`font-['Outfit','Plus_Jakarta_Sans',sans-serif] font-black tracking-[-0.03em] leading-none ${textColor} ${currentSize.text} ${textClassName}`}
        >
          Fwork
        </span>
      )}
    </div>
  );
}
