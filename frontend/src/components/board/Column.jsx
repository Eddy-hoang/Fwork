import { useState, useRef, useEffect } from "react";
import { useDroppable } from "@dnd-kit/core";
import { SortableContext, verticalListSortingStrategy } from "@dnd-kit/sortable";
import { Plus, MoreHorizontal, Trash2, Sparkles, Pencil } from "lucide-react";
import TaskCard from "./TaskCard";
import ConfirmDialog from "../ui/ConfirmDialog";
import { cn, columnAccent } from "../../lib/utils";

const Column = ({ column, tasks, index = 0, onTaskClick, onAddTask, onRename, onDelete, onAiGenerate }) => {
  const { setNodeRef, isOver } = useDroppable({ id: column.id, data: { type: "column", column } });
  const accent = columnAccent(index);
  const [menuOpen, setMenuOpen] = useState(false);
  const [editing, setEditing] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [title, setTitle] = useState(column.title);
  const menuRef = useRef(null);

  useEffect(() => setTitle(column.title), [column.title]);
  useEffect(() => {
    const onClick = (e) => menuRef.current && !menuRef.current.contains(e.target) && setMenuOpen(false);
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  const commitRename = () => {
    setEditing(false);
    const t = title.trim();
    if (t && t !== column.title) onRename(column.id, t);
    else setTitle(column.title);
  };

  return (
    <div
      className={cn(
        "flex h-full w-[330px] shrink-0 flex-col rounded-2xl p-2.5 transition-colors",
        isOver && "ring-2 ring-inset"
      )}
      style={{ backgroundColor: accent.soft, ...(isOver ? { "--tw-ring-color": accent.ring } : {}) }}
    >
      {/* Header */}
      <div className="mb-2 flex items-center gap-2 px-1.5 pt-1">
        <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: accent.dot }} />
        {editing ? (
          <input
            autoFocus
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            onBlur={commitRename}
            onKeyDown={(e) => e.key === "Enter" && commitRename()}
            className="w-full rounded bg-surface px-2 py-0.5 text-sm font-semibold outline-none"
          />
        ) : (
          <h3 className="cursor-text font-display text-[15px] font-bold tracking-tight text-ink" onDoubleClick={() => setEditing(true)}>
            {column.title}
          </h3>
        )}
        <span className="rounded-full bg-surface px-2 py-0.5 text-xs font-semibold tabular text-muted shadow-[var(--shadow-card)]">
          {tasks.length}
        </span>

        <div className="ml-auto flex items-center gap-0.5">
          <button
            onClick={() => onAddTask(column.id)}
            className="rounded-lg p-1.5 text-faint transition-colors hover:bg-surface hover:text-ink"
            title="Add task"
          >
            <Plus className="h-4 w-4" />
          </button>
          <div className="relative" ref={menuRef}>
            <button
              onClick={() => setMenuOpen((o) => !o)}
              className="rounded-lg p-1.5 text-faint transition-colors hover:bg-surface hover:text-ink"
            >
              <MoreHorizontal className="h-4 w-4" />
            </button>
            {menuOpen && (
              <div className="card animate-in absolute right-0 z-10 mt-1 w-44 rounded-2xl p-1.5 shadow-[var(--shadow-lift)]">
                <MenuItem icon={Sparkles} onClick={() => { setMenuOpen(false); onAiGenerate(column.id); }}>
                  Generate with AI
                </MenuItem>
                <MenuItem icon={Pencil} onClick={() => { setMenuOpen(false); setEditing(true); }}>
                  Rename
                </MenuItem>
                <div className="my-1 border-t" />
                <MenuItem icon={Trash2} danger onClick={() => { setMenuOpen(false); setConfirmOpen(true); }}>
                  Delete column
                </MenuItem>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Drop zone */}
      <div ref={setNodeRef} className="flex flex-1 flex-col gap-2.5 overflow-y-auto px-0.5 pb-1 no-scrollbar">
        <SortableContext items={tasks.map((t) => t.id)} strategy={verticalListSortingStrategy}>
          {tasks.map((task) => (
            <TaskCard key={task.id} task={task} onClick={onTaskClick} />
          ))}
        </SortableContext>

        <button
          onClick={() => onAddTask(column.id)}
          className="flex items-center justify-center gap-1.5 rounded-xl border border-dashed border-line/80 bg-surface/40 py-2.5 text-xs font-medium text-faint transition-colors hover:border-ink/20 hover:bg-surface hover:text-muted"
        >
          <Plus className="h-3.5 w-3.5" /> Add task
        </button>
      </div>

      <ConfirmDialog
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        onConfirm={() => { onDelete(column.id); setConfirmOpen(false); }}
        title="Delete column?"
        description={`“${column.title}” and all its tasks will be permanently removed. This can’t be undone.`}
        confirmLabel="Delete column"
        danger
      />
    </div>
  );
};

const MenuItem = ({ icon: Icon, children, onClick, danger }) => (
  <button
    onClick={onClick}
    className={cn(
      "flex w-full items-center gap-2 rounded-lg px-2.5 py-1.5 text-sm transition-colors hover:bg-surface-2",
      danger ? "text-priority-urgent" : "text-muted hover:text-ink"
    )}
  >
    <Icon className="h-3.5 w-3.5" /> {children}
  </button>
);

export default Column;
