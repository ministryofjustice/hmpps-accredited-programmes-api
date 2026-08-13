# PR-9 — Scrub `prisonerNumber` from surviving sections (CRN cascades from PR-8)

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/scrub-nomis-and-crn`
> • **Est.:** ½ dev day
> • **Sequencing:** must merge **after** PR-8 (not because of a code dep — PR-9 touches different fields — but because PR-9 and PR-8 both edit `SubjectAccessRequestService.kt` + `sar_template.mustache` and would merge-conflict if parallel). PR-9 must **also** be serialised with PR-10 and PR-11 for the same reason.
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

## Scope (verified against `origin/main` @ `0cf89850`, 2026-08-13 pm)

**Product code — all inside `src/main/kotlin/.../service/SubjectAccessRequestService.kt`:**

- Remove `prisonerNumber: String` field from nested `data class SarReferral(...)` (line 175 on main)
- Remove `prisonNumber: String` field from nested `data class SarCourseParticipation(...)` (line 214)
- Remove corresponding population statements in `toSarReferral(...)` mapper (line 296+) and `toSarParticipation(...)` mapper (line 275+)

**Template — `src/main/resources/sar_template.mustache`:**

- Delete line 6 (`<tr><td>Prisoner number</td><td>{{ optionalValue prisonerNumber }}</td></tr>` inside referrals block)
- Delete line 43 (`<tr><td>Prisoner number</td><td>{{ optionalValue prisonNumber }}</td></tr>` inside courseParticipation block)

**Test code — pre-verified 2026-08-13:**

`src/test/kotlin/.../integration/SubjectAccessRequestServiceIntegrationTest.kt`:

| Lines | Change |
|---|---|
| 124 | Remove `assertThat(prisonerNumber).isEqualTo(prisonNumber)` inside `with(content.referrals[0]) { … }` block |
| 133 | Remove `assertThat(prisonNumber).isEqualTo(prisonNumber)` inside `with(content.courseParticipation[0]) { … }` block |

No other test files reference `.prisonerNumber` / `.prisonNumber` on the removed DTO fields (grep-verified against `SubjectAccessRequestServiceTest.kt` and `SarContractIntegrationTest.kt`; the latter uses snapshot goldens rather than field-level assertions).

**Snapshot goldens**: regenerated. Expected diff: `prisonerNumber` field removed from every `referrals[*]` object in JSON; `prisonNumber` field removed from every `courseParticipation[*]` object. Two template rows removed from HTML per referral / per courseParticipation.

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

