# PR-11 — Remove top-level `staff[]` list (option (a))

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/remove-top-level-staff`
> • **Est.:** ½ dev day
> • **Sequencing:** must merge **after PR-8** (**merged 2026-08-17 as `b7b05283`** ✅), **PR-9** (**merged 2026-08-17 as `f8e04ab0`** ✅), and **PR-10** (**merged 2026-08-17 as `d710fa7f`** ✅). No parallel PRs to serialise against — PR-11 is the last of the four sibling PRs touching `SubjectAccessRequestService.kt` + `sar_template.mustache`.
> • **Status:** ready for execution — line refs re-verified against `origin/main @ d710fa7f`

## Purpose

Round-2 ask #5 (Deborah, 2026-08-13) — *"Add staff name field to the
referral rather than list separately so it is in context"*.

Raby clarified with Deborah (Slack, 2026-08-13): the referral already
carries `primaryPomStaffSurname` + `secondaryPomStaffSurname` inline
(added in PR-5). Deborah confirmed **option (a)** — just drop the
redundant top-level `staff[]` list, keep the inline surname fields.

## Scope (verified against `origin/main` @ `d710fa7f`, 2026-08-17 — post PR-8 + PR-9 + PR-10 merges)

**Product code — all inside `src/main/kotlin/.../service/SubjectAccessRequestService.kt`:**

- Delete `staff: List<SarStaff>` field from nested `data class Content(...)` — Content decl at line 150, `staff` field at line 154
- Delete `staff = staffRepository.findByPrisonNumber(prn).distinctBy { it.username }.map { it.toSarStaff() }` line in `Content(...)` construction — line 103
- Delete nested `data class SarStaff(...)` at line 218 — grep-verified: only used inside this service file
- Delete `.toSarStaff()` extension mapper at line 287 — same

**Template — `src/main/resources/sar_template.mustache`:**

- Delete top-level `<h2>Staff</h2>` block — lines 79-89 (block opens at 79, closes at 89 after the mustache `{{^staff}}`…`{{/staff}}` empty-state pair)

**Repositories — orphan-audit re-verified 2026-08-17 against `d710fa7f`:**

- `staffRepository.findByPrisonNumber` — **orphan-in-prod re-confirmed.** Only src/main caller on `d710fa7f`:
  - `SubjectAccessRequestService.kt:103` (SAR — going away in this PR)
  - Plus test-side callers in `SubjectAccessRequestServiceTest.kt` (going away with the mock cleanup below)
  - Note: `StaffLookupService.kt` and `StaffService.kt` reference the `StaffRepository` interface but use **different methods** (not `findByPrisonNumber`) — not blocking deletion of this specific query.
  - Also referenced in `V144__…` and `V145__…` SQL migration comments — SQL comments cost nothing, leave them.

  **Delete the query** along with its ORDER BY and KDoc. Sibling methods on `StaffRepository` (`findByStaffId`, `findLastNameByUsername`, `findLastNameByStaffId`, `findSurnamesByUsernames`, `findSurnamesByStaffIds`) stay untouched.

  **Repository-interface pattern:** since `StaffRepository` still has multiple in-use methods (unlike PR-8's `OasysPniResultEntityRepository` which was fully orphaned), the interface stays alive as-is — **NOT an empty-shell restoration**. Only the specific `findByPrisonNumber` method definition is deleted from the interface.

- `V145__add_staff_last_name_index.sql` — **STAYS.** Flyway forward-only. The index on `staff.last_name` costs nothing to leave in place even without a query that uses it.

**Test code — pre-verified 2026-08-17 against `d710fa7f`:**

`src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt`:

| Lines (`@ d710fa7f`) | Change |
|---|---|
| 172-… | Remove mock `every { staffRepository.findByPrisonNumber(prn) } returns listOf(...)` (StaffEntityFactory-built list — spot-check exact block end, mock opens at 172) |
| 197 | Remove `assertThat(staff).hasSize(1)` inside `with(result!!.content as SubjectAccessRequestService.Content) { … }` block |
| 246 | Remove `val staffMember = staff[0]` + adjacent `assertThat(staffMember.lastName).isEqualTo(...)` line |
| 253 | Remove `verify { staffRepository.findByPrisonNumber(prn) }` |

`src/test/kotlin/.../integration/SubjectAccessRequestServiceIntegrationTest.kt`:

- No test-code change (test doesn't reference `content.staff`; grep-verified). `persistenceHelper.createStaff(...)` seed at line 83 stays because it feeds `referral.primaryPomStaffId` which still gets exercised via the inline `primaryPomStaffSurname` field.

`src/test/kotlin/.../integration/SarContractIntegrationTest.kt`:

- Second-POM staff seed (`persistenceHelper.createStaff(...)` at lines 184-190 on `d710fa7f`) **stays** — still exercises `referral.secondaryPomStaffId` → inline `secondaryPomStaffSurname` field. Companion consts `SECONDARY_STAFF_ID` (line 208) and `SECONDARY_STAFF_ROW_ID` (line 209) stay.
- First `createStaff` seed at line 177 also stays (feeds `primaryPomStaffId`).
- `expectedFlywaySchemaVersion = "145"` at line 55 — **unchanged from `0cf89850`**, no migration this PR.
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

**Optional "while you're in the file" tidy** (flagged by PR-10
nine-lens self-review 2026-08-17): `sar_template.mustache` has a
cosmetic double-blank line between the Courses and Staff sections
— one line of trailing whitespace, harmlessly absorbed into the
goldens. Since PR-11 deletes the `<h2>Staff></h2>` block, this
double-blank may end up cleaned or worsened depending on where
exactly it sits. If cleaning it is a one-line no-op, do it in this
PR; otherwise leave for PR-12 hygiene. Not a blocker either way.

