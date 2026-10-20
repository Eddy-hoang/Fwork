import React, { useState } from 'react';
import { useAuth } from './hooks/useAuth';
import { LoginView } from './components/LoginView';
import { CaptureView } from './components/CaptureView';
import { SuccessView } from './components/SuccessView';
import { TaskResponse } from './api/types';

export const App: React.FC = () => {
  const {
    user,
    isAuthenticated,
    loading,
    login,
    logout,
    webBaseUrl,
  } = useAuth();

  const [createdTaskInfo, setCreatedTaskInfo] = useState<{
    task: TaskResponse;
    boardId: string;
  } | null>(null);

  if (loading) {
    return (
      <div className="popup-container centered-loading">
        <div className="spinner" style={{ borderColor: 'var(--primary)', borderTopColor: 'transparent', width: 24, height: 24 }} />
        <span>Loading Fwork Companion...</span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <LoginView
        onLogin={login}
      />
    );
  }

  if (createdTaskInfo) {
    return (
      <SuccessView
        task={createdTaskInfo.task}
        boardId={createdTaskInfo.boardId}
        webBaseUrl={webBaseUrl}
        onReset={() => setCreatedTaskInfo(null)}
      />
    );
  }

  return (
    <CaptureView
      user={user}
      onLogout={logout}
      onAuthExpired={logout}
      onSuccess={(task, boardId) => {
        setCreatedTaskInfo({ task, boardId });
      }}
    />
  );
};

export default App;
