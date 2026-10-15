# Splash + Bootstrap

**Status:** CURRENT FEATURE CONTRACT
**Scope:** Startup UI, configuration bootstrap and handoff into Dynamic UI

## 1. Responsibility

Splash is the visible startup experience. Bootstrap is the startup configuration API flow executed while Splash is shown.

They are one startup flow, not two competing architectures.

## 2. Frozen boundary

```text
Platform Entry
  ↓
App / ApplicationContent
  ↓
Splash Store + UI
  ↓
BootstrapRepository
  ↓
BootstrapRepositoryImpl
  ↓
BootstrapRemoteDataSource
  ↓
RequestHandler / HttpClient
  ↓
Bootstrap DTO
  ↓
BootstrapMapper
  ↓
BootstrapOutput
  ↓
DynamicDestination
  ↓
DynamicNavigationState
```

**V2 W2 owner-approved reconciliation (2026-10-16):** remove the redundant `ApplicationBootstrap` forwarding interface and Koin adapter. The Domain `BootstrapRepository` is the sole startup acquisition contract; Data owns remote source/DTO/mapping/repository implementation; SplashStore owns startup policy and passes the canonical destination to app navigation. This explicitly supersedes the older Option B use-case wrapper chain, without altering wire contracts.

## 3. Bootstrap API

```text
GET /api/v1/partner/config/bootstrap
```

Known request metadata:

```text
X-CarBroz-Platform
X-CarBroz-App-Version
X-CarBroz-Build-Number
```

The backend-provided startup destination maps directly to the canonical `DynamicDestination`.

## 4. Canonical destination

Bootstrap, Dynamic API responses and Dynamic actions all use the same destination identity:

```text
screenId
templateId
templateType
endpoint
```

No second bootstrap-specific navigation model is permitted.

## 5. Legacy contracts

The following must not remain as parallel production contracts:

- `DefaultApplicationBootstrap` as a second direct HTTP path;
- `StartupConfig`;
- `NextScreenConfig`;
- standalone `BootstrapConfig`.

`BootstrapOutput` is the canonical bootstrap result.

## 6. Splash responsibility

Splash owns only startup presentation/state orchestration.

It must not:
- perform raw HTTP;
- construct Ktor clients;
- own Dynamic navigation;
- render Dynamic screen definitions;
- introduce authentication-specific state.

## 7. Bootstrap responsibility

Bootstrap owns startup configuration retrieval and mapping.

It must not:
- contain Login/OTP business behavior;
- infer business destinations from template types;
- create a second navigation stack;
- introduce Dynamic-specific networking;
- persist speculative offline configuration unless separately approved.

## 8. Current state

The foundational Splash/Bootstrap implementation exists and is integrated with the Dynamic destination boundary.

Remaining startup verification must be tracked in the Project Master State and the relevant implementation source/tests. Do not maintain a separate duplicate Splash status tracker.

## 9. Related authority

- Base architecture: `docs/architecture/BASE-ARCHITECTURE.md`
- Current architecture: `docs/architecture/ARCHITECTURE.md`
- Dynamic contract: `docs/dynamic/DYNAMIC-ARCHITECTURE.md`
- API integration: `docs/api/API_CONTRACT.md`
- Current state: `docs/project-management/04-PROJECT-MASTER-STATE.md`
