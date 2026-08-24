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
| `cameron-template-registration-slack-draft.md` | ✅ sent 2026-08-18; ✅ confirmed same-day ("that's done in dev") | Raby (dev registration — actioned) | Pre-13c Slack ping to Cameron's SAR product team (`#haa-sar-functionality-change-request`) — **template changed round-1 → round-2 (five `<h2>` blocks removed, `Organisation name` row added inline, `Prisoner number` rows removed), so re-registration was REQUIRED, not a courtesy check**. Also confirmed SARBT001 still on the test nDelius account. |
| `cameron-template-registration-preprod-slack-draft.md` | ✅ committed (ready to send) | Raby sends after preprod ACP deploy verified (done 2026-08-19) | **Preprod** template re-registration ping — same channel, same template diff, distinct SAR-service environment. Post-OSAR-sign-off step so real preprod SAR requests hitting Accredited Programmes render the round-2 shape. |
| `osar-email-draft.md` | ✅ committed (template, brackets unfilled) | Raby fills `[CRN]` / `[date]` at 13d send time | OSAR round-2 review email template. To: David Evans, Sharon Hepworth, Roxanne Stephenson, William Falconer, QAT. CC: Cameron Farquhar, Deborah, Naseem Ashraf, Kiril Kolev. |
| `deborah-slack-dm-draft.md` | ✅ committed (template, brackets unfilled) | Raby fills `[CRN]` at 13d send time | Slack DM to Deborah (SDM, Cameron's SAR product team) confirming round-2 delivery + previewing the Branston-review kickoff. |
| `roxanne-dd-update-email-draft.md` | ✅ committed (ready to send) 2026-08-20 | Raby sends after Roxanne's 2026-08-19 email asking for the updated DD | Reply email to Roxanne with the updated DD attached. 69 column-H changes on `Accredited Programmes Custody` (67 Yes→No removals across rounds 1 + 2, 2 DD-drift corrections on rows 109 + 224). Attachment lives in `~/Downloads/` (untracked in git per repo convention). Regenerable via `doc/planning/APG-2546/scripts/dd-column-h-update.py`. |
| `roxanne-followup-course-name-reply-draft.md` | ✅ committed (ready to send) 2026-08-20 pm | Raby sends after Roxanne's 2026-08-20 follow-up asking for `courseName` inline on each referral | Confirmation reply. Scoping call: fold in as PR-14 (round-2 addendum) rather than spinning a new ticket. Working doc `PR-14-course-name-into-referral.md` scaffolded. |
| `haar-team-prod-registration-slack-reply-draft.md` | ✅ committed (ready to send) 2026-08-21 am; **re-framed 2026-08-24 am** | Raby sends in-thread on the same 2026-08-20 registration channel | Response to the HAAR team's 2026-08-21 alert that SAR-prod's registered template doesn't match ACP prod's deployed template. **First-draft framing on 2026-08-21 was superseded on 2026-08-24 after re-reading the Thursday 2026-08-20 thread**, which showed Dave Llewellyn had already registered both preprod AND prod on Thursday afternoon (16:06 BST for prod, via Deborah's in-thread ask). Corrected draft explains the alert is a SHA-pointer housekeeping mismatch on byte-identical mustache (`99264496` vs `f84f41b2` — zero-diff on the template file), offers Dave a choice of (a) bumping the prod registration pointer or (b) HAAR ack'ing the alert, and pre-empts the identical situation post-PR-14 (which does change bytes) by promising a single ping for both envs. |
| `round-2-sample.pdf` | ⏳ **pending — Raby (13c)** | Raby (or Deborah's dev) generates via Cameron's SAR dev-service (Option 1, full-chrome) against a preprod CRN | Branston-facing full-chrome PDF for round-2 content sign-off. See PR-13 doc §13c for the recipe + eyeball-check list. |
| `roxanne-dd-drift-nudge-draft.md` | 🚫 **not committed** | Raby (only if PR-6's original P.S. didn't already close her records) | Optional DD-drift nudge to Roxanne re rows 109 + 224. Check DELIVERY-LOG PR-6 outcome first; skip if closed. |

## Sequencing (updated 2026-08-18 pm — pre-13c steps added)

Order matters. Round-1 saw multi-day pipeline blocks; give Option 1 as much lead time as possible.

1. **Pre-13c step A — local eyeball (Raby, no external asks).** Regenerate the Option 2 chrome-less test-harness PDF locally on `main @ 99264496` to sanity-check content shape before engaging Cameron's team:

   ```zsh
   git fetch origin --prune && git checkout main && git pull --ff-only
   git --no-pager log --oneline -1   # expect 99264496 at top
   # Docker Desktop must be running for Testcontainers-backed regen
   open -a Docker   # macOS; skip if already up
   ./script/local-scripts/regenerate-sar-snapshots.sh
   open build/test-generated/sar-generated-report.pdf
   ```

   Eyeball against the sanity list below. If the local PDF looks structurally wrong (missing/extra sections, orphan headings), **stop** — diff `sar-api-response.json` / `sar-expected-render-result.html` against `origin/main` (should be byte-identical). Don't proceed to step B on a broken local render.

2. **Pre-13c step B — template re-registration (Raby, REQUIRED).** Send `cameron-template-registration-slack-draft.md` to `#haa-sar-functionality-change-request`. The mustache template changed materially between round-1 and round-2 (five `<h2>` blocks removed, `Organisation name` row added inline in each referral, `Prisoner number` rows removed) — if the SAR dev-service still holds the round-1 revision, the round-2 PDF will render the exact sections Deborah asked us to remove. Cameron's team re-registers at `99264496`. Also flushes any SARBT001-role rotation before you need it. Give it up to 24h; chase in-channel if silent.

3. **13c — PDF drop (Raby, after Cameron's team confirms).** Log in to the SAR dev-service with the test nDelius account (SARBT001 role), enter preprod CRN `A9648CH` (round-1's pick — swap for a comparable rich CRN if A9648CH is no longer viable), select **Accredited Programmes only**, generate + download, eyeball-check against the sanity list below, then `cp` into this directory, commit + push. Update the DELIVERY-LOG placeholder from `<PDF: pending>` to concrete file size + CRN + generation date + verdict.

4. **13d — comms send (Raby, planning-agent chat).** Fill in the `[CRN]` and `[date]` brackets in `osar-email-draft.md` and `deborah-slack-dm-draft.md` from 13c's outcome. Send email + DM. Planning agent updates DELIVERY-LOG with send timestamps after Raby confirms.

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

