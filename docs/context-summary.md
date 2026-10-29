# Context Summary

## Project
Bastion Brute Reborn 1.2.0 is a Minecraft 1.21.10 Fabric mod providing Piglin Brute respawning near bastion remnants and configurable Ancient Debris drops. Requires Java 21, Fabric Loader >= 0.19.2, and Fabric API.

## Current Known Issue
No known issues currently identified. Compilation via CI and manual in-game verification are pending; runtime behavior has not yet been verified for 1.2.0.

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
