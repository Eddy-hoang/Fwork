import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { Zap } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { Input } from "../components/ui/Input";
import Button from "../components/ui/Button";
import AuthAside from "../components/auth/AuthAside";

const Register = () => {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    if (form.password.length < 6) return toast.error("Password must be at least 6 characters");
    setLoading(true);
    try {
      await register(form);
      toast.success("Account created!");
      navigate("/dashboard", { replace: true });
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
        <Link to="/" className="mb-8 flex items-center justify-center gap-2.5 font-semibold">
          <div className="brand-gradient flex h-10 w-10 items-center justify-center rounded-2xl shadow-[var(--shadow-brand)]">
            <Zap className="h-5 w-5 fill-white text-white" />
          </div>
          <span className="font-display text-lg font-bold tracking-tight">Flowboard</span>
        </Link>

        <div className="card rounded-3xl p-8 shadow-[var(--shadow-soft)]">
          <h1 className="font-display text-2xl font-semibold tracking-tight">Create your account</h1>
          <p className="mt-1.5 text-sm text-muted">Start managing projects with AI.</p>

          <form onSubmit={onSubmit} className="mt-6 space-y-4">
            <Input
              id="name"
              label="Full name"
              placeholder="Ada Lovelace"
              autoComplete="name"
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
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
              placeholder="At least 6 characters"
              autoComplete="new-password"
              required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
            <Button type="submit" size="lg" className="w-full" loading={loading}>
              Create account
            </Button>
          </form>
        </div>

          <p className="mt-5 text-center text-sm text-muted">
            Already have an account?{" "}
            <Link to="/login" className="font-semibold text-brand-600 hover:text-brand-500">
              Log in
            </Link>
          </p>
        </div>
      </div>

      <AuthAside
        title="Start shipping with AI"
        subtitle="Join 2,500+ teams turning one-line goals into shipped work."
      />
    </div>
  );
};

export default Register;
