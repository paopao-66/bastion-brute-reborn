# Decisions

## 2026-05-29 — Workflow Migration
- Standardized AI workflow template applied.
- All future AI tasks must use `docs/context-summary.md` for project state, not chat history.
- Code edits go through Codex CLI only; Hermes orchestrates.
- Cost limits: 12 LLM calls, 20 tool calls per task.
