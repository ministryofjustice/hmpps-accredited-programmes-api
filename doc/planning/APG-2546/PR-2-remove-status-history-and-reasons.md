# PR-2 — Remove `referralStatusHistory` + `referralStatusReasons` sections from SAR

> **Ticket:** APG-2546 • **Branch:** `APG-2546/remove-status-history-and-reasons`
> • **Est.:** 0.5 dev day • **Blocks:** nothing (independent of Q1/Q2)
> • **Depends on:** PR-1 merged (rebase off `main` after PR-1 lands so
> the fixture-snapshot diff stays comprehensible)

## Purpose

Removes two SAR sections in a single PR because they are structurally
coupled — `referralStatusReasons` is derived from
`referralStatusHistory` inside `SubjectAccessRequestService.kt`
(around line 134: `referralStatusHistory.mapNotNull { it.reason }
.distinctBy { it.code }...`). They cannot be removed independently
without leaving dead code.

Row references on Roxanne's DD spreadsheet:

- `referralStatusHistory` — rows 192–201 (all 10 fields flagged red)
- `referralStatusReasons` — rows 205–209 (all 5 fields flagged red)

## Prerequisites for a fresh agent

Read:

- `doc/planning/APG-2546-sar-field-removals.md` (§A rows 2 & 3, PR-2 detail)
- The PR-1 doc if you haven't done PR-1 — same shape, same test
  regeneration flow.

## Files to change

### 1. `src/main/kotlin/…/service/SubjectAccessRequestService.kt`

- Line 69 — delete
  `referralStatusHistory = referralStatusHistoryRepository.findByPrisonNumber(prn)`.
- `resolveStaffSurnames(...)` (~line 149) — delete the
  `referralStatusHistory` parameter and the matching call-site
  argument. This is the second parameter to shrink out of that
  function during APG-2546 (PR-1 removed `auditRecords`); the query
  count stays at 2-per-SAR unchanged.
- Lines 133 and 134 — delete both
  `referralStatusHistory = referralStatusHistory.toSarReferralStatusHistory(staffSurnames),`
  and
  `referralStatusReasons = referralStatusHistory.mapNotNull { it.reason }.distinctBy { it.code }...`
  from the `Content(...)` constructor call.
- Lines 199 and 200 — delete
  `referralStatusHistory: List<SarReferralStatusHistoryEntity>,`
  and
  `referralStatusReasons: List<SarReferralStatusReason>,`
  from the `Content` data class.
- Lines 322–342 — delete both `data class SarReferralStatusHistoryEntity(...)`
  and `data class SarReferralStatusReason(...)`.
- Delete both mappers:
  - `fun List<ReferralStatusHistoryEntity>.toSarReferralStatusHistory(...)` (~line 485)
  - `fun List<ReferralStatusReasonEntity>.toSarReferralStatusReason(...)` (~line 501)
- Constructor injection — remove
  `referralStatusHistoryRepository: ReferralStatusHistoryRepository,`.
- Imports — remove `ReferralStatusHistoryEntity`,
  `ReferralStatusHistoryRepository`, `ReferralStatusReasonEntity`.

### 2. `src/main/kotlin/…/domain/repository/ReferralStatusHistoryRepository.kt`

**Do NOT delete** `findByPrisonNumber`. As of 2026-08-03 the SAR
service was its only `src/main` caller (`ReferralStatusHistoryService`
uses `getAllByReferralIdOrderByStatusStartDateDesc` instead, not
`findByPrisonNumber`). That said:

- Confirm before touching:
  ```zsh
  grep -rn "findByPrisonNumber" src/main
  ```
- If SAR service really is the only caller, the method becomes dead
  after this PR. **Delete it in this PR** — otherwise it lingers as
  unused code.
- If any other caller has appeared since 2026-08-03, keep the method
  and note it in the PR description.

The point of the note here is: don't reflexively delete without the
grep, and don't reflexively keep either.

### 3. `src/main/resources/sar_template.mustache`

- Delete lines 172–208 — both the `<h2>Referral status history</h2>`
  block and the `<h2>Referral status reasons</h2>` block, including
  their empty-state branches.

### 4. `src/test/kotlin/…/integration/SarContractIntegrationTest.kt`

- Line 209 — delete the `persistenceHelper.createReferralStatusHistory(...)`
  seed call inside `setupTestData()`.
- Delete `REFERRAL_STATUS_HISTORY_ID` from the UUID constants block
  (lines 237–243).

### 5. `src/test/kotlin/…/service/SubjectAccessRequestServiceTest.kt`

- ~Line 236 — delete
  `every { referralStatusHistoryRepository.findByPrisonNumber(...) } returns …`.
- Lines 266 and 267 — delete
  `referralStatusHistory.size shouldBe …` and
  `referralStatusReasons.size shouldBe …` assertions.
- Around lines 335–337 — delete the block of field-level assertions
  against `referralStatusHistory[0]` / `referralStatusReasons[0]`.
- ~Line 355 — delete
  `verify { referralStatusHistoryRepository.findByPrisonNumber(...) }`.
- Top of file — remove the `referralStatusHistoryRepository` mock
  declaration, the constructor arg passed to the service under
  test, and the import.

### 6. `src/test/kotlin/…/integration/SubjectAccessRequestServiceIntegrationTest.kt`

**Not in the original plan — surfaced by PR-1.** Once the two
`Content` fields are removed in step 1, this file will fail to
compile because it asserts on `content.referralStatusHistory` and/or
`content.referralStatusReasons`. Mirror the PR-1 fix:

- Grep the file for `referralStatusHistory` and `referralStatusReasons`
  — delete every assertion, seed, and mock that references either.
- Delete any UUID constants, seed helpers, or imports that become
  unused as a result.
- The rest of the file (B1's "exactly 3 staff-repo calls per SAR"
  assertion, referrals seeding, etc.) stays.

## Snapshot regeneration

Same as PR-1:

```zsh
SAR_GENERATE_ACTUAL=true ./gradlew test \
  --tests '*SarContractIntegrationTest*' --rerun-tasks
./gradlew test --tests '*SarContractIntegrationTest*'
./gradlew test --tests '*SubjectAccessRequestServiceTest*'
```

Commit the regenerated `sar-api-response.json` and
`sar-expected-render-result.html`. `entity-schema.json` should not
change (we're deleting reads, not schema).

## Verification checklist

```zsh
grep -rn "referralStatusHistory\|referralStatusReasons" src/main   # zero
grep -rn "SarReferralStatusHistoryEntity\|SarReferralStatusReason" src/main   # zero
grep -rn "toSarReferralStatusHistory\|toSarReferralStatusReason" src/main   # zero
grep -rn "findByPrisonNumber" src/main   # only ReferralStatusHistoryRepository if kept; zero if deleted
./gradlew ktlintCheck test
```

## Non-obvious things

### 1. Two sections in one PR is deliberate

The alternative — remove `referralStatusReasons` first, then
`referralStatusHistory` — leaves an intermediate state where the
service still queries history rows just to throw the derived
`reasons` away. Reviewer confusion outweighs the small-PR benefit.

### 2. `ReferralStatusHistoryEntity` vs the DTO

The domain entity `ReferralStatusHistoryEntity` (in
`domain/entity/`) stays. Only the SAR-facing DTOs
(`SarReferralStatusHistoryEntity`, `SarReferralStatusReason`) go.
The naming clash between the entity and the SAR DTO is a
pre-existing quirk of the codebase and is fine to leave in place —
the DTOs are being removed anyway.

### 3. Repository method deletion is decoupled from DTO removal

If you can't confirm dead-repository status quickly, ship the PR
without deleting `findByPrisonNumber` and file a follow-up "cleanup
dead SAR repo methods" issue. Don't hold the OSAR-review win for
this cleanup.

## PR description template

```
APG-2546: remove referralStatusHistory + referralStatusReasons sections from SAR

Removes two coupled sections per OSAR round-2 review (Roxanne DD
spreadsheet rows 192–201 and 205–209 — all fields flagged red).

Coupling rationale: referralStatusReasons is derived in-service from
referralStatusHistory, so removing them together avoids an
intermediate state where we query history just to discard the
derived value.

Changes:
- Delete both fields from Content data class + Content(...) call
- Delete SarReferralStatusHistoryEntity + SarReferralStatusReason DTOs
- Delete the two mappers
- Drop ReferralStatusHistoryRepository from SubjectAccessRequestService
- Delete/keep ReferralStatusHistoryRepository.findByPrisonNumber
  based on grep of remaining callers (see PR body for decision)
- Delete both <h2> blocks from sar_template.mustache
- Delete seed, mock, and assertions from tests
- Regenerate SAR contract snapshots

resolveStaffSurnames is the second parameter shrink in APG-2546
(PR-1 removed auditRecords). Query count per SAR is unchanged.
```

## Definition of done

- [ ] Grep checks return zero hits in `src/main` for the removed types.
- [ ] `./gradlew ktlintCheck test` green.
- [ ] Snapshot diffs committed.
- [ ] PR description notes the `findByPrisonNumber` keep/delete
      decision explicitly.
- [ ] Sample PDF page count noted in the artefacts table.

