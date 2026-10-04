# CB-Partner — Development Workflow

**Status:** FROZEN / CURRENT
**Last Updated:** 2026-10-04

## Master Feature Lifecycle

```text
FEATURE DISCUSSION
  Discuss → Research → Decide → Discussion Freeze
        ↓
IMPLEMENTATION PLAN
  Complete Plan → Plan Freeze
        ↓
IMPLEMENTATION
  Implement Completely → Test → Review → Feature Freeze
```

This is the current three-phase feature workflow. Do not create separate approval/freeze gates for individual implementation units.

## Phase 1 — Feature Discussion

Clarify behavior, constraints, assumptions, architecture boundaries and important edge cases. Research only what is required. Freeze the complete feature discussion once the required decisions are accepted.

## Phase 2 — Implementation Plan

Create one complete implementation contract covering objective, scope, non-goals, modules/files, dependencies, implementation sequence, tests, acceptance criteria, risks and documentation.

Plan freeze authorizes implementation.

## Phase 3 — Implementation

Implement the complete feature according to the frozen plan. Use internal implementation units only for sequencing. Do not stop for additional unit-level authorization.

Then run complete testing, review the complete feature and freeze it.

## Testing

Testing must cover the behavior required by the feature plan. A successful compile alone is not completion.

## Review

Review the complete implementation against requirements, frozen discussion, plan, architecture, tests, scope and final Git diff.

## Git Lifecycle

```text
working branch
   ↓
implementation
   ↓
test
   ↓
review
   ↓
feature freeze
   ↓
commit / PR
   ↓
development
   ↓
main when production-ready
```

## Official Branch Model

Permanent branches:

- `main` — production-ready code only.
- `development` — integration branch and source for new working branches.

Working branches:

- `architecture/<clear-name>`
- `feature/<clear-name>`
- `fix/<clear-name>`
- `hotfix/<clear-name>`

## Current Feature Branch

Current Dynamic implementation branch:

`feature/dynamic-ui`

Do not continue from stale feature branches without checking the current Tracker and Git state.

## Current Product Progression

Base Architecture is frozen through BASE-ARCH-017. Splash/Bootstrap foundation is implemented and integrated with the Dynamic destination contract. The current product vertical slice is Dynamic UI through the Login → OTP contract.

## Productivity Rule

After a feature discussion is frozen, move immediately to its complete implementation plan. After the plan is frozen, implement the complete feature. Do not create process overhead between implementation units.

## Recovery Rule

Use `AI_START_HERE.md`, the Project Master State, Current Status, Project Tracker and the relevant feature documents to recover state. Historical chat is not the workflow source of truth.
