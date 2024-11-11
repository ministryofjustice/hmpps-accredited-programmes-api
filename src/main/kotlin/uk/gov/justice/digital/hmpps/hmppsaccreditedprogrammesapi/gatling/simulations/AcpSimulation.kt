package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.gatling.simulations

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import java.util.*

class AcpSimulation : Simulation() {
  private val httpProtocol = http
    .baseUrl("https://accredited-programmes-api-dev.hmpps.service.justice.gov.uk")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader("Bearer TOKEN")

  private val allCoursesScenario = scenario("GET all courses endpoint")
    .exec(
      http("Get all courses") // Name of the request
        .get("/courses") // Endpoint path
        .check(status().`is`(200))
        .check(bodyString().saveAs("responseBody")),
    )
    .exec { session ->
      // Log the response body
      val responseBody = session.getString("responseBody")
//      println("Response Body: $responseBody")
      session
    }

  private val courseAudienceScenario = scenario("GET all audiences")
    .exec(
      http("GET all audiences")
        .get("/courses/audiences")
        .check(status().`is`(200))
        .check(bodyString().saveAs("responseBody")),
    )

  private val courseNamesScenario = scenario("GET all course names")
    .exec(
      http("GET all course names")
        .get("/courses/course-names")
        .check(status().`is`(200))
        .check(bodyString().saveAs("responseBody")),
    )

  private val coursesByOrganisationScenario = scenario("GET all WTI courses")
    .exec(
      http("GET all WTI course names")
        .get("/organisations/WTI/courses")
        .check(status().`is`(200))
        .check(bodyString().saveAs("responseBody")),
    )

//  private val createReferral = scenario("Create referrals")
//    .exec(
//      http("POST create referral")
//        .post("/referrals")
//        .body(
//          toJson {
//            ReferralCreate(
//              offeringId = UUID.fromString("72820fe9-ad4a-4d1a-b730-ded300075749"),
//              prisonNumber = "G8335GI",
//            )
//          },
//        )
//        .check(status().`is`(201))
//        .check(bodyString().saveAs("responseBody"))
//    ).exec { session ->
//      // Log the response body
//      val responseBody = session.getString("responseBody")
//      println("Response Body: $responseBody")
//      session
//    }

  init {
    setUp(
      allCoursesScenario.injectOpen(atOnceUsers(1)),
      courseAudienceScenario.injectOpen(atOnceUsers(1)),
      courseNamesScenario.injectOpen(atOnceUsers(1)),
      coursesByOrganisationScenario.injectOpen(atOnceUsers(1)),
//        createReferral.injectOpen(atOnceUsers(1))
    ).protocols(httpProtocol)
  }
}

private val objectMapper = lazy { ObjectMapper().findAndRegisterModules() }

fun toJson(value: Any) = StringBody(objectMapper.value.writeValueAsString(value))

fun toJson(f: (Session) -> Any) = StringBody { session -> objectMapper.value.writeValueAsString(f(session)) }
