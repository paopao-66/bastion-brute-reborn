# AGENTS.md — Project Governance for AI Workflows

## Role Assignment
- **Hermes** is the orchestrator and memory layer.
- **v4-flash** is the default model for normal analysis and daily tasks.
- **v4-pro** is only for explicit escalation, hard bugs, or high-risk reasoning.
- **Codex CLI** is used only for bounded code edits, patches, diffs, and tests.

## Cost & Loop Safety Rules
- Max 12 LLM calls per normal task.
- Max 20 tool calls per normal task.
- Max 2 failures for the same tool, then stop.
- Max 2 failed attempts for the same strategy, then stop.
- Do not scan the full repository unless explicitly approved.
- Do not resend full project history.
- Prefer `docs/context-summary.md` over chat history.
- Prefer `tasks/current-task.md` over long prompts.
- Stop if continuing requires guessing user intent.
- Stop on permission denied, path encoding error, or same-tool failure halt.
- Do not claim tests passed unless they were actually run.

## Versioning Rules
- Every code update must include a version bump in the project metadata.
- Choose the bump size based on the update scope, for example `1.0.12` to `1.0.13` for small fixes and `1.0.12` to `1.1.0` for larger feature or behavior updates.
- When reporting the update, explain why that version bump was chosen.

## Cleanup Rules
- After an update is complete, old generated files or obsolete artifacts may be deleted if they are no longer needed.
- Deletions must still follow the repository safety rule: delete only one explicit file path at a time.
- If cleanup would require deleting many files or any directory, stop and ask the user to handle or explicitly confirm the cleanup path.
