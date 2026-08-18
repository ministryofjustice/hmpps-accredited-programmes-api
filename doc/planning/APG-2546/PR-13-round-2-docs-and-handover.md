# PR-13 — Round-2 docs handover + fresh sample PDF for Branston

> **Ticket:** APG-2546 (round 2 close-out) • **Branch:** `APG-2546/round-2-docs-handover`
> • **Est.:** 1 dev day (drafting emails + Slack + generating a fresh preprod PDF + committing the artefact + wrapping DELIVERY-LOG rarely fits in half a day in practice)
> • **Depends on:** PR-8 (`b7b05283`) + PR-9 (`f8e04ab0`) + PR-10 (`d710fa7f`) + PR-11 (`47488c8a`) + **PR-12 (`99264496`, #1120, merged 2026-08-18)** all on `main`. ✅ **all satisfied** — PR-13 is unblocked.
> • **Anchor SHA:** `origin/main @ 99264496` (post-PR-12).
> • **Blocks:** APG-2546 ticket close-out.
> • **Status:** ✅ **fully agent-executable** — no further expansion needed before pickup. Third-pass verified 2026-08-18 against `99264496`.

## Purpose

Round-2 close-out PR. Two jobs, in this order:

1. **Docs close-out** — flip DELIVERY-LOG + ROUND-2-PLAN to "delivered" for PR-8…PR-12, record PR-12's `99264496` merge SHA in every status table, close R7 (`.gitignore` sweep for `.snyk` + DD xlsx working copy), tighten the top-level plan artefacts table.
2. **Branston-facing handover** — generate a fresh Option 1 (full-chrome) sample PDF from a preprod CRN via Cameron's SAR dev-service, commit a copy for paper trail, send email to Branston/OSAR, DM close-out note to Deborah.

**No `src/main` or `src/test/kotlin` changes.** This PR is docs + generated artefact + one `.gitignore` line.

## Prerequisites for a fresh agent

Read this whole section before touching anything. Round-1's PR-6 had a kick-off checklist that time-shifted the template-registration ask to the *earliest possible moment*; PR-13 doesn't have the same time pressure because the template registered in PR-6 hasn't materially changed — round-2 removed sections but did not rename or restructure the surviving ones. Deborah's team's SAR dev service will render `sar_template.mustache` at whatever revision is deployed on `main`, and the round-2 removals are additive-of-nothing (pure section deletions), so no re-registration is required.

That said:

- **Confirm main state and dev deploy.**

  ```zsh
  git fetch origin --prune
  git --no-pager log origin/main --oneline | head -10
  # Expect (top of list, most recent first):
  # 99264496  APG-2546: PR-12 round-2 hygiene tidy (#1120)
  # 47488c8a  APG-2546: drop top-level staff[] list (option (a)) (#1119)
  # d710fa7f  APG-2546: fold organisation into referral; drop top-level organisations[] (#1118)
  # f8e04ab0  APG-2546: scrub prisonerNumber from surviving SAR sections (#1117)
  # b7b05283  APG-2546: remove PNI + OASys PNI + Person sections from SAR (#1116)
  ```

  Verify dev deploy is green — CircleCI dashboard or `#hmpps-accredited-programmes` deploy log. If dev is behind `99264496`, wait or ping platform. **Do not generate the Branston PDF from a dev environment that isn't running `99264496` (or later)** — the PDF would be a lie about what's on `main`.

- **DO NOT start this PR from the planning branch worktree.** Cut PR-13's branch from a fresh checkout of `origin/main @ 99264496`:

  ```zsh
  git checkout main && git pull --ff-only
  git checkout -b APG-2546/round-2-docs-handover
  ```

  The planning branch is behind `origin/main` (merge-base is `106e27d2`, pre-round-1). Its `src/` tree does **not** reflect PR-8/9/10/11/12 changes. R6 in the round-2 risk register.

- **Test nDelius account with `SARBT001` role.** Should already be in place from PR-6's round-1 handover. If it's expired / been rotated, request re-add in `#haa-sar-functionality-change-request` in parallel with the docs work.

- **Access to preprod CRN A9648CH** (round-1's pick). If A9648CH is no longer richly populated, pick a comparable CRN with ≥1 referral + ≥1 course-participation + ideally ≥1 `originalReferral` link so the SAR PDF exercises all three surviving sections. Note the chosen CRN in the DELIVERY-LOG timeline entry when you commit.

- **Cameron's SAR dev-service access** (`sign-in-dev.hmpps.service.justice.gov.uk`) — same login used for PR-6's round-1 handover.

## Ground-truth verification against `99264496` (pre-verified 2026-08-18)

Recorded here so the executing agent does **not** need to re-derive these. All numbers/keys/paths verified from `origin/main @ 99264496`:

| Assertion | Ground truth on `99264496` |
|---|---|
| Top-level SAR JSON keys | `["referrals", "courseParticipation", "courses"]` (three sections) |
| `referrals[]` count in golden | 2 |
| `referrals[0].organisationName` | `"HMP Brixton"` (withdrawn referral) |
| `referrals[1].organisationName` | `"HMP Moorland"` |
| `referrals[1].originalReferral.organisationName` | `"HMP Brixton"` |
| Per-referral organisation variance | **HMP Brixton × 2, HMP Moorland × 1** — visibly demonstrable in golden |
| `originalReferral` sub-block keys | `[courseName, organisationName, submittedOn, statusCode, referrerSurname, referrerOverrideReason, hasLdc, additionalInformation]` — no `id`, no `prisonerNumber`, no UUIDs |
| `courseParticipation[0]` keys | `[isDraft, otherCourseName, yearStarted, source, type, outcomeStatus, outcomeDetail, yearCompleted, location, detail, courseName, createdByUser, createdDateTime, updatedByUser, updatedDateTime]` — no `prisonNumber` |
| Referral inline surname fields | `primaryPomStaffSurname` + `secondaryPomStaffSurname` present on both referrals (option (a) retention) |
| `sar_template.mustache` `<h2>` blocks | 3: `Referrals` (L2), `Course participation` (L39), `Courses` (L66). Staff / Organisation / Personal / PNI / OASys-PNI all deleted |
| `SubjectAccessRequestService.kt` | 277 lines; `Content` at L146 (3 fields); `SarReferral.organisationName` at L165; `SarOriginalReferral.organisationName` at L182 |
| `PersistenceHelper.createPerson` | **absent** (deleted PR-12) |
| `PersistenceHelper.createOasysPniResult` | **absent** (deleted PR-12) |
| `PersistenceHelper.createPniResult` | present (used by `DomainEventsListenerTest.kt:222` — prisoner-merge NOMIS flow, not SAR) |
| JSON golden trailing bytes | `}]}` — no trailing newline (PR-12 strip) |
| UUID leak grep on both goldens | 0 hits |
| `expectedFlywaySchemaVersion` (`SarContractIntegrationTest.kt:55`) | `"145"` — V145 kept per PR-12 decision |
| Test-harness (Option 2) PDF page count | 2 pages (PR-11 dropped it from 3 → 2 with top-level `Staff` block removal; PR-12 did not change count) |
| Untracked hygiene files (R7) | `.snyk` + `doc/Copy of 2026.07.08_copy_Probation Digital Data review December 251.xlsx` still untracked in every clone — PR-13 closes this |

If any of these fail on your fresh checkout, **stop** and cross-check the SHA — you may not be on `99264496`.

## Scope (three tracks, in execution order)

### Track A — Docs close-out (do this first — it's the "safe" work while template registration / PDF generation runs)

**Files to touch:**

1. `doc/planning/APG-2546/DELIVERY-LOG.md`
   - Round-2 PR-outcomes table — flip PR-13's row from `⬜ ready to start` → `✅ merged <SHA> 2026-08-18` when this PR merges. (You can pre-fill everything but the SHA and let the merge-time follow-up commit fill the SHA — same pattern PR-8→PR-12 used.)
   - Append a 2026-08-18 "PR-13 opened" timeline entry (initial commit, branch name, base SHA `99264496`).
   - Append a 2026-08-18 "PR-13 merged" timeline entry after merge (SHA, final diff, verification summary).
   - Append a 2026-08-18 "APG-2546 round-2 close-out" wrap-up entry: total elapsed calendar time, PR SHAs at a glance, artefact links, next-step ownership (Branston's round-3 review; APG-2547 coordination handoff to Cameron's team).

2. `doc/planning/APG-2546/ROUND-2-PLAN.md`
   - Status columns — flip PR-8…PR-12 to ✅ merged with SHAs (they're already recorded in DELIVERY-LOG; mirror the state at the plan-level summary).
   - Add a "Round 2 closed" banner at the top of the file with the final PR SHAs and PR-13 merge SHA.

3. `doc/planning/APG-2546-sar-field-removals.md` (top-level plan) — round-2 artefacts table
   - Fill in every merge SHA in the artefacts table (round-1 already done; add round-2 rows if missing: PR-8 `b7b05283`, PR-9 `f8e04ab0`, PR-10 `d710fa7f`, PR-11 `47488c8a`, PR-12 `99264496`, PR-13 <this PR's SHA>).

4. `doc/planning/APG-2546/README.md`
   - Update the "State as of …" banner to reference PR-13 completion once merged.

5. `doc/planning/APG-2546/AGENT-PROMPT-TEMPLATE.md`
   - Mark PR-13's prompt slot as delivered (or add a "round-2 closed" note at the top).

6. `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md` (parent live-like retest planning doc)
   - Append an "OSAR round 2 close-out (2026-08-18)" entry mirroring the round-1 pattern PR-6 used. Reference the sample PDF path + the round-3 review-ask date window.

### Track B — R7 close-out: `.gitignore` sweep

**Ground truth:** the repo's `.gitignore` already covers JetBrains, Gradle, and build artefacts but does not exclude the two untracked files R7 flags:

- `.snyk` — generated by Snyk CLI; not a source artefact.
- `doc/Copy of 2026.07.08_copy_Probation Digital Data review December 251.xlsx` — Roxanne's DD working copy; owned by her team, not us.

**Add these two entries to `.gitignore`** (append at bottom under a fresh `# APG-2546 R7 close-out — hygiene` heading):

```gitignore

# APG-2546 R7 close-out — hygiene
# .snyk is a Snyk CLI-generated policy file; not a source artefact.
.snyk
# Roxanne's DD working copy under doc/ — owned by the DD team, kept locally
# for reference during ACP DD sweeps but not versioned here.
doc/Copy of *.xlsx
```

**Verify** with:

```zsh
git status --short
# Expect: NO listing of .snyk or the xlsx file
# (they should be silently ignored)
```

Include this `.gitignore` change in the same PR as the docs close-out — no need to split.

### Track C — Branston-facing handover PDF (do this in parallel with Tracks A + B — the SAR-dev-service render can take some elapsed time)

Full-chrome Option 1 PDF via Cameron's SAR dev service. Following PR-6's proven recipe (no scope change — template on `main` is what the dev-service already renders; round-2 removals do not require re-registration).

1. **Log in** to `https://sign-in-dev.hmpps.service.justice.gov.uk` with the test nDelius account (SARBT001 role).

2. **Click through** to the Subject Access Request tile.

3. **Enter CRN + date range**:
   - Recommended CRN: **`A9648CH`** (round-1's pick). Verify still resolvable in dev; if not, pick a comparable rich CRN.
   - Date range: a 12-month window around the most recent referral is usually enough. Widen if the subject's most recent activity is > 12 months old.

4. **Select product: Accredited Programmes only** in the product picker. Round 2 is scoped to our product; combined-report is not what Branston asked for.

5. **Wait for generation**, then download the PDF.

6. **Sanity-check the PDF locally** before sending — eyeball every item on this list:

   - **Cover sheet + top-and-tail pages present.** If absent, it's Option 2 output — the pipeline glitched or you hit the fallback path. Ping Cameron's team.
   - **Three surviving sections only:** Referrals, Course participation, Courses. No `Staff` `<h2>` block. No `Organisation` `<h2>` block. No `Personal data` block. No `PNI results` block. No `OASys PNI results` block.
   - **`prisonerNumber` / `prisonNumber` fields absent** from every rendered referral row and every rendered course-participation row (PR-9 held). NOMIS ID + CRN appear only in the wrapper header (owned by Cameron's team).
   - **`organisationName` present on every referral row** and on any `originalReferral` sub-block (PR-10 held). For CRN A9648CH specifically, this should render distinct organisation names per referral if the subject has referrals across offerings at different prisons — a live demonstration of "organisation into referral context" (Deborah's ask #4).
   - **Referrer surnames + POM staff surnames render inline** on each referral (option (a) held — Deborah's ask #5).
   - **All raw internal UUIDs absent** — no `SarPerson.id`, no `SarOrganisation.id`, no `SarReferral.originalReferralId`, no `SarOriginalReferral.id`. Only surface UUIDs would be legitimate JSON keys the wrapper injects (e.g. request ID); no UUIDs *from the ACP payload*.
   - **`isDraft`** rendered per course-participation (row 64 of the DD; already implemented pre-APG-2546).
   - **Reasonable page count** — for a moderately-populated subject, low tens of pages. If four figures, stop and diff against the on-disk golden.

7. **Commit the PDF for paper trail:**

   ```zsh
   mkdir -p doc/planning/APG-2546/handover
   cp <downloaded-pdf-path> doc/planning/APG-2546/handover/round-2-sample.pdf
   git add doc/planning/APG-2546/handover/round-2-sample.pdf
   ```

   Note the file size + generation date in the DELIVERY-LOG close-out entry.

## Docs to touch — file-by-file summary

- `doc/planning/APG-2546/DELIVERY-LOG.md` — close-out timeline entries + PR-13 outcomes row (see Track A)
- `doc/planning/APG-2546/ROUND-2-PLAN.md` — "Round 2 closed" banner + flip PR outcome markers (see Track A)
- `doc/planning/APG-2546-sar-field-removals.md` — fill in artefacts table with all round-2 SHAs (see Track A)
- `doc/planning/APG-2546/README.md` — update "State as of …" banner (see Track A)
- `doc/planning/APG-2546/AGENT-PROMPT-TEMPLATE.md` — round-2-closed note (see Track A)
- `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md` — append round-2 close-out entry (see Track A)
- `.gitignore` — R7 sweep (see Track B)
- `doc/planning/APG-2546/handover/round-2-sample.pdf` — new, committed artefact (see Track C)
- `doc/planning/APG-2546/PR-13-round-2-docs-and-handover.md` (this file) — flip to ✅ delivered at the top banner

## Handover artefacts to produce

Two distinct PDF artefacts — **do not conflate**:

- **Branston-facing sample PDF** — the Option 1 full-chrome output committed at `doc/planning/APG-2546/handover/round-2-sample.pdf`. This is the artefact Branston reviews. Landing path is inside the repo for paper trail; the *source* is Cameron's SAR dev-service output, not `build/test-generated/`.
- **Local contract-test render** at `build/test-generated/sar-generated-report.pdf` — produced by `SarContractIntegrationTest` via `script/local-scripts/regenerate-sar-snapshots.sh`. **2 pages** as of `99264496`. Useful as an internal readability metric; **not** what goes to Branston.

Additional deliverables (drafts, not committed to the repo):

- **OSAR email** with the Branston-facing PDF attached (see template below).
- **Slack DM to Deborah** closing the loop on all five round-2 asks (see template below).
- **Optional: nudge Roxanne** with the DD residual-drift P.S. carried over from PR-6 (rows 109, 224) — only if PR-6's version didn't already flip her records. Check DELIVERY-LOG PR-6 outcome before re-sending.

## Communication templates (fill in the bracketed values before sending)

### OSAR email (send after PR-13 merges + PDF committed)

```
To: David Evans, Sharon Hepworth, Roxanne Stephenson, William Falconer, QAT
CC: Cameron Farquhar (template stewardship), Deborah [surname] (SDM,
    SAR product), Naseem Ashraf, Kiril Kolev
Subject: APG-2546 round-2 — Accredited Programmes SAR review PDF (round 2)

Hi all,

APG-2546 round-2 is complete on main (final merge #1120, 2026-08-18)
and deployed to dev. Attached is the round-2 SAR review PDF,
generated via the SAR dev service against CRN [CRN] on [date] —
full standard cover-sheet + top-and-tail pages.

This is a direct response to Deborah's 2026-08-13 review-meeting
action list. All five asks delivered:

1. NOMIS IDs / CRNs removed from the body — they now appear only
   in the wrapper header per Cameron's team's confirmation
   (`prisonerNumber` scrubbed from every referral + course-participation
   row; CRN removed via the PNI-block deletion). PR #1116 + #1117.
2. PNI + OASys PNI blocks removed entirely — SAR consumers get PNI
   data from the ARNs Probation Hub feed, so the ACP copy was
   duplicative. Supersedes DD row 139's earlier "keep pni_result_json"
   annotation. PR #1116.
3. Personal Data section (`person{}`) removed entirely. PR #1116.
4. Organisation is now inline on each referral (`organisationName`
   field), replacing the previous top-level `organisations[]` list —
   organisation now sits in the same context as its referral.
   Demonstrated in the attached PDF by [distinct organisation
   names across the referrals for CRN [CRN]]. PR #1118.
5. Top-level `staff[]` list removed; POM staff surnames continue to
   render inline on each referral (`primaryPomStaffSurname` /
   `secondaryPomStaffSurname`) per Deborah's option (a) decision.
   PR #1119.

Also included in round-2 (hygiene): `PersistenceHelper` tightened
by two dead methods, contract-test fixture widened to demonstrate
per-referral organisation variance, `expectedFlywaySchemaVersion`
held at 145 (V145 kept — rationale on PR #1120).

Ask: content sign-off, 5 working days if you can. Any appearance /
cover-sheet feedback continues to sit with Cameron's team under
APG-2547.

Round-2 close-out condition (recorded for transparency): APG-2546
closes on *feedback received*. Any further change requests from
this review land as a fresh ticket (round-3 scope), not folded
back into APG-2546.

Thanks,
Raby
```

### Slack DM to Deborah (send at the same time as the OSAR email)

```
Hi Deborah — APG-2546 round-2 all merged (PR #1116–#1120, final
2026-08-18 as 99264496). Round-2 sample PDF generated from
preprod CRN [CRN] via the SAR dev service is going to
Branston + OSAR now with a 5-working-day ask for content sign-off.

Direct fold-through of your 2026-08-13 action list:

1. NOMIS IDs / CRNs — gone from body, header ownership confirmed
   (thanks to your team's follow-up).
2. PNI + OASys PNI — gone. DD row 139 override recorded in our
   planning docs; happy to loop Roxanne if you'd like her to
   annotate the DD for the next refresh.
3. Personal data section — gone.
4. Organisation now inline on referral (visibly variant in the
   sample PDF).
5. Top-level Staff list gone (option (a)); inline POM surnames
   retained.

Round-2 pace: five of six code PRs shipped in a single day
(2026-08-17), hygiene PR overnight, docs handover today. Total
round-2 elapsed 5 calendar days from your action-list DM to
sample-PDF-out.

Round-3 handling: any Branston asks from this review will spin a
new ticket rather than reopen APG-2546 (paper trail in our
ROUND-2-PLAN §"Out of scope"). Not a pushback on Branston —
just cleaner close-out discipline. If something surfaces as a
security / compliance hard block, we escalate to you + Sharon
outside the ticket workflow.

Cheers,
Raby
```

### Optional: Roxanne DD residual-drift nudge (only if PR-6 didn't already close these)

If PR-6's P.S. to Roxanne (rows 109 + 224) didn't get her to update the DD, forward it once more with the round-2 close-out note. Otherwise skip — no point spamming her.

## Not in scope

- **No product code changes.** Docs + generated artefact + `.gitignore` line only.
- **No re-litigation of PR-12's V145 keep decision.** If someone raises it, link PR #1120.
- **No round-3 feedback handling.** If Branston responds to the sample PDF with further change requests (add / remove / rearrange fields, template layout, header-owner re-negotiation, etc.):
  1. Log Branston's reply verbatim in `doc/planning/APG-2546/round-3-branston-feedback.md` (paper trail only — do **not** edit any other APG-2546 planning doc).
  2. Open a new Jira ticket (working name APG-25xx-round-3) with the verbatim feedback + scope reading; assign per team capacity.
  3. Close APG-2546 with the round-2 outcome regardless of whether round-3 asks exist.
  4. If any round-3 ask is a security / compliance hard block, escalate to Deborah + Sharon separately as a hot-fix (still not a scope change on this ticket).

  See [`ROUND-2-PLAN.md`](./ROUND-2-PLAN.md) §"Out of scope (round 3+)" for the full rationale.
- **No `sar_template.mustache` re-registration.** Round-2 removals do not require re-registration — the SAR dev-service renders whatever revision is deployed on `main`. If Cameron's team asks for a re-registration ping for their own bookkeeping, do it, but it's not a scope item.

## Close-out condition

APG-2546 closes on **all four**:

- [ ] PR-13 merged (this PR).
- [ ] Branston-facing sample PDF sent (email in flight or delivered).
- [ ] DELIVERY-LOG round-2 section closed with PR-8/9/10/11/12/13 merge SHAs + timings.
- [ ] Ticket transitioned in Jira to reflect "content-review in-flight" (not "Done" yet — Jira Done fires on Branston's reply per round-2 close-out signal).

**Not** on "zero further asks from Branston." Feedback received is the close-out signal; whatever Branston asks next is a fresh ticket per the OOS process.

## Verification checklist

Run these before opening the PR. All should pass on a fresh checkout of the branch you cut from `origin/main @ 99264496`:

```zsh
# 1. main is where we think it is
git --no-pager log origin/main --oneline | head -6
# Expect 99264496 at the top.

# 2. No src/main or src/test/kotlin changes on this branch
git --no-pager diff --stat origin/main -- src/main src/test/kotlin
# Expect: zero files.

# 3. .gitignore change lands + hygiene files disappear from status
grep -nE '\.snyk|Copy of \*\.xlsx' .gitignore
# Expect: two matches.
git status --short
# Expect: no listing of .snyk or "Copy of *.xlsx"

# 4. Handover PDF committed
ls -lh doc/planning/APG-2546/handover/
# Expect: round-2-sample.pdf present, sensible size (typically a few
# hundred KB to ~2 MB for a moderate subject with cover sheet + chrome).

# 5. Regression guard — no drift caused elsewhere
./gradlew ktlintCheck test
# Expect: 678 tests green, ktlint clean (unchanged from post-PR-12).

# 6. Golden snapshots unchanged from post-PR-12 state
git --no-pager diff origin/main -- src/test/resources/sar/
# Expect: zero diff.

# 7. DELIVERY-LOG closed out
grep -nE 'PR-13|round-2 close-out|99264496' doc/planning/APG-2546/DELIVERY-LOG.md | head -10
# Expect: multiple hits — PR-13 outcome row, close-out timeline entry,
# PR-12 SHA in the ground-truth table.

# 8. Top-level artefacts table current
grep -nE '99264496|b7b05283|f8e04ab0|d710fa7f|47488c8a' doc/planning/APG-2546-sar-field-removals.md
# Expect: all five round-2 merge SHAs listed.
```

Additional visual checks (do these before requesting review):

- [ ] Open `doc/planning/APG-2546/handover/round-2-sample.pdf` in Preview. Cover sheet present, three surviving sections rendered, no orphan "No Data Held" placeholders in section headings, per-referral `organisationName` visible.
- [ ] Skim the DELIVERY-LOG close-out entry — does it read as a satisfying paper trail to someone opening the doc for the first time?
- [ ] All comms drafts (Slack DM + OSAR email) reviewed for tone, sign-off chain (David / Sharon / Roxanne / William / QAT; cc Cameron; cc Deborah).

## PR description template

```
APG-2546: round-2 docs handover + Branston sample PDF (PR-13)

Docs + generated-artefact + one .gitignore line. No src/main or
src/test/kotlin changes. Anchors to origin/main @ 99264496
(post-PR-12).

Round-2 close-out — all five of Deborah's 2026-08-13 asks delivered
across PR-8…PR-12 (SHAs listed below). This PR wraps the paper
trail and sends the round-2 sample PDF to Branston / OSAR.

Changes:
- doc/planning/APG-2546/DELIVERY-LOG.md — close-out timeline
  entries, PR-13 outcome row, ground-truth verification table
  against 99264496.
- doc/planning/APG-2546/ROUND-2-PLAN.md — "Round 2 closed"
  banner, PR-8…PR-12 flipped to ✅ merged.
- doc/planning/APG-2546-sar-field-removals.md — artefacts table
  populated with all six round-2 SHAs (b7b05283, f8e04ab0,
  d710fa7f, 47488c8a, 99264496, <this-PR-SHA>).
- doc/planning/APG-2546/README.md — "State as of …" banner
  updated.
- doc/planning/APG-2546/AGENT-PROMPT-TEMPLATE.md — round-2-closed
  note.
- doc/planning/APG-2495-post-deploy-retest-live-like-sar.md —
  round-2 close-out entry appended.
- doc/planning/APG-2546/PR-13-round-2-docs-and-handover.md —
  flipped to ✅ delivered.
- .gitignore — R7 close-out: excludes .snyk (Snyk CLI policy) and
  doc/Copy of *.xlsx (DD working copies).
- doc/planning/APG-2546/handover/round-2-sample.pdf — Branston-
  facing PDF, generated by Cameron's SAR dev service against
  preprod CRN [CRN] on [date].

Handover state:
- OSAR email drafted per doc, ready to send with the committed
  PDF attached.
- Slack DM to Deborah drafted per doc.
- Round-3 handling: any Branston asks land as a fresh ticket
  (see ROUND-2-PLAN §"Out of scope"). APG-2546 closes on
  feedback received.

Verification: ./gradlew ktlintCheck test = 678 green, snapshot
goldens unchanged from 99264496, git status --short shows no
untracked hygiene noise, PDF eyeball-checked against the doc's
sanity list.

Rollback: single git revert (docs + .gitignore + PDF only; zero
runtime impact).
```

## Definition of done

- [ ] All of Track A file edits landed with correct SHAs / dates.
- [ ] `.gitignore` R7 sweep landed and verified (`.snyk` + xlsx no longer show in `git status --short`).
- [ ] `doc/planning/APG-2546/handover/round-2-sample.pdf` committed with cover sheet + three surviving sections + no orphan placeholders.
- [ ] `./gradlew ktlintCheck test` = 678 tests green, ktlint clean.
- [ ] Snapshot goldens byte-identical to `origin/main @ 99264496`.
- [ ] PR opened with description template above filled in.
- [ ] OSAR email sent with the committed PDF attached (dated + logged in DELIVERY-LOG).
- [ ] Slack DM to Deborah sent (dated + logged in DELIVERY-LOG).
- [ ] DELIVERY-LOG round-2 close-out entry appended post-merge with the PR-13 merge SHA.
- [ ] Jira APG-2546 transitioned to "content-review in-flight" (or the org's equivalent state — **not** Done, per round-2 close-out signal).
- [ ] APG-2547 coordination status logged in DELIVERY-LOG (any updates from Cameron's team on template stewardship since PR-6).

## Non-obvious things

### 1. No re-registration required for round-2

Round-2 was pure section-removal on `sar_template.mustache` — no new field names, no structural changes to surviving sections. Cameron's team's dev-service reads the template at whatever revision is deployed on `main`, so a re-registration ping is a courtesy, not a requirement. Do the courtesy ping in `#haa-sar-functionality-change-request` when you post the sample PDF back to Branston — one Slack message closes it.

### 2. Test-harness PDF is at 2 pages now, not 3

PR-11's top-level `Staff` section removal dropped page count 3 → 2. Don't panic when comparing against PR-8/9/10-era DELIVERY-LOG entries that reference "3 pages". PR-12 held at 2. This is the internal-readability metric only; the Branston-facing PDF has cover sheets so its page count will be higher.

### 3. Per-referral organisation variance is a walkthrough beat, not just a wiring check

PR-12's fixture add (BXI / HMP Brixton on the withdrawn referral + Moorland on the current one + Brixton again on the `originalReferral` sub-block) means the round-2 goldens now render **two distinct organisation names across three referral rows**. Worth a *"here's how per-referral wiring looks under two-organisation load"* beat in the Branston-facing walkthrough — closes Deborah's ask #4 with visible variance, not just wiring-verified variance. Only the golden demonstrates this though; the Branston PDF will render whatever the chosen CRN happens to have. Pick a CRN with referrals at ≥2 organisations if possible.

### 4. `PersistenceHelper` inventory is tighter — refresh any doc that enumerates helpers

PR-12 removed `createPerson` + `createOasysPniResult`. If any planning doc or KDoc anywhere enumerates helper-method inventory or fixture-authoring recipes, refresh. Unlikely at that level of detail — check with a `grep -rn createPerson doc/` sweep before merging.

### 5. V145 keep-with-reasoning — link, don't re-litigate

The drop-vs-keep decision for `idx_staff_last_name` landed as **KEEP** in PR #1120 with rationale (Flyway forward-only default, write cost not measurable on background reference table, additive indices are cheap institutional history). Reference PR #1120 from the docs handover — do **not** re-open the debate in the PDF, the DD note, or the DELIVERY-LOG close-out. Single-source-of-truth on the PR body.

### 6. R7 close-out is a one-line `.gitignore` edit, not a `git rm --cached` sweep

The two hygiene files (`.snyk`, DD xlsx) have never been tracked — `git status --short` shows them as untracked, not tracked-and-modified. So `.gitignore` alone silences them for future clones. No `git rm --cached` needed. If your `git status --short` after adding to `.gitignore` still shows either file, double-check the pattern — Mac's shell may have expanded `*` in the `Copy of *.xlsx` line before commit.

### 7. Branston's round-3 turnaround budget = 3–5 working days

Not a scope item, but worth pre-baking into your calendar so you're not surprised. Per the 2026-08-13 OOS decision, APG-2546 closes on *reply received* regardless of round-3 asks. If Branston reply within 3–5 working days with no further asks → clean close. If they come back with change requests → spin the round-3 ticket per the OOS process above, close APG-2546 anyway.

## Suggested execution order

Small-first. Docs and `.gitignore` are quick wins; the PDF generation has the most variable elapsed time (dev-service pipeline).

1. Track B: `.gitignore` sweep (5 minutes). Verify with `git status --short`.
2. Track A: DELIVERY-LOG close-out timeline entry + PR-13 outcome row skeleton (30–60 minutes). ROUND-2-PLAN banner (10 minutes). Top-level plan artefacts table (10 minutes). README + AGENT-PROMPT-TEMPLATE + APG-2495 (15 minutes together).
3. Track C: kick off SAR dev-service PDF generation. Once cover-sheet PDF is in hand + sanity-checked, commit to `doc/planning/APG-2546/handover/`.
4. Open PR with description template. Request review (Sharon, Deborah — mirror PR-6's reviewer chain).
5. On approval + merge: append the merge SHA to DELIVERY-LOG + top-level artefacts table (or open a small follow-up commit if merge auto-fired without SHA). Send OSAR email + Deborah Slack DM.
6. Transition Jira. Log the transition + email/DM send times in DELIVERY-LOG.

Total elapsed budget: **1 dev day**. Most of the variance sits in Track C's dev-pipeline lag.

## Rollback

Single `git revert <PR-13-merge-SHA>`. Docs + `.gitignore` + a committed PDF only — zero runtime impact.

If the Branston-facing PDF turns out to have a rendering defect after send (e.g. a section rendered as "No Data Held" because the chosen CRN was too sparse), the fix is to regenerate against a richer CRN and send a follow-up — **not** to revert PR-13. Log the follow-up as a new DELIVERY-LOG entry ("2026-08-XX — PR-13 handover PDF regenerated against CRN [Y] after Branston flagged sparse coverage on CRN [X]"). PR-13 stays merged; the sample PDF is just a snapshot of one preprod subject at one moment.


