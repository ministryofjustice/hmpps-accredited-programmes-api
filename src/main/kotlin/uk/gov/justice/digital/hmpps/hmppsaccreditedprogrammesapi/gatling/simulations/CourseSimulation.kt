package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.gatling.simulations

import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.http.HttpDsl
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class CourseSimulation {

  val allCoursesScenario = CoreDsl.scenario("GET all courses endpoint")
    .exec(
      HttpDsl.http("Get all courses") // Name of the request
        .get("/courses") // Endpoint path
        .check(HttpDsl.status().`is`(200))
        .check(CoreDsl.bodyString().saveAs("responseBody")),
    )
    .pause(10.seconds.toJavaDuration())

  val courseAudienceScenario = CoreDsl.scenario("GET all audiences")
    .exec(
      HttpDsl.http("GET all audiences")
        .get("/courses/audiences")
        .check(HttpDsl.status().`is`(200))
        .check(CoreDsl.bodyString().saveAs("responseBody")),
    )
    .pause(10.seconds.toJavaDuration())

  val courseNamesScenario = CoreDsl.scenario("GET all course names")
    .exec(
      HttpDsl.http("GET all course names")
        .get("/courses/course-names")
        .check(HttpDsl.status().`is`(200))
        .check(CoreDsl.bodyString().saveAs("responseBody")),
    )
    .pause(10.seconds.toJavaDuration())

  val coursesByOrganisationScenario = CoreDsl.scenario("GET all WTI courses")
    .exec(
      HttpDsl.http("GET all WTI course names")
        .get("/organisations/WTI/courses")
        .check(HttpDsl.status().`is`(200))
        .check(CoreDsl.bodyString().saveAs("responseBody")),
    )
    .pause(10.seconds.toJavaDuration())
}
