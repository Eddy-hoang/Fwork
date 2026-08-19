import { createContext, useContext, useState, useCallback, useEffect } from "react";
import { Outlet, NavLink } from "react-router-dom";
import { LayoutDashboard, CheckSquare, Calendar, Users, Settings } from "lucide-react";
import { BoardsProvider } from "../../context/BoardsContext";
import { cn } from "../../lib/utils";
import Sidebar from "./Sidebar";
import CreateBoardModal from "../board/CreateBoardModal";
import CommandMenu from "../CommandMenu";
import { useLanguage } from "../../context/LanguageContext";

const LayoutContext = createContext(null);
export const useLayout = () => useContext(LayoutContext);

const MobileNavItem = ({ to, icon: Icon, label }) => (
  <NavLink
    to={to}
    className={({ isActive }) =>
      cn(
        "flex flex-col items-center justify-center gap-1 text-[10px] font-medium transition-colors duration-200 w-16",
        isActive ? "text-brand-500 font-semibold" : "text-muted hover:text-ink"
      )
    }
  >
    {({ isActive }) => (
      <>
        <div className={cn(
          "flex h-8 w-14 items-center justify-center rounded-full transition-all duration-200",
          isActive ? "bg-brand-100 dark:bg-brand-50/10 text-brand-700 dark:text-brand-300" : "text-muted"
        )}>
          <Icon className="h-5 w-5" />
        </div>
        <span className="truncate max-w-full scale-95">{label}</span>
      </>
    )}
  </NavLink>
);

const LayoutInner = () => {
  const [createOpen, setCreateOpen] = useState(false);
  const [commandOpen, setCommandOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(
    () => localStorage.getItem("sidebar-collapsed") === "true"
  );
  const { t } = useLanguage();

  const openCreateBoard = useCallback(() => setCreateOpen(true), []);
  const openCommand = useCallback(() => setCommandOpen(true), []);
  const toggleSidebar = useCallback(
    () =>
      setCollapsed((c) => {
        const next = !c;
        localStorage.setItem("sidebar-collapsed", String(next));
        return next;
      }),
    []
  );

  // Global ⌘K / Ctrl+K
  useEffect(() => {
    const onKey = (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
        e.preventDefault();
        setCommandOpen((o) => !o);
      }
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, []);

  return (
    <LayoutContext.Provider value={{ openCreateBoard, openCommand }}>
      <div className="h-screen overflow-hidden flex flex-col md:flex-row">
        <Sidebar
          collapsed={collapsed}
          onToggle={toggleSidebar}
          onCreateBoard={openCreateBoard}
          onCommand={openCommand}
        />
        <main
          className={cn(
            "flex-1 flex flex-col min-w-0 overflow-hidden transition-[padding] duration-300 ease-[var(--ease-spring)]",
            collapsed ? "main-content-pad-collapsed" : "main-content-pad"
          )}
        >
          <div className="flex-1 flex flex-col overflow-hidden pb-16 md:pb-0">
            <Outlet />
          </div>
        </main>

        {/* Mobile Bottom Navigation (Material Design 3 Style) */}
        <nav className="fixed bottom-0 left-0 right-0 z-30 flex h-16 items-center justify-around border-t border-line bg-surface/90 pb-safe shadow-[var(--shadow-lift)] backdrop-blur-xl md:hidden">
          <MobileNavItem to="/dashboard" icon={LayoutDashboard} label={t("Dashboard")} />
          <MobileNavItem to="/my-tasks" icon={CheckSquare} label={t("My Tasks")} />
          <MobileNavItem to="/calendar" icon={Calendar} label={t("Calendar")} />
          <MobileNavItem to="/team" icon={Users} label={t("Team")} />
          <MobileNavItem to="/settings" icon={Settings} label={t("Settings")} />
        </nav>
      </div>

      <CreateBoardModal open={createOpen} onClose={() => setCreateOpen(false)} />
      <CommandMenu
        open={commandOpen}
        onClose={() => setCommandOpen(false)}
        onCreateBoard={() => {
          setCommandOpen(false);
          setCreateOpen(true);
        }}
      />
    </LayoutContext.Provider>
  );
};

const AppLayout = () => (
  <BoardsProvider>
    <LayoutInner />
  </BoardsProvider>
);

export default AppLayout;
