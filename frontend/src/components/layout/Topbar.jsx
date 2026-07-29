import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronDown, LogOut, Plus, Search, Command, Bell } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { useLayout } from "./AppLayout";
import Avatar from "../ui/Avatar";
import Button from "../ui/Button";

const Topbar = ({ title, subtitle, actions, onCreateBoard }) => {
  const { user, logout } = useAuth();
  const { openCommand } = useLayout() || {};
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    const onClick = (e) => ref.current && !ref.current.contains(e.target) && setMenuOpen(false);
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  return (
    <header className="glass sticky top-0 z-20 flex h-[72px] items-center gap-4 border-b px-6">
      <div className="min-w-0 shrink">
        {title && <h1 className="truncate font-display text-lg font-bold leading-tight tracking-tight text-ink">{title}</h1>}
        {subtitle && <p className="truncate text-xs text-muted">{subtitle}</p>}
      </div>

      <div className="ml-auto flex items-center gap-2.5">
        {/* Search → command menu */}
        <button
          onClick={openCommand}
          className="hidden h-10 w-56 items-center gap-2.5 rounded-full border border-line bg-surface px-4 text-sm text-faint shadow-[var(--shadow-card)] transition-all duration-200 hover:border-brand-300 hover:text-muted hover:shadow-[var(--shadow-soft)] md:flex lg:w-64"
        >
          <Search className="h-4 w-4 shrink-0" />
          <span className="flex-1 text-left">Search tasks, boards…</span>
          <kbd className="flex items-center gap-0.5 rounded-md bg-surface-2 px-1.5 py-0.5 text-[10px] font-semibold text-muted">
            <Command className="h-3 w-3" />K
          </kbd>
        </button>

        {actions}

        <button
          className="hidden h-10 w-10 items-center justify-center rounded-full border border-line bg-surface text-muted shadow-[var(--shadow-card)] transition-all duration-200 hover:-translate-y-px hover:text-ink hover:shadow-[var(--shadow-soft)] sm:flex"
          title="Notifications"
        >
          <Bell className="h-4.5 w-4.5" />
        </button>

        <Button size="md" onClick={onCreateBoard} className="hidden sm:inline-flex">
          <Plus className="h-4 w-4" /> New board
        </Button>

        <div className="relative" ref={ref}>
          <button
            onClick={() => setMenuOpen((o) => !o)}
            className="flex items-center gap-2 rounded-full border border-line bg-surface py-1 pl-1 pr-2.5 shadow-[var(--shadow-card)] transition-all duration-200 hover:border-brand-300 hover:shadow-[var(--shadow-soft)]"
          >
            <Avatar name={user?.name} id={user?.id} src={user?.avatar_url} size="sm" />
            <span className="hidden max-w-[7rem] truncate text-sm font-medium text-ink lg:block">
              {user?.name?.split(" ")[0]}
            </span>
            <ChevronDown className="h-4 w-4 text-faint" />
          </button>

          {menuOpen && (
            <div className="card animate-in absolute right-0 mt-2 w-56 rounded-2xl p-1.5 shadow-[var(--shadow-lift)]">
              <div className="px-3 py-2">
                <p className="truncate text-sm font-semibold text-ink">{user?.name}</p>
                <p className="truncate text-xs text-faint">{user?.email}</p>
              </div>
              <div className="my-1 border-t" />
              <button
                onClick={() => {
                  logout();
                  navigate("/login");
                }}
                className="flex w-full items-center gap-2 rounded-xl px-3 py-2 text-sm text-priority-urgent transition-colors hover:bg-surface-2"
              >
                <LogOut className="h-4 w-4" /> Log out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Topbar;
