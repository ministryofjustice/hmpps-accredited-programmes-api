# APG-2546 round-2 handover artefacts

Directory scaffolded 2026-08-18 pm as part of PR-13b (planning-branch
close-out). Everything here supports the round-2 handover to Branston /
OSAR that closes APG-2546. Nothing in this directory ever merges to
`main` — it lives on the planning branch (`APG-2546/planning-sar-field-removals`)
alongside the rest of `doc/planning/APG-2546/`.

## Contents

| File | State | Owner | Purpose |
|---|---|---|---|
| `README.md` | ✅ committed | fresh agent (PR-13b) | This file. |
| `osar-email-draft.md` | ✅ committed (template, brackets unfilled) | Raby fills `[CRN]` / `[date]` at 13d send time | OSAR round-2 review email template. To: David Evans, Sharon Hepworth, Roxanne Stephenson, William Falconer, QAT. CC: Cameron Farquhar, Deborah, Naseem Ashraf, Kiril Kolev. |
| `deborah-slack-dm-draft.md` | ✅ committed (template, brackets unfilled) | Raby fills `[CRN]` at 13d send time | Slack DM to Deborah (SDM, Cameron's SAR product team) confirming round-2 delivery + previewing the Branston-review kickoff. |
| `round-2-sample.pdf` | ⏳ **pending — Raby (13c)** | Raby (or Deborah's dev) generates via Cameron's SAR dev-service (Option 1, full-chrome) against a preprod CRN | Branston-facing full-chrome PDF for round-2 content sign-off. See PR-13 doc §13c for the recipe + eyeball-check list. |
| `roxanne-dd-drift-nudge-draft.md` | 🚫 **not committed** | Raby (only if PR-6's original P.S. didn't already close her records) | Optional DD-drift nudge to Roxanne re rows 109 + 224. Check DELIVERY-LOG PR-6 outcome first; skip if closed. |

## Sequencing (13c → 13d)

1. **13c — PDF drop (Raby):** log in to the SAR dev-service with the
   test nDelius account (SARBT001 role), enter preprod CRN
   `A9648CH` (round-1's pick — swap for a comparable rich CRN if
   A9648CH is no longer viable), select **Accredited Programmes
   only**, generate + download, eyeball-check against the sanity
   list in PR-13 §13c, then `cp` into this directory, commit +
   push. Update the DELIVERY-LOG placeholder from `<PDF: pending>`
   to concrete file size + CRN + generation date + verdict.
2. **13d — comms send (Raby, planning-agent chat):** fill in the
   `[CRN]` and `[date]` brackets in `osar-email-draft.md` and
   `deborah-slack-dm-draft.md` from 13c's outcome. Send email +
   DM. Planning agent updates DELIVERY-LOG with send timestamps
   after Raby confirms.

## Eyeball-check list for `round-2-sample.pdf` (13c)

Copied here for convenience — full context in PR-13 doc §13c step 5:

- Cover sheet + top-and-tail pages present (if absent → Option 2
  fallback fired; ping Cameron's team).
- Three surviving `<h2>` sections only: **Referrals**, **Course
  participation**, **Courses**. No `Staff`, no `Organisation`, no
  `Personal data`, no `PNI results`, no `OASys PNI results`.
- `prisonerNumber` / `prisonNumber` absent from every referral row
  and every course-participation row.
- `organisationName` present on every referral row + on each
  `originalReferral` sub-block.
- Referrer surnames + POM staff surnames render inline on each
  referral (`primaryPomStaffSurname` / `secondaryPomStaffSurname`).
- No raw internal UUIDs anywhere in the ACP payload sections.
- Reasonable page count (low tens for a moderate subject; not four
  figures — round-1 baseline was ~8,000 pages).

## Sanity anchors (from PR-13 ground-truth table @ `origin/main 99264496`)

- `referrals[]` count in golden fixture: **2**
- Per-referral organisation variance the round-2 fixture
  demonstrates: **HMP Brixton × 2, HMP Moorland × 1** (this is the
  headline demo of Deborah's ask #4 — "organisation inline on
  referral, in context").
- Test-harness (Option 2) chrome-less PDF page count on
  `99264496`: **2 pages**. Option 1 full-chrome PDF will be
  larger by cover + top-and-tail pages (round-1 comparison
  benchmark for a rich real CRN was low tens of pages).

