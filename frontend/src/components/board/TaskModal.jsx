import { useState, useEffect } from "react";
import toast from "react-hot-toast";
import { Trash2, GitBranch, Loader2 } from "lucide-react";
import Modal from "../ui/Modal";
import Button from "../ui/Button";
import { Input, Textarea, Select } from "../ui/Input";
import { PRIORITIES } from "../../lib/utils";

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

const TaskModal = ({ open, onClose, task, defaultColumnId, columns, members, actions, onBreakdown }) => {
  const isEdit = Boolean(task);
  const [form, setForm] = useState(empty(defaultColumnId));
  const [saving, setSaving] = useState(false);
  const [breakingDown, setBreakingDown] = useState(false);

  useEffect(() => {
    if (!open) return;
    if (task) {
      setForm({
        title: task.title || "",
        description: task.description || "",
        priority: task.priority || "medium",
        due_date: toDateInput(task.due_date),
        assignee_id: task.assignee_id || "",
        column_id: task.column_id,
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
    const payload = {
      title: form.title.trim(),
      description: form.description.trim() || null,
      priority: form.priority,
      due_date: form.due_date || null,
      assignee_id: form.assignee_id || null,
    };
    try {
      if (isEdit) {
        await actions.updateTask(task.id, payload);
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
    <Modal open={open} onClose={onClose} title={isEdit ? "Edit task" : "New task"} size="md">
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
            {members.map((m) => (
              <option key={m.id} value={m.id}>{m.name}</option>
            ))}
          </Select>
          {!isEdit && (
            <Select label="Column" value={form.column_id} onChange={set("column_id")}>
              {columns.map((c) => (
                <option key={c.id} value={c.id}>{c.title}</option>
              ))}
            </Select>
          )}
        </div>

        <div className="flex items-center justify-between gap-2 pt-2">
          <div>
            {isEdit && (
              <Button type="button" variant="ghost" onClick={handleDelete} className="text-priority-urgent">
                <Trash2 className="h-4 w-4" /> Delete
              </Button>
            )}
          </div>
          <div className="flex gap-2">
            {isEdit && (
              <Button type="button" variant="outline" onClick={handleBreakdown} disabled={breakingDown}>
                {breakingDown ? <Loader2 className="h-4 w-4 animate-spin" /> : <GitBranch className="h-4 w-4" />}
                AI breakdown
              </Button>
            )}
            <Button type="submit" loading={saving}>{isEdit ? "Save" : "Create task"}</Button>
          </div>
        </div>
      </form>
    </Modal>
  );
};

export default TaskModal;
