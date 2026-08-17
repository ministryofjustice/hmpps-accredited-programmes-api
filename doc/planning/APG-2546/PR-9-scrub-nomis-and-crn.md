# PR-9 — Scrub `prisonerNumber` from surviving sections (CRN cascades from PR-8)

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/scrub-nomis-and-crn`
> • **Est.:** ½ dev day
> • **Sequencing:** must merge **after** PR-8 (**merged 2026-08-17 as `b7b05283`** — unblocked ✅) and be serialised with PR-10 and PR-11 (all three touch `SubjectAccessRequestService.kt` + `sar_template.mustache` and would merge-conflict if parallel).
> • **Status:** ready for execution — line refs re-verified against `origin/main @ b7b05283`

## Purpose

Round-2 ask #1 (Deborah, 2026-08-13) — *"Remove NOMIS IDs and CRNs as
they are in the header"*.

After PR-8 lands, the only sections still emitting `prisonerNumber` /
`prisonNumber` are:
- `referrals[].prisonerNumber` (line 6 of the template on `main`)
- `courseParticipation[].prisonNumber` (line 43)

`crn` is only on the deleted `pniResults[]` block, so it's already
gone by cascade from PR-8. Sanity-grep confirms.

## Scope (verified against `origin/main` @ `b7b05283`, 2026-08-17 — post-PR-8 merge)

**Product code — all inside `src/main/kotlin/.../service/SubjectAccessRequestService.kt`:**

- Remove `prisonerNumber: String` field from nested `data class SarReferral(...)` — **field at line 160** (data class decl at line 159)
- Remove `prisonNumber: String` field from nested `data class SarCourseParticipation(...)` — **field at line 199** (data class decl at line 198)
- Remove corresponding population statements in `toSarReferral(...)` mapper (definition at line 246) and `toSarParticipation(...)` mapper (definition at line 225) — search inside the mapper bodies for the `prisonerNumber = …` / `prisonNumber = …` assignments and delete

**Template — `src/main/resources/sar_template.mustache`:**

- Delete line 6 (`<tr><td>Prisoner number</td><td>{{ optionalValue prisonerNumber }}</td></tr>` inside referrals block) — **unchanged from 0cf89850, upstream of PR-8's deletions**
- Delete line 43 (`<tr><td>Prisoner number</td><td>{{ optionalValue prisonNumber }}</td></tr>` inside courseParticipation block) — **unchanged from 0cf89850, upstream of PR-8's deletions**

**Test code — pre-verified 2026-08-17 against `b7b05283`:**

`src/test/kotlin/.../integration/SubjectAccessRequestServiceIntegrationTest.kt`:

| Lines (`@ b7b05283`) | Change |
|---|---|
| 101 | Remove `assertThat(prisonerNumber).isEqualTo(prisonNumber)` inside `with(content.referrals[0]) { … }` block (at line 100) |
| 110 | Remove `assertThat(prisonNumber).isEqualTo(prisonNumber)` inside `with(content.courseParticipation[0]) { … }` block (at line 109) |

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

Header-ownership claim in Deborah's ask (NOMIS ID + CRN sourced by
the SAR wrapper header, not our payload) — **✅ CONFIRMED 2026-08-13
by SAR wrapper team (Cameron's team)**, covering both PR-8 (person +
NOMIS ID) and PR-9 (CRN + prisonerNumber). Verbatim:

> *"Yes — confirm we retrieve the information for the header from
> two APIs — one for NOMIS IDs and one for nDelius CRNs. We do not
> in any way retrieve that data from their product — so it's safe
> to remove it as the OSAR team requested."*

Full paper trail in DELIVERY-LOG round-2 timeline. No further
confirmation needed to proceed.

