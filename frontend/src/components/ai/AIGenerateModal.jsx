import { useState, useEffect } from "react";
import toast from "react-hot-toast";
import { Sparkles, Loader2, Check } from "lucide-react";
import Modal from "../ui/Modal";
import Button from "../ui/Button";
import { Input, Select } from "../ui/Input";
import { PriorityBadge } from "../ui/Badge";
import { aiApi } from "../../lib/api";

const AIGenerateModal = ({ open, onClose, boardId, columns, defaultColumnId, onCreated }) => {
  const [goal, setGoal] = useState("");
  const [count, setCount] = useState(6);
  const [columnId, setColumnId] = useState(defaultColumnId || columns[0]?.id || "");
  const [loading, setLoading] = useState(false);
  const [suggestions, setSuggestions] = useState(null);

  useEffect(() => {
    if (open) {
      setGoal("");
      setSuggestions(null);
      setColumnId(defaultColumnId || columns[0]?.id || "");
    }
  }, [open, defaultColumnId, columns]);

  const generate = async (e) => {
    e?.preventDefault();
    if (!goal.trim()) return;
    setLoading(true);
    setSuggestions(null);
    try {
      const res = await aiApi.generateTasks(boardId, { goal: goal.trim(), count: Number(count) });
      setSuggestions(res.tasks);
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  const addAll = async () => {
    setLoading(true);
    try {
      const res = await aiApi.generateTasks(boardId, {
        goal: goal.trim(),
        count: Number(count),
        column_id: columnId,
      });
      onCreated?.(res.tasks);
      toast.success(`Added ${res.tasks.length} tasks`);
      onClose();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      size="lg"
      title={
        <span className="flex items-center gap-2">
          <Sparkles className="h-5 w-5 text-brand-500" /> AI Task Generator
        </span>
      }
      description="Describe a goal and let AI draft a backlog."
    >
      <form onSubmit={generate} className="space-y-4">
        <Input
          label="Project goal"
          placeholder="e.g. Build an e-commerce checkout flow"
          autoFocus
          value={goal}
          onChange={(e) => setGoal(e.target.value)}
        />
        <div className="grid grid-cols-2 gap-4">
          <Select label="Number of tasks" value={count} onChange={(e) => setCount(e.target.value)}>
            {[4, 6, 8, 10].map((n) => (
              <option key={n} value={n}>{n} tasks</option>
            ))}
          </Select>
          <Select label="Add to column" value={columnId} onChange={(e) => setColumnId(e.target.value)}>
            {columns.map((c) => (
              <option key={c.id} value={c.id}>{c.title}</option>
            ))}
          </Select>
        </div>
        {!suggestions && (
          <Button type="submit" className="w-full" loading={loading}>
            <Sparkles className="h-4 w-4" /> Generate tasks
          </Button>
        )}
      </form>

      {loading && !suggestions && (
        <div className="mt-5 flex items-center justify-center gap-2 py-8 text-muted">
          <Loader2 className="h-5 w-5 animate-spin text-brand-500" /> Thinking…
        </div>
      )}

      {suggestions && (
        <div className="mt-5 space-y-2">
          <p className="text-[11px] font-semibold uppercase tracking-[0.1em] text-faint tabular">{suggestions.length} suggested tasks</p>
          <div className="max-h-72 space-y-2 overflow-y-auto pr-1">
            {suggestions.map((t, i) => (
              <div key={i} className="card rounded-2xl p-3.5">
                <div className="flex items-start justify-between gap-2">
                  <p className="text-sm font-medium">{t.title}</p>
                  <PriorityBadge priority={t.priority} />
                </div>
                {t.description && <p className="mt-1 text-xs text-muted">{t.description}</p>}
              </div>
            ))}
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="ghost" onClick={generate} disabled={loading}>Regenerate</Button>
            <Button onClick={addAll} loading={loading}>
              <Check className="h-4 w-4" /> Add all to {columns.find((c) => c.id === columnId)?.title}
            </Button>
          </div>
        </div>
      )}
    </Modal>
  );
};

export default AIGenerateModal;
