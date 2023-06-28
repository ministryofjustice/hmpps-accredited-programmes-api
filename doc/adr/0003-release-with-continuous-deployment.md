# 4. Release with continuous deployment

Date: 2023-06-22

## Status

Accepted

## Context

The Accredited Programmes service is about to launch a private beta,
piloting with a small number of prisons, and as such we are focused on
delivering quickly which requires as little release overhead as possible.

MOJ provide the [tools for continuous
deployment](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/deploying-an-app/using-circleci-for-continuous-deployment.html),
configured as part of the
[TypeScript](https://github.com/ministryofjustice/hmpps-template-typescript/blob/eac50e97924df530e1649dc4ccd4339f4491789d/.circleci/config.yml#L148)
and
[Kotlin](https://github.com/ministryofjustice/hmpps-template-kotlin/blob/4c7bc649f8bf8deb8dc49af58c90fd1e9e085437/.circleci/config.yml#L66)
template repositories. This is an approach taken by other projects at MOJ.

As an (at the time of writing) external team we need to be mindful that when
handing over the project there will be advantages to having a release process
consistent with other MOJ services.

We have seen other teams that [have diverged from continuous
deployment](https://dsdmoj.atlassian.net/wiki/spaces/MRD/pages/4228547717/Release+process)
and are releasing in batches. We are concerned that adding a heavier process
like this while we’re still in private beta may introduce overheads that will
slow down delivery.

## Decision

We will release using continuous deployment. The author of a pull request should
be responsible for releasing their change through to production before
considering the work done. We'll perform both manual and automated testing on
the development environment before releasing changes to production to ensure
everything is working as expected.

This will be done through manual approval of a release to each environment when
a change is merged to the `main` branch.

## Consequences

There will be no big bang releases for the team to organise and navigate.

The author of a piece of work has the most context around the work and when
shipping it in isolation they should be best placed to ship any fixes quickly.

By treating each change as a smaller, releasable piece of work and shipping them
quickly we can deliver to users and catch bugs earlier. Once caught we are then
set up to release fixes quickly.

We'll follow the convention found in MOJ which should make it more familiar when
we exit and hand the project back to MOJ.

We'll need to make sure all changes to the codebase are backwards compatible,
across both sides of the service (UI and API). For example, if we are working on
a feature that depends a new API integration or a new piece of infrastructure
like a database, then those must be released ahead of the work that depends on
it. That work is not ready to be merged into main until the dependencies are
ready.

To help the team with this we propose extending our pull request template to
include release prompts. If we can increase our confidence when releasing small
parts then we should be able to release at any point.

For larger new features that we don't want to release in an incomplete state,
we'll need to decide on a way of ensuring `main` is unblocked while allowing
unrelated branches to continue being merged to `main` before the whole feature
is ready to be used by users. We'll likely use a combination of feature flags
and feature branches for this.
