# CB-Partner Dynamic UI — Discussion & Decisions

**Status:** ACTIVE DISCUSSION RECORD — decisions become authoritative only when explicitly marked FROZEN  
**Date:** 2026-10-06

This is the single active document for new Dynamic architectural discussion and decisions.

## 1. How to use this document

New Dynamic questions belong here.

Each topic follows:

```
DISCUSS → RESEARCH → DECIDE → FREEZE
```

When a decision is frozen, its final contract is copied/merged into `DYNAMIC-ARCHITECTURE-FROZEN.md` and this document retains only the decision history needed for context.

Historical wording never overrides the frozen architecture.

## 2. Already-frozen decisions

The following are closed and must not be reopened without an explicit new discussion:

- one permanent Dynamic Container;
- one four-field DynamicDestination;
- no empty template/instance stage;
- backend response supplies the actual template definition;
- explicit TemplateRegistry and ElementRegistry;
- typed concrete Dynamic UI models;
- generic DynamicValue at raw/dynamic boundaries;
- DynamicStore/DynamicScreenState as canonical runtime/binding state;
- DynamicActionExecutor as the single action-dispatch boundary;
- no ExecuteDynamicActionUseCase;
- LoadDynamicScreenUseCase as the screen-loading use case;
- request metadata separate from destination identity;
- direct navigate and request-generated destination converge on DynamicDestination;
- generic references `$literal`, `$binding`, `$context`, `$response`;
- existing :navigation owns application navigation mechanics;
- external URI remains separate from internal destination/navigation;
- no reflection/package scanning;
- no business-specific Dynamic architecture.

## 3. Current open topics

No new architectural decision is opened by this reconciliation.

Implementation/test findings that reveal a contract contradiction must be reported here rather than silently changing a frozen contract.

## 4. Decision record format

For each future topic use:

### DYNAMIC-XX — <topic>

**Status:** DISCUSSING / RESEARCHING / DECIDED / FROZEN

**Question**

What must be decided?

**Evidence**

Relevant source, backend contract, tests and prior frozen decisions.

**Options**

List only materially different options.

**Decision**

State the selected contract.

**Non-goals**

State what is explicitly not being changed.

**Freeze**

Record the final accepted rules and the date.

## 5. Conflict rule

If source, tests or documentation contradict a frozen decision:

1. identify the exact contradiction;
2. do not silently reinterpret the frozen contract;
3. determine whether source or documentation is stale;
4. if the architecture itself must change, stop and start a new discussion;
5. freeze the new decision before implementation.

This document must never become a second implementation-status journal.
