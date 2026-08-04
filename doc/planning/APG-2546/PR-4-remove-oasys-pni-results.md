# PR-4 — Remove `oasysPniResults` from SAR (or strip its IDs)

> **Ticket:** APG-2546 • **Branch:** `APG-2546/strip-oasys-pni-result-ids`
> (Option B — confirmed 2026-08-04 pm)
> • **Est.:** 0.5 dev day • **Blocks:** nothing (Q1 answered)
> • **Depends on:** PR-3 merged (rebase off `main` after PR-3 lands)

## ✅ Q1 answered — execute Option B (corrected)

Roxanne confirmed in person on 2026-08-04 pm (see DELIVERY-LOG
"Roxanne in-person answers 2026-08-04 pm" entry). **Skip Option A
entirely.** Option A section retained below strictly for the
paper trail — do not execute.

Confirmed Option B scope: **strip `pniResultId` +
`oasysAssessmentId`, keep `prisonNumber` + `programmePathway`.**
Two-field surviving section, no ambiguity, no "default vs
definite" hedging.

## Purpose

Applies whichever of the two options Roxanne confirms in the Q1
follow-up (see `00-roxanne-followup.md`).

Row references on Roxanne's DD spreadsheet: rows 85, 86, 87, 88.

The `SarOasysPniResult` DTO currently exposes four fields:

| Field | Type | Role | DD row | DD signal |
|---|---|---|---|---|
| `pniResultId` | UUID | internal ID | 85 | 29.07 red-flag + "All IDs should be a No" → **strip** |
| `prisonNumber` | String | subject's business ID | 86 | 10.07 dev "should be on the report" + NOT red-flagged → **keep** |
| `oasysAssessmentId` | Long | external OASys ref | 87 | 10.07 dev "should be on the report" *and* row 85's blanket "All IDs should be a No" → **ambiguous** (default: strip, but flag to Roxanne) |
| `programmePathway` | String | category (e.g. `HIGH_INTENSITY_BC`) | 88 | 10.07 dev "should be on the report" + NOT red-flagged → **keep** |

**Option A** removes the whole section — the field-by-field
signals above are moot because nothing renders.

**Option B** — *scope refined 2026-08-04 pm after full DD notes
sweep* (see DELIVERY-LOG "DD notes sweep beyond red-flagged rows"
entry). Previous framing "strip all three IDs, keep only
programmePathway" was wrong: it counted `prisonNumber` as an
internal ID for removal, but the DD row 86 dev note explicitly
says to keep it, and every other SAR section retains
`prisonNumber` as the subject identifier. Real Option B is:

> **Strip `pniResultId` (definitely) and `oasysAssessmentId`
> (probably — resolve with Roxanne before final commit). Keep
> `prisonNumber` and `programmePathway`.**

If Roxanne comes back saying "keep `oasysAssessmentId` too",
that's fine — it's a one-line diff either way. Default position
is strip because it aligns with the ID-removal theme and row
85's "All IDs should be a No" was written as a blanket policy.

## Prerequisites for a fresh agent

**Q1 is answered.** Corrected Option B is what to execute. Do
not re-litigate A vs B — Roxanne closed it in person.

Read `doc/planning/APG-2546-sar-field-removals.md` (§A row 5, "Open
questions", PR-4 detail) for context, and the DELIVERY-LOG
"Roxanne in-person answers 2026-08-04 pm" timeline entry for
provenance.

## Option A — remove the whole section [SUPERSEDED, do NOT execute]

> **Status:** superseded by Roxanne's 2026-08-04 pm in-person
> answer confirming corrected Option B. Kept below strictly as
> historical / paper-trail — if you're a fresh agent picking up
> PR-4, skip to "Option B" further down.

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

### 6. `src/test/kotlin/…/integration/SubjectAccessRequestServiceIntegrationTest.kt`

**Not in the original plan — surfaced by PR-1.** Once
`Content.oasysPniResults` is removed in step 1, this file will fail
to compile if it asserts on `content.oasysPniResults`. Mirror the
PR-1 fix:

- Grep the file for `oasysPniResults` — delete every assertion,
  seed, and helper that references it.
- Delete any UUID constants, seed helpers, or imports that become
  unused as a result.
- The rest of the file (B1's "exactly 3 staff-repo calls per SAR"
  assertion, referrals seeding, etc.) stays.

**Option B skips this step** — Option B keeps `Content.oasysPniResults`
in place; only its DTO shape shrinks. Existing assertions on the
`Content` field continue to compile. But re-run the tests anyway to
catch anything that asserted on the fields that were stripped inside
the DTO.

## Option B — strip IDs, keep `prisonNumber` + `programmePathway`

*Scope refined 2026-08-04 pm — see the field table at the top of
this doc. Previous "strip all three IDs, keep only
programmePathway" wording was semantically wrong because
`prisonNumber` isn't an internal ID.*

This is close to what Roxanne was offered in the Q1 message
("keep `programme_pathway`, remove the ID fields") — with the DD
sweep clarifying that `prisonNumber` is not on the strip list
and `oasysAssessmentId` is ambiguous (default: strip).

The section wrapper (heading, table shell, empty-state branch)
stays; only the ID row(s) get removed from the DTO / template /
assertions.

### 1. `src/main/kotlin/…/service/SubjectAccessRequestService.kt`

- Lines 315–320 — inside `data class SarOasysPniResult(...)`,
  delete `pniResultId` (definite) and `oasysAssessmentId`
  (default). Keep `prisonNumber` and `programmePathway`.
- Inside `toSarOasysPniResult` mapper (~line 476) — delete the
  matching `pniResultId = …` (definite) and
  `oasysAssessmentId = …` (default) field assignments. Keep
  the `prisonNumber = …` and `programmePathway = …` assignments.
- Everything else in the service, repository, and constructor stays.

If Roxanne confirms `oasysAssessmentId` should also stay, the
diff is: don't delete it from the DTO, don't delete it from the
mapper, don't delete the corresponding template row / assertion.
One-liner divergence per surface.

### 2. Repository, template block, integration seed

- **Repository:** no change. `findAllByPrisonNumber` is still called
  for both SAR and `PersonService`.
- **Template** `src/main/resources/sar_template.mustache`, lines
  145–158 — delete the `<tr>` row rendering `{{pniResultId}}`
  (definite) and the `<tr>` row rendering `{{oasysAssessmentId}}`
  (default). Keep the section heading, table shell, empty-state
  branch, the `{{prisonNumber}}` row, and the `{{programmePathway}}`
  row.
- **Integration seed:** no change — the seed row still needs to
  exist, it just renders two fields instead of four (or three
  fields if Roxanne says keep `oasysAssessmentId`).

### 3. `src/test/kotlin/…/service/SubjectAccessRequestServiceTest.kt`

- Around line 332 — delete the `oasysPniResults[0].pniResultId
  shouldBe …` (definite) and `oasysPniResults[0].oasysAssessmentId
  shouldBe …` (default) assertions. Keep the collection-size,
  `prisonNumber`, and `programmePathway` assertions.

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
grep -rn "pniResultId\|prisonNumber\|oasysAssessmentId" src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt
# expect zero hits inside SarOasysPniResult DTO and toSarOasysPniResult
# mapper (other unrelated .prisonNumber reads in the file are fine
# — visual inspect)
grep -n "pniResultId\|prisonNumber\|oasysAssessmentId" src/main/resources/sar_template.mustache
# expect zero inside the {{#oasysPniResults}} block (lines 145–158)
# — other blocks may still render prisonNumber, that's fine
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

Strips pniResultId, prisonNumber, and oasysAssessmentId from the
oasysPniResults section per Roxanne's Q1 answer (Option B — "keep
programme_pathway, remove the three ID fields"). Retains only
programmePathway (a category label, not an ID — e.g.
HIGH_INTENSITY_BC) so the subject still sees which pathway their
OASys PNI result placed them on.

Changes:
- Remove the three ID fields from SarOasysPniResult DTO + mapper
- Remove the three ID rows from the OASys PNI results template
  block (keep the heading, table shell, and the programmePathway
  row)
- Remove the three ID assertions from the unit test
- Regenerate SAR contract snapshots

Not changed:
- OasysPniResultEntityRepository.findAllByPrisonNumber — still
  used by PersonService for the person-deletion cascade
- Integration test seed — still needed to populate the row that
  the section renders
```

## Definition of done

- [ ] Q1 answer recorded in `00-roxanne-followup.md` internal notes.
- [ ] Correct option's checklist run and green.
- [ ] `./gradlew ktlintCheck test` green.
- [ ] Snapshot diffs committed.
- [ ] Sample PDF page count noted in the artefacts table.

