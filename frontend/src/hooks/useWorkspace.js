import { useState, useEffect } from "react";
import api, { boardApi, columnApi, workspaceApi } from "../lib/api";
import { useBoards } from "../context/BoardsContext";

/**
 * Aggregates every board the user can see into a single flat list of tasks
 * (each tagged with its board + status) and a de-duplicated member directory.
 */
export const useWorkspace = () => {
  const {
    boards,
    workspaces,
    currentWorkspace,
    setCurrentWorkspace,
    refreshWorkspaces,
    loading: boardsLoading,
  } = useBoards();

  const [tasks, setTasks] = useState([]);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchMembers = async () => {
    if (!currentWorkspace?.id) return;
    try {
      const res = await workspaceApi.getMembers(currentWorkspace.id);
      if (Array.isArray(res)) setMembers(res);
    } catch (e) {
      console.error("Failed to fetch workspace members", e);
    }
  };

  useEffect(() => {
    if (boardsLoading) return;
    if (!boards || !boards.length) {
      setTasks([]);
      setLoading(false);
      return;
    }

    let cancelled = false;
    setLoading(true);

    Promise.all(
      boards.map(async (b) => {
        try {
          const [colsRes, tasksRes] = await Promise.all([
            columnApi.list(b.id).catch(() => []),
            api.get(`/boards/${b.id}/tasks`).catch(() => []),
          ]);

          const board = b;
          const rawCols = Array.isArray(colsRes) ? colsRes : (colsRes?.data || []);
          const rawTasks = Array.isArray(tasksRes) ? tasksRes : (tasksRes?.data || []);

          const colTitleMap = {};
          rawCols.forEach((c) => {
            const cId = c.id;
            const cTitle = c.title || c.name || "Untitled";
            if (cId) colTitleMap[cId] = cTitle;
          });

          const boardTasks = rawTasks.map((t) => {
            const colId = t.column_id || t.columnId;
            const assigneeId = t.assignee_id || t.assigneeId || (typeof t.assignee === "object" ? t.assignee?.id : t.assignee);
            const dueDate = t.due_date || t.dueDate;
            return {
              ...t,
              board_id: board.id,
              board_title: board.title,
              board_color: board.color,
              column_id: colId,
              columnId: colId,
              assignee_id: assigneeId,
              assigneeId: assigneeId,
              due_date: dueDate,
              dueDate: dueDate,
              status: colTitleMap[colId] || "",
            };
          });

          return boardTasks;
        } catch (e) {
          console.error(`Error fetching data for board ${b.id}:`, e);
          return [];
        }
      })
    ).then((results) => {
      if (cancelled) return;
      const allTasks = results.flat().filter(Boolean);
      setTasks(allTasks);
      setLoading(false);
    });

    return () => {
      cancelled = true;
    };
  }, [boards, boardsLoading]);

  // Fetch workspace members when currentWorkspace changes
  useEffect(() => {
    if (currentWorkspace?.id) {
      fetchMembers();
    }
  }, [currentWorkspace?.id]);

  return {
    tasks,
    members,
    boards,
    workspaces,
    currentWorkspace,
    setCurrentWorkspace,
    refreshWorkspaces,
    refreshMembers: fetchMembers,
    loading: loading || boardsLoading,
  };
};
