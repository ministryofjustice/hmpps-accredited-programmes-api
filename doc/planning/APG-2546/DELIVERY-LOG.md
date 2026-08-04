# APG-2546 delivery log

Running log for the whole ticket. This chat is the coordinating
context — each PR is executed in its own fresh chat using the
matching working-notes doc, but the outcome (merge SHA, PDF page
count, artefacts) is recorded here as soon as it lands, so we have
one place to look for state and can close the ticket cleanly at the
end.

## Status at a glance

> **📉 Content-readability target already hit (internal metric).**
> With PR-1 merged and PR-2 open + reviewed, the **test-harness**
> SAR PDF has dropped from the round-1 baseline of ~8,000 pages
> to **3 pages**. This is a chrome-less content dump (no cover,
> no headers, no footers) and is **not** what OSAR will review —
> the OSAR PDF is produced by Cameron's team's SAR worker with
> full chrome (APG-2547). It is nonetheless reassuring evidence
> that the "8,000-page complaint" is fixed at the *content* level,
> which is APG-2546's remit. PRs 3, 4, and 5 are still worthwhile
> (Roxanne's red-flagged rows, privacy hygiene, etc.).

| Item | State | Notes |
|---|---|---|
| Planning branch (`APG-2546/planning-sar-field-removals`) | ✅ committed, ⏳ awaiting push | Squash `95993514` into `1dd32fef` before push if you want a clean history. |
| Q1 to Roxanne (`oasys_pni_result` A vs B) | ⏳ sent + followed up 2026-08-03 | Default → **A** if no reply by 2026-08-14. |
| Q2 to Roxanne (`is_national` on organisation) | ⏳ sent + followed up 2026-08-03 | Default → **leave off** if no reply by 2026-08-14. |
| PR-1 — remove `auditRecords` | ✅ merged `50f67cff` 2026-08-03 | PR #1107. 9-lens agent review all green. Branch head `04ab44ed` (initial `4801f6e6` + review-fix `04ab44ed`). |
| PR-2 — remove `referralStatusHistory` + `referralStatusReasons` | ✅ merged `cd306c99` 2026-08-03 | PR #1109. Branch head was `f890b221` (initial `22c97122` + review-fix amend). No deviations from doc. Sample PDF post-PR-2 = **3 pages**. |
| PR-3 — remove `sexualOffenceDetails` + `selectedSexualOffenceDetails` | ⬜ ready to start | Branch off `main` @ `cd306c99`. Same integration-test note as PR-2. |
| PR-4 — remove `oasysPniResults` (or strip IDs) | 🚫 blocked on Q1 | Same integration-test note if Q1 = A. |
| PR-5 — strip `SarPerson.id` + `SarOrganisation.id` | ⬜ not started | Independent of Q1/Q2 answers. |
| PR-6 — OSAR round-2 content handover | 🚫 blocked on PRs 1–5 | **Scope updated 2026-08-04:** content-only (JSON + HTML), no PDF, per William Falconer's guidance. Appearance = APG-2547 = Cameron's team. See `PR-6-osar-round-2-content-handover.md`. |
| OSAR **content** sign-off (Sharon + Roxanne + QAT + William) | 🚫 blocked on PR-6 handover | Round-2 review of the content we produce. This is APG-2546's end state. |
| APG-2547 — OSAR **appearance** sign-off (headers / footers / cover) | 🔄 out of our scope | Owned by Cameron's team on `../hmpps-subject-access-request-worker`. Precedent = Accommodation team. Any appearance pushback in the round-2 review gets redirected to `#haa-sar-functionality-change-request`. Tracked here so we don't get pulled back in. |
| Ticket transition to Done | 🚫 blocked on OSAR **content** sign-off | APG-2546 closes on content sign-off only. APG-2547 lives independently. |

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

### YYYY-MM-DD — Q1 answer received

- **Roxanne's answer:** _(fill in "A" or "B" and quote her exact
  words)_.
- **PR-4 branch chosen:** _(Option A → `APG-2546/remove-oasys-pni-results`
  / Option B → `APG-2546/strip-oasys-pni-result-ids`)_.
- **PR-4 doc updated:** _(yes/no — only needed if Q1=B, per the
  "Correction to be aware of" section of the follow-up doc)_.

### YYYY-MM-DD — Q2 answer received

- **Roxanne's answer:** _(fill in "leave off" or "add" and quote
  her exact words)_.
- **Follow-up ticket:** _(N/A if leave off / new APG-XXXX ticket
  link if add)_.

### YYYY-MM-DD — Q1/Q2 default triggered

Use only if we hit 2026-08-14 without answers and fall back to
defaults:

- **Q1 defaulted to A** (remove whole section).
- **Q2 defaulted to leave off** (no code change; APG-2494 closed
  won't-do again).
- **Nudge log:** _(list the reminder pings sent to Roxanne / OSAR
  channel before the default was applied)_.

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

### YYYY-MM-DD — PR-3 merged

- **PR link:** #1110.
- **Merge commit on `main`:** _(short SHA — fill in once merged)_.
- **Sample PDF page count post-PR:** 3 pages (measured at branch
  head — expected unchanged at merge).
- **Reviewer:** _(name)_.
- **Notes / surprises:** _(anything raised in review)_.

### YYYY-MM-DD — PR-4 merged

- **Option applied:** _(A or B)_.
- **PR link:** _()_.
- **Merge commit on `main`:** _()_.
- **Sample PDF page count post-PR:** _()_.
- **Reviewer:** _()_.
- **Notes / surprises:** _()_.

### YYYY-MM-DD — PR-5 merged

- **PR link:** _()_.
- **Merge commit on `main`:** _()_.
- **Sample PDF page count post-PR:** _()_.
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



