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
- **2026-08-13 pm** — **Round-3 review scoped OUT of APG-2546.** Decision: any further OSAR feedback from Branston after PR-13's sample PDF lands — whether during the round-2 sprint or after close-out — is handled as a **separate ticket** (working name APG-25xx-round-3), not folded into APG-2546. Rationale: APG-2546 already spans two rounds and multiple weeks; a third round mid-flight would blur close-out signals, muddy the DD paper trail, and delay OSAR sign-off on the round-2 deliverable. Round-2 close-out signal is *feedback received*, not *feedback with zero further asks*. Process baked into `ROUND-2-PLAN.md` §"Out of scope (round 3+)" (verbatim-log path, new-ticket path, escalation path for security/compliance hard blocks). PR-13 "Not in scope" + "Close-out condition" sections updated to reflect. R5 mitigation flipped from "open PR-8b/PR-9b" (was: fold into APG-2546 as sub-PRs) to "spin new ticket" (out-of-scope).

  **Estimate for round-2 delivery** (recorded here as the paper-trail forecast before PR-8 opens):

  | Scenario | Working days |
  |---|---|
  | Pure dev-time (from PR estimates) | ~4½ days (PR-8 1½ + PR-9 ½ + PR-10 ½ + PR-11 ½ + PR-12 ½ + PR-13 1) |
  | Optimistic calendar (focused dev, same-day reviews) | ~6 working days (~1¼ weeks) |
  | **Realistic calendar** (round-1's actual cadence, one review cycle per PR) | **8–10 working days (~2 sprint weeks)** |
  | Slightly-slow (one of PR-9/10/11 needs a small follow-up commit — e.g. nullable-safety on `organisationNamesByCode`, or a preprod check that inline `primaryPomStaffSurname` populates on every referral before option (a) locks in; or a slow review day) | ~11–12 working days (~2¼ weeks) |

  **No "3-week pessimistic" scenario** — the two triggers that would have justified it are both closed:

  - **R1 (header ownership)** — closed 2026-08-13 by SAR wrapper team confirmation. No remaining external-dependency wait states.
  - **R5 (round-3 mid-sprint)** — scoped OUT of APG-2546 (see §"Round-3 review scoped OUT" above). Branston feedback on the PR-13 sample PDF does not block or extend round-2; it triggers a new ticket per the OOS process.

  Remaining unknowns (PR-9 grep miss, PR-10 nullable edge case, PR-11 inline-field coverage gap, PR-12 cross-PR straggler) are each *"add half a day to one PR"* size, not *"revise the plan"* size — the line-number, orphan-audit, and test-impact tables are third-pass verified against `origin/main @ 0cf89850`, so fresh agents shouldn't hit "the doc says X but the code says Y" surprises.

  Not included: OSAR round-3 review turnaround from Branston (budget 3–5 working days after PR-13 ships) — which per the OOS decision closes APG-2546 on *reply received* regardless of round-3 asks.
- **2026-08-14** — PR-8 opened as **#1116** on branch `APG-2546/remove-pni-oasys-person`, initial head `1b986744`. Base: `origin/main @ 0cf89850`. Clean execution against the doc; fresh-checkout warning (R6) caught the planning-branch worktree drift (agent switched to `origin/main` before reading files, exactly as intended).

  **Nine-lens self-review by executing agent before requesting reviewer** — all 14 lens rows green post-fix. Two findings surfaced:

  - **Finding 1 — FIXED before push.** Initial whole-file deletion of `OasysPniResultEntityRepository.kt` was inconsistent with round-1 PR-1 precedent: `AuditRepository` was kept alive as an empty `JpaRepository<AuditEntity, UUID>` shell after removing its only SAR-orphan method. Both entities behave identically post-round-2 (table + entity survive, no application-code writer, no non-SAR reader), so consistency argued for keeping the interface. Restored `OasysPniResultEntityRepository.kt` as a 9-line empty shell (preserves future-writer optionality on the still-declared `OasysPniResultEntity`). Commit amended `1b986744 → eb049a07`, force-pushed with `--force-with-lease`. Tests re-run: 678 pass, ktlint clean. PR body + commit message updated to explain the shell.
  - **Finding 2 — DEFERRED to PR-12 (already in doc directive).** `PersistenceHelper.createOasysPniResult` + `createPerson` are **genuinely orphaned** as of `origin/main @ 0cf89850`: grep proves both were only called from the two SAR-side sites PR-8 removed. This means PR-8's "Non-obvious #2" claim (*"leave the helper as-is — helper is called by other tests"*) is **stale on the anchor SHA**. PR-8 doc directive to defer to PR-12 hygiene was followed; PR-12 doc updated in this same commit to add explicit scope for these two helper removals. PR-8 doc "Non-obvious #2" flipped to SUPERSEDED with a pointer to this delivery-log entry so future readers see the correction.
  - **Finding 3 — NO ACTION.** All doc line-refs against `0cf89850` exact. Third-pass verification held.

  **Within-scope R3-spirit extension retained** (also called out on outcomes row): removed three now-dead SAR-service ctor params after verifying single public entry point + no other in-class callers; `PersonRepository` interface stays alive for its 6 other prod callers, only the SAR ctor injection removed.

  Final verification numbers on the pushed head `eb049a07`: 8 files, +3 / -358 (was +1 / -365 pre-amend), 678 tests pass, ktlint clean, 0 UUID leaks, 0 PACT contracts on SAR endpoint, no `@Operation`/`@Schema` on SAR controller (OpenAPI check N/A confirmed), `entity-schema.json` 0-line diff (byte-identical, 24 classes preserved). Test-harness (Option 2) PDF = 3 pages, unchanged from round-1 baseline. Deleted-DTO cross-refs / deleted-repo class refs / KDoc `[Symbol]` cross-refs / build+helm+config wiring: all 0 hits. JSON key order matches surviving `Content` ctor field order: `[referrals, courseParticipation, courses, staff, organisations]`.

  PR-9 unblocked to start from a fresh session once #1116 merges. When merge SHA lands, replace `_pending_` on the outcomes-table row + append merge timeline entry.

  Two paper-cuts surfaced during PR-8 execution, folded into `AGENT-PROMPT-TEMPLATE.md` §"Session-hygiene tips" so future PR agents skip them:

  - **Do not use inline zsh heredocs for commit messages** — multi-line quoted strings containing `{`, `#`, backticks or em-dashes get mangled and leave stuck `dquote>` prompts. Use file-write tool to drop the message into `/tmp/<ticket>-<step>-msg.txt`, then `git commit -F /tmp/...`.
  - **After `git checkout`, if `read_file` returns stale content that doesn't match the working tree**, cross-check with `sed -n 'A,Bp' <file>` or `git show <sha>:<file>` before acting. PR-8 hit a stale-cache `read_file` on `SubjectAccessRequestService.kt` immediately after switching from planning branch to `origin/main`; terminal-verified content matched the doc, nothing broke.
  - (Also noted: Docker Desktop must be running for Testcontainers-backed snapshot regen — `open -a Docker` on macOS if the daemon isn't up.)
- **2026-08-17** — **PR-8 merged to `main` as `b7b05283`** (PR #1116, "APG-2546: remove PNI + OASys PNI + Person sections from SAR"). `origin/APG-2546/remove-pni-oasys-person` auto-deleted post-merge. Total round-2 elapsed to first merge: 4 calendar days from PR-8 draft start (2026-08-14). No further findings from reviewer — self-review Findings 1 (empty JpaRepository shell) and 2 (`PersistenceHelper` orphan deferral to PR-12) held on merged code. Outcomes-table row flipped ✅ merged; PR-9 unblocked.

  **PR-9 re-anchoring done in this same delivery-log entry** — PR-8's cuts shifted downstream line-numbers in `SubjectAccessRequestService.kt` (`SarReferral` L174→L159, `SarCourseParticipation` L213→L198; ~−15 line shift on the surviving DTOs) and in `SubjectAccessRequestServiceIntegrationTest.kt` (`with(content.referrals[0])` block L123→L100; ~−23 line shift). Template `sar_template.mustache` L6 + L43 (both upstream of PR-8's deletions) unchanged. PR-9 doc "Scope" section updated to reference `b7b05283`-anchored line-refs; status flipped from "skeleton" to "ready for execution — line refs re-verified against `origin/main @ b7b05283`". `AGENT-PROMPT-TEMPLATE.md` PR-9 prompt updated with new anchor SHA.

  **PR-10 and PR-11 flagged for their own re-anchoring** — both docs now carry a ⚠️ "Line-ref re-anchoring required before execution" banner at the header. Their current line-refs still point at `0cf89850`; when PR-10 and PR-11 are picked up (each after the preceding PR merges), the fresh agent needs to re-verify the anchors against the then-current `origin/main` HEAD using the same pattern PR-9 followed. Not re-verifying them now because PR-9 and PR-10 will each shift the SAR service + template further, so any pre-emptive update would be stale by the time the agent picks it up.
- **2026-08-17** — **PR-9 merged to `main` as `f8e04ab0`** (PR #1117, "APG-2546: scrub prisonerNumber from surviving SAR sections"). `origin/APG-2546/scrub-nomis-and-crn` auto-deleted post-merge. Same-day cycle: PR-8 morning merge (`b7b05283`) → PR-9 execution → **two rounds of executing-agent self-review before push** → team review → merge. Doc followed literally against the re-anchored `b7b05283` refs; no reviewer findings. Verification: 678 tests pass, ktlint clean, 0 UUID leaks, `entity-schema.json` unchanged, test-harness PDF still 3 pages. Round-2 pace running ahead of the ~2-week realistic-calendar estimate — two merges on day 1. PR-9 outcomes-table row flipped ✅ merged; PR-10 unblocked.

  **PR-10 re-anchored in this same delivery-log entry** against `origin/main @ f8e04ab0`. Line drift from `0cf89850` was substantial because PR-8 removed three whole data classes (`SarPniResult`, `SarPerson`, `SarOasysPniResult`) plus their fields/mapper/three template `<h2>` blocks, and PR-9 removed one field + template row + two test assertions. Key PR-10 anchor shifts:
  - `data class Content`: L163 → **L151** (−12)
  - `organisations` field in Content: L171 → **L156** (−15)
  - `data class SarReferral`: L174 → **L159** (−15)
  - `data class SarOrganisation`: L384 → **L291** (−93 — three big data classes removed above it)
  - `.toSarOrganisation()` mapper: L390 → **L297** (−93)
  - `organisations = codesFromFiltered.mapNotNull …` construction line: L116 → **L104** (−12)
  - `toSarReferral(...)` call: L109 → **L100** (−9)
  - `allOrgCodes` / `organisationsByCode` / `organisationNamesByCode` block: L96-107 → **L87-96** (−10)
  - `sar_template.mustache` `<h2>Organisation>` block: L136-146 → **L78-88** (−58)
  - `sar_template.mustache` `organisationName` in originalReferral: L24 → **L23** (−1)
  - Unit test `organisationRepository` mock: L225 → **L181** (−44)
  - Unit test `originalReferral.organisationName` assertion (pattern to mirror for new parent-referral assertion): L258 → **L211** (−47)
  - Unit test `assertThat(organisations).hasSize(1)`: L302 → **L244** (−58)
  - Unit test `val organisation = organisations[0]`: L303-305 → **L245-246** (−58)
  - ITest `createOrganisation` fixture: L104 → **L30** (−74; a lot of the fixture setup above it collapsed with PR-8's section removals)
  - ITest `with(content.referrals[0])` block: L123-130 → **L100+** (−23)

  PR-10 doc "Scope" section fully rewritten with `f8e04ab0`-anchored refs + explicit call-outs for the "pattern to mirror" lines (e.g. `SarOriginalReferral` mapper body line 272 is now the exact template for the parent `SarReferral` wiring). Status flipped from "skeleton — expand before execution" to "ready for execution — line refs re-verified against `origin/main @ f8e04ab0`". `AGENT-PROMPT-TEMPLATE.md` PR-10 prompt to be updated with `f8e04ab0` anchor + doc-ready state.

  **PR-11 still deliberately deferred** — its refs still point at `0cf89850`; PR-10 will shift the SAR service + template again when it merges. PR-11 will be re-anchored when picked up.
- **2026-08-17 (late)** — **PR-10 opened as (likely) #1118** on branch `APG-2546/organisation-into-referral`, head `08c05432`. Auto-merge queued — will land on first reviewer approval. Base: `origin/main @ f8e04ab0` (post PR-9). Executing agent did a full **nine-lens self-review** before pushing, all 9 lenses ✅ pass. Doc followed literally against the re-anchored `f8e04ab0` refs. Third same-day PR execution (PR-8 morning, PR-9 afternoon, PR-10 late) — round-2 pace remains well ahead of the ~2-week realistic-calendar estimate.

  **What changed** (paper-trail for closeout):

  - Added `SarReferral.organisationName: String?`, populated in the existing `toSarReferral(...)` mapper via the same `organisationNamesByCode` map that `SarOriginalReferral.organisationName` already reads (post-fetch pattern, no JPQL change, no new query).
  - Deleted `Content.organisations` field, nested `data class SarOrganisation`, and the `OrganisationEntity.toSarOrganisation()` extension.
  - `<h2>Organisation></h2>` block removed from `sar_template.mustache`; new `<tr>Organisation name</tr>` row added inside the referrals table (mirrors line 23's `originalReferral.organisationName` row shape).
  - `organisationRepository.findAllByCodeIn(...)` **stays live** — still resolving `SarOriginalReferral.organisationName`. **PR-8 empty-shell precedent did NOT apply**: PR-8's `OasysPniResultEntityRepository` was an all-round SAR orphan, whereas `organisationRepository` still has an active in-file consumer. Agent explicitly called this out in the PR body under "Not touched" so reviewers don't ask why the shell pattern wasn't applied.
  - `PersistenceHelper.createOasysPniResult` / `createPerson` **not touched** (PR-8 Finding 2 guidance held — still deferred to PR-12).
  - `entity-schema.json` **genuinely unchanged** (byte-identical). Agent noted the PR-10 doc's verification bullet ("added `SarReferral.organisationName`, removed `SarOrganisation` class") was **leftover text from an earlier doc revision** — `entity-schema.json` tracks only JPA entities not SAR DTOs, and no JPA entity was touched in PR-10. Called out in the PR body so reviewers don't waste time re-reading a byte-identical file.

  **Two non-blocking observations from self-review** — logged here + folded into PR-12 hygiene scope:

  1. **Fixture-hardening backlog: per-referral variance not yet demonstrable.** PR-10 working doc verification-checklist item 6 asked for the fixture to render **different** `organisationName` per referral so the field is demonstrably per-referral. The `SarContractIntegrationTest` fixture currently seeds only one offering (`MDI → HMP Moorland`), so both referrals in the regenerated JSON/HTML show the same `organisationName`. Wiring is verified per-referral in the mapper + in the unit test's single-row assertion — the gap is only in the contract-test golden's ability to demonstrate the shape visually. Would need a second offering (e.g. `BXI`) wired into one of the two referrals. Same shape as the fixture-hardening backlog item after PR-7 (already tracked). **Recommendation from executing agent: fold into PR-12 hygiene rather than back-patching PR-10.** Accepted — PR-12 scope updated in this same commit. PR-10 verification-checklist item 6 flipped to SUPERSEDED with pointer.
  2. **Cosmetic mustache double-blank** between the Courses and Staff sections in `sar_template.mustache` — one line of trailing whitespace, absorbed into the golden without incident. Non-functional. **Recommendation: clean up whenever the template next gets a real edit** (PR-11 is next; add to PR-11 verification checklist as a one-line "while you're in the file" fix, or defer to PR-12 hygiene). Folded into PR-11 doc + PR-12 checklist as belt-and-braces.

  PR-11 to be re-anchored against `origin/main` HEAD once PR-10 merges (same pattern as PR-9 → PR-10 handoff).
- **2026-08-17** — **PR-10 merged to `main` as `d710fa7f`** (PR #1118, "APG-2546: fold organisation into referral; drop top-level organisations[]"). `origin/APG-2546/organisation-into-referral` auto-deleted post-merge. PR number **confirmed as #1118** (was inferred). Same-day cadence continues: PR-8 → PR-9 → PR-10 all merged 2026-08-17. **Four of six PRs shipped on day 1.** Team review turnaround: auto-merge fired on first approval. No reviewer findings; two non-blocking self-review observations already logged + deferred to PR-12 hygiene. Outcomes-table row flipped ✅ merged.

  **PR-11 re-anchored in this same delivery-log entry** against `origin/main @ d710fa7f`. PR-10 removed the `organisations[]` block from Content + `SarOrganisation` DTO + `.toSarOrganisation()` mapper + `<h2>Organisation></h2>` template block, so downstream `staff`-related refs (which sit AFTER `organisations` in Content field order and after the Organisation `<h2>` in template order) shifted UP. Key PR-11 anchor shifts:

  - `data class Content`: L163 → **L150** (−13)
  - `staff` field in Content: L170 → **L154** (−16)
  - `staff = staffRepository.findByPrisonNumber…` construction: L115 → **L103** (−12)
  - `data class SarStaff`: L271 → **L218** (−53)
  - `.toSarStaff()` mapper: ~L271+ → **L287** (moved down slightly as PR-10 rearranged mapper block ordering)
  - `sar_template.mustache` `<h2>Staff></h2>` block: L148-158 → **L79-89** (−69 — biggest shift, PR-10 removed the `<h2>Organisation></h2>` block above it)
  - `SubjectAccessRequestServiceTest.kt` `staffRepository` mock: L216 → **L172** (−44)
  - `assertThat(staff).hasSize(1)`: L244 → **L197** (−47)
  - `val staffMember = staff[0]`: L299 → **L246** (−53)
  - `verify { staffRepository.findByPrisonNumber(prn) }`: L314 → **L253** (−61)
  - ITest `createStaff` fixture (primaryPomStaffId feed): L103 → **L83** (−20)
  - CTest `expectedFlywaySchemaVersion = "145"`: L55 → **L55** (unchanged ✓)
  - CTest `SECONDARY_STAFF_ID` / `SECONDARY_STAFF_ROW_ID`: L246/L247 → **L208/L209** (−38)
  - CTest first `createStaff` (primary POM): L212 → **L177** (−35)
  - CTest second `createStaff` (secondary POM): L219-225 → **L184-190** (−35)

  **Orphan-audit re-confirmed against `d710fa7f`**: `staffRepository.findByPrisonNumber` still has only **one** `src/main` caller (`SubjectAccessRequestService.kt:103`). `StaffLookupService` and `StaffService` reference the `StaffRepository` interface but use different methods (not `findByPrisonNumber`). V144/V145 SQL migration comments cost nothing. After PR-11 removes the SAR call site, `findByPrisonNumber` becomes a genuine prod-orphan — **deletion verdict still holds**. Interface itself stays alive (multiple in-use methods, unlike PR-8's `OasysPniResultEntityRepository` which was fully orphaned → PR-8 empty-shell precedent does **not** apply here; only the specific method definition is deleted from the interface).

  PR-11 doc "Scope" section fully rewritten with `d710fa7f`-anchored refs + explicit clarification of the "repository-interface pattern: NOT an empty-shell restoration" so the executing agent doesn't accidentally apply PR-8's precedent to the wrong situation. Status flipped from "skeleton — expand before execution" to "ready for execution — line refs re-verified against `origin/main @ d710fa7f`". `AGENT-PROMPT-TEMPLATE.md` PR-11 prompt to be updated with `d710fa7f` anchor + doc-ready state.

  After PR-11 merges, PR-12 (hygiene) can start — needs the full four-PR combined state to sanity-grep, and now carries two extra scope items from PR-10 self-review (fixture per-referral variance + cosmetic mustache double-blank).
- **2026-08-17 (evening)** — **PR-11 opened as (likely) #1119** on branch `APG-2546/remove-top-level-staff`, head `045b7054`. Auto-merge queued. Base: `origin/main @ d710fa7f` (post PR-10). Executing agent self-review ✅ ship-it verdict — no blockers. **Fourth same-day PR execution** (PR-8 morning, PR-9 afternoon, PR-10 late, PR-11 evening). PR-11 completes the four sibling `SubjectAccessRequestService.kt` + `sar_template.mustache` cuts; PR-12 (hygiene) now unblocked to start once #1119 merges.

  **What changed** (paper-trail for closeout):

  - Deleted `Content.staff` field, nested `data class SarStaff`, `.toSarStaff()` extension mapper.
  - Deleted `staff = staffRepository.findByPrisonNumber(prn).distinctBy { it.username }.map { it.toSarStaff() }` construction line.
  - Deleted `StaffRepository.findByPrisonNumber` method definition (surname-sort `@Query` from PR #1115). Interface itself stays alive — 5 other in-use methods (`findByStaffId`, `findLastNameByUsername`, `findLastNameByStaffId`, `findSurnamesByUsernames`, `findSurnamesByStaffIds`) preserved. **PR-8 empty-shell precedent deliberately NOT applied** — doc called this out explicitly.
  - Deleted top-level `<h2>Staff></h2>` block from `sar_template.mustache`.
  - Inline `primaryPomStaffSurname` / `secondaryPomStaffSurname` fields on each referral **preserved** (option (a) — Deborah 2026-08-13 pm decision).
  - **Optional mustache cosmetic tidy from PR-10 self-review** (double-blank between Courses/Staff sections): **done here** as one-line no-op when the Staff block was deleted; removed from PR-12 hygiene scope.
  - Test-side cleanup: removed `staffRepository` mock decl + verify + assertion lines per doc.

  **Scope-creep call-out (consistent with PR-10 ctor param precedent):**

  Removed the now-orphaned `staffRepository` constructor param + `StaffRepository`/`StaffEntity` imports from `SubjectAccessRequestService.kt`. Doc's Scope bullets covered the DTO/mapper/call site but did **not** explicitly cover the ctor injection — leaving it would trigger ktlint's no-unused-imports rule and be dead code besides. Mirrored on the unit test (removed `staffRepository = mockk()` decl + ctor arg in `setup()`). **Same shape as PR-10's ctor-param removal.** Reviewer welcome to push back if they want stricter scope discipline; called out on PR body. Now formalised as a round-2 learning pattern (see below).

  **Six non-blocking observations from self-review** — most already handled in-flight; the rest folded into PR-12 scope:

  1. **PDF page count moved 3 → 2.** First round-2 PR to move page count (PR-8/9/10 all held 3). Expected because removing a whole top-level `<h2>` section is the point. Not a bug; flagged so future test-harness comparisons don't panic when the golden HTML/PDF shrinks. Log-only; no action.
  2. **JSON golden gained a trailing newline** (was `\ No newline at end of file`). Harmless today. Brittle to a future strict-bytes comparator upgrade. **Optional one-line fix — folded into PR-12 scope** as belt-and-braces.
  3. **`idx_staff_last_name` (V145) index now fully orphaned** — `findByPrisonNumber` is gone. Write-path cost (every `staff.last_name` update pays for the index) until the index is dropped. Flyway forward-only means dropping the index needs a new `V146__drop_staff_last_name_index.sql`. **Deferred to PR-12 hygiene** as a proposal (weigh write-path cost vs "forward-only, additive is cheap, keep for future readers" — recommend keeping unless the write cost is measurable).
  4. **SQL migration comments still name-drop `findByPrisonNumber`** — V144__ / V145__ SQL comments mention the now-deleted method by name. Grep-clean elsewhere. **Optional cleanup in PR-12** if a follow-up migration V146 lands (per finding 3). Otherwise leave — Flyway forward-only + comment-only edit not typically justified.
  5. **`distinctBy { it.username }` post-filter design smell** (deleted with the block). The original query was `SELECT DISTINCT s FROM StaffEntity s WHERE …` which de-duplicated by JPA identity (id), then the Kotlin call site added `.distinctBy { it.username }` as a compensating Kotlin-side dedup because SQL DISTINCT didn't actually dedup by username. Latent design smell in the surname-sort query PR #1115 introduced. Non-issue now that the whole call site is gone; logged so future SAR-collection queries don't reintroduce the same shape.
  6. **Nothing surprising in the codebase itself.** Line-refs against `d710fa7f` were spot-on. `StaffLookupService` / `StaffService` confirmed to use different `StaffRepository` methods (not `findByPrisonNumber`) — supports the "no empty-shell needed" call. Inline `primaryPomStaffSurname` / `secondaryPomStaffSurname` fields on both goldens survived intact as expected. Third-pass verification held for a fourth consecutive PR.

  **Round-2 learning pattern captured** — added to R3 mitigation shape: *"When removing a top-level `Content` collection, sweep for orphaned constructor params + imports in the same file. Doc scope bullets tend to cover the DTO/mapper/call-site trio but miss the injection point. ktlint's no-unused-imports rule will catch imports; ctor params can silently linger as dead code. Precedent: PR-10 (organisation), PR-11 (staff). Applicable to any similar future removal."*

  PR-12 to be re-scoped once PR-11 merges — see PR-12 doc updates in this same commit.
- **2026-08-17 (late evening)** — **PR-11 MERGED as `47488c8a`** (PR **#1119** confirmed — inferred number held). Auto-merge fired on approval same-day. **FIVE OF SIX ROUND-2 PRs SHIPPED ON DAY 1** (PR-8 `b7b05283` morning → PR-9 `f8e04ab0` afternoon → PR-10 `d710fa7f` late → PR-11 `47488c8a` late evening). Only PR-12 (hygiene) + PR-13 (docs handover) remain. Round-2 pace running dramatically ahead of the 8–10 working-day realistic calendar estimate.

  Final merged diff for PR-11 (per merge commit `47488c8a`): **6 files, +1 / −82**:
  - `domain/repository/StaffRepository.kt` — −22 (deleted `findByPrisonNumber` `@Query` + KDoc; interface stays alive with 5 other in-use methods)
  - `service/SubjectAccessRequestService.kt` — −13 (deleted `Content.staff`, `data class SarStaff`, `.toSarStaff()` mapper, `staffRepository` ctor param + `StaffRepository`/`StaffEntity` imports, construction line)
  - `resources/sar_template.mustache` — −12 (deleted `<h2>Staff></h2>` block + cosmetic double-blank between Courses/Staff sections, addressing the PR-10 nine-lens flag)
  - `SubjectAccessRequestServiceTest.kt` — −18 (removed `staffRepository = mockk()` decl + ctor arg, `StaffRepository`/`StaffEntityFactory` imports, `findByPrisonNumber` every/verify pair, `Content.staff` assertion, `staffMember` lastName assertion)
  - `sar-api-response.json` — ±1 (`staff[]` array removed)
  - `sar-expected-render-result.html` — −16 (whole `<h2>Staff></h2>` block gone)

  **PR-12 pre-verification against `47488c8a`** (all the greps PR-12's verification checklist calls for, run pre-execution so the executing agent's task shrinks dramatically):

  | Sweep | Command | Result | Verdict |
  |---|---|---|---|
  | `StaffRepository.findByPrisonNumber` orphan check | `grep -rn 'findByPrisonNumber' src/main` | 0 hits in `src/main/kotlin`; 2 hits in V144/V145 SQL comments only | ✅ **Delete complete.** Other `findByPrisonNumber`/`findAllByPrisonNumber` hits are unrelated (`ReferralRepository`, `CourseParticipationRepository`, `PniResultRepository` — all still in use). |
  | Deleted SAR-DTO name grep | `grep -rn 'SarPniResult\|SarOasysPniResult\|SarPerson\|SarOrganisation\|SarStaff' src` | **0 hits anywhere in src** | ✅ **All five DTOs cleanly deleted.** No orphaned imports, `@Schema` refs, or mapper helpers survive. `entity-schema.json` also 0 hits. |
  | Orphan ctor-param sweep on `SubjectAccessRequestService.kt` | `grep -nE 'private val [a-zA-Z]+Repository' service/SubjectAccessRequestService.kt` | 4 lines: `referralRepository`, `courseParticipationRepository`, `courseRepository`, `organisationRepository` — all in-use | ✅ **No orphans.** PR-10/11 handled theirs in-flight, precedent held. |
  | `PersistenceHelper.createPerson` callers | `grep -rn 'createPerson' src/test` | **0 test callers** (only the def at `PersistenceHelper.kt:157`) | 🔨 **Delete confirmed.** |
  | `PersistenceHelper.createOasysPniResult` callers | `grep -rn 'createOasysPniResult' src/test` | **0 test callers** (only the def at `PersistenceHelper.kt:191`) | 🔨 **Delete confirmed.** |
  | `PersistenceHelper.createPniResult` callers | `grep -rn 'createPniResult' src/test` | **1 test caller** (`DomainEventsListenerTest.kt:222` — NOMIS prisoner-merge domain-event handler) | 🚫 **Keep alive.** Confirms round-1 impact-matrix verdict (this method serves the prisoner-merge NOMIS flow, not SAR). |
  | KDoc dangling `[...]` refs to deleted siblings | `grep -rn '\[PniResult\|\[OasysPniResult\|\[SarPniResult\|\[SarOasysPniResult\|\[SarPerson\|\[SarOrganisation\|\[SarStaff\|StaffRepository\.findByPrisonNumber' src/main --include='*.kt'` | **0 hits** | ✅ **No fixup needed.** PR-8/9/10/11 executing agents cleaned KDoc siblings in-flight (or PR #1115's KDocs never used fully-qualified `[Class.method]` shape — either way, the executing agent's checklist item is a no-op). |
  | `SarContractIntegrationTest` companion-const scan | `grep -nE 'const val\|_ID = ' SarContractIntegrationTest.kt` | Only `const val PRISON_NUMBER = "A1234BC"` remains | ✅ **No unreferenced consts.** `PNI_RESULT_ID`, `OASYS_PNI_RESULT_ID`, `PERSON_ID`, `SECONDARY_STAFF_ID`, `SECONDARY_STAFF_ROW_ID` etc. all cleaned in-flight by PR-8/11. |
  | JSON golden trailing byte | `tail -c 5 sar-api-response.json \| xxd` | Ends `22 7d 5d 7d 0a` (`"}]}\n`) — **trailing newline present** | 🔨 **Optional strip.** Was `\ No newline at end of file` pre-round-2; PR-11 regen added it. Brittle to a future strict-bytes comparator. One-line fix or update the regen script — pick and document. |
  | `expectedFlywaySchemaVersion` | `grep -rn expectedFlywaySchemaVersion src/test` | `SarContractIntegrationTest.kt:55` = `"145"` | ✅ **Correct.** No round-2 migrations. Only flip to `"146"` if the V145 index-drop decision below ships a `V146__drop_staff_last_name_index.sql`. |
  | UUID-leak grep on both goldens | `grep -cE '<uuid-regex>' sar-api-response.json sar-expected-render-result.html` | **0 hits both** | ✅ **Clean.** |
  | Fixture per-referral organisation variance | `grep -n 'MDI\|BXI\|createOffering\|createOrganisation' SarContractIntegrationTest.kt` | Only `MDI` / HMP Moorland seeded; single `createOrganisation` + single `createOffering` call | 🔨 **Add second offering** (BXI / HMP Brixton wired to one of the two referrals) + regen goldens. Confirms PR-10 nine-lens flag. |
  | V144/V145 SQL migration comment `findByPrisonNumber` name-drop | (see sweep 1 output) | Both migrations reference the now-deleted method by name | 🔨 **Optional cleanup** — conditional on V146 landing per the V145-drop decision. If V145 stays, leave the comments (editing Flyway-applied SQL for comment hygiene isn't idiomatic). |

  **Effective PR-12 delta (post-pre-verification):**
  1. **Delete two `PersistenceHelper` methods** — `createPerson` (L157) and `createOasysPniResult` (L191). Includes `createPerson`'s `LocalDate` bind fix from PR #1115 (dead without a consumer). ~40 line deletion + import cleanup.
  2. **Add second offering to `SarContractIntegrationTest` fixture** — seed BXI / HMP Brixton, wire to one of the two referrals, regen `sar-api-response.json` + `sar-expected-render-result.html` goldens, confirm both referrals render **different** `organisationName` values. ~10-line fixture add + snapshot regen.
  3. **Optional: strip trailing newline** from `sar-api-response.json` OR update `regenerate-sar-snapshots.sh` to write without one, whichever is idiomatic across the file's siblings. One-line fix; document the decision in PR body.
  4. **Document V145 `idx_staff_last_name` keep-vs-drop decision** in PR body. Recommend keep (Flyway forward-only default; write-path cost not measurable on background reference table). No code change unless drop chosen.
  5. **Full-suite regression** — `./gradlew ktlintCheck test`, snapshot regen zero-diff, UUID-leak grep, `entity-schema.json` diff sanity.

  Everything else on PR-12's verification checklist is CONFIRMED CLEAN pre-execution. The executing agent's job is small and boring — precisely what PR-12 was designed to be (the "confirmed-clean checkpoint before Branston sees the new PDF").

  **AGENT-PROMPT-TEMPLATE PR-12 prompt** to be updated with `47488c8a` anchor + pointer to this pre-verification block. PR-13 anchor deliberately NOT pre-updated (will shift on PR-12 merge — re-anchor at pickup, same pattern as PR-8→9→10→11).
- **2026-08-17 (night)** — **PR-12 opened as #1120** on `APG-2546/round-2-hygiene-tidy`, single commit `1d49a7f3` on top of `47488c8a` (post-PR-11 main). Auto-merge queued. Executing agent nine-lens self-review ✅ **ship-it verdict**; two non-blocking observations parked as follow-on hygiene candidates (see below). **Sixth same-day round-2 PR-open event** — five merged, one open. Only PR-13 (docs handover) remains on the round-2 workboard.

  **Five-bullet effective delta landed exactly as pre-verified scope predicted** (matches DELIVERY-LOG 2026-08-17 late-evening pre-verification table 1:1):

  1. `PersistenceHelper.createPerson` deleted (L157). Unused `java.time.LocalDate` import dropped as a consequence.
  2. `PersistenceHelper.createOasysPniResult` deleted (L191).
  3. `SarContractIntegrationTest` fixture seeds a **second BXI / HMP Brixton offering** wired to the original (withdrawn) referral. Regenerated goldens now render:
     - **HMP Brixton × 2** (the withdrawn referral + the `originalReferral` sub-block inside the main referral)
     - **HMP Moorland × 1** (the main `REFERRAL_STARTED` referral)

     Per-referral organisation variance is now **visibly demonstrable** in the golden JSON/HTML — closes the PR-10 nine-lens flag exactly as designed.
  4. **Trailing `\n` stripped** from `sar-api-response.json` — both JSON goldens now end consistently (no trailing newline). HTML sibling unchanged (already consistent). Closes the PR-11 nine-lens flag.
  5. **V145 `idx_staff_last_name` decision: KEEP.** Rationale documented in PR body: Flyway forward-only default holds, write cost not measurable on a background reference table, additive indices are cheap institutional history. V144/V145 SQL comment name-drops of `findByPrisonNumber` left alone as consequence (per doc: editing Flyway-applied SQL for comment hygiene isn't idiomatic).

  **Verification (per agent report):**

  - `./gradlew ktlintCheck test` = **678 tests green** (holds at round-2 baseline; two `PersistenceHelper` methods gone were unused, so no test count change — the BXI fixture add exercises existing tests, no new test count)
  - Snapshot regen zero-diff on **save-then-regen-then-diff** (regen script is idempotent on the new fixture)
  - UUID-leak grep on both goldens = **0**
  - `entity-schema.json` **unchanged** (no JPA entity touched)
  - `expectedFlywaySchemaVersion` still `"145"` (V145 kept, no V146)

  **Two non-blocking observations parked as follow-on hygiene** (executing agent's nine-lens catch — worth logging so future readers see the trail):

  1. **UUID literal-style consistency** — new `BXI_OFFERING_ID` (or equivalent const introduced by the fixture add) may not exactly mirror the literal style used by `SECONDARY_STAFF_ROW_ID` and its siblings (raw string vs `UUID.fromString(...)` vs companion `val`). Style-only; runs fine either way. Candidate for a fresh-eyes cleanup ticket if the codebase has an idiomatic style guide — not scoped to round-2.
  2. **Pre-existing filename mismatch** — `sar-generated-report.html.log` (build artefact filename) vs `sar-expected-render-result.html` (golden filename). Pre-existing on `main` (not introduced by PR-12), noticed during golden regen. Cosmetic; either rename the build artefact or accept the drift. Not scoped to round-2.

  **Notes for PR-13 (docs handover) — fold into PR-13 doc:**

  1. **Fixture now demonstrates per-referral organisation variance in the SAR goldens.** Worth a call-out in the Branston-facing PDF walkthrough as a *"here's how per-referral wiring looks under two-organisation load"* beat — Deborah's round-2 ask #4 (organisation into referral) delivered and demonstrably variant.
  2. **V145 kept-with-reasoning is documented in the PR-12 body.** The docs handover PDF should **reference PR #1120** (or the PR-12 outcomes row) rather than re-litigate the drop-vs-keep debate. Keeps the paper trail single-source-of-truth.
  3. **`PersistenceHelper` is tighter by two methods.** If the handover PDF anywhere enumerates helper-method inventory or fixture-authoring recipes, refresh accordingly. (Unlikely to be enumerated at that level — flag only.)
  4. **Untracked `.snyk` + `Copy of *.xlsx` under `doc/` persisted through PR-12** (not PR-12's job to clean up). Worth a one-liner `.gitignore` sweep in PR-13, or asking whoever owns those files. Closes register-item R7.

  **Round-2 workboard status:** nothing else blocked on PR-12 other than PR-13 itself. PR-13 anchor to be updated to PR-12's merge SHA when auto-merge fires; re-anchor happens at PR-13 pickup (same pattern as prior sibling PRs).

  Waiting on: CI green + squash-merge. Merge SHA to be recorded in the next timeline entry once landed.
- **2026-08-18 (morning)** — **PR-12 MERGED to `main` as `99264496`** (PR #1120 confirmed). Auto-merge fired overnight after CI green. `origin/APG-2546/round-2-hygiene-tidy` auto-deleted post-merge (confirmed via `git ls-remote origin 'refs/heads/APG-2546/*'` — only `planning-sar-field-removals` remains). Merge diff per `99264496`: **4 files, +18 / −53** (`PersistenceHelper.kt` −49, `SarContractIntegrationTest.kt` +16 / −0, `sar-api-response.json` +1 / −1, `sar-expected-render-result.html` +2 / −2). Zero `src/main/kotlin` touches, zero `sar_template.mustache` touches — precisely the hygiene-only footprint the doc predicted. All five pre-verified deltas landed intact; no reviewer follow-up findings.

  **Post-merge ground-truth verification against `99264496`** (sanity for PR-13 pickup — recorded so the fresh agent doesn't need to re-derive):

  | Sweep | Command / evidence | Result |
  |---|---|---|
  | SAR JSON top-level keys | `python3 -c "import json; print(list(json.load(open('src/test/resources/sar/sar-api-response.json')).keys()))"` | `['referrals', 'courseParticipation', 'courses']` — three surviving sections, no `pniResults`/`oasysPniResults`/`person`/`organisations`/`staff` |
  | Referral count + per-referral organisation variance | inspect `content` | 2 referrals; `ref[0].organisationName = "HMP Brixton"` (withdrawn), `ref[1].organisationName = "HMP Moorland"` + `ref[1].originalReferral.organisationName = "HMP Brixton"` → **HMP Brixton × 2, HMP Moorland × 1** as advertised |
  | `originalReferral` sub-block keys | inspect | `[courseName, organisationName, submittedOn, statusCode, referrerSurname, referrerOverrideReason, hasLdc, additionalInformation]` — no `id` (PR-7 held), no `prisonerNumber` (PR-9 cascade held), no residual UUIDs |
  | Referral keys drift check | inspect | `[oasysConfirmed, statusCode, hasReviewedProgrammeHistory, additionalInformation, submittedOn, primaryPomStaffSurname, secondaryPomStaffSurname, referrerOverrideReason, referrerUsername, hasLdc, hasLdcBeenOverriddenByProgrammeTeam, hasReviewedAdditionalInformation, organisationName, originalReferral]` — no `prisonerNumber`, `organisationName` present, inline surname fields retained (option (a) — Deborah 2026-08-13) |
  | `courseParticipation[0]` keys | inspect | `[isDraft, otherCourseName, yearStarted, source, type, outcomeStatus, outcomeDetail, yearCompleted, location, detail, courseName, createdByUser, createdDateTime, updatedByUser, updatedDateTime]` — no `prisonNumber` (PR-9 held) |
  | Mustache template `<h2>` blocks | `grep -nE '<h2>' src/main/resources/sar_template.mustache` | 3 hits: L2 Referrals, L39 Course participation, L66 Courses. Staff / Organisation / Personal / PNI / OASys-PNI `<h2>` blocks all gone |
  | `SubjectAccessRequestService.kt` shape | inspect | 277 lines; `data class Content` at **L146** with only 3 fields (referrals, courseParticipation, courses); `SarReferral.organisationName` at **L165**; `SarOriginalReferral.organisationName` at **L182**; `SarStaff`/`SarPerson`/`SarPniResult`/`SarOasysPniResult`/`SarOrganisation` all absent |
  | `PersistenceHelper` inventory | `grep -nE 'fun create' PersistenceHelper.kt` | `createPerson` **absent**, `createOasysPniResult` **absent** (PR-12 deletes held); `createPniResult` still present (used by `DomainEventsListenerTest.kt:222`) |
  | JSON golden trailing bytes | `tail -c 3 sar-api-response.json \| xxd` | `}]}` — **no trailing newline** (PR-12 strip held) |
  | UUID leak grep on both goldens | `grep -cE '[0-9a-f]{8}-[0-9a-f]{4}-…' <goldens>` | 0 hits both |
  | `expectedFlywaySchemaVersion` | grep | `"145"` — V145 kept per PR-12 decision |
  | `.snyk` + `Copy of *.xlsx` untracked (R7) | `git status --short` | still untracked on planning branch worktree — **PR-13 to close R7** via `.gitignore` sweep |

  **PR-13 unblocked** (last remaining round-2 workboard item). Doc rewritten this same commit into fully agent-executable shape, anchored to `99264496`. New anchor prompt in `AGENT-PROMPT-TEMPLATE.md` to be updated when PR-13 is picked up (branch cut + head SHA aren't known until then). Ready for a fresh agent to pick up in a clean chat window using the `PR-13-round-2-docs-and-handover.md` doc end-to-end.

  **Round-2 pacing recap on merge of PR-12:**
  - PR-8 → PR-9 → PR-10 → PR-11 all merged 2026-08-17 (four merges in a single working day).
  - PR-12 opened 2026-08-17 night, auto-merge queued, landed 2026-08-18 morning.
  - PR-13 (docs) is the only round-2 item outstanding.
  - Round-2 elapsed to five-of-six merges: **~5 calendar days** from the 2026-08-13 kickoff. **Dramatically ahead** of the 8–10 working-day realistic-calendar estimate; even the optimistic 6-day scenario overshot.
  - Sole remaining external gate is Branston's round-3 review of the fresh sample PDF (PR-13 output) — per the OOS decision, APG-2546 closes on *reply received* regardless of round-3 asks.
- **2026-08-18 (afternoon) — PR-13 shape correction: split into 13b / 13c / 13d.** Fresh agent picking up PR-13 correctly stopped and flagged three blockers on the previous draft:

  1. **Target branch ambiguity.** Verified `doc/planning/` does not exist on `origin/main` at all — no planning docs have ever merged to main. PR-6 (round-1 handover) also never merged to main; it was planning-branch commits + external comms sent by Raby. The previous PR-13 draft conflated "docs live on main so PR-13 touches main" with "docs live on the planning branch so PR-13 touches the planning branch". Correct answer: **planning-branch-only**, mirroring PR-6 precedent.
  2. **Track C (Branston PDF) is not agent-executable.** Requires interactive `sign-in-dev.hmpps.service.justice.gov.uk` access with a live SARBT001 nDelius test account against preprod CRN A9648CH. Not fabricatable, not sanity-checkable from a repo checkout.
  3. **External comms should not be sent by an agent.** OSAR email to David Evans / Sharon Hepworth / Roxanne Stephenson / William Falconer / QAT (cc Cameron / Deborah / Naseem / Kiril) + Slack DM to Deborah — these are real people; the send action needs Raby in the loop.

  **Direction locked in with Raby (planning-agent chat) 2026-08-18 pm:**

  - **13a dropped entirely.** The two-line `.gitignore` PR to main (R7 close-out) idea is scrapped. Coupling APG-2546 close-out to another review cycle for two ignore-pattern lines is a bad trade. Every round-2 PR agent (PR-8/9/10/11/12) discipline-gated on `git status --short` before commit without incident — the muscle memory is already there. R7 is now recorded as **accepted paper-cut / deferred**; fold a two-line `.gitignore` fix into the next adjacent code PR on some other ticket if the noise re-annoys us. ROUND-2-PLAN R7 row updated to match.
  - **13b — fresh agent, planning-branch commits.** Docs close-out on `APG-2546/planning-sar-field-removals` (this branch): DELIVERY-LOG close-out timeline entry, ROUND-2-PLAN R7 flip + status updates, PR-13 doc self-flip, `handover/` directory scaffolding (README + two comms draft files). No `src/` touches, no `.gitignore` touches, no PR to main.
  - **13c — Raby (or Deborah's dev) step.** Generate the Branston-facing full-chrome PDF via Cameron's SAR dev-service (Option 1) against preprod CRN A9648CH, eyeball-check against the sanity list, commit at `doc/planning/APG-2546/handover/round-2-sample.pdf` on the planning branch. Update the 13b DELIVERY-LOG placeholder (`<PDF: pending>` → concrete file size + CRN + generation date + eyeball-check verdict).
  - **13d — Raby in the planning-agent chat.** Send OSAR email + Deborah Slack DM using the drafts 13b committed at `doc/planning/APG-2546/handover/`. Bracketed values (`[CRN]`, `[date]`) filled in from 13c's outcomes. Planning agent updates DELIVERY-LOG with send timestamps once Raby confirms send.

  PR-13 doc rewritten 2026-08-18 pm from the earlier main-targeted shape into the 13b/13c/13d split. Structure now:
  - **Shape correction §** at the top explaining precedent + why we split.
  - **R7 explicitly deferred §** with the accepted-paper-cut rationale.
  - **Prerequisites for 13b** — invert the R6 discipline (13b works FROM the planning branch, reads code state via `git show origin/main:<path>`, never `read_file`s the planning branch's stale `src/`).
  - **Ground-truth verification table** against `99264496` retained (13 assertions from morning entry).
  - **Scope §13b** — DELIVERY-LOG close-out + ROUND-2-PLAN R7 flip + PR-13 self-flip + `handover/` scaffolding + comms drafts (as files, not to send).
  - **§13c** — Raby's PDF drop recipe (PR-6-derived).
  - **§13d** — comms drafts to be sent by Raby.
  - **Verification checklist** trimmed to 13b-only sweeps (branch identity check, no-src-touches gate, `handover/` scaffolding present, R7 defer recorded, close-out entry present).
  - **Non-obvious things** — five items including "work FROM planning branch but READ main for code truth", "don't fabricate the PDF", "R7 defer is the correct call not a scope regression".

  APG-2546 close-out condition unchanged: PR-13 merged (as planning-branch commits) + Branston PDF committed (13c) + OSAR email + Deborah DM sent (13d) + Jira transitioned to "content-review in-flight" (not Done — Jira Done fires on Branston's reply). Round-3 asks still handled as fresh ticket per the OOS decision.

  Fresh-agent prompt for 13b is available in the planning-agent chat; will be folded into `AGENT-PROMPT-TEMPLATE.md` at 13b commit time.

- **2026-08-18 (afternoon) — PR-13b DELIVERED: planning-branch close-out docs pass.** Fresh agent executed the shape-corrected 13b scope end-to-end on `APG-2546/planning-sar-field-removals` (this branch) as commit **`815e2d8f`** (docs + `handover/` scaffolding + comms drafts) followed by an outcomes-row SHA-fold amend. No `src/` touches, no `.gitignore` touches, no PR to `main`. Mirrors PR-6 round-1 handover precedent (planning-branch commits + external comms sent by Raby).

  **What landed on the planning branch (close-out commit `815e2d8f` + this outcomes-row SHA amend):**

  - `doc/planning/APG-2546/DELIVERY-LOG.md` — this timeline entry + PR-13 outcomes-table row flipped from ⬜ ready to "13b ✅ delivered / 13c pending Raby / 13d drafted awaiting send" with the close-out commit SHA folded in.
  - `doc/planning/APG-2546/ROUND-2-PLAN.md` — "Working directory index" entry for `PR-13-round-2-docs-and-handover.md` flipped from "skeleton" to "✅ fully agent-executable (13b delivered 2026-08-18 pm); 13c pending Raby PDF drop; 13d comms drafts committed". R7 row confirmed already flipped to accepted / deferred (Raby's earlier setup commit `3127e266`) — sanity-checked; no re-flip needed.
  - `doc/planning/APG-2546/PR-13-round-2-docs-and-handover.md` — status banner flipped from "13b fully agent-executable" to "13b delivered; 13c pending PDF; 13d comms drafted".
  - `doc/planning/APG-2546/README.md` — sanity-checked; Raby's earlier banner refresh (2026-08-18) is accurate on this branch state; no touch needed.
  - `doc/planning/APG-2546/handover/` — new directory, three files committed:
    - `README.md` — explains what belongs here (round-2-sample.pdf, osar-email-draft, deborah-slack-dm-draft, optional roxanne-dd-drift-nudge), sequencing 13c → 13d, PDF eyeball-check list copied inline for convenience, sanity anchors from PR-13 ground-truth table (referrals × 2, HMP Brixton × 2 + HMP Moorland × 1 variance, Option 2 = 2 pages @ `99264496`).
    - `osar-email-draft.md` — verbatim template from PR-13 doc §13d.1; `[CRN]` / `[date]` brackets left unfilled for Raby to complete at 13d send time. Recipients: David Evans, Sharon Hepworth, Roxanne Stephenson, William Falconer, QAT; cc Cameron, Deborah, Naseem, Kiril.
    - `deborah-slack-dm-draft.md` — verbatim template from PR-13 doc §13d.2; `[CRN]` bracket unfilled. Direct DM to Deborah (SDM, Cameron's SAR product team).
    - Optional Roxanne DD-drift nudge intentionally **not** committed (per PR-13 doc §13b.5 / §13d.3 — check PR-6's original P.S. state first; only add if that P.S. didn't close her records).

  **Round-2 code delivery closed with all five merges on main** (recap at close-out):

  | PR | Merge SHA | PR # | Merged |
  |---|---|---|---|
  | PR-8 (remove PNI + OASys PNI + Person sections) | `b7b05283` | #1116 | 2026-08-17 |
  | PR-9 (scrub `prisonerNumber` from surviving sections) | `f8e04ab0` | #1117 | 2026-08-17 |
  | PR-10 (fold organisation into referral) | `d710fa7f` | #1118 | 2026-08-17 |
  | PR-11 (drop top-level `staff[]`, option (a)) | `47488c8a` | #1119 | 2026-08-17 |
  | PR-12 (round-2 hygiene tidy) | `99264496` | #1120 | 2026-08-18 (am) |

  Round-2 elapsed to five-of-six merges: **5 calendar days** from the 2026-08-13 kickoff. Dramatically ahead of the 8–10 working-day realistic-calendar estimate — even the optimistic 6-day scenario overshot.

  **Round-2 risk register at close-out:**

  - **R1** (delete a header-owned field): ✅ RESOLVED 2026-08-13 — SAR wrapper team confirmed header ownership.
  - **R2** (org field populated elsewhere unspotted): ✅ closed by PR-10 grep-sweep.
  - **R3** (repo queries had other callers): ✅ closed by PR-8/PR-11 per-query grep-audits (net one dead query removed; one dead helper site removed).
  - **R4** (fixture regen side-effects): ✅ no repeat of the round-1 date-binding surprise; per-PR ktlint + full-suite + UUID-leak greps held.
  - **R5** (round-3 mid-sprint): ✅ scoped OUT 2026-08-13 — round-3 asks spin a fresh ticket.
  - **R6** (fresh-agent misreads planning branch `src/` tree): ✅ discipline held on PR-8→PR-12 (fresh checkouts of `origin/main`); **inverted** for PR-13b (docs-only on planning branch, but READ code state via `git show origin/main:<path>`).
  - **R7** (`.snyk` + `Copy of *.xlsx` untracked noise): ✅ **accepted paper-cut / deferred out of APG-2546.** Rationale in the shape-correction entry above + on ROUND-2-PLAN R7 row (flipped by Raby's setup commit `3127e266`, re-confirmed by 13b executing agent). Fold a two-line `.gitignore` fix into the next adjacent code PR on some other ticket if it re-annoys us.

  **Shape correction reminder** (for readers scanning this entry cold — full detail in the 2026-08-18 pm shape-correction entry above): PR-13 does **not** merge to `main`. `doc/planning/**` has never merged to main across the whole APG-2546 lifetime (round-1 or round-2). PR-6 (round-1 handover) shipped the same way — planning-branch commits + external comms sent by Raby. PR-13 mirrors that shape: 13b docs land here; 13c PDF lands here; 13d comms go out from Raby's chat; DELIVERY-LOG closes on Branston reply received (per the round-2 close-out signal, not on "zero further asks").

  **Current APG-2546 state at end of PR-13b:**

  - **Code side:** ✅ round-2 complete on main. Five PRs (#1116–#1120), five merge SHAs, all on `origin/main @ 99264496`. Nothing else queued.
  - **Docs side (planning branch):** ✅ close-out docs landed (this entry + outcomes-row + `handover/` scaffolding + PR-13 doc banner flip). No further doc changes planned before 13c.
  - **13c (Branston PDF):** ⏳ pending Raby — Option 1 recipe (Cameron's SAR dev-service, SARBT001 role, preprod CRN `A9648CH` or a comparable rich CRN). Commits at `doc/planning/APG-2546/handover/round-2-sample.pdf` on the planning branch. Eyeball-check list on `handover/README.md` + PR-13 doc §13c step 5.
  - **13d (external comms):** ✅ drafted, ⏳ pending Raby send. OSAR email → David/Sharon/Roxanne/William/QAT (cc Cameron/Deborah/Naseem/Kiril). Slack DM → Deborah. Both live at `doc/planning/APG-2546/handover/`; `[CRN]` / `[date]` brackets filled at 13d send time from 13c's outcomes. Planning agent logs send timestamps here after Raby confirms.
  - **Close-out signal:** APG-2546 closes on **feedback received** from Branston round-2 review, not on "zero further asks". Round-3 asks handled as a fresh ticket per the OOS decision. Jira transitions to "content-review in-flight" after 13d send (**not** Done — Jira Done fires on Branston's reply).

  **Handover to planning agent (Raby):** paper trail closes here for the 13b docs pass. Next moves are (a) Raby (or Deborah's dev) runs the SAR dev-service against a preprod CRN and commits the resulting PDF at `doc/planning/APG-2546/handover/round-2-sample.pdf` (13c); (b) Raby fills the `[CRN]` / `[date]` brackets in the two comms drafts and sends OSAR email + Deborah DM (13d); (c) planning agent records send timestamps + the outcome of Branston's reply here to close APG-2546.
- **2026-08-18 (afternoon → evening) — Pre-13c A + B + 13c all landed same day. Branston-facing PDF committed.** Rapid execution of all three planning-agent-tracked pre-comms steps in one working afternoon after PR-13b close-out landed. Full paper trail:

  **Pre-13c step A — local Option 2 eyeball ✅.** Raby ran `./script/local-scripts/regenerate-sar-snapshots.sh` on `main @ 99264496` and opened `build/test-generated/sar-generated-report.pdf` — 2-page chrome-less render, all round-2 removals verified absent, `Organisation name` row present per referral in the fixture (Brixton × 2 + Moorland × 1), no UUID leaks. Verdict: local render structurally correct → cleared to proceed with the template re-registration ping.

  **Pre-13c step B — template re-registration on Slack ✅.** Raby posted `doc/planning/APG-2546/handover/cameron-template-registration-slack-draft.md` (verbatim) to `#haa-sar-functionality-change-request`. Cameron's team confirmed same-day: **"that's done in dev"** — template re-registered at `99264496` on the SAR dev-service. SARBT001 role on the test nDelius account confirmed present (no rotation needed). Turnaround: same working day. Notable — no pipeline stall this round (contrast round 1's multi-day block that motivated Option 2 fallback existing at all).

  **Correction to earlier PR-13 draft (recorded for the paper trail):** the previous "no re-registration required for round-2" claim (in the 2026-08-18 morning entry's PR-13 doc rewrite) was wrong. The mustache template file DID change materially round-1 → round-2 (five `<h2>` block removals, `Organisation name` row added inline per referral, `Prisoner number` rows scrubbed), and Cameron's team's dev-service serves the registered revision — so re-registration was required, not a courtesy. Committed the correction 2026-08-18 pm as `e9e58fa2` (new `cameron-template-registration-slack-draft.md`, PR-13 doc §"Pre-13c steps" section added, handover README pre-13c step B marker flipped to REQUIRED, PR-13 "Not in scope" bullet flipped). Fresh executing agent's decision to stop at the target-branch + PDF-can't-fabricate + comms-can't-send blockers earlier in the afternoon indirectly surfaced this: shape-correction commit `3127e266` didn't include the re-registration correction because the assumption was still tentative at that point; correction fell out of the subsequent pre-13c conversation.

  **13c — Branston-facing sample PDF committed on planning branch ✅.** Merge commit **`539bcdb0`** on `APG-2546/planning-sar-field-removals`. Single file added: `doc/planning/APG-2546/handover/round-2-sample.pdf`, 139,732 bytes, 47 pages.

  **CRN selection paper trail** (recorded because it deviates from PR-13's default recommendation of A9648CH):

  | CRN | File size | Pages | Referrals | Course participations | Verdict |
  |---|---|---|---|---|---|
  | A9648CH (round-1's pick) | 5,750 B | 5 | 0 (No Data Held) | 0 | ❌ Empty in current dev. Preprod DB has been reset since round 1 — round-1's PDF pick is not reusable. |
  | A7416EA | 6,100 B | 5 | 0 | 0 (2 courses on the catalogue only) | ❌ Too sparse. Insufficient to demonstrate any round-2 change in context. |
  | **A8610DY (JONES, Tim) — selected** | **139,732 B** | **47** | **74** | **18** | ✅ Rich vettor-training exemplar. ~30 distinct organisation names across 74 referrals. All round-2 changes clearly demonstrated. |

  **Round-2 sanity validation against A8610DY PDF (all 9 items PASS)** — done via `pdftotext -layout` + grep on the committed PDF, before commit:

  - `Prisoner number` / `Prison number` row anywhere in body: **0 hits** ✅ (PR-9 held)
  - Top-level `Staff` `<h2>` section: **0 hits** ✅ (PR-11 held)
  - Top-level `Organisation` / `Organisations` `<h2>` section: **0 hits** ✅ (PR-10 held)
  - `Person` / `Personal data` `<h2>` section: **0 hits** ✅ (PR-8 held)
  - `PNI results` `<h2>` section: **0 hits** ✅ (PR-8 held)
  - `OASys PNI results` `<h2>` section: **0 hits** ✅ (PR-8 held)
  - Raw ACP-payload UUID leak (36-char UUID regex): **0 hits** ✅ (PR-5 + PR-7 held)
  - `Organisation name` row inline per referral: **74 hits** ✅ (PR-10 — one per referral, perfect)
  - `Primary POM staff` + `Secondary POM staff` rows inline per referral: **74 + 74 hits** ✅ (option (a) retention — Deborah 2026-08-13 pm)

  Three surviving `<h2>` sections rendered: **Referrals**, **Course participation**, **Courses**. Three, no more.

  **Vettor-exemplar quality note:** A8610DY delivers rich per-referral organisation variance — Ashfield, Aylesbury, Belmarsh, Bristol, Buckley Hall, Dovegate, Drake Hall, Elmley, Erlestoke, Five Wells, Forest Bank, Fosse Way, Frankland, Garth, Gartree, High Down, Humber, Isis, Lancaster Farms, Long Lartin, Lowdham Grange, Onley, Stafford, Stocken, Stoke Heath, Wakefield, Wealstun, Whatton, Wymott, plus one seed-quirk ("United Kingdom" — see deferred follow-ups below). Real POM staff surnames render inline (Pobee-norris, Robertson) alongside referrer surnames (ELANGOVAN). This is a genuinely stronger vettor-training exemplar than round-1's PDF was — Deborah's rationale for the round-1 fixture widening explicitly cited "rich enough to be a genuine vettor training exemplar", and A8610DY nails it in live preprod data.

  **13d — comms drafts bracket-filled ✅.** Both drafts under `doc/planning/APG-2546/handover/` had their `[CRN]` (→ `A8610DY`) and `[date]` (→ `2026-08-18`) brackets filled in the same 13c commit sequence. The OSAR email also gained a concrete list of the ~30 organisation names for ask #4's "visibly variant" claim. Drafts ready to paste-and-send; awaiting Raby.

  **Deferred follow-ups (post round-2 close-out — NOT scoped to APG-2546):**

  1. **Preprod DB fixture health — A9648CH empty in current dev.** Round-1's canonical PDF pick has been reset. Whoever owns preprod ACP fixtures should re-seed A9648CH (or explicitly deprecate it) so future SAR handovers have a stable canonical CRN. Not APG-2546 scope; a next-DB-refresh item.
  2. **Bad organisation seed — one org named "United Kingdom".** Cosmetic; the DB has one `OrganisationEntity` with `name = "United Kingdom"` seeded somewhere and it renders through `SarReferral.organisationName`. Not a round-2 defect (the round-2 wiring is correct — it renders whatever's in the DB). Flag to whoever owns preprod ACP fixtures for cleanup. Not APG-2546 scope.

  Both follow-ups are logged here for future reference so they aren't lost, and are explicitly OUT of APG-2546. Neither blocks close-out; neither triggers a round-3 ticket (they're not Branston-review feedback — they're internal fixture hygiene).

  **State on planning branch after 13c push:**

  - Branch tip: `539bcdb0` (PR-13c PDF commit).
  - Trail across the day: `3127e266` (shape correction) → `815e2d8f` (13b close-out) → `8ba82cc5` (13b outcomes-row fold-in) → `e9e58fa2` (pre-13c template re-registration correction) → `539bcdb0` (13c PDF commit) → **this timeline entry commit** (in flight).
  - `git status --short` on planning branch worktree shows only `?? .snyk` (R7 deferred paper-cut; won't be committed).

  **Remaining APG-2546 close-out steps (Raby-owned; planning agent logs outcomes):**

  1. Raby sends OSAR email (`handover/osar-email-draft.md`, attach `handover/round-2-sample.pdf`). Planning agent logs send timestamp.
  2. Raby sends Deborah Slack DM (`handover/deborah-slack-dm-draft.md`). Planning agent logs send timestamp.
  3. Jira APG-2546 transitions to "content-review in-flight" (**not** Done — Jira Done fires on Branston's reply per round-2 close-out signal).
  4. When Branston reply lands: planning agent logs outcome + APG-2546 closes. Any round-3 asks spin a fresh ticket per the OOS decision (paper trail: `doc/planning/APG-2546/round-3-branston-feedback.md` for verbatim reply capture).

- **2026-08-19 — OSAR passed round-2 for preprod; preprod ACP deploy validated end-to-end; preprod SAR-service template re-registration drafted.** Branston / OSAR signed off the round-2 content review (paper trail lives in the Branston reply thread; verbatim capture to be committed at `round-3-branston-feedback.md` if any residual asks surface, else a short send-timestamp confirmation appended here). Round-2 close-out condition met.

  **Preprod ACP deploy validation (done 2026-08-19 in this planning-agent chat, cast-iron):**

  | Check | Method | Outcome |
  |---|---|---|
  | Preprod deployment image tag | `kubectl -n hmpps-accredited-programmes-preprod get deploy hmpps-accredited-programmes-api -o jsonpath='{.spec.template.spec.containers[0].image}'` | `ghcr.io/ministryofjustice/hmpps-accredited-programmes-api:2026-08-18.532.9926449` |
  | Preprod pod image (sanity — same as deployment) | `kubectl … get pod … -o jsonpath='{.spec.containers[0].image}'` on `hmpps-accredited-programmes-api-8854f8d87-44tzw` | matches — `2026-08-18.532.9926449` |
  | Preprod pod count + status | `kubectl … get pods` | 4/4 Running, all on ReplicaSet `8854f8d87`, ages 25h + 29m (older two = original rollout, newer two = post-rollout reschedule; identical image) |
  | Runtime `/info` (pod-local, via `kubectl port-forward`) | `curl http://localhost:18080/info` | `git.branch=main`, `git.commit.id=9926449`, `git.commit.time=2026-08-18T09:47:38Z`, `build.version=2026-08-18.532.9926449` |
  | Runtime `/health` | as above, `/health` endpoint | `UP` on all components (allocation-manager, audit, etc.) |
  | Deployed SHA = tip of `main`? | `git log origin/main --oneline -1` | `99264496` → short `9926449` — exact match ✅ |
  | All five round-2 merge SHAs ancestors of deployed image commit? | `git merge-base --is-ancestor <sha> 9926449` for each | ✅ PR-8 `b7b05283`, ✅ PR-9 `f8e04ab0`, ✅ PR-10 `d710fa7f`, ✅ PR-11 `47488c8a`, ✅ PR-12 `99264496` — **all five deployed, cast-iron via git ancestry** |

  **Reassurance for Raby's "worried I missed a PR" concern:** you cannot have missed a PR on preprod. Even if you never approved the CircleCI `deploy_preprod` hold on any of PR-8/9/10/11 individually, PR-12's approval promoted an image built from the tip of `main` (`99264496`) which by construction contains every prior merge as a git ancestor. The image tag encodes the short SHA (`9926449`) directly and the runtime `/info` endpoint confirms it. Nothing to backfill, nothing to redeploy.

  **Prod status (for the record, not APG-2546 scope):** prod still on `dfa27a1` from 2026-07-21 (pre-round-2, in fact pre-most-of-round-1 too). Prod promotion runs on the normal CircleCI `deploy_prod` manual-approval gate whenever the team decides — APG-2546 close-out does not require prod.

  **Preprod SAR-service template re-registration ping — drafted, awaiting Raby send.** Draft committed at `doc/planning/APG-2546/handover/cameron-template-registration-preprod-slack-draft.md`. Same channel (`#haa-sar-functionality-change-request`), same template revision (`99264496`), same diff (five `<h2>` deletions + one row added + two rows removed vs the round-1 registered revision `baee4510`) as the dev re-registration ping already actioned 2026-08-18. Distinct action because the SAR service maintains a per-environment template registration; dev was done 2026-08-18, preprod is next.

  Post preprod re-registration, prod-side registration is the only outstanding SAR-service-environment step — schedule alongside prod ACP promotion whenever the team's ready. Not APG-2546 scope; recorded here so the paper trail stays complete.

  **State on planning branch after this entry:**

  - Branch tip: this timeline entry's commit (SHA folded in post-push).
  - `handover/` now contains a distinct preprod-registration draft alongside the dev one (dev flipped to "sent + confirmed 2026-08-18"). `handover/README.md` inventory updated.
  - APG-2546 Jira: transition to Done once the preprod SAR-service re-registration confirmation lands (feedback received closed the code-side condition; the two SAR-service registrations are follow-through housekeeping, not blockers on Done).

- **2026-08-20 — Roxanne DD-column-H update prepared + reply drafted.** Roxanne emailed 2026-08-19 asking for the DD's "In SAR API - Y/N or N/A" column (col H on `Accredited Programmes Custody`) to be brought up to date with the round-1 + round-2 removals; her working copy is the 2026-07-08 baseline she originally distributed. Handled same-day (2026-08-20 am):

  **Updated DD prepared.** Ran a fresh full-sheet dump against her 2026-07-08 copy at `~/Downloads/Copy of 2026.07.08_copy_Probation Digital Data review December 251.xlsx` and computed the exact column-H delta caused by every merged round-1 + round-2 PR. Updater script committed at `doc/planning/APG-2546/scripts/dd-column-h-update.py` so any future DD refresh (or a repeat request against a newer baseline) can re-run non-interactively.

  **69 changes applied** (all baseline values matched expectations — no drift surprises; output "safe to send" verdict from the updater):

  - **67 rows flipped Yes → No** (fields removed from SAR API by round-1 + round-2 code):
    - `audit_record` (9 rows: R22, R23, R24, R25, R27, R28, R29, R30, R31) — PR-1
    - `course_participation.prison_number` (R47) — PR-9
    - `oasys_pni_result` (4 rows: R85, R86, R87, R88) — PR-4 + PR-8
    - `organisation` (3 rows: R105, R106, R108 — R109 covered under DD-drift below) — PR-5 + PR-10
    - `person` (14 rows: R111–R124) — PR-5 + PR-8
    - `pni_result` (11 rows: R129, R130, R132–R140) — PR-8 (R139 `pni_result_json` supersedes Roxanne's 10.07 flip per Deborah's 2026-08-13 "PNI now sourced via ARNs Probation Hub" decision — recorded in `ROUND-2-PLAN.md` §"DD spreadsheet override"; flagged tactfully in the reply email so Roxanne sees the paper trail)
    - `referral` (2 rows: R153 prison_number, R165 original_referral_id) — PR-9 + PR-5/PR-7
    - `referral_status_history` (11 rows: R192–R202) — PR-2
    - `referral_status_reason` (5 rows: R205–R209) — PR-2
    - `selected_sexual_offence_details` (3 rows: R226, R227, R228) — PR-3
    - `sexual_offence_details` (4 rows: R233, R234, R235, R237) — PR-3
  - **2 rows flipped as DD-drift corrections** (code truth vs baseline, not APG-2546 removals):
    - **R109 `organisation.is_national`**: Yes → No. Code has always been No; Roxanne's 10.07 flip to Yes was based on a dev's earlier indication; Q2 closed 2026-08-04 on "leave off" default (per this log's 2026-08-04 pm end-of-day entry). Flip brings the DD in line with the code.
    - **R224 `referrer_user.referrer_username`**: No → Yes. Row 224's own note reads *"Yes if we can provide surname"*; we do — APG-2492 resolves the referrer username to a surname before it's returned. Flip closes the *"if"* condition Roxanne wrote into her own note.

  **Rows deliberately kept as Yes** (verified in the reply email so Roxanne can sanity-check that these are intentional, not gaps):

  - R107 `organisation.name` — surfaces as `organisationName` inline on each referral (PR-10). Reply email offers to also add a dedicated `referral.organisation_name` row on the referral entity if Roxanne would rather see it there in the DD.
  - R159 `referral.referrer_username` — resolved to surname (APG-2492), still in SAR API.
  - R162 / R163 `referral.{primary,secondary}_pom_staff_id` — resolved to surname, still inline on referral (retained per option (a) — Deborah 2026-08-13 pm).
  - R242 `staff.last_name` — top-level `staff[]` list gone (PR-11), but the surname still surfaces via the resolved POM staff fields above, so the field itself is still in the SAR API.

  **Output artefact** (untracked per repo convention): `~/Downloads/Copy of 2026.07.08_copy_Probation Digital Data review December 251_APG-2546-round-2-update.xlsx` (207 KB). Sanity spot-check of 9 rows (6 flipped + 3 kept) all passed.

  **Reply email drafted** at `doc/planning/APG-2546/handover/roxanne-dd-update-email-draft.md`. Ready to paste + attach. Includes: (a) direct answer to Roxanne's ask, (b) five-point summary of what's now absent from the SAR API matched against Deborah's 2026-08-13 action list, (c) the full row-by-row delta table above grouped by causing PR, (d) tactful call-out of the two DD-drift corrections (rows 109 + 224), (e) an optional "new row on `referral` for `organisation_name`?" offer for Roxanne's DD-shape preference, (f) sanity-check pointer at the A8610DY Branston-review PDF she can eyeball independently.

  **Handover-README updated** to add the new draft to the artefacts inventory.

  **Post-send follow-through** (Raby-owned):

  1. Send the reply (paste `roxanne-dd-update-email-draft.md`; attach the xlsx from `~/Downloads/`). Confirm send here so I log the timestamp.
  2. If Roxanne responds with the DD-column-H review closed / no more asks → APG-2546 close-out condition is fully satisfied (feedback received + DD annotated). Transition APG-2546 to Done at that point.
  3. If Roxanne wants row 139 (`pni_result_json`) reconsidered → loop Deborah + Cameron's team; the "PNI via ARNs Probation Hub" decision is theirs to authoritatively confirm.
  4. If she asks for a highlighted / filtered view of the 69 changes inside the xlsx rather than a text-table in the email → same updater script, add an `openpyxl.styles.PatternFill` on column H for the DELTAS row list; one-line change. No new working doc needed.

  **Independence from prod / preprod re-registration threads:** this DD-column-H update is a docs-only correction on Roxanne's paper trail; it's independent from the preprod SAR-service template re-registration (still pending Raby send per the 2026-08-19 entry above) and from prod ACP promotion. All three can close on independent timelines; APG-2546 Jira Done fires once (a) Branston/OSAR feedback received [✅ 2026-08-19] AND (b) Roxanne's DD-review side is closed [pending her reply to today's email].

- **2026-08-20 15:37 BST — Reply email to Roxanne sent.** Raby sent the drafted reply (`handover/roxanne-dd-update-email-draft.md`) with the updated xlsx (`~/Downloads/Copy of 2026.07.08_copy_Probation Digital Data review December 251_APG-2546-round-2-update.xlsx`, 207 KB, 69 col-H changes) attached. Awaiting Roxanne's response. Post-send follow-through steps from the 2026-08-20 morning entry remain the plan (Jira → Done once she confirms DD-review closed).

- **2026-08-20 (afternoon) — Preprod SAR-service template registered by Cameron's team; ACP prod promoted to round-2 same day; two OSAR-facing environments now fully aligned.** Same-day close-out on two of the three remaining follow-through threads.

  **Preprod SAR-service template registration ✅** (`#haa-sar-functionality-change-request` thread):

  - Raby posted the preprod-registration draft (`handover/cameron-template-registration-preprod-slack-draft.md`) at ~12:30 BST.
  - **Dave Llewellyn** picked it up. Slack ↔ Jira sync misfired first attempt, so two mirror tickets were created — **HAAR-5939** (the real one, thread-synced) and **HAAR-5940** (accidental duplicate from the retry). Dave has confirmed 5940 is a dupe of 5939; cleanup is his side. No paper-trail action for us.
  - Dave asked two things: (a) attach OSAR sign-off to the ticket, (b) confirm the code is actually deployed on preprod so he can enable the service (it was disabled in preprod until now because no template had ever been registered).
  - **Deborah handled the sign-off attachment**, using Naseem's Teams message as a temporary receipt while the official email is in flight.
  - Raby's answer on the preprod-deploy question was already covered by yesterday's `2026-08-19` validation entry above (image `2026-08-18.532.9926449`, pod `/info` = `9926449`, all 5 round-2 SHAs ancestors — cast iron).
  - **Dave enabled preprod + confirmed at ~13:58 BST.** The SAR preprod service now (a) has the round-2 mustache template registered, (b) is enabled for the Accredited Programmes payload. So real preprod SAR requests targeting the Accredited Programmes Custody payload will now render the round-2 shape.

  **Prod ACP promoted to round-2 ✅** — noticed today during a follow-up validation triggered by Deborah's 14:27 BST DM asking Raby to confirm preprod *and* prod have the latest template/code:

  | Env | Image tag | Short SHA | Round-2 SHAs ancestors? | Notes |
  |---|---|---|---|---|
  | Preprod ACP | `2026-08-18.532.9926449` | `9926449` = `99264496` (PR-12) | ✅ all 5 | Unchanged since 2026-08-19 validation |
  | **Prod ACP** | **`2026-08-20.546.f84f41b`** | **`f84f41b2`** on `origin/main` | ✅ all 5 | **Newly promoted ~13:07 BST today.** Pods 154 min old. Includes PR #1122 ("Add support for static and dynamic violent/offending predictors to `PniAssessment` and update related tests") which merged to `main` between yesterday's snapshot and today's promotion. PR #1122 is unrelated to APG-2546 SAR work — it's an ARNs Probation Hub / PNI-assessment feature — so from a SAR-content perspective, prod carries the same round-2 payload shape as preprod. |

  Raby's 15:38 BST reply to Deborah — *"Yes pre-prod and prod have the latest template/code changes"* — is factually correct on both environments as verified in this validation. Deborah relaying that to OSAR so they don't inadvertently check an old version → cleaner close-out signal.

  **Prod-side SAR-service template registration** is now the ONLY outstanding SAR follow-through step. Dev ✅ (2026-08-18), preprod ✅ (today, Dave), prod ⏳ (Cameron's team will need the same registration on their prod SAR service before real prod SAR requests targeting the Accredited Programmes payload render the round-2 shape). Not scoped to APG-2546 close-out; scheduled alongside OSAR's real-prod-run readiness whenever they're ready.

  **APG-2546 Jira → Done gating**:

  | Condition | Status |
  |---|---|
  | Branston / OSAR round-2 content sign-off received | ✅ 2026-08-19 |
  | Roxanne DD-column-H review closed | ⏳ pending her reply to today's email (sent 15:37 BST) |

  Nothing else on the code / deploy / SAR-service side blocks APG-2546 close-out. Prod-side SAR-service template registration + OSAR's independent prod-side check happen on their own timeline post-close.

  **Also worth noting** (paper-trail hygiene): Deborah offered to escalate the OSAR sign-off through the official-email route once it arrives — the ticket currently attaches Naseem's Teams-message screenshot as an interim receipt. If any auditor later reviews HAAR-5939 they'll see the Teams-message → official-email progression, so the paper trail is intact.



## Round 2 — PR outcomes

_(Filled as PRs land. PR-8/9/10/11 all shipped 2026-08-17; PR-12/PR-13 pending.)_

| PR | Working doc | Branch | PR # | Merged | SHA | Notes |
|---|---|---|---|---|---|---|
| PR-8 | `PR-8-remove-pni-oasys-person.md` | `APG-2546/remove-pni-oasys-person` | **#1116** | ✅ **merged 2026-08-17** (initial `1b986744` → force-pushed `eb049a07` after nine-lens self-review Finding 1 fix → merged to main as **`b7b05283`**) | `b7b05283` | Doc followed literally. **Nine-lens self-review by executing agent surfaced two findings**: (1) FIXED before push — the initial commit's whole-file deletion of `OasysPniResultEntityRepository.kt` was inconsistent with round-1 PR-1 precedent (`AuditRepository` was kept alive as an empty `JpaRepository` shell after its only SAR-orphan method was removed). Interface restored as 9-line empty shell (preserves future-writer optionality on the still-declared `OasysPniResultEntity`); amended `1b986744 → eb049a07`, force-pushed with `--force-with-lease`. (2) DEFERRED to PR-12 — `PersistenceHelper.createOasysPniResult` + `createPerson` are **genuinely orphaned** after this PR merges. Grep proves the PR-8 doc's "Non-obvious #2" claim (*"helper is called by other tests"*) is stale on `0cf89850`: both helpers were only called from the two SAR-side sites PR-8 removed. Doc directive was explicit: leave for PR-12 hygiene sweep — followed. PR-12 doc scope-item added to catch these on the combined-state grep. **Within-scope R3-spirit extension retained**: removed three now-dead SAR-service ctor params (`pniResultRepository`, `personRepository`, `oasysPniResultEntityRepository`) after verifying single public entry point + no other in-class callers; `PersonRepository` interface stays alive (6 other prod callers), only its SAR ctor injection removed. Final diff: 8 files, +3 / -358 (was +1 / -365 pre-amend). 678 tests pass, ktlint clean, snapshots regenerated with 0 UUID leaks, 0 PACT contracts on SAR endpoint, no `@Operation`/`@Schema` on SAR controller (OpenAPI N/A confirmed), `entity-schema.json` 0-line diff (byte-identical — 24 classes preserved). Test-harness (Option 2) PDF = 3 pages, unchanged. |
| PR-9 | `PR-9-scrub-nomis-and-crn.md` | `APG-2546/scrub-nomis-and-crn` | **#1117** | ✅ **merged 2026-08-17** | **`f8e04ab0`** | Clean execution. Executing agent did two rounds of self-review before pushing (implementation → self-review 1 → self-review 2 → PR out). Team review turnaround same-day. Doc followed literally against the re-anchored `b7b05283` line-refs. Removed `prisonerNumber` from `SarReferral` DTO + template row + mapper positional arg; removed `prisonNumber` from `SarCourseParticipation` DTO + named mapper arg + template row. CRN already gone via PR-8 cascade (only appeared on the deleted `pniResults[]` block) — no separate CRN work needed. Removed two now-redundant fixture assertions in `SubjectAccessRequestServiceIntegrationTest.kt` (self-comparing `prisonerNumber == prisonNumber`, no longer possible with the field gone). Verification: 678 tests pass, ktlint clean, 0 UUID leaks, `entity-schema.json` unchanged, test-harness PDF still 3 pages (unchanged from round-1 baseline). |
| PR-10 | `PR-10-organisation-into-referral.md` | `APG-2546/organisation-into-referral` | **#1118** | ✅ **merged 2026-08-17** | **`d710fa7f`** | Clean execution against re-anchored `f8e04ab0` refs. Executing agent nine-lens self-review ✅ all 9 lenses pass; two non-blocking observations logged in 2026-08-17 (late) timeline entry — both deferred to PR-12 hygiene. Added `SarReferral.organisationName: String?`, populated via `organisationNamesByCode` map (same wiring `SarOriginalReferral.organisationName` already used). Deleted `Content.organisations`, `data class SarOrganisation`, and `.toSarOrganisation()` extension. `<h2>Organisation></h2>` block removed from template; new `<tr>Organisation name</tr>` row added inside referrals table. `organisationRepository.findAllByCodeIn(...)` **stays live** (still resolving `SarOriginalReferral.organisationName`) — PR-8 empty-shell precedent didn't apply because the repo has an active in-file consumer. `entity-schema.json` byte-identical (schema tracks JPA entities, not SAR DTOs; no JPA entity touched). Team review turnaround same-day; auto-merge fired on approval. PR number **confirmed as #1118** (was inferred pre-merge). |
| PR-11 | `PR-11-remove-top-level-staff.md` | `APG-2546/remove-top-level-staff` | **#1119** | ✅ **merged 2026-08-17** | **`47488c8a`** | Clean execution against re-anchored `d710fa7f` refs; auto-merge fired on approval same-day (fourth same-day merge). Executing agent self-review ✅ ship-it verdict, no blockers. **First round-2 PR to move the PDF page count (3 → 2)** — top-level `<h2>Staff></h2>` block removal was the trigger; expected and correct, flagged so future test-harness comparisons don't panic. Scope-creep call-out (consistent with PR-10 precedent): removed the now-orphaned `staffRepository` ctor param + `StaffRepository`/`StaffEntity` imports from `SubjectAccessRequestService.kt`; mirrored on unit test (removed `staffRepository = mockk()` decl + ctor arg in `setup()`). Optional mustache tidy from PR-10 nine-lens (cosmetic double-blank between Courses/Staff) — **done here as one-line no-op** when the Staff block was deleted; PR-12 doesn't need to catch it. ktlint auto-format needed once for residual double-blank after `SarStaff` deletion. StaffRepository interface stays alive — 5 other in-use methods preserved, only `findByPrisonNumber` method definition removed; PR-8 empty-shell pattern deliberately NOT applied per doc callout. Final diff (merged): 6 files, +1 / -82. |
| PR-12 | `PR-12-round-2-hygiene-tidy.md` | `APG-2546/round-2-hygiene-tidy` | **#1120** | ✅ **merged 2026-08-18** | **`99264496`** | Auto-merge fired overnight after CI green. Merge diff (per `99264496`): **4 files, +18 / −53** — `PersistenceHelper.kt` −49 (deleted `createPerson` + `createOasysPniResult` + unused `java.time.LocalDate` import), `SarContractIntegrationTest.kt` +16/−0 (added second BXI / HMP Brixton offering wired to the withdrawn referral), `sar-api-response.json` +1/−1 (trailing `\n` strip + HMP Brixton `organisationName` on withdrawn referral), `sar-expected-render-result.html` +2/−2 (Brixton × 2 vs Moorland × 1 row variance). Zero touches to `src/main/kotlin` or `sar_template.mustache`. Clean execution against pre-verified `47488c8a` state; executing agent nine-lens self-review ✅ ship-it, two non-blocking observations parked as follow-on hygiene (see 2026-08-17 night timeline entry). Five-bullet effective delta landed exactly as scoped: (1) `PersistenceHelper.createPerson` + unused `java.time.LocalDate` import deleted; (2) `PersistenceHelper.createOasysPniResult` deleted; (3) `SarContractIntegrationTest` fixture seeds a second BXI / HMP Brixton offering wired to the original (withdrawn) referral — goldens now render HMP Brixton × 2 (withdrawn + `originalReferral` sub-block) and HMP Moorland × 1 (main `REFERRAL_STARTED`) — per-referral variance demonstrably visible; (4) trailing `\n` stripped from `sar-api-response.json` so both JSON goldens are consistent (HTML sibling unchanged); (5) V145 `idx_staff_last_name` **decision: KEEP** — Flyway forward-only default, write cost not measurable on background reference table, additive indices are cheap institutional history — V144/V145 SQL comment name-drops of `findByPrisonNumber` left alone as consequence (per doc). Verification: `./gradlew ktlintCheck test` = **678 tests green**, snapshot regen zero-diff on save-then-regen-then-diff, UUID-leak grep = 0, `entity-schema.json` unchanged, `expectedFlywaySchemaVersion` still `"145"`. |
| PR-13 | `PR-13-round-2-docs-and-handover.md` | `APG-2546/planning-sar-field-removals` (target — planning branch, NOT main) | _n/a (planning-branch commits, not a main PR)_ | ⏳ **13b delivered 2026-08-18 pm** (planning-branch close-out docs + `handover/` scaffolding + comms drafts committed as `815e2d8f`); **13c pending Raby PDF drop**; **13d comms drafted awaiting Raby send** | `815e2d8f` (13b docs close-out) + this SHA-fold amend | **Shape-corrected 2026-08-18 pm** — 13b/13c/13d split (see the 2026-08-18 pm shape-correction + 2026-08-18 afternoon close-out timeline entries above). 13b delivered by fresh agent on this branch as commit `815e2d8f`: DELIVERY-LOG close-out entry, ROUND-2-PLAN working-directory-index flip (R7 row already flipped by setup commit `3127e266`), PR-13 doc banner flip to "13b delivered", `handover/` directory scaffolded with `README.md` + `osar-email-draft.md` + `deborah-slack-dm-draft.md` (verbatim from PR-13 §13d.1/§13d.2 templates, `[CRN]` / `[date]` brackets unfilled for Raby to complete at 13d send time). Optional Roxanne DD-drift nudge intentionally not committed (per PR-13 §13d.3 — check PR-6 P.S. state first). No `src/` touches, no `.gitignore` touches, no PR to `main`. R7 stays deferred (accepted paper-cut). |

## Handover artefacts (round 2)

- Sample PDF sent to Branston round 2 (2026-08-12, from preprod CRN A9648CH): **superseded** by Deborah's round-2 asks.
- Sample PDF for round 3 (post PR-8…PR-11 merge): _pending — generated in PR-13_.

