# PR-13 — Round-2 docs handover + fresh sample PDF for Branston

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/round-2-docs-handover`
> • **Est.:** ½ dev day • **Depends on:** PR-8 + PR-9 + PR-10 + PR-11 + PR-12 merged
> • **Status:** skeleton — expand before execution

## Purpose

Close-out PR for round-2, analogous to PR-6 (round-1 handover) for
round-1.

## Scope

- Close `DELIVERY-LOG.md` round-2 section with final SHAs + timings for PRs 8–11
- Record DD row 139 override + the two dead-query outcomes from
  PR #1115 in the closeout
- Generate a fresh sample PDF from a preprod CRN (candidate list at
  hand — A9648CH is still the recommended pick unless something in
  round 2 changes the calculus)
- Draft email + Slack message to Branston with the new PDF attached
- Draft short update note for Deborah closing the loop on all five
  round-2 asks
- If desired, update the top-level `README.md` to note round-2 as
  delivered

## Docs to touch

- `doc/planning/APG-2546/DELIVERY-LOG.md` — closeout timeline entry
- `doc/planning/APG-2546/ROUND-2-PLAN.md` — mark PRs 8–11 delivered
- `README.md` (top-level) — optional badge / status update
- `doc/planning/APG-2546/PR-12-round-2-docs-and-handover.md` (this
  file) — mark itself delivered

## Handover artefacts to produce

- Fresh sample PDF: `build/test-generated/sar-generated-report.pdf`
  from the SAR dev-service using the recommended preprod CRN
- Copy of the PDF committed to a discoverable planning location for
  the paper trail (`doc/planning/APG-2546/handover/round-2-sample.pdf`?)
- Email draft with attachment for Branston
- Slack DM draft for Deborah

## Not in scope

- No product code changes in this PR. Docs + generated artefacts
  only.

## Verification checklist skeleton

- [ ] `./gradlew ktlintCheck test` clean (regression guard, no code changed)
- [ ] Snapshot goldens unchanged from post-PR-11 state
- [ ] Sample PDF renders sensibly — fewer sections, no orphaned "No Data Held" placeholders
- [ ] All five of Deborah's asks demonstrably reflected in the sample PDF
- [ ] Email + Slack drafts reviewed for tone, sign-off chain (cc Deborah SDM, cc Sharon per usual escalation, cc Roxanne for DD row 139 note)

