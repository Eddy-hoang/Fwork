import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { Sparkles, ArrowRight } from "lucide-react";
import Button from "../ui/Button";

const EASE = [0.16, 1, 0.3, 1];

const avatars = ["AR", "MJ", "SK", "TL"];

const FinalCTA = () => (
  <section className="mx-auto max-w-6xl px-6 py-20 sm:py-28">
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: "-80px" }}
      transition={{ duration: 0.55, ease: EASE }}
      className="relative overflow-hidden rounded-[2rem] border border-line bg-surface px-6 py-16 text-center  sm:py-20"
    >
      {/* soft brand wash + faint geometry */}
      <div
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            "radial-gradient(40rem 22rem at 50% -20%, rgba(47,129,89,0.14), transparent 70%)",
        }}
      />
      <div className="pointer-events-none absolute -right-10 -top-10 h-40 w-40 rotate-12 rounded-[2rem] border border-line" />
      <div className="pointer-events-none absolute -bottom-12 -left-8 h-32 w-32 rotate-6 rounded-3xl border border-line" />

      <div className="relative">
        <span className="inline-flex items-center gap-2 rounded-full border border-brand-500/25 bg-brand-50 px-3.5 py-1.5 text-xs font-semibold text-brand-600">
          <Sparkles className="h-3.5 w-3.5" /> Start in seconds
        </span>
        <h2 className="mx-auto mt-6 max-w-2xl font-display text-[clamp(30px,4.5vw,50px)] font-semibold leading-[1.05] tracking-tight">
          Ready to <span className="text-gradient">ship faster?</span>
        </h2>
        <p className="mx-auto mt-5 max-w-xl text-lg leading-relaxed text-muted">
          Join teams turning goals into shipped work with an AI-native Kanban.
          Free to start.
        </p>
        <div className="mt-9 flex items-center justify-center gap-3">
          <Link to="/register">
            <Button size="lg" className="gap-2">
              Start for free <ArrowRight className="h-4 w-4" />
            </Button>
          </Link>
          <Link to="/login">
            <Button size="lg" variant="outline">
              Live demo
            </Button>
          </Link>
        </div>

        {/* social proof */}
        <div className="mt-8 flex items-center justify-center gap-3">
          <div className="flex -space-x-2.5">
            {avatars.map((initials) => (
              <span
                key={initials}
                className="brand-gradient grid h-8 w-8 place-items-center rounded-full text-[11px] font-semibold text-white ring-2 ring-surface"
              >
                {initials}
              </span>
            ))}
          </div>
          <p className="text-sm text-muted">
            Loved by <span className="font-semibold text-ink">2,500+</span>{" "}
            teams
          </p>
        </div>
      </div>
    </motion.div>
  </section>
);

export default FinalCTA;
