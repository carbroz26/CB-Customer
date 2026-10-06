# CB-Partner Dynamic UI — Implementation Plan Frozen

**Status:** PLAN FROZEN — IMPLEMENTATION AUTHORIZED  
**Date:** 2026-10-06  
**Branch:** `feature/dynamic-ui`

This is the single active implementation-plan contract for the current Dynamic implementation phase.

## 1. Governing architecture

Implementation follows `DYNAMIC-ARCHITECTURE-FROZEN.md`.

No implementation may introduce:

- a second Dynamic navigation framework;
- an empty template/instance abstraction;
- ExecuteDynamicActionUseCase;
- duplicate action executors;
- duplicate binding/state stores;
- business-specific screen classes;
- speculative manager/coordinator/router layers;
- reflection/package scanning.

## 2. Current implementation boundary

The implementation contains:

```
:data
  Dynamic DTOs
  Dynamic mappers
  repository/data-source boundary

:domain
  Dynamic contracts/models
  DynamicDestination
  DynamicResponse
  typed UI domain models

:feature:dynamic
  Dynamic Container
  Store/runtime
  action executor/handlers
  registries
  use cases
  renderer

:navigation
  application navigation boundary
```

## 3. Typed contract

DTOs decode the backend response. Mappers convert DTOs into concrete typed domain models.

Current concrete vocabulary:

```
FormTemplate
StackComponent
StackSection
StackGroup

TextElement
ImageElement
InputElement
ButtonElement
IconElement
DividerElement
```

The screen-level theme is a sibling of the template.

## 4. Destination lifecycle

```
DynamicDestination
  ↓
Dynamic Container
  ↓
TemplateRegistry(templateType)
  ↓
LoadDynamicScreenUseCase(endpoint)
  ↓
Dynamic API
  ↓
complete Dynamic response
  ↓
typed domain model
  ↓
DynamicRenderer
```

## 5. Action lifecycle

```
interaction
  ↓
DynamicStore
  ↓
DynamicActionExecutor
  ↓
action-specific handler
  ↓
DynamicActionResult
  ↓
DynamicStore
```

Request actions resolve references/bindings before request construction.

Request `responseMode=destination` maps `data.destination` into DynamicDestination and uses the same navigation/container flow as direct navigation.

## 6. Test scope

The implementation acceptance suite must cover:

### Bootstrap
- successful bootstrap mapping;
- startup destination mapping;
- maintenance/update/authenticated flags;
- HTTP/network/serialization failure classification;
- bootstrap headers and endpoint contract.

### Dynamic mapping
- screen/template/theme mapping;
- component/section/group ordering;
- all six known element types;
- typed element properties/binding/actions;
- unsupported element rejection;
- response-level destination;
- arbitrary response payload preservation.

### Binding/reference
- `$binding`;
- `$context`;
- `$response`;
- `$literal`;
- missing/invalid references;
- multiple bindings.

### Actions
- request;
- navigate;
- response destination;
- present;
- dismiss;
- state;
- external_uri;
- sequence and recursive sequence execution.

### Store/runtime
- loading lifecycle;
- binding updates;
- action orchestration;
- action result/effect application;
- retry/error paths.

### Backend-shaped flow
```
Bootstrap
  ↓
Login destination
  ↓
Login screen
  ↓
mobileNumber binding
  ↓
send_otp request
  ↓
OTP destination
  ↓
OTP screen
  ↓
authFlow.phoneNumber / challengeId context
  ↓
otp binding
  ↓
verify_otp request
```

## 7. Fixture policy

Backend-shaped JSON fixtures must represent the current DTO contract exactly.

Reusable fixtures are preferred over giant duplicated inline JSON.

Screen theme belongs at `data.theme`, not inside `data.template`.

Response navigation destination belongs at `data.destination`, not inside `data.template`.

Fixture changes must not weaken production contract tests.

## 8. Verification commands

Required local verification:

```
./gradlew.bat :data:jvmTest
./gradlew.bat :feature:dynamic:jvmTest
./gradlew.bat :data:compileKotlinJvm
./gradlew.bat :feature:dynamic:compileKotlinJvm
```

Then run the broader project test/build gates required by the project operating system.

Windows developer checkout remains the authoritative environment for Gradle verification.

## 9. Acceptance

The Dynamic implementation is accepted only when:

- source matches frozen architecture;
- DTO fixtures match the current contract;
- data tests are green;
- feature/dynamic tests are green;
- backend-shaped Bootstrap → Login → Send OTP → OTP → Verify flow is covered and green;
- no prohibited duplicate architecture exists;
- final documentation status is updated.

A test failure must be diagnosed from the contract/source before changing production code.
