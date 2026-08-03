# Follow-up to Roxanne — APG-2546 Q1 / Q2 nudge

> **Status:** sent 2026-08-03. Text preserved here for the
> paper trail and so we know exactly what defaults + deadline
> Roxanne is expecting.
>
> **Context:** Q1 (oasys_pni_result) and Q2 (organisation.is_national)
> were sent to Roxanne as two separate messages earlier 2026-08-03.
> This follow-up (a) gives her the "no pressure, here's my default if
> you don't reply" bit so she knows we're not blocked and (b) adds
> nothing that contradicts the framing already sent.
>
> **Do NOT resend the A / B options** — she has them from the
> originals. Restating them would just make her scroll and wonder if
> the terms have changed.

---

## Slack / email draft

Hi Roxanne,

Small update on the two questions I sent earlier (oasys_pni_result
and organisation.is_national) — no reply needed yet if you haven't
got round to them, this is just so you know how I'm working around
them.

Since sending those I've walked the code properly with a colleague
and confirmed both options on oasys_pni_result are the same effort
at our end — roughly half a day either way — so genuinely no
pressure on which you pick. And on is_national I've double-checked
the SAR code and it really isn't in the payload today, so if you're
happy with "leave it off" that's a zero-work answer at our end. If
you'd like it added, no problem — I'd spin up a separate small
ticket rather than fold an add into this batch of removes.

I'm not sitting blocked on either — I'll be cracking on with the
audit_records / referral_status_history / sexual_offence_details
removals in the meantime. Those are the ones that turn the
8,000-page PDF into something a human can actually read, so worth
getting through first anyway.

If you can point me at answers by end of next week that would be
ideal. After that I'd default oasys_pni_result to Option A (remove
the whole section) so it doesn't hold up the rest, and I'd assume
is_national is "leave it off" unless you say otherwise. Both are
easy to change later if your steer comes in after the fact.

Thanks again — and no rush, just wanted to flag the "here's what
I'll do if I don't hear back" bit so you know nothing's stuck.

All the best,
Raby

---

## Internal notes (not for Roxanne)

Deadline for a reply before we act on defaults: **Friday
2026-08-14** (end of next week from send date). Set a calendar
nudge for the Monday after (2026-08-17) to either kick off PR-4
Option A or ping her again in `#osar-review`.

Default behaviour if she doesn't reply by 2026-08-14:

- **Q1 default → Option A** (remove whole `oasysPniResults` section).
  Rationale: consistent with her 29.07 blanket "all IDs → No" rule;
  the remaining single field (`programme_pathway`) on its own gives
  a subject minimal context without the ID scaffolding around it, so
  a whole-section removal is the cleaner default. Easy to add back
  as Option B if she comes back later saying "actually keep the
  pathway".
- **Q2 default → A / leave off**. Rationale: consistent with the
  previous won't-do call on APG-2494 (marked won't-do earlier in the
  year); the field simply isn't in the code today, so "do nothing"
  is safe. If she pushes for it later, spin fresh ticket.

Branching from her answers into the working docs:

- **Q1 = A** → `PR-4-remove-oasys-pni-results.md`, Option A branch.
  Branch name `APG-2546/remove-oasys-pni-results` (already the
  planned name).
- **Q1 = B** → `PR-4-remove-oasys-pni-results.md`, Option B branch.
  Rename branch to `APG-2546/strip-oasys-pni-result-ids`. Option B
  as offered to her strips **all three IDs** (`pni_result_id`,
  `prison_number`, `oasys_assessment_id`), keeping only
  `programme_pathway`. PR-4 doc already matches that definition
  (fixed 2026-08-03).
- **Q2 = "leave off"** → close APG-2494 as won't-do (again), no
  code change.
- **Q2 = "please add"** → new ticket (revive APG-2494 or spin
  fresh). Do not fold into APG-2546 — different shape (add not
  remove) and different reviewer path.


