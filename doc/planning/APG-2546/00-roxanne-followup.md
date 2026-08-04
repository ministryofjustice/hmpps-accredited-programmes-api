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

> **📌 This section is superseded — kept for the paper trail.**
> Both Qs resolved 2026-08-04 pm (Q1 in person, Q2 on default).
> No calendar nudge needed; no PR-4 default kick-off decision to
> make. See DELIVERY-LOG "Roxanne in-person answers 2026-08-04
> pm" and "Q2 closed on default" for the concrete outcomes.

Deadline for a reply before we act on defaults: **Friday
2026-08-14** (end of next week from send date). Set a calendar
nudge for the Monday after (2026-08-17) to either kick off PR-4
Option A or ping her again in `#osar-review`.

Default behaviour if she doesn't reply by 2026-08-14:

> **Both defaults have been overtaken by events** — Q1 answered
> in person 2026-08-04 pm (see DELIVERY-LOG "Roxanne in-person
> answers 2026-08-04 pm"); Q2 closed on default 2026-08-04 pm
> (see DELIVERY-LOG "Q2 closed on default"). The original text
> below is kept for the paper trail only.

- **Q1 default → Option A** (remove whole `oasysPniResults` section).
  Rationale: consistent with her 29.07 blanket "all IDs → No" rule;
  the remaining single field (`programme_pathway`) on its own gives
  a subject minimal context without the ID scaffolding around it, so
  a whole-section removal is the cleaner default. Easy to add back
  as Option B if she comes back later saying "actually keep the
  pathway".
  → **N/A — Q1 answered in person, corrected Option B confirmed.**
- **Q2 default → A / leave off**. Rationale: consistent with the
  previous won't-do call on APG-2494 (marked won't-do earlier in the
  year); the field simply isn't in the code today, so "do nothing"
  is safe. If she pushes for it later, spin fresh ticket.
  → **Applied 2026-08-04 pm.** Roxanne had face-to-face window and
  did not raise, so the follow-up's "unless you say otherwise" clause
  locked the default in. APG-2494 stays won't-do.

Branching from her answers into the working docs:

> **📌 Superseded — kept for the paper trail.** Actual outcome:
> Q1 = corrected Option B on branch
> `APG-2546/strip-oasys-pni-result-ids` (see PR-4 doc); Q2 =
> "leave off" default applied (APG-2494 stays won't-do).

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

---

## Q1 Option B correction (2026-08-04 pm) — needs sending

> **Status:** ⚠️ NOT SENT — SUPERSEDED. Roxanne answered the
> whole correction stack (Option B scope, `oasys_assessment_id`,
> `original_referral_id`) in person on 2026-08-04 pm before the
> correction message went out. Answers recorded below and
> propagated into DELIVERY-LOG timeline + PR-4 / PR-5 docs.
> The draft is kept here for the paper trail; do NOT send.
>
> **Roxanne's in-person answers (2026-08-04 pm):**
> - **Option B scope correction** ("`prison_number` should stay,
>   not be stripped"): ✅ confirmed. Corrected Option B stands
>   as "strip `pni_result_id` + `oasys_assessment_id`, keep
>   `prison_number` + `programme_pathway`".
> - **`oasys_assessment_id`** (was ambiguous): ✅ strip. Aligns
>   with the "OASys system reference, not user-facing" default.
> - **`original_referral_id`** on `SarReferral` (was flagged as
>   PR-5 potential extension): ✅ strip the UUID (fold into
>   PR-5 scope). The resolved `originalReferral` sub-block
>   stays; only the raw UUID + its "Original referral ID"
>   template row are removed.
>
> **What this unblocks:**
> - PR-4 → ⬜ ready to start on the corrected Option B branch
>   (`APG-2546/strip-oasys-pni-result-ids`).
> - PR-5 → scope extension confirmed (originalReferralId fold-in).
> - Q2 (`is_national`) — unchanged, still open.

### Slack / email draft

> Hi Roxanne — quick correction on the oasys_pni_result Q1 I
> sent, sorry for the extra ping. I did a wider sweep of the
> DD notes column this afternoon and spotted that Option B as
> I framed it would strip `prison_number` off the row — but
> row 86 has a dev note from 10.07 saying `prison_number`
> should stay, and it wasn't in your 29.07 "all IDs → No"
> pass. That's my mistake in the write-up — `prison_number`
> is the PRN, not an internal ID, and every other section of
> the SAR keeps it.
>
> So Option B is really "strip `pni_result_id` (and probably
> `oasys_assessment_id` — see below), keep `prison_number`
> and `programme_pathway`". Option A is unchanged.
>
> While I'm here — `oasys_assessment_id` (row 87) has a
> similar 10.07 "should be on the report" note but your 29.07
> blanket "all IDs → No" arguably covers it too. My default
> would be to strip it (it's an OASys system reference, not
> user-facing), but if you'd prefer to keep it as a link back
> for reviewers, that's fine — one-line change either way.
>
> No response needed if Option A is where you land. If it's
> B, a thumbs-up on "keep `prison_number` + `programme_pathway`,
> strip both IDs" is all I need. Same deadline (Fri 14th) and
> same defaults otherwise.
>
> Thanks — Raby

### Internal notes on this correction

- **Send channel:** same as Q1 (`#osar-review` / DM — whichever
  Q1 went out on).
- **Timing:** send before Roxanne's Q1 response lands. If her
  response has already come in as "B" under the old framing,
  send this as a "just to confirm before I cut the PR" check.
- **If she doesn't reply to the correction by 2026-08-14:**
  default is still Option A (unchanged from original follow-up).
  If she has already replied "B" and doesn't respond to the
  correction, act on the *corrected* B (keep `prison_number` +
  `programme_pathway`, strip both IDs) — the DD is her source
  of truth and the correction only removes a mistake we
  introduced, not something she asked for.
- **Extra topic to fold in if you're sending this anyway:**
  `SarReferral.originalReferralId` (row 165) — 10.07 dev note
  "pull referral data (if not already) do not add the uuid".
  Not red-flagged. We already pull the referral data (as
  `originalReferral` sub-block); the raw UUID and its template
  row (`sar_template.mustache:14`) should probably be
  stripped. See PR-5 doc §"Potential scope extension" for the
  code position and the (a)/(b) decision. Reasonable to add a
  short "and while we're here" paragraph asking her to
  green-light stripping `originalReferralId` too — natural
  extension of PR-5 rather than a separate ticket.


