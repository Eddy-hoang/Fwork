import { useState, useRef, useEffect } from "react";
import { useDroppable } from "@dnd-kit/core";
import { SortableContext, verticalListSortingStrategy } from "@dnd-kit/sortable";
import { Plus, MoreHorizontal, Trash2, Sparkles, Pencil } from "lucide-react";
import TaskCard from "./TaskCard";
import ConfirmDialog from "../ui/ConfirmDialog";
import { cn, columnAccent } from "../../lib/utils";

const Column = ({ column, tasks, index = 0, onTaskClick, onToggleComplete, onAddTask, onRename, onDelete, onAiGenerate }) => {
  const { setNodeRef, isOver } = useDroppable({ id: column.id, data: { type: "column", column } });
  const accent = columnAccent(index);
  const [menuOpen, setMenuOpen] = useState(false);
  const [editing, setEditing] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [title, setTitle] = useState(column.title || column.name || "");
  const menuRef = useRef(null);

  useEffect(() => setTitle(column.title || column.name || ""), [column.title, column.name]);
  useEffect(() => {
    const onClick = (e) => menuRef.current && !menuRef.current.contains(e.target) && setMenuOpen(false);
    const onKeyDown = (e) => e.key === "Escape" && setMenuOpen(false);
    document.addEventListener("mousedown", onClick);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("mousedown", onClick);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, []);

  const commitRename = () => {
    setEditing(false);
    const t = title.trim();
    if (t && t !== (column.title || column.name)) onRename(column.id, t);
    else setTitle(column.title || column.name || "");
  };

  return (
    <div
      className={cn(
        "flex h-full w-[310px] shrink-0 flex-col rounded-xl border border-zinc-200/80 bg-zinc-50/70 p-2.5 transition-colors dark:border-zinc-800 dark:bg-zinc-900/40",
        isOver && "ring-2 ring-zinc-950 dark:ring-zinc-200"
      )}
    >
      {/* Header */}
      <div className="mb-2.5 flex items-center gap-2 px-1 pt-0.5">
        <span className="h-2 w-2 rounded-full" style={{ backgroundColor: accent.dot }} />
        {editing ? (
          <input
            autoFocus
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            onBlur={commitRename}
            onKeyDown={(e) => e.key === "Enter" && commitRename()}
            className="w-full rounded border border-zinc-300 bg-white px-2 py-0.5 text-xs font-semibold text-zinc-900 outline-none dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-50"
          />
        ) : (
          <h3 className="cursor-text text-xs font-semibold tracking-tight text-zinc-900 dark:text-zinc-50" onDoubleClick={() => setEditing(true)}>
            {column.title}
          </h3>
        )}
        <span className="rounded-md bg-zinc-200/60 dark:bg-zinc-800 px-1.5 py-0.5 text-[10px] font-medium text-zinc-600 dark:text-zinc-400">
          {tasks.length}
        </span>

        <div className="ml-auto flex items-center gap-0.5">
          <button
            onClick={() => onAddTask(column.id)}
            className="rounded-md p-1 text-zinc-400 transition-colors hover:bg-zinc-200/60 hover:text-zinc-700 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
            title="Add task"
          >
            <Plus className="h-3.5 w-3.5" />
          </button>
          <div className="relative" ref={menuRef}>
            <button
              onClick={() => setMenuOpen((o) => !o)}
              className="rounded-md p-1 text-zinc-400 transition-colors hover:bg-zinc-200/60 hover:text-zinc-700 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
            >
              <MoreHorizontal className="h-3.5 w-3.5" />
            </button>
            {menuOpen && (
              <div className="absolute right-0 z-20 mt-1 w-40 rounded-lg border border-zinc-200 bg-white p-1 shadow-md dark:border-zinc-800 dark:bg-zinc-900">
                <MenuItem icon={Sparkles} onClick={() => { setMenuOpen(false); onAiGenerate(column.id); }}>
                  Generate with AI
                </MenuItem>
                <MenuItem icon={Pencil} onClick={() => { setMenuOpen(false); setEditing(true); }}>
                  Rename
                </MenuItem>
                <div className="my-1 border-t border-zinc-100 dark:border-zinc-800" />
                <MenuItem icon={Trash2} danger onClick={() => { setMenuOpen(false); setConfirmOpen(true); }}>
                  Delete column
                </MenuItem>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Drop zone */}
      <div ref={setNodeRef} className="flex flex-1 flex-col gap-2 overflow-y-auto px-0.5 pb-1 no-scrollbar">
        <SortableContext items={tasks.map((t) => t.id)} strategy={verticalListSortingStrategy}>
          {tasks.map((task) => (
            <TaskCard
              key={task.id}
              task={{ ...task, status: column.title || column.name }}
              onClick={onTaskClick}
              onToggleComplete={onToggleComplete}
            />
          ))}
        </SortableContext>

        <button
          onClick={() => onAddTask(column.id)}
          className="flex items-center justify-center gap-1.5 rounded-lg border border-dashed border-zinc-300 py-2 text-xs font-medium text-zinc-400 transition-colors hover:border-zinc-400 hover:bg-white hover:text-zinc-700 dark:border-zinc-750 dark:hover:border-zinc-600 dark:hover:bg-zinc-900 dark:hover:text-zinc-300"
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
      "flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-xs transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800",
      danger ? "text-red-600 dark:text-red-400" : "text-zinc-600 hover:text-zinc-900 dark:text-zinc-300 dark:hover:text-zinc-50"
    )}
  >
    <Icon className="h-3.5 w-3.5" /> {children}
  </button>
);

export default Column;
