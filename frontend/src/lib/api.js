import axios from "axios";

const TOKEN_KEY = "kanban_token";
export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (t) => localStorage.setItem(TOKEN_KEY, t);
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Unwrap backend ApiResponse<T> format: { success, status, message, data }
api.interceptors.response.use(
  (res) => {
    // If backend returns standard ApiResponse wrapper, extract data field if success is true
    if (res.data && typeof res.data === "object" && "success" in res.data) {
      if (res.data.success === false) {
        return Promise.reject(new Error(res.data.message || "Request failed"));
      }
      return res.data.data !== undefined ? res.data.data : res.data;
    }
    return res.data;
  },
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      "Something went wrong";

    if (error.response?.status === 401 && getToken()) {
      clearToken();
      if (!window.location.pathname.startsWith("/login")) {
        window.location.assign("/login");
      }
    }
    return Promise.reject(new Error(message));
  }
);

export default api;

export const authApi = {
  register: (data) =>
    api.post("/auth/register", data).then((res) => {
      const token = res?.accessToken || res?.token;
      if (token) setToken(token);
      return { token, user: res?.user || res };
    }),
  login: (data) =>
    api.post("/auth/login", data).then((res) => {
      const token = res?.accessToken || res?.token;
      if (token) setToken(token);
      return { token, user: res?.user || res };
    }),
  me: () => api.get("/auth/me"),
};

export const workspaceApi = {
  list: () => api.get("/workspaces"),
  get: (id) => api.get(`/workspaces/${id}`),
  create: (data) => api.post("/workspaces", data),
  update: (id, data) => api.patch(`/workspaces/${id}`, data),
  remove: (id) => api.delete(`/workspaces/${id}`),
  getMembers: (id) => api.get(`/workspaces/${id}/members`),
  addMember: (id, data) => api.post(`/workspaces/${id}/members`, data),
  removeMember: (workspaceId, userId) => api.delete(`/workspaces/${workspaceId}/members/${userId}`),
  sendInvitation: (id, data) => api.post(`/workspaces/${id}/invitations`, data),
  acceptInvitation: (token) => api.post(`/invitations/accept?token=${token}`),
  updateMemberRole: (id, data) => api.patch(`/workspaces/${id}/members/role`, data),
  transferOwnership: (id, data) => api.post(`/workspaces/${id}/transfer-ownership`, data),
};

export const boardApi = {
  list: (workspaceId) =>
    workspaceId && workspaceId !== "undefined"
      ? api.get(`/workspaces/${workspaceId}/boards`)
      : api.get("/boards"),
  create: (workspaceIdOrData, data) => {
    if (workspaceIdOrData && typeof workspaceIdOrData === "string" && workspaceIdOrData !== "undefined") {
      return api.post(`/workspaces/${workspaceIdOrData}/boards`, data);
    }
    return api.post("/boards", workspaceIdOrData || data);
  },
  get: (id) => api.get(`/boards/${id}`),
  update: (id, data) => api.patch(`/boards/${id}`, data),
  remove: (id) => api.delete(`/boards/${id}`),
  activity: (id, params) => api.get(`/boards/${id}/activities`, { params }),
};

export const columnApi = {
  list: (boardId) => api.get(`/boards/${boardId}/columns`),
  create: (boardId, data) => api.post(`/boards/${boardId}/columns`, data),
  update: (columnId, data) => api.patch(`/columns/${columnId}`, data),
  remove: (columnId) => api.delete(`/columns/${columnId}`),
};

export const taskApi = {
  listByColumn: (columnId) => api.get(`/columns/${columnId}/tasks`),
  get: (id) => api.get(`/tasks/${id}`),
  create: (columnId, data) => api.post(`/columns/${columnId}/tasks`, data),
  update: (id, data) => api.patch(`/tasks/${id}`, data),
  move: (id, data) => api.patch(`/tasks/${id}/move`, data),
  assign: (id, data) => api.patch(`/tasks/${id}/assign`, data),
  remove: (id) => api.delete(`/tasks/${id}`),
};

export const commentApi = {
  list: (taskId, params) => api.get(`/tasks/${taskId}/comments`, { params }),
  create: (taskId, data) => api.post(`/tasks/${taskId}/comments`, data),
  remove: (id) => api.delete(`/comments/${id}`),
};

export const notificationApi = {
  list: (params) => api.get("/notifications", { params }),
  getUnreadCount: () => api.get("/notifications/unread-count"),
  markAsRead: (id) => api.patch(`/notifications/${id}/read`),
  markAllAsRead: () => api.patch("/notifications/read-all"),
  remove: (id) => api.delete(`/notifications/${id}`),
};

export const aiApi = {
  generateTasks: (boardId, data) =>
    api.post(`/boards/${boardId}/ai/generate-tasks`, data).catch(() => ({ tasks: [] })),
  breakdown: (boardId, data) =>
    api.post(`/boards/${boardId}/ai/breakdown`, data).catch(() => []),
  summary: (boardId) =>
    api.post(`/boards/${boardId}/ai/summary`).catch(() => ({ summary: "AI Summary unavailable" })),
};
