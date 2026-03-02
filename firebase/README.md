# Firebase Notes

## Rules
- Source of truth: `firebase/firestore.rules`

## Emulator Tests (planned)
Suggested command sequence:
1. `firebase emulators:start --only firestore`
2. Run rule tests for member/non-member/unauthenticated access.

The Kotlin app code assumes project-scoped data under `projects/{projectId}`.

## Functions (Price Ingestion)
- Location: `firebase/functions`
- Entry point: `firebase/functions/src/index.js`
- Scheduled function: `ingestPrices` (every 2 hours)

### Written Collections
- `projects/{projectId}/prices`
- `projects/{projectId}/price_ingestion_failures`

### Price Document Baseline
- `materialId`, `materialName`
- `store`, `amount`, `price`, `currency`
- `inStock`, `productUrl`
- `fetchedAt`
- `sourceType`, `sourceName`
- `fetchStatus`, `fetchLatencyMs`, `attemptCount`
