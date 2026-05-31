# Context Summary

## Project
Minecraft Fabric mod. The mod provides a protected range mechanic to prevent certain mobs from spawning.

## Current Known Issue
Piglin Brutes still generate inside the configured protected range. The spawn-blocking logic is not correctly applying to Piglin Brutes specifically.

## Main Workflow Risks
- Broad repository scans waste tokens.
- Full-history prompts inflate costs.
- Repeated failed tool calls loop without guardrails.
- Modifying unrelated systems introduces regressions.

## Files Future Agents Should Read First
1. `tasks/current-task.md` — active task and constraints.
2. `docs/requirements.md` — project requirements and open questions.
3. `docs/decisions.md` — past design decisions.
4. `docs/workflow.md` — testing and debugging workflow.
5. `docs/test-log.md` — test results history.
6. `docs/changelog.md` — version history.
