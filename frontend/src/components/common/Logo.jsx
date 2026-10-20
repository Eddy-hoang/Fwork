import React from "react";

/**
 * Fwork Logo Component
 * - Automatically switches between Light and Dark theme logomarks
 * - Typography designed with Taste-Skill geometric grotesque font system (Outfit / Plus Jakarta Sans)
 * - Signature gradient 'F' + high-contrast 'work' wordmark
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
          className="h-full w-full object-contain select-none pointer-events-none drop-shadow-[0_2px_8px_rgba(139,92,246,0.35)]"
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
    // auto theme: switches via Tailwind's dark class
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
          className="h-full w-full object-contain select-none pointer-events-none hidden dark:block drop-shadow-[0_2px_8px_rgba(139,92,246,0.35)]"
        />
      </>
    );
  };

  const textColor =
    theme === "dark"
      ? "text-slate-100"
      : theme === "light"
      ? "text-slate-900"
      : "text-slate-900 dark:text-slate-100";

  return (
    <div className={`inline-flex items-center gap-2.5 select-none ${className}`}>
      <div
        className={`relative shrink-0 ${currentSize.box} flex items-center justify-center transition-transform duration-200 hover:scale-105 active:scale-95`}
      >
        {renderIcon()}
      </div>
      {showText && (
        <span
          className={`font-['Outfit','Plus_Jakarta_Sans',sans-serif] font-black tracking-[-0.035em] leading-none ${currentSize.text} ${textClassName}`}
        >
          <span className="bg-gradient-to-tr from-blue-600 via-indigo-500 to-fuchsia-500 dark:from-blue-400 dark:via-purple-400 dark:to-pink-400 bg-clip-text text-transparent">
            F
          </span>
          <span className={`${textColor} font-extrabold tracking-[-0.035em]`}>
            work
          </span>
        </span>
      )}
    </div>
  );
}
