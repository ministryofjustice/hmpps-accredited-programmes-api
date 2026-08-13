# PR-9 — Scrub `prisonerNumber` from surviving sections

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/scrub-nomis-and-crn`
> • **Est.:** ½ dev day • **Depends on:** PR-8 merged (three sections gone; simplifies grep surface)
> • **Status:** skeleton — expand before execution

## Purpose

Round-2 ask #1 (Deborah, 2026-08-13) — *"Remove NOMIS IDs and CRNs as
they are in the header"*.

After PR-8 lands, the only sections still emitting `prisonerNumber` /
`prisonNumber` are:
- `referrals[].prisonerNumber` (line 6 of the template on `main`)
- `courseParticipation[].prisonNumber` (line 43)

`crn` is only on the deleted `pniResults[]` block, so it's already
gone by cascade from PR-8. Sanity-grep confirms.

## Scope

- Remove `prisonerNumber` field from `SarReferral` DTO (grep exact class name)
- Remove `prisonNumber` field from `SarCourseParticipation` DTO
- Remove corresponding template rows (2 rows total)
- Remove population statements in mappers
- Regenerate snapshots

## Not in scope

- The nested `originalReferral{…}` sub-block does **not** carry
  `prisonerNumber` (PR-7 verified the shape). Sanity-check post-regen.
- `person{}`, `pniResults[]`, `oasysPniResults[]` — all gone from PR-8.

## Verification checklist skeleton

- [ ] `grep -rn 'prisonerNumber\|prisonNumber' src/main | grep -v test` — expect zero SAR-DTO hits post-change
- [ ] `./gradlew ktlintCheck test` clean
- [ ] Snapshot regen produces exactly a 2-row deletion in each of `sar-api-response.json` (per referral / per courseParticipation) and `sar-expected-render-result.html`
- [ ] UUID-leak grep still 0

## Notes for the agent

The header-owner claim in Deborah's ask needs a one-line Slack
confirmation from Cameron / HAA if PR-8's agent hasn't already done
it. If confirmed there, mark it here and proceed.

