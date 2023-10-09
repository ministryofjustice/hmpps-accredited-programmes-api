# Accredited Programmes API
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-accredited-programmes-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-accredited-programmes-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-accredited-programmes-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-accredited-programmes-api)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-accredited-programmes-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-accredited-programmes-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://accredited-programmes-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)

## Prerequisites
For building and running:
- Docker
- Java 19

Additionally, for running scripts:
- kubectl
- jq
- curl

## Setup

Before running the application for the first time, run the following command:

```bash
./gradlew clean build
```

## Running the application

The running application expects to connect to a PostgresQL database and an hmpps-auth instance.
Use the docker-compose.yml file to pull and start these:

```bash
docker compose up hmpps-auth postgresql
```

Then, to run the server:

```bash
./gradlew bootRunLocal
```

This runs the project as a Spring Boot application on `localhost:8080`

You can confirm that the application is running by querying an endpoint using one of the 
scripts in script/local-scripts. For example:
```bash
./scripts/local-scripts/all-courses
```
should output a nicely formatted JSON document containing information about courses.

### Running/Debugging from IntelliJ

To run from IntelliJ, first start hmpps-auth and the PostgresQL database in docker as above.

Then in the "Gradle" panel (`View->Tool Windows->Gradle` if not visible), expand `hmpps-accredited-programmes-api`, `Tasks`,
`application`, right-click on `bootRunLocal` and select either Run or Debug.

## Running the tests

To run linting and tests, do:

```bash
./gradlew clean build
```
Repository integration tests use an embedded H2 database. REST API tests start a local server which listens on a random
port.

## OpenAPI documentation

The API which is offered to front-end UI apps is documented using Swagger/OpenAPI.

This is available in development at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


