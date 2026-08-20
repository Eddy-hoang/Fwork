import { Client } from "@stomp/stompjs";
import { getToken } from "./api";

let stompClient = null;

export const getStompClient = () => {
  if (!stompClient) {
    const wsProtocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const defaultWsUrl = window.location.port === "5173"
      ? "ws://localhost:8080/ws"
      : `${wsProtocol}//${window.location.host}/ws`;
    const wsUrl = import.meta.env.VITE_WS_URL || defaultWsUrl;
    stompClient = new Client({
      brokerURL: wsUrl,
      connectHeaders: {
        Authorization: `Bearer ${getToken()}`,
      },
      debug: (str) => {
        if (import.meta.env.DEV) console.log("[STOMP]", str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });
  }
  return stompClient;
};

export const connectSocket = () => {
  const client = getStompClient();
  const token = getToken();
  if (token) {
    client.connectHeaders = {
      Authorization: `Bearer ${token}`,
    };
  }
  if (!client.active) {
    client.activate();
  }
  return client;
};

export const disconnectSocket = () => {
  if (stompClient && stompClient.active) {
    stompClient.deactivate();
  }
};

export const subscribeBoard = (boardId, onEventCallback) => {
  const client = connectSocket();
  
  const setupSubscription = () => {
    return client.subscribe(`/topic/boards/${boardId}`, (message) => {
      try {
        const event = JSON.parse(message.body);
        if (onEventCallback) {
          onEventCallback(event);
        }
      } catch (err) {
        console.error("Failed to parse STOMP message:", err);
      }
    });
  };

  if (client.connected) {
    return setupSubscription();
  } else {
    let sub = null;
    const previousOnConnect = client.onConnect;
    client.onConnect = (frame) => {
      if (previousOnConnect) previousOnConnect(frame);
      sub = setupSubscription();
    };
    return {
      unsubscribe: () => {
        if (sub) sub.unsubscribe();
      },
    };
  }
};

// Legacy stubs for smooth compatibility with existing component imports
export const getSocket = () => ({
  connected: stompClient ? stompClient.connected : false,
  on: () => {},
  off: () => {},
  emit: () => {},
});
