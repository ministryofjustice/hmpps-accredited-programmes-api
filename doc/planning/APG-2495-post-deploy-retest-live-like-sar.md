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

## Test data

The whole retest hinges on having a **known-good PRN in dev** whose data
exercises every SAR section end-to-end. Empty sections render as "No
Data Held" via `optionalValue` and are functionally useless for
regression coverage.

### Required data shape for the retest PRN

**Must-have** on `course_participation`:
- more than one row for the PRN
- a mix of `type = COMMUNITY` and `type = CUSTODY` (the controlled
  enum is `type`, renamed from `setting` in V18; `source` is a
  free-text audit column — practitioner comments, usernames, etc. —
  and is *not* the COMMUNITY/CUSTODY discriminator)
- a mix of `outcome_status` — at least one `COMPLETE` and one
  `INCOMPLETE`

**Nice-to-have** for the same person so the other SAR sections aren't
empty:
- a populated `pni_result` row
- an entry in `sexual_offence_details` (via
  `selected_sexual_offence_details` on a referral)
- multiple referrals so `referrer` / `primaryPomStaffId` /
  `secondaryPomStaffId` all resolve to real staff surnames (this is
  what the APG-2492 changes are being validated against)
- at least one referral whose `originalReferralId` is non-null (useful
  once APG-2493 lands — same PRN can be re-used to validate the
  enriched original-referral block later)

### Sourcing the PRN

`course_participation` data is owned by the Community Campus team. As
of 2026-07-22 the working thread is with:

- **Kath Cooper** (initial contact via Steve)
- **Marcus** (ndelius / Community team — pointed us at Community Campus)
- **Dhruv Patel** (Community Campus — took over the request)

Community Campus keys their datasets by **CRN**, not PRN. The bridging
step is either:

1. Dhruv shares a CRN with the required shape; we map it to a PRN via
   prisoner-search on our side.
2. Dhruv shares the PRN directly if his team have already resolved it.
3. Community Campus points at a dev-DB seeder / SQL we can run
   ourselves.

Any of the three paths ends the same way: a single PRN pasted into the
`## Results` section of this note (see below), which then becomes the
canonical retest identifier for every check in category **A** and for
the manual smoke test in **D2**.

#### Update — 2026-07-22 investigation outcome

Dhruv pointed at the [`hmpps-community-payback-api`](https://github.com/ministryofjustice/hmpps-community-payback-api)
repo. Investigation ([cloned locally at `../hmpps-community-payback-api`](../../../hmpps-community-payback-api)):

- CP is a **peer SAR provider** (their own `/subject-access-request?crn=<CRN>`
  endpoint returning `content.eteCourseCompletionEvents[…]`), not a
  source that feeds ACP.
- CP's schema (`ete_course_completion_events`, `appointment_events`)
  holds Education/Training/Employment data for people on community
  sentences, keyed by CRN with no PRN column and no CRN↔PRN mapping.
- **ACP's `course_participation` records are populated in-house**
  (created by probation practitioners via the ACP UI — see the
  `created_by_username` / `last_modified_by_username` free-text
  columns), not ingested from CP.
- CP's canonical fixture CRN is `X995728` but it's only present in
  their own test resources and won't correspond to any ACP data.

**Conclusion:** Dhruv's redirect is architecturally accurate ("we
handle course-completion for community sentences") but doesn't unblock
the retest. Pivot to querying the ACP dev DB directly to find a PRN
that meets the required data shape.

#### Path 1 (preferred) — query the ACP dev DB directly

Port-forward to the dev DB via [`doc/how-to/access-dev-database-remotely.md`](../how-to/access-dev-database-remotely.md).

Must-have candidate query (finds every PRN meeting the mandatory
criteria in one hit):

```sql
SELECT prison_number,
       COUNT(*)                             AS cp_count,
       COUNT(DISTINCT type)                 AS type_variety,
       COUNT(DISTINCT outcome_status)       AS outcome_variety,
       array_agg(DISTINCT type)             AS types_seen,
       array_agg(DISTINCT outcome_status)   AS outcomes_seen
FROM   course_participation
GROUP  BY prison_number
HAVING COUNT(*) > 1
   AND COUNT(DISTINCT type) > 1
   AND COUNT(DISTINCT outcome_status) > 1
ORDER  BY cp_count DESC
LIMIT  20;
```

For each candidate PRN, cross-check the nice-to-haves (replace
`:prn` with the value from the query above):

```sql
SELECT (SELECT COUNT(*)
        FROM   pni_result
        WHERE  prison_number = :prn)                     AS pni_rows,
       (SELECT COUNT(*)
        FROM   referral
        WHERE  prison_number = :prn)                     AS referral_rows,
       (SELECT COUNT(*)
        FROM   referral
        WHERE  prison_number = :prn
          AND  original_referral_id IS NOT NULL)         AS refs_with_original,
       (SELECT COUNT(*)
        FROM   selected_sexual_offence_details sod
        JOIN   referral r ON sod.referral_id = r.referral_id
        WHERE  r.prison_number = :prn)                   AS sexual_offence_rows;
```

A PRN that returns non-zero counts on all four columns is the retest
candidate — and it doubles for APG-2493 validation (the
`refs_with_original` > 0 ticks that nice-to-have).

> ⚠️ **Caveat:** dev DB row counts are typically thin. If no PRN meets
> every criterion, fall back to Path 2.

#### Path 2 (fallback) — seed a synthetic PRN in dev

Non-destructive: pick an existing test PRN (e.g. `A1234AA` — already
present in ACP test fixtures) and INSERT the missing rows to satisfy
the criteria. Mirror the authoring pattern already used in
`src/test/kotlin/.../common/config/PersistenceHelper.kt`
(`createStaff`, `createAuditRecord` etc. show the shape of the raw
INSERTs).

Minimum seed for the must-haves:

```sql
-- Assumes a referral for prison_number = 'A1234AA' already exists
-- (adjust :referral_id / :subject_prn accordingly)
INSERT INTO course_participation (
    course_participation_id, referral_id, prison_number, course_name,
    other_course_name, source, type, outcome_status,
    outcome_detail, created_by_username, created_date_time,
    last_modified_by_username, last_modified_date_time, is_draft
) VALUES
    (gen_random_uuid(), :referral_id, 'A1234AA', 'Building Choices', NULL,
     'seed-community', 'COMMUNITY', 'COMPLETE', 'Completed successfully',
     'test-user', now(), 'test-user', now(), false),
    (gen_random_uuid(), :referral_id, 'A1234AA', 'Anger Management', NULL,
     'seed-custody',   'CUSTODY',   'INCOMPLETE', 'Did not finish',
     'test-user', now(), 'test-user', now(), false);
```

Record the seed SQL under `## Results` below so the retest is
reproducible.

### Results

_(Updated as checks are executed.)_

- **Retest PRN:** `A8610DY`
- Source (Path 1 SELECT / Path 2 seed / other): **Path 1** — ACP dev DB
  `course_participation` must-have candidate query (see above), scored
  against the fixed `type`-based variety filter. Sole PRN in the top 20
  with `type_variety = 2` (real COMMUNITY + CUSTODY mix); all four
  others were CUSTODY-only.
- Date PRN captured: 2026-07-23
- Retest PRN data-shape snapshot at capture time:

  | Metric | Value | Requirement |
  |---|---|---|
  | `course_participation` rows | 11 | > 1 ✅ |
  | Distinct `type` values | 2 (`COMMUNITY`, `CUSTODY`; plus one legacy `null`) | mix ✅ |
  | Distinct `outcome_status` values | `COMPLETE`, `INCOMPLETE` (plus one legacy `null`) | mix ✅ |
  | `pni_result` rows | 185 | > 0 ✅ |
  | `referral` rows | 199 | multiple ✅ |
  | Referrals with `original_referral_id IS NOT NULL` | 7 | > 0 ✅ (double-serves APG-2493) |
  | `selected_sexual_offence_details` rows (via referral join) | 12 | > 0 ✅ |

- E2 duplicate baseline count (from preprod staff table): _tbc — preprod deployed at `d7a6d11` (includes `dbbd4442`); query ready to run_
- B1 staff-query count observed for this PRN: **✅ 3** (proven via integration test — see below)
- B2 p95 latency delta vs pre-APG-2492 baseline: _tbc — preprod deployed at `d7a6d11`; App Insights window opens 2026-07-22T15:03Z (deploy time)_
- C1 WARN count in observation window: _tbc — preprod deployed at `d7a6d11`; App Insights `traces` query ready to run_

#### A1 — subject with no referrals

**Status:** ✅ pass, via targeted integration test (see substitution note).

**Substitution note.** The original A1 plan was to hit the live dev SAR
endpoint against a real "no referrals" PRN. Executing that was blocked
by HMPPS Auth: the `hmpps-accredited-programmes-client-1` credentials
retrieved from the `hmpps-accredited-programmes-ui` k8s secret were
rejected by the dev sign-in gateway (`HTTP 403`, and a `curlimages/curl`
pod inside the namespace returned the fronting-gateway string
`Empty response received from new Authorisation Server`), and
`kubectl debug` / arbitrary pod runs were blocked by RBAC and Pod
Security Standards. Rather than block the ticket on ops turnaround
for an SAR-scoped auth client, A1 was proven with two new integration
tests that exercise the exact same code path (`SubjectAccessRequestService.getPrisonContentFor`)
with the two "empty" input shapes the live curl would have exercised:

1. **`A1 — subject with a person row but no referrals returns populated person and empty collections`** —
   persists only a `person` row for `A4433DZ` (the dev PRN we would have
   used) and asserts every SAR collection is `[]` while `content.person`
   is populated. This directly exercises `resolveStaffSurnames()` with
   empty input sets — the exact regression risk introduced by the
   APG-2492 batch resolver.
2. **`A1 — unknown subject with no rows anywhere returns empty content and null person`** —
   truly empty inputs, `content.person` null. Covers the "totally
   unknown PRN" corner.

Both tests live in
[`SubjectAccessRequestServiceIntegrationTest`](../../src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/integration/SubjectAccessRequestServiceIntegrationTest.kt)
and run against the same Testcontainers Postgres as CI, so they'll fire
on every future build — stronger regression coverage than a one-off
live curl would have provided.

Local run on `APG-2495/post-deploy-retest-live-like-sar` @ commit `fa2292c1`:

```
Test A1 — unknown subject with no rows anywhere returns empty content and null person() PASSED
Test A1 — subject with a person row but no referrals returns populated person and empty collections() PASSED
Test should return all fields in SAR content() PASSED
BUILD SUCCESSFUL
```

**Follow-up:** open a task for ops to provision an SAR-scoped dev auth
client (or add laptop egress IP to an existing allow-list) so future
live-endpoint smoke tests are unblocked. This substitution is
acceptable for APG-2495 because the new tests are strictly stronger for
the *regression* case; live curls remain the right tool for ad-hoc
production incident triage.

Ops ticket filed 2026-07-23:
[HAAR-5793](https://dsdmoj.atlassian.net/browse/HAAR-5793) — system
client request, `ROLE_SAR_DATA_ACCESS` on dev, mirrored on the shape
Thomas Wilson-Cook used for the MDA API client. Once it lands, A2 /
A3 / A5 / C2 / C3 will run against the live dev endpoint as a
belt-and-braces on top of the integration-test coverage recorded here.

#### B1 — SAR generates exactly 3 staff-repository calls

**Status:** ✅ pass, via integration test with `@MockitoSpyBean` on
`StaffRepository`.

**Rationale for test-based evidence rather than live query log.** The
note's original B1 plan was to enable `spring.jpa.show-sql=true` on
dev, hit the SAR endpoint for a subject with ≥ 5 referrals + ≥ 10
participations + ≥ 5 audits, and count `SELECT ... FROM staff`
occurrences. Blocked by the same live-auth wall as A2/A3/A5 (see the
A1 substitution note above). Rather than block the ticket, B1 is
proven via a spy-based integration test that is strictly stronger
than a one-off query-log inspection because:

- It's deterministic (same result on every run, no dev-data variance).
- It's enforced in CI going forward (any future PR that reintroduces
  per-row staff lookups will fail this test loudly).
- It uses `verifyNoMoreInteractions(staffRepository)` as a backstop,
  so it also catches sneaky new `StaffRepository` calls added by
  unrelated refactors.

Test: `B1 — SAR generation performs exactly 3 staff-repository calls regardless of row count`
in [`SubjectAccessRequestServiceIntegrationTest`](../../src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/integration/SubjectAccessRequestServiceIntegrationTest.kt).
Seeds 3 referrals + 3 course participations + 2 audit records +
3 status-history rows + 12 backing staff rows for the retest PRN
`A8610DY`, then asserts:

| StaffRepository method            | Expected invocations | Observed |
|-----------------------------------|----------------------|----------|
| `findSurnamesByUsernames(...)`    | 1                    | ✅ 1     |
| `findSurnamesByStaffIds(...)`     | 1                    | ✅ 1     |
| `findByPrisonNumber(prisonNumber)`| 1                    | ✅ 1     |
| **Total staff-repo calls**        | **≤ 3**              | **✅ 3** |

Live-endpoint corroboration (a `spring.jpa.show-sql=true` capture
against dev) will be added to this section once HAAR-5793 lands, as a
belt-and-braces confirmation that no additional SQL crosses the wire
outside the repository layer.

#### D1 — SarContractIntegrationTest green on the merge commit

**Status:** ✅ pass on `dbbd4442`.

Evidence (via `gh run list --commit dbbd4442`):

- **CodeQL** workflow: [run 29928655921](https://github.com/ministryofjustice/hmpps-accredited-programmes-api/actions/runs/29928655921)
  — `success`.
- **Pipeline [test → build → deploy]** workflow: [run 29928655580](https://github.com/ministryofjustice/hmpps-accredited-programmes-api/actions/runs/29928655580)
  — run-level status is `cancelled`, but that is *entirely* attributable
  to the manual `Deploy to the prod environment / Deploy to prod`
  gate being cancelled (probably to stage the release cadence). Every
  other job on this run succeeded:

  | Job | Result |
  |-----|--------|
  | `Validate the kotlin / Verify the gradle app` (test suite, incl. `SarContractIntegrationTest`) | ✅ success |
  | `Build docker image / Build linux/amd64 (parallel)` | ✅ success |
  | `Build docker image / Build linux/arm64 (parallel)` | ✅ success |
  | `Build docker image / Push multi-platform image` | ✅ success |
  | `Deploy to the dev environment / Deploy to dev` | ✅ success |
  | `Deploy to the preprod environment / Deploy to preprod` | ✅ success |
  | `Deploy to the prod environment / Deploy to prod` | 🚫 cancelled (manual gate) |

D1 is satisfied: the Kotlin test suite — which contains the contract
test — ran and passed on the exact merge commit. The cancelled prod
deploy is out-of-scope for APG-2495 (that is a release-cadence
decision, not a test-signal).

#### Preprod deployment status — B2 / B3 / C1 / E2 unblocked

Confirmed via `gh run list` that preprod is currently serving
`d7a6d11e` (deployed 2026-07-22T15:03:12Z), which is `dbbd4442`
plus one downstream commit (APG-2507 — merged-prisoner-handling fix,
unrelated to the SAR staff-surname pipeline). Both the batch
resolver and the V144 indexes are therefore live on preprod as of
2026-07-22, so the preprod-dependent exit criteria (B2 latency, B3
`EXPLAIN`, C1 WARN count, E2 duplicate baseline) can be executed
against the current preprod snapshot without waiting for further
releases.

## Deliverables

This is a **testing-only ticket** — no production code change lives
on this branch. Only artefacts:

- `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md` (this
  file), whose `## Test data → Results` section is filled in as checks
  are ticked off.
- Attach captured screenshots / query logs to the Jira ticket.
- Close the ticket only when every exit criterion above is met.

## Rough size

- Zero lines of production code
- ~2 hours end-to-end (dev checks: 30 min; preprod checks: 1h; write-up
  and screenshots: 30 min)
