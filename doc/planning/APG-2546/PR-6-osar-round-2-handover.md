# PR-6 — OSAR round-2 handover (Option 1 primary, Option 2 fallback)

> **Ticket:** APG-2546 • **Branch:** `APG-2546/osar-round-2-handover`
> • **Est.:** 0.5–1 dev day (depending on Option 1 pipeline responsiveness)
> • **Blocks:** OSAR round-2 sign-off
> • **Depends on:** PRs 1–5 all merged to `main` **and deployed to DEV**
> • **Related:** APG-2547 (tracked separately; see below)

## Scope-changing update (2026-08-04 pm) — read first

The scope of PR-6 has moved twice in a day:

1. **Original plan** — hand over a live-like PDF generated in this
   repo.
2. **Post-William-email revision** (morning) — content-only handover
   (JSON + HTML), no PDF.
3. **Post-Deborah-clarification** (afternoon, current) — **hand over
   a proper PDF, produced by Cameron's team's SAR product.** OSAR
   prefer the full-chrome PDF (Option 1). William's steer was
   pointing at the chrome-less fallback (Option 2), not saying
   "don't ship a PDF at all".

Deborah (Senior DM on the SAR product, DM 2026-08-04) laid out
both options in one message:

- **Option 1 — OSAR-preferred, full chrome.** Register our
  template with Cameron's team, push to DEV, add `SARBT001` role
  to a test nDelius account, run a report via
  `sign-in-dev.hmpps.service.justice.gov.uk`. Report is
  generated with the standard cover-sheet + top-and-tail pages
  and downloaded as a proper PDF.
- **Option 2 — fallback for when Option 1 pipeline is blocked.**
  Cameron's team's test harness / library (Indy's, documented on
  the SAR Confluence at <https://dsdmoj.atlassian.net/wiki/x/DgMOaQE>)
  produces the same PDF **without** cover sheets. Same PDF our
  `SarContractIntegrationTest` already emits at
  `build/test-generated/sar-generated-report.pdf` (95% sure — see
  §"Non-obvious things" for the verification step).

Rationale for the two options existing at all: Deborah's team
pre-empted the case where Option 1 (real dev pipeline) is stuck
for reasons outside the consumer team's control — exactly what
happened during round 1 back in July.

**Both options produce a PDF** that David Evans (Offender
Information Access & Registry Project Lead) and his OSAR reviewers
can open in Preview / Adobe Reader. Both are valid handover
artefacts. Option 1 is the ask if we can get it.

## Prerequisites for a fresh agent

- All of PRs 1–5 merged to `main`. Confirm with:
  ```zsh
  git checkout main && git pull --ff-only
  git --no-pager log --oneline --grep 'APG-2546' | head -20
  ```
  Expect five merge commits (one per code PR).
- Automatic dev deploy from `main` completed. Check the dev pipeline
  in CircleCI/GitHub Actions or ask in `#hmpps-accredited-programmes`.
- Read Indy's Confluence page for Option 2 in case Option 1 stalls:
  <https://dsdmoj.atlassian.net/wiki/x/DgMOaQE>.
- Test nDelius account with `SARBT001` role. If not already added,
  request via `#haa-sar-functionality-change-request`. This can be
  done in parallel with template registration to save time.
- Access to `~/Downloads/sar-dev-3/` (kept as a local handover
  directory since round 1's `sar-dev-1`).

## Option 1 — primary path

### 1. Confirm main + dev are in sync

```zsh
git checkout main && git pull --ff-only
git --no-pager log --oneline --grep 'APG-2546' | head -20
# expect: PR-1..PR-5 merges all listed
```

Verify dev deploy is green (CircleCI / GitHub Actions dashboard or
`#hmpps-accredited-programmes` deploy log). If dev isn't up to date
with `main`, wait — or push through with `docker pull` on the dev
namespace if you have platform access.

### 2. Register our template with Cameron's team

Post in `#haa-sar-functionality-change-request` with:

- Ticket: APG-2546
- Product: Accredited Programmes API
- Template link on `main`:
  `src/main/resources/sar_template.mustache`
  (permalink to the commit on `main` post-PR-5 is ideal so they
  register the exact revision)
- Ask: "please register the updated template on the SAR dev
  service for OSAR round-2 review"

Deborah's guidance: "One of my devs will do that for you." Expect
a same-day or next-day turnaround if the channel is quiet. Note
the message time and ping again after 24h if silent.

### 3. Get `SARBT001` role added if not already

If your test nDelius account doesn't already have `SARBT001`
(HMPPS Subject Access Report User), request in the same channel or
via your usual identity-team route. The role is what surfaces the
Subject Access Request tile on the Auth home page.

### 4. Generate the test report

Once template is registered and role is in place:

1. Log in to <https://sign-in-dev.hmpps.service.justice.gov.uk>
   with the test nDelius account.
2. Click through to the Subject Access Request tile.
3. Enter an nDelius CRN + date range. Use one that has decent
   Accredited Programmes data:
   - Round-1 used the preprod PRN `A0137CY` (per your 24 Jul
     email). Check that CRN is still present in dev, or pick a
     comparable one with ≥1 referral + ≥1 participation.
   - Restrict the date range to shrink noise — a 12-month window
     around the most recent referral is usually enough.
4. Select **Accredited Programmes only** in the product picker
   (unless OSAR specifically asked for a combined report — round 2
   is scoped to our product).
5. Wait for generation, then download the PDF.

### 5. Sanity-check the PDF locally before sending

Open the downloaded PDF and eyeball:

- **Cover sheet + top-and-tail pages present.** If absent, it's
  Option 2 output — you've hit the fallback path without meaning
  to. Ping Cameron's team, don't send.
- **The removed sections are actually absent** — no
  `auditRecords`, no `referralStatusHistory` /
  `referralStatusReasons`, no `sexualOffenceDetails` /
  `selectedSexualOffenceDetails`, and either no `oasysPniResults`
  or (Option B) the section only shows `programmePathway`.
- **`SarPerson.id` and `SarOrganisation.id` are absent** from
  their respective sections.
- **Referrer surnames render** (APG-2492 territory, cross-check),
  and staff `username` is absent (APG-2510 territory).
- **Page count is roughly what you'd expect** — for a
  moderate-sized subject, low tens of pages of Accredited
  Programmes content. If it's still four figures, something's off
  and you should stop and diff against the JSON fixture on `main`.

### 6. Copy to handover directory and email OSAR

```zsh
mkdir -p ~/Downloads/sar-dev-3
cp <downloaded-pdf> ~/Downloads/sar-dev-3/accredited-programmes-sar-round2.pdf

# Optional evidence trail:
cp src/test/resources/sar/sar-api-response.json           ~/Downloads/sar-dev-3/
cp src/test/resources/sar/sar-expected-render-result.html ~/Downloads/sar-dev-3/
```

Email OSAR (see §"OSAR email draft" below).

## Option 2 — fallback path (if Option 1 stalls)

Trigger conditions:

- Template registration hasn't happened after 2 working days on
  the channel and the dev pipeline is confirmed stuck.
- `SARBT001` role assignment stalled by identity team.
- Any similar release-pipeline block Deborah's team explicitly
  built Option 2 for.

Steps (high-level — follow the authoritative version on
<https://dsdmoj.atlassian.net/wiki/x/DgMOaQE>):

1. Regenerate our snapshots on `main` as a belt-and-braces:
   ```zsh
   SAR_GENERATE_ACTUAL=true ./gradlew clean test --tests '*SarContractIntegrationTest*'
   ```
2. Locate the generated PDF at
   `build/test-generated/sar-generated-report.pdf`. This is the
   Option 2 output — chrome-less but a valid PDF. Verify by
   opening it: no cover sheet, no "Official Sensitive" footer.
3. Copy into the handover directory:
   ```zsh
   cp build/test-generated/sar-generated-report.pdf ~/Downloads/sar-dev-3/accredited-programmes-sar-round2-fallback.pdf
   ```
4. Send with an explicit note that this is the Option 2
   (chrome-less) fallback because Option 1 was blocked, and offer
   to re-send the Option 1 PDF once the pipeline is unblocked.

**Do NOT do a hybrid** — do not fabricate a cover sheet locally to
"make Option 2 look like Option 1". That's exactly the "full
rendering of headers and footers" William's email warned against.

## OSAR email draft (Option 1 wording)

```
To: David Evans, Sharon Hepworth, Roxanne Stephenson, William Falconer, QAT
CC: Cameron Farquhar (as courtesy — his team registered the template),
    Naseem Ashraf, Kiril Kolev
Subject: APG-2546 — Accredited Programmes SAR round-2 review PDF

Hi all,

APG-2546 is complete on main and deployed to dev. Attached is the
round-2 SAR review PDF, generated via the SAR dev service against
CRN [X] on [date] — full standard cover-sheet + top-and-tail
pages, per OSAR preference (thanks to Deborah and Cameron's team
for getting our updated template registered quickly).

Content changes vs round 1 (Roxanne's red-flagged rows on the DD
spreadsheet from 30 July, sheet "Accredited Programmes Custody"):

- auditRecords section — removed in full (the "8,000-page PDF"
  contributor). Rows 22–31.
- referralStatusHistory + referralStatusReasons — removed in
  full. Rows 192–201, 205–209.
- sexualOffenceDetails + selectedSexualOffenceDetails — removed
  in full. Rows 233/234/235/237; row 225's table-level note
  directly rebutted the "keep the join table" fallback.
- oasysPniResults — [Option A: removed in full | Option B: three
  ID fields stripped, programme_pathway kept]. Rows 85–88.
- SarPerson.id / SarOrganisation.id — removed. Rows 105, 111.
- SarPniResult IDs (rows 127/128/131) — already absent from the
  DTO; comment-only cleanup, no code change.

Cross-checked against already-delivered rows:
- SarReferral.deleted (row 160) — APG-2491.
- SarStaff.username (implicit) — APG-2510.
- Referrer surname resolution (rows 23, 30, 159) — APG-2492.

Ask: content sign-off, 5 working days if you can. Appearance /
cover-sheet / header-footer feedback (if any) sits with Cameron's
team under APG-2547.

Thanks,
Raby
```

Fallback wording (Option 2) — swap the "Full standard cover-sheet
+ top-and-tail pages, per OSAR preference" sentence for:

> Attached is the round-2 SAR review PDF, generated via the
> Option 2 test-harness route (chrome-less) because Option 1 dev
> pipeline is currently blocked. Content is identical to what
> Option 1 will produce; happy to re-send the full-chrome PDF
> once the pipeline is unblocked.

## Verification checklist

```zsh
# 1. main is what we think it is
git --no-pager log --oneline --grep 'APG-2546' main | head -10

# 2. Handover directory contents (Option 1)
ls -lh ~/Downloads/sar-dev-3/
# Expected: accredited-programmes-sar-round2.pdf (with cover sheet)
# Optional: sar-api-response.json + sar-expected-render-result.html
#           (evidence trail; NOT the primary artefact)

# 3. Planning docs updated
git --no-pager diff main -- doc/planning/APG-2495-post-deploy-retest-live-like-sar.md
git --no-pager diff main -- doc/planning/APG-2546-sar-field-removals.md
git --no-pager diff main -- doc/planning/APG-2546/DELIVERY-LOG.md

# 4. No src/main or src/test/kotlin changes on this branch
git --no-pager diff --stat main -- src/main src/test/kotlin
# Expected: zero files.
```

## Non-obvious things

### 1. Verify Option 2 output = our contract-test PDF

Before relying on `build/test-generated/sar-generated-report.pdf`
as the Option 2 artefact, read Indy's Confluence page and confirm
the library it references is the same as the one we depend on
(`hmpps-subject-access-request-test-support:2.4.2` at
`build.gradle.kts:71`). If they diverge, follow Confluence, not
this doc.

### 2. Cameron's team may need up to 2 working days to register the template

Deborah's message implied same-day turnaround if the channel is
quiet, but round 1 saw a pipeline block. Kick Option 1 off early
so there's slack if it stalls. Don't wait until PR-5 merges to
post — post as soon as PR-5 is on `main`.

### 3. Test-harness PDF page count is still a useful internal metric

The ~8,000 → 3-page drop between round 1 and round 2 is a valid
proxy for "how much content is in the SAR now". It's what the
delivery-log 📉 banner tracks. But once we've generated the
Option 1 PDF, the *authoritative* page count (with cover sheets)
comes from there — record both in the artefacts table for
clarity.

### 4. Populated-fields concern

Deborah flagged that OSAR prefer as many fields populated as
possible in the sample, for two reasons:

- Vettors (redaction reviewers) need to see every field type to
  understand what data appears and match it against our domain.
- Vettors need training material — including examples of fields
  that need redaction and fields that don't.

Under **Option 1**, the sample is generated from a real dev
subject — so we depend on the chosen CRN having rich data. Pick a
CRN with maximum coverage. If no single CRN has enough coverage,
consider whether to seed one specifically for OSAR review (out of
scope for PR-6, but flag to Roxanne for round 3 planning if it
comes up).

Under **Option 2**, the fixture at
`src/test/resources/sar/sar-api-response.json` is what gets
rendered. That fixture currently has minimum-viable coverage —
several derived fields render as `null`. Consider whether to beef
it up before shipping Option 2, but treat as a nice-to-have not a
blocker.

### 5. APG-2547 status is clarified, not resolved

Pre-Deborah, APG-2547 was framed as "everything visual, wholly
Cameron's team's problem". That framing was partially right:
appearance still is Cameron's team's remit, but our participation
is required to get the template *registered* against their SAR
product. So APG-2547 is a coordination ticket, not a
"we-don't-touch-it" ticket. Reflect this in the delivery log.

## PR description template

```
APG-2546: OSAR round-2 handover — docs-only (PR-6)

Docs + planning-branch updates. No src/main or src/test/kotlin
changes.

Round-2 handover follows Option 1 (OSAR-preferred, full-chrome
PDF from Cameron's team's SAR dev service) per Deborah's
2026-08-04 clarification. Option 2 (chrome-less fallback via
Indy's test harness) documented in case the pipeline blocks.

Changes:
- Append "OSAR round 2 (2026-08-XX)" entry to
  doc/planning/APG-2495-post-deploy-retest-live-like-sar.md.
- Fill in merged commit hashes in the artefacts table of
  doc/planning/APG-2546-sar-field-removals.md.
- Flip statuses + record the actual handover route in
  doc/planning/APG-2546/DELIVERY-LOG.md.

Handover artefact:
- ~/Downloads/sar-dev-3/accredited-programmes-sar-round2.pdf
  (Option 1) OR
- ~/Downloads/sar-dev-3/accredited-programmes-sar-round2-fallback.pdf
  (Option 2)

Follow-up: OSAR round-2 review 5 working days from send.
Appearance feedback (if any) → Cameron's team, APG-2547.
```

## Definition of done

- [ ] PRs 1–5 all merged to `main` and deployed to DEV.
- [ ] Template registered on the SAR dev service (or the
      documented reason Option 2 was used instead).
- [ ] `~/Downloads/sar-dev-3/` contains the round-2 PDF.
- [ ] PDF eyeballed against the "sanity-check" list above; nothing
      obviously wrong.
- [ ] APG-2495 run-log entry appended, real numbers.
- [ ] APG-2546 artefacts table filled in.
- [ ] Delivery log flipped for PR-6.
- [ ] OSAR email sent.
- [ ] APG-2546 ticket transitioned to "awaiting OSAR content review".
- [ ] APG-2547 status noted in delivery log with the current
      coordination state.

