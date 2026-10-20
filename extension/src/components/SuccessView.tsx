import React from 'react';
import { CheckCircle2, ExternalLink, Plus } from 'lucide-react';
import { TaskResponse } from '../api/types';

interface SuccessViewProps {
  task: TaskResponse;
  boardId: string;
  webBaseUrl: string;
  onReset: () => void;
}

export const SuccessView: React.FC<SuccessViewProps> = ({
  task,
  boardId,
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
    <div className="popup-container">
      <div className="popup-header">
        <div className="header-brand">
          <div className="header-logo">F</div>
          <div className="header-title">Task Created</div>
        </div>
      </div>

      <div className="success-card">
        <div className="success-icon-wrap">
          <CheckCircle2 size={28} />
        </div>

        <div className="success-title">Saved to Fwork!</div>
        
        <div className="success-desc">
          <strong>"{task.title}"</strong> has been added to your board.
        </div>

        <div className="success-actions">
          <button type="button" className="btn-primary" onClick={handleOpenBoard}>
            <ExternalLink size={15} />
            <span>Open in Fwork</span>
          </button>

          <button type="button" className="btn-secondary" onClick={onReset}>
            <Plus size={15} />
            <span>Capture Another</span>
          </button>
        </div>
      </div>
    </div>
  );
};
