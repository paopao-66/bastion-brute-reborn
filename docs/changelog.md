# Changelog

## [1.2.0] — 2026-10-29
- Restrict structure and spawn searches to loaded chunks; scan structures once per player.
- Limit spawn searches to the world's logical height, avoiding the Nether roof.
- Share spawn search and debug logging code; disable debug logs by default.
- Load/save UTF-8 configuration, preserve malformed files, log errors, and sanitize numeric settings.
- Implement configurable drop item/count, player-owned tamed wolf kills, and capped spawn batches; remove unused options.
- Use the difficulty enum for peaceful checks, remove the missing icon, and add a bilingual README.
- Refresh project context, task tracking, and validation notes.
- Replace the broken Gradle wrapper with the official 9.5.1 wrapper (jar now tracked), add a GitHub Actions build, and remove the empty build_out.txt.

## [1.1.9] — 2026-05-31 — Piglin Brute weapon fix
- Ensured respawned Piglin Brutes are equipped with a golden axe when spawned by the mod.

## [1.1.8] — 2026-05-31 — Piglin Brute respawn and drop fix
- Fixed Piglin Brute spawn position collision checks so valid standing positions are not rejected by the floor block.
- Kept respawn registration on server tick so it works on both dedicated servers and single-player integrated servers.
- Enforced Ancient Debris drops only for player kills, with Looting affecting chance only and successful drops fixed at 1 item.
- Added clearer spawn debug logging for dedicated vs integrated server checks and empty-player worlds.

## [0.0.0] — 2026-05-29 — Workflow Migration
- Added standardized AI workflow files.
- No code changes.
