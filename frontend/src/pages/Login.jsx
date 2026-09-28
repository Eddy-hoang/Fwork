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
    <div className="flex min-h-screen bg-zinc-50 dark:bg-zinc-950">
      <div className="flex w-full items-center justify-center px-4 py-10 lg:w-1/2">
        <div className="w-full max-w-sm">
          <Link
            to="/"
            className="mb-8 flex items-center justify-center gap-2 font-semibold"
          >
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-zinc-900 text-white dark:bg-zinc-50 dark:text-zinc-900 shadow-xs">
              <Zap className="h-4 w-4 fill-current" />
            </div>
            <span className="text-lg font-bold tracking-tight text-zinc-900 dark:text-zinc-50">
              Fwork
            </span>
          </Link>

          <div className="rounded-xl border border-zinc-200 bg-white p-6 shadow-xs dark:border-zinc-800 dark:bg-zinc-900">
            <h1 className="text-xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50">
              Welcome back
            </h1>
            <p className="mt-1 text-xs text-zinc-500 dark:text-zinc-400">
              Log in to your workspace.
            </p>

            <form onSubmit={onSubmit} className="mt-5 space-y-4">
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
                size="md"
                className="w-full mt-2"
                loading={loading}
              >
                Log in
              </Button>
            </form>

            <div className="relative my-5 flex items-center justify-center">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-zinc-200 dark:border-zinc-800"></div>
              </div>
              <div className="relative bg-white px-2 text-[10px] uppercase font-medium tracking-wider text-zinc-400 dark:bg-zinc-900 dark:text-zinc-500">
                Or continue with
              </div>
            </div>

            <div className="w-full flex justify-center">
              <div id="google-signin-button"></div>
            </div>
          </div>

          <p className="mt-5 text-center text-xs text-zinc-500 dark:text-zinc-400">
            New here?{" "}
            <Link
              to="/register"
              className="font-semibold text-zinc-900 underline underline-offset-4 dark:text-zinc-50"
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
