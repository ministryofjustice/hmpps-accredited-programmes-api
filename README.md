# Accredited Programmes API

[![repo standards
badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-accredited-programmes-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-accredited-programmes-api
"Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-accredited-programmes-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-accredited-programmes-api)
[![Docker Repository on
Quay](https://quay.io/repository/hmpps/hmpps-accredited-programmes-api/status
"Docker Repository on
Quay")](https://quay.io/repository/hmpps/hmpps-accredited-programmes-api) [![API
docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://accredited-programmes-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)

## Prerequisites

For building and running:

- Docker
- Java 19

Additionally, for running scripts:

- `kubectl` (see [Manage Infrastructure
  docs](/doc/how-to/manage-infrastructure.md#prerequisites) for further details)
- `jq`
- `curl`
- `bash` version `4.2` or later

## Setup

Before running the application for the first time, run the following command:

```bash
./gradlew clean build
```

## Running the application

The running application expects to connect to a PostgresQL database and an
hmpps-auth instance. Use the docker-compose.yml file to pull and start these:

```bash
docker compose up hmpps-auth postgresql
```

Then, to run the server:

```bash
./gradlew bootRunLocal
```

This runs the project as a Spring Boot application on `localhost:8080`

You can confirm that the application is running by querying an endpoint using
one of the scripts in script/local-scripts. For example:

```bash
./script/local-scripts/all-courses
```

should output a nicely formatted JSON document containing information about
courses.

### Running/Debugging from IntelliJ

To run from IntelliJ, first start `hmpps-auth` and the `postgresql` database in
Docker as above.

You may need to manually install the Java 19 SDK to run the application. We've
used `temurin-19` for this in most developer environments.

To set this, in the "Project Structure" window (`File -> Project Structure`),
expand the SDK select, and if the version you need (e.g. `temurin-19`) isn't
available, click `Add SDK -> Download JDK...`, then search for it and download
it.

To then run the project, in the "Gradle" panel (`View -> Tool Windows -> Gradle`
if not visible), expand `hmpps-accredited-programmes-api`, `Tasks`,
`application`, right-click on `bootRunLocal` and select either Run or Debug.

## Running the tests

To run linting and tests, do:

```bash
./gradlew clean build
```

Repository integration tests use an embedded H2 database. REST API tests start a
local server which listens on a random port.

### Pact

[We use
Pact](https://github.com/ministryofjustice/hmpps-accredited-programmes-ui/blob/main/doc/adr/0002-use-pact-for-contract-testing.md)
for testing the interactions between the UI and API services:

- [UI Course client tests](https://github.com/ministryofjustice/hmpps-accredited-programmes-ui/blob/5d9e92aca4f89177be1b464f6317e9a04867ae9a/server/data/accreditedProgrammesApi/courseClient.test.ts#L15)
- [UI Referral client tests](https://github.com/ministryofjustice/hmpps-accredited-programmes-ui/blob/5d9e92aca4f89177be1b464f6317e9a04867ae9a/server/data/accreditedProgrammesApi/referralClient.test.ts#L11)

When the client tests are updated, the [API tests](src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/pact/PactContractTest.kt)
need to be kept in sync, otherwise the build will fail. Test state is currently
set up in [an SQL migration
file](src/test/resources/db/migration/R__test_data.sql) which retains state
between tests, and is unfortunately very brittle. We would like to refactor this
as soon as possible to set up and clear state between tests.

### End to End Tests

[We use Playwright](doc/adr/0002-use-playwright-for-end-to-end-tests.md) for
running our end to end tests. These are run against the deployed development
environment on each merge to `main`. These tests log in with a test user account
`ACP_TEST`, linked to one of the developers' email addresses. From time to time
the password needs to be reset, causing the tests to fail. When this happens, we
need to update the `HMPPS_AUTH_PASSWORD` environment variable set in the
[CircleCI project settings](https://app.circleci.com/settings/project/github/ministryofjustice/hmpps-accredited-programmes-api/environment-variables).

## OpenAPI documentation

The API which is offered to front-end UI apps is documented using
Swagger/OpenAPI.

This is available in development at
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

There's currently a slight issue with one of the environments, so you may need
to refresh a couple of times before the OpenAPI documentation loads.
