# Test Log

## 2026-10-29 — v1.2.0 pre-publication optimization
- 本地无 JDK，未编译；依赖 CI 构建，需游戏内人工验证。
- Static review/checks: removed options and hardcoded height/stack traces absent; only non-loading four-argument getChunk remains.
- In-game checks pending: loaded-chunk boundaries, Nether roof, peaceful mode, spawn batches/caps, preferred-block fallback, drop item/count, wolf kills, and malformed configuration preservation.

## 2026-05-31 — Piglin Brute weapon fix
- Ran `.\gradlew.bat build`: passed.
- Manual in-game check still required to visually confirm respawned Piglin Brutes hold the axe.

## 2026-05-31 — Piglin Brute respawn and drop fix
- Ran `.\gradlew.bat compileJava`: passed.
- Ran `.\gradlew.bat build`: passed.
- Manual in-game single-player and dedicated-server checks still required.

## 2026-05-29 — Workflow Migration
No tests run. Project workflow migration in progress.
