import { createContext, useContext, useState } from "react";

const translations = {
  en: {
    // Navigation / General
    "Dashboard": "Dashboard",
    "My Tasks": "My Tasks",
    "Calendar": "Calendar",
    "Team": "Team",
    "Settings": "Settings",
    "Help & search": "Help & search",
    "Log out": "Log out",
    "Boards": "Boards",
    "New board": "New board",
    "Plan with AI": "Plan with AI",
    "Menu": "Menu",
    "General": "General",
    "No boards yet": "No boards yet",
    "Search tasks, boards…": "Search tasks, boards…",
    "Notifications": "Notifications",
    "No notifications": "No notifications",
    "Mark all read": "Mark all read",
    
    // Settings
    "Profile": "Profile",
    "ProfileDesc": "How you appear across your workspace.",
    "Edit": "Edit",
    "Name": "Name",
    "Choose Preset Avatar": "Choose Preset Avatar",
    "Custom Avatar URL": "Custom Avatar URL",
    "Cancel": "Cancel",
    "Save": "Save",
    "Image too large (max 1MB)": "Image too large (max 1MB)",
    "Failed to read image file": "Failed to read image file",
    "Profile updated successfully": "Profile updated successfully",
    "Failed to update profile": "Failed to update profile",
    "Notification deleted": "Notification deleted",
    "Failed to delete notification": "Failed to delete notification",
    "Delete notification": "Delete notification",
    "Workspace": "Workspace",
    "WorkspaceDesc": "Your activity at a glance.",
    "Preferences": "Preferences",
    "PreferencesDesc": "Saved to this browser.",
    "ReduceMotion": "Reduce motion",
    "ReduceMotionDesc": "Minimize animations and transitions across the app.",
    "DarkTheme": "Dark theme",
    "DarkThemeDesc": "Switch between light and dark visual interfaces.",
    "CommandMenu": "Command menu",
    "CommandMenuDesc": "Jump anywhere, search boards, create tasks.",
    "Language": "Language",
    "LanguageDesc": "Choose your preferred language.",
    "About": "About",
    "AboutDesc": "AI-powered Kanban · Light theme",
    "Account": "Account",
    "AccountDesc": "Manage your session.",
    "Sign out": "Sign out",
    "People": "People",
    "Tasks": "Tasks",

    // Dashboard
    "Workspace Overview": "Workspace Overview",
    "Welcome back": "Welcome back, {name} 👋",
    "Total boards": "Total boards",
    "Total tasks": "Total tasks",
    "Owned by you": "Owned by you",
    "Shared with you": "Shared with you",
    "Across your workspace": "Across your workspace",
    "avg per board": "{avg} avg per board",
    "of workspace": "{pct}% of workspace",
    "From your teammates": "From your teammates",
    "Board analytics": "Board analytics",
    "Board analytics desc": "Tasks across your busiest boards",
    "Breakdown": "Breakdown",
    "Composition": "Composition",
    "Owned vs shared": "Owned vs shared",
    "Jump back in": "Jump back in",
    "Jump back in desc": "Pick up where you left off",
    "Let AI plan your sprint": "Let AI plan your sprint",
    "Let AI plan desc": "Spin up a board and turn a one-line goal into a prioritized backlog in seconds.",
    "Create your first board": "Create your first board",
    "Create board desc": "Spin up a board and let AI generate your first set of tasks from a simple goal.",
    "All boards": "All boards",
    "No description": "No description",
    "Updated": "Updated",
    "Delete board": "Delete board",
    "Shared": "Shared",
    "Active boards": "Active boards",
    "Busiest board": "Busiest board",
    "Avg per board": "Avg per board",

    // My Tasks
    "Everything assigned to you": "Everything assigned to you",
    "Assigned to you": "Assigned to you",
    "Overdue": "Overdue",
    "Due this week": "Due this week",
    "Completed": "Completed",
    "Search your tasks": "Search your tasks",
    "All priorities": "All priorities",
    "Clear": "Clear",
    "No tasks assigned": "No tasks assigned to you",
    "Tasks you’re assigned": "Tasks you’re assigned across all your boards will show up here.",
    "No matching tasks": "No matching tasks",
    "Clear filters": "Try clearing your filters.",

    // Calendar
    "Tasks by due date": "Tasks by due date",
    "due": "{count} due",
    "Today": "Today",
    "Previous month": "Previous month",
    "Next month": "Next month",
    "Dot color = priority": "Dot color = priority",

    // Team
    "People in": "People in {name}",
    "People across boards": "People across your boards",
    "Invite Teammate": "Invite Teammate",
    "person": "person",
    "people": "people",
    "owner": "owner",
    "owners": "owners",
    "Search people": "Search people",
    "No teammates yet": "No teammates yet",
    "Invite desc": "Invite people to your workspace and they’ll appear here.",
    "Invite Member": "Invite Member",
    "On boards": "On {count} {boards}",
    "Remove member?": "Remove member?",
    "Are you sure you want to remove this member from the workspace?": "Are you sure you want to remove this member from the workspace?",
    "Leave workspace?": "Leave workspace?",
    "Are you sure you want to leave this workspace?": "Are you sure you want to leave this workspace?",
    "Remove": "Remove",
    "Leave": "Leave",
    "Member removed": "Member removed successfully",
    "You left the workspace": "You left the workspace successfully",
    "Failed to remove member": "Failed to remove member",
    "Failed to leave workspace": "Failed to leave workspace",

    // Board
    "Viewing": "Viewing",
    "Activity": "Activity",
    "Members": "Members",
    "Summary": "Summary",
    "AI tasks": "AI tasks",
    "Search tasks": "Search tasks",
    "All assignees": "All assignees",
    "Add column": "Add column",
    "Add task": "Add task"
  },
  vi: {
    // Navigation / General
    "Dashboard": "Bảng điều khiển",
    "My Tasks": "Nhiệm vụ của tôi",
    "Calendar": "Lịch",
    "Team": "Thành viên",
    "Settings": "Cài đặt",
    "Help & search": "Trợ giúp & Tìm kiếm",
    "Log out": "Đăng xuất",
    "Boards": "Bảng công việc",
    "New board": "Bảng mới",
    "Plan with AI": "Lập kế hoạch bằng AI",
    "Menu": "Danh mục",
    "General": "Chung",
    "No boards yet": "Chưa có bảng nào",
    "Search tasks, boards…": "Tìm kiếm thẻ, bảng…",
    "Notifications": "Thông báo",
    "No notifications": "Không có thông báo nào",
    "Mark all read": "Đánh dấu tất cả đã đọc",

    // Settings
    "Profile": "Thông tin cá nhân",
    "ProfileDesc": "Cách bạn xuất hiện trên không gian làm việc.",
    "Edit": "Chỉnh sửa",
    "Name": "Họ và tên",
    "Choose Preset Avatar": "Chọn ảnh đại diện mẫu",
    "Custom Avatar URL": "Đường dẫn ảnh tùy chọn",
    "Cancel": "Hủy",
    "Save": "Lưu",
    "Image too large (max 1MB)": "Ảnh quá lớn (tối đa 1MB)",
    "Failed to read image file": "Đọc file ảnh thất bại",
    "Profile updated successfully": "Cập nhật hồ sơ thành công",
    "Failed to update profile": "Cập nhật hồ sơ thất bại",
    "Notification deleted": "Đã xóa thông báo",
    "Failed to delete notification": "Xóa thông báo thất bại",
    "Delete notification": "Xóa thông báo",
    "Workspace": "Không gian làm việc",
    "WorkspaceDesc": "Tổng quan nhanh về hoạt động của bạn.",
    "Preferences": "Tùy chọn hiển thị",
    "PreferencesDesc": "Được lưu trên trình duyệt này.",
    "ReduceMotion": "Giảm chuyển động",
    "ReduceMotionDesc": "Giảm thiểu hiệu ứng chuyển động và chuyển cảnh.",
    "DarkTheme": "Giao diện tối",
    "DarkThemeDesc": "Chuyển đổi giữa giao diện sáng và tối.",
    "CommandMenu": "Menu lệnh nhanh",
    "CommandMenuDesc": "Đi nhanh mọi nơi, tìm kiếm bảng, tạo công việc.",
    "Language": "Ngôn ngữ",
    "LanguageDesc": "Chọn ngôn ngữ hiển thị của bạn.",
    "About": "Thông tin Fwork",
    "AboutDesc": "Bảng Kanban tích hợp AI · Giao diện Tím",
    "Account": "Tài khoản",
    "AccountDesc": "Quản lý phiên đăng nhập của bạn.",
    "Sign out": "Đăng xuất tài khoản",
    "People": "Thành viên",
    "Tasks": "Nhiệm vụ",

    // Dashboard
    "Workspace Overview": "Tổng quan không gian",
    "Welcome back": "Chào mừng trở lại, {name} 👋",
    "Total boards": "Tổng số bảng",
    "Total tasks": "Tổng số thẻ",
    "Owned by you": "Bảng của bạn",
    "Shared with you": "Bảng được chia sẻ",
    "Across your workspace": "Trên toàn bộ không gian làm việc",
    "avg per board": "Trung bình {avg} thẻ/bảng",
    "of workspace": "Chiếm {pct}% không gian",
    "From your teammates": "Được chia sẻ bởi đồng nghiệp",
    "Board analytics": "Phân tích bảng công việc",
    "Board analytics desc": "Thẻ công việc trên các bảng hoạt động nhiều nhất",
    "Breakdown": "Phân rã chi tiết",
    "Composition": "Cơ cấu không gian",
    "Owned vs shared": "Bảng sở hữu vs chia sẻ",
    "Jump back in": "Tiếp tục công việc",
    "Jump back in desc": "Quay lại bảng bạn vừa làm việc gần đây",
    "Let AI plan your sprint": "Lập kế hoạch sprint bằng AI",
    "Let AI plan desc": "Tạo một bảng mới và biến mục tiêu một dòng thành danh sách thẻ công việc sau vài giây.",
    "Create your first board": "Tạo bảng công việc đầu tiên",
    "Create board desc": "Bắt đầu với một bảng mới và để AI đề xuất các nhiệm vụ đầu tiên từ mục tiêu của bạn.",
    "All boards": "Tất cả bảng công việc",
    "No description": "Chưa có mô tả",
    "Updated": "Cập nhật",
    "Delete board": "Xóa bảng",
    "Shared": "Được chia sẻ",
    "Active boards": "Bảng đang hoạt động",
    "Busiest board": "Bảng bận rộn nhất",
    "Avg per board": "Trung bình mỗi bảng",

    // My Tasks
    "Everything assigned to you": "Tất cả công việc được giao cho bạn",
    "Assigned to you": "Công việc được giao",
    "Overdue": "Đã quá hạn",
    "Due this week": "Hạn tuần này",
    "Completed": "Đã hoàn thành",
    "Search your tasks": "Tìm nhiệm vụ của bạn",
    "All priorities": "Mọi độ ưu tiên",
    "Clear": "Xóa bộ lọc",
    "No tasks assigned": "Không có nhiệm vụ nào được giao",
    "Tasks you’re assigned": "Các thẻ công việc bạn được giao trên các bảng sẽ xuất hiện ở đây.",
    "No matching tasks": "Không tìm thấy nhiệm vụ phù hợp",
    "Clear filters": "Hãy thử xóa bớt các bộ lọc đang chọn.",

    // Calendar
    "Tasks by due date": "Nhiệm vụ theo ngày hạn",
    "due": "{count} nhiệm vụ hạn",
    "Today": "Hôm nay",
    "Previous month": "Tháng trước",
    "Next month": "Tháng sau",
    "Dot color = priority": "Màu chấm = Độ ưu tiên",

    // Team
    "People in": "Thành viên của {name}",
    "People across boards": "Thành viên trên các bảng",
    "Invite Teammate": "Mời đồng nghiệp",
    "person": "người",
    "people": "người",
    "owner": "Chủ sở hữu",
    "owners": "Chủ sở hữu",
    "Search people": "Tìm kiếm thành viên",
    "No teammates yet": "Chưa có thành viên nào",
    "Invite desc": "Mời mọi người tham gia không gian làm việc để cùng cộng tác.",
    "Invite Member": "Mời thành viên",
    "On boards": "Tham gia {count} {boards}",
    "Remove member?": "Xóa thành viên?",
    "Are you sure you want to remove this member from the workspace?": "Bạn có chắc chắn muốn xóa thành viên này khỏi không gian làm việc?",
    "Leave workspace?": "Rời không gian làm việc?",
    "Are you sure you want to leave this workspace?": "Bạn có chắc chắn muốn rời khỏi không gian làm việc này?",
    "Remove": "Xóa",
    "Leave": "Rời khỏi",
    "Member removed": "Đã xóa thành viên thành công",
    "You left the workspace": "Bạn đã rời không gian làm việc thành công",
    "Failed to remove member": "Xóa thành viên thất bại",
    "Failed to leave workspace": "Rời không gian làm việc thất bại",

    // Board
    "Viewing": "Đang xem",
    "Activity": "Hoạt động",
    "Members": "Thành viên",
    "Summary": "Tóm tắt AI",
    "AI tasks": "Gợi ý AI",
    "Search tasks": "Tìm thẻ công việc",
    "All assignees": "Mọi người thực hiện",
    "Add column": "Thêm cột",
    "Add task": "Tạo thẻ mới"
  }
};

const LanguageContext = createContext(null);

export const LanguageProvider = ({ children }) => {
  const [lang, setLangState] = useState(() => {
    return localStorage.getItem("pref-lang") || "en";
  });

  const setLang = (next) => {
    localStorage.setItem("pref-lang", next);
    setLangState(next);
  };

  const t = (key, params = {}) => {
    let raw = translations[lang]?.[key] || translations["en"]?.[key] || key;
    
    // Simple interpolation for params like {name}, {count}, etc.
    Object.keys(params).forEach((k) => {
      raw = raw.replace(`{${k}}`, params[k]);
    });
    
    return raw;
  };

  return (
    <LanguageContext.Provider value={{ lang, setLang, t }}>
      {children}
    </LanguageContext.Provider>
  );
};

export const useLanguage = () => {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error("useLanguage must be used within a LanguageProvider");
  }
  return context;
};
