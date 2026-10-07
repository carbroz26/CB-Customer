# CB-Partner — AI Rules

**Status:** AUTHORITATIVE
**Scope:** AI work across the frontend repository

## Permission
- DISCUSS / RESEARCH / DECIDE: no source changes.
- PLAN: plan/documentation changes only unless explicitly authorized.
- IMPLEMENT: source changes only against an approved/frozen plan.
- TEST: verification and genuine fixes within the authorized scope.
- REVIEW: inspect and report; do not silently redesign.
- DOCUMENT: update durable records.
- STATUS: recover and report state.

## Mandatory preflight
1. Read `AI_START_HERE.md`.
2. Read `docs/project-management/04-PROJECT-MASTER-STATE.md`.
3. Read `docs/project-management/03-DECISION-LOG.md` for relevant decisions.
4. Read the relevant architecture/feature/build/API document.
5. Inspect the actual repository and Git state.

Never reconstruct current truth from historical chat when durable documentation exists.

## Frozen architecture rules
- No ViewModel.
- Pure Store/MVI/UDF.
- Features do not perform raw HTTP.
- Do not create a second navigation framework.
- Do not create business-specific Dynamic state namespaces.
- Do not create abstractions for hypothetical requirements.
- Do not add modules merely to make diagrams symmetrical.
- Do not add dependencies without a real consumer.
- Do not duplicate existing infrastructure boundaries.

## Dynamic safety
The frozen Dynamic contract is authoritative in `docs/dynamic/`.

Never introduce:
- `$response`;
- `previousResponse` or `previousResponseData`;
- persistent response state;
- business namespaces such as `authFlow.*`, `login.*`, or `otp.*`;
- a Dynamic-specific HttpClient;
- a Dynamic-specific navigation stack/cache;
- native Login/OTP/Dashboard architecture;
- template-type business navigation.

Sensitive values such as OTPs, credentials and tokens must never be logged by the frontend.

## Scope protection
Do not restart the project because one feature is broken, mix unrelated refactoring into an authorized feature, weaken tests, invent backend fields, silently change frozen decisions, merge unrelated branches, overwrite unknown user changes, or call a feature complete because it only compiles.

If a material deviation is required:

```text
STOP → REPORT → DISCUSS / RESEARCH → DECIDE → FREEZE → UPDATE PLAN → IMPLEMENT
```

## Git safety
Before source changes:

```powershell
git status
git branch --show-current
git diff --stat
```

Preserve local user work. Do not use destructive Git operations to resolve uncertainty.

After changes:

```powershell
git status
git diff --check
git diff --stat
git diff
```

Never silently merge, tag, rebase, force-push or clean unrelated work.

## Verification
- Claim only verification actually executed.
- Windows iOS simulator limitations are expected host limitations.
- Do not suppress meaningful failures.
- Keep fixtures aligned with frozen contracts.
- Fix genuine failures only inside the authorized scope.

## Documentation
One authority per topic:
- Decisions → `03-DECISION-LOG.md`
- Current state → `04-PROJECT-MASTER-STATE.md`
- Architecture → `docs/architecture/`
- Dynamic → `docs/dynamic/`
- Feature behavior → relevant feature document
- Build/version/setup → `docs/build/GRADLE-SETUP-AND-WORKFLOW.md`
- API integration → `docs/api/API_CONTRACT.md`

Do not create duplicate status trackers. Remove or clearly supersede stale documents.

## Stop conditions
Stop when required context is missing, documents materially conflict, a frozen decision must change, implementation authority is missing, unexpected user changes exist, a destructive Git operation is proposed, a dependency/module boundary must materially change, backend behavior would need to be invented, or scope expands beyond the approved plan.

## Completion states

```text
PLANNED → AUTHORIZED → IMPLEMENTED → VERIFIED → REVIEWED → FROZEN → MERGED → SYNCED
```

These states are distinct and must not be conflated.
