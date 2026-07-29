import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { CheckSquare, AlertTriangle, CalendarClock, Calendar, ArrowUpRight, ListTodo } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { useLayout } from "../components/layout/AppLayout";
import { useWorkspace } from "../hooks/useWorkspace";
import Topbar from "../components/layout/Topbar";
import { PriorityTag } from "../components/ui/Badge";
import { FilterSelect } from "../components/ui/Input";
import { PRIORITIES, formatDueDate, cn } from "../lib/utils";

const isDone = (t) => (t.status || "").toLowerCase() === "done";

const MyTasks = () => {
  const { user } = useAuth();
  const { openCreateBoard } = useLayout();
  const { tasks, loading } = useWorkspace();

  const [priority, setPriority] = useState("");
  const [boardId, setBoardId] = useState("");
  const [search, setSearch] = useState("");
  const [now] = useState(() => Date.now());

  const mine = useMemo(
    () => tasks.filter((t) => t.assignee_id && t.assignee_id === user?.id),
    [tasks, user]
  );

  const boardsInPlay = useMemo(() => {
    const map = new Map();
    mine.forEach((t) => map.set(t.board_id, { id: t.board_id, title: t.board_title }));
    return [...map.values()];
  }, [mine]);

  const stats = useMemo(() => {
    let overdue = 0;
    let dueSoon = 0;
    const week = now + 7 * 24 * 60 * 60 * 1000;
    mine.forEach((t) => {
      if (isDone(t) || !t.due_date) return;
      const d = new Date(t.due_date).getTime();
      if (d < now) overdue += 1;
      else if (d <= week) dueSoon += 1;
    });
    return { total: mine.length, overdue, dueSoon, done: mine.filter(isDone).length };
  }, [mine, now]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return mine.filter((t) => {
      if (priority && t.priority !== priority) return false;
      if (boardId && t.board_id !== boardId) return false;
      if (q && !t.title.toLowerCase().includes(q) && !(t.description || "").toLowerCase().includes(q))
        return false;
      return true;
    });
  }, [mine, priority, boardId, search]);

  const grouped = useMemo(() => {
    const map = new Map();
    filtered.forEach((t) => {
      const arr = map.get(t.board_id) || [];
      arr.push(t);
      map.set(t.board_id, arr);
    });
    return [...map.entries()].map(([id, items]) => ({
      id,
      title: items[0].board_title,
      color: items[0].board_color,
      items,
    }));
  }, [filtered]);

  const hasFilters = priority || boardId || search;

  return (
    <>
      <Topbar title="My Tasks" subtitle="Everything assigned to you" onCreateBoard={openCreateBoard} />

      <div className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-[1600px] px-6 py-8 md:px-8">
          {/* KPIs */}
          <div className="mb-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            <MiniStat icon={ListTodo} label="Assigned to you" value={stats.total} tint="#2f8159" />
            <MiniStat icon={AlertTriangle} label="Overdue" value={stats.overdue} tint="#e11d48" />
            <MiniStat icon={CalendarClock} label="Due this week" value={stats.dueSoon} tint="#d97706" />
            <MiniStat icon={CheckSquare} label="Completed" value={stats.done} tint="#10b981" />
          </div>

          {/* Filters */}
          <div className="mb-6 flex flex-wrap items-center gap-2.5">
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search your tasks"
              className="h-9 w-56 rounded-full border border-line bg-surface px-4 text-xs shadow-[var(--shadow-card)] outline-none transition-all duration-200 focus:border-brand-500/50 focus:ring-2 focus:ring-brand-500/15"
            />
            <FilterSelect value={priority} onChange={(e) => setPriority(e.target.value)}>
              <option value="">All priorities</option>
              {PRIORITIES.map((p) => (
                <option key={p.value} value={p.value}>{p.label}</option>
              ))}
            </FilterSelect>
            <FilterSelect value={boardId} onChange={(e) => setBoardId(e.target.value)}>
              <option value="">All boards</option>
              {boardsInPlay.map((b) => (
                <option key={b.id} value={b.id}>{b.title}</option>
              ))}
            </FilterSelect>
            {hasFilters && (
              <button
                onClick={() => { setPriority(""); setBoardId(""); setSearch(""); }}
                className="rounded-full px-3 py-1.5 text-xs font-medium text-faint transition-colors hover:bg-surface-2 hover:text-ink"
              >
                Clear
              </button>
            )}
            <span className="ml-auto rounded-full bg-surface-2 px-3 py-1 text-xs font-medium tabular text-muted">
              {filtered.length} tasks
            </span>
          </div>

          {loading ? (
            <div className="space-y-3">
              {Array.from({ length: 6 }).map((_, i) => <div key={i} className="skeleton h-16 rounded-2xl" />)}
            </div>
          ) : mine.length === 0 ? (
            <EmptyState
              title="No tasks assigned to you"
              body="Tasks you’re assigned across all your boards will show up here."
            />
          ) : filtered.length === 0 ? (
            <EmptyState title="No matching tasks" body="Try clearing your filters." />
          ) : (
            <div className="space-y-8">
              {grouped.map((g) => (
                <div key={g.id}>
                  <div className="mb-3 flex items-center gap-2">
                    <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: g.color || "#2f8159" }} />
                    <h3 className="font-display text-sm font-semibold tracking-tight">{g.title}</h3>
                    <span className="rounded-full bg-surface-2 px-2 py-0.5 text-[11px] font-medium tabular text-muted">
                      {g.items.length}
                    </span>
                  </div>
                  <div className="space-y-2.5">
                    {g.items.map((t) => <TaskRow key={t.id} task={t} />)}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
};

const TaskRow = ({ task }) => {
  const due = formatDueDate(task.due_date);
  const done = isDone(task);
  return (
    <Link
      to={`/board/${task.board_id}`}
      className="group flex items-center gap-3 rounded-2xl border border-line bg-surface p-4 shadow-[var(--shadow-card)] transition-shadow duration-200 hover:shadow-[var(--shadow-soft)]"
    >
      <PriorityTag priority={task.priority} className="shrink-0" />
      <div className="min-w-0 flex-1">
        <p className={cn("truncate text-sm font-semibold tracking-tight", done ? "text-faint line-through" : "text-ink")}>
          {task.title}
        </p>
        {task.status && <p className="mt-0.5 text-[11px] text-faint">{task.status}</p>}
      </div>
      {due && (
        <span
          className={cn(
            "hidden items-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-medium tabular sm:flex",
            due.overdue && !done ? "bg-priority-urgent/10 text-priority-urgent" : "bg-surface-2 text-muted"
          )}
        >
          <Calendar className="h-3 w-3" /> {due.label}
        </span>
      )}
      <ArrowUpRight className="h-4 w-4 shrink-0 text-faint opacity-0 transition-all duration-200 group-hover:translate-x-0.5 group-hover:text-brand-500 group-hover:opacity-100" />
    </Link>
  );
};

const MiniStat = ({ icon: Icon, label, value, tint }) => (
  <div className="rounded-3xl border border-line bg-surface p-5 shadow-[var(--shadow-card)] transition-shadow duration-300 hover:shadow-[var(--shadow-soft)]">
    <div className="mb-6 flex items-center justify-between">
      <span className="text-xs font-medium uppercase tracking-[0.1em] text-muted">{label}</span>
      <div className="flex h-10 w-10 items-center justify-center rounded-2xl" style={{ backgroundColor: `${tint}1a`, color: tint }}>
        <Icon className="h-[18px] w-[18px]" />
      </div>
    </div>
    <p className="font-display text-4xl font-semibold tracking-tight tabular text-ink">{value}</p>
  </div>
);

const EmptyState = ({ title, body }) => (
  <div className="card flex flex-col items-center justify-center gap-3 rounded-3xl py-20 text-center">
    <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-50 text-brand-500">
      <CheckSquare className="h-7 w-7" />
    </div>
    <div>
      <h3 className="font-display text-lg font-semibold tracking-tight">{title}</h3>
      <p className="mt-1.5 max-w-sm text-sm leading-relaxed text-muted">{body}</p>
    </div>
  </div>
);

export default MyTasks;
