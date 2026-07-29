import { useEffect, useRef, useState } from "react";
import { motion, LayoutGroup } from "framer-motion";

const CARDS = {
  1: { title: "Design checkout UI", color: "#0ea5e9" },
  2: { title: "Integrate Stripe payments", color: "#d97706" },
  3: { title: "Real-time presence", color: "#e11d48" },
  4: { title: "Ship sprint summary", color: "#2f8159" },
  5: { title: "Write API tests", color: "#8b5cf6" },
  6: { title: "Auth & onboarding", color: "#0ea5e9" },
};

const COLUMNS = ["Todo", "In progress", "Done"];

const initialBoard = {
  Todo: [3, 6],
  "In progress": [1, 2],
  Done: [4, 5],
};

const spring = { type: "spring", stiffness: 420, damping: 34 };

const themes = {
  dark: {
    column: "bg-white/[0.08] ring-1 ring-inset ring-white/10 backdrop-blur-sm",
    header: "text-white/85",
    count: "bg-white/15 text-white/75",
  },
  light: {
    column: "bg-surface-2 ring-1 ring-inset ring-line",
    header: "text-ink",
    count: "bg-elevated text-muted",
  },
};

/**
 * A self-animating mini kanban board: every couple of seconds one card slides
 * to the next column (todo → doing → done → …) with a "picked up" lift, using
 * framer-motion shared layout so the move glides across columns.
 * Styled for placement on a dark / brand-gradient surface.
 */
const KanbanDemo = ({ className = "", theme = "dark" }) => {
  const t = themes[theme] ?? themes.dark;
  const [board, setBoard] = useState(initialBoard);
  const [movingId, setMovingId] = useState(null);
  const boardRef = useRef(initialBoard);
  const sourceRef = useRef(0);

  useEffect(() => {
    const id = setInterval(() => {
      // compute the move outside setBoard so the updater stays pure
      const prev = boardRef.current;
      let s = sourceRef.current;
      let guard = 0;
      while (prev[COLUMNS[s]].length === 0 && guard < COLUMNS.length) {
        s = (s + 1) % COLUMNS.length;
        guard += 1;
      }
      const src = COLUMNS[s];
      if (prev[src].length === 0) return;
      const dst = COLUMNS[(s + 1) % COLUMNS.length];
      const moving = prev[src][0];
      sourceRef.current = (s + 1) % COLUMNS.length;
      const next = {
        ...prev,
        [src]: prev[src].slice(1),
        [dst]: [...prev[dst], moving],
      };
      boardRef.current = next;
      setBoard(next);
      setMovingId(moving);
    }, 2200);
    return () => clearInterval(id);
  }, []);

  useEffect(() => {
    if (movingId == null) return undefined;
    const t = setTimeout(() => setMovingId(null), 700);
    return () => clearTimeout(t);
  }, [movingId]);

  return (
    <LayoutGroup>
      <div className={`grid grid-cols-3 gap-3.5 ${className}`}>
        {COLUMNS.map((col) => (
          <div key={col} className={`rounded-2xl p-3 ${t.column}`}>
            <div className="mb-2.5 flex items-center justify-between px-1.5 pt-0.5">
              <span className={`text-xs font-semibold ${t.header}`}>{col}</span>
              <span
                className={`grid h-5 min-w-[20px] place-items-center rounded-full px-1.5 text-[10px] font-semibold ${t.count}`}
              >
                {board[col].length}
              </span>
            </div>
            <div className="flex h-[376px] flex-col gap-2.5">
              {board[col].map((id) => {
                const card = CARDS[id];
                const isMoving = id === movingId;
                return (
                  <motion.div layout layoutId={`card-${id}`} key={id} transition={spring}>
                    <motion.div
                      animate={isMoving ? { scale: 1.06, rotate: -3 } : { scale: 1, rotate: 0 }}
                      transition={{ duration: 0.3, ease: "easeOut" }}
                      className={`rounded-xl border border-line bg-surface p-3 text-left ${
                        isMoving
                          ? "relative z-10 shadow-[var(--shadow-lift)] ring-2 ring-brand-300"
                          : "shadow-[var(--shadow-card)]"
                      }`}
                    >
                      <div className="flex items-start gap-2">
                        <span
                          className="mt-1 h-2 w-2 shrink-0 rounded-full"
                          style={{ backgroundColor: card.color }}
                        />
                        <span className="line-clamp-2 text-[13px] font-medium leading-snug text-ink">
                          {card.title}
                        </span>
                      </div>
                      <div className="mt-3 flex items-center justify-between">
                        <span className="h-1.5 w-10 rounded-full bg-surface-2" />
                        <span className="brand-gradient h-5 w-5 rounded-full ring-2 ring-surface" />
                      </div>
                    </motion.div>
                  </motion.div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </LayoutGroup>
  );
};

export default KanbanDemo;
