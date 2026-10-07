# CB-Partner Documentation

This directory contains the authoritative documentation for the CB-Partner frontend.

## Authority and routing

Start here:

1. `../AI_START_HERE.md` — global AI/project router.
2. `project-management/04-PROJECT-MASTER-STATE.md` — single current-state and recovery record.
3. `project-management/03-DECISION-LOG.md` — durable accepted/frozen decisions.
4. Read the relevant architecture, feature, API, build, or engineering document for the task.

## Documentation structure

```text
docs/
├── README.md
├── api/
│   └── API_CONTRACT.md
├── architecture/
│   ├── ARCHITECTURE.md
│   ├── BASE-ARCHITECTURE.md
│   ├── BASE-ARCH-015-DECISION-RECORD.md
│   ├── BASE-ARCH-017-DECISION-RECORD.md
│   └── BASE-ARCH-017-IMPLEMENTATION-PLAN.md
├── build/
│   └── GRADLE-SETUP-AND-WORKFLOW.md
├── dynamic/
│   ├── DYNAMIC-ARCHITECTURE.md
│   ├── DYNAMIC-IMPLEMENTATION-PLAN.md
│   └── DYNAMIC-IMPLEMENTATION-STATUS.md
├── features/
│   ├── splash-bootstrap/
│   │   └── SPLASH-BOOTSTRAP.md
│   └── web-app/
│       └── WEB-APP.md
├── frontend/
│   └── FRONTEND.md
└── project-management/
    ├── 00-PROJECT-OPERATING-SYSTEM.md
    ├── 01-AI-CONTEXT.md
    ├── 02-AI-RULES.md
    ├── 03-DECISION-LOG.md
    ├── 04-PROJECT-MASTER-STATE.md
    ├── 05-ROADMAP.md
    ├── 06-TRELLO-WORKFLOW.md
    └── 07-DEVELOPMENT-WORKFLOW.md
```

## Single-responsibility rule

Each document has one authority:

| Topic | Authority |
|---|---|
| AI routing | `AI_START_HERE.md` |
| Project operating rules | `project-management/00-PROJECT-OPERATING-SYSTEM.md` |
| AI context | `project-management/01-AI-CONTEXT.md` |
| AI safety/rules | `project-management/02-AI-RULES.md` |
| Decisions | `project-management/03-DECISION-LOG.md` |
| Current state/recovery | `project-management/04-PROJECT-MASTER-STATE.md` |
| Roadmap | `project-management/05-ROADMAP.md` |
| Trello process | `project-management/06-TRELLO-WORKFLOW.md` |
| Development/Git/review workflow | `project-management/07-DEVELOPMENT-WORKFLOW.md` |
| High-level architecture | `architecture/ARCHITECTURE.md` |
| Frozen base architecture | `architecture/BASE-ARCHITECTURE.md` |
| Runtime infrastructure decision | `architecture/BASE-ARCH-015-DECISION-RECORD.md`, `BASE-ARCH-017-DECISION-RECORD.md` |
| Runtime infrastructure implementation contract | `architecture/BASE-ARCH-017-IMPLEMENTATION-PLAN.md` |
| Gradle/build setup | `build/GRADLE-SETUP-AND-WORKFLOW.md` |
| API integration | `api/API_CONTRACT.md` |
| Dynamic architecture | `dynamic/DYNAMIC-ARCHITECTURE.md` |
| Dynamic implementation contract | `dynamic/DYNAMIC-IMPLEMENTATION-PLAN.md` |
| Dynamic implementation state | `dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md` |
| Splash/bootstrap | `features/splash-bootstrap/SPLASH-BOOTSTRAP.md` |
| Web app | `features/web-app/WEB-APP.md` |
| Frontend engineering | `frontend/FRONTEND.md` |

## Rules

- Do not create a second document that restates an existing authority.
- Put decisions in the Decision Log, not in random feature notes.
- Put current state in Project Master State, not in multiple status trackers.
- Put implementation contracts in the relevant architecture/feature plan.
- Put implementation progress only in the relevant feature status document.
- Historical chat is not documentation authority.
- Superseded documents are removed rather than left as competing sources of truth.
