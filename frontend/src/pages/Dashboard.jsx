import { useMemo } from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import {
  Plus,
  LayoutGrid,
  Users,
  CheckSquare,
  Sparkles,
  ArrowUpRight,
  BarChart3,
  PieChart,
  Clock,
  FolderKanban,
  Crown,
  Share2,
} from "lucide-react";
import { useBoards } from "../context/BoardsContext";
import { useAuth } from "../context/AuthContext";
import { useLayout } from "../components/layout/AppLayout";
import Topbar from "../components/layout/Topbar";
import Button from "../components/ui/Button";
import { BoardCardSkeleton } from "../components/ui/Skeleton";
import { cn, relativeTime } from "../lib/utils";

const Dashboard = () => {
  const { boards, loading } = useBoards();
  const { user } = useAuth();
  const { openCreateBoard } = useLayout();

  const stats = useMemo(() => {
    const totalTasks = boards.reduce(
      (sum, b) => sum + Number(b.task_count || 0),
      0,
    );
    const owned = boards.filter((b) => b.is_owner).length;
    const shared = boards.filter((b) => !b.is_owner).length;
    return { total: boards.length, totalTasks, owned, shared };
  }, [boards]);

  const topBoards = useMemo(
    () =>
      [...boards]
        .sort((a, b) => Number(b.task_count || 0) - Number(a.task_count || 0))
        .slice(0, 7),
    [boards],
  );

  const avgPerBoard = stats.total
    ? Math.round(stats.totalTasks / stats.total)
    : 0;
  const ownedPct = stats.total
    ? Math.round((stats.owned / stats.total) * 100)
    : 0;

  // Real per-board task distributions feeding the KPI mini bar charts.
  const trends = useMemo(() => {
    const sizes = boards.map((b) => Number(b.task_count || 0));
    return {
      boards: sizes,
      tasks: [...sizes].sort((a, b) => b - a),
      owned: boards
        .filter((b) => b.is_owner)
        .map((b) => Number(b.task_count || 0)),
      shared: boards
        .filter((b) => !b.is_owner)
        .map((b) => Number(b.task_count || 0)),
    };
  }, [boards]);

  const recentBoards = useMemo(
    () =>
      [...boards]
        .sort((a, b) => new Date(b.updated_at) - new Date(a.updated_at))
        .slice(0, 4),
    [boards],
  );

  return (
    <>
      <Topbar
        title="Dashboard"
        subtitle="Your boards and shared projects"
        onCreateBoard={openCreateBoard}
      />

      <div className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-[1600px] px-6 py-8 md:px-8">
          {/* Greeting */}
          <div className="mb-8">
            <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-brand-500">
              Workspace Overview
            </p>
            <h2 className="mt-2 font-display text-[clamp(26px,3vw,34px)] font-semibold leading-tight tracking-tight">
              Welcome back, {user?.name?.split(" ")[0]} 👋
            </h2>
          </div>

          {/* KPIs */}
          <div className="mb-5 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            <KpiCard
              featured
              label="Total boards"
              value={stats.total}
              hint="Across your workspace"
              trend={trends.boards}
              icon={FolderKanban}
            />
            <KpiCard
              label="Total tasks"
              value={stats.totalTasks}
              hint={`${avgPerBoard} avg per board`}
              trend={trends.tasks}
              icon={CheckSquare}
            />
            <KpiCard
              label="Owned by you"
              value={stats.owned}
              hint={`${ownedPct}% of workspace`}
              trend={trends.owned}
              icon={Crown}
            />
            <KpiCard
              label="Shared with you"
              value={stats.shared}
              hint="From your teammates"
              trend={trends.shared}
              icon={Share2}
            />
          </div>

          {/* Insights — bento */}
          {loading ? (
            <div className="mb-10 space-y-5">
              <div className="grid gap-5 lg:grid-cols-12">
                <div className="skeleton h-[300px] rounded-3xl lg:col-span-8" />
                <div className="skeleton h-[300px] rounded-3xl lg:col-span-4" />
              </div>
            </div>
          ) : boards.length > 0 ? (
            <div className="mb-10 space-y-5">
              <div className="grid gap-5 lg:grid-cols-12">
                <TasksByBoard boards={topBoards} className="lg:col-span-8" />
                <WorkspaceDonut
                  owned={stats.owned}
                  shared={stats.shared}
                  boards={boards}
                  className="lg:col-span-4"
                />
              </div>
              <div className="grid gap-5 lg:grid-cols-12">
                <RecentBoards boards={recentBoards} className="lg:col-span-8" />
                <AIPromo onCreate={openCreateBoard} className="lg:col-span-4" />
              </div>
            </div>
          ) : null}

          <div className="mb-5 flex items-end justify-between">
            <h3 className="text-[11px] font-semibold uppercase tracking-[0.14em] text-faint">
              All boards
            </h3>
            <Button size="sm" onClick={openCreateBoard}>
              <Plus className="h-4 w-4" /> New board
            </Button>
          </div>

          {loading ? (
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {Array.from({ length: 6 }).map((_, i) => (
                <BoardCardSkeleton key={i} />
              ))}
            </div>
          ) : boards.length === 0 ? (
            <EmptyState onCreate={openCreateBoard} />
          ) : (
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {boards.map((b, i) => (
                <motion.div
                  key={b.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{
                    delay: (i % 6) * 0.05,
                    ease: [0.16, 1, 0.3, 1],
                  }}
                >
                  <Link
                    to={`/board/${b.id}`}
                    className="group relative block h-full overflow-hidden rounded-3xl border border-line bg-surface p-5 shadow-[var(--shadow-card)] transition-shadow duration-300 hover:shadow-[var(--shadow-soft)]"
                  >
                    {/* color accent strip */}
                    <span
                      className="absolute inset-x-0 top-0 h-1"
                      style={{ background: b.color || "#2f8159" }}
                    />
                    <div className="mb-3.5 flex items-start justify-between">
                      <div
                        className="flex h-11 w-11 items-center justify-center rounded-2xl"
                        style={{
                          backgroundColor: `${b.color || "#2f8159"}1f`,
                          color: b.color || "#2f8159",
                        }}
                      >
                        <LayoutGrid className="h-5 w-5" />
                      </div>
                      <span className="flex items-center gap-1.5 text-faint transition-all duration-200 group-hover:text-brand-500">
                        {!b.is_owner && (
                          <span className="rounded-full bg-surface-2 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wide text-muted">
                            Shared
                          </span>
                        )}
                        <ArrowUpRight className="h-4 w-4 transition-transform duration-200 group-hover:-translate-y-0.5 group-hover:translate-x-0.5" />
                      </span>
                    </div>
                    <h4 className="font-display text-base font-semibold tracking-tight transition-colors group-hover:text-brand-600">
                      {b.title}
                    </h4>
                    <p className="mt-1.5 line-clamp-2 min-h-10 text-sm leading-relaxed text-muted">
                      {b.description || "No description"}
                    </p>
                    <div className="mt-4 flex items-center gap-4 border-t pt-3.5 text-xs text-faint">
                      <span className="flex items-center gap-1.5 tabular">
                        <CheckSquare className="h-3.5 w-3.5" /> {b.task_count}{" "}
                        tasks
                      </span>
                      <span className="flex items-center gap-1.5 tabular">
                        <Users className="h-3.5 w-3.5" /> {b.member_count}
                      </span>
                      <span className="ml-auto">
                        {relativeTime(b.updated_at)}
                      </span>
                    </div>
                  </Link>
                </motion.div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
};

// ── Shared bits for the insight cards ─────────────────────────────────────
const SectionTitle = ({ icon: Icon, children, hint }) => (
  <div className="mb-5 flex items-center gap-2.5">
    <span className="grid h-8 w-8 shrink-0 place-items-center rounded-xl bg-brand-50 text-brand-500">
      <Icon className="h-4 w-4" />
    </span>
    <div className="min-w-0">
      <h3 className="font-display text-sm font-semibold tracking-tight">
        {children}
      </h3>
      {hint && <p className="truncate text-[11px] text-faint">{hint}</p>}
    </div>
  </div>
);

const EmptyHint = ({ icon: Icon, children }) => (
  <div className="flex flex-1 flex-col items-center justify-center gap-2 py-8 text-center">
    <Icon className="h-6 w-6 text-faint" />
    <p className="max-w-[15rem] text-xs text-muted">{children}</p>
  </div>
);

const Legend = ({ color, label, value }) => (
  <div className="flex items-center gap-2.5">
    <span
      className="h-2.5 w-2.5 shrink-0 rounded-full"
      style={{ backgroundColor: color }}
    />
    <span className="text-sm text-muted">{label}</span>
    <span className="ml-auto text-sm font-semibold tabular text-ink">
      {value}
    </span>
  </div>
);

// violet shade scaled to a bar's relative height — taller = deeper iris
const barShade = (pct) =>
  pct >= 0.8
    ? "#1d5038"
    : pct >= 0.5
      ? "#2f8159"
      : pct >= 0.25
        ? "#57a47b"
        : "#8bc4a4";

// ── Board analytics (vertical pill-bar chart + ranked breakdown) ──────────
const TasksByBoard = ({ boards, className }) => {
  const max = Math.max(1, ...boards.map((b) => Number(b.task_count || 0)));
  const total = boards.reduce((s, b) => s + Number(b.task_count || 0), 0);
  const hasTasks = total > 0;
  return (
    <div
      className={cn(
        "flex flex-col rounded-3xl border border-line bg-surface p-6 shadow-[var(--shadow-card)]",
        className,
      )}
    >
      <SectionTitle icon={BarChart3} hint="Tasks across your busiest boards">
        Board analytics
      </SectionTitle>
      {hasTasks ? (
        <div className="mt-2 flex flex-1 flex-col gap-6 lg:flex-row">
          {/* bars */}
          <div className="flex h-[230px] flex-1 items-end gap-3">
            {boards.map((b, i) => {
              const count = Number(b.task_count || 0);
              const pct = count / max;
              const color = b.color || "#2f8159";
              return (
                <Link
                  key={b.id}
                  to={`/board/${b.id}`}
                  title={`${b.title} · ${count} tasks`}
                  className="group flex h-full flex-1 flex-col items-center justify-end gap-3"
                >
                  <div className="relative flex w-full max-w-[44px] flex-1 items-end justify-center">
                    {count > 0 ? (
                      <>
                        <span className="absolute left-1/2 top-0 z-10 -translate-x-1/2 -translate-y-1 rounded-full bg-ink px-2 py-0.5 text-[10px] font-semibold tabular text-white opacity-0 shadow-[var(--shadow-soft)] transition-opacity duration-200 group-hover:opacity-100">
                          {count}
                        </span>
                        <motion.div
                          className="w-full rounded-full"
                          style={{ backgroundColor: barShade(pct) }}
                          initial={{ height: 0 }}
                          animate={{ height: `${Math.max(pct * 100, 8)}%` }}
                          transition={{
                            duration: 0.7,
                            delay: i * 0.06,
                            ease: [0.16, 1, 0.3, 1],
                          }}
                        />
                      </>
                    ) : (
                      <div className="hatch h-full w-full rounded-full border border-line" />
                    )}
                  </div>
                  <span
                    className="grid h-7 w-7 shrink-0 place-items-center rounded-lg font-display text-[11px] font-bold"
                    style={{ backgroundColor: `${color}22`, color }}
                  >
                    {b.title?.[0]?.toUpperCase() || "B"}
                  </span>
                </Link>
              );
            })}
          </div>

          {/* ranked breakdown — decodes the bar initials */}
          <div className="shrink-0 lg:w-[300px] lg:border-l lg:border-line lg:pl-6">
            <p className="mb-3.5 text-[11px] font-semibold uppercase tracking-[0.12em] text-faint">
              Breakdown
            </p>
            <div className="space-y-3">
              {boards.map((b) => {
                const count = Number(b.task_count || 0);
                const share = total ? Math.round((count / total) * 100) : 0;
                return (
                  <Link
                    key={b.id}
                    to={`/board/${b.id}`}
                    className="group flex items-center gap-2.5"
                  >
                    <span
                      className="h-2.5 w-2.5 shrink-0 rounded-full"
                      style={{ backgroundColor: b.color || "#2f8159" }}
                    />
                    <span className="flex-1 truncate text-[13px] font-medium text-ink transition-colors group-hover:text-brand-600">
                      {b.title}
                    </span>
                    <span className="text-[13px] font-semibold tabular text-ink">
                      {count}
                    </span>
                    <span className="w-9 text-right text-[11px] tabular text-faint">
                      {share}%
                    </span>
                  </Link>
                );
              })}
            </div>
          </div>
        </div>
      ) : (
        <EmptyHint icon={BarChart3}>
          No tasks yet — add some to see the breakdown.
        </EmptyHint>
      )}
    </div>
  );
};

// ── Workspace composition (hand-built SVG donut) ──────────────────────────
const WorkspaceDonut = ({ owned, shared, boards = [], className }) => {
  const total = owned + shared;
  const R = 56;
  const SW = 16;
  const C = 2 * Math.PI * R;
  const ownedLen = total ? (owned / total) * C : 0;
  const sharedLen = total ? (shared / total) * C : 0;

  const totalTasks = boards.reduce((s, b) => s + Number(b.task_count || 0), 0);
  const avg = total ? Math.round(totalTasks / total) : 0;
  const active = boards.filter((b) => Number(b.task_count || 0) > 0).length;
  const busiest = boards.reduce(
    (a, b) => (Number(b.task_count || 0) > Number(a?.task_count || 0) ? b : a),
    null,
  );

  return (
    <div
      className={cn(
        "flex flex-col rounded-3xl border border-line bg-surface p-6 shadow-[var(--shadow-card)]",
        className,
      )}
    >
      <SectionTitle icon={PieChart} hint="Owned vs shared">
        Composition
      </SectionTitle>
      <div className="flex items-center gap-5">
        <div className="relative h-[128px] w-[128px] shrink-0">
          <svg viewBox="0 0 140 140" className="h-full w-full -rotate-90">
            <circle
              cx="70"
              cy="70"
              r={R}
              fill="none"
              stroke="var(--color-surface-2)"
              strokeWidth={SW}
            />
            {owned > 0 && (
              <circle
                cx="70"
                cy="70"
                r={R}
                fill="none"
                stroke="#2f8159"
                strokeWidth={SW}
                strokeDasharray={`${ownedLen} ${C}`}
              />
            )}
            {shared > 0 && (
              <circle
                cx="70"
                cy="70"
                r={R}
                fill="none"
                stroke="#8bc4a4"
                strokeWidth={SW}
                strokeDasharray={`${sharedLen} ${C}`}
                strokeDashoffset={-ownedLen}
              />
            )}
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span className="font-display text-3xl font-semibold tabular leading-none">
              {total}
            </span>
            <span className="mt-1 text-[10px] uppercase tracking-[0.1em] text-faint">
              boards
            </span>
          </div>
        </div>
        <div className="flex flex-1 flex-col gap-3">
          <Legend color="#2f8159" label="Owned" value={owned} />
          <Legend color="#8bc4a4" label="Shared" value={shared} />
        </div>
      </div>

      {/* Workspace at a glance */}
      <div className="mt-6 grid flex-1 content-center gap-3.5 border-t pt-5">
        <StatRow label="Total tasks" value={totalTasks} />
        <StatRow label="Avg per board" value={avg} />
        <StatRow label="Active boards" value={`${active} / ${total}`} />
        {busiest && Number(busiest.task_count || 0) > 0 && (
          <div className="flex items-center justify-between gap-3">
            <span className="text-sm text-muted">Busiest board</span>
            <span className="flex min-w-0 items-center gap-2">
              <span className="truncate text-sm font-semibold text-ink">
                {busiest.title}
              </span>
              <span className="shrink-0 rounded-full bg-surface-2 px-2 py-0.5 text-[11px] font-medium tabular text-muted">
                {busiest.task_count}
              </span>
            </span>
          </div>
        )}
      </div>
    </div>
  );
};

const StatRow = ({ label, value }) => (
  <div className="flex items-center justify-between gap-3">
    <span className="text-sm text-muted">{label}</span>
    <span className="text-sm font-semibold tabular text-ink">{value}</span>
  </div>
);

// ── Recent boards (quick-jump rows) ───────────────────────────────────────
const RecentBoards = ({ boards, className }) => (
  <div
    className={cn(
      "flex flex-col rounded-3xl border border-line bg-surface p-6 shadow-[var(--shadow-card)]",
      className,
    )}
  >
    <SectionTitle icon={Clock} hint="Pick up where you left off">
      Jump back in
    </SectionTitle>
    <div className="flex flex-col gap-0.5">
      {boards.map((b) => {
        const color = b.color || "#2f8159";
        return (
          <Link
            key={b.id}
            to={`/board/${b.id}`}
            className="group -mx-2 flex items-center gap-3 rounded-xl px-2 py-2 transition-colors hover:bg-surface-2"
          >
            <span
              className="grid h-8 w-8 shrink-0 place-items-center rounded-xl font-display text-[12px] font-bold"
              style={{ backgroundColor: `${color}22`, color }}
            >
              {b.title?.[0]?.toUpperCase() || "B"}
            </span>
            <div className="min-w-0 flex-1">
              <p className="truncate text-[13px] font-medium text-ink transition-colors group-hover:text-brand-600">
                {b.title}
              </p>
              <p className="text-[11px] text-faint">
                Updated {relativeTime(b.updated_at)}
              </p>
            </div>
            <span className="hidden items-center gap-1.5 text-[11px] font-medium tabular text-faint sm:flex">
              <Users className="h-3.5 w-3.5" /> {b.member_count}
            </span>
            <span className="shrink-0 rounded-full bg-surface-2 px-2.5 py-1 text-[11px] font-medium tabular text-muted">
              {b.task_count} tasks
            </span>
            <ArrowUpRight className="h-4 w-4 shrink-0 text-faint opacity-0 transition-all duration-200 group-hover:translate-x-0.5 group-hover:text-brand-500 group-hover:opacity-100" />
          </Link>
        );
      })}
    </div>
  </div>
);

// ── Mini bar chart for the KPI corner (per-board task distribution) ────────
const Sparkbars = ({ data, featured }) => {
  const bars = data.slice(-11);
  const max = Math.max(1, ...bars);
  const peak = Math.max(...bars);
  return (
    <div className="flex h-10 items-end gap-[3px]" aria-hidden="true">
      {bars.map((v, i) => (
        <span
          key={i}
          className={cn(
            "w-1.5 rounded-full",
            featured
              ? "bg-white/45"
              : v === peak
                ? "bg-brand-500"
                : "bg-brand-300",
          )}
          style={{ height: `${Math.max((v / max) * 100, 14)}%` }}
        />
      ))}
    </div>
  );
};

// ── KPI card (bento style — big number, icon medallion, contextual hint) ──
const KpiCard = ({
  label,
  value,
  hint,
  featured,
  trend,
  icon: Icon = ArrowUpRight,
}) => (
  <div
    className={cn(
      "group relative overflow-hidden rounded-3xl p-5 transition-shadow duration-300",
      featured
        ? "brand-gradient text-white shadow-[var(--shadow-brand)]"
        : "border border-line bg-surface text-ink hover:shadow-[var(--shadow-soft)]",
    )}
  >
    {featured && (
      <>
        <div className="absolute -right-8 -top-10 h-28 w-28 rounded-full bg-white/15 blur-xl" />
        <div className="absolute -bottom-12 -left-6 h-24 w-24 rounded-full bg-white/10 blur-xl" />
      </>
    )}
    <div className="relative">
      <div className="mb-7 flex items-start justify-between gap-2">
        <span
          className={cn(
            "text-[15px] font-medium",
            featured ? "text-white/90" : "text-ink",
          )}
        >
          {label}
        </span>
        <span
          className={cn(
            "grid h-9 w-9 shrink-0 place-items-center rounded-2xl transition-colors duration-200",
            featured
              ? "bg-white/20 text-white"
              : "bg-brand-50 text-brand-600 group-hover:bg-brand-100",
          )}
        >
          <Icon className="h-[18px] w-[18px]" />
        </span>
      </div>
      <div className="flex items-end justify-between gap-3">
        <div className="min-w-0">
          <p className="font-display text-[40px] font-semibold leading-none tracking-tight tabular">
            {value}
          </p>
          {hint && (
            <p
              className={cn(
                "mt-3 text-xs",
                featured ? "text-white/75" : "text-muted",
              )}
            >
              {hint}
            </p>
          )}
        </div>
        {trend && trend.length >= 2 && (
          <Sparkbars data={trend} featured={featured} />
        )}
      </div>
    </div>
  </div>
);

// ── AI promo (gradient bento accent card) ─────────────────────────────────
const AIPromo = ({ onCreate, className }) => (
  <div
    className={cn(
      "brand-gradient relative flex flex-col justify-between overflow-hidden rounded-3xl p-6 text-white shadow-[var(--shadow-brand)]",
      className,
    )}
  >
    <div className="absolute -right-10 -top-12 h-32 w-32 rounded-full bg-white/15 blur-2xl" />
    <div className="absolute -bottom-14 -left-8 h-28 w-28 rounded-full bg-white/10 blur-2xl" />
    <div className="relative">
      <span className="grid h-11 w-11 place-items-center rounded-2xl bg-white/20 backdrop-blur">
        <Sparkles className="h-5 w-5" />
      </span>
      <h3 className="mt-4 font-display text-lg font-semibold tracking-tight">
        Let AI plan your sprint
      </h3>
      <p className="mt-1.5 text-sm leading-relaxed text-white/80">
        Spin up a board and turn a one-line goal into a prioritized backlog in
        seconds.
      </p>
    </div>
    <button
      onClick={onCreate}
      className="relative mt-6 inline-flex h-11 items-center justify-center gap-2 rounded-full bg-white px-5 text-sm font-semibold text-brand-700 shadow-[var(--shadow-card)] transition-transform duration-200 active:scale-[0.97]"
    >
      <Plus className="h-4 w-4" /> New board
    </button>
  </div>
);

const EmptyState = ({ onCreate }) => (
  <div className="card flex flex-col items-center justify-center gap-4 rounded-3xl py-20 text-center">
    <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-50 text-brand-500">
      <Sparkles className="h-7 w-7" />
    </div>
    <div>
      <h3 className="font-display text-lg font-semibold tracking-tight">
        Create your first board
      </h3>
      <p className="mt-1.5 max-w-sm text-sm leading-relaxed text-muted">
        Spin up a board and let AI generate your first set of tasks from a
        simple goal.
      </p>
    </div>
    <Button onClick={onCreate}>
      <Plus className="h-4 w-4" /> New board
    </Button>
  </div>
);

export default Dashboard;
