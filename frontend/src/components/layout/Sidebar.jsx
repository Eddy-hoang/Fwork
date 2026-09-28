import { NavLink, useNavigate } from "react-router-dom";
import {
  Plus, LayoutDashboard, Zap, CheckSquare, Calendar, Users, Settings,
  ChevronLeft, ChevronRight, HelpCircle, LogOut, Sparkles,
} from "lucide-react";
import { useBoards } from "../../context/BoardsContext";
import { useAuth } from "../../context/AuthContext";
import Avatar from "../ui/Avatar";
import WorkspaceSelector from "../workspace/WorkspaceSelector";
import { cn } from "../../lib/utils";
import { useLanguage } from "../../context/LanguageContext";

// Section eyebrow
const SectionLabel = ({ children, collapsed }) =>
  collapsed ? (
    <div className="mx-auto my-2 h-px w-6 bg-zinc-200 dark:bg-zinc-800" />
  ) : (
    <p className="px-3 pb-1 pt-3 text-[10px] font-semibold uppercase tracking-wider text-zinc-400 dark:text-zinc-500">
      {children}
    </p>
  );

// Notion-style NavItem
const NavItem = ({ to, icon: Icon, label, collapsed, badge }) => (
  <NavLink
    to={to}
    title={collapsed ? label : undefined}
    className={({ isActive }) =>
      cn(
        "group relative flex h-9 items-center rounded-md text-xs font-medium transition-colors duration-150 select-none",
        collapsed ? "mx-auto w-9 justify-center" : "gap-2.5 px-2.5",
        isActive
          ? "bg-zinc-100 dark:bg-zinc-800 font-semibold text-zinc-900 dark:text-zinc-50"
          : "text-zinc-600 hover:bg-zinc-50 hover:text-zinc-900 dark:text-zinc-400 dark:hover:bg-zinc-850 dark:hover:text-zinc-200"
      )
    }
  >
    {({ isActive }) => (
      <>
        {isActive && !collapsed && (
          <span className="absolute left-0 top-1/2 h-4 w-0.5 -translate-y-1/2 rounded-r-full bg-zinc-900 dark:bg-zinc-100" />
        )}
        <Icon className={cn("h-4 w-4 shrink-0", isActive ? "text-zinc-900 dark:text-zinc-50" : "text-zinc-500 dark:text-zinc-400")} />
        {!collapsed && <span className="flex-1 truncate">{label}</span>}
        {!collapsed && badge != null && (
          <span className="rounded-md bg-zinc-100 dark:bg-zinc-800 px-1.5 py-0.5 text-[10px] font-medium text-zinc-600 dark:text-zinc-400">
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
        "fixed inset-y-0 left-0 z-40 hidden flex-col overflow-hidden border-r border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900 transition-[width] duration-200 ease-out md:flex",
        collapsed ? "w-[64px]" : "w-[240px]"
      )}
    >
      {/* Header */}
      <div className="flex h-14 items-center justify-between px-3 shrink-0 border-b border-zinc-100 dark:border-zinc-800/60">
        <div className="flex items-center gap-2 min-w-0">
          <div className="grid h-8 w-8 shrink-0 place-items-center rounded-md bg-zinc-900 text-white dark:bg-zinc-50 dark:text-zinc-900 shadow-xs">
            <Zap className="h-4 w-4 fill-current" />
          </div>
          {!collapsed && (
            <span className="truncate font-semibold text-sm tracking-tight text-zinc-900 dark:text-zinc-50">
              Fwork
            </span>
          )}
        </div>
        {!collapsed && (
          <button
            onClick={onToggle}
            title="Collapse sidebar"
            className="grid h-7 w-7 shrink-0 place-items-center rounded-md text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-700 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
          >
            <ChevronLeft className="h-4 w-4" />
          </button>
        )}
      </div>

      {collapsed && (
        <div className="flex justify-center py-2 shrink-0 border-b border-zinc-100 dark:border-zinc-800/60">
          <button
            onClick={onToggle}
            title="Expand sidebar"
            className="grid h-7 w-7 place-items-center rounded-md text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-700 dark:hover:bg-zinc-800 dark:hover:text-zinc-200"
          >
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* Scrollable Menu */}
      <div className="flex-1 overflow-y-auto no-scrollbar flex flex-col min-h-0 py-2">
        <WorkspaceSelector collapsed={collapsed} />

        <SectionLabel collapsed={collapsed}>{t("Menu")}</SectionLabel>
        <nav className="space-y-0.5 px-2">
          <NavItem to="/dashboard" icon={LayoutDashboard} label={t("Dashboard")} collapsed={collapsed} />
          <NavItem to="/my-tasks" icon={CheckSquare} label={t("My Tasks")} collapsed={collapsed} />
          <NavItem to="/calendar" icon={Calendar} label={t("Calendar")} collapsed={collapsed} />
          <NavItem to="/team" icon={Users} label={t("Team")} collapsed={collapsed} />
        </nav>

        {/* Boards */}
        <div className={cn("mt-3 flex h-6 items-center", collapsed ? "justify-center" : "justify-between px-3")}>
          {!collapsed && (
            <span className="text-[10px] font-semibold uppercase tracking-wider text-zinc-400 dark:text-zinc-500">{t("Boards")}</span>
          )}
          <button
            onClick={onCreateBoard}
            title={t("New board")}
            className="rounded-md p-1 text-zinc-400 transition-colors hover:bg-zinc-100 hover:text-zinc-900 dark:hover:bg-zinc-800 dark:hover:text-zinc-100"
          >
            <Plus className="h-3.5 w-3.5" />
          </button>
        </div>

        <div className="mt-1 space-y-0.5 px-2">
          {loading ? (
            Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className={cn("flex h-8 items-center gap-2", collapsed ? "justify-center" : "px-2")}>
                <div className="skeleton h-5 w-5 shrink-0 rounded" />
                {!collapsed && <div className="skeleton h-3 flex-1 rounded" />}
              </div>
            ))
          ) : boards.length === 0 ? (
            !collapsed && <p className="px-2 py-1.5 text-xs text-zinc-400">{t("No boards yet")}</p>
          ) : (
            boards.map((b) => {
              const color = b.color || "#6366F1";
              return (
                <NavLink
                  key={b.id}
                  to={`/board/${b.id}`}
                  title={b.title}
                  className={({ isActive }) =>
                    cn(
                      "flex h-8 items-center rounded-md text-xs transition-colors duration-150 select-none",
                      collapsed ? "mx-auto w-8 justify-center" : "gap-2.5 px-2",
                      isActive ? "bg-zinc-100 font-medium text-zinc-900 dark:bg-zinc-800 dark:text-zinc-50" : "text-zinc-600 hover:bg-zinc-50 hover:text-zinc-900 dark:text-zinc-400 dark:hover:bg-zinc-850 dark:hover:text-zinc-200"
                    )
                  }
                >
                  <span
                    className="grid h-5 w-5 shrink-0 place-items-center rounded text-[10px] font-bold"
                    style={{ backgroundColor: `${color}20`, color }}
                  >
                    {b.title?.[0]?.toUpperCase() || "B"}
                  </span>
                  {!collapsed && <span className="flex-1 truncate">{b.title}</span>}
                  {!collapsed && (
                    <span className="shrink-0 text-[10px] font-medium text-zinc-400">{b.task_count}</span>
                  )}
                </NavLink>
              );
            })
          )}
        </div>

        {/* General */}
        <SectionLabel collapsed={collapsed}>{t("General")}</SectionLabel>
        <nav className="space-y-0.5 px-2">
          <NavItem to="/settings" icon={Settings} label={t("Settings")} collapsed={collapsed} />
          <button
            onClick={onCommand}
            title={collapsed ? t("Help & search") : undefined}
            className={cn(
              "group flex h-8 w-full items-center rounded-md text-xs font-medium text-zinc-600 transition-colors hover:bg-zinc-50 hover:text-zinc-900 dark:text-zinc-400 dark:hover:bg-zinc-850 dark:hover:text-zinc-200",
              collapsed ? "mx-auto w-8 justify-center" : "gap-2.5 px-2"
            )}
          >
            <HelpCircle className="h-4 w-4 shrink-0 text-zinc-500 dark:text-zinc-400" />
            {!collapsed && <span className="flex-1 truncate text-left">{t("Help & search")}</span>}
          </button>
          <button
            onClick={() => { logout(); navigate("/login"); }}
            title={collapsed ? t("Log out") : undefined}
            className={cn(
              "group flex h-8 w-full items-center rounded-md text-xs font-medium text-zinc-600 transition-colors hover:bg-red-50 hover:text-red-600 dark:text-zinc-400 dark:hover:bg-red-950/40 dark:hover:text-red-400",
              collapsed ? "mx-auto w-8 justify-center" : "gap-2.5 px-2"
            )}
          >
            <LogOut className="h-4 w-4 shrink-0 text-zinc-500 dark:text-zinc-400" />
            {!collapsed && <span className="flex-1 truncate text-left">{t("Log out")}</span>}
          </button>
        </nav>

        {!collapsed && (
          <div className="px-2 pt-3">
            <button
              onClick={onCreateBoard}
              className="group relative w-full overflow-hidden rounded-lg border border-zinc-200 bg-zinc-50 p-3 text-left transition-colors hover:border-zinc-300 dark:border-zinc-800 dark:bg-zinc-850/50 dark:hover:border-zinc-700"
            >
              <div className="flex items-center gap-2 text-zinc-900 dark:text-zinc-100">
                <Sparkles className="h-4 w-4 text-indigo-500" />
                <p className="font-semibold text-xs tracking-tight">{t("Plan with AI")}</p>
              </div>
              <p className="mt-1 text-[11px] leading-snug text-zinc-500 dark:text-zinc-400">
                {t("Let AI plan desc")}
              </p>
            </button>
          </div>
        )}
      </div>

      {/* User Footer */}
      <div className="shrink-0 border-t border-zinc-100 p-2 dark:border-zinc-800/60">
        <div className={cn("flex items-center", collapsed ? "justify-center" : "gap-2.5 px-1")}>
          <Avatar name={user?.name} id={user?.id} src={user?.avatar || user?.avatar_url} size="sm" className="shrink-0 ring-1 ring-zinc-200 dark:ring-zinc-700" />
          {!collapsed && (
            <div className="min-w-0 flex-1">
              <p className="truncate text-xs font-semibold text-zinc-900 dark:text-zinc-100">{user?.name}</p>
              <p className="truncate text-[11px] text-zinc-400">{user?.email}</p>
            </div>
          )}
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
