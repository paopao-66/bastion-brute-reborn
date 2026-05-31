# Workflow

## Normal Task Flow
1. Load `tasks/current-task.md` for active task.
2. Load `docs/context-summary.md` for project state.
3. Analyze using v4-flash unless escalated.
4. Use Codex CLI for bounded code edits only.
5. After any code attempt, update `docs/test-log.md` and `docs/changelog.md`.
6. Stop and report if 2 fix attempts fail.

## Escalation
- If the bug is hard or requires high-risk reasoning, escalate to v4-pro.
- If the task is unclear, ask the user — do not guess.

## Guardrails
- Max 12 LLM calls.
- Max 20 tool calls.
- Max 2 same-tool failures → stop.
- Max 2 same-strategy failures → stop.
- Do not scan the full repository unless approved.
