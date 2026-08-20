# Slack draft — Cameron's SAR product team (`#haa-sar-functionality-change-request`)

**Purpose:** register the round-2 mustache template change on the SAR dev-service **before** using the SAR PDF aggregator for the round-2 Branston review PDF. This is the documented pre-13c step.

**Why required (not optional):** the round-2 template (`sar_template.mustache`) has changed materially vs the round-1 revision Cameron's team registered in PR-6:

- **Five `<h2>` blocks removed** from the top-level template: `PNI results`, `Person`, `OASys PNI results`, `Organisations`, `Staff`.
- **`Organisation name` `<tr>` row added inline** inside each referral's summary-list.
- **`Prisoner number` `<tr>` row removed** from every referral row + every course-participation row.

Diff against the round-1 registered revision:

```zsh
git diff baee4510..99264496 -- src/main/resources/sar_template.mustache
```

If the SAR dev-service still holds the round-1-registered template, the round-2 review PDF will render the old sections (PNI, Person, etc.) and Branston will see the exact content Deborah's 2026-08-13 action list asked us to remove. Registering first eliminates that failure mode.

**Channel:** `#haa-sar-functionality-change-request`
**Ticket:** APG-2546

## Draft

```
Hi team — template change to register for APG-2546 round 2, please.

The Accredited Programmes SAR mustache template has changed on main
following the five round-2 PRs shipped 2026-08-17/18:

  - #1116 PR-8  — removed PNI results, OASys PNI results, Person <h2> sections
  - #1117 PR-9  — scrubbed Prisoner number rows from Referrals + Course participation
  - #1118 PR-10 — added Organisation name row inline on each referral;
                  removed top-level Organisations <h2> section
  - #1119 PR-11 — removed top-level Staff <h2> section
  - #1120 PR-12 — hygiene / fixture widening (no template shape change)

main is at 99264496 as of 2026-08-18 (PR #1120 merge).

Template on main (permalink at the round-2 tip):
https://github.com/ministryofjustice/hmpps-accredited-programmes-api/blob/99264496/src/main/resources/sar_template.mustache

Ask: please re-register the template on the SAR dev service at this
revision so the round-2 Branston review PDF renders the removed
sections as absent. Round 2 is a direct response to Deborah's
2026-08-13 review-meeting action list, so if the dev-service is
still serving the round-1 revision, the review PDF will show the
exact sections OSAR asked us to remove.

Diff for the record (just so you can eyeball scope of the change):

  git diff baee4510..99264496 -- src/main/resources/sar_template.mustache

Five <h2> block deletions + one row added + two rows removed. Pure
section thinning; no new field names on surviving sections.

Also — please confirm the test nDelius account we used for round-1's
Option 1 PDF run still has SARBT001 (HMPPS Subject Access Report
User). If it's been rotated, I'll need it re-added before I can
generate the round-2 PDF.

Deborah's SDM team pre-empted an Option 2 (chrome-less test-harness)
fallback for the case where the Option 1 pipeline stalls — I'll fall
back to that if needed, but the OSAR-preferred handover is the
full-chrome PDF from your dev-service, so I'd like Option 1 if you
can.

Aiming to have the PDF in front of Branston / OSAR this week —
please let me know if the pipeline needs any lead time.

Ticket: APG-2546. Happy to jump on a call if easier.

Thanks,
Raby
```

## Follow-up expectations

- **Re-registration confirmed at `99264496`** → proceed to 13c PDF generation. Note the registration timestamp in DELIVERY-LOG for the paper trail.
- **Cameron's team asks for a different revision** → we're between rounds so there isn't a subsequent SHA on this ticket; supply `99264496` as-is.
- **Dev is behind main / needs a redeploy** → wait; kick the CircleCI dashboard or ping platform. Do NOT generate the PDF against a stale dev environment.
- **SARBT001 was rotated on the test account** → parallel request via the usual identity-team route (round 1 saw this take longer than the template registration).
- **Silence after 24h** → chase in the same channel. Round 1 saw the pipeline stall for reasons outside the consumer team's control.

## Notes for the record

- Corrects the earlier tentative framing of this ping ("courtesy check, not a requirement"). Because the template *did* change substantively vs round-1, re-registration is required, not optional. Confirmed with Raby 2026-08-18 pm in the planning-agent chat.
- The Option 2 fallback (`build/test-generated/sar-generated-report.pdf` via `script/local-scripts/regenerate-sar-snapshots.sh` on `main @ 99264496`) is what Raby uses locally to eyeball round-2 content shape *before* sending this ping — smart pre-gate. If the local render is structurally wrong, we don't burn Cameron's team's time with a bogus re-registration ask.
- After Cameron's team confirms + we generate the round-2 PDF (13c) + send comms (13d), APG-2546 close-out condition is "feedback received from Branston", not "feedback with zero further asks". Round-3 change requests spin a fresh ticket per the OOS decision.
