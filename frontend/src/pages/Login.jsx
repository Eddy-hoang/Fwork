import { useState, useEffect, useCallback } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import toast from "react-hot-toast";
import { Zap } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { Input } from "../components/ui/Input";
import Button from "../components/ui/Button";
import AuthAside from "../components/auth/AuthAside";

const Login = () => {
  const { login, loginWithGoogle } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: "", password: "" });
  const [loading, setLoading] = useState(false);

  const handleGoogleSuccess = useCallback(async (response) => {
    setLoading(true);
    try {
      await loginWithGoogle(response.credential);
      toast.success("Welcome back!");
      navigate(location.state?.from?.pathname || "/dashboard", {
        replace: true,
      });
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  }, [loginWithGoogle, navigate, location.state]);

  useEffect(() => {
    const initializeGoogle = () => {
      if (window.google && window.google.accounts) {
        const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || "870630656041-3g8n9o6eb5g8n265n1b00e3u5p9p6b0p.apps.googleusercontent.com";
        window.google.accounts.id.initialize({
          client_id: googleClientId,
          callback: handleGoogleSuccess,
        });
        window.google.accounts.id.renderButton(
          document.getElementById("google-signin-button"),
          {
            theme: "outline",
            size: "large",
            width: 320,
            text: "signin_with",
            shape: "rectangular",
          }
        );
      } else {
        setTimeout(initializeGoogle, 100);
      }
    };

    initializeGoogle();
  }, [handleGoogleSuccess]);

  const fillDemo = () =>
    setForm({ email: "alex@timetoprogram.com", password: "Test@1234" });

  const onSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(form);
      toast.success("Welcome back!");
      navigate(location.state?.from?.pathname || "/dashboard", {
        replace: true,
      });
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen">
      <div className="flex w-full items-center justify-center px-4 py-10 lg:w-1/2">
        <div className="w-full max-w-sm animate-in">
          <Link
            to="/"
            className="mb-8 flex items-center justify-center gap-2.5 font-semibold"
          >
            <div className="brand-gradient flex h-10 w-10 items-center justify-center rounded-2xl shadow-[var(--shadow-brand)]">
              <Zap className="h-5 w-5 fill-white text-white" />
            </div>
            <span className="font-display text-lg font-bold tracking-tight">
              Fwork
            </span>
          </Link>

          <div className="card rounded-3xl p-8 shadow-[var(--shadow-soft)]">
            <h1 className="font-display text-2xl font-semibold tracking-tight">
              Welcome back
            </h1>
            <p className="mt-1.5 text-sm text-muted">
              Log in to your workspace.
            </p>

            <form onSubmit={onSubmit} className="mt-6 space-y-4">
              <Input
                id="email"
                label="Email"
                type="email"
                placeholder="you@company.com"
                autoComplete="email"
                required
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
              <Input
                id="password"
                label="Password"
                type="password"
                placeholder="••••••••"
                autoComplete="current-password"
                required
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
              />
              <Button
                type="submit"
                size="lg"
                className="w-full"
                loading={loading}
              >
                Log in
              </Button>
              
            </form>

            <div className="relative my-5 flex items-center justify-center">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-line"></div>
              </div>
              <div className="relative bg-surface px-3 text-[10px] uppercase text-faint font-semibold tracking-wider">
                Or continue with
              </div>
            </div>

            <div className="w-full flex justify-center">
              <div id="google-signin-button"></div>
            </div>
          </div>

          <p className="mt-5 text-center text-sm text-muted">
            New here?{" "}
            <Link
              to="/register"
              className="font-semibold text-brand-600 hover:text-brand-500"
            >
              Create an account
            </Link>
          </p>
        </div>
      </div>

      <AuthAside
        title="Welcome back to Fwork"
        subtitle="Log in and pick up right where you and your team left off."
      />
    </div>
  );
};

export default Login;
