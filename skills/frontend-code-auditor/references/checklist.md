## Frontend Audit Checklist (Vue/Vite Focus)

### Quick Grep Pass (fast signal)

Run inside the frontend project root:

- Broken imports / Vite errors:
  - `rg -n \"Failed to resolve import|Failed to resolve component|Duplicate attribute\" src`
- Debug leftovers:
  - `rg -n \"console\\.(log|debug|warn)|debugger;\" src`
- TODO/FIXME debt:
  - `rg -n \"TODO|FIXME|HACK\" src`
- Auth/token/header:
  - `rg -n \"Authorization|Bearer|localStorage|sessionStorage\" src`
- Routing/guards:
  - `rg -n \"beforeEach|requiresAuth|router\\.push\\(|router\\.replace\\(\" src`
- API paths:
  - `rg -n \"http\\.(get|post|put|delete)\\(\" src/api`
  - `rg -n \"'/api/\" src/api`
- Video/playback hot spots:
  - `rg -n \"@wheel|deltaY|requestFullscreen|exitFullscreen|timeupdate|loadedmetadata|@error\" src`

### Vue SFC Structure
- Ensure `<script setup>` is used consistently and imports exist.
- Check for duplicated attributes in templates (common after copy/paste).
- Ensure refs arrays (`videoRefs[index]`) are kept in sync with list changes.

### API Contract (Spring Boot patterns)
- `@RequestParam` endpoints: use `URLSearchParams` or `params`, not JSON body.
- Pagination wrappers: prefer `data.data.records` and guard against nulls.
- Auth-required endpoints: confirm `Authorization: Bearer <token>` and 401/403 UX.

### Playback & Gesture
- Wheel switching:
  - Guard against scroll conflict (e.g. comment panel open → ignore wheel switching).
  - Throttle/switch lock to prevent multi-skip.
- Progress/seek:
  - Avoid flooding `currentTime` updates while dragging.
  - If supported, use `fastSeek`.
- Caching:
  - Do not cache signed URLs (contains `?`) unless refresh-on-error exists with retry limit.
- Error handling:
  - Bind to `<video @error>` and provide 1 auto-retry + manual retry.

### Performance
- Avoid N+1 detail fetches for lists; prefer backend filtering or batch APIs.
- Use `preload='metadata'` for non-active videos; only active video `auto`.
- Do not rerender heavy lists unnecessarily; keep reactive dependencies minimal.

### UX/A11y
- Visible focus state for keyboard navigation.
- Buttons must have obvious hover/active states and `cursor: pointer`.
- Ensure fixed containers don’t cause scroll bleed (100vh + overflow hidden patterns).

