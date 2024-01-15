# Running Pact tests locally

## Why would I need to do this?

We use Pact for testing the interactions between our UI and API codebases.

When running the Pact tests locally, the `@PactBroker` annotation in `/test/.../pact/PactContractTest.kt` fetches the
most recently-published Pact contract from
the [Pact Broker](https://pact-broker-prod.apps.live-1.cloud-platform.service.justice.gov.uk/), essentially running the
API's tests against the version of the UI running on Dev.

There are times when you may want to run the tests against a local development version of the UI, e.g. when debugging or
checking the stability of our Pact tests which, until recently, have provided string or null values for optional API
fields,
which [Pact struggles to deal with](https://docs.pact.io/faq#why-is-there-no-support-for-specifying-optional-attributes).

## How to run Pact tests against a local JSON file

When the UI runs its client tests, it generates a JSON file in the UI
codebase: `pact/pacts/Accredited Programmes UI-Accredited Programmes API.json`, containing a specification of all the
endpoints the UI expects to consume, and the shape of the data that comes back from those endpoints.

To run your local API tests against a version of this JSON file:

1. In the UI codebase, generate the JSON file by running npm run test:unit (you may need to manually delete your local
   pact/pacts/Accredited Programmes UI-Accredited Programmes API.json.
2. Copy the newly-generated JSON file from UI
   codebase (`pact/pacts/Accredited Programmes UI-Accredited Programmes API.json` into API codebase, under a `pact/`
   folder in root directory.
3. In `test/.../pact/PactContractTest.kt` , change the `@PactBroker` annotation (which instructs tests to read from
   remote pact broker) to `@PactFolder("pact")` (you'll need to update import
   to `import au.com.dius.pact.provider.junitsupport.loader.PactFolder` for this)
4. Run the `PactContractTest.kt` tests and see output.
