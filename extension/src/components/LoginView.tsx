import React, { useState } from 'react';
import { LogIn, AlertCircle, Settings, Server } from 'lucide-react';
import { DEFAULT_API_BASE, DEFAULT_WEB_BASE } from '../api/client';

interface LoginViewProps {
  onLogin: (credentials: { email: string; password: string }, customApiUrl?: string, customWebUrl?: string) => Promise<any>;
}

export const LoginView: React.FC<LoginViewProps> = ({ onLogin }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [showConfig, setShowConfig] = useState(false);
  const [apiUrl, setApiUrl] = useState(DEFAULT_API_BASE);
  const [webUrl, setWebUrl] = useState(DEFAULT_WEB_BASE);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password) {
      setError('Please enter both email and password.');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      await onLogin({ email: email.trim(), password }, apiUrl.trim(), webUrl.trim());
    } catch (err: any) {
      setError(err?.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="popup-container">
      <div className="popup-header">
        <div className="header-brand">
          <div className="header-logo">F</div>
          <div>
            <div className="header-title">Fwork Companion</div>
            <div style={{ fontSize: 11, color: 'var(--on-surface-variant)' }}>Sign in to continue</div>
          </div>
        </div>
        <button
          type="button"
          className="icon-btn"
          title="Server Settings"
          onClick={() => setShowConfig(!showConfig)}
        >
          <Settings size={16} />
        </button>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={16} style={{ flexShrink: 0, marginTop: 1 }} />
          <span>{error}</span>
        </div>
      )}

      {showConfig && (
        <div style={{ background: 'var(--surface)', padding: 10, borderRadius: 8, marginBottom: 12, border: '1px solid var(--outline-variant)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 8, fontWeight: 600, fontSize: 11 }}>
            <Server size={14} color="var(--primary)" />
            <span>Connection Settings</span>
          </div>
          <div className="form-group" style={{ marginBottom: 8 }}>
            <label className="form-label" style={{ fontSize: 11 }}>API Base URL</label>
            <input
              type="text"
              className="form-input"
              style={{ fontSize: 11, padding: '6px 8px' }}
              value={apiUrl}
              onChange={(e) => setApiUrl(e.target.value)}
              placeholder="http://localhost:8080/api"
            />
          </div>
          <div className="form-group" style={{ marginBottom: 4 }}>
            <label className="form-label" style={{ fontSize: 11 }}>Web App URL</label>
            <input
              type="text"
              className="form-input"
              style={{ fontSize: 11, padding: '6px 8px' }}
              value={webUrl}
              onChange={(e) => setWebUrl(e.target.value)}
              placeholder="http://localhost:5173"
            />
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label" htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            className="form-input"
            autoComplete="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            disabled={loading}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            className="form-input"
            autoComplete="current-password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            disabled={loading}
          />
        </div>

        <button type="submit" className="btn-primary" disabled={loading} style={{ marginTop: 12 }}>
          {loading ? (
            <>
              <div className="spinner" />
              <span>Signing in...</span>
            </>
          ) : (
            <>
              <LogIn size={16} />
              <span>Sign In</span>
            </>
          )}
        </button>
      </form>
    </div>
  );
};
