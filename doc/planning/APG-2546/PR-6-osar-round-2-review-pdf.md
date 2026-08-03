# PR-6 — Regenerate the live-like OSAR round-2 review PDF

> **Ticket:** APG-2546 • **Branch:** `APG-2546/osar-round-2-review-pdf`
> • **Est.:** 0.5 dev day • **Blocks:** OSAR round-2 sign-off
> • **Depends on:** PRs 1–5 all merged to `main`

## Purpose

Docs-only PR. Regenerates the SAR test fixture PDF that OSAR reviews
to sign the API off content-wise, hands it over to the reviewers, and
appends a run-log entry to the round-1 planning note so the paper
trail stays intact for future audits.

Target PDF size: ~4–5 pages (down from the 8,000-page round-1 PDF).

## Prerequisites for a fresh agent

- All of PRs 1–5 merged to `main`. Confirm with:
  ```zsh
  git log --oneline --grep 'APG-2546' main | head -20
  ```
  Expect five merge commits (one per code PR).
- Read the round-1 planning note:
  `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`.
  You'll be appending to it.
- You need write access to `~/Downloads/sar-dev-3/` (the local
  handover directory used for round 1). If it doesn't exist,
  create it and note the path in the handover email.

## Steps

### 1. Belt-and-braces snapshot regeneration on `main`

Each preceding PR should already have regenerated the snapshots.
Do a clean re-run on tip-of-`main` anyway — this catches the case
where two PRs' snapshot regens went into conflict during rebasing.

```zsh
git checkout main && git pull --ff-only
git checkout -b APG-2546/osar-round-2-review-pdf

SAR_GENERATE_ACTUAL=true ./gradlew clean test \
  --tests '*SarContractIntegrationTest*'

git status --short
```

Expected: `src/test/resources/sar/sar-api-response.json` and
`src/test/resources/sar/sar-expected-render-result.html` may show
a small diff (whitespace, key ordering, or drift caused by the
earlier PRs' merges). Commit any diff on this branch — do **not**
edit the snapshots by hand.

If a large diff shows up, stop and investigate: probably a
merge conflict was resolved incorrectly in one of PR-1 through
PR-5, and the fixture no longer matches what the code produces.

### 2. Collect the artefacts

The SAR contract integration test writes the generated PDF to:

```
build/test-generated/sar-generated-report.pdf
```

Also collect:

- `src/test/resources/sar/sar-api-response.json` — the SAR JSON
- `src/test/resources/sar/sar-expected-render-result.html` — the
  rendered HTML (what the aggregator would render into PDF)

Copy all three into `~/Downloads/sar-dev-3/`:

```zsh
mkdir -p ~/Downloads/sar-dev-3
cp build/test-generated/sar-generated-report.pdf ~/Downloads/sar-dev-3/
cp src/test/resources/sar/sar-api-response.json ~/Downloads/sar-dev-3/
cp src/test/resources/sar/sar-expected-render-result.html ~/Downloads/sar-dev-3/
```

Record the PDF page count:

```zsh
mdls -name kMDItemNumberOfPages ~/Downloads/sar-dev-3/sar-generated-report.pdf
```

(or open the PDF and check the last page number — Preview shows it
in the toolbar).

### 3. Append the run-log entry

Add a new heading to
`doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`:

```markdown
## OSAR round 2 (2026-08-XX)

Regenerated after APG-2546 PRs 1–5 merged. Artefacts handed to
Sharon + Roxanne + QAT in `~/Downloads/sar-dev-3/`.

- **PDF page count:** N pages (down from ~8,000 pages round-1)
- **JSON size:** N KB (down from N MB round-1)
- **HTML size:** N KB (down from N MB round-1)

Removed sections vs round 1:

- `auditRecords` (PR-1) — the "28,483 rows" contributor
- `referralStatusHistory` + `referralStatusReasons` (PR-2)
- `sexualOffenceDetails` + `selectedSexualOffenceDetails` (PR-3)
- `oasysPniResults` (PR-4, per Roxanne's Q1 answer) *or* only the
  two internal IDs (PR-4 Option B — update this line based on
  which option shipped)
- `SarPerson.id` and `SarOrganisation.id` (PR-5)

Ready for OSAR content sign-off.
```

Fill in the actual numbers before committing.

### 4. Update the APG-2546 planning-doc artefacts table

Open `doc/planning/APG-2546-sar-field-removals.md`, scroll to the
"Artefacts to capture" table near the bottom, and fill in the
merged commit hashes + PDF page counts across all six rows:

```zsh
for pr in remove-audit-records remove-status-history-and-reasons \
          remove-sexual-offence-details remove-oasys-pni-results \
          strip-internal-ids osar-round-2-review-pdf; do
  echo "=== APG-2546/$pr ==="
  git --no-pager log --oneline --grep "APG-2546.*$pr" main | head -1
done
```

Paste each commit's short SHA into the table.

### 5. Send the OSAR handover email

Reuse the framing from the round-1 handover email (search Sent
folder for "SAR test-harness content sign-off" or similar). Key
points:

- Round 2 of the OSAR content review, following the DD spreadsheet
  red-flag rows Roxanne raised on 30 Jul.
- Attached: the three artefacts from `~/Downloads/sar-dev-3/`.
- Ask for content sign-off, not appearance sign-off (cover sheet /
  dev-portal artefacts are still blocked on the aggregator team's
  pipeline — separate channel, not this review).
- CC: Sharon, Roxanne, William Falconer, `#osar-review` if that
  workflow uses email-to-Slack.
- Deadline: 5 working days for review-comments back; anything
  after that goes into an APG-2547 follow-up.

## Verification checklist

```zsh
# 1. Snapshot fixtures match what the code produces
./gradlew test --tests '*SarContractIntegrationTest*'   # green

# 2. Artefact files exist and are non-empty
ls -lh ~/Downloads/sar-dev-3/

# 3. Planning docs updated
git --no-pager diff main -- doc/planning/APG-2495-post-deploy-retest-live-like-sar.md
git --no-pager diff main -- doc/planning/APG-2546-sar-field-removals.md

# 4. No source or test file changes on this branch
git --no-pager diff --stat main -- src/   # expect only snapshot files, or nothing
```

## Non-obvious things

### 1. Handover directory naming

`~/Downloads/sar-dev-3/` — the `-3` counts the retest number, not
the OSAR review round. This is the third dev-generated SAR bundle
in the ticket family (round-1 was `sar-dev-1`, an intermediate
retest was `sar-dev-2`). If you need to look up round-1 artefacts
they're in `sar-dev-1/`.

### 2. No source code changes on this PR

If you find yourself editing anything under `src/main/`, stop —
you're in scope for a follow-up code PR (probably APG-2547), not
PR-6. PR-6 is docs + regenerated fixtures + handover only.

### 3. If OSAR come back asking for something restored

Do not revert one of PRs 1–5. Open a new PR that re-adds only the
requested subset — see the "Rollback plan" section of
`doc/planning/APG-2546-sar-field-removals.md`.

## PR description template

```
APG-2546: regenerate live-like OSAR review PDF (round 2)

Docs + regenerated fixtures only. Ships the round-2 SAR review
artefact to OSAR following PRs 1–5.

Changes:
- Clean regen of SAR contract snapshots on tip-of-main (fixtures
  may show a small merge-consolidation diff — no code drift)
- Append "OSAR round 2 (2026-08-XX)" run-log entry to
  doc/planning/APG-2495-post-deploy-retest-live-like-sar.md
- Fill in merged commit hashes + PDF page counts in the artefacts
  table of doc/planning/APG-2546-sar-field-removals.md

Not changed:
- Any src/main file
- Any src/test source file (only snapshot resources may diff)

Follow-up: OSAR content sign-off email handover to Sharon,
Roxanne, QAT, William Falconer. Aggregator-team dev-portal /
cover-sheet appearance work continues on
#haa-sar-functionality-change-request — out of scope here.
```

## Definition of done

- [ ] Snapshot regen ran clean on tip-of-`main`.
- [ ] Three artefact files in `~/Downloads/sar-dev-3/`.
- [ ] APG-2495 run-log entry appended with real numbers.
- [ ] APG-2546 artefacts table filled in with real commit hashes
      and PDF page counts.
- [ ] Handover email sent, thread linked in
      `doc/planning/APG-2546-sar-field-removals.md` "Origin of this
      work" section as an epilogue.
- [ ] APG-2546 ticket transitioned to "awaiting OSAR review".

