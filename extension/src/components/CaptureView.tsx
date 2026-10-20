import React, { useState, useEffect, useRef } from 'react';
import { Bookmark, Globe, AlertCircle, LogOut } from 'lucide-react';
import { api, AuthenticationError, ForbiddenError } from '../api/client';
import { BoardColumnResponse, BoardResponse, TaskResponse, UserResponse } from '../api/types';
import { useActiveTab } from '../hooks/useActiveTab';

interface CaptureViewProps {
  user?: UserResponse;
  onLogout: () => void;
  onAuthExpired: () => void;
  onSuccess: (task: TaskResponse, boardId: string) => void;
}

export const CaptureView: React.FC<CaptureViewProps> = ({
  user,
  onLogout,
  onAuthExpired,
  onSuccess,
}) => {
  const activeTab = useActiveTab();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [boards, setBoards] = useState<BoardResponse[]>([]);
  const [columns, setColumns] = useState<BoardColumnResponse[]>([]);
  const [selectedBoardId, setSelectedBoardId] = useState<string>('');
  const [selectedColumnId, setSelectedColumnId] = useState<string>('');

  const [loadingBoards, setLoadingBoards] = useState<boolean>(true);
  const [loadingColumns, setLoadingColumns] = useState<boolean>(false);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Sync tab title into editable title input once loaded
  const titleInitialized = useRef(false);
  useEffect(() => {
    if (!titleInitialized.current && activeTab.title) {
      setTitle(activeTab.title);
      titleInitialized.current = true;
    }
  }, [activeTab.title]);

  // Load Boards
  useEffect(() => {
    let isMounted = true;
    setLoadingBoards(true);
    setError(null);

    api.getBoards()
      .then((data) => {
        if (!isMounted) return;
        setBoards(data);
        if (data.length > 0) {
          setSelectedBoardId(data[0].id);
        }
      })
      .catch((err) => {
        if (!isMounted) return;
        if (err instanceof AuthenticationError) {
          onAuthExpired();
        } else if (err instanceof ForbiddenError) {
          setError('Permission denied when accessing boards.');
        } else {
          setError(err?.message || 'Failed to load boards.');
        }
      })
      .finally(() => {
        if (isMounted) setLoadingBoards(false);
      });

    return () => {
      isMounted = false;
    };
  }, [onAuthExpired]);

  // Load Columns whenever selectedBoardId changes
  useEffect(() => {
    if (!selectedBoardId) {
      setColumns([]);
      setSelectedColumnId('');
      return;
    }

    let isMounted = true;
    setLoadingColumns(true);

    api.getColumns(selectedBoardId)
      .then((data) => {
        if (!isMounted) return;
        setColumns(data);
        if (data.length > 0) {
          setSelectedColumnId(data[0].id);
        } else {
          setSelectedColumnId('');
        }
      })
      .catch((err) => {
        if (!isMounted) return;
        if (err instanceof AuthenticationError) {
          onAuthExpired();
        } else {
          setError(err?.message || 'Failed to load columns for this board.');
        }
      })
      .finally(() => {
        if (isMounted) setLoadingColumns(false);
      });

    return () => {
      isMounted = false;
    };
  }, [selectedBoardId, onAuthExpired]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (submitting) return; // Prevent duplicate submissions

    if (!title.trim()) {
      setError('Task title is required.');
      return;
    }

    if (!selectedColumnId) {
      setError('Please select a valid destination column.');
      return;
    }

    setError(null);
    setSubmitting(true);

    // Format description without inventing unsupported sourceUrl property
    const sourceText = activeTab.url ? `Source: ${activeTab.url}` : '';
    const finalDescription = description.trim()
      ? (sourceText ? `${description.trim()}\n\n${sourceText}` : description.trim())
      : sourceText || null;

    try {
      const task = await api.createTask(selectedColumnId, {
        title: title.trim(),
        description: finalDescription,
      });

      onSuccess(task, selectedBoardId);
    } catch (err: any) {
      if (err instanceof AuthenticationError) {
        onAuthExpired();
      } else if (err instanceof ForbiddenError) {
        setError('Permission denied: You cannot create tasks in this column.');
      } else {
        setError(err?.message || 'Failed to create task. Submission result is uncertain.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="popup-container">
      {/* Header */}
      <div className="popup-header">
        <div className="header-brand">
          <div className="header-logo">F</div>
          <div>
            <div className="header-title">Save to Fwork</div>
            {user && (
              <div className="header-user">
                <span>{user.name || user.email}</span>
              </div>
            )}
          </div>
        </div>
        <button
          type="button"
          className="icon-btn"
          title="Sign out"
          onClick={onLogout}
        >
          <LogOut size={16} />
        </button>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={16} style={{ flexShrink: 0, marginTop: 1 }} />
          <span>{error}</span>
        </div>
      )}

      {/* Captured page pill */}
      {activeTab.url && (
        <div className="page-badge">
          <Globe size={14} className="page-badge-icon" />
          <div className="page-badge-content">
            <div className="page-badge-title">{activeTab.title || 'Untitled Page'}</div>
            <div className="page-badge-url">{activeTab.url}</div>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="task-title">Task Title *</label>
          <input
            id="task-title"
            type="text"
            className="form-input"
            maxLength={255}
            required
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="What needs to be done?"
            disabled={submitting}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="task-desc">Notes (Optional)</label>
          <textarea
            id="task-desc"
            className="form-textarea"
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Add context or notes about this page..."
            disabled={submitting}
          />
        </div>

        <div className="row-selectors">
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label" htmlFor="board-select">Board</label>
            <select
              id="board-select"
              className="form-select"
              value={selectedBoardId}
              onChange={(e) => setSelectedBoardId(e.target.value)}
              disabled={loadingBoards || submitting || boards.length === 0}
            >
              {loadingBoards ? (
                <option>Loading...</option>
              ) : boards.length === 0 ? (
                <option value="">No boards found</option>
              ) : (
                boards.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.title}
                  </option>
                ))
              )}
            </select>
          </div>

          <div className="form-group" style={{ marginBottom: 0 }}>
            <label className="form-label" htmlFor="column-select">Column</label>
            <select
              id="column-select"
              className="form-select"
              value={selectedColumnId}
              onChange={(e) => setSelectedColumnId(e.target.value)}
              disabled={loadingColumns || submitting || columns.length === 0}
            >
              {loadingColumns ? (
                <option>Loading...</option>
              ) : columns.length === 0 ? (
                <option value="">No columns</option>
              ) : (
                columns.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))
              )}
            </select>
          </div>
        </div>

        <button
          type="submit"
          className="btn-primary"
          disabled={submitting || loadingBoards || loadingColumns || !selectedColumnId || !title.trim()}
        >
          {submitting ? (
            <>
              <div className="spinner" />
              <span>Saving task...</span>
            </>
          ) : (
            <>
              <Bookmark size={15} />
              <span>Add to Fwork</span>
            </>
          )}
        </button>
      </form>
    </div>
  );
};
