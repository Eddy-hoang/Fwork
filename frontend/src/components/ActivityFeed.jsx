import { useState, useEffect } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Activity, X } from "lucide-react";
import { boardApi } from "../lib/api";
import { getSocket } from "../lib/socket";
import Avatar from "./ui/Avatar";
import { relativeTime } from "../lib/utils";

const ActivityFeed = ({ open, onClose, boardId }) => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!open) return;
    setLoading(true);
    boardApi
      .activity(boardId, 50)
      .then(setActivities)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [open, boardId]);

  // Live updates (subscribe always so the feed stays fresh when reopened).
  useEffect(() => {
    const socket = getSocket();
    const onNew = (a) => setActivities((prev) => [a, ...prev].slice(0, 80));
    socket.on("activity:new", onNew);
    return () => socket.off("activity:new", onNew);
  }, [boardId]);

  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            className="fixed inset-0 z-30 bg-ink/35 backdrop-blur-sm"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />
          <motion.aside
            className="glass fixed right-0 top-0 z-40 flex h-full w-80 flex-col border-l"
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "tween", duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
          >
            <div className="flex items-center justify-between border-b px-4 py-4">
              <h3 className="flex items-center gap-2 font-display text-sm font-semibold tracking-tight">
                <Activity className="h-4 w-4 text-brand-500" /> Activity
              </h3>
              <button onClick={onClose} className="rounded p-1 text-muted hover:bg-surface-2 hover:text-ink">
                <X className="h-4 w-4" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-3">
              {loading ? (
                <div className="space-y-3">
                  {Array.from({ length: 6 }).map((_, i) => (
                    <div key={i} className="skeleton h-12 rounded-lg" />
                  ))}
                </div>
              ) : activities.length === 0 ? (
                <p className="px-2 py-8 text-center text-sm text-faint">No activity yet.</p>
              ) : (
                <ul className="space-y-1">
                  {activities.map((a) => (
                    <li key={a.id} className="flex gap-3 rounded-lg px-2 py-2 hover:bg-surface-2">
                      <Avatar name={a.user_name || "System"} id={a.user_id || a.id} src={a.user_avatar} size="xs" />
                      <div className="min-w-0">
                        <p className="text-sm leading-snug text-muted">{a.message}</p>
                        <p className="mt-0.5 text-[11px] text-faint">{relativeTime(a.created_at)}</p>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </motion.aside>
        </>
      )}
    </AnimatePresence>
  );
};

export default ActivityFeed;
