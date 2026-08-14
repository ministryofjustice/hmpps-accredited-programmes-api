# PR-12 — Round-2 code hygiene + test tidy

> **Ticket:** APG-2546 (round 2) • **Branch:** `APG-2546/round-2-hygiene-tidy`
> • **Est.:** ½ dev day • **Depends on:** PR-8 + PR-9 + PR-10 + PR-11 merged
> • **Blocks:** PR-13 (docs handover expects a clean state)
> • **Status:** skeleton — expand before execution

## Purpose

Standalone hygiene pass after the four round-2 removals land. Same
rationale as the PR #1115 follow-on hygiene sweep in round 1 — a
distinct "everything's-clean-now" review before the handover PDF is
generated is cheap signal for reviewers and Branston.

Scope is deliberately narrow: nothing PR-8/9/10/11 should have caught
themselves. This PR only exists to catch **cross-PR interactions**
that surface only when everything is together.

## What this PR does (and does not do)

### Does

- **Final orphan-query audit.** Each of PR-8/10/11 removed the SAR
  call site for its section's repository query and did a local
  orphan-check. This PR does a **whole-`src/main` grep** for each of
  the query names to catch anything a per-PR audit could miss (e.g.
  test-code callers, comment references, KDoc cross-references).
  Delete stragglers.
- **Dead-DTO scan.** `SarPniResult`, `SarOasysPniResult`, `SarPerson`,
  `SarOrganisation`, `SarStaff` — each deleted in its own PR. Verify
  no orphaned imports / spring-doc `@Schema` references / mapper
  helpers survive.
- **KDoc cross-reference fixup.** PR #1115 added KDoc on the four
  SAR-collection getters that cross-referenced each other (e.g.
  *"sibling getters are [PniResultRepository.findAllByPrisonNumber],
  [OasysPniResultEntityRepository.findAllByPrisonNumber] and
  [StaffRepository.findByPrisonNumber]"*). If any of those siblings
  are deleted in PR-8/PR-11, the remaining KDocs on the surviving
  getters (`ReferralRepository.getSarReferrals` and
  `CourseParticipationRepository.getSarParticipations`) will have
  dangling `[...]` links. Fix them up so the code compiles clean and
  KDoc renders correctly.
- **Fixture companion-const scan.** `SarContractIntegrationTest`'s
  companion object accumulated a lot of `_ID` consts during round 1
  and PR #1115. Any that are no longer referenced from
  `setupTestData()` after the round-2 removals — delete.
- **`entity-schema.json` sanity-check.** The schema golden should
  reflect only removed classes / fields, no additions. If any
  round-2 PR accidentally left an addition (e.g. a rogue `?` making
  a field nullable, or a new field crept in), catch it here.
- **`expectedFlywaySchemaVersion` sanity-check.** Should still be
  `145` — round 2 adds no migrations. If any round-2 PR drifted
  this, revert.
- **Fixture unused-import cleanup.** After stanzas are removed,
  `SarContractIntegrationTest.kt` may have unused imports. `ktlint`
  should catch these but a manual look is cheap.
- **Widened fixture unwind for genuinely dead pieces.** PR #1115's
  fixture widening added a lot to `setupTestData()`. Most stays;
  the parts that seeded PNI / OASys PNI / person go with PR-8; the
  second-POM staff seed decision was left ambiguous in PR-11 — this
  PR is the last chance to trim it if it's clearly dead.
- **`PersistenceHelper` orphaned-method sweep.** Flagged by the
  PR-8 nine-lens self-review (2026-08-14, DELIVERY-LOG entry):
  after PR-8 merges, `PersistenceHelper.createOasysPniResult`
  and `PersistenceHelper.createPerson` are **genuinely orphaned**
  — both were only called from the two SAR-side sites PR-8
  removed. This supersedes PR-8's "Non-obvious #2" claim
  (*"leave the helper as-is — helper is called by other tests"*),
  which grep proved stale on `origin/main @ 0cf89850`. Verify with
  a fresh grep across all `src/test` once PR-8/9/10/11 have
  merged (some other round-2 PR might legitimately re-introduce a
  caller — unlikely but check); delete both methods if no other
  callers surface. `createPerson`'s `LocalDate` bind fix from
  PR #1115 goes with the deletion — it was generically useful in
  theory but has no consumer left in practice.
- **Fixture companion-const scan (round-2 additions).**
  `SarContractIntegrationTest`'s companion object accumulated
  `PNI_RESULT_ID`, `OASYS_PNI_RESULT_ID`, `PERSON_ID` (and possibly
  more) during round-1 and PR #1115. Any that are no longer
  referenced from `setupTestData()` after the round-2 removals —
  delete. (Already listed above but calling out these three
  specific consts as the PR-8-driven expected deletions.)
- **Full-suite regression run.** `./gradlew ktlintCheck test` on the
  merged tip — 678 (or whatever the post-round-2 count is) tests
  green with the four PR-8/9/10/11 heads combined.
- **UUID-leak grep** across both goldens (defensive; nothing in
  round 2 should have re-added UUIDs, but the grep is cheap).

### Does not

- **No product-behaviour changes.** If a hygiene finding here would
  change API shape, snapshot output, or observable service
  behaviour, that finding is scoped to a fresh PR (call it PR-14 or
  a hot-fix on the offending round-2 PR) — not this one.
- **No new migrations.** V145 stays; nothing gets added.
- **No further section removals or additions.** Locked to the
  scope PR-8/9/10/11 established.

## Prerequisites for a fresh agent

Read in this order:

1. [`ROUND-2-PLAN.md`](./ROUND-2-PLAN.md) §"Round-2 PR breakdown" +
   §"Impact on PR #1115 (recently merged)" — understand what should
   have been cleaned up already
2. `DELIVERY-LOG.md` round-2 section — the "PR outcomes" table
   should list SHAs for PR-8/9/10/11. Note anything flagged in
   their "Notes" column as deferred to this PR.
3. Each of PR-8/9/10/11 working docs + their merged commits — you
   need to know what was deleted to know what to sanity-grep for.
4. This file end-to-end.

## Verification checklist skeleton

Run in order:

- [ ] `git checkout` a merge-commit that includes all four of
      PR-8/9/10/11 merged into main
- [ ] `grep -rn 'findAllByPrisonNumber' src/main` — expect zero
      hits for PNI-related repositories (deleted in PR-8), zero
      for `StaffRepository` (deleted in PR-11)
- [ ] `grep -rn 'SarPniResult\|SarOasysPniResult\|SarPerson\|SarOrganisation\|SarStaff' src` —
      expect zero hits (all DTOs deleted in their respective PRs)
- [ ] `grep -rn 'pniResults\|oasysPniResults\|\.person\b' src/main` —
      expect zero SAR-DTO hits (`Person` unrelated classes may exist,
      inspect any hit)
- [ ] KDoc dangling-reference scan on the two surviving
      SAR-collection getters — `ReferralRepository.getSarReferrals`
      and `CourseParticipationRepository.getSarParticipations` — no
      broken `[...]` links to deleted siblings
- [ ] `SarContractIntegrationTest` companion object — no unreferenced
      `_ID` consts
- [ ] **`PersistenceHelper.createOasysPniResult` + `createPerson`
      orphan sweep** (flagged by PR-8 nine-lens self-review,
      2026-08-14): `grep -rn 'createOasysPniResult\|createPerson' src/test` —
      expect **zero hits** post PR-8/9/10/11 merge. If zero, delete
      both methods from `PersistenceHelper.kt` (including the
      `createPerson` `LocalDate` bind fix from PR #1115 — dead
      without a consumer). If any hit surfaces, leave both alive
      and record in the PR-12 body which round-2 PR re-introduced
      the caller.
- [ ] `./gradlew ktlintCheck test` — clean, all tests green
- [ ] `entity-schema.json` — diff makes sense (removals only, no
      additions)
- [ ] `expectedFlywaySchemaVersion` still `"145"`
- [ ] Snapshot goldens unchanged from post-PR-11 state (verify
      `regenerate-sar-snapshots.sh` produces zero diff)
- [ ] UUID-leak grep on both goldens returns 0
- [ ] `git status --short` — no accidentally staged `.snyk` /
      xlsx / other untracked files

## Deliverables

- Cleanup commit(s) on `APG-2546/round-2-hygiene-tidy`
- PR body: bullet list of every stray thing removed (empty is fine
  if the four PRs cleaned everything up — the PR still has value as
  the "confirmed clean" checkpoint before Branston sees the new PDF)
- Merged to `main` before PR-13 (docs handover) opens

## Description template

```markdown
## Round-2 code hygiene + test tidy

Standalone cleanup pass after PRs #<8>, #<9>, #<10>, #<11> land the
four round-2 removals. Nothing here changes observable behaviour or
snapshot output — this PR only catches cross-PR interactions and
ties off any orphaned pieces the per-PR audits couldn't see.

### What this removes

- < enumerate: orphaned queries, dead DTOs, unused companion consts,
  dangling KDoc references, unused imports, dead fixture stanzas >
- < or "None found — all four round-2 PRs cleaned up their own
  scope; this PR stands as the confirmed-clean checkpoint" >

### Verification

- `./gradlew ktlintCheck test` — N tests pass (unchanged from
  post-PR-11), ktlint clean
- Snapshot regen produces zero diff
- UUID-leak grep returns 0
- `expectedFlywaySchemaVersion` still `"145"`, `entity-schema.json`
  unchanged from post-PR-11

### Not touched

- V145 index (Flyway forward-only)
- Any surviving KDoc that reads sensibly without the deleted
  siblings

_(Note: the previous "`PersistenceHelper.createPerson` LocalDate
bind fix — helper serves other tests" bullet is **superseded** by
the PR-8 nine-lens self-review finding — grep at `origin/main @
0cf89850` proved zero other callers, so `createPerson` +
`createOasysPniResult` are on the deletion candidate list per the
"Does" section above. Retain the wording only if the pre-merge
grep surfaces an unexpected caller from a round-2 PR.)_
```

