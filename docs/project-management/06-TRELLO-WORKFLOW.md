# CB-Partner — Trello Workflow

**Status:** FROZEN  
**Scope:** Frontend Trello workspace only  
**Frozen:** 2026-09-22  
**Decision:** TRELLO-002 — Simple Frontend Work Board

## Purpose

Trello is the simple, human-readable execution board for the CB-Partner frontend.

It answers:

> **What work have we decided to do, what are we ready to do, what are we doing now, and what is finished?**

Trello contains actual project work only. It is not an architecture database, AI instruction system, decision log, implementation-plan repository, Git tracker, or document index.

Project documentation remains responsible for explaining **why, architecture, rules, decisions, constraints, and how the work must be implemented**.

## Authoritative Board

- Board: **CB-Partner — Frontend**
- Workspace: **CarBroz**
- URL: https://trello.com/b/QcuvaDbi/cb-partner-frontend

The previous **CarBroz Test** board is non-authoritative.

## Board Lists

**BACKLOG → TO DO → READY → IN PROGRESS → DONE**

### BACKLOG

Possible or future work that has not yet been committed to the project's planned work.

Move a card from BACKLOG to TO DO when the work becomes an approved part of the project plan.

### TO DO

The complete list of work that has been decided and is planned to be done.

TO DO is the project's human-readable planned-work list.

A card belongs here when:
- the work is real project work;
- the scope is understood sufficiently to describe it;
- the project has decided that the work should be done;
- it has not yet been prepared/selected for execution.

### READY

Work that is approved and prepared to start.

A READY card should have enough information and prerequisites for implementation to begin without inventing scope.

### IN PROGRESS

Work currently being performed.

Only work that is actually being worked on should be in this list.

### DONE

Work that has actually been completed and verified according to the applicable project process.

A card is not moved to DONE merely because someone started it or created files.

## Card Model

Every Trello card represents an **actual work item**.

Use:

**Card title → Description → Checklist/subtasks**

### Card title

Use a clear, human-readable name for the actual work.

Good:
- Set Up Gradle Project Structure
- Add Version Catalog
- Set Up Gradle Build Conventions
- Configure Kotlin Multiplatform Targets
- Set Up Dependency Injection
- Set Up Application Navigation

Do not use internal architecture IDs as titles.

Do not make cards named only after documents, decisions, workflow states, or AI processes.

### Description

Keep the description short.

It should explain:
- what is being done;
- what outcome the work should produce;
- optionally, a short definition of completion.

Do not put architecture history, AI instructions, Git commands, document references, or long implementation plans into the card.

### Checklist

Use the checklist for the concrete tasks/subtasks required to complete the card.

Example:

**Set Up Gradle Build Conventions**

Description:

> Create the reusable Gradle build conventions needed by the CB-Partner project.

Checklist:

- Create the build-logic included build
- Create Kotlin Multiplatform convention
- Create Compose Multiplatform convention
- Create Android application convention
- Create Desktop application convention
- Connect conventions to the version catalog
- Apply conventions to intended modules
- Verify Gradle configuration

## What Does Not Belong in Trello

Do not create Trello cards for:

- BASE-ARCH-001, BASE-ARCH-002, etc. as standalone work items;
- architecture decision records;
- research conclusions;
- AI routing/process documents;
- implementation-plan documents;
- Git branches;
- commits;
- pull requests;
- detailed architecture rules;
- internal class/file design;
- every small code change;
- every conversation;
- document maintenance by itself unless documentation work is itself a real project task.

Architecture decisions still control implementation, but they remain in the project documentation.

## Relationship Between Documentation and Trello

The systems have separate responsibilities:

**Project documentation**
- architecture;
- decisions;
- technical constraints;
- implementation contracts;
- process rules;
- detailed status/recovery information.

**Trello**
- actual work;
- planned work;
- execution state;
- task/subtask progress.

**GitHub**
- source code;
- branches;
- commits;
- pull requests;
- code review.

Trello does not need to contain links to all of these systems. The AI uses the authoritative project documents and GitHub state when executing the work.

## Work Planning Rule

When a project decision produces real implementation work, convert that work into a normal human-readable Trello task.

For example:

Architecture decision:
> Koin is the approved DI mechanism.

Trello work:
> **Set Up Dependency Injection**

Architecture decision:
> Navigation 3 is the approved navigation approach.

Trello work:
> **Set Up Application Navigation**

Architecture decision:
> Pure Store/MVI without ViewModel.

Trello work:
> **Implement Pure Store Foundation**

The architecture ID itself does not need to appear in the Trello card.

## Backlog vs TO DO

Use this distinction consistently:

**BACKLOG**
> We may do this later.

**TO DO**
> We have decided to do this.

This prevents the TO DO list from becoming a collection of ideas.

## Ready and Execution

Moving a card from TO DO to READY means the work has been sufficiently prepared.

Moving a card from READY to IN PROGRESS means the work has actually started.

Moving a card from IN PROGRESS to DONE means the work is actually complete and the applicable verification/acceptance requirements have been satisfied.

## Blocked Work

There is no BLOCKED list.

If work becomes blocked, keep the card in its current workflow list and clearly record the blocking issue in the card when useful. The detailed blocker and recovery state remain in the Project Master State.

## Labels

Labels are optional and should remain minimal.

Do not use labels to reproduce architecture states or AI workflow states.

Workflow state is represented by the list itself.

## AI Responsibility

The AI maintains Trello so that it reflects actual project work.

The AI may:
- create approved work cards;
- create and update task checklists;
- move cards as work progresses;
- keep task descriptions clear;
- mark completed work DONE after verification.

The AI must not:
- invent product work;
- silently expand task scope;
- use Trello to make architecture decisions;
- turn internal AI/process states into Trello cards;
- automatically start the next task after finishing one.

## Project Owner Authority

The project owner retains authority over:
- product scope;
- architecture decisions;
- what work is approved;
- implementation authorization;
- changing frozen decisions;
- accepting completed work.

## Current Board State

The board is organized as:

**BACKLOG → TO DO → READY → IN PROGRESS → DONE**

Current execution after the Trello model cleanup:

- **DONE:** Set Up Gradle Project Structure
- **DONE:** Add Version Catalog
- **READY:** Set Up Gradle Build Conventions
- **TO DO:** remaining approved foundation and architecture implementation work
- **BACKLOG:** future/uncommitted work

The old **Define Frontend Trello Workflow** card has been archived because the workflow itself is documentation/process, not an ongoing project task.

## Frozen Decision

**TRELLO-002 — Simple Frontend Work Board**

Approved by the project owner on 2026-09-22.

This decision supersedes the earlier TRELLO-001 card-oriented workflow model.

The simple rule is:

> **Trello tracks the work we are actually doing. Documentation explains why and how we do it.**

Changes to this Trello operating model require explicit project-owner approval.


## Documentation consolidation — 2026-10-07

Trello remains an execution board only. Current project truth is now maintained in one `docs/project-management/04-PROJECT-MASTER-STATE.md` document. Do not create Trello cards merely to reproduce documentation consolidation, decision records or AI workflow states.
