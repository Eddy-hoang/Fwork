// ----------------------------------------------------------------------------
// API layer — BOILERPLATE (mock) build
//
// The whole UI runs on local mock data so you can build the frontend before the
// backend exists. Components never change — they only import authApi/boardApi/
// taskApi/columnApi/aiApi/userApi from here.
//
//   👉  WHEN THE BACKEND IS READY:
//        1. Uncomment the "REAL API" block below (it's the actual axios code).
//        2. Delete — or comment out — the "MOCK API" block at the bottom.
//        3. Remove the `./mockData` import.
//      That's the only file you touch to go live.
// ----------------------------------------------------------------------------

import * as mock from "./mockData";

const TOKEN_KEY = "kanban_token";
export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (t) => localStorage.setItem(TOKEN_KEY, t);
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);

/* ============================================================================
 * REAL API  —  uncomment this whole block once the backend is built
 * ==========================================================================*/
/*
import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:5050/api",
});

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Normalize errors to a readable message; bounce to login on 401.
api.interceptors.response.use(
  (res) => res,
  (error) => {
    const message =
      error.response?.data?.error || error.message || "Something went wrong";
    if (error.response?.status === 401 && getToken()) {
      clearToken();
      if (!location.pathname.startsWith("/login")) location.assign("/login");
    }
    return Promise.reject(new Error(message));
  }
);

export default api;

export const authApi = {
  register: (data) => api.post("/auth/register", data).then((r) => r.data),
  login: (data) => api.post("/auth/login", data).then((r) => r.data),
  me: () => api.get("/auth/me").then((r) => r.data.user),
};

export const userApi = {
  search: (q) => api.get("/users/search", { params: { q } }).then((r) => r.data.users),
};

export const boardApi = {
  list: () => api.get("/boards").then((r) => r.data.boards),
  create: (data) => api.post("/boards", data).then((r) => r.data.board),
  get: (id) => api.get(`/boards/${id}`).then((r) => r.data),
  update: (id, data) => api.patch(`/boards/${id}`, data).then((r) => r.data.board),
  remove: (id) => api.delete(`/boards/${id}`).then((r) => r.data),
  activity: (id, limit = 30) =>
    api.get(`/boards/${id}/activity`, { params: { limit } }).then((r) => r.data.activities),
  addMember: (id, data) => api.post(`/boards/${id}/members`, data).then((r) => r.data.member),
  removeMember: (id, userId) => api.delete(`/boards/${id}/members/${userId}`).then((r) => r.data),
};

export const columnApi = {
  create: (boardId, data) =>
    api.post(`/boards/${boardId}/columns`, data).then((r) => r.data.column),
  update: (boardId, columnId, data) =>
    api.patch(`/boards/${boardId}/columns/${columnId}`, data).then((r) => r.data.column),
  remove: (boardId, columnId) =>
    api.delete(`/boards/${boardId}/columns/${columnId}`).then((r) => r.data),
};

export const taskApi = {
  list: (boardId, params) =>
    api.get(`/boards/${boardId}/tasks`, { params }).then((r) => r.data.tasks),
  create: (boardId, data) =>
    api.post(`/boards/${boardId}/tasks`, data).then((r) => r.data.task),
  update: (boardId, taskId, data) =>
    api.patch(`/boards/${boardId}/tasks/${taskId}`, data).then((r) => r.data.task),
  move: (boardId, taskId, data) =>
    api.patch(`/boards/${boardId}/tasks/${taskId}/move`, data).then((r) => r.data.task),
  remove: (boardId, taskId) =>
    api.delete(`/boards/${boardId}/tasks/${taskId}`).then((r) => r.data),
};

export const aiApi = {
  generateTasks: (boardId, data) =>
    api.post(`/boards/${boardId}/ai/generate-tasks`, data).then((r) => r.data),
  breakdown: (boardId, data) =>
    api.post(`/boards/${boardId}/ai/breakdown`, data).then((r) => r.data.subtasks),
  summary: (boardId) => api.post(`/boards/${boardId}/ai/summary`).then((r) => r.data.summary),
};
*/

/* ============================================================================
 * MOCK API  —  delete this block once the REAL API above is live.
 * Each method mirrors the real one's return shape, with a small delay so the
 * loading states still show.
 * ==========================================================================*/
const delay = (ms = 350) => new Promise((res) => setTimeout(res, ms));

export const authApi = {
  register: async () => {
    await delay();
    return { user: mock.currentUser, token: "mock-token" };
  },
  login: async () => {
    await delay();
    return { user: mock.currentUser, token: "mock-token" };
  },
  me: async () => {
    await delay(150);
    return mock.currentUser;
  },
};

export const userApi = {
  search: async (q) => {
    await delay();
    return mock.searchUsers(q);
  },
};

export const boardApi = {
  list: async () => {
    await delay();
    return mock.boards;
  },
  create: async (data) => {
    await delay();
    return mock.createBoard(data);
  },
  get: async (id) => {
    await delay();
    return mock.getBoardDetail(id);
  },
  update: async (id, data) => {
    await delay();
    return mock.updateBoard(id, data);
  },
  remove: async () => {
    await delay();
    return { success: true };
  },
  activity: async (id) => {
    await delay();
    return mock.activitiesFor(id);
  },
  addMember: async (id, data) => {
    await delay();
    return mock.addMember(data);
  },
  removeMember: async () => {
    await delay();
    return { success: true };
  },
};

export const columnApi = {
  create: async (boardId, data) => {
    await delay();
    return mock.createColumn(boardId, data);
  },
  update: async (boardId, columnId, data) => {
    await delay();
    return { id: columnId, board_id: boardId, ...data };
  },
  remove: async () => {
    await delay();
    return { success: true };
  },
};

export const taskApi = {
  list: async (boardId) => {
    await delay();
    return mock.getBoardDetail(boardId).tasks;
  },
  create: async (boardId, data) => {
    await delay();
    return mock.createTask(boardId, data);
  },
  update: async (boardId, taskId, data) => {
    await delay();
    return mock.updateTask(boardId, taskId, data);
  },
  move: async (boardId, taskId, data) => {
    await delay();
    return { id: taskId, board_id: boardId, ...data };
  },
  remove: async () => {
    await delay();
    return { success: true };
  },
};

export const aiApi = {
  generateTasks: async (boardId, data) => {
    await delay(900);
    return mock.aiGenerate(boardId, data);
  },
  breakdown: async () => {
    await delay(900);
    return mock.aiBreakdown();
  },
  summary: async () => {
    await delay(900);
    return mock.aiSummary();
  },
};
/* ======================================================================== */
