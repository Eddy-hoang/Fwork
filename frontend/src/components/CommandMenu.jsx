import { useState, useEffect, useMemo, useRef } from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import { Search, Plus, LayoutGrid, LayoutDashboard, CornerDownLeft, CheckSquare, Calendar, Users, Settings } from "lucide-react";
import { useBoards } from "../context/BoardsContext";
import { cn } from "../lib/utils";

const CommandMenu = ({ open, onClose, onCreateBoard }) => {
  const { boards } = useBoards();
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [active, setActive] = useState(0);
  const inputRef = useRef(null);

  useEffect(() => {
    if (open) {
      setQuery("");
      setActive(0);
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  }, [open]);

  const items = useMemo(() => {
    const actions = [
      { id: "create", label: "Create new board", icon: Plus, onSelect: onCreateBoard },
      { id: "dashboard", label: "Go to dashboard", icon: LayoutDashboard, onSelect: () => navigate("/dashboard") },
      { id: "my-tasks", label: "Go to My Tasks", icon: CheckSquare, onSelect: () => navigate("/my-tasks") },
      { id: "calendar", label: "Go to Calendar", icon: Calendar, onSelect: () => navigate("/calendar") },
      { id: "team", label: "Go to Team", icon: Users, onSelect: () => navigate("/team") },
      { id: "settings", label: "Go to Settings", icon: Settings, onSelect: () => navigate("/settings") },
    ];
    const boardItems = boards.map((b) => ({
      id: `board-${b.id}`,
      label: b.title,
      sub: "Board",
      icon: LayoutGrid,
      color: b.color,
      onSelect: () => navigate(`/board/${b.id}`),
    }));
    const all = [...actions, ...boardItems];
    if (!query.trim()) return all;
    const q = query.toLowerCase();
    return all.filter((i) => i.label.toLowerCase().includes(q));
  }, [boards, query, navigate, onCreateBoard]);

  const run = (item) => {
    onClose();
    item?.onSelect?.();
  };

  const onKeyDown = (e) => {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setActive((a) => Math.min(a + 1, items.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setActive((a) => Math.max(a - 1, 0));
    } else if (e.key === "Enter") {
      e.preventDefault();
      run(items[active]);
    } else if (e.key === "Escape") {
      onClose();
    }
  };

  return createPortal(
    <AnimatePresence>
      {open && (
        <motion.div
          className="fixed inset-0 z-[60] flex items-start justify-center p-4 pt-[12vh]"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
        >
          <div className="fixed inset-0 bg-ink/35 backdrop-blur-sm" onClick={onClose} />
          <motion.div
            className="card relative z-10 w-full max-w-xl overflow-hidden rounded-3xl shadow-[var(--shadow-lift)]"
            initial={{ opacity: 0, scale: 0.98, y: -8 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.98, y: -4 }}
            transition={{ duration: 0.18, ease: [0.16, 1, 0.3, 1] }}
          >
            <div className="flex items-center gap-3 border-b px-4">
              <Search className="h-4 w-4 text-faint" />
              <input
                ref={inputRef}
                value={query}
                onChange={(e) => {
                  setQuery(e.target.value);
                  setActive(0);
                }}
                onKeyDown={onKeyDown}
                placeholder="Search boards or run a command…"
                className="h-12 w-full bg-transparent text-sm outline-none placeholder:text-faint"
              />
            </div>

            <div className="max-h-80 overflow-y-auto p-2">
              {items.length === 0 ? (
                <p className="px-3 py-6 text-center text-sm text-faint">No results</p>
              ) : (
                items.map((item, i) => (
                  <button
                    key={item.id}
                    onMouseEnter={() => setActive(i)}
                    onClick={() => run(item)}
                    className={cn(
                      "flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm transition-colors",
                      active === i ? "bg-brand-50 text-brand-700" : "text-muted"
                    )}
                  >
                    {item.color ? (
                      <span className="h-3.5 w-3.5 rounded-full" style={{ backgroundColor: item.color }} />
                    ) : (
                      <item.icon className="h-4 w-4" />
                    )}
                    <span className="flex-1 truncate">{item.label}</span>
                    {item.sub && <span className="text-[11px] text-faint">{item.sub}</span>}
                    {active === i && <CornerDownLeft className="h-3.5 w-3.5 text-faint" />}
                  </button>
                ))
              )}
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>,
    document.body
  );
};

export default CommandMenu;
