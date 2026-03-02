# Offline Handling Strategy (SB27)

Last updated: 2026-03-02

## Covered Core Modules

- Planning (read path via Firestore local cache)
- Todos (read fallback + queued write retries)
- Shopping (read fallback + queued write retries)
- Measurements (queued write retries)

## Strategy

1. Connectivity monitor updates global sync state (`online` / `offline`).
2. Writes go through `OfflineSyncCoordinator.runOrQueue`.
3. If offline or write fails, write is queued in memory.
4. On connectivity restoration, queued writes are retried in order.

## UI Communication

- Main scaffold top banner shows:
  - Offline mode notice
  - Syncing state
  - Pending write count

## Current Scope Limits

- Queue is in-memory (not persisted across process death).
- Full offline parity for all modules is out of scope in SB27.
- Conflict resolution is last-write-wins via backend write order.
