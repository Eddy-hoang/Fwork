import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { LogOut, Command, Zap, FolderKanban, CheckSquare, Users } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { useLayout } from "../components/layout/AppLayout";
import { useWorkspace } from "../hooks/useWorkspace";
import Topbar from "../components/layout/Topbar";
import Button from "../components/ui/Button";
import Avatar from "../components/ui/Avatar";
import { cn } from "../lib/utils";

const Switch = ({ checked, onChange }) => (
  <button
    type="button"
    role="switch"
    aria-checked={checked}
    onClick={() => onChange(!checked)}
    className={cn(
      "relative h-6 w-11 shrink-0 rounded-full transition-colors duration-200",
      checked ? "bg-brand-500" : "bg-elevated"
    )}
  >
    <span
      className={cn(
        "absolute top-0.5 h-5 w-5 rounded-full bg-white shadow-[var(--shadow-card)] transition-transform duration-200",
        checked ? "translate-x-[22px]" : "translate-x-0.5"
      )}
    />
  </button>
);

const Card = ({ title, description, children }) => (
  <section className="rounded-3xl border border-line bg-surface p-6 shadow-[var(--shadow-card)]">
    <h3 className="font-display text-sm font-semibold tracking-tight">{title}</h3>
    {description && <p className="mt-1 text-xs text-muted">{description}</p>}
    <div className="mt-5">{children}</div>
  </section>
);

const Settings = () => {
  const { user, logout } = useAuth();
  const { openCreateBoard } = useLayout();
  const { boards, tasks, members } = useWorkspace();
  const navigate = useNavigate();

  const [reduceMotion, setReduceMotion] = useState(
    () => localStorage.getItem("pref-reduced-motion") === "true"
  );

  useEffect(() => {
    document.documentElement.dataset.reduceMotion = reduceMotion ? "true" : "false";
    localStorage.setItem("pref-reduced-motion", String(reduceMotion));
  }, [reduceMotion]);

  return (
    <>
      <Topbar title="Settings" subtitle="Profile and preferences" onCreateBoard={openCreateBoard} />

      <div className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-3xl space-y-5 px-6 py-8 md:px-8">
          {/* Profile */}
          <Card title="Profile" description="How you appear across your workspace.">
            <div className="flex items-center gap-4">
              <Avatar name={user?.name} id={user?.id} src={user?.avatar_url} size="lg" className="h-16 w-16 text-lg" />
              <div className="min-w-0">
                <p className="font-display text-lg font-semibold tracking-tight">{user?.name}</p>
                <p className="truncate text-sm text-muted">{user?.email}</p>
              </div>
            </div>
          </Card>

          {/* Workspace */}
          <Card title="Workspace" description="Your activity at a glance.">
            <div className="grid grid-cols-3 gap-3">
              <Metric icon={FolderKanban} label="Boards" value={boards.length} tint="#2f8159" />
              <Metric icon={CheckSquare} label="Tasks" value={tasks.length} tint="#0ea5e9" />
              <Metric icon={Users} label="People" value={members.length} tint="#10b981" />
            </div>
          </Card>

          {/* Preferences */}
          <Card title="Preferences" description="Saved to this browser.">
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-sm font-medium text-ink">Reduce motion</p>
                <p className="mt-0.5 text-xs text-muted">Minimize animations and transitions across the app.</p>
              </div>
              <Switch checked={reduceMotion} onChange={setReduceMotion} />
            </div>
            <div className="mt-5 flex items-center justify-between gap-4 border-t pt-5">
              <div>
                <p className="text-sm font-medium text-ink">Command menu</p>
                <p className="mt-0.5 text-xs text-muted">Jump anywhere, search boards, create tasks.</p>
              </div>
              <kbd className="flex items-center gap-0.5 rounded-md bg-surface-2 px-2 py-1 text-[11px] font-semibold text-muted">
                <Command className="h-3 w-3" />K
              </kbd>
            </div>
          </Card>

          {/* About */}
          <Card title="About">
            <div className="flex items-center gap-3">
              <div className="brand-gradient flex h-10 w-10 items-center justify-center rounded-2xl shadow-[var(--shadow-brand)]">
                <Zap className="h-5 w-5 fill-white text-white" />
              </div>
              <div>
                <p className="text-sm font-semibold text-ink">Flowboard</p>
                <p className="text-xs text-muted">AI-powered Kanban · Light theme</p>
              </div>
            </div>
          </Card>

          {/* Account */}
          <Card title="Account" description="Manage your session.">
            <Button variant="danger" onClick={() => { logout(); navigate("/login"); }}>
              <LogOut className="h-4 w-4" /> Sign out
            </Button>
          </Card>
        </div>
      </div>
    </>
  );
};

const Metric = ({ icon: Icon, label, value, tint }) => (
  <div className="rounded-2xl bg-surface-2/60 p-4">
    <div className="mb-3 flex h-9 w-9 items-center justify-center rounded-xl" style={{ backgroundColor: `${tint}1a`, color: tint }}>
      <Icon className="h-4 w-4" />
    </div>
    <p className="font-display text-2xl font-semibold tracking-tight tabular text-ink">{value}</p>
    <p className="mt-0.5 text-xs text-muted">{label}</p>
  </div>
);

export default Settings;
