# APG-2493 — Enrich `originalReferralId` on SAR with an `originalReferral` block

> **Status:** planning • **Depends on:** APG-2492 (all three PRs merged and deployed) • **Sign-off:** confirmed by OSAR via Naseem's email 2026-07-2x — Ops asked for the *entire* candidate table to be surfaced.

## Context

Each `SarReferral` currently exposes `originalReferralId: UUID?` — a
raw UUID that points at the referral this record supersedes (typically
a WITHDRAWN referral that was re-submitted to a different pathway).
A subject reading their SAR cannot make any sense of a bare UUID.

OSAR reviewed a candidate table of enrichment fields (see email chain
2026-07-2x) and confirmed **all 8 fields should be surfaced**.

## Confirmed spec — nested `originalReferral` block

Add a sibling field on `SarReferral`:

```kotlin
val originalReferral: SarOriginalReferral?
```

`null` when `originalReferralId` is `null` **or** when the referenced
referral cannot be loaded (defensive — should never happen but avoids
throwing if data drifts).

Retain the existing `originalReferralId: UUID?` alongside the nested
block for back-compat and debugging.

### `SarOriginalReferral` DTO

| # | Field | Type | Source | Example |
|---|---|---|---|---|
| 1 | `id` | `UUID` | `originalReferral.id` | `abc12345-...` |
| 2 | `courseName` | `String?` | `originalReferral.offering.course.name` | `Building Choices` |
| 3 | `organisationName` | `String?` | via `originalReferral.offering.organisationId` → `organisation.name` | `Belmarsh (HMP)` |
| 4 | `submittedOn` | `LocalDateTime?` | `originalReferral.submittedOn` | `2024-06-10T09:16:16` |
| 5 | `statusCode` | `String?` | `originalReferral.status` | `WITHDRAWN` |
| 6 | `referrerSurname` | `String?` | via `originalReferral.referrer.username` → **`StaffLookupService.resolveSurnamesByUsername` (APG-2492 batch resolver)** | `Doe` |
| 7 | `referrerOverrideReason` | `String?` | `originalReferral.referrerOverrideReason` | free text |
| 8 | `hasLdc` | `Boolean?` | `originalReferral.hasLdc` | `true` / `false` / `null` |
| 9 | `additionalInformation` | `String?` | `originalReferral.additionalInformation` | free text |

## Query strategy — zero new N+1s

The current SAR generation (after APG-2492 batching) issues:

- 2 queries for staff surname resolution (username + staffId batches)
- 1 query per organisation code (individual `findOrganisationEntityByCode`
  calls) — mild N per unique code

This ticket adds **exactly two more queries per SAR** and, opportunistically,
collapses the existing per-organisation lookups into one batch:

1. **Batch-load original referrals** — collect all non-null
   `originalReferralId`s from the current referral set, then
   `referralRepository.findAllById(originalIds)` (this method is
   inherited from `JpaRepository` — no new repo method needed).
2. **Batch-load organisations** — collect the union of organisation
   codes from `filteredReferrals` **and** the just-loaded originals,
   then a new `OrganisationRepository.findAllByCodeIn(codes)` call
   (new method — see below).
3. **Feed original-referral referrer usernames into the existing
   `resolveStaffSurnames` collection.** Zero extra queries — they
   piggy-back on the existing username batch.

**Net cost:** +2 queries regardless of how many originals or unique
organisations exist.

## Touch-points

### Production code

| File | Change |
|---|---|
| `src/main/kotlin/.../service/SubjectAccessRequestService.kt` | 1. Add `data class SarOriginalReferral(...)` (9 fields, see spec).<br>2. Add `originalReferral: SarOriginalReferral?` field to `SarReferral`.<br>3. In `getPrisonContentFor`:<br> &nbsp;&nbsp;&nbsp;&nbsp;a. After loading `filteredReferrals`, collect `originalIds = filteredReferrals.mapNotNull { it.originalReferralId }.toSet()`.<br> &nbsp;&nbsp;&nbsp;&nbsp;b. Call `referralRepository.findAllById(originalIds).associateBy { it.id!! }` → `Map<UUID, ReferralEntity>`.<br> &nbsp;&nbsp;&nbsp;&nbsp;c. Build the union of organisation codes across `filteredReferrals` **and** the originals; call the new batch org lookup → `Map<String, String>` (code → name).<br> &nbsp;&nbsp;&nbsp;&nbsp;d. Extend `resolveStaffSurnames` to also add every `original.referrer.username` into the username set.<br> &nbsp;&nbsp;&nbsp;&nbsp;e. Pass the `originalsMap` and `orgNamesMap` into `toSarReferral` so it can build `SarOriginalReferral` per row.<br>4. Update the existing `Content.organisations` construction to use the batch org map instead of the per-code `findOrganisationEntityByCode` calls (small refactor, wins both perf and consistency). |
| `src/main/kotlin/.../domain/repository/OrganisationRepository.kt` | Add `fun findAllByCodeIn(codes: Collection<String>): List<OrganisationEntity>` (Spring Data derived query — no `@Query` needed). |
| `src/main/resources/sar_template.mustache` | Add a nested "Original referral" table under each referral: renders all 9 fields, gated by `{{#originalReferral}} ... {{/originalReferral}}` so it silently disappears when the block is null. Use `{{ optionalValue ... }}` for every field so the "No Data Held" convention is preserved. |

### Fixtures & tests

| File | Change |
|---|---|
| `src/test/resources/sar/sar-api-response.json` | Add `"originalReferral": null` to the existing single referral (it has no `originalReferralId` in the fixture); OR add a second referral fixture with a populated original for stronger coverage. |
| `src/test/resources/sar/sar-expected-render-result.html` | Update the rendered HTML to include the new (empty or populated) original-referral section for the fixture case above. |
| `src/test/kotlin/.../integration/SarContractIntegrationTest.kt` | Fixture diff is picked up automatically; no code change. |
| `src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt` | Extend the `should return filtered and mapped prison content` fixture with a second referral whose `originalReferralId` points at a seeded original; mock `referralRepository.findAllById(...)` and the new `organisationRepository.findAllByCodeIn(...)` to return the enriching data; assert every field of the resulting `originalReferral` block. |
| `src/test/kotlin/.../domain/repository/OrganisationRepositoryIntegrationTest.kt` *(new)* | Two tests for `findAllByCodeIn`: matched + unmatched. Small & fast. |

### Non-changes

- **No DB migration.** All required data already lives on the existing
  `referral`, `offering`, `course`, `organisation`, `staff` tables.
- **No `StaffLookupService` changes.** The batch resolver from
  APG-2492 already handles the extra usernames.
- **No `ReferralRepository` new methods.** `JpaRepository.findAllById`
  is inherited.

## Semantic considerations

- **When the original referral is missing.** Rare (referential
  integrity should prevent it) but defensive: if
  `originalsMap[originalReferralId] == null`, emit
  `originalReferral = null` and log a WARN with the missing UUID.
  This mirrors the "duplicate staff row" WARN convention introduced
  in APG-2492.
- **When the original's referrer surname can't be resolved.** Use the
  same `null` fallback the batch resolver returns — the mustache
  template already renders "No Data Held" for null via
  `optionalValue`.
- **When the original's organisation code isn't in the org table.**
  `orgNamesMap[code]` returns `null`; DTO field is `null`; template
  renders "No Data Held". No throw.
- **`hasLdc` is `Boolean?`.** Render as `Yes` / `No` / `No Data Held`
  in the mustache — check whether the codebase already has a helper
  or if we need to expose the raw boolean and let mustache stringify
  it. Existing template uses `{{ optionalValue hasLdc }}` on the
  parent referral, so re-use that.
- **Idempotence.** Two originals pointing at the same organisation
  are looked up once (union set of codes → single batch call).

## Test plan

1. **Unit** — `SubjectAccessRequestServiceTest`:
   - Extend fixture with a referral chain: `A → originalReferralId = B`, where `B` has its own `referrer.username`, `offering.organisationId`, etc.
   - Mock `referralRepository.findAllById({B.id})` returns `[B]`.
   - Mock `organisationRepository.findAllByCodeIn(...)` returns the combined orgs.
   - Assert every 9 fields of `referrals[0].originalReferral` match `B`.
   - Assert a referral with `originalReferralId = null` maps to `originalReferral = null`.
   - Assert a referral whose `originalReferralId` misses in the batch map maps to `originalReferral = null` and produces a WARN log.
2. **Repository integration** — `OrganisationRepositoryIntegrationTest` (new): matched + unmatched batch behaviour.
3. **Contract** — `SarContractIntegrationTest`: fixture JSON diff + HTML render diff pick up the new nested section.
4. **Manual** — hit the SAR endpoint for a subject known to have superseded referrals; visually confirm the "Original referral" block renders with real course / organisation / surname.

## Sequencing rationale

Do this **after** APG-2495 (post-deploy retest of live-like SAR):

1. `APG-2492/*` — merge & deploy the staff-surname pipeline.
2. `APG-2495` — verify in dev/preprod with realistic data that the
   pipeline is stable and the query counts look right.
3. `APG-2493` (this ticket) — layer the enrichment on top of a proven
   surname-resolution pipeline.
4. `APG-2510` — remove `staff.username` from the staff section (the
   final SAR field cleanup).

This ordering means `SarOriginalReferral.referrerSurname` is the only
field consuming freshly-hardened infrastructure, and if anything goes
wrong we can rollback APG-2493 in isolation.

## Suggested commit sequence

Two commits recommended for clean review:

**Commit 1** — `APG-2493: batch-load organisations by code`
- New `OrganisationRepository.findAllByCodeIn`.
- Refactor `Content.organisations` in `SubjectAccessRequestService` to
  use the batch call.
- New `OrganisationRepositoryIntegrationTest`.
- **No behaviour change** for the SAR consumer — same organisation
  content, one query instead of N.

**Commit 2** — `APG-2493: enrich originalReferralId with SarOriginalReferral`
- Add `SarOriginalReferral` DTO + `originalReferral` field on
  `SarReferral`.
- Batch-load originals and thread `originalsMap` + `orgNamesMap`
  through mappers.
- Extend `resolveStaffSurnames` to include originals' referrer
  usernames.
- Mustache template + fixture updates.
- Unit test extension.

## Rough size

- ~20 lines in `OrganisationRepository` + tests
- ~50 lines in `SubjectAccessRequestService` (new DTO, mapper update,
  content wiring)
- ~15 lines in `sar_template.mustache` (new nested table)
- ~30 lines fixture / test updates
- 1 PR, 2 commits, ~2 hours end-to-end

## Reply-to-Naseem draft

> Hi Naseem — thanks for confirming. I'll surface all eight fields as a nested `originalReferral` block on each SarReferral in APG-2493, alongside the existing `originalReferralId`. This will land after the staff surname-lookup work (APG-2492) is fully in production and the live-like SAR regression check (APG-2495) has passed. I'll ping you and OSAR when it's up for review. Kind regards, Raby.

