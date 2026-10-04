# CB-Partner Dynamic UI — Implementation Plan

## Status

**PLAN FROZEN — IMPLEMENTATION AUTHORIZED**

This is the single implementation contract for the current Dynamic refactor. Implementation must match this plan exactly.

> **Strict rule:** after every implementation unit, inspect the changed source and verify architecture, Clean Architecture boundaries, SRP, imports, dependencies, calls, lifecycle, tests, and platform compatibility. If implementation reveals a material conflict with this plan or a frozen Dynamic decision, STOP → REPORT → DECIDE → UPDATE THIS PLAN → CONTINUE. Never silently redesign the plan during coding.

## 1. Authority

Order of authority:

1. Current user instruction.
2. `AI_START_HERE.md` and project-management rules.
3. Frozen Dynamic discussion: `docs/features/dynamic/00-FEATURE-DISCUSSION.md`.
4. Frozen navigation/container record: `docs/features/dynamic/03-NAVIGATION-CONTAINER-FREEZE.md`.
5. Fixed SDUI/backend JSON contract.
6. This implementation plan.
7. Current source as forensic baseline only.

No implementation detail may override a frozen contract.

## 2. Platform Scope

Supported application platforms:

```text
Android
 iOS
 Web (Wasm)
```

There is **no `desktopApp`**. Do not add, restore, or maintain a Desktop application for this feature.

The Dynamic Container, Dynamic runtime, models, Store/use cases, repository boundary, renderer, and Navigation 3 integration are shared/common code wherever technically possible.

`iosApp` remains the native/Xcode boundary already frozen by Base Architecture. Android and Web have their own thin platform entry points. Platform code must not contain Dynamic business logic.

## 3. Target Architecture

```text
Android / iOS / Web entry
            ↓
       Shared App
            ↓
    Application Composition
            ↓
       Navigation 3
            ↓
 DynamicDestination back stack
            ↓
    One Dynamic Container
            ↓
    Dynamic screen/runtime
            ↓
 LoadDynamicScreenUseCase
            ↓
    DynamicRepository
            ↓
   RemoteDataSource
            ↓
         Backend
            ↓
 Dynamic Screen JSON
            ↓
    DynamicRenderer
            ↓
 registered Template
            ↓
Component / Section / Group / Element
```

Action/navigation flow:

```text
User interaction
      ↓
DynamicStore
      ↓
ExecuteDynamicActionUseCase
      ↓
request/action result
      ↓
DynamicDestination or navigation effect
      ↓
Navigation boundary
      ↓
Dynamic Container
```

The Dynamic Store does not own navigation mechanics.

## 4. Navigation 3 Boundary

The existing `:navigation` module becomes the single shared application navigation boundary. No second navigation framework is introduced.

The repository already pins Navigation 3 dependencies in the version catalog. Do not upgrade or replace those versions as part of this implementation unless a separate dependency decision is explicitly approved.

Navigation 3 is used according to its user-owned-back-stack model: the application owns observable navigation state and `NavDisplay` renders the current entries. Dynamic destinations are serializable navigation keys so the same navigation model works across Android, iOS, and Web.

### 4.1 DynamicDestination

`DynamicDestination` is the canonical navigation key for Dynamic screens. Reuse/refactor the existing domain model; do not create a duplicate route model.

It represents the complete backend destination:

```text
screenId
templateId
templateType
endpoint
method
authentication
```

Meaning:

```text
templateType
    → which registered Template definition renders the destination

templateId
    → concrete Dynamic destination/template instance identity

endpoint + method + authentication
    → how the Dynamic screen document is loaded
```

The navigation stack stores complete `DynamicDestination` entries, never only `templateType`, only `templateId`, or a string route.

### 4.2 Navigation state

The navigation module owns the navigation back stack and the minimum operations required by the frozen contract:

```text
setRoot(destination)
push(destination)
replace(destination)
pop()
```

`replace` is required for backend-driven flows that replace the current destination rather than creating another history entry.

Do not create a `NavigationManager`, `NavigationCoordinator`, `NavigationRouter`, or other wrapper unless a concrete single responsibility cannot be expressed by the minimal navigation boundary.

### 4.3 Navigation host

Use Navigation 3's `NavDisplay`/entry mechanism. The navigation host resolves a `DynamicDestination` to the one Dynamic Container.

There are no Login/OTP/Dashboard navigation entries/classes.

### 4.4 Back

System/application back removes the current full Dynamic destination and reveals the previous destination.

The Dynamic Store does not implement back-stack mutation.

### 4.5 Refresh

Refreshing the current Dynamic screen reuses the current complete `DynamicDestination`. It does not create a new navigation entry.

### 4.6 External URI

`external_uri` is not internal navigation.

```text
external_uri
    ↓
external URI capability/effect
    ↓
platform boundary
```

Do not put external URIs into the Dynamic navigation stack.

Future backend actions such as `dismiss`, `present`, `deeplink`, or additional capabilities remain extensible, but are not speculatively implemented before their backend contract is required.

## 5. Dynamic Container

There is exactly **one permanent shared Dynamic Container**.

Mental model only:

```text
Dynamic Container ≈ stable Activity-like host
Template         ≈ replaceable Fragment-like content
```

This is not implemented with Android Activities or Fragments. It is shared Compose Multiplatform UI.

The container remains stable while the current `DynamicDestination` changes.

```text
Dynamic Container
    ↓
current DynamicDestination
    ↓
load current screen document
    ↓
render current templateType
```

The container must not:

- own the navigation stack;
- call Ktor or a repository directly;
- execute backend actions directly;
- contain Login/OTP/Dashboard branches;
- infer destinations from template types;
- create a container per template.

## 6. Dynamic Store

`DynamicStore` remains a Pure MVI + UDF Store and is responsible only for Dynamic screen/runtime state and intent/effect coordination.

It owns:

```text
Dynamic screen state
loading/error/success state
current Dynamic interaction/binding state
intent handling
use-case invocation
state emission
Dynamic effects
```

It does not own:

```text
Ktor/HTTP
JSON request serialization
endpoint parsing
common request metadata
navigation stack
Navigation 3 APIs
external URI launching
recursive rendering
registry lookup
business-screen branching
response-history storage
```

No ViewModel is introduced.

## 7. Use Cases

The Dynamic implementation must contain meaningful use-case boundaries. Do not add use-case classes merely to satisfy a naming pattern.

### 7.1 `LoadDynamicScreenUseCase`

Responsibility:

```text
DynamicDestination
    ↓
DynamicRepository
    ↓
Dynamic screen domain result
```

It does not render, navigate, or mutate Store state.

### 7.2 `ExecuteDynamicActionUseCase`

Responsibility:

```text
Dynamic action + resolved runtime input
    ↓
Dynamic action execution boundary
    ↓
result/effect for Store
```

It may coordinate request execution and interpretation of the fixed action contract. It must not become a generic workflow engine.

Do not create one use case per action type.

## 8. Data / Domain Boundary

### Domain

Own canonical Dynamic contracts:

```text
DynamicDestination
DynamicAction
DynamicReference
DynamicRepository
Dynamic request/result models
```

Domain must not depend on Ktor, Compose, or Data DTOs.

### Data

Own:

```text
DTOs / serialization models
DynamicRepository implementation
RemoteDataSource usage
DTO → domain mapping
```

Dynamic Data code uses the application's existing common `RemoteDataSource`. It does not create another `HttpClient`, base URL, request infrastructure, or Ktor layer.

### Common request metadata

Headers/metadata that are common to requests are resolved from the common application request infrastructure, not `$binding`.

Examples include platform, app version, build number, device/session metadata, and authentication/session metadata where applicable.

Dynamic `$binding` is for current Dynamic input/runtime data.

```text
Common request metadata
    → common request infrastructure

Dynamic input data
    → $binding / Dynamic runtime state
```

No device ID or platform value is hardcoded in `DynamicStore`.

## 9. Destination and Screen Loading Flow

### Bootstrap

```text
Bootstrap
    ↓
startup.nextScreen
    ↓
DynamicDestination
    ↓
Navigation.setRoot()
```

Bootstrap does not call Dynamic UI code and does not own navigation.

`DefaultApplicationBootstrap` remains the Bootstrap/Data implementation. It is not renamed into a Dynamic coordinator and is not moved into `feature:dynamic`.

### Current Dynamic screen

```text
current DynamicDestination
    ↓
LoadDynamicScreenUseCase
    ↓
DynamicRepository
    ↓
RemoteDataSource
    ↓
backend endpoint/method/authentication
    ↓
response envelope
    ↓
Dynamic screen domain model
```

### Next destination

When a backend response/action provides a complete next destination:

```text
nextScreen
    ↓
DynamicDestination
    ↓
Navigation operation
```

The frontend never decides the destination from `templateType`.

## 10. SDUI Registry and Renderer

The first implementation vocabulary is exactly:

```text
Template  → stack_template
Component → stack_component
Section   → stack_section
Group     → stack_group
Elements  → text, image
```

Do not retain or add `form_template`, `input`, `button`, `row_*`, icon, divider, spacer, or other unregistered definitions unless the backend contract is explicitly expanded and a new discussion is frozen.

### Registry

One public `DynamicRegistry` with category-specific internal registration/lookup.

It owns only registration and lookup.

No:

- reflection;
- package scanning;
- plugin framework;
- generic factory hierarchy;
- navigation;
- networking;
- Store state.

### Renderer

One small recursive `DynamicRenderer`:

```text
DynamicRenderer
    ↓
category-specific registry lookup
    ↓
registered definition
    ↓
definition renders its own Compose structure
    ↓
children delegate back to DynamicRenderer
```

No giant renderer containing all definition implementations.

Definitions do not own API calls, navigation, Store state, or action execution.

## 11. Actions and References

The frozen action vocabulary is:

```text
request
navigate
present
dismiss
state
external_uri
sequence
```

The implementation must not invent speculative actions.

The frozen reference vocabulary is:

```text
$binding
$context
$response
$literal
```

Reference meanings:

```text
$binding
    → current Dynamic input/runtime values

$context
    → controlled application/flow context

$response
    → current supported action/request response reference semantics

$literal
    → literal backend-provided value
```

Do not create a response-history subsystem, challenge-specific state, or separate form-state owner merely to support references.

## 12. Binding and Request Body

Binding is generic. It must work for arbitrary backend-defined input such as:

```text
firstName
lastName
email
phone
image
address
etc.
```

The backend determines which Dynamic fields are bound and which request body values are produced from those bindings.

Common metadata is added by the request infrastructure, not by binding.

Example conceptual request construction:

```text
backend request definition
        ↓
resolve $binding values
        ↓
resolve $context values when allowed
        ↓
add common request metadata
        ↓
RemoteDataSource
```

No Login/OTP-specific request builder is introduced.

## 13. Exact Initial File Direction

The implementation may reuse/refactor existing files. Do not duplicate equivalent ownership.

Expected ownership after refactor:

```text
:navigation
└── shared Navigation 3 boundary
    ├── navigation state/back stack
    ├── DynamicDestination NavKey integration
    └── NavDisplay host

:feature:dynamic
├── Dynamic Container
├── DynamicScreenStore / DynamicStore (one canonical Store)
├── usecase/
│   ├── LoadDynamicScreenUseCase.kt
│   └── ExecuteDynamicActionUseCase.kt
├── renderer/
│   └── DynamicRenderer.kt
├── registry/
│   └── DynamicRegistry.kt
├── action/
│   └── existing action boundary only if still needed
└── sdui/
    ├── template/StackTemplate.kt
    ├── component/StackComponent.kt
    ├── section/StackSection.kt
    ├── group/StackGroup.kt
    └── element/
        ├── TextElement.kt
        └── ImageElement.kt
```

Canonical domain/data models remain in their existing correct Clean Architecture owners. If an equivalent model already exists, refactor/reuse it; never create a second copy.

### Application composition

Create/rework the shared application composition so Android, iOS, and Web enter the same common App/navigation boundary.

Platform entry points remain thin.

`webApp/Main.kt` must not remain the owner of Dynamic navigation or Dynamic business orchestration.

`androidApp` must be wired to the same shared App when its platform entry is implemented.

`iosApp` remains the native/Xcode boundary and hosts the same shared App.

No `desktopApp` is created.

## 14. Existing Code Disposition

Existing code is not protected merely because it exists. If it conflicts with this frozen architecture, it must be refactored or removed.

### Refactor/remove

- destination mutation/navigation logic currently embedded in application `Main.kt`;
- navigation ownership from `DynamicStore`;
- direct HTTP/request construction from `DynamicStore`;
- hardcoded device ID or platform metadata from Dynamic feature code;
- duplicate Dynamic destination models;
- renderer branches for definitions outside the frozen initial vocabulary;
- speculative manager/coordinator abstractions;
- any duplicate form/input/button implementation not supported by the current backend contract.

### Keep/reuse

- `DefaultApplicationBootstrap` as Bootstrap/Data implementation;
- common RemoteDataSource/network infrastructure;
- existing Dynamic domain contracts when they already satisfy the frozen model;
- existing Dynamic registry/renderer when they can be cleanly refactored;
- Pure MVI/UDF Store pattern;
- canonical SDUI model/definition concepts.

## 15. Implementation Units and Gates

Implementation proceeds in small units. After **every unit**:

1. inspect every changed file;
2. verify imports and call sites;
3. verify dependency direction;
4. verify SRP and no overengineering;
5. verify no duplicate abstraction was introduced;
6. compile the directly affected modules;
7. run focused JVM tests where available;
8. inspect the resulting diff;
9. only then continue.

### Unit 1 — Navigation foundation

- wire `:navigation` to the selected Navigation 3 APIs already pinned in the repository;
- implement the minimal shared navigation state/back stack;
- make canonical `DynamicDestination` usable as the navigation key;
- implement `setRoot`, `push`, `replace`, `pop`;
- implement `NavDisplay` host resolving Dynamic destinations to the single Dynamic Container;
- add focused navigation tests.

### Unit 2 — Dynamic Container / application composition

- create/rework one shared Dynamic Container;
- remove Dynamic navigation ownership from `webApp/Main.kt`;
- establish shared App → Navigation → Dynamic Container composition;
- wire Android/iOS/Web entry boundaries without platform-specific Dynamic logic;
- do not add desktop.

### Unit 3 — Use-case boundary

- add/refactor `LoadDynamicScreenUseCase`;
- add/refactor `ExecuteDynamicActionUseCase`;
- inject them into the canonical Store;
- remove direct repository/network orchestration from Store.

### Unit 4 — Dynamic Store cleanup

- reduce Store to Pure MVI/UDF state/intent/effect coordination;
- remove navigation mechanics;
- remove direct request construction;
- remove hardcoded common metadata;
- preserve generic binding/runtime state;
- preserve navigation effects without executing navigation itself.

### Unit 5 — Dynamic data flow

- ensure DynamicRepository is the domain/data boundary;
- route through the single RemoteDataSource;
- map fixed response envelope and destination/screen contracts;
- verify request body binding and common metadata separation.

### Unit 6 — Registry / renderer cleanup

- enforce exact initial vocabulary;
- remove unsupported current registrations/branches;
- verify recursive renderer and definition ownership;
- preserve backend ordering and controlled unsupported handling.

### Unit 7 — Action/reference integration

- connect the fixed action vocabulary to the use-case/runtime boundary;
- keep navigation and external URI separate;
- verify `$binding`, `$context`, `$response`, `$literal` according to the frozen contract;
- do not create speculative action infrastructure.

### Unit 8 — End-to-end Dynamic flow

Verify:

```text
Bootstrap
  ↓
Login DynamicDestination
  ↓
Navigation stack
  ↓
Dynamic Container
  ↓
Login endpoint
  ↓
Login screen JSON
  ↓
stack_template → stack_component → ... → text/image
  ↓
backend action/request
  ↓
next DynamicDestination
  ↓
push/replace as instructed
  ↓
new screen endpoint
```

No Login/OTP/Dashboard branching is permitted in generic Dynamic code.

## 16. Required Tests

At minimum:

### Navigation

- root destination is installed correctly;
- push preserves complete destination;
- replace replaces only the current entry;
- pop returns the previous complete destination;
- refresh does not add an entry;
- identical `templateType` values can coexist when destinations differ;
- `DynamicDestination` serialization works for the supported multiplatform targets/strategy.

### Store/use cases

- Store invokes use cases rather than network directly;
- loading/success/error transitions are deterministic;
- navigation is emitted as an effect/result, not performed by Store;
- external URI is emitted separately from internal navigation;
- arbitrary binding values are resolved without hardcoded field names.

### Registry/renderer

- exact initial vocabulary is registered;
- unsupported definitions fail cleanly;
- duplicate registration is rejected;
- recursive hierarchy renders in backend order;
- template type selects rendering only.

### Integration

- Bootstrap `startup.nextScreen` becomes the root destination;
- current destination endpoint loads the corresponding Dynamic screen;
- response-provided next destination becomes the next navigation entry according to the frozen action/response semantics;
- back restores the previous full destination;
- no business-screen-specific frontend branching exists.

## 17. Local Verification Protocol

Before every test run:

```powershell
cd D:\CarBroz\CB-Partner
git status
git fetch origin
git pull --ff-only origin feature/dynamic-ui
git branch --show-current
git log -1 --oneline
git status
```

The branch must remain:

```text
feature/dynamic-ui
```

Do not add the existing untracked `kotlin-js-store/` directory to Git unless it is explicitly determined to be a required project file.

### Compilation

```powershell
.\gradlew.bat :navigation:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :domain:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
```

### Focused tests

```powershell
.\gradlew.bat :navigation:jvmTest :data:jvmTest :domain:jvmTest :feature:dynamic:jvmTest
```

If a task does not exist, use the exact available task for that module; do not invent a Gradle task.

### Full project verification

After the affected units are green:

```powershell
.\gradlew.bat build
```

Windows verification cannot run iOS Simulator tests. The expected disabled-target warning on Windows is not itself a test failure.

## 18. Definition of Done

Dynamic implementation is complete only when all are true:

```text
[ ] Android/iOS/Web are the only application platforms
[ ] no desktopApp is introduced
[ ] one shared Navigation 3 boundary exists
[ ] DynamicDestination is the canonical navigation key
[ ] complete destination is stored in back stack
[ ] one permanent Dynamic Container exists
[ ] templates are replaceable/renderable content inside that container
[ ] DynamicStore does not own navigation
[ ] DynamicStore does not call Ktor directly
[ ] meaningful use cases exist and are actually used
[ ] common request metadata is outside $binding
[ ] Dynamic input/form values use generic bindings
[ ] registry contains only the frozen initial vocabulary
[ ] renderer remains small and recursive
[ ] no templateType-based business navigation exists
[ ] external_uri remains separate from internal navigation
[ ] no speculative manager/coordinator/router framework exists
[ ] Bootstrap and Dynamic share the common network boundary
[ ] Android/iOS/Web share the same application composition
[ ] focused tests pass
[ ] affected module compilation passes
[ ] final full build passes where host-supported
[ ] every implementation unit was cross-checked against this plan
```

## 19. Strict Change Rule

If any source requirement cannot be implemented exactly as written:

```text
STOP
 ↓
inspect actual source/contract
 ↓
report conflict
 ↓
make an explicit architecture decision
 ↓
update this plan/freeze record
 ↓
resume implementation
```

No silent deviation is allowed.

No "while we are here" refactor is allowed unless it is required by this frozen plan.

No new abstraction is justified merely because it looks architecturally sophisticated.

The target is production-quality Clean Architecture with the **minimum number of concepts necessary to satisfy the actual product contract**.