import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { boardApi, workspaceApi } from "../lib/api";

const BoardsContext = createContext(null);

export const BoardsProvider = ({ children }) => {
  const [boards, setBoards] = useState([]);
  const [workspaces, setWorkspaces] = useState([]);
  const [currentWorkspace, setCurrentWorkspace] = useState(null);
  const [loading, setLoading] = useState(true);

  const refreshWorkspaces = useCallback(async () => {
    try {
      const list = await workspaceApi.list();
      const wsList = Array.isArray(list) ? list : (list?.content || list?.data || []);
      setWorkspaces(wsList);
      
      const savedWsId = localStorage.getItem("fwork_active_workspace_id");
      let selectedWs = null;

      if (savedWsId) {
        selectedWs = wsList.find((w) => w.id === savedWsId);
      }
      if (!selectedWs && wsList.length > 0) {
        selectedWs = wsList[0];
      }

      if (selectedWs) {
        setCurrentWorkspace((prev) => {
          if (prev?.id === selectedWs.id) return prev;
          return selectedWs;
        });
      } else {
        setLoading(false);
      }
      return wsList;
    } catch (e) {
      console.error("Failed to load workspaces", e);
      setLoading(false);
      return [];
    }
  }, []);

  const switchWorkspace = useCallback((ws) => {
    if (!ws) return;
    setCurrentWorkspace(ws);
    localStorage.setItem("fwork_active_workspace_id", ws.id);
  }, []);

  const refreshBoards = useCallback(async (wsId) => {
    if (!wsId) {
      setBoards([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const boardList = await boardApi.list(wsId);
      const rawBoards = Array.isArray(boardList) ? boardList : (boardList?.data || []);
      setBoards(rawBoards);
    } catch (e) {
      console.error("Failed to load boards for workspace", e);
      setBoards([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const refresh = useCallback(async () => {
    const wsList = await refreshWorkspaces();
    const activeId = currentWorkspace?.id || wsList[0]?.id;
    if (activeId) {
      await refreshBoards(activeId);
    } else {
      setLoading(false);
    }
  }, [refreshWorkspaces, refreshBoards, currentWorkspace?.id]);

  useEffect(() => {
    refreshWorkspaces();
  }, [refreshWorkspaces]);

  useEffect(() => {
    if (currentWorkspace?.id) {
      refreshBoards(currentWorkspace.id);
    }
  }, [currentWorkspace?.id, refreshBoards]);

  const createWorkspace = useCallback(async (data) => {
    const newWs = await workspaceApi.create(data);
    setWorkspaces((prev) => [newWs, ...prev]);
    switchWorkspace(newWs);
    return newWs;
  }, [switchWorkspace]);

  const create = useCallback(
    async (data) => {
      const wsId = data?.workspaceId || currentWorkspace?.id;
      const board = await boardApi.create(wsId, data);
      setBoards((prev) => [board, ...prev]);
      return board;
    },
    [currentWorkspace]
  );

  const remove = useCallback(async (id) => {
    await boardApi.remove(id);
    setBoards((prev) => prev.filter((b) => b.id !== id));
  }, []);

  return (
    <BoardsContext.Provider
      value={{
        boards,
        workspaces,
        currentWorkspace,
        setCurrentWorkspace: switchWorkspace,
        switchWorkspace,
        createWorkspace,
        refreshWorkspaces,
        loading,
        refresh,
        create,
        remove,
      }}
    >
      {children}
    </BoardsContext.Provider>
  );
};

export const useBoards = () => {
  const ctx = useContext(BoardsContext);
  if (!ctx) throw new Error("useBoards must be used within BoardsProvider");
  return ctx;
};
