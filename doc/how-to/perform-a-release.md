# Perform a release

## Prerequisites

- Be a part of the Ministry of Justice GitHub organisation. This should be done
  as part of your team onboarding process.
- Be a part of the `accredited-programmes-team` team. This should be done as
  part of your team onboarding process.

## Releasing to the development environment

This will be performed automatically on a merge to the `main` branch providing
the `helm_lint` and `validate` jobs pass on CI.

## Releasing to the preprod environment

Once the `deploy_dev` CI job has passed on CI after merging a branch to `main`,
the pipeline will hold and await manual approval (as documented in the Circle CI
documentation
[here](https://circleci.com/docs/workflows/#holding-a-workflow-for-a-manual-approval))
at the `deploy_preprod` step.

Perform any necessary manual testing on the development environment and ensure
your changes are backwards compatible. For example, if we are working on a
feature that depends a new API integration or a new piece of infrastructure like
a database, then those must be released ahead of the work that depends on it.
Also consider how the service would behave if we needed to roll back that
release - would anything break?

Clicking on that step of the workflow and selecting "Approve" will deploy the
latest version of the code to the preprod environment.

## Releasing to the production environment

Once the `deploy_preprod` CI job has passed on CI after performing the above
step, the pipeline will hold again and await manual approval at the
`deploy_prod` step.

Clicking on that step of the workflow and selecting "Approve" will deploy the
latest version of the code to the production environment.

## Rolling back a release

To rollback a release, you'll need to open a new PR that reverts your previous
release PR. This should then be deployed in the same way as outlined in the
steps above.

If you need to roll back a change urgently you can do this using the Helm
command line client. This is quick and buys you time to back out your change (or
fix forward) via GitHub and CircleCI.

To do this, first get the deployment history:

```
helm history hmpps-accredited-programmes-api
```

This gives you something like:

| REVISION | UPDATED                  | STATUS     | CHART                                | APP VERSION              | DESCRIPTION      |
|----------|--------------------------|------------|--------------------------------------|--------------------------|------------------|
| 79       | Thu Jun 22 10:32:17 2023 | superseded | hmpps-accredited-programmes-api-0.2.0 | 2023-06-22.2240.dd4d7ae | Upgrade complete |
| 80       | Thu Jun 22 13:10:02 2023 | superseded | hmpps-accredited-programmes-api-0.2.0 | 2023-06-22.2240.dd4d7ae | Upgrade complete |
| 81       | Thu Jun 22 13:31:16      | superseded | hmpps-accredited-programmes-api-0.2.0 | 2023-06-22.2240.dd4d7ae | Upgrade complete |

Then roll back to a previous deployment using the revision number:

```
helm rollback hmpps-accredited-programmes-api 80
```

After this, you'll need to go back and revert your PR to ensure the latest
version of the codebase lines up with the latest version of the code to ensure
that the service is in a clean state to be deployed.
