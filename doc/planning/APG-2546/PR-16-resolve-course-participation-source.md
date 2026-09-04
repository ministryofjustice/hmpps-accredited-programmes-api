# PR-16 — Resolve `SarCourseParticipation.source` via staff-surname lookup (round-2 addendum)

> **Ticket:** APG-2546 (round 2, third addendum) • **Branch:** `APG-2546/resolve-course-participation-source`
> • **Est.:** ½ dev day
> • **Sequencing:** must merge **after PR-15** (**merged 2026-08-27 as `10d794d9`** ✅). No parallel PRs to serialise against.
> • **Status:** ready for execution — all anchors verified against `origin/main @ 10d794d9` on 2026-09-01.

## Purpose

Roxanne's 2026-09-01 reply to the post-PR-15 preprod PDF:

> "Thanks for this. I have had a check and there is just one issue. In the Course Participation section the source is coming through as what looks like an ID."
>
> "source = LQU64X on pdf in downloads folder"

`LQU64X` is a NOMIS/DPS username — a 6-char alphanumeric code. It's showing up under **Course participation → Source** on the SAR PDF because `SarCourseParticipation.source` is a raw pass-through of `CourseParticipationEntity.source`, and that column has **two** write paths in production:

1. **Auto-derived** — when a course participation is auto-created from a referral, `CourseParticipationService.buildCourseParticipation` (`src/main/kotlin/.../service/CourseParticipationService.kt:90`) sets `source = referralEntity.referrer.username`. `updateExistingParticipation` at `L68` does the same. This is what Roxanne is seeing — the referrer's NOMIS username code.
2. **Free-text via UI** — `CourseParticipationCreate.source` / `CourseParticipationUpdate.source` are pass-through strings a user typed (e.g. `"OASys"`, `"self-declared"`, `"referral form"`). These are meaningful human-readable text.

The SAR view surfaces `source` as a raw string with no lookup, so path 1 leaks a NOMIS code onto the PDF. Every other username-typed field on the SAR view **is** resolved via the batch-prefetched `StaffSurnames.forUsername(...)` helper — `createdByUser`, `updatedByUser`, `referrerUsername`, POM staff, audit usernames, status-history usernames. `source` is the sole outlier.

## Fix approach

Two-line change:

1. **Widen the batch pre-fetch** in `resolveStaffSurnames` (`SubjectAccessRequestService.kt`) to include `participation.source` values alongside `createdByUsername` and `lastModifiedByUsername`.
2. **Resolve with fallback** in the `toSarParticipation` mapper: `source = surnames.forUsername(it.source) ?: it.source`. Fallback preserves path-2 free-text values that don't match any staff row.

This matches the existing project pattern applied at eight other username-resolution sites in the same file — the fix is bringing `source` into line with the SAR service's own convention.

## Precedent — mirror of the eight existing forUsername sites in `SubjectAccessRequestService.kt`

Every other username surfaced on the SAR is resolved through `surnames.forUsername(...)`. On `origin/main @ 10d794d9`:

- L204 `createdByUser = surnames.forUsername(it.createdByUsername)`
- L206 `updatedByUser = surnames.forUsername(it.lastModifiedByUsername)`
- Plus six more sites across referrals, POM staff, audit, status history.

Every one of those pre-fetch call sites is already contributing usernames into `resolveStaffSurnames.buildSet { ... }` at L110-115. This PR adds one more `?.let(::add)` line to the same `buildSet` and one `forUsername(...) ?: raw` swap in the mapper.

The fallback pattern (`forUsername(x) ?: x`) is **new** — the existing sites don't need it because they resolve from columns that are guaranteed to be usernames. `source` is dual-purpose, so the fallback is required to preserve the free-text case. This is the single new pattern introduced by PR-16.

## Scope (verified against `origin/main` @ `10d794d9`, 2026-09-01)

**Product code — `src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt`:**

| Line (`@ 10d794d9`) | Change |
|---|---|
| L110-115 | Inside `resolveStaffSurnames.buildSet { … }`, add `it.source?.let(::add)` alongside the existing `add(it.createdByUsername)` / `it.lastModifiedByUsername?.let(::add)` lines in the `participations.forEach { … }` block. |
| L209 | Change `source = it.source,` → `source = surnames.forUsername(it.source) ?: it.source,` |

Nothing else in `SubjectAccessRequestService.kt` changes. No new imports. No signature change on `resolveStaffSurnames` (still takes referrals + participations). No change to the `SarCourseParticipation` data class (`val source: String?` at L191 stays as-is — the type doesn't change, only the value population).

**Template — `src/main/resources/sar_template.mustache`:**

- **No change.** The `<tr><td>Source</td><td>{{ optionalValue source }}</td></tr>` row inside the `courseParticipation` block already renders whatever value the API sends. Fix is data-only. **Zero mustache bytes change ⇒ no HAAR re-registration required** (see HAAR section below).

**Repositories:**

- **No change.** No new repository method, no orphan cleanup. `StaffLookupService.resolveSurnamesByUsername` already handles arbitrary strings — null/blank filtered, non-matches simply absent from the returned map (`StaffLookupService.kt:45-60`). Adding a non-username string like `"OASys"` to the input `Collection<String?>` costs one entry in the `IN`-list of the batch query and yields zero matched rows — safe, well-defined behaviour.

**Test code — pre-verified 2026-09-01 against `10d794d9`:**

`src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestServiceTest.kt` (229 lines):

| Line (`@ 10d794d9`) | Change |
|---|---|
| L223 (append) | Add `assertThat(participation.source).isEqualTo("River")` after the `updatedByUser` assertion inside the existing `with(content) { … }` block. |

Why "River"? The default stub at L48-56 resolves every non-blank username to `"River"`. The participation setup at L141 uses `.withSource("SOURCE")`. Post-fix, `"SOURCE"` is added to the batch prefetch → stub returns `{"SOURCE" -> "River"}` → mapper resolves `source` to `"River"`. This assertion proves **path 1** (username → surname resolution) works.

**Add a second test** to prove **path 2** (free-text fallback). Insert after the existing `should return filtered and mapped prison content` test at ~L228:

```kotlin
@Test
fun `should preserve free-text source when it does not match any staff row`() {
  // Given: participation seeded with a non-username source, and the batch
  // lookup returns an empty map for that specific value.
  val prn = "A1234BC"
  every { staffLookupService.resolveSurnamesByUsername(any()) } answers {
    val usernames = firstArg<Collection<String?>>()
    usernames.asSequence()
      .filterNotNull()
      .filter { it.isNotBlank() && it != "OASys" }   // "OASys" deliberately unresolvable
      .toSet()
      .associateWith { "River" }
  }
  // ... reuse the same referral+participation seed as the main test, but with
  //     .withSource("OASys") on the participation.
  //
  // Then: participation.source == "OASys" (raw fallback preserved).
}
```

The test-case body is scoped tightly to `source` — reuse the existing referral/participation factories with a single `.withSource("OASys")` override. Expect this test to be ~30 lines including setup.

`src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/integration/SubjectAccessRequestServiceIntegrationTest.kt` (116 lines):

| Line (`@ 10d794d9`) | Change |
|---|---|
| L113 (append) | Add `assertThat(source).isEqualTo("Source")` inside the existing `with(content.courseParticipation[0]) { … }` block. |

Why `"Source"` (unchanged)? The integration test seeds `source = "Source"` at L69 and one staff row with `username = "TEST_USER"`. Post-fix, batch lookup asks the real testcontainer DB for `{TEST_USER, Source}` → DB returns only `{TEST_USER: Doe}` → `forUsername("Source")` returns null → fallback yields `"Source"`. This assertion proves the fallback works end-to-end against a real database, not just a mock.

`src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/integration/SarContractIntegrationTest.kt`:

- **No test-code change.** The seed `source = "Source"` at L150 stays as-is. The staff row seeded at L216 uses `username = "TEST_USER"` — no staff row matches `"Source"`, so post-fix behaviour: batch lookup returns `{TEST_USER → Doe}`, `forUsername("Source")` → null, fallback → `"Source"`. **Goldens byte-identical.** (Verified: no other seed in the contract test sets a participation source to a staff-matched username.)
- `expectedFlywaySchemaVersion = "145"` — **unchanged.** No migration this PR. (Note: PR-15 planning doc said "144" as the anchor; main was actually at `145` when PR-15 ran — flagged in PR-15 delivery report. `145` is the correct current anchor. No change needed here.)

`src/test/resources/sar/sar-api-response.json`:

- **Byte-identical after regen.** `"source": "Source"` at `courseParticipation[0].source` stays the same value (fallback path). Sanity-check with `git diff --stat` on this file post-regen: expect 0 changes.

`src/test/resources/sar/sar-expected-render-result.html`:

- **Byte-identical after regen.** L291 `<tr><td>Source</td><td>Source</td></tr>` stays the same. Same sanity-check.

`src/test/resources/sar/entity-schema.json`:

- **Byte-identical.** JPA-derived, no JPA entity touched.

## DD impact (Roxanne's spreadsheet)

**None on column-H answers.** The relevant DD row (`courseParticipation.source`) is already `Yes` in the "In SAR API" column. This PR does not remove the field — it just resolves its display value from a code to a surname when the underlying value happens to be a username.

- **Do NOT run `dd-column-h-update.py`.** No delta to sweep.
- **Do NOT send Roxanne a fresh xlsx.** PDF-only close-out reply, matching PR-14/PR-15 discipline.

## HAAR re-registration (post-merge)

**NOT required — mustache bytes are unchanged.** The template's `<tr><td>Source</td><td>{{ optionalValue source }}</td></tr>` row is untouched; only the API's value for that field changes. HAAR's registration is a byte-hash of the mustache template, not a hash of the rendered output, so a data-only change does not trigger a re-registration.

- **No Slack ping to `#haa-sar-functionality-change-request` needed.**
- **No `deploy_preprod` hold on HAAR side.** Approve preprod immediately after CI green + review.

**BUT: still verify preprod PDF before Roxanne close-out reply.** The whole point is that the value on the PDF changes. Regenerate the preprod PDF against a rich CRN and eyeball the `Source` row in the Course participation section: should now show a surname (like "Doe") instead of a code (like "LQU64X").

## PDF page count expectation

Currently 2 pages (post-PR-15 preprod). This PR changes one cell's value in one table row. **Zero row-count delta.** Page count stays at 2. If it doesn't, something else is going on — investigate before shipping.

## Verification checklist

- [ ] Read this doc end-to-end.
- [ ] `git fetch origin && git checkout origin/main && git checkout -b APG-2546/resolve-course-participation-source` (or fresh clone).
- [ ] Confirm anchor: `git log --oneline -1` shows `10d794d9` (or a descendant on `main`; re-anchor line refs if `main` has moved).
- [ ] Apply the two product-code changes.
- [ ] Apply the two test additions (unit + integration) and the new free-text-fallback unit test.
- [ ] Full-suite `./gradlew ktlintCheck test` — expect the full test suite green (currently 678 tests; +1 from the new unit test = 679 expected).
- [ ] `git diff --stat` — expect roughly 4 files touched, net **+~35 / −2** lines (product code near-zero, test additions dominate).
- [ ] `grep -c 'surnames.forUsername(it.source)' src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt` — expect **1** (the new mapper line).
- [ ] `grep -c 'it.source?.let(::add)' src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt` — expect **1** (the new prefetch seed line).
- [ ] Regenerate goldens via `./script/local-scripts/regenerate-sar-snapshots.sh` (Docker Desktop must be running).
- [ ] `git diff src/test/resources/sar/sar-api-response.json src/test/resources/sar/sar-expected-render-result.html src/test/resources/sar/entity-schema.json` — expect **byte-identical** (zero diff).
- [ ] UUID-leak grep on both goldens = 0 (unchanged from PR-15).
- [ ] `SarContractIntegrationTest.expectedFlywaySchemaVersion` unchanged from `145`.
- [ ] Test-harness (Option 2) PDF — expect 2 pages (unchanged from PR-15).
- [ ] **Nine-lens self-review** before PR out — same lenses as PR-8 through PR-15.
- [ ] PR title: `APG-2546: resolve course participation source via staff-surname lookup (round-2 addendum)`.
- [ ] PR body: reference Roxanne's 2026-09-01 email + PR-15 as the trigger; note **no HAAR ping required** (mustache bytes unchanged); note goldens byte-identical; note test-count delta (+1 unit test).

## Non-obvious things (hard guardrails)

1. **Fallback direction matters.** Must be `surnames.forUsername(it.source) ?: it.source` — resolve first, fallback to raw. Reversed order (`it.source ?: surnames.forUsername(it.source)`) is a no-op because `it.source` is only null when the column is null. Do not flip this.
2. **Do NOT change the `SarCourseParticipation` data class type.** `val source: String?` stays. Only the value population changes.
3. **Do NOT touch `CourseParticipationService.kt`.** The auto-derived `source = referral.referrer.username` write path is correct product behaviour outside the SAR context (analytics, internal CRUD APIs, participation reconciliation all consume the raw column). The SAR is the only surface where this needs display-side resolution. Do NOT try to "fix" the write side.
4. **Do NOT change the batch-lookup contract.** `StaffLookupService.resolveSurnamesByUsername` already filters nulls/blanks and tolerates non-matching inputs. Adding non-username strings ("OASys", "Source", etc.) to the input set is safe and returns them absent from the map — the mapper's fallback then preserves the raw string. No `try { ... } catch` needed, no new method, no signature change.
5. **Do NOT extend the SarContract seed to add a resolvable source.** Tempting: seed a participation with `source = "TEST_USER"` to demonstrate the resolution path in the golden. Don't. Goldens byte-identical (fallback path) is a stronger regression-safety property than proving both paths in the same golden. The two dedicated unit tests + one integration assertion cover both paths adequately.
6. **Golden byte-identity is a load-bearing assertion.** If the golden JSON or HTML diff after regen, something is wrong. The most likely cause is the seed accidentally matching a staff username — check for that first. Second most likely: the fallback direction is flipped. Third: the batch prefetch didn't include `it.source`.
7. **No mustache change ⇒ no HAAR ping.** This is different from PR-10/PR-11/PR-14/PR-15 which all touched the template. Skip the HAAR-team draft entirely. Do NOT hold `deploy_preprod`.
8. **Rollback shape.** Single `git revert` of the PR-16 merge commit restores the pass-through behaviour. No coordination needed with anyone — value change only.
9. **Free-text case is not hypothetical.** grep of production data (via `CourseParticipationController` audit logs) shows `source` values like `"OASys"`, `"self-declared"`, `"OASys assessment"`, `"paper referral"` in real records. Preserving these is a hard requirement, not a nice-to-have. That's why the fallback is essential.
10. **Two username-typed columns (`createdByUsername`, `lastModifiedByUsername`) are ALREADY prefetched.** Grep the `resolveStaffSurnames` function on `10d794d9`: L112-114 already adds them. This PR only adds `it.source?.let(::add)` as a third line inside the same `participations.forEach { ... }` block. Do NOT duplicate the two existing lines.
11. **`source` column is Postgres `text` (unbounded)** — see `V12__create_course_participation_history.sql:8`. Adding arbitrary free-text values to the batch prefetch's `IN (...)` list is safe at SAR-request scale (per-prisoner, typically ≤20 participation rows → ≤20 extra IN-list entries). The `staff.username IN (...)` query returns empty rows for non-matching values, adding a few bytes to the SQL parameter payload. No perf concern.
12. **Free-text collision with a real staff username is an accepted tradeoff.** In the (exceedingly rare) case that a user types free text like `"OASys"` and there happens to exist a staff row with `username = "OASys"`, we'd resolve `source` to that staff's surname on the SAR — which is technically wrong for that record. Collision-detection is out of scope for PR-16. The behaviour is: "if free text collides with a real username, we resolve." Document this in the PR body under "known limitations" so reviewers see it flagged.

## Session hygiene (learned from earlier round-2 PRs)

- **Do NOT use inline zsh heredocs for multi-line commit messages.** Backticks, em-dashes, and `#` inside a heredoc silently corrupt the commit body (bit us on planning-branch commits and PR-15 delivery). Write the message to `/tmp/apg2546-pr-16-msg.txt` via the workspace file-write tool, then `git commit -F /tmp/apg2546-pr-16-msg.txt`.
- **Docker Desktop must be running before `./script/local-scripts/regenerate-sar-snapshots.sh`.** The regen invokes Testcontainers; without Docker Desktop it fails with `Cannot connect to the Docker daemon`.
- **`read_file` output can be stale after a `git checkout`.** If your reader's line numbers disagree with `sed -n '...' file` or `git show <sha>:file`, trust the terminal — the read cache lags checkout.
- **`.snyk` is auto-regenerated by the gradle plugin at build time and is NOT in `.gitignore`** — `git add -A` after a build will pick it up. If your commit shows `.snyk` in the diff, drop it with `git rm --cached .snyk && git commit --amend`. Same trap PR-15 tripped on. (Separate hygiene PR to add `.snyk` to `.gitignore` is queued in the DELIVERY-LOG surprises list — do NOT bundle it into PR-16.)
- **`git fetch origin` may time out** — the JetBrains-IU workspace's SSH-to-github is flaky. Fallback: `git fetch origin --no-tags 2>&1 | tail -5`, or use HTTPS remote. `git show origin/main:<path>` still works against the last-fetched cache.
- **Verify `git log --oneline -1 origin/main` shows `10d794d9`** (or a descendant) before you begin. If `origin/main` has moved past `10d794d9`, re-verify each line ref in the "Scope" table via `git show origin/main:<path> | sed -n '<N-2>,<N+2>p'` — anchor line numbers may shift.

## Files to change (summary)

| File | Change | ~Lines |
|---|---|---|
| `SubjectAccessRequestService.kt` | Add `it.source?.let(::add)` to prefetch (L110-115 block) + swap `source = it.source` → `source = surnames.forUsername(it.source) ?: it.source` (L209) | +1 / ~1 modified |
| `SubjectAccessRequestServiceTest.kt` | Add one `assertThat(participation.source).isEqualTo("River")` assertion + one new `@Test` for the free-text fallback path | +~35 |
| `SubjectAccessRequestServiceIntegrationTest.kt` | Add one `assertThat(source).isEqualTo("Source")` assertion inside the existing `with(...)` block | +1 |
| `sar-api-response.json` (golden) | Regen — expect **byte-identical** | 0 |
| `sar-expected-render-result.html` (golden) | Regen — expect **byte-identical** | 0 |
| `entity-schema.json` (golden) | Regen — expect **byte-identical** | 0 |

Total: net **+~37** lines (test-dominant), 1 product-code line modified, 1 product-code line added.

## Rollback

Single `git revert` of the PR-16 merge commit. Restores pass-through of `source`. No template registration to reset (mustache unchanged), no schema migration, no cross-service coordination. Rollback is fully local to the API service.

## Post-merge close-out

1. Wait for CI green + team review + merge.
2. Approve `deploy_preprod` immediately (no HAAR gate).
3. Verify preprod pod picks up new image (image tag contains merge SHA).
4. Regenerate the preprod PDF against the same rich CRN Roxanne referenced (the one that yielded `source = LQU64X`).
5. Eyeball the PDF: `Source` under Course participation should now show a surname (or the raw string, if source was free text originally).
6. Reply to Roxanne with the fresh PDF attached. Draft goes in `handover/roxanne-followup-source-resolution-reply-draft.md` (write it inline in the tracking chat when close-out is imminent — don't scope it into this doc).
7. Approve `deploy_prod` after preprod verified.

