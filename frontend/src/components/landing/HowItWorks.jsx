import { motion } from "framer-motion";
import { Sparkles, LayoutGrid, Rocket } from "lucide-react";
import SectionHeading from "./SectionHeading";

const EASE = [0.16, 1, 0.3, 1];

const steps = [
  { icon: LayoutGrid, title: "Create a board", desc: "Spin up a board pre-built with Todo, In Progress, Review and Done — in one click." },
  { icon: Sparkles, title: "Let AI draft your backlog", desc: "Describe your goal and watch a prioritized set of tasks appear, ready to refine." },
  { icon: Rocket, title: "Collaborate & ship", desc: "Assign, drag and track in real time while AI keeps everyone in sync." },
];

const HowItWorks = () => (
  <section id="how-it-works" className="mx-auto max-w-6xl scroll-mt-24 px-6 py-20 sm:py-28">
    <SectionHeading eyebrow="How it works" title="From goal to shipped in three steps" />

    <div className="relative mt-14">
      {/* connector path linking the three steps (desktop) */}
      <div className="pointer-events-none absolute inset-x-[16%] top-[3.75rem] hidden h-px bg-gradient-to-r from-transparent via-brand-300/60 to-transparent md:block" />

      <div className="grid gap-5 md:grid-cols-3">
        {steps.map((s, i) => (
          <motion.div
            key={s.title}
            initial={{ opacity: 0, y: 16 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, margin: "-60px" }}
            transition={{ duration: 0.5, delay: i * 0.1, ease: EASE }}
            className="group relative overflow-hidden rounded-3xl border border-line bg-surface p-7 shadow-[var(--shadow-card)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[var(--shadow-soft)]"
          >
            {/* ghost step number */}
            <span className="pointer-events-none absolute -right-2 -top-4 select-none font-display text-[104px] font-bold leading-none text-brand-50 transition-colors duration-300 group-hover:text-brand-100/70">
              {i + 1}
            </span>

            <div className="relative">
              <div className="brand-gradient flex h-14 w-14 items-center justify-center rounded-2xl text-white shadow-[var(--shadow-brand)] transition-transform duration-300 group-hover:scale-[1.06]">
                <s.icon className="h-6 w-6" />
              </div>
              <p className="mt-5 text-[11px] font-semibold uppercase tracking-[0.18em] text-brand-600">
                Step {String(i + 1).padStart(2, "0")}
              </p>
              <h3 className="mt-1.5 font-display text-lg font-semibold tracking-tight">{s.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-muted">{s.desc}</p>
            </div>
          </motion.div>
        ))}
      </div>
    </div>
  </section>
);

export default HowItWorks;
