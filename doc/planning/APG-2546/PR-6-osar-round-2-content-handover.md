# PR-6 — OSAR round-2 content handover (content-only, no PDF)

> **Ticket:** APG-2546 • **Branch:** `APG-2546/osar-round-2-content-handover`
> • **Est.:** 0.5 dev day • **Blocks:** OSAR round-2 sign-off (content half)
> • **Depends on:** PRs 1–5 all merged to `main`
> • **Related:** APG-2547 (appearance / headers / footers — separate
> ticket, out of scope here per architecture guidance below)

## Scope-changing update (2026-08-04) — read first

The original PR-6 draft framed this as "hand over a live-like PDF".
**That is wrong.** Guidance from the Snr Tech Architect (William
Falconer, 2026-08-04 email) is that consumer teams (us) hand over
**content**, not fully-rendered PDFs:

> To be clear — we should provide content produced by the test
> harness as provided by \[Cameron's\] team for this. This is the
> agreed approach and should not include full rendering of headers
> and footers.
>
> This is produced by the SAR Service, and is outside of the scope
> of the teams building the report contents.
>
> We already have precedent in accommodation for this and expect
> to follow the same consistent approach on all teams doing this
> work.
>
> — William Falconer, 2026-08-04

Translated:

- **Our team** owns the SAR JSON payload shape (the SAR endpoint
  response) and the HTML that the test-support library renders
  from it. That's the "content".
- **Cameron's team** (the SAR worker at `../hmpps-subject-access-request-worker`)
  owns the OSAR-quality PDF wrapping — the cover page, "Official
  Sensitive" footer, subject-name / NOMIS-ID / n-Delius-case header,
  contents page, rear page, merged output. Verified in code at
  `services/pdf/v2/PdfService.kt` and
  `services/pdf/events/SubjectAccessRequestHeaderAndFooterEventHandler.kt`.
- **Precedent:** the Accommodation SAR consumer team followed this
  content-only handover pattern. William expects the same from us.

Which means PR-6 is:

1. Confirm the JSON + HTML snapshots on `main` are the final
   content shape post PRs 1–5.
2. Bundle those two files (**no PDF**) and hand to OSAR.
3. If OSAR reviewers push back on appearance (headers / footers /
   cover / branding), redirect to Cameron's team via
   `#haa-sar-functionality-change-request` under APG-2547.

## Prerequisites for a fresh agent

- All of PRs 1–5 merged to `main`. Confirm with:
  ```zsh
  git checkout main && git pull --ff-only
  git --no-pager log --oneline --grep 'APG-2546' | head -20
  ```
  Expect five merge commits (one per code PR).
- Read the round-1 planning note:
  `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`.
  You'll be appending a round-2 entry to it.
- Read this scope-changing update section end-to-end before writing
  the handover email. Do NOT include a PDF in the handover.

## Steps

### 1. Belt-and-braces snapshot regeneration on `main`

Each preceding PR should already have regenerated the snapshots.
Do a clean re-run on tip-of-`main` anyway to catch the case where
two PRs' snapshot regens got resolved incorrectly during rebasing.

```zsh
git checkout main && git pull --ff-only
git checkout -b APG-2546/osar-round-2-content-handover

SAR_GENERATE_ACTUAL=true ./gradlew clean test \
  --tests '*SarContractIntegrationTest*'

git --no-pager status --short
```

Expected: `src/test/resources/sar/sar-api-response.json` and
`src/test/resources/sar/sar-expected-render-result.html` may show
a small diff (whitespace, key ordering, or drift caused by earlier
PRs' merges). Commit any diff on this branch — do **not** edit the
snapshots by hand.

If a large diff shows up, stop and investigate — probably a
merge-conflict resolution error in one of PRs 1–5, and the
fixture no longer matches what the code produces.

Then re-run *without* the env var to prove the new snapshots pass:

```zsh
./gradlew test --tests '*SarContractIntegrationTest*'
```

### 2. Collect the handover artefacts (content only)

```zsh
mkdir -p ~/Downloads/sar-dev-3

cp src/test/resources/sar/sar-api-response.json           ~/Downloads/sar-dev-3/
cp src/test/resources/sar/sar-expected-render-result.html ~/Downloads/sar-dev-3/
```

That's it. **Do not copy `build/test-generated/sar-generated-report.pdf`**
into the handover directory — it's a bare content-dump PDF produced
by the test-support library with no cover / headers / footers, and
handing it to OSAR would (a) contradict William's guidance and (b)
invite exactly the "appearance" pushback that belongs to APG-2547 /
Cameron's team, not us.

If you want to eyeball the content locally for your own sanity
before sending, opening `sar-expected-render-result.html` in a
browser is the closest thing to what OSAR will see.

Record the file sizes:

```zsh
ls -lh ~/Downloads/sar-dev-3/
```

### 3. Append the round-2 run-log entry

Add to `doc/planning/APG-2495-post-deploy-retest-live-like-sar.md`
under a new heading:

```markdown
## OSAR round 2 (2026-08-XX)

Regenerated after APG-2546 PRs 1–5 merged. Content-only handover
to OSAR (Sharon + Roxanne + QAT), per William Falconer's 2026-08-04
guidance that consumer teams provide content only, not fully
rendered PDFs. Appearance / cover / headers / footers are
Cameron's team's remit under APG-2547.

Artefacts handed over in `~/Downloads/sar-dev-3/`:

- `sar-api-response.json` — SAR JSON payload (N KB)
- `sar-expected-render-result.html` — HTML content rendered by
  the `hmpps-subject-access-request-test-support` library (N KB)

**Not included** (deliberately, per William):

- The bare `sar-generated-report.pdf` from
  `build/test-generated/`. That's a test-harness readability
  aid, not an OSAR-quality PDF.

Removed sections vs round 1:

- `auditRecords` (PR-1) — the "28,483 rows" contributor
- `referralStatusHistory` + `referralStatusReasons` (PR-2)
- `sexualOffenceDetails` + `selectedSexualOffenceDetails` (PR-3)
- `oasysPniResults` (PR-4, per Roxanne's Q1 answer) *or* the
  three ID fields inside it, keeping `programmePathway` (PR-4
  Option B). Update this line based on which option shipped.
- `SarPerson.id` and `SarOrganisation.id` (PR-5)

Internal readability check (test-harness PDF, **not** the OSAR
handover artefact — for our own sanity only): dropped from
round-1 baseline ~8,000 pages to ~N pages.

Ready for OSAR **content** sign-off.
```

Fill in the actual sizes / page counts before committing.

### 4. Update the APG-2546 planning-doc artefacts table + delivery log

Open `doc/planning/APG-2546-sar-field-removals.md`, scroll to
"Artefacts to capture", fill in merged commit hashes across all
six rows:

```zsh
for pr in remove-audit-records \
          remove-status-history-and-reasons \
          remove-sexual-offence-details \
          remove-oasys-pni-results \
          strip-internal-ids \
          osar-round-2-content-handover; do
  echo "=== APG-2546/$pr ==="
  git --no-pager log --oneline --grep "APG-2546.*$pr" main | head -1
done
```

Also fill in the delivery log
(`doc/planning/APG-2546/DELIVERY-LOG.md`) — flip statuses, record
the test-harness PDF page count as an internal readability metric
only, and note the content-only handover framing.

### 5. Send the OSAR content-sign-off email

Draft below. Adjust names / channels as needed.

```
To: Sharon <X>, Roxanne <X>, William Falconer <X>, QAT <X>
CC: #osar-review (if email-to-Slack is configured)
Subject: APG-2546 — Accredited Programmes SAR content ready for OSAR round-2 review

Hi all,

APG-2546 is complete on main. Following William's guidance from
2026-08-04, this handover is content-only — the JSON payload our
SAR endpoint produces plus the HTML rendered by the shared
test-support library. Cameron's team wraps that content in the
standard OSAR-quality PDF (cover / headers / footers / branding)
in production, and any concerns on that appearance side sit with
them under APG-2547 rather than with us.

Attached / in ~/Downloads/sar-dev-3/:

- sar-api-response.json — the SAR JSON payload
- sar-expected-render-result.html — the HTML content the shared
  test harness renders from that JSON. Opening it in a browser
  is the closest we can give you to how the content will read
  once Cameron's team wraps it.

Content changes vs round 1 (Roxanne's red-flagged rows on the DD
spreadsheet from 30 July, sheet "Accredited Programmes Custody"):

- auditRecords section — removed in full (the "8,000-page PDF"
  contributor). Rows 22–31 on DD sheet.
- referralStatusHistory + referralStatusReasons — removed in
  full. Rows 192–201, 205–209.
- sexualOffenceDetails + selectedSexualOffenceDetails —
  removed in full. Rows 233/234/235/237; row 225's table-level
  note directly rebutted the "keep the join table" fallback.
- oasysPniResults — [Option A: removed in full | Option B:
  three ID fields stripped, programme_pathway kept]. Rows 85–88.
- SarPerson.id / SarOrganisation.id — removed. Rows 105, 111.
- SarPniResult IDs (rows 127/128/131) — already absent from the
  DTO; comment-only cleanup, no code change.

Cross-checked against previously-flagged (already delivered) rows:
- SarReferral.deleted (row 160) — APG-2491.
- SarStaff.username (implicit) — APG-2510.
- Referrer surname resolution (rows 23, 30, 159) — APG-2492.

Ask: content sign-off, 5 working days if you can. Anything on
appearance / cover / headers / footers, please loop in Cameron's
team on #haa-sar-functionality-change-request under APG-2547.

Thanks,
Raby
```

## Verification checklist

```zsh
# 1. Snapshot fixtures match code
./gradlew test --tests '*SarContractIntegrationTest*'   # green

# 2. Handover directory contains only the two content files
ls -lh ~/Downloads/sar-dev-3/
# Expected: sar-api-response.json + sar-expected-render-result.html
# No PDF. If there's a PDF, delete it before sending.

# 3. Planning docs updated
git --no-pager diff main -- doc/planning/APG-2495-post-deploy-retest-live-like-sar.md
git --no-pager diff main -- doc/planning/APG-2546-sar-field-removals.md
git --no-pager diff main -- doc/planning/APG-2546/DELIVERY-LOG.md

# 4. No source or test-source changes on this branch
git --no-pager diff --stat main -- src/main src/test/kotlin
# Expected: zero files. Snapshot resources under src/test/resources may diff.
```

## Non-obvious things

### 1. The two "PDFs" in the codebase

There are two different PDF paths in the wider SAR ecosystem and
the terminology gets confusing:

- **`build/test-generated/sar-generated-report.pdf`** — produced
  by `hmpps-subject-access-request-test-support:2.4.2` during our
  contract integration test. No chrome (no cover, no header, no
  footer). Purely a content dump for local eyeballing. **Not for
  OSAR.**
- **The real OSAR PDF** — produced by the SAR worker
  (`hmpps-subject-access-request-worker`) using iText, with all
  the standard chrome. Owned by Cameron's team. **Not our
  concern for APG-2546.**

If you find yourself measuring the test-harness PDF and reporting
it externally, stop and re-read this section.

### 2. Internal readability metric is still useful

The test-harness PDF page count (round-1 ~8,000 → post-PR-2
already 3 pages) is a valid *internal* proxy for "how much
content is in the SAR now". It's what we've been tracking in
the delivery log, and it's genuinely reassuring evidence that
the OSAR readability problem is fixed at the content level.

But it does NOT go in the OSAR handover email or the run-log
entry as the primary metric — those emphasise the *content* delta
(what sections were removed, why, which DD rows), not the
page-count of a chromeless PDF that OSAR will never see.

### 3. If OSAR asks for a rendered preview anyway

Some OSAR reviewers may still ask "can you show us what it will
look like?". Options:

- **Preferred:** point them at Cameron's team for a preview under
  APG-2547 — they can render a real end-to-end PDF from our
  updated JSON payload.
- **Fallback:** send them the HTML file with a note that it is
  the content only, no styling, and Cameron's team owns the final
  presentation. Explicitly do NOT send them the test-harness PDF
  and imply it's representative of what they'll see.

### 4. Round-2 does not need a fresh live-like SAR run

Round 1 involved a live-like SAR against PRN `A8610DY` for the
8,000-page evidence. Round 2 does not — the fixture snapshots
already capture the content shape post PRs 1–5, and William's
guidance is that content review, not appearance review, is the
ask. If OSAR later escalate to a full live-like run, that's a
separate exercise (probably co-owned with Cameron's team).

## PR description template

```
APG-2546: OSAR round-2 content handover — docs-only

Docs + regenerated snapshots. No src/main or src/test/kotlin
changes.

Scope change vs original plan: content-only handover per William
Falconer's 2026-08-04 guidance. No PDF in the handover bundle —
Cameron's team (SAR worker) owns the OSAR-quality PDF wrapping
under APG-2547.

Changes:
- Clean regen of SAR contract snapshots on tip-of-main (fixtures
  may show a small merge-consolidation diff — no code drift).
- Append "OSAR round 2 (2026-08-XX)" content-only run-log entry
  to doc/planning/APG-2495-post-deploy-retest-live-like-sar.md.
- Fill in merged commit hashes in the artefacts table of
  doc/planning/APG-2546-sar-field-removals.md.
- Flip statuses + record content handover in
  doc/planning/APG-2546/DELIVERY-LOG.md.

Handover artefacts (in ~/Downloads/sar-dev-3/):
- sar-api-response.json
- sar-expected-render-result.html
(No PDF — deliberately.)

Follow-up: OSAR content sign-off email sent to Sharon, Roxanne,
William Falconer, QAT. APG-2547 (appearance) continues with
Cameron's team on #haa-sar-functionality-change-request.
```

## Definition of done

- [ ] Snapshot regen ran clean on tip-of-`main`; diffs (if any) committed.
- [ ] `~/Downloads/sar-dev-3/` contains **exactly** the two content
      files (no PDF).
- [ ] APG-2495 run-log entry appended with real sizes + the
      content-only framing + the APG-2547 pointer.
- [ ] APG-2546 artefacts table filled in with real commit hashes.
- [ ] Delivery log flipped for PR-6 with content-only note.
- [ ] Handover email sent; thread archived / linked in the
      delivery log.
- [ ] APG-2546 ticket transitioned to "awaiting OSAR content review".
- [ ] APG-2547 either (a) already progressing on Cameron's team's
      side, or (b) explicitly noted in the delivery log as the
      known follow-up for the appearance half of OSAR sign-off.

