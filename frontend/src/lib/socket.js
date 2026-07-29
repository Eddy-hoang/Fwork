// ----------------------------------------------------------------------------
// Realtime (Socket.IO) — BOILERPLATE stub
//
// Real-time sync is disabled until the backend Socket.IO server exists. This
// stub returns a no-op socket so every `getSocket().on(...)`, `.emit(...)`,
// `connectSocket()` etc. in the app keeps working without a server.
//
//   👉  WHEN THE BACKEND IS READY:
//        1. Uncomment the "REAL SOCKET" block below.
//        2. Delete — or comment out — the "MOCK SOCKET" block at the bottom.
// ----------------------------------------------------------------------------

/* ============================================================================
 * REAL SOCKET  —  uncomment once the backend is built
 * ==========================================================================*/
/*
import { io } from "socket.io-client";
import { getToken } from "./api";

const URL = import.meta.env.VITE_SOCKET_URL || "http://localhost:5050";

let socket = null;

// Lazily create (and authenticate) the shared socket connection.
export const getSocket = () => {
  if (!socket) {
    socket = io(URL, {
      autoConnect: false,
      auth: { token: getToken() },
      transports: ["websocket"],
    });
  }
  return socket;
};

export const connectSocket = () => {
  const s = getSocket();
  s.auth = { token: getToken() };
  if (!s.connected) s.connect();
  return s;
};

export const disconnectSocket = () => {
  if (socket) socket.disconnect();
};
*/

/* ============================================================================
 * MOCK SOCKET  —  delete this block once the REAL SOCKET above is live.
 * A chainable no-op so `.on().off().emit()` all work and never throw.
 * ==========================================================================*/
const noopSocket = {
  connected: false,
  on() {
    return this;
  },
  off() {
    return this;
  },
  emit() {
    return this;
  },
  connect() {
    return this;
  },
  disconnect() {
    return this;
  },
};

export const getSocket = () => noopSocket;
export const connectSocket = () => noopSocket;
export const disconnectSocket = () => {};
/* ======================================================================== */
