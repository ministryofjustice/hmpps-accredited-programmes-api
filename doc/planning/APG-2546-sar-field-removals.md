# APG-2546 — SAR field removals from OSAR round-2 review

> **Status:** planning • **Depends on:** APG-2492 / APG-2493 / APG-2510 (all merged) • **Blocks:** OSAR sign-off for the round-2 review • **Ticket:** APG-2546

## Origin of this work

Following the round-1 OSAR review (dev PDF handed over 2026-07-24 —
see `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`), QAT
came back with two workstreams (Slack, 30 Jul):

1. **Content changes** — remove several fields / whole sections from
   the SAR payload that Cameron's team's SAR worker renders into an
   OSAR PDF. **This is APG-2546 / this branch.**
2. **Appearance changes** — cover-sheet / top-and-tail pages,
   headers, footers, branding. **This is APG-2547** and is
   substantively owned by Cameron's team on the SAR worker
   (`../hmpps-subject-access-request-worker`) and its SAR product
   dev service. Our participation is limited to registering the
   updated template with them via
   `#haa-sar-functionality-change-request` when we ship content
   changes — per Deborah's 2026-08-04 clarification. Any OSAR
   reviewer feedback on appearance goes to that channel under
   APG-2547, not APG-2546.

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
| 2 | `referralStatusHistory` | `SarReferralStatusHistoryEntity` | all 11 | 192–202 |
| 3 | `referralStatusReasons` | `SarReferralStatusReason` | all 5 | 205–209 |
| 4 | `sexualOffenceDetails` | `SarSexualOffenceDetails` | all 4 | 233, 234, 235, 237 |
| 5 | `oasysPniResults` | `SarOasysPniResult` | 2 of 4 (strip `pniResultId` + `oasysAssessmentId`; keep `prisonNumber` + `programmePathway`) | 85, 86, 87, 88 (✅ Q1 answered 2026-08-04 pm — see below) |

### B. Internal ID fields to strip from remaining sections

| Section | Field(s) to remove from DTO | Roxanne rows |
|---|---|---|
| `SarPerson` | `id` | 111 |
| `SarOrganisation` | `id` | 105 |
| `SarReferral` | `originalReferralId` | 165 (10.07 dev note "do not add the uuid", confirmed in person by Roxanne 2026-08-04 pm — fold into PR-5) |
| `SarPniResult` | `pniResultId`, `referralId`, `oasysAssessmentId` | 127, 128, 131 |

`SarPerson.id`, `SarOrganisation.id`, and `SarReferral.originalReferralId`
are currently populated — they'll go from the DTO, the mapper, the
template (where rendered), and the snapshots. The resolved
`originalReferral` sub-block on `SarReferral` stays.
`SarPniResult` doesn't currently expose `pniResultId` / `referralId`
/ `oasysAssessmentId` (verified against the DTO at
`SubjectAccessRequestService.kt` lines 284–295) — the fields Roxanne
flagged there are already absent. This PR is therefore a snapshot /
data-dictionary-comment tidy-up on the `SarPniResult` side, not a
code change.

### C. Coupled removals (need to happen at the same time as A/B)

- `selectedSexualOffenceDetails` (whole section) — no per-field
  red flag from Roxanne, but PR-3's DD cross-check (2026-08-03)
  surfaced a **table-level** note on row 225 that directly rebuts
  the "it's just join IDs, keep it" fallback: *"It still
  represents personal criminal data, even if derived or selected
  rather than raw."* Combined with the pure-coupling argument
  (its only purpose is to link `sexualOffenceDetails` back to
  referrals; once `sexualOffenceDetails` goes it is meaningless),
  the removal is doubly justified. Removed alongside
  `sexualOffenceDetails` in PR-3.

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

**✅ ANSWERED 2026-08-04 pm (in person).** Corrected Option B
confirmed by Roxanne — strip `pniResultId` + `oasysAssessmentId`,
keep `prisonNumber` + `programmePathway`. See DELIVERY-LOG
"Roxanne in-person answers 2026-08-04 pm" entry for provenance;
see `APG-2546/PR-4-remove-oasys-pni-results.md` for the
implementation. Q1-correction message (drafted in
`00-roxanne-followup.md`) was **not sent** — Roxanne answered
the whole stack in person before it went out. Kept in the doc
for the paper trail but explicitly marked "do NOT send".

Historical context (kept for provenance) — the two conflicting
notes on the row that motivated Q1 in the first place:

- *"10.07.26 — Dev states this should be on the report, will need to
  check new report"* — implied **keep**
- *"After call with Raby 29.07 — this should be a no — All IDs should
  be a No"* — implied **remove all IDs** (but `programme_pathway`
  isn't an ID, it's a category like `HIGH_INTENSITY_BC`)

Options put to Roxanne (Q1 sent + follow-up sent 2026-08-03) — kept
for the paper trail:

- **A** — remove the whole section from the SAR (nothing surfaces).
  ✂️ superseded by in-person answer.
- **B** — strip all three ID fields (`pniResultId`, `prisonNumber`,
  `oasysAssessmentId`), keep only `programmePathway`.
  ⚠️ semantically wrong as sent (`prisonNumber` is the PRN, not
  an internal ID; DD row 86 says keep). Corrected in person.
- **B (corrected)** — the actual confirmed answer, see banner above.

Implementation effort is identical to the sent-but-superseded
Option B (one fewer field to strip). ~0.5 dev day.

**Default banner deleted — no longer relevant, Q1 answered.**

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
| 4 | `APG-2546/strip-oasys-pni-result-ids` | `APG-2546: strip pniResultId + oasysAssessmentId from oasysPniResults, keep prisonNumber + programmePathway` | see PR-4 detail below | 0.5 d |
| 5 | `APG-2546/strip-internal-ids` | `APG-2546: strip internal ID fields from remaining SAR sections (SarPerson.id, SarOrganisation.id, SarReferral.originalReferralId)` | see PR-5 detail below | 0.5–1 d |
| 6 | `APG-2546/osar-round-2-handover` | `APG-2546: OSAR round-2 handover — docs-only` | doc-only (planning notes + downloaded PDF handed off in `~/Downloads/sar-dev-3/`; Option 1 primary, Option 2 fallback) | 0.5–1 d |

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

### PR-4 detail — strip IDs from `oasysPniResults` (Option B corrected, confirmed 2026-08-04 pm)

**✅ Q1 answered in person 2026-08-04 pm.** Execute Option B
(corrected). Option A superseded, do not execute — kept below
strictly for the paper trail.

**Option B (corrected — the confirmed path):**
- Same shape as PR-5 pattern — DTO field removals + template row
  deletions only, section wrapper stays.
- **Strip** `pniResultId` (DD row 85 red-flagged + "All IDs should
  be a No"). **Strip** `oasysAssessmentId` (Roxanne confirmed
  2026-08-04 pm — "OASys system reference, not user-facing").
- **Keep** `prisonNumber` (DD row 86 dev note "should be on the
  report"; PRN, not internal ID; consistent with every other SAR
  section retaining the subject's PRN).
- **Keep** `programmePathway` (DD row 88 dev note "should be on
  the report"; routing category like `HIGH_INTENSITY_BC`, not
  an ID; the subject has a right to see the routing decision).
- See `APG-2546/PR-4-remove-oasys-pni-results.md` Option B section
  for the file-by-file breakdown.

**Option A (whole-section removal) — SUPERSEDED, do NOT execute:**
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


### PR-5 detail — strip internal ID fields from remaining sections

Three DTO field-removals: two `id`s that Roxanne red-flagged as
"internal ref — should be a no" (rows 105 and 111), plus
`SarReferral.originalReferralId` (row 165, "pull referral data
(if not already) do not add the uuid") **confirmed in person by
Roxanne 2026-08-04 pm as a fold-in to this PR**.

Currently `SarPerson.id`, `SarOrganisation.id`, and
`SarReferral.originalReferralId` populate every SAR response with
UUID strings that are meaningless to the subject. The
`originalReferral` sub-block on `SarReferral` (already populated
via batch lookup) stays — only the raw UUID + its template row
come out.

**Files:**
- `src/main/kotlin/.../service/SubjectAccessRequestService.kt`
  - Delete `id: UUID?` field from `SarPerson` (line 299)
  - Delete `id = id` field-assignment in `PersonEntity.toSarPerson()` mapper
  - Delete `id: String` field from `SarOrganisation` (line 533)
  - Delete `id = id.toString()` from `OrganisationEntity.toSarOrganisation()` mapper
  - Delete `originalReferralId: UUID?` field from `SarReferral`
    (~line 245 area — grep for `originalReferralId` inside
    `data class SarReferral(...)`)
  - Delete `originalReferralId = …` field assignment inside
    `toSarReferral` mapper
- `src/main/resources/sar_template.mustache`
  - Delete the `<tr><td>Person ID</td>...</tr>` line inside `{{#person}}` block (line 125)
  - Delete the `<tr><td>Original referral ID</td><td>{{ optionalValue originalReferralId }}</td></tr>`
    row at line 14. The `originalReferral` block (rendered lower in the template) is unaffected.
  - **Note:** the `{{#organisations}}` block (lines 160–170) does *not*
    render an Id row today — it only renders `Name`. `SarOrganisation.id`
    removal is therefore DTO + mapper only, no template change.
- Regenerate + promote snapshots.

**Test file changes:**
- `SubjectAccessRequestServiceTest.kt` around lines 277 / 300 /
  305 — delete `assertThat(referral.originalReferralId).isEqualTo(…)`
  / `.isNull()` assertions. Keep assertions on the
  `originalReferral` sub-block (id, prison number etc.) — that's
  the subject-facing surface now.
- No other changes — the unit test asserts collection sizes and
  a couple of surname / status fields; it doesn't currently probe
  `SarPerson.id` or `SarOrganisation.id`. Snapshots pick up the diff.

**Entity layer is unchanged.** The batch lookup that populates
the `originalReferral` sub-block (via `referralRepository.findAllById(...)`)
still needs the source UUID from the entity layer, so
`ReferralEntity.originalReferralId` stays. Only the *DTO* and
*template* stop exposing it.

### PR-6 detail — OSAR round-2 handover (Option 1 primary, Option 2 fallback)

**Scope updated 2026-08-04 pm following Deborah's clarification
of the two handover routes. See
`APG-2546/PR-6-osar-round-2-handover.md` for the full doc.**

Once PRs 1–5 are merged and deployed to DEV:

1. **Register our template** with Cameron's team by posting on
   `#haa-sar-functionality-change-request` with a link to
   `src/main/resources/sar_template.mustache` on `main`. Do this
   as soon as PR-5 is on `main`, not after — Round 1 saw a
   pipeline block and Option 1 needs lead time.
2. **Get `SARBT001` role** added to a test nDelius account if not
   already present (same channel, same team).
3. **Generate the report** via the SAR dev service at
   `sign-in-dev.hmpps.service.justice.gov.uk`. Pick a CRN with
   rich Accredited Programmes data (round 1 used `A0137CY`),
   restrict the date range, select Accredited Programmes only,
   download the PDF (full cover-sheet + top-and-tail pages).
4. **Sanity-check the PDF** — cover sheet present, removed
   sections actually absent, referrer surnames render, staff
   `username` absent.
5. **Copy to `~/Downloads/sar-dev-3/`** and email OSAR (Sharon +
   Roxanne + William + QAT, CC Cameron + Naseem + Kiril) using
   the draft in the PR-6 doc.
6. **Fallback to Option 2** (chrome-less test harness, per Indy's
   Confluence page at <https://dsdmoj.atlassian.net/wiki/x/DgMOaQE>)
   only if Option 1 pipeline stalls. Same PDF our contract test
   emits at `build/test-generated/sar-generated-report.pdf`.
7. **Append a run-log entry** to
   `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`
   under "OSAR round 2 (2026-08-xx)".

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

- **Cover-sheet, top-and-tail pages, headers, footers, and other
  appearance concerns on the OSAR PDF** — this is **APG-2547**,
  owned by Cameron's team's SAR worker + SAR dev service. Our
  participation is limited to (a) registering the updated template
  with them and (b) generating a test PDF via their dev service or
  test harness during the round-2 handover. See
  `APG-2546/PR-6-osar-round-2-handover.md` for the handover mechanics.
  Any OSAR reviewer feedback on appearance goes to
  `#haa-sar-functionality-change-request` under APG-2547, not here.
- **Aggregator dev-portal "pending status" stuck** — same
  `#haa-sar-functionality-change-request` channel; also Cameron's
  team's remit. This is the reason Option 2 (chrome-less test
  harness) exists as a fallback path (see PR-6 doc).
- **`is_national` on `SarOrganisation`** — deferred pending
  Roxanne's clarification (Q2). If she confirms it's a real ADD,
  a separate ticket (revived APG-2494 or new) will be spun up.
- **Any preprod UAT of live SARs** — that's downstream of OSAR
  content sign-off and possibly co-owned with Cameron's team;
  separate exercise, not APG-2546.
- **Beefing up test-fixture data coverage for vettor training** —
  Deborah flagged (2026-08-04) that OSAR prefer as many fields
  populated as possible so vettors can be trained on the full data
  domain. Under PR-6 Option 1 we work around this by choosing a
  richer dev CRN; under Option 2 the fixture would need
  beefing up. Nice-to-have, not APG-2546 blocker; consider for a
  future ticket if OSAR asks in round 2.

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
| 1 (auditRecords) | `50f67cff` (PR #1107, merged 2026-08-03) | Not measured at merge time; combined PR-1+PR-2 = **3 pages** (see PR-2 row) | 9-lens agent review all green. Included cleanup of `SubjectAccessRequestServiceIntegrationTest.kt` (compile-blocker after `Content.auditRecords` removal — not in PR-1 doc, propagated into PR-2/3/4 docs). |
| 2 (statusHistory + reasons) | `cd306c99` (PR #1109, merged 2026-08-03) | **3 pages** (down from round-1 ~8,000) | No deviations from doc. Review-fix amend `f890b221` dropped stale `resolveStaffSurnames` KDoc + rationale-comment enumerations that no longer applied after PR-1 and PR-2 stripped `audits` and `statusHistory` from the resolver. |
| 3 (sexualOffenceDetails ×2) | PR #1110 opened 2026-08-03, head `fc5ae133` (incl. review-amend) — merge SHA TBD | **3 pages** (unchanged from PR-2) | No deviations from doc. DD cross-check: all 4 red-flagged `sexual_offence_details` fields (rows 233/234/235/237) removed exactly. Row 225's table-level note strengthens the coupled `selected_sexual_offence_details` removal beyond the pure-coupling argument (§C updated). Review-amend `fc5ae133` reinstated the blank line after `clearAllTableContent()` per project convention. |
| 4 (oasysPniResults) | TBD | TBD | pending Q1 |
| 5 (IDs strip) | TBD | TBD | |
| 6 (OSAR round-2 handover) | TBD | Option 1 primary: full-chrome PDF from Cameron's SAR dev service (page count TBD). Option 2 fallback: chrome-less PDF from our contract test (3 pages post-PR-2). Round-1 baseline: ~8,000 pages. | Handover per Deborah's 2026-08-04 clarification. Appearance / cover / headers / footers wrapped in via Option 1 = Cameron's team's SAR product / APG-2547. |

## Rough size

- ~500 lines removed across `src/`
- 5 new commits + 1 doc commit
- ~5 dev days (comfortably inside 3-week envelope communicated to William)
- Zero migrations, zero new dependencies

