# PR-8 — Remove `pniResults`, `oasysPniResults` and `person` sections

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/remove-pni-oasys-person`
> • **Est.:** 1½ dev days • **Blocks:** PR-12 (hygiene tidy); transitively PR-13 via PR-12
> • **Sequencing:** must merge **first** in the round-2 sequence; PR-9/10/11 must all be serialised after it (shared edits to `SubjectAccessRequestService.kt` + `sar_template.mustache`).
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

**Start from a fresh checkout of `origin/main` (currently `0cf89850`),
NOT this planning branch.** The planning branch
(`APG-2546/planning-sar-field-removals`) only adds docs — its `src/`
tree is at merge-base `106e27d2` (pre-round-1) so every line-number
below will look wrong if you read files from the planning-branch
worktree. First command in your session:

```sh
git fetch origin && git checkout origin/main  # should be 0cf89850
```

Read in this order:

1. [`ROUND-2-PLAN.md`](./ROUND-2-PLAN.md) — round-2 overview + PR breakdown + PR #1115 impact matrix
2. [`DELIVERY-LOG.md`](./DELIVERY-LOG.md) — round-2 kickoff entry (2026-08-13) captures the meeting, the (a)/(b) clarification outcome, and the DD row 139 override
3. This file end to end
4. Aug-12 sample PDF (last artefact sent to Branston) — **that's
   Cameron's SAR dev-service output, NOT the local test-generated
   PDF.** If you still have the Aug-12 copy on disk from PR-6
   handover, open it for context. The local
   `build/test-generated/sar-generated-report.pdf` (produced by
   `script/local-scripts/regenerate-sar-snapshots.sh`) is a
   separate contract-test artefact, useful as an internal
   before/after readability sanity-check but **not** the Branston
   deliverable.

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

**Where the DTOs live** — verified 2026-08-13 on `origin/main`:

- Top-level SAR response class is **`Content`** (NOT `SarResponse` — the class name is `Content`, nested inside `SubjectAccessRequestService`).
- **All SAR DTOs are nested classes inside `SubjectAccessRequestService.kt`** — no separate DTO files exist. So "deleting `SarPniResult`" means deleting the nested `data class SarPniResult(…)` block inside that single file, not a separate `.kt` file.

`Content` DTO on `origin/main` (line 163):

```kotlin
data class Content(
    val referrals: List<SarReferral>,
    val courseParticipation: List<SarCourseParticipation>,
    val courses: List<SarCourse>,
    val pniResults: List<SarPniResult>,           // ← this PR removes
    val person: SarPerson?,                       // ← this PR removes
    val oasysPniResults: List<SarOasysPniResult>, // ← this PR removes
    val staff: List<SarStaff>,
    val organisations: List<SarOrganisation>,
)
```

`Content` construction on `origin/main` (lines 107-119):

```kotlin
return HmppsSubjectAccessRequestContent(
  content = Content(
    referrals = filteredReferrals.toSarReferral(staffSurnames, originalsById, organisationNamesByCode),
    courseParticipation = filteredParticipations.toSarParticipation(staffSurnames),
    courses = courseRepository.getSarCourses(prn).toSarCourse(),
    pniResults = pniResultRepository.findAllByPrisonNumber(prn).toSarPniResult(),         // ← remove
    person = personRepository.findPersonEntityByPrisonNumber(prn)?.toSarPerson(),          // ← remove
    oasysPniResults = oasysPniResultEntityRepository.findAllByPrisonNumber(prn).toSarOasysPniResult(), // ← remove
    staff = staffRepository.findByPrisonNumber(prn).distinctBy { it.username }.map { it.toSarStaff() },
    organisations = codesFromFiltered.mapNotNull { organisationsByCode[it]?.toSarOrganisation() },
  ),
)
```

## Files to change

**Product code — all inside `src/main/kotlin/.../service/SubjectAccessRequestService.kt`:**

| Change | Lines on `origin/main` (@ `0cf89850`) |
|---|---|
| Remove three fields from `data class Content(...)` | lines 167 (`pniResults`), 168 (`person`), 169 (`oasysPniResults`) |
| Remove three population lines in the `Content(...)` construction | lines 112, 113, 114 |
| Delete nested `data class SarPniResult(...)` and its mapper `.toSarPniResult()` extension | line 236 + mapper elsewhere in the file |
| Delete nested `data class SarPerson(...)` and its mapper `.toSarPerson()` extension | line 250 + mapper |
| Delete nested `data class SarOasysPniResult(...)` and its mapper `.toSarOasysPniResult()` extension | line 266 + mapper |
| Delete the three field-inject/import references (`pniResultRepository`, `oasysPniResultEntityRepository`, `personRepository`) **IF** no other callers within the class. `personRepository` **stays** (other SAR-service methods may use it; verify). | class constructor params |

**Template — `src/main/resources/sar_template.mustache`:**

| Change | Lines on `origin/main` |
|---|---|
| Delete `<h2>PNI results>` block | 79–99 |
| Delete `<h2>Person>` block | 101–121 |
| Delete `<h2>OASys PNI results>` block | 123–134 |

**Repositories — orphan-audit pre-verified 2026-08-13, results locked in:**

| Repository query | Prod callers besides SAR | Verdict |
|---|---|---|
| `PniResultRepository.findAllByPrisonNumber` | **`PersonService.kt:287`** (prisoner-merge NOMIS domain-event handler) | 🛑 **KEEP THE QUERY.** Only remove the SAR service call site (line 112). ORDER BY from PR #1115 becomes irrelevant to SAR but remains harmless for the PersonService caller. |
| `OasysPniResultEntityRepository.findAllByPrisonNumber` | None in `src/main` (only SAR line 114 + test-side callers in `SubjectAccessRequestServiceTest.kt`) | 🗑️ **Delete the query.** Test-side mocks in `SubjectAccessRequestServiceTest.kt` go with PR-8 anyway. V145 stays (forward-only). |
| `PersonRepository.findPersonEntityByPrisonNumber` | **Six prod callers** — `AdminController.kt:172`, `PersonService.kt:50, 76, 177, 278`, plus SAR line 113. | 🛑 **KEEP THE QUERY.** Only remove the SAR call site. |

**Migrations**

None. V145 stays.

**Test code — three test files affected, all lines pre-verified 2026-08-13 against `origin/main` @ `0cf89850`:**

**1. `src/test/kotlin/.../integration/SarContractIntegrationTest.kt`** — fixture cleanup only (snapshot goldens are the assertion; no field-level assertions):

| Lines | Change |
|---|---|
| 177–189 | Remove `persistenceHelper.createPniResult(...)` block |
| 190–195 | Remove `persistenceHelper.createOasysPniResult(...)` block |
| 196–211 | Remove `persistenceHelper.createPerson(...)` block |
| companion object | Remove `PNI_RESULT_ID`, `OASYS_PNI_RESULT_ID`, `PERSON_ID` consts (only fed the removed calls) |

Then regenerate goldens with `script/local-scripts/regenerate-sar-snapshots.sh`.

**2. `src/test/kotlin/.../integration/SubjectAccessRequestServiceIntegrationTest.kt`** — real fixture + assertions; both sides need updating:

| Lines | Change |
|---|---|
| 83–88 | Remove `persistenceHelper.createPniResult(...)` block |
| 89–93 | Remove `persistenceHelper.createOasysPniResult(...)` block |
| 94–102 | Remove `persistenceHelper.createPerson(...)` block |
| 119 | Remove `assertThat(content.pniResults).hasSize(1)` |
| 120 | Remove `assertThat(content.person).isNotNull` |
| 121 | Remove `assertThat(content.oasysPniResults).hasSize(1)` |
| 144–147 | Remove entire `with(content.pniResults[0]) { … }` block |
| 149–153 | Remove entire `with(content.person!!) { … }` block |
| 155–158 | Remove entire `with(content.oasysPniResults[0]) { … }` block |

**3. `src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt`** — mock unit test:

| Lines | Change |
|---|---|
| 26 | Remove unused `import ... PniResultEntityFactory` (ktlint will fail if left) |
| 184–191 | Remove mock `every { pniResultRepository.findAllByPrisonNumber(prn) } returns listOf(…)` |
| 192–207 | Remove mock `every { personRepository.findPersonEntityByPrisonNumber(prn) } returns PersonEntity(…)` |
| 208–215 | Remove mock `every { oasysPniResultEntityRepository.findAllByPrisonNumber(prn) } returns listOf(…)` |
| 241 | Remove `assertThat(pniResults.size).isEqualTo(1)` inside `with(result!!.content as SubjectAccessRequestService.Content)` block |
| 242 | Remove `assertThat(person).isNotNull` |
| 243 | Remove `assertThat(oasysPniResults.size).isEqualTo(1)` |
| 288–290 | Remove `val pniResult = pniResults[0]` + two `assertThat` lines (crn, pniResultJson) |
| 292–294 | Remove `val person = person!!` + two `assertThat` lines (prisonNumber, forename) |
| 296–297 | Remove `val oasysPniResult = oasysPniResults[0]` + one `assertThat` line (prisonNumber) |
| 311 | Remove `verify { pniResultRepository.findAllByPrisonNumber(prn) }` |
| 312 | Remove `verify { personRepository.findPersonEntityByPrisonNumber(prn) }` |
| 313 | Remove `verify { oasysPniResultEntityRepository.findAllByPrisonNumber(prn) }` |

**Leave alone (belongs to PR-11 or unaffected):**
- Line 216 mock + line 244 assertion + line 299–300 assertions + line 314 verify — all `staff`-scoped, PR-11's job
- Line 225 mock + line 302–305 assertions — `organisations`-scoped, PR-10's job
- Lines 308–310 verifies (referral / courseParticipation / course repos) — sections retained

**Not affected by PR-8** (verified via grep, no action needed):
- `src/test/kotlin/.../integration/DomainEventsListenerTest.kt` — calls `pniResultRepository.findAllByPrisonNumber` and `createPniResult` at unrelated line numbers (prisoner-merge domain-event handler, still uses the query kept alive by PR-8's stay-decision on that repository)
- `src/test/kotlin/.../integration/CourseParticipationControllerIntegrationTest.kt` — uses `HmppsSubjectAccessRequestContent` at line 507 for deserialisation only, no field-level assertions on removed fields

**Snapshot goldens**: regenerated by `script/local-scripts/regenerate-sar-snapshots.sh` after all code changes above. Expected diff: three whole sections deleted from both `sar-api-response.json` and `sar-expected-render-result.html`; `entity-schema.json` shrinks by the three nested class definitions (SarPniResult, SarPerson, SarOasysPniResult).

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
- [ ] **PACT contract check** — `grep -rn 'sar\|subjectAccessRequest' src/test/**/pact 2>/dev/null` — expect **zero PACT contracts** on the SAR endpoint (SAR API is the HMPPS SAR wrapper contract, not a PACT-covered consumer surface). If any hit, stop and flag — deleting fields would break a documented consumer contract.
- [ ] **OpenAPI/Swagger check** — if the repo publishes an OpenAPI schema for the SAR endpoint (grep for `@Operation`/`@Schema` on the SAR controller), regenerate it and confirm removed fields disappear from the schema cleanly. Likely N/A (SAR is server-owned contract with the HMPPS SAR wrapper, not a REST-consumer contract) — confirm and record.
- [ ] `./gradlew ktlintCheck` clean
- [ ] `./gradlew test` — record actual pre-change test count in DELIVERY-LOG round-2 entry; expect **same or fewer** post-change (only assertion removals); zero failures
- [ ] Snapshot regen: `./script/local-scripts/regenerate-sar-snapshots.sh` — commit the resulting `sar-api-response.json` + `sar-expected-render-result.html` diffs
- [ ] Post-regen: `./gradlew test --tests '*SarContractIntegrationTest*'` clean without `SAR_GENERATE_ACTUAL`
- [ ] UUID-leak grep on both goldens returns **0 matches** (regression guard from APG-2546 round 1)
- [ ] Sample PDF page count noted for delivery-log (expect **fewer pages than round-1** — three whole sections gone). **Reminder:** the delivery-facing PDF comes from Cameron's SAR dev-service (Option 1, full-chrome); the local `build/test-generated/sar-generated-report.pdf` is the contract-test artefact, not the deliverable.
- [ ] `entity-schema.json` — either unchanged, or if changed only via removed classes, diff makes sense
- [ ] `git status --short` before commit — no `.snyk` / xlsx staged (recurring paper-cut from PR #1115; see ROUND-2-PLAN R7 for the permanent-fix task)

## Rollback

`git revert <sha>` restores three whole sections. No migration; no
new API contract addition (revert only re-adds fields, which is
backward-compatible for consumers). The concerning direction is the
*forward* removal potentially breaking consumers that still read
these keys — Deborah's 2026-08-13 ask explicitly authorises the
removal on the basis that redaction reviewers now source PNI /
personal data from the SAR wrapper header + ARNs Probation Hub, so
no consumer post-merge should be relying on them.

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

