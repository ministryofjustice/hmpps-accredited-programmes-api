# SAR contract-fixture widening — vettor-training exemplar + snapshot coverage

**Status:** ✅ picked up and shipped 2026-08-11. Branch
`test/sar-contract-fixture-widening` (head `3969f93f`) off `main`
@ `baee4510`; draft PR opened. See "Pickup notes 2026-08-11" at
the bottom of this doc for what actually happened vs the plan
below — three deviations were required and are captured there.
The rest of this doc is preserved as-shipped so a future re-run
against a different fixture (e.g. a full-chrome PDF replacement)
has the same field-by-field cited-values plan to work from.

**~~Status:~~** ~~ready to pick up on a fresh branch.~~ **Not APG-2546 scope.**
Deferred follow-up recorded in `doc/planning/APG-2546/DELIVERY-LOG.md`
under "Deferred follow-ups (out of APG-2546 scope)" — see there for
origin history. This doc is self-contained so a fresh agent can execute
in a clean chat without loading APG-2546 context.

## TL;DR

Widen `src/test/resources/sar/sar-api-response.json` (and the matching
`sar-expected-render-result.html` snapshot) so the SAR contract test
exercises **every field** the SAR API can emit, using **realistic
domain-shaped values verified against elsewhere in the codebase**. Two
motivations, both currently live:

1. **Vettor-training exemplar.** Deborah (Senior DM on Cameron's SAR
   product) said 2026-08-04 pm: *"OSAR prefer as many fields populated
   as possible for vettor training"*. Under Option 2 (chrome-less
   test-harness PDF fallback) the fixture is what OSAR see. Ideally
   Option 1 (rich preprod CRN via Cameron's SAR dev service) covers
   this — but this ticket is the belt-and-braces if Option 1 pipeline
   blocks like it did in round 1.
2. **Snapshot coverage.** PR-7 (APG-2546, merged `baee4510`
   2026-08-07) stripped the retained `SarOriginalReferral.id` UUID
   but produced zero snapshot diff because the fixture's parent
   referral has `originalReferralId = null`, so `{{#originalReferral}}`
   never renders. Any future accidental re-introduction of a raw UUID
   into the sub-block will slip past the contract test. Seeding a
   resolvable original referral fixes the gap.

Estimated effort: **~2 hours engineering + ~1 hour review**. Diff is
~40 lines added to `SarContractIntegrationTest.kt` + a large-but-
expected snapshot regeneration.

## Prerequisites — do not skip

**Assumed starting point:** tip of `main` after APG-2546 PR-7 (#1113)
has merged. Current tip should be `baee4510` or later. Verify with:

```
git checkout main && git pull --ff-only && git log --oneline -1
```

If `baee4510` isn't in the history yet, stop and flag — this ticket
assumes the `SarOriginalReferral.id` field is already gone from the
DTO. Working against pre-PR-7 code will look like a re-introduction
attempt.

**Sanity check the DD spreadsheet hasn't drifted.** This ticket
populates fields that Roxanne's Digital Data review (`doc/Copy of
2026.07.08_copy_Probation Digital Data review December 251.xlsx`,
sheet **Accredited Programmes Custody**) has explicitly cleared as
SAR-appropriate. Rerun `python3 doc/planning/APG-2546/scripts/dd-notes-sweep.py
"doc/Copy of 2026.07.08_copy_Probation Digital Data review December 251.xlsx"`
(copy the working xlsx into `doc/` first — it's untracked) and confirm
none of the fields in §"Field-by-field plan" below have been re-flagged
as "should be a No" since 2026-08-05. If they have, stop and flag
before touching the fixture.

## Branch + PR conventions

- **Branch name:** `test/sar-contract-fixture-widening` (no Jira
  prefix — this isn't APG-2546 and doesn't yet have its own ticket
  in Jira; if you spin one, prefix the branch to match).
- **Commit style:** matches recent history — see PRs #1107 through
  #1113 on `main`. Subject line: `test: widen SAR contract fixture
  for vettor-training exemplar + originalReferral snapshot coverage`.
- **PR body template:** see §"PR body template" at the bottom of
  this doc. **Lead with "expected snapshot diff = large, this is
  intentional"** or reviewers will bounce it — same discipline as
  PR-7's "expect zero snapshot diff" note but the opposite direction.

## Files to change

Exactly two source files:

1. **`src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/common/config/PersistenceHelper.kt`**
   — extend `createReferral(...)` (currently line 108) with one
   optional new parameter `hasReviewedAdditionalInformation: Boolean? = null`,
   add the corresponding column to the SQL and one `setParameter`
   call. See §"Bucket 3 spike outcome" below for the exact 3-line
   edit — this parameter is genuinely missing today.
2. **`src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/integration/SarContractIntegrationTest.kt`**
   — the substantive change. Add companion consts for the second
   referral + second staff. Extend the existing `createReferral`,
   `createPerson`, `createPniResult`, `createStaff` calls with the
   widened values. Add a new `createReferral` call for the "original
   referral" that the primary referral points at, plus a matching
   `createStaff` for the secondary POM.

Two golden files regenerated by the SAR test library:

3. **`src/test/resources/sar/sar-api-response.json`** — expect
   grown from ~1.7 KB to ~2.5 KB, no removed keys, ~15 nulls
   become populated + new populated `originalReferral: { … }`.
4. **`src/test/resources/sar/sar-expected-render-result.html`** —
   expect ~11.7 KB → ~13 KB, populated rows now show values
   instead of "No Data Held", new `<h4>Original referral</h4>`
   block appears with 8 rows.

**Do not touch:**
- `src/test/resources/sar/entity-schema.json` — schema of the JPA
  entities, unrelated to this change. Must remain byte-identical.
- Any file under `src/main/` — this is a test-fixture change with
  zero product-code impact.
- The `dd-notes-sweep.py` script (planning branch only, not on
  `main`).

## Bucket 3 spike outcome — `hasReviewedAdditionalInformation`

Verified during doc prep 2026-08-11:

- **Real DB column** (`referral.has_reviewed_additional_information`)
  — introduced by Flyway migration
  `V128__add_has_reviewed_info_flag_to_referral.sql`.
- **DTO field** (`SarReferral.hasReviewedAdditionalInformation: Boolean?`)
  — at `SubjectAccessRequestService.kt:187`.
- **Mapper passthrough** at `SubjectAccessRequestService.kt:314`.
- **Template row** at `sar_template.mustache:16`
  (`{{ convertBoolean hasReviewedAdditionalInformation }}`).
- **Entity property** at `ReferralEntity.kt:52`.
- ❌ **Missing from `PersistenceHelper.createReferral(...)`** —
  verified via `grep`; this parameter genuinely isn't there. So
  Bucket 3 = extend the helper by 3 lines:

```kotlin
// PersistenceHelper.createReferral — extend signature (line 108) and
// SQL (line 109) and add one setParameter call (after line 124):

fun createReferral(
  referralId: UUID, offeringId: UUID, prisonNumber: String, referrerUsername: String,
  additionalInformation: String, oasysConfirmed: Boolean, hasReviewedProgrammeHistory: Boolean,
  status: String, submittedOn: LocalDateTime?, primaryPomStaffId: BigInteger = "1".toBigInteger(),
  secondaryPomStaffId: BigInteger = "2".toBigInteger(), referrerOverrideReason: String? = null,
  originalReferralId: UUID? = null, hasLdc: Boolean = false,
  hasLdcBeenOverriddenByProgrammeTeam: Boolean = false,
  hasReviewedAdditionalInformation: Boolean? = null,          // ← NEW
) {
  entityManager.createNativeQuery(
    "INSERT INTO referral (referral_id, offering_id, prison_number, referrer_username, " +
      "additional_information, oasys_confirmed, has_reviewed_programme_history, status, submitted_on, " +
      "primary_pom_staff_id, secondary_pom_staff_id, referrer_override_reason, original_referral_id, " +
      "has_ldc, has_ldc_been_overridden_by_programme_team, has_reviewed_additional_information) " +   // ← EXTEND
      "VALUES (:id, :offeringId, :prisonNumber, :referrerUsername, :additionalInformation, " +
      ":oasysConfirmed, :hasReviewedProgrammeHistory, :status, :submittedOn, :primaryPomStaffId, " +
      ":secondaryPomStaffId, :referrerOverrideReason, :originalReferralId, :hasLdc, " +
      ":hasLdcBeenOverriddenByProgrammeTeam, :hasReviewedAdditionalInformation)"                     // ← EXTEND
  )
    // ...existing setParameter calls...
    .setParameter("hasReviewedAdditionalInformation", hasReviewedAdditionalInformation)             // ← NEW
    .executeUpdate()
}
```

Backwards-compatible (default `null`), so no other test breaks.

## Field-by-field plan — every value verified against source

### Bucket 1: extend existing calls (values verified against codebase)

Each value below is either an already-in-use literal elsewhere in the
codebase, or a domain-consistent extension. **Do not deviate from
these values without doing the same source-check** — the point of the
widened fixture is to be a realistic training exemplar, and a made-up
value that doesn't match a real enum wastes vettor time.

#### On the existing `createPerson(...)` call (currently `SarContractIntegrationTest.kt:185–194`)

| Field | Value | Source of truth |
|---|---|---|
| `conditionalReleaseDate` | `"2026-11-15"` | ISO-8601 date-string parseable by Postgres; format matches `PersonEntity.conditionalReleaseDate: LocalDate`. Value is illustrative — pick a future-facing date consistent with the other dates below (i.e. earlier than `paroleEligibilityDate` and `tariffExpiryDate`). |
| `paroleEligibilityDate` | `"2027-03-01"` | Same format. |
| `tariffExpiryDate` | `"2028-06-30"` | Same format. |
| `earliestReleaseDate` | `"2026-09-10"` | Same format. Earlier than the other three so the rendered PDF shows a plausible sentence-progression ordering. |
| `indeterminateSentence` | `false` | Matches the existing `sentenceType = "Determinate"`. If you change to `true` for a richer exemplar, also change `sentenceType` to `"Indeterminate"` for internal consistency. |
| `nonDtoReleaseDateType` | `"Standard"` | Verified used in `ReferralServiceTest.kt:804`. TestConstants.kt:50 uses the literal placeholder `"Release date type"` — don't use that, it's obviously placeholder. `"Standard"` is a real-shaped value. |

#### On the existing `createPniResult(...)` call (currently `SarContractIntegrationTest.kt:172–178`)

| Field | Value | Source of truth |
|---|---|---|
| `oasysAssessmentCompletedDate` | `LocalDateTime.of(2026, 4, 15, 9, 0, 0)` | `LocalDateTime?` per `PersistenceHelper.kt:301`. Import already present. |
| `needsClassification` | `"HIGH_NEED"` | **Verified exact string** — `PniServiceIntegrationTest.kt:200` asserts `pniResults[0].needsClassification` equals `"HIGH_NEED"` (note: singular "NEED" not "NEEDS"). This is the value produced by prod code path `PniService.kt:91` reading `pniScore.needsScore.classification`. Do not guess. |
| `overallNeedsScore` | `12` | `Int?`. Illustrative but plausible per the OASys PNI score range convention seen in `PniControllerIntegrationTest` fixtures. |
| `riskClassification` | `"HIGH_RISK"` | **Verified exact string** — `PniServiceIntegrationTest.kt:201`. Same provenance as `needsClassification`. |
| `pniAssessmentDate` | `LocalDateTime.of(2026, 4, 20, 14, 0, 0)` | Chosen ~5 days after `oasysAssessmentCompletedDate` for a realistic workflow ordering. |
| `basicSkillsScore` | `3` | `Int?`. Illustrative small integer. |

#### On the existing `createReferral(...)` call (currently `SarContractIntegrationTest.kt:130–144`)

Add `hasReviewedAdditionalInformation = true` (safe now the helper
supports it, per Bucket 3 above). No other change to this call except
adding `originalReferralId = ORIGINAL_REFERRAL_ID` — see Bucket 2.

**Important consistency note on `referrerOverrideReason`.** The
existing referral has `oasysConfirmed = true`. Setting
`referrerOverrideReason` non-null on the *same* referral is
semantically inconsistent — the override reason exists precisely for
"submitting *without* OASys confirmation". Do **not** set it on the
primary referral. Set it instead on the *original* referral in
Bucket 2 (which is the WITHDRAWN one that predates the current
resubmission and has a realistic reason for having been withdrawn).
That preserves internal data consistency and gives the vettor a
realistic scenario to see the field in.

#### On the existing `createStaff(...)` call (currently `SarContractIntegrationTest.kt:216–222`)

No change to the existing staff record. Add a **new** `createStaff`
call after it for the secondary POM (see Bucket 2 §Staff).

### Bucket 2: seed the `originalReferral` sub-block

#### New companion consts

Add after `REFERRAL_STATUS_HISTORY_ID` at `SarContractIntegrationTest.kt:243`:

```kotlin
val ORIGINAL_REFERRAL_ID: UUID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")
val SECONDARY_STAFF_ID: BigInteger = "67890".toBigInteger()  // matches the existing secondaryPomStaffId on createReferral
val SECONDARY_STAFF_ROW_ID: UUID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")
val ORIGINAL_REFERRAL_SUBMITTED_ON: LocalDateTime = LocalDateTime.of(2024, 1, 15, 9, 30, 0)
```

Rationale for `ORIGINAL_REFERRAL_SUBMITTED_ON`: earlier than the
primary referral's `SUBMITTED_ON = 2024-06-01T10:00:00` so the
workflow ordering makes sense (original submitted → withdrawn →
re-referred onto current pathway).

#### New `createReferral(...)` call — the resolvable original

Add **before** the existing primary `createReferral` call (i.e.
between `createReferrerUser` at line 129 and the existing
`createReferral` at line 130), so the FK from the primary → original
is satisfied at insert time:

```kotlin
persistenceHelper.createReferral(
  referralId = ORIGINAL_REFERRAL_ID,
  offeringId = OFFERING_ID,
  prisonNumber = PRISON_NUMBER,
  referrerUsername = "TEST_USER",
  additionalInformation = "Initial referral — subsequently withdrawn following OSP re-scoring",
  oasysConfirmed = false,                                       // consistent with the override reason below
  hasReviewedProgrammeHistory = true,
  status = "WITHDRAWN",
  submittedOn = ORIGINAL_REFERRAL_SUBMITTED_ON,
  primaryPomStaffId = 12345.toBigInteger(),                     // same POM as primary referral (same subject)
  secondaryPomStaffId = SECONDARY_STAFF_ID,
  referrerOverrideReason = "Scored higher in OSP, should go onto Kaizen",  // verified: ReferralControllerIntegrationTest.kt:308
  hasLdc = false,
  hasLdcBeenOverriddenByProgrammeTeam = false,
  hasReviewedAdditionalInformation = true,
)
```

Every value cross-checked:

- `status = "WITHDRAWN"` — a valid `ReferralStatusEntity.code` per
  the existing status-history seed pattern. Realistic reason for the
  primary referral to have superseded it.
- `oasysConfirmed = false` paired with a populated
  `referrerOverrideReason` — internally consistent per §"consistency
  note" above.
- `referrerOverrideReason` is a **verified real-shaped domain string**
  (verbatim from `ReferralControllerIntegrationTest.kt:308`). Do NOT
  substitute `"Override reason"` — that's the placeholder used
  elsewhere and would degrade the training exemplar.

#### Extend the existing primary `createReferral(...)`

Add two named args to the existing call at
`SarContractIntegrationTest.kt:130–144`:

```kotlin
// ...existing args...
originalReferralId = ORIGINAL_REFERRAL_ID,          // ← NEW
hasReviewedAdditionalInformation = true,            // ← NEW (also needs helper extension per Bucket 3)
```

Once these land, `SubjectAccessRequestService.getPrisonContentFor(...)`
line 65 (`referralRepository.findAllById(setOf(...))`) will resolve
`ORIGINAL_REFERRAL_ID` and `SarReferral.originalReferral` will render
non-null — populating the JSON sub-block and the mustache
`{{#originalReferral}}` HTML section.

#### New `createStaff(...)` call — the secondary POM

Add after the existing `createStaff(...)` call (currently
`SarContractIntegrationTest.kt:216–222`):

```kotlin
persistenceHelper.createStaff(
  id = SECONDARY_STAFF_ROW_ID,
  staffId = SECONDARY_STAFF_ID,
  firstName = "Jane",
  lastName = "Bloggs",
  username = "SECONDARY_USER",
  primaryEmail = "jane.bloggs@test.com",
)
```

Once this lands, `surnames.forStaffId(secondaryPomStaffId)` at
`SubjectAccessRequestService.kt:309` resolves and
`SarReferral.secondaryPomStaffSurname` becomes `"Bloggs"` instead of
null.

## Fields we are deliberately NOT populating — DD-sourced justification

Every field left null / unchanged in the fixture is either:

- **Stripped per APG-2546** (DD says "No" — must stay off the SAR):
  `SarPerson.id`, `SarOrganisation.id`, `SarReferral.originalReferralId`
  (top-level UUID, distinct from the resolved sub-block),
  `SarOriginalReferral.id`, `SarPniResult.pniResultId`,
  `SarOasysPniResult.pniResultId`, `SarOasysPniResult.oasysAssessmentId`,
  `SarStaff.firstName/username/email/accountType`, and the whole
  `auditRecords`, `referralStatusHistory`, `referralStatusReasons`,
  `sexualOffenceDetails`, `selectedSexualOffenceDetails` sections.
  **Never populate these** — populating would be undoing APG-2546.
- **Not on the SAR surface at all** (Column H = No, uncontested).
  Not applicable here.

Cross-checked against `Accredited Programmes Custody` sheet rows 22–235
via `doc/planning/APG-2546/scripts/dd-notes-sweep.py` on 2026-08-11.
No field this doc proposes populating carries a red-flag or "should
be a No" note.

## Verification checklist

After the source-code changes above, run in order:

1. **Compile.** `./gradlew compileTestKotlin` — expect green.
2. **Regenerate snapshots.**
   ```
   SAR_GENERATE_ACTUAL=true ./gradlew test --tests SarContractIntegrationTest
   ```
   Copy the generated files over the goldens:
   ```
   cp build/test-generated/sar-actual-api-response.json  src/test/resources/sar/sar-api-response.json
   cp build/test-generated/sar-actual-render-result.html src/test/resources/sar/sar-expected-render-result.html
   ```
   (Filenames match the SAR test-support library's `SAR_GENERATE_ACTUAL`
   convention — same pattern used across APG-2546 PRs 1–7. If they
   differ on your run, list `build/test-generated/` and adapt.)
3. **Re-run the test without the env var.** Should now be green
   against the new snapshots:
   ```
   ./gradlew test --tests SarContractIntegrationTest
   ```
4. **Full test + lint.** `./gradlew ktlintCheck test` — expect 678
   tests pass (same count as before) + ktlint green. If a test count
   change, investigate — this should be a pure fixture widening.
5. **Sanity-grep the snapshot goldens for expected shape.**
   ```
   grep -c '"[a-zA-Z]*": null' src/test/resources/sar/sar-api-response.json
   ```
   Should be substantially lower than the pre-change count (was ~15
   nulls, target ~0–2). And:
   ```
   grep -c 'No Data Held' src/test/resources/sar/sar-expected-render-result.html
   ```
   Should be substantially lower than the pre-change count (was ~15,
   target ~0–2). And:
   ```
   grep -c 'Original referral' src/test/resources/sar/sar-expected-render-result.html
   ```
   Should be `1` (the new `<h4>Original referral</h4>` block).
6. **UUID leak check.** Prove APG-2546's UUID strip is preserved
   despite widening:
   ```
   grep -Eo '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}' src/test/resources/sar/sar-api-response.json
   grep -Eo '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}' src/test/resources/sar/sar-expected-render-result.html
   ```
   Should return **zero matches** on both. **If any UUID appears, the
   widening has regressed APG-2546 — do not merge.**
7. **PDF page count.** Open `build/test-generated/sar-generated-report.pdf`
   (or run the test once more; the library regenerates it). Expected
   3 or 4 pages. Anything higher signals a template-rendering surprise
   — investigate before merging.
8. **`entity-schema.json` unchanged.** `git diff --stat
   src/test/resources/sar/entity-schema.json` should show no diff.

## Risks + gotchas

1. **The snapshot diff will look scary.** ~15 rows in the HTML flip
   from "No Data Held" → real value, plus a whole new
   `<h4>Original referral</h4>` block with 8 rows appears. Lead the PR body
   with **"expected snapshot diff = large, this is intentional. Here
   is the coverage-before/coverage-after."** Same discipline as
   PR-7's "expect zero snapshot diff" call-out but the opposite
   direction.
2. **Data consistency across the two referrals.** Both share
   `PRISON_NUMBER` and `primaryPomStaffId`, which is correct
   (subject is the same person, POM is the same person). The
   `secondaryPomStaffId` also matches on both (same secondary POM
   throughout the subject's referral history). If you change any of
   these, keep them consistent across both referrals or the render
   will look nonsensical.
3. **`originalReferral.originalReferralId` chain.** The
   `originalReferral` sub-block resolves at most one hop
   (`SubjectAccessRequestService.kt:315–318`) — it does *not*
   recurse. Do not seed the original referral itself with an
   `originalReferralId` — it would be silently ignored today and
   any future change to introduce recursion would surprise anyone
   reading this fixture. Leave it null (default) on the original.
4. **`persistenceHelper.clearAllTableContent()` at line 104** does
   clear the `referral` table between test invocations, so seeding
   two referrals is safe from bleed-through — verified same pattern
   as `createCourseParticipation` / `createSelectedSexualOffenceDetails`.
5. **DD spreadsheet drift.** The DD-sweep result is a point-in-time
   snapshot from the working xlsx copy. If Roxanne refreshes her DD
   between now and merge, rerun the sweep and reconfirm no field
   below has been flipped to "should be a No".
6. **Do not touch `entity-schema.json`.** It's a snapshot of JPA
   entity shape, not response shape. Changes there would signal a
   Kotlin entity mutation, which we're deliberately not doing.

## Definition of done

- [ ] Both source files edited exactly per §"Files to change".
- [ ] Both snapshot goldens regenerated.
- [ ] Every item in §"Verification checklist" green.
- [ ] Sample PDF opened + eyeballed — populated rows visibly render
      real values (not raw enum-looking strings that would confuse
      vettors).
- [ ] PR body uses the template below and leads with the "expected
      large diff" warning.
- [ ] Optional: link the PR back to APG-2546 DELIVERY-LOG's
      "Deferred follow-ups" entry, and (if a Jira ticket exists for
      this) flip the DELIVERY-LOG follow-up from "warm" to "ticketed"
      with the ticket URL.

## PR body template

Copy verbatim; fill in the bracketed placeholders.

---

**Widen SAR contract-test fixture — vettor-training exemplar +
`originalReferral` snapshot coverage**

> ⚠️ **Expected snapshot diff is intentionally large.** ~15 HTML
> rows flip from `No Data Held` → real values, plus a new
> `<h4>Original referral</h4>` block with 8 rows appears. This is
> the whole point of the change. See "Why the diff is loud" below
> before dismissing.

### Motivation

Two drivers, both tracked in `doc/planning/APG-2546/DELIVERY-LOG.md`
under "Deferred follow-ups":

1. **Vettor-training exemplar.** Cameron's SAR product team
   (Deborah, 2026-08-04) asked for as-populated-as-possible sample
   data so redaction reviewers train on realistic values rather
   than "No Data Held" placeholders. Fallback-relevant if OSAR
   handover uses the chrome-less test-harness PDF (Option 2)
   instead of the full-chrome dev-CRN-rendered PDF (Option 1).
2. **APG-2546 PR-7 (#1113) follow-up.** PR-7 stripped
   `SarOriginalReferral.id` but produced zero snapshot diff because
   the fixture's parent referral had `originalReferralId = null`.
   Seeding a resolvable original referral closes the coverage gap
   so future accidental UUID re-introductions surface in review.

### What this PR does

- Extends `PersistenceHelper.createReferral(...)` with one optional
  `hasReviewedAdditionalInformation` parameter (backwards-compatible,
  defaults to `null`).
- Widens `SarContractIntegrationTest.setupTestData()`:
  - Populates ~15 previously-null fields on `person`, `pniResult`,
    and the primary referral with values verified against
    already-in-use literals elsewhere in the codebase.
  - Adds a second referral (the resolvable "original") + a second
    staff record (secondary POM), and points the primary referral
    at the original via `originalReferralId`.
- Regenerates both SAR contract snapshot goldens.

**No product-code changes.**

### Why the diff is loud

The fixture was hand-built to test the *shape* of the SAR response,
not to be a training exemplar or exhaustive coverage. Widening it
naturally produces a big golden-file churn: previously-null fields
now render real values; the whole `{{#originalReferral}}` block
which the fixture never previously exercised now appears in the
HTML with 8 populated rows. Zero previously-emitted keys are
removed — every diff is either a `null → value` flip or a new key.

### Value verification

Every populated value is either a verbatim string used elsewhere
in the codebase (e.g. `needsClassification = "HIGH_NEED"` per
`PniServiceIntegrationTest.kt:200`, `referrerOverrideReason =
"Scored higher in OSP, should go onto Kaizen"` per
`ReferralControllerIntegrationTest.kt:308`), or a domain-consistent
extension of the shape those literals establish. Full audit in
`doc/planning/sar-contract-fixture-widening.md` §"Field-by-field
plan" on the planning branch — reviewers can spot-check any
individual value against its cited source.

### DD spreadsheet cross-check

Every widened field verified NOT to carry a "should be a No" note
in Roxanne's Digital Data review (`Accredited Programmes Custody`
sheet). See §"Fields we are deliberately NOT populating" for the
list of DD-mandated exclusions preserved. APG-2546's UUID scrub is
verified preserved by an explicit grep check in the widening doc's
verification checklist.

### Testing

- `./gradlew ktlintCheck test` — [X] tests pass, ktlint clean.
- Snapshot goldens regenerated via
  `SAR_GENERATE_ACTUAL=true ./gradlew test --tests SarContractIntegrationTest`;
  post-copy run passes without the env var.
- Sample PDF page count: [3 or 4]. UUID leak check (`grep -E`
  UUID regex on both goldens) returns zero matches, confirming
  APG-2546's UUID scrub is preserved.

### Rollback

`git revert <sha>` — pure test-code change, no data / API /
migration surface. Zero product-code risk.

---

## Handoff prompt for a fresh chat

Paste this into a new chat when handing off:

> Please pick up the SAR contract-fixture widening ticket. The
> working doc is at
> `doc/planning/sar-contract-fixture-widening.md` on branch
> `APG-2546/planning-sar-field-removals`. Read it end to end,
> follow the "Files to change" and "Field-by-field plan" sections
> literally, verify no field value has drifted from its cited
> source-of-truth (grep + read the cited test files), run the
> "Verification checklist", and open a PR using the "PR body
> template" at the bottom of the doc. Do NOT deviate from the
> field values in the doc without flagging first — the whole
> point of "no guessing" was to cite them from source. Assumed
> starting point: tip of `main` after APG-2546 PR-7 (#1113
> `baee4510`) has merged.

## Pickup notes 2026-08-11 — what actually happened

Picked up on branch `test/sar-contract-fixture-widening` (head
`3969f93f`) off `main` @ `baee4510`. Shipped as a draft PR
following this doc's plan. Recording the three deviations that
were required so a future re-run doesn't repeat the debugging.

### Outcomes

- 678 tests pass (matches the doc's target count exactly — pure
  fixture widening, no test-count change).
- `./gradlew ktlintCheck` green.
- **UUID-leak grep returns 0 on both goldens** (APG-2546 UUID
  scrub preserved — the merge-blocker check from
  §"Verification checklist" #6).
- Snapshot golden byte counts:
  - `sar-api-response.json`: 1762 → **2720 B**
  - `sar-expected-render-result.html`: 11720 → **13681 B**
  - `entity-schema.json`: **unchanged** (5836 B ✓)
- Sample PDF: **4 pages** (within expected 3–4 range).
- Post-widening sanity greps:
  - JSON `":null"` occurrences: 16 → **2** (target 0–2 ✓)
  - HTML `No Data Held`: 15 → **1** (target 0–2 ✓)
  - HTML `Original referral`: **1** ✓
- DD-sweep re-run 2026-08-11 against the working xlsx — no field
  this doc proposes populating has been re-flagged as "should be
  a No" since the doc was written.

### Deviations from this doc (kept minimal, all flagged in the PR body)

1. **§"Files to change" / §"Field-by-field plan" line-number
   references are stale.** The doc references `createAuditRecord`,
   `createSexualOffenceDetails`, `createSelectedSexualOffenceDetails`,
   and `createReferralStatusHistory` calls in
   `SarContractIntegrationTest.setupTestData()`, plus corresponding
   companion consts `AUDIT_RECORD_ID`, `SEXUAL_OFFENCE_ID`,
   `SELECTED_SEXUAL_OFFENCE_ID`, `REFERRAL_STATUS_HISTORY_ID`.
   **None of these exist on `main` at `baee4510`** — the test
   file has been slimmed since this doc was written (likely as
   part of APG-2546's PR-1 → PR-4 removals dropping the
   corresponding sections from the SAR surface, which made
   their fixture scaffolding redundant). Consequence:
   - Doc says "Add after `REFERRAL_STATUS_HISTORY_ID` at
     `SarContractIntegrationTest.kt:243`" — actual: added after
     `PERSON_ID` (the real last const in the companion).
   - Doc says "Extend the existing primary `createReferral(...)`
     at `SarContractIntegrationTest.kt:130–144`" — actual line
     numbers differ, but the block is unambiguous by contents.
   The substantive plan is unaffected: still exactly one new
   `createReferral` insertion (the original), extension of the
   primary `createReferral`, widening of `createPerson` and
   `createPniResult`, and one new `createStaff`. No made-up
   values were needed.

2. **§"Verification checklist" #2 snapshot-regeneration path
   is wrong.** The doc says
   `SAR_GENERATE_ACTUAL=true ./gradlew test --tests SarContractIntegrationTest`
   writes `build/test-generated/sar-actual-api-response.json`
   and `sar-actual-render-result.html`. **It doesn't.** The SAR
   library writes `entity-schema.json.log`,
   `sar-api-response.json.log`, and
   `sar-generated-report.html.log` under `src/test/resources/`
   (peer to the `sar/` folder). The repo has a canonical
   `script/local-scripts/regenerate-sar-snapshots.sh` which
   knows the real convention (finds the three `.log` files,
   copies them into `sar/`, deletes the `.log`s). **Use the
   script** — the manual `SAR_GENERATE_ACTUAL=true …` + `cp`
   incantation in the doc is stale.

3. **§"Field-by-field plan" §`createPerson` — the ISO-8601
   date strings don't bind cleanly.** The doc claims each date
   value (`conditionalReleaseDate = "2026-11-15"` etc.) is
   "ISO-8601 date-string parseable by Postgres". This is wrong
   under the current `PersistenceHelper.createPerson` binding:
   the params are declared as `String?` and JPA/Hibernate binds
   them as `varchar`, so Postgres raises
   `ERROR: column "conditional_release_date" is of type date
   but expression is of type character varying / Hint: You
   will need to rewrite or cast the expression`. Fixed **inside
   the helper** by parsing to `LocalDate` at bind time:

   ```kotlin
   .setParameter("conditionalReleaseDate", conditionalReleaseDate?.let { LocalDate.parse(it) })
   .setParameter("paroleEligibilityDate",  paroleEligibilityDate?.let  { LocalDate.parse(it) })
   .setParameter("tariffExpiryDate",       tariffExpiryDate?.let       { LocalDate.parse(it) })
   .setParameter("earliestReleaseDate",    earliestReleaseDate?.let    { LocalDate.parse(it) })
   ```

   The public helper signature still accepts `String?` (so no
   caller signature change), and no existing caller passed a
   non-null value for any of these four params (verified via
   `grep`), so no other test is affected. Adds one import
   (`java.time.LocalDate`) to `PersistenceHelper.kt`. **This is
   a fourth file changed vs. this doc's "Files to change"
   section #1 (which only anticipated adding
   `hasReviewedAdditionalInformation` to `createReferral`).**

### For a future re-run: prerequisites-check tweak

The "Prerequisites — do not skip" section still applies verbatim
(main tip check, DD sweep). No changes there. But if a future
picker-up sees `SarContractIntegrationTest.kt` has grown *back*
(e.g. a new section has been added), they should verify the
missing-const claim in deviation #1 is still true before placing
new consts after `PERSON_ID`.
