# Slack draft — Cameron's SAR product team, PREPROD template re-registration

**Purpose:** register the round-2 mustache template change on the SAR **preprod** service, now that:

- OSAR content sign-off is in (round-2 handover PDF from the SAR dev-service passed Branston's review — 2026-08-19).
- All five round-2 code PRs are deployed on preprod ACP (`99264496`, verified via pod `/info` and image tag `2026-08-18.532.9926449`).

**Distinct from the dev registration** (`cameron-template-registration-slack-draft.md`) already actioned 2026-08-18: the SAR service maintains a separate registered template per environment, so preprod needs its own re-registration before real preprod SAR requests targeting Accredited Programmes render the round-2 shape.

**Why required (not optional):** identical rationale to the dev ping — the round-2 `sar_template.mustache` has changed materially vs the round-1 revision. If the SAR preprod service still holds the round-1 revision, real preprod SAR requests hitting the Accredited Programmes payload will render sections OSAR just signed off as removed.

Same template diff for the record:

```zsh
git diff baee4510..99264496 -- src/main/resources/sar_template.mustache
```

**Channel:** `#haa-sar-functionality-change-request`
**Ticket:** APG-2546

## Draft

```
Hi team — thanks again for the quick dev re-registration on 18/8.
Now that OSAR have signed off round-2 content, please could you
also re-register the Accredited Programmes SAR mustache template on
your PREPROD service?

Same revision — main @ 99264496 (PR #1120 merge, 2026-08-18):
https://github.com/ministryofjustice/hmpps-accredited-programmes-api/blob/99264496/src/main/resources/sar_template.mustache

Preprod ACP is already on that build — image tag
2026-08-18.532.9926449, verified via pod /info this morning
(git.commit.id = 9926449). So preprod ACP will emit the round-2
SAR payload shape as soon as your preprod service starts asking
for it against the newly registered template.

Five <h2> block deletions + one row added + two rows removed vs
the round-1 registered revision (baee4510) — same diff as the dev
re-registration on 18/8, no additional changes since:

  #1116 PR-8  — removed PNI results, OASys PNI results, Person <h2>s
  #1117 PR-9  — scrubbed Prisoner number rows
  #1118 PR-10 — inline Organisation name row per referral;
                removed top-level Organisations <h2>
  #1119 PR-11 — removed top-level Staff <h2>
  #1120 PR-12 — hygiene / fixture widening (no template shape change)

If the test nDelius account we've been using retains SARBT001 on
preprod as well, that would cover the last dependency for a live
round-trip check. Happy to run a preprod-side sanity generation
against a rich preprod CRN once you confirm registration — mainly
to sense-check that preprod SAR service + preprod ACP payload
render the same content OSAR already signed off in dev.

Ticket: APG-2546. Cheers.

Raby
```

## Follow-up expectations

- **Re-registration confirmed at `99264496` on preprod** → log the confirmation timestamp in DELIVERY-LOG under the "Preprod template re-registration" entry. Optionally run a preprod-side sanity generation against a rich preprod CRN (e.g. `A8610DY`, same one used for the dev-service Branston PDF) as a belt-and-braces round-trip check — content should match the already-signed-off dev output modulo the CRN's data drift.
- **Cameron's team asks for a different revision** → still `99264496`. No SHA has moved on `origin/main` since dev re-registration.
- **SARBT001 rotation needed on preprod test account** → parallel identity-team request.
- **Silence after 24h** → chase in-channel.

## Notes for the record

- This is a **preprod-service** registration, not a preprod-ACP deploy — the ACP side is already validated deployed (see DELIVERY-LOG 2026-08-19 entry: pod `/info` = `9926449`, image tag `2026-08-18.532.9926449`, all five round-2 merge SHAs are ancestors of the deployed image commit).
- Post preprod re-registration, prod is the only outstanding environment for both sides (ACP prod deploy + SAR-service prod template registration). Neither is scoped to APG-2546 close-out — APG-2546 closed on OSAR content sign-off. Prod promotion runs on the normal CircleCI `deploy_prod` manual-approval gate whenever we're ready.

