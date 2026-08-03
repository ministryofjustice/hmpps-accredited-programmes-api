# PR-4 — Remove `oasysPniResults` from SAR (or strip its IDs)

> **Ticket:** APG-2546 • **Branch:** `APG-2546/remove-oasys-pni-results`
> (Option A) or `APG-2546/strip-oasys-pni-result-ids` (Option B)
> • **Est.:** 0.5 dev day • **Blocks:** Q1 answer from Roxanne
> • **Depends on:** PR-3 merged (rebase off `main` after PR-3 lands)

## Purpose

Applies whichever of the two options Roxanne confirms in the Q1
follow-up (see `00-roxanne-followup.md`).

Row references on Roxanne's DD spreadsheet: rows 85, 86, 87, 88.

The `SarOasysPniResult` DTO currently exposes four fields:

| Field | Type | Role |
|---|---|---|
| `pniResultId` | UUID | internal ID |
| `prisonNumber` | String | subject identifier |
| `oasysAssessmentId` | Long | internal / external ID |
| `programmePathway` | String | category (e.g. `HIGH_INTENSITY_BC`) |

**Both options remove the two internal IDs**; the difference is
whether `prisonNumber` and `programmePathway` remain visible.

## Prerequisites for a fresh agent

**Do not start this PR until Q1 is answered.** Check
`doc/planning/APG-2546/00-roxanne-followup.md` for the current state,
and confirm in `#osar-review` if uncertain.

If Q1 is answered Option A → follow the "Option A" branch below.
If Option B → follow the "Option B" branch below.

Read `doc/planning/APG-2546-sar-field-removals.md` (§A row 5, "Open
questions", PR-4 detail).

## Option A — remove the whole section

### 1. `src/main/kotlin/…/service/SubjectAccessRequestService.kt`

- Line 132 — delete
  `oasysPniResults = oasysPniResultEntityRepository.findAllByPrisonNumber(prn).toSarOasysPniResult(),`
  (or similar — search by LHS name) from the `Content(...)` call.
- Line 197 — delete
  `oasysPniResults: List<SarOasysPniResult>,` from the `Content`
  data class.
- Lines 315–320 — delete `data class SarOasysPniResult(...)`.
- Delete `toSarOasysPniResult` mapper (~line 476).
- Constructor injection — remove
  `oasysPniResultEntityRepository: OasysPniResultEntityRepository,`.
- Imports — remove `OasysPniResultEntity`,
  `OasysPniResultEntityRepository`.

### 2. `src/main/kotlin/…/domain/repository/OasysPniResultEntityRepository.kt`

**Do NOT delete `findAllByPrisonNumber`.** Verified 2026-08-03: it is
still called by `src/main/kotlin/…/service/PersonService.kt` (~line
287) as part of the person-deletion cascade. Deleting it will break
person deletion.

Confirm before touching:

```zsh
grep -rn "findAllByPrisonNumber" src/main
```

Expect at least one hit in `PersonService.kt` after your edits.

### 3. `src/main/resources/sar_template.mustache`

- Delete lines 145–158 — the `<h2>OASys PNI results</h2>` block
  including empty-state.

### 4. `src/test/kotlin/…/integration/SarContractIntegrationTest.kt`

- Line 179 — delete `persistenceHelper.createOasysPniResult(...)`.
- Delete `OASYS_PNI_RESULT_ID` from the UUID constants block
  (lines 237–243).

### 5. `src/test/kotlin/…/service/SubjectAccessRequestServiceTest.kt`

- ~Line 265 — delete `oasysPniResults.size shouldBe …` assertion.
- ~Line 332 — delete the block of `oasysPniResults[0].xxx shouldBe …`
  field assertions.
- No mock removal needed if `oasysPniResults` was sourced through
  the integration seed rather than a mock — grep to confirm.

## Option B — strip only the two internal IDs

Keep the section, remove `pniResultId` and `oasysAssessmentId`.
Leaves `prisonNumber` + `programmePathway` visible to the subject.

### 1. `src/main/kotlin/…/service/SubjectAccessRequestService.kt`

- Lines 315–320 — inside `data class SarOasysPniResult(...)`, delete
  the `pniResultId` and `oasysAssessmentId` parameters.
- Inside `toSarOasysPniResult` mapper (~line 476) — delete the
  `pniResultId = …` and `oasysAssessmentId = …` field assignments.
- Everything else in the service, repository, and constructor stays.

### 2. Repository, template block, integration seed

- **Repository:** no change. `findAllByPrisonNumber` is still called
  for both SAR and PersonService.
- **Template** `src/main/resources/sar_template.mustache`, lines
  145–158 — delete the two `<tr><td>…</td>{{pniResultId}}…</tr>` and
  `<tr><td>…</td>{{oasysAssessmentId}}…</tr>` rows inside the OASys
  PNI results table. Leave the section heading, table shell, and
  the `prisonNumber` + `programmePathway` rows alone.
- **Integration seed:** no change — the seed row still needs to
  exist, it just renders two fewer fields.

### 3. `src/test/kotlin/…/service/SubjectAccessRequestServiceTest.kt`

- Around line 332 — delete only the `oasysPniResults[0].pniResultId
  shouldBe …` and `oasysPniResults[0].oasysAssessmentId shouldBe …`
  assertions. Keep the size and the `prisonNumber` /
  `programmePathway` assertions.

## Snapshot regeneration (both options)

```zsh
SAR_GENERATE_ACTUAL=true ./gradlew test \
  --tests '*SarContractIntegrationTest*' --rerun-tasks
./gradlew test --tests '*SarContractIntegrationTest*'
./gradlew test --tests '*SubjectAccessRequestServiceTest*'
```

Snapshot diff will be dramatically smaller for Option B than
Option A — mention this in the PR description so reviewers know
what to expect.

## Verification checklist

### Option A

```zsh
grep -rn "oasysPniResults\|SarOasysPniResult\|toSarOasysPniResult" src/main   # zero
grep -rn "OasysPniResultEntityRepository" src/main   # only inside PersonService (+ the repo itself)
grep -rn "findAllByPrisonNumber" src/main   # still present in PersonService
./gradlew ktlintCheck test
```

### Option B

```zsh
grep -rn "pniResultId\|oasysAssessmentId" src/main/kotlin/…/service/SubjectAccessRequestService.kt
# expect zero — both removed from DTO and mapper
grep -rn "pniResultId\|oasysAssessmentId" src/main/resources/sar_template.mustache
# expect zero
./gradlew ktlintCheck test
```

## Non-obvious things

### 1. `findAllByPrisonNumber` is genuinely shared

This is the one repository method in APG-2546 whose SAR caller is
not the only caller. Do not treat it like `AuditRepository.getSarAuditRecords`
(PR-1) or `ReferralStatusHistoryRepository.findByPrisonNumber` (PR-2,
sole caller) — those are dead after their respective PRs, this is
not. The planning doc had originally said "consider deleting if
verified dead" — that guidance was **wrong** and has been corrected.

### 2. `programmePathway` is not an ID

Roxanne's "all IDs should be a No" rule (29.07 call) does **not**
extend to `programmePathway` — that's a category label, human
readable, meaningful to the subject. This is exactly why Option B
exists as a possibility, and why the follow-up asks her to pick
between A and B rather than assuming.

### 3. If Q1 is answered but Roxanne changes her mind post-merge

Rollback plan: revert the PR. Both options are pure deletes /
DTO strips — no schema state, no data migrations. Reverting brings
back the fixture, the DTO shape, and the mapper.

## PR description templates

### Option A

```
APG-2546: remove oasysPniResults section from SAR

Removes the oasysPniResults section per Roxanne's Q1 answer
(Option A — full removal). See DD spreadsheet rows 85–88 and
doc/planning/APG-2546/00-roxanne-followup.md.

Changes:
- Delete oasysPniResults from Content + Content(...) call
- Delete SarOasysPniResult DTO + toSarOasysPniResult mapper
- Drop OasysPniResultEntityRepository from SubjectAccessRequestService
- Delete <h2>OASys PNI results</h2> block from sar_template.mustache
- Delete seed, UUID constant, assertions from tests
- Regenerate SAR contract snapshots

Not changed:
- OasysPniResultEntityRepository.findAllByPrisonNumber — still used
  by PersonService for the person-deletion cascade (verified via
  grep). Do not delete.
```

### Option B

```
APG-2546: strip internal IDs from oasysPniResults section in SAR

Removes pniResultId and oasysAssessmentId from the oasysPniResults
section per Roxanne's Q1 answer (Option B). Retains prisonNumber
and programmePathway (a category label, not an ID) so the subject
still sees which pathway their OASys PNI result placed them on.

Changes:
- Remove the two ID fields from SarOasysPniResult DTO + mapper
- Remove the two ID rows from the OASys PNI results template block
- Remove the two ID assertions from the unit test
- Regenerate SAR contract snapshots (small diff — two fields per row)

Repository, seed, section heading, empty-state branch all unchanged.
```

## Definition of done

- [ ] Q1 answer recorded in `00-roxanne-followup.md` internal notes.
- [ ] Correct option's checklist run and green.
- [ ] `./gradlew ktlintCheck test` green.
- [ ] Snapshot diffs committed.
- [ ] Sample PDF page count noted in the artefacts table.

