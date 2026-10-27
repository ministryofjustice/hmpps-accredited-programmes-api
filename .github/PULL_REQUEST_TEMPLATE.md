## Context

<!-- Is there a Trello or Jira ticket you can link to? -->
<!-- Do you need to add any environment variables? -->
<!-- Is an ADR required? An ADR should be added if this PR introduces a change to the architecture. -->

## Changes in this PR

## Release checklist

[Release process documentation](../doc/how-to/perform-a-release.md)

As part of our continuous deployment strategy we must ensure that this work is
ready to be released once merged.

### Pre-merge

- [ ] There are changes required to the Accredited Programmes UI for this change to work...
  - [ ] ... and they been released to production already
- [ ] This PR modifies `src/main/resources/sar_template.mustache`...
  - [ ] ... and I will [register the new template](../doc/how-to/register-sar-template.md) with the SAR team **before deploying** to each environment (dev, preprod, prod). Registering ahead of time stores the new template as `PENDING` and it is auto-promoted to `PUBLISHED` when the worker first fetches it, avoiding any ERRORED SARs during the release.

### Post-merge

- [ ] [Manually approve](../doc/how-to/perform-a-release.md#releasing-to-the-preprod-environment) release to preprod
- [ ] [Manually approve](../doc/how-to/perform-a-release.md#releasing-to-the-production-environment) release to prod

<!-- Should a release fail at any step, you as the author should now lead the work to
fix it as soon as possible. You can monitor deployment failures in CircleCI
itself and application errors are found in
[Sentry](https://ministryofjustice.sentry.io/projects/hmpps-accredited-programmes-api/?project=4505330122686464&referrer=sidebar&statsPeriod=24h). -->
