# APG-2546 — SAR field removals from OSAR round-2 review

> **Status:** planning • **Depends on:** APG-2492 / APG-2493 / APG-2510 (all merged) • **Blocks:** OSAR sign-off for the round-2 review • **Ticket:** APG-2546

## Origin of this work

Following the round-1 OSAR review (dev PDF handed over 2026-07-24 —
see `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`), QAT
came back with two workstreams (Slack, 30 Jul):

1. **Content changes** — remove several fields / whole sections from
   the SAR payload the aggregator will render into an OSAR PDF.
2. **Appearance changes** — cover-sheet / dev-portal artefacts,
   currently blocked on the aggregator team's stuck dev pipeline
   (kicked to `#haa-sar-functionality-change-request`; out of scope
   for this branch).

Content changes were captured by Roxanne on the data-dictionary
spreadsheet held locally at
`doc/2026.07.08_copy_Probation Digital Data review December 251.xlsx`
(sheet `Accredited Programmes Custody`, not committed to the repo).
This branch turns her red-flagged rows into a concrete PR plan.

**Working estimate:** ~3 weeks (2–4 range communicated to William
Falconer 30 Jul). Reduced by fact that all changes are section-level
deletes rather than schema-level additions.

## How Roxanne's spreadsheet was read

Roxanne left comments in red font on column I (Additional Notes) and
occasionally column H (In SAR API - Y/N). Reading chronologically:

- Older notes reflect an earlier state (often *"H changed to Yes"*
  after the 01/07 or 10/07 dev call).
- **Post-2026-07-29 notes ("After call with Raby") override earlier
  ones**. The current instruction is whichever was written last.
- Sections where every field carries *"After call with Raby 29.07 —
  this should be a no"* → the whole section is being removed, not
  just individual fields.

Full parse output cached at `/tmp/dd-flagged.txt` (see
`/tmp/read_dd2.py` for the extractor if the spreadsheet changes).

## Removal decisions

Grouped by scope class, with row references so we can defend each
decision back at Roxanne.

### A. Whole SAR sections to delete

Each of these removes one line from `Content` (in
`SubjectAccessRequestService.kt`), one DTO, one `to<Section>` mapper,
one `{{#section}}...{{/section}}` block from the mustache template,
one seed call in `SarContractIntegrationTest.setupTestData()`, and
the matching assertions from `SubjectAccessRequestServiceTest`.

| # | Section | DTO | Fields flagged | Roxanne rows |
|---|---|---|---|---|
| 1 | `auditRecords` | `SarAuditRecord` | all 9 | 22, 23, 24, 25, 27, 28, 29, 30, 31 |
| 2 | `referralStatusHistory` | `SarReferralStatusHistoryEntity` | all 10 | 192–201 |
| 3 | `referralStatusReasons` | `SarReferralStatusReason` | all 5 | 205–209 |
| 4 | `sexualOffenceDetails` | `SarSexualOffenceDetails` | all 4 | 233, 234, 235, 237 |
| 5 | `oasysPniResults` | `SarOasysPniResult` | all 4 | 85, 86, 87, 88 (⚠ clarification pending — see below) |

### B. Internal ID fields to strip from remaining sections

| Section | Field(s) to remove from DTO | Roxanne rows |
|---|---|---|
| `SarPerson` | `id` | 111 |
| `SarOrganisation` | `id` | 105 |
| `SarPniResult` | `pniResultId`, `referralId`, `oasysAssessmentId` | 127, 128, 131 |

`SarPerson.id` and `SarOrganisation.id` are currently populated —
they'll go from the DTO, the mapper, the template, and the snapshots.
`SarPniResult` doesn't currently expose `pniResultId` / `referralId`
/ `oasysAssessmentId` (verified against the DTO at
`SubjectAccessRequestService.kt` lines 284–295) — the fields Roxanne
flagged there are already absent. This PR is therefore a snapshot /
data-dictionary-comment tidy-up on the `SarPniResult` side, not a
code change.

### C. Coupled removals (need to happen at the same time as A/B)

- `selectedSexualOffenceDetails` (whole section) — not explicitly
  flagged by Roxanne but its only purpose is to link
  `sexualOffenceDetails` back to referrals. Once `sexualOffenceDetails`
  goes, `selectedSexualOffenceDetails` is meaningless. Removing both
  in the same PR (§3 above).

### D. Cross-checks — already done in prior tickets, no action here

| Item | Roxanne row | Landed on |
|---|---|---|
| `SarReferral.deleted` removed | 160 | APG-2491 |
| `SarStaff.username` removed | (implicit) | APG-2510 |
| Referrer surname resolution (not raw username) | 23, 30, 159 | APG-2492 |

Included below so a reviewer can look at the spreadsheet's red rows,
tick these off, and see we're not silently ignoring them.

### E. Course reference-data fields — no code change needed

Row 35 (`description`), 36 (`alternate_name`), 39 (`audience`), 41
(`list_display_name`), 44 (`intensity`) are flagged with *"remove
if already in SAR endpoint"*. Current `SarCourse` DTO
(`SubjectAccessRequestService.kt` line 280) contains only `name` —
none of these fields are exposed. **Verified — no action needed.**

## Open questions for Roxanne (block PRs 4 and 5)

Both are ambiguous in the spreadsheet paper trail. Clarify **before**
merging PR-4 (oasysPniResults) or PR-5 (strip IDs), because the shape
of those PRs depends on the answer.

### Q1 — `oasysPniResults` section (rows 85–88)

Two conflicting notes on the same rows:

- *"10.07.26 — Dev states this should be on the report, will need to
  check new report"* — implies **keep**
- *"After call with Raby 29.07 — this should be a no — All IDs should
  be a No"* — implies **remove all IDs** (but is the `programme_pathway`
  field an ID? no — it's a category like `HIGH_INTENSITY_BC`)

Options put to Roxanne (Q1 sent + follow-up sent 2026-08-03):

- **A** — remove the whole section from the SAR (nothing surfaces).
- **B** — strip all three ID fields (`pniResultId`, `prisonNumber`,
  `oasysAssessmentId`), keep only `programmePathway`.
  (Verified against DTO `SubjectAccessRequestService.kt` lines
  315–320: those are the four fields, `programmePathway` is a
  category label — e.g. `HIGH_INTENSITY_BC` — not an ID.)

Implementation effort is roughly identical either way.

**Default if no reply by 2026-08-14 (see `APG-2546/00-roxanne-followup.md`):
Option A.**

### Q2 — `is_national` on `SarOrganisation` (row 109)

Roxanne changed column H (In SAR API) from No to Yes on 10/07, with
note *"advised should be in new SAR report form 01.07.26 hence
needed new live like report"*. But the current `SarOrganisation`
DTO (line 532) does not include `isNational`. So her spreadsheet is
either:

- **Stale** (a dev told her it had landed but it hadn't) — no action
- **A silent ADD request** — needs a new field + entity read +
  template + snapshot regen (~0.5 d, roughly on par with a Removal PR)

This was tracked earlier as APG-2494 and marked won't-do; may have
been revived by Roxanne without realising. Clarification needed.

**Do not attempt to add `isNational` until Roxanne confirms.**

**Default if no reply by 2026-08-14 (see `APG-2546/00-roxanne-followup.md`):
leave off (close APG-2494 won't-do again).** Q2 sent + follow-up sent
2026-08-03.

## PR plan

6 focused PRs. Each stands alone, each ~15-min review, each includes
its own snapshot regen. Sequence:

| # | Branch | Title | Files touched | Est. |
|---|---|---|---|---|
| 1 | `APG-2546/remove-audit-records` | `APG-2546: remove auditRecords section from SAR` | see PR-1 detail below | 0.5 d |
| 2 | `APG-2546/remove-status-history-and-reasons` | `APG-2546: remove referralStatusHistory + referralStatusReasons sections from SAR` | see PR-2 detail below | 0.5 d |
| 3 | `APG-2546/remove-sexual-offence-details` | `APG-2546: remove sexualOffenceDetails + selectedSexualOffenceDetails sections from SAR` | see PR-3 detail below | 0.5 d |
| 4 | `APG-2546/remove-oasys-pni-results` | `APG-2546: remove oasysPniResults section from SAR` (or "strip IDs from oasysPniResults" depending on Q1 answer) | see PR-4 detail below | 0.5 d |
| 5 | `APG-2546/strip-internal-ids` | `APG-2546: strip internal ID fields from remaining SAR sections` | see PR-5 detail below | 0.5–1 d |
| 6 | `APG-2546/osar-round-2-review-pdf` | `APG-2546: regenerate live-like OSAR review PDF (round 2)` | doc-only (planning note + generated PDF handed off in `~/Downloads/sar-dev-3/`) | 0.5 d |

**Total working days:** ~3.5 dev + ~1.5 buffer = **~5 dev days** — comfortably inside the 3-week (~15 dev days) envelope. Slack is intentionally large to absorb one OSAR review-cycle.

### PR-1 detail — remove `auditRecords`

The biggest single OSAR-review unblock. On the retest PRN `A8610DY`
the live-dev SAR had **28 483** audit rows in this section alone —
i.e. essentially all of the "8 000-page PDF" complaint. Removing it
turns that PDF back into something a human can read.

**Files:**
- `src/main/kotlin/.../service/SubjectAccessRequestService.kt`
  - Delete `auditRecords = auditRepository.getSarAuditRecords(prn)` (line 68)
  - Delete `auditRecords = auditRecords.toSarAudit(staffSurnames)` from `Content(...)` (line 128)
  - Delete `auditRecords: List<SarAuditRecord>` from `Content` data class (line 195)
  - Delete `data class SarAuditRecord(...)` (lines 268–278)
  - Delete `AuditEntity.List<*>.toSarAudit(...)` mapper (search once file is open)
  - Remove `AuditRepository` from constructor injection (line 41)
  - Remove `AuditEntity` and `AuditRepository` imports
  - Remove `auditRecords` argument from `resolveStaffSurnames(...)` call site + method signature
- `src/main/kotlin/.../domain/repository/AuditRepository.kt`
  - Delete `getSarAuditRecords` method (verified: only caller is the SAR service — dead after this PR)
- `src/main/resources/sar_template.mustache`
  - Delete lines 68–86 (whole `<h2>Audit records</h2>` block including empty-state branch)
- `src/test/kotlin/.../integration/SarContractIntegrationTest.kt`
  - Delete `persistenceHelper.createAuditRecord(...)` seed (line 164)
  - Delete `AUDIT_RECORD_ID` UUID constant (constants block at lines 237–243)
  - Delete `AuditAction` import if now unused
- `src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt`
  - Delete mock `every { auditRepository.getSarAuditRecords(...) }` (line 186)
  - Delete auditRecords-size assertion (line 261)
  - Delete `val audit = auditRecords[0]` block + audit-field assertions around line 316
  - Delete `verify { auditRepository.getSarAuditRecords(prn) }` (line 350)
  - Remove `auditRepository` mock + constructor arg + import
- Regenerate snapshots (SAR_GENERATE_ACTUAL=true) and promote:
  - `src/test/resources/sar/sar-api-response.json`
  - `src/test/resources/sar/sar-expected-render-result.html`
  - `src/test/resources/sar/entity-schema.json` (only if entity-graph changed)

**Impact on APG-2492 batch staff-surname resolver.** Removing the
`auditRecords` argument from `resolveStaffSurnames(...)` shrinks its
inputs but the resolver's shape stays 2-queries-per-SAR (one by-username,
one by-staff-id). The B1 integration test in
`SubjectAccessRequestServiceIntegrationTest.kt` asserted "exactly 3
staff-repo calls per SAR" and that count **holds** — audit rows never
added additional queries, they just added usernames into the batched
sets. Note in the PR that B1's staff-repo-call assertion is unaffected;
call it out so reviewers don't wonder.

### PR-2 detail — remove `referralStatusHistory` + `referralStatusReasons`

Coupled because `referralStatusReasons` is derived from
`referralStatusHistory` (line 134:
`referralStatusHistory.mapNotNull { it.reason }.distinctBy { it.code }...`)
— they cannot be removed independently.

**Files:**
- `src/main/kotlin/.../service/SubjectAccessRequestService.kt`
  - Delete `referralStatusHistory = referralStatusHistoryRepository.findByPrisonNumber(prn)` (line 69)
  - Delete `referralStatusHistory` argument from `resolveStaffSurnames(...)` call + signature
  - Delete both `referralStatusHistory = ...` and `referralStatusReasons = ...` from `Content(...)` (lines 133, 134)
  - Delete `referralStatusHistory: List<SarReferralStatusHistoryEntity>` and `referralStatusReasons: List<SarReferralStatusReason>` fields from `Content` (lines 199, 200)
  - Delete `SarReferralStatusHistoryEntity` and `SarReferralStatusReason` DTOs (lines 322–342)
  - Delete `List<ReferralStatusHistoryEntity>.toSarReferralStatusHistory(...)` and `List<ReferralStatusReasonEntity>.toSarReferralStatusReason(...)` mappers
  - Remove `ReferralStatusHistoryRepository` from constructor injection
  - Remove `ReferralStatusHistoryEntity`, `ReferralStatusHistoryRepository`, `ReferralStatusReasonEntity` imports
- `src/main/kotlin/.../domain/repository/ReferralStatusHistoryRepository.kt`
  - **Do NOT delete** `findByPrisonNumber` — it's still used by
    `ReferralStatusHistoryService.kt` (verified via
    `grep -rln referralStatusHistoryRepository src/main`).
- `src/main/resources/sar_template.mustache`
  - Delete lines 172–208 (both `<h2>Referral status history</h2>` and
    `<h2>Referral status reasons</h2>` blocks)
- `src/test/kotlin/.../integration/SarContractIntegrationTest.kt`
  - Delete `persistenceHelper.createReferralStatusHistory(...)` seed (line 209)
  - Delete `REFERRAL_STATUS_HISTORY_ID` UUID constant
- `src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt`
  - Delete mock `every { referralStatusHistoryRepository.findByPrisonNumber(...) }` (line 236)
  - Delete referralStatusHistory / referralStatusReasons assertions (lines 266, 267, 335–337)
  - Delete `verify { referralStatusHistoryRepository.findByPrisonNumber(...) }` (line 355)
  - Remove `referralStatusHistoryRepository` mock + constructor arg + import
- Regenerate + promote snapshots.

### PR-3 detail — remove `sexualOffenceDetails` + `selectedSexualOffenceDetails`

Coupled: `sexualOffenceDetails` is derived from
`selectedSexualOffenceDetails` (line 136). Removing both together
lets us delete the whole `selectedSexualOffenceDetails` local var
(lines 70–72).

**Files:**
- `src/main/kotlin/.../service/SubjectAccessRequestService.kt`
  - Delete `selectedSexualOffenceDetails = filteredReferrals.flatMap {...}` local (lines 70–72)
  - Delete `selectedSexualOffenceDetails = ...` and `sexualOffenceDetails = ...` lines from `Content(...)` (lines 135, 136)
  - Delete the two matching fields from `Content` data class (lines 201, 202)
  - Delete `SarSelectedSexualOffenceDetails` and `SarSexualOffenceDetails` DTOs (lines 344–355)
  - Delete `toSarSelectedSexualOffenceDetails` and `toSarSexualOffenceDetails` mappers
  - Remove `SelectedSexualOffenceDetailsEntity` and `SexualOffenceDetailsEntity` imports (verify no other users in this file first)
- `src/main/resources/sar_template.mustache`
  - Delete lines 210–237 (both blocks)
- `src/test/kotlin/.../integration/SarContractIntegrationTest.kt`
  - Delete `persistenceHelper.createSexualOffenceDetails(...)` (line 195)
  - Delete `persistenceHelper.createSelectedSexualOffenceDetails(...)` (line 204)
  - Delete `persistenceHelper.deleteSexualOffenceDetails(SEXUAL_OFFENCE_ID)` teardown (line 108, top of `setupTestData()`)
  - Delete `SEXUAL_OFFENCE_ID` and `SELECTED_SEXUAL_OFFENCE_ID` UUID constants
  - Delete `SexualOffenceDetailsEntity` and `SexualOffenceCategoryType` imports
- `src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt`
  - Delete sexualOffenceDetails-empty assertion (line 269)
- Regenerate + promote snapshots.

### PR-4 detail — remove `oasysPniResults` (or strip IDs, per Q1 answer)

**If Roxanne confirms Option A (whole section):**
- `src/main/kotlin/.../service/SubjectAccessRequestService.kt`
  - Delete `oasysPniResults = ...` from `Content(...)` (line 132)
  - Delete `oasysPniResults: List<SarOasysPniResult>` field from `Content` (line 197)
  - Delete `SarOasysPniResult` DTO (lines 315–320)
  - Delete `toSarOasysPniResult` mapper
  - Remove `OasysPniResultEntityRepository` from constructor injection
  - Remove `OasysPniResultEntity` and `OasysPniResultEntityRepository` imports
- `src/main/kotlin/.../domain/repository/OasysPniResultEntityRepository.kt`
  - **Do NOT delete** `findAllByPrisonNumber` — still called by
    `PersonService.kt` (line 287) in the person-deletion cascade
    (verified via `grep -rln findAllByPrisonNumber src/main`).
- `src/main/resources/sar_template.mustache`
  - Delete lines 145–158
- `src/test/kotlin/.../integration/SarContractIntegrationTest.kt`
  - Delete `persistenceHelper.createOasysPniResult(...)` (line 179)
  - Delete `OASYS_PNI_RESULT_ID` UUID constant
- `src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt`
  - Delete oasysPniResults assertions (lines 265, 332)
- Regenerate + promote snapshots.

**If Option B (strip all three IDs, keep only `programmePathway`):**
- Same shape as PR-5 pattern — DTO field removals + template row
  deletions only, section wrapper stays.
- Strip `pniResultId`, `prisonNumber`, `oasysAssessmentId` from the
  DTO, mapper, template rows, and unit-test assertions.
- Keep only `programmePathway` visible.
- See `APG-2546/PR-4-remove-oasys-pni-results.md` Option B section
  for the file-by-file breakdown.

### PR-5 detail — strip internal ID fields from remaining sections

Two DTO field-removals (both are `id`s that Roxanne flagged as
"internal ref — should be a no"). Currently `SarPerson.id` and
`SarOrganisation.id` are populated in every SAR response — the
subject sees a UUID string that is meaningless to them.

**Files:**
- `src/main/kotlin/.../service/SubjectAccessRequestService.kt`
  - Delete `id: UUID?` field from `SarPerson` (line 299)
  - Delete `id = id` field-assignment in `PersonEntity.toSarPerson()` mapper
  - Delete `id: String` field from `SarOrganisation` (line 533)
  - Delete `id = id.toString()` from `OrganisationEntity.toSarOrganisation()` mapper
- `src/main/resources/sar_template.mustache`
  - Delete the `<tr><td>Person ID</td>...</tr>` line inside `{{#person}}` block (line 125)
  - **Note:** the `{{#organisations}}` block (lines 160–170) does *not*
    render an Id row today — it only renders `Name`. `SarOrganisation.id`
    removal is therefore DTO + mapper only, no template change.
- Regenerate + promote snapshots.

**No test file changes** — the unit test asserts collection sizes
and a couple of surname / status fields; it doesn't currently probe
`SarPerson.id` or `SarOrganisation.id`. Snapshots pick up the diff.

### PR-6 detail — regenerate the OSAR round-2 review PDF

Once PRs 1–5 are merged:

1. Regenerate the SAR contract fixture snapshots on `main` — should
   already be reflected via each preceding PR, but do a clean
   `SAR_GENERATE_ACTUAL=true ./gradlew test --tests '*SarContractIntegrationTest*'`
   as a belt-and-braces.
2. The generated `build/test-generated/sar-generated-report.pdf` is
   the review artefact.
3. Copy into `~/Downloads/sar-dev-3/` alongside the JSON + HTML.
4. Append a run-log entry to `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`
   under a new heading "OSAR round 2 (2026-08-xx)".
5. Email OSAR (Sharon + Roxanne + QAT) using the "test-harness content
   sign-off" framing from the round-1 handover.

## Sequencing

PRs 1–5 all touch `SubjectAccessRequestService.kt` and
`sar_template.mustache`. Pragmatic route: **serial**, one merge →
rebase next → merge. Reasons:

- Each snapshot (`sar-api-response.json`, `sar-expected-render-result.html`)
  is rewritten on every PR. Serial merges keep every reviewer
  looking at a small, comprehensible snapshot diff.
- Order is designed to shrink the fixture progressively — PR-1 alone
  drops ~15 KB from the rendered HTML, PR-2 drops another chunk, etc.
  If any reviewer wants to see the intermediate PDF between two PRs,
  they can build locally at that point.

Order chosen: **1 → 2 → 3 → 4 → 5 → 6**, matching descending impact
on OSAR review pain (audit records first because it's the "28 000
rows" complaint we're primarily fixing).

## Not in scope for APG-2546

Explicitly excluded so no scope creep:

- **Aggregator dev-portal "pending status" stuck** — kicked to
  `#haa-sar-functionality-change-request`. Not this branch.
- **Cover-sheet on the eventual PDF** — same channel, same reason.
- **`is_national` on `SarOrganisation`** — deferred pending Roxanne's
  clarification (Q2). If she confirms it's a real ADD, a separate
  ticket (revived APG-2494 or new) will be spun up.
- **Any preprod UAT of live SARs** — that's downstream of OSAR
  content sign-off, and blocked on the aggregator team's pipeline
  regardless.

## Rollback plan

Each PR is a pure delete of one SAR section — reverting is a single
`git revert`, no schema state to rewind. Fixture snapshots come back
from the revert commit.

If OSAR come back asking for one of the removed sections to be
partially restored (e.g. "keep audit records but strip the internal
IDs"), the pattern is to open a new PR that re-adds the DTO / mapper /
template block with the requested subset of fields, rather than
reverting.

## Artefacts to capture

Filled in as each PR merges. Kept here so the OSAR round-2 hand-off
email has a clean paper trail.

| PR | Merged commit | Sample PDF page count | Notes |
|---|---|---|---|
| 1 (auditRecords) | PR #1107 opened 2026-08-03, head `4801f6e6` — merge SHA TBD | TBD (target: several thousand pages ↓) | Included cleanup of `SubjectAccessRequestServiceIntegrationTest.kt` (compile-blocker after `Content.auditRecords` removal — not in PR-1 doc, propagated into PR-2/3/4 docs). |
| 2 (statusHistory + reasons) | TBD | TBD | |
| 3 (sexualOffenceDetails ×2) | TBD | TBD | |
| 4 (oasysPniResults) | TBD | TBD | pending Q1 |
| 5 (IDs strip) | TBD | TBD | |
| 6 (OSAR round-2 PDF) | TBD | TBD (target: ~4–5 pages) | |

## Rough size

- ~500 lines removed across `src/`
- 5 new commits + 1 doc commit
- ~5 dev days (comfortably inside 3-week envelope communicated to William)
- Zero migrations, zero new dependencies

