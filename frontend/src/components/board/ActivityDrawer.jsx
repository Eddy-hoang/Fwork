import React, { useState, useEffect, useCallback } from "react";
import api from "../../lib/api";

const formatRelativeTime = (dateStr) => {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  const now = new Date();
  const diffInSeconds = Math.floor((now - date) / 1000);

  if (diffInSeconds < 60) return "Vừa xong";
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) return `${diffInMinutes} phút trước`;
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) return `${diffInHours} giờ trước`;
  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 30) return `${diffInDays} ngày trước`;
  return date.toLocaleDateString("vi-VN");
};

export const ActivityDrawer = ({ isOpen, onClose, boardId }) => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState("ALL");
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  const fetchActivities = useCallback(
    async (pageNum = 0, filterType = "ALL", append = false) => {
      if (!boardId) return;
      try {
        setLoading(true);
        const actionParam = filterType !== "ALL" ? filterType : "";
        const res = await api.get(
          `/boards/${boardId}/activities?page=${pageNum}&size=20${actionParam ? `&type=${actionParam}` : ""}`
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

  if (!isOpen) return null;

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
    <div className="fixed inset-0 z-50 overflow-hidden bg-black/40 backdrop-blur-sm transition-opacity">
      <div className="absolute inset-0" onClick={onClose} />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-white dark:bg-slate-900 shadow-xl flex flex-col">
          {/* Header */}
          <div className="p-4 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800 dark:text-slate-100">
              Lịch sử hoạt động
            </h2>
            <button
              onClick={onClose}
              className="p-1 rounded-md text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
            >
              Close
            </button>
          </div>

          {/* Filter Tabs */}
          <div className="px-4 pt-3 flex gap-2 border-b border-slate-200 dark:border-slate-800 pb-3 text-sm">
            <button
              onClick={() => handleFilterChange("ALL")}
              className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
                filter === "ALL"
                  ? "bg-indigo-50 text-indigo-600 dark:bg-indigo-950/60 dark:text-indigo-400"
                  : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
              }`}
            >
              Tất cả
            </button>
            <button
              onClick={() => handleFilterChange("TASK_MOVED")}
              className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
                filter === "TASK_MOVED"
                  ? "bg-indigo-50 text-indigo-600 dark:bg-indigo-950/60 dark:text-indigo-400"
                  : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
              }`}
            >
              Di chuyển
            </button>
            <button
              onClick={() => handleFilterChange("COMMENT_ADDED")}
              className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
                filter === "COMMENT_ADDED"
                  ? "bg-indigo-50 text-indigo-600 dark:bg-indigo-950/60 dark:text-indigo-400"
                  : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
              }`}
            >
              Bình luận
            </button>
          </div>

          {/* Activity List */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {activities.length === 0 && !loading ? (
              <div className="text-center py-8 text-slate-500 text-sm">
                Chưa có lịch sử hoạt động nào.
              </div>
            ) : (
              activities.map((item) => (
                <div
                  key={item.id}
                  className="flex items-start gap-3 p-3 rounded-lg border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/40"
                >
                  <div className="w-8 h-8 rounded-full bg-indigo-500 text-white flex items-center justify-center text-xs font-semibold shrink-0">
                    {item.actorAvatar ? (
                      <img
                        src={item.actorAvatar}
                        alt={item.actorName}
                        className="w-8 h-8 rounded-full object-cover"
                      />
                    ) : (
                      (item.actorName || "U").charAt(0).toUpperCase()
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-slate-800 dark:text-slate-200 leading-snug">
                      <span className="font-semibold">{item.actorName || "Thành viên"}</span>{" "}
                      {item.description}
                    </p>
                    <span className="text-xs text-slate-400 mt-1 block">
                      {formatRelativeTime(item.createdAt)}
                    </span>
                  </div>
                </div>
              ))
            )}

            {hasMore && (
              <div className="pt-2 text-center">
                <button
                  onClick={handleLoadMore}
                  disabled={loading}
                  className="px-4 py-2 text-xs font-medium text-indigo-600 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-800 rounded-md hover:bg-indigo-50 dark:hover:bg-indigo-950/50 transition-colors disabled:opacity-50"
                >
                  {loading ? "Đang tải..." : "Tải thêm"}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ActivityDrawer;
