# PR-3 — Remove `sexualOffenceDetails` + `selectedSexualOffenceDetails` sections from SAR

> **Ticket:** APG-2546 • **Branch:** `APG-2546/remove-sexual-offence-details`
> • **Est.:** 0.5 dev day • **Blocks:** nothing (independent of Q1/Q2)
> • **Depends on:** PR-2 merged (rebase off `main` after PR-2 lands)

## Purpose

Removes two coupled sections in one PR. `sexualOffenceDetails` is
derived from `selectedSexualOffenceDetails` inside
`SubjectAccessRequestService.kt` (~line 136), and the
`selectedSexualOffenceDetails` local (~lines 70–72) exists solely to
populate the two sections. All three go together.

Row references on Roxanne's DD spreadsheet:

- `sexualOffenceDetails` — rows 233, 234, 235, 237 (all fields
  flagged red — "After call with Raby 29.07 — this should be a no").
- `selectedSexualOffenceDetails` — not explicitly flagged by
  Roxanne, but its only purpose is to link `sexualOffenceDetails`
  back to referrals. Once `sexualOffenceDetails` goes,
  `selectedSexualOffenceDetails` is meaningless. Called out to
  Roxanne in the planning doc §C.

## Prerequisites for a fresh agent

Read `doc/planning/APG-2546-sar-field-removals.md` (§A row 4, §C, PR-3 detail).

## Files to change

### 1. `src/main/kotlin/…/service/SubjectAccessRequestService.kt`

- Lines 70–72 — delete the whole
  `selectedSexualOffenceDetails = filteredReferrals.flatMap { ... }`
  local variable assignment.
- Lines 135 and 136 — delete
  `selectedSexualOffenceDetails = selectedSexualOffenceDetails.toSarSelectedSexualOffenceDetails(),`
  and
  `sexualOffenceDetails = selectedSexualOffenceDetails.toSarSexualOffenceDetails(),`
  (or whatever the exact right-hand expressions are — search by LHS
  name) from the `Content(...)` constructor call.
- Lines 201 and 202 — delete
  `selectedSexualOffenceDetails: List<SarSelectedSexualOffenceDetails>,`
  and `sexualOffenceDetails: List<SarSexualOffenceDetails>,` from
  the `Content` data class.
- Lines 344–355 — delete both DTOs:
  `data class SarSelectedSexualOffenceDetails(...)` and
  `data class SarSexualOffenceDetails(...)`.
- Delete both mappers:
  - `toSarSelectedSexualOffenceDetails` (~line 511)
  - `toSarSexualOffenceDetails` (~line 519)
- Imports — remove `SelectedSexualOffenceDetailsEntity` and
  `SexualOffenceDetailsEntity` **only after** grepping to confirm
  neither is used elsewhere in this file (the `filteredReferrals`
  path may still touch them for other reasons — unlikely, but check).

### 2. Repositories

Neither `SelectedSexualOffenceDetailsRepository` nor
`SexualOffenceDetailsRepository` needs a signature change here —
`selectedSexualOffenceDetails` was populated from the referrals
graph, not via a dedicated SAR repo method. Confirm with:

```zsh
grep -rn "SexualOffenceDetailsRepository\|SelectedSexualOffenceDetailsRepository" src/main
```

If a repo method exists that was added purely for SAR and has no
other callers, consider deleting it as part of this PR. Otherwise
leave the repos alone.

### 3. `src/main/resources/sar_template.mustache`

- Delete lines 210–237 — both the `<h2>Sexual offence details</h2>`
  and `<h2>Selected sexual offence details</h2>` blocks, including
  empty-state branches.

### 4. `src/test/kotlin/…/integration/SarContractIntegrationTest.kt`

- Line 195 — delete `persistenceHelper.createSexualOffenceDetails(...)`.
- Line 204 — delete `persistenceHelper.createSelectedSexualOffenceDetails(...)`.
- Line 108 (top of `setupTestData()`) — delete the
  `persistenceHelper.deleteSexualOffenceDetails(SEXUAL_OFFENCE_ID)`
  teardown call.
- Delete `SEXUAL_OFFENCE_ID` and `SELECTED_SEXUAL_OFFENCE_ID` from
  the UUID constants block (lines 237–243).
- Delete `SexualOffenceDetailsEntity` and `SexualOffenceCategoryType`
  imports.

### 5. `src/test/kotlin/…/service/SubjectAccessRequestServiceTest.kt`

- ~Line 269 — delete
  `sexualOffenceDetails.size shouldBe 0` (or similar
  `sexualOffenceDetails` empty-list assertion — this test asserts
  the empty case).
- Grep the file for `selectedSexualOffenceDetails` and delete any
  associated assertions.
- No mock removal — these sections weren't sourced through a mocked
  repository at the unit-test layer (they came out of the referrals
  graph seeded via the integration test).

### 6. `src/test/kotlin/…/integration/SubjectAccessRequestServiceIntegrationTest.kt`

**Not in the original plan — surfaced by PR-1.** Once the two
`Content` fields are removed in step 1, this file will fail to
compile because it asserts on `content.sexualOffenceDetails` and/or
`content.selectedSexualOffenceDetails`. Mirror the PR-1 fix:

- Grep the file for `sexualOffenceDetails` and
  `selectedSexualOffenceDetails` — delete every assertion, seed, and
  helper that references either.
- Delete any UUID constants, seed helpers, or imports that become
  unused as a result.
- The rest of the file (B1's "exactly 3 staff-repo calls per SAR"
  assertion, referrals seeding, etc.) stays.

## Snapshot regeneration

```zsh
SAR_GENERATE_ACTUAL=true ./gradlew test \
  --tests '*SarContractIntegrationTest*' --rerun-tasks
./gradlew test --tests '*SarContractIntegrationTest*'
./gradlew test --tests '*SubjectAccessRequestServiceTest*'
```

Commit the regenerated snapshots.

## Verification checklist

```zsh
grep -rn "sexualOffenceDetails\|selectedSexualOffenceDetails" src/main   # zero
grep -rn "SarSexualOffenceDetails\|SarSelectedSexualOffenceDetails" src/main   # zero
grep -rn "toSarSexualOffenceDetails\|toSarSelectedSexualOffenceDetails" src/main   # zero
./gradlew ktlintCheck test
```

## Non-obvious things

### 1. `deleteSexualOffenceDetails` teardown

The teardown at line 108 exists because the shared test-DB state
was leaking sexual-offence rows between integration tests earlier
in the ticket's history. Once the seed goes, the teardown is dead —
delete both together. Do not delete the teardown alone in an
earlier PR; you'll get a compilation error when
`SEXUAL_OFFENCE_ID` is still referenced but the constant is gone.

### 2. `SexualOffenceDetailsEntity` and friends stay

The domain entities `SexualOffenceDetailsEntity`,
`SelectedSexualOffenceDetailsEntity`, and enum
`SexualOffenceCategoryType` remain — they're used by the referrals
domain (the courses/offering side, not SAR). Only the SAR-facing
DTOs and the SAR test-seed helpers go.

### 3. Roxanne's explicit sign-off on the coupled `selected` section

`selectedSexualOffenceDetails` was not on her red-flagged rows, but
the planning doc's §C explains why it must be removed at the same
time. If Roxanne pushes back on that in review, the fallback is:
keep `selectedSexualOffenceDetails` populated but empty in the SAR
response, and delete only `sexualOffenceDetails`. **Do not** do
this pre-emptively — it's ugly and she hasn't asked for it. Just
be ready to explain the coupling in the PR review.

## PR description template

```
APG-2546: remove sexualOffenceDetails + selectedSexualOffenceDetails sections from SAR

Removes the sexualOffenceDetails section (Roxanne DD spreadsheet
rows 233, 234, 235, 237 — all flagged red on 29.07) and the
coupled selectedSexualOffenceDetails section from the SAR payload.

Coupling rationale: selectedSexualOffenceDetails exists solely to
join referrals to sexualOffenceDetails. Once the latter goes, the
former has no reader. See planning doc §C.

Changes:
- Delete both fields from Content + Content(...) call
- Delete the selectedSexualOffenceDetails local (lines 70–72)
- Delete both DTOs and both mappers
- Delete both <h2> blocks from sar_template.mustache
- Delete seeds, teardown, UUID constants, imports from integration
  test
- Delete the empty-list assertion from the unit test
- Regenerate SAR contract snapshots

Domain entities SexualOffenceDetailsEntity,
SelectedSexualOffenceDetailsEntity, and enum
SexualOffenceCategoryType remain — they're used by referrals.
```

## Definition of done

- [ ] All grep checks return zero hits in `src/main`.
- [ ] `./gradlew ktlintCheck test` green.
- [ ] Snapshot diffs committed.
- [ ] PR description surfaces the `selected*` coupling to reviewers.
- [ ] Sample PDF page count noted in the artefacts table.

