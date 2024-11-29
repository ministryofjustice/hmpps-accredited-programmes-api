# Deployment Notes

This helm deployment makes use of the generic-service chart, see chart documentation here:

<https://github.com/ministryofjustice/hmpps-helm-charts/tree/main/charts/generic-service>


# Database refresh - Prod to Preprod

The service has a [cronjob](./hmpps-accredited-programmes-api/templates/cronjob-prod-to-preprod-data-refresh.yaml) that runs every Monday and Wednesday at 8pm
to refresh the data in preprod using data from production.

If setting this up for the first time use **extreme caution** - this job connects to production databases.

**__Ensure that all environment variables are named and setup correctly.__**

## Prerequisite

For this job to work the preprod database credentials need to be available production namespace.
This is achieved by having terraform export the preprod credentials, which is an output of the
Terraform rds module, to a secret in the production namespace. See this example:

https://github.com/ministryofjustice/cloud-platform-environments/blob/7968f9c66f6914d33db35b68209c55b2dcb25d7d/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-accredited-programmes-preprod/resources/rds-postgresql.tf#L66

## Overview

The refresh job performs a `pg_dump` using the existing production credentials, already setup in
the production namespace of the application. The job then dumps the existing users from preprod
(to ensure that preprod users don't lose access). It then uses the preprod credentials
(see prerequisite) to carry out a `pg_restore` of the production database, and then adds the
preprod users back to the preprod database.

## Run an adhoc database refresh

If you need to run the database refresh outside the schedule, you can run the follwing
command:

```
kubectl create job --from=cronjob/db-refresh-job db-refresh-job-adhoc
```

The job creates a pod that runs to completion. You can review the command output by
using `kubectl` to show pod logs.