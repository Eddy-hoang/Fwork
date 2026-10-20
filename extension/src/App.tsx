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
    loginWithGoogle,
    logout,
    webBaseUrl,
  } = useAuth();

  const [createdTaskInfo, setCreatedTaskInfo] = useState<{
    task: TaskResponse;
    boardId: string;
    boardTitle?: string;
    columnName?: string;
    workspaceName?: string;
  } | null>(null);

  if (loading) {
    return (
      <div className="app-shell" style={{ alignItems: 'center', justifyContent: 'center', gap: 12 }}>
        <div className="spinner-dot spinner-primary" style={{ width: 24, height: 24, borderWidth: 3 }} />
        <span style={{ fontSize: 12, color: 'var(--md-outline-strong)', fontWeight: 500 }}>
          Loading Fwork Companion...
        </span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <LoginView
        onLogin={login}
        onGoogleLogin={loginWithGoogle}
      />
    );
  }

  if (createdTaskInfo) {
    return (
      <SuccessView
        task={createdTaskInfo.task}
        boardId={createdTaskInfo.boardId}
        boardTitle={createdTaskInfo.boardTitle}
        columnName={createdTaskInfo.columnName}
        workspaceName={createdTaskInfo.workspaceName}
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
      onSuccess={(task, boardId, boardTitle, columnName, workspaceName) => {
        setCreatedTaskInfo({ task, boardId, boardTitle, columnName, workspaceName });
      }}
    />
  );
};

export default App;
