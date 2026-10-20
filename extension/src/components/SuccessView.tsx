import React from 'react';
import { Check, ExternalLink, Plus, FolderKanban } from 'lucide-react';
import { TaskResponse } from '../api/types';

interface SuccessViewProps {
  task: TaskResponse;
  boardId: string;
  boardTitle?: string;
  columnName?: string;
  workspaceName?: string;
  webBaseUrl: string;
  onReset: () => void;
}

export const SuccessView: React.FC<SuccessViewProps> = ({
  task,
  boardId,
  boardTitle,
  columnName,
  workspaceName,
  webBaseUrl,
  onReset,
}) => {
  const boardUrl = `${webBaseUrl.replace(/\/+$/, '')}/board/${boardId}`;

  const handleOpenBoard = () => {
    if (typeof chrome !== 'undefined' && chrome.tabs && chrome.tabs.create) {
      chrome.tabs.create({ url: boardUrl });
    } else {
      window.open(boardUrl, '_blank');
    }
  };

  return (
    <div className="app-shell">
      {/* Sticky Header */}
      <header className="app-header">
        <div className="brand-badge">
          <div className="brand-icon">
            <img src="/icons/icon32-light.png" alt="Fwork" className="logo-light" />
            <img src="/icons/icon32-dark.png" alt="Fwork" className="logo-dark" />
          </div>
          <div className="brand-text-stack">
            <span className="brand-name">Fwork Companion</span>
            <span className="brand-subtext">Task Created</span>
          </div>
        </div>
      </header>

      {/* Main Success Body */}
      <div className="app-body">
        <div className="success-card">
          <div className="success-badge-icon" aria-hidden="true">
            <Check size={30} strokeWidth={2.7} />
          </div>

          <h2 className="success-headline">Saved to Fwork</h2>

          {/* Created Task Summary Box */}
          <div className="success-summary-box">
            <div className="success-task-title" title={task.title}>
              {task.title}
            </div>

            <div className="success-task-destination">
              <FolderKanban size={13} color="var(--md-primary)" />
              <span>Saved into</span>
              {workspaceName && (
                <>
                  <span className="destination-chip">{workspaceName}</span>
                  <span style={{ color: 'var(--md-outline-strong)' }}>›</span>
                </>
              )}
              {boardTitle ? (
                <span className="destination-chip">{boardTitle}</span>
              ) : (
                <span className="destination-chip">Board</span>
              )}
              {columnName && (
                <>
                  <span style={{ color: 'var(--md-outline-strong)' }}>›</span>
                  <span className="destination-chip">{columnName}</span>
                </>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Sticky Footer Actions */}
      <footer className="app-footer" style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        <button
          type="button"
          className="btn-primary"
          onClick={handleOpenBoard}
        >
          <ExternalLink size={16} />
          <span>Open Board in Fwork</span>
        </button>

        <button
          type="button"
          className="btn-secondary"
          onClick={onReset}
        >
          <Plus size={16} />
          <span>Capture Another Page</span>
        </button>
      </footer>
    </div>
  );
};
