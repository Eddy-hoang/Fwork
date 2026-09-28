import { useState, useEffect, useCallback, useMemo } from "react";
import toast from "react-hot-toast";
import api, { boardApi, taskApi, columnApi, workspaceApi } from "../lib/api";
import { subscribeBoard } from "../lib/socket";
import { getCompletedTasks, toggleCompletedTask } from "../lib/utils";

/**
 * Normalizes a column object to have both `name` and `title`
 */
const normalizeColumn = (col) => {
  if (!col) return col;
  const name = col.name || col.title || "Untitled";
  return {
    ...col,
    name,
    title: name,
  };
};

/**
 * Normalizes a task object to have both snake_case and camelCase field aliases
 */
const normalizeTask = (task) => {
  if (!task) return task;
  const columnId = task.columnId || task.column_id;
  const assigneeId = task.assigneeId || task.assignee_id;
  const dueDate = task.dueDate || task.due_date;
  return {
    ...task,
    columnId,
    column_id: columnId,
    assigneeId,
    assignee_id: assigneeId,
    dueDate,
    due_date: dueDate,
  };
};

/**
 * Loads a board and keeps it in sync via WebSocket STOMP. Returns board state plus
 * mutation helpers that update optimistically and persist to the API.
 */
export const useBoard = (boardId) => {
  const [board, setBoard] = useState(null);
  const [columns, setColumns] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [members, setMembers] = useState([]);
  const [role, setRole] = useState("MEMBER");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [presence, setPresence] = useState([]);
  const [completedIds, setCompletedIds] = useState(() => getCompletedTasks());

  const upsertTask = useCallback((task) => {
    if (!task) return;
    const normalized = normalizeTask(task);
    setTasks((prev) => {
      const idx = prev.findIndex((t) => t.id === normalized.id);
      if (idx === -1) return [...prev, normalized];
      const next = [...prev];
      next[idx] = normalized;
      return next;
    });
  }, []);

  const removeTaskLocal = useCallback((id) => {
    setTasks((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const loadBoardData = useCallback(async () => {
    if (!boardId) return;
    try {
      setLoading(true);
      setError(null);

      // Fetch board, columns, and tasks in parallel
      const [boardRes, colsRes, tasksRes] = await Promise.all([
        boardApi.get(boardId),
        columnApi.list(boardId).catch(() => []),
        api.get(`/boards/${boardId}/tasks`).catch(() => []),
      ]);

      const bData = boardRes?.board || boardRes;
      setBoard(bData);

      // Process columns
      const rawCols = Array.isArray(colsRes) ? colsRes : (colsRes?.data || []);
      const cols = rawCols.map(normalizeColumn);
      setColumns(cols.sort((a, b) => (a.position || 0) - (b.position || 0)));

      // Process tasks
      const rawTasks = Array.isArray(tasksRes) ? tasksRes : (tasksRes?.data || []);
      const tks = rawTasks.map(normalizeTask);
      setTasks(tks);

      // Fetch members from workspace if available
      const wsId = bData?.workspaceId || bData?.workspace_id;
      if (wsId) {
        workspaceApi.getMembers(wsId)
          .then((mRes) => {
            const mList = Array.isArray(mRes) ? mRes : (mRes?.data || []);
            setMembers(mList);
          })
          .catch(() => {});
      }
    } catch (err) {
      console.error("Error loading board:", err);
      setError(err.message || "Failed to load board");
    } finally {
      setLoading(false);
    }
  }, [boardId]);

  // Initial load
  useEffect(() => {
    loadBoardData();
  }, [loadBoardData]);

  // Real-time STOMP sync
  useEffect(() => {
    if (!boardId) return;
    const subscription = subscribeBoard(boardId, (wsEvent) => {
      switch (wsEvent.type) {
        case "TASK_CREATED":
        case "TASK_UPDATED":
        case "TASK_MOVED":
        case "TASK_ASSIGNED":
        case "LABELS_UPDATED":
          if (typeof wsEvent.payload === "string") {
            taskApi.get(wsEvent.payload).then(upsertTask).catch(() => loadBoardData());
          } else if (wsEvent.payload && wsEvent.payload.id) {
            upsertTask(wsEvent.payload);
          } else {
            loadBoardData();
          }
          break;
        case "COMMENT_ADDED":
        case "COMMENT_DELETED":
          loadBoardData();
          break;
        default:
          loadBoardData();
      }
    });

    return () => {
      if (subscription && subscription.unsubscribe) {
        subscription.unsubscribe();
      }
    };
  }, [boardId, upsertTask, loadBoardData]);

  /* ----------------------------- mutations ----------------------------- */

  const createTask = useCallback(
    async (data) => {
      try {
        const colId = data.column_id || data.columnId || (columns[0] && columns[0].id);
        if (!colId) {
          throw new Error("Cannot create task: No column found");
        }
        const created = await taskApi.create(colId, {
          title: data.title,
          description: data.description,
          priority: data.priority ? data.priority.toUpperCase() : "MEDIUM",
          dueDate: data.dueDate || data.due_date,
          position: data.position,
        });
        let finalTask = created;
        const assigneeId = data.assigneeId || data.assignee_id;
        if (assigneeId) {
          try {
            finalTask = await taskApi.assign(created.id, { assigneeId });
          } catch (e) {
            console.error("Failed to assign user on task creation", e);
          }
        }
        const normalized = normalizeTask(finalTask);
        upsertTask(normalized);
        return normalized;
      } catch (err) {
        toast.error(err.message);
        throw err;
      }
    },
    [columns, upsertTask]
  );

  const updateTask = useCallback(
    async (taskId, data) => {
      const prev = tasks.find((t) => t.id === taskId);
      if (prev) upsertTask({ ...prev, ...data }); // optimistic
      try {
        let updated = await taskApi.update(taskId, {
          ...data,
          priority: data.priority ? data.priority.toUpperCase() : undefined,
        });
        const assigneeId = data.assigneeId || data.assignee_id;
        if (assigneeId !== undefined) {
          try {
            updated = await taskApi.assign(taskId, { assigneeId: assigneeId || null });
          } catch (e) {
            console.error("Failed to update assignee", e);
          }
        }
        const normalized = normalizeTask(updated);
        upsertTask(normalized);
        return normalized;
      } catch (err) {
        if (prev) upsertTask(prev);
        toast.error(err.message);
        throw err;
      }
    },
    [tasks, upsertTask]
  );

  const deleteTask = useCallback(
    async (taskId) => {
      const prev = tasks.find((t) => t.id === taskId);
      removeTaskLocal(taskId); // optimistic
      try {
        await taskApi.remove(taskId);
        toast.success("Task deleted");
      } catch (err) {
        if (prev) upsertTask(prev);
        toast.error(err.message);
      }
    },
    [tasks, removeTaskLocal, upsertTask]
  );

  const moveTask = useCallback(
    async (taskId, columnId, position) => {
      const prev = tasks.find((t) => t.id === taskId);
      if (!prev) return;
      const targetPosInt = Math.max(0, Math.floor(position || 0));
      upsertTask({ ...prev, column_id: columnId, columnId: columnId, position: targetPosInt });
      try {
        await taskApi.move(taskId, {
          targetColumnId: columnId,
          targetPosition: targetPosInt,
          position: targetPosInt,
        });
      } catch (err) {
        upsertTask(prev);
        toast.error(err.message);
      }
    },
    [tasks, upsertTask]
  );

  const addColumn = useCallback(
    async (name) => {
      try {
        const col = await columnApi.create(boardId, {
          name: name || "New Column",
          position: columns.length,
        });
        const normalized = normalizeColumn(col);
        setColumns((p) => [...p, normalized].sort((a, b) => (a.position || 0) - (b.position || 0)));
      } catch (err) {
        toast.error(err.message);
      }
    },
    [boardId, columns.length]
  );

  const renameColumn = useCallback(
    async (columnId, name) => {
      setColumns((p) =>
        p.map((c) => (c.id === columnId ? normalizeColumn({ ...c, name, title: name }) : c))
      );
      try {
        await columnApi.update(columnId, { name });
      } catch (err) {
        toast.error(err.message);
      }
    },
    []
  );

  const deleteColumn = useCallback(
    async (columnId) => {
      try {
        await columnApi.remove(columnId);
        setColumns((p) => p.filter((c) => c.id !== columnId));
        setTasks((p) => p.filter((t) => (t.column_id || t.columnId) !== columnId));
      } catch (err) {
        toast.error(err.message);
      }
    },
    []
  );

  const toggleTaskComplete = useCallback(
    async (task) => {
      if (!task) return;
      
      const next = toggleCompletedTask(task.id);
      setCompletedIds(next);
      const isDone = next.includes(task.id);
      
      // Optimistic local state update
      upsertTask({ ...task, isCompleted: isDone, is_completed: isDone });

      try {
        await taskApi.update(task.id, {
          title: task.title,
          description: task.description,
          priority: task.priority,
          due_date: task.due_date || task.dueDate,
          isCompleted: isDone,
        });
        
        if (isDone) {
          toast.success("Nhiệm vụ được đánh dấu hoàn thành!");
        } else {
          toast.success("Nhiệm vụ được đánh dấu chưa hoàn thành!");
        }
      } catch (err) {
        // Rollback on error
        const prevList = toggleCompletedTask(task.id);
        setCompletedIds(prevList);
        upsertTask(task);
        toast.error(err.message || "Failed to update task state");
      }
    },
    [upsertTask]
  );

  const enrichedTasks = useMemo(() => {
    const map = new Map();
    members.forEach((m) => {
      const uid = m.userId || m.user_id || m.id;
      if (uid) map.set(String(uid).toLowerCase(), m);
    });
    return tasks.map((t) => {
      const aid = t.assigneeId || t.assignee_id || (typeof t.assignee === "object" ? t.assignee?.id : t.assignee);
      const m = aid ? map.get(String(aid).toLowerCase()) : null;
      const name = m?.name || t.assignee_name || (typeof t.assignee === "object" ? t.assignee?.name : null);
      const avatar = m?.avatar || t.assignee_avatar || (typeof t.assignee === "object" ? t.assignee?.avatar : null);
      const isDone = t.isCompleted || t.is_completed || completedIds.some((id) => String(id).toLowerCase() === String(t.id).toLowerCase());
      return {
        ...t,
        assignee_id: aid,
        assigneeId: aid,
        assignee_name: name,
        assignee_avatar: avatar,
        isCompleted: isDone,
        status: isDone ? "Done" : (t.status || t.columnName || t.columnTitle)
      };
    });
  }, [tasks, members, completedIds]);

  return {
    board,
    columns,
    tasks: enrichedTasks,
    members,
    role,
    loading,
    error,
    presence,
    setBoard,
    setMembers,
    createTask,
    updateTask,
    deleteTask,
    moveTask,
    toggleTaskComplete,
    upsertTask,
    addColumn,
    renameColumn,
    deleteColumn,
  };
};
