# 159 — Copy/Paste New-Chat Starter Prompt: CB-Partner V2 / Login v19 / OTP v3 / Backend Integration

**Date:** 2026-10-25  
**Status:** Handoff prompt, not application code.  
**Use with:** `README.md`, `157` (frozen backend target), `158` (full current architecture/decisions), and all other files in `docs/architecture-proposal/dynamic-runtime-v2/`.

---

You are continuing a production Kotlin Multiplatform / Compose Multiplatform project called **CB-Partner**. You MUST first reconstruct the actual architecture and implementation status from the latest checked-in source and every approved Dynamic Runtime V2 document. DO NOT implement, refactor, rename, "optimize", delete code or alter JSON simply based on this prompt. Treat it as an accurate handoff and a mandatory audit checklist, not a replacement for code inspection.

## 1. Source of truth, repositories, branch

- Frontend: https://github.com/carbroz26/CB-Partner
- **Current working branch:** `architecture/dynamic-runtime-v2-discussion-freeze`. At handoff pre-document HEAD: `585ecd070e6981dc610b99fdbe1167e8de8ae2b4`; **check the live HEAD**, because handoff commits may advance it.
- Backend: https://github.com/carbroz26/CB-Backend, branch `development`, last checked `b67b36141aa6ae9d0488c2582b0114d2abb09c86`; confirm current source before decisions.
- Local Windows PowerShell project: `D:\CarBroz\CB-Partner`.
- Preserve untracked `CB-Partner-Live-Trace-V1.13.ps1` and local changes. Do not use force push, destructive reset/clean, unnecessary branch work or historical branch names as the active branch.
- **Only current source and `docs/architecture-proposal/dynamic-runtime-v2/` are planning authorities**, including later explicit owner decisions. Older unrelated docs are not alternative design authorities. Any mismatch must be reconciled and reported, not silently resolved.

## 2. Mandatory documentation audit — do not skim

Read **ALL files**, including all markdown documents and JSON contracts, in:

`docs/architecture-proposal/dynamic-runtime-v2/`

There were **179 files in the folder before Documents 158 and 159**; discover the actual latest list and read all files rather than trusting a count. Start with:

- `README.md` (now points to latest handoff);
- `157-LOGIN-OTP-BACKEND-FRONTEND-EXACT-CONTRACT-RECONCILIATION-FREEZE-2026-10-25.md` — **latest frozen target Backend/Frontend contract**, implementation not done;
- `158-2026-10-25-COMPLETE-CHAT-HANDOFF-ARCHITECTURE-RUNTIME-AND-INTEGRATION-GATES.md` — full verified/pending status and complete hierarchy/action flow;
- this `159` prompt;
- `01–20`: architecture, contracts, bootstrap, six-category hierarchy, bindings, state, action, navigation, testing and original freeze;
- `44–81`: all mapper/render categories, navigation, Request/Present/Dismiss/State/Sequence, resolver and effect-batching corrections;
- `82–111`: lifecycle, logging, tracing, platform and testing decisions;
- `112–150`: Login v19, Country Picker, validation, safe area, visuals and recoveries;
- `121–122` and `151–156`: OTP v1/v2/v3 progression, segmented input/keyboard, timer and Resend verification;
- every JSON version (historical ones clarify what changed; latest versions are the executable UI contract).

**Cross-check chronological supersession:** documents labelled "FROZEN", "COMPLETE" or "PENDING" from earlier days may have been superseded by later decisions. Explicitly flag such conflicts with the newer doc and current source; do NOT revive old decisions. Distinguish discussions, drafts, approved freezes, source-written, tests-run, device-verified, Backend-live-verified and unimplemented. Never assume the latest document implies successful code integration.

## 3. Mandatory entire source-code audit — line by line

Inspect **all frontend modules, their packages, classes and tests**, line-by-line in the latest branch:

- `app`: App, ApplicationContent, DynamicSession, LocalDynamicPreview, DI and session/effect ownership;
- `core`, `network`, `navigation`, `feature/splash`;
- `domain`: all Dynamic template/component/section/group/element/content/action/value/response/destination/validation models, repositories;
- `data`: bootstrap DTO/mapper/repository, Dynamic repository/remote, every DTO and mapper, PartnerCountryCatalog, environment/logging;
- `feature/dynamic`: DynamicModule, DynamicContainer, Store/ScreenState, LoadUseCase, Registry/registration, all renderers, FormFieldSection/ListSection, presentation host, InputElement segmented implementation, DynamicLayout, validator, resolver, runtime state/countdown, every action handler;
- `androidApp`, `webApp`, `iosApp` and platform APIs/build configurations; project module dependencies, tests.
- Then Backend `CB-Backend/development`: root compositions/config/identity/security policy, bootstrap route/config/GET, SduiModel, Action, SduiBuilder, Login/OTP screen composers, productionSduiService and controllers, Partner auth routes/DTO/controllers, AuthUseCases, ResponseHelper and error mapping; all relevant tests.

Determine *who calls what, why each class exists, where each responsibility belongs, what is already implemented/duplicated, and whether DTO → Domain → Mapper → Runtime → Renderer → Action flow matches approved JSON*. Do not rely on package/class filenames alone. If you cannot access a source file, say so; no hallucinated implementation or fabricated completed tests. Keep a concise findings ledger, not a long speculative rewrite.

## 4. Architecture constraints — nonnegotiable

**Kotlin Multiplatform, Compose Multiplatform commonMain-first, Clean Architecture, SRP, MVI + UDF, Pure Store, NO ViewModel, no screen-specific business hack, no duplicate abstractions.** Existing roles: SplashStore bootstrap, one DynamicContainer and **one DynamicStore per entry** for orchestration, one DynamicSession for retained values/effect queue, one navigation authority, one DynamicActionExecutor + per-action typed handlers, existing network and data repositories, existing explicit registries and typed models. No arbitrary `Any` bag/reflection dispatch or duplicate HTTP/navigation/cache/auth implementation.

Use the six logical Dynamic rendering categories:
`Screen -> exactly one Template (FormTemplate) -> multiple Component instances (StackComponent) -> Sections / direct Elements -> Groups / direct Elements -> Elements (Text, Input, Button, Image, Icon, Divider as currently registered) -> optional leading/trailing ElementContent (Icon, Divider, Image).`
- Component may own both Sections and direct Elements; Section may own both Groups and direct Elements; Group owns Elements only.
- Header/Body/Footer are Template slots; Body remains `template.components`; Header/Footer padding is independent.
- Presentations are separate dynamic overlays using typed child components, not new top-level Template.
- Typed DTOs/models/mappers/registries/renderers for each real concrete category. Current source contains registered `DividerElement` and `DividerContent`; do not assume old "DividerElement removed" proposals are current truth.
- Renderers display only. Store handles intents/validation/timer lifecycle. Repositories handle network. Handler returns action results/effects; Host applies navigation. **No shortcuts**.
- `$binding` (form input), `$context` (device/app approved runtime), `$state` (retained data and active countdown computed state), `$literal`; **no `$response`**.
- Registered action types `request, navigate, present, dismiss, external_uri, state, sequence`; current Sequence is **intentionally inert** (not a flow runner) and State/Present require source/Backend parity before expanding. Do not "complete" deferred handlers on your own.
- Styling/layout/errors/icons/assets authored in approved JSON, not hardcoded frontend defaults. Do not create `TimerElement`, a second OTP Store, per-element timer coroutine, another renderer/validation engine, or duplicated enum/helper infrastructure.

## 5. Exact UI authority and past regression lessons

Login v19:
`docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v19.json`
↔ `webApp/src/wasmJsMain/resources/preview/partner-login.json` (same content).

OTP v3:
`docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v3.json`
↔ `webApp/src/wasmJsMain/resources/preview/partner-otp.json` (same content).

These are **not to be redesigned or visually modified**. Read every JSON key/value and actual current renderers. Login Country Picker was painstakingly corrected; phone validation/error border affects editable phone-number region ONLY, not flag/dial code/separator. Keep conditional validation error (no invisible reserve), country sheet/list/empty-state fallback, hero/logo/card, all spacing/colors/typography. OTP v3 uses the same approved design style and exactly **one native BasicTextField mapped to six painted slots** and one `otp` binding. Preserve sequential focus/replacement/paste/backspace, keyboard Done/outside behavior, inline error alignment, timer accessory `icons/clock` centered beside text, and direct Resend without dialog. Historical old OTP JSON and old Backend composer are references only for actions/IDs, NOT designs to restore.

## 6. Current verified and open state (2026-10-25)

**Verified with Android A35 development preview**: 10-second countdown end-to-end UI, Resend disabled while counting/enabled at zero, Resend mock success restarts countdown, Resend simulated failure shows `Unable to complete action` and does not restart. Latest failure simulation launched with:
`adb -s 192.168.0.102:37353 shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.otp_preview true --ez cbpartner.otp_resend_failure true`.
Device serial is ephemeral; always check `adb devices -l`. Normal success omits the failure extra. This does NOT prove real backend 429, real SMS, token session, actual challenge validity, or Backend old/new screen parity. Keep 10 seconds in dev preview ONLY. Backend production policy remains 60 seconds.

Frozen existing timer contract (`155`): `data.countdowns` references numeric `$state.resendAfterSeconds`; transient computed `$state.countdowns.resend.remainingText` and `ready`; existing TextElement/guarded RequestAction; success retain new challenge/cooldown and restart; failure no replacement or restart. Existing tests source checks guard, replacement and duplicate taps. Review up-to-date test execution rather than assuming tests ran on latest HEAD.

The **exact Backend target** is already frozen in `157`, but implementation is **NOT** complete. Your next work is *source reconciliation -> targeted Backend schema/composer/Bootstrap/action changes -> focused tests -> actual HTTP integration*, not another architectural redesign.

## 7. Exact Backend ↔ Frontend gap and frozen target

- Bootstrap: Frontend Login screen `partner_login` / template ID `tpl_7K2M9Q`, Backend default bootstrap points to historic `tpl_P101L`. Align default AND persisted configuration, verify destination equality with loaded screen.
- GET Login `/api/v1/partner/screen/auth_login`: Backend old composer not Login v19; Backend must author approved v19.
- Initial Login Send OTP: Frontend action currently `/api/v1/auth/send_otp` with `countryCode,phoneNumber,deviceId`. Real Backend Partner endpoint `POST /api/v1/partner/auth/send_otp` body `{phoneNumber:$binding.mobileNumber,deviceId:$context.deviceId}`. Preserve validate=true, responseMode=destination, mode=push, retain `data.challengeId, data.phoneNumber, data.resendAfterSeconds`. Backend also returns expiresInSeconds/isNewUser/destination; response enveloped.
- OTP GET `/api/v1/partner/screen/auth_otp`: preserve screen `partner_otp`, template `tpl_P102O`, type `form_template`. Backend old composer must become approved v3 output; strict Backend schema lacks countdowns and other current UI vocabulary. Need specific typed changes (FormFieldSection, ListSection, presentations, enabledWhen and relevant action/asset/layout vocabulary) rather than permissive unknown fields.
- Resend already uses `POST /api/v1/partner/auth/send_otp` and phoneNumber/deviceId, `responseMode:none`, `enabledWhen=$state.countdowns.resend.ready`, retains new challengeId and resendAfterSeconds. Backend emits 60 seconds. Failing requests preserve old state; no automatic 10-second bypass.
- Verify currently Frontend `/api/v1/auth/verify` and `code=$binding.otp`. Real Backend is `POST /api/v1/partner/auth/verify_otp`, payload `{challengeId:$state.challengeId,phoneNumber:$state.phoneNumber,otp:$binding.otp,deviceId:$context.deviceId}`. Keep validate=true and responseMode=destination. No extra optional device fields invented.
- Navigation destination contains exactly **four** fields `screenId,templateId,templateType,endpoint`. Old six-field destination documents are superseded. Renderer must verify loaded screen identity.
- Backend Verify may return token/refreshToken and next `partner_dashboard/default_template`. Secure token persistence and authenticated next-screen renderability are **blocking open decisions**. Frontend currently supports `form_template`, not that next template; do not claim full login/verified dashboard.
- Real Country Picker data source is missing (dev has eight fixture countries). Allowed region/phone normalization and `+91` literal require separate product/security decision. Typed 429 cooldown error UX and process-restored deadline semantics also remain open.
- Security: Backend 6-digit OTP, TTL 300 seconds, resend cooldown 60 seconds, rate limiting and OTP/provider fail-closed rules. Never log OTP/token, relax security, or rely on client timer for actual authority.

## 8. Productive next step, minimal output and code scope

1. Verify actual branch heads, documentation supersessions, all source classes and both JSON pairs. Note evidence of real current tests/status and source conflicts; do not invent parity.
2. Create a concise **KEEP / MODIFY / CREATE** and file-by-file scope ONLY for frozen `157`, primarily Backend `SduiModel/Action/SduiBuilder`, Login/OTP composers, Bootstrap data source/config, tests, and action-field JSON mirrors. Highlight only genuinely open blockers that prevent the intended first wave.
3. Since `157` already freezes target behavior, **avoid asking again to freeze settled decisions**. If the request is audit-only, return audit; if implementation is requested, work in smallest safe wave, run available focused tests, review diff and report actual results. Changes requiring a new architecture/business contract must be separately discussed/frozen.
4. Preserve Login v19 and OTP v3 appearance and one generic runtime. Do not change unrelated modules, update old historical docs unnecessarily or expand State/Sequence; never handwave failing compilation.
5. Update `docs/architecture-proposal/dynamic-runtime-v2/` as source-verified gates complete, with tested vs untested explicitly labelled. Keep discussion in concise Hindi/Hinglish, docs/KDoc/code in English.
6. For local commands prefer `git fetch/pull` (after repairing any broken tracking ref) + narrow Gradle tests/build. Protect `CB-Partner-Live-Trace-V1.13.ps1`. For Android, target explicit ADB serial when more than one device. No unrelated Git cleanup.

**Do not waste time on extended routine discussion or giant plans for minor fixes. Speed means accurate scope, source-first audit, minimal diffs and immediate test evidence — not shortcuts, guesses, or silently changing frozen visual/contracts.**

End the preflight by reporting: **latest heads, authoritative doc version, current working UI, exact backend contracts, unresolved blockers, smallest next wave, expected tests.** Then proceed according to the user's authorization.
