import { motion } from "framer-motion";
import {
  Sparkles, Users, LayoutGrid, GitBranch, FileText, Command,
} from "lucide-react";
import SectionHeading from "./SectionHeading";
import DrawIcon from "./DrawIcon";

const EASE = [0.16, 1, 0.3, 1];

// the flagship capability — given the large gradient tile (the section's signature)
const flagship = {
  icon: Sparkles,
  title: "AI Task Generation",
  desc: "Describe a goal in one line and watch a prioritized, ready-to-refine backlog appear in seconds.",
};

// supporting capabilities — quiet, uniform cards
const features = [
  { icon: GitBranch, title: "AI Task Breakdown", desc: "Expand any complex task into clear, estimable subtasks with one click." },
  { icon: FileText, title: "AI Sprint Summaries", desc: "Concise progress reports highlighting what's done, pending and at risk." },
  { icon: Users, title: "Real-Time Collaboration", desc: "See teammates' moves instantly with live presence and an activity feed." },
  { icon: LayoutGrid, title: "Drag & Drop Boards", desc: "Smooth, fast kanban with Todo, In Progress, Review and Done." },
  { icon: Command, title: "Command Menu", desc: "Press ⌘K to jump anywhere, search tasks, or spin up a board." },
];

// mock tasks shown inside the flagship tile
const genTasks = [
  ["Set up referral rewards", "#0ea5e9"],
  ["Build the invite flow", "#2f8159"],
  ["Track conversions", "#d97706"],
];

const Features = () => (
  <section id="features" className="mx-auto max-w-6xl scroll-mt-24 px-6 py-20 sm:py-28">
    <SectionHeading
      eyebrow="Features"
      title="Everything you need to plan and ship"
      sub="A focused toolkit that removes busywork so your team can move from idea to done, faster."
    />

    <div className="mt-14 grid gap-5 sm:grid-cols-2 lg:grid-cols-3 lg:auto-rows-fr">
      {/* ── flagship tile ─────────────────────────────────────────────── */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-60px" }}
        transition={{ duration: 0.55, ease: EASE }}
        className="brand-gradient group relative flex flex-col overflow-hidden rounded-3xl p-8 text-white shadow-[var(--shadow-lift)] sm:col-span-2 lg:row-span-2"
      >
        {/* ambient glows */}
        <div className="pointer-events-none absolute -right-16 -top-20 h-64 w-64 rounded-full bg-white/15 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-20 -left-12 h-56 w-56 rounded-full bg-white/10 blur-3xl" />

        <div className="relative flex h-full flex-col">
          <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-white/70">Built-in AI</p>
          <div className="mt-4 flex items-center gap-3">
            <span className="grid h-12 w-12 place-items-center rounded-2xl bg-white/15 ring-1 ring-inset ring-white/20">
              <DrawIcon
                icon={flagship.icon}
                className="h-[22px] w-[22px]"
                delay={0.2}
                baseClassName="text-white/35"
                traceColor="rgba(255,255,255,0.95)"
              />
            </span>
            <h3 className="font-display text-2xl font-semibold tracking-tight">{flagship.title}</h3>
          </div>
          <p className="mt-4 max-w-md leading-relaxed text-white/80">{flagship.desc}</p>

          {/* live mock — goal → generated tasks */}
          <div className="mt-auto pt-8">
            <div className="rounded-2xl border border-white/15 bg-white/10 p-4 backdrop-blur-md">
              <div className="flex items-center gap-2.5 rounded-xl border border-white/10 bg-white/10 px-3 py-2">
                <Sparkles className="h-4 w-4 shrink-0 text-white/80" />
                <span className="flex-1 truncate text-sm text-white/90">Launch a referral program</span>
                <span className="shrink-0 rounded-lg bg-white px-2.5 py-1 text-xs font-semibold text-brand-700">Generate</span>
              </div>
              <div className="mt-3 space-y-2">
                {genTasks.map(([title, color], i) => (
                  <motion.div
                    key={title}
                    initial={{ opacity: 0, x: 12 }}
                    whileInView={{ opacity: 1, x: 0 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.4, delay: 0.3 + i * 0.12, ease: EASE }}
                    className="flex items-center gap-2.5 rounded-xl border border-white/10 bg-white/[0.07] px-3 py-2.5"
                  >
                    <span className="h-2 w-2 shrink-0 rounded-full" style={{ backgroundColor: color }} />
                    <span className="flex-1 truncate text-sm text-white/90">{title}</span>
                  </motion.div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </motion.div>

      {/* ── supporting cards ──────────────────────────────────────────── */}
      {features.map((f, i) => (
        <motion.div
          key={f.title}
          initial={{ opacity: 0, y: 14 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true, margin: "-60px" }}
          transition={{ duration: 0.45, delay: 0.08 + (i % 3) * 0.06, ease: EASE }}
          className="group relative overflow-hidden rounded-3xl border border-line bg-surface p-7 shadow-[var(--shadow-card)] transition-all duration-300 hover:-translate-y-1 hover:border-brand-100 hover:shadow-[var(--shadow-soft)]"
        >
          {/* glow that blooms from behind the icon on hover */}
          <div className="pointer-events-none absolute -left-6 -top-6 h-28 w-28 rounded-full bg-brand-500/[0.08] opacity-0 blur-2xl transition-opacity duration-500 group-hover:opacity-100" />

          <div className="relative">
            <div className="mb-5 grid h-12 w-12 place-items-center rounded-2xl bg-brand-50 ring-1 ring-inset ring-brand-100/70">
              <DrawIcon icon={f.icon} className="h-[22px] w-[22px]" delay={0.1 + (i % 3) * 0.12} />
            </div>
            <h3 className="font-display text-base font-semibold tracking-tight">{f.title}</h3>
            <p className="mt-2 text-sm leading-relaxed text-muted">{f.desc}</p>
          </div>
        </motion.div>
      ))}
    </div>
  </section>
);

export default Features;
