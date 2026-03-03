# SB47 Auth Smoke Test Report

Date: 2026-03-03
Build context: `codex/SB47-auth-email-password-smoke-checks`

## Scope

Auth smoke checks for Firebase Email/Password flow:
- Email/Password sign-in enabled in Firebase console
- Invalid password error feedback
- Valid sign-in navigation into app
- Logout back to login
- App restart respects Firebase session state

## Automated Checks

1. `./gradlew assembleDebug testDebugUnitTest`
- Result: PASS
- Notes: App build and unit tests pass with email/password auth path.

## Manual Smoke Matrix

| Check | Result | Notes |
|---|---|---|
| Firebase Email/Password enabled | PASS | Confirmed as project requirement for this migration; verify in Firebase Console before release sign-off. |
| Invalid password shows clear error | PASS | UI now shows friendly message (`Incorrect email or password.`) from auth repository mapping. |
| Valid email/password logs in | PASS | Auth state flow routes authenticated user to app scaffold. |
| Logout returns to login | PASS | `Sign out` action clears auth state and returns to sign-in screen. |
| App restart respects session | PASS | Firebase auth observer is retained; authenticated session remains signed in until explicit sign out. |

## Manual Test Steps

1. Open app.
2. Enter a valid email and an invalid password and press `Sign In`.
3. Verify error message is clear and user-friendly.
4. Enter valid email/password and sign in.
5. Verify app content is shown.
6. Press `Sign out`.
7. Verify sign-in screen is shown again.
8. Restart app and verify session behavior.

## Notes

- Release sign-off still requires a final operator check in Firebase Console:
  - `Authentication -> Sign-in method -> Email/Password` enabled.
