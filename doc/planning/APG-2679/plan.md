# APG-2679 — Verify & (if needed) exclude closed statuses from the Custody caselist

**Branch:** `APG-2679/exclude-closed-statuses-from-custody-caselist`
**Related PR (already open):** `APG-2679/fix-staff-lookup-500-on-duplicate-staff-id`
**Ticket:** APG-2679 / Incident INC4684438
**Status:** ⚠️ **Verification pending** — do not implement §6 until §4 is complete.

> This planning document was rewritten on 2026-09-17 after inspecting
> `/Users/raby.whyte/code/hmpps-accredited-programmes-ui`. That
> inspection materially changed the plan: **neither UI relies on the
> API's default status filter, so a server-side default change would not
> alter any current user-visible behaviour.** See §3.
>
> Every claim in this document is anchored to a file + line number in
> either this repo or the UI repo.

---

## 1. Incident summary

> _"There are two referrals for A2519CZ showing on the Custody service
> when only one was submitted. When clicking into either referral the
> user gets the 'something went wrong' error message."_
> — INC4684438

Two independent code paths were suspected:

| # | Suspected defect | Status |
|---|---|---|
| A | Click-through 500 on `GET /referrals/{id}` | **Fixed** on sibling branch `APG-2679/fix-staff-lookup-500-on-duplicate-staff-id`. Root cause: `StaffRepository.findByStaffId` throwing `IncorrectResultSizeDataAccessException` when the POM's `staff_id` was duplicated (V144 documents this DQ issue). 54 exceptions/7d in App Insights (prod). |
| B | Two referrals showing when only one submitted | **Unverified** — see §3 & §4. |

## 2. What we actually observed in the DB

For prisoner `A2519CZ` in prod:

| Row | Status | `submitted_on` | `original_referral_id` |
|---|---|---|---|
| A | `MOVED_TO_BUILDING_CHOICES` | 2024-05-21 | `NULL` |
| B | `ON_HOLD_ASSESSMENT_STARTED` | 2025-04-07 | `<A>` |

Both rows are legitimate. They are the two rows left behind by a
completed Building Choices transfer:

- `ReferralService.transferReferralToBuildingChoices` (this repo, `service/ReferralService.kt:510–534`).
- `createNewReferral` (`service/ReferralService.kt:554–569`) inserts Row B with `originalReferralId = A.id`.
- `updateOriginalReferralStatusToBuildingChoices` (`service/ReferralService.kt:571–583`) flips Row A's status to `MOVED_TO_BUILDING_CHOICES`.
- `V124__update_referral_status_for_building_choices.sql:2` marks `MOVED_TO_BUILDING_CHOICES` as `closed = true`.

## 3. UI validation — the plan's original premise no longer holds

The API exposes a `statusGroup` query parameter on both dashboard
endpoints. When neither `status` nor `statusGroup` is passed,
`ReferralService.getFilterStatuses` (this repo, `service/ReferralService.kt:372–400`)
returns `null` and the JPQL `WHERE (:status IS NULL OR r.status IN :status)`
short-circuits to "all statuses". This was the original suspected
defect.

**However**, both consuming UIs always pass `statusGroup` explicitly:

- **Refer** (`hmpps-accredited-programmes-ui`, `server/controllers/refer/caseListController.ts:51–63`)
  iterates `referralStatusGroups = ['open', 'draft', 'closed']`
  (`server/@types/models/Referral.ts:24`) and passes
  `statusGroup: group` on every call to `getMyReferralViews`.
  Endpoint hit: `/referrals/view/me/dashboard`.
- **Assess** (`hmpps-accredited-programmes-ui`, `server/controllers/assess/caseListController.ts:76–114`)
  iterates `statusGroups: Array<ReferralStatusGroup> = ['open', 'closed']`
  and passes `statusGroup: group` on every call to `getReferralViews`.
  Endpoint hit: `/referrals/view/organisation/{orgId}/dashboard`.

Both dashboard client methods only forward `statusGroup` when the
caller supplies it (they use `...(query?.statusGroup && { statusGroup: query.statusGroup })`
spreads — `referralClient.ts:132–141` and `170–184`) — but every
in-repo caller always supplies it.

⇒ **The `no-filter` default of the API is not reachable from any known
UI today.** Changing that default server-side would fix nothing
user-visible on either UI.

## 4. Verification checklist — must complete before writing any code

- [ ] **4.1 Confirm which page the reporter of INC4684438 is looking at.**
      Ask for a screenshot or the URL path. Distinguish between:
      - Refer → *My referrals* (per referrer) — 3 tabs: open / draft / closed
      - Assess → *Programme team caselist* (per organisation) — 2 tabs: open / closed
      - Some other page (referral detail, "other referrals for this person", HSP dashboard)
- [ ] **4.2 Ask what "two referrals showing" means concretely.**
      Two rows on a **single** tab, or one row on open + one row on
      closed (i.e. the sub-nav badges say `Open: 1  Closed: 1`)?
- [ ] **4.3 Reproduce against preprod** (via the port-forwarded DB and
      a bearer token from `script/kubernetes-scripts/get-token -ns preprod`)
      hitting the by-organisation and by-me endpoints with the exact
      params the UI sends. Confirm whether closed rows appear on the
      "open" tab response.
- [ ] **4.4 If the sub-nav badges are the "two referrals":**
      This is expected & correct behaviour post-transfer. No code
      change; close the ticket with an explanation and rely on the
      sibling PR (click-through fix) to remove the error message the
      reporter also saw.
- [ ] **4.5 If two rows appear on a single tab:**
      Capture the exact JSON response and the two referral_ids, then
      proceed with §5 to diagnose which code path returned them.
- [ ] **4.6 If a page other than Refer/Assess caselist is involved:**
      Identify the endpoint, add it to §5, and restart scoping.

Do **not** proceed to §6 until §4 is done.

## 5. Hypotheses if §4.5 is confirmed (two rows on a single tab)

Ordered by likelihood, based on the code paths currently active:

1. **Duplicate raw SQL rows leaked past Hibernate dedup.**
   `referral_view` `LEFT JOIN staff st ON st.staff_id = r.primary_pom_staff_id`
   fans out because `staff` has duplicated rows (V144 documents this).
   Hibernate should dedup on `@Id referral_id`
   (`domain/entity/view/ReferralViewEntity.kt:19–21`), but `Pageable`'s
   `LIMIT/OFFSET` is applied to the raw SQL result BEFORE dedup — so on
   some page boundaries the same referral could appear on the page.
   Fix: `V###__dedupe_referral_view.sql` (LATERAL join, see §7
   follow-ups). Independent of any status filter.
2. **An unknown non-UI consumer is calling the endpoint without a
   `statusGroup`.** e.g. a Postman collection, a scheduled job, an
   internal debug UI, a Grafana panel. Then the server-side default
   IS reachable and IS misconfigured. Fix: §6.
3. **The `statusGroup` value being sent doesn't map to what we expect.**
   e.g. a translation bug where a status appears in more than one
   group. Sanity check: the three group queries in `getFilterStatuses`
   (`service/ReferralService.kt:381–386`) each filter cleanly on the
   `closed` and `draft` flags — no overlap by construction, so a
   status can only be in one group. Unlikely.

## 6. Implementation plan — only actionable if §5.2 (unknown non-UI consumer) is confirmed

### 6.1 Source change (1 file)

`src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/ReferralService.kt`

- Add a defensive default to `getReferralViewByOrganisationId`
  (line 340): when both `status` and `statusGroup` are unset, pass
  `statusGroup = "open"` into `getFilterStatuses`.
- Leave `getReferralViewByUsername` alone — Refer's "My referrals" page
  legitimately shows drafts by default.

### 6.2 Tests

`src/test/kotlin/…/service/ReferralServiceTest.kt`:

- Defaults to open statuses when no filters supplied (by-organisation endpoint).
- Honours explicit `statusGroup=closed`.
- Honours explicit `status` list (intersection semantics preserved).

`src/test/kotlin/…/restapi/controller/…` (or the wiremock/integration
test — locate the existing `getReferralViewsByOrganisationId` test first):

- Seed 1 submitted + 1 `MOVED_TO_BUILDING_CHOICES` for one org.
- Endpoint with no filters returns only the submitted one.
- Endpoint with `statusGroup=closed` returns only the closed one.

### 6.3 Documentation

- Update the `@Operation.description` on
  `getReferralViewsByOrganisationId` (`ReferralController.kt:542`) to
  document the new default.
- Update `Progress log` in this file (§10).

### 6.4 Deliberately out of scope

- Changing `getReferralViewByUsername` default — see §7.
- Deleting either DB row for A2519CZ.
- Changing the semantics of `MOVED_TO_BUILDING_CHOICES` or any other
  `referral_status` row.
- The HSP dashboard (`getHspReferrals`).

## 7. Follow-ups (independent of this ticket)

- **`V###__dedupe_referral_view.sql`** — rewrite the `staff` join in
  the DB view as
  `LEFT JOIN LATERAL (SELECT username FROM staff WHERE staff_id = r.primary_pom_staff_id ORDER BY id LIMIT 1) st ON true`.
  Kills the fan-out at source, fixes pagination edge cases, and drops
  needless work. Regression test seeds duplicate staff rows and
  confirms the row count is unchanged.
- **`V###__dedupe_staff_and_unique_staff_id.sql`** — one-off
  `DELETE ... WHERE ctid NOT IN (SELECT MIN(ctid) FROM staff GROUP BY staff_id)`
  keeping one row per `staff_id`, then
  `ALTER TABLE staff ADD CONSTRAINT staff_staff_id_key UNIQUE (staff_id);`.
  Query E confirmed the duplicates are byte-identical, so cleanup is
  safe.
- **Harden `ReferralTransformers.toApi` / `fetchCompleteReferralDataSetForId`**
  — replace `!!` with typed exceptions naming the referral_id and
  field; wrap external calls (PNI, staff) with error handling that
  logs enough context to diagnose from App Insights in one query.

## 8. Impact & backward compatibility (if §6 lands)

- API contract: no path change, no schema change. Only the *default*
  response of `GET /referrals/view/organisation/{orgId}/dashboard`
  (with no filter params) changes: from "all statuses" to "open
  statuses". Every current UI caller sends `statusGroup` explicitly
  and is therefore unaffected.
- Data: no migration.
- Performance: an extra reference-data lookup on the default path;
  negligible.
- PACT: search `postman/`, `build/pact/`, and any `.pact.json` under
  this repo before merge; if a contract asserts "no filter returns
  everything", update it or explicitly opt-in with `statusGroup=closed`.

## 9. Rollout & verification (if §6 lands)

Post-deploy sanity, prod, App Insights:

```kusto
requests
| where timestamp > ago(24h)
| where cloud_RoleName == 'hmpps-accredited-programmes-api'
| where name matches regex @"GET /referrals/view/organisation/.*/dashboard"
| summarize p95_ms = percentile(duration, 95), n = count() by bin(timestamp, 1h)
```

Latency should be flat (or fractionally lower). Manually verify one
prisoner from the 3,447-row set is no longer returning
`MOVED_TO_BUILDING_CHOICES` on the default call path.

Rollback: `git revert <sha>`. Zero data risk (no migration).

## 10. Progress log

- [x] 2026-09-17 — Draft planning doc written.
- [x] 2026-09-17 — Rewrite after UI-repo validation invalidated the
      original premise. Reduced from "implementation plan" to
      "verification-first plan".
- [ ] §4 verification with the reporter of INC4684438.
- [ ] §5 hypothesis chosen based on §4 outcome.
- [ ] If §5.2: §6 source change + tests.
- [ ] Prod verification (§9).

## 11. Appendix — commands run during validation

Used to build §3:

```bash
# UI repo callers of the two dashboard paths
grep -Rn "dashboardPath\|myDashboardPath" \
  /Users/raby.whyte/code/hmpps-accredited-programmes-ui/server --include='*.ts'

# Who calls each API client method
grep -Rn "findReferralViews\|findMyReferralViews" \
  /Users/raby.whyte/code/hmpps-accredited-programmes-ui/server --include='*.ts' \
  | grep -v '\.test\.ts' | grep -v referralClient.ts

# Definition of referralStatusGroups
grep -Rn "referralStatusGroups\s*=\s*\[" \
  /Users/raby.whyte/code/hmpps-accredited-programmes-ui/server --include='*.ts'
```

Prod data query (still valid — this is business background, not
implementation input):

```sql
select r.prison_number,
       count(*) filter (where r.status = 'MOVED_TO_BUILDING_CHOICES') as bc_originals,
       count(*) filter (where r.original_referral_id is not null)     as bc_new_refs
from referral r
where r.deleted = false
group by r.prison_number
having count(*) filter (where r.status = 'MOVED_TO_BUILDING_CHOICES') > 0
order by bc_originals desc, r.prison_number;
-- 3,447 rows (2026-09-17)
```

