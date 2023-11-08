# 5. Use API for aggregating data for caselists

Date: 2023-11-08

## Status

Accepted

## Context

We've tended to use the UI codebase to make calls to other services like the
Prisoner Search and the Prison API, using references like Prison Number that we
store on our entities, in an attempt to keep the API as focused on our own
domain as possible. This has worked well up until now, and we'll continue to do
this where it feels like fetching this data is more of a presentational concern,
that doesn't need to live in our own API.

Now we're adding endpoints to return data for a user's caselist that, as per
current designs, will involve fetching a person's sentence information and more
personal details, we need to find an efficient approach for retrieving this
information for large datasets.

## Decision

We will shift the work populating caselist data onto the API when calling other
services like the Prison API. This will be more efficient when aggregating data
over a large dataset, as it will allow us to:

1. Paginate data, allowing us to return only the first x results we need per
   page to keep the response size down.
1. Sort and filter on the API by passing query parameters to the search endpoint
   to further limit the number of results we show.
1. Only make calls to external services for the number of results we want to
   show, based on criteria above, keeping the number of requests low.
1. If we need to, cache some of these responses from external services to
   increase efficiency.

## Consequences

In addition to the benefits mentioned above, this will allow us to keep the UI
logic fairly simple, so we can populate the caselists with a small number of API
calls on the frontend.

This may introduce some confusion as to which side of the service is responsible
for fetching data from other sources, as we've usually used the UI for this. For
now, we'll only use the API when aggregating data from external services to
populate the caselist views, but may revisit this decision in future if we see a
reason for more of this logic to shift to the backend.
