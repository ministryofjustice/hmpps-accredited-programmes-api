# PR-13 — Round-2 docs handover + Branston sample PDF (planning-branch close-out)

> **Ticket:** APG-2546 (round 2 close-out) • **Target branch:** `APG-2546/planning-sar-field-removals` (this planning branch) — **NOT `main`**
> • **Est.:** ½ dev day for the fresh-agent close-out pass (13b); ¼ day for the human PDF drop (13c); comms send (13d) is a Raby step
> • **Depends on:** PR-8 (`b7b05283`) + PR-9 (`f8e04ab0`) + PR-10 (`d710fa7f`) + PR-11 (`47488c8a`) + **PR-12 (`99264496`, #1120, merged 2026-08-18)** all on `main`. ✅ **all satisfied.**
> • **Anchor SHA for code ground truth:** `origin/main @ 99264496` (post-PR-12). Code refs in this doc were verified against `99264496`; we consult main for truth but *commit to the planning branch*.
> • **Blocks:** APG-2546 ticket close-out.
> • **Status:** ✅ **13b delivered** (planning-branch close-out docs + `handover/` scaffolding + comms drafts committed 2026-08-18 pm); ⏳ **13c pending PDF** (Raby drop via Cameron's SAR dev-service, Option 1); ✅ **13d comms drafted** — awaiting Raby send after 13c lands.

## Shape correction — read this first

Earlier drafts of this doc conflated two shapes and the executing agent correctly stopped at the ambiguity. The corrected model:

**Nothing under `doc/planning/**` lands on `main` as part of APG-2546.** Precedent: PR-6 (round-1 handover) never merged to main either — it was planning-branch commits + external comms sent by Raby. PR-13 mirrors that shape.

**PR-13 splits into three sub-artefacts** (previously "PR-13a `.gitignore` mini-PR" was folded in; that idea is now dropped — see §"R7 explicitly deferred" below):

| Sub-artefact | Target | Executor | What lands |
|---|---|---|---|
| **13b — close-out docs** | Commits (or PR *into*) `APG-2546/planning-sar-field-removals` | Fresh agent | DELIVERY-LOG close-out entry, ROUND-2-PLAN + README + this doc's status flips, `handover/` scaffolding, comms drafts |
| **13c — Branston PDF drop** | Commit on `APG-2546/planning-sar-field-removals` | **Raby** (or Deborah's dev) — needs interactive SAR dev-service access | `doc/planning/APG-2546/handover/round-2-sample.pdf` + one-line DELIVERY-LOG placeholder fill |
| **13d — comms send** | External (email + Slack DM) | **Raby** in the planning-agent chat | OSAR email sent; Deborah Slack DM sent. DELIVERY-LOG updated with send timestamps after |

**No PR to `main` in PR-13.** No `.gitignore` change. No `src/` touches of any kind.

## R7 explicitly deferred (not scoped into PR-13)

The round-2 risk register R7 (`.snyk` + DD xlsx working copy showing as untracked in every planning session) is a real paper-cut but the cure is worse than the disease for APG-2546 close-out:

- A `.gitignore`-only PR to `main` for two lines couples APG-2546 close-out to another review cycle and CI run for basically no benefit.
- Adding `.gitignore` entries to the planning branch's copy would never propagate to main.
- The two files have never been tracked; they're pure `git status` noise, not a footgun for accidental commits (round-2 PRs 8/9/10/11/12 all avoided them cleanly).

**Decision:** leave R7 open. Every APG-2546 PR agent has correctly discipline-gated on `git status --short` before committing; that muscle memory is enough. If it re-annoys us on the next ACP ticket, fold a two-line `.gitignore` change into whatever code PR is nearest. Not APG-2546's job to fix.

**Action here:** DELIVERY-LOG entry notes R7 as accepted paper-cut; ROUND-2-PLAN R7 mitigation row flipped from "close in PR-12/13" → "accepted / deferred".

## Prerequisites for a fresh agent (13b)

- **Work directly on the planning branch — the target branch — from its own worktree.** This inverts the R6 advice that applied to code PRs (PR-8…PR-12). Code PRs needed a fresh checkout of `origin/main` because the planning branch's `src/` tree is stale. PR-13b is docs-only on the planning branch itself, so the correct starting point IS the planning branch.

  ```zsh
  git fetch origin --prune
  git checkout APG-2546/planning-sar-field-removals
  git pull --ff-only
  ```

- **When you need to reference code state, read main, don't read the working tree.** The line-refs and DTO shapes in this doc's ground-truth table are against `origin/main @ 99264496`, not against the planning branch's stale `src/`. Use:

  ```zsh
  git show origin/main:src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt | sed -n '140,190p'
  ```

  …or an equivalent `git --no-pager show` invocation, whenever you want to eyeball a Kotlin file. **Do not** `read_file` the planning branch's `src/` copies — they're pre-round-1 and will mislead you.

- **Confirm main is where the doc says it is.**

  ```zsh
  git --no-pager log origin/main --oneline | head -6
  # Expect (top of list, most recent first):
  # 99264496  APG-2546: PR-12 round-2 hygiene tidy (#1120)
  # 47488c8a  APG-2546: drop top-level staff[] list (option (a)) (#1119)
  # d710fa7f  APG-2546: fold organisation into referral; drop top-level organisations[] (#1118)
  # f8e04ab0  APG-2546: scrub prisonerNumber from surviving SAR sections (#1117)
  # b7b05283  APG-2546: remove PNI + OASys PNI + Person sections from SAR (#1116)
  ```

  If main has moved past `99264496` with a further APG-2546 commit, stop — that would mean someone opened a round-3 code PR without the round-3 ticket, which shouldn't happen and needs escalation.

## Ground-truth verification against `99264496` (pre-verified 2026-08-18)

Recorded here so 13b doesn't re-derive. All verified from `origin/main @ 99264496`:

| Assertion | Ground truth on `99264496` |
|---|---|
| Top-level SAR JSON keys | `["referrals", "courseParticipation", "courses"]` (three sections) |
| `referrals[]` count in golden | 2 |
| `referrals[0].organisationName` | `"HMP Brixton"` (withdrawn referral) |
| `referrals[1].organisationName` | `"HMP Moorland"` |
| `referrals[1].originalReferral.organisationName` | `"HMP Brixton"` |
| Per-referral organisation variance | **HMP Brixton × 2, HMP Moorland × 1** |
| `originalReferral` sub-block keys | `[courseName, organisationName, submittedOn, statusCode, referrerSurname, referrerOverrideReason, hasLdc, additionalInformation]` — no `id`, no `prisonerNumber`, no UUIDs |
| `courseParticipation[0]` keys | no `prisonNumber` |
| Referral inline surname fields | `primaryPomStaffSurname` + `secondaryPomStaffSurname` present on both referrals |
| `sar_template.mustache` `<h2>` blocks | 3: `Referrals` (L2), `Course participation` (L39), `Courses` (L66) |
| `SubjectAccessRequestService.kt` | 277 lines; `Content` at L146 (3 fields); `SarReferral.organisationName` at L165; `SarOriginalReferral.organisationName` at L182 |
| `PersistenceHelper.createPerson` | **absent** (deleted PR-12) |
| `PersistenceHelper.createOasysPniResult` | **absent** (deleted PR-12) |
| `PersistenceHelper.createPniResult` | present (used by `DomainEventsListenerTest.kt:222`) |
| JSON golden trailing bytes | `}]}` — no trailing newline |
| UUID leak grep on both goldens | 0 hits |
| `expectedFlywaySchemaVersion` | `"145"` (V145 kept per PR-12) |
| Test-harness (Option 2) PDF page count | 2 pages |

If any of these disagree with the current state of `origin/main @ 99264496`, **stop** and cross-check the SHA — you may not be on `99264496`, in which case escalate before touching docs.

## Scope — 13b (fresh agent's job)

Docs-only, committed to `APG-2546/planning-sar-field-removals`. No `src/` touches. No `main` touches. No `.gitignore` touches. No PDF fabrication.

### 13b.1 — DELIVERY-LOG close-out timeline entry

Append to `doc/planning/APG-2546/DELIVERY-LOG.md`:

- A **2026-08-18 (afternoon) — Round-2 close-out documentation pass** timeline entry that:
  - References the R1/R5 resolutions and the R7 defer decision (with the rationale above).
  - Lists all round-2 merge SHAs at a glance: PR-8 `b7b05283`, PR-9 `f8e04ab0`, PR-10 `d710fa7f`, PR-11 `47488c8a`, PR-12 `99264496`. Note the round-2 elapsed calendar time from 2026-08-13 kickoff = 5 days to five-of-six merges, dramatically ahead of the 8–10-day realistic estimate.
  - Notes the shape correction (PR-13 is planning-branch-only, not a main PR; PR-6 precedent).
  - Records the current APG-2546 state: five code PRs merged, PR-13 docs close-out in flight, Branston-facing PDF pending Raby handoff, external comms drafted awaiting Raby send.
  - Marks R7 as accepted paper-cut / deferred (see rationale above), and updates the round-2 risk register table in ROUND-2-PLAN.md accordingly.

- A **PR-13 outcomes row** update in the round-2 outcomes table. Flip the existing row from "⬜ ready to start 2026-08-18" to "⏳ 13b in flight; 13c pending Raby PDF drop; 13d comms drafted awaiting send." Include SHAs of your close-out commits.

### 13b.2 — ROUND-2-PLAN status update

Update `doc/planning/APG-2546/ROUND-2-PLAN.md`:

- **R7 risk-register row** — flip mitigation from "Add to `.gitignore` in PR-12 or PR-13" to "**Accepted paper-cut / deferred out of APG-2546.** Per PR-13 shape-correction 2026-08-18: `.gitignore`-only PR to main isn't worth the coupling cost; round-2 PR agents all discipline-gated on `git status --short` before commit without incident. Fold a two-line fix into the next adjacent code PR if it re-annoys us."
- **Round-2 code delivery banner** at the top of the file already reflects five-of-six merges; leave in place.
- Bottom §"Working directory index" — flip `PR-13-round-2-docs-and-handover.md` marker from "skeleton" to "✅ fully agent-executable (13b); 13c pending PDF drop; 13d comms drafts committed" once you commit them.

### 13b.3 — README banner

`doc/planning/APG-2546/README.md` — the banner was refreshed 2026-08-18. Re-check for accuracy after your commits and touch only if drift.

### 13b.4 — This doc self-flip

Flip the top-of-file `Status:` line from "✅ 13b fully agent-executable" to "✅ 13b delivered; 13c pending PDF; 13d comms drafted" as your closing commit.

### 13b.5 — `handover/` scaffolding

Create `doc/planning/APG-2546/handover/README.md` explaining what belongs in that directory:

- `round-2-sample.pdf` — Branston-facing full-chrome PDF generated via Cameron's SAR dev-service (Option 1). Dropped by Raby / Deborah's dev after the sample generation run. See PR-13 doc §"13c — Branston PDF drop" for the recipe.
- `osar-email-draft.md` — the OSAR email template from PR-13, brackets filled in with the chosen CRN + date once known.
- `deborah-slack-dm-draft.md` — the Slack DM to Deborah, same treatment.
- (optional) `roxanne-dd-drift-nudge-draft.md` — reserved for the Roxanne rows-109/224 P.S. carried over from PR-6, only if PR-6's version didn't already close it.

### 13b.6 — Comms drafts (13d preparation)

Create both draft files under `doc/planning/APG-2546/handover/` with the templates from §"13d — comms drafts (to be sent by Raby)" below. Leave the `[CRN]` / `[date]` brackets unfilled — Raby fills them at 13c/13d time.

## Pre-13c steps (Raby, before touching the SAR dev-service)

Order matters. Both steps are documented at `doc/planning/APG-2546/handover/README.md` and staged as commit-ready drafts under `handover/`.

### Pre-13c step A — local Option 2 eyeball

Regenerate the chrome-less test-harness PDF locally on `main @ 99264496` to sanity-check the round-2 content shape *before* engaging Cameron's team. Cheap, no external asks, catches structural rendering bugs early.

```zsh
git fetch origin --prune && git checkout main && git pull --ff-only
git --no-pager log --oneline -1        # expect 99264496 at top
open -a Docker                         # macOS; skip if already up
./script/local-scripts/regenerate-sar-snapshots.sh
open build/test-generated/sar-generated-report.pdf
```

Eyeball against the sanity list in §13c step 5 below. Expected: 2 pages, three surviving `<h2>` sections (Referrals / Course participation / Courses), `organisationName` present per referral, inline POM staff surnames, no residual UUIDs. If the local PDF looks structurally wrong, **stop** — diff `sar-api-response.json` / `sar-expected-render-result.html` against `origin/main` (both should be byte-identical). Do NOT proceed to step B on a broken local render.

### Pre-13c step B — template re-registration on Slack (REQUIRED)

Post `doc/planning/APG-2546/handover/cameron-template-registration-slack-draft.md` to `#haa-sar-functionality-change-request`. **The template file changed materially between round-1 and round-2:**

- Five `<h2>` blocks removed: `PNI results`, `Person`, `OASys PNI results`, `Organisations`, `Staff`.
- `Organisation name` `<tr>` row added inline inside each referral's summary-list.
- `Prisoner number` `<tr>` row removed from every referral and every course-participation row.

Verify locally with:

```zsh
git --no-pager diff baee4510..99264496 -- src/main/resources/sar_template.mustache
```

If the SAR dev-service still holds the round-1-registered template, the round-2 review PDF will render the sections Deborah asked us to remove. So re-registration is **required**, not a courtesy — this revises earlier assumption in the previous PR-13 draft that dev-service dynamically reads main.

The Slack draft is a straight "please re-register at `99264496`" ping, not a "please confirm if needed" ping. Also asks Cameron's team to confirm the test nDelius account still has SARBT001 (round-1 saw the identity route take longer than the template registration, so parallelise if it's been rotated).

**Gate for proceeding to 13c:** Cameron's team confirms re-registration + SARBT001 available. Record confirmation timestamp in DELIVERY-LOG for the paper trail. Silence after 24h → chase in-channel. Do NOT proceed to 13c on an unconfirmed dev-service state.

## 13c — Branston-facing PDF drop (Raby step)

Follows PR-6's proven Option 1 recipe. Not fabricatable by an agent — needs interactive `sign-in-dev.hmpps.service.justice.gov.uk` access.

1. Log in to the SAR dev-service with the test nDelius account (SARBT001 role).
2. Enter preprod CRN `A9648CH` (round-1's pick — swap for a comparable rich CRN if A9648CH is no longer viable). Date range: 12-month window around most recent referral.
3. Select **Accredited Programmes only** in the product picker.
4. Wait for generation, download the PDF.
5. **Sanity-check** — the eyeball list:
   - Cover sheet + top-and-tail pages present (if absent → Option 2 fallback fired; ping Cameron's team).
   - Three surviving sections only: Referrals, Course participation, Courses. No `Staff`, no `Organisation`, no `Personal data`, no `PNI results`, no `OASys PNI results` `<h2>` blocks.
   - `prisonerNumber` / `prisonNumber` absent from every referral row and every course-participation row.
   - `organisationName` present on every referral row + `originalReferral` sub-block.
   - Referrer surnames + POM staff surnames render inline on each referral.
   - No raw internal UUIDs anywhere in the ACP payload sections.
   - Reasonable page count (low tens for a moderate subject; not four figures).
6. Commit to the planning branch:

   ```zsh
   git checkout APG-2546/planning-sar-field-removals && git pull --ff-only
   mkdir -p doc/planning/APG-2546/handover
   cp <downloaded-pdf-path> doc/planning/APG-2546/handover/round-2-sample.pdf
   git add doc/planning/APG-2546/handover/round-2-sample.pdf
   git commit -m "APG-2546 round-2: PR-13c — commit Branston sample PDF (CRN <CRN>, generated <date>)"
   git push
   ```

7. Update the DELIVERY-LOG placeholder (`<PDF: pending>` → concrete file size + CRN + generation date + your eyeball-check verdict).

## 13d — comms drafts (to be sent by Raby)

Both drafts live at `doc/planning/APG-2546/handover/*.md` and are ready to send once 13c lands. Bracketed values (`[CRN]`, `[date]`) filled in by Raby at send time.

### 13d.1 — OSAR email draft

Committed at `doc/planning/APG-2546/handover/osar-email-draft.md`.

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

### 13d.2 — Slack DM to Deborah draft

Committed at `doc/planning/APG-2546/handover/deborah-slack-dm-draft.md`.

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

### 13d.3 — Optional Roxanne DD-drift nudge

Only commit if PR-6's original P.S. (rows 109 + 224) didn't already close her records. Check DELIVERY-LOG PR-6 outcome first. Skip if closed.

## Not in scope

- **No `.gitignore` change.** R7 is deferred (see above).
- **No product code changes.** No `src/` touches at any point.
- **No PR to `main`.** Nothing in PR-13 lands on `main`.
- **No re-litigation of PR-12's V145 keep decision.** If someone raises it, link PR #1120.
- **No round-3 feedback handling.** If Branston respond to the sample PDF with change requests:
  1. Log verbatim to `doc/planning/APG-2546/round-3-branston-feedback.md`.
  2. Spin new Jira ticket (APG-25xx-round-3).
  3. Close APG-2546 with round-2 outcome regardless.
  4. Escalate hard blocks to Deborah + Sharon separately.
- **Template re-registration IS in scope** — see §"Pre-13c step B — template re-registration on Slack (REQUIRED)". Corrects the earlier PR-13 draft's incorrect "not required, courtesy only" claim: the template file changed materially round-1 → round-2, and the SAR dev-service serves the registered revision, so re-registration must happen before the Option 1 PDF run.

## Close-out condition

APG-2546 closes on **all four**:

- [ ] 13b — planning-branch close-out docs committed (this task).
- [ ] 13c — Branston-facing sample PDF committed at `doc/planning/APG-2546/handover/round-2-sample.pdf`.
- [ ] 13d — OSAR email sent + Slack DM to Deborah sent (Raby step; timestamps recorded in DELIVERY-LOG after).
- [ ] Jira APG-2546 transitioned to "content-review in-flight" (**not** Done — Jira Done fires on Branston's reply per the round-2 close-out signal).

**Not** on "zero further asks from Branston." Feedback received is the signal; further asks spin a round-3 ticket.

## Verification checklist (13b only)

```zsh
# 1. You are on the planning branch, not main.
git branch --show-current
# Expect: APG-2546/planning-sar-field-removals

# 2. Main is where the doc says it is.
git --no-pager log origin/main --oneline | head -6
# Expect: 99264496 at top.

# 3. No src/ or .gitignore changes on your commits.
git --no-pager diff --stat origin/APG-2546/planning-sar-field-removals -- src/ .gitignore
# Expect: zero files.

# 4. Handover scaffolding present.
ls doc/planning/APG-2546/handover/
# Expect: README.md, osar-email-draft.md, deborah-slack-dm-draft.md
# (round-2-sample.pdf pending Raby drop — not required for 13b sign-off)

# 5. DELIVERY-LOG close-out entry present + R7 defer recorded.
grep -nE 'PR-13|round-2 close-out|R7.*deferred|13b|13c|13d' doc/planning/APG-2546/DELIVERY-LOG.md | head
# Expect: multiple hits.

# 6. ROUND-2-PLAN R7 row flipped.
grep -nE 'R7.*deferred|Accepted paper-cut' doc/planning/APG-2546/ROUND-2-PLAN.md
# Expect: 1+ hit.
```

## Suggested execution order (13b)

1. Read `DELIVERY-LOG.md` §"Status at a glance" + the 2026-08-18 morning entry — get the current shape into your head.
2. Read `ROUND-2-PLAN.md` risk-register R7 row so you know what to flip.
3. Read this doc end-to-end.
4. Draft your DELIVERY-LOG close-out entry offline (in `/tmp/apg-2546-pr-13b-msg.txt` or a scratch file) — it's the biggest doc-add. Include all five round-2 SHAs, the R7 defer rationale, the shape-correction note, and the current 13c/13d hand-back state.
5. Create the `handover/` directory with README + two comms draft files.
6. Update the outcomes-table row for PR-13 in DELIVERY-LOG.
7. Update ROUND-2-PLAN R7 mitigation row.
8. Update this doc's status banner (13b delivered).
9. Commit + push. Suggested commit-msg (via `-F` file — do NOT use inline zsh heredoc):

   ```
   APG-2546 round-2: PR-13b — planning-branch docs close-out (shape-corrected model)

   Close-out docs pass on the planning branch. No src/ or .gitignore
   touches, no main PR. R7 explicitly deferred (accepted paper-cut).
   13c (Branston PDF drop) waits on Raby; 13d (comms send) is a Raby
   step after 13c lands.

   Round-2 code delivery closed with all five merges on main:
   PR-8 b7b05283, PR-9 f8e04ab0, PR-10 d710fa7f, PR-11 47488c8a,
   PR-12 99264496. Elapsed calendar time from 2026-08-13 kickoff
   to five-of-six merges: 5 days.

   Files:
   - doc/planning/APG-2546/DELIVERY-LOG.md — round-2 close-out
     timeline entry + PR-13 outcomes row update + R7 defer note.
   - doc/planning/APG-2546/ROUND-2-PLAN.md — R7 mitigation flipped
     to accepted / deferred.
   - doc/planning/APG-2546/PR-13-round-2-docs-and-handover.md —
     status banner flipped to 13b delivered.
   - doc/planning/APG-2546/handover/README.md — new directory
     README.
   - doc/planning/APG-2546/handover/osar-email-draft.md — new,
     from PR-13 §13d.1 template.
   - doc/planning/APG-2546/handover/deborah-slack-dm-draft.md —
     new, from PR-13 §13d.2 template.
   ```

10. Report back: commit SHAs on the planning branch, confirmation that DELIVERY-LOG + ROUND-2-PLAN + `handover/` scaffolding all landed, and any surprises. Planning agent (Raby's tracking chat) then coordinates 13c (PDF drop) and 13d (send).

## Non-obvious things

### 1. Work FROM the planning branch, but READ from `origin/main` for code truth

The planning branch's `src/` tree is pre-round-1 (merge-base `106e27d2`). Ignore it when you need to verify code shape. Use `git show origin/main:<path>` instead. This inverts the R6 discipline that governed PR-8→PR-12.

### 2. Don't fabricate the PDF

The Branston-facing PDF requires interactive access to Cameron's SAR dev-service. If you can't run the SAR-dev-service pipeline yourself, do NOT invent a PDF, do NOT commit a placeholder image, do NOT copy `build/test-generated/sar-generated-report.pdf` and call it the Branston artefact. Just complete 13b and hand back. Raby handles 13c.

### 3. R7 defer is the correct call, not a scope regression

We considered a two-line `.gitignore` PR to main and rejected it: coupling APG-2546 close-out to another review cycle for two lines of ignore pattern is a bad trade. Every APG-2546 code PR agent kept `.snyk` + xlsx out of their commits by discipline. If the noise re-annoys us, fold it into the next adjacent code PR on some other ticket.

### 4. Per-referral organisation variance is the round-2 headline demo

PR-12's fixture add makes the round-2 goldens render **HMP Brixton × 2 + HMP Moorland × 1** across three referral rows. Call this out in the OSAR email as a *"here's how per-referral wiring looks under two-organisation load"* beat — it's Deborah's ask #4 delivered with visible variance.

### 5. Commit-message hygiene

Use `git commit -F /tmp/apg-2546-pr-13b-msg.txt` — do NOT use inline zsh heredocs for multi-line commit messages. Backticks, em-dashes, `#`, `{` all get mangled and leave stuck `dquote>` prompts. Same paper-cut PR-8 hit; captured in AGENT-PROMPT-TEMPLATE session-hygiene tips.

## Rollback (13b)

Single `git revert` on the close-out commit if something goes wrong. Docs-only on the planning branch, zero runtime impact anywhere.

If PDF (13c) turns out to have a rendering defect after Raby sends it, the fix is to regenerate against a different CRN and drop-in a replacement — not to revert 13b. PR-13's paper trail stays intact; the PDF is just a snapshot artefact.

