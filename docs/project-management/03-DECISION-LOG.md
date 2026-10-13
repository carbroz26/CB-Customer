# CB-Partner — Decision Log

**Status:** ACTIVE — Base Architecture decisions frozen/documented through BASE-ARCH-017; BASE-ARCH-014 implementation accepted and code frozen; TRELLO-002 frozen.

## Purpose
Durable record of significant project decisions. It prevents the project from depending on old conversations to remember why a choice was made.

## Decision Classes
- Architecture
- Technology
- Product/technical contract
- API integration
- Quality/security
- Process

## Status
PROPOSED → ACCEPTED or REJECTED.  
ACCEPTED → SUPERSEDED only through the reopening process.

## Architecture Decision Record
Detailed Base Architecture research/freeze record:
docs/architecture/BASE-ARCHITECTURE.md

## Base Architecture Status
BASE-ARCH-001 through BASE-ARCH-017 → DECISION_FROZEN.

## Acceptance
A decision is not accepted because AI recommends it. The project owner explicitly accepts it, then it becomes frozen.

## Reopening
Use concrete evidence: changed requirements, incompatibility, security issue, unacceptable performance, maintenance problem, or incorrect assumption. Record evidence and impact before replacing a decision.

## TRELLO-001 Freeze Record — Superseded
**Decision:** Earlier Frontend Trello Operating Model  
**State:** SUPERSEDED  
**Superseded by:** TRELLO-002

The earlier model was replaced because the project owner clarified that Trello should remain a simple human-readable work board rather than a process/document tracker. The historical decision is retained here; the current model is documented in `docs/project-management/06-TRELLO-WORKFLOW.md`.

## TRELLO-002 Freeze Record
**Decision:** Simple Frontend Work Board  
**State:** DECISION_FROZEN  
**Owner approval:** Explicitly approved by the project owner on 2026-09-22.  
**Durable record:** docs/project-management/06-TRELLO-WORKFLOW.md

### Frozen model
- One authoritative frontend board: **CB-Partner — Frontend**.
- Lists: **BACKLOG → TO DO → READY → IN PROGRESS → DONE**.
- BACKLOG means possible/future work not yet committed.
- TO DO is the complete list of approved work that has been decided to be done.
- READY means approved and prepared work.
- IN PROGRESS means work actually being performed.
- DONE means work actually completed and verified.
- Cards represent actual human-readable project work, not architecture IDs or documents.
- Card structure is simple: title, short description, and concrete checklist/subtasks where useful.
- Trello does not contain architecture history, AI workflow, Git details, implementation plans, or document references as required card content.
- Project documentation remains responsible for architecture, decisions, technical constraints, implementation contracts, and AI/project process.
- GitHub remains responsible for source code and Git workflow.
- The AI maintains Trello execution state but does not invent project scope or architecture decisions.
- No automatic start of the next task after DONE.

This process decision does not change any frozen technical architecture decision.

## BASE-ARCH-014 Freeze Record
**Decision:** Minimum Gradle/KMP/Compose/Build-Logic Foundation  
**State:** DECISION_FROZEN  
**Owner approval:** Explicitly approved by the project owner on 2026-09-22.  
**Implementation plan:** docs/architecture/BASE-ARCH-014-IMPLEMENTATION-PLAN.md  
**Implementation plan state:** PLAN_FROZEN  
**Implementation authorization:** Granted through the approved unit-by-unit implementation workflow.

### Frozen foundation direction
- Included build-logic.
- Minimal type-oriented convention strategy.
- Central version catalog.
- Current KMP/AGP configuration.
- Default KMP source-set hierarchy unless concrete justification exists.
- Compose only where consumed.
- Minimal repeated testing/build baseline.
- Initial targets: Android, iosArm64, iosSimulatorArm64, JVM.
- Initial conventions: KMP shared-library, Compose Multiplatform, Android application, Desktop application.
- Foundation-required dependencies only; runtime architecture libraries are introduced when their real implementation unit has a consumer.
- No fake bootstrap or fake JSON.
- Later startup vertical slice uses the actual backend API and exact response supplied by the project owner.

### Implementation gate
The plan is frozen, but implementation remains unit-by-unit:
014-01 Gradle Project Skeleton → 014-02 Version Catalog → 014-03 Build Logic → 014-04 KMP Targets → 014-05 Compose → 014-06 Platform Boundaries → 014-07 Dependency Wiring → 014-08 Build/Test Baseline → 014-09 Architecture Verification.

No unit may silently change a frozen architecture decision.

## Current Documentation Checkpoint
BASE-ARCH-014 implementation planning is complete and the plan is frozen. The durable implementation plan and living status document have been created.

014-02 — Version Catalog was verified locally and explicitly accepted by the project owner on 2026-09-22. Acceptance authorizes progression to the next unit under the frozen plan; it does not authorize bypassing the required Trello READY/implementation preflight for 014-03.

No application source code has been implemented.

TRELLO-002 is frozen. TRELLO-001 is superseded. The Trello board has been migrated to the TRELLO-002 simple work model. 014-01 and 014-02 are represented as completed work; 014-03 is prepared in READY. Remaining approved work is represented as human-readable TO DO cards.

## BASE-ARCH-014-07 Acceptance Record

**Decision:** BASE-ARCH-014-07 — Dependency and Module Wiring  
**State:** ACCEPTED  
**Date:** 2026-09-23

The project owner supplied and reviewed the required local verification results. The implementation preserves the frozen dependency graph:

- :domain → :core
- :data → :domain, :core
- :navigation → :core
- :feature:splash → :domain, :core, :navigation
- :feature:dynamic → :domain, :core, :navigation

The five dependency reports and targeted JVM compilation all completed successfully. No out-of-scope runtime architecture or business implementation was introduced.

## BASE-ARCH-014-06 Namespace Boundary Record

**State:** ACCEPTED / FROZEN  
**Date:** 2026-09-23

The Android KMP namespace requirement is owned by the 014-06 platform boundary. The approved convention is deterministic and centrally derived from the Gradle module path using com.carbroz.cbpartner as the base package.

This correction resolved the Android KMP configuration failure without changing the 014-07 dependency decision, module boundaries, target matrix, or other BASE-ARCH-014 decisions.

The detailed namespace mapping is recorded in docs/architecture/BASE-ARCHITECTURE.md and the BASE-ARCH-014 implementation records.

## BASE-ARCH-014-09 Platform Boundary Clarification — 2026-09-23

**Decision:** Platform application boundary is distinct from Gradle module boundary.  
**State:** DECISION_FROZEN  
**Owner approval:** Explicitly approved by the project owner on 2026-09-23.  
**Durable records:** docs/architecture/BASE-ARCHITECTURE.md; docs/architecture/BASE-ARCH-014-IMPLEMENTATION-PLAN.md; docs/architecture/BASE-ARCH-014-IMPLEMENTATION-STATUS.md

### Frozen rule
Every supported platform has an explicit application boundary, but not every platform application boundary is a Gradle module.

- Android: `androidApp/` is the platform application boundary and Gradle project `:androidApp`.
- Desktop: `desktopApp/` is the platform application boundary and Gradle project `:desktopApp`.
- iOS: `iosApp/` is the platform application boundary owned by Xcode and is not a Gradle project.

Therefore `:iosApp` must not be added to `settings.gradle.kts`, and no synthetic iOS Gradle convention is to be created merely for platform symmetry.

This clarification preserves application-boundary symmetry while allowing each platform to use its native build-system boundary. It does not reopen or change the selected KMP, Clean Architecture, MVI/UDF, Pure Store, no-ViewModel, multi-module, Navigation, DI, or dependency-direction decisions.

### 014-09 consequence
The previous 014-09 finding that treated the absence of `:iosApp` from the Gradle hierarchy as a discrepancy is superseded by this clarification. A fresh 014-09 verification must instead verify the actual `iosApp/` native/Xcode application boundary.

## BASE-ARCH-014-09 F02 Resolution Record — 2026-09-23

**Finding:** BASE-ARCH-014-09-F02 — missing native iOS/Xcode application boundary.  
**State:** RESOLVED / VERIFIED.  
**Owner authorization:** Explicitly granted on 2026-09-23.

The repository previously contained only iosApp/.gitkeep. The authorized correction established the native Xcode application boundary under iosApp/ without creating a Gradle :iosApp project or an iOS Gradle convention.

The fresh 014-09 verification confirmed:
- native Xcode project boundary exists;
- iOS remains outside the Gradle module hierarchy;
- Android and Desktop retain their Gradle application boundaries;
- shared module structure and frozen dependency direction remain unchanged;
- no prohibited architecture or business/SDUI scope was introduced.

The Windows verification host cannot execute Xcode/macOS builds. That limitation is recorded as an environment limitation, not as a repository architecture failure.

No new architecture finding was identified.

## BASE-ARCH-014-T01 — Gradle Wrapper and Git/Build Operational Workflow — 2026-09-23

**State:** DECISION_FROZEN  
**Owner authorization:** Explicitly granted on 2026-09-23.

### Frozen operational rule
The project must have one recoverable operational path for repository lifecycle and build verification. The authoritative Git/terminal/build reference is `docs/project-management/07-DEVELOPMENT-WORKFLOW.md`, and `AI_START_HERE.md` must route AI sessions to it.

The required lifecycle is:

CLONE → VERIFY → FETCH → BRANCH → PULL/UPDATE → IMPLEMENT → BUILD/TEST → REVIEW → COMMIT → PUSH → PR/REVIEW → MERGE → FETCH/SYNC → BUILD/TEST.

The repository must use the official Gradle Wrapper for normal builds. BASE-ARCH-014-T01 targets Gradle 9.7.1 because the repository currently uses Android Gradle Plugin 9.3.0, whose documented minimum/default compatible Gradle version is 9.7.1.

This decision does not change the application architecture. It is a repository tooling and development-process decision.

### Implementation state
The documentation/routing portion and the official Wrapper implementation are complete. The Wrapper was generated by the Gradle wrapper task; its JAR was not manually fabricated. T01 is VERIFIED.

## BASE-ARCH-014-T01 Verification Record — 2026-09-23

**State:** VERIFIED

The official Gradle 9.7.1 Wrapper was generated, committed, and verified. Wrapper-based project hierarchy, build-logic build, shared JVM tests, and shared JVM compilation all completed successfully. The Windows iOS simulator disabled-target warning is an expected host limitation and was not suppressed.

The authoritative operational document records the verified baseline and Wrapper state. T01 is complete; no further tooling implementation is pending.

## BASE-ARCH-014-T01 Version Correction — 2026-09-23

**Authoritative Gradle version:** **9.7.1**

The earlier 9.5.0 wording in T01 was superseded by the project owner's explicit clarification that CB-Partner was using Gradle 9.7.1. The selected Wrapper target is therefore 9.7.1.

The single Gradle operational source of truth is docs/build/GRADLE-SETUP-AND-WORKFLOW.md. It covers Windows setup, Wrapper generation, commands, troubleshooting, version history, and the upgrade procedure.

9.5.0 remains only as historical context for the earlier compatibility-based proposal; it is not the CB-Partner baseline.

## BASE-ARCH-014 Final Implementation Checkpoint — 2026-09-23

**State:** IMPLEMENTATION VERIFIED — OWNER ACCEPTANCE PENDING

The complete implementation record has been reconciled with the repository state on `feature/base-arch-014`:
- 014-01 through 014-07 accepted;
- 014-08 verified;
- 014-09 verified and F02 resolved;
- T01 verified with Gradle 9.7.1 Wrapper and Wrapper-based baseline;
- no new architecture finding identified.

This checkpoint does not itself freeze the completed implementation. Final BASE-ARCH-014 code freeze remains subject to explicit project-owner acceptance.

## BASE-ARCH-014 Owner Acceptance and Code Freeze — 2026-09-23

**Decision:** ACCEPTED / FROZEN

The project owner explicitly accepted the complete BASE-ARCH-014 implementation after the recorded verification gates passed. The implementation is now code-frozen. Existing BASE-ARCH-014 decisions are not to be reopened without a new approved work item and evidence requiring reconsideration.

## BASE-ARCH-015 Freeze Record — 2026-09-30

**Decision:** Runtime Dependency & Technical Infrastructure Implementation Contract  
**State:** DECISION_FROZEN  
**Owner approval:** Explicitly accepted and frozen by the project owner.  
**Durable record:** docs/architecture/BASE-ARCH-015-DECISION-RECORD.md

BASE-ARCH-015 A–F are frozen. The decision covers core runtime contracts, Koin composition, Navigation 3 direction, Pure Store/MVIKotlin, the runtime compatibility baseline, and runtime testing boundaries. No production runtime implementation is authorized by this decision record alone.

## BASE-ARCH-016 Freeze Record — 2026-09-30

**Decision:** Koin Composition Root & Dependency Injection Implementation Contract  
**State:** DECISION_FROZEN  
**Owner approval:** Explicitly accepted and frozen by the project owner.  
**Durable record:** docs/architecture/BASE-ARCH-016-DECISION-RECORD.md

BASE-ARCH-016 A–G are frozen. The decision covers Koin 4.2.x/compiler-plugin baseline, constructor injection, DI module organization, DI graph validation/testing, Compose integration boundary, platform dependency boundary, and Store lifecycle ownership. No production Koin/DI implementation is authorized by this decision record alone.

## Current Documentation Checkpoint — BASE-ARCH-016

**State:** DOCUMENTATION/TRACKER SYNCHRONIZED — READY FOR NEXT AUTHORIZATION

The project records now agree that BASE-ARCH-001 through BASE-ARCH-016 are decision-frozen, BASE-ARCH-014 implementation is code-frozen and merged, and BASE-ARCH-015/016 have frozen contracts without automatic production implementation.

The next action must be a separately authorized work item processed through the established discussion/research/decision/freeze/plan workflow. No automatic implementation starts after this checkpoint.

## DYNAMIC-BOOTSTRAP-001 — Bootstrap Data Boundary Refinement — 2026-10-04

**Decision:** Option B — refine Bootstrap within the frozen startup responsibility
**State:** ACCEPTED / IMPLEMENTATION REQUIRED
**Scope:** Frontend Dynamic vertical slice only

### Decision
Bootstrap will follow the same repository + remote-source Data layering pattern already established for Dynamic, while preserving the frozen `ApplicationBootstrap` startup-facing contract:

```text
ApplicationBootstrap
    ↓
BootstrapRepository
    ↓
BootstrapRepositoryImpl
    ↓
BootstrapRemoteDataSource
    ↓
RequestHandler / Ktor
```

### Responsibilities

- `ApplicationBootstrap` remains the startup-facing domain contract consumed by Splash/application composition.
- `BootstrapRepository` is the domain repository abstraction.
- `BootstrapRepositoryImpl` coordinates Bootstrap data acquisition/mapping.
- `BootstrapRemoteDataSource` owns the Bootstrap endpoint, HTTP method, request headers and transport DTO acquisition.
- `BootstrapMapper` performs explicit DTO → domain mapping.
- `startup.nextScreen` maps to the canonical `DynamicDestination`.

### Important boundary rule

Splash and Bootstrap are one startup flow. Splash is the visible startup UI; Bootstrap is the config/startup API execution behind it. This decision does not create a second Splash/Bootstrap feature architecture and does not reopen BASE-ARCH-012/013.

### Known API contract

```text
GET /api/v1/partner/config/bootstrap

X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

### Superseded implementation assumption

The previous direct `DefaultApplicationBootstrap → RequestHandler` implementation is transitional and is superseded as the final implementation boundary. It must be removed/reworked so there is one production Bootstrap HTTP path.

### Required documentation/source follow-up

- Update implementation plan/status/tracker/master-state — completed by this reconciliation.
- Implement the repository/remote-source boundary.
- Update DI and tests.
- Verify Splash → Bootstrap → DynamicDestination end-to-end.

This decision is an implementation refinement within the frozen startup responsibility, not a reopening of the frozen base architecture.


## Documentation Consolidation Record — 2026-10-07

**Decision:** ACCEPTED / APPLIED

The frontend documentation tree was audited and consolidated to eliminate overlapping authorities.

### New authority model

- `AI_START_HERE.md` is the global router.
- `docs/project-management/04-PROJECT-MASTER-STATE.md` is the single current-state/recovery record.
- `docs/project-management/03-DECISION-LOG.md` is the durable decision record.
- Architecture, Dynamic, feature, API and build documents each own only their subject.

### Consolidated project-management records

The overlapping Current Status, Project Tracker, Checkpoints and previous Master State records were consolidated into Project Master State.

Implementation Protocol, Review Protocol, Terminal/Git Commands and Git Branching Workflow were consolidated into Development Workflow. Gradle setup/version authority remains exclusively in the Gradle document.

AI Guidelines and Do Not Do were consolidated into AI Rules.

### Feature/document consolidation

Splash/Bootstrap now has one feature document.

Web now has one platform document.

The Dynamic directory remains intentionally three-document:
- architecture;
- frozen implementation plan;
- implementation status.

### Architecture consolidation

The current architecture and platform-entry boundary are now represented by the single Architecture authority plus the frozen base/runtime decision records.

BASE-ARCH-014 implementation plan/status and the standalone platform-entry decision were superseded after their accepted decisions were folded into the current architecture/frozen records.

### Stale documentation removal

Superseded Dynamic discussion/frozen-plan documents, obsolete feature duplicates and duplicate project-management records were removed rather than left as competing sources of truth.

### Current Dynamic verification incorporated

The Dynamic source audit is complete and the local JVM verification is green:

```text
:data:jvmTest
:network:jvmTest
:navigation:jvmTest
:feature:dynamic:jvmTest

BUILD SUCCESSFUL
42 actionable tasks: 11 executed, 31 up-to-date
```

Numeric-looking JSON strings are preserved generically as `DynamicValue.StringValue`; this is not a phoneNumber-specific exception.

### Documentation rule

Future documentation must pass the single-responsibility test:

> If a document can be removed without losing a unique authority, it should not exist.

No new duplicate tracker, status, frozen-plan, or discussion document should be created for an existing authority.


## Historical/superseded: DYNAMIC-CONTRACT-REVISION-CONTENT-PADDING-001 — Accessory-owned padding

**Date:** 2026-10-11  
**State:** ACCEPTED / PLAN FROZEN / IMPLEMENTED — JVM test matrix and targeted compilation passed; final diff/obsolete-property audit and Web visual verification remain open.

### Decision
The frontend will represent accessory spacing through one shared nullable padding: DynamicPadding property on the existing ElementContent domain contract. Both IconContent and DividerContent use the same four-sided DynamicPadding type (start, top, end, bottom).

### Contract and ownership
- Backend accessory JSON owns the properties.padding values.
- :data maps the accessory's padding; :domain defines the shared typed contract; :feature:dynamic applies it in the existing accessory content renderer.
- Reuse the existing DynamicPadding type and padding helper where suitable.
- Remove the competing parent-level text-spacing fields from the frontend model, mapper, renderer, fixtures, tests and documentation.
- Missing padding adds no accessory-owned padding; do not invent spacing defaults.

### Explicit non-goals
No new abstraction, registry, renderer hierarchy, module, business-specific Login/OTP logic, architecture redesign, or unrelated cleanup.

### Verification and implementation gate
The backend owner reports that the engine test/build passed and the live Login and OTP responses contain the intended divider padding. The owner explicitly froze the reconciled frontend plan and authorized implementation on 2026-10-11. The shared `ElementContent.padding` contract, icon/divider mapping and rendering, obsolete parent-level spacing removal, and mapper regression coverage are implemented. The owner-environment JVM matrix passed for `:core`, `:domain`, `:data`, `:network`, `:navigation`, `:feature:splash` and `:feature:dynamic`; targeted compilation passed for `:domain`, `:data` and `:feature:dynamic`. Remaining gates: complete line-by-line diff/repository-wide obsolete-property audit and separate Web visual verification. Do not mark final freeze until those remaining gates are completed.



## DYNAMIC-CONTRACT-REVISION-CONTENT-SPACING-001 — Content-owned spacing

**Date:** 2026-10-11  
**State at initial decision:** ACCEPTED / PLAN FROZEN / IMPLEMENTATION NOT STARTED — superseded by the implementation record below  
**Authority:** `docs/dynamic/DYNAMIC-ARCHITECTURE.md`, section 21; `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`, section 22.26.

### Decision
Replace the accessory-owned `ElementContent.padding: DynamicPadding?` contract with `ElementContent.spacing: DynamicSpacing?`. The new `DynamicSpacing` value has four external layout-space edges: start, top, end, and bottom. It applies uniformly to IconContent and DividerContent and is supplied by backend `properties.spacing`.

### Superseded behavior
- Content-level `properties.padding` is superseded by `properties.spacing`.
- Text-parent `contentSpacing` and `trailingContentSpacing` are removed as competing ways to control accessory gaps.
- `DynamicPadding` remains valid for ordinary node/container padding and is not globally renamed.

### Boundaries
Spacing affects the accessory's outer layout bounds; it must not alter text characters/letter spacing or the divider's visible line geometry. No screen-specific logic, new registry, renderer hierarchy, architecture layer, or unrelated refactor is allowed.

### Implementation and verification record — 2026-10-12
The owner subsequently authorized implementation on 2026-10-12. Frontend implementation is committed on PR #6, head `c08a6729186590eb094e79176f34daa119aa8a4a`: `ElementContent.spacing: DynamicSpacing?` is shared by IconContent and DividerContent; the mapper reads backend `properties.spacing`; existing renderers apply four-sided external spacing; ordinary node/container `DynamicPadding` remains unchanged.

The owner ran the six-module JVM test matrix, Android unit tests, Android Kotlin compilation and debug assembly, Web/Wasm development webpack, and Web/Wasm browser tests successfully on that PR head. GitHub returned no PR-triggered workflow runs and zero commit check-runs, so no CI pass is claimed. Final line-by-line diff/obsolete-property audit and actual Web visual inspection remain open; do not mark final freeze or merge complete until those gates are addressed.


## DYNAMIC-REGISTRY-ELEMENT-GENERIC-001 — Generic Element Renderer Registry Consolidation

**Date:** 2026-10-13  
**State:** FROZEN ARCHITECTURAL DECISION; IMPLEMENTATION PLAN FROZEN; SOURCE IMPLEMENTATION NOT YET STARTED  
**Scope:** CB-Partner frontend, `feature/dynamic-ui` only.  
**Authority:** This decision entry, once reconciled and frozen with `docs/dynamic/DYNAMIC-ARCHITECTURE.md` and `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`.

### Problem
The current registry repeats per-element maps, lookups, builder maps and registration methods for Text, Image, Input, Button and Icon. `DynamicRendererContracts.kt` also declares one renderer interface per concrete element, while `DynamicRenderContext` repeats class-to-key dispatch. Adding a new element therefore requires changing registry infrastructure in addition to adding the model, mapper, renderer and explicit registration.

### Owner-approved direction
- Introduce a common domain `DynamicElement` contract implemented by the existing concrete element models.
- Replace the five concrete renderer interfaces with `ElementRenderer<T : DynamicElement>`; concrete renderer implementations remain typed to their own model.
- Consolidate element registration into one normalized-key map, one lookup and one builder API: `.element(type, renderer)`.
- Keep explicit registrations for each supported backend type.
- Confine unavoidable generic type-erasure handling to one checked adapter/dispatch boundary. Do not place casts in concrete renderers or retain a growing class-dispatch `when` in `DynamicRenderContext`.
- Keep Template, Component, Section, Group and ElementContent contracts and registries separate. Do not introduce reflection-based discovery, `Map<String, Any>`, a universal cross-category registry, a central generic mapper, or unrelated architecture changes.
- Preserve mapper ownership, DynamicValue/reference semantics, rendering behavior, key normalization, deterministic duplicate rejection, and existing backend type keys.

### Reconciliation and freeze rule
The current frozen clauses in the Dynamic architecture and implementation plan remain authoritative until the revised plan is reviewed and explicitly frozen. This accepted direction does not by itself authorize source edits. The plan must define the common element collection typing and the exact checked dispatch mechanism, then identify affected source/tests and verification gates. No implementation is authorized while that reconciliation remains open.


### Frozen dispatch detail

### Frozen dispatch algorithm — amended and frozen

The owner approved this dispatch amendment on 2026-10-13 after the compatibility and impact audit. **Renderer lookup is model-class-based, not backend-key-based.**

- The public builder API is `.element(type, modelClass, renderer)`; `type` is normalized and retained for explicit backend-key registration/duplicate-key validation, while `modelClass: KClass<T>` is the lookup identity for `T : DynamicElement`.
- `DynamicRegistry` uses one element-renderer map keyed by `KClass<out DynamicElement>`. The builder also tracks normalized registered keys for blank/duplicate rejection; this key-validation set is not a second renderer map.
- `DynamicRenderContext` dispatches using the concrete element model's class identity. It must not reconstruct backend keys with a growing class `when`.
- `ElementRenderer<T : DynamicElement>` remains strongly typed. One internal adapter captures the explicit `KClass<T>`, defensively checks `modelClass.isInstance(element)`, and contains the sole unchecked cast. Concrete renderers do not cast.
- Explicit registrations remain `text`, `image`, `input`, `button`, and `icon`. No reflection discovery, `Map<String, Any>`, centralized generic mapper, or cross-category registry is allowed.
- Backend type-key interpretation remains in the existing `:data` mapper. Unknown backend keys keep the mapper's existing handling; a domain element with no registered model class must fail through a deterministic missing-renderer diagnostic. A model/adapter mismatch must fail deterministically with a useful diagnostic.
- `KClass.isInstance` is available in Kotlin common API. The project-specific Gradle/Compose compilation has not yet been run; perform that check before source edits and stop if the configured toolchain rejects the frozen design.

The generic registry consolidation supersedes earlier Dynamic registry clauses only where they require five element-specific renderer contracts, maps, lookups, builder methods, or concrete-element dispatch in `DynamicRenderContext`. All other frozen Dynamic rules remain in force. This freezes the architectural decision and planned scope; source implementation remains a separate execution step.


## DYNAMIC-REGISTRY-ELEMENT-SIMPLIFICATION-001 — Concrete typed Element registry

**Date:** 2026-10-13  
**State:** OWNER-APPROVED; ARCHITECTURE AND IMPLEMENTATION PLAN FROZEN; SOURCE SIMPLIFICATION NOT STARTED  
**Supersedes:** the active implementation direction of `DYNAMIC-REGISTRY-ELEMENT-GENERIC-001` where it requires a generic renderer adapter, model-class-keyed registry, and normalized-key side set. The earlier entry is retained as decision history, not the current implementation target.

### Problem

The candidate generic implementation made Element different from the established string-keyed Template/Component/Section/Group/Content registry pattern. It introduced `ElementRenderer<T>`, `CheckedElementRenderer`, a `KClass` map, and a separate normalized-key set. The owner requires simpler, concrete typed paths rather than generic type-erasure machinery.

### Frozen decision — Option A

- Use five concrete renderer contracts for Text, Image, Input, Button, and Icon.
- Use category-specific string-keyed renderer maps and typed registration/lookups, with the existing normalized-key and shared blank/duplicate validation pattern.
- Keep backend keys `text`, `image`, `input`, `button`, and `icon` as the actual registration/lookup keys.
- Remove `CheckedElementRenderer`, generic `ElementRenderer<T>`, `KClass` lookup/registration, the unchecked cast, and the `elementTypes` side set.
- Use a small exhaustive concrete-model-to-backend-key dispatch in `DynamicRenderContext`. Explicit dispatch is accepted to preserve concrete renderer parameter types without generic adapters or casts.
- Keep the common `DynamicElement` marker only for heterogeneous typed collections. Do not add backend type fields to domain models.
- Keep `:data` mapping and all five concrete mapper classes unchanged in this registry amendment.
- Do not change `ElementMapperSupport` in this work item. It shares mapping behavior and is not part of renderer registry dispatch. Reconsider it only through a separate focused mapper review.
- Keep Content registry/rendering and all other Dynamic architecture unchanged.

### Exact implementation and verification authority

- Architecture: `docs/dynamic/DYNAMIC-ARCHITECTURE.md`, section 23.
- File-by-file plan and tests: `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md`, section 22.28.
- Current state: `docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md`, section 16.

The implementation plan is frozen, but the source simplification has not started and no tests/builds have been run for this new direction. Implement only the specified files, stop on source/contract conflicts, and update status only after actual verification.
## DYNAMIC-ELEMENT-MAPPER-CONSOLIDATION-001 — One Element Mapper

**State:** ACCEPTED / PLAN FROZEN / IMPLEMENTATION AUTHORIZED  
**Owner approval:** Explicitly approved on 2026-10-13 by “ok continue” after source-level review.  
**Scope:** `data` module's Dynamic element DTO-to-domain mapping only.

### Decision
- Keep exactly one concrete element mapper class: `ElementMapper`.
- `ElementMapper` owns backend element type dispatch (`text`, `image`, `input`, `button`, `icon`), concrete DTO decoding, and direct mapping into the corresponding domain element model.
- Remove `TextElementMapper`, `ImageElementMapper`, `InputElementMapper`, `ButtonElementMapper`, `IconElementMapper`, and `ElementMapperSupport`.
- Keep `ElementContentMapper` separate for content DTOs. Reuse one focused `DynamicActionMapper` for shared action DTO → domain action conversion used by elements and content. This removes duplicate action conversion without placing action mapping in an unrelated value mapper or content mapper.
- Keep `DynamicValueMapper` responsible for JSON/DynamicValue and property-value conversion; do not move action DTO mapping into it.
- Keep template/component/section/group mappers, DTO/domain schemas, backend type keys, error behavior, defaults, binding/validation mapping, content mapping, and DynamicValue/reference semantics unchanged except for injecting the renamed `ElementMapper` into existing container mappers.
- Do not alter renderer registries, domain models, frontend/backend contracts, or unrelated mapper behavior.

### Implementation boundary
This is separate from `DYNAMIC-REGISTRY-ELEMENT-SIMPLIFICATION-001`. The latter remains intact. Verify implementation against `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md` section 22.29.