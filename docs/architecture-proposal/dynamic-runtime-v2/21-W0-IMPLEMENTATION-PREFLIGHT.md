# 21 — V2 Implementation W0 Preflight (Authorization Received)

**State: IMPLEMENTATION AUTHORIZED by owner on 2026-10-16; W0 PARTIALLY VERIFIED; W1 NOT STARTED.**
**Authority:** frozen plan 20, detailed file manifest 17 as overridden by 19/20, direct owner instruction; existing core/frozen decisions remain in force unless explicitly superseded. No Kotlin source edited in this preflight.

## Verified remotely
- GitHub branch `architecture/dynamic-runtime-v2-discussion-freeze` exists, at commit `6f3f90b5e8cca713eacfad9ae37e27f2fc5f5a77` at beginning of W0.
- Reviewed `AI_START_HERE.md`, `docs/project-management/{04-PROJECT-MASTER-STATE,03-DECISION-LOG,07-DEVELOPMENT-WORKFLOW}.md`, relevant `docs/dynamic/DYNAMIC-ARCHITECTURE.md`, frozen plan 20.
- Read actual W1 source: `network/http/{RequestHandler,HttpClientBuilder}.kt`, `network/{ApiBaseUrl,NetworkModule}.kt`, `network/ApiBaseUrlTest.kt`.
- Existing `RequestHandler` is an active shared Ktor implementation with metadata headers, bearer provider, request methods and typed error mapping. It is NOT a throwaway placeholder. Reuse/focus implementation instead of introducing parallel transport.
- Existing `HttpClientBuilder` builds one client via Koin and already has ContentNegotiation; preserve current Android/Web/iOS assumptions, validate URLs/auth at shared boundary.
- Existing `ApiBaseUrl` validates HTTP(S) origin, host and path/query/fragment exclusion. Preserve behavior and tests.
- Existing `RequestHandler` accepts caller-provided headers and authentication string; W1 tests should confirm untrusted caller headers cannot override auth/approved metadata and ensure URL resolution/timeout/cancellation.
- Historical `AI_START_HERE` and Project Master State still say `feature/dynamic-ui` is current and contain specific previously frozen Dynamic work. The **owner's newer V2 authorization** selects the frozen V2 branch/plan for this work, but the authoritative state needs an explicit reconciliation update when implementation proceeds. Do not overwrite other active work blindly.

## Safety checks NOT established
1. No access to the owner's local `D:\CarBroz\CB-Partner` Git working tree; `git status`, working branch, dirty/untracked files, local HEAD and diff have NOT been observed.
2. No Gradle/KMP compile/tests in this turn. The GitHub connector exposes source, not a validated local Gradle executor; local container cannot reach GitHub through Git.
3. No actual Android/Web/iOS runtime test. CI freshness also not independently verified.
4. No project source changes; remote docs-only commit changes branch HEAD after preflight.

## Exact local Windows preflight (non-destructive)
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git branch --show-current
git rev-parse HEAD
git diff --stat
git fetch origin --prune
git switch architecture/dynamic-runtime-v2-discussion-freeze
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
git status
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:dynamic:jvmTest
```
Do not switch branches if `git status --short` reports user changes; preserve them and first determine a safe approach. No reset/force push/branch deletion.

## W1 first code slice after confirmed clean baseline
1. Keep one Ktor `HttpClient` and the current `RequestHandler` as baseline. Evolve typed request transport without duplicate client.
2. Harden reserved header/auth and relative-origin validation in `RequestHandler.kt` with focused MockEngine tests in existing `network/src/commonTest/.../http/RequestHandlerTest.kt`.
3. Distinguish cancellation, HTTP failure, serialization and transport errors without leaking response bodies; preserve metadata behavior.
4. Run `:network:jvmTest :data:jvmTest` (then Android/Web compile as required). Review full diff; proceed to W2 only after W1 passes.

**W0 is not marked complete until local branch/working-tree, build and original-contract impact are verified.** The frozen plan remains unchanged and implementation authorization remains valid; only safety preflight is pending.
