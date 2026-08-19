import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { LogOut, Command, Zap, FolderKanban, CheckSquare, Users, Edit2, Camera } from "lucide-react";
import toast from "react-hot-toast";
import { useAuth } from "../context/AuthContext";
import { useLayout } from "../components/layout/AppLayout";
import { useWorkspace } from "../hooks/useWorkspace";
import { userApi } from "../lib/api";
import Topbar from "../components/layout/Topbar";
import Button from "../components/ui/Button";
import Avatar from "../components/ui/Avatar";
import { cn } from "../lib/utils";
import { useLanguage } from "../context/LanguageContext";

const Switch = ({ checked, onChange }) => (
  <button
    type="button"
    role="switch"
    aria-checked={checked}
    onClick={() => onChange(!checked)}
    className={cn(
      "relative h-6 w-11 shrink-0 rounded-full transition-colors duration-200",
      checked ? "bg-brand-500" : "bg-elevated"
    )}
  >
    <span
      className={cn(
        "absolute top-0.5 h-5 w-5 rounded-full bg-white shadow-[var(--shadow-card)] transition-all duration-200",
        checked ? "left-[22px]" : "left-[2px]"
      )}
    />
  </button>
);

const Card = ({ title, description, children }) => (
  <section className="rounded-3xl border border-line bg-surface p-6 shadow-[var(--shadow-card)]">
    <h3 className="font-display text-sm font-semibold tracking-tight">{title}</h3>
    {description && <p className="mt-1 text-xs text-muted">{description}</p>}
    <div className="mt-5">{children}</div>
  </section>
);

const Settings = () => {
  const { user, logout, updateUser } = useAuth();
  const { openCreateBoard } = useLayout();
  const { boards, tasks, members } = useWorkspace();
  const navigate = useNavigate();
  const { lang, setLang, t } = useLanguage();

  const [reduceMotion, setReduceMotion] = useState(
    () => localStorage.getItem("pref-reduced-motion") === "true"
  );
  const [darkMode, setDarkMode] = useState(
    () => localStorage.getItem("pref-theme") === "dark"
  );

  // Profile editing states
  const [isEditing, setIsEditing] = useState(false);
  const [name, setName] = useState(user?.name || "");
  const [avatar, setAvatar] = useState(user?.avatar || "");
  const [saving, setSaving] = useState(false);

  // Preset avatars from Dicebear adventurer style
  const presets = [
    "https://api.dicebear.com/7.x/adventurer/svg?seed=Felix",
    "https://api.dicebear.com/7.x/adventurer/svg?seed=Max",
    "https://api.dicebear.com/7.x/adventurer/svg?seed=Nala",
    "https://api.dicebear.com/7.x/adventurer/svg?seed=Harley",
    "https://api.dicebear.com/7.x/adventurer/svg?seed=Kiki",
    "https://api.dicebear.com/7.x/adventurer/svg?seed=Bella"
  ];

  useEffect(() => {
    setName(user?.name || "");
    setAvatar(user?.avatar || "");
  }, [user]);

  useEffect(() => {
    document.documentElement.dataset.reduceMotion = reduceMotion ? "true" : "false";
    localStorage.setItem("pref-reduced-motion", String(reduceMotion));
  }, [reduceMotion]);

  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add("dark");
      localStorage.setItem("pref-theme", "dark");
    } else {
      document.documentElement.classList.remove("dark");
      localStorage.setItem("pref-theme", "light");
    }
  }, [darkMode]);

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.size > 1024 * 1024) {
      toast.error(t("Image too large (max 1MB)"));
      return;
    }

    const reader = new FileReader();
    reader.onloadend = () => {
      setAvatar(reader.result);
    };
    reader.onerror = () => {
      toast.error(t("Failed to read image file"));
    };
    reader.readAsDataURL(file);
  };

  const handleSaveProfile = async () => {
    if (!name.trim()) {
      toast.error(t("Name cannot be blank"));
      return;
    }
    setSaving(true);
    try {
      const updated = await userApi.updateProfile({ name, avatar });
      updateUser(updated);
      setIsEditing(false);
      toast.success(t("Profile updated successfully"));
    } catch (err) {
      toast.error(err.message || t("Failed to update profile"));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <Topbar title={t("Settings")} subtitle={lang === "vi" ? "Hồ sơ và tùy chọn hiển thị" : "Profile and preferences"} onCreateBoard={openCreateBoard} />

      <div className="flex-1 overflow-y-auto">
        <div className="mx-auto max-w-3xl space-y-5 px-6 py-8 md:px-8">
          {/* Profile */}
          <Card title={t("Profile")} description={t("ProfileDesc")}>
            {!isEditing ? (
              <div className="flex items-center gap-4">
                <Avatar name={user?.name} id={user?.id} src={user?.avatar_url || user?.avatar} size="lg" className="h-16 w-16 text-lg" />
                <div className="min-w-0 flex-1">
                  <p className="font-display text-lg font-semibold tracking-tight">{user?.name}</p>
                  <p className="truncate text-sm text-muted">{user?.email}</p>
                </div>
                <Button variant="outline" size="sm" onClick={() => setIsEditing(true)}>
                  <Edit2 className="h-3.5 w-3.5 mr-1" /> {t("Edit")}
                </Button>
              </div>
            ) : (
              <div className="space-y-4 animate-in">
                <div className="flex flex-col sm:flex-row items-center gap-6 pb-4 border-b border-line">
                  <div className="relative group shrink-0">
                    <Avatar name={name} id={user?.id} src={avatar} size="lg" className="h-20 w-20 text-xl" />
                    <label htmlFor="edit-avatar-upload" className="absolute inset-0 flex items-center justify-center bg-black/40 rounded-full opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer text-white">
                      <Camera className="h-5 w-5" />
                    </label>
                    <input type="file" id="edit-avatar-upload" accept="image/*" onChange={handleFileUpload} className="hidden" />
                  </div>

                  <div className="flex-1 w-full space-y-3">
                    <div>
                      <label className="text-[11px] font-semibold text-muted uppercase tracking-wider">{t("Name")}</label>
                      <input
                        type="text"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        className="h-10 w-full mt-1.5 rounded-lg border border-line bg-surface px-3 text-xs text-ink outline-none transition-all focus:border-brand-500 focus:ring-2 focus:ring-brand-500/15"
                      />
                    </div>
                    <div>
                      <label className="text-[11px] font-semibold text-muted uppercase tracking-wider">{t("Custom Avatar URL")}</label>
                      <input
                        type="text"
                        value={avatar?.startsWith("data:") ? "" : avatar}
                        placeholder="https://example.com/avatar.png"
                        onChange={(e) => setAvatar(e.target.value)}
                        className="h-10 w-full mt-1.5 rounded-lg border border-line bg-surface px-3 text-xs text-ink outline-none transition-all focus:border-brand-500 focus:ring-2 focus:ring-brand-500/15"
                      />
                    </div>
                  </div>
                </div>

                <div>
                  <label className="text-[11px] font-semibold text-muted uppercase tracking-wider block mb-2">{t("Choose Preset Avatar")}</label>
                  <div className="grid grid-cols-6 gap-2">
                    {presets.map((url, idx) => (
                      <button
                        key={idx}
                        type="button"
                        onClick={() => setAvatar(url)}
                        className={cn(
                          "p-1 rounded-2xl border-2 transition-all cursor-pointer hover:scale-105 flex items-center justify-center",
                          avatar === url ? "border-brand-500 bg-brand-50/10" : "border-transparent bg-surface-2"
                        )}
                      >
                        <Avatar name={`preset-${idx}`} src={url} size="md" className="h-10 w-10 pointer-events-none" />
                      </button>
                    ))}
                  </div>
                </div>

                <div className="flex justify-end gap-2 pt-2">
                  <Button variant="ghost" size="sm" onClick={() => setIsEditing(false)} disabled={saving}>
                    {t("Cancel")}
                  </Button>
                  <Button variant="primary" size="sm" onClick={handleSaveProfile} loading={saving}>
                    {t("Save")}
                  </Button>
                </div>
              </div>
            )}
          </Card>

          {/* Workspace */}
          <Card title={t("Workspace")} description={t("WorkspaceDesc")}>
            <div className="grid grid-cols-3 gap-3">
              <Metric icon={FolderKanban} label={t("Boards")} value={boards.length} tint="var(--color-brand-600)" />
              <Metric icon={CheckSquare} label={t("Tasks")} value={tasks.length} tint="var(--color-priority-low)" />
              <Metric icon={Users} label={t("People")} value={members.length} tint="var(--color-brand-500)" />
            </div>
          </Card>

          {/* Preferences */}
          <Card title={t("Preferences")} description={t("PreferencesDesc")}>
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-sm font-medium text-ink">{t("ReduceMotion")}</p>
                <p className="mt-0.5 text-xs text-muted">{t("ReduceMotionDesc")}</p>
              </div>
              <Switch checked={reduceMotion} onChange={setReduceMotion} />
            </div>
            <div className="mt-5 flex items-center justify-between gap-4 border-t pt-5">
              <div>
                <p className="text-sm font-medium text-ink">{t("DarkTheme")}</p>
                <p className="mt-0.5 text-xs text-muted">{t("DarkThemeDesc")}</p>
              </div>
              <Switch checked={darkMode} onChange={setDarkMode} />
            </div>
            <div className="mt-5 flex items-center justify-between gap-4 border-t pt-5">
              <div>
                <p className="text-sm font-medium text-ink">{t("CommandMenu")}</p>
                <p className="mt-0.5 text-xs text-muted">{t("CommandMenuDesc")}</p>
              </div>
              <kbd className="flex items-center gap-0.5 rounded-md bg-surface-2 px-2 py-1 text-[11px] font-semibold text-muted">
                <Command className="h-3 w-3" />K
              </kbd>
            </div>
            <div className="mt-5 flex items-center justify-between gap-4 border-t pt-5">
              <div>
                <p className="text-sm font-medium text-ink">{t("Language")}</p>
                <p className="mt-0.5 text-xs text-muted">{t("LanguageDesc")}</p>
              </div>
              <select
                value={lang}
                onChange={(e) => setLang(e.target.value)}
                className="h-9 cursor-pointer rounded-lg border border-line bg-surface px-3 text-xs font-semibold text-ink shadow-[var(--shadow-card)] outline-none transition-all duration-200 hover:border-brand-300 focus:border-brand-500/50"
              >
                <option value="en">English</option>
                <option value="vi">Tiếng Việt</option>
              </select>
            </div>
          </Card>

          {/* About */}
          <Card title={t("About")}>
            <div className="flex items-center gap-3">
              <div className="brand-gradient flex h-10 w-10 items-center justify-center rounded-md shadow-[var(--shadow-brand)]">
                <Zap className="h-5 w-5 fill-white text-white" />
              </div>
              <div>
                <p className="text-sm font-semibold text-ink">Fwork</p>
                <p className="text-xs text-muted">{t("AboutDesc")}</p>
              </div>
            </div>
          </Card>

          {/* Account */}
          <Card title={t("Account")} description={t("AccountDesc")}>
            <Button variant="danger" onClick={() => { logout(); navigate("/login"); }}>
              <LogOut className="h-4 w-4" /> {t("Sign out")}
            </Button>
          </Card>
        </div>
      </div>
    </>
  );
};

const Metric = ({ icon: Icon, label, value, tint }) => (
  <div className="rounded-2xl bg-surface-2/60 p-4">
    <div className="mb-3 flex h-9 w-9 items-center justify-center rounded-md" style={{ backgroundColor: `color-mix(in oklab, ${tint} 10%, transparent)`, color: tint }}>
      <Icon className="h-4 w-4" />
    </div>
    <p className="font-display text-2xl font-semibold tracking-tight tabular text-ink">{value}</p>
    <p className="mt-0.5 text-xs text-muted">{label}</p>
  </div>
);

export default Settings;
