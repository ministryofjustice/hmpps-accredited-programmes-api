# PR-10 — Add `organisationName` to referral; delete top-level `organisations[]`

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/organisation-into-referral`
> • **Est.:** 1 dev day • **Depends on:** PR-8 merged
> • **Status:** skeleton — expand before execution

## Purpose

Round-2 ask #4 (Deborah, 2026-08-13) — *"Add organisation field to the
referral rather than list separately so it is in context"*.

The nested `originalReferral{…}` sub-block already carries
`organisationName` (line 24 of template, verified in the golden). We
apply the same shape to the parent referral, and delete the redundant
top-level `<h2>Organisation</h2>` block (lines 136–146).

## Scope

- Add `organisationName: String?` to `SarReferral` DTO
- Add template row `<tr><td>Organisation</td><td>{{ optionalValue organisationName }}</td></tr>`
  in the referrals `<table>` block — position sensibly (near the top,
  above the POM surnames)
- Backend: `getSarReferrals` in `ReferralRepository` — add a JOIN to
  the `organisation` table on `referral.organisation_id` and project
  `organisation.name` into the returned row. Referral entity already
  carries `organisation_id`, so this is not a new lookup.
- Delete top-level `organisations[]` field from `SarResponse`
- Delete top-level `<h2>Organisation</h2>` template block
- Delete `SarOrganisation` DTO (verify no other callers first)
- Delete `OrganisationRepository.findByPrisonNumber` or equivalent
  SAR-specific query (orphan-audit like PR-8)
- Update fixture: single-line change to seed the organisation FK
  on the primary referral in `setupTestData()`
- Regenerate snapshots

## Backend design decision to confirm

Two shapes possible for the JOIN:

**(A) Extend the existing `getSarReferrals` `@Query`** — add
`LEFT JOIN organisation o ON o.organisation_id = r.organisation_id`
and project `o.name`. Simplest; keeps one query.

**(B) Post-fetch enrichment** — fetch referrals unchanged, then
resolve organisation names in the service layer via
`OrganisationRepository.findAllByIds(...)`.

Recommend **(A)** — one round-trip, sortable by
`organisation.name` if we ever need it, no N+1 risk. Verify
schema (`referral.organisation_id` FK exists + is nullable-friendly).

## Verification checklist skeleton

- [ ] `grep -rn 'organisations\b\|SarOrganisation' src/main | grep -v test` — expect zero SAR-DTO hits post-change
- [ ] `./gradlew ktlintCheck test` clean
- [ ] Snapshot regen: **added** one row per referral in JSON + HTML; **removed** the whole `<h2>Organisation</h2>` block
- [ ] `entity-schema.json` reflects the new `SarReferral.organisationName` field
- [ ] UUID-leak grep still 0
- [ ] Confirm the two referrals in the fixture render different organisation names (`HMP Moorland` for the original, something else for the primary — worth exercising the field is per-referral, not global)

## Notes for the agent

Verify with a full grep before deleting `SarOrganisation` — some
non-SAR call site might also reference it, especially historic seeder
code.

