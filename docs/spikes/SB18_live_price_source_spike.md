# SB18 Spike: Reliable Live Price Sources (Swedish Building Stores)

Date: 2026-03-02  
Scope: Evaluate source strategy for Beijer, XL-Bygg, Bauhaus, Byggmax.

## Question
What is the most reliable and compliant strategy to collect live material prices and stock for StugBygget?

## Findings
1. Public, stable retailer price APIs are not clearly documented for the target stores.
2. Automated scraping has legal/compliance and maintenance risk, especially if it conflicts with site policies.
3. Aggregator APIs can reduce maintenance but may have coverage gaps, latency, and licensing limits.
4. Source reliability should be treated as per-store capability, not one global strategy.

## Evidence (Primary Links)
- Bauhaus robots policy: <https://www.bauhaus.se/robots.txt>
- Prisjakt public API reference: <https://www.prisjakt.nu/info/api>
- Google Merchant Center structured product feed standards: <https://support.google.com/merchants/answer/7052112?hl=en>

## Option Assessment

### Option A: Store-by-store scraping only
Pros:
- Fast to bootstrap if HTML is simple.
- No dependency on third-party APIs.

Cons:
- High maintenance when HTML changes.
- Higher legal/compliance risk.
- Potential anti-bot/rate-limit instability.

Verdict: Not recommended as primary strategy.

### Option B: Aggregator/API only
Pros:
- Lower parser maintenance.
- Potentially cleaner contracts.

Cons:
- Coverage/latency may not match all stores.
- Licensing constraints.
- May not include all stock metadata needed for logistics.

Verdict: Good where licensed and coverage is acceptable.

### Option C: Hybrid (recommended)
Pros:
- Best reliability by source fallback.
- Allows compliant preferred path per store.
- Reduces operational risk via adapter model.

Cons:
- Slightly more implementation complexity.
- Needs per-source observability and provenance.

Verdict: Recommended.

## Recommendation
Use a **hybrid ingestion architecture**:
1. Prefer official/licensed API feed per store (when available).
2. Use aggregator data only where terms allow and fields are sufficient.
3. Use scraping only as a controlled fallback with explicit legal approval and rate limits.
4. Persist source provenance for each price row (`sourceType`, `sourceName`, `fetchStatus`).

## Operational Requirements for SB19
1. Adapter interface per source.
2. Retry with exponential backoff and jitter.
3. Circuit breaker per source after repeated failures.
4. Dead-letter log for failed payloads.
5. Metric fields in Firestore: `fetchedAt`, `fetchLatencyMs`, `attemptCount`, `lastError`.

## Follow-up Ticket Adjustments
1. SB19 should explicitly include source adapters and provenance fields.
2. Add a legal/compliance checklist before enabling any scraper adapter in production.
