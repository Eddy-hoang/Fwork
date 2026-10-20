import React, { useState } from 'react';
import { LogIn, AlertCircle, Settings, Server, Mail, Lock, Eye, EyeOff, RotateCcw, Copy, Check } from 'lucide-react';
import { DEFAULT_API_BASE, DEFAULT_WEB_BASE } from '../api/client';
import { getGoogleIdToken, getGoogleRedirectUri, DEFAULT_GOOGLE_CLIENT_ID } from '../utils/googleAuth';

interface LoginViewProps {
  onLogin: (credentials: { email: string; password: string }, customApiUrl?: string, customWebUrl?: string) => Promise<any>;
  onGoogleLogin?: (idToken: string, customApiUrl?: string, customWebUrl?: string) => Promise<any>;
}

export const LoginView: React.FC<LoginViewProps> = ({ onLogin, onGoogleLogin }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [showConfig, setShowConfig] = useState(false);
  const [apiUrl, setApiUrl] = useState(DEFAULT_API_BASE);
  const [webUrl, setWebUrl] = useState(DEFAULT_WEB_BASE);
  const [copiedRedirect, setCopiedRedirect] = useState(false);

  const redirectUri = getGoogleRedirectUri();

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

  const handleGoogleLogin = async () => {
    if (!onGoogleLogin) return;

    setError(null);
    setGoogleLoading(true);

    try {
      const idToken = await getGoogleIdToken(DEFAULT_GOOGLE_CLIENT_ID);
      await onGoogleLogin(idToken, apiUrl.trim(), webUrl.trim());
    } catch (err: any) {
      const msg = err?.message || 'Google Sign-In failed.';
      if (msg.includes('redirect_uri_mismatch')) {
        setError(
          `Google redirect URI mismatch. Please add "${redirectUri}" to Authorized Redirect URIs in Google Cloud Console.`
        );
      } else {
        setError(msg);
      }
    } finally {
      setGoogleLoading(false);
    }
  };

  const handleCopyRedirect = () => {
    navigator.clipboard.writeText(redirectUri);
    setCopiedRedirect(true);
    setTimeout(() => setCopiedRedirect(false), 2000);
  };

  const handleResetUrls = () => {
    setApiUrl(DEFAULT_API_BASE);
    setWebUrl(DEFAULT_WEB_BASE);
  };

  return (
    <div className="app-shell">
      {/* Top Bar with Settings Toggle */}
      <header className="app-header">
        <div className="brand-badge">
          <div className="brand-icon">
            <img src="/icons/icon32-light.png" alt="Fwork" className="logo-light" />
            <img src="/icons/icon32-dark.png" alt="Fwork" className="logo-dark" />
          </div>
          <div className="brand-text-stack">
            <span className="brand-name">Fwork Companion</span>
            <span className="brand-subtext">Browser Extension</span>
          </div>
        </div>

        <button
          type="button"
          className="icon-button"
          title={showConfig ? 'Close Server Settings' : 'Configure Server URLs'}
          onClick={() => setShowConfig(!showConfig)}
          aria-label="Server settings"
        >
          <Settings size={16} />
        </button>
      </header>

      {/* Main Form Body */}
      <div className="app-body">
        {/* Brand Hero */}
        <div className="login-hero">
          <div className="login-hero-logo">
            <img src="/icons/icon128-light.png" alt="Fwork" className="logo-light" />
            <img src="/icons/icon128-dark.png" alt="Fwork" className="logo-dark" />
          </div>
          <h1 className="login-title">Sign in to Fwork</h1>
          <p className="login-desc">Capture webpages and save them as tasks in your workspace boards.</p>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="alert-box alert-error" role="alert">
            <AlertCircle size={15} style={{ flexShrink: 0, marginTop: 1 }} />
            <span>{error}</span>
          </div>
        )}

        {/* Collapsible Connection Settings */}
        {showConfig && (
          <div className="settings-drawer">
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 2 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontWeight: 600, fontSize: 11.5, color: 'var(--md-primary)' }}>
                <Server size={14} />
                <span>Connection Settings</span>
              </div>
              <button
                type="button"
                onClick={handleResetUrls}
                className="icon-button"
                style={{ padding: 3, fontSize: 10, display: 'flex', gap: 4, alignItems: 'center' }}
                title="Reset to default URLs"
              >
                <RotateCcw size={11} />
                <span>Reset</span>
              </button>
            </div>

            <div className="field-group">
              <label className="field-label" style={{ fontSize: 11 }}>API Base URL</label>
              <input
                type="text"
                className="input-control"
                style={{ fontSize: 11.5, padding: '7px 10px' }}
                value={apiUrl}
                onChange={(e) => setApiUrl(e.target.value)}
                placeholder="http://localhost:8080/api"
              />
            </div>

            <div className="field-group">
              <label className="field-label" style={{ fontSize: 11 }}>Web App URL</label>
              <input
                type="text"
                className="input-control"
                style={{ fontSize: 11.5, padding: '7px 10px' }}
                value={webUrl}
                onChange={(e) => setWebUrl(e.target.value)}
                placeholder="http://localhost:5173"
              />
            </div>

            <div className="field-group">
              <label className="field-label" style={{ fontSize: 11 }}>OAuth Redirect URI (Google Console)</label>
              <div className="mono-chip-box">
                <span className="mono-chip-text">{redirectUri}</span>
                <button
                  type="button"
                  className="icon-button"
                  style={{ padding: 2 }}
                  onClick={handleCopyRedirect}
                  title="Copy Redirect URI"
                >
                  {copiedRedirect ? <Check size={12} color="var(--md-success)" /> : <Copy size={12} />}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Email & Password Form */}
        <form id="login-form" onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {/* Email */}
          <div className="field-group">
            <label className="field-label" htmlFor="email">Email</label>
            <div className="input-container input-with-leading-icon">
              <Mail size={15} className="leading-icon" />
              <input
                id="email"
                type="email"
                className="input-control"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                disabled={loading || googleLoading}
              />
            </div>
          </div>

          {/* Password */}
          <div className="field-group">
            <label className="field-label" htmlFor="password">Password</label>
            <div className="input-container input-with-leading-icon input-with-trailing-icon">
              <Lock size={15} className="leading-icon" />
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                className="input-control"
                autoComplete="current-password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                disabled={loading || googleLoading}
              />
              <button
                type="button"
                className="trailing-btn"
                onClick={() => setShowPassword(!showPassword)}
                title={showPassword ? 'Hide password' : 'Show password'}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                tabIndex={-1}
              >
                {showPassword ? <EyeOff size={15} /> : <Eye size={15} />}
              </button>
            </div>
          </div>

          {/* Sign In with Email Action Button */}
          <button
            type="submit"
            className="btn-primary"
            disabled={loading || googleLoading || !email.trim() || !password}
            style={{ marginTop: 2 }}
          >
            {loading ? (
              <>
                <div className="spinner-dot" />
                <span>Signing in...</span>
              </>
            ) : (
              <>
                <LogIn size={16} />
                <span>Sign In with Email</span>
              </>
            )}
          </button>
        </form>

        {/* Divider */}
        <div className="login-divider" style={{ margin: '2px 0' }}>
          <span>or continue with</span>
        </div>

        {/* Google Sign-In Action (Placed at the bottom) */}
        <button
          type="button"
          className="btn-google"
          onClick={handleGoogleLogin}
          disabled={loading || googleLoading}
        >
          {googleLoading ? (
            <>
              <div className="spinner-dot spinner-primary" />
              <span>Connecting to Google...</span>
            </>
          ) : (
            <>
              <svg className="google-icon-svg" viewBox="0 0 24 24">
                <path
                  fill="#4285F4"
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                />
                <path
                  fill="#34A853"
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                />
                <path
                  fill="#FBBC05"
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                />
                <path
                  fill="#EA4335"
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                />
              </svg>
              <span>Continue with Google</span>
            </>
          )}
        </button>
      </div>
    </div>
  );
};
