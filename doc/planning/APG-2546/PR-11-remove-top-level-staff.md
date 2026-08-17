# PR-11 — Remove top-level `staff[]` list (option (a))

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/remove-top-level-staff`
> • **Est.:** ½ dev day
> • **Sequencing:** must merge **after PR-8** (**merged 2026-08-17 as `b7b05283`** ✅), PR-9 and PR-10 (all three touch `SubjectAccessRequestService.kt` + `sar_template.mustache` — serial only, no parallel).
> • **Status:** skeleton — expand before execution
> • **⚠️ Line-ref re-anchoring required before execution.** Line references below are anchored to `origin/main @ 0cf89850` (pre-PR-8). PR-8's merge shifted downstream refs (e.g. `staff` field at Content L170 → shifted; `SarStaff` decl at L271 → shifted; `<h2>Staff>` at template L148 → shifted). PR-9 and PR-10 will also merge before this PR, causing further shifts. **Re-verify every line ref in this doc against the current `origin/main` HEAD before opening the PR**, using the pattern the PR-9 doc followed (git show `<HEAD>:<file>` + grep). Same rule as R6: fresh checkout of `origin/main` first.

## Purpose

Round-2 ask #5 (Deborah, 2026-08-13) — *"Add staff name field to the
referral rather than list separately so it is in context"*.

Raby clarified with Deborah (Slack, 2026-08-13): the referral already
carries `primaryPomStaffSurname` + `secondaryPomStaffSurname` inline
(added in PR-5). Deborah confirmed **option (a)** — just drop the
redundant top-level `staff[]` list, keep the inline surname fields.

## Scope (verified against `origin/main` @ `0cf89850`, 2026-08-13 pm)

**Product code — all inside `src/main/kotlin/.../service/SubjectAccessRequestService.kt`:**

- Delete `staff: List<SarStaff>` field from nested `data class Content(...)` (line 170 on main)
- Delete `staff = staffRepository.findByPrisonNumber(prn).distinctBy { it.username }.map { it.toSarStaff() }` line in `Content(...)` construction (line 115)
- Delete nested `data class SarStaff(...)` (line 271) — grep-verified: only used inside this service file
- Delete `.toSarStaff()` extension mapper — same

**Template — `src/main/resources/sar_template.mustache`:**

- Delete top-level `<h2>Staff</h2>` block (lines 148–158)

**Repositories — orphan-audit pre-verified 2026-08-13, result locked:**

- `staffRepository.findByPrisonNumber` (the SAR surname-sort `@Query` from PR #1115) — **orphan-in-prod confirmed.** Only callers on `origin/main`:
  - `SubjectAccessRequestService.kt:115` (SAR — going away in this PR)
  - `SubjectAccessRequestServiceTest.kt:216, 314` (test — going away with the mock cleanup)
  
  **Delete the query** along with its ORDER BY and KDoc. Sibling methods on `StaffRepository` (`findByStaffId`, `findLastNameByUsername`, `findLastNameByStaffId`, `findSurnamesByUsernames`, `findSurnamesByStaffIds`) stay untouched.

- `V145__add_staff_last_name_index.sql` — **STAYS.** Flyway forward-only. The index on `staff.last_name` costs nothing to leave in place even without a query that uses it.

**Test code — pre-verified 2026-08-13:**

`src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt`:

| Lines | Change |
|---|---|
| 216–224 | Remove mock `every { staffRepository.findByPrisonNumber(prn) } returns listOf(...)` (StaffEntityFactory-built list) |
| 244 | Remove `assertThat(staff).hasSize(1)` inside `with(result!!.content as SubjectAccessRequestService.Content) { … }` block |
| 299–300 | Remove `val staffMember = staff[0]` + `assertThat(staffMember.lastName).isEqualTo("River")` |
| 314 | Remove `verify { staffRepository.findByPrisonNumber(prn) }` |

`src/test/kotlin/.../integration/SubjectAccessRequestServiceIntegrationTest.kt`:

- No test-code change (test doesn't reference `content.staff`; grep-verified). `persistenceHelper.createStaff(...)` seed at line 103 stays because it feeds `referral.primaryPomStaffId` which still gets exercised via the inline `primaryPomStaffSurname` field.

`src/test/kotlin/.../integration/SarContractIntegrationTest.kt`:

- Second-POM staff seed (`persistenceHelper.createStaff(...)` at lines 219–225 on main) **stays** — still exercises `referral.secondaryPomStaffId` → inline `secondaryPomStaffSurname` field. Companion consts `SECONDARY_STAFF_ID` and `SECONDARY_STAFF_ROW_ID` stay.
- Snapshot goldens regenerated: removes top-level `staff[]` array in JSON + `<h2>Staff></h2>` block in HTML. Inline `primaryPomStaffSurname` / `secondaryPomStaffSurname` fields on each referral **stay** — sanity-check they're still present in the goldens.

## Impact on PR #1115 hygiene fix

PR #1115 added `ORDER BY s.lastName, s.staffId` to
`StaffRepository.findByPrisonNumber`. That hygiene fix goes with the
query in this PR — which is fine, it was only in place to make the
(now-being-deleted) top-level `staff[]` collection deterministic. Record
in DELIVERY-LOG round-2 entry.

V145 stays regardless (see above).

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

