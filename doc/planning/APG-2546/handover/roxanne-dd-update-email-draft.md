# Reply to Roxanne — DD column-H update for APG-2546 rounds 1 + 2

> **Status:** ✅ ready to send. Attach the updated xlsx.
>
> **In reply to:** Roxanne's 2026-08-19 email — *"could you please ensure that the 'In SAR API - Y/N or N/A' column has been updated in the data dictionary? The copy I currently have does not appear to contain the latest updates."*
>
> **Attachment:** `Copy of 2026.07.08_copy_Probation Digital Data review December 251_APG-2546-round-2-update.xlsx` (207 KB, in `~/Downloads/`).
>
> **Baseline used:** the same July 8 copy Roxanne distributed — safe to eyeball against her own copy. If her master has drifted since July 8, she can fold the row-by-row delta table below into her master instead of replacing outright.
>
> **Also updated:** `DELIVERY-LOG.md` — records this email + the two DD-drift corrections (rows 109 + 224). The updater script lives at `doc/planning/APG-2546/scripts/dd-column-h-update.py` so any future refresh can re-run it against a newer baseline.

---

## Draft

**Subject:** Re: APG-2546 — updated DD (In SAR API column) attached

Hi Roxanne,

Thanks — and glad the service is up in pre-production for you to review against.

Attached is your July 8 baseline with the "In SAR API - Y/N or N/A" column brought in line with the code that's now on preprod (`main @ 99264496`, all five round-2 PRs merged 17–18/8). If your master DD has moved on since the July 8 copy you sent us, please treat the attachment as the source of truth for column H on Accredited Programmes Custody rather than as a full replacement — the row-by-row delta below should let you fold it into your master directly.

**Total changes: 69 rows on the `Accredited Programmes Custody` sheet, column H only.** No other sheets touched, no other columns touched.

### Summary of what's now absent from the SAR API

The five OSAR asks from Deborah's 2026-08-13 meeting have all landed:

1. **NOMIS IDs / CRNs (already in the SAR wrapper header)** — `prisonerNumber` scrubbed from every surviving referral row and course-participation row; `crn` gone with the PNI section.
2. **PNI data (now sourced by SAR consumers via ARNs Probation Hub)** — the whole `pniResults[]` and `oasysPniResults[]` blocks are removed. This supersedes row 139's 10.07 note *"these are in SAR report hence H should be Yes"* — happy to loop back if you'd like context. Deborah's team agreed on 2026-08-13.
3. **Personal Data section** — the whole `person` block is removed.
4. **Organisation folded into referral** — top-level `organisations[]` gone; each referral now carries `organisationName` inline in the same context as the referral it belongs to. Row 107 (`organisation.name`) stays as **Yes** because that's the field now inline on referral — happy to add a dedicated row on the `referral` entity if you'd rather see it there in the DD.
5. **Top-level Staff list gone** — but the primary/secondary POM staff surnames still render inline on each referral, so `staff.last_name` (row 242) stays **Yes**.

Round-1 removals are also folded in (audit records, referral status history + reasons, sexual offence details, internal UUIDs on Person / Organisation / SarReferral.originalReferralId / SarOriginalReferral.id).

### Row-by-row delta — 69 changes

**67 rows flipped Yes → No** (removed from SAR API), grouped by the PR that caused each removal:

| Rows | Entity | PR | Why |
|---|---|---|---|
| 22, 23, 24, 25, 27, 28, 29, 30, 31 | `audit_record` | PR-1 #1107 | Whole `auditRecords` section removed. Matches your 29.07 "should be a no" notes on all nine. |
| 47 | `course_participation.prison_number` | PR-9 #1117 | NOMIS ID scrubbed (in wrapper header). |
| 85, 86, 87, 88 | `oasys_pni_result` (all 4 fields) | PR-8 #1116 | Whole section gone (PR-4 also stripped 2 IDs in round 1). |
| 105, 106, 108, 109 | `organisation.{organisation_id, code, gender, is_national}` | PR-5 #1112 + PR-10 #1118 | Top-level `organisations[]` block removed; only `name` survives inline on referral. Row 109 = DD drift correction (see below). |
| 111–124 (14 rows) | `person` (all fields) | PR-5 #1112 + PR-8 #1116 | Whole Person block removed. |
| 129, 130, 132–140 (11 rows) | `pni_result` (surviving fields) | PR-8 #1116 | Whole PNI results section removed — see PNI note above re row 139. |
| 153 | `referral.prison_number` | PR-9 #1117 | NOMIS ID scrubbed (in wrapper header). |
| 165 | `referral.original_referral_id` | PR-5 #1112 + PR-7 #1113 | UUID stripped; resolved sub-block (courseName, submittedOn, referrerSurname, organisationName, statusCode etc.) is retained without a UUID — matches your 10.07 note *"pull referral data (if not already) do not add the uuid"* exactly. |
| 192–202 (11 rows) | `referral_status_history` (all fields) | PR-2 #1109 | Whole section removed. Matches your 29.07 "should be a no" notes on all. |
| 205–209 (5 rows) | `referral_status_reason` (all fields) | PR-2 #1109 | Whole section removed. Matches your 29.07 "should be a no" notes. |
| 226, 227, 228 | `selected_sexual_offence_details` (all 3 fields) | PR-3 #1110 | Whole section removed. |
| 233, 234, 235, 237 | `sexual_offence_details` (4 red-flagged fields) | PR-3 #1110 | Whole section removed. Row 236 `hint_text` stays **No** (was never in SAR). |

**2 rows flipped as DD-drift corrections** (code truth vs baseline — not APG-2546 removals):

- **Row 109 `organisation.is_national`** — `Yes → No`. Code has always been No; row 109 was flipped to Yes on 10.07 during a dev clarification, but on 04.08 (in-person) we agreed to leave `is_national` off pending a fresh ticket, which lines up with APG-2494's earlier won't-do call. Flipping H back to No lines the DD up with the code.
- **Row 224 `referrer_user.referrer_username`** — `No → Yes`. Your row-224 note reads *"Yes if we can provide surname"*. We do — since APG-2492 the referrer's username is resolved to a surname before it's returned. So the answer to the *"if"* is yes, and row 224 should reflect that.

### One optional add — new row on `referral`?

The `organisationName` field is now present inline on each referral (mirrors your row-165 pattern for resolved originalReferral data). There isn't currently a row for it on the `referral` entity; you could either (a) leave row 107 `organisation.name = Yes` as the canonical source of truth (what the attachment does) or (b) add a new row `referral.organisation_name` = Yes if you'd like it visible on the referral entity. Happy to do either — let me know what fits your DD conventions.

### Sanity checklist

If you'd like to eyeball independently against a preprod-generated SAR PDF, the sample we sent Branston yesterday for CRN A8610DY is a rich exemplar — three surviving `<h2>` sections (Referrals, Course participation, Courses), no top-level Staff / Organisation / Person / PNI / OASys-PNI blocks, `organisationName` visible on each referral. All the removals in this DD update are directly observable in that PDF.

Ticket: APG-2546. Preprod ACP is on `2026-08-18.532.9926449` (verified via pod `/info` yesterday). Shout if anything on the delta table looks off and I'll dig in.

Cheers,
Raby

---

## Notes for Raby before sending

- Attachment path: `~/Downloads/Copy of 2026.07.08_copy_Probation Digital Data review December 251_APG-2546-round-2-update.xlsx`.
- If the `[Deborah 2026-08-13 decision on PNI]` phrasing is uncomfortable given Roxanne's earlier 10.07 update on row 139, feel free to soften — the paper-trail authority is Deborah's meeting outcome, not our unilateral call.
- If Roxanne responds asking for the delta as a filter/highlight inside the attachment (rather than a text table in the email), we can regenerate with cell-highlighting on the 69 changed rows — the updater script has the row list already; adding an openpyxl `PatternFill` on col H is one extra line.
- After send, ping the planning-agent chat with the send timestamp so I log it in DELIVERY-LOG.

## Post-send follow-through

- **If Roxanne says thanks + no more asks:** she can close the DD-review side of round-2; APG-2546 close-out condition (Branston / OSAR feedback received) is already met, and this email closes the DD-annotation loop. Transition APG-2546 to Done at that point.
- **If she wants row 139 (`pni_result_json`) reconsidered:** loop Deborah + Cameron's team — the "PNI via ARNs Probation Hub" decision is theirs to authoritatively confirm; we implemented against it.
- **If she wants the DD organised differently for round-2 (e.g. new referral.organisation_name row):** happy small doc-only change; not APG-2546 scope but same-week turnaround.

