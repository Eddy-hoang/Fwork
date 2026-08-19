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
        "group cursor-default rounded-2xl border border-line bg-surface p-4",
        "shadow-[var(--shadow-card)] transition-all duration-200",
        "hover:shadow-[var(--shadow-soft)]",
        isDone && "bg-surface-2/40 border-line/50 opacity-50 shadow-none pointer-events-auto select-none",
        isDragging && "opacity-40",
        overlay && "rotate-2 cursor-grabbing shadow-[var(--shadow-lift)]"
      )}
    >
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5">
          <button
            type="button"
            title="Drag to reorder"
            aria-label="Drag to reorder task"
            onClick={(e) => e.stopPropagation()}
            className="cursor-grab active:cursor-grabbing text-faint hover:text-ink transition-colors p-1 -ml-1 rounded focus-ring"
            {...attributes}
            {...listeners}
          >
            <GripVertical className="h-4 w-4 shrink-0" />
          </button>
          <PriorityTag priority={task.priority} className={cn(isDone && "opacity-40")} />
        </div>
        {onToggleComplete && (
          <button
            type="button"
            onClick={handleCheckboxClick}
            onPointerDown={(e) => e.stopPropagation()}
            onMouseDown={(e) => e.stopPropagation()}
            className="z-10 text-faint hover:text-brand-500 transition-colors p-1 -m-1"
            title={isDone ? "Mark as incomplete" : "Mark as completed"}
          >
            {isDone ? (
              <CheckCircle2 className="h-4 w-4 text-emerald-500 fill-emerald-500/20" />
            ) : (
              <Circle className="h-4 w-4 hover:stroke-brand-500" />
            )}
          </button>
        )}
      </div>

      <p
        className={cn(
          "mt-2.5 text-[15px] font-semibold leading-snug tracking-tight",
          isDone ? "text-muted line-through opacity-60" : "text-ink"
        )}
      >
        {task.title}
      </p>

      {task.description && (
        <p className={cn("mt-1.5 line-clamp-2 text-[13px] leading-relaxed text-muted", isDone && "line-through opacity-50")}>
          {task.description}
        </p>
      )}

      <div className={cn("mt-3.5 flex items-center justify-between border-t border-line/70 pt-3", isDone && "opacity-40")}>
        {task.assignee_id ? (
          <div className="flex items-center gap-1.5">
            <Avatar name={task.assignee_name} id={task.assignee_id} src={task.assignee_avatar} size="xs" />
            <span className="max-w-[7rem] truncate text-[11px] text-muted">{task.assignee_name}</span>
          </div>
        ) : (
          <span className="text-[11px] text-faint">Unassigned</span>
        )}

        {due && (
          <span
            className={cn(
              "flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-medium tabular",
              due.overdue && !isDone
                ? "bg-priority-urgent/10 text-priority-urgent"
                : "bg-surface-2 text-muted"
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
