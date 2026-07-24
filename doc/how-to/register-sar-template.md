# Register the SAR template with OSAR

## Background

Our Subject Access Request (SAR) response is rendered into a PDF by the
[`hmpps-subject-access-request-worker`][worker] using the Mustache template at
[`src/main/resources/sar_template.mustache`](../../src/main/resources/sar_template.mustache).

As a safeguard against unreviewed template changes reaching production, the
worker refuses to render any template whose SHA-256 hash it does not recognise.
Each deployed version of the template must therefore be registered with the
Office for Subject Access Requests (OSAR) / SAR team **in every environment
separately** (`dev`, `preprod`, `prod`).

If the template hash on a running API does not match a registered version, the
SAR UI shows the product as `ERRORED` for every request touching this service,
even though our API itself is returning `200 OK`. Nothing appears in our
Application Insights, because the failure happens inside the worker.

## When to register

Whenever a change to `src/main/resources/sar_template.mustache` is deployed to
an environment. Registration is per-environment, so you will do this three
times over the course of a release: once for `dev` on merge, then again for
`preprod` and `prod` as the release is promoted.

Changes to `SubjectAccessRequestService.kt` that only alter the JSON payload
(fields, ordering) do **not** require registration — only edits to the
`.mustache` file itself change the hash.

## Who to contact

- Slack channel: [`#dps-subject-access-requests`][slack]
- Owning team: Subject Access Request Service
  (`subject-access-request-service@digital.justice.gov.uk`)
- Point of contact who has registered our templates in the past: Dave
  Llewellyn (as of July 2026)

## How to register

1. Confirm the commit that is deployed in the target environment:

   ```sh
   curl -s https://accredited-programmes-api-<env>.hmpps.service.justice.gov.uk/info \
     | jq '.git // .build // .'
   ```

   Replace `<env>` with `dev`, `preprod`, or the production host as
   appropriate.

2. Compute the SHA-256 of the template at that commit. From a checkout of the
   repo:

   ```sh
   git show <commit-sha>:src/main/resources/sar_template.mustache | shasum -a 256
   ```

3. Post a request in [`#dps-subject-access-requests`][slack] with:

   - Service name: `hmpps-accredited-programmes-api`
   - Environment: e.g. `dev`
   - Deployed commit SHA
   - Permalink to the raw template at that SHA, e.g.
     `https://raw.githubusercontent.com/ministryofjustice/hmpps-accredited-programmes-api/<commit-sha>/src/main/resources/sar_template.mustache`
   - The SHA-256 of the file from step 2

   A short message template:

   > Hi — please could you register a new version of our SAR template on
   > `<env>`? Service: `hmpps-accredited-programmes-api`, commit `<sha>`,
   > file: `<raw permalink>`, sha256: `<hash>`. Thanks!

4. Once the SAR team confirms it is registered, retry a SAR for a known
   subject and verify the product no longer shows `ERRORED`. In-flight
   requests are typically retried automatically ~30 minutes after their last
   attempt.

## Verifying you have the right hash

The worker computes the hash over the exact file bytes it fetches from our
`/subject-access-request/template` endpoint. You can reproduce that check
locally:

```sh
curl -s https://accredited-programmes-api-<env>.hmpps.service.justice.gov.uk/subject-access-request/template \
  | shasum -a 256
```

This should match the SHA-256 you sent to the SAR team.

[worker]: https://github.com/ministryofjustice/hmpps-subject-access-request-worker
[slack]: https://mojdt.slack.com/archives/dps-subject-access-requests

