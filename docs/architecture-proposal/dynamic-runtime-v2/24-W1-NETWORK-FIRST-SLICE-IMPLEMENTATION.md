# 24 — W1: First Verified-Source Network Change

**State: SOURCE CHANGE COMMITTED; REGRESSION TESTS ADDED; POST-CHANGE EXECUTION PENDING.** This is NOT W1 completion, and not a runtime proof.

## Authority and branch
- Plan 20, W0 source audit 22, addenda 23.
- Branch **only** `architecture/dynamic-runtime-v2-discussion-freeze`; no other branch.
- Owner supplied pre-change clean checkout and successful `:network:jvmTest :data:jvmTest :feature:dynamic:jvmTest`.

## Evidence → change
- The existing shared `RequestHandler.performRequest` inserts transport-owned metadata headers, applies arbitrary caller headers, and inserts optional bearer Authorization. There was no explicit guard against caller supplying case-insensitive reserved names.
- Retained one `RequestHandler`, one Ktor `HttpClient` and existing error mapping.
- Implemented a single `isTransportOwnedHeader` predicate protecting `Authorization`, `X-CarBroz-Platform`, `X-CarBroz-App-Version`, `X-CarBroz-Build-Number`; rejects conflicting caller header map **before preparing/executing the request**, with a safe error message that does not echo user data.
- Added focused class KDoc explaining the protected-header/cancellation contract.
- Added MockEngine tests for case-insensitive conflicting keys and successful ordinary `X-Request-Id` forwarding. Tests are committed but were **not run by the remote GitHub editing tool**.

## Exact files changed
1. `network/src/commonMain/kotlin/com/carbroz/cbpartner/network/http/RequestHandler.kt` — targeted input validation and KDoc.
2. `network/src/commonTest/kotlin/com/carbroz/cbpartner/network/http/RequestHandlerTest.kt` — two focused regression tests.

## Required local verification
```powershell
cd D:\CarBroz\CB-Partner
git status --short
git fetch origin --prune
git switch architecture/dynamic-runtime-v2-discussion-freeze
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :network:jvmTest :data:jvmTest :feature:dynamic:jvmTest
git diff origin/feature/dynamic-ui...HEAD --check
```

If tests fail, inspect failure and correct on the **same branch**; do not move to W2. W1 remains open: endpoint validation, bearer credential wiring, timeout and HTTP error-security assessments require independent source/caller-level evidence and tests. Do not invent token storage or external deployment URLs.

## Logging, KDoc, environments
Addenda 23 are frozen but have **not yet been implemented** beyond the first RequestHandler KDoc. Existing no-raw-HTTP-logging remains intact. Android development/staging/production build environments and iOS/Web deployment configuration are planned for platform integration wave with tests and fail-closed staging/production, not silently introduced in this narrow network change.
