import { useState } from "react";
import toast from "react-hot-toast";
import Modal from "../ui/Modal";
import Button from "../ui/Button";
import { Input } from "../ui/Input";
import { workspaceApi } from "../../lib/api";
import { useWorkspace } from "../../hooks/useWorkspace";

const InviteMemberModal = ({ open, onClose }) => {
  const { currentWorkspace, refreshMembers } = useWorkspace();
  const [email, setEmail] = useState("");
  const [role, setRole] = useState("MEMBER");
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!email.trim()) return;
    if (!currentWorkspace?.id) {
      toast.error("No active workspace selected");
      return;
    }

    setLoading(true);
    try {
      await workspaceApi.addMember(currentWorkspace.id, { email, role });
      toast.success(`Successfully added ${email} to ${currentWorkspace.name}`);
      setEmail("");
      refreshMembers();
      onClose();
    } catch (err) {
      toast.error(err.message || "Failed to add member");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Invite a teammate"
      description={`Add members to ${currentWorkspace?.name || "your workspace"} using their email address.`}
    >
      <form onSubmit={onSubmit} className="space-y-4">
        <Input
          label="Email address"
          type="email"
          placeholder="colleague@company.com"
          autoFocus
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <div className="space-y-1.5">
          <label className="block text-xs font-medium text-muted">Role</label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setRole("MEMBER")}
              className={`rounded-2xl border p-3 text-left transition-all ${
                role === "MEMBER"
                  ? "border-brand-500 bg-brand-50/50 ring-2 ring-brand-500/20"
                  : "border-line bg-surface hover:border-line-hover"
              }`}
            >
              <div className="text-xs font-semibold">Member</div>
              <div className="mt-0.5 text-[11px] text-muted">Can view & work on assigned tasks.</div>
            </button>
            <button
              type="button"
              onClick={() => setRole("ADMIN")}
              className={`rounded-2xl border p-3 text-left transition-all ${
                role === "ADMIN"
                  ? "border-brand-500 bg-brand-50/50 ring-2 ring-brand-500/20"
                  : "border-line bg-surface hover:border-line-hover"
              }`}
            >
              <div className="text-xs font-semibold">Admin</div>
              <div className="mt-0.5 text-[11px] text-muted">Can manage boards & workspace settings.</div>
            </button>
          </div>
        </div>

        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={loading}>
            Add Teammate
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default InviteMemberModal;
