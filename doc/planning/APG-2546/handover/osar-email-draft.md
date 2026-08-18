# OSAR round-2 email draft

> **Status:** template committed 2026-08-18 pm as part of PR-13b.
> Bracketed values (`[CRN]`, `[date]`) get filled in by Raby at 13d
> send time — after 13c commits the sample PDF and Raby knows the
> concrete CRN + generation date.
>
> **To send:** Raby, in the planning-agent chat, after 13c PDF is
> committed on the planning branch. DELIVERY-LOG gets a send-timestamp
> entry after Raby confirms send.
>
> Attach `round-2-sample.pdf` (committed at
> `doc/planning/APG-2546/handover/round-2-sample.pdf`).

---

**To:** David Evans, Sharon Hepworth, Roxanne Stephenson, William Falconer, QAT
**CC:** Cameron Farquhar (template stewardship), Deborah [surname] (SDM, SAR product), Naseem Ashraf, Kiril Kolev
**Subject:** APG-2546 round-2 — Accredited Programmes SAR review PDF (round 2)

Hi all,

APG-2546 round-2 is complete on main (final merge #1120, 2026-08-18)
and deployed to dev. Attached is the round-2 SAR review PDF,
generated via the SAR dev service against CRN [CRN] on [date] —
full standard cover-sheet + top-and-tail pages.

This is a direct response to Deborah's 2026-08-13 review-meeting
action list. All five asks delivered:

1. NOMIS IDs / CRNs removed from the body — they now appear only
   in the wrapper header per Cameron's team's confirmation
   (`prisonerNumber` scrubbed from every referral + course-participation
   row; CRN removed via the PNI-block deletion). PR #1116 + #1117.
2. PNI + OASys PNI blocks removed entirely — SAR consumers get PNI
   data from the ARNs Probation Hub feed, so the ACP copy was
   duplicative. Supersedes DD row 139's earlier "keep pni_result_json"
   annotation. PR #1116.
3. Personal Data section (`person{}`) removed entirely. PR #1116.
4. Organisation is now inline on each referral (`organisationName`
   field), replacing the previous top-level `organisations[]` list —
   organisation now sits in the same context as its referral.
   Demonstrated in the attached PDF by [distinct organisation
   names across the referrals for CRN [CRN]]. PR #1118.
5. Top-level `staff[]` list removed; POM staff surnames continue to
   render inline on each referral (`primaryPomStaffSurname` /
   `secondaryPomStaffSurname`) per Deborah's option (a) decision.
   PR #1119.

Also included in round-2 (hygiene): `PersistenceHelper` tightened
by two dead methods, contract-test fixture widened to demonstrate
per-referral organisation variance, `expectedFlywaySchemaVersion`
held at 145 (V145 kept — rationale on PR #1120).

Ask: content sign-off, 5 working days if you can. Any appearance /
cover-sheet feedback continues to sit with Cameron's team under
APG-2547.

Round-2 close-out condition (recorded for transparency): APG-2546
closes on *feedback received*. Any further change requests from
this review land as a fresh ticket (round-3 scope), not folded
back into APG-2546.

Thanks,
Raby

