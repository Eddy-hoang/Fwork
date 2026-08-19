import { useState, useEffect, useRef } from "react";
import toast from "react-hot-toast";
import { UserPlus, X } from "lucide-react";
import Modal from "../ui/Modal";
import Button from "../ui/Button";
import { Input } from "../ui/Input";
import Avatar from "../ui/Avatar";
import { workspaceApi, userApi } from "../../lib/api";

const MembersModal = ({ open, onClose, workspaceId, members, setMembers, canManage, ownerId }) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [searching, setSearching] = useState(false);
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

  const invite = async (e) => {
    e.preventDefault();
    const targetEmail = searchQuery.trim();
    if (!targetEmail) return;

    if (!workspaceId) {
      toast.error("No active workspace ID provided");
      return;
    }

    setLoading(true);
    try {
      await workspaceApi.addMember(workspaceId, {
        email: targetEmail,
        role: "MEMBER"
      });

      // Fetch updated members list directly from the database
      const updatedMembers = await workspaceApi.getMembers(workspaceId);
      const mList = Array.isArray(updatedMembers) ? updatedMembers : (updatedMembers?.data || []);
      setMembers(mList);

      toast.success("Teammate added successfully");
      setSearchQuery("");
      setSelectedUser(null);
      setSuggestions([]);
    } catch (err) {
      toast.error(err.message || "Failed to add member");
    } finally {
      setLoading(false);
    }
  };

  const remove = async (userId) => {
    if (!workspaceId) return;
    try {
      await workspaceApi.removeMember(workspaceId, userId);
      setMembers((prev) => prev.filter((m) => (m.userId || m.id) !== userId));
      toast.success("Member removed");
    } catch (err) {
      toast.error(err.message);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title="Members" description="Invite teammates to collaborate in real time.">
      {canManage && (
        <form onSubmit={invite} className="mb-5 flex gap-2 items-end">
          <div className="relative flex-1" ref={containerRef}>
            <Input
              placeholder="Search by name or email..."
              type="text"
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                if (selectedUser && e.target.value !== selectedUser.email) {
                  setSelectedUser(null);
                }
              }}
            />
            {searching && (
              <div className="absolute right-3 top-3 text-[10px] text-faint animate-pulse">
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
          <Button type="submit" loading={loading} className="shrink-0 h-10 px-5">
            <UserPlus className="h-4 w-4" /> Invite
          </Button>
        </form>
      )}

      <ul className="space-y-1">
        {members.map((m) => (
          <li key={m.id} className="flex items-center gap-3 rounded-xl px-2 py-2 hover:bg-surface-2">
            <Avatar name={m.name} id={m.userId || m.id} src={m.avatar || m.avatar_url} size="sm" />
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium">{m.name}</p>
              <p className="truncate text-xs text-faint">{m.email}</p>
            </div>
            <span className="rounded-full bg-surface-2 px-2.5 py-1 text-[11px] font-medium capitalize text-muted">{m.role}</span>
            {canManage && (m.userId || m.id) !== ownerId && (
              <button onClick={() => remove(m.userId || m.id)} className="rounded-full p-1.5 text-faint transition-colors hover:bg-elevated hover:text-priority-urgent cursor-pointer">
                <X className="h-4 w-4" />
              </button>
            )}
          </li>
        ))}
      </ul>
    </Modal>
  );
};

export default MembersModal;
