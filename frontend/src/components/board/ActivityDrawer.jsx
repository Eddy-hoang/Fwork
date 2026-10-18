import React, { useState, useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  X,
  Activity,
  ArrowRightLeft,
  MessageSquare,
  PlusCircle,
  UserPlus,
  Tag,
  Clock,
  Sparkles,
  ListFilter,
} from "lucide-react";
import api from "../../lib/api";

// Safely parse date strings (treats ISO timestamps without offset from DB as UTC)
const parseDate = (dateStr) => {
  if (!dateStr) return new Date();
  if (typeof dateStr === "number") return new Date(dateStr);
  if (
    typeof dateStr === "string" &&
    !dateStr.endsWith("Z") &&
    !/[+-]\d{2}:\d{2}$/.test(dateStr)
  ) {
    return new Date(dateStr + "Z");
  }
  return new Date(dateStr);
};

const formatRelativeTime = (dateStr) => {
  if (!dateStr) return "";
  const date = parseDate(dateStr);
  const now = new Date();
  const diffInSeconds = Math.floor((now - date) / 1000);

  if (diffInSeconds < 0 || diffInSeconds < 60) return "Vừa xong";
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) return `${diffInMinutes} phút trước`;
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) return `${diffInHours} giờ trước`;
  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 30) return `${diffInDays} ngày trước`;
  return date.toLocaleDateString("vi-VN");
};

const getActivityStyle = (actionType) => {
  switch (actionType) {
    case "TASK_CREATED":
      return {
        icon: PlusCircle,
        badgeBg:
          "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20",
        label: "Tạo mới",
      };
    case "TASK_MOVED":
      return {
        icon: ArrowRightLeft,
        badgeBg:
          "bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20",
        label: "Di chuyển",
      };
    case "COMMENT_ADDED":
      return {
        icon: MessageSquare,
        badgeBg:
          "bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/20",
        label: "Bình luận",
      };
    case "TASK_ASSIGNED":
      return {
        icon: UserPlus,
        badgeBg:
          "bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20",
        label: "Phân công",
      };
    case "LABEL_UPDATED":
      return {
        icon: Tag,
        badgeBg:
          "bg-pink-500/10 text-pink-600 dark:text-pink-400 border-pink-500/20",
        label: "Thẻ nhãn",
      };
    default:
      return {
        icon: Activity,
        badgeBg:
          "bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 border-indigo-500/20",
        label: "Cập nhật",
      };
  }
};

const formatDescription = (actorName, description) => {
  if (!description) return "";
  if (actorName && description.startsWith(actorName)) {
    return description.substring(actorName.length).trim();
  }
  return description;
};

export const ActivityDrawer = ({ isOpen, onClose, boardId }) => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState("ALL");
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  const fetchActivities = useCallback(
    async (pageNum = 0, filterType = "ALL", append = false) => {
      if (!boardId) return;
      try {
        setLoading(true);
        setError(null);
        const actionParam = filterType !== "ALL" ? filterType : "";
        const res = await api.get(
          `/boards/${boardId}/activities?page=${pageNum}&size=20${
            actionParam ? `&type=${actionParam}` : ""
          }`
        );

        const data = res?.content || (Array.isArray(res) ? res : []);
        const totalPages = res?.totalPages || 1;

        if (append) {
          setActivities((prev) => [...prev, ...data]);
        } else {
          setActivities(data);
        }

        setPage(pageNum);
        setHasMore(pageNum < totalPages - 1);
      } catch (err) {
        console.error("Failed to load activities:", err);
        setError(err.message || "Không thể tải lịch sử hoạt động");
      } finally {
        setLoading(false);
      }
    },
    [boardId]
  );

  useEffect(() => {
    if (isOpen && boardId) {
      fetchActivities(0, filter, false);
    }
  }, [isOpen, boardId, filter, fetchActivities]);

  const handleFilterChange = (newFilter) => {
    setFilter(newFilter);
    setPage(0);
    fetchActivities(0, newFilter, false);
  };

  const handleLoadMore = () => {
    if (hasMore && !loading) {
      fetchActivities(page + 1, filter, true);
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 overflow-hidden">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="absolute inset-0 bg-ink/40 backdrop-blur-sm"
            onClick={onClose}
          />

          {/* Drawer Panel */}
          <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
            <motion.div
              initial={{ x: "100%" }}
              animate={{ x: 0 }}
              exit={{ x: "100%" }}
              transition={{ type: "spring", damping: 25, stiffness: 280 }}
              className="w-screen max-w-md bg-surface border-l border-line shadow-2xl flex flex-col text-ink"
            >
              {/* Header */}
              <div className="p-5 border-b border-line bg-surface/90 backdrop-blur flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 rounded-2xl bg-brand-50 text-brand-700 dark:bg-brand-50 dark:text-brand-400 border border-brand-100 dark:border-brand-300/30">
                    <Activity className="w-5 h-5" />
                  </div>
                  <div>
                    <h2 className="text-base font-semibold text-ink flex items-center gap-2">
                      Lịch sử hoạt động
                    </h2>
                    <p className="text-xs text-muted">
                      Nhật ký thay đổi trên bảng công việc
                    </p>
                  </div>
                </div>

                <button
                  onClick={onClose}
                  className="p-2 rounded-xl text-muted hover:text-ink hover:bg-surface-2 transition-all border border-transparent hover:border-line"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Filter Tabs */}
              <div className="px-5 py-3.5 flex gap-2 border-b border-line bg-surface-2/60 text-xs overflow-x-auto no-scrollbar">
                <button
                  onClick={() => handleFilterChange("ALL")}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-medium transition-all ${
                    filter === "ALL"
                      ? "bg-brand-500 text-white shadow-md shadow-brand-500/20"
                      : "text-muted hover:text-ink hover:bg-surface"
                  }`}
                >
                  <ListFilter className="w-3.5 h-3.5" />
                  Tất cả
                </button>
                <button
                  onClick={() => handleFilterChange("TASK_MOVED")}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-medium transition-all ${
                    filter === "TASK_MOVED"
                      ? "bg-brand-500 text-white shadow-md shadow-brand-500/20"
                      : "text-muted hover:text-ink hover:bg-surface"
                  }`}
                >
                  <ArrowRightLeft className="w-3.5 h-3.5" />
                  Di chuyển
                </button>
                <button
                  onClick={() => handleFilterChange("COMMENT_ADDED")}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-medium transition-all ${
                    filter === "COMMENT_ADDED"
                      ? "bg-brand-500 text-white shadow-md shadow-brand-500/20"
                      : "text-muted hover:text-ink hover:bg-surface"
                  }`}
                >
                  <MessageSquare className="w-3.5 h-3.5" />
                  Bình luận
                </button>
              </div>

              {/* Activity Timeline List */}
              <div className="flex-1 overflow-y-auto p-5 relative">
                {/* Vertical Timeline Line */}
                {activities.length > 0 && (
                  <div className="absolute left-[2.4rem] top-7 bottom-7 w-0.5 bg-line rounded-full" />
                )}

                {loading && activities.length === 0 ? (
                  <div className="space-y-4">
                    {[1, 2, 3, 4].map((i) => (
                      <div
                        key={i}
                        className="flex items-start gap-4 p-3.5 rounded-2xl bg-surface-2/50 border border-line animate-pulse"
                      >
                        <div className="w-9 h-9 rounded-full bg-line shrink-0" />
                        <div className="flex-1 space-y-2">
                          <div className="h-4 bg-line rounded w-3/4" />
                          <div className="h-3 bg-line/60 rounded w-1/3" />
                        </div>
                      </div>
                    ))}
                  </div>
                ) : error && activities.length === 0 ? (
                  <div className="h-full flex flex-col items-center justify-center text-center p-8">
                    <p className="text-sm font-medium text-rose-500 mb-1">
                      {error}
                    </p>
                    <p className="text-xs text-muted mb-3">
                      Máy chủ gặp lỗi khi lấy dữ liệu hoạt động.
                    </p>
                    <button
                      onClick={() => fetchActivities(0, filter, false)}
                      className="px-3.5 py-1.5 text-xs font-medium rounded-xl bg-brand-500 text-white hover:bg-brand-600 transition-colors"
                    >
                      Thử lại
                    </button>
                  </div>
                ) : activities.length === 0 ? (
                  <div className="h-full flex flex-col items-center justify-center text-center p-8">
                    <div className="w-12 h-12 rounded-2xl bg-surface-2 text-faint flex items-center justify-center mb-3 border border-line">
                      <Sparkles className="w-6 h-6 text-faint" />
                    </div>
                    <p className="text-sm font-medium text-ink">
                      Chưa có nhật ký nào
                    </p>
                    <p className="text-xs text-faint mt-1 max-w-xs">
                      Các thay đổi trên bảng công việc sẽ được tự động ghi lại tại đây.
                    </p>
                  </div>
                ) : (
                  <div className="space-y-5 relative">
                    {activities.map((item) => {
                      const style = getActivityStyle(item.actionType);
                      const IconComponent = style.icon;
                      const cleanDesc = formatDescription(
                        item.actorName,
                        item.description
                      );

                      return (
                        <div
                          key={item.id}
                          className="group relative flex items-start gap-3.5"
                        >
                          {/* Avatar & Action Badge */}
                          <div className="relative shrink-0 z-10">
                            <div className="w-9 h-9 rounded-full bg-brand-500 text-white flex items-center justify-center text-xs font-semibold ring-2 ring-surface shadow-sm overflow-hidden">
                              {item.actorAvatar ? (
                                <img
                                  src={item.actorAvatar}
                                  alt={item.actorName}
                                  className="w-full h-full object-cover"
                                />
                              ) : (
                                (item.actorName || "U")
                                  .charAt(0)
                                  .toUpperCase()
                              )}
                            </div>
                            <div className="absolute -bottom-1 -right-1 w-4 h-4 rounded-full bg-surface border border-line flex items-center justify-center">
                              <IconComponent className="w-2.5 h-2.5 text-brand-500" />
                            </div>
                          </div>

                          {/* Activity Content Card */}
                          <div className="flex-1 min-w-0 bg-surface hover:bg-surface-2/60 border border-line hover:border-line rounded-2xl p-3.5 transition-all shadow-sm group-hover:shadow-md">
                            <div className="flex items-center justify-between gap-2 mb-1.5">
                              <span className="text-xs font-semibold text-ink truncate">
                                {item.actorName || "Thành viên"}
                              </span>
                              <span
                                className={`text-[10px] font-medium px-2 py-0.5 rounded-full border ${style.badgeBg}`}
                              >
                                {style.label}
                              </span>
                            </div>

                            <p className="text-xs text-muted leading-relaxed break-words">
                              {cleanDesc}
                            </p>

                            <div className="flex items-center gap-1 text-[11px] text-faint mt-2">
                              <Clock className="w-3 h-3" />
                              <span>{formatRelativeTime(item.createdAt)}</span>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}

                {/* Load More Button */}
                {hasMore && (
                  <div className="pt-6 pb-2 text-center">
                    <button
                      onClick={handleLoadMore}
                      disabled={loading}
                      className="inline-flex items-center gap-2 px-4 py-2 text-xs font-medium text-brand-500 border border-brand-300 dark:border-brand-500/30 rounded-xl hover:bg-brand-50 transition-all disabled:opacity-50"
                    >
                      {loading ? (
                        <>
                          <div className="w-3 h-3 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
                          <span>Đang tải...</span>
                        </>
                      ) : (
                        <span>Tải thêm lịch sử</span>
                      )}
                    </button>
                  </div>
                )}
              </div>
            </motion.div>
          </div>
        </div>
      )}
    </AnimatePresence>
  );
};

export default ActivityDrawer;
