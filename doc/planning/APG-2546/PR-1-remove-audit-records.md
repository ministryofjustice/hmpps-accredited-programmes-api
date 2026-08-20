# PR-1 — Remove `auditRecords` section from SAR

> **Ticket:** APG-2546 • **Branch:** `APG-2546/remove-audit-records`
> • **Est.:** 0.5 dev day • **Blocks:** nothing (independent of Q1/Q2)
> • **Depends on:** planning branch `APG-2546/planning-sar-field-removals`
> merged (informational only — this PR branches from `main`)

## Purpose

The single biggest OSAR-review unblock. On the retest for PRN
`A8610DY` the live-dev SAR had **28,483** audit rows in this section
alone — essentially all of the "8,000-page PDF" complaint that came
out of the round-1 review. Removing the section turns the PDF back
into something a human can read.

Row references on Roxanne's data-dictionary spreadsheet: rows 22, 23,
24, 25, 27, 28, 29, 30, 31 — every field in the `auditRecords` section
is flagged red with "After call with Raby 29.07 — this should be a no".
The whole section goes.

## Prerequisites for a fresh agent

Before touching code, read:

- `doc/planning/APG-2546-sar-field-removals.md` (§A row 1, §D, and PR-1
  detail)
- `src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt`
  end-to-end (once — it's the file you're editing)

You do **not** need to run the app locally. You do need to run the
test suite and regenerate two fixture files.

## Files to change

Package prefix (elided below as `…`):
`uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi`.

### 1. `src/main/kotlin/…/service/SubjectAccessRequestService.kt`

Line numbers are as of the planning-doc verification (2026-08-03).
If they've drifted, search by the surrounding token — the tokens
are unique in the file.

- Line 41 — remove `auditRepository: AuditRepository,` from the
  constructor argument list.
- Line 68 — delete the whole line
  `auditRecords = auditRepository.getSarAuditRecords(prn)`.
- Line 128 — delete
  `auditRecords = auditRecords.toSarAudit(staffSurnames),` from the
  `Content(...)` constructor call.
- Line 195 — delete
  `auditRecords: List<SarAuditRecord>,` from the `Content` data class.
- Lines 268–278 — delete `data class SarAuditRecord(...)` block.
- Line 423 (approx) — delete
  `fun List<AuditEntity>.toSarAudit(...)` extension mapper.
- `resolveStaffSurnames(...)` (declared ~line 149) — delete
  `auditRecords: List<AuditEntity>` from the parameter list and the
  matching call-site argument. See "Non-obvious" §1 below.
- Imports — remove `AuditEntity`, `AuditRepository`, and any
  `AuditAction`-related import if now unused. Rely on the IDE / a
  final `grep -n` pass.

### 2. `src/main/kotlin/…/domain/repository/AuditRepository.kt`

- Delete `getSarAuditRecords(prisonNumber: String): List<AuditEntity>`
  (and any `@Query` annotation attached to it). Verified as of
  2026-08-03: the only caller in `src/main` is
  `SubjectAccessRequestService`. Confirm before deleting with:

  ```zsh
  grep -rn "getSarAuditRecords" src/main src/test
  ```

  Expected after edits: matches only in `AuditRepository.kt` (if you
  haven't yet deleted the definition) and zero elsewhere.

### 3. `src/main/resources/sar_template.mustache`

- Delete lines 68–86 — the entire `<h2>Audit records</h2>` block,
  including the empty-state / `{{^auditRecords}}` branch.

### 4. `src/test/kotlin/…/integration/SarContractIntegrationTest.kt`

- Line 164 — delete the `persistenceHelper.createAuditRecord(...)`
  seed call inside `setupTestData()`.
- Lines 237–243 (UUID constants block) — delete `AUDIT_RECORD_ID`.
- Delete `AuditAction` import if unused after the seed removal.

### 5. `src/test/kotlin/…/service/SubjectAccessRequestServiceTest.kt`

- Line 186 (approx) — delete the
  `every { auditRepository.getSarAuditRecords(...) } returns …` mock.
- Line 261 (approx) — delete the `auditRecords.size shouldBe …`
  assertion.
- Around line 316 — delete
  `val audit = auditRecords[0]` and the block of audit-field
  assertions that follows it.
- Line 350 (approx) — delete
  `verify { auditRepository.getSarAuditRecords(prn) }`.
- Top of file — remove the `auditRepository` mock declaration, the
  constructor arg passed to `SubjectAccessRequestService(...)`, and
  the `AuditRepository` import.

## Snapshot regeneration

Fixtures live at `src/test/resources/sar/`. Regenerate them with:

```zsh
SAR_GENERATE_ACTUAL=true ./gradlew test \
  --tests '*SarContractIntegrationTest*' --rerun-tasks
```

This rewrites:

- `src/test/resources/sar/sar-api-response.json`
- `src/test/resources/sar/sar-expected-render-result.html`
- `src/test/resources/sar/entity-schema.json` **only if the entity
  graph changed** — for PR-1 it should not change (we're deleting a
  read, not a schema). If it does change, that's a red flag — stop
  and investigate before committing.

Then re-run the tests **without** the env var, to prove the new
snapshots pass:

```zsh
./gradlew test --tests '*SarContractIntegrationTest*'
./gradlew test --tests '*SubjectAccessRequestServiceTest*'
```

Commit the regenerated snapshot files as part of this PR.

## Verification checklist

Run each of these before pushing:

```zsh
# 1. No references to the removed section remain in src/main
grep -rn "auditRecords" src/main   # expect zero hits (template + service both clean)
grep -rn "SarAuditRecord" src/main # expect zero hits
grep -rn "toSarAudit"     src/main # expect zero hits

# 2. Repository method fully gone
grep -rn "getSarAuditRecords" src   # expect zero hits

# 3. Full test suite passes
./gradlew ktlintCheck test

# 4. PDF sanity check (optional, only if you want to eyeball the win)
open build/test-generated/sar-generated-report.pdf
```

## Non-obvious things

### 1. Impact on APG-2492's batch staff-surname resolver

Removing the `auditRecords` argument from `resolveStaffSurnames(...)`
shrinks its inputs but does **not** change its shape — the resolver
still issues exactly two queries per SAR (one by-username, one
by-staff-id). The B1 integration test in
`SubjectAccessRequestServiceIntegrationTest.kt` asserts "exactly 3
staff-repo calls per SAR" and that count **holds** — audit rows
never added extra queries, they just added usernames into the
already-batched sets.

**Call this out in the PR description** so reviewers don't wonder
whether B1 needs updating. It doesn't.

### 2. `AuditRepository` may still have other methods

Only `getSarAuditRecords` is being deleted, not the whole file /
interface. If it's a Spring Data repository with other methods (e.g.
paged listings for the audit endpoint), leave those alone. Verify
with:

```zsh
wc -l src/main/kotlin/…/domain/repository/AuditRepository.kt
```

before and after — expect a small delta (~5 lines), not a whole-file
deletion.

### 3. Don't delete `AuditEntity`

The domain entity is still written to by the audit-writing side of
the codebase; only the SAR read is going away. Deleting
`AuditEntity` will break the app.

## PR description template

```
APG-2546: remove auditRecords section from SAR

Removes the auditRecords section from the SAR payload per OSAR
round-2 review (Roxanne, DD spreadsheet rows 22–31).

Motivation: on retest PRN A8610DY the audit section contributed
28,483 rows to the SAR — the majority of the "8,000-page PDF"
complaint. Removing this section is the single largest OSAR
readability win in APG-2546.

Changes:
- Delete auditRecords from Content data class + Content(...) call
- Delete SarAuditRecord DTO + toSarAudit mapper
- Drop AuditRepository from SubjectAccessRequestService constructor
- Delete AuditRepository.getSarAuditRecords (dead after this PR)
- Delete the <h2>Audit records</h2> block from sar_template.mustache
- Delete audit-record seed, mock, and assertions from tests
- Regenerate SAR contract snapshots

Not changed:
- AuditEntity (still used by audit writers)
- Other AuditRepository methods (untouched)
- resolveStaffSurnames B1 test — call count is unaffected (see PR
  planning doc for reasoning)

Rollback: single git revert. Fixtures come back with the revert
commit; no schema state to rewind.
```

## Definition of done

- [ ] All grep checks return zero hits.
- [ ] `./gradlew ktlintCheck test` green.
- [ ] Snapshot diffs committed and reviewed (expect large shrink of
      `sar-api-response.json` and `sar-expected-render-result.html`).
- [ ] PR description filled in from the template above.
- [ ] Sample PDF page count noted in
      `doc/planning/APG-2546-sar-field-removals.md` "Artefacts" table.

