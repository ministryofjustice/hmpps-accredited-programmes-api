# Fresh-agent prompt templates — APG-2546 round 2

Copy-paste-ready prompts for kicking off a fresh Copilot agent session
on any round-2 PR. Same rhythm we used for PR-7 in round 1 (which
worked cleanly).

Each prompt:
- Names the ticket + PR
- Points at the working doc as the source of truth
- Names the assumed starting point (main SHA)
- Requires the agent to stop and confirm if the starting point drifted
- Requires the agent to flag any deviation before touching code
- Requires the agent to report back merge SHA + PDF page count + surprises

---

## Template (generic)

Substitute `<N>`, `<title>`, `<filename>`, `<expected-parent-SHA>`,
and `<expected-parent-branch>` as needed.

```text
Pick up APG-2546 PR-<N>. The working doc is at
doc/planning/APG-2546/PR-<N>-<filename>.md — read it end to end,
follow the "Files to change" section literally, run the
verification checklist, and open a PR using the description
template at the bottom of the doc. Do not deviate from the doc
without flagging it back to me first.

Assumed starting point: tip of <expected-parent-branch> after
<expected-parent-SHA> has merged. If that hasn't merged yet, stop
and tell me before touching anything.

When you're done, report back the PR number, merge SHA (once
merged), PDF page count if regenerated, and any surprises so I can
update doc/planning/APG-2546/DELIVERY-LOG.md in the tracking chat.
```

---

## PR-8 — remove PNI + OASys PNI + Person sections

```text
Pick up APG-2546 PR-8. The working doc is at
doc/planning/APG-2546/PR-8-remove-pni-oasys-person.md — read it end
to end, follow the "Files to change" section literally, run the
verification checklist, and open a PR using the description
template at the bottom of the doc. Do not deviate from the doc
without flagging it back to me first.

Assumed starting point: tip of main after PR #1115 has merged
(SHA 0cf89850). If main has moved on to something incompatible,
stop and tell me before touching anything.

Also worth reading before you start:
  - doc/planning/APG-2546/ROUND-2-PLAN.md §"Round-2 PR breakdown"
    and §"Impact on PR #1115 (recently merged)"
  - doc/planning/APG-2546/DELIVERY-LOG.md round-2 kickoff entry
    (2026-08-13) — captures the DD row 139 override and PR #1115
    impact matrix

When you're done, report back the PR number, merge SHA, PDF page
count from the regenerated sample, and any surprises so I can
update DELIVERY-LOG.md.
```

---

## PR-9 — scrub `prisonerNumber` from surviving sections

```text
Pick up APG-2546 PR-9. The working doc is at
doc/planning/APG-2546/PR-9-scrub-nomis-and-crn.md — read it end to
end, follow the "Files to change" section literally (all line refs
were re-verified against origin/main @ b7b05283 on 2026-08-17
after PR-8 merged), and open a PR using a similar description
template to PR-8's.

FIRST COMMAND IN YOUR SESSION must be:
  git fetch origin && git checkout origin/main
Verify HEAD is b7b05283 (the anchor SHA every line-number in the
PR-9 doc is measured against — this is PR-8's merge commit on
main). If main has moved on to a newer SHA (someone else merged
between now and your session), stop and tell me before touching
anything — line refs may need spot-checking.

Assumed starting point: tip of main after PR-8 has merged
(SHA b7b05283, merged 2026-08-17). PR-8 is confirmed merged.

Also worth reading before you start:
  - doc/planning/APG-2546/ROUND-2-PLAN.md §"Round-2 PR breakdown"
  - doc/planning/APG-2546/PR-8-remove-pni-oasys-person.md — you're
    working on the surface PR-8 leaves behind
  - doc/planning/APG-2546/DELIVERY-LOG.md 2026-08-14 PR-8 outcome
    entry — captures Findings 1 (empty JpaRepository shell for
    PR-1 precedent — pattern to follow if this PR orphans a
    repository) and 2 (PersistenceHelper method-body cleanup
    deferred to PR-12 — do NOT delete these in PR-9)

Header-ownership claim in Deborah's ask (NOMIS ID + CRN sourced
by SAR wrapper header, not our payload) is CONFIRMED — see
2026-08-13 DELIVERY-LOG entry, no further Slack ping needed to
proceed.

When you're done, report back the PR number, merge SHA, PDF page
count from the regenerated sample, and any surprises.
```

---

## PR-10 — move `organisationName` into referral

```text
Pick up APG-2546 PR-10. The working doc is at
doc/planning/APG-2546/PR-10-organisation-into-referral.md — read
it end to end and follow it (all line refs were re-verified
against origin/main @ f8e04ab0 on 2026-08-17 after PR-9 merged).
The design decision (JPQL JOIN vs post-fetch) is already resolved
in the doc: post-fetch is what the service already does for
SarOriginalReferral.organisationName, and PR-10 just wires the
parent SarReferral through the same map. No new query, no schema
check. Scope is smaller than the original skeleton suggested (½
day, not 1 day).

Open a PR using a similar description template to PR-8's / PR-9's.

FIRST COMMAND IN YOUR SESSION must be:
  git fetch origin && git checkout origin/main
Verify HEAD is f8e04ab0 (the anchor SHA every line-number in the
PR-10 doc is measured against — this is PR-9's merge commit on
main). If main has moved on to a newer SHA, stop and tell me
before touching anything — line refs may need spot-checking.

Assumed starting point: tip of main after PR-9 has merged
(SHA f8e04ab0, merged 2026-08-17). PR-8 (b7b05283) and PR-9
(f8e04ab0) both confirmed merged.

Also worth reading before you start:
  - doc/planning/APG-2546/ROUND-2-PLAN.md §"Round-2 PR breakdown"
  - doc/planning/APG-2546/DELIVERY-LOG.md 2026-08-17 PR-9 merge
    entry — captures the full PR-10 line-drift map from 0cf89850
    to f8e04ab0
  - The existing originalReferral sub-block in
    sar_template.mustache (line 23 on f8e04ab0) already carries
    organisationName — same shape applies to the parent referral.
  - The existing SarOriginalReferral mapper body in
    SubjectAccessRequestService.kt (line 272 on f8e04ab0) does
    `organisationName = offering?.organisationId?.let { organisationNamesByCode[it] }` —
    that's the exact wiring to copy onto SarReferral.

Pattern learned from PR-8: if this PR ends up orphaning a
repository interface, keep it alive as an empty JpaRepository
shell (round-1 AuditRepository / PR-8's OasysPniResultEntity-
Repository precedent). Don't delete the interface file.

Pattern learned from PR-8 Finding 2: do NOT delete
PersistenceHelper.createOasysPniResult or createPerson — those are
already scoped to PR-12 hygiene, don't fold into this PR.

When you're done, report back the PR number, merge SHA, PDF page
count, and any surprises.
```

---

## PR-11 — remove top-level `staff[]` (option (a))

```text
Pick up APG-2546 PR-11. The working doc is at
doc/planning/APG-2546/PR-11-remove-top-level-staff.md — read it
end to end and follow it (all line refs were re-verified against
origin/main @ d710fa7f on 2026-08-17 after PR-10 merged).
Open a PR using a similar description template to PR-8/9/10's.

FIRST COMMAND IN YOUR SESSION must be:
  git fetch origin && git checkout origin/main
Verify HEAD is d710fa7f (the anchor SHA every line-number in the
PR-11 doc is measured against — this is PR-10's merge commit on
main). If main has moved on to a newer SHA, stop and tell me
before touching anything.

Assumed starting point: tip of main after PR-10 has merged
(SHA d710fa7f, merged 2026-08-17). PR-8 (b7b05283), PR-9
(f8e04ab0), and PR-10 (d710fa7f) all confirmed merged.

PR-11 is the LAST of the four sibling PRs touching
SubjectAccessRequestService.kt + sar_template.mustache. No
parallel-serial concern; you're clear to execute.

Option (a) is locked (Deborah confirmed 2026-08-13 pm — see
DELIVERY-LOG round-2 kickoff): just delete the top-level staff[]
list, keep the inline primaryPomStaffSurname /
secondaryPomStaffSurname fields already on each referral.

Also worth reading before you start:
  - doc/planning/APG-2546/ROUND-2-PLAN.md §"Round-2 PR breakdown"
    and §"Impact on PR #1115 (recently merged)" (StaffRepository
    surname-sort query becomes orphaned here)
  - doc/planning/APG-2546/DELIVERY-LOG.md 2026-08-17 PR-10 merge
    entry — captures the full PR-11 line-drift map from 0cf89850
    to d710fa7f

Pattern learned from PR-8: if this PR ends up orphaning a whole
repository interface, keep it alive as an empty JpaRepository
shell (round-1 AuditRepository / PR-8's OasysPniResultEntity-
Repository precedent). BUT this PR does NOT hit that case:
StaffRepository has multiple other in-use methods
(findByStaffId, findLastNameByUsername, findLastNameByStaffId,
findSurnamesByUsernames, findSurnamesByStaffIds). Only the
specific findByPrisonNumber method definition is deleted from
the interface — the interface itself stays as-is. Doc calls
this out explicitly under "Repositories".

Pattern learned from PR-8 Finding 2: do NOT delete
PersistenceHelper.createOasysPniResult or createPerson - those
are already scoped to PR-12 hygiene.

Optional "while you're in the file" tidy from PR-10 self-review:
cosmetic mustache double-blank line between Courses and Staff
sections. Since PR-11 deletes the <h2>Staff> block right below
this gap, offered as a nice-to-have if cleaning is a one-line
no-op. Not a blocker; PR-12 will catch it otherwise.

When you're done, report back the PR number, merge SHA, PDF page
count, and any surprises.
```

---

## PR-12 — round-2 code hygiene / test tidy

```text
Pick up APG-2546 PR-12. The working doc is at
doc/planning/APG-2546/PR-12-round-2-hygiene-tidy.md — read it end
to end, follow it, and open a PR using the description template
at the bottom of the doc.

FIRST-COMMAND fresh-checkout guard (R6):
    git fetch origin && git checkout origin/main
Verify you are on 47488c8a (PR-11 merge commit) or later. If
main is behind that, stop — PR-12 depends on all four sibling
PRs (PR-8 b7b05283, PR-9 f8e04ab0, PR-10 d710fa7f, PR-11
47488c8a) being merged. All four merged 2026-08-17.

The planning doc's "Pre-verification summary" section + the
DELIVERY-LOG 2026-08-17 late-evening entry contain a full grep
table pre-run against 47488c8a. Ten of thirteen verification
sweeps are already CONFIRMED CLEAN — the executing agent's job
has shrunk to five bullets (see "Effective delta after
pre-verification" in the doc). Re-run every grep as a sanity
check; expect the pre-verified sweeps to still be clean.

This is a NO-BEHAVIOUR-CHANGE PR (except fixture #3 below,
which changes snapshot output ONLY because a new organisation
was seeded — expected diff, not a bug). If any other grep
finding would change API shape or observable service behaviour,
that is out of scope — flag it back to me.

Effective delta (from doc's "Effective delta" section):
  1. Delete PersistenceHelper.createPerson (L157) — 0 test
     callers confirmed on 47488c8a
  2. Delete PersistenceHelper.createOasysPniResult (L191) — 0
     test callers confirmed on 47488c8a
     (Note: createPniResult stays alive — 1 caller at
     DomainEventsListenerTest.kt:222 for the NOMIS prisoner-merge
     flow)
  3. Add BXI / HMP Brixton offering to SarContractIntegrationTest
     fixture and wire to one of the two referrals; regen goldens.
     Confirm both referrals render DIFFERENT organisationName
     values in the JSON + HTML goldens.
  4. Optional: strip trailing \n from sar-api-response.json OR
     update regenerate-sar-snapshots.sh to write without one.
     Pick per the file's siblings; document.
  5. Document V145 idx_staff_last_name keep-vs-drop decision in
     PR body. Recommend keep (Flyway forward-only default; write
     cost not measurable on background reference table). Only
     ship V146__drop_staff_last_name_index.sql if profile data
     justifies (unlikely). If V145 stays, leave the V144/V145
     SQL comment name-drops of findByPrisonNumber alone — editing
     Flyway-applied SQL for comment hygiene isn't idiomatic.
  6. Full-suite regression: ./gradlew ktlintCheck test — expect
     N tests green (record actual N in PR body; will differ from
     round-2 baseline because fixture #3 adds test data).
     Snapshot regen zero-diff on second run. UUID-leak grep = 0.
     entity-schema.json diff sanity (unchanged expected — no
     JPA entity touched).

Session-hygiene reminders (from prior PR-8/9/10/11 executions):
  - Do NOT use zsh heredocs for commit messages — write to a
    file and use `git commit -F /tmp/msg.txt`.
  - Docker Desktop must be running for
    regenerate-sar-snapshots.sh (Testcontainers backing).
  - After git checkout, cross-check stale read_file cache with
    sed / git show if content doesn't match working tree.
  - PR-11 self-review pattern (ship-it verdict + non-blocking
    observations) is the model: worth a nine-lens self-review
    before pushing.

When you're done, report back the PR number, merge SHA, tests
green count, and whether the V145 decision landed as keep or
drop (with reasoning).
```

---

## PR-13 — round-2 docs handover + fresh sample PDF

```text
Pick up APG-2546 PR-13. The working doc is at
doc/planning/APG-2546/PR-13-round-2-docs-and-handover.md — read
it end to end, expand the skeleton into a full working doc,
follow it, and open a PR.

Assumed starting point: tip of main after PR-12 has merged
(post round-2 code hygiene). If PR-12 isn't merged yet, stop and
tell me.

This is a docs + generated-artefact PR — no product code
changes. Deliverables:
  - DELIVERY-LOG round-2 closeout entry with final SHAs +
    timings for PR-8/9/10/11/12
  - DD row 139 override closeout note (Roxanne looped in if
    that hasn't happened by hand yet)
  - Fresh sample PDF from a preprod CRN (recommended: A9648CH
    from doc/planning/APG-2546/DELIVERY-LOG.md — verify it's
    still the top shortlist candidate by re-running
    /tmp/pni-candidate-shortlist-v2.sql if the port-forward
    pod is still around; else re-shortlist)
  - Email draft + Slack DM draft to Branston with the PDF
    attached
  - Short update note for Deborah closing the loop on all
    five round-2 asks

When you're done, report back the PR number, merge SHA, and
the round-3 sample-PDF artefact path.
```

---

## PR-15 — remove duplicate top-level `<h2>Courses</h2>` section

```text
Pick up APG-2546 PR-15. The working doc is at
doc/planning/APG-2546/PR-15-remove-top-level-courses.md — read
it end to end, follow the "Files to change" section literally,
run the verification checklist, and open a PR using the
description shape at the bottom of the doc. Do not deviate from
the doc without flagging it back to me first.

Assumed starting point: tip of origin/main after PR-14 has
merged. PR-14 landed 2026-08-24 as `6d713186`. First commands in
this fresh session should be:

  git fetch origin
  git checkout origin/main
  git log --oneline -1   # expect 6d713186 (or later main tip)
  git checkout -b APG-2546/remove-top-level-courses

If HEAD isn't 6d713186 (or a descendant on main), stop and tell
me — line refs in the working doc are anchored to 6d713186 and
will need re-anchoring if main has moved past that.

This PR is a mirror of PR-11 in shape: remove a top-level list
whose content is now covered by an inline field on each
SarReferral. PR-11 removed the Staff block (once inline surnames
covered it); PR-15 removes the Courses block (once inline
courseName from PR-14 covered it). Roxanne's 2026-08-25 email
after PR-14's preprod PDF explicitly asked for this — verbatim
quote is at the top of the working doc.

Pre-verified anchors on 6d713186:
  - SubjectAccessRequestService.kt: courseRepository ctor param
    L42, `courses = ...` line L129, Content.courses field L196,
    SarCourse data class L280-282, .toSarCourse() ext L437-441
  - CourseRepository.kt: getSarCourses @Query at L46-54
  - sar_template.mustache: <h2>Courses</h2> block at L88-98
  - SubjectAccessRequestServiceTest.kt: mock decl L44, ctor arg
    L61, stub L198-202, assertions L262 + L321-322, verify L351
  - SubjectAccessRequestServiceIntegrationTest.kt: size assertion
    L147, `with(content.courses[0]) { ... }` block L178-180
    (integration test — caught in nine-lens review 2026-08-25;
    initial scoping missed it, now correctly listed)
  - sar-api-response.json: `"courses":[{"name":"Course 1"}],`
    substring inside the top-level JSON object
  - sar-expected-render-result.html: <h2>Courses</h2> rendered
    block at L291-297

The working doc's non-obvious items #1-7 are the discipline
guardrails — read them all, especially #1 (SarCourse vs
SarCourseParticipation are DIFFERENT, do not touch
courseParticipation) and #6 (HAAR ping required post-merge,
same discipline as PR-14, single ping covers both envs).

Nine-lens self-review is required before push, matching PR-8
through PR-14. Both self-review passes must ship-it before you
open the PR. Two agent-side self-reviews then human-side team
review, same discipline PR-14 landed under.

When you're done, report back:
  - PR number + merge SHA (once merged)
  - PDF page count from the regen (1 or 2 both fine — flag which)
  - Any surprises so I can update DELIVERY-LOG in the tracking chat
  - The exact HAAR-team Slack draft text you'd suggest (I'll edit
    to voice and send once merge lands)
```

Session-hygiene reminders same as previous PRs: no zsh heredocs
for commit messages (use `git commit -F /tmp/…`), Docker Desktop
must be running for Testcontainers-backed snapshot regen, stale
`read_file` cache after `git checkout` — cross-check with `sed`
or `git show` if line refs don't match.

---

## Tips for the human running the prompt

- **Fresh chat session per PR.** Round-1 PR-7 was executed cleanly
  in a fresh session and the discipline held. Cross-context
  contamination is the main risk.
- **Always start from a fresh checkout of `origin/main`, NOT the
  planning branch.** The planning branch (`APG-2546/planning-sar-field-removals`)
  was cut from merge-base `106e27d2` (pre-round-1) and only adds
  docs — its `src/` tree is behind `origin/main`. Every line-number
  in the working docs is anchored to `origin/main @ 0cf89850`; if
  the agent reads files from the planning-branch working tree it
  will see wildly different line numbers and think everything is
  wrong. First command in every fresh session should be
  `git fetch origin && git checkout origin/main`.
- **Confirm the starting-point SHA** in the prompt before pasting.
  The templates above hard-code `0cf89850` (main tip after
  PR #1115) as PR-8's starting point — update as later PRs merge.
- **If the agent flags a deviation**, don't just say "proceed" —
  read the deviation, decide whether it's a real change of plan
  or a doc-drift, and update the working doc accordingly before
  the agent commits.
- **Round-1 PR-7 was the reference-clean execution** — you can
  read `doc/planning/APG-2546/DELIVERY-LOG.md` PR-7 outcome entry
  for what a well-executed round looks like.

## Session-hygiene tips (learned from PR-8 execution, 2026-08-14)

Tell the agent up-front, either by adding to the prompt or as a
first message once the session starts:

- **Do not use inline zsh heredocs for commit messages.** Multi-line
  quoted strings containing `{`, `#`, backticks, or em-dashes get
  mangled by zsh and produce corrupted history / stuck `dquote>`
  prompts. **Do this instead:** write the full commit message to a
  scratch file via the workspace file-write tool (e.g.
  `/tmp/apg2546-pr-<N>-msg.txt`), then `git commit -F /tmp/…`. PR-8
  hit this on the first commit attempt; recovered cleanly with
  `-F`. This paper-cut has cost us minutes on more than one PR now;
  bake it into the workflow.
- **After a `git checkout`, verify file reads with the terminal.**
  If `read_file` returns content that doesn't match the working
  tree (stale editor / tool cache after a branch switch), cross-check
  with `sed -n 'A,Bp' <file>` or `git show <sha>:<file>` before
  acting on it. PR-8 hit a stale-cache `read_file` on
  `SubjectAccessRequestService.kt` immediately after switching from
  the planning branch to `origin/main`; terminal-verified content
  confirmed the doc's line-refs were correct against the actual
  on-disk file. Nothing broke, but the failure mode is worth
  explicit warning to save the next agent a confused half-hour.
- **Docker Desktop pre-requisite** — Testcontainers-backed
  `SarContractIntegrationTest` / `regenerate-sar-snapshots.sh`
  needs Docker running locally. If the agent's first snapshot-regen
  attempt fails with "Cannot connect to the Docker daemon", have
  them run `open -a Docker` (macOS) and wait ~15 s before retrying.
  Not a code issue; standard local-dev prerequisite.

