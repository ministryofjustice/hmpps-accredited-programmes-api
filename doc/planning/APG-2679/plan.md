# APG-2679 — Exclude closed statuses from the Custody caselist by default

**Branch:** `APG-2679/exclude-closed-statuses-from-custody-caselist`
**Related PR (already open):** `APG-2679/fix-staff-lookup-500-on-duplicate-staff-id`
**Ticket:** APG-2679 / Incident INC4684438

> Planning document — implementation not started. Every claim here is
> anchored to a specific file and line number in this repo so the PR can
> be executed without further guess-work.

---

## 1. Problem statement

The Custody caselist shows referrals that are in a **closed** state. The
originating incident report was:

> _"There are two referrals for A2519CZ showing on the Custody service when
> only one was submitted. When clicking into either referral the user gets
> the 'something went wrong' error message."_

Investigation confirmed:

1. Both DB rows for A2519CZ are **legitimate** — they are the two rows
   left behind by a completed Building Choices transfer. See §2 for the
   evidence.
2. One of them (`MOVED_TO_BUILDING_CHOICES`) is flagged
   `closed = true` in the `referral_status` reference table
   (`src/main/resources/db/migration/V124__update_referral_status_for_building_choices.sql`, line 2)
   — meaning "no further action from this org's caselist perspective".
3. It should not appear on the Custody caselist. It does, because the
   caselist endpoint applies no default status filter when the UI passes
   none.
4. Clicking either row triggered a 500 — that click-through defect is
   fixed on the sibling branch
   `APG-2679/fix-staff-lookup-500-on-duplicate-staff-id`. This document
   only concerns the "row shouldn't be listed at all" defect.

### Blast radius

The following diagnostic query was run against prod on 2026-09-17 and
returned **3,447 rows** — i.e. 3,447 prisoners currently exhibit the
same two-row pattern (a `MOVED_TO_BUILDING_CHOICES` original plus a
downstream referral):

```sql
select r.prison_number,
       count(*) filter (where r.status = 'MOVED_TO_BUILDING_CHOICES') as bc_originals,
       count(*) filter (where r.original_referral_id is not null)     as bc_new_refs
from referral r
where r.deleted = false
group by r.prison_number
having count(*) filter (where r.status = 'MOVED_TO_BUILDING_CHOICES') > 0
order by bc_originals desc, r.prison_number;
```

The Custody caselist is therefore surfacing thousands of stale-looking
records today, not just A2519CZ.

---

## 2. Evidence for the diagnosis

### 2.1 Building Choices transfer leaves two rows by design

`ReferralService.transferReferralToBuildingChoices`
(`src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/ReferralService.kt`, lines 510–534):

```kotlin
fun transferReferralToBuildingChoices(transferReferralRequest: TransferReferralRequest): ReferralEntity? {
  val referral = getReferralById(transferReferralRequest.referralId) ?: throw NotFoundException(...)
  val newOffering = offeringRepository.findById(transferReferralRequest.offeringId).getOrElse { ... }
  val newReferral = createNewReferral(referral = referral, newOffering = newOffering)
  referralStatusHistoryService.createReferralHistory(newReferral)
  auditService.audit(newReferral, null, AuditAction.CREATE_REFERRAL.name)
  updateOriginalReferralStatusToBuildingChoices(referral, transferReferralRequest)
  ...
  return newReferral
}
```

`createNewReferral` (lines 554–569) inserts the **new** referral with
`originalReferralId = referral.id` and status
`REFERRAL_SUBMITTED`. `updateOriginalReferralStatusToBuildingChoices`
(lines 571–583) flips the **original** referral's status to
`MOVED_TO_BUILDING_CHOICES` and keeps its `submitted_on` untouched.

### 2.2 `MOVED_TO_BUILDING_CHOICES` is `closed = true`

`src/main/resources/db/migration/V124__update_referral_status_for_building_choices.sql`:

```sql
UPDATE referral_status set colour = 'grey' where code = 'MOVED_TO_BUILDING_CHOICES';
UPDATE referral_status set closed = true where code = 'MOVED_TO_BUILDING_CHOICES';
```

### 2.3 The service already knows which statuses are "open"

`ReferralService.getFilterStatuses`
(`src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/ReferralService.kt`, lines 372–400):

```kotlin
private fun getFilterStatuses(status: List<String>?, statusGroup: String?): List<String>? {
  val uppercaseStatuses = status?.map { it.uppercase() }?.toMutableList() ?: mutableListOf()
  val groupStatuses = statusGroup?.let { group ->
    when (group) {
      "closed" -> referralStatusRepository.findAllByActiveIsTrueAndClosedIsTrueOrderByDefaultOrder().map { it.code }
      "draft"  -> referralStatusRepository.findAllByActiveIsTrueAndDraftIsTrueOrderByDefaultOrder().map { it.code }
      "open"   -> referralStatusRepository.findAllByActiveIsTrueAndClosedIsFalseAndDraftIsFalseOrderByDefaultOrder().map { it.code }
      else     -> emptyList()
    }
  } ?: emptyList()
  ...
  return filteredStatuses.takeIf { it.isNotEmpty() }
}
```

When both inputs are null this returns null → the JPQL in
`ReferralViewRepository.getReferralsByOrganisationId`
(`domain/entity/view/ReferralViewEntity.kt`, lines 50–82) short-circuits
via `(:status IS NULL OR r.status IN :status)` and matches **every**
status, including `MOVED_TO_BUILDING_CHOICES`.

The same is true of `getReferralsByUsername` (lines 84–118) and
`getHspReferrals` (lines 126–155).

---

## 3. Non-goals

- Do **not** delete either DB row for A2519CZ (or any of the 3,447
  affected prisoners). Both rows are legitimate business data.
- Do **not** change the semantics of `MOVED_TO_BUILDING_CHOICES` or any
  other `referral_status` row.
- Do **not** touch the Refer / Assess UI in this PR. The UI change (if
  any) is scoped separately, see §7 "Rollout".
- Do **not** touch `/referrals/{id}` or its 500 → the sibling PR fixes
  that.
- Do **not** touch the HSP referrals dashboard (`getHspReferrals`) —
  scope is only the two "Custody / Refer dashboard" endpoints. HSP has
  its own domain-specific filters and different callers.

---

## 4. Approach — server-side default of `statusGroup = "open"`

### 4.1 Chosen approach

In `ReferralService.getFilterStatuses`, when **both** `status` and
`statusGroup` are `null`, treat the request as if
`statusGroup = "open"` was passed. Nothing else changes:

- Explicit `status=[…]` → unchanged.
- Explicit `statusGroup=closed` / `draft` / `open` → unchanged.
- Both explicit `status` and explicit `statusGroup` → unchanged
  (existing intersection logic on lines 390–397).

### 4.2 Why server-side rather than UI

| Criterion | Server-side default | UI-side default |
|---|---|---|
| Blast radius today (3,447 prisoners) | Fixed on deploy of the API | Requires UI release for every consumer |
| Consistency across future consumers | Every caller sees the safe default | Every new UI must remember to pass the param |
| Reversibility | New caller can opt out with explicit `statusGroup=closed` | Same |
| Cross-repo coordination | None | Refer UI + potentially Assess UI + Custody UI |
| Testing surface | One repo, integration test | Multiple repos |

### 4.3 Which endpoints change

| Endpoint | Change | Rationale |
|---|---|---|
| `GET /referrals/view/organisation/{organisationId}/dashboard` (`ReferralController.getReferralViewsByOrganisationId`, lines 576–650) | Default to `open` | This is the caselist the Custody incident is about. Closed referrals have no operational meaning at the org level. |
| `GET /referrals/view/me/dashboard` (`ReferralController.getReferralViewsByCurrentUser`, lines 465–533) | **Do not change default** in this PR | A user's own "my referrals" view might legitimately show drafts and completed items. Needs product input before changing. |
| `GET /referrals/view/hsp/dashboard` (`getHspReferrals` path, if wired up in controller) | Do not change | Explicit scope exclusion (§3). |

⚠️ **Open question** — this doc assumes the Custody caselist consumes
`getReferralViewsByOrganisationId`. That must be confirmed against the
UI repo before the PR is merged (see §7.1). If the Custody caselist
actually consumes `getReferralViewsByCurrentUser`, we defer the default
change to that endpoint instead.

### 4.4 Alternatives considered

1. **Add a new endpoint** e.g. `/referrals/view/organisation/{orgId}/custody-caselist`.
   - Pros: zero backward-compat risk.
   - Cons: duplicates ~60 lines of controller boilerplate; UIs still on
     the old endpoint keep the bug; more surface for future drift.
   - Verdict: **rejected** — the existing endpoint's default is simply
     wrong for its dominant use case; fix it in place.
2. **Server-side hard filter (ignore `statusGroup=closed` on this endpoint)**
   - Pros: prevents anyone ever seeing closed refs on this endpoint.
   - Cons: breaks legitimate historical / audit UIs that might want to
     opt in.
   - Verdict: **rejected** — closed referrals must remain reachable via
     explicit opt-in.
3. **UI-only fix.**
   - Pros: no API risk.
   - Cons: 3,447 prisoners affected today, requires UI release cycle,
     leaves the API contract with a footgun default forever.
   - Verdict: **rejected** — the primary defect is at the API layer.

---

## 5. Implementation checklist

### 5.1 Source change (1 file)

`src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/ReferralService.kt`

- [ ] In `getReferralViewByOrganisationId` (line 340), before calling
      `getFilterStatuses`, resolve the effective `statusGroup`:
      ```kotlin
      val effectiveStatusGroup = when {
        status.isNullOrEmpty() && statusGroup.isNullOrBlank() -> "open"
        else -> statusGroup
      }
      val uppercaseStatuses = getFilterStatuses(status, effectiveStatusGroup)
      ```
- [ ] Add a KDoc paragraph on `getReferralViewByOrganisationId`
      explaining the default and linking to APG-2679 for context.
- [ ] Leave `getFilterStatuses` **unchanged**. It stays a pure helper.
- [ ] Leave `getReferralViewByUsername` **unchanged** (see §4.3 & §7.1
      before broadening scope).

### 5.2 Tests (2 files)

`src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/ReferralServiceTest.kt`

- [ ] Add: `getReferralViewByOrganisationId defaults to open statuses when no status filters are supplied`
      → verifies the derived list passed to the repository excludes
      `MOVED_TO_BUILDING_CHOICES`.
- [ ] Add: `getReferralViewByOrganisationId honours an explicit statusGroup=closed`
      → passes `statusGroup="closed"` → asserts the derived list matches
      `findAllByActiveIsTrueAndClosedIsTrueOrderByDefaultOrder`.
- [ ] Add: `getReferralViewByOrganisationId honours an explicit status list`
      → passes `status=["MOVED_TO_BUILDING_CHOICES"]` and no
      `statusGroup` → asserts the derived list contains only that code.
- [ ] Add: `getReferralViewByOrganisationId honours an explicit status list combined with statusGroup=open`
      → asserts intersection semantics of the existing code path are
      preserved.

`src/test/kotlin/…/restapi/controller/ReferralControllerTest.kt` (or the
existing wiremock/integration test for this endpoint — look for a
`getReferralViewsByOrganisationId` test first)

- [ ] Add an integration test: seed one `REFERRAL_SUBMITTED` and one
      `MOVED_TO_BUILDING_CHOICES` referral for the same organisation,
      hit `GET /referrals/view/organisation/{orgId}/dashboard` without a
      `status`/`statusGroup`, assert only the submitted one comes back.
- [ ] Add the mirror test: same seed, request with
      `statusGroup=closed`, assert only the `MOVED_TO_BUILDING_CHOICES`
      one comes back.

### 5.3 Documentation

- [ ] Update the KDoc block on `getReferralViewsByOrganisationId` in
      `ReferralController.kt` (line 542+ `@Operation.description`) to
      note the new default:
      > Returns referrals whose status is currently "open" (not closed,
      > not draft) by default. Pass `statusGroup=closed` or an explicit
      > `status` list to include closed referrals.
- [ ] Note: this makes the OpenAPI description slightly more accurate;
      no breaking change to the schema.
- [ ] Add an entry to the top of this planning doc under a
      "Progress log" heading as each item is completed, so the PR
      description can link back to it.

### 5.4 What is deliberately **not** in scope

- Removing `MOVED_TO_BUILDING_CHOICES` referrals from `referral_view`.
  The view is used by additional consumers (Refer's "your referrals"
  dashboard etc.); filtering must happen at the query layer where the
  caller context is known.
- Any change to the sibling `staff` duplicate fix — that's on
  `APG-2679/fix-staff-lookup-500-on-duplicate-staff-id`.

---

## 6. Impact & backward-compatibility analysis

### 6.1 API contract

- No path change, no schema change, no new / removed parameters.
- Only the **default** response for
  `GET /referrals/view/organisation/{orgId}/dashboard` (with no filter
  params) changes: from "all statuses" to "open statuses".
- Any caller currently passing `status=…` or `statusGroup=…` is
  unaffected.

### 6.2 Known callers

The following callers **must** be verified before merge (see §7.1):

- `hmpps-accredited-programmes-ui` (Refer UI) — the "Custody caselist"
  page.
- Any Assess UI page that calls the by-organisation endpoint.
- The PACT contract file (search this repo for
  `build/pact/Accredited Programmes API.md` and any `.pact.json`
  fixtures) for expectations that assume "no filter = all statuses".

### 6.3 Data

- No data migration.
- No schema change.
- No touch on any `referral` row.

### 6.4 Performance

- The new default adds a call to
  `findAllByActiveIsTrueAndClosedIsFalseAndDraftIsFalseOrderByDefaultOrder`.
  Cheap (~20 rows, indexed by PK). Negligible.
- The generated JPQL is unchanged in shape; the `IN` list is just
  non-null for the default case.
- Overall caselist result set gets smaller — a net win for
  serialisation + payload size.

### 6.5 Security

- No auth changes.
- The endpoint is already `bearerAuth`-guarded (line 569).

---

## 7. Rollout & verification

### 7.1 Pre-merge checks (must complete before requesting review)

1. Confirm which UI actually powers the "Custody caselist" page by
   grepping the two UI repos for the
   `/referrals/view/organisation/…/dashboard` path and for the string
   "Custody":
   ```bash
   # In each UI repo checkout
   grep -R "view/organisation" --include='*.ts' --include='*.tsx' --include='*.njk'
   ```
   Document the finding in the PR description.
2. Confirm the UI currently passes no `statusGroup` on that call (i.e.
   this fix will actually take effect). If it already passes
   `statusGroup=open`, this PR becomes purely defensive — still worth
   landing.
3. Search this repo for PACT expectations targeting the endpoint and
   confirm none rely on a `MOVED_TO_BUILDING_CHOICES` result being
   present in the default response:
   ```bash
   grep -R "view/organisation" postman/ build/pact/ 2>/dev/null
   ```

### 7.2 Post-merge verification (App Insights, prod)

The following should all trend to zero after the deploy:

```kusto
requests
| where timestamp > ago(24h)
| where cloud_RoleName == 'hmpps-accredited-programmes-api'
| where name matches regex @"GET /referrals/view/organisation/.*/dashboard"
| project timestamp, url, resultCode, duration
| summarize p95_ms = percentile(duration, 95), count() by bin(timestamp, 1h)
```

Latency should drop slightly (smaller result sets). Sanity check by
picking a prison_number known to be in the affected set (any from
the 3,447-row query) and confirming the caselist no longer shows a
`MOVED_TO_BUILDING_CHOICES` row.

### 7.3 Rollback plan

- Revert the single-commit PR (`git revert <sha>`). Zero data risk
  because there is no migration.
- On revert, callers immediately go back to "no filter = all statuses"
  behaviour.

---

## 8. Risks & mitigations

| Risk | Likelihood | Mitigation |
|---|---|---|
| A UI page relies on the old default to render closed referrals in a "history" tab | Low–Medium | §7.1 pre-merge UI grep + PACT check. If found: that UI page starts passing `statusGroup=closed` explicitly (or a combined `status=[…]`). |
| Product actually wants closed referrals on the caselist for some workflow | Low | Confirmed in §2 that `MOVED_TO_BUILDING_CHOICES` is intentionally `closed=true`; showing it on an "active work" list has no operational meaning. Escalate to product only if §7.1 turns up a legitimate consumer. |
| A test in this repo asserts the old default | Low | The tests being added in §5.2 will surface any collision at green-CI time. Fix by updating the pre-existing assertion to pass `statusGroup=closed` explicitly. |
| Similar defect in `getReferralViewByUsername` | Confirmed exists | Deliberately out of scope (§4.3); tracked as a follow-up ticket linked from this PR. |

---

## 9. Follow-up work (not in this PR)

- Same server-side default for `/referrals/view/me/dashboard` once
  product confirms drafts should stay visible on the owner's own view
  (they very likely should) — this may become a one-line change to
  default to `"open,draft"` composition, or a no-op.
- `V###__dedupe_referral_view.sql` migration to rewrite the `staff`
  LEFT JOIN as `LEFT JOIN LATERAL … LIMIT 1`. Prevents subtle
  pagination bugs when POM `staff_id` is duplicated. Independent of
  this PR.
- One-off cleanup of duplicate `staff` rows + `UNIQUE(staff_id)`
  constraint. Depends on the dedupe view migration landing first so no
  in-flight query relies on the fan-out.
- Harden `ReferralTransformers.toApi` / `fetchCompleteReferralDataSetForId`
  so future incidents include the referral id + failing field in logs
  and produce a specific error code, not a generic 500.

---

## 10. Progress log

- [x] 2026-09-17 — Planning document written and reviewed.
- [ ] Pre-merge UI grep completed (§7.1).
- [ ] Source change landed (§5.1).
- [ ] Unit tests added (§5.2).
- [ ] Integration test added (§5.2).
- [ ] Controller KDoc updated (§5.3).
- [ ] Reviewed + merged.
- [ ] Prod verification (§7.2) complete.

