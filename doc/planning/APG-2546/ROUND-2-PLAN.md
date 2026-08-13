# APG-2546 — Round 2 SAR cuts (post-Aug-13 OSAR review)

> **Ticket:** APG-2546 (continuation) • **Round-1 close date:** 2026-08-11 (PR #1115 merged)
> • **Round-2 kickoff:** 2026-08-13 • **Est.:** ~1 sprint / 6 thin PRs (PR-8…PR-13)
> • **Owner:** Raby • **Reviewer chain:** Cameron's SAR team / Deborah SDM / Roxanne (DD) / Branston (OSAR)

## Why round 2 exists

On 2026-08-12 the SAR PDF generated from preprod CRN A9648CH was sent
to Branston (OSAR) for round-2 review, using the widened fixture from
PR #1115 and the round-1 field-removal PRs (#1107–#1113) merged.

On 2026-08-13, following the review meeting, **Deborah (SDM,
Cameron's SAR product team) came back with a follow-up action list**.
Deborah's verbatim asks (Slack, 13:25):

1. Remove NOMIS IDs and CRNs as they are in the header.
2. Remove PNI data and this is retrieved from ARNs via the Probation
   Hub request.
3. Remove Personal Data section.
4. Add organisation field to the referral rather than list separately
   so it is in context.
5. Add staff name field to the referral rather than list separately
   so it is in context.

Raby DM'd Deborah to clarify #5: does she want (a) drop the redundant
top-level `staff[]` list — the referral already carries
`primaryPomStaffSurname` / `secondaryPomStaffSurname` inline — or (b)
upgrade those two surname fields to full names and remove the
top-level list?

**Deborah confirmed (a).** Locked into the plan.

Round-2 is a **thinning sweep** — it removes surface Branston no
longer needs (three whole blocks) and inlines two collections that
duplicate context already available at the referral level. Same
ticket (APG-2546) because it's a direct continuation of the same
data-review conversation; new PR sequence PR-8…PR-13 continues from
PR-7 (the last round-1 PR).

## Round-1 close-out state

Round-1 shipped:

- **PRs #1107–#1113** — six thin field-removal PRs (audit records,
  status history internal fields, sexual-offence details, oasys-PNI
  ID stripping, remaining internal ID scrub, `SarOriginalReferral.id`
  UUID strip).
- **PR #1115** (fixture widening + SAR-collection `ORDER BY` hygiene)
  — widened the contract-test fixture to be a vettor-training
  exemplar and added deterministic ordering to the five
  SAR-collection queries.

Round-1 close-out artefacts:

- Sample PDF sent to Branston 2026-08-12, generated from preprod CRN
  A9648CH via Cameron's SAR dev-service (Option 1, full-chrome).
- `DELIVERY-LOG.md` closed round-1 at head `8e89ec95`.

## Round-2 asks mapped to current shape

Cross-referenced against:
- current SAR response golden: `src/test/resources/sar/sar-api-response.json`
- current template: `src/main/resources/sar_template.mustache`
- Aug-12 sample PDF from preprod CRN A9648CH

| # | Deborah's ask | Concrete change | Handled in |
|---|---|---|---|
| 1 | NOMIS ID / CRN in header, not body | Drop `prisonerNumber` from `referrals[]` + `courseParticipation[]`. (Also cascades from #2/#3.) Drop `crn` from `pniResults[]` (cascades from #2). | PR-9 (surviving sections) + PR-8 (cascade) |
| 2 | PNI data sourced from ARNs via Probation Hub | Delete entire `pniResults[]` **AND** `oasysPniResults[]` blocks — JSON + template + DTO + repository-call from SAR service + fixture setup. | PR-8 |
| 3 | Personal data section removed | Delete entire `person{}` — JSON + template + DTO + person-fetching wiring + fixture setup. | PR-8 |
| 4 | Organisation into referral context | Add `organisationName` to each referral (same shape as it already has on `originalReferral`). Delete top-level `organisations[]`. Backend: referral row already carries `organisation_id` so this is a JOIN in `getSarReferrals`, not a new lookup. | PR-10 |
| 5 | Staff into referral context — **option (a)** | Delete top-level `staff[]` (JSON + template + DTO). Keep `primaryPomStaffSurname` / `secondaryPomStaffSurname` already inline in each referral. | PR-11 |

## Round-2 PR breakdown

Thin PRs, one theme each, snapshot-golden regen scoped to what that
PR removes / moves. Same discipline as round-1.

| PR | Scope | Working doc | Est |
|---|---|---|---|
| **PR-8** | Remove `pniResults[]`, `oasysPniResults[]`, `person{}` — three whole sections, one coherent theme "sourced elsewhere in the SAR bundle". Cleans up now-dead PR #1115 wiring in the same PR. | [`PR-8-remove-pni-oasys-person.md`](./PR-8-remove-pni-oasys-person.md) | 1½ days |
| **PR-9** | Drop `prisonerNumber` from `referrals[]` + `courseParticipation[]` (surviving sections). | [`PR-9-scrub-nomis-and-crn.md`](./PR-9-scrub-nomis-and-crn.md) | ½ day |
| **PR-10** | Add `organisationName` field to each referral; delete top-level `organisations[]`. | [`PR-10-organisation-into-referral.md`](./PR-10-organisation-into-referral.md) | ½ day |
| **PR-11** | Delete top-level `staff[]` list per **option (a)**. Keep inline surname fields. | [`PR-11-remove-top-level-staff.md`](./PR-11-remove-top-level-staff.md) | ½ day |
| **PR-12** | Round-2 code hygiene / test tidy — final orphan-query audit across the combined post-PR-8/9/10/11 state, dead-DTO scan, KDoc cross-reference fixup (siblings deleted by PR-8/PR-11), fixture companion-const cleanup, `expectedFlywaySchemaVersion` verify, full-suite regression. No observable behaviour change. | [`PR-12-round-2-hygiene-tidy.md`](./PR-12-round-2-hygiene-tidy.md) | ½ day |
| **PR-13** | Round-2 docs handover: DELIVERY-LOG closeout, DD row 139 override recorded, fresh sample PDF from a preprod CRN + email/Slack templates to Branston. | [`PR-13-round-2-docs-and-handover.md`](./PR-13-round-2-docs-and-handover.md) | ½ day |

**Total ~4 days.** Ordering: PR-8 first (biggest cut, reduces surface
for everything downstream), then PR-9 / PR-10 / PR-11 **serial** —
they all touch `SubjectAccessRequestService.kt` and
`sar_template.mustache`, so parallel drafts *will* merge-conflict.
(They have no logical dependency on PR-8's cuts — PR-8 just shrinks
the sanity-grep surface. Serial is a merge-conflict-avoidance
decision, not a code-dependency one.) **PR-12 after all four merge**
(needs the combined state to sanity-grep), PR-13 last.

## Impact on PR #1115 (recently merged)

Recorded up front so nothing surprises the reviewer when PRs open:

| From PR #1115 | Fate in round-2 (verified 2026-08-13 pm against `origin/main` @ `0cf89850`) | Handled in |
|---|---|---|
| `PniResultRepository.findAllByPrisonNumber` — new `@Query` + `ORDER BY p.pniAssessmentDate NULLS LAST, p.pniResultId` | 🛑 **STAY** — `PersonService.kt:287` (prisoner-merge NOMIS domain-event handler) is a production caller. Query is NOT SAR-orphaned. Just remove the SAR call site. | PR-8 |
| `OasysPniResultEntityRepository.findAllByPrisonNumber` — same pattern | 🗑️ **DELETE** — only SAR + SAR-service-test callers on `origin/main`. Genuine prod-orphan after PR-8. | PR-8 |
| `PersistenceHelper.createPerson` — `LocalDate` bind fix | Dead in SAR fixture (person section gone). Helper also called from `SubjectAccessRequestServiceIntegrationTest.kt:94` — **leave the fix alone**; just remove the SAR-contract-test call site. | PR-8 |
| `StaffRepository.findByPrisonNumber` — `ORDER BY s.lastName, s.staffId` (surname-sort) | 🗑️ **DELETE** — only SAR + SAR-service-test callers. Prod-orphan after PR-11. `V145__add_staff_last_name_index.sql` **stays** (Flyway forward-only, additive + reversible). | PR-11 |
| `ReferralRepository.getSarReferrals` — `ORDER BY r.submittedOn NULLS LAST, r.id` | ✅ **Stays useful** — referrals section retained. | — |
| `CourseParticipationRepository.getSarParticipations` — `ORDER BY cp.createdDateTime NULLS LAST, cp.id` | ✅ **Stays useful** — courseParticipation section retained. | — |
| Fixture widening — `originalReferral` sub-block + second-POM seed | Mostly stays useful; `person` widening stanza + PNI widening stanzas removed. Second-POM seed stays (still exercises the referral's inline `secondaryPomStaffSurname`). | PR-8 / PR-11 |
| V145 index | Stays. Additive, reversible, forward-only migration. | — |

One dead query (OasysPniResult) + one dead SAR-only query
(StaffRepository.findByPrisonNumber) + one dead helper-call site
(createPerson in SAR contract test) is the total sunk-cost from PR #1115.
Cheap. Paper trail here. **All orphan-audit outcomes pre-verified
2026-08-13 pm against `origin/main` @ `0cf89850`**, so PR-8/PR-11
agents don't need to re-derive them.

## DD spreadsheet override — row 139

Roxanne's Digital Data review (row 139, `pni_result . pni_result_json`)
had **explicitly kept** the PNI JSON payload in scope — she upgraded
"In SAR API" from No to Yes on 2026-07-10 with the note:

> *"these are in SAR report hence H should be Yes. Updated"*

**Deborah's 2026-08-13 meeting outcome supersedes DD row 139.**
Rationale: PNI data is now sourced by SAR consumers via the ARNs
Probation Hub feed, so replicating it in the Accredited Programmes SAR
report is duplicative and confusing for redaction reviewers.

**Recorded here + in `DELIVERY-LOG.md` round-2 kickoff entry.**
Deborah aware; may loop Roxanne to annotate row 139 for future DD
refreshes so nobody re-adds `pni_result_json` on a subsequent sweep.

## Risk register

| # | Risk | Mitigation |
|---|---|---|
| R1 | We delete a field the SAR wrapper header-owner still relies on. | ✅ **RESOLVED 2026-08-13** — SAR wrapper team (Cameron's team) confirmed verbatim: *"we retrieve the information for the header from two APIs — one for NOMIS IDs and one for nDelius CRNs. We do not in any way retrieve that data from their product — so it's safe to remove it as the OSAR team requested."* Covers PR-8 (person + NOMIS ID) and PR-9 (CRN + prisonerNumber). Full quote in DELIVERY-LOG round-2 timeline. |
| R2 | An organisation-related field is populated somewhere we didn't spot. | PR-10: full grep for `organisationName` / `organisations` before deleting the top-level block. Verify DB has a resolvable `organisation_id` on every referral row (nullable OK). |
| R3 | Repository queries removed in PR-8 / PR-11 turn out to have other callers. | Each PR: `grep -r <queryName>` across `src/main` before deletion. If any non-SAR caller hit, leave the query alive (cheap) and just remove the SAR-service call site. |
| R4 | Fixture regen produces surprising side-effects (as it did in #1115 with Postgres date-binding). | Same guardrail: run `./gradlew ktlintCheck test` on top of every PR, use `script/local-scripts/regenerate-sar-snapshots.sh` (not raw gradle + copy), UUID-leak grep on both goldens post-regen. |
| R5 | Round-3 review lands mid-sprint from Branston asking for further cuts. | Land PR-8 + PR-9 first (biggest wins). Open round-3 changes as PR-8b / PR-9b rather than re-scoping. |
| R6 | Fresh agent reads the *planning branch's* working tree and thinks every code line-ref is wrong. | Planning branch was cut from merge-base `106e27d2` (pre-round-1) and only adds docs. Its `src/` tree is behind `origin/main`. Every PR prereq **must** say "start from a fresh checkout of `origin/main` (currently `0cf89850`), NOT this planning branch's working tree". PR-8 prereqs updated accordingly; propagate as PR-9/10/11 are picked up. |
| R7 | `.snyk` + `Copy of *.xlsx` untracked files reappear in `git status` on every planning session and risk being accidentally staged. | Add to `.gitignore` (or the ticket's `.git/info/exclude`) as a permanent hygiene fix in PR-12 or PR-13. Interim: every PR checklist includes `git status --short` gate before commit. |

## Success criteria

- All 5 asks from Deborah's action list delivered
- All 6 PRs (PR-8…PR-13) merged to main
- Fresh sample PDF generated from a preprod CRN + shared with Branston via PR-13
- OSAR round-3 sign-off received
- `DELIVERY-LOG.md` closed out with final SHAs + timings

## Working directory index

**Round 1 (delivered):**
- [`README.md`](./README.md) — round-1 overview
- [`00-roxanne-followup.md`](./00-roxanne-followup.md)
- [`PR-1-remove-audit-records.md`](./PR-1-remove-audit-records.md)
- [`PR-2-remove-status-history-and-reasons.md`](./PR-2-remove-status-history-and-reasons.md)
- [`PR-3-remove-sexual-offence-details.md`](./PR-3-remove-sexual-offence-details.md)
- [`PR-4-remove-oasys-pni-results.md`](./PR-4-remove-oasys-pni-results.md)
- [`PR-5-strip-internal-ids.md`](./PR-5-strip-internal-ids.md)
- [`PR-6-osar-round-2-handover.md`](./PR-6-osar-round-2-handover.md)
- [`PR-7-strip-original-referral-uuid.md`](./PR-7-strip-original-referral-uuid.md)

**Round 2 (this sweep):**
- [`ROUND-2-PLAN.md`](./ROUND-2-PLAN.md) — this file
- [`AGENT-PROMPT-TEMPLATE.md`](./AGENT-PROMPT-TEMPLATE.md) — copy-paste-ready fresh-agent prompts for every round-2 PR
- [`PR-8-remove-pni-oasys-person.md`](./PR-8-remove-pni-oasys-person.md) — fully drafted, agent-executable
- [`PR-9-scrub-nomis-and-crn.md`](./PR-9-scrub-nomis-and-crn.md) — skeleton, expand before execution
- [`PR-10-organisation-into-referral.md`](./PR-10-organisation-into-referral.md) — skeleton
- [`PR-11-remove-top-level-staff.md`](./PR-11-remove-top-level-staff.md) — skeleton
- [`PR-12-round-2-hygiene-tidy.md`](./PR-12-round-2-hygiene-tidy.md) — skeleton, expand after PR-8/9/10/11 merge (needs post-merge state)
- [`PR-13-round-2-docs-and-handover.md`](./PR-13-round-2-docs-and-handover.md) — skeleton

**Both rounds share:**
- [`DELIVERY-LOG.md`](./DELIVERY-LOG.md) — single running log (round-2 section appended)
- [`scripts/dd-notes-sweep.py`](./scripts/dd-notes-sweep.py) — DD row-note auditor

