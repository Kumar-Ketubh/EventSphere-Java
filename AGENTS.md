# AGENTS.md

This project uses **Kaizen**. Read this file first, then `docs/PRODUCT.md`. Keep this file short: only what an agent cannot work out from the code.

## Run, check, build

- Check everything: `kz check` (the project's tests, types, lint, build, and the look gate). Work is done only when it passes.
- Quick check: `kz check --fast`
- Run the app: <!-- fill in, e.g. `npm run dev` on http://localhost:5173 -->

## Layout

<!-- One line per top-level folder: what lives there. -->

## Rules

- Hard rules are enforced by hooks and permissions; see Kaizen's `core/RULES.md`. Don't try to work around a block. Stop and ask the person.
- Learned rules for this project are in `docs/RULES.md`.
- UI: use shadcn and Magic UI components and the theme tokens. Read `DESIGN.md` before touching UI.
- Record decisions that future agents must not undo in `docs/DECISIONS.md`.
