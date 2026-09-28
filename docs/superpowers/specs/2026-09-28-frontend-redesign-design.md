# Design Spec: Frontend Redesign - Notion & Shadcn Minimalist Style

## 1. Executive Summary
This document specifies the architectural redesign of the **Fwork** frontend application. The goal is to replace the current Material Design 3 (24px large rounded cards, soft purple) styling with a **Clean Minimalist (Notion / Shadcn UI Style)** visual identity, while retaining 100% of existing backend REST API contracts, authentication flows, and real-time WebSocket state management.

---

## 2. Design System & Tokens (Notion / Shadcn UI Aesthetic)

### 2.1 Color Palette
The color system relies on crisp, high-contrast monochrome bases (Zinc/Slate family) paired with subtle status accents.

```css
/* Notion / Shadcn UI Light Mode Tokens */
:root {
  --bg-app: #FAFAFA;          /* Subtle off-white page background */
  --bg-surface: #FFFFFF;      /* Primary card & sidebar surface */
  --bg-surface-hover: #F4F4F5;/* Muted hover state (Zinc-100) */
  --bg-subtle: #F4F4F5;       /* Secondary element background */
  --border-line: #E4E4E7;     /* Crisp 1px border (Zinc-200) */
  --border-line-dark: #D4D4D8;/* Strong border on active/focus */

  --text-primary: #09090B;    /* High contrast black (Zinc-950) */
  --text-secondary: #71717A;  /* Muted text (Zinc-500) */
  --text-faint: #A1A1AA;      /* Helper text & icons (Zinc-400) */

  --brand-primary: #18181B;   /* Charcoal primary action (Zinc-900) */
  --brand-primary-fg: #FAFAFA;/* Primary button text */
  --brand-accent: #6366F1;    /* Subtle Indigo highlight */

  --priority-low: #0EA5E9;    /* Sky Blue */
  --priority-medium: #F59E0B; /* Amber */
  --priority-high: #F97316;   /* Orange */
  --priority-urgent: #EF4444; /* Red */
}

/* Shadcn Dark Mode Tokens */
.dark {
  --bg-app: #09090B;          /* Deep zinc background */
  --bg-surface: #18181B;      /* Card & surface (Zinc-900) */
  --bg-surface-hover: #27272A;/* Hover surface (Zinc-800) */
  --bg-subtle: #27272A;       /* Subtle dark surface */
  --border-line: #27272A;     /* Dark crisp border (Zinc-800) */
  --border-line-dark: #3F3F46;/* Active border (Zinc-700) */

  --text-primary: #FAFAFA;    /* Pure off-white text (Zinc-50) */
  --text-secondary: #A1A1AA;  /* Secondary text (Zinc-400) */
  --text-faint: #71717A;      /* Helper text (Zinc-500) */

  --brand-primary: #FAFAFA;   /* White primary action */
  --brand-primary-fg: #18181B;/* Dark button text */
  --brand-accent: #818CF8;    /* Indigo glow */
}
```

### 2.2 Radius & Geometry Hierarchy
Replacing `24px` oversized rounding with precise, modern geometry:
* **`radius-sm`**: `6px` (Badges, Tags, Tooltips)
* **`radius-md`**: `8px` (Buttons, Inputs, Dropdown items)
* **`radius-lg`**: `12px` (Cards, Kanban columns, Modals)
* **`radius-full`**: `999px` (User Avatars, Status dots)

### 2.3 Typography & Elevation
* **Font Family**: `Inter`, `-apple-system`, `BlinkMacSystemFont`, `Segoe UI`, `sans-serif`.
* **Headings**: `tracking-tight` (-0.02em font-weight: 600/700).
* **Borders & Shadows**: Minimalist 1px solid borders combined with `shadow-sm` (`0 1px 2px 0 rgba(0,0,0,0.05)`). No bulky 3D drop shadows.

---

## 3. Layout & Component Architecture

### 3.1 Layout (`AppLayout.jsx`, `Sidebar.jsx`, `Topbar.jsx`)
* **Collapsible Sidebar**:
  * Workspace Selector with dropdown menu.
  * Clean vertical navigation with active indicator bar.
  * Collapsible section headers (*Main Menu*, *Workspaces*, *Projects*).
  * Counter badges with subtle muted backgrounds (`bg-zinc-100 dark:bg-zinc-800`).
* **Topbar**:
  * Dynamic breadcrumb trail (`Workspace / Project / Board`).
  * Command Menu trigger input (`Search or press Ctrl+K`).
  * Quick Add (`+ New Task`) primary button.
  * Notifications dropdown trigger & Theme toggle (Sun/Moon).

### 3.2 Kanban Board (`BoardPage.jsx`, `KanbanColumn.jsx`, `KanbanCard.jsx`)
* **Column Headers**: Clean status dot, task count pill, inline column action menu (`...`).
* **Task Cards**:
  * 1px crisp border card (`border-zinc-200 dark:border-zinc-800`).
  * Title with line-clamp truncation.
  * Priority indicator badge (Low/Med/High/Urgent) in Notion tag style.
  * Subtask completion progress bar (`3/5 subtasks`).
  * Avatar stack for assignees.
  * Smooth `@dnd-kit` drag overlay with 4px lift shadow (`shadow-md scale-[1.02]`).

### 3.3 Dashboard (`Dashboard.jsx`)
* **KPI Metrics**: 4-grid stat summary (Total Tasks, In Progress, Completed, Overdue) with percentage delta trends.
* **Activity Feed**: Timeline layout with timestamp, user avatar, and action badge.
* **Task Overview**: Quick filterable list (Due Today, Assigned to Me).

### 3.4 UI Component Primitives (`src/components/ui/`)
All UI primitives will follow Shadcn UI patterns with Tailwind v4:
* `Button`: Variants (`default`, `secondary`, `outline`, `ghost`, `destructive`, `link`), Sizes (`sm`, `md`, `lg`, `icon`).
* `Input` & `Textarea`: Bordered inputs with focus ring (`focus-visible:ring-2 focus-visible:ring-zinc-950`).
* `Badge`: Clean pill tag with outline or soft tint variants.
* `Modal` / `Dialog`: Fixed backdrop with `backdrop-blur-sm`, smooth scale animation via Framer Motion.
* `Avatar`: Circle image with fallback initials avatar.

---

## 4. API & Integration Preservation
* All REST endpoints (`/api/tasks`, `/api/auth`, `/api/users`, `/api/workspaces`) remain untouched.
* All WebSocket / STOMP hooks (`useTaskSocket`, `sockjs-client`) remain intact.
* React Router v7 routes (`/dashboard`, `/board/:id`, `/my-tasks`, `/settings`, `/login`) maintain exact route paths.

---

## 5. Verification & Testing Strategy
1. **Compilation Check**: `npm run build` inside `frontend/` must complete without ESLint or Vite errors.
2. **Visual Check**: Dev server `npm run dev` to verify dark/light mode switching, responsive layout, and UI alignment.
3. **Interactive Check**: Drag-and-drop task movement, modal open/close, command menu search (`Ctrl+K`).
