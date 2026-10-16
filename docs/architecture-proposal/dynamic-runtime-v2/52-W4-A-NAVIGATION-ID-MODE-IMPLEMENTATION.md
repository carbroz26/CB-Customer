# 52 — W4-A Entry ID and Strict Navigation Mode: Implementation

**Status:** PARTIAL W4 IMPLEMENTATION; LOCAL TESTS PENDING. Frozen owner permission granted 2026-10-16. Branch `architecture/dynamic-runtime-v2-discussion-freeze`.

## Implemented narrow slice
- NavigationState allocates a monotonically increasing session-local visit number for every entry it creates (root/push/replace/present/setRoot). Previous entries preserve their entryId through pop. Standalone entry construction remains supported for existing unit tests and caller compatibility; session identity is allocated exclusively by NavigationState.
- NavigateActionHandler defaults **only an absent** mode to Push; explicit Push, Replace, Pop and Back are supported. Unknown, explicit null or wrong-typed mode fails with IllegalArgumentException instead of silently turning into Push. Pop/Back accepts no destination; Push and Replace require it.
- Existing top-level action-type Back alias in DynamicActionExecutor is deliberately untouched, per frozen compatibility decision.
- Tests: NavigationState identity assertions and focused NavigateActionHandlerTest.
- No Web/Android/iOS runtime test claimed and no new infrastructure introduced.

## Next outstanding frozen W4 work
- **P0:** Replace per-entry effect channel and collector with one session-owned batch sink/consumer; prove remaining effects after navigation survive disposing source entry and preserve state-before-effects.
- **P1:** actual Nav3 back-stack membership vs Compose composition and input binding retention, safe errors/outcomes for rejected transitions.
- **P1:** verify explicit entryId propagation and whether standalone Entry default is suitable for final contract. Avoid speculative process restoration.

## Run owner local validation first
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git merge --ff-only origin/architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :navigation:jvmTest :feature:dynamic:jvmTest
git diff --check
```

Do not mark W4 complete until remaining gates and Web compilation tests pass.
