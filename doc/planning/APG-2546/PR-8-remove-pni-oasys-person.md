# PR-8 — Remove `pniResults`, `oasysPniResults` and `person` sections

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/remove-pni-oasys-person`
> • **Est.:** 1½ dev days • **Blocks:** PR-12 (round-2 handover)
> • **Depends on:** clean `main` after PR #1115 (`0cf89850`)

## Purpose

Round-2 asks #2 (Deborah, 2026-08-13) — *"Remove PNI data and this is
retrieved from ARNs via the Probation Hub request"* — plus ask #3
*"Remove Personal Data section"*.

Both the `pniResults[]` and `oasysPniResults[]` collections go
together under the "PNI data" umbrella per Deborah. The `person{}`
block goes with them because it's the same category of "sourced
elsewhere in the SAR bundle" (personal data is provided by the
prisoner-search / delius SAR feeds, not us).

Grouped into one PR because:

- Same coherent theme — three sections deleted for the same reason
  ("SAR wrapper aggregates it from a more authoritative source").
- The three deletions each produce a snapshot-golden diff of similar
  shape (whole `<h2>…</h2>` block removed) — one review pass reads
  cleanly across all three.
- Fixture cleanup + orphaned-repository-call cleanup naturally
  co-locate.

## Overrides recorded

**DD row 139** — Roxanne had `pni_result . pni_result_json`
explicitly kept in scope (SAR=Yes, In SAR API=Yes) with the note
*"these are in SAR report hence H should be Yes. Updated"*
(2026-07-10). Deborah's 2026-08-13 meeting supersedes this. See
[`ROUND-2-PLAN.md`](./ROUND-2-PLAN.md) §"DD spreadsheet override".

## Prerequisites for a fresh agent

Read in this order:

1. [`ROUND-2-PLAN.md`](./ROUND-2-PLAN.md) — round-2 overview + PR breakdown + PR #1115 impact matrix
2. [`DELIVERY-LOG.md`](./DELIVERY-LOG.md) — round-2 kickoff entry (2026-08-13) captures the meeting, the (a)/(b) clarification outcome, and the DD row 139 override
3. This file end to end
4. Aug-12 sample PDF (last artefact sent to Branston) at `build/test-generated/sar-generated-report.pdf` if still on disk, else regenerate via `script/local-scripts/regenerate-sar-snapshots.sh` — helpful for visualising what goes away

No external questions gate this PR. Deborah's ask is unambiguous;
DD row 139 is explicitly superseded.

## Current shape (post PR #1115)

The three sections on `main` (`sar_template.mustache` line refs):

```mustache
<h2>PNI results</h2>                (lines 79–99)
{{#pniResults}}
    <h3>PNI result {{@index+1}}</h3>
    Prisoner number, CRN, Programme pathway, Needs classification,
    Overall needs score, Risk classification, OASys assessment
    completed date, PNI assessment date, PNI valid, Basic skills
    score, PNI result JSON
{{/pniResults}}

<h2>Person</h2>                     (lines 101–121)
{{#person}}
    Prison number, Forename, Surname, Location, Gender,
    Conditional release date, Parole eligibility date, Tariff
    expiry date, Earliest release date, Earliest release date
    type, Sentence type, Indeterminate sentence, Non DTO release
    date type
{{/person}}

<h2>OASys PNI results</h2>          (lines 123–134)
{{#oasysPniResults}}
    <h3>OASys PNI result {{@index+1}}</h3>
    Prison number, Programme pathway
{{/oasysPniResults}}
```

Corresponding DTO fields (top-level SAR response):

```kotlin
data class SarResponse(
    // ...existing fields (referrals, courseParticipation, courses, staff, organisations)...
    val pniResults: List<SarPniResult>,           // ← this PR removes
    val person: SarPerson?,                       // ← this PR removes
    val oasysPniResults: List<SarOasysPniResult>, // ← this PR removes
)
```

(Exact class names to be confirmed on branch — grep
`SarResponse`, `SarPniResult`, `SarPerson`, `SarOasysPniResult`
before editing.)

## Files to change

**Product code**

| File | Change |
|---|---|
| `src/main/resources/sar_template.mustache` | Delete lines 79–99 (`<h2>PNI results>`), 101–121 (`<h2>Person>`), 123–134 (`<h2>OASys PNI results>`). Preserve whitespace between remaining blocks. |
| SAR response DTO (grep `data class SarResponse`) | Remove three fields: `pniResults`, `person`, `oasysPniResults` (or whatever their exact names are). |
| SAR service class (grep for `.pniResults =` / `.person =` / `.oasysPniResults =`) | Remove the population statements + the underlying repository / service calls. |
| Three now-unused DTO classes (`SarPniResult`, `SarPerson`, `SarOasysPniResult`) | Delete the whole classes. |
| `PniResultRepository.findAllByPrisonNumber(...)` | **Orphan-audit** — `grep -r findAllByPrisonNumber src/main` scoped to `PniResultRepository`. If no non-SAR caller, delete the query. Otherwise leave with ORDER BY intact. |
| `OasysPniResultEntityRepository.findAllByPrisonNumber(...)` | Same orphan-audit + treatment. |
| Any `SarPersonMapper` / equivalent | Delete if it becomes unused. |

**Migrations**

None. V145 stays (Flyway forward-only; the index it adds is on
`staff.last_name` and is used by another query PR-11 will decide about
separately).

**Test code**

| File | Change |
|---|---|
| `src/test/kotlin/.../SarContractIntegrationTest.kt` — `setupTestData()` | Remove `pniResult`, `oasysPniResult`, `person` fixture stanzas. Remove the corresponding companion `object` consts (`PNI_RESULT_ID`, `OASYS_PNI_RESULT_ID`, `PERSON_ID` — whichever are only used for these). Remove the `createPerson(...)` call (helper stays; other tests use it). |
| `src/test/kotlin/.../SarContractIntegrationTest.kt` — expectations | The snapshot goldens are the primary assertion — no explicit field-by-field expectations should need touching. Verify. |
| Snapshot goldens | Regenerated by `script/local-scripts/regenerate-sar-snapshots.sh` after the code changes. Expected diff: three whole sections deleted from both `sar-api-response.json` and `sar-expected-render-result.html`; `entity-schema.json` unchanged. |

## Non-obvious things

1. **`pniResults[]` empty in preprod/dev but not empty in the fixture.**
   PR #1115's fixture widening added a PNI-result row and an
   OASys-PNI-result row so the sections would render. Those seed rows
   go away with the section removal.
2. **`PersistenceHelper.createPerson` has a `LocalDate` bind fix from
   PR #1115** that we're rendering dead here. Leave the helper as-is
   — the fix is generically useful and the helper is called by other
   tests. Just delete the SAR-fixture call site.
3. **Header ownership check** — before deleting `person.forename` /
   `person.surname` verify with Cameron / HAA that the SAR wrapper
   header injects name from its own context (prisoner-search /
   delius), so redaction reviewers won't lose the subject name from
   the report. Deborah's ask implies this is the case ("in the
   header") but a one-line Slack confirmation avoids a nasty
   round-3 surprise.
4. **`SarOriginalReferral` on the parent referral survives.** PR-7
   stripped the UUID from it; PR-8 does NOT touch it. Sanity-check
   the goldens still contain the `originalReferral{…}` sub-object
   post-regen.

## Verification checklist

Run in order, tick each:

- [ ] `grep -rn 'pniResults\|oasysPniResults\|person' src/main | grep -v test` returns only expected remnants (e.g. Kotlin `Person` unrelated classes — inspect any hit)
- [ ] `grep -rn 'PniResult\|OasysPniResult\|SarPerson' src/main` returns zero SAR-DTO/service hits (product code cleanup complete)
- [ ] `./gradlew ktlintCheck` clean
- [ ] `./gradlew test` — expect **678 tests → same or fewer count** (dropped assertions, if any); zero failures
- [ ] Snapshot regen: `./script/local-scripts/regenerate-sar-snapshots.sh` — commit the resulting `sar-api-response.json` + `sar-expected-render-result.html` diffs
- [ ] Post-regen: `./gradlew test --tests '*SarContractIntegrationTest*'` clean without `SAR_GENERATE_ACTUAL`
- [ ] UUID-leak grep on both goldens returns **0 matches** (regression guard from APG-2546 round 1)
- [ ] Sample PDF page count noted for delivery-log (expect **fewer pages than round-1** — three whole sections gone)
- [ ] `entity-schema.json` — either unchanged, or if changed only via removed classes, diff makes sense
- [ ] `git status --short` before commit — no `.snyk` / xlsx staged (recurring paper-cut from PR #1115)

## Rollback

`git revert <sha>` restores three whole sections. No migration; no
API contract addition (only removals). Consumers that were consuming
the removed keys would break on revert — but Deborah's ask
explicitly authorises the removal so no consumer should be relying
on those keys post-merge.

## Description template

```markdown
## Remove PNI + OASys PNI + Person sections from SAR report

Round-2 asks from OSAR (Deborah, 2026-08-13, following Aug-12
sample-PDF review): PNI data is now sourced by SAR consumers via
the ARNs Probation Hub feed; personal data is aggregated from the
SAR wrapper header. Replicating either in the Accredited
Programmes SAR report is duplicative and confusing for redaction
reviewers.

### What changed

- Deleted `<h2>PNI results</h2>` block — DTO, template, service
  wiring, fixture setup, snapshot goldens.
- Deleted `<h2>OASys PNI results</h2>` block — same treatment.
- Deleted `<h2>Person</h2>` block — same treatment.
- Removed orphaned `PniResultRepository.findAllByPrisonNumber` /
  `OasysPniResultEntityRepository.findAllByPrisonNumber` calls
  from SAR service (query bodies audited for other callers;
  { deleted / retained defensively } — see commit).
- Regenerated `sar-api-response.json` + `sar-expected-render-result.html`.

### Not touched

- `V145__add_staff_last_name_index.sql` — Flyway is forward-only.
- `PersistenceHelper.createPerson` — helper serves other tests;
  only the SAR-fixture call site removed.
- `originalReferral{…}` sub-block on referrals — PR-7's UUID strip
  stands; block still renders.

### Overrides recorded

DD row 139 (Roxanne, 2026-07-10) had kept `pni_result_json` in
scope. Deborah's 2026-08-13 meeting outcome supersedes — see
`doc/planning/APG-2546/ROUND-2-PLAN.md` §"DD spreadsheet override".

### Testing

- `./gradlew ktlintCheck test` — X tests pass, ktlint clean.
- UUID-leak grep on both goldens returns 0 matches.
- Sample PDF: N pages (down from 4).

### Rollback

`git revert <sha>` — no migration, only additive DTO/template
removals.
```

