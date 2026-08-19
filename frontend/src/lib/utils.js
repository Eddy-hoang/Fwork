import { formatDistanceToNow, format, isPast, isToday, isTomorrow } from "date-fns";

/** Tiny classnames joiner (no extra deps). */
export const cn = (...args) => args.flat().filter(Boolean).join(" ");

export const PRIORITIES = [
  { value: "low", label: "Low", color: "var(--color-priority-low)" },
  { value: "medium", label: "Medium", color: "var(--color-priority-medium)" },
  { value: "high", label: "High", color: "var(--color-priority-high)" },
  { value: "urgent", label: "Urgent", color: "var(--color-priority-urgent)" },
];

export const priorityMeta = (value) => {
  if (!value) return PRIORITIES[1];
  const val = String(value).toLowerCase();
  return PRIORITIES.find((p) => p.value === val) || PRIORITIES[1];
};

// Pastel accents assigned to columns by index (Linear/Notion-style boards).
const COLUMN_ACCENTS = [
  { dot: "#f43f5e", soft: "rgba(244, 63, 94, 0.06)", ring: "rgba(244, 63, 94, 0.22)" }, // rose
  { dot: "#f59e0b", soft: "rgba(245, 158, 11, 0.07)", ring: "rgba(245, 158, 11, 0.24)" }, // amber
  { dot: "#0ea5e9", soft: "rgba(14, 165, 233, 0.06)", ring: "rgba(14, 165, 233, 0.22)" }, // sky
  { dot: "#8b5cf6", soft: "rgba(139, 92, 246, 0.06)", ring: "rgba(139, 92, 246, 0.22)" }, // violet
  { dot: "#10b981", soft: "rgba(16, 185, 129, 0.06)", ring: "rgba(16, 185, 129, 0.22)" }, // emerald
  { dot: "#ec4899", soft: "rgba(236, 72, 153, 0.06)", ring: "rgba(236, 72, 153, 0.22)" }, // pink
];

export const columnAccent = (index = 0) =>
  COLUMN_ACCENTS[((index % COLUMN_ACCENTS.length) + COLUMN_ACCENTS.length) % COLUMN_ACCENTS.length];

export const initials = (name) =>
  String(name || "")
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0]?.toUpperCase())
    .join("") || "?";

/** Deterministic avatar color from a string id — earthy palette to match the green theme. */
export const colorFromId = (id) => {
  const str = String(id || "");
  const palette = ["#2f8159", "#2c9c8f", "#6f9b54", "#5f7da6", "#c26a45", "#9a7b3c", "#a05d7d"];
  let hash = 0;
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash);
  return palette[Math.abs(hash) % palette.length];
};

export const relativeTime = (date) => {
  if (!date) return "";
  try {
    return formatDistanceToNow(new Date(date), { addSuffix: true });
  } catch {
    return "";
  }
};

export const formatDueDate = (date) => {
  if (!date) return null;
  const d = new Date(date);
  if (isToday(d)) return { label: "Today", overdue: false };
  if (isTomorrow(d)) return { label: "Tomorrow", overdue: false };
  return { label: format(d, "MMM d"), overdue: isPast(d) };
};

export const getCompletedTasks = () => {
  try {
    const saved = localStorage.getItem("completed_task_ids");
    return saved ? JSON.parse(saved) : [];
  } catch {
    return [];
  }
};

export const toggleCompletedTask = (taskId) => {
  try {
    const current = getCompletedTasks();
    const next = current.includes(taskId)
      ? current.filter((id) => id !== taskId)
      : [...current, taskId];
    localStorage.setItem("completed_task_ids", JSON.stringify(next));
    return next;
  } catch {
    return [];
  }
};
