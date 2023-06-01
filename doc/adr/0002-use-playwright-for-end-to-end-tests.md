# 2. Use Playwright for End-to-End tests

Date: 2023-05-30

## Status

Accepted

## Context

We have a suite of integration tests in this repository that use
[Cypress](https://www.cypress.io/) to test the application locally against a
mock API. We'd like to be able to test the application as a whole with a real
backend to ensure everything is working as expected on a deployed environment.

Although in [this ADR](./0002-use-pact-for-contract-testing.md) we decided we'd
use contract tests instead of E2E tests to cover some of this functionality,
contract tests don't quite cover everything we might want to test, so, for the
sake of confidence, we would like to keep using a suite of E2E tests somewhere.

Other teams, particularly the Approved Premises team initially trialled running
Cypress against a deployed environment but encountered issues with slowness,
flakiness and conflicting Typescript settings with their main codebase and the
code used to write the tests so have moved away from this.

## Decision

As the Approved Premises team [have
done](https://github.com/ministryofjustice/hmpps-approved-premises-ui/blob/main/doc/architecture/decisions/0010-use-playwright-for-end-to-end-tests.md),
we're going to trial using a suite of end to end tests in a different repo
using [Playwright](https://playwright.dev/). This is a more lightweight
alternative to Cypress, which is in use in other parts of HMPPS (See
<https://github.com/ministryofjustice/hmpps-probation-integration-e2e-tests>).

These tests will be run in the `end_to_end_test` CircleCI pipeline on the
development environment, after the environment has deployed, both on the UI and
API codebases.

We will deliberately keep the tests focused, as more of a smoke test to ensure
the application runs as expected, rather than reusing integration test code
which is more robust and has a lot of expectations.

We will keep an eye on these tests and how they go, because there is a balance
between us having a consistently passing test suite (which may suggest our
happy path is too "happy") and a test suite that breaks a lot, but for
legitimate reasons (i.e. a change in the UI or API that breaks functionality).

## Consequences

This will mean we're maintaining two test suites, one for integration and one
for end-to-end tests, however:

- The end-to-end tests will be kept deliberately simplistic, only testing the
  "happy path", with minimal expectations
- We will be able to use the VSCode [Playwright Test
  plugin](https://marketplace.visualstudio.com/items?itemName=ms-playwright.playwright),
  which will allow us to "record" any new/changed tests - meaning we can
  rapidly generate / iterate end to end tests
