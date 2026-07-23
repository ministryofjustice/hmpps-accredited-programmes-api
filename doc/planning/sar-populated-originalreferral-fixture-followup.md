# SAR contract fixture — populated `originalReferral` follow-up

> **Status:** planning • **Depends on:** APG-2493 (merged) • **Ticket:** TBD (rename this file to `APG-XXXX-...` once triaged)

## Context

APG-2493 added a nested `SarOriginalReferral` block on every
`SarReferral` in the SAR custody payload, with a `{{#originalReferral}}`
Mustache section in `src/main/resources/sar_template.mustache`.

The `SarContractIntegrationTest` fixture at
`src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/integration/SarContractIntegrationTest.kt`
currently seeds a single referral with `originalReferralId = null`.
Consequently:

- `src/test/resources/sar/sar-api-response.json` snapshot only exercises
  `"originalReferral":null` — the populated 9-field shape is never
  compared byte-for-byte against a fixture.
- `src/test/resources/sar/sar-expected-render-result.html` snapshot
  never contains an `<h4>Original referral</h4>` block — the new
  Mustache template branch is never rendered against a fixture.

A regression that (say) deletes `{{ optionalValue courseName }}` from
the template block would pass the SAR contract test, because the
block never fires.

Unit-test coverage in `SubjectAccessRequestServiceTest` does exercise
the populated DTO branch with per-field assertions, so the risk is
confined to the render layer — but the SAR contract is the
integration guard OSAR (via Naseem) is signed off against, so a real
snapshot for the populated shape is worth having.

## Task

Extend the SAR contract fixture with a **second referral** whose
`originalReferralId` points at a **third seeded referral** (the
"original"), then regenerate the JSON and HTML snapshots.

### Files to touch

1. **`SarContractIntegrationTest.setupTestData()`** — add two more
   `persistenceHelper.createReferral(...)` calls:
   - `ORIGINAL_REFERRAL_ID` — the antecedent, seeded first (must exist
     before the referring referral's `originalReferralId` FK resolves).
     Give it distinct-from-parent values so the fixture diff is
     unambiguous:
     - `status = "WITHDRAWN"`
     - `submittedOn = 2023-05-10T09:30:00` (before parent)
     - `additionalInformation = "Superseded original"`
     - `referrerOverrideReason = "Original override"`
     - `hasLdc = false`
     - referrer username same as the existing seed (`TEST_USER`) so
       we don't have to seed a second staff row — the surname will
       resolve to `"Doe"`.
   - `SUPERSEDED_BY_REFERRAL_ID` — the referring referral, with
     `originalReferralId = ORIGINAL_REFERRAL_ID` and otherwise
     mirroring the existing fixture referral so we get one referral
     with populated + one with null `originalReferralId`.

2. **UUID constants** — add to the `companion object`:
   ```kotlin
   val ORIGINAL_REFERRAL_ID: UUID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")
   val SUPERSEDED_BY_REFERRAL_ID: UUID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
   ```

3. **Regenerate the snapshots** (from repo root):
   ```bash
   SAR_GENERATE_ACTUAL=true ./gradlew test --tests '*.SarContractIntegrationTest' -q
   ```
   This will rewrite `sar-api-response.json` and
   `sar-expected-render-result.html` in place.

4. **Diff the regenerated fixtures** carefully. Expected changes only:
   - `sar-api-response.json`:
     - two new entries in the `referrals` array (the original + the
       referring one), the referring one carrying a populated
       `"originalReferral":{...}` block with all 9 fields.
     - possible re-order of `referrals` entries — verify the order
       matches `ReferralRepository.getSarReferrals` sort semantics.
     - **no changes** to any other top-level key (courseParticipation,
       auditRecords, courses, pniResults, person, staff, organisations,
       etc.) — if anything else changes, stop and investigate.
   - `sar-expected-render-result.html`:
     - new `<h3>Referral 2</h3>` and `<h3>Referral 3</h3>` blocks (or
       whatever the ordering shakes out to), one containing the
       nested `<h4>Original referral</h4>` table.
     - **no changes** to any other section.

## Non-changes

- No production code changes.
- No new mustache template edits — the block already exists from
  APG-2493; this ticket only makes it renderable against a real
  fixture referral.
- No new unit-test coverage — the DTO shape is already covered by
  `SubjectAccessRequestServiceTest`; this ticket is purely about
  render-layer snapshot coverage.

## Rough size

- ~30 lines in `SarContractIntegrationTest.setupTestData` (two new
  referral seeds + companion-object constants)
- ~15 lines added to `sar-api-response.json` (populated block +
  second null-original entry)
- ~15 lines added to `sar-expected-render-result.html` (nested table)
- 1 PR, 1 commit, ~30 minutes end-to-end

## Verification command

```bash
./gradlew compileKotlin compileTestKotlin \
          ktlintMainSourceSetCheck ktlintTestSourceSetCheck \
          test --tests '*.SarContractIntegrationTest' \
               --tests '*.SubjectAccessRequestService*' -q
```

## Handoff notes for the picking-up agent

- Branch off `origin/main`, not this one. This branch only carries
  the doc.
- APG-2493 must be on `main` before this ticket makes sense — confirm
  by checking that
  `src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt`
  contains a `SarOriginalReferral` data class.
- Signed commits required (repo policy). See the APG-2493 planning
  note for the pinentry-in-agent-terminal workaround.
- Do NOT hand-edit the JSON/HTML snapshots — always regenerate via
  `SAR_GENERATE_ACTUAL=true` and diff. Hand-edits drift from what
  the SAR framework actually renders.
- When diffing, confirm with OSAR (Naseem) if the reviewer wants any
  specific field values swapped for something more representative of
  a real referral chain — the seed above uses placeholder-ish values.

