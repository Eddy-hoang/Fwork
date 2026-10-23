import { NavLink, useNavigate } from "react-router-dom";
import {
  Plus, LayoutDashboard, Zap, CheckSquare, Calendar, Users, Settings,
  ChevronLeft, ChevronRight, HelpCircle, LogOut, Sparkles,
} from "lucide-react";
import { useBoards } from "../../context/BoardsContext";
import { useAuth } from "../../context/AuthContext";
import Avatar from "../ui/Avatar";
import WorkspaceSelector from "../workspace/WorkspaceSelector";
import Logo from "../common/Logo";
import { cn } from "../../lib/utils";
import { useLanguage } from "../../context/LanguageContext";

// Section eyebrow (hidden when collapsed)
const SectionLabel = ({ children, collapsed }) =>
  collapsed ? (
    <div className="mx-auto my-2 h-px w-6 bg-line" />
  ) : (
    <p className="px-4 pb-1.5 pt-4 text-[11px] font-semibold uppercase tracking-[0.12em] text-faint">
      {children}
    </p>
  );

// A nav row that adapts to collapsed/expanded
const NavItem = ({ to, icon: Icon, label, collapsed, badge }) => (
  <NavLink
    to={to}
    title={collapsed ? label : undefined}
    className={({ isActive }) =>
      cn(
        "group relative flex h-11 items-center rounded-2xl text-sm font-medium transition-all duration-200",
        collapsed ? "mx-auto w-11 justify-center" : "gap-3 px-3",
        isActive
          ? "bg-brand-50/90 dark:bg-brand-50/15 font-semibold text-brand-700 dark:text-brand-300 border border-brand-200/50 dark:border-brand-300/20 shadow-xs"
          : "text-muted hover:bg-surface-2 hover:text-ink hover:translate-x-0.5"
      )
    }
  >
    {({ isActive }) => (
      <>
        {isActive && !collapsed && (
          <span className="absolute left-0 top-1/2 h-5 w-1 -translate-y-1/2 rounded-r-full bg-brand-500 shadow-[0_0_8px_rgba(103,80,164,0.6)]" />
        )}
        <Icon className="h-5 w-5 shrink-0" />
        {!collapsed && <span className="flex-1 truncate">{label}</span>}
        {!collapsed && badge != null && (
          <span className="rounded-full bg-ink px-1.5 py-0.5 text-[10px] font-bold tabular text-bg">
            {badge}
          </span>
        )}
      </>
    )}
  </NavLink>
);

const Sidebar = ({ collapsed, onToggle, onCreateBoard, onCommand }) => {
  const { boards, loading } = useBoards();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { t } = useLanguage();

  return (
    <aside
      className={cn(
        "fixed inset-y-0 left-0 z-40 hidden flex-col overflow-hidden border-r border-line bg-surface/90 shadow-soft backdrop-blur-xl transition-[width] duration-300 ease-[var(--ease-spring)] md:flex",
        collapsed ? "w-[72px]" : "w-[252px]"
      )}
    >
      {/* Header (pinned at top) */}
      <div className={cn("flex h-16 items-center shrink-0", collapsed ? "justify-center px-2" : "justify-between px-4")}>
        <Logo size="lg" showText={!collapsed} />
        {!collapsed && (
          <button
            onClick={onToggle}
            title="Collapse sidebar"
            className="grid h-7 w-7 shrink-0 place-items-center rounded-lg text-faint transition-colors hover:bg-surface-2 hover:text-ink"
          >
            <ChevronLeft className="h-4 w-4" />
          </button>
        )}
      </div>

      {collapsed && (
        <div className="flex justify-center pb-1 shrink-0">
          <button
            onClick={onToggle}
            title="Expand sidebar"
            className="grid h-7 w-7 place-items-center rounded-lg text-faint transition-colors hover:bg-surface-2 hover:text-ink"
          >
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* Middle Scrollable Section */}
      <div className="flex-1 overflow-y-auto no-scrollbar flex flex-col min-h-0 pb-3">
        {/* Workspace Selector */}
        <WorkspaceSelector collapsed={collapsed} />

        {/* Menu */}
        <SectionLabel collapsed={collapsed}>{t("Menu")}</SectionLabel>
        <nav className="space-y-1 px-3">
          <NavItem to="/dashboard" icon={LayoutDashboard} label={t("Dashboard")} collapsed={collapsed} />
          <NavItem to="/my-tasks" icon={CheckSquare} label={t("My Tasks")} collapsed={collapsed} />
          <NavItem to="/calendar" icon={Calendar} label={t("Calendar")} collapsed={collapsed} />
          <NavItem to="/team" icon={Users} label={t("Team")} collapsed={collapsed} />
        </nav>

        {/* Boards */}
        <div className={cn("mt-2 flex h-7 items-center", collapsed ? "justify-center" : "justify-between px-4")}>
          {!collapsed && (
            <span className="text-[11px] font-semibold uppercase tracking-[0.12em] text-faint">{t("Boards")}</span>
          )}
          <button
            onClick={onCreateBoard}
            title={t("New board")}
            className="rounded-md p-1 text-faint transition-colors hover:bg-surface-2 hover:text-brand-600"
          >
            <Plus className="h-4 w-4" />
          </button>
        </div>

        <div className="mt-1 space-y-0.5 px-3">
          {loading ? (
            Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className={cn("flex h-10 items-center gap-3", collapsed ? "justify-center" : "px-1")}>
                <div className="skeleton h-7 w-7 shrink-0 rounded-lg" />
                {!collapsed && <div className="skeleton h-3 flex-1 rounded" />}
              </div>
            ))
          ) : boards.length === 0 ? (
            !collapsed && <p className="px-3 py-2 text-xs text-faint">{t("No boards yet")}</p>
          ) : (
            boards.map((b) => {
              const color = b.color || "var(--color-brand-600)";
              return (
                <NavLink
                  key={b.id}
                  to={`/board/${b.id}`}
                  title={b.title}
                  className={({ isActive }) =>
                    cn(
                      "flex h-10 items-center rounded-2xl text-sm transition-all duration-200",
                      collapsed ? "mx-auto w-10 justify-center" : "gap-3 px-2.5",
                      isActive
                        ? "bg-brand-50/90 dark:bg-brand-50/15 font-semibold text-brand-700 dark:text-brand-300 border border-brand-200/50 dark:border-brand-300/20 shadow-xs"
                        : "text-muted hover:bg-surface-2 hover:text-ink hover:translate-x-0.5"
                    )
                  }
                >
                  <span
                    className="grid h-7 w-7 shrink-0 place-items-center rounded-xl font-display text-[12px] font-bold shadow-2xs"
                    style={{ backgroundColor: `color-mix(in oklab, ${color} 14%, transparent)`, color }}
                  >
                    {b.title?.[0]?.toUpperCase() || "B"}
                  </span>
                  {!collapsed && <span className="flex-1 truncate">{b.title}</span>}
                  {!collapsed && (
                    <span className="shrink-0 rounded-full bg-surface-2 px-1.5 py-0.5 text-[10px] font-semibold tabular text-faint border border-line/60">
                      {b.task_count}
                    </span>
                  )}
                </NavLink>
              );
            })
          )}
        </div>

        {/* General */}
        <SectionLabel collapsed={collapsed}>{t("General")}</SectionLabel>
        <nav className="space-y-1 px-3">
          <NavItem to="/settings" icon={Settings} label={t("Settings")} collapsed={collapsed} />
          <button
            onClick={onCommand}
            title={collapsed ? t("Help & search") : undefined}
            className={cn(
              "group flex h-11 w-full items-center rounded-2xl text-sm font-medium text-muted transition-colors duration-200 hover:bg-surface-2 hover:text-ink",
              collapsed ? "mx-auto w-11 justify-center" : "gap-3 px-3"
            )}
          >
            <HelpCircle className="h-5 w-5 shrink-0" />
            {!collapsed && <span className="flex-1 truncate text-left">{t("Help & search")}</span>}
          </button>
          <button
            onClick={() => { logout(); navigate("/login"); }}
            title={collapsed ? t("Log out") : undefined}
            className={cn(
              "group flex h-11 w-full items-center rounded-2xl text-sm font-medium text-muted transition-colors duration-200 hover:bg-priority-urgent/10 hover:text-priority-urgent",
              collapsed ? "mx-auto w-11 justify-center" : "gap-3 px-3"
            )}
          >
            <LogOut className="h-5 w-5 shrink-0" />
            {!collapsed && <span className="flex-1 truncate text-left">{t("Log out")}</span>}
          </button>
        </nav>

      </div>

      {/* User (pinned at bottom) */}
      <div className="shrink-0 border-t border-line">
        <div className={cn("flex h-16 items-center", collapsed ? "justify-center px-2" : "gap-3 px-3.5")}>
          <Avatar name={user?.name} id={user?.id} src={user?.avatar || user?.avatar_url} size="sm" className="shrink-0" />
          {!collapsed && (
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-semibold text-ink">{user?.name}</p>
              <p className="truncate text-xs text-faint">{user?.email}</p>
            </div>
          )}
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
