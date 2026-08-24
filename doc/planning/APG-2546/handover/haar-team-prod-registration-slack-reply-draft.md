# Slack reply — HAAR team's prod-registration mismatch alert (2026-08-21 → re-framed 2026-08-24)

> **Status:** ✅ ready to send — CORRECTED framing after re-reading the Thursday 2026-08-20 registration thread.
>
> **Correction from Friday's first draft:** the first draft owned the miss as *"we never asked for prod registration, that's on us"*. Re-reading the Thursday 2026-08-20 thread shows Dave Llewellyn actually registered prod at 16:06 BST Thursday (via Deborah's in-thread ask right after he'd done preprod). So the accurate framing isn't "we missed a step" — Deborah caught it mid-thread and Dave actioned it same day.
>
> **Actual root cause of the Friday alert (verified 2026-08-24 am against `origin/main`):** Dave's Thursday-registered SHA (`99264496`, the revision Raby's preprod-registration draft pointed at) is not the same string as prod ACP's deployed SHA (`f84f41b2`, promoted Thursday ~13:07 BST). HAAR's monitoring compares SHA pointers, not template bytes. But:
>
> ```zsh
> git diff 99264496..f84f41b2 -- src/main/resources/sar_template.mustache
> # zero-diff — byte-identical
> git log --oneline 99264496..f84f41b2
> #   f84f41b2 PR #1122 PNI-assessment predictors
> #   418b6950 PR #1121 PNI-assessment model changes
> #   (both unrelated to SAR template)
> ```
>
> So the alert is real on the SHA-pointer check but a false positive on template content. Two paths forward: (a) Dave bumps the prod registration pointer to `f84f41b2`, silencing the alert; (b) HAAR agree the alert is safe to ack because the template bytes match. Not our call which they prefer.
>
> **Prod / preprod state (verified 2026-08-24 am):**
>
> - Prod image: `2026-08-20.546.f84f41b` (deployed 2026-08-20, unchanged since; some pods 3d19h old, some 60m from routine reschedules).
> - Preprod image: same, `2026-08-20.546.f84f41b`.
> - Nothing on `origin/main` past `f84f41b2` — main still tip = same commit as Thursday.

---

## Draft (Slack reply, in-thread on the same 2026-08-20 channel)

```
Morning — following up on Thursday's registration thread + Friday's
alert on the same. Ran the numbers again this morning:

The mustache Dave registered on Thursday 16:06 (SHA 99264496) is
byte-identical to what's on prod today (deployed SHA f84f41b2).
The two commits between them (#1121, #1122) are PNI-assessment
predictor work, no template touch:

  git diff 99264496..f84f41b2 -- src/main/resources/sar_template.mustache
  # zero-diff

So Friday's alert looks like a SHA-pointer housekeeping mismatch,
not an actual template drift — HAAR monitor comparing registered
SHA string vs deployed SHA string, understandably firing when
they don't match even if the bytes are the same.

Two ways to clear it, either fine on our side:

  (a) Dave bumps the prod registration pointer to f84f41b2 — same
      template bytes, zero risk, quickest silencing of the alert.
  (b) You ack the alert on your side because the bytes match.
      Lower fidelity going forward (a future prod deploy that
      does change the mustache wouldn't re-fire against Thursday's
      registered SHA) but that risk is captured next paragraph.

Heads up either way — we've got one more mustache-touching PR
lined up (PR-14, folding courseName inline on each referral — same
shape as PR-10's organisationName fold-in, Roxanne asked for it
after seeing the round-2 sample PDF). When it merges I'll ping in
this channel asking for registration on BOTH preprod + prod, same
day, so we don't repeat this. Aiming this week; won't approve the
deploy_prod hold until you've confirmed registration.

Ticket: APG-2546 (same as the round-2 work).

Thanks,
Raby
```

---

## Notes for Raby before sending

- **Do NOT send the earlier "we missed the ping, that's on us" version.** That framing was wrong; Thursday's thread proves the ping happened and Dave did prod. This corrected version cites the actual issue (SHA-pointer vs byte comparison).
- **Tone:** helpful/analytical, not defensive. Owns nothing that isn't ours to own. Offers a concrete way forward for both sides. Also pre-empts the PR-14 situation so we don't have this conversation a third time.
- **The (a)/(b) choice belongs to HAAR.** Both are technically fine; (a) is cleaner. Dave will probably just do (a) in 30 seconds. Don't insist.
- **Attach nothing.** Text-only Slack thread reply.
- **If the alert has since auto-cleared** (weekend maintenance / monitoring re-run after Dave's Thursday 16:06 registration finally propagated), Dave might just reply "already cleared, all good". That's fine — the message still teed up the PR-14 sequencing which is worth capturing before it lands.

## Post-send follow-through

1. Log the exchange in DELIVERY-LOG (already covered in the corrected 2026-08-21 entry — the 2026-08-24 re-framing note appended).
2. On PR-14 merge: single Slack ping asking for registration on BOTH preprod + prod in the same message.
3. Then don't approve `deploy_preprod` / `deploy_prod` CircleCI holds until both confirmations land — new discipline captured in PR-14 doc §"Non-obvious §7" (updated to reflect today's re-framing).

## What NOT to do

- Do not push a fix or PR for the SHA-pointer situation itself — HAAR's monitoring behaviour is theirs, not ours. If they want byte comparison instead of SHA comparison that's a HAAR-team tooling decision, not an APG-2546 scope item.
- Do not add "always ping with the deployed SHA specifically" as a rule — the reason Thursday's ping cited `99264496` is because that's what our preprod-registration draft said, which was accurate at the time preprod was on `99264496`. The gap is between (SHA-when-preprod-ping-drafted) and (SHA-when-prod-is-later-deployed) — a moving-target problem, not a "forgot to update the SHA" problem. Cheapest fix is (a) or (b) above per event.
- Do not spin a fresh ticket for the process improvement (the checklist item in `perform-a-release.md`) yet — see if the PR-14 sequencing discipline actually holds first; if it does, we know the human process works and the checklist is nice-to-have; if it doesn't, we know we need real automation.

