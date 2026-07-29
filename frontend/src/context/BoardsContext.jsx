import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { boardApi } from "../lib/api";

const BoardsContext = createContext(null);

export const BoardsProvider = ({ children }) => {
  const [boards, setBoards] = useState([]);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      setBoards(await boardApi.list());
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const create = useCallback(async (data) => {
    const board = await boardApi.create(data);
    setBoards((prev) => [board, ...prev]);
    return board;
  }, []);

  const remove = useCallback(async (id) => {
    await boardApi.remove(id);
    setBoards((prev) => prev.filter((b) => b.id !== id));
  }, []);

  return (
    <BoardsContext.Provider value={{ boards, loading, refresh, create, remove }}>
      {children}
    </BoardsContext.Provider>
  );
};

export const useBoards = () => {
  const ctx = useContext(BoardsContext);
  if (!ctx) throw new Error("useBoards must be used within BoardsProvider");
  return ctx;
};
