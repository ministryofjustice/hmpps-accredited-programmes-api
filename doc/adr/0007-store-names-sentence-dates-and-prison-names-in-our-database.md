# 7. Store names, sentence dates and prison names in our database

Date: 2024-01-22

## Status

Accepted

## Context

To be able to prioritise their caselists, users need to be able to sort the list of referrals by fields we don't store
in our database but fetch from external services, e.g. Name, sentence dates.

To make use of JPA repository sorting, we need to fetch these fields in advance, rather than fetching them at the time a
user loads the case list page, then sorting over the returned values - this would be inefficient.

In the [previous ADR](0006-avoid-caching-caselist-endpoint-data-until-we-re-sure-we-need-it.md) we decided against
caching data from the Prisoner Search and Prison Register APIs, as we believed it wouldn't have a big impact on
performance. However, now we know we'll need to sort referrals by these external fields, we need some way of fetching
this data in advance to provide API sorting via a query parameter on the `/referrals/*/dashboard` endpoints.

We initially opted for implementing caching using Redis following the approach outlined in that ADR as a way to
temporarily store this data and provide sorting functionality. There are a few problems with this approach:

- We'd need to rewrite a fair bit of our case-list functionality from scratch, particularly how we filter and sort
  fields we do own.
- Caching in itself is complex to implement, and we'd need an approach for things like how regularly to update the
  cache, which would require changes to infrastructure, refactoring the codebase and development time that we don't
  really have right now.
- In addition to a regular re-fetch of data to ensure we're always showing the most up to date details, what do we do
  when a new referral comes in? Do we need to re-fetch all personal details for referrals we own, or do we re-write some
  custom functionality for fetching just this one person's data?
- We'd still essentially be storing personal details in our service, as we are technically storing this information in
  Redis, despite it being a more short-lived approach.
- The Prisoner Search service only allows for searching for 1000 prison numbers at once (when fetching the data to
  cache, we'd be making a large bulk request for multiple Prison Numbers at once), so as the service grows, we'd need to
  find an approach for doing this efficiently.

## Decision

We will not use caching to store the fields from external services, but will instead store these values in our own
Postgres database to make use of JPA sorting.

From the Prisoner Search API, we will fetch and store the following details about the referred person:

- `prisonNumber`
- `bookingId`
- `firstName`
- `lastName`
- `indeterminateSentence`
- `nonDtoReleaseDateType`
- `conditionalReleaseDate`
- `tarriffDate`
- `paroleEligibilityDate`
- `earliestReleaseDate` (one of the dates above, based on whether person is on an indeterminate sentence)

From the Prison Register API, we will fetch and store the `name` of a Prison in our service.

These fields will be fetched and stored on a Referral (linking to a new Person table) when the draft Referral is
created.

To prevent this data from getting out of date, e.g. someone's name or their sentence details changing we will initially
re-fetch data from the Prisoner Search when a user clicks on the entry in their caselist.

Longer term, however, we'll want to listen out for domain events emitted by
the [Offender Events service](https://offender-events-ui-dev.prison.service.justice.gov.uk/messages?text-filter=) when a
record is updated and update Referrals in our own database accordingly.

When someone creates a new referral, we fetch and store their name and sentence dates (the columns we want to sort by)
in our real database, rather than a cache. This means we can make use of the current sorting functionality and wouldn't
have to hand-write everything ourselves.

## Consequences

### Benefits

Storing this data in our database will greatly simplify our code in contrast to the complexity introduced by
implementing caching, and we'll be able to make use of the existing filtering and sorting functionality provided by JPA.

It will also speed up parts of the service where we previously would have made requests to other services for this
information on read. We will instead be able to read these values from our own database and skip this round trip.

### Disadvantages

We'll be storing extra details about a referred person (their name and sentence dates) in our own database. This is
something we've actively been trying to avoid as much as possible, preferring to read this directly from other sources
to prevent storing any more personal data in our service than is necessary. However, the importance of sorting by these
fields has made it necessary for us to store this data.

We'll need to ensure we keep this data up to date and inform users of the time and date the data was fetched to ensure
they're aware they could be viewing stale data, until we implement reading domain events from the Offender Events
service and update records more regularly.
