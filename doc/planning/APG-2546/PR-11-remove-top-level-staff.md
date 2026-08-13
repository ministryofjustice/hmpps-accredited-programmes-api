# PR-11 — Remove top-level `staff[]` list (option (a))

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/remove-top-level-staff`
> • **Est.:** ½ dev day • **Depends on:** PR-8 merged
> • **Status:** skeleton — expand before execution

## Purpose

Round-2 ask #5 (Deborah, 2026-08-13) — *"Add staff name field to the
referral rather than list separately so it is in context"*.

Raby clarified with Deborah (Slack, 2026-08-13): the referral already
carries `primaryPomStaffSurname` + `secondaryPomStaffSurname` inline
(added in PR-5). Deborah confirmed **option (a)** — just drop the
redundant top-level `staff[]` list, keep the inline surname fields.

## Scope

- Remove `staff: List<SarStaff>` field from `SarResponse` DTO
- Delete top-level `<h2>Staff</h2>` template block (lines 148–158)
- Delete `SarStaff` DTO class (orphan-audit first)
- Remove SAR service population statement + underlying
  `StaffRepository.findByPrisonNumber(...)` call
- **Orphan-audit `StaffRepository.findByPrisonNumber`** — if no
  non-SAR caller exists, delete the query. If it stays orphan-free,
  the V145 `idx_staff_last_name` index also becomes orphan but stays
  in the schema (Flyway forward-only; migration is additive +
  reversible, no operational cost to leave the index in place).
- Update fixture: `setupTestData()`'s second-POM staff seed added in
  PR #1115 partly stays useful (still exercises the inline surname
  fields on the referral) — decide whether to keep the second staff
  row or thin it out. Probably keep — it's cheap and the referral
  fields still exercise it.
- Regenerate snapshots

## Impact on PR #1115 hygiene fix

PR #1115 added `ORDER BY s.lastName, s.staffId` to
`StaffRepository.findByPrisonNumber`. If the query is deleted here,
that hygiene fix goes with it — which is fine, it was only in place
to make the (now-being-deleted) top-level `staff[]` collection
deterministic. Record in DELIVERY-LOG round-2 entry.

If the orphan-audit shows another non-SAR caller, leave the query +
its ORDER BY intact (it's cheap and now-deterministic behaviour
doesn't cost anyone).

## Verification checklist skeleton

- [ ] `grep -rn 'SarStaff\|\.staff\b' src/main | grep -v test` — expect zero SAR-DTO hits post-change
- [ ] `./gradlew ktlintCheck test` clean
- [ ] Snapshot regen: single-block deletion — top-level `staff[]` array in JSON, `<h2>Staff</h2>` block in HTML. The **inline** `primaryPomStaffSurname` / `secondaryPomStaffSurname` fields on each referral **stay** — sanity-check they're still present in the goldens.
- [ ] UUID-leak grep still 0
- [ ] `SarContractIntegrationTest.expectedFlywaySchemaVersion` unchanged from 145 (no new migration this PR)

## Notes for the agent

Do not touch V145. Flyway is forward-only; the index it added on
`staff.last_name` costs nothing to leave in place even if the query
that used it is gone.

