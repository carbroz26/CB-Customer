# 23 — V2 W0 Addenda FROZEN: KDoc, Safe Logging, Environments

**State: PLAN ADDENDA FROZEN / IMPLEMENTATION AUTHORIZED (owner's 2026-10-16 instruction); build/runtime not yet validated.** Supplements frozen plan 20. No extra branch or Gradle module.

## A. KDoc
- Public cross-module and nontrivial APIs get responsibility, ownership/lifetime, valid inputs, failure/cancellation contracts, and non-responsibilities where material. KDoc must match callers/source; no per-line narration, KDoc boilerplate or separate DocumentationManager.
- Document changed APIs in the same implementation wave; architecture docs record decisions, source KDoc describes actual contracts. For internal trivial helpers name clarity wins.
- Verify usage/callers and tests before adding or rewriting an abstraction. No guesses based on names.

## B. Safe diagnostics
- Reuse existing Kermit already in :core, once actual call paths and platform APIs are checked; do not add another logger, LoggerFacade without proven need, or restore raw transport loggers.
- **All environments prohibit** request/response bodies, phone, OTP, tokens, device ID, arbitrary query/URL and unvalidated server messages in logs. Redacted operational type, status code, safe error category, latency only. No sensitive values even in Debug/Staging.
- Development: debug and safe trace events; Staging: info and safe errors; Production: warning/error only. This is policy; logger configuration belongs at platform composition root/one shared policy, not feature-specific loggers. Safe no-op if logging cannot initialize; logger failures never change business results.
- Retain Decision 002 removing raw RequestHandler console logs. Cover no-sensitive-log behavior with focused tests.

## C. Deployment environments
- Exactly three environment identifiers: `development`, `staging`, `production`. Each uses the same business/UI/domain code and one shared Ktor transport.
- Android: apply a single `environment` flavor dimension with `development`, `staging`, `production` flavors in existing Android Application convention; keep existing `debug` / `release` build types independently (six named variants are Gradle's natural matrix, not six different architectures). Debuggable build type solely controls debugging; deployment env solely selects API URL and default safe-log policy.
- API configuration is injected from environment Gradle properties, not source constants. Development may default to current emulator `http://10.0.2.2:3000` for backward-compatible local runs; staging and production must have explicitly configured trustworthy API origins before a distributable build is accepted. Do not use `api.invalid` or localhost as production/staging fallback; fail packaging/distribution validation if missing. Debug production build follows production origin too.
- Different install identities: development suffix `.dev`, staging suffix `.staging`, production canonical `com.carbroz.cbpartner.android`. UI app labels remain existing unless brand-approved suffixes are available; signing/upload never hardcoded.
- Web: separate environment selection from bundling; development may retain current local :3000 behavior; staging/production require explicit trusted API base URL from existing meta tag or deployment-time configuration, validated by `ApiBaseUrl`. No new router or duplicate transport.
- iOS: Xcode configuration/scheme or build settings supplies exact same environment identifier and `CBPARTNER_API_BASE_URL`; development local default permitted, staging/production explicit origin. Do not pretend Android flavors compile for iOS.
- Shared contract: immutable `AppEnvironment` (enum or small value class, no Manager) plus `ApiBaseUrl` platform-provided to app composition. No secret embedded in source, staging/production endpoint values remain externally supplied.
- Environment-specific build labels/config tests are W7; W1 must not preemptively rewrite Android/Web/iOS entry code. Build recipe validation can occur after baseline compile tests. Release signing and published store metadata remain deployment operations, not speculative code-level abstractions.

## D. W1 first source gate
- Actual source test discovery first, then minimal targeted change to existing `RequestHandler.kt`: protect reserved system metadata/Authorization headers from caller-supplied overrides, preserving approved custom headers and bearer behavior.
- No second HttpClient, generic transport layer, token store or new module without independently demonstrated need. MockEngine tests check header conflict and custom header path; apply no token provider behavior change without secure provider contract.
- Source changes reviewed on `architecture/dynamic-runtime-v2-discussion-freeze` only.
- User's pre-change `:network:jvmTest :data:jvmTest :feature:dynamic:jvmTest` passed; each new change still requires *new* local test/build evidence before W1 completion.
