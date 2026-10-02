# Troubleshooting crib sheet: "cannot retrieve data from OASys or NOMIS"

A validated reference for debugging referral data-retrieval failures (and related issues)
in `hmpps-accredited-programmes-api`, using **Application Insights (KQL)** and **PostgreSQL**.

> Status legend used in this doc
> - ✅ **Validated** — confirmed by reading the source in this repo (file path cited).
> - 🧩 **Query template** — built on the standard Application Insights / PostgreSQL schema.
>   The structure is standard, but exact values (e.g. whether the Java agent parameterises a
>   URL path) should be confirmed the first time you run it, then this doc updated.
>
> When a query is wrong, **don't delete it** — fix it and note the correction in the
> [Refinement log](#refinement-log) at the bottom so the next person/agent doesn't re-learn it.

---

## 1. The worked example

> **Issue:** When trying to view a prisoner's accredited programmes referral, the application
> states it cannot retrieve data from OASys or NOMIS at the moment.
> **Identifier given:** `A8784EY`

### ⚠️ First gotcha: `A8784EY` is a prison number (NOMS ID), not a CRN ✅

- This API keys **everything** off the **prison number** (NOMS ID), stored as `prison_number`.
  See `ReferralEntity.prisonNumber` → `@Column(name = "prison_number")`
  (`src/main/kotlin/.../domain/entity/create/ReferralEntity.kt`).
- `A8784EY` matches the NOMS ID format `^[A-Z]\d{4}[A-Z]{2}$`. A Delius **CRN** looks like
  `^[A-Z]\d{6}$` (e.g. `X123456`). The label "CRN" in the ticket is a misnomer — treat
  `A8784EY` as a **prison number** in every query below.
- The OASys integration in this service is also addressed **by prison number**, not CRN —
  e.g. `OasysApiClient.getAssessments(prisonerNumber)` calls
  `/assessments/timeline/{prisonerNumber}` (`src/main/kotlin/.../client/oasysApi/OasysApiClient.kt`).

---

## 2. Key facts (all code-validated) ✅

| Thing | Value | Source |
|---|---|---|
| App Insights cloud role name | `hmpps-accredited-programmes-api` | `applicationinsights.json` → `role.name` |
| Spring app name | `hmpps-accredited-programmes-api` | `application.yml` → `spring.application.name` |
| Telemetry mechanism | App Insights **Java agent** auto-instrumentation (`applicationinsights-agent-3.7.8.jar`). No custom `TelemetryClient`/`trackEvent` code exists. Exceptions also go to **Sentry**. | `build/libs/…agent…jar`, `logback-spring.xml`, `ApiExceptionHandler` |
| Upstream HTTP timeout | `10000` ms (`upstream-timeout-ms`); case notes `30000` ms | `application.yml` |
| DB pool connect timeout | Hikari `connectionTimeout: 1000` ms, `validationTimeout: 500` ms | `application.yml` |

### Downstream services & PROD hostnames ✅

Base URLs from `helm_deploy/values-prod.yaml`. The App Insights **`dependencies.target`**
column is the **host**, so filter on these hostnames.

| Logical service | Internal log name (`serviceName`) | PROD host (`dependencies.target`) |
|---|---|---|
| **HMPPS Auth (OAuth token)** | — | `sign-in.hmpps.service.justice.gov.uk` (path `/auth/oauth/token`) |
| OASys (ARNS-fronted) | `Oasys API` | `accredited-programmes-and-oasys.hmpps.service.justice.gov.uk` |
| Assess Risks & Needs | — | `assess-risks-and-needs.hmpps.service.justice.gov.uk` |
| Prison API (NOMIS) | — | `prison-api.prison.service.justice.gov.uk` |
| Prisoner Search (NOMIS) | — | `prisoner-search.prison.service.justice.gov.uk` |
| NOMIS User Roles | `NOMIS USER ROLEMANAGEMENT API` | `nomis-user-roles-api.prison.service.justice.gov.uk` |
| Prisoner Alerts | — | `alerts-api.hmpps.service.justice.gov.uk` |
| Prison Register | — | `prison-register.hmpps.service.justice.gov.uk` |
| Manage Offences | — | `manage-offences-api.hmpps.service.justice.gov.uk` |
| Case Notes | — | `offender-case-notes.service.justice.gov.uk` |
| Allocation Manager | — | `moic.service.justice.gov.uk` |

> `serviceName` (e.g. `"Oasys API"`) is only used in **log messages / exception text**, not in
> `dependencies.target`. Use it to grep `traces`/`exceptions`; use the **host** for `dependencies`.

### ⭐ Most common root cause: transient HMPPS Auth token failures ✅

**Validated by `WebClientConfiguration.kt` + observed in prod traces (2026-10-01).**

Every downstream WebClient is built with `ServletOAuth2AuthorizedClientExchangeFilterFunction`
using the **client-credentials** grant, and **there is no retry** configured
(`buildWebClient(...)` only sets a response/connect timeout of `upstream-timeout-ms` = 10 s).
So **before every OASys/NOMIS/ARNS/alerts call**, the app fetches a token from
`https://sign-in.hmpps.service.justice.gov.uk/auth/oauth/token`
(`spring.security.oauth2.client.provider.hmpps-auth.token-uri`).

If that token fetch momentarily fails, the downstream call fails **even though the downstream
service itself is healthy (200s in query 3.3)**. Signatures seen in `traces`:

- `ClientAuthorizationException: [invalid_token_response] … POST ".../auth/oauth/token": Connection reset`
- `… auth/oauth/token": Read timed out`
- `WebClientRequestException: recvAddress(..) failed with error(-104): Connection reset by peer`

Most of these are **swallowed to `null`** (e.g. `OasysService.getPniCalculation`,
`getActiveAlerts`, `AllocationManagerService` POM lookup all log a WARN/ERROR `Failure to
retrieve …` and continue), so the API may still return **HTTP 200 with missing data** — which the
UI renders as *"cannot retrieve data from OASys or NOMIS at the moment"*. A token failure during
`getAssessments` instead surfaces as a **404** (`No assessment found …`).

**Implication for debugging:** if per-prisoner request/exception queries (3.1/3.4/3.5) come back
empty but the user definitely saw the error, suspect a **transient auth blip** and run the
**auth-focused queries 3.8/3.9** below, correlating by time rather than prison number.

### ⭐⭐ Root cause for a **persistent** 500 on `risks-and-alerts`: ARNS predictors **deserialization** failure ✅

**Validated 2026-10-01 via code + prod telemetry (343 prisoners affected over 10 days).**

`GET /oasys/{prisonNumber}/risks-and-alerts` → `OasysService.getRisks(prisonNumber)` makes several
downstream calls. **All but one tolerate failure and degrade to `null`** (`getPniCalculation`,
`getActiveAlerts`, `getRoshSummary`, `getOffendingInfo` — via `fetchDetail`). The exception is:

```kotlin
// OasysService.getRisks(...)
val allPredictorVersioned = assessRiskAndNeedsService.getRiskPredictors(assessmentId)  // ⚠️ UNGUARDED
```

```kotlin
// AssessRiskAndNeedsService.getRiskPredictors(assessmentId)
is ClientResult.Failure -> {
  log.error("Failure when retrieving risk predictors for assessment id : $assessmentId", result.toException())
  result.throwException()   // ⚠️ RETHROWS — not swallowed
}
```

**What actually fails (important):** the ARNS HTTP call
`GET {arns}/assessments/id/{assessmentId}/risk/predictors/all` **returns HTTP 200** (confirmed in
`dependencies`, query 3.10a). The failure happens **after** the 200, when `BaseHMPPSClient`
deserialises the body: `objectMapper.readValue(result.body, AllPredictorVersioned<Any>)`
(`BaseHMPPSClient.kt:79`) throws, is caught by the **generic `catch (exception: Exception)`** →
`ClientResult.Failure.Other` → its `toException()` is `RuntimeException("Unable to complete request.
Service ARNS API for GET request to /assessments/id/{assessmentId}/risk/predictors/all")` →
`getRiskPredictors` rethrows → generic `@ExceptionHandler(Throwable)` → **HTTP 500**.

**Why deserialisation throws — CONFIRMED (2026-10-01, query 3.10b `innermostMessage`/`details`):**
ARNS added a **new JSON field `assessmentType`** to the predictors response that neither of our
model classes tolerates. The exact Jackson cause is:

```
com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException:
  Unrecognized field "assessmentType"
  (class uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedDto),
  not marked as ignorable (4 known properties: "completedDate", "status", "outputVersion", "output")
```

The **same error occurs for `AllPredictorVersionedLegacyDto`** (`outputVersion` `"1"`) as well as
`AllPredictorVersionedDto` (`outputVersion` `"2"`) — so this is **not** an unmapped `outputVersion`
/ subtype-resolution problem. Both existing subtypes resolve fine; they then **fail on the unknown
field** because Jackson defaults to `FAIL_ON_UNKNOWN_PROPERTIES = true` and neither DTO has
`@JsonIgnoreProperties(ignoreUnknown = true)`.

> **Validated ObjectMapper wiring (2026-10-01):** this is **not** Spring's auto-configured mapper
> (which would have `FAIL_ON_UNKNOWN_PROPERTIES = false`). `BaseHMPPSClient.readValue` (`:79`) uses
> the mapper passed to its constructor, and **all 10 clients** pass a plain
> `jacksonObjectMapper()` with **no** feature config — so Jackson's default `true` applies. A
> _global_ disable therefore changes behaviour for every client (ARNS, OASys, PrisonApi,
> PrisonerSearch, CaseNotes, PrisonerAlerts, ManageOffences, NomisUserRoles, PrisonRegister,
> AllocationManager) — see the separate-PR note below.

The polymorphic mapping
(`client/arnsApi/model/AllPredictorVersioned.kt`) is working as intended:

```kotlin
@JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY, property = "outputVersion", visible = true)
@JsonSubTypes(
  JsonSubTypes.Type(value = AllPredictorVersionedLegacyDto::class, name = "1"),
  JsonSubTypes.Type(value = AllPredictorVersionedDto::class,       name = "2"),
)
```

**Signature:** `dependencies` shows ARNS **200**, but `requests` shows our endpoint **500**, with an
`exceptions` row `java.lang.RuntimeException: Unable to complete request. Service ARNS API …
risk/predictors/all` whose **`innermostMessage` is the `UnrecognizedPropertyException` for
`"assessmentType"`** above. It is **systemic** — any prisoner whose predictors response now carries
`assessmentType` 500s (343 prisoners over 10 days per 3.10d), regardless of `outputVersion`. It is
**not** a single-prisoner data issue, and **not** an ARNS outage.

> **⚠️ Two innermost causes share this same endpoint + rethrow path.** The **dominant** one is the
> `UnrecognizedPropertyException "assessmentType"` above (a real 200 body our DTO rejects). A
> **minority** of the same `risks-and-alerts` 500s instead have innermost
> `org.springframework.web.reactive.function.client.WebClientRequestException: Connection
> prematurely closed BEFORE response` (a transient ARNS connection drop — no 200, no deserialisation).
> Both surface identically in `requests`/`exceptions` (same `RuntimeException … risk/predictors/all`
> wrapper, same `OasysService.getRisks` → `getRiskPredictors` rethrow), so **always read
> `exceptions.details`/`innermostMessage`** (query 3.10b) to tell them apart. The resilience fix
> (layer 2 below) covers both; the correctness fix (layer 1) only addresses the `assessmentType` one.

> **Fix — two layers — all options validated against source 2026-10-01.**
> ✅ **Implemented on branch `APG-2707/fix-risks-and-alerts-500-arns-predictors` using 1b (ARNS-scoped) + 2a.**
>
> **Layer 1 — Correctness (make the ARNS DTOs tolerate the new field).** Three options were considered:
> - **1a: `@JsonIgnoreProperties(ignoreUnknown = true)`** on the DTOs. Discoverable, but only protects
>   the annotated classes — deeper nested score DTOs (`OgpScoreDto`, `StaticOrDynamicPredictorDto`, …)
>   stay strict, so a future ARNS field *inside* those would 500 again. Rejected as too brittle.
> - **1b (chosen — ✅ DONE): disable `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES` on the ARNS
>   client's own `jacksonObjectMapper()`** (`AssessRiskAndNeedsApiClient`). One line, covers the
>   **whole ARNS response tree** (top-level *and* any nesting depth), and is **scoped to this client
>   only** — no blast radius to the other 9 clients. Matches the existing repo precedent
>   (`CsvHttpMessageConverter`, `ResourceLoader`). Regression test:
>   `AssessRiskAndNeedsApiClientIntegrationTest."should tolerate unknown fields anywhere …"` (asserts
>   tolerance of unknown fields at the top level **and** nested inside predictor objects).
> - **1c (not recommended): add the `assessmentType` field** to the models — its type/enum values
>   aren't known (guesswork) and it doesn't protect against the next new field.
>
> **Layer 2 — Resilience (stop the unguarded rethrow).** Two options:
> - **2a (chosen — ✅ DONE): change `AssessRiskAndNeedsService.getRiskPredictors`** to log +
>   return `null` on `ClientResult.Failure` (mirroring `OasysService.fetchDetail`). Validated safe — its
>   only caller is `OasysService.getRisks`. Degrades to a partial page and covers **both** innermost
>   causes (the `assessmentType` one *and* the minority "Connection prematurely closed" drops).
>   Regression test: `AssessRiskAndNeedsServiceTest."… degrades to null on failure …"`.
> - **2b: guard at the `getRisks` call site** (try/catch or a `fetchDetail`-style wrapper), leaving
>   the throwing contract intact — localized but duplicates the degradation logic.
>
> Layer 1 (1b) fully closes the `assessmentType`/unknown-field 500s for *all* ARNS responses; Layer 2
> (2a) makes the endpoint resilient to _any_ ARNS predictors failure. Applied **1b + 2a** together.
>
> **Record for a SEPARATE PR (broader hardening — out of scope for this incident fix):**
> - The minority `WebClientRequestException: Connection prematurely closed BEFORE response` is a
>   transient ARNS connection drop (no 200, no deserialisation). Option 2a masks it, but a proper
>   **retry/timeout policy** on the ARNS WebClient is a distinct concern.
> - **All 10 HMPPS clients** share the brittle `jacksonObjectMapper()` (default
>   `FAIL_ON_UNKNOWN_PROPERTIES = true`), so an unknown field from *any* upstream 500s. A global
>   ignore-unknown default in `BaseHMPPSClient` (+ retry policy) is broader hardening worth its own PR.

**Implication for debugging:** when 3.1 shows a **persistent 500** on `risks-and-alerts`, get the
`assessmentId` from 3.6a (`Saving PNI score … assessmentId=…`) and run **3.10a** (expect ARNS
**200**) + **3.10b** (expect the `UnrecognizedPropertyException: Unrecognized field "assessmentType"`
in `innermostMessage`/`details`) to confirm it's an unknown-field deserialisation failure rather
than an ARNS outage.

### How the error is produced ✅

Flow for the OASys endpoints (`OasysController` → `OasysService` → `OasysApiClient` → `BaseHMPPSClient`):

1. `BaseHMPPSClient.doRequest(...)` calls the downstream service.
   (`src/main/kotlin/.../client/BaseHMPPSClient.kt`)
2. **If downstream returns 5xx** → throws
   `ServiceUnavailableException("<serviceName> is temporarily unavailable. Please try again later.")`
   and logs at **ERROR**: `Request to <serviceName> failed with status code <code> reason <msg>.`
3. `ApiExceptionHandler` maps `ServiceUnavailableException` → **HTTP 500** with
   `userMessage = "Service unavailable: <serviceName> is temporarily unavailable. Please try again later."`
   (`src/main/kotlin/.../restapi/config/ApiExceptionHandler.kt`)
4. **If downstream returns non-2xx but non-5xx** (e.g. 404) → returns `ClientResult.Failure`
   (no throw). Many callers log a **WARN** `Failure to retrieve …` and continue with `null`.
5. **No assessment found** (empty/!COMPLETE timeline) → `OasysService.getAssessments(...)`
   throws `NotFoundException("No assessment found for prison number: <prisonNumber>")`
   → `ApiExceptionHandler` maps to **HTTP 404**, `userMessage = "Not Found: …"`.

**So the UI "cannot retrieve data from OASys or NOMIS" most likely corresponds to:**
- an API **HTTP 500** (downstream 5xx → `ServiceUnavailableException`), **or**
- an API **HTTP 404** (`NotFoundException` — e.g. no completed OASys assessment for that prisoner).

Distinguish the two with the queries below — the fix is very different (downstream outage vs.
missing/incomplete OASys data for that prisoner).

### Relevant API endpoints ✅

- OASys: `GET /oasys/{prisonNumber}/{section}` where section ∈ `attitude, behaviour,
  drug-and-alcohol-details, health, assessment_date, learning-needs, lifestyle, offence-details,
  psychiatric, relationships, risks-and-alerts, rosh-analysis` (`OasysController`).
- PNI: `GET /PNI/{prisonNumber}` (`PNIController`) — calls OASys PNI calculation.
- Referral: `GET /referrals/{id}` (`ReferralController`).
- People/NOMIS: `GET /people/{prisonNumber}/sentences`, `/people/offences/{prisonNumber}`,
  `/people/{prisonNumber}/course-participations`, `POST /people/search`,
  `POST /prisoner-search` (`PeopleController`).
- POM allocation (NOMIS, via moic.service.justice.gov.uk): `AllocationManagerService` — logs
  `Failure to retrieve POM information <prisonNumber>` at ERROR and degrades to `null`.

---

## 3. Application Insights (KQL) queries

Run these in **Azure Portal → Application Insights (prod resource) → Logs**. Set a time range
that covers when the user hit the error.

> All queries filter on `cloud_RoleName == "hmpps-accredited-programmes-api"` so they're safe to
> run in a shared/clustered App Insights workspace.

### 3.1 🧩 Start here — all failed server requests for this prisoner

```kql
let prisonNumber = "A8784EY";
requests
| where timestamp > ago(24h)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where url has prisonNumber
| where success == false or toint(resultCode) >= 400
| project timestamp, name, url, resultCode, duration, operation_Id, cloud_RoleName
| order by timestamp desc
```

- `resultCode == 500` → downstream 5xx / `ServiceUnavailableException` (outage). Go to **3.3 / 3.4**.
- `resultCode == 404` → `NotFoundException` (likely no completed OASys assessment, **or** a swallowed
  auth/token blip during `getAssessments`). Go to **3.5 / 3.8** + SQL **4.x**.
- Note the `operation_Id` of the failing request to trace everything it did.
- **No results?** This is common and does **not** mean "no problem". First, **widen the window**
  (`ago(24h)` → `ago(96h)` → `ago(10d)`): the incident may predate 24h. (Observed 2026-10-01: the
  A8784EY 500s only appeared at `ago(96h)` — they were on 29–30 Sep.) Second, remember many failures
  are **swallowed to `null`** (API returns 200 with missing data), so they never appear as a failed
  request for this prisoner — if still empty after widening, jump to the auth-focused queries
  **3.8 / 3.9** and correlate by time, not prison number.

### 3.2 🧩 Full trace of a single failing request (end-to-end)

Paste the `operation_Id` from 3.1.

```kql
let opId = "PASTE_OPERATION_ID";
union requests, dependencies, exceptions, traces
| where timestamp > ago(10d)   // ⚠️ REQUIRED — without this the Logs UI default (24h) hides older events
| where operation_Id == opId
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| project timestamp, itemType, name, target, data, resultCode,
          success, message, type, outerMessage, duration
| order by timestamp asc
```

This shows the inbound request, every downstream **dependency** call it made (OASys/NOMIS/ARNS),
any **exception**, and log **traces** — in order.

> ⚠️ **Always set an explicit `timestamp` filter** (or widen the UI time picker). A query with no
> `where timestamp > ago(...)` uses the portal's default range (often 24h) and will return
> **nothing** for an incident that happened days ago — even though `requests` clearly has the rows
> (learned 2026-10-01 — 3.2 came back empty for a 29 Sep `operation_Id` until the window was widened).

### 3.3 🧩 Are the downstream dependencies failing? (OASys / NOMIS health)

```kql
dependencies
| where timestamp > ago(3h)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where target in (
    "accredited-programmes-and-oasys.hmpps.service.justice.gov.uk",
    "assess-risks-and-needs.hmpps.service.justice.gov.uk",
    "prison-api.prison.service.justice.gov.uk",
    "prisoner-search.prison.service.justice.gov.uk",
    "nomis-user-roles-api.prison.service.justice.gov.uk",
    "alerts-api.hmpps.service.justice.gov.uk")
| summarize total = count(),
            failures = countif(success == false),
            p95_ms = percentile(duration, 95),
            maxResultCode = max(resultCode)
        by target, bin(timestamp, 5m)
| order by timestamp desc
```

High `failures` or `resultCode` 5xx against `accredited-programmes-and-oasys…` (OASys) or the
`prison-…`/`prisoner-search…` (NOMIS) hosts ⇒ **downstream outage**, not our data.

### 3.4 🧩 Dependency failures just for this prisoner

```kql
let prisonNumber = "A8784EY";
dependencies
| where timestamp > ago(24h)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where data has prisonNumber or name has prisonNumber
| where success == false
| project timestamp, target, name, data, resultCode, duration, operation_Id
| order by timestamp desc
```

`data` holds the outgoing URL (e.g. `…/assessments/timeline/A8784EY`). The `resultCode` here is
the **downstream** HTTP status that triggered our 500.

### 3.5 🧩 Exceptions thrown by our app for this prisoner

```kql
let prisonNumber = "A8784EY";
exceptions
| where timestamp > ago(24h)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where outerMessage has prisonNumber
    or innermostMessage has prisonNumber
    or customDimensions has prisonNumber
| project timestamp, type, outerMessage, innermostMessage, operation_Id, method
| order by timestamp desc
```

Expect `type` ending in `ServiceUnavailableException` (downstream 5xx) or `NotFoundException`
(`No assessment found for prison number: A8784EY`).

### 3.6 🧩 Log traces for this prisoner (WARN/ERROR)

These catch the non-throwing failures (step 4 in the flow) that silently degrade to `null`.

> `severityLevel`: `2` = WARNING, `3` = ERROR.
> **Important:** keep the prison-number filter and the message filter in **separate `where`
> clauses** (or parenthesise the `or`). An un-parenthesised
> `where message has prisonNumber or message has "Failure to retrieve"` matches **every**
> "Failure to retrieve" line for **all** prisoners (learned 2026-10-01 — see Refinement log).

**3.6a — this prisoner only:**

```kql
let prisonNumber = "A8784EY";
traces
| where timestamp > ago(7d)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where message has prisonNumber
| project timestamp, severityLevel, message, operation_Id
| order by timestamp desc
```

**3.6b — systemic failure patterns (all prisoners):** use this to spot a service-wide issue.

```kql
traces
| where timestamp > ago(3h)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where message has "Failure to retrieve"
    or message has "temporarily unavailable"
    or message has "Connection reset"
    or message has "Read timed out"
| project timestamp, severityLevel, message, operation_Id
| order by timestamp desc
```

Watch for `Failure to retrieve Assessment for <pn> reason …`,
`No completed assessment found for prison number <pn>`,
`Request to Oasys API failed with status code 5xx …`, and — most commonly —
auth/token `Connection reset` / `Read timed out` messages (see 3.8).

### 3.7 🧩 Service health at a glance (last 1h, all endpoints)

```kql
requests
| where timestamp > ago(1h)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| summarize total = count(), failures = countif(success == false),
            failRatePct = round(100.0 * countif(success == false) / count(), 1)
        by name
| where total > 0
| order by failures desc
```

### 3.8 🧩 HMPPS Auth token-fetch failures (the usual culprit)

Covers the validated root cause: transient failures fetching the client-credentials token.
Run **both** — the agent may record these as a `dependency` to the auth host and/or as `traces`.

**3.8a — dependency calls to the auth token endpoint that failed:**

```kql
dependencies
| where timestamp > ago(6h)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where target has "sign-in.hmpps.service.justice.gov.uk" or data has "oauth/token"
| summarize total = count(), failures = countif(success == false),
            p95_ms = percentile(duration, 95), maxResultCode = max(resultCode)
        by bin(timestamp, 5m)
| order by timestamp desc
```

**3.8b — token/connection errors in logs (works even if the agent didn't log a dependency):**

```kql
traces
| where timestamp > ago(6h)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where message has "oauth/token"
    or message has "ClientAuthorizationException"
    or message has "invalid_token_response"
    or message has "Connection reset"
    or message has "Read timed out"
    or message has "Connection reset by peer"
| project timestamp, severityLevel, message, operation_Id
| order by timestamp desc
```

### 3.9 🧩 Transient-failure trend — correlate with the time the user reported the error

```kql
traces
| where timestamp > ago(24h)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| extend bucket = case(
    message has "oauth/token" or message has "invalid_token_response" or message has "ClientAuthorizationException", "auth_token",
    message has "Connection reset" or message has "Read timed out" or message has "recvAddress", "connection_reset",
    message has "Failure to retrieve PNI", "pni_fail",
    message has "Failure to retrieve ActiveAlerts", "alerts_fail",
    message has "Failure to retrieve POM", "pom_fail",
    "other")
| where bucket != "other"
| summarize count() by bucket, bin(timestamp, 15m)
| order by timestamp desc
```

A spike in `auth_token` / `connection_reset` around the user's report time ⇒ transient HMPPS Auth
blip, not a data problem for this prisoner. If it's a sustained spike, raise with the HMPPS Auth /
Cloud Platform team (intermittent `Connection reset by peer (-104)` to `sign-in.hmpps…`).

### 3.10 🧩 ARNS risk-predictors failure (persistent `risks-and-alerts` 500 for one prisoner)

Get the prisoner's `assessmentId` from 3.6a first (log line `Saving PNI score … assessmentId=…`).

**3.10a — the ARNS predictors dependency for that assessment (what did ARNS return?):**

```kql
let assessmentId = "2515574776";   // from 3.6a for this prisoner
dependencies
| where timestamp > ago(10d)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where target has "assess-risks-and-needs"
| where data has "risk/predictors" and data has assessmentId
| project timestamp, name, data, resultCode, success, duration, operation_Id
| order by timestamp desc
```

`resultCode` here is the **real** downstream status (e.g. 404/422/5xx) that gets rethrown as our 500.

**3.10b — the matching error log / exception (and the Jackson root cause):**

```kql
let assessmentId = "2515574776";
union traces, exceptions
| where timestamp > ago(10d)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where message has assessmentId or outerMessage has assessmentId
    or message has "risk predictors" or outerMessage has "risk/predictors"
| project timestamp, severityLevel, message, type, outerMessage, innermostMessage, details, operation_Id
| order by timestamp desc
```

`outerMessage` is the wrapper (`Unable to complete request. Service ARNS API … risk/predictors/all`).
The **`innermostMessage` / `details`** hold the real cause. **Confirmed (2026-10-01):** it is
`com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field
"assessmentType" (class … AllPredictorVersionedDto / AllPredictorVersionedLegacyDto), not marked as
ignorable (4 known properties: "completedDate", "status", "outputVersion", "output")` — i.e. ARNS
added an unknown field. (A different Jackson error such as `InvalidTypeIdException: Could not resolve
type id '3' … known type ids = [1, 2]` would instead indicate a genuinely new `outputVersion`.)

**3.10c — is it still happening, or has it recovered?** (all `risks-and-alerts` 500s, last 10d)

```kql
requests
| where timestamp > ago(10d)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where name == "GET /oasys/{prisonNumber}/risks-and-alerts"
| where toint(resultCode) >= 500
| summarize count(), min(timestamp), max(timestamp) by tostring(parse_url(url).Path)
| order by max_timestamp desc
```

`max_timestamp` tells you when it last failed — compare with "now" to see if it's ongoing.

**3.10d — blast radius: how many prisoners / assessments are affected, and the trend:**

```kql
// Distinct affected assessments + total errors per day
exceptions
| where timestamp > ago(14d)
| where cloud_RoleName == "hmpps-accredited-programmes-api"
| where outerMessage has "risk/predictors/all"
| extend assessmentId = extract(@"/assessments/id/(\d+)/risk/predictors/all", 1, outerMessage)
| summarize errors = count(), distinctAssessments = dcount(assessmentId) by bin(timestamp, 1d)
| order by timestamp desc
```

A rising `distinctAssessments` over time strongly indicates an ARNS **schema rollout** (the new
`assessmentType` field being added to more and more responses) progressively affecting more
assessments — prioritise the correctness fix (ignore unknown fields).

---

## 4. Database (PostgreSQL) queries

Connect per [`access-dev-database-remotely.md`](./access-dev-database-remotely.md) (port-forward
pod + secrets). Apply the same pattern to preprod/prod namespaces
(`hmpps-accredited-programmes-preprod` / `-prod`) with appropriate approvals.

> **Prod/preprod access — correct order (the pod must exist *before* you port-forward):**
> ```bash
> cd script/kubernetes-scripts
> ./start-db-portforward-pod -ns prod          # creates pod "db-port-forward-pod" in the -prod namespace
> kubectl port-forward db-port-forward-pod --namespace=hmpps-accredited-programmes-prod 5432:5432
> ```
> Then get the username/password from the RDS secret and connect to `localhost:5432`:
> ```bash
> kubectl -n hmpps-accredited-programmes-prod get secret rds-postgresql-instance-output \
>   -o json | jq '.data | map_values(@base64d)'
> ```
> `Error from server (NotFound): pods "db-port-forward-pod" not found` just means you skipped
> `start-db-portforward-pod` (learned 2026-10-01). `setup-service-pod.bash` is a **different** tool
> (an IRSA shell for `aws` CLI / RDS snapshots), not for DB port-forwarding.

> Column facts are validated from the JPA entities and Flyway migrations:
> `referral` (`ReferralEntity`), `person` (`PersonEntity`), `referral_status_history`,
> `referral_status`, `pni_result`, `oasys_pni_result`. The `person` table uses Spring's default
> snake_case naming (e.g. `prisonNumber` → `prison_number`,
> `conditionalReleaseDate` → `conditional_release_date`).

### 4.1 ✅ Does a referral exist for this prisoner, and what state is it in?

```sql
SELECT referral_id,
       prison_number,
       status,
       offering_id,
       submitted_on,
       oasys_confirmed,
       has_ldc,
       deleted
FROM   referral
WHERE  prison_number = 'A8784EY'
ORDER  BY submitted_on DESC NULLS LAST;
```

- `deleted = true` ⇒ it's soft-deleted; the API hides it (`@SQLRestriction("deleted = false")`).
- Note the `status` and `referral_id` for the next queries.

### 4.2 ✅ Has person data been cached for this prisoner?

The service stores names / sentence dates / location locally (see ADR 0007). Missing/stale rows
here can cause NOMIS-derived fields to render as unavailable.

```sql
SELECT person_id,
       prison_number,
       forename,
       surname,
       location,
       gender,
       conditional_release_date,
       parole_eligibility_date,
       tariff_expiry_date,
       earliest_release_date,
       sentence_type
FROM   person
WHERE  prison_number = 'A8784EY';
```

No row ⇒ the person cache was never populated / failed to refresh for this prisoner.

### 4.3 ✅ Is there a cached OASys PNI / assessment id?

```sql
-- PNI results (includes the OASys assessment id and CRN, if captured)
SELECT prison_number, crn, oasys_assessment_id, programme_pathway, pni_valid
FROM   pni_result
WHERE  prison_number = 'A8784EY';

-- OASys PNI temp/cache table
SELECT prison_number, oasys_assessment_id, programme_pathway
FROM   oasys_pni_result
WHERE  prison_number = 'A8784EY';
```

A present `oasys_assessment_id` confirms OASys previously returned a completed assessment for
this prisoner — so a fresh "no assessment" 404 points to an OASys-side data change/outage rather
than this prisoner never having had an assessment.

### 4.4 ✅ Referral status history (what changed and when)

```sql
SELECT h.status_start_date,
       h.status,
       h.previous_status,
       h.category,
       h.reason,
       h.status_end_date
FROM   referral_status_history h
JOIN   referral r ON r.referral_id = h.referral_id
WHERE  r.prison_number = 'A8784EY'
ORDER  BY h.status_start_date DESC;
```

### 4.5 ✅ Decode a status code

```sql
SELECT code, description, active, draft, closed, hold, release
FROM   referral_status
WHERE  code = 'PASTE_STATUS_CODE';
```

---

## 5. Decision tree

```
UI: "cannot retrieve data from OASys or NOMIS"
        │
        ├─ KQL 3.1: find the failing request for A8784EY
        │
        ├─ resultCode 500 ──► KQL 3.3 / 3.4 downstream failing?
        │        ├─ persistent 500 on risks-and-alerts ► ⭐⭐ ARNS predictors
        │        │   DESERIALISATION failure. 3.10a shows ARNS 200; 3.2/3.10b show
        │        │   "Unable to complete request. Service ARNS API … risk/predictors/all"
        │        │   + innermostMessage UnrecognizedPropertyException "assessmentType".
        │        │   3.10d = blast radius.
        │        ├─ yes (downstream 5xx) ► OASys/NOMIS/ARNS outage. Confirm 5xx in
        │        │   dependencies.target. Raise with owning team. (5xx → 500.)
        │        └─ no  ► Unexpected 500 ► KQL 3.5 exceptions + Sentry for stack trace.
        │
        ├─ resultCode 404 ──► KQL 3.6a "No completed assessment" / "Failure to retrieve"
        │        ├─ SQL 4.3 shows a cached assessment id ► was working before ►
        │        │   check 3.8 for an auth/token blip at that time.
        │        └─ no cached id ► likely genuinely no completed LAYER3 OASys assessment.
        │
        └─ NO failing request found (3.1/3.4/3.5 empty) ──►  ⭐ MOST COMMON
                 ├─ KQL 3.3 shows downstream all 200? (healthy) AND
                 ├─ KQL 3.8 / 3.9 show auth/token "Connection reset" / "Read timed out"?
                 │     └─ YES ► transient HMPPS Auth token blip at page-load. Failure was
                 │              swallowed to null (API returned 200 w/ missing data) or
                 │              already recovered. Correlate 3.9 spike with report time.
                 │              Not a data problem for this prisoner. If sustained, raise
                 │              with HMPPS Auth / Cloud Platform.
                 └─ otherwise ► widen window (ago(7d)), or issue is UI-side / auth (401/403).
```

> **Worked outcome for `A8784EY` (2026-10-01):** The **24h window initially showed nothing** — the
> failures were on **29–30 Sep**, so we widened 3.1 to `ago(96h)`. That revealed a **persistent
> HTTP 500** on `GET /oasys/A8784EY/risks-and-alerts` (3× per load, UI retries), e.g.
> `operation_Id f26002df…`. 3.10a showed the ARNS predictors call returned **HTTP 200**, yet 3.2's
> end-to-end trace of that `operation_Id` contained
> `exception java.lang.RuntimeException: Unable to complete request. Service ARNS API for GET request
> to /assessments/id/2515574776/risk/predictors/all` — i.e. **our deserialisation of the 200 body
> failed**, not ARNS. 3.10b's `innermostMessage` pinned the exact cause:
> `com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field
> "assessmentType" … not marked as ignorable (4 known properties: "completedDate", "status",
> "outputVersion", "output")` — for **both** `AllPredictorVersionedDto` and
> `AllPredictorVersionedLegacyDto`. 3.10d confirmed it is **systemic** (343 prisoners over 10 days):
> ARNS added a new `assessmentType` field our DTOs don't tolerate. **Not** missing data, **not** an
> unmapped `outputVersion`, **not** the transient auth blip (which is a separate, real issue — see
> 3.8/3.9). DB side: SQL 4.1 showed 7 referrals (current `0d43e5f6-…` = `AWAITING_ASSESSMENT`); 4.3
> showed valid `pni_result` (crn `E557789`, assessmentId `2515574776`, pathway
> `MODERATE_INTENSITY_BC`) with `oasys_pni_result` **empty** — i.e. the prisoner's data is fine; the
> page breaks purely on the ARNS predictors deserialisation.
>
> **Lessons:** (1) always **widen the time window** before concluding "no problem"; (2) a **200
> dependency can still 500 the request** if the response body fails to deserialise — always read the
> `exceptions.innermostMessage`, not just the HTTP codes; (3) an unknown field (new
> `assessmentType`) breaks a DTO even when the polymorphic `outputVersion` subtype resolves fine —
> prefer `@JsonIgnoreProperties(ignoreUnknown = true)` on integration DTOs.

---

## 6. Quick identifier reference ✅

| Format | Regex | Example | Used by |
|---|---|---|---|
| Prison number (NOMS ID) | `^[A-Z]\d{4}[A-Z]{2}$` | `A8784EY` | `referral.prison_number`, OASys/NOMIS calls |
| Delius CRN | `^[A-Z]\d{6}$` | `X123456` | `pni_result.crn` only |
| Referral id | UUID | `…` | `referral.referral_id` |

---

## Refinement log

Append a dated entry whenever a query here was wrong and you corrected it. Keep the original
intent, record the real behaviour, and update the query above.

| Date | Query | What was wrong / learned | Fix applied |
|---|---|---|---|
| 2026-10-01 | Fix implemented (`APG-2707`) | **Applied 1b (ARNS-scoped) + 2a on branch `APG-2707/fix-risks-and-alerts-500-arns-predictors`.** 1b: disabled `FAIL_ON_UNKNOWN_PROPERTIES` on `AssessRiskAndNeedsApiClient`'s own `jacksonObjectMapper()` — covers the **whole ARNS response tree** (incl. deep nesting), ARNS-scoped, no 10-client blast radius; chosen over per-DTO `@JsonIgnoreProperties` (1a) which left nested score DTOs strict. 2a: `AssessRiskAndNeedsService.getRiskPredictors` now logs + returns `null` on `ClientResult.Failure` instead of rethrowing. | Added regression tests: `AssessRiskAndNeedsApiClientIntegrationTest` (new `assessmentType` wiremock stub with unknown fields at top level **and** nested in predictors) and new `AssessRiskAndNeedsServiceTest` (success passthrough + degrade-to-null). All pass; `ktlintFormat`/`ktlintCheck`/compile clean. Remaining out of scope → **separate PR**: ARNS retry/timeout + global `BaseHMPPSClient` unknown-field default. |
| 2026-10-01 | 3.10b (full stack) | **Confirmed the exact frames + a second innermost cause.** The `details.rawStack` showed `UnrecognizedPropertyException "assessmentType"` at `BaseHMPPSClient.kt:79` (readValue) for **both** `AllPredictorVersionedDto` and `AllPredictorVersionedLegacyDto` (ref chain `…Dto["assessmentType"]`). Also learned a **minority** of the same `risks-and-alerts` 500s have innermost `WebClientRequestException: Connection prematurely closed BEFORE response` instead — same wrapper/rethrow path, different trigger. | Added the "two innermost causes" ⚠️ note to the ⭐⭐ section; reaffirmed: always read `exceptions.details`/`innermostMessage`, not just the wrapper. | **Root cause locked down — no guesswork.** The earlier "unmapped `outputVersion`" hypothesis was **wrong**. 3.10b's `innermostMessage`/`details` showed `UnrecognizedPropertyException: Unrecognized field "assessmentType" … 4 known properties: completedDate, status, outputVersion, output` for **both** `AllPredictorVersionedDto` and `AllPredictorVersionedLegacyDto`. ARNS added a new field our DTOs reject (`FAIL_ON_UNKNOWN_PROPERTIES` default). Both `outputVersion` subtypes resolve fine. | Rewrote the ⭐⭐ root-cause section, 3.10b description, decision tree and worked outcome. Correctness fix changed from "add a subtype" to `@JsonIgnoreProperties(ignoreUnknown = true)` / disable `FAIL_ON_UNKNOWN_PROPERTIES`. |
| 2026-10-01 | 4.1 / 4.3 (confirm) | DB side validated: A8784EY has 7 referrals (current `0d43e5f6-…` = `AWAITING_ASSESSMENT`); `pni_result` holds crn `E557789`, assessmentId `2515574776`, pathway `MODERATE_INTENSITY_BC`; `oasys_pni_result` **empty**. Proves the prisoner's stored data is healthy — the 500 is purely ARNS predictors deserialisation. | No query change — recorded the confirmed values in the worked outcome. |
| 2026-10-01 | DB access | Prod `kubectl port-forward db-port-forward-pod` failed `pods … not found` because the pod was never created; `setup-service-pod.bash` is a different (IRSA) tool. | Added the correct prod/preprod order using `start-db-portforward-pod -ns prod` first. |
| 2026-10-01 | Fix review (source-validated) | **Full no-guesswork review of the fix options.** Read the actual source: both ARNS DTOs have 4 props + no `@JsonIgnoreProperties`; `AssessRiskAndNeedsService.getRiskPredictors` **rethrows** and its only caller is `OasysService.getRisks`; every sibling call uses `fetchDetail` (log WARN + `null`). **New validated fact:** the deserialiser is **not** Spring's mapper — all **10** clients pass a plain `jacksonObjectMapper()` (default `FAIL_ON_UNKNOWN_PROPERTIES = true`), so a global disable has a 10-client blast radius. | Expanded Layer 1 into options 1a/1b/1c and Layer 2 into 2a/2b with trade-offs; recommended **1a + 2a** for this PR; recorded ARNS retry/timeout + global client unknown-field hardening as a **separate PR**. |
| 2026-10-01 | 3.1 | Default `ago(24h)` showed **no results** for `A8784EY`; widening to `ago(96h)` revealed a persistent 500 on `risks-and-alerts` from **29–30 Sep**. **Always widen the window before concluding "no problem".** | Added "widen the window" guidance (24h→96h→10d) to the 3.1 notes. |
| 2026-10-01 | 3.2 | Returned **empty** for a 29 Sep `operation_Id` because it had **no `timestamp` filter** → used the Logs UI default (24h). | Added `where timestamp > ago(10d)` and a ⚠️ note to always set an explicit time filter. |
| 2026-10-01 | — (new) | Root cause for A8784EY identified: `OasysService.getRisks` calls `AssessRiskAndNeedsService.getRiskPredictors` **unguarded**; that method **rethrows** on `ClientResult.Failure`, so an ARNS predictors failure for one assessment 500s the whole `risks-and-alerts` endpoint. | Added "⭐⭐ ARNS risk-predictors rethrow" root-cause section + queries **3.10a/b/c**. |
| 2026-10-01 | 3.6 (old) | `where message has prisonNumber or message has "Failure to retrieve"` — the un-parenthesised `or` ignored the prison-number filter and returned **every** "Failure to retrieve" line for all prisoners. | Split into **3.6a** (prisoner only) and **3.6b** (systemic). Keep filters in separate `where` clauses. |
| 2026-10-01 | 3.1 / 3.4 / 3.5 | Returned **no results** for `A8784EY` even though the user saw the error. Learned that most failures are **swallowed to `null`** (API returns 200), so they never show as a failed request/exception for the prisoner. | Added "No results?" guidance; added auth-focused **3.8 / 3.9** to correlate by time instead. |
| 2026-10-01 | 3.3 | Confirmed working: returns host + success/fail counts. All downstream hosts were **200** during the incident, proving OASys/NOMIS themselves were healthy. | No change — keep as the "is it a real outage?" check. |
| 2026-10-01 | — (new) | Discovered the real pattern via 3.6b: transient `ClientAuthorizationException … oauth/token: Connection reset` / `Read timed out` and `recvAddress(-104): Connection reset by peer`. Root cause = HMPPS Auth token-fetch blips (no retry in `WebClientConfiguration`). | Added root-cause section, auth host row, and queries **3.8 / 3.9**. |
| _(template)_ | 3.4 | e.g. agent parameterises path so `data` shows `/assessments/timeline/{prisonerNumber}` not the raw id | filter on `operation_Id` from 3.1 instead of `data has prisonNumber` |

---

### Notes on confidence

- **Section 2, 4 and the flow in Section 2 are validated against source** (file paths cited).
- **Section 3 KQL** uses the standard App Insights tables (`requests`, `dependencies`,
  `exceptions`, `traces`) and the **validated** `cloud_RoleName` and PROD hostnames. The only
  thing to confirm on first run is how the **Java agent names/populates** `dependencies.name`
  vs `data` for parameterised paths — note any correction in the Refinement log.
```

