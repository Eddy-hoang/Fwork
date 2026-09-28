import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronDown, LogOut, Plus, Search, Command, Bell, CheckCircle, Sun, Moon, Trash2 } from "lucide-react";
import toast from "react-hot-toast";
import { useAuth } from "../../context/AuthContext";
import { useLayout } from "./AppLayout";
import { notificationApi } from "../../lib/api";
import Avatar from "../ui/Avatar";
import Button from "../ui/Button";
import { useLanguage } from "../../context/LanguageContext";

const Topbar = ({ title, subtitle, actions, onCreateBoard }) => {
  const { user, logout } = useAuth();
  const { openCommand } = useLayout() || {};
  const navigate = useNavigate();
  const { t } = useLanguage();
  const [menuOpen, setMenuOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [isDark, setIsDark] = useState(
    () => localStorage.getItem("pref-theme") === "dark"
  );
  const ref = useRef(null);
  const notifRef = useRef(null);

  useEffect(() => {
    const onClick = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setMenuOpen(false);
      if (notifRef.current && !notifRef.current.contains(e.target)) setNotifOpen(false);
    };
    const onKeyDown = (e) => {
      if (e.key === "Escape") {
        setMenuOpen(false);
        setNotifOpen(false);
      }
    };
    document.addEventListener("mousedown", onClick);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("mousedown", onClick);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, []);

  // Sync document class with theme state
  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add("dark");
      localStorage.setItem("pref-theme", "dark");
    } else {
      document.documentElement.classList.remove("dark");
      localStorage.setItem("pref-theme", "light");
    }
  }, [isDark]);

  // Fetch unread count periodically
  useEffect(() => {
    if (!user) return;
    const fetchUnread = () => {
      notificationApi
        .getUnreadCount()
        .then((data) => setUnreadCount(data?.count || 0))
        .catch(() => {});
    };
    fetchUnread();
    const interval = setInterval(fetchUnread, 30000);
    return () => clearInterval(interval);
  }, [user]);

  const loadNotifications = () => {
    notificationApi
      .list({ page: 0, size: 10 })
      .then((data) => {
        setNotifications(data?.content || data || []);
      })
      .catch(() => {});
  };

  const handleNotifClick = () => {
    setNotifOpen((o) => {
      const next = !o;
      if (next) loadNotifications();
      return next;
    });
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      setUnreadCount(0);
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch (err) {}
  };

  const handleDeleteNotification = async (id, isRead) => {
    try {
      await notificationApi.remove(id);
      setNotifications((prev) => prev.filter((n) => n.id !== id));
      if (!isRead) {
        setUnreadCount((c) => Math.max(0, c - 1));
      }
      toast.success(t("Notification deleted"));
    } catch (err) {
      toast.error(err.message || t("Failed to delete notification"));
    }
  };

  return (
    <header className="glass sticky top-0 z-20 flex h-[72px] items-center gap-4 border-b px-6">
      <div className="min-w-0 shrink">
        {title && <h1 className="truncate font-display text-lg font-bold leading-tight tracking-tight text-ink">{title}</h1>}
        {subtitle && <p className="truncate text-xs text-muted">{subtitle}</p>}
      </div>

      <div className="ml-auto flex items-center gap-2.5">
        {/* Search → command menu */}
        <button
          onClick={openCommand}
          className="hidden h-10 w-56 items-center gap-2.5 rounded-full border border-line bg-surface px-4 text-sm text-faint shadow-[var(--shadow-card)] transition-all duration-200 hover:border-brand-300 hover:text-muted hover:shadow-[var(--shadow-soft)] md:flex lg:w-64"
        >
          <Search className="h-4 w-4 shrink-0" />
          <span className="flex-1 text-left">{t("Search tasks, boards…")}</span>
          <kbd className="flex items-center gap-0.5 rounded-md bg-surface-2 px-1.5 py-0.5 text-[10px] font-semibold text-muted">
            <Command className="h-3 w-3" />K
          </kbd>
        </button>

        {actions}

        {/* Theme Toggle */}
        <button
          onClick={() => setIsDark((d) => !d)}
          className="hidden h-10 w-10 items-center justify-center rounded-full border border-line bg-surface text-muted shadow-[var(--shadow-card)] transition-all duration-200 hover:-translate-y-px hover:text-ink hover:shadow-[var(--shadow-soft)] sm:flex cursor-pointer"
          title={isDark ? "Switch to light theme" : "Switch to dark theme"}
        >
          {isDark ? <Sun className="h-4.5 w-4.5 text-amber-500" /> : <Moon className="h-4.5 w-4.5" />}
        </button>

        {/* Notifications Dropdown */}
        <div className="relative" ref={notifRef}>
          <button
            onClick={handleNotifClick}
            className="relative hidden h-10 w-10 items-center justify-center rounded-full border border-line bg-surface text-muted shadow-[var(--shadow-card)] transition-all duration-200 hover:-translate-y-px hover:text-ink hover:shadow-[var(--shadow-soft)] sm:flex"
            title={t("Notifications")}
          >
            <Bell className="h-4.5 w-4.5" />
            {unreadCount > 0 && (
              <span className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-priority-urgent text-[10px] font-bold text-white shadow-sm">
                {unreadCount > 9 ? "9+" : unreadCount}
              </span>
            )}
          </button>

          {notifOpen && (
            <div className="card animate-in absolute right-0 mt-2 w-80 rounded-2xl p-3 shadow-[var(--shadow-lift)] z-30">
              <div className="flex items-center justify-between pb-2 border-b border-line">
                <span className="font-semibold text-sm text-ink">{t("Notifications")}</span>
                {unreadCount > 0 && (
                  <button
                    onClick={handleMarkAllRead}
                    className="flex items-center gap-1 text-xs text-brand-600 hover:underline"
                  >
                    <CheckCircle className="h-3.5 w-3.5" /> {t("Mark all read")}
                  </button>
                )}
              </div>
              <div className="max-h-64 overflow-y-auto py-2 flex flex-col gap-1.5">
                {notifications.length === 0 ? (
                  <p className="py-4 text-center text-xs text-muted">{t("No notifications")}</p>
                ) : (
                  notifications.map((n) => (
                    <div
                      key={n.id}
                      className={`group relative p-2 pr-8 rounded-xl text-xs flex flex-col gap-0.5 ${
                        n.read ? "bg-transparent opacity-75" : "bg-surface-2 font-medium"
                      }`}
                    >
                      <span className="font-semibold text-ink">{n.title}</span>
                      <span className="text-muted">{n.message}</span>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteNotification(n.id, n.read);
                        }}
                        className="absolute right-2 top-1/2 -translate-y-1/2 rounded-full p-1 text-faint transition-all duration-200 hover:bg-surface hover:text-priority-urgent opacity-100 md:opacity-0 md:group-hover:opacity-100 cursor-pointer"
                        title={t("Delete notification")}
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        <Button size="md" onClick={onCreateBoard} className="hidden sm:inline-flex">
          <Plus className="h-4 w-4" /> {t("New board")}
        </Button>

        <div className="relative" ref={ref}>
          <button
            onClick={() => setMenuOpen((o) => !o)}
            className="flex items-center gap-2 rounded-full border border-line bg-surface py-1 pl-1 pr-2.5 shadow-[var(--shadow-card)] transition-all duration-200 hover:border-brand-300 hover:shadow-[var(--shadow-soft)]"
          >
            <Avatar name={user?.name} id={user?.id} src={user?.avatar || user?.avatar_url} size="sm" />
            <span className="hidden max-w-[7rem] truncate text-sm font-medium text-ink lg:block">
              {user?.name?.split(" ")[0]}
            </span>
            <ChevronDown className="h-4 w-4 text-faint" />
          </button>

          {menuOpen && (
            <div className="card animate-in absolute right-0 mt-2 w-56 rounded-2xl p-1.5 shadow-[var(--shadow-lift)]">
              <div className="px-3 py-2">
                <p className="truncate text-sm font-semibold text-ink">{user?.name}</p>
                <p className="truncate text-xs text-faint">{user?.email}</p>
              </div>
              <div className="my-1 border-t" />
              <button
                onClick={() => {
                  logout();
                  navigate("/login");
                }}
                className="flex w-full items-center gap-2 rounded-xl px-3 py-2 text-sm text-priority-urgent transition-colors hover:bg-surface-2"
              >
                <LogOut className="h-4 w-4" /> {t("Log out")}
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Topbar;
