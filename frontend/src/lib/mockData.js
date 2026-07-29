// ----------------------------------------------------------------------------
// Mock data layer for the BOILERPLATE UI.
//
// This file fakes everything the backend would normally return, so the whole
// app is fully clickable before any API exists. Once the real backend is ready,
// you can delete this file along with the MOCK block in `api.js`.
// ----------------------------------------------------------------------------

const DAY = 86400000;
const iso = (offsetDays = 0) => new Date(Date.now() + offsetDays * DAY).toISOString();
const uid = (prefix) => `${prefix}-${Math.random().toString(36).slice(2, 9)}`;

const COLUMN_TITLES = ["Todo", "In Progress", "Review", "Done"];
const PRIORITIES = ["low", "medium", "high", "urgent"];

/* ----------------------------------- users ----------------------------------- */
export const currentUser = {
  id: "u-alex",
  name: "Alex Rivera",
  email: "alex@timetoprogram.com",
  avatar_url: null,
  created_at: iso(-60),
};

const team = [
  currentUser,
  { id: "u-maya", name: "Maya Chen", email: "maya@timetoprogram.com", avatar_url: null },
  { id: "u-diego", name: "Diego Santos", email: "diego@timetoprogram.com", avatar_url: null },
  { id: "u-priya", name: "Priya Nair", email: "priya@timetoprogram.com", avatar_url: null },
  { id: "u-sam", name: "Sam Okafor", email: "sam@timetoprogram.com", avatar_url: null },
  { id: "u-lena", name: "Lena Fischer", email: "lena@timetoprogram.com", avatar_url: null },
];
const byId = Object.fromEntries(team.map((u) => [u.id, u]));

/* ---------------------------------- boards ----------------------------------- */
const boardMetas = [
  { id: "b-roadmap", title: "Product Roadmap", description: "Quarterly planning, OKRs and feature prioritization.", color: "#2f8159", owner: "u-alex", members: ["u-maya", "u-diego", "u-priya"], updatedDaysAgo: 0.3,
    titles: ["Define Q3 OKRs", "Prioritize backlog", "User interview synthesis", "Pricing experiment", "Competitor analysis", "Roadmap review", "Define success metrics", "Beta feedback triage", "Planning deck", "Stakeholder alignment"] },
  { id: "b-mobile", title: "Mobile App Launch", description: "Ship the iOS & Android apps to the stores.", color: "#c26a45", owner: "u-alex", members: ["u-sam", "u-lena"], updatedDaysAgo: 1.2,
    titles: ["App store listing", "Push notifications", "Crash reporting", "Onboarding screens", "TestFlight beta", "Performance profiling", "Deep linking", "Release checklist"] },
  { id: "b-website", title: "Website Redesign", description: "Marketing site refresh with a new design language.", color: "#5f7da6", owner: "u-alex", members: ["u-lena", "u-diego"], updatedDaysAgo: 2.6,
    titles: ["Wireframe homepage", "Design hero section", "Responsive nav", "Migrate blog content", "SEO audit", "Accessibility pass", "Lighthouse optimization"] },
  { id: "b-marketing", title: "Marketing Q3", description: "Campaigns, content and growth experiments.", color: "#d4a23c", owner: "u-alex", members: ["u-diego", "u-priya"], updatedDaysAgo: 4,
    titles: ["Launch email sequence", "Social calendar", "Webinar planning", "Case study writeup", "Ad creative refresh"] },
  { id: "b-design", title: "Design System", description: "Tokens, components and documentation.", color: "#2c9c8f", owner: "u-alex", members: ["u-lena", "u-maya"], updatedDaysAgo: 0.8,
    titles: ["Token naming audit", "Button component", "Form primitives", "Theme tokens", "Icon set cleanup", "Documentation site"] },
  // shared with Alex (owned by teammates)
  { id: "b-eng", title: "Engineering Sprint", description: "Current two-week delivery sprint.", color: "#6f9b54", owner: "u-maya", members: ["u-alex", "u-sam", "u-diego"], updatedDaysAgo: 1.8,
    titles: ["Refactor auth module", "Fix websocket reconnect", "Add rate limiting", "Database index tuning", "API pagination", "Integration tests", "Upgrade Node 22", "Cache layer"] },
  { id: "b-content", title: "Content Calendar", description: "Editorial pipeline for blog & newsletter.", color: "#4f9d82", owner: "u-diego", members: ["u-alex", "u-priya"], updatedDaysAgo: 9,
    titles: ["Blog: AI in PM", "Newsletter #14", "Customer spotlight"] },
];

const COL_CYCLE = [0, 1, 1, 2, 3, 0, 2, 3, 1, 3];
const DUE_CYCLE = [-9, 2, null, 5, -2, 14, 1, null, 20, -4, 6, 9, 3, null, 12];

const attachAssignee = (task, user) => ({
  ...task,
  assignee_id: user ? user.id : null,
  assignee_name: user ? user.name : null,
  assignee_email: user ? user.email : null,
  assignee_avatar: user ? user.avatar_url : null,
});

const buildDetail = (meta) => {
  const ownerId = byId[meta.owner].id;
  let memberKeys = [meta.owner, ...meta.members];
  if (!memberKeys.includes("u-alex")) memberKeys.push("u-alex");
  memberKeys = [...new Set(memberKeys)];

  const members = memberKeys.map((k, i) => ({
    ...byId[k],
    role: k === meta.owner ? "owner" : i === 1 ? "admin" : "member",
    joined_at: iso(-50),
  }));

  const columns = COLUMN_TITLES.map((title, i) => ({
    id: `${meta.id}-col-${i}`,
    board_id: meta.id,
    title,
    position: (i + 1) * 1000,
    created_at: iso(-45),
  }));

  const assignPool = ["u-alex", "u-alex", ...memberKeys];
  const tasks = meta.titles.map((title, i) => {
    const col = columns[COL_CYCLE[i % COL_CYCLE.length]];
    const off = DUE_CYCLE[i % DUE_CYCLE.length];
    const assigneeKey = i % 5 === 4 ? null : assignPool[i % assignPool.length];
    return attachAssignee(
      {
        id: `${meta.id}-t-${i}`,
        board_id: meta.id,
        column_id: col.id,
        title,
        description: i % 3 === 0 ? `${title} — details and acceptance criteria.` : null,
        priority: PRIORITIES[(i + meta.title.length) % PRIORITIES.length],
        due_date: off == null ? null : iso(off),
        position: (i + 1) * 1000,
        created_by: ownerId,
        created_at: iso(-20),
        updated_at: iso(-2),
      },
      assigneeKey ? byId[assigneeKey] : null
    );
  });

  const board = {
    id: meta.id,
    title: meta.title,
    description: meta.description,
    color: meta.color,
    owner_id: ownerId,
    created_at: iso(-45),
    updated_at: iso(-meta.updatedDaysAgo),
  };

  return { board, columns, tasks, members, role: meta.owner === "u-alex" ? "owner" : "member" };
};

// Pre-build every board's detail and keep it in a map (so created boards persist
// for the session and useWorkspace can fetch each one).
const details = {};
boardMetas.forEach((m) => {
  details[m.id] = buildDetail(m);
});

/* ------------------------------- public API ---------------------------------- */
export const boards = boardMetas
  .map((m) => {
    const d = details[m.id];
    return {
      ...d.board,
      is_owner: m.owner === "u-alex",
      task_count: d.tasks.length,
      member_count: d.members.length,
    };
  })
  .sort((a, b) => new Date(b.updated_at) - new Date(a.updated_at));

export const getBoardDetail = (id) => {
  if (details[id]) return structuredClone(details[id]);
  // unknown id (e.g. a board just created this session) → empty board
  const columns = COLUMN_TITLES.map((title, i) => ({
    id: `${id}-col-${i}`,
    board_id: id,
    title,
    position: (i + 1) * 1000,
    created_at: iso(),
  }));
  return {
    board: { id, title: "Untitled board", description: null, color: "#2f8159", owner_id: currentUser.id, created_at: iso(), updated_at: iso() },
    columns,
    tasks: [],
    members: [{ ...currentUser, role: "owner", joined_at: iso() }],
    role: "owner",
  };
};

export const createBoard = (data) => {
  const board = {
    id: uid("b"),
    title: data.title,
    description: data.description || null,
    color: data.color || "#2f8159",
    owner_id: currentUser.id,
    created_at: iso(),
    updated_at: iso(),
    is_owner: true,
    task_count: 0,
    member_count: 1,
  };
  details[board.id] = {
    board,
    columns: COLUMN_TITLES.map((title, i) => ({ id: `${board.id}-col-${i}`, board_id: board.id, title, position: (i + 1) * 1000, created_at: iso() })),
    tasks: [],
    members: [{ ...currentUser, role: "owner", joined_at: iso() }],
    role: "owner",
  };
  return board;
};

export const updateBoard = (id, data) => {
  const d = details[id];
  const board = { ...(d ? d.board : {}), id, ...data, updated_at: iso() };
  if (d) d.board = board;
  return board;
};

export const createTask = (boardId, data) =>
  attachAssignee(
    {
      id: uid("t"),
      board_id: boardId,
      column_id: data.column_id,
      title: data.title,
      description: data.description || null,
      priority: data.priority || "medium",
      due_date: data.due_date || null,
      position: Date.now(),
      created_by: currentUser.id,
      created_at: iso(),
      updated_at: iso(),
    },
    data.assignee_id ? byId[data.assignee_id] || null : null
  );

export const updateTask = (boardId, taskId, data) =>
  attachAssignee(
    { id: taskId, board_id: boardId, ...data, updated_at: iso() },
    data.assignee_id ? byId[data.assignee_id] || null : null
  );

export const createColumn = (boardId, data) => ({
  id: uid("col"),
  board_id: boardId,
  title: data.title,
  position: Date.now(),
  created_at: iso(),
});

export const addMember = (data) => {
  const existing = team.find((u) => u.email.toLowerCase() === (data.email || "").toLowerCase());
  const user = existing || { id: uid("u"), name: (data.email || "guest").split("@")[0], email: data.email, avatar_url: null };
  return { ...user, role: data.role === "admin" ? "admin" : "member" };
};

export const searchUsers = (q) => {
  const term = (q || "").toLowerCase();
  return team.filter((u) => u.id !== currentUser.id && (u.name.toLowerCase().includes(term) || u.email.toLowerCase().includes(term)));
};

export const activitiesFor = (boardId) => {
  const d = details[boardId] || {};
  const names = (d.members || [currentUser]).map((m) => m.name);
  const samples = [
    { action: "task.created", message: `${names[0] || "Alex"} created "${d.tasks?.[0]?.title || "a task"}"` },
    { action: "task.moved", message: `${names[1] || "Maya"} moved "${d.tasks?.[1]?.title || "a task"}" to Done` },
    { action: "member.added", message: `${names[0] || "Alex"} added ${names[2] || "Diego"} to the board` },
    { action: "task.updated", message: `${names[1] || "Maya"} updated a task's priority` },
  ];
  return samples.map((a, i) => ({
    id: uid("act"),
    board_id: boardId,
    user_id: (d.members?.[i % (d.members?.length || 1)] || currentUser).id,
    user_name: names[i % names.length] || "Alex",
    user_avatar: null,
    action: a.action,
    message: a.message,
    created_at: iso(-(i + 1) * 0.25),
  }));
};

/* ------------------------------------ AI ------------------------------------- */
const GENERATED = [
  { title: "Design cart & checkout UI", description: "Wireframe and build the multi-step checkout.", priority: "high" },
  { title: "Integrate Stripe payments", description: "Add Stripe Elements and handle webhooks.", priority: "urgent" },
  { title: "Build order confirmation emails", description: "Transactional email on successful purchase.", priority: "medium" },
  { title: "Add address validation", description: "Validate shipping addresses at entry.", priority: "low" },
  { title: "Cart persistence", description: "Persist the cart across sessions.", priority: "medium" },
  { title: "Apply discount codes", description: "Support promo codes at checkout.", priority: "high" },
  { title: "Order history page", description: "Let users review past orders.", priority: "low" },
  { title: "Inventory checks", description: "Block checkout for out-of-stock items.", priority: "high" },
];

export const aiGenerate = (boardId, { count = 6, column_id } = {}) => {
  const tasks = GENERATED.slice(0, count).map((t, i) =>
    attachAssignee(
      {
        id: uid("t"),
        board_id: boardId,
        column_id: column_id || `${boardId}-col-0`,
        title: t.title,
        description: t.description,
        priority: t.priority,
        due_date: null,
        position: Date.now() + i,
        created_by: currentUser.id,
        created_at: iso(),
        updated_at: iso(),
      },
      null
    )
  );
  return { tasks };
};

export const aiBreakdown = () => [
  { title: "Define data model", description: "Outline the schema and relationships.", priority: "high" },
  { title: "Build the API endpoint", description: "Implement the route and validation.", priority: "medium" },
  { title: "Wire up the UI", description: "Connect the frontend to the new endpoint.", priority: "medium" },
  { title: "Write tests", description: "Cover the happy path and edge cases.", priority: "low" },
];

export const aiSummary = () => ({
  headline: "Solid momentum this sprint — most core work is in progress, with a few risks to watch on the payments track.",
  completed: ["Onboarding screens shipped", "SEO audit completed", "Design tokens finalized"],
  inProgress: ["Stripe payment integration", "Responsive navigation", "Beta feedback triage"],
  risks: ["Payments work is blocked on API keys", "Two urgent tasks have no assignee"],
  recommendations: ["Assign owners to the urgent items", "Time-box the payments spike to 2 days"],
});
