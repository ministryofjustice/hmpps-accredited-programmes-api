# APG-2546 — per-PR working notes

Verbose per-PR docs so a fresh agent can pick any one PR up in a
clean chat window and ship it without needing the full ticket
history in context. Read these alongside the top-level plan at
`../APG-2546-sar-field-removals.md`.

| File | Purpose |
|---|---|
| `00-roxanne-followup.md` | Follow-up message to Roxanne re Q1 / Q2, with internal branching notes. |
| `PR-1-remove-audit-records.md` | Delete `auditRecords` section — the "28,483 rows" fix. |
| `PR-2-remove-status-history-and-reasons.md` | Delete `referralStatusHistory` + `referralStatusReasons`. |
| `PR-3-remove-sexual-offence-details.md` | Delete `sexualOffenceDetails` + `selectedSexualOffenceDetails`. |
| `PR-4-remove-oasys-pni-results.md` | Delete `oasysPniResults` (Option A) *or* strip its two IDs (Option B) — blocked on Roxanne's Q1 answer. |
| `PR-5-strip-internal-ids.md` | Delete `SarPerson.id` and `SarOrganisation.id`. Independent of Q2. |
| `PR-6-osar-round-2-review-pdf.md` | Docs-only. Regenerate the review PDF and hand over to OSAR. |

## Suggested sequencing

1. Send `00-roxanne-followup.md` to Roxanne.
2. Start **PR-1** immediately (blocks nothing).
3. Once PR-1 is merged, rebase and start **PR-2**, then **PR-3**,
   then **PR-5** — serial merges keep each snapshot diff readable.
4. When Roxanne answers Q1, do **PR-4** (chose Option A or B based
   on her answer).
5. When all five are on `main`, do **PR-6** (docs handover).

If Roxanne answers Q1 fast, PR-4 can slot in wherever it fits —
the docs don't assume a specific merge order between PRs 1–5, only
that they land serially so snapshot diffs stay comprehensible.

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

