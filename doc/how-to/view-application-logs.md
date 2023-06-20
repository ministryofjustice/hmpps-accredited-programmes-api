# View application logs

## Prerequisites

- Be a part of the Ministry of Justice GitHub organisation. This should be done
  as part of your your team onboarding process.

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
