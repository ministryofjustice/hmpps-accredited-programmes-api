# APG-2546 delivery log

Running log for the whole ticket. This chat is the coordinating
context — each PR is executed in its own fresh chat using the
matching working-notes doc, but the outcome (merge SHA, PDF page
count, artefacts) is recorded here as soon as it lands, so we have
one place to look for state and can close the ticket cleanly at the
end.

## Status at a glance

| Item | State | Notes |
|---|---|---|
| Planning branch (`APG-2546/planning-sar-field-removals`) | ✅ committed, ⏳ awaiting push | Squash `95993514` into `1dd32fef` before push if you want a clean history. |
| Q1 to Roxanne (`oasys_pni_result` A vs B) | ⏳ sent + followed up 2026-08-03 | Default → **A** if no reply by 2026-08-14. |
| Q2 to Roxanne (`is_national` on organisation) | ⏳ sent + followed up 2026-08-03 | Default → **leave off** if no reply by 2026-08-14. |
| PR-1 — remove `auditRecords` | ⏳ opened #1107 2026-08-03 | Awaiting merge to `main`. Head `4801f6e6`. Update state to ✅ + merge SHA once landed. |
| PR-2 — remove `referralStatusHistory` + `referralStatusReasons` | ⬜ ready | Rebase off `main` after PR-1 lands. **Also update `SubjectAccessRequestServiceIntegrationTest.kt`** — same compile-blocker pattern PR-1 hit. |
| PR-3 — remove `sexualOffenceDetails` + `selectedSexualOffenceDetails` | ⬜ ready | Rebase off `main` after PR-2. Same integration-test note as PR-2. |
| PR-4 — remove `oasysPniResults` (or strip IDs) | 🚫 blocked on Q1 | Same integration-test note if Q1 = A. |
| PR-5 — strip `SarPerson.id` + `SarOrganisation.id` | ⬜ not started | Independent of Q1/Q2 answers. |
| PR-6 — regenerate OSAR round-2 review PDF + handover | 🚫 blocked on PRs 1–5 | Docs + snapshot regen only. |
| OSAR content sign-off (Sharon + Roxanne + QAT) | 🚫 blocked on PR-6 handover | Round-2 review. |
| Ticket transition to Done | 🚫 blocked on OSAR sign-off | |

Legend: ⬜ ready • 🚫 blocked • ⏳ in flight • ✅ complete.

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
- **Head commit on branch:** `4801f6e6`.
- **Files changed:** 8 files, +19 / −152.
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
- **Impact on PR-2 / PR-3 / PR-4:** all three will need the same
  `SubjectAccessRequestServiceIntegrationTest.kt` cleanup as Content
  fields drop out. PR-2, PR-3, and PR-4 (Option A) docs updated to
  call this out explicitly.

### YYYY-MM-DD — PR-1 merged

- **PR link:** #1107.
- **Merge commit on `main`:** _(short SHA — fill in once merged)_.
- **Sample PDF page count post-PR:** _(N pages)_.
- **Reviewer:** _(name)_.
- **Notes / surprises:** _(anything raised in review)_.

### YYYY-MM-DD — PR-2 merged

- **PR link:** _()_.
- **Merge commit on `main`:** _()_.
- **Sample PDF page count post-PR:** _()_.
- **Reviewer:** _()_.
- **Notes / surprises:** _()_.

### YYYY-MM-DD — PR-3 merged

- **PR link:** _()_.
- **Merge commit on `main`:** _()_.
- **Sample PDF page count post-PR:** _()_.
- **Reviewer:** _()_.
- **Notes / surprises:** _()_.

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



