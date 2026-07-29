import { useMemo, useState } from "react";
import { Search, Users, Crown } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { useLayout } from "../components/layout/AppLayout";
import { useWorkspace } from "../hooks/useWorkspace";
import Topbar from "../components/layout/Topbar";
import Avatar from "../components/ui/Avatar";
import { cn } from "../lib/utils";

const roleTone = (role) =>
  role === "owner"
    ? "bg-brand-50 text-brand-700"
    : role === "admin"
    ? "bg-[#FBF1E2] text-[#c28a3a]"
    : "bg-surface-2 text-muted";

const Team = () => {
  const { user } = useAuth();
  const { openCreateBoard } = useLayout();
  const { members, loading } = useWorkspace();
  const [search, setSearch] = useState("");

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    const sorted = [...members].sort((a, b) => (a.name || "").localeCompare(b.name || ""));
    if (!q) return sorted;
    return sorted.filter(
      (m) => m.name?.toLowerCase().includes(q) || m.email?.toLowerCase().includes(q)
    );
  }, [members, search]);

  const owners = members.filter((m) => m.role === "owner").length;

  return (
    <>
      <Topbar title="Team" subtitle="People across your boards" onCreateBoard={openCreateBoard} />

      <div className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-[1600px] px-6 py-8 md:px-8">
          {/* Header row */}
          <div className="mb-6 flex flex-wrap items-center gap-3">
            <div className="flex items-center gap-2">
              <span className="rounded-full bg-surface-2 px-3 py-1 text-xs font-medium tabular text-muted">
                {members.length} {members.length === 1 ? "person" : "people"}
              </span>
              {owners > 0 && (
                <span className="rounded-full bg-surface-2 px-3 py-1 text-xs font-medium tabular text-muted">
                  {owners} {owners === 1 ? "owner" : "owners"}
                </span>
              )}
            </div>
            <div className="relative ml-auto">
              <Search className="pointer-events-none absolute left-3.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-faint" />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search people"
                className="h-9 w-56 rounded-full border border-line bg-surface pl-9 pr-4 text-xs shadow-[var(--shadow-card)] outline-none transition-all duration-200 focus:border-brand-500/50 focus:ring-2 focus:ring-brand-500/15"
              />
            </div>
          </div>

          {loading ? (
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {Array.from({ length: 8 }).map((_, i) => <div key={i} className="skeleton h-44 rounded-3xl" />)}
            </div>
          ) : members.length === 0 ? (
            <div className="card flex flex-col items-center justify-center gap-3 rounded-3xl py-20 text-center">
              <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-50 text-brand-500">
                <Users className="h-7 w-7" />
              </div>
              <div>
                <h3 className="font-display text-lg font-semibold tracking-tight">No teammates yet</h3>
                <p className="mt-1.5 max-w-sm text-sm leading-relaxed text-muted">
                  Invite people to a board and they’ll appear here.
                </p>
              </div>
            </div>
          ) : (
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {filtered.map((m) => (
                <div
                  key={m.id}
                  className="flex flex-col items-center rounded-3xl border border-line bg-surface p-6 text-center shadow-[var(--shadow-card)] transition-shadow duration-300 hover:shadow-[var(--shadow-soft)]"
                >
                  <Avatar name={m.name} id={m.id} src={m.avatar_url} size="lg" className="h-16 w-16 text-lg" />
                  <p className="mt-3 flex items-center gap-1.5 font-display text-base font-semibold tracking-tight">
                    {m.name}
                    {m.id === user?.id && <span className="text-[11px] font-medium text-faint">(You)</span>}
                  </p>
                  <p className="mt-0.5 max-w-full truncate text-xs text-muted">{m.email}</p>
                  <span
                    className={cn(
                      "mt-3 inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-medium capitalize",
                      roleTone(m.role)
                    )}
                  >
                    {m.role === "owner" && <Crown className="h-3 w-3" />}
                    {m.role || "member"}
                  </span>
                  <p className="mt-3 border-t pt-3 text-[11px] text-faint" style={{ width: "100%" }}>
                    On {m.boards.length} {m.boards.length === 1 ? "board" : "boards"}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default Team;
