import { createContext, useContext, useEffect, useState, useCallback } from "react";
import { authApi, setToken, clearToken, getToken } from "../lib/api";
import { connectSocket, disconnectSocket } from "../lib/socket";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Restore session on first load if a token is present.
  useEffect(() => {
    const token = getToken();
    if (!token) {
      setLoading(false);
      return;
    }
    authApi
      .me()
      .then((u) => {
        setUser(u);
        connectSocket();
      })
      .catch(() => clearToken())
      .finally(() => setLoading(false));
  }, []);

  const handleAuth = useCallback(({ user, token }) => {
    setToken(token);
    setUser(user);
    connectSocket();
    return user;
  }, []);

  const login = useCallback(
    async (data) => handleAuth(await authApi.login(data)),
    [handleAuth]
  );

  const register = useCallback(
    async (data) => handleAuth(await authApi.register(data)),
    [handleAuth]
  );

  const logout = useCallback(() => {
    clearToken();
    disconnectSocket();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};
