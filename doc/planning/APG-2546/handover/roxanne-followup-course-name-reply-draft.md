# Reply to Roxanne — courseName inline on referral (PR-14 kickoff confirmation)

> **Status:** ✅ ready to send. Response to Roxanne's 2026-08-20 email (after she saw the DD-update attachment).
>
> **Context:** She's happy with the DD update ("this is great!"). Two substantive points in her reply:
>
> 1. **On `organisationName` DD placement:** she's fine with row 107 `organisation.name = Yes` as the canonical location — no new row on the `referral` entity needed. Documented in case any issues arise later.
> 2. **New ask:** add `courseName` inline on each referral (same shape as `organisationName`), because right now a referral doesn't reference what course it's for, and subjects with multiple referrals will inevitably follow up asking for clarification.
>
> Her intuition on the course→referral link is correct — `ReferralEntity.offering.course` is already eager-loaded, and `SarOriginalReferral.courseName` already uses exactly this path (`offering?.course?.name`, line 262 of `SubjectAccessRequestService.kt` on `origin/main @ f84f41b2`). Adding the same field on the parent `SarReferral` is a natural completion of PR-10's inline-context pattern — and even simpler than PR-10 was because no new batch query is needed.
>
> **Decision:** fold in as **PR-14** under APG-2546 rather than spinning a new ticket. Rationale:
>
> - It's DD-annotation-side feedback from the reviewer whose sign-off is our current close-out gate — not OSAR/Branston round-3 material (the OOS decision was specifically about Branston asks post-PR-13 sample PDF; Roxanne's DD-review side has always been a distinct close-out condition).
> - Mirrors PR-10 exactly (same one-field-inline pattern, same mustache-row shape). ~15-line diff.
> - Knocking it out same-week means Roxanne can review both the DD and the new field in a single sitting → clean close-out signal.
>
> Working doc: `PR-14-course-name-into-referral.md` (companion to this reply). Not agent-executable yet at draft — Raby to decide whether to fire off a fresh execution agent today or wait.

---

## Draft

**Subject:** Re: APG-2546 — updated DD (In SAR API column) attached

Hi Roxanne,

Great to hear — thanks for confirming the DD row-107 placement for `organisationName`.

Course name inline on referral: yes, you're right that they link. Every referral is made against a specific "offering" (the course-at-a-prison record), and the offering carries the course entity, so we can surface `courseName` on each referral using the same one-field-inline shape as `organisationName` — no new query, just an extra column on the referral table. `SarOriginalReferral` (the nested block for withdrawn/re-submitted referrals) already surfaces `courseName` this way, so this brings the parent referral in line with it — which also makes sense for the "originalReferral vs current referral" side-by-side reading that vettors do.

Going to fold this in as one small round-2 addendum PR (PR-14) rather than spinning a fresh ticket, given it's the same pattern as PR-10 (which put `organisationName` inline) and completes the "referral in context" story you and Deborah kicked off on 2026-08-13. Aiming to have it merged this week; I'll drop you a copy of the updated DD (row for `referral.course_name`, or `course.name` referenced on the referral — whichever fits your DD conventions best) once it's on `main`, so you can review the DD update alongside the fresh preprod PDF.

The new field will render as an extra row inside each referral's summary table, right below "Organisation name" — no rework of the existing sections, no impact on any of the round-2 removals we've already agreed. When you eyeball the next PDF you'll see:

- Status
- Submitted on
- Referrer
- Organisation name
- **Course name** ← new inline row
- OASys confirmed
- Has reviewed programme history
- Primary POM staff surname
- (etc.)

And if a referral has an `originalReferral` sub-block (previously withdrawn version), that already includes `courseName` today — so the two blocks will be readable side by side without you having to cross-reference the top-level Courses section.

Cheers,
Raby

---

## Notes for Raby before sending

- If you'd rather push back on scope and route Roxanne's ask through a fresh ticket per the OOS pattern, the honest read is *no, this doesn't hit the OOS bar* — OOS was for OSAR/Branston round-3 asks after the sample PDF landed. Roxanne's DD-review side has always been an APG-2546 close-out condition, and she's asking for one small pattern-consistent addition before she signs off. Fresh ticket adds cycles and delays close-out for zero technical benefit.
- If timing pressure is real (e.g. you'd rather push PR-14 into next sprint), soften the "aiming to have it merged this week" to "will loop back with an ETA once I've scheduled it in" — but this is a ~½-day-of-agent-work PR, so no reason not to commit unless there's something I don't know.
- Attachment: none needed — this is a text reply confirming the plan. DD update happens on the back of PR-14 merging, not with this reply.

## Post-send follow-through

1. When Roxanne acknowledges (likely quick "great, thanks"): APG-2546 close-out is now gated on PR-14 merging + Roxanne's final DD sign-off.
2. Kick PR-14 off in a fresh chat using `PR-14-course-name-into-referral.md` doc. Same execution pattern as PR-10 (single-agent execution against `origin/main @ f84f41b2`, self-review, PR out, merge).
3. After PR-14 merges: regenerate the sample PDF (or lean on Cameron's team's preprod SAR service now that the template's registered — PDF option 1 is available). Send Roxanne the updated DD row + PDF reference; she confirms; Jira → Done.

---

# ETA follow-up reply — 2026-08-24

> **Status:** ✅ ready to send. Roxanne replied 2026-08-24 asking for a concrete ETA on PR-14 so she can decide whether to pause her testing.
>
> Verbatim: *"Thank you, that would be great. When do you think that is likely to be done? If its going to be relatively soon, I will hold off on completing the testing until this is updated."*
>
> **Honest ETA estimate** (based on round-2 precedent + PR-14's ~10-line scope + current branch state):
>
> - Round-2 precedent: PR-8/9/10/11 all shipped in a single working day (2026-08-17). PR-12 landed the next morning.
> - PR-14 scope: ~10-line diff, no batch pre-fetch needed (simpler than PR-10 was). Fully agent-executable doc anchored at `origin/main @ f84f41b2` (still the tip today).
> - Pipeline: PR-14 merges → CircleCI auto-deploys dev → hold for `deploy_preprod` approval → HAAR-team SAR-preprod re-registration ping → preprod PDF regenerable.
> - Best case: PR-14 kicked off today, merged Tuesday am, preprod deploy + HAAR re-registration Tuesday pm/Wednesday am. **Fresh preprod PDF ready to send Roxanne Wednesday 27/8 afternoon.**
> - Worst case (nothing goes wrong but sprint pressure eats a day): **Thursday 28/8 or Friday 29/8 morning**.
>
> Reply below commits to **end of the week** with a stretch of "possibly Wednesday afternoon", which is honest without over-promising.

---

## Draft

**Subject:** Re: APG-2546 — updated DD (In SAR API column) attached

Hi Roxanne,

Aim is end of this week — realistically **Wednesday afternoon** if
everything runs to pattern (~½-day-of-work PR + a preprod deploy
+ Cameron's team re-registering the updated template on their
preprod service). If anything slips, worst case is Thursday or
Friday morning. Happy to ping you the moment the fresh preprod
PDF is available so you know when to pick testing back up.

Yes, worth holding off on the field-level testing until then —
saves you re-checking the referral summary tables.

Cheers,
Raby

---

## Notes for Raby before sending

- **Kick off PR-14 execution today** — the ETA I'm quoting Roxanne assumes we get moving. Fresh chat with the executing agent, using `PR-14-course-name-into-referral.md` as the doc, anchored at `origin/main @ f84f41b2`. Same execution pattern as PR-8 through PR-12.
- **Sequence discipline post-merge** (already captured in PR-14 doc §"Non-obvious §7"):
  1. PR-14 merges to `main`.
  2. Immediately ping HAAR-team in `#haa-sar-functionality-change-request` asking for SAR-preprod AND SAR-prod re-registration at the new merge SHA, in the *same message*. Do NOT split — that's the mistake that led to the SHA-pointer confusion earlier.
  3. Hold the CircleCI `deploy_preprod` approval until HAAR confirms preprod registration. Then approve.
  4. Regenerate fresh preprod PDF against the same CRN Branston reviewed (A8610DY or comparable).
  5. Send Roxanne the fresh PDF + a note pointing at the new `Course name` row inline on each referral.
- **DD attachment for the final send:** the existing `dd-column-h-update.py` script doesn't need re-running for PR-14 — column H for `course.name` (row 34) is already `Yes` (it's been in the SAR API via the top-level Courses list forever; PR-14 only surfaces it inline additionally). Roxanne agreed on 2026-08-20 that no new `referral.course_name` row is needed on the DD (`"this is fine where it is. We have it documented in case any issues arise in the future"` for the organisationName equivalent — same rationale). So the final Roxanne email won't need a fresh xlsx attachment, just the PDF.
- **If PR-14 hits an unexpected snag** (e.g. `offering.course` is LAZY-loaded and we need to add a batch pre-fetch → half-day extension): update Roxanne with a revised ETA the same day. Don't let her hold testing for something that's actually slipping.

## Post-send follow-through

1. **Kick off PR-14 execution today** — don't let this drift.
2. When PR-14 merges: post the HAAR ping (both envs, same message).
3. When HAAR confirms + preprod deploys + PDF regenerates: reply to Roxanne with the PDF, close the loop.
4. Once she signs off: APG-2546 → Done.


