# PR-5 — Strip internal ID fields from remaining SAR sections

> **Ticket:** APG-2546 • **Branch:** `APG-2546/strip-internal-ids`
> • **Est.:** 0.5–1 dev day • **Blocks:** nothing (independent of Q1/Q2)
> • **Depends on:** PR-4 merged (rebase off `main` after PR-4 lands)

## Purpose

Three DTO field-removals, all `id`s that either Roxanne red-flagged
directly or the DD's dev-note column tells us shouldn't render as
raw UUIDs:

1. **`SarPerson.id`** (spreadsheet row 111) — Roxanne 29.07
   red-flag "internal ref — should be a no".
2. **`SarOrganisation.id`** (row 105) — same red-flag pattern.
3. **`SarReferral.originalReferralId`** (row 165) — 10.07 dev note
   "pull referral data (if not already) do not add the uuid".
   **Confirmed 2026-08-04 pm in person by Roxanne** — fold into
   this PR (see DELIVERY-LOG "Roxanne in-person answers 2026-08-04
   pm"). The resolved `originalReferral` sub-block stays; only the
   raw UUID + its template row come out.

Currently `SarPerson.id`, `SarOrganisation.id`, and
`SarReferral.originalReferralId` populate every SAR response with
UUID strings that are meaningless to the subject.

There is a **fourth** related row on the spreadsheet — rows 127, 128,
131 flag `SarPniResult.pniResultId`, `referralId`,
`oasysAssessmentId` — but those fields are already absent from the
`SarPniResult` DTO (verified against
`SubjectAccessRequestService.kt` lines 284–296, 2026-08-03). No
code change needed for `SarPniResult`; this PR only touches
`SarPerson`, `SarOrganisation`, and `SarReferral`.

## `SarReferral.originalReferralId` — confirmed scope extension (2026-08-04 pm)

Confirmed by Roxanne in person on 2026-08-04 pm — see
DELIVERY-LOG "Roxanne in-person answers 2026-08-04 pm" entry.

**State on `main`:**

- We *do* pull the referral data — `SarReferral.originalReferral`
  is a populated sub-block (verified `SubjectAccessRequestServiceTest.kt`
  lines 277–305).
- We *also* still expose the raw UUID at
  `SarReferral.originalReferralId`, and the template renders it at
  `src/main/resources/sar_template.mustache:14`
  (`<td>Original referral ID</td><td>{{originalReferralId}}</td>`).

**Code changes** (fits alongside step 1 below):

- `SubjectAccessRequestService.kt` — delete
  `originalReferralId: UUID?` field from `SarReferral` data class
  (~line 245 area — grep for `originalReferralId` inside
  `data class SarReferral(...)`).
- `SubjectAccessRequestService.kt` — inside the `toSarReferral`
  mapper, delete the `originalReferralId = …` field assignment.
- `src/main/resources/sar_template.mustache:14` — delete the
  `<tr><td>Original referral ID</td><td>{{ optionalValue originalReferralId }}</td></tr>`
  row. The `originalReferral` block (rendered lower down in the
  template) is unaffected.
- `SubjectAccessRequestServiceTest.kt` — around lines 277 and 300,
  delete any `assertThat(referral.originalReferralId).isEqualTo(…)`
  / `.isNull()` assertions. Keep the assertions on the
  `originalReferral` sub-block (id, prison number etc.) — that's
  what the subject actually sees now.
- Sanity check with `grep -n "originalReferralId" src/main` after
  edits — expect zero hits inside `SubjectAccessRequestService.kt`
  and zero renders in the template.

The batch lookup that populates `originalReferral` (via
`referralRepository.findAllById(...)`) still needs the source
UUID from the entity layer — so the *entity* still has
`originalReferralId`; only the *DTO* + *template* stop exposing
it.

## Prerequisites for a fresh agent

Read `doc/planning/APG-2546-sar-field-removals.md` (§B, PR-5 detail).

You do **not** need Q2 answered to do this PR. Q2 concerns *adding*
`isNational` to `SarOrganisation`, which is out of scope for
APG-2546 regardless of her answer.

## Files to change

### 1. `src/main/kotlin/…/service/SubjectAccessRequestService.kt`

- Line 299 — delete `id: UUID?` from `SarPerson` data class.
- Inside `toSarPerson` mapper (~line 459) — delete `id = id` (or
  similar — grep for `id = ` inside that function) field assignment.
- Line 532 area — delete `id: String` from `SarOrganisation` data class.
- Inside `toSarOrganisation` mapper (~line 539) — delete
  `id = id.toString()` (or similar) field assignment.

Do a `grep -n "\.id" src/main/kotlin/…/service/SubjectAccessRequestService.kt`
sanity check after your edits — expect no leftover reads of
`sarPerson.id` or `sarOrganisation.id` inside the service.

### 2. `src/main/resources/sar_template.mustache`

- Line 125 — delete the `<tr><td>Person ID</td>…{{id}}…</tr>` row
  inside the `{{#person}}` block.
- **Organisation block (`{{#organisations}}` at lines 160–170) —
  no change.** Verified 2026-08-03: it renders only `Name`, not
  `Id`. The planning-doc PR-5 detail originally said to delete an
  organisation Id row, but that row doesn't exist. Do not add one
  just to delete it. This has been corrected in the planning doc.

### 3. Tests

**No test file changes expected.** The unit test asserts collection
sizes and a couple of surname / status fields; it does not probe
`SarPerson.id` or `SarOrganisation.id` directly. Snapshot
regeneration picks up the diff.

If a grep for `.id` in the test files surfaces an unexpected
assertion, delete it and mention in the PR description.

## Snapshot regeneration

```zsh
SAR_GENERATE_ACTUAL=true ./gradlew test \
  --tests '*SarContractIntegrationTest*' --rerun-tasks
./gradlew test --tests '*SarContractIntegrationTest*'
./gradlew test --tests '*SubjectAccessRequestServiceTest*'
```

Small snapshot diff — one field per person, one field per
organisation. `entity-schema.json` should not change.

## Verification checklist

```zsh
# Service — DTO and mappers
grep -n "\.id\b" src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt
# manually eyeball — any remaining .id reads should be domain-entity
# reads (e.g. inside a mapper feeding a different DTO) not SarPerson
# or SarOrganisation reads

# Template
grep -n "Person ID\|<td>Id</td>" src/main/resources/sar_template.mustache   # zero

./gradlew ktlintCheck test
```

## Non-obvious things

### 1. `SarPniResult` is already correct — comment tidy only

Roxanne's rows 127/128/131 look like real work but aren't. The
DTO shape has drifted from her spreadsheet's expectation in a
compatible direction — the fields she wants removed are already
gone. Flag this in the PR description so a reviewer looking at the
spreadsheet can tick those rows off without wondering why they
aren't touched.

If the DD spreadsheet is versioned and updatable, consider posting
a comment on rows 127/128/131 clarifying that the fields are
already absent. Out of scope for the code PR itself.

### 2. Watch for `id` used as a Mustache section variable

Mustache templates can render `{{id}}` in an implicit-`this` scope
inside `{{#person}}` or `{{#organisations}}` blocks. Grep both
blocks for `{{id}}` after your edits — leftover references will
render as blank strings, not compile errors. Snapshot HTML diff
should catch this; but a manual eyeball of the two block bodies is
cheap insurance.

### 3. Q2 (`isNational`) is out of scope

Q2 asks whether to *add* a field to `SarOrganisation`. Whichever
way Roxanne answers, it doesn't affect this PR. If she says "add",
spin up a new ticket after PR-5 lands — do not fold it into this
branch.

## PR description template

```
APG-2546: strip internal ID fields from remaining SAR sections

Removes SarPerson.id and SarOrganisation.id from the SAR payload
per Roxanne's DD spreadsheet rows 105 and 111 ("internal ref —
should be a no"). Also updates the person block of the mustache
template to drop the corresponding row.

Changes:
- Delete id: UUID? from SarPerson + toSarPerson mapper
- Delete id: String from SarOrganisation + toSarOrganisation mapper
- Delete the "Person ID" row from the {{#person}} block of
  sar_template.mustache
- Regenerate SAR contract snapshots

Not changed:
- {{#organisations}} template block — never rendered an Id row in
  the first place; no code drift from Roxanne's expectation there,
  the row is already absent.
- SarPniResult DTO — Roxanne's rows 127/128/131 flag
  pniResultId / referralId / oasysAssessmentId as internal, but
  those fields are already absent from the DTO. No action needed;
  planning doc §B has the audit trail.
- Test files — no assertion currently probes the two IDs directly;
  snapshot diff covers the change.

Out of scope:
- Q2 isNational on SarOrganisation — pending Roxanne's follow-up
  answer (see doc/planning/APG-2546/00-roxanne-followup.md). If she
  confirms "add", separate ticket.
```

## Definition of done

- [ ] Grep checks return zero hits for `.id` reads on the two SAR DTOs.
- [ ] `./gradlew ktlintCheck test` green.
- [ ] Snapshot diffs committed.
- [ ] PR description surfaces the `SarPniResult` no-op and the
      template-block no-op explicitly.
- [ ] Sample PDF page count noted in the artefacts table.

