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

## Scope (verified against `origin/main` @ `f8e04ab0`, 2026-08-17 — post PR-8 + PR-9 merges)

**Product code — all inside `src/main/kotlin/.../service/SubjectAccessRequestService.kt`:**

- Add `organisationName: String?` field to nested `data class SarReferral(...)` (decl at line 159)
- Populate it in the existing `toSarReferral(...)` mapper (defn at line 243) by looking up `organisationNamesByCode[it.offering?.organisationId]` — the `organisationNamesByCode: Map<String, String>` parameter is **already threaded into `toSarReferral(...)`** on `origin/main` (call site line 100, mapper signature `organisationNamesByCode` at line 246) because `SarOriginalReferral` uses it (see line 262 + toSarOriginalReferral defn), so no new plumbing is required. Mirror the pattern from line 272: `organisationName = offering?.organisationId?.let { organisationNamesByCode[it] }` — this is the exact wiring `SarOriginalReferral.toSarOriginalReferral` already uses; copy the same shape onto the parent `SarReferral` mapper body.
- Delete `organisations: List<SarOrganisation>` field from nested `data class Content(...)` (Content decl at line 151; `organisations` field at line 156)
- Delete `organisations = codesFromFiltered.mapNotNull { organisationsByCode[it]?.toSarOrganisation() }` line in `Content(...)` construction (line 104)
- Delete nested `data class SarOrganisation(...)` (line 291) — **grep-verified 2026-08-17**: only used inside this service file (assignment at line 104, decl at 291, mapper at 297). Safe to delete.
- Delete `.toSarOrganisation()` extension mapper (line 297)

**Template — `src/main/resources/sar_template.mustache`:**

- Add `<tr><td>Organisation name</td><td>{{ optionalValue organisationName }}</td></tr>` in the referrals `<table>` block. Position sensibly (near the top, above the POM surnames). Mirror the shape at line 23 which already renders `organisationName` inside the `originalReferral` sub-block.
- Delete top-level `<h2>Organisation</h2>` block (lines 78-88 — starts at `<h2>Organisation>` on 78, closes on 88 after two mustache `{{/organisations}}` closes)

**Repositories — no changes**

`organisationRepository.findAllByCodeIn(...)` **stays** — still needed for `SarOriginalReferral.organisationName` resolution. All the associated resolver variables (`codesFromFiltered`, `allOrgCodes` at line 87, `organisationsByCode` at line 91, `organisationNamesByCode` at line 96) stay for the same reason.

## No backend query change needed — verified 2026-08-17

The organisation-lookup plumbing is **already in place** because `SarOriginalReferral.organisationName` needs it. On `origin/main @ f8e04ab0`:

```kotlin
// Lines 87-96 in SubjectAccessRequestService.kt (verified)
val allOrgCodes = buildSet {
  addAll(codesFromFiltered)
  originalsById.values.forEach { it.offering?.organisationId?.let(::add) }
}
val organisationsByCode: Map<String, OrganisationEntity> = if (allOrgCodes.isEmpty()) {
  emptyMap()
} else {
  organisationRepository.findAllByCodeIn(allOrgCodes).associateBy { it.code }
}
val organisationNamesByCode: Map<String, String> = organisationsByCode.mapValues { it.value.name }

// Line 100:
referrals = filteredReferrals.toSarReferral(staffSurnames, originalsById, organisationNamesByCode),
```

So the parent `SarReferral` mapper **already has `organisationNamesByCode` in scope**. We just wire the lookup in and add the field to the DTO — no `getSarReferrals` JPQL change, no new query, no schema check. The "JPQL JOIN vs post-fetch" design question from an earlier version of this doc is moot: post-fetch is already the implementation and it's correct.

## Test code — pre-verified 2026-08-17 against `f8e04ab0`

`src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt`:

| Lines (`@ f8e04ab0`) | Change |
|---|---|
| 181+ | Leave alone — `organisationRepository.findAllByCodeIn(...)` mock (line 181) still needed (SarOriginalReferral resolution) |
| 244 | Remove `assertThat(organisations).hasSize(1)` |
| 245-246 | Remove `val organisation = organisations[0]` + `assertThat` lines (code, name) — spot-check exact span before deletion |
| **new** | Add an assertion on `referral.organisationName` inside the existing `with(result!!.content as SubjectAccessRequestService.Content) { … val referral = referrals[0] … }` block. Pattern to mirror: the `SarOriginalReferral.organisationName` assertion at **line 211** (was line 258 pre-PR-8/9). |

`src/test/kotlin/.../integration/SubjectAccessRequestServiceIntegrationTest.kt`:

- No mandatory test-code change (test doesn't reference `content.organisations`; grep-verified).
- **Recommended add**: an `assertThat(organisationName).isEqualTo("HMP Moorland")` inside the existing `with(content.referrals[0]) { … }` block (currently at line 100+ post-PR-9; spot-check span), to lock the field end-to-end in the integration test too.

`src/test/kotlin/.../integration/SarContractIntegrationTest.kt`:

- Fixture already seeds an organisation via `persistenceHelper.createOrganisation(...)` at **line 30** (was line 104 pre-PR-8/9; the fixture setup consolidated as PR-8 removed a lot of PNI/person/oasys seed rows above it) — no fixture change needed.
- Snapshot goldens regenerated: adds `organisationName` field to each `referrals[*]` object in JSON + one `<tr><td>Organisation name></td>` row per referral in HTML; removes top-level `organisations[]` array in JSON + `<h2>Organisation></h2>` block in HTML.

## Verification checklist

- [ ] `grep -rn 'organisations\b\|SarOrganisation\|toSarOrganisation' src/main` — expect zero hits post-change
- [ ] `grep -rn '\.organisations\b\|SarOrganisation' src/test` — expect only fixture-seed hits; no top-level mock-setup or assertion hits
- [ ] `./gradlew ktlintCheck test` clean
- [ ] Snapshot regen: **added** one row per referral in JSON + HTML; **removed** the whole `<h2>Organisation</h2>` block and top-level `organisations` array in JSON
- [ ] ~~`entity-schema.json` reflects: added `SarReferral.organisationName` field, removed `SarOrganisation` class~~ **⚠️ CORRECTED 2026-08-17 by PR-10 nine-lens self-review** — `entity-schema.json` tracks only JPA entities, not SAR DTOs. PR-10 touches no JPA entity (only nested DTOs inside `SubjectAccessRequestService`), so `entity-schema.json` should be **byte-identical**. Actual verification: `entity-schema.json` unchanged. Correct expectation: **zero diff** on this file.
- [ ] UUID-leak grep still 0
- [ ] ~~Confirm the two referrals in the fixture render different organisation names so the field is demonstrably per-referral (not global)~~ **⚠️ SUPERSEDED 2026-08-17 by PR-10 nine-lens self-review** — fixture currently seeds only one offering (`MDI → HMP Moorland`), so both referrals render identical `organisationName`. Per-referral wiring is verified in the mapper + in the unit test (single-row assertion). Fixture-level demonstration of variance requires a second offering seed (e.g. `BXI`) wired into one of the referrals — **deferred to PR-12 hygiene** per executing agent's recommendation and DELIVERY-LOG 2026-08-17 (late) entry. Not a PR-10 blocker.

## Notes for the agent

- Do not touch `originalReferralId` or `SarOriginalReferral` in this PR — PR-7 stripped the UUID and the sub-block already renders `organisationName`. It's the parent-referral wiring that needs the same treatment.

