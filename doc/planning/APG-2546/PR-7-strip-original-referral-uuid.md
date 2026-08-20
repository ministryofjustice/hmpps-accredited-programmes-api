# PR-7 — Strip `SarOriginalReferral.id` UUID from the SAR sub-block

> **Ticket:** APG-2546 • **Branch:** `APG-2546/strip-original-referral-uuid`
> • **Est.:** 0.25 dev day (single field, one mapper, one template row,
> one test assertion, snapshot regen) • **Blocks:** PR-6 (OSAR handover
> should reflect the final zero-UUID state)
> • **Depends on:** PR-5 merged (rebase off `main` after PR-5 #1112 lands)

## Purpose

Follow-on cleanup from PR-5's nine-lens code review (2026-08-05).
PR-5 stripped every raw UUID that leaked into the SAR payload
**except** the nested `SarOriginalReferral.id`, which was retained
per PR-5's doc as a conscious "sub-block is unaffected" choice.

Roxanne's stance from DD rows 105 + 111 is a blanket rule:
*"No Ids to be included in SAR reports."* The nested sub-block's
own UUID is another instance of the same category — an internal
referral primary key, opaque to the subject, no informational
value once the resolved details (course name, submitted on,
status, referrer surname, override reason, LDC flag, additional
info) are rendered right next to it. Confirmed by Raby on
2026-08-05 following the PR-5 review flag: *"she wants the ids
stripping [—] internal ones not useful to reader."*

This PR is the last raw-UUID scrub on the SAR API surface for
APG-2546. Everything else already went in PRs 4 and 5.

## Why this is a separate PR rather than a PR-5 amend

- PR-5 (#1112) is already open with a green nine-lens review and
  a snapshot diff a reviewer can eyeball at a glance. Amending it
  after review would waste that signal.
- The scope is small enough that a serial PR is cheap and keeps
  the "one field per PR, snapshot diff obvious" rhythm we've held
  for PRs 1–5.
- Sequencing-wise, PR-7 slots between PR-5 and PR-6 (OSAR
  handover) so the round-2 PDF Cameron's team wraps reflects the
  final zero-UUID content shape. See "Sequencing" below.

## Prerequisites for a fresh agent

Read `doc/planning/APG-2546-sar-field-removals.md` (§B) and
`doc/planning/APG-2546/PR-5-strip-internal-ids.md` (esp. the
retained `{{#originalReferral}}` block discussion) so you
understand why this cleanup exists as a follow-on.

Also skim `doc/planning/APG-2546/DELIVERY-LOG.md` "PR-5 review
flag → PR-7 spun" timeline entry for the paper trail.

No external questions gate this PR — Roxanne's rows 105 + 111
"no IDs" note covers this by construction and Raby confirmed
2026-08-05.

## `SarOriginalReferral` state on `main` after PR-5

```kotlin
data class SarOriginalReferral(
    val id: UUID,                       // ← this PR removes
    val courseName: String?,
    val organisationName: String?,
    val submittedOn: LocalDateTime?,
    val statusCode: String?,
    val referrerSurname: String?,
    val referrerOverrideReason: String?,
    val hasLdc: Boolean?,
    val additionalInformation: String?,
)
```

Mapper (`ReferralEntity.toSarOriginalReferral(...)`) sets
`id = id!!` from the resolved original-referral entity. All other
fields stay — they're the observable contract for the subject.

Template block (`{{#originalReferral}}` in `sar_template.mustache`,
lines 21–34 on `main`):

```mustache
{{#originalReferral}}
    <h4>Original referral</h4>
    <table class="summary-list">
        <tr><td>Original referral ID</td><td>{{ optionalValue id }}</td></tr>  ← this PR removes
        <tr><td>Course name</td><td>{{ optionalValue courseName }}</td></tr>
        <tr><td>Organisation name</td><td>{{ optionalValue organisationName }}</td></tr>
        ...
    </table>
{{/originalReferral}}
```

## Files to change

### 1. `src/main/kotlin/…/service/SubjectAccessRequestService.kt`

- **`data class SarOriginalReferral`** — delete `val id: UUID,`
  (currently the first field of the data class, right after
  `data class SarOriginalReferral(`).
- **`ReferralEntity.toSarOriginalReferral(...)` mapper** — delete
  the `id = id!!,` line (currently the first argument inside the
  `SarOriginalReferral(...)` constructor call). Named-arg mapper,
  so no positional-alignment risk.

Post-edit sanity check (should print zero hits):

```zsh
grep -n "SarOriginalReferral\.id\|originalReferral\.id\|id = id!!" \
  src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt
```

The `originalsById.associateBy { it.id!! }` at ~line 68 stays —
that's `ReferralEntity.id`, entity-layer, not the DTO field.

### 2. `src/main/resources/sar_template.mustache`

Inside the `{{#originalReferral}}` block (around line 24 on
`main`), delete the row:

```mustache
<tr><td>Original referral ID</td><td>{{ optionalValue id }}</td></tr>
```

Keep the surrounding `<h4>Original referral</h4>` heading and the
remaining rows (course name, organisation name, submitted on,
status, referrer surname, referrer override reason, has LDC,
additional information) — those are what the subject actually
sees.

Post-edit sanity check:

```zsh
grep -n "Original referral ID\|Person ID" src/main/resources/sar_template.mustache
```

Should return zero hits. PR-5 already removed the top-level
"Original referral ID" row (line 14) and "Person ID" row
(line 106).

### 3. `src/test/kotlin/…/service/SubjectAccessRequestServiceTest.kt`

Delete the single line (currently around line 258 after PR-5):

```kotlin
assertThat(originalReferral.id).isEqualTo(originalReferralId)
```

**Do not** delete the local `val originalReferralId = UUID.fromString("11111111-…")`
at ~line 85 — it's still used by
`ReferralEntityFactory.withOriginalReferralId(originalReferralId)`
(seeding the parent referral's entity-level FK) and by the
`referralRepository.findAllById(setOf(originalReferralId, orphanedOriginalId))`
mock stub (~line 176). Same for `orphanedOriginalId`. Both drive
the batch-lookup behaviour we still assert (resolved sub-block
vs. null-block on orphan / null-fk).

Post-edit sanity check:

```zsh
grep -n "originalReferral\.id\|\.originalReferralId" \
  src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestServiceTest.kt
```

Expect the seed / mock references at ~lines 85, 101, 130, 142,
176 to remain; expect the assertion at ~line 258 to be gone.

## Snapshot regeneration

```zsh
SAR_GENERATE_ACTUAL=true ./gradlew test \
  --tests '*SarContractIntegrationTest*' --rerun-tasks
cp src/test/resources/sar-api-response.json.log            src/test/resources/sar/sar-api-response.json
cp src/test/resources/sar-generated-report.html.log        src/test/resources/sar/sar-expected-render-result.html
./gradlew test --tests '*SarContractIntegrationTest*'
./gradlew test --tests '*SubjectAccessRequestServiceTest*'
```

**Expected snapshot diff:**

- `sar-api-response.json` — **no diff.** The integration-test
  fixture doesn't seed a resolvable `originalReferral`; the
  parent referral has no `originalReferralId`, so
  `referrals[0].originalReferral` is already `null` in the
  snapshot. Removing a field from a data class whose instances
  are never constructed in the fixture won't change the JSON.
- `sar-expected-render-result.html` — **no diff.** Same reason:
  the `{{#originalReferral}}` block never renders in the
  integration-test fixture because the section variable is null.
- `entity-schema.json` — no diff (no JPA entity edits).

If any of those actually diff, you've hit a fixture drift
elsewhere and should stop and investigate before committing.

Unit-test coverage of the actual change comes from
`SubjectAccessRequestServiceTest` — that test *does* seed a
resolvable original referral and its assertions on the resolved
sub-block (courseName, organisationName, statusCode,
referrerSurname, referrerOverrideReason, hasLdc,
additionalInformation) remain the observable contract. Losing
`originalReferral.id` from those assertions is the only behavioural
change the tests need to see.

## Verification checklist

```zsh
# Service — DTO + mapper
grep -n "SarOriginalReferral(" src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt
grep -n "val id: UUID" src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt   # should not name SarOriginalReferral

# Template
grep -n "Original referral ID" src/main/resources/sar_template.mustache          # zero
grep -n "Person ID" src/main/resources/sar_template.mustache                     # zero (already gone via PR-5)

# Test — assertion gone but seed / mock references kept
grep -n "originalReferral\.id\|\.originalReferralId" src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestServiceTest.kt

# Build
./gradlew ktlintCheck test
```

Verify sample PDF still renders at **3 pages** (should not change
— same fixture, one row narrower on the nested block which
doesn't render in the fixture anyway).

## Non-obvious things

### 1. Snapshots may not diff — that's expected, not a bug

The integration-test fixture seeds a single referral with no
`originalReferralId`, so the nested `originalReferral` block is
`null` in the JSON snapshot and the HTML fixture never renders
the `{{#originalReferral}}` block. Removing a field from
`SarOriginalReferral` therefore doesn't alter the emitted JSON /
HTML. This is different from PR-5 (which changed fields on
always-rendered blocks). A committer who's expecting a snapshot
diff and doesn't see one may worry — the correct behaviour is to
verify the sanity greps + let the unit test's dropped assertion
carry the coverage.

If you want a defensive snapshot diff for reviewers to see, you
*could* extend the integration test's `setupTestData()` to seed an
`originalReferralId` + matching resolved referral row. This is
**out of scope** for PR-7 — it's a test-fixture enhancement, not
a field-removal change, and would blur the PR's intent. Flag it
as a follow-up idea if you feel strongly, but don't fold it in.

### 2. Entity-layer `ReferralEntity.originalReferralId` stays

Untouched. The batch lookup in `getPrisonContentFor` still needs
to read `it.originalReferralId` off the parent entity, use it as
the batch-`findAllById` key, and thread the resolved entity into
`toSarOriginalReferral`. The removed field is the *DTO surface*
`SarOriginalReferral.id`, not the FK on the source entity.

### 3. `id!!` double-bang goes with the mapper line

The `id = id!!,` line in `toSarOriginalReferral` was the only
double-bang on the entity's nullable `id: UUID?` inside that
mapper. Its removal is safe: the batch-lookup that populates
`originalsById` already asserts non-null on the entity id via
`originalsById.associateBy { it.id!! }` at ~line 68 (an earlier
NPE point), so we haven't lost a null-safety canary — we've just
moved the last observable read of it up the pipeline.

### 4. PR-6 sequencing

Once PR-7 is on `main`, kick off PR-6 (OSAR handover) so the
round-2 PDF Cameron's team wraps reflects the final zero-UUID
content shape. If PR-6 is already in-flight when PR-7 lands,
rebase PR-6 onto `main` and re-generate the sample PDF as part
of PR-6's artefacts capture.

## PR description template

```
APG-2546: strip SarOriginalReferral.id UUID from the SAR sub-block

Follow-on to PR-5 (#1112) per its nine-lens review flag
(2026-08-05). PR-5 stripped every raw UUID on the SAR API surface
except the nested SarOriginalReferral.id, which was retained as a
conscious "sub-block unaffected" choice. Raby confirmed 2026-08-05
that Roxanne's row 105 + 111 blanket "No Ids to be included in
SAR reports" rule applies here too — internal referral primary
keys are opaque to the subject once the resolved details are
rendered right next to them.

Changes:
- Delete val id: UUID from SarOriginalReferral data class
- Delete id = id!! from ReferralEntity.toSarOriginalReferral mapper
- Delete the "Original referral ID" row from the {{#originalReferral}}
  block of sar_template.mustache
- Drop the SubjectAccessRequestServiceTest assertion on
  originalReferral.id (line ~258) — coverage of the resolved
  sub-block via courseName / organisationName / statusCode /
  referrerSurname / referrerOverrideReason / hasLdc /
  additionalInformation assertions remains

Not changed:
- Entity-level ReferralEntity.originalReferralId — still required
  for the findAllById batch lookup that populates the sub-block
- Every other field on SarOriginalReferral — that's the observable
  contract for the subject and remains fully covered by unit tests
- SAR contract snapshots (sar-api-response.json + sar-expected-render-result.html)
  — no diff expected because the integration-test fixture doesn't
  seed a resolvable originalReferralId (the {{#originalReferral}}
  block never renders in the fixture). Coverage of the removal
  lives in the unit test. Snapshot regen still run + committed
  if any actual diff surfaces (would signal fixture drift).

Sample PDF: 3 pages (same as PR-5 baseline — nested block
doesn't render in the fixture).
```

## Definition of done

- [ ] Grep checks in "Verification checklist" all pass.
- [ ] `./gradlew ktlintCheck test` green.
- [ ] Snapshot regen run, `.log` vs `sar/*` diffs eyeballed
      (expect zero diff — record if any).
- [ ] PR description surfaces the "no snapshot diff expected"
      explicitly so reviewers know not to bounce.
- [ ] Sample PDF page count noted (expect 3).
- [ ] DELIVERY-LOG updated with PR-7 status row + timeline entry
      on the planning branch.

