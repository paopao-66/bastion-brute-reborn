# Changelog

## [1.1.8] — 2026-05-31 — Piglin Brute respawn and drop fix
- Fixed Piglin Brute spawn position collision checks so valid standing positions are not rejected by the floor block.
- Kept respawn registration on server tick so it works on both dedicated servers and single-player integrated servers.
- Enforced Ancient Debris drops only for player kills, with Looting affecting chance only and successful drops fixed at 1 item.
- Added clearer spawn debug logging for dedicated vs integrated server checks and empty-player worlds.

## [0.0.0] — 2026-05-29 — Workflow Migration
- Added standardized AI workflow files.
- No code changes.
