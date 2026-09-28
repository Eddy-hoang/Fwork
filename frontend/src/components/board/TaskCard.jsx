import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { Calendar, CheckCircle2, Circle, GripVertical } from "lucide-react";
import Avatar from "../ui/Avatar";
import { PriorityTag } from "../ui/Badge";
import { cn, formatDueDate } from "../../lib/utils";

const isDoneStatus = (status) => {
  if (!status) return false;
  const s = status.toLowerCase();
  return s.includes("done") || s.includes("complete") || s.includes("hoàn thành") || s.includes("finish");
};

const TaskCard = ({ task, onClick, onToggleComplete, overlay = false }) => {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: task.id,
    data: { type: "task", task },
  });

  const style = { transform: CSS.Translate.toString(transform), transition };
  const due = formatDueDate(task.due_date || task.dueDate);
  const isDone = task.isCompleted || isDoneStatus(task.status || task.columnName || task.columnTitle);

  const handleCheckboxClick = (e) => {
    e.stopPropagation();
    e.preventDefault();
    onToggleComplete?.(task);
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      onClick={() => !isDragging && onClick?.(task)}
      className={cn(
        "group cursor-default rounded-lg border border-zinc-200 bg-white p-3.5 shadow-xs transition-all duration-150 dark:border-zinc-800 dark:bg-zinc-900 hover:border-zinc-300 dark:hover:border-zinc-700",
        isDone && "bg-zinc-50/60 dark:bg-zinc-950/40 border-zinc-200/60 dark:border-zinc-800/60 opacity-60 shadow-none pointer-events-auto select-none",
        isDragging && "opacity-40",
        overlay && "rotate-1 cursor-grabbing shadow-md border-zinc-300 dark:border-zinc-700 scale-[1.02]"
      )}
    >
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5">
          <button
            type="button"
            title="Drag to reorder"
            aria-label="Drag to reorder task"
            onClick={(e) => e.stopPropagation()}
            className="cursor-grab active:cursor-grabbing text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors p-0.5 -ml-1 rounded"
            {...attributes}
            {...listeners}
          >
            <GripVertical className="h-3.5 w-3.5 shrink-0" />
          </button>
          <PriorityTag priority={task.priority} className={cn(isDone && "opacity-40")} />
        </div>
        {onToggleComplete && (
          <button
            type="button"
            onClick={handleCheckboxClick}
            onPointerDown={(e) => e.stopPropagation()}
            onMouseDown={(e) => e.stopPropagation()}
            className="z-10 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors p-0.5"
            title={isDone ? "Mark as incomplete" : "Mark as completed"}
          >
            {isDone ? (
              <CheckCircle2 className="h-4 w-4 text-emerald-500 fill-emerald-500/10" />
            ) : (
              <Circle className="h-4 w-4 hover:stroke-zinc-600 dark:hover:stroke-zinc-300" />
            )}
          </button>
        )}
      </div>

      <p
        className={cn(
          "mt-2 text-xs font-semibold leading-snug tracking-tight text-zinc-900 dark:text-zinc-50",
          isDone && "text-zinc-400 line-through opacity-70"
        )}
      >
        {task.title}
      </p>

      {task.description && (
        <p className={cn("mt-1 line-clamp-2 text-[11px] leading-relaxed text-zinc-500 dark:text-zinc-400", isDone && "line-through opacity-60")}>
          {task.description}
        </p>
      )}

      <div className={cn("mt-3 flex items-center justify-between border-t border-zinc-100 dark:border-zinc-800/80 pt-2.5", isDone && "opacity-50")}>
        {task.assignee_id ? (
          <div className="flex items-center gap-1.5">
            <Avatar name={task.assignee_name} id={task.assignee_id} src={task.assignee_avatar} size="xs" />
            <span className="max-w-[6.5rem] truncate text-[11px] font-medium text-zinc-600 dark:text-zinc-400">{task.assignee_name}</span>
          </div>
        ) : (
          <span className="text-[10px] text-zinc-400">Unassigned</span>
        )}

        {due && (
          <span
            className={cn(
              "flex items-center gap-1 rounded-md px-1.5 py-0.5 text-[10px] font-medium tabular-nums",
              due.overdue && !isDone
                ? "bg-red-50 text-red-600 dark:bg-red-950/40 dark:text-red-400"
                : "bg-zinc-100 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-400"
            )}
          >
            <Calendar className="h-3 w-3" /> {due.label}
          </span>
        )}
      </div>
    </div>
  );
};

export default TaskCard;
