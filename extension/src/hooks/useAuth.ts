import { useEffect, useState, useCallback } from 'react';
import { api, DEFAULT_API_BASE, DEFAULT_WEB_BASE } from '../api/client';
import { ExtensionSession, LoginRequest, UserResponse } from '../api/types';

const STORAGE_KEY = 'fwork_extension_session';

async function getStoredSession(): Promise<ExtensionSession | null> {
  if (typeof chrome !== 'undefined' && chrome.storage && chrome.storage.local) {
    return new Promise((resolve) => {
      chrome.storage.local.get([STORAGE_KEY], (result) => {
        resolve(result[STORAGE_KEY] || null);
      });
    });
  }
  const raw = localStorage.getItem(STORAGE_KEY);
  return raw ? JSON.parse(raw) : null;
}

async function setStoredSession(session: ExtensionSession | null): Promise<void> {
  if (typeof chrome !== 'undefined' && chrome.storage && chrome.storage.local) {
    return new Promise((resolve) => {
      if (session) {
        chrome.storage.local.set({ [STORAGE_KEY]: session }, () => resolve());
      } else {
        chrome.storage.local.remove([STORAGE_KEY], () => resolve());
      }
    });
  }
  if (session) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  } else {
    localStorage.removeItem(STORAGE_KEY);
  }
}

export function useAuth() {
  const [session, setSession] = useState<ExtensionSession | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    getStoredSession()
      .then((stored) => {
        if (stored?.token) {
          api.setToken(stored.token);
          api.setBaseUrl(stored.apiBaseUrl || DEFAULT_API_BASE);
          setSession(stored);
        }
      })
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (credentials: LoginRequest, customApiUrl?: string, customWebUrl?: string) => {
    const targetApi = customApiUrl || session?.apiBaseUrl || DEFAULT_API_BASE;
    const targetWeb = customWebUrl || session?.webBaseUrl || DEFAULT_WEB_BASE;
    api.setBaseUrl(targetApi);

    const res = await api.login(credentials);
    const newSession: ExtensionSession = {
      token: res.accessToken,
      user: res.user,
      apiBaseUrl: targetApi,
      webBaseUrl: targetWeb,
    };

    api.setToken(res.accessToken);
    await setStoredSession(newSession);
    setSession(newSession);
    return res;
  }, [session]);

  const loginWithGoogle = useCallback(async (idToken: string, customApiUrl?: string, customWebUrl?: string) => {
    const targetApi = customApiUrl || session?.apiBaseUrl || DEFAULT_API_BASE;
    const targetWeb = customWebUrl || session?.webBaseUrl || DEFAULT_WEB_BASE;
    api.setBaseUrl(targetApi);

    const res = await api.loginWithGoogle(idToken);
    const newSession: ExtensionSession = {
      token: res.accessToken,
      user: res.user,
      apiBaseUrl: targetApi,
      webBaseUrl: targetWeb,
    };

    api.setToken(res.accessToken);
    await setStoredSession(newSession);
    setSession(newSession);
    return res;
  }, [session]);

  const logout = useCallback(async () => {
    api.setToken(null);
    await setStoredSession(null);
    setSession(null);
  }, []);

  const updateUrls = useCallback(async (apiBaseUrl: string, webBaseUrl: string) => {
    if (!session) return;
    const updated: ExtensionSession = {
      ...session,
      apiBaseUrl,
      webBaseUrl,
    };
    api.setBaseUrl(apiBaseUrl);
    await setStoredSession(updated);
    setSession(updated);
  }, [session]);

  return {
    session,
    user: session?.user as UserResponse | undefined,
    isAuthenticated: Boolean(session?.token),
    loading,
    login,
    loginWithGoogle,
    logout,
    updateUrls,
    webBaseUrl: session?.webBaseUrl || DEFAULT_WEB_BASE,
  };
}
