# SAR originals — JOIN FETCH follow-up

> **Status:** planning • **Depends on:** APG-2493 (merged) • **Ticket:** TBD (rename this file to `APG-XXXX-...` once triaged)

## Context

APG-2493 added a nested `SarOriginalReferral` block on every `SarReferral`
in the SAR custody payload. It batch-loads the originals in a single
`referralRepository.findAllById(originalReferralIds.toSet())` call,
which is the correct outer shape.

However — the loaded originals have their `offering` and `referrer`
associations marked `FetchType.LAZY` on `ReferralEntity`:

```kotlin
// domain/entity/create/ReferralEntity.kt
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "offering_id", ...)
var offering: OfferingEntity,

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "referrer_username", ...)
var referrer: ReferrerUserEntity,
```

The `SubjectAccessRequestService.toSarOriginalReferral` mapper then
reads:

- `offering?.organisationId`       — hydrates `offering` (1 query per unique offering)
- `offering?.course?.name`          — `course` is EAGER on `OfferingEntity`, piggy-backs
- `referrer.username`               — hydrates `referrer` (1 query per unique referrer)

Net cost per SAR is therefore:

```
+1 batched query (findAllById on originals)
+ N_unique_offerings hydration selects
+ N_unique_referrers hydration selects
```

The `SubjectAccessRequestService` `getPrisonContentFor` commit message
(commit `d92019c9` at time of writing) already documents this caveat
honestly. In real-world data the number of originals per subject is
typically 0–1, so absolute impact is negligible — but the +2N is
silent and would bite hard if a subject with a long referral chain
ever hit the endpoint.

## Task

Replace the JpaRepository-inherited `findAllById` call with a
`@Query`-annotated method on `ReferralRepository` that eagerly joins
`offering`, `course`, and `referrer` in the same select — collapsing
the +2N hidden hydrations into 0.

### Proposed repository method

```kotlin
// ReferralRepository.kt
@Query(
  """
  SELECT DISTINCT r
    FROM ReferralEntity r
    JOIN FETCH r.offering o
    JOIN FETCH o.course
    JOIN FETCH r.referrer
   WHERE r.id IN :ids
  """,
)
fun findAllByIdWithOfferingAndReferrer(@Param("ids") ids: Collection<UUID>): List<ReferralEntity>
```

Notes:

- `SELECT DISTINCT` is needed because `JOIN FETCH` on collections can
  duplicate parent rows (safe here because `offering` and `referrer`
  are `@ManyToOne`, but keep `DISTINCT` defensively for future
  extensions).
- Keep the parameter type as `Collection<UUID>` to match the current
  call-site's `.toSet()` shape without forcing a re-copy.
- `SQLRestriction(value = "deleted = false")` on `ReferralEntity`
  applies automatically to the derived FROM — an original that was
  soft-deleted will silently drop out of the result, matching the
  current behaviour (`findAllById` also respects `@SQLRestriction`).
  This is worth confirming with an integration test.

### Call-site change

`SubjectAccessRequestService.getPrisonContentFor`:

```kotlin
// before
referralRepository.findAllById(originalReferralIds).associateBy { it.id!! }
// after
referralRepository.findAllByIdWithOfferingAndReferrer(originalReferralIds).associateBy { it.id!! }
```

One-line swap.

### Tests

1. **`SubjectAccessRequestServiceTest`** — update the existing
   `every { referralRepository.findAllById(...) }` stub to
   `every { referralRepository.findAllByIdWithOfferingAndReferrer(...) }`.
   No other test changes.

2. **New integration test** (or extend
   `SubjectAccessRequestServiceIntegrationTest`) — assert that a
   populated original's `offering`, `course`, and `referrer` are
   all reachable on the returned entities *without* triggering
   additional selects. Use a Hibernate `Statistics` spy or the
   `SQLStatementCountValidator` pattern seen in APG-2495's B1 spy
   test to prove exactly 1 query is issued for the originals batch.

3. **`ReferralRepositoryTest`** — add a repository-level test that
   seeds one referral + a soft-deleted variant, then asserts
   `findAllByIdWithOfferingAndReferrer` respects `deleted = false`
   filter.

### Non-changes

- No DTO changes.
- No mustache template changes.
- No fixture changes (SAR contract fixture's originalReferralId is
  `null` — see the sister follow-up
  `sar-populated-originalreferral-fixture-followup.md`).

## Rough size

- ~15 lines in `ReferralRepository.kt` (new `@Query` method + doc)
- 2 lines in `SubjectAccessRequestService.kt` (call-site swap)
- ~5 lines in `SubjectAccessRequestServiceTest.kt` (mock rename)
- ~40 lines of new integration/repo tests
- 1 PR, 1 commit, ~1 hour end-to-end

## Verification command

```bash
./gradlew compileKotlin compileTestKotlin \
          ktlintMainSourceSetCheck ktlintTestSourceSetCheck \
          test --tests '*.SubjectAccessRequestService*' \
               --tests '*.ReferralRepository*' -q
```

## Handoff notes for the picking-up agent

- Branch off `origin/main`, not this one. This branch only carries
  the doc.
- APG-2493 must be on `main` before this ticket makes sense —
  confirm by grepping for
  `findAllById(originalReferralIds.toSet())` in
  `src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/service/SubjectAccessRequestService.kt`.
- Signed commits required (repo policy). See the APG-2493 planning
  note for the pinentry-in-agent-terminal workaround.
- Coordinate with any concurrent SAR work (search the tracker for
  active `SAR` / `originalReferral` tickets).

