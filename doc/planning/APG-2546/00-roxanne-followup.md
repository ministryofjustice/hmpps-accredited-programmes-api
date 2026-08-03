# Follow-up to Roxanne — APG-2546 Q1 / Q2 clarifications

> **Context:** Q1 (oasysPniResults) and Q2 (`is_national`) were already
> sent to Roxanne. On re-reading the code we can tighten the wording so
> she can answer them with a single yes/no, and pre-empt one likely
> follow-up. Draft below is written to be pasted straight into Slack /
> email as a reply to the original message — no preamble needed.

---

## Slack / email draft

Hi Roxanne — quick follow-up to the two questions I sent earlier, now
that I've had a chance to walk the code and confirm what's actually
in the SAR today. Nothing has changed on your end; I just want to
make sure the options I gave you match reality.

**Q1 — `oasysPniResults` section**

I said Option B was "keep `programme_pathway`, remove
`pniResultId` / `prisonNumber` / `oasysAssessmentId`". Having looked
at the DTO, the section currently exposes exactly four fields:

- `pniResultId` (internal ID)
- `prisonNumber`
- `oasysAssessmentId` (internal ID)
- `programmePathway` (category — e.g. `HIGH_INTENSITY_BC`, not an ID)

So the concrete options are:

- **Option A** — remove the whole section (nothing surfaces to the
  subject about their OASys PNI result).
- **Option B** — strip only the two internal IDs (`pniResultId`,
  `oasysAssessmentId`), leaving `prisonNumber` + `programmePathway`
  visible to the subject.

Both are ~half a day of work. Which would you like?

**Q2 — `is_national` on the organisation object**

I've double-checked the SAR API and `is_national` isn't in the
payload today — never was. Your row (109) was marked "should be in
new SAR report" back on 10.07 but nothing has been added since. Two
possibilities:

- The dev who told you it was landing was mistaken, and the row is
  stale — in which case we do nothing and I'll mark it won't-do.
- You do want it added — in which case we spin up a separate ticket
  (this would be an addition, not a removal, so it sits outside
  APG-2546's scope).

A one-word answer ("stale" or "add") is enough to unblock me.

Cheers,
Raby

---

## Internal notes (not for Roxanne)

- **If Q1 = A** → PR-4 is a full section delete (see
  `PR-4-remove-oasys-pni-results.md`, "Option A" branch).
- **If Q1 = B** → PR-4 is a two-field DTO strip (see PR-4, "Option B"
  branch). Rename branch to `APG-2546/strip-oasys-pni-result-ids`.
- **If Q2 = stale** → close APG-2494 as won't-do, no code change.
- **If Q2 = add** → new ticket (revive APG-2494 or spin fresh). Do
  not fold into APG-2546 — different shape (add not remove) and
  different reviewer path.

Timebox for Roxanne's reply: **3 working days**. If nothing by
2026-08-06, ping in `#osar-review`. PRs 1, 2, 3, 5 do not depend on
her answer and should be underway by then.

