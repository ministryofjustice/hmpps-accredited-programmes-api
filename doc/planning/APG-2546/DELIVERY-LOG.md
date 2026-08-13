# APG-2546 delivery log

Running log for the whole ticket. This chat is the coordinating
context — each PR is executed in its own fresh chat using the
matching working-notes doc, but the outcome (merge SHA, PDF page
count, artefacts) is recorded here as soon as it lands, so we have
one place to look for state and can close the ticket cleanly at the
end.

## Status at a glance

> **📉 Content-readability target hit + all six code PRs on `main`.**
> With PRs 1–5 + 7 merged (PR-7 merged `baee4510` 2026-08-07),
> the **test-harness** SAR PDF has dropped from the round-1
> baseline of ~8,000 pages to **3 pages** and has held at 3
> pages since PR-2 (later PRs strip fields inside already-rendered
> sections rather than removing sections). This is the
> chrome-less Option 2 output (no cover, no headers, no footers)
> — the same content that Cameron's team's SAR product wraps in
> the full OSAR-quality PDF under Option 1 (the OSAR-preferred
> handover route). So it's both a genuine internal readability
> metric *and* representative of what OSAR will see, minus
> chrome. The "8,000-page complaint" is fixed at the content
> level and the last raw-UUID scrub (PR-7 nested
> `originalReferral.id`) is now on `main`. Only APG-2546 work
> remaining is **PR-6** (docs-only round-2 OSAR handover) and
> the round-2 content sign-off it kicks off.

| Item | State | Notes |
|---|---|---|
| Planning branch (`APG-2546/planning-sar-field-removals`) | ⏳ in flight | Committed and pushed rolling; typically local ahead of origin by a small number of commits between chats. |
| Q1 to Roxanne (`oasys_pni_result` A vs B) | ✅ answered 2026-08-04 pm in person → corrected Option B | Strip `pniResultId` + `oasysAssessmentId`; keep `prisonNumber` + `programmePathway`. See "Roxanne in-person answers" timeline entry. |
| Q2 to Roxanne (`is_national` on organisation) | ✅ closed 2026-08-04 pm on "leave off" default | Roxanne had in-person window on 2026-08-04 pm and did not raise; per 2026-08-03 follow-up terms this locks the default in. APG-2494 stays won't-do. If raised later, spin fresh ticket. See "Q2 closed on default" timeline entry. |
| PR-1 — remove `auditRecords` | ✅ merged `50f67cff` 2026-08-03 | PR #1107. 9-lens agent review all green. Branch head `04ab44ed` (initial `4801f6e6` + review-fix `04ab44ed`). |
| PR-2 — remove `referralStatusHistory` + `referralStatusReasons` | ✅ merged `cd306c99` 2026-08-03 | PR #1109. Branch head was `f890b221` (initial `22c97122` + review-fix amend). No deviations from doc. Sample PDF post-PR-2 = **3 pages**. |
| PR-3 — remove `sexualOffenceDetails` + `selectedSexualOffenceDetails` | ✅ merged `d6587351` 2026-08-04 | PR #1110. Branch head `fc5ae133` (incl. review-amend for blank-line convention). |
| PR-4 — strip `pniResultId` + `oasysAssessmentId` from `oasysPniResults` (Option B corrected) | ✅ merged `2a79b856` 2026-08-05 am | PR #1111. Q1 answered in person 2026-08-04 pm → Option B (corrected). Stripped `pniResultId` + `oasysAssessmentId`; kept `prisonNumber` + `programmePathway`. Sample PDF: 3 pages. |
| PR-5 — strip `SarPerson.id` + `SarOrganisation.id` (+ `SarReferral.originalReferralId`) | ✅ merged `50968d07` 2026-08-05 | PR #1112. Branch head was `677c8ea2`. 9-lens review 2026-08-05 all green + one non-blocking flag (retained `SarOriginalReferral.id`) → spun as PR-7. |
| PR-7 — strip retained `SarOriginalReferral.id` UUID | ✅ merged `baee4510` 2026-08-07 | PR #1113. Branch head `1cc54e9d`. Snapshot regen produced zero diff as predicted by PR-7 doc "Non-obvious things §1" — fixture doesn't seed a resolvable `originalReferral`. Fixture-hardening item flagged during nine-lens review → recorded in "Deferred follow-ups" section below (not APG-2546 scope). Sample PDF: 3 pages. |
| PR-6 — OSAR round-2 handover | ⬜ ready to start (unblocked 2026-08-07 by PR-7 merge) | **Scope re-updated 2026-08-04 pm:** Option 1 primary (full-chrome PDF from Cameron's SAR dev service, OSAR-preferred), Option 2 fallback (chrome-less test harness). Post template-registration on `#haa-sar-functionality-change-request` **now** — round-2 PDF now reflects the final zero-UUID content shape. Doc pre-refreshed 2026-08-05 with Q1 resolved, PR-5 + PR-7 field-removals folded in, expanded cross-check block, and P.S. to Roxanne re residual DD drift on rows 109 + 224 — see `PR-6-osar-round-2-handover.md`. |
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
- DD row 224 (`referrer_user.referrer_username`) note reads
  *"Yes if we can provide surname"* — implying Column H should
  now be Yes (since we do surface a surname via
  `surnames.forUsername(referrer.username)` — APG-2492). Column
  H currently says No. Same DD-drift pattern; flag on the same
  next DD pass. Surfaced 2026-08-05 during the full-DD-sweep
  validation of PR-7 planning docs — see top-level plan §D.

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

### 2026-08-05 — PR-5 merged

- **PR link:** #1112.
- **Merge commit on `main`:** `50968d07`.
- **Sample PDF page count post-PR:** 3 pages (unchanged — PR-5
  strips DTO fields on already-rendered sections).
- **Reviewer:** _(fill in from GitHub once notification lands)_.
- **Notes / surprises:** none blocking. 9-lens agent review 2026-08-05
  am was all green; only non-blocking flag (retained
  `SarOriginalReferral.id` UUID on the nested sub-block) was
  spun into PR-7 (#1113) rather than folded back into PR-5 —
  see "PR-5 opened + nine-lens review clean + PR-7 spun off
  review flag" entry above for the reasoning.

### 2026-08-05 — PR-7 opened as draft + agent 9-lens review clean

- **Branch:** `APG-2546/strip-original-referral-uuid` (from `main`
  @ `50968d07`, tip-of-`main` after PR-5 merged).
- **PR link:** #1113 (opened as draft — awaiting peer review).
- **Head commit on branch:** `1cc54e9d` (single commit; no
  review-fix amend on the branch — the fixture-hardening item
  raised in the nine-lens review was deferred rather than
  folded in, see below).
- **Files changed:** 3 files, +0 / −4 (2 DTO/mapper lines, 1
  template row, 1 test assertion).
- **Verification:** `./gradlew ktlintCheck test` green (678 tests),
  snapshot regen run — **zero diff** on `sar-api-response.json` +
  `sar-expected-render-result.html` as predicted by PR-7 doc
  "Non-obvious things §1" (integration-test fixture doesn't seed
  a resolvable `originalReferral`, so the `{{#originalReferral}}`
  block never renders in the fixture and removing a field from
  its DTO doesn't diff the golden output). Coverage of the
  removal lives in the unit test's dropped
  `assertThat(originalReferral.id).isEqualTo(originalReferralId)`
  assertion. Sample PDF: 3 pages.
- **Nine-lens agent review outcome:** all lenses green.
  correctness ✅ (DTO / mapper / template / test edits map 1:1
  to the doc's "Files to change" spec), tests ✅ (dropped
  assertion is the only meaningful test change; local
  `originalReferralId` var still drives factory + mock stubs so
  isn't dead), API/contract ✅ (no consumers of
  `SarOriginalReferral.id` outside `SubjectAccessRequestService.kt`
  per grep), data/migrations ✅ (no JPA edits;
  `entity-schema.json` unchanged),
  security/privacy ✅ (net win — last raw internal UUID egress
  removed from the SAR API surface),
  performance ✅ (unchanged), observability ✅ (unresolved-original
  `log.warn` preserved), style/conventions ✅ (matches PR-4 + PR-5
  named-arg-mapper pattern), devex/docs/rollback ✅ (rollback =
  single `git revert`, PR body from doc template).
- **One non-blocking flag deferred out of scope → Deferred
  follow-ups section:** the SAR contract fixture doesn't seed a
  resolvable `originalReferralId`, so the golden snapshots don't
  actually observe the change and would miss any future
  accidental re-introduction of a raw UUID inside the sub-block.
  Fixture hardening (seed one extra referral + resolvable
  original, regen snapshots to include the sub-block) is a
  belt-and-braces test-hygiene item, **not APG-2546 scope**.
  Captured in "Deferred follow-ups (out of APG-2546 scope)"
  section below with trigger conditions for promoting it to a
  real ticket. Rationale for not folding into PR-7: PR-7 is a
  removal PR, fixture-widening is an additive PR; separate
  concerns, separate tickets.
- **Impact on PR-6 (OSAR handover):** none — PR-6 doc was
  already refreshed 2026-08-05 to reflect PR-7 as the trigger
  point and to fold PR-7's `SarOriginalReferral.id` removal
  into the OSAR email draft. Waits for #1113 to merge before
  kicking off.

### 2026-08-07 — PR-7 merged

- **PR link:** #1113.
- **Merge commit on `main`:** `baee4510` (full SHA
  `baee45103cb07cc6f0c00ed128b51730deaaf1c1`), merged 2026-08-07 14:42 UTC.
- **Sample PDF page count post-PR:** **3 pages** (unchanged — as
  predicted; fixture doesn't seed a resolvable `originalReferral`
  so the nested block never renders in the SAR contract snapshot).
- **Snapshot diff observed:** **zero** on both
  `sar-api-response.json` and `sar-expected-render-result.html`,
  exactly as PR-7 doc "Non-obvious things §1" predicted. No
  fixture drift.
- **Reviewer:** _(fill in from GitHub notification when convenient
  — Raby self-merged after peer review approval)._
- **Notes / surprises:** none blocking. Nine-lens agent review
  pre-merge was all green. Only non-blocking item (contract-test
  fixture doesn't exercise the sub-block, so the change isn't
  snapshot-visible) already captured in "Deferred follow-ups" —
  now warm not cold thanks to Deborah's independent interest in
  the fixture for vettor training (see next entry).
- **Impact on PR-6:** unblocks it immediately. Post the
  template-registration on `#haa-sar-functionality-change-request`
  today — round-2 artefacts now reflect the final zero-UUID
  content shape.
- **All six APG-2546 code PRs are now on `main`.** Remaining
  APG-2546 work is docs-only PR-6 handover + round-2 sign-off.

### 2026-08-06 — Deborah fixture-interest thread → deferred follow-up promoted from cold to warm

**What happened.** Follow-on to the 2026-08-04 pm Deborah exchange.
On 2026-08-06 Deborah messaged: *"Also a good shout on populated
fields — worth me looking at our fixture / seed data before we
hand anything over so the vettors get a proper training set."*
i.e. she is volunteering to eyeball
`src/test/resources/sar/sar-api-response.json` before PR-6's
handover pack ships, specifically to judge whether an Option 2
fallback PDF would be a genuine vettor training exemplar or a
"labels only, half the values null" placeholder.

**Impact.**

- **The "Deferred follow-ups" fixture-widening item is no longer
  purely defensive belt-and-braces test-hygiene.** It has an
  active stakeholder driver (round-2 vettor training) and one of
  its written trigger conditions ("a stakeholder wraps a round-2
  artefact that needs snapshot-visible populated sub-block
  evidence") is now half-fulfilled — Deborah hasn't asked for it
  yet, but she's actively looking at whether to.
- **PR-6 execution has three branch conditions to prepare for**,
  depending on Deborah's post-look verdict:
  1. *Fixture is fine as-is* → ship Option 2 fallback with
     current fixture; PR-6 unchanged.
  2. *Widen fixture before shipping* → spin a small test-hygiene
     ticket (seed a resolvable original referral + populate
     currently-null derived fields), land it before PR-6's
     Option 2 artefact is regenerated. Naturally rolls up with
     the deferred follow-up. ~half a day of work.
  3. *Prefer Option 1 with a rich CRN* → PR-6 primary route
     anyway; fixture question moot for the shipped artefact but
     the deferred follow-up stays open for future belt-and-braces.
- **Nothing to do in code today.** The correct next action is a
  Raby-to-Deborah reply attaching the current fixture JSON +
  the post-PR-7 sample PDF (`build/test-generated/sar-generated-report.pdf`,
  3 pages, 6.8 KB) and asking for her steer on 1/2/3 above.
  Drafted as "Option A" in the 2026-08-06 chat.

**No PR is being cut for this — it's a status / framing update
on the planning branch only, coincident with the PR-7-merged
update.**

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

> **PR-7 exception to "PR-(N-1)":** the serial merge order is
> `1 → 2 → 3 → 4 → 5 → 7 → 6` (PR-6 is docs-only handover and
> lands after PR-7). For PR-7, "PR-(N-1)" means **PR-5 (#1112)**,
> not PR-6. Verbatim prompt for PR-7:
>
> > I'm executing APG-2546 PR-7. The working doc is at
> > `doc/planning/APG-2546/PR-7-strip-original-referral-uuid.md`.
> > Please read it end to end, follow the "Files to change"
> > section literally, run the snapshot regeneration (expect zero
> > diff — see "Non-obvious things §1"), run the verification
> > checklist, and open a PR using the description template at
> > the bottom of the doc. Do not deviate from the doc without
> > flagging it back to me first. Assumed starting point: tip of
> > `main` after PR-5 (#1112) has merged. If #1112 hasn't merged
> > yet, stop and tell me before touching anything.

## Contingencies

- **Roxanne asks a question we didn't anticipate** → answer in the
  same thread as her question, then record it here under a new
  timeline entry, and update the PR-5 (or PR-7) doc if it changes
  the technical scope. Q1 + Q2 already resolved 2026-08-04 pm, so
  new questions are less likely — but the residual DD-drift items
  on rows 109 and 224 are the most likely re-open path.
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

## Deferred follow-ups (out of APG-2546 scope)

Small items surfaced during APG-2546 delivery that are worth
capturing but don't belong in this ticket. Right home is a
separate ticket (SAR test hardening / hygiene), *not* a PR-N
planning doc under this folder. If any of these grow beyond
~30 minutes of work when picked up, the picker-up writes a
short plan then, with fresh context.

- **Seed a resolvable `originalReferral` in the SAR contract
  fixture.** Surfaced during PR-7 nine-lens review 2026-08-05.
  **Status: ticketed 2026-08-11** — picked up on branch
  `APG-2546/sar-contract-fixture-widening` (head `3969f93f`) off
  `main` @ `baee4510`; draft PR opened. Working doc:
  `doc/planning/sar-contract-fixture-widening.md` on this branch.
  **Outcomes:** 678 tests pass, ktlint clean, UUID-leak grep
  returns 0 on both goldens (APG-2546 UUID scrub preserved),
  sample PDF = 4 pages, snapshot goldens grew as predicted
  (`sar-api-response.json` 1762→2720 B; `sar-expected-render-result.html`
  11720→13681 B; `entity-schema.json` unchanged). Three
  deviations from the working doc surfaced during pickup and
  have been folded back into the doc (see its "Pickup notes
  2026-08-11" section) so a future re-run doesn't repeat the
  debugging: (i) stale line-number references — test file
  slimmed on `main` since doc was written; (ii)
  `SAR_GENERATE_ACTUAL=true` writes `.log` files under
  `src/test/resources/`, not `build/test-generated/sar-actual-*`;
  (iii) `PersistenceHelper.createPerson` needed a bind-type
  fix — the four date-string params bound as `varchar` and
  Postgres refused the implicit cast to `DATE` (fixed by
  parsing to `LocalDate` at bind time). Historical rationale
  preserved below.
  **Follow-on deviation #5 recorded 2026-08-11** — a second
  PDF-regen run hit non-determinism in
  `StaffRepository.findByPrisonNumber` (JPQL query had no
  `ORDER BY`; with the new two-staff fixture Postgres returned
  the join in arbitrary row order and the SAR-API-snapshot
  assertion flip-flopped between `[Doe, Bloggs]` and
  `[Bloggs, Doe]`). Fixed with `ORDER BY s.staffId` — commit
  `3312e63b` on the fixture branch. **Second `src/main/`
  deviation** — semantically a correctness hygiene fix
  (deterministic SAR ordering), but reviewers should call it
  out on the PR. 678 tests green post-fix.
  **Follow-on deviation #6 recorded 2026-08-11** — CI on PR
  #1115 caught a **second** ordering non-determinism, this
  time in `ReferralRepository.getSarReferrals` (also no
  `ORDER BY`; two referrals flip-flopped between chronological
  and reverse-chronological order across runs). Local runs had
  green-lit the golden in chronological order by luck. Fixed
  with `ORDER BY r.submittedOn NULLS LAST, r.id` — commit
  `bc9539b2`. **Third `src/main/` deviation**, same shape as
  #5. Also worth a follow-up scan of the other SAR-collection
  queries (course participation, PNI results, oasys PNI
  results) — the widened fixture still seeds only one row of
  each so they didn't surface yet, but they're the same class
  of latent flake once a widening adds a second row.
  **Follow-on deviation #7 recorded 2026-08-11 pm** — same-day
  follow-on to close out the latent-flake follow-up flagged
  under #6. Rather than spin a separate hygiene ticket the
  work was folded onto the same PR-1115 branch (agreed with
  Raby) since it's the same class of fix and the reviewer is
  already looking at the ORDER BY story. Commit `9bdb6f7a`.
  Three product-code ORDER BY additions + one staff-sort-key
  change + one Flyway migration + one test schema-version bump:
  (a) `CourseParticipationRepository.getSarParticipations` —
      added `ORDER BY cp.createdDateTime NULLS LAST, cp.id`.
  (b) `PniResultRepository.findAllByPrisonNumber` — promoted
      from Spring-Data derived query to explicit `@Query`
      with `ORDER BY p.pniAssessmentDate NULLS LAST,
      p.pniResultId`.
  (c) `OasysPniResultEntityRepository.findAllByPrisonNumber`
      — same conversion, `ORDER BY o.oasysAssessmentId NULLS
      LAST, o.pniResultId`.
  (d) `StaffRepository.findByPrisonNumber` — **changed** the
      #5 sort key from `s.staffId` to `s.lastName, s.staffId`.
      Reviewer preference for a semantically-natural PDF
      ordering (alphabetical by surname is the natural read
      for a vettor scanning the Staff section); `staffId`
      retained as tie-break. Snapshot goldens flipped
      `[Doe, Bloggs]` → `[Bloggs, Doe]` accordingly.
  (e) New Flyway `V145__add_staff_last_name_index.sql` —
      non-UNIQUE `idx_staff_last_name` backing the new sort.
      Same additive `IF NOT EXISTS` pattern as V144.
  (f) `SarContractIntegrationTest.expectedFlywaySchemaVersion`
      bumped `144` → `145` to match.
  Verification unchanged: 678 tests pass, ktlint clean,
  UUID-leak grep 0 on both goldens, sample PDF 4 pages,
  `entity-schema.json` unchanged. **Ops note:** V145 will
  run on the next dev deploy of this branch; idempotent,
  cheap, reversible, no impact on the Option 1 template
  registration + SAR-dev-service generation that's about to
  kick off (which reads the template not the DB layout).
  **One caveat during commit:** first commit attempt
  accidentally staged `.snyk` (generated file) + the DD
  working-copy xlsx via `git add -A`; caught immediately,
  reset --soft + re-commit + force-push-with-lease
  (`527d83a6` → `9bdb6f7a`). Neither file made it to origin
  on the second-push tree.
  **Review round 2 (2026-08-11 pm):** nine-lens re-review of
  `9bdb6f7a`. All lenses green; two agreed follow-ups actioned:
  (i) KDoc + V145 comment tweaks — standardised the four
  SAR-collection getters' KDoc into a common shape, added a
  cross-reference between siblings so the sweep is
  grep-navigable, and clarified that the ORDER BY guarantee
  applies to both the JSON payload and the rendered PDF (not
  just the PDF). V145 header comment got the same JSON+PDF
  clarification, DDL untouched. Doc-only, no snapshot change.
  Committed as `fe1257f3` (GPG-signed, explicit `git add
  <paths>` — no `-A` this time).
  (ii) Considered adding `@DataJpaTest` repository-level unit
  tests to lock the four new ORDER BYs at repo layer.
  **Deliberately deferred** — the ordering is already
  end-to-end pinned by `SarContractIntegrationTest`'s JSON +
  HTML goldens (which flipped `[Doe, Bloggs]` → `[Bloggs,
  Doe]` accordingly when the staff sort key changed, proving
  the goldens are load-bearing on order). Adding repo tests
  would need new testcontainer scaffolding on the three repos
  that don't currently have one plus dedicated multi-row
  fixtures per repo, and would duplicate assertions the
  golden already provides. Belongs in a broader "repository
  test harness" story, not bolted onto a hygiene PR.
  **Local PDF regenerated 2026-08-11 13:01** via
  `script/local-scripts/regenerate-sar-snapshots.sh` on top
  of `9bdb6f7a` to keep the on-disk artefact in sync with
  the committed goldens. Idempotent (`git status --short`
  showed no golden diffs), 4 pages, 9,080 bytes. Confirms
  committed goldens match a fresh regen bit-for-bit.
  **Status warmed 2026-08-06** — Deborah (Cameron's team SDM)
  independently expressed interest in eyeballing the same
  fixture ahead of the round-2 handover to judge whether it's
  rich enough to be a genuine vettor training exemplar under
  Option 2. Trigger condition (a) below is now partially
  fulfilled and awaiting Deborah's post-look verdict — see
  "2026-08-06 — Deborah fixture-interest thread" timeline
  entry above.
  The integration-test fixture (`SarContractIntegrationTest`
  `setupTestData()`) currently seeds a single referral with
  `originalReferralId = null`, so the `{{#originalReferral}}`
  mustache block never renders and the sub-block never appears
  in the golden JSON / HTML snapshots. Consequence: PR-7's
  removal of `SarOriginalReferral.id` produced zero snapshot
  diff (correct behaviour — see PR-7 doc "Non-obvious things
  §1"), and any *future* accidental re-introduction of a raw
  UUID inside the sub-block would slip past the contract test.
  The unit test (`SubjectAccessRequestServiceTest`) carries
  correctness coverage today; this follow-up is defensive
  belt-and-braces *plus* now a possible vettor-training-quality
  driver. Estimated shape: seed one extra referral row + one
  resolvable "original" row + populate currently-null derived
  fields (`referrerOverrideReason`, `hasReviewedAdditionalInformation`,
  various dates) with realistic dev-shaped values, regen
  snapshots, verify sub-block appears in golden JSON and
  mustache `<tr>`s appear in golden HTML. ~20–40-line diff
  (fixture + snapshot bytes) depending on scope of populated-fields
  widening. Trigger conditions to promote to a real PR:
  (a) a stakeholder (e.g. Deborah / OSAR) wraps a round-2
  artefact that needs snapshot-visible populated sub-block
  evidence — **partially fulfilled 2026-08-06**, or (b) a
  future UUID re-introduction slips past review — either signal
  flips this from nice-to-have to need-to-have.

## Related tickets and channels

- **Blocked by:** none.
- **Depends on:** APG-2492, APG-2493, APG-2510 (all merged).
- **Blocks:** OSAR sign-off for the round-2 review.
- **Related channels:** `#osar-review`, `#haa-sar-functionality-change-request`
  (aggregator dev-portal / cover-sheet work — out of scope for
  this ticket).
- **Predecessor round-1 note:** `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`.




---

# Round 2 — 2026-08-13 kickoff

## Context

Round 1 closed 2026-08-11 with PR #1115 merged (widened fixture + SAR-collection `ORDER BY` hygiene). The 2026-08-12 sample PDF generated from preprod CRN A9648CH via Cameron's SAR dev-service (Option 1, full-chrome) was sent to Branston (OSAR) for round-2 review.

On 2026-08-13 (13:25) Deborah (SDM, Cameron's SAR product team) came back with a follow-up action list from the review meeting. Verbatim:

1. Remove NOMIS IDs and CRNs as they are in the header.
2. Remove PNI data and this is retrieved from ARNs via the Probation Hub request.
3. Remove Personal Data section.
4. Add organisation field to the referral rather than list separately so it is in context.
5. Add staff name field to the referral rather than list separately so it is in context.

Raby DM'd Deborah to clarify #5: the referral already carries `primaryPomStaffSurname` + `secondaryPomStaffSurname` inline (added in PR-5). Options: (a) drop the redundant top-level `staff[]` list, keep the inline surname fields; or (b) upgrade the two surname fields to full names (forename + surname) and remove the top-level list. **Deborah confirmed (a).** Locked.

Round-2 delivery scaffolded on this planning branch as PRs 8–13 (continuation of the PR-1…PR-7 sequence). New overview: [`ROUND-2-PLAN.md`](./ROUND-2-PLAN.md). Round-2 working docs:

- [`PR-8-remove-pni-oasys-person.md`](./PR-8-remove-pni-oasys-person.md) — three-section removal, fully drafted, agent-executable
- [`PR-9-scrub-nomis-and-crn.md`](./PR-9-scrub-nomis-and-crn.md) — skeleton
- [`PR-10-organisation-into-referral.md`](./PR-10-organisation-into-referral.md) — skeleton
- [`PR-11-remove-top-level-staff.md`](./PR-11-remove-top-level-staff.md) — skeleton
- [`PR-12-round-2-hygiene-tidy.md`](./PR-12-round-2-hygiene-tidy.md) — skeleton
- [`PR-13-round-2-docs-and-handover.md`](./PR-13-round-2-docs-and-handover.md) — skeleton

## DD row 139 override

Roxanne's Digital Data review row 139 (`pni_result . pni_result_json`, SAR=Yes, In SAR API=Yes, note *"these are in SAR report hence H should be Yes. Updated"* dated 2026-07-10) is **superseded** by Deborah's 2026-08-13 meeting outcome.

Rationale: PNI data (both `pniResults[]` and `oasysPniResults[]`) is now sourced by SAR consumers via the ARNs Probation Hub feed, so replicating it in the Accredited Programmes SAR report is duplicative and confusing for redaction reviewers.

Deborah aware; may loop Roxanne to annotate row 139 for future DD refreshes so nobody re-adds `pni_result_json` on a subsequent sweep. Recorded here + in [`ROUND-2-PLAN.md`](./ROUND-2-PLAN.md) §"DD spreadsheet override".

## Impact on PR #1115 (recorded up front)

Two queries + one persistence-helper stanza from PR #1115 become dead code as round-2 sections are deleted. **Orphan-audit outcomes verified 2026-08-13 pm against `origin/main` @ `0cf89850`** (see [`ROUND-2-PLAN.md`](./ROUND-2-PLAN.md) §"Impact on PR #1115" for the full matrix):

| From PR #1115 | Fate | Handled in |
|---|---|---|
| `PniResultRepository.findAllByPrisonNumber` `@Query` + ORDER BY | 🛑 **STAY** — `PersonService.kt:287` is a prod caller (prisoner-merge handler) | PR-8 |
| `OasysPniResultEntityRepository.findAllByPrisonNumber` `@Query` + ORDER BY | 🗑️ **DELETE** — prod-orphan after PR-8 | PR-8 |
| `PersistenceHelper.createPerson` `LocalDate` bind fix | **STAY** in helper — `SubjectAccessRequestServiceIntegrationTest.kt:94` also calls it. Remove only the SAR-contract-test call site. | PR-8 |
| `StaffRepository.findByPrisonNumber` surname-sort `@Query` | 🗑️ **DELETE** — prod-orphan after PR-11 | PR-11 |
| `V145__add_staff_last_name_index.sql` | Stays. Flyway is forward-only; additive + reversible; costs nothing to leave in place. | — |
| `ReferralRepository.getSarReferrals` ORDER BY | **Stays useful** (referrals retained) | — |
| `CourseParticipationRepository.getSarParticipations` ORDER BY | **Stays useful** (courseParticipation retained) | — |
| Fixture widening: `originalReferral` sub-block + second-POM seed | Mostly stays useful | — |
| Fixture widening: `person` widening + PNI widening stanzas | Deleted along with their sections | PR-8 |

One dead query (OasysPniResult) + one dead SAR-only query (StaffRepository.findByPrisonNumber) + one dead helper-call site (createPerson in SAR contract test) is the total sunk-cost from PR #1115. Cheap. Paper trail here.

## Timeline (round 2)

- **2026-08-13 13:25** — Deborah's action list received (Slack).
- **2026-08-13 pm** — Raby DM'd (a) vs (b) clarification for ask #5; Deborah confirmed (a).
- **2026-08-13 pm** — Round-2 planning docs scaffolded on `APG-2546/planning-sar-field-removals`:
  - `ROUND-2-PLAN.md` created
  - `PR-8-remove-pni-oasys-person.md` fully drafted, agent-executable
  - `PR-9…PR-12` skeletons created for later expansion
  - This DELIVERY-LOG round-2 section appended
- **2026-08-13 pm** — Structural revision: PR-12 split into hygiene (PR-12) + docs handover (PR-13). `AGENT-PROMPT-TEMPLATE.md` added.
- **2026-08-13 pm** — Nine-lens deep validation review executed against `origin/main` @ `0cf89850`. Six corrections applied:
  1. **PR-8 doc DTO location fixed** — top-level SAR response class is `Content` (nested in `SubjectAccessRequestService.kt`), not `SarResponse` in a separate file. All SAR DTOs are nested classes in the same file.
  2. **PR-8 orphan-audit outcomes locked in** — `PniResultRepository.findAllByPrisonNumber` **stays** (prod caller in `PersonService.kt:287` — prisoner-merge handler); `OasysPniResultEntityRepository.findAllByPrisonNumber` **deletes** (prod-orphan after PR-8); `PersistenceHelper.createPerson` LocalDate fix **stays** (other test callers). This corrects a dangerous "delete if grep-empty in src/main" instruction that could have caused a production regression.
  3. **PR-10 design decision superseded** — the "JPQL JOIN vs post-fetch" choice is moot; the service already uses `organisationRepository.findAllByCodeIn(...)` + `organisationNamesByCode` map threaded into `toSarReferral(...)` (line 109 on main) for `SarOriginalReferral` resolution. PR-10 scope is much smaller than the original doc suggested (½ day vs 1 day).
  4. **PR-11 orphan-audit outcome locked in** — `StaffRepository.findByPrisonNumber` is prod-orphan after PR-11; delete the SAR-only query. V145 index stays.
  5. **PR-8, PR-11 test-caller impact called out** — `SubjectAccessRequestServiceTest.kt` mock setups at lines 184/192/208/216/311/312/313/314 need explicit removal in the relevant PR.
  6. Parallelisation claim tightened — PR-9/10/11 all touch `SubjectAccessRequestService.kt` + mustache; recommend serial over parallel to avoid merge conflicts.
- **2026-08-13 pm (third-pass exhaustive review)** — Third and final validation pass surfaced **major missed test impact** in the round-2 docs. `SubjectAccessRequestServiceIntegrationTest.kt` (integration test with real fixture + field-level DTO assertions) and `SubjectAccessRequestServiceTest.kt` (unit test with `with(content) { assertThat(pniResults.size) … }` style assertions) both have compile-breaking assertions on the fields PR-8 removes. Same class of issue for PR-9 (2 lines), PR-10 (4 lines + one recommended add), and PR-11 (4 lines). PR-8/9/10/11 docs updated with **exact line-number-per-file tables** so agents don't need to re-derive. Also verified: `DomainEventsListenerTest.kt` and `CourseParticipationControllerIntegrationTest.kt` are NOT affected (grep-confirmed).
- **2026-08-13 pm** — Nine-lens post-review polish commit landed (drift/wording only; zero code-fact changes). Fixed PR-12→PR-12/PR-13 split fallout across `ROUND-2-PLAN` / `DELIVERY-LOG` / `PR-13` / `AGENT-PROMPT-TEMPLATE`; clarified PR-9/10/11 sequencing is merge-conflict avoidance not a code dep; corrected PR-8 rollback reversed-logic wording; added PACT + OpenAPI verification steps; added fresh-checkout warnings (R6) and .snyk/xlsx paper-cut (R7) to risk register. Full paper trail in the commit message.
- **2026-08-13 pm** — **R1 CLOSED.** SAR wrapper team (Cameron's team) responded to the header-ownership check that PR-8 non-obvious #3 and PR-9 notes had flagged as needing Slack confirmation. Verbatim reply:

  > *"Yes — confirm we retrieve the information for the header from two APIs — one for NOMIS IDs and one for nDelius CRNs. We do not in any way retrieve that data from their product — so it's safe to remove it as the OSAR team requested."*

  Meaning: the SAR wrapper injects NOMIS ID (from a NOMIS API) and nDelius CRN (from a separate nDelius API) into the wrapper header from its own upstream sources — it does **not** consume those keys from the Accredited Programmes SAR payload. Safe to strip.

  Covers both PR-8 (person block, NOMIS ID) and PR-9 (CRN + prisonerNumber). PR-8's "Non-obvious #3" and PR-9's "Notes for the agent" both updated to reference this confirmation; ROUND-2-PLAN R1 row flipped to ✅ RESOLVED. No further wrapper-team confirmation needed for round-2 execution.

## Round 2 — PR outcomes

_(To be filled in as PRs land.)_

| PR | Working doc | Branch | PR # | Merged | SHA | Notes |
|---|---|---|---|---|---|---|
| PR-8 | `PR-8-remove-pni-oasys-person.md` | `APG-2546/remove-pni-oasys-person` | _pending_ | _pending_ | _pending_ | — |
| PR-9 | `PR-9-scrub-nomis-and-crn.md` | `APG-2546/scrub-nomis-and-crn` | _pending_ | _pending_ | _pending_ | — |
| PR-10 | `PR-10-organisation-into-referral.md` | `APG-2546/organisation-into-referral` | _pending_ | _pending_ | _pending_ | — |
| PR-11 | `PR-11-remove-top-level-staff.md` | `APG-2546/remove-top-level-staff` | _pending_ | _pending_ | _pending_ | — |
| PR-12 | `PR-12-round-2-hygiene-tidy.md` | `APG-2546/round-2-hygiene-tidy` | _pending_ | _pending_ | _pending_ | Needs post-PR-8/9/10/11 combined state to sanity-grep — split out from what was originally PR-12 (docs) on 2026-08-13 pm. |
| PR-13 | `PR-13-round-2-docs-and-handover.md` | `APG-2546/round-2-docs-handover` | _pending_ | _pending_ | _pending_ | — |

## Handover artefacts (round 2)

- Sample PDF sent to Branston round 2 (2026-08-12, from preprod CRN A9648CH): **superseded** by Deborah's round-2 asks.
- Sample PDF for round 3 (post PR-8…PR-11 merge): _pending — generated in PR-13_.

