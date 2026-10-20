import { useState, useEffect } from 'react';

export interface ActiveTabInfo {
  title: string;
  url: string;
  favIconUrl?: string;
  loading: boolean;
}

export function useActiveTab(): ActiveTabInfo {
  const [tabInfo, setTabInfo] = useState<ActiveTabInfo>({
    title: '',
    url: '',
    favIconUrl: '',
    loading: true,
  });

  useEffect(() => {
    if (typeof chrome !== 'undefined' && chrome.tabs && chrome.tabs.query) {
      chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
        const active = tabs && tabs[0];
        if (active) {
          setTabInfo({
            title: active.title || '',
            url: active.url || '',
            favIconUrl: active.favIconUrl || '',
            loading: false,
          });
        } else {
          setTabInfo({ title: '', url: '', loading: false });
        }
      });
    } else {
      // Mock for standard browser development preview
      setTabInfo({
        title: document.title || 'Example Active Page',
        url: window.location.href || 'https://example.com/test-article',
        loading: false,
      });
    }
  }, []);

  return tabInfo;
}
