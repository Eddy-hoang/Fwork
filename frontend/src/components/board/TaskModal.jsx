import { useState, useEffect } from "react";
import toast from "react-hot-toast";
import { Trash2, GitBranch, Loader2, Send, MessageSquare } from "lucide-react";
import Modal from "../ui/Modal";
import Button from "../ui/Button";
import Avatar from "../ui/Avatar";
import { Input, Textarea, Select } from "../ui/Input";
import { PRIORITIES, relativeTime } from "../../lib/utils";
import { commentApi } from "../../lib/api";
import { useAuth } from "../../context/AuthContext";
import { subscribeBoard } from "../../lib/socket";

const toDateInput = (value) => {
  if (!value) return "";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return "";
  return d.toISOString().slice(0, 10);
};

const empty = (columnId) => ({
  title: "",
  description: "",
  priority: "medium",
  due_date: "",
  assignee_id: "",
  column_id: columnId || "",
});

const TaskModal = ({ open, onClose, task, defaultColumnId, columns, members, actions, onBreakdown, boardId }) => {
  const isEdit = Boolean(task);
  const [form, setForm] = useState(empty(defaultColumnId));
  const [saving, setSaving] = useState(false);
  const [breakingDown, setBreakingDown] = useState(false);

  const { user: currentUser } = useAuth();
  const [comments, setComments] = useState([]);
  const [loadingComments, setLoadingComments] = useState(false);
  const [newComment, setNewComment] = useState("");
  const [submittingComment, setSubmittingComment] = useState(false);

  const fetchComments = async () => {
    if (!task?.id) return;
    setLoadingComments(true);
    try {
      const res = await commentApi.list(task.id, { size: 100 });
      setComments(res.content || []);
    } catch (err) {
      console.error("Failed to load comments", err);
    } finally {
      setLoadingComments(false);
    }
  };

  useEffect(() => {
    if (open && isEdit && task?.id) {
      fetchComments();
    } else {
      setComments([]);
      setNewComment("");
    }
  }, [open, isEdit, task?.id]);

  useEffect(() => {
    if (!open || !isEdit || !boardId || !task?.id) return;
    const subscription = subscribeBoard(boardId, (wsEvent) => {
      if (wsEvent.type === "COMMENT_ADDED") {
        fetchComments();
        // Show a notification if another user commented
        const comment = wsEvent.payload || wsEvent.data?.comment;
        if (comment) {
          const authorId = comment.createdBy?.id || comment.user_id || comment.userId;
          const authorName = comment.createdBy?.name || comment.user_name || comment.userName || "A user";
          if (currentUser && String(authorId) !== String(currentUser.id)) {
            toast.success(`New comment from ${authorName}: "${comment.content || "..."}"`);
          }
        }
      } else if (wsEvent.type === "COMMENT_DELETED") {
        fetchComments();
      }
    });
    return () => {
      if (subscription && subscription.unsubscribe) {
        subscription.unsubscribe();
      }
    };
  }, [open, isEdit, boardId, task?.id, currentUser]);

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim() || !task?.id) return;
    setSubmittingComment(true);
    try {
      const created = await commentApi.create(task.id, { content: newComment.trim() });
      if (created && !created.createdBy && currentUser) {
        created.createdBy = {
          id: currentUser.id,
          name: currentUser.name,
          avatar: currentUser.avatar_url || currentUser.avatar,
        };
      }
      setComments((prev) => [...prev, created]);
      setNewComment("");
      toast.success("Comment added successfully");
    } catch (err) {
      toast.error(err.message || "Failed to add comment");
    } finally {
      setSubmittingComment(false);
    }
  };

  useEffect(() => {
    if (!open) return;
    if (task) {
      setForm({
        title: task.title || "",
        description: task.description || "",
        priority: task.priority || "medium",
        due_date: toDateInput(task.due_date || task.dueDate),
        assignee_id: task.assignee_id || task.assigneeId || (typeof task.assignee === "object" ? task.assignee?.id : task.assignee) || "",
        column_id: task.column_id || task.columnId,
      });
    } else {
      setForm(empty(defaultColumnId || columns[0]?.id));
    }
  }, [open, task, defaultColumnId, columns]);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!form.title.trim()) return toast.error("Title is required");
    setSaving(true);
    const formattedDueDate = form.due_date ? `${form.due_date}T23:59:59` : null;
    const payload = {
      title: form.title.trim(),
      description: form.description.trim() || null,
      priority: form.priority,
      dueDate: formattedDueDate,
      due_date: formattedDueDate,
      assignee_id: form.assignee_id || null,
    };
    try {
      if (isEdit) {
        await actions.updateTask(task.id, payload);
        const prevColId = task.column_id || task.columnId;
        if (form.column_id && form.column_id !== prevColId && actions.moveTask) {
          await actions.moveTask(task.id, form.column_id, 0);
        }
        toast.success("Task updated");
      } else {
        await actions.createTask({ ...payload, column_id: form.column_id });
        toast.success("Task created");
      }
      onClose();
    } catch {
      /* handled in hook */
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    await actions.deleteTask(task.id);
    onClose();
  };

  const handleBreakdown = async () => {
    setBreakingDown(true);
    try {
      await onBreakdown(task);
    } finally {
      setBreakingDown(false);
    }
  };

  return (
    <>
    <Modal open={open} onClose={onClose} title={isEdit ? "Task Details" : "New task"} size={isEdit ? "lg" : "md"}>
      {isEdit ? (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* Left Column: Form details */}
          <form onSubmit={onSubmit} className="lg:col-span-7 space-y-4">
            <Input label="Title" placeholder="What needs to be done?" value={form.title} onChange={set("title")} />
            <Textarea label="Description" rows={4} placeholder="Add more detail…" value={form.description} onChange={set("description")} />

            <div className="grid grid-cols-2 gap-4">
              <Select label="Priority" value={form.priority} onChange={set("priority")}>
                {PRIORITIES.map((p) => (
                  <option key={p.value} value={p.value}>{p.label}</option>
                ))}
              </Select>
              <Input label="Due date" type="date" value={form.due_date} onChange={set("due_date")} />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Select label="Assignee" value={form.assignee_id} onChange={set("assignee_id")}>
                <option value="">Unassigned</option>
                {members.map((m) => {
                  const uId = m.userId || m.user_id || m.id;
                  return (
                    <option key={uId} value={uId}>{m.name}</option>
                  );
                })}
              </Select>
              <Select label="Column / Status" value={form.column_id} onChange={set("column_id")}>
                {columns.map((c) => (
                  <option key={c.id} value={c.id}>{c.title || c.name}</option>
                ))}
              </Select>
            </div>

            <div className="flex items-center justify-between gap-2 pt-4 border-t border-line mt-6">
              <div>
                <Button type="button" variant="ghost" onClick={handleDelete} className="text-priority-urgent hover:bg-priority-urgent/10">
                  <Trash2 className="h-4 w-4" /> Delete
                </Button>
              </div>
              <div className="flex gap-2">
                <Button type="button" variant="outline" onClick={handleBreakdown} disabled={breakingDown}>
                  {breakingDown ? <Loader2 className="h-4 w-4 animate-spin" /> : <GitBranch className="h-4 w-4" />}
                  AI breakdown
                </Button>
                <Button type="submit" loading={saving}>Save</Button>
              </div>
            </div>
          </form>

          {/* Right Column: Comments section */}
          <div className="lg:col-span-5 flex flex-col border-t lg:border-t-0 lg:border-l border-line pt-6 lg:pt-0 lg:pl-6">
            <h3 className="flex items-center gap-2 font-display text-sm font-semibold tracking-tight text-ink mb-4">
              <MessageSquare className="h-4 w-4 text-brand-500" />
              Comments
              <span className="ml-1.5 rounded-full bg-surface-2 px-2 py-0.5 text-xs font-medium tabular text-muted">
                {comments.length}
              </span>
            </h3>

            {/* Comments List */}
            <div 
              className="flex-1 overflow-y-auto space-y-4 pr-1 mb-4"
              style={{ minHeight: "220px", maxHeight: "320px" }}
            >
              {loadingComments ? (
                <div className="space-y-3">
                  {Array.from({ length: 3 }).map((_, i) => (
                    <div key={i} className="flex gap-3 items-start">
                      <div className="h-8 w-8 rounded-full skeleton shrink-0" />
                      <div className="flex-1 space-y-1.5">
                        <div className="h-3 w-20 skeleton rounded" />
                        <div className="h-8 skeleton rounded-xl" />
                      </div>
                    </div>
                  ))}
                </div>
              ) : comments.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-12 text-center text-faint">
                  <MessageSquare className="h-8 w-8 mb-2 opacity-40 text-brand-300" />
                  <p className="text-xs">No comments yet.</p>
                  <p className="text-[11px]">Start the conversation below.</p>
                </div>
              ) : (
                <div className="space-y-3.5">
                  {comments.map((comment) => (
                      <div key={comment.id} className="flex gap-2.5 items-start group">
                        <Avatar
                          name={comment.createdBy?.name || "User"}
                          id={comment.createdBy?.id}
                          src={comment.createdBy?.avatar}
                          size="xs"
                        />
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between gap-2 mb-0.5">
                            <span className="text-xs font-medium text-ink truncate">
                              {comment.createdBy?.name || "Unknown User"}
                            </span>
                            <span className="text-[10px] text-faint shrink-0">
                              {relativeTime(comment.createdAt)}
                            </span>
                          </div>
                          <div className="relative rounded-2xl rounded-tl-none bg-surface-2/65 px-3 py-2 text-xs text-muted leading-relaxed group-hover:bg-surface-2 transition-colors">
                            <p className="whitespace-pre-wrap break-words">{comment.content}</p>
                          </div>
                        </div>
                      </div>
                    ))}
                </div>
              )}
            </div>

            {/* Comment Form */}
            <form onSubmit={handleAddComment} className="border-t border-line pt-4 mt-auto">
              <div className="relative flex items-center">
                <input
                  type="text"
                  placeholder="Write a comment…"
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  disabled={submittingComment}
                  className="w-full rounded-full border border-line bg-surface py-2 pl-4 pr-10 text-xs outline-none transition-all focus:border-brand-500/50 focus:ring-2 focus:ring-brand-500/15 disabled:opacity-50"
                />
                <button
                  type="submit"
                  disabled={!newComment.trim() || submittingComment}
                  className="absolute right-1 top-1/2 -translate-y-1/2 flex h-7 w-7 items-center justify-center rounded-full bg-brand-500 text-white shadow-sm hover:bg-brand-600 active:scale-95 transition-all disabled:opacity-30 disabled:pointer-events-none cursor-pointer"
                >
                  {submittingComment ? (
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                  ) : (
                    <Send className="h-3.5 w-3.5" />
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : (
        <form onSubmit={onSubmit} className="space-y-4">
          <Input label="Title" placeholder="What needs to be done?" autoFocus value={form.title} onChange={set("title")} />
          <Textarea label="Description" rows={4} placeholder="Add more detail…" value={form.description} onChange={set("description")} />

          <div className="grid grid-cols-2 gap-4">
            <Select label="Priority" value={form.priority} onChange={set("priority")}>
              {PRIORITIES.map((p) => (
                <option key={p.value} value={p.value}>{p.label}</option>
              ))}
            </Select>
            <Input label="Due date" type="date" value={form.due_date} onChange={set("due_date")} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Select label="Assignee" value={form.assignee_id} onChange={set("assignee_id")}>
              <option value="">Unassigned</option>
              {members.map((m) => {
                const uId = m.userId || m.user_id || m.id;
                return (
                  <option key={uId} value={uId}>{m.name}</option>
                );
              })}
            </Select>
            <Select label="Column / Status" value={form.column_id} onChange={set("column_id")}>
              {columns.map((c) => (
                <option key={c.id} value={c.id}>{c.title || c.name}</option>
              ))}
            </Select>
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <Button type="submit" loading={saving}>Create task</Button>
          </div>
        </form>
      )}
    </Modal>
    </>
  );
};

export default TaskModal;
