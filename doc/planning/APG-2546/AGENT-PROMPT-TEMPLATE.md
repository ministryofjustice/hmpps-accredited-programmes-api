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
end, expand the skeleton into a full working doc (same shape as
PR-8's doc — the skeleton has scope + notes but you'll need to
flesh out "Files to change" and the verification checklist),
follow it, and open a PR using a similar description template.

Assumed starting point: tip of main after PR-8 has merged. PR-10
and PR-11 must be serialised with this PR (not parallel) — all
three touch SubjectAccessRequestService.kt + sar_template.mustache
and will merge-conflict otherwise. If PR-8 hasn't merged yet, stop
and tell me before touching anything.

Also worth reading before you start:
  - doc/planning/APG-2546/ROUND-2-PLAN.md §"Round-2 PR breakdown"
  - doc/planning/APG-2546/PR-8-remove-pni-oasys-person.md — you're
    working on the surface PR-8 leaves behind

When you're done, report back the PR number, merge SHA, PDF page
count from the regenerated sample, and any surprises.
```

---

## PR-10 — move `organisationName` into referral

```text
Pick up APG-2546 PR-10. The working doc is at
doc/planning/APG-2546/PR-10-organisation-into-referral.md — read
it end to end and follow it. The design decision (JPQL JOIN vs
post-fetch) is already resolved in the doc: post-fetch is what
the service already does for SarOriginalReferral.organisationName,
and PR-10 just wires the parent SarReferral through the same map.
No new query, no schema check. Scope is smaller than the original
skeleton suggested (½ day, not 1 day).

Open a PR using a similar description template to PR-8's.

Assumed starting point: tip of main after PR-8 has merged. PR-9
and PR-11 must be serialised (not parallel) with PR-10 — all three
touch SubjectAccessRequestService.kt + sar_template.mustache and
will merge-conflict otherwise. If you're picking this up mid-flight,
check with the human which of PR-9/10/11 is currently in review
before opening yours.

Also worth reading before you start:
  - doc/planning/APG-2546/ROUND-2-PLAN.md §"Round-2 PR breakdown"
  - The existing originalReferral sub-block in
    sar_template.mustache (line 26 on origin/main @ 0cf89850)
    already carries organisationName — same shape applies to the
    parent referral.

When you're done, report back the PR number, merge SHA, PDF page
count, and any surprises.
```

---

## PR-11 — remove top-level `staff[]` (option (a))

```text
Pick up APG-2546 PR-11. The working doc is at
doc/planning/APG-2546/PR-11-remove-top-level-staff.md — read it
end to end, expand the skeleton into a full working doc, follow
it, and open a PR using a similar description template to PR-8's.

Assumed starting point: tip of main after PR-8 has merged. PR-9
and PR-10 must be serialised with this PR (not parallel) — all
three touch SubjectAccessRequestService.kt + sar_template.mustache.

Option (a) is locked (Deborah confirmed 2026-08-13 pm — see
DELIVERY-LOG round-2 kickoff): just delete the top-level staff[]
list, keep the inline primaryPomStaffSurname /
secondaryPomStaffSurname fields already on each referral.

Also worth reading before you start:
  - doc/planning/APG-2546/ROUND-2-PLAN.md §"Round-2 PR breakdown"
    and §"Impact on PR #1115 (recently merged)" (StaffRepository
    surname-sort query becomes orphaned here)

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

Assumed starting point: tip of main after ALL of PR-8, PR-9,
PR-10 and PR-11 have merged. If any of those are still open, stop
and tell me — this PR needs the combined post-PR-11 state to
sanity-grep for cross-PR interactions.

This is a NO-BEHAVIOUR-CHANGE PR. If your grep sweep finds
something that would change API shape, snapshot output, or
observable service behaviour, that is out of scope for PR-12 —
flag it back to me and I'll decide whether to hot-fix on the
offending round-2 PR or open a follow-on.

Deliverables per the doc:
  - Final orphan-query audit (whole-src/main grep, not per-PR)
  - Dead-DTO scan (SarPniResult, SarOasysPniResult, SarPerson,
    SarOrganisation, SarStaff — all should be gone; flag any
    ghost references)
  - KDoc cross-reference fixup on the surviving SAR-collection
    getters (ReferralRepository.getSarReferrals,
    CourseParticipationRepository.getSarParticipations)
  - SarContractIntegrationTest companion-const cleanup
  - expectedFlywaySchemaVersion still "145" verify
  - Full-suite regression, snapshot regen produces zero diff,
    UUID-leak grep = 0

When you're done, report back the PR number, merge SHA, and
whether anything cross-PR was found (empty PR body is a valid
outcome — the PR still has value as the confirmed-clean
checkpoint before Branston sees the new PDF).
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

