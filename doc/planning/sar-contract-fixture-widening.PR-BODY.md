# PR body — APG-2546/sar-contract-fixture-widening

Title: **test: widen SAR contract fixture for vettor-training exemplar + originalReferral snapshot coverage**

Base: `main` · Head: `APG-2546/sar-contract-fixture-widening` @ `3969f93f` · Draft.

---

**Widen SAR contract-test fixture — vettor-training exemplar +
`originalReferral` snapshot coverage**

> ⚠️ **Expected snapshot diff is intentionally large.** ~14 HTML
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
- Also fixes `PersistenceHelper.createPerson(...)` to bind the four
  date-string parameters (`conditionalReleaseDate`,
  `paroleEligibilityDate`, `tariffExpiryDate`, `earliestReleaseDate`)
  as `LocalDate` rather than `String`, so Postgres accepts the
  now-populated values. See "Deviations from the working doc" below.
- Widens `SarContractIntegrationTest.setupTestData()`:
  - Populates ~14 previously-null fields on `person`, `pniResult`,
    and the primary referral with values verified against
    already-in-use literals elsewhere in the codebase.
  - Adds a second referral (the resolvable "original") + a second
    staff record (secondary POM), and points the primary referral
    at the original via `originalReferralId`.
- Regenerates both SAR contract snapshot goldens via
  `script/local-scripts/regenerate-sar-snapshots.sh`.

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
sheet) — `dd-notes-sweep.py` re-run on 2026-08-11, no drift since
doc was written. See §"Fields we are deliberately NOT populating"
for the list of DD-mandated exclusions preserved. APG-2546's UUID
scrub is verified preserved by an explicit grep check (zero UUIDs
in either golden post-widening).

### Testing

- `./gradlew ktlintCheck test` — **678 tests pass**, ktlint clean.
- Snapshot goldens regenerated via
  `script/local-scripts/regenerate-sar-snapshots.sh`; post-regen
  contract run passes without `SAR_GENERATE_ACTUAL`.
- Sample PDF page count: **4**. UUID leak check (`grep -E` UUID
  regex on both goldens) returns **zero matches on both**,
  confirming APG-2546's UUID scrub is preserved.
- Snapshot byte counts:
  - `sar-api-response.json`: 1762 → **2720 bytes** (14 populated
    fields + full `originalReferral` sub-object)
  - `sar-expected-render-result.html`: 11720 → **13681 bytes**
    (populated rows + new `<h4>Original referral</h4>` block)
  - `entity-schema.json`: **unchanged** (5836 bytes)

### Deviations from the working doc

Flagging as instructed by the doc's "no guessing" discipline:

1. **`SarContractIntegrationTest.kt` line-number references in the
   doc are stale.** The doc was written against a version of the
   test that included `createAuditRecord`, `createSexualOffenceDetails`,
   `createSelectedSexualOffenceDetails`, and
   `createReferralStatusHistory` calls plus their corresponding
   `AUDIT_RECORD_ID` / `SEXUAL_OFFENCE_ID` /
   `SELECTED_SEXUAL_OFFENCE_ID` / `REFERRAL_STATUS_HISTORY_ID`
   companion consts. Those don't exist on `main` at `baee4510`
   (the assumed starting point). The substantive plan is
   unaffected — the widened `createPerson` / `createPniResult` /
   `createReferral` / `createStaff` calls and the new companion
   consts landed as described; the new consts were placed after
   `PERSON_ID` (the actual last existing const) rather than after
   the non-existent `REFERRAL_STATUS_HISTORY_ID`.
2. **`SAR_GENERATE_ACTUAL=true` writes `.log` files under
   `src/test/resources/`, not `build/test-generated/sar-actual-*`.**
   Used the canonical `script/local-scripts/regenerate-sar-snapshots.sh`
   (which knows the real convention) instead of the raw gradle
   invocation + manual `cp` from the doc.
3. **`PersistenceHelper.createPerson` needed a bind-type fix.**
   The doc claimed ISO-8601 date-strings would bind fine to the
   Postgres `DATE` columns. They don't — JPA binds `String` as
   `varchar` and Postgres rejects the implicit cast (verified via
   `SQLGrammarException` on the first regen attempt). Fixed by
   parsing each of the four date strings to `LocalDate` at bind
   time inside the helper. Backwards-compatible (still accepts
   `String?`), and no existing caller passed a non-null value for
   these params, so no other tests are affected.
4. **`StaffRepository.findByPrisonNumber` needed an `ORDER BY`.**
   Surfaced on a second PDF regen: the JPQL query had no ordering,
   so Postgres returned the two joined staff rows in arbitrary
   order and the SAR-API-snapshot assertion flip-flopped between
   `[Doe, Bloggs]` and `[Bloggs, Doe]`. Fixed with `ORDER BY
   s.staffId` — a single-line JPQL addition. This is a **second
   `src/main/` deviation** vs. the doc's "zero product-code
   impact" claim; semantically it's a correctness hygiene fix
   (SAR responses shouldn't be non-deterministically ordered)
   rather than scope creep, and the widened fixture is the first
   case in the codebase that actually surfaced it.

None of these deviations required inventing a fixture value — every
populated value is still exactly as cited in the doc.

### Rollback

`git revert <sha>` — pure test-code change, no data / API /
migration surface. Zero product-code risk.

