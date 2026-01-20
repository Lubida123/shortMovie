---
name: frontend-code-auditor
description: Review/audit frontend codebases (especially Vue 3 + Vite + Pinia + Element Plus/Tailwind) for correctness, broken imports/components, API contract mismatches, auth/token issues, routing/guard bugs, performance pitfalls (N+1, over-fetching, caching), and UX regressions. Use when a user asks to "审查前端代码", "检查有没有逻辑错误", "确保基本功能不变", "排查黑屏/滑动/播放异常", or when integrating/changing backend APIs and needing a structured, prioritized code review report with actionable fixes.
---

# Frontend Code Auditor

## Overview

Produce a prioritized frontend code review report and optionally apply focused fixes with minimal regressions.
Optimized for Vue SFCs (`.vue`) and Vite projects, but works for most web frontends.

## Workflow (Fast → Deep)

### 0) Establish Context (2–3 mins)
- Identify stack: Vue/React, router, state, UI lib, build tool.
- Read project entrypoints: `package.json`, `vite.config.*`, `src/main.*`, `src/router/*`, `src/store/*`.
- Check repo instructions: `AGENTS.md` and any local coding conventions.

### 1) Quick Health Checks (optional, but recommended)
- Run the lowest-cost checks first:
  - `pnpm -v` / `npm -v`
  - `pnpm lint` / `npm run lint` (if present)
  - `pnpm typecheck` (if present)
  - `pnpm build` (only if needed to reproduce)

### 2) Static Scan (find common breakages fast)
Prefer running the bundled script:
- PowerShell: `powershell -ExecutionPolicy Bypass -File scripts/scan_frontend.ps1 -Root <projectRoot>`

If scripts cannot run, use these manual `rg` queries from `references/checklist.md`.

### 3) Deep Review (targeted to the reported issue)
Use `references/checklist.md` sections based on the task:
- API integration & DTO mapping
- Auth/token/guards
- Video playback: wheel switching, fullscreen, progress/seek, caching
- UI/UX regressions and layout/scroll conflicts

### 4) Output Report (always)
Use `references/report-template.md` to produce:
- **Findings** (High/Medium/Low) with file:line references
- **Root cause** and **fix plan**
- **Safe fixes** applied (if requested) and how to validate

## Guardrails (Avoid Regressions)
- Keep behavioral changes minimal; prefer fixing root cause without changing UX flows unless requested.
- Don’t introduce new dependencies unless explicitly needed.
- When touching event handlers (wheel/scroll/touch), add clear guards to avoid conflicts (e.g. comment panel scrolling vs switching).
- For IDs, prefer nullish coalescing (`??`) over `||` when `0` can be valid.
- Don’t cache signed URLs (URLs containing `?`) unless you also implement refresh-on-error with retry limit.

## Resources

### scripts/
- `scripts/scan_frontend.ps1`: quick ripgrep-based scan that prints a markdown-ish report to stdout.

### references/
- `references/checklist.md`: manual query list + review checklist.
- `references/report-template.md`: standard report format for consistent, actionable reviews.
