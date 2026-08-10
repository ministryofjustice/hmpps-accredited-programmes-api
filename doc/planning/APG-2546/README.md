# APG-2546 — per-PR working notes

Verbose per-PR docs so a fresh agent can pick any one PR up in a
clean chat window and ship it without needing the full ticket
history in context. Read these alongside the top-level plan at
`../APG-2546-sar-field-removals.md`.

| File | Purpose |
|---|---|
| `00-roxanne-followup.md` | Follow-up message to Roxanne re Q1 / Q2 with internal branching notes. As of 2026-08-04 pm: both Qs resolved (Q1 in person, Q2 on default) — doc is paper-trail only. See DELIVERY-LOG for the concrete outcomes. |
| `DELIVERY-LOG.md` | Running delivery log — status at a glance, timeline entries, contingencies. Update this every time a PR merges or Roxanne answers. |
| `PR-1-remove-audit-records.md` | Delete `auditRecords` section — the "28,483 rows" fix. |
| `PR-2-remove-status-history-and-reasons.md` | Delete `referralStatusHistory` + `referralStatusReasons`. |
| `PR-3-remove-sexual-offence-details.md` | Delete `sexualOffenceDetails` + `selectedSexualOffenceDetails`. |
| `PR-4-remove-oasys-pni-results.md` | Strip `pniResultId` + `oasysAssessmentId` from `oasysPniResults`; keep `prisonNumber` + `programmePathway`. **Option B corrected, confirmed by Roxanne in person 2026-08-04 pm.** Option A retained in doc as superseded / paper-trail only. |
| `PR-5-strip-internal-ids.md` | Delete `SarPerson.id`, `SarOrganisation.id`, and `SarReferral.originalReferralId` (the last fold-in confirmed by Roxanne in person 2026-08-04 pm). All external questions resolved — nothing external gates this PR. |
| `PR-6-osar-round-2-handover.md` | Docs-only. Round-2 handover to OSAR via Option 1 (OSAR-preferred, full-chrome PDF from Cameron's team's SAR dev service) with Option 2 (chrome-less test harness) as fallback. Per Deborah's 2026-08-04 clarification. Sequenced *after* PR-7 so the round-2 PDF reflects the final zero-UUID content shape. |
| `PR-7-strip-original-referral-uuid.md` | Follow-on to PR-5 per its nine-lens review flag 2026-08-05 — delete the retained `SarOriginalReferral.id` UUID from the nested sub-block. Last raw-UUID scrub on the SAR API surface for APG-2546. Confirmed by Raby 2026-08-05 as covered by Roxanne's rows 105 + 111 blanket "No Ids to be included in SAR reports" rule. **Merged `baee4510` 2026-08-07 (#1113). Zero snapshot diff as predicted. Fixture-hardening follow-up flagged during review → captured in DELIVERY-LOG "Deferred follow-ups" section, warmed 2026-08-06 by Deborah's independent fixture-interest thread.** |
| `scripts/dd-notes-sweep.py` | Belt-and-braces DD reader — dumps every note-bearing row on the "Accredited Programmes Custody" sheet, not just red-flagged ones. Missing this sweep on day zero cost us a Q1 Option B correction to Roxanne. Rerun whenever the DD refreshes. Working copy of the xlsx is untracked; either drop a copy at the default path or pass one as argv[1]. |

## Suggested sequencing

> **State as of 2026-08-10:** all six code PRs merged
> (PR-7 merged `baee4510` 2026-08-07). PR-6 (docs-only OSAR
> round-2 handover) is now unblocked and next in line — post
> the template-registration on `#haa-sar-functionality-change-request`
> today. Deborah's fixture-interest thread from 2026-08-06 is
> a live parallel conversation feeding into PR-6's Option 2
> readiness (see DELIVERY-LOG 2026-08-06 entry + "Deferred
> follow-ups").

1. Send `00-roxanne-followup.md` to Roxanne. ✅ done 2026-08-03.
2. Start **PR-1** immediately (blocks nothing). ✅ merged 2026-08-03.
3. Once PR-1 is merged, rebase and start **PR-2**, then **PR-3**,
   then **PR-5** — serial merges keep each snapshot diff readable.
   ✅ PR-2 merged 2026-08-03, PR-3 merged 2026-08-04, PR-5 merged
   `50968d07` 2026-08-05.
4. When Roxanne answers Q1, do **PR-4** (chose Option A or B based
   on her answer). *(Update 2026-08-04 pm: Q1 answered in person,
   corrected Option B confirmed; execute Option B directly.)*
   ✅ merged 2026-08-05 am.
5. Once PR-5 is on `main`, do **PR-7** (strip the retained
   `SarOriginalReferral.id`). Small, scoped, closes out the raw-UUID
   scrub started in PR-5. ✅ merged `baee4510` 2026-08-07 (#1113,
   zero snapshot diff as predicted).
6. When PR-7 is on `main`, do **PR-6** (docs handover). Post
   template-registration request in `#haa-sar-functionality-change-request`
   as soon as PR-7 is on `main` — don't wait until every code PR
   is deployed. Round 1 saw a pipeline block, so give Option 1 as
   much lead time as possible. ⬜ ready to start 2026-08-10.

If Roxanne answers Q1 fast, PR-4 can slot in wherever it fits —
the docs don't assume a specific merge order between PRs 1–5 and
PR-7, only that they land serially so snapshot diffs stay
comprehensible.

## Fresh-agent prompt template

Paste this into a new chat when handing off a PR:

> Pick up APG-2546 PR-N (where N is the PR number you want).
> The working doc is at `doc/planning/APG-2546/PR-N-<slug>.md` —
> read it end to end, follow the "Files to change" section
> literally, run the snapshot regeneration, run the verification
> checklist, and open a PR using the description template at the
> bottom of the doc. Do not deviate from the doc without flagging
> it back to me first. Assumed starting point: tip of `main` after
> PR-(N-1) has merged.

> **PR-7 exception:** the serial merge order is
> `1 → 2 → 3 → 4 → 5 → 7 → 6` (PR-6 is docs-only handover and
> lands *after* PR-7). For PR-7, replace "after PR-(N-1) has
> merged" with **"after PR-5 (#1112) has merged"** — otherwise
> the template points at the wrong predecessor.

