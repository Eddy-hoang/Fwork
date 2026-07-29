import { motion } from "framer-motion";

const EASE = [0.16, 1, 0.3, 1];

const SectionHeading = ({ eyebrow, title, sub }) => (
  <motion.div
    initial={{ opacity: 0, y: 14 }}
    whileInView={{ opacity: 1, y: 0 }}
    viewport={{ once: true, margin: "-80px" }}
    transition={{ duration: 0.5, ease: EASE }}
    className="mx-auto max-w-2xl text-center"
  >
    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-brand-600">{eyebrow}</p>
    <h2 className="mt-3 font-display text-[clamp(28px,4vw,44px)] font-semibold leading-[1.08] tracking-tight">
      {title}
    </h2>
    {sub && <p className="mt-4 text-lg leading-relaxed text-muted">{sub}</p>}
  </motion.div>
);

export default SectionHeading;
