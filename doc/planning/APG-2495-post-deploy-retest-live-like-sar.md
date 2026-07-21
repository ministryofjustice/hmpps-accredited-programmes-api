# APG-2495 — Post-deploy retest of live-like SAR after APG-2492 pipeline

> **Status:** planning • **Depends on:** all three `APG-2492/*` PRs merged and deployed to **dev** and **preprod**. • **Blocks:** APG-2493, APG-2510 (do not start SAR-field additions until this passes).

## Purpose

APG-2492 landed as three PRs that fundamentally changed how the SAR
resolves staff identifiers to surnames:

1. **`APG-2492/staff-lookup-surname-resolution`** ✅ merged — introduced
   `StaffLookupService` + scalar surname queries returning
   `List<String>`.
2. **`APG-2492/index-staff-lookup-columns`** — adds V144 non-UNIQUE
   indexes on `staff.username` and `staff.staff_id`.
3. **`APG-2492/batch-staff-surname-lookups`** — collapses the O(rows)
   point-lookup pattern into two batch queries per SAR, adds
   `ORDER BY <key>, s.id` for deterministic winner selection when
   duplicates exist, and logs WARN when duplicates are seen.

APG-2495 is the **regression gate** that proves the above is behaving
correctly against realistic data before we start layering new SAR
fields on top (APG-2493 enrichment, APG-2510 staff.username removal).

## What is being verified

### A. Functional correctness

| # | Check | How |
|---|---|---|
| A1 | SAR endpoint for a subject with **no referrals** returns the same content as pre-APG-2492 (empty `staff`, empty referral collections). | `GET /subject-access-request?prn=<pnp with no referrals>` in dev. Diff against a captured pre-change response. |
| A2 | SAR for a subject with **one referral** returns surnames (not usernames / staffIds) in every place the old code emitted raw identifiers. | Pick a dev subject with a referral whose referrer has a resolvable staff row; assert surnames present on `referrals[0].referrerUsername`, `.primaryPomStaffSurname`, audit rows, status history. |
| A3 | SAR for a subject with **multiple referrals + participations** returns the same surname for the same identifier across all sections. | Cross-check: if `A2.referrer.username == B.auditUsername`, both should render the same surname. |
| A4 | SAR for a subject whose staff row is **missing** (e.g. old data) renders "No Data Held" via `optionalValue`, not a stack trace. | Manually null out a staff row in dev; hit SAR; confirm renders `No Data Held`. |
| A5 | The `referralStatusHistory[].username` field carries the surname (post-APG-2492), not the raw username. | Read one status-history row for a subject with a known-good `SecurityContextHolder` identity; assert surname. |

### B. Perf & query-count regression

| # | Check | How |
|---|---|---|
| B1 | Total DB queries for a SAR request drop from ~O(referrals + participations + audits + statusHistory) to a small constant. | Enable Hibernate `spring.jpa.show-sql=true` in dev; capture query log for a subject with ≥ 5 referrals + ≥ 10 participations + ≥ 5 audits; count `SELECT`s tagged against `staff`. **Expect ≤ 3 staff queries total** (1 by-username batch, 1 by-staff-id batch, 1 `findByPrisonNumber` for the `Content.staff` array). |
| B2 | SAR endpoint p95 latency in preprod is **≤ pre-APG-2492 baseline**. | App Insights: compare `p95(request-duration)` for `POST /subject-access-request` over the 24h post-deploy vs the 24h pre-deploy. |
| B3 | The two new V144 indexes are being used (not scanned). | `EXPLAIN` a live query against preprod: `EXPLAIN SELECT last_name FROM staff WHERE username = 'ARIVER';` — expect `Index Scan using idx_staff_username`, not `Seq Scan`. |

### C. Duplicate-visibility WARN log

The batch resolvers log a WARN when the same `username` / `staff_id`
maps to more than one staff row. This is the observability hook that
lets Ops catch data-quality issues before they cause weird SAR output.

| # | Check | How |
|---|---|---|
| C1 | If **no duplicates exist** in preprod staff data, no WARN is logged during a normal SAR request. | App Insights `traces` query for `severityLevel >= 2` and message contains `"Multiple staff rows found"` — **expect zero results** for the observation window. |
| C2 | If a duplicate **is** injected (dev only), a WARN is logged with the offending `username` / `staff_id` and the row count. | Insert a duplicate staff row for a test username in dev; hit SAR; grep dev application logs for the WARN line. |
| C3 | The *chosen* surname when a duplicate exists is deterministic across repeated requests (courtesy of `ORDER BY <key>, s.id`). | Hit SAR three times back-to-back for the C2 subject; diff the returned surnames — should be identical every time. |

### D. Contract & fixtures

| # | Check | How |
|---|---|---|
| D1 | `SarContractIntegrationTest` passes in CI on the deployed commit. | GitHub Actions run for the merge commit; expect green. |
| D2 | The HMPPS SAR aggregator's downstream consumer still renders the ACP block cleanly. | Trigger a full-service SAR (aggregator + ACP + other backends) against a preprod subject; visually inspect the rendered PDF. |

### E. Data pipeline sanity

| # | Check | How |
|---|---|---|
| E1 | The staff table row-count in preprod matches the number of unique staff identifiers seen in referrals + audits. Any drift indicates missing rows. | Simple SQL comparison run against preprod snapshot. |
| E2 | Confirm the number of usernames / staffIds with more than one matching staff row in preprod (baseline for the C1 expectation). | `SELECT username, COUNT(*) FROM staff GROUP BY username HAVING COUNT(*) > 1;` — record the number so C1's WARN expectation is calibrated. |

## Test environments

- **Dev** — full test suite (A1–A5, C1–C3, D1) on refreshed dev data.
- **Preprod** — B1–B3 perf and observability checks (D2 optional if we
  can coordinate with the aggregator team).
- **Prod** — post-release monitoring only: watch App Insights WARN
  count and p95 latency for 24h after cut, roll back if either regresses.

## Exit criteria

All of the following must hold before APG-2493 / APG-2510 can start:

- [ ] A1–A5 pass (functional).
- [ ] B1 shows staff-query count ≤ 3 per SAR.
- [ ] B2 shows p95 latency ≤ baseline.
- [ ] B3 confirms both V144 indexes are used.
- [ ] C1 baseline captured (expected WARN count in preprod, per E2).
- [ ] C2 + C3 confirm WARN logging + deterministic pick behave as
      designed.
- [ ] D1 CI green on the merge commit.
- [ ] D2 aggregator render sane (or scheduled to check with team).

## Rollback plan

If any exit criterion fails on preprod:

1. Revert `APG-2492/batch-staff-surname-lookups` first (safest — it's
   purely a code change, no schema).
2. If issues persist, revert `APG-2492/index-staff-lookup-columns`
   (V144 uses `CREATE INDEX IF NOT EXISTS`, so a revert migration is
   safe to write as `DROP INDEX IF EXISTS idx_staff_username;
   DROP INDEX IF EXISTS idx_staff_staff_id;`).
3. Last resort: revert `APG-2492/staff-lookup-surname-resolution` —
   this reverts the entire SAR staff-surname behaviour, so downstream
   consumers should be given notice first.

Each revert is a separate, small PR — no need for a coordinated
big-bang rollback.

## Artefacts to capture

Even if all checks pass, capture and paste into the ticket:

1. Query log from B1 (staff query count).
2. App Insights latency chart from B2 (before/after screenshots).
3. `EXPLAIN` output from B3 for both indexes.
4. E2's duplicate count so future retests have a baseline.
5. A short "signed off by Ops" note once D2 has been walked through
   with someone from OSAR.

## Deliverables

This is a **testing-only ticket** — no production code change lives
on this branch. Only artefacts:

- `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md` (this
  file), which grows a `## Results` section as checks are ticked off.
- Attach captured screenshots / query logs to the Jira ticket.
- Close the ticket only when every exit criterion above is met.

## Rough size

- Zero lines of production code
- ~2 hours end-to-end (dev checks: 30 min; preprod checks: 1h; write-up
  and screenshots: 30 min)

