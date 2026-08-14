import { useState } from "react";
import toast from "react-hot-toast";
import Modal from "../ui/Modal";
import Button from "../ui/Button";
import { Input } from "../ui/Input";
import { useBoards } from "../../context/BoardsContext";

const CreateWorkspaceModal = ({ open, onClose }) => {
  const { createWorkspace } = useBoards();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;

    setLoading(true);
    try {
      await createWorkspace({ name: name.trim(), description: description.trim() });
      toast.success(`Created workspace "${name.trim()}"`);
      setName("");
      setDescription("");
      onClose();
    } catch (err) {
      toast.error(err.message || "Failed to create workspace");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Create Workspace"
      description="Workspaces organize teams, boards, and projects into separate environments."
    >
      <form onSubmit={onSubmit} className="space-y-4">
        <Input
          label="Workspace Name"
          type="text"
          placeholder="e.g. Acme Marketing, Mobile Team"
          autoFocus
          required
          value={name}
          onChange={(e) => setName(e.target.value)}
        />

        <div className="space-y-1.5">
          <label className="block text-xs font-medium text-muted">Description (optional)</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Short overview of what this workspace is for..."
            className="w-full rounded-2xl border border-line bg-surface p-3 text-xs shadow-[var(--shadow-card)] outline-none transition-all focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 resize-none h-20"
          />
        </div>

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={loading}>
            Create Workspace
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default CreateWorkspaceModal;
