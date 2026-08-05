# APG-2546 delivery log

Running log for the whole ticket. This chat is the coordinating
context — each PR is executed in its own fresh chat using the
matching working-notes doc, but the outcome (merge SHA, PDF page
count, artefacts) is recorded here as soon as it lands, so we have
one place to look for state and can close the ticket cleanly at the
end.

## Status at a glance

> **📉 Content-readability target already hit (internal metric).**
> With PRs 1 and 2 both merged, the **test-harness** SAR PDF has
> dropped from the round-1 baseline of ~8,000 pages to **3
> pages**. This is the chrome-less Option 2 output (no cover, no
> headers, no footers) — the same content that Cameron's team's
> SAR product wraps in the full OSAR-quality PDF under Option 1
> (the OSAR-preferred handover route). So it's both a genuine
> internal readability metric *and* representative of what OSAR
> will see, minus chrome. The "8,000-page complaint" is fixed at
> the content level. PRs 3, 4, and 5 are still worthwhile
> (Roxanne's red-flagged rows, privacy hygiene, etc.).

| Item | State | Notes |
|---|---|---|
| Planning branch (`APG-2546/planning-sar-field-removals`) | ⏳ in flight | Committed and pushed rolling; typically local ahead of origin by a small number of commits between chats. |
| Q1 to Roxanne (`oasys_pni_result` A vs B) | ✅ answered 2026-08-04 pm in person → corrected Option B | Strip `pniResultId` + `oasysAssessmentId`; keep `prisonNumber` + `programmePathway`. See "Roxanne in-person answers" timeline entry. |
| Q2 to Roxanne (`is_national` on organisation) | ✅ closed 2026-08-04 pm on "leave off" default | Roxanne had in-person window on 2026-08-04 pm and did not raise; per 2026-08-03 follow-up terms this locks the default in. APG-2494 stays won't-do. If raised later, spin fresh ticket. See "Q2 closed on default" timeline entry. |
| PR-1 — remove `auditRecords` | ✅ merged `50f67cff` 2026-08-03 | PR #1107. 9-lens agent review all green. Branch head `04ab44ed` (initial `4801f6e6` + review-fix `04ab44ed`). |
| PR-2 — remove `referralStatusHistory` + `referralStatusReasons` | ✅ merged `cd306c99` 2026-08-03 | PR #1109. Branch head was `f890b221` (initial `22c97122` + review-fix amend). No deviations from doc. Sample PDF post-PR-2 = **3 pages**. |
| PR-3 — remove `sexualOffenceDetails` + `selectedSexualOffenceDetails` | ✅ merged `d6587351` 2026-08-04 | PR #1110. Branch head `fc5ae133` (incl. review-amend for blank-line convention). |
| PR-4 — strip `pniResultId` + `oasysAssessmentId` from `oasysPniResults` (Option B corrected) | ✅ merged `2a79b856` 2026-08-05 am | PR #1111. Q1 answered in person 2026-08-04 pm → Option B (corrected). Stripped `pniResultId` + `oasysAssessmentId`; kept `prisonNumber` + `programmePathway`. Sample PDF: 3 pages. |
| PR-5 — strip `SarPerson.id` + `SarOrganisation.id` (+ `SarReferral.originalReferralId`) | ⏳ open, nine-lens review clean, awaiting merge | PR #1112. Branch head `677c8ea2`. 9-lens review 2026-08-05 all green + one non-blocking flag → spun as PR-7. |
| PR-7 — strip retained `SarOriginalReferral.id` UUID | ⬜ ready to start (once PR-5 merges) | Follow-on from PR-5's review flag. Raby confirmed 2026-08-05 that Roxanne's rows 105 + 111 blanket "no IDs" rule covers the nested sub-block's UUID too. See `PR-7-strip-original-referral-uuid.md`. Branch: `APG-2546/strip-original-referral-uuid`. |
| PR-6 — OSAR round-2 handover | 🚫 blocked on PRs 1–5 + PR-7 (+ dev deploy) | **Scope re-updated 2026-08-04 pm:** Option 1 primary (full-chrome PDF from Cameron's SAR dev service, OSAR-preferred), Option 2 fallback (chrome-less test harness). Kick off template-registration on `#haa-sar-functionality-change-request` as soon as **PR-7** hits `main` — the round-2 PDF should reflect the final zero-UUID content shape. See `PR-6-osar-round-2-handover.md`. |
| OSAR content sign-off (Sharon + Roxanne + QAT + William + David) | 🚫 blocked on PR-6 handover | Round-2 review. This is APG-2546's end state. |
| APG-2547 — appearance / template registration with Cameron's SAR product | 🤝 coordination needed | Not "out of our scope" — we need to post the template link on `#haa-sar-functionality-change-request` and get `SARBT001` role added. Cameron's team wraps our template in cover/header/footer; that wrapping is theirs, but the coordination is joint. |
| Ticket transition to Done | 🚫 blocked on OSAR content sign-off | APG-2546 closes on content sign-off. |

Legend: ⬜ ready • 🚫 blocked • ⏳ in flight • ✅ complete • 🔄 out of our scope.

## Timeline

### 2026-08-03 — planning + code cross-check

- Verified planning doc `APG-2546-sar-field-removals.md` against
  code line by line. Two substantive corrections, several
  line-number drifts. Recorded in commit `9b66aa5a`.
- Added per-PR working-notes folder under
  `doc/planning/APG-2546/`. Commits `e1acc9a0` through `0165180d`.
- Roxanne Q1 + Q2 originals sent (by Raby, direct to Roxanne).
- Follow-up drafted, rewritten in Raby's voice, sent same day.
  Commit `1dd32fef`.
- Delivery-log added. Commit `acba45c4`.
- Pre-handoff consistency pass: fixed Option B definition drift
  across `APG-2546-sar-field-removals.md` and
  `PR-4-remove-oasys-pni-results.md` so both match what Roxanne
  was actually offered (strip **all three** IDs, keep only
  `programmePathway`). Removed the "Correction to be aware of"
  workaround from the follow-up doc since it's no longer needed.
  Marked the follow-up doc as "sent 2026-08-03" in its header.

### 2026-08-04 pm — Q1 answered (in person) + Q2 closed on default

Consolidated placeholder for the two Roxanne outcomes. Full
details in the two later timeline entries:

- **Q1:** "Roxanne in-person answers 2026-08-04 pm" —
  corrected Option B confirmed. `SarReferral.originalReferralId`
  fold-in to PR-5 confirmed. `oasysAssessmentId` = strip.
- **Q2:** "Q2 closed on default 2026-08-04 (pm, end of day)" —
  Roxanne did not raise `is_national` in the same window; per
  2026-08-03 follow-up terms, "leave off" default locks in.
  APG-2494 stays won't-do.

Original placeholder headings for "Q1 answer received", "Q2
answer received", and "Q1/Q2 default triggered" removed —
both outcomes are now concrete.

### YYYY-MM-DD — Q2 answer received

_(Placeholder resolved — see "2026-08-04 pm — Q1 answered (in
person) + Q2 closed on default" above.)_

### YYYY-MM-DD — Q1/Q2 default triggered

_(Placeholder resolved — Q2 defaulted "leave off" on 2026-08-04
pm as recorded above; Q1 did not need default because Roxanne
answered in person. Kept as a heading only so future ticket
authors doing similar Q&Q patterns can see the structure.)_

### 2026-08-03 — PR-1 opened (awaiting merge to `main`)

- **Branch:** `APG-2546/remove-audit-records` (from `main`
  @ `106e27d2`).
- **PR link:** #1107.
- **Head commit on branch:** `04ab44ed` (initial `4801f6e6`, plus
  review-fix `04ab44ed`).
- **Files changed:** 8 files, +19 / −152 (initial) + a small
  block-comment fix.
- **Verification:** all grep checks zero, `./gradlew ktlintCheck test`
  green (678 tests), snapshots regenerated, `entity-schema.json`
  byte-identical as predicted.
- **Deviations from the PR-1 doc** — both good calls:
  1. Also removed the `createAuditRecord` seed +
     `content.auditRecords` assertions in
     `SubjectAccessRequestServiceIntegrationTest.kt`. Not listed in
     the PR-1 doc — was a compile-blocker after `Content.auditRecords`
     was deleted. Mirrors the pattern the doc did prescribe for the
     sibling test files. Flagged in the PR body.
  2. Skipped the "Artefacts" table note in
     `doc/planning/APG-2546-sar-field-removals.md`. That file
     doesn't exist on `main`, only on the planning branch — out of
     scope for PR-1. Update path handled here on the planning
     branch (this log + the plan's artefacts table) instead.
- **Review outcome (9-lens agent review, all green):**
  correctness ✅, tests ✅, API/contract ✅ (no controller /
  OpenAPI / `restapi/model` touched), data/migrations ✅ (no Flyway,
  `AuditEntity` table untouched, schema v144 asserted unchanged),
  security/privacy ✅ (net win — usernames + audit-action strings
  no longer egress via SAR; write-side `AuditService` untouched),
  performance ✅ (one fewer JPA query per SAR, B1 two-query shape
  preserved), observability ✅ (unresolved-original `log.warn`
  preserved), style/conventions ✅ (zero `APG-` refs in
  `src/main` or `src/test`, commit-msg format matches history,
  ktlint clean), devex/docs/rollback ✅ (PR body from doc template,
  rollback = single `git revert`).
- **Review fix pushed as `04ab44ed`:** stale "audit" enumeration
  removed from the block comment above `resolveStaffSurnames(...)`
  — it was still listing "referral / course participation / audit
  / status-history row" as sources of usernames after the parameter
  was dropped.
- **Optional pre-merge to-dos flagged** (non-blocking):
  - Artefacts-row update on the planning branch — done here as
    part of commit `8d934143`.
  - Smoke-test verify the write-side audit path
    (`AuditService`, `PeopleController.auditService.audit(...)`) is
    unaffected. Nothing in the PR touched it; CI covers this
    implicitly via the write-side unit tests.
- **Impact on PR-2 / PR-3 / PR-4:** all three will need the same
  `SubjectAccessRequestServiceIntegrationTest.kt` cleanup as Content
  fields drop out. PR-2, PR-3, and PR-4 (Option A) docs updated to
  call this out explicitly.

### 2026-08-03 — PR-1 merged

- **PR link:** #1107.
- **Merge commit on `main`:** `50f67cff`.
- **Sample PDF page count post-PR:** not measured at merge time —
  first measurement is post-PR-2 = **3 pages** (see PR-2 entry
  below). PR-1 alone certainly delivered the bulk of the drop
  (28,483-row `auditRecords` section was the ~8,000-page
  contributor); PR-2 shaves off a further couple of hundred rows
  of status-history/reasons.
- **Reviewer:** _(fill in from GitHub once merge notification lands)_.
- **Notes / surprises:** none blocking. 9-lens agent review all
  green. Review-fix `04ab44ed` (stale block comment) pushed and
  merged as part of the PR. Both flagged pre-merge to-dos handled:
  artefacts-table update landed on the planning branch; audit
  write-side confirmed untouched by inspection (no PR changes to
  `AuditService` / `PeopleController` write paths).

### 2026-08-03 — PR-2 opened (awaiting merge to `main`)

- **Branch:** `APG-2546/remove-status-history-and-reasons` (from
  `main` @ `50f67cff`, i.e. tip-of-`main` after PR-1 merged).
- **PR link:** #1109.
- **Head commit on branch:** `f890b221` (initial `22c97122` +
  review-fix amend; force-pushed once).
- **Files changed:** 8 files, +5 / −167 (post-amend).
- **Verification:** `./gradlew ktlintCheck test` green (678 tests),
  snapshots regenerated (`sar-api-response.json` +
  `sar-expected-render-result.html`), `entity-schema.json`
  correctly untouched.
- **No deviations from the PR-2 doc.** The
  `SubjectAccessRequestServiceIntegrationTest.kt` §6 that
  PR-1's experience added was hit exactly as documented — both
  `createReferralStatusHistory` seeds + the
  `content.referralStatusHistory` assertion removed.
- **`ReferralStatusHistoryRepository.findByPrisonNumber`:** the
  agent confirmed by grep that SAR service was the only remaining
  caller in `src/main` and **deleted** the method (as suggested in
  the PR-2 doc). `@EntityGraph` import correctly kept for the
  surviving `getAllByReferralIdOrderByStatusStartDateDesc`.
- **Review outcome (post-review by second agent — clean):**
  - No ticket refs anywhere in the code diff.
  - Import removals limited to what's necessary; no dangling unused
    imports.
  - Constructor-injection ordering preserved.
  - Mustache blocks removed cleanly; blank-line spacing between
    surviving `{{#organisations}}` and
    `<h2>Selected sexual offence details</h2>` preserved.
- **Review fixes amended into `f890b221`:**
  1. Stale KDoc on `resolveStaffSurnames` still said "four SAR
     entity collections" — accurate pre-APG-2492 (referrals /
     participations / audits / statusHistory); PR-1 dropped audits
     (→ three), PR-2 dropped statusHistory (→ two). Changed to
     "the SAR entity collections" to match PR-1's "don't bother
     counting" approach.
  2. Stale rationale comment above
     `val staffSurnames = resolveStaffSurnames(...)` still
     enumerated "O(N) queries per referral / course participation /
     status-history row". Dropped `status-history row` — mirrors
     PR-1's `audit` removal in commit `04ab44ed`.
- **Sample PDF post-PR-2:** **3 pages**. Round-1 baseline was
  ~8,000 pages, so with PRs 1 + 2 landed we are effectively at the
  round-2 target already. PRs 3, 4 (whichever option), and 5 will
  trim further but the "8,000-page complaint" is functionally
  resolved from this point forward.
- **DD spreadsheet cross-check (post-review sanity pass):** re-read
  `doc/2026.07.08_copy_Probation Digital Data review December 251.xlsx`,
  sheet `Accredited Programmes Custody`, rows 190–212, against the
  fields PR-2 removed. All **16** red-flagged fields removed, zero
  over-removed, zero under-removed. **One doc-only correction:** the
  planning docs originally said "referralStatusHistory rows 192–201,
  all 10 fields" — spreadsheet actually flags **rows 192–202, all 11
  fields** (row 202 = `username`, tagged with both a "surname not
  user code" note *and* the standard "After call with Raby 29.07 —
  this should be a no" verdict). PR-2 removed the whole DTO so the
  code is unaffected; the count was fixed in
  `APG-2546-sar-field-removals.md` §A and
  `PR-2-remove-status-history-and-reasons.md` so PR-6's OSAR
  handover email inherits the right number. `referral_status_reason`
  rows 205–209 (5 fields) match the doc as originally written.

### 2026-08-03 — PR-2 merged

- **PR link:** #1109.
- **Merge commit on `main`:** `cd306c99`.
- **Sample PDF page count post-PR:** 3 pages (measured at branch
  head pre-merge — expected unchanged post-merge).
- **Reviewer:** _(fill in from GitHub once merge notification lands)_.
- **Notes / surprises:** none blocking. Review-fix amend `f890b221`
  merged as part of the PR.

- **PR link:** _()_.
- **Merge commit on `main`:** _()_.
- **Sample PDF page count post-PR:** _()_.
- **Reviewer:** _()_.
- **Notes / surprises:** _()_.

### 2026-08-03 — PR-3 opened (awaiting merge to `main`)

- **Branch:** `APG-2546/remove-sexual-offence-details` (from
  `main` @ `cd306c99`, i.e. tip-of-`main` after PR-2 merged).
- **PR link:** #1110.
- **Head commit on branch:** `fc5ae133` (initial `448f0d2f` +
  review-amend for blank-line convention; force-pushed once).
- **Files changed:** 7 files, +1 / −147 post-amend (pre-amend was
  −148; the one-line delta is the reinstated blank line between
  `persistenceHelper.clearAllTableContent()` and the first
  `create*` call in `SarContractIntegrationTest.setupTestData()` —
  matches convention verified against three sibling integration
  tests).
- **Verification:** `./gradlew ktlintCheck test --tests '*Sar*'
  --tests '*SubjectAccessRequestService*'` green (ktlint clean),
  snapshots regenerated.
- **No deviations from the PR-3 doc.** The integration-test §6
  (`SubjectAccessRequestServiceIntegrationTest.kt`) added post-PR-1
  was hit exactly as documented.
- **DD spreadsheet cross-check (Roxanne's sheet, `Accredited
  Programmes Custody`):**
  - **`sexual_offence_details` table (rows 233–237):** four fields
    carry the "After call with Raby 29.07 — this should be a no"
    note (`id`, `category`, `description`, `score`). PR-3 removed
    exactly those four from `SarSexualOffenceDetails`. Row 236
    `hint_text` was never on SAR and stays absent. 1:1 match, zero
    over/under-removal.
  - **`selected_sexual_offence_details` table (rows 225–228):** no
    per-field red flag but row 225's **table-level** note says
    *"It still represents personal criminal data, even if derived
    or selected rather than raw."* That's a direct Roxanne
    rebuttal of the "it's just join IDs, keep it" fallback the
    PR-3 doc's "Non-obvious §3" floated. So the coupled removal
    is more strongly justified than the doc suggested — Roxanne
    pre-empted the fallback argument in writing.
- **Review outcome (agent-reviewed, all clean):** blank-line
  convention fix noted above amended into `fc5ae133`. Xlsx working
  copy (`Copy of 2026.07.08_copy_...`) intentionally untracked,
  same as prior cycle.
- **Sample PDF post-PR-3:** **3 pages** (unchanged from PR-2 — the
  sexual-offence sections were tiny in the seed set).

### 2026-08-04 — William Falconer email → PR-6 scope changed to content-only

**What happened.** Snr Tech Architect William Falconer emailed
`#osar-review` (or similar) to clarify how consumer teams should
hand over for OSAR review. Direct quote:

> To be clear — we should provide content produced by the test
> harness as provided by \[Cameron's\] team for this. This is the
> agreed approach and should not include full rendering of headers
> and footers. This is produced by the SAR Service, and is outside
> of the scope of the teams building the report contents. We
> already have precedent in accommodation for this and expect to
> follow the same consistent approach on all teams doing this work.

**Impact.**

- **PR-6** was framed as "hand over a live-like PDF". Revised to
  "hand over content only (JSON + HTML), no PDF". New doc:
  `PR-6-osar-round-2-content-handover.md`. Old
  `PR-6-osar-round-2-review-pdf.md` deleted.
- **APG-2547** (appearance / headers / footers) is confirmed as
  Cameron's team's remit, not ours. Added to status table as
  a 🔄 out-of-scope item so it's visible but doesn't block
  APG-2546's close-out.
- **Test-harness PDF page count** (round-1 ~8,000 → now 3 pages)
  is reframed as an **internal readability metric only**. It's
  still reassuring evidence that the content-level readability
  problem is fixed, but it does NOT go into the OSAR handover
  email or the round-2 run-log entry as the primary metric —
  those emphasise the content delta (sections removed, DD rows,
  Roxanne's flags) instead.
- **Top-level plan** updated: §"Origin of this work" now says
  APG-2547 owns appearance; §"Not in scope for APG-2546" leads
  with William's email; PR-6 detail block rewritten;
  artefacts-table row 6 updated.

**Verified on the sibling repo** (`../hmpps-subject-access-request-worker`):

- `services/pdf/v2/PdfService.kt` — 370 lines, uses iText to
  build cover + contents + service partials + rear + merge.
- `services/pdf/events/SubjectAccessRequestHeaderAndFooterEventHandler.kt`
  — 72 lines, adds "Official Sensitive" footer + subject-name /
  NOMIS-ID / n-Delius-case header via a `PdfDocumentEvent` handler.
- Our contract test uses `hmpps-subject-access-request-test-support:2.4.2`
  (build.gradle.kts line 71) — a *different* code path that has
  none of this chrome. This is the "PDF generator we wrote quickly"
  in colloquial terms — it's fine as a content dump, not fine as
  an OSAR handover.

**No PR is being cut for this — it's a docs / framing update on
the planning branch only.**

### 2026-08-04 (pm) — Deborah clarification → PR-6 scope re-updated to Option 1 primary / Option 2 fallback

**What happened.** Deborah (Senior Delivery Manager on Cameron's
SAR product team) responded to a DM asking how a consumer team
actually generates a report. She laid out **two** paths:

- **Option 1 (OSAR-preferred).** Register the template with her
  team via `#haa-sar-functionality-change-request`, push code to
  DEV, get `SARBT001` role on a test nDelius account, generate
  the report via `sign-in-dev.hmpps.service.justice.gov.uk`. The
  output is a **full-chrome PDF** with the standard cover sheet
  and top-and-tail pages. This is what OSAR actually want.
- **Option 2 (fallback).** Cameron's team's test harness /
  library (Indy's, documented at
  <https://dsdmoj.atlassian.net/wiki/x/DgMOaQE>) — same PDF but
  **without** cover sheets. Deliberately built as an escape
  hatch for exactly the sort of dev-pipeline block that hit
  round 1. This is what William's email was pointing at, and
  it's the same library our `SarContractIntegrationTest`
  already uses (`build/test-generated/sar-generated-report.pdf`).

**Impact — the morning's revision was materially wrong.**

- PR-6 was rewritten this morning to "content-only handover
  (JSON + HTML), no PDF" on the reading that William's email
  meant "no PDF at all". Deborah's message shows that reading
  was too aggressive — William was steering us at Option 2
  (chrome-less PDF) not "content only". Both Options produce a
  PDF; neither produces a JSON+HTML bundle as the primary
  artefact.
- **PR-6 doc renamed and rewritten (this pm):**
  `PR-6-osar-round-2-content-handover.md` → deleted.
  `PR-6-osar-round-2-handover.md` → new, with Option 1 as
  primary path, Option 2 as fallback, sanity-check list, and
  updated OSAR email draft mentioning David Evans.
- **APG-2547 reframed.** Was 🔄 "wholly Cameron's team". Now
  🤝 "coordination needed" — appearance still lives with
  Cameron's team but we participate by registering the template
  and taking the SARBT001 role. Not a we-don't-touch-it ticket.
- **📉 banner reframed.** The 3-page test-harness PDF is *both*
  an internal readability metric *and* representative of what
  OSAR see (minus chrome) under Option 2 — because it is
  literally the Option 2 output.
- **New concern surfaced by Deborah:** OSAR prefer as many
  fields populated as possible for vettor training. Under
  Option 1 we address this by picking a rich dev CRN. Under
  Option 2 the current fixture is minimum-viable and would
  need beefing up — recorded as out-of-scope for APG-2546 but
  flagged for a possible future ticket if OSAR asks in round 2.
- **Kick-off timing.** Template-registration request should be
  posted on `#haa-sar-functionality-change-request` as soon as
  PR-5 hits `main`, not after PR-6 starts, to give Option 1 as
  much lead time as possible against another pipeline block.

**Verification still to do (see PR-6 doc §"Non-obvious §1"):**
confirm the Confluence page's Option 2 library is the same as
`hmpps-subject-access-request-test-support:2.4.2` at
`build.gradle.kts:71` — 95% confident, not yet proven.

**No PR is being cut for this — it's another docs / framing
update on the planning branch only. Superseded the morning's
William-email revision within the same day.**

### 2026-08-04 (pm, later) — DD notes sweep beyond red-flagged rows → PR-4 Option B scope needs refining, PR-5 has a potential extension

**Why the sweep.** During Deborah's exchange the DD row for
`oasys_pni_result.programme_pathway` (row 88) surfaced with a
10.07.26 dev note: "Dev states this should be on the report will
need to check new report". That note isn't on a red-flagged row
so all our earlier DD cross-checks (red-only filters) missed it.
Ran a full-sheet sweep of column I (Additional Notes) on the
"Accredited Programmes Custody" tab — 65 note-bearing rows —
via `/tmp/dd_notes_sweep.py`.

**Findings that change APG-2546 scope:**

1. **PR-4 Option B is mis-specified.** Currently the PR-4 doc
   and top-level plan describe Option B as "strip all three IDs
   (`pniResultId`, `prisonNumber`, `oasysAssessmentId`), keep
   only `programmePathway`". But the DD says:
   - **row 85** `pni_result_id` — red-flagged 29.07 + note
     "All IDs should be a No" → strip. ✓
   - **row 86** `prison_number` — 10.07 dev "should be on the
     report" + NOT red-flagged → **KEEP**. `prisonNumber` is
     the subject's business ID (a PRN like "A1234BC"), not an
     internal UUID. Treating it as an "internal ID for removal"
     is a semantic error. Every other SAR section keeps
     `prisonNumber` — this one should too.
   - **row 87** `oasys_assessment_id` — 10.07 dev "should be on
     the report" AND row 85's blanket "All IDs should be a No".
     **Ambiguous** — a Long ref to the OASys assessment, not a
     UUID. The 29.07 red-flag pass didn't explicitly re-flag
     row 87 individually. Recommend: strip by default (aligns
     with the ID-removal theme + row 85's blanket note), and
     add explicit call-out to Roxanne in her Q1 follow-up.
   - **row 88** `programme_pathway` — 10.07 dev "should be on
     the report" + NOT red-flagged → **KEEP**. ✓ (already the
     PR-4 Option B target.)

   Net: real Option B is "strip `pniResultId` (+ probably
   `oasysAssessmentId`); keep `prisonNumber` + `programmePathway`",
   not "strip 3 keep 1". Updated the PR-4 doc + top-level plan
   to reflect this.

2. **PR-5 has a potential extension: `originalReferralId`.**
   Row 165 (`referral.original_referral_id`) dev note: "pull
   referral data (if not already) do not add the uuid".
   - We already pull the referral data — `SarReferral` has an
     `originalReferral` sub-block populated via batch lookup
     (verified `SubjectAccessRequestServiceTest.kt` lines
     277–305).
   - We also still expose the raw UUID on
     `SarReferral.originalReferralId`, and the template renders
     it at `sar_template.mustache:14`
     (`<td>Original referral ID</td><td>{{originalReferralId}}</td>`).
   - The DD dev note explicitly says "do not add the uuid" —
     so `originalReferralId` and its template row should be
     stripped. Consistent with PR-5's ID-strip theme.
   - Not in Roxanne's 30 Jul red-flag pass, but the 10.07 dev
     note is unambiguous. Recommend: add to PR-5's scope
     rather than spin a separate PR, since the mechanics
     (DTO field + template row + optional test assertion)
     match `SarPerson.id` / `SarOrganisation.id` exactly.
   - Added a "potential scope extension" block to PR-5 doc.
     Not committing to it as PR-5 scope until confirmed with
     Roxanne (or you decide unilaterally that a dev-note
     signal is enough — an argument can be made either way
     because "do not add the uuid" is unambiguous).

**Findings adjacent to APG-2546 (worth logging, not scope):**

- **Row 64** `course_participation.is_draft` — "ensure we share
  drafts too". Verify the SAR query doesn't filter out drafts.
  If it does, separate ticket.
- **Rows 57, 59** `course_participation.{created,last_modified}_by_username`
  — "surname only", currently API=No. If we should surface as
  surnames on SAR, separate scope.
- **Rows 162, 163** `referral.{primary,secondary}_pom_staff_id`
  — "surname only". Verify the SAR resolves to surnames rather
  than raw IDs/usernames. APG-2492 (referrer surname) precedent.
- **Row 108** `organisation.gender` + **row 109**
  `organisation.is_national` — 10.07 dev "should be in new SAR
  report form". Row 109 is exactly what Q2 to Roxanne is about.
  Row 108: verify current SAR includes `gender` for
  organisations.
- **Row 107** `organisation.name` — has both "needs to be
  removed from SAR endpoint" (old) AND "should be in new SAR
  report form" (10.07 update). Currently API=Yes. Contradictory
  notes on the same row — verify SAR output is what dev
  eventually settled on.

**Sweep methodology.** `doc/planning/APG-2546/scripts/dd-notes-sweep.py`,
reads `doc/Copy of 2026.07.08_copy_...xlsx` via openpyxl,
`data_only=True`, iterates Accredited Programmes Custody sheet
rows 15..245, collects (Entity, Element, Mandatory, SAR data,
In SAR API, Additional Notes) for every row with a non-null
Additional Notes. Kept in-repo so future DD refreshes (or
adjacent tickets on the same sheet) can rerun without
rebuilding. Working copy of the xlsx is untracked — either drop
your copy at the default path or pass the xlsx path as the
first argument.

**No PR is being cut for this — docs / framing update only.
The PR-4 Option B scope refinement will land as part of the
actual PR-4 code branch when Roxanne's Q1 comes back.**

### 2026-08-04 (pm, later still) — Roxanne in-person answers → Q1 fully resolved, PR-4 unblocked, PR-5 scope confirmed

**What happened.** Bumped into Roxanne in person. Walked her
through the three DD-sweep findings before the correction
message went out. She answered all three on the spot.

**Answers:**

1. **Option B scope correction on `oasys_pni_result`** (was:
   "Option B as I framed it wrongly strips `prison_number`;
   corrected Option B keeps `prison_number` + `programme_pathway`,
   strips `pni_result_id` + `oasys_assessment_id`") — ✅
   **corrected Option B confirmed**. `prison_number` stays
   because it's the PRN not an internal ID, `programme_pathway`
   stays because it's the routing decision the subject has a
   right to see, and both IDs go.
2. **`oasys_assessment_id`** ambiguity — ✅ **strip**. Aligns
   with the "OASys system reference, not user-facing" default
   position we noted in the correction draft.
3. **`SarReferral.originalReferralId`** (was: PR-5 potential
   scope extension, (a) fold-in vs (b) defer) — ✅ **strip
   the UUID, fold into PR-5**. Resolved `originalReferral`
   sub-block stays; template row 14 comes out.

**Q1 status:** fully answered. PR-4 unblocked.
**Q2 (`is_national` on `SarOrganisation`)**: at the time this
entry was written, still open with a Fri 2026-08-14 deadline
and "leave off" default. **Subsequently closed later 2026-08-04
pm** on that same "leave off" default — see the next timeline
entry "Q2 closed on default → APG-2546 has zero open external
questions".

**Impact.**

- **`00-roxanne-followup.md`** — Q1-correction draft flipped
  from DRAFT → NOT SENT / SUPERSEDED. Kept in doc for the
  paper trail but explicitly labelled "do NOT send". Roxanne's
  in-person answers recorded at the top of that section.
- **PR-4 doc** — Option B section becomes the confirmed path
  (no more "definite + default" hedging). Option A section
  retained as historical / superseded — do not execute.
- **PR-5 doc** — "Potential scope extension" for
  `originalReferralId` flipped to "Confirmed scope extension".
  Code positions kept, (a)/(b) decision block removed.
- **Top-level plan** — Q1 block gains a ✅ ANSWERED banner
  pointing at the corrected Option B; PR-4 detail simplified
  to just Option B corrected; PR-5 detail extended with the
  `originalReferralId` strip.
- **Status table** at top of this log — PR-4 flipped from
  🚫 blocked → ⬜ ready to start; PR-5 note extended.

**Sequencing decision.** With Q1 out of the way and PR-3 still
in review, we can start PR-4 (corrected Option B) as soon as
PR-3 merges. PR-5 stays after PR-4 (rebase off `main`) to keep
snapshot diffs readable, as originally planned.

**No PR is being cut for this — docs / framing update only.
The PR-4 code branch is now the next thing off the rank once
PR-3 merges.**

### 2026-08-04 (pm, end of day) — Q2 closed on default → APG-2546 has zero open external questions

**What happened.** Q2 (`is_national` on `SarOrganisation`, DD
row 109) was left open when Q1 got answered in person. Roxanne
had face-to-face contact this afternoon and did not raise
`is_national`. Under the terms of the 2026-08-03 follow-up
("I'd assume `is_national` is 'leave it off' unless you say
otherwise") that in-person window without an override locks
the default in.

**Resolution.** Q2 closed on the "leave off" default, consistent
with APG-2494's earlier won't-do call. No code change; no add
work in APG-2546. If Roxanne raises `is_national` later, spin a
fresh ticket — do not fold in.

**DD state check** (for provenance):
- DD row 109 Column H = Yes (Roxanne flipped 10.07 based on a
  dev telling her it should be there) + dev note "should be in
  new SAR report form".
- `SarOrganisation` DTO field set (verified 2026-08-04):
  `id`, `code`, `name`, `gender`. No `isNational`.
- Confirmed via `sar-api-response.json` fixture — organisations
  render `{"id","code","name","gender"}` only.

**Residual DD drift** (not APG-2546 scope, not blocking):
- DD row 109 still says Column H = Yes; code says No. If we
  want zero DD drift, ask Roxanne to flip Column H back to No
  on her next DD pass. Nice-to-have. Logged here so it's not
  forgotten.

**Impact.**

- **Top-level plan** — Q2 block flipped from "Do not attempt to
  add until Roxanne confirms" + default banner → ✅ CLOSED on
  default banner + kept the historical context for provenance.
  New heading "No open questions blocking APG-2546" now names
  the state explicitly.
- **Status table** at top of this log — Q2 blocking references
  removed; new "external questions" row added showing all
  external clarifications closed.
- **`00-roxanne-followup.md`** — defaults section flipped to
  show both Q1 and Q2 as resolved (Q1 in person, Q2 on default).

**No external actions blocking APG-2546.** Next step is
purely internal: get PR-3 merged, then PR-4 (corrected Option
B), then PR-5 (three-strip), then PR-6 (round-2 handover).

**No PR is being cut for this — docs / framing update only.**

### 2026-08-04 — PR-3 merged

- **PR link:** #1110.
- **Merge commit on `main`:** `d6587351`.
- **Sample PDF page count post-PR:** 3 pages (unchanged from
  post-PR-2 baseline — PR-3 removed two sections, but the rows in
  question were low-volume in the fixture).
- **Reviewer:** _(fill in from GitHub once notification lands)_.
- **Notes / surprises:** none blocking. Blank-line convention amend
  handled inline on the branch pre-merge.

### 2026-08-05 (am) — PR-4 merged

- **Option applied:** B (corrected — strip `pniResultId` +
  `oasysAssessmentId`, keep `prisonNumber` + `programmePathway`).
- **PR link:** #1111.
- **Merge commit on `main`:** `2a79b856`.
- **Sample PDF page count post-PR:** 3 pages (unchanged — Option B
  is a two-field DTO trim, not a whole-section removal).
- **Reviewer:** _(fill in from GitHub once notification lands)_.
- **Notes / surprises:** none blocking. Q1's in-person answer
  eliminated the "Option A vs B" branching — clean execution of
  Option B directly per the confirmed scope.

### 2026-08-05 — PR-5 opened + nine-lens review clean + PR-7 spun off review flag

- **Branch:** `APG-2546/strip-internal-ids` (from `main` @ `2a79b856`,
  tip-of-`main` after PR-4 merged).
- **PR link:** #1112.
- **Head commit on branch:** `677c8ea2` (single commit — no
  review-fix amend needed).
- **Files changed:** 5 files, +1 / −15.
- **Verification:** `./gradlew ktlintCheck test` green (678 tests),
  snapshots regenerated (`sar-api-response.json` +
  `sar-expected-render-result.html`), `entity-schema.json`
  correctly untouched, sample PDF 3 pages.
- **DD cross-check:** Ran `python3 doc/planning/APG-2546/scripts/dd-notes-sweep.py`
  against the working xlsx and confirmed all three targeted rows
  verbatim:
  - Row 105 (`organisation.organisation_id`) — "After call with
    Raby 29.07 - this should be a no - No Ids to be included in
    SAR reports"
  - Row 111 (`person.person_id`) — same note verbatim
  - Row 165 (`referral.original_referral_id`) — "pull referral
    data (if not already) do not add the uuid"
  Also re-confirmed rows 127/128/131 (SarPniResult IDs) are
  already Mand=Yes/SAR=No/API=No — the PR's "no-op / already
  absent" claim is accurate.
- **Nine-lens agent review (all green):**
  correctness ✅ (DTO / mapper / template edits map 1:1 to DD
  rows), tests ✅ (three dropped assertions correspond exactly to
  the three removed DTO fields; test-scope local vars remain live
  as seed / mock inputs), positional-arg alignment ✅ (`toSarReferral`
  uses positional construction — 14 DTO fields align with 13
  positional + 1 named mapper arg by type and order), API/contract
  ✅ (no consumers outside `SubjectAccessRequestService.kt` — grep
  clean), data/migrations ✅ (no JPA edits, `entity-schema.json`
  unchanged), security/privacy ✅ (net win — three UUIDs no longer
  egress via SAR), performance ✅ (unchanged), observability ✅
  (unresolved-original `log.warn` preserved), style/conventions ✅
  (matches PR-4 pattern exactly — DTO delete + mapper delete +
  template row delete + snapshot regen), devex/docs/rollback ✅
  (PR body from doc template, rollback = single `git revert`).
- **One non-blocking flag → spun as PR-7:** the nested
  `SarOriginalReferral.id` UUID was retained in PR-5 per the doc's
  explicit "sub-block is unaffected" instruction. On reflection
  with Raby 2026-08-05, Roxanne's blanket "no IDs to be included
  in SAR reports" rule from rows 105 + 111 covers this too —
  internal referral primary keys are opaque to the subject once
  the resolved details (course name, submitted on, status, etc.)
  are rendered next to them. Spun as `PR-7-strip-original-referral-uuid.md`
  rather than folded back into PR-5 to preserve the review signal
  and keep the "one field per PR, snapshot diff obvious" rhythm.
- **Impact on PR-6 (OSAR handover):** pushed one slot in the
  sequence — trigger PR-6 after **PR-7** is on `main` (not PR-5)
  so the round-2 PDF Cameron's team wraps reflects the final
  zero-UUID content shape. Status table + `README.md` sequencing
  updated to match.

### YYYY-MM-DD — PR-5 merged

- **PR link:** _()_.
- **Merge commit on `main`:** _()_.
- **Sample PDF page count post-PR:** _()_.
- **Reviewer:** _()_.
- **Notes / surprises:** _()_.

### YYYY-MM-DD — PR-7 merged

- **PR link:** _()_.
- **Merge commit on `main`:** _()_.
- **Sample PDF page count post-PR:** expected 3 (fixture doesn't
  seed a resolvable `originalReferral` — nested block never
  renders in the SAR contract snapshot). Record actual.
- **Snapshot diff observed:** expected none (see PR-7 doc "Non-obvious
  things §1"). Record actual — any diff signals fixture drift and
  should be investigated before merge.
- **Reviewer:** _()_.
- **Notes / surprises:** _()_.

### YYYY-MM-DD — PR-6 merged

- **PR link:** _()_.
- **Merge commit on `main`:** _()_.
- **Handover artefacts:**
  - PDF: `~/Downloads/sar-dev-3/sar-generated-report.pdf` (N pages)
  - JSON: `~/Downloads/sar-dev-3/sar-api-response.json` (N KB)
  - HTML: `~/Downloads/sar-dev-3/sar-expected-render-result.html` (N KB)
- **Round-1 → round-2 delta:** _(page count / size delta)_.
- **APG-2495 run-log entry appended:** _(yes/no)_.
- **APG-2546 planning-doc artefacts table filled in:** _(yes/no)_.

### YYYY-MM-DD — OSAR handover email sent

- **Recipients:** Sharon, Roxanne, William Falconer, QAT (+ others?)
- **Subject line:** _()_.
- **Thread link / archive:** _()_.
- **Requested response deadline:** _(5 working days from send)_.

### YYYY-MM-DD — OSAR content sign-off received

- **Signed off by:** _()_.
- **Any residual asks:** _()_. If any, decide inline whether to
  fold into APG-2546 or spin follow-up (usually the latter — see
  planning-doc "Rollback plan" §).

### YYYY-MM-DD — Ticket closed

- **APG-2546 ticket state:** transitioned to Done.
- **Follow-ups spun:**
  - _(APG-XXXX if Q2 = "add" was chosen)_
  - _(APG-XXXX if OSAR came back asking for a partial restoration)_
  - _(any cleanup-dead-repo-methods follow-up)_

## Handoff prompt for a fresh chat

When starting a new chat to execute a specific PR, paste the
following — replace `N` with the PR number:

> I'm executing APG-2546 PR-N. The working doc is at
> `doc/planning/APG-2546/PR-N-<slug>.md`. Please read it end to
> end, follow the "Files to change" section literally, run the
> snapshot regeneration, run the verification checklist, and open
> a PR using the description template at the bottom of the doc.
>
> Assumed starting point: tip of `main` after PR-(N-1) has merged.
> If PR-(N-1) hasn't merged yet, stop and tell me before touching
> anything.
>
> When you're done, report back the merge SHA, PDF page count,
> and any surprises so I can update
> `doc/planning/APG-2546/DELIVERY-LOG.md` in the tracking chat.

## Contingencies

- **Roxanne asks a question we didn't anticipate** → answer in the
  same thread as her question, then record it here under a new
  timeline entry, and update the PR-4 (or PR-5) doc if it changes
  the technical scope.
- **A PR review asks for a substantial change** → decide whether
  to (a) fold in on the branch, (b) split into a follow-up PR
  within APG-2546, or (c) spin a new ticket. Record the decision
  and reason here.
- **A snapshot regen produces an unexpectedly large diff** → stop,
  investigate, and update the log with the finding before merging.
  Most likely cause is a merge-conflict resolution error in an
  earlier PR.
- **The 8,000-page complaint reappears in round-2 review** → check
  the PDF page count in the log against the round-2 handover
  artefact; if they match, the aggregator rendering has changed
  post-handover — kick to `#haa-sar-functionality-change-request`
  and record here.

## Related tickets and channels

- **Blocked by:** none.
- **Depends on:** APG-2492, APG-2493, APG-2510 (all merged).
- **Blocks:** OSAR sign-off for the round-2 review.
- **Related channels:** `#osar-review`, `#haa-sar-functionality-change-request`
  (aggregator dev-portal / cover-sheet work — out of scope for
  this ticket).
- **Predecessor round-1 note:** `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`.



