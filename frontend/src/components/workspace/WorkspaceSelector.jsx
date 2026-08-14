import { useState, useRef, useEffect } from "react";
import { ChevronsUpDown, Check, Plus, Building2, Crown, Shield, User } from "lucide-react";
import { useBoards } from "../../context/BoardsContext";
import CreateWorkspaceModal from "./CreateWorkspaceModal";
import { cn } from "../../lib/utils";

const roleIcon = (role) => {
  const r = role?.toLowerCase();
  if (r === "owner") return <Crown className="h-3 w-3 text-amber-500" />;
  if (r === "admin") return <Shield className="h-3 w-3 text-blue-500" />;
  return <User className="h-3 w-3 text-slate-400" />;
};

const WorkspaceSelector = ({ collapsed }) => {
  const { workspaces, currentWorkspace, switchWorkspace } = useBoards();
  const [open, setOpen] = useState(false);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  if (!currentWorkspace && workspaces.length === 0) {
    return null;
  }

  const activeName = currentWorkspace?.name || "Workspace";
  const activeRole = currentWorkspace?.currentUserRole || "OWNER";

  return (
    <div className="relative px-3 py-1.5" ref={dropdownRef}>
      <button
        type="button"
        onClick={() => setOpen(!open)}
        title={collapsed ? activeName : undefined}
        className={cn(
          "group flex w-full items-center rounded-2xl border border-line/80 bg-surface-2/70 p-2.5 text-left text-xs font-medium transition-all hover:border-brand-500/40 hover:bg-surface-2 hover:shadow-card",
          collapsed ? "justify-center p-2" : "gap-2.5"
        )}
      >
        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-brand-500/10 text-brand-600 font-bold font-display text-sm">
          {activeName[0]?.toUpperCase() || <Building2 className="h-4 w-4" />}
        </div>

        {!collapsed && (
          <div className="min-w-0 flex-1">
            <div className="flex items-center gap-1.5">
              <p className="truncate font-semibold text-ink leading-snug">{activeName}</p>
            </div>
            <p className="flex items-center gap-1 text-[10px] text-muted capitalize">
              {roleIcon(activeRole)}
              <span>{activeRole.toLowerCase()}</span>
            </p>
          </div>
        )}

        {!collapsed && (
          <ChevronsUpDown className="h-4 w-4 shrink-0 text-faint transition-colors group-hover:text-muted" />
        )}
      </button>

      {/* Dropdown Menu */}
      {open && (
        <div
          className={cn(
            "absolute z-50 mt-1.5 w-64 rounded-2xl border border-line bg-surface p-2 shadow-soft backdrop-blur-xl animate-in fade-in zoom-in-95 duration-150",
            collapsed ? "left-14 top-0" : "left-3 right-3 w-[calc(100%-24px)]"
          )}
        >
          <div className="px-2 py-1 text-[10px] font-semibold uppercase tracking-wider text-faint">
            Workspaces ({workspaces.length})
          </div>

          <div className="mt-1 max-h-56 overflow-y-auto space-y-1 no-scrollbar">
            {workspaces.map((ws) => {
              const isSelected = ws.id === currentWorkspace?.id;
              const role = ws.currentUserRole || "OWNER";
              return (
                <button
                  key={ws.id}
                  type="button"
                  onClick={() => {
                    switchWorkspace(ws);
                    setOpen(false);
                  }}
                  className={cn(
                    "flex w-full items-center gap-2.5 rounded-xl p-2 text-left text-xs transition-colors",
                    isSelected
                      ? "bg-brand-50/80 font-semibold text-brand-700"
                      : "text-ink hover:bg-surface-2"
                  )}
                >
                  <div
                    className={cn(
                      "flex h-7 w-7 shrink-0 items-center justify-center rounded-lg font-bold text-xs",
                      isSelected
                        ? "bg-brand-500 text-white"
                        : "bg-surface-2 text-muted"
                    )}
                  >
                    {ws.name?.[0]?.toUpperCase() || "W"}
                  </div>

                  <div className="min-w-0 flex-1">
                    <p className="truncate font-medium">{ws.name}</p>
                    <p className="flex items-center gap-1 text-[10px] text-faint capitalize">
                      {roleIcon(role)}
                      <span>{role.toLowerCase()}</span>
                      {ws.memberCount != null && (
                        <>
                          <span>•</span>
                          <span>{ws.memberCount} members</span>
                        </>
                      )}
                    </p>
                  </div>

                  {isSelected && <Check className="h-4 w-4 shrink-0 text-brand-600" />}
                </button>
              );
            })}
          </div>

          <div className="mt-2 border-t pt-1.5">
            <button
              type="button"
              onClick={() => {
                setOpen(false);
                setCreateModalOpen(true);
              }}
              className="flex w-full items-center gap-2 rounded-xl p-2 text-left text-xs font-medium text-brand-600 transition-colors hover:bg-brand-50"
            >
              <Plus className="h-4 w-4" />
              <span>Create New Workspace</span>
            </button>
          </div>
        </div>
      )}

      <CreateWorkspaceModal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
      />
    </div>
  );
};

export default WorkspaceSelector;
