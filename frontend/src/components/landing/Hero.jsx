import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import {
  Sparkles,
  ArrowRight,
  TrendingUp,
  CheckCircle2,
  Users,
  Gauge,
  Star,
} from "lucide-react";

const EASE = [0.16, 1, 0.3, 1];

const aiTasks = [
  ["Design checkout UI", "#0ea5e9"],
  ["Integrate payments", "#e11d48"],
  ["Write API tests", "#2f8159"],
];

// mini kanban columns shown inside the app preview
const boardCols = [
  ["Todo", 8],
  ["In progress", 6],
  ["Review", 4],
  ["Done", 18],
];

const stats = [
  { icon: Users, value: "2,500+", label: "Teams planning with AI" },
  { icon: Gauge, value: "40%", label: "Faster sprint planning" },
  { icon: Star, value: "4.9", suffix: "/5.0", label: "Average team rating" },
];

const Hero = () => (
  <section className="px-3 pb-20 pt-4 sm:px-6 sm:pb-24 sm:pt-6">
    <div className="mx-auto max-w-[88rem]">
      {/* ── gradient hero panel ─────────────────────────────────────────── */}
      <div className="brand-gradient animate-gradient-pan relative overflow-hidden rounded-[2rem] px-6 pb-56 pt-16 text-center shadow-[var(--shadow-lift)] sm:rounded-[2.5rem] sm:pb-64 sm:pt-24">
        {/* soft abstract geometry + depth */}
        <div
          className="pointer-events-none absolute inset-0"
          aria-hidden="true"
        >
          {/* aurora waves — theme-green ribbons that drift behind the content */}
          <div className="absolute inset-0 mix-blend-screen">
            <motion.div
              className="absolute left-[-15%] top-[6%] h-56 w-[70%] rounded-[50%] blur-3xl"
              style={{ background: "radial-gradient(closest-side, rgba(124,214,164,0.85), transparent)" }}
              animate={{ x: ["-6%", "18%", "-6%"], y: [0, 32, 0], rotate: [-10, 8, -10] }}
              transition={{ duration: 18, repeat: Infinity, ease: "easeInOut" }}
            />
            <motion.div
              className="absolute right-[-15%] top-[22%] h-64 w-[72%] rounded-[50%] blur-3xl"
              style={{ background: "radial-gradient(closest-side, rgba(150,236,186,0.7), transparent)" }}
              animate={{ x: ["8%", "-18%", "8%"], y: [0, -28, 0], rotate: [8, -10, 8] }}
              transition={{ duration: 22, repeat: Infinity, ease: "easeInOut" }}
            />
            <motion.div
              className="absolute left-1/4 top-[42%] h-52 w-[58%] rounded-[50%] blur-3xl"
              style={{ background: "radial-gradient(closest-side, rgba(196,250,216,0.6), transparent)" }}
              animate={{ x: ["-12%", "16%", "-12%"], y: [0, 22, 0], scale: [1, 1.22, 1] }}
              transition={{ duration: 26, repeat: Infinity, ease: "easeInOut" }}
            />
            <motion.div
              className="absolute right-[6%] top-[2%] h-44 w-[40%] rounded-[50%] blur-3xl"
              style={{ background: "radial-gradient(closest-side, rgba(90,200,140,0.7), transparent)" }}
              animate={{ x: ["6%", "-12%", "6%"], y: [0, 18, 0], rotate: [6, -8, 6] }}
              transition={{ duration: 30, repeat: Infinity, ease: "easeInOut" }}
            />
          </div>

          <motion.div
            className="absolute -right-20 -top-24 h-80 w-80 rounded-full bg-white/12 blur-3xl"
            animate={{ x: [0, 26, 0], y: [0, 20, 0], scale: [1, 1.12, 1] }}
            transition={{ duration: 14, repeat: Infinity, ease: "easeInOut" }}
          />
          <motion.div
            className="absolute -bottom-16 -left-20 h-80 w-80 rounded-full bg-white/10 blur-3xl"
            animate={{ x: [0, -22, 0], y: [0, -18, 0], scale: [1, 1.1, 1] }}
            transition={{ duration: 17, repeat: Infinity, ease: "easeInOut" }}
          />
          <motion.div
            className="absolute left-1/2 top-1/3 h-[26rem] w-[26rem] -translate-x-1/2 rounded-full bg-white/[0.06] blur-3xl"
            animate={{ scale: [1, 1.18, 1], opacity: [0.6, 1, 0.6] }}
            transition={{ duration: 11, repeat: Infinity, ease: "easeInOut" }}
          />
          {/* faint rounded-top "skyline" silhouettes behind the showcase */}
          <div className="absolute inset-x-0 bottom-0 flex items-end justify-center gap-5 opacity-[0.07]">
            <div className="h-56 w-40 rounded-t-[2.5rem] bg-white" />
            <div className="h-72 w-44 rounded-t-[2.5rem] bg-white" />
            <div className="h-52 w-40 rounded-t-[2.5rem] bg-white" />
          </div>
          <div className="absolute -left-8 top-12 h-36 w-36 rotate-12 rounded-3xl border border-white/10" />
          <div className="absolute right-6 top-24 h-24 w-24 rotate-6 rounded-2xl border border-white/10" />
        </div>

        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, ease: EASE }}
          className="relative z-10 mx-auto max-w-3xl text-white"
        >
          <span className="inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-3.5 py-1.5 text-xs font-semibold text-white backdrop-blur">
            <Sparkles className="h-3.5 w-3.5" /> AI-native project management
          </span>
          <h1 className="mx-auto mt-6 max-w-2xl font-display text-[clamp(36px,5.4vw,62px)] font-medium leading-[1.04] tracking-tight">
            Plan smarter, ship faster with AI
          </h1>
          <p className="mx-auto mt-5 max-w-xl text-[15px] leading-relaxed text-white/80 sm:text-lg">
            Flowboard turns a one-line goal into a prioritized backlog and keeps
            your whole team moving in real time — so you plan less and ship
            more.
          </p>
          <div className="mt-9 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Link to="/register">
              <button className="inline-flex h-12 items-center justify-center gap-2 rounded-full bg-white px-7 text-sm font-semibold text-brand-700 shadow-[var(--shadow-soft)] transition-transform duration-200 hover:scale-[1.02] active:scale-[0.98]">
                Start now — it's free <ArrowRight className="h-4 w-4" />
              </button>
            </Link>
            <Link to="/login">
              <button className="inline-flex h-12 items-center justify-center rounded-full border border-white/30 bg-white/10 px-6 text-sm font-semibold text-white backdrop-blur transition-colors hover:bg-white/20">
                Live demo
              </button>
            </Link>
          </div>
          <p className="mt-5 text-xs text-white/65">
            Free for small teams · No credit card required
          </p>
        </motion.div>
      </div>

      {/* ── product showcase — cards overlapping the hero's bottom edge ──── */}
      <motion.div
        initial={{ opacity: 0, y: 28 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.2, ease: EASE }}
        className="relative z-10 mx-auto -mt-48 max-w-6xl px-2 sm:-mt-52"
      >
        <div className="grid items-end gap-4 sm:grid-cols-12">
          {/* left — sprint snapshot */}
          <div className="rounded-3xl border border-line bg-surface p-5 text-left shadow-[var(--shadow-lift)] sm:col-span-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase tracking-wide text-muted">
                This sprint
              </span>
              <span className="rounded-full bg-brand-50 px-2 py-0.5 text-[10px] font-semibold text-brand-700">
                On track
              </span>
            </div>
            <p className="mt-3 font-display text-3xl font-semibold tabular tracking-tight">
              24
            </p>
            <p className="text-xs text-faint">tasks in flight</p>
            <div className="mt-4 grid grid-cols-2 gap-2">
              <div className="rounded-xl bg-surface-2 p-2.5">
                <p className="font-display text-base font-semibold tabular">
                  18
                </p>
                <p className="text-[10px] text-muted">Done</p>
              </div>
              <div className="rounded-xl bg-surface-2 p-2.5">
                <p className="font-display text-base font-semibold tabular">
                  6
                </p>
                <p className="text-[10px] text-muted">In progress</p>
              </div>
            </div>
          </div>

          {/* center — polished app preview (the centerpiece) */}
          <div className="overflow-hidden rounded-3xl border border-line bg-surface text-left shadow-[var(--shadow-lift)] sm:col-span-6 sm:-translate-y-8">
            {/* window chrome */}
            <div className="flex items-center gap-2 border-b border-line px-4 py-3">
              <span className="flex gap-1.5">
                <span className="h-2.5 w-2.5 rounded-full bg-elevated" />
                <span className="h-2.5 w-2.5 rounded-full bg-elevated" />
                <span className="h-2.5 w-2.5 rounded-full bg-elevated" />
              </span>
              <span className="ml-2 text-xs font-medium text-muted">
                Flowboard · Sprint 14
              </span>
              <span className="ml-auto inline-flex items-center gap-1 rounded-full bg-brand-50 px-2 py-0.5 text-[10px] font-semibold text-brand-700">
                <span className="h-1.5 w-1.5 rounded-full bg-brand-500" /> Live
              </span>
            </div>

            <div className="p-5">
              {/* velocity header */}
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-faint">
                    Sprint velocity
                  </p>
                  <div className="mt-1.5 flex items-baseline gap-2">
                    <span className="font-display text-[28px] font-semibold tabular leading-none tracking-tight">
                      87%
                    </span>
                    <span className="inline-flex items-center gap-0.5 rounded-full bg-brand-50 px-2 py-0.5 text-[11px] font-semibold text-brand-700">
                      <TrendingUp className="h-3 w-3" /> +12%
                    </span>
                  </div>
                </div>
                <span className="grid h-9 w-9 place-items-center rounded-xl bg-brand-50 text-brand-600">
                  <TrendingUp className="h-[18px] w-[18px]" />
                </span>
              </div>

              {/* velocity chart */}
              <svg
                viewBox="0 0 480 120"
                preserveAspectRatio="none"
                className="mt-4 h-28 w-full"
              >
                <defs>
                  <linearGradient id="hero-spark" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#2f8159" stopOpacity="0.26" />
                    <stop offset="100%" stopColor="#2f8159" stopOpacity="0" />
                  </linearGradient>
                </defs>
                <path
                  d="M0,92 C52,82 78,40 120,52 C166,66 188,22 236,36 C284,50 308,18 356,28 C404,38 436,60 480,24"
                  fill="none"
                  stroke="#2f8159"
                  strokeWidth="3"
                  strokeLinecap="round"
                />
                <path
                  d="M0,92 C52,82 78,40 120,52 C166,66 188,22 236,36 C284,50 308,18 356,28 C404,38 436,60 480,24 L480,120 L0,120 Z"
                  fill="url(#hero-spark)"
                />
              </svg>

              {/* mini board columns */}
              <div className="mt-4 grid grid-cols-4 gap-2">
                {boardCols.map(([name, count], i) => (
                  <div key={name} className="rounded-xl bg-surface-2 p-2.5">
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] font-medium text-muted">
                        {name}
                      </span>
                    </div>
                    <p className="mt-1 font-display text-lg font-semibold tabular tracking-tight">
                      {count}
                    </p>
                    <div className="mt-1.5 h-1 rounded-full bg-elevated">
                      <div
                        className="h-1 rounded-full bg-brand-500"
                        style={{ width: `${[40, 55, 30, 100][i]}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* right — AI generated */}
          <div className="rounded-3xl border border-line bg-surface p-5 text-left shadow-[var(--shadow-lift)] sm:col-span-3">
            <div className="flex items-center gap-2">
              <span className="brand-gradient grid h-7 w-7 place-items-center rounded-lg text-white">
                <Sparkles className="h-4 w-4" />
              </span>
              <span className="text-sm font-semibold tracking-tight">
                AI generated
              </span>
            </div>
            <div className="mt-3 space-y-2">
              {aiTasks.map(([title, color]) => (
                <div
                  key={title}
                  className="flex items-center gap-2 rounded-xl border border-line bg-surface-2/60 px-2.5 py-2"
                >
                  <span
                    className="h-1.5 w-1.5 shrink-0 rounded-full"
                    style={{ backgroundColor: color }}
                  />
                  <span className="flex-1 truncate text-xs font-medium text-ink">
                    {title}
                  </span>
                  <CheckCircle2 className="h-3.5 w-3.5 text-brand-500" />
                </div>
              ))}
            </div>
          </div>
        </div>
      </motion.div>

      {/* ── stats section ───────────────────────────────────────────────── */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-80px" }}
        transition={{ duration: 0.55, ease: EASE }}
        className="mx-auto mt-20 max-w-6xl px-2 sm:mt-24"
      >
        <div className="flex flex-col items-start justify-between gap-5 sm:flex-row sm:items-end">
          <h2 className="max-w-xl font-display text-[clamp(26px,3.6vw,40px)] font-semibold leading-[1.08] tracking-tight">
            Real results for teams that{" "}
            <span className="text-gradient">ship faster</span>
          </h2>
          <Link to="/register">
            <button className="brand-gradient inline-flex h-11 items-center justify-center gap-2 rounded-full px-6 text-sm font-semibold text-white shadow-[var(--shadow-brand)] transition-transform duration-200 hover:scale-[1.02] active:scale-[0.98]">
              Start free trial <ArrowRight className="h-4 w-4" />
            </button>
          </Link>
        </div>

        <div className="mt-8 grid gap-4 sm:grid-cols-3">
          {stats.map((s) => (
            <div
              key={s.label}
              className="flex items-center gap-4 rounded-3xl border border-line bg-surface p-5 shadow-[var(--shadow-card)] transition-shadow duration-300 hover:shadow-[var(--shadow-soft)]"
            >
              <span className="brand-gradient grid h-12 w-12 shrink-0 place-items-center rounded-2xl text-white shadow-[var(--shadow-brand)]">
                <s.icon className="h-[22px] w-[22px]" />
              </span>
              <div>
                <p className="font-display text-2xl font-semibold tabular leading-none tracking-tight">
                  {s.value}
                  {s.suffix && (
                    <span className="text-base font-medium text-faint">
                      {s.suffix}
                    </span>
                  )}
                </p>
                <p className="mt-1.5 text-sm text-muted">{s.label}</p>
              </div>
            </div>
          ))}
        </div>
      </motion.div>
    </div>
  </section>
);

export default Hero;
