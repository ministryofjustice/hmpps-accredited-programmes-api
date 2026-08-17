# PR-10 — Add `organisationName` to referral; delete top-level `organisations[]`

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/organisation-into-referral`
> • **Est.:** ½ dev day (revised down from 1 day after 2026-08-13 pm plan review — the backend plumbing already exists on `origin/main`; scope is much smaller than the original skeleton suggested)
> • **Sequencing:** must merge **after PR-8** (**merged 2026-08-17 as `b7b05283`** ✅) and **after PR-9** (must be serialised — merge-conflict avoidance on `SubjectAccessRequestService.kt` + `sar_template.mustache`).
> • **Status:** skeleton — expand before execution
> • **⚠️ Line-ref re-anchoring required before execution.** Line references below are anchored to `origin/main @ 0cf89850` (pre-PR-8). PR-8's merge deleted content from the middle of `SubjectAccessRequestService.kt` (fields at lines 167/168/169) and the middle of `sar_template.mustache` (three `<h2>` blocks between the surviving Referrals and Organisation blocks). Downstream line refs (e.g. `organisations` field at Content L171 → shifted; `SarOrganisation` decl at L384 → shifted; `<h2>Organisation>` at template L136 → shifted). PR-9 will also merge before this PR, causing a further ~2-line shift. **Re-verify every line ref in this doc against the current `origin/main` HEAD before opening the PR**, using the pattern the PR-9 doc followed (git show `<HEAD>:<file>` + grep). Same rule as R6: fresh checkout of `origin/main` first.

## Purpose

Round-2 ask #4 (Deborah, 2026-08-13) — *"Add organisation field to the
referral rather than list separately so it is in context"*.

The nested `originalReferral{…}` sub-block already carries
`organisationName` (line 24 of template, verified in the golden). We
apply the same shape to the parent referral, and delete the redundant
top-level `<h2>Organisation</h2>` block (lines 136–146).

## Scope (verified against `origin/main` @ `0cf89850`, 2026-08-13 pm)

**Product code — all inside `src/main/kotlin/.../service/SubjectAccessRequestService.kt`:**

- Add `organisationName: String?` field to nested `data class SarReferral(...)` (line 174 on main)
- Populate it in the existing `toSarReferral(...)` mapper by looking up `organisationNamesByCode[it.offering?.organisationId]` — the `organisationNamesByCode: Map<String, String>` parameter is **already threaded into `toSarReferral(...)`** on `origin/main` (line 109) because `SarOriginalReferral` uses it, so no new plumbing is required
- Delete `organisations: List<SarOrganisation>` field from nested `data class Content(...)` (line 171)
- Delete `organisations = codesFromFiltered.mapNotNull { organisationsByCode[it]?.toSarOrganisation() }` line in `Content(...)` construction (line 116)
- Delete nested `data class SarOrganisation(...)` (line 384) — **grep-verified 2026-08-13**: only used inside this service file (assignment at line 116, declaration at 384, mapper at 390). Safe to delete.
- Delete `.toSarOrganisation()` extension mapper (line 390)

**Template — `src/main/resources/sar_template.mustache`:**

- Add `<tr><td>Organisation</td><td>{{ optionalValue organisationName }}</td></tr>` in the referrals `<table>` block. Position sensibly (near the top, above the POM surnames).
- Delete top-level `<h2>Organisation</h2>` block (lines 136–146)

**Repositories — no changes**

`organisationRepository.findAllByCodeIn(...)` **stays** — still needed for `SarOriginalReferral.organisationName` resolution. All the associated resolver variables (`codesFromFiltered`, `allOrgCodes`, `organisationsByCode`, `organisationNamesByCode`) stay for the same reason.

## No backend query change needed — verified 2026-08-13

The organisation-lookup plumbing is **already in place** because `SarOriginalReferral.organisationName` needs it. On `origin/main`:

```kotlin
// Lines 96-107 in SubjectAccessRequestService.kt
val allOrgCodes = buildSet {
  addAll(codesFromFiltered)
  originalsById.values.forEach { it.offering?.organisationId?.let(::add) }
}
val organisationsByCode: Map<String, OrganisationEntity> =
  if (allOrgCodes.isEmpty()) emptyMap()
  else organisationRepository.findAllByCodeIn(allOrgCodes).associateBy { it.code }
val organisationNamesByCode: Map<String, String> = organisationsByCode.mapValues { it.value.name }

// Line 109:
referrals = filteredReferrals.toSarReferral(staffSurnames, originalsById, organisationNamesByCode),
```

So the parent `SarReferral` mapper **already has `organisationNamesByCode` in scope**. We just wire the lookup in and add the field to the DTO — no `getSarReferrals` JPQL change, no new query, no schema check. The "JPQL JOIN vs post-fetch" design question from an earlier version of this doc is moot: post-fetch is already the implementation and it's correct.

## Test code — pre-verified 2026-08-13

`src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt`:

| Lines | Change |
|---|---|
| 225–230 | Leave alone — `organisationRepository.findAllByCodeIn(...)` mock still needed (SarOriginalReferral resolution) |
| 302 | Remove `assertThat(organisations).hasSize(1)` |
| 303–305 | Remove `val organisation = organisations[0]` + two `assertThat` lines (code, name) |
| **new** | Add an assertion on `referral.organisationName` inside the existing `with(result!!.content as SubjectAccessRequestService.Content) { … val referral = referrals[0] … }` block. Pattern to mirror: the `SarOriginalReferral.organisationName` assertion at line 258. |

`src/test/kotlin/.../integration/SubjectAccessRequestServiceIntegrationTest.kt`:

- No mandatory test-code change (test doesn't reference `content.organisations`; grep-verified).
- **Recommended add**: an `assertThat(organisationName).isEqualTo("HMP Moorland")` inside the existing `with(content.referrals[0]) { … }` block (currently at lines 123–130), to lock the field end-to-end in the integration test too.

`src/test/kotlin/.../integration/SarContractIntegrationTest.kt`:

- Fixture already seeds an organisation via `persistenceHelper.createOrganisation(...)` at line 104 — no fixture change needed.
- Snapshot goldens regenerated: adds `organisationName` field to each `referrals[*]` object in JSON + one `<tr><td>Organisation></td>` row per referral in HTML; removes top-level `organisations[]` array in JSON + `<h2>Organisation></h2>` block in HTML.

## Verification checklist

- [ ] `grep -rn 'organisations\b\|SarOrganisation\|toSarOrganisation' src/main` — expect zero hits post-change
- [ ] `grep -rn '\.organisations\b\|SarOrganisation' src/test` — expect only fixture-seed hits; no top-level mock-setup or assertion hits
- [ ] `./gradlew ktlintCheck test` clean
- [ ] Snapshot regen: **added** one row per referral in JSON + HTML; **removed** the whole `<h2>Organisation</h2>` block and top-level `organisations` array in JSON
- [ ] `entity-schema.json` reflects: added `SarReferral.organisationName` field, removed `SarOrganisation` class
- [ ] UUID-leak grep still 0
- [ ] Confirm the two referrals in the fixture render different organisation names so the field is demonstrably per-referral (not global)

## Notes for the agent

- Do not touch `originalReferralId` or `SarOriginalReferral` in this PR — PR-7 stripped the UUID and the sub-block already renders `organisationName`. It's the parent-referral wiring that needs the same treatment.

