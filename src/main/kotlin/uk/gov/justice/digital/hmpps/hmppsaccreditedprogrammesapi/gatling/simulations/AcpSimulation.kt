package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.gatling.simulations

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.core.CoreDsl.StringBody
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.bodyString
import io.gatling.javaapi.core.CoreDsl.constantUsersPerSec
import io.gatling.javaapi.core.CoreDsl.csv
import io.gatling.javaapi.core.CoreDsl.jsonPath
import io.gatling.javaapi.core.CoreDsl.rampUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.CoreDsl.stressPeakUsers
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


class AcpSimulation : Simulation() {

  val courseSimulation = CourseSimulation()


  private val httpProtocol = HttpDsl.http
    .baseUrl("https://accredited-programmes-api-preprod.hmpps.service.justice.gov.uk")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader("Bearer <TOKEN>")


  private val createReferral = scenario("Create and Submit Referral")
    .feed(csv("prisonIds.csv").circular()) // Load CSV with prison numbers
    .exec { session ->
      val prisonNumber = session.getString("name") // Retrieve the 'name' column from the CSV
      println("Creating referral for prison number: $prisonNumber")
      session.set("prisonNumber", prisonNumber) // Set 'prisonNumber' in the session
    }
    .exec(
      http("POST create referral")
        .post("/referrals")
        .header("Content-Type", "application/json")
        .body(
          StringBody(
            """{
              "offeringId": "09a684c5-ebb1-43b3-ac63-b53c1da20640",
              "prisonNumber": "#{prisonNumber}"
            }"""
          )
        )
        .check(status().`is`(201))
        .check(jsonPath("$.id").saveAs("referralId")) // Save the created referral ID
    )
    .exec { session ->
      val referralId = session.getString("referralId")
      println("Referral ID: $referralId")
      session
    }
    .exec(
      http("PUT update referral")
        .put("/referrals/#{referralId}")
        .header("Content-Type", "application/json")
        .body(
          StringBody(
            """{
              "oasysConfirmed": true,
              "hasReviewedProgrammeHistory": true,
              "additionalInformation": "this is a test"
            }"""
          )
        )
        .check(status().`is`(204))
    )
    .exec(
      http("POST submit referral")
        .post("/referrals/#{referralId}/submit")
        .check(status().`is`(200))
        .check(bodyString().saveAs("submitResponseBody"))
    )
    .exec { session ->
      val responseBody = session.getString("submitResponseBody")
      println("Submit Referral Response: $responseBody")
      session
    }
    .exec(
      http("PUT update referral status after submission")
        .put("/referrals/#{referralId}/status")
        .header("Content-Type", "application/json")
        .body(
          StringBody(
            """{
              "status": "WITHDRAWN",
              "category": "W_ADMIN",
              "reason": "W_DUPLICATE",
              "notes": "this is a test",
              "ptUser": false
            }"""
          )
        )
        .check(status().`is`(204))
    )

  val myCaseLoadScenario = CoreDsl.scenario("GET caseloads for my user")
    .exec(
      HttpDsl.http("GET caseloads for an org") // Name of the request
        .get("/referrals/view/me/dashboard") // Endpoint path
        .check(HttpDsl.status().`is`(200))
        .check(CoreDsl.bodyString().saveAs("responseBody")),
    )
    .pause(10.seconds.toJavaDuration())

  val caseLoadForAnOrgScenario = CoreDsl.scenario("GET caseloads for an org")
    .exec(
      HttpDsl.http("GET caseloads for an org") // Name of the request
        .get("/referrals/view/organisation/WTI/dashboard") // Endpoint path
        .check(HttpDsl.status().`is`(200))
        .check(CoreDsl.bodyString().saveAs("responseBody")),
    )
    .pause(10.seconds.toJavaDuration())

  private val pniScenario = scenario("Read Prison ID")
    .feed(csv("prisonIds.csv").circular()) // Load CSV with prison numbers
    .exec { session ->
      val prisonNumber = session.getString("name") // Retrieve the 'name' column from the CSV
      println("Creating referral for prison number: $prisonNumber")
      session.set("prisonNumber", prisonNumber) // Set 'prisonNumber' in the session
    }
    .exec(
      HttpDsl.http("Get PNI") // Name of the request
        .get("/PNI/#{prisonNumber}") // Endpoint path
        .check(HttpDsl.status().`is`(200))
        .check(CoreDsl.bodyString().saveAs("responseBody")),
    )
    .pause(10.seconds.toJavaDuration())


  init {
    setUp(
      courseSimulation.allCoursesScenario.injectOpen(
        rampUsers(100).during(2.minutes.toJavaDuration()), // Ramp-up to 50 users in 2 mins
        constantUsersPerSec(6.0).during(16.minutes.toJavaDuration()).randomized() // Maintain load
      ),
      courseSimulation.courseAudienceScenario.injectOpen(
        rampUsers(100).during(2.minutes.toJavaDuration()),
        constantUsersPerSec(6.0).during(16.minutes.toJavaDuration()).randomized()
      ),
      courseSimulation.courseNamesScenario.injectOpen(
        rampUsers(100).during(2.minutes.toJavaDuration()),
        constantUsersPerSec(6.0).during(16.minutes.toJavaDuration()).randomized()
      ),
      courseSimulation.coursesByOrganisationScenario.injectOpen(
        rampUsers(100).during(2.minutes.toJavaDuration()),
        constantUsersPerSec(6.0).during(16.minutes.toJavaDuration()).randomized()
      ),
      createReferral.injectOpen(
        rampUsers(100).during(2.minutes.toJavaDuration()),
        constantUsersPerSec(6.0).during(16.minutes.toJavaDuration()).randomized()
      ),
      pniScenario.injectOpen(
        rampUsers(100).during(2.minutes.toJavaDuration()),
        constantUsersPerSec(6.0).during(16.minutes.toJavaDuration()).randomized()
      ),
      myCaseLoadScenario.injectOpen(
        rampUsers(100).during(2.minutes.toJavaDuration()),
        constantUsersPerSec(6.0).during(16.minutes.toJavaDuration()).randomized()
      ),
      caseLoadForAnOrgScenario.injectOpen(
        rampUsers(100).during(2.minutes.toJavaDuration()),
        constantUsersPerSec(6.0).during(16.minutes.toJavaDuration()).randomized()
      )
    ).protocols(httpProtocol)
  }



}

private val objectMapper = lazy { ObjectMapper().findAndRegisterModules() }

val json = Json {
  serializersModule = SerializersModule {
    contextual(UUID::class, UUIDSerializer)
  }
}
object UUIDSerializer : KSerializer<UUID> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: UUID) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): UUID {
    return UUID.fromString(decoder.decodeString())
  }
}

private fun toJson(referralCreate: ReferralCreate): String =
  json.encodeToString(referralCreate)

@Serializable
data class ReferralCreate(
  @Contextual val offeringId: UUID,
  val prisonNumber: String,
)
