# Accredited Programmes API
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-accredited-programmes-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-accredited-programmes-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-accredited-programmes-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-accredited-programmes-api)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-accredited-programmes-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-accredited-programmes-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-accredited-programmes-api-dev.hmpps.service.justice.gov.uk/webjars/swagger-ui/index.html?configUrl=/v3/api-docs)

## Prerequisites

- Docker
- Java

## Setup

When running the application for the first time, run the following command:

```bash
script/setup # TODO - this script is currently a stub
```

If you're coming back to the application after a certain amount of time, you can run:

```bash
script/bootstrap # TODO - this script is currently a stub
```

## Running the application

To run the server, from the root directory, run:

```bash
script/server
```

This runs the project as a Spring Boot application on `localhost:8080`

### Running/Debugging from IntelliJ

To run from IntelliJ, first start the database:

```bash
script/development_database
```

Then in the "Gradle" panel (`View->Tool Windows->Gradle` if not visible), expand `hmpps-accredited-programmes-api`, `Tasks`,
`application` and right click on `bootRunLocal` and select either Run or Debug.

## Running the tests

To run linting and tests, run:

```bash
script/test
```
