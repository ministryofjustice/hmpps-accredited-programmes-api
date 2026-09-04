# PR-15 — Remove top-level `<h2>Courses</h2>` section (duplicate of inline `SarReferral.courseName`)

> **Ticket:** APG-2546 (round 2, second addendum) • **Branch:** `APG-2546/remove-top-level-courses`
> • **Est.:** ½ dev day
> • **Sequencing:** must merge **after PR-14** (**merged 2026-08-24 as `6d713186`** ✅). No parallel PRs to serialise against.
> • **Status:** ready for execution — line refs verified against `origin/main @ 6d713186` on 2026-08-25.

## Purpose

Roxanne's 2026-08-25 reply to the preprod PDF (`sar-<CRN>-2026-08-25.pdf`) delivered post-PR-14:

> "Everything is looking good, but the only thing is as you have moved the course name into the referral, its not required at the end of the report. It is currently repeating as a Courses section at the end of the report. Are you able to remove this duplicate?"

The trailing `<h2>Courses</h2>` block is the top-level deduped list of course names surfaced from `courseRepository.getSarCourses(prn)`. With PR-14 landed, every referral now shows its own `courseName` inline (below `Organisation name`), so the trailing list is verbatim duplication with the added downside of losing the per-referral context.

**Position in the round-2 story:** the natural completion of PR-11's inline-context pattern. Chronologically:

- **PR-10** — folded `organisation.name` into each `SarReferral`; kept `<h2>Organisation</h2>` list (Deborah's ask #4 was ambiguous on whether to remove the list — kept for safety).
- **PR-11** — removed the top-level `<h2>Staff</h2>` list once inline `primaryPomStaffSurname` / `secondaryPomStaffSurname` covered the same content.
- **PR-14** — folded `course.name` into each `SarReferral`.
- **PR-15 (this one)** — removes the top-level `<h2>Courses</h2>` list, closing the same loop PR-11 closed for staff.

Roxanne's ask is specifically about **courses**, not **organisations**. Do NOT scope-creep by also removing the `<h2>Organisation</h2>` block — she has explicitly reviewed and accepted that one already (2026-08-20 email confirming DD row 107 stays put). If she later asks to symmetrically remove the Organisation block too, that's a follow-on PR.

## Scoping call

Kept under APG-2546 (not a fresh round-3 ticket) — same call as PR-14. The OOS decision is about further OSAR/Branston asks; Roxanne's DD-review close-out is a distinct APG-2546 gate, and this is a one-block hygiene ask directly consequential to PR-14.

## Precedent — mirror PR-11 shape almost line for line

PR-11 removed the top-level `staff[]` list once inline `primaryPomStaffSurname` covered it. PR-15 is the same shape swapping `Staff` → `Courses`. Every decision below is by analogy to PR-11.

## Scope (verified against `origin/main` @ `6d713186`, 2026-08-25)

**Product code — `src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt`:**

| Line (`@ 6d713186`) | Change |
|---|---|
| L21 | Remove `import ...domain.repository.CourseRepository` (becomes orphan after ctor param drop) |
| L42 | Remove `private val courseRepository: CourseRepository,` ctor param |
| L129 | Remove `courses = courseRepository.getSarCourses(prn).toSarCourse(),` line inside `Content(...)` construction |
| L196 | Remove `val courses: List<SarCourse>,` field from nested `data class Content(...)` |
| L280-282 | Remove nested `data class SarCourse(val name: String,)` — grep-verified: only used inside this service file |
| L437-441 | Remove `private fun List<CourseEntity>.toSarCourse(): List<SarCourse>` extension — same |
| (line varies) | Remove `import ...domain.entity.create.CourseEntity` if the ext-fn drop left it orphan — verify with a post-edit `./gradlew ktlintCheck` (auto-caught) |

**Template — `src/main/resources/sar_template.mustache`:**

- Delete top-level `<h2>Courses</h2>` block — **lines 88-98** (block opens at 88, closes at 98 after the mustache `{{^courses}}<p>No Data Held</p>{{/courses}}` empty-state pair).

**Repositories — orphan-audit verified 2026-08-25 against `6d713186`:**

- `CourseRepository.getSarCourses` (currently defined at `CourseRepository.kt:46-54`) — **orphan-in-prod after this PR merges.** Only src/main caller on `6d713186` is `SubjectAccessRequestService.kt:129` (going away in this PR). Test callers in `SubjectAccessRequestServiceTest.kt` also going away with the mock cleanup below.
  - **Decision: DELETE the `getSarCourses` @Query method + its KDoc/comment.** Mirrors PR-11's decision on `StaffRepository.findByPrisonNumber` — orphan-in-prod, delete when the caller goes.
  - **Sibling methods on `CourseRepository` stay untouched** — `findAllByOrganisationId`, `findBuildingChoicesCourses`, `findAllByName`, etc. all have live prod callers (grep-verified: 13 references to `CourseRepository` across the codebase, all non-SAR).
  - **Repository-interface stays alive as-is** — not an empty-shell restoration. Only the specific `getSarCourses` method definition is removed from the interface.

**Test code — pre-verified 2026-08-25 against `6d713186`:**

`src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestServiceTest.kt`:

| Line (`@ 6d713186`) | Change |
|---|---|
| L15 | Remove `import ...domain.repository.CourseRepository` (becomes orphan after mock decl drop) |
| L44 | Remove `private val courseRepository: CourseRepository = mockk()` |
| L61 | Remove `courseRepository,` ctor arg passed to `SubjectAccessRequestService(...)` under test |
| L198-202 | Remove `every { courseRepository.getSarCourses(prn) } returns listOf(CourseEntityFactory().withName("Course Name").produce(),)` mock setup block |
| L262 | Remove `assertThat(courses.size).isEqualTo(1)` inside the `with(result!!.content as SubjectAccessRequestService.Content) { … }` block |
| L321-322 | Remove `val course = courses[0]` + `assertThat(course.name).isEqualTo("Course Name")` pair |
| L351 | Remove `verify { courseRepository.getSarCourses(prn) }` |

**Keep** `CourseEntityFactory` import (L24) — it's used at L106, L127, L147, L160 to seed `.withCourse(...)` on offerings so referral `courseName` resolution still works. Grep-verified: five in-file uses; only the L199 use goes away with the mock block.

`src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/integration/SubjectAccessRequestServiceIntegrationTest.kt` — **caught in nine-lens review 2026-08-25 (was missed in initial scoping)**:

| Line (`@ 6d713186`) | Change |
|---|---|
| L147 | Remove `assertThat(content.courses).hasSize(1)` |
| L178-180 | Remove the `with(content.courses[0]) { assertThat(name).isEqualTo("Course 1") }` block (three lines including braces) |

**Do NOT touch** `persistenceHelper.createCourse(...)` at L35-44 or `persistenceHelper.createCourseParticipation(...)` at L68-86 — both feed live inline paths (`SarReferral.courseName` via `offering.course.name`, and `SarCourseParticipation.courseName`) that stay after PR-15. Same rationale as `SarContractIntegrationTest.kt` (which has zero code changes — snapshot regen only).

`src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/integration/SarContractIntegrationTest.kt`:

- **No test-code change.** The seed `persistenceHelper.createCourse(...)` at L111 stays because it feeds the offering that supplies `SarReferral.courseName` (inline, added in PR-14) — still exercised.
- `expectedFlywaySchemaVersion = "144"` at L57 — **unchanged.** No migration this PR.
- Snapshot goldens regenerate: removes top-level `courses[]` array from JSON + `<h2>Courses</h2>` block from HTML. **Inline `courseName` on each referral stays** — sanity-check it's still present in both goldens.

`src/test/resources/sar/sar-api-response.json`:

- Snapshot regen — the JSON substring `"courses":[{"name":"Course 1"}],` disappears; every other key stays byte-identical to the PR-14 golden.

`src/test/resources/sar/sar-expected-render-result.html`:

- Snapshot regen — lines 291-297 (the `<h2>Courses</h2>` block plus the single course row) disappear; every other line stays byte-identical to the PR-14 golden.

## DD impact (Roxanne's spreadsheet)

**None on column-H answers.** Row 34 `course.name` on the DD is already `Yes` in the "In SAR API" column — post-PR-14, `SarReferral.courseName` carries that answer inline. Removing the top-level `<h2>Courses</h2>` list does not change any column-H flag; it just removes duplicate surfacing of the same field.

- **Do NOT run `dd-column-h-update.py`.** No delta to sweep.
- **Do NOT send Roxanne a fresh xlsx.** PDF-only close-out reply, matching PR-14 discipline.

## HAAR re-registration (post-merge)

**Required — mustache bytes change (block removed).** Same single-ping discipline as PR-14:

- **Channel:** `#haa-sar-functionality-change-request`
- **Envs:** SAR-preprod AND SAR-prod (single ping)
- **Message shape:** short pointer at the PR-15 merge SHA + one-line summary ("top-level `<h2>Courses</h2>` block removed, no other changes") + SHA-pinned permalink to the updated `sar_template.mustache`. Adapt the PR-14 draft in `handover/` or DELIVERY-LOG for the shape.
- **`deploy_preprod` hold:** yes — same discipline as PR-14. Wait for HAAR confirmation before approving.
- **`deploy_prod`:** approve after preprod is verified.

## PDF page count expectation

Currently 2 pages (post-PR-14 preprod). Removing the trailing Courses section removes ~4 rendered rows (one `<h2>` + one `<h3>` + one `<tr>` row × N courses, or the `No Data Held` line). Likely stays at 2 pages; possibly drops to 1 if the layout was on the boundary. **Both outcomes are correct** — flag whichever value in the merge outcome, don't panic.

## Verification checklist

- [ ] Read this doc end-to-end.
- [ ] `git fetch origin && git checkout origin/main && git checkout -b APG-2546/remove-top-level-courses` (or fresh clone).
- [ ] Confirm anchor: `git log --oneline -1` shows `6d713186` (or a descendant on `main`; re-anchor line refs if `main` has moved).
- [ ] Apply the changes above.
- [ ] Full-suite `./gradlew ktlintCheck test` — expect 678 tests green.
- [ ] `git diff --stat` — expect roughly 6 files touched, +0 / −40-ish lines (plus golden regen bytes).
- [ ] `grep -c 'SarCourse\|getSarCourses' src/main src/test -r` — expect **zero** hits after edit (excludes `SarCourseParticipation` which is a distinct type — spot-check the grep).
  - Concretely: `grep -rn 'SarCourse\b\|getSarCourses' src/` — expect zero.
- [ ] `grep -c '"courses"' src/test/resources/sar/sar-api-response.json` — expect **0** (was 1 pre-PR-15).
- [ ] `grep -c '<h2>Courses</h2>' src/` — expect **0**.
- [ ] `grep -c 'courseName' src/test/resources/sar/sar-api-response.json` — expect **≥2** (inline field on each referral post-PR-14 stays intact).
- [ ] `grep -c '<tr><td>Course name</td>' src/test/resources/sar/sar-expected-render-result.html` — expect **≥2** (inline row on each referral + `originalReferral` sub-block stays intact).
- [ ] UUID-leak grep on both goldens = 0.
- [ ] `entity-schema.json` unchanged (no JPA entity touched).
- [ ] `SarContractIntegrationTest.expectedFlywaySchemaVersion` unchanged from `144` (no migration).
- [ ] Test-harness (Option 2) PDF — record whether 1 or 2 pages, either is correct.
- [ ] **Nine-lens self-review** before PR out — same lenses as PR-8 through PR-14.
- [ ] PR title: `APG-2546: remove duplicate top-level Courses section (round-2 addendum)`.
- [ ] PR body: mirror PR-11's body shape (same-shape precedent), reference Roxanne's 2026-08-25 email + PR-14 as the inline-source-of-truth reason.

## Non-obvious things

1. **`SarCourse` vs `SarCourseParticipation` — do NOT confuse.** `SarCourse` (going away) is the trailing top-level list. `SarCourseParticipation` (stays) is the separate `<h2>Course participation</h2>` block earlier in the template (mustache line 41). PR-15 does NOT touch course participation — it's a different entity, different data source (`courseParticipationRepository`), different DD row, different consumer story.
2. **Inline `courseName` stays intact.** PR-14 added `SarReferral.courseName` and `SarOriginalReferral.courseName`. Both stay. PR-15 only removes the trailing `Content.courses[]` list and its template block.
3. **DD impact is nil.** Row 34 `course.name` = `Yes` regardless — the value is now sourced inline instead of from a trailing list. No sweep-script re-run, no fresh xlsx.
4. **Repository method deletion follows PR-11 precedent.** `CourseRepository.getSarCourses` is orphan-in-prod after this PR; delete it with the caller. Sibling methods on `CourseRepository` stay live.
5. **Ctor-param cleanup follows PR-8/PR-11 precedent.** `courseRepository` becomes fully orphaned inside `SubjectAccessRequestService.kt` after L129 removal — drop the ctor param + import. Mirror on the unit test's mock decl + ctor arg.
6. **HAAR ping required.** Template bytes change → single ping to `#haa-sar-functionality-change-request` for both envs at the new merge SHA. Hold `deploy_preprod` until confirmed. Same discipline as PR-14 (corrected 2026-08-21 shape). Do NOT wait for prod alarm — proactive ping, same message covers both envs.
7. **Rollback shape.** Single `git revert` of the PR-15 merge commit restores the pre-PR-15 template. That's forward/backward compatible with any deployed API (the removed field is purely additive-inverse). No coordinated rollback needed.

## Files to change (summary)

| File | Change | ~Lines |
|---|---|---|
| `SubjectAccessRequestService.kt` | Remove `courseRepository` ctor param + import + `courses = ...` Content line + `Content.courses` field + `SarCourse` data class + `.toSarCourse()` ext | −18 |
| `CourseRepository.kt` | Remove `getSarCourses` @Query method | −10 |
| `sar_template.mustache` | Remove `<h2>Courses</h2>` block (lines 88-98) | −11 |
| `SubjectAccessRequestServiceTest.kt` | Remove `courseRepository` mock/import/ctor-arg + `getSarCourses` stub + two `courses` assertions + `verify` | −12 |
| `SubjectAccessRequestServiceIntegrationTest.kt` | Remove `assertThat(content.courses).hasSize(1)` (L147) + `with(content.courses[0]) { ... }` block (L178-180) | −4 |
| `sar-api-response.json` (golden) | Regen — remove `"courses":[...]` substring | −1 (JSON) |
| `sar-expected-render-result.html` (golden) | Regen — remove `<h2>Courses</h2>` rendered block | −7 |

Total: net **−63-ish** lines (deletion-heavy), zero additions.

## Rollback

Single `git revert` of the PR-15 merge commit. Preprod SAR-service registration would then serve the pre-PR-15 template, which is still round-2 compliant (the block removal is purely subtractive-of-duplicate) — so a revert is safe even without an immediate re-registration.

