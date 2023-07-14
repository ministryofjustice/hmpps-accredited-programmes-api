# View application logs

## Prerequisites

- Be a part of the Ministry of Justice GitHub organisation. This should be done
  as part of your team onboarding process.
- For access to Application Insights, follow [this self-service workflow](https://dsdmoj.atlassian.net/wiki/spaces/DSTT/pages/3897131056) to create your account.
- You’ll need security clearance to access logs for prod

## Kibana

While you can [view the logs of an individual Kubernetes
pod](./manage-infrastructure.md#view-the-application-logs-of-a-pod), the logs
can also be viewed in the interface on
[Kibana](https://kibana.cloud-platform.service.justice.gov.uk/).

There are lots of projects being logged here, so you'll want to filter down the
logs by clicking "+ Add filter" at the top left-hand corner of the interface.

Here's an example for viewing the logs for the API on the Dev environment:

**Filter 1**:
Field: `kubernetes.labels.app`
Operator: `is`
Value: `hmpps-accredited-programmes-api`

**Filter 2**:
Field: `kubernetes.namespace_name`
Operator: `is`
Value: `hmpps-accredited-programmes-dev`

## Application Insights

More detailed API logs can be viewed in Application Insights, held for around 3
months.

This is configured by the Kotlin template to be run as part of each service.
Telemetry should be being sent to Application Insights for our services as well
as other MOJ services such as NOMIS. This gives us rich insights into what
happened to any given request.

You'll able to use your Microsoft account (see [prerequisites](#prerequisites)
for instructions) to sign into Application Insights:

- [nomisapi-prod](https://portal.azure.com/#@nomsdigitechoutlook.onmicrosoft.com/resource/subscriptions/a5ddf257-3b21-4ba9-a28c-ab30f751b383/resourceGroups/nomisapi-prod-rg/providers/Microsoft.Insights/components/nomisapi-prod) for prod
- [nomisapi-preprod](https://portal.azure.com/#@nomsdigitechoutlook.onmicrosoft.com/resource/subscriptions/a5ddf257-3b21-4ba9-a28c-ab30f751b383/resourceGroups/nomisapi-preprod-rg/providers/Microsoft.Insights/components/nomisapi-preprod) for pre-prod
- [nomisapi-t3](https://portal.azure.com/#@nomsdigitechoutlook.onmicrosoft.com/resource/subscriptions/c27cfedb-f5e9-45e6-9642-0fad1a5c94e7/resourceGroups/nomisapi-t3-rg/providers/Microsoft.Insights/components/nomisapi-t3) for dev

In each environment, you can view logs by selecting "Logs" from the "Monitoring"
section of left hand menu.

Close the "Queries" modal that pops up (we don't currently have any saved
queries) to access the main querying fuctionality.

A sample query for filtering all requests to the API might look like the following:

```
requests | where cloud_RoleName == 'hmpps-accredited-programmes-api'`
```

Traces are also useful for grouping all logs for a single request:

```
  traces
  | where cloud_RoleName == 'hmpps-accredited-programmes-api'
  | sort by timestamp
```
