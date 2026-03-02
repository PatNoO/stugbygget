# Remote Config Keys (SB26)

Last updated: 2026-03-02

## Feature Flags

1. `feature_enable_ai_chat`
- Type: Boolean
- Default: `true`
- Owner: Product + AI feature owner
- Purpose: Enable/disable Stugan AI entry points.

2. `feature_enable_ar_measure`
- Type: Boolean
- Default: `true`
- Owner: AR feature owner
- Purpose: Enable/disable AR measurement module.

3. `feature_enable_live_price_ingestion`
- Type: Boolean
- Default: `false`
- Owner: Backend/platform owner
- Purpose: Enable/disable live external ingestion flow.

## Calculation Parameters

1. `calc_logistics_fuel_cost_per_km`
- Type: Double
- Default: `1.52`
- Intended range: `0.5` to `5.0`
- Owner: Logistics domain owner
- Purpose: Fuel cost baseline for self-transport cost model.

2. `calc_logistics_freight_rate_per_kg`
- Type: Double
- Default: `1.10`
- Intended range: `0.2` to `20.0`
- Owner: Logistics domain owner
- Purpose: Freight estimate rate per kg.

3. `calc_shopping_overspend_warning_threshold`
- Type: Double
- Default: `1.0`
- Intended range: `1.0` to `2.0`
- Owner: Budget domain owner
- Purpose: Multiplier threshold for overspend warning logic.

## Fallback Strategy

- If `fetchAndActivate()` fails, app uses local defaults from code.
- If remote values are unavailable, `getCached()` still resolves to defaults.
- No runtime crash should occur from missing Remote Config values.
