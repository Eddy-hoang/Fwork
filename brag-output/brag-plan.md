# Brag Plan: Fwork

## What is this app?
Fwork is an enterprise real-time Kanban collaboration platform and workspace built with Spring Boot 4.1 + Java 21 and React 19, paired with a Chrome Extension Companion and Gemini AI to turn messy work into instant team velocity.

## The angle
**"Manual Chaos vs. Instant Automation."**
Most teams waste hours juggling 50 open tabs, manual copy-pasting into spreadsheets, and arguing over out-of-sync backlogs. Fwork eliminates that friction with three instantaneous automation vectors: one-click browser capture, AI task breakdown, and sub-10ms real-time Kanban synchronization.

## Hook (first 3.8 seconds)
A stark, high-contrast visual statement: a chaotic blur of spreadsheet cells, sticky notes, and open tab overload with a sharp typographic contrast:
*"Still copy-pasting tasks across 47 browser tabs?"*
Instantly shattered by a sleek, floating violet glassmorphic card:
*"Work shouldn't feel like wrestling with your tools."*

## Key moments (the middle)
- **Workflow 1: Fwork Companion Extension (1-Click Web Capture)**: The browser extension captures the current page, extracts title and URL, and auto-dispatches it to the team board with zero tab switching.
- **Workflow 2: Gemini 2.0 AI Sprint Breakdown**: A single vague prompt (*"Refactor auth & build OAuth 2.0 flow"*) expands in 1.2 seconds into a structured checklist with priorities, estimate, and assignees.
- **Workflow 3: Sub-10ms Zero-Refresh Real-Time Kanban**: Smooth drag-and-drop powered by `@dnd-kit` and WebSocket STOMP broadcasting instant card movement across multiple user avatars simultaneously.

## Outro / punchline
The Fwork emblem resolves into crisp focus on a soft lavender floating canvas:
*"Zero polling. Zero manual chaos. 100% velocity."*
Final lockup: **Fwork — Enterprise Real-time Collaboration Platform** (`fwork-web.vercel.app`).

## User flow worth showing
1. **Entry**: Browser extension popup captures live web context in 1 click.
2. **Key Action**: Gemini AI automatically structures and categorizes the backlog task.
3. **Result**: Real-time collaborative Kanban board updates across all team members in <10ms without refreshing.

## Tone
- **Preset**: `polished`
- **Creative direction**: High-end productivity product film with razor-sharp problem/solution tension.
- **Interpretation**: Confident and restrained. Restrained typography with generous tracking, soft layered elevation shadows, smooth Spring easing (`cubic-bezier(0.16, 1, 0.3, 1)`), zero aggressive cuts, allowing every screen and stat to settle and be fully digested.

## Format: landscape — 1920x1080
## Duration: 22.0 seconds

## Visual identity (from the project)
- Background: `#f7f6fb` (Soft lavender-tinted foundational surface)
- Surface: `#ffffff` (Crisp white floating card surface)
- Secondary Surface: `#f1eff8` (Tinted chromatic secondary)
- Text (Primary Ink): `#1a1726` (Deep obsidian-violet)
- Text (Muted): `#565169` (Balanced secondary text)
- Brand Primary Accent: `#6750a4` (MD3 purple primary)
- Brand Gradient: `linear-gradient(135deg, #6750a4 0%, #9373ee 100%)`
- Priority Urgency: `#e11d48` (Coral Rose) / Success: `#059669` (Mint Emerald)
- Display Font: `Plus Jakarta Sans`, sans-serif
- Body Font: `Inter`, sans-serif
- Strongest Visual Element: Fwork rounded floating cards (`border-radius: 24px`), dual layered violet shadows, and live Kanban columns.

## Share copy (draft)
Stop managing task clutter across 50 browser tabs. Fwork turns your team's chaos into instant real-time automation with sub-10ms Kanban and Gemini AI breakdown.

## Audio direction
- Role: Warm, elegant, high-restraint corporate-chill music bed.
- Music: `happy-beats-business-moves-vol-12-by-ende-dot-app.mp3`
- Music treatment: Smooth 0.8s fade-in from 0.0s, settled volume at -14dB, subtle dip under key feature reveals, and smooth final fade-out from 21.2s to 22.0s.
- Music cue guidance: Preset cue file read (`happy-beats-business-moves-vol-12-by-ende-dot-app.music-cues.md`). Key transition anchors: 3.82s (The Shift), 7.09s (Workflow 1), 10.93s (Workflow 2), 14.73s (Workflow 3), 18.56s (Outro lockup).
- Audio-reactive treatment: Subtle; background vignette and floating violet accent glow breathe softly with track RMS energy. No equalizer visualizers.
- SFX posture: **Disabled (`--no-sfx`)** as explicitly requested by user. Complete acoustic focus on music bed and visual rhythm.
- Audio-coupled moments: Card arrivals and metric counters smoothly timed to music beat grid windows (3.82s, 7.09s, 10.93s, 14.73s, 18.56s).
- Restraint rule: No harsh stings, no generic synth whooshes, no robotic voiceover.

---

## Storyboard

### Scene 1 — The Manual Chaos — 3.8s (0.00s – 3.82s)
- **Visuals**: Dark desaturated slate background (`#0f0d17`) representing disorganized workflow. Floating fragmented UI shards: cluttered spreadsheets, 40+ browser tab icons, overlapping red badge warnings ("37 Unassigned Tickets", "Merge Conflict", "Out of sync").
- **Headline**: *"Still copy-pasting tasks across 47 browser tabs?"* (settles at 0.6s, holds for full reading).
- **Sub-caption**: *"Manual tracking is slowing your team down."*
- **Sequential / Interaction**: The fragmented cards slightly shake and drift outward as a glowing violet laser line sweeps horizontally across the screen at 3.2s, clearing the chaos.
- **Audio intent**: Low, restrained music intro creating tension.
- **Transition mood**: Sharp wipe/slide-dissolve on strong beat `3.82s` → Scene 2.

### Scene 2 — The Shift: Fwork Platform Reveal — 3.3s (3.82s – 7.09s)
- **Visuals**: Clean transition into Fwork's signature soft lavender universe (`#f7f6fb`). An elegant floating enterprise shell appears with Spring easing (`cubic-bezier(0.16, 1, 0.3, 1)`).
- **Centerpiece**: Official Fwork emblem with gradient violet badge:
  - Header: *"Instant Automation for High-Velocity Teams"*
  - Subtitle: *"Enterprise Real-Time Kanban & Workspace Platform"*
- **Sequential / Interaction**: 3 key metric pills animate into view one by one along the beat grid (4.39s, 4.91s, 5.34s):
  1. `⚡ Sub-10ms STOMP Sync`
  2. `🤖 Gemini 2.0 AI Engine`
  3. `🧩 Manifest V3 Extension`
- **Audio intent**: Music opens up and kicks into groove with warm bassline.
- **Transition mood**: Confident fluid horizontal push at `7.09s` → Scene 3.

### Scene 3 — Workflow 1: 1-Click Browser Web Capture — 3.8s (7.09s – 10.93s)
- **Visuals**: Realistic split view. Left side: Simulated live web research (GitHub PR / Technical RFC). Right side: Floating Fwork Companion Extension card (`CaptureView.tsx`) with chrome elevation and violet glow.
- **Micro-interaction**:
  - URL & Page Title (`"RFC: Enterprise RBAC & Virtual Threads"`) auto-populate in 0.3s.
  - Target selector smoothly highlights: `Engineering Workspace` ➔ `Sprint 42 Board` ➔ `In Progress Column`.
  - Action button flashes: `[ + Add to Board ]` with instant green confirmation tag: `Dispatched in 42ms`.
- **Badge / Callout**: *"Fwork Companion Chrome Extension — Capture anything from any tab."*
- **Sequential / Interaction**: Cursor clicks `[ + Add to Board ]` at 9.29s (beat cue); card transforms into a flying task token.
- **Audio intent**: Smooth melodic groove supporting quick decisive action.
- **Transition mood**: Seamless camera zoom into board canvas at `10.93s` → Scene 4.

### Scene 4 — Workflow 2: Gemini AI Sprint Breakdown — 3.8s (10.93s – 14.73s)
- **Visuals**: Close-up of Fwork's Task Detail Modal (`TaskModal.jsx`) featuring the `[ ✨ AI Breakdown ]` action button.
- **Micro-interaction**:
  - Input field shows high-level epic: `"Build Enterprise Authentication Matrix"`.
  - Click `[ ✨ AI Breakdown ]` at 11.46s.
  - Gemini 2.0 Flash shimmer effect cascades across the modal:
    - `[✓] Spring Security 6 Stateless JWT Filter` (High Priority • Coral Tag)
    - `[✓] ChannelInterceptor WebSocket Handshake Auth` (Urgent Priority)
    - `[✓] RBAC Matrix: OWNER / ADMIN / MEMBER` (Medium Priority)
  - Execution counter displays: `Generated 4 subtasks & checklist in 1.2s`.
- **Headline**: *"AI Sprint Breakdown — From vague idea to structured backlog in seconds."*
- **Audio intent**: Music energy steady and refined, highlighting technological speed.
- **Transition mood**: Upward spatial transition at `14.73s` → Scene 5.

### Scene 5 — Workflow 3: Sub-10ms Zero-Refresh Kanban — 3.8s (14.73s – 18.56s)
- **Visuals**: Full widescreen interactive Kanban board view (`KanbanBoard.jsx`). Three columns: `Backlog`, `In Progress`, `Done`.
- **Micro-interaction**:
  - Multiple live user avatars (`Eddy H.`, `Sarah K.`, `Alex M.`) hovering over cards with real-time presence indicators.
  - Card `"Deploy OAuth 2.0 Flow"` is smoothly lifted with `@dnd-kit` elevation physics and dropped into `Done`.
  - Column counter instantly updates (`Done: 14 -> 15`) and status turns emerald green (`#059669`).
  - STOMP WebSocket status beacon pulses: `Synced across 8 clients (latency 4ms)`.
- **Headline**: *"Sub-10ms Collaborative Sync — Zero polling. Zero page refresh."*
- **Audio intent**: Swell leading up to the final crest of the music track.
- **Transition mood**: Slow cinematic zoom out into brand lockup at strong beat `18.56s` → Scene 6.

### Scene 6 — Outro / Resolution: 100% Velocity & Launch CTA — 3.44s (18.56s – 22.00s)
- **Visuals**: Premium brand finale on soft lavender gradient.
- **Centerpiece**:
  - High-resolution Fwork Logo (`assets/logo.png`) centered with soft violet aura.
  - Tagline: *"Work at the speed of thought."*
  - Contrast Resolution:
    - ~~47 Open Tabs & Manual Backlogs~~  ➔  **Instant Real-Time Velocity**
- **Action Links & Ecosystem**:
  - Web App: `fwork-web.vercel.app`
  - Chrome Extension: `Manifest V3`
  - Backend Engine: `Spring Boot 4.1 + Java 21`
- **Audio intent**: Music resolves into final chord and gracefully fades into silence at 22.0s.

---

## Music mood for this video: Upbeat, refined corporate-chill (109.96 BPM)
## Audio summary: A polished, elegant corporate-chill groove that carries the transition from stressful fragmented chaos to serene, lightning-fast automated team productivity, with zero harsh SFX clutter.
