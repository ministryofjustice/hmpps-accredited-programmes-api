# PR-14 — fold `courseName` inline on `SarReferral`

**Status:** **PR [#1123](https://github.com/ministryofjustice/hmpps-accredited-programmes-api/pull/1123) OPEN** (2026-08-24) on branch `APG-2546/course-name-into-referral`, commit `ef376824`. Agent-reviewed twice (both LGTM). CI all green (Kotlin build, CodeQL, helm lint dev/preprod/prod). Mergeable. Awaiting team review. Anchored against `origin/main @ f84f41b2` (still tip on 2026-08-24 pm).

**Origin:** Roxanne's 2026-08-20 email follow-up after the DD-column-H update. She confirmed she's happy with the DD-side placement of `organisationName` (row 107 stays as canonical) and asked for `courseName` to also be surfaced inline on each referral. Verbatim ask:

> "as you have with the organisation name, would it also be possible for you to add the course name into the referral. I am pretty sure that the course names link to a referral (correct me if I am wrong). Our concern at the moment is the referral doesn't reference what it is for, and if there are multiple we feel like this may increase communication from offenders wanting clarification."

**Position in the round-2 story:** a natural completion of PR-10's inline-context pattern. PR-10 folded organisation into referral; PR-14 folds course name into referral. Together they address Deborah's 2026-08-13 ask #4 ("Add organisation field to the referral rather than list separately so it is in context") more thoroughly — the "in context" story now covers both the where (organisation) and the what (course).

**Scoping call:** kept under APG-2546 rather than spun as a new round-3 ticket per the OOS decision. The OOS decision was specifically about OSAR/Branston asks after the PR-13 sample PDF; Roxanne's DD-review side has always been a distinct APG-2546 close-out condition, and this ask is a one-field consistency completion of PR-10's pattern.

---

## Scope

**One-line addition on each of five artefacts.** No new query, no new batch resolution — the field is already eager-loaded on `ReferralEntity.offering.course.name` and `SarOriginalReferral.courseName` already reads exactly this path (`SubjectAccessRequestService.kt:262` on `f84f41b2`).

### 1. `SarReferral` data class

**File:** `src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt`

Anchor (on `f84f41b2`): `data class SarReferral(...)` currently at L159. Add `courseName: String?` immediately before `organisationName` so the reading order matches the mustache row order (Course name is rendered above Organisation name — nope actually mirror the template order — see step 3).

Suggested position (immediately after `secondaryPomStaffSurname`, right before the two other resolved-name fields, or right before `organisationName` — whichever reads best; the exact position doesn't affect the JSON output because Kotlin data classes serialise by declaration order, but the goldens will regenerate accordingly):

```kotlin
data class SarReferral(
  val oasysConfirmed: Boolean,
  val statusCode: String?,
  val hasReviewedProgrammeHistory: Boolean?,
  val additionalInformation: String?,
  val submittedOn: LocalDateTime?,
  val primaryPomStaffSurname: String?,
  val secondaryPomStaffSurname: String?,
  val referrerOverrideReason: String?,
  val referrerUsername: String?,
  val hasLdc: Boolean?,
  val hasLdcBeenOverriddenByProgrammeTeam: Boolean,
  val hasReviewedAdditionalInformation: Boolean?,
  val courseName: String?,           // NEW — mirrors organisationName pattern; source ReferralEntity.offering.course.name
  val organisationName: String?,
  val originalReferral: SarOriginalReferral?,
)
```

### 2. `toSarReferral` mapper

**File:** same file.

Anchor: `private fun List<ReferralEntity>.toSarReferral(...)` at ~L233. Add one line inside the `SarReferral(...)` constructor call:

```kotlin
SarReferral(
  // ...existing fields...
  courseName = it.offering?.course?.name,           // NEW — same source SarOriginalReferral already uses
  organisationName = it.offering?.organisationId?.let { orgCode -> organisationNamesByCode[orgCode] },
  originalReferral = it.originalReferralId?.let { originalId ->
    originalsById[originalId]?.toSarOriginalReferral(surnames, organisationNamesByCode)
  },
)
```

No new mapper signature change — the existing signature `(staffSurnames, originalsById, organisationNamesByCode)` is sufficient. No `courseNamesByOfferingId` map needed because the course is already loaded on the referral via eager fetch.

**Why no `resolveCourseNames` batch pre-fetch:** unlike organisation (which requires a per-code lookup via `organisationRepository.findAllByCodeIn` because `offering.organisationId` is a code, not a JPA relation), `offering.course` is a `@ManyToOne`-eager-loaded association on `OfferingEntity`. Confirm via `git show origin/main:src/main/kotlin/.../domain/entity/OfferingEntity.kt` before finalising; if the fetch is LAZY, then either (a) add a `FetchType.EAGER` on the association (cheap; only affects SAR fetch since Hibernate second-level caches keep it warm for other callers) or (b) add a `resolveCourseNames` batch pre-fetch mirroring `organisationNamesByCode`. Option (a) is preferable because it mirrors the existing `SarOriginalReferral.courseName` resolution which already relies on the eager path.

### 3. `sar_template.mustache`

**File:** `src/main/resources/sar_template.mustache`

Anchor: the referral summary-list table currently renders `<tr>Organisation name</tr>` (this row was added by PR-10). Add a `<tr>Course name</tr>` row immediately above or below it — reviewer preference; suggest **above** so the reading order is `[Course name][Organisation name]` (the what before the where), mirroring the natural spoken order ("Building Better Relationships at HMP Isis" reads more naturally than "HMP Isis, Building Better Relationships"). Apply the same to the `originalReferral` block below — `originalReferral.courseName` is already rendered on the template today; just confirm ordering is consistent.

Suggested addition:

```mustache
<tr><td>Course name</td><td>{{ optionalValue courseName }}</td></tr>
<tr><td>Organisation name</td><td>{{ optionalValue organisationName }}</td></tr>
```

Use the same `optionalValue` helper as sibling rows.

### 4. `SubjectAccessRequestServiceTest.kt`

**File:** `src/test/kotlin/.../service/SubjectAccessRequestServiceTest.kt`

Add:
- One `assertThat(referral.courseName).isEqualTo(...)` inside the existing `with(content.referrals[0]) { ... }` block, mirroring the existing `organisationName` assertion.
- (No new mock setup needed — the offering→course chain is already stubbed for the `SarOriginalReferral.courseName` assertion; the same test data drives both.)

### 5. Snapshot goldens

**Files:**
- `src/test/resources/sar/sar-api-response.json`
- `src/test/resources/sar/sar-expected-render-result.html`

Regenerate via `./script/local-scripts/regenerate-sar-snapshots.sh` (Docker Desktop must be running — Testcontainers-backed regen).

Expect:
- **JSON**: `courseName` key present on both referrals in the golden (PR-12 seeded a second BXI/HMP Brixton offering wired to the withdrawn referral). Reference `PR-12` timeline entry for fixture shape.
- **HTML**: new `<tr>Course name</tr>` row rendered on each referral. Also on the `originalReferral` block if it wasn't already there (verify — it should already be there via `SarOriginalReferral.courseName` today).
- **Page count**: expected to stay at 2 pages (Option 2 test-harness). One extra row per referral is negligible.
- **UUID-leak grep**: still 0.

### 6. DD update (post-merge)

Roxanne asked us to also send her the updated DD once PR-14 is on `main`. Add a single delta to `dd-column-h-update.py`:

- Option A (preferred): add a new row entry noting `course.name` is now surfaced on the referral (row 34 on the DD, entity `course`, element `name`, currently `M=Yes, SAR=Yes, API=Yes` — no change needed since it's already flagged as in-SAR-API via the top-level `Content.courses[]` list; the addition of an inline `SarReferral.courseName` doesn't change the column-H answer for `course.name`).
- Option B (if Roxanne prefers a dedicated row): add a new row on the `referral` entity — `referral.course_name` = Yes. This would require inserting a new row into the sheet, which is fiddly to automate cleanly through openpyxl. Simpler to describe it in the follow-up email and let Roxanne slot it in on her master.

Recommend Option A + one-line note in the follow-up email. If Roxanne prefers Option B, adding a new row post-hoc is a 5-minute change to the sweep script — spin at that point.

---

## Verification checklist

- [ ] Read this doc end-to-end.
- [ ] `git fetch origin && git checkout origin/main && git checkout -b APG-2546/course-name-into-referral` (or fresh clone).
- [ ] Confirm anchor: `git log --oneline -1` shows `f84f41b2` (or a descendant on `main`; re-anchor line refs if `main` has moved).
- [ ] Verify `offering.course` fetch type on `OfferingEntity` (see step 2 "Why no batch pre-fetch").
- [ ] Apply the 5 changes (data class, mapper, mustache, unit test assertion, snapshot regen).
- [ ] Full-suite `./gradlew ktlintCheck test` — expect 678 tests green.
- [ ] `git diff --stat` — expect roughly 5 files touched, +5–10 / −0 lines (excluding golden diffs).
- [ ] `grep -c 'courseName' src/test/resources/sar/sar-api-response.json` — expect **2** (one per referral) + additional hits on `originalReferral.courseName` if the sub-block was seeded.
- [ ] UUID-leak grep on both goldens = 0.
- [ ] `entity-schema.json` unchanged (no JPA entity touched).
- [ ] Test-harness (Option 2) PDF still 2 pages.
- [ ] Nine-lens self-review before PR out — same lenses as PR-8 through PR-12.
- [ ] PR title: `APG-2546: fold courseName into referral (round-2 addendum)`.
- [ ] PR body: mirror PR-10's body shape, reference Roxanne's 2026-08-20 email.

---

## Non-obvious things

1. **Empty-shell precedent does NOT apply.** No repository is orphaned by this change — it's purely an additive field. Don't invent any deletion logic.
2. **Data-class field position matters for JSON goldens but not for API compatibility.** Kotlin serialises data class fields in declaration order; the goldens will regenerate accordingly, so no consumer breakage — but choose the declaration position deliberately (recommend adjacent to `organisationName` for readability). If reviewer wants a different position, easy re-order.
3. **`SarOriginalReferral.courseName` already exists and works.** This is the reference implementation; treat it as the canonical shape. Do NOT re-implement resolution differently on the parent.
4. **`courseName` naming vs `otherCourseName`.** `SarCourseParticipation` has `otherCourseName` (a free-text override); `SarOriginalReferral` uses `courseName` (the canonical resolved name). Match `SarOriginalReferral` — don't accidentally overload `otherCourseName` semantics.
5. **PDF page count movement:** the last time we added an inline field (PR-10, `organisationName`), page count didn't move. Same expected here. If it does move (e.g. 2 → 3 pages), that's fine but worth a paper-trail line in the merge outcome — probably won't.
6. **Preprod SAR-service template re-registration on merge:** yes, needed. The mustache template file changed (new `<tr>` row), so Cameron's team's registered revision falls behind again. This is the same shape as the round-1 → round-2 re-registration Dave Llewellyn did today for HAAR-5939 (preprod). Draft a new short Slack ping in `handover/` at PR-14 merge time — same channel (`#haa-sar-functionality-change-request`), same shape, pointer at the new merge SHA. Note in advance: Dave already knows the shape, so this second registration should be quicker than the first.

7. **Prod SAR-service template re-registration on merge — critical, and PR-14 is a real byte-changing case.** In the round-2 timeline, the 2026-08-21 HAAR-team alert about SAR-prod mismatch turned out to be a SHA-pointer housekeeping issue on byte-identical mustache — Dave had actually registered both preprod AND prod on Thursday 2026-08-20 (16:06 BST) via Deborah's in-thread ask, and the alert fired only because the registered SHA (`99264496`) differed from the deployed SHA (`f84f41b2`) even though the template bytes were identical. See DELIVERY-LOG's 2026-08-21 entry (with 2026-08-24 correction) for the full paper trail. **PR-14 is DIFFERENT** — this one genuinely changes template bytes (new `<tr>` row), so both preprod AND prod SAR-service re-registrations are actually required, not just SHA-pointer bumps. When PR-14 merges to `main` (call this SHA `X`), ping HAAR team in `#haa-sar-functionality-change-request` asking for registration on **both** SAR-preprod AND SAR-prod at `X` in a single message. Where possible, hold the CircleCI `deploy_preprod` / `deploy_prod` approvals until HAAR confirms — or accept a brief cosmetic mismatch during the overlap window (the template + API are forward/backward compatible on the new `courseName` row: pre-PR-14 template renders fine against post-PR-14 API just missing the row; reverse is `optionalValue`-blank; not a data-integrity issue).

---

## Files to change (summary)

| File | Change | ~Lines |
|---|---|---|
| `SubjectAccessRequestService.kt` | Add `courseName: String?` to `SarReferral`; add `courseName = it.offering?.course?.name` in `toSarReferral` mapper | +2 |
| `sar_template.mustache` | Add `<tr>Course name</tr>` row inside the referral summary-list | +1 |
| `SubjectAccessRequestServiceTest.kt` | Add one assertion on the new field | +1 |
| `sar-api-response.json` (golden) | Regen — new `courseName` key on each referral | +2 |
| `sar-expected-render-result.html` (golden) | Regen — new `<tr>` row per referral | +2–4 |

Total ~10-line diff (excluding golden regen bytes).

---

## Rollback

Single `git revert` of the PR-14 merge commit. Preprod SAR-service registration would then serve the pre-PR-14 template, which is still round-2 compliant (the row addition is purely additive) — so a revert is safe even without an immediate re-registration.

