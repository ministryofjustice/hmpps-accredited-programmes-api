package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.gatling.simulations

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

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
    .pause(10.seconds.toJavaDuration())

  private val courseAudienceScenario = scenario("GET all audiences")
    .exec(
      http("GET all audiences")
        .get("/courses/audiences")
        .check(status().`is`(200))
        .check(bodyString().saveAs("responseBody")),
    )
    .pause(10.seconds.toJavaDuration())

  private val courseNamesScenario = scenario("GET all course names")
    .exec(
      http("GET all course names")
        .get("/courses/course-names")
        .check(status().`is`(200))
        .check(bodyString().saveAs("responseBody")),
    )
    .pause(10.seconds.toJavaDuration())

  private val coursesByOrganisationScenario = scenario("GET all WTI courses")
    .exec(
      http("GET all WTI course names")
        .get("/organisations/WTI/courses")
        .check(status().`is`(200))
        .check(bodyString().saveAs("responseBody")),
    )
    .pause(10.seconds.toJavaDuration())


  init {
    setUp(
      allCoursesScenario.injectOpen(
        constantUsersPerSec(50.0).during(1.minutes.toJavaDuration()).randomized(),
        stressPeakUsers(40).during(1.minutes.toJavaDuration()),
      ),
      courseAudienceScenario.injectOpen(
        constantUsersPerSec(10.0).during(1.minutes.toJavaDuration()).randomized(),
        stressPeakUsers(40).during(1.minutes.toJavaDuration()),
      ),

      courseNamesScenario.injectOpen(
        constantUsersPerSec(20.0).during(2.minutes.toJavaDuration()).randomized(),
        stressPeakUsers(40).during(1.minutes.toJavaDuration()),
      ),

      coursesByOrganisationScenario.injectOpen(
        constantUsersPerSec(20.0).during(2.minutes.toJavaDuration()).randomized(),
        stressPeakUsers(40).during(1.minutes.toJavaDuration()),
      ),
    ).protocols(httpProtocol)
  }
}

private val objectMapper = lazy { ObjectMapper().findAndRegisterModules() }

fun toJson(value: Any) = StringBody(objectMapper.value.writeValueAsString(value))

fun toJson(f: (Session) -> Any) = StringBody { session -> objectMapper.value.writeValueAsString(f(session)) }
