import { useState, useMemo } from "react";
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  closestCorners,
} from "@dnd-kit/core";
import { Plus } from "lucide-react";
import Column from "./Column";
import TaskCard from "./TaskCard";

// Compute a fractional position so a task lands at `index` within `siblings`
// (siblings must already exclude the task being moved, sorted by position).
const positionForIndex = (siblings, index) => {
  const prev = index > 0 ? siblings[index - 1]?.position : null;
  const next = index < siblings.length ? siblings[index]?.position : null;
  if (prev == null && next == null) return 1000;
  if (prev == null) return next / 2;
  if (next == null) return prev + 1000;
  return (prev + next) / 2;
};

const KanbanBoard = ({ columns, tasks, actions, onTaskClick, onAddTask, onAiGenerate, onAddColumn }) => {
  const [activeTask, setActiveTask] = useState(null);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } })
  );

  const tasksByColumn = useMemo(() => {
    const map = {};
    for (const col of columns) map[col.id] = [];
    for (const t of tasks) (map[t.column_id] ||= []).push(t);
    for (const id in map) map[id].sort((a, b) => a.position - b.position);
    return map;
  }, [columns, tasks]);

  const onDragStart = ({ active }) => {
    setActiveTask(tasks.find((t) => t.id === active.id) || null);
  };

  const onDragEnd = ({ active, over }) => {
    setActiveTask(null);
    if (!over) return;

    const activeId = active.id;
    const activeTaskObj = tasks.find((t) => t.id === activeId);
    if (!activeTaskObj) return;

    // Resolve the target column from the drop target.
    const overData = over.data.current;
    let targetColumnId;
    if (overData?.type === "column") targetColumnId = over.id;
    else if (overData?.type === "task") targetColumnId = overData.task.column_id;
    else targetColumnId = activeTaskObj.column_id;

    const siblings = (tasksByColumn[targetColumnId] || []).filter((t) => t.id !== activeId);

    let index;
    if (overData?.type === "task") {
      const overIndex = siblings.findIndex((t) => t.id === over.id);
      index = overIndex === -1 ? siblings.length : overIndex;
    } else {
      index = siblings.length; // dropped on empty column area
    }

    const newPosition = positionForIndex(siblings, index);

    // No-op guard
    if (
      activeTaskObj.column_id === targetColumnId &&
      activeTaskObj.position === newPosition
    ) {
      return;
    }

    actions.moveTask(activeId, targetColumnId, newPosition);
  };

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCorners}
      onDragStart={onDragStart}
      onDragEnd={onDragEnd}
      onDragCancel={() => setActiveTask(null)}
    >
      <div className="flex h-full gap-5 overflow-x-auto px-6 pb-6">
        {columns.map((col, i) => (
          <Column
            key={col.id}
            column={col}
            index={i}
            tasks={tasksByColumn[col.id] || []}
            onTaskClick={onTaskClick}
            onAddTask={onAddTask}
            onAiGenerate={onAiGenerate}
            onRename={actions.renameColumn}
            onDelete={actions.deleteColumn}
          />
        ))}

        {/* Add column */}
        <button
          onClick={onAddColumn}
          className="flex h-11 w-[300px] shrink-0 items-center justify-center gap-1.5 rounded-2xl border border-dashed border-line text-sm font-medium text-faint transition-colors hover:border-brand-500/50 hover:bg-surface hover:text-muted"
        >
          <Plus className="h-4 w-4" /> Add column
        </button>
      </div>

      <DragOverlay>
        {activeTask ? (
          <div className="w-[314px]">
            <TaskCard task={activeTask} overlay />
          </div>
        ) : null}
      </DragOverlay>
    </DndContext>
  );
};

export default KanbanBoard;
