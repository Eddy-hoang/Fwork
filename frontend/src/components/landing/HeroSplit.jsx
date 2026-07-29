import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { Sparkles, ArrowRight } from "lucide-react";
import Button from "../ui/Button";
import KanbanDemo from "./KanbanDemo";

const EASE = [0.16, 1, 0.3, 1];

const HeroSplit = () => (
  <section className="px-4 pb-20 pt-10 sm:px-6 sm:pb-28 sm:pt-16">
    <div className="mx-auto grid max-w-7xl items-center gap-10 lg:grid-cols-2 lg:gap-12">
      {/* left — copy */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, ease: EASE }}
        className="text-center lg:text-left"
      >
        <span className="inline-flex items-center gap-2 rounded-full border border-brand-500/25 bg-brand-50 px-3.5 py-1.5 text-xs font-semibold text-brand-600">
          <Sparkles className="h-3.5 w-3.5" /> AI-native project management
        </span>
        <h1 className="mt-6 font-display text-[clamp(36px,5vw,60px)] font-semibold leading-[1.04] tracking-tight">
          Plan smarter, ship faster with{" "}
          <span className="text-gradient">AI</span>
        </h1>
        <p className="mx-auto mt-5 max-w-xl text-[15px] leading-relaxed text-muted sm:text-lg lg:mx-0">
          Flowboard turns a one-line goal into a prioritized backlog and keeps
          your whole team moving in real time — so you plan less and ship more.
        </p>
        <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row lg:justify-start">
          <Link to="/register">
            <Button size="lg" className="gap-2">
              Start now — it's free <ArrowRight className="h-4 w-4" />
            </Button>
          </Link>
          <Link to="/login">
            <Button size="lg" variant="outline">
              Live demo
            </Button>
          </Link>
        </div>
        <p className="mt-5 text-center text-xs text-faint lg:text-left">
          Free for small teams · No credit card required
        </p>
      </motion.div>

      {/* right — light panel with the live kanban board */}
      <motion.div
        initial={{ opacity: 0, y: 24 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, delay: 0.15, ease: EASE }}
        className="relative overflow-hidden rounded-[2rem] border border-line bg-bg p-6 sm:p-8"
      >
        {/* soft brand wash */}
        <div
          className="pointer-events-none absolute inset-0"
          style={{
            background:
              "radial-gradient(36rem 20rem at 80% -10%, rgba(47,129,89,0.12), transparent 70%)",
          }}
        />

        <div className="relative z-10 flex flex-col items-center">
          <KanbanDemo className="w-full" theme="light" />
          <div className="mt-6 inline-flex items-center gap-2 rounded-full border border-line bg-surface px-3.5 py-1.5 text-xs font-semibold text-muted shadow-[var(--shadow-card)]">
            <Sparkles className="h-3.5 w-3.5 text-brand-500" /> AI is
            prioritizing your backlog
          </div>
        </div>
      </motion.div>
    </div>
  </section>
);

export default HeroSplit;
