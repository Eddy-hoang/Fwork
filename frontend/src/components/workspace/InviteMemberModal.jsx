import { useState, useEffect, useRef } from "react";
import toast from "react-hot-toast";
import Modal from "../ui/Modal";
import Button from "../ui/Button";
import { Input } from "../ui/Input";
import Avatar from "../ui/Avatar";
import { workspaceApi, userApi } from "../../lib/api";
import { useWorkspace } from "../../hooks/useWorkspace";

const InviteMemberModal = ({ open, onClose }) => {
  const { currentWorkspace, refreshMembers } = useWorkspace();
  const [searchQuery, setSearchQuery] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [searching, setSearching] = useState(false);
  const [role, setRole] = useState("MEMBER");
  const [loading, setLoading] = useState(false);
  const containerRef = useRef(null);

  // Close suggestions dropdown when clicking outside
  useEffect(() => {
    const handleOutsideClick = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) {
        setSuggestions([]);
      }
    };
    document.addEventListener("mousedown", handleOutsideClick);
    return () => document.removeEventListener("mousedown", handleOutsideClick);
  }, []);

  // Fetch suggestions when search query changes
  useEffect(() => {
    if (searchQuery.trim().length < 2) {
      setSuggestions([]);
      return;
    }

    // Skip query if search matches currently selected user email
    if (selectedUser && searchQuery === selectedUser.email) {
      setSuggestions([]);
      return;
    }

    const delayDebounceFn = setTimeout(async () => {
      setSearching(true);
      try {
        const users = await userApi.search(searchQuery);
        setSuggestions(users || []);
      } catch (err) {
        console.error("Failed to search users:", err);
      } finally {
        setSearching(false);
      }
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [searchQuery, selectedUser]);

  const onSubmit = async (e) => {
    e.preventDefault();
    const targetEmail = searchQuery.trim();
    if (!targetEmail) return;

    if (!currentWorkspace?.id) {
      toast.error("No active workspace selected");
      return;
    }

    setLoading(true);
    try {
      await workspaceApi.addMember(currentWorkspace.id, { email: targetEmail, role });
      toast.success(`Successfully added ${targetEmail} to ${currentWorkspace.name}`);
      setSearchQuery("");
      setSelectedUser(null);
      setSuggestions([]);
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
        <div className="relative" ref={containerRef}>
          <Input
            label="Email address"
            type="email"
            placeholder="Search by name or email..."
            autoFocus
            required
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              // Reset selection if input deviates
              if (selectedUser && e.target.value !== selectedUser.email) {
                setSelectedUser(null);
              }
            }}
          />
          {searching && (
            <div className="absolute right-3 top-9 text-[10px] text-faint animate-pulse">
              Searching...
            </div>
          )}

          {suggestions.length > 0 && (
            <div className="absolute left-0 right-0 z-50 mt-1 max-h-48 overflow-y-auto rounded-xl border border-line bg-surface py-1.5 shadow-lift">
              {suggestions.map((u) => (
                <button
                  type="button"
                  key={u.id}
                  onClick={() => {
                    setSelectedUser(u);
                    setSearchQuery(u.email);
                    setSuggestions([]);
                  }}
                  className="flex w-full items-center gap-2.5 px-3.5 py-2.5 text-left text-xs transition-colors hover:bg-surface-2 cursor-pointer"
                >
                  <Avatar name={u.name} id={u.id} src={u.avatar} size="xs" />
                  <div className="min-w-0 flex-1">
                    <div className="font-semibold text-ink truncate">{u.name}</div>
                    <div className="text-[10px] text-muted truncate">{u.email}</div>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="space-y-1.5">
          <label className="block text-xs font-medium text-muted">Role</label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setRole("MEMBER")}
              className={`rounded-2xl border p-3 text-left transition-all cursor-pointer ${
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
              className={`rounded-2xl border p-3 text-left transition-all cursor-pointer ${
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
