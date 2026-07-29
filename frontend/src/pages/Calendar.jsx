import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  startOfMonth, endOfMonth, startOfWeek, endOfWeek, eachDayOfInterval,
  addMonths, subMonths, format, isSameMonth, isToday,
} from "date-fns";
import { ChevronLeft, ChevronRight, CalendarDays } from "lucide-react";
import { useLayout } from "../components/layout/AppLayout";
import { useWorkspace } from "../hooks/useWorkspace";
import Topbar from "../components/layout/Topbar";
import { cn, priorityMeta } from "../lib/utils";

const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

const Calendar = () => {
  const { openCreateBoard } = useLayout();
  const { tasks, loading } = useWorkspace();
  const [cursor, setCursor] = useState(() => new Date());

  const days = useMemo(() => {
    const gridStart = startOfWeek(startOfMonth(cursor), { weekStartsOn: 0 });
    const gridEnd = endOfWeek(endOfMonth(cursor), { weekStartsOn: 0 });
    return eachDayOfInterval({ start: gridStart, end: gridEnd });
  }, [cursor]);

  const byDay = useMemo(() => {
    const map = {};
    tasks.forEach((t) => {
      if (!t.due_date) return;
      const key = format(new Date(t.due_date), "yyyy-MM-dd");
      (map[key] ||= []).push(t);
    });
    return map;
  }, [tasks]);

  const monthCount = useMemo(
    () => tasks.filter((t) => t.due_date && isSameMonth(new Date(t.due_date), cursor)).length,
    [tasks, cursor]
  );

  return (
    <>
      <Topbar title="Calendar" subtitle="Tasks by due date" onCreateBoard={openCreateBoard} />

      <div className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-[1600px] px-6 py-8 md:px-8">
          {/* Toolbar */}
          <div className="mb-6 flex flex-wrap items-center gap-3">
            <h2 className="font-display text-2xl font-semibold tracking-tight">{format(cursor, "MMMM yyyy")}</h2>
            <span className="rounded-full bg-surface-2 px-3 py-1 text-xs font-medium tabular text-muted">
              {monthCount} due
            </span>
            <div className="ml-auto flex items-center gap-2">
              <button
                onClick={() => setCursor(new Date())}
                className="h-9 rounded-full border border-line bg-surface px-4 text-xs font-semibold text-ink shadow-[var(--shadow-card)] transition-all duration-200 hover:shadow-[var(--shadow-soft)]"
              >
                Today
              </button>
              <button
                onClick={() => setCursor((c) => subMonths(c, 1))}
                className="grid h-9 w-9 place-items-center rounded-full border border-line bg-surface text-muted shadow-[var(--shadow-card)] transition-all duration-200 hover:text-ink hover:shadow-[var(--shadow-soft)]"
                title="Previous month"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
              <button
                onClick={() => setCursor((c) => addMonths(c, 1))}
                className="grid h-9 w-9 place-items-center rounded-full border border-line bg-surface text-muted shadow-[var(--shadow-card)] transition-all duration-200 hover:text-ink hover:shadow-[var(--shadow-soft)]"
                title="Next month"
              >
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>

          {loading ? (
            <div className="skeleton h-[640px] rounded-3xl" />
          ) : (
            <div className="overflow-hidden rounded-3xl border border-line bg-surface shadow-[var(--shadow-card)]">
              {/* Weekday header */}
              <div className="grid grid-cols-7 border-b bg-surface-2/50">
                {WEEKDAYS.map((d) => (
                  <div key={d} className="px-3 py-2.5 text-center text-[11px] font-semibold uppercase tracking-[0.1em] text-faint">
                    <span className="hidden sm:inline">{d}</span>
                    <span className="sm:hidden">{d[0]}</span>
                  </div>
                ))}
              </div>
              {/* Day grid */}
              <div className="grid grid-cols-7">
                {days.map((day) => {
                  const key = format(day, "yyyy-MM-dd");
                  const items = byDay[key] || [];
                  const inMonth = isSameMonth(day, cursor);
                  const today = isToday(day);
                  return (
                    <div
                      key={key}
                      className={cn(
                        "min-h-[116px] border-b border-r p-2 last:border-r-0 [&:nth-child(7n)]:border-r-0",
                        !inMonth && "bg-surface-2/30"
                      )}
                    >
                      <div className="mb-1.5 flex justify-end">
                        <span
                          className={cn(
                            "grid h-6 w-6 place-items-center rounded-full text-[12px] font-semibold tabular",
                            today ? "bg-brand-500 text-white" : inMonth ? "text-ink" : "text-faint"
                          )}
                        >
                          {format(day, "d")}
                        </span>
                      </div>
                      <div className="space-y-1">
                        {items.slice(0, 3).map((t) => (
                          <Link
                            key={t.id}
                            to={`/board/${t.board_id}`}
                            title={`${t.title} · ${t.board_title}`}
                            className="flex items-center gap-1.5 rounded-md bg-surface-2 px-1.5 py-1 text-[11px] font-medium text-ink transition-colors hover:bg-elevated"
                          >
                            <span
                              className="h-1.5 w-1.5 shrink-0 rounded-full"
                              style={{ backgroundColor: priorityMeta(t.priority).color }}
                            />
                            <span className="truncate">{t.title}</span>
                          </Link>
                        ))}
                        {items.length > 3 && (
                          <p className="px-1.5 text-[10px] font-medium text-faint">+{items.length - 3} more</p>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Legend */}
          <div className="mt-4 flex flex-wrap items-center gap-4">
            <span className="flex items-center gap-2 text-xs text-muted">
              <CalendarDays className="h-3.5 w-3.5" /> Dot color = priority
            </span>
            {["low", "medium", "high", "urgent"].map((p) => {
              const m = priorityMeta(p);
              return (
                <span key={p} className="flex items-center gap-1.5 text-xs text-muted">
                  <span className="h-2 w-2 rounded-full" style={{ backgroundColor: m.color }} /> {m.label}
                </span>
              );
            })}
          </div>
        </div>
      </div>
    </>
  );
};

export default Calendar;
