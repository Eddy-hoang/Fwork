# Frontend Redesign (Notion & Shadcn UI Style) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the Fwork React frontend from Material Design 3 (24px large rounded purple surfaces) to a Clean Minimalist (Notion / Shadcn UI) aesthetic with crisp 1px borders, subtle rounding, high-contrast typography, and responsive layouts.

**Architecture:** We will update the CSS token engine in `src/index.css` first, then modernize the core UI primitives in `src/components/ui/`, refactor the primary navigation shell (`AppLayout`, `Sidebar`, `Topbar`), update feature pages (Kanban Board, Dashboard, Auth), and verify complete build and API compatibility.

**Tech Stack:** React 19, Vite, Tailwind CSS v4, Framer Motion, Lucide React, @dnd-kit.

**Spec:** [2026-09-28-frontend-redesign-design.md](file:///d:/Fwork/docs/superpowers/specs/2026-09-28-frontend-redesign-design.md)

## Global Constraints
- Retain all existing REST API client calls, React Contexts, and WebSocket STOMP hooks verbatim.
- All modified code must build cleanly (`npm run build`) without ESLint or syntax errors.
- Ensure full dark mode and light mode support across all screens.

---

### Task 1: Design Tokens & CSS Engine Refactor

**Files:**
- Modify: `d:/Fwork/frontend/src/index.css`

**Interfaces:**
- Consumes: Tailwind v4 theme definitions
- Produces: CSS CSS variables `--bg-app`, `--bg-surface`, `--border-line`, `--text-primary`, `--brand-primary`, `--radius-sm`, `--radius-md`, `--radius-lg`

- [ ] **Step 1: Update `src/index.css` theme tokens**

Replace MD3 theme tokens with Shadcn/Notion color variables and precise border radiuses (`radius-sm: 6px`, `radius-md: 8px`, `radius-lg: 12px`):

```css
@import "tailwindcss";

@theme {
  --color-bg-app: #FAFAFA;
  --color-bg-surface: #FFFFFF;
  --color-bg-surface-hover: #F4F4F5;
  --color-border-line: #E4E4E7;
  --color-border-line-dark: #D4D4D8;

  --color-text-primary: #09090B;
  --color-text-secondary: #71717A;
  --color-text-faint: #A1A1AA;

  --color-brand-primary: #18181B;
  --color-brand-primary-fg: #FAFAFA;
  --color-brand-accent: #6366F1;

  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-full: 999px;

  --font-sans: "Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.dark {
  --color-bg-app: #09090B;
  --color-bg-surface: #18181B;
  --color-bg-surface-hover: #27272A;
  --color-border-line: #27272A;
  --color-border-line-dark: #3F3F46;

  --color-text-primary: #FAFAFA;
  --color-text-secondary: #A1A1AA;
  --color-text-faint: #71717A;

  --color-brand-primary: #FAFAFA;
  --color-brand-primary-fg: #18181B;
  --color-brand-accent: #818CF8;
}
```

- [ ] **Step 2: Verify CSS build**

Run: `npm --prefix frontend run build`
Expected: Successful Vite compilation.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/index.css
git commit -m "style: update frontend design tokens to Notion/Shadcn style"
```

---

### Task 2: Core UI Primitives Refactor

**Files:**
- Modify: `d:/Fwork/frontend/src/components/ui/Button.jsx`
- Modify: `d:/Fwork/frontend/src/components/ui/Input.jsx`
- Modify: `d:/Fwork/frontend/src/components/ui/Badge.jsx`
- Modify: `d:/Fwork/frontend/src/components/ui/Avatar.jsx`
- Modify: `d:/Fwork/frontend/src/components/ui/Modal.jsx`

**Interfaces:**
- Consumes: UI Props (`variant`, `size`, `className`, `children`)
- Produces: Standardized Shadcn-style UI primitives

- [ ] **Step 1: Refactor `Button.jsx`**

Apply Shadcn variant classes (`default`, `outline`, `ghost`, `destructive`, `secondary`), 8px radius (`rounded-md`), and tight font weights.

- [ ] **Step 2: Refactor `Input.jsx`**

Apply 1px border (`border-zinc-200 dark:border-zinc-800`), crisp focus rings (`focus-visible:ring-2 focus-visible:ring-zinc-950 dark:focus-visible:ring-zinc-300`), and rounded-md.

- [ ] **Step 3: Refactor `Badge.jsx` & `Avatar.jsx`**

Apply Notion tag outline styles for badges and crisp circular border rings for user avatars.

- [ ] **Step 4: Refactor `Modal.jsx`**

Add `backdrop-blur-sm` overlay, 12px rounded dialog surface (`rounded-xl`), and Framer Motion spring transition.

- [ ] **Step 5: Verify build & commit**

Run: `npm --prefix frontend run build`
Expected: PASS

```bash
git add frontend/src/components/ui/
git commit -m "feat(ui): refactor UI primitives to Shadcn style"
```

---

### Task 3: Layout & Navigation Shell Redesign

**Files:**
- Modify: `d:/Fwork/frontend/src/components/layout/AppLayout.jsx`
- Modify: `d:/Fwork/frontend/src/components/layout/Sidebar.jsx`
- Modify: `d:/Fwork/frontend/src/components/layout/Topbar.jsx`

**Interfaces:**
- Consumes: `useAuth`, `useTheme`, `useLocation`
- Produces: Minimalist SaaS layout wrapper

- [ ] **Step 1: Redesign `Sidebar.jsx`**

Build clean vertical Notion sidebar with workspace dropdown header, section labels, active item indicator (`bg-zinc-100 dark:bg-zinc-800/60 text-zinc-900 dark:text-zinc-50`), and task counter pills.

- [ ] **Step 2: Redesign `Topbar.jsx`**

Build crisp top navigation with breadcrumb route links, `Ctrl+K` search bar, primary `+ New Task` button, notifications popover, and theme switcher.

- [ ] **Step 3: Redesign `AppLayout.jsx`**

Update outer app container background (`bg-[#FAFAFA] dark:bg-[#09090B]`) and main content viewport padding.

- [ ] **Step 4: Verify build & commit**

Run: `npm --prefix frontend run build`
Expected: PASS

```bash
git add frontend/src/components/layout/
git commit -m "feat(layout): redesign AppLayout, Sidebar, and Topbar to Notion style"
```

---

### Task 4: Kanban Board & Card Component Redesign

**Files:**
- Modify: `d:/Fwork/frontend/src/pages/BoardPage.jsx`
- Modify: `d:/Fwork/frontend/src/components/board/KanbanColumn.jsx` (or inline column in BoardPage)
- Modify: `d:/Fwork/frontend/src/components/board/KanbanCard.jsx` (or inline card)

**Interfaces:**
- Consumes: Task data array, `@dnd-kit` drag-and-drop hooks, task status updates
- Produces: Minimalist Kanban view

- [ ] **Step 1: Refactor Kanban Columns**

Render column headers with status dots (e.g. Gray for Backlog, Blue for In Progress, Green for Done), item count pills, and column action menus.

- [ ] **Step 2: Refactor `KanbanCard.jsx`**

Update task card with 1px zinc border, title, priority badge pills (`Urgent`, `High`, `Medium`, `Low`), subtask completion bar, assignee avatar stack, and `@dnd-kit` shadow lift state (`shadow-md scale-[1.02]`).

- [ ] **Step 3: Verify build & commit**

Run: `npm --prefix frontend run build`
Expected: PASS

```bash
git add frontend/src/components/board/ frontend/src/pages/BoardPage.jsx
git commit -m "feat(board): redesign Kanban columns and task cards to Shadcn style"
```

---

### Task 5: Dashboard & Feature Pages Redesign

**Files:**
- Modify: `d:/Fwork/frontend/src/pages/Dashboard.jsx`
- Modify: `d:/Fwork/frontend/src/pages/MyTasks.jsx`
- Modify: `d:/Fwork/frontend/src/pages/Login.jsx`
- Modify: `d:/Fwork/frontend/src/pages/Register.jsx`

**Interfaces:**
- Consumes: User task lists, auth endpoints, project statistics
- Produces: Redesigned feature pages

- [ ] **Step 1: Redesign `Dashboard.jsx`**

Layout 4 clean KPI summary cards (Total, Pending, Completed, Overdue) with percentage badges, activity feed list with timeline dots, and filterable task list.

- [ ] **Step 2: Redesign Auth Pages (`Login.jsx`, `Register.jsx`)**

Center a Shadcn-style authentication card (`max-w-md border border-zinc-200 dark:border-zinc-800 rounded-xl p-6 shadow-sm`) with clean input fields and brand logo.

- [ ] **Step 3: Verify build & commit**

Run: `npm --prefix frontend run build`
Expected: PASS

```bash
git add frontend/src/pages/
git commit -m "feat(pages): redesign Dashboard, MyTasks, and Auth pages to Notion style"
```

---

### Task 6: Final Integration & Build Verification

**Files:**
- Test: Full Frontend project build

- [ ] **Step 1: Run ESLint**

Run: `npm --prefix frontend run lint`
Expected: 0 errors.

- [ ] **Step 2: Run Production Build**

Run: `npm --prefix frontend run build`
Expected: Successful dist output without warnings or errors.

- [ ] **Step 3: Final Commit**

```bash
git add .
git commit -m "chore: complete frontend redesign to Notion/Shadcn style"
```
