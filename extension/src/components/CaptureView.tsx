import React, { useState, useEffect, useRef, useMemo } from 'react';
import { Bookmark, Globe, AlertCircle, LogOut, X, ChevronDown, Layers, ExternalLink, User } from 'lucide-react';
import { api, AuthenticationError, ForbiddenError } from '../api/client';
import { BoardColumnResponse, BoardResponse, TaskResponse, UserResponse } from '../api/types';
import { useActiveTab } from '../hooks/useActiveTab';

interface CaptureViewProps {
  user?: UserResponse;
  onLogout: () => void;
  onAuthExpired: () => void;
  onSuccess: (task: TaskResponse, boardId: string, boardTitle?: string, columnName?: string) => void;
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
  const [faviconError, setFaviconError] = useState<boolean>(false);

  // Derive domain from active tab URL
  const domain = useMemo(() => {
    if (!activeTab.url) return '';
    try {
      const parsed = new URL(activeTab.url);
      return parsed.hostname.replace(/^www\./, '');
    } catch {
      return '';
    }
  }, [activeTab.url]);

  // Sync tab title into editable title input once loaded
  const titleInitialized = useRef(false);
  useEffect(() => {
    if (!titleInitialized.current && activeTab.title) {
      setTitle(activeTab.title);
      titleInitialized.current = true;
    }
  }, [activeTab.title]);

  // Load Boards on mount
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
    if (submitting) return;

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

    // Format description preserving URL append standard
    const sourceText = activeTab.url ? `Source: ${activeTab.url}` : '';
    const finalDescription = description.trim()
      ? (sourceText ? `${description.trim()}\n\n${sourceText}` : description.trim())
      : sourceText || null;

    try {
      const task = await api.createTask(selectedColumnId, {
        title: title.trim(),
        description: finalDescription,
      });

      const currentBoard = boards.find((b) => b.id === selectedBoardId);
      const currentColumn = columns.find((c) => c.id === selectedColumnId);
      onSuccess(task, selectedBoardId, currentBoard?.title, currentColumn?.name);
    } catch (err: any) {
      if (err instanceof AuthenticationError) {
        onAuthExpired();
      } else if (err instanceof ForbiddenError) {
        setError('Permission denied: You cannot create tasks in this column.');
      } else {
        setError(err?.message || 'Failed to create task. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="app-shell">
      {/* Sticky Header */}
      <header className="app-header">
        <div className="brand-badge">
          <div className="brand-icon">
            <img src="/icons/icon32.png" alt="Fwork" />
          </div>
          <div className="brand-text-stack">
            <span className="brand-name">Fwork Companion</span>
            {user && (
              <span className="user-pill" title={user.email}>
                <User size={10} />
                <span>{user.name || user.email.split('@')[0]}</span>
              </span>
            )}
          </div>
        </div>

        <div className="header-actions">
          <button
            type="button"
            className="icon-button"
            title="Sign out"
            onClick={onLogout}
            aria-label="Sign out"
          >
            <LogOut size={16} />
          </button>
        </div>
      </header>

      {/* Scrollable Form Body */}
      <form id="capture-form" onSubmit={handleSubmit} className="app-body">
        {/* Error Alert */}
        {error && (
          <div className="alert-box alert-error" role="alert">
            <AlertCircle size={15} style={{ flexShrink: 0, marginTop: 1 }} />
            <span>{error}</span>
          </div>
        )}

        {/* Captured Webpage Preview Hero Card */}
        {activeTab.url && (
          <div className="webpage-hero-card">
            <div className="webpage-favicon-box">
              {activeTab.favIconUrl && !faviconError ? (
                <img
                  src={activeTab.favIconUrl}
                  alt=""
                  onError={() => setFaviconError(true)}
                />
              ) : (
                <Globe size={14} color="var(--md-primary)" />
              )}
            </div>

            <div className="webpage-details">
              <div className="webpage-title-line" title={activeTab.title}>
                {activeTab.title || 'Active Webpage'}
              </div>
              <div className="webpage-url-line" title={activeTab.url}>
                {domain ? <span className="domain-pill">{domain}</span> : null}
                <span style={{ fontSize: 10, color: 'var(--md-outline-strong)' }}>
                  <ExternalLink size={9} style={{ display: 'inline', verticalAlign: 'middle', marginRight: 2 }} />
                  {activeTab.url.replace(/^https?:\/\//, '')}
                </span>
              </div>
            </div>
          </div>
        )}

        {/* Task Title Field */}
        <div className="field-group">
          <div className="field-header">
            <label className="field-label" htmlFor="task-title">
              Task Title <span style={{ color: 'var(--md-error)' }}>*</span>
            </label>
            <span className={`field-counter ${title.length > 240 ? 'warning' : ''}`}>
              {title.length}/255
            </span>
          </div>
          <div className="input-container input-with-trailing-icon">
            <input
              id="task-title"
              type="text"
              className="input-control"
              maxLength={255}
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="What needs to be done?"
              disabled={submitting}
              autoFocus
            />
            {title && !submitting && (
              <button
                type="button"
                className="trailing-btn"
                title="Clear title"
                onClick={() => setTitle('')}
                aria-label="Clear task title"
              >
                <X size={14} />
              </button>
            )}
          </div>
        </div>

        {/* Destination Group: Board & Column */}
        <div className="destination-panel">
          <div className="destination-header">
            <Layers size={13} />
            <span>DESTINATION</span>
          </div>

          <div className="destination-grid">
            {/* Board Selector */}
            <div className="field-group">
              <label className="field-label" htmlFor="board-select">Board</label>
              <div className="select-wrapper">
                <select
                  id="board-select"
                  className="select-control"
                  value={selectedBoardId}
                  onChange={(e) => setSelectedBoardId(e.target.value)}
                  disabled={loadingBoards || submitting || boards.length === 0}
                >
                  {loadingBoards ? (
                    <option>Loading boards...</option>
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
                <ChevronDown size={14} className="select-chevron" />
              </div>
            </div>

            {/* Column Selector */}
            <div className="field-group">
              <label className="field-label" htmlFor="column-select">Column</label>
              <div className="select-wrapper">
                <select
                  id="column-select"
                  className="select-control"
                  value={selectedColumnId}
                  onChange={(e) => setSelectedColumnId(e.target.value)}
                  disabled={loadingColumns || submitting || columns.length === 0}
                >
                  {loadingColumns ? (
                    <option>Loading columns...</option>
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
                <ChevronDown size={14} className="select-chevron" />
              </div>
            </div>
          </div>
        </div>

        {/* Notes (Optional) */}
        <div className="field-group">
          <div className="field-header">
            <label className="field-label" htmlFor="task-desc">Notes (Optional)</label>
          </div>
          <textarea
            id="task-desc"
            className="input-control textarea-control"
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Add context, thoughts, or acceptance criteria..."
            disabled={submitting}
          />
          <span style={{ fontSize: 10.5, color: 'var(--md-outline-strong)', marginTop: 2 }}>
            Source URL is attached to the description automatically.
          </span>
        </div>
      </form>

      {/* Sticky Pinned Footer Action */}
      <footer className="app-footer">
        <button
          form="capture-form"
          type="submit"
          className="btn-primary"
          disabled={submitting || loadingBoards || loadingColumns || !selectedColumnId || !title.trim()}
        >
          {submitting ? (
            <>
              <div className="spinner-dot" />
              <span>Saving to Fwork...</span>
            </>
          ) : (
            <>
              <Bookmark size={16} />
              <span>Add to Fwork</span>
            </>
          )}
        </button>
      </footer>
    </div>
  );
};
