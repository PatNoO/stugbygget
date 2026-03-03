# Release Checklist (SB28)

Date: 2026-03-03
Last verified: 2026-03-03

## Security

- [x] No hardcoded production API secrets in source.
- [x] Runtime keys sourced from `local.properties` or Firebase services.
- [ ] Firestore security rule review completed for all new collections.
- [ ] Notification permission rationale UX reviewed.

## Configuration

- [x] Remote Config defaults implemented for feature flags and calc parameters.
- [x] Remote Config key/range ownership documented (`docs/remote_config_keys.md`).
- [x] Maps API fallback strategy defined for missing key/failure.
- [ ] Production Remote Config values verified in Firebase console.

## Performance / Reliability

- [x] Unit-test suite passing (`testDebugUnitTest`).
- [x] App compile/build passing (`assembleDebug`).
- [x] Offline read fallback + queued retry strategy implemented for core writes.
- [x] Retry/backoff strategy present in ingestion and offline queue layers.
- [ ] End-to-end UI instrumentation tests in CI pipeline.

## Release Readiness Decision

Current status: **Conditionally Ready** for controlled MVP testing.  
Required before broad release:
1. Security rule audit for newly added data paths.
2. CI instrumentation smoke lane.
3. Final production Firebase/Remote Config verification.
4. Deploy and validate callable AI proxy secret configuration (`CLAUDE_API_KEY`) in Firebase Functions.
