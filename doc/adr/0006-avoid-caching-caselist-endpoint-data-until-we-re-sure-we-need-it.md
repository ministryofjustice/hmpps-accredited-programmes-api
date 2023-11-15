# 6. Avoid caching caselist endpoint data until we're sure we need it

Date: 2023-11-08

## Status

Accepted

## Context

As outlined in [Use API for aggregating data for
caselists](0005-use-api-for-aggregating-data-for-caselists.md), we'll be using
the API rather than the UI to make requests to external services for populating
a user or organisation's caselist table. We'll need to fetch data from the
Prison API, Prison Register API and Prisoner Search API for this, for up to 10
records at a time (thanks to pagination). This could, realistically, result in
lots of requests being made to other services and slow our service down.

## Decision

Because we're making a request to three external services here for potentially a
small amount of data, we're going to delay implementing caching until we know a
bit more about how the service will perform, as we're hesistant to overengineer
an approach at this stage.

We will monitor the performance of our caselist endpoint in Application Insights
and implement caching if we believe it to be necessary.

However, we have a plan for how we will implement caching if we *do* opt for
caching the data returned from other services. See [How we plan to implement caching if it becomes necessary](#how-we-plan-to-implement-caching-if-it-becomes-necessary) for more
details on the approach.

## Consequences

This will save us some development time at the moment to continue building out
other areas of the service. It will also reduce the complexity of the code as we
don't need to add caching functionality.

We'll need to ensure we monitor the load on pre-production and production
services and implement caching if we do believe it to be necessary for
performance.

## How we plan to implement caching if it becomes necessary

This approach has been largely inspired by the [CAS team's
implementation](https://dsdmoj.atlassian.net/wiki/spaces/AP/pages/4545904995/API+caching),
which involves two different types of caching:

### Pre-emptive cache

This would be used to cache details we know we're likely to need to re-request
regularly, like a request for a person in prison by their `prisonNumber`. We'll
be displaying user details like Sentence Dates, Name etc to multiple users
viewing their caselists, so making a fresh request to the source system each
time would be inefficient.

This is pre-emptive as it runs startup and renews itself every 6 hours (an API
trick, not done via CI or a cron job) and expires after 12 hours of no response.
We could, for example, loop through `prisonNumber`s on all our Referrals and
fetch details from source systems like the Prison API on startup and cache them
so they do not need to be re-fetched.

### A regular cache

This handles traditionally and does not renew itself; is not opinionated, if a
request is made it caches the response for a set period of time. If a request is
made it caches the response for a period of time and doesn't renew itself after
the TLL expires. We will also likely need to implement functionality for
manually clearing this cache.
