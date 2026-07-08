# Engineering Central (project instance)

This folder lives inside your **project** repo (as `tanii/central/`), copied from the Tanii OS scaffold. It stores only human-approved knowledge **derived from this project's real, shipped code** — never speculative or written upfront.

## Areas

Created on demand, when the first approved entry lands:

- `architecture-decisions/` — decisions with lasting consequences (ADRs).
- `lessons-learned/` — what went wrong or surprised you, and how to avoid it.
- `patterns/` — reusable approaches, recorded only after they repeat at least twice.

## Rules

- Keep knowledge in English, in Markdown.
- Store only what a human approved.
- Derive knowledge from approved code via the `make-library` skill; do not hand-author it upfront.
- Ephemeral session state belongs in `../WORKLOG.md`, not here.
- The reusable quality gate is the method's `ACCEPTANCE.md`, not a per-project copy.
- Treat what is here as the source of truth for future work on this project.
