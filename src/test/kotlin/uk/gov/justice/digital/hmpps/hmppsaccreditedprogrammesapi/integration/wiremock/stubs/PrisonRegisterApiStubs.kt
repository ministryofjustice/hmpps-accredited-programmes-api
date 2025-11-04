package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.wiremock.stubs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.Prison
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil.PrisonFactory

class PrisonRegisterApiStubs(
  val wiremock: WireMockServer,
  val objectMapper: ObjectMapper,
) {

  fun stubSuccessfulGetPrison(prisonId: String, prison: Prison = PrisonFactory().produce()) {
    wiremock.stubFor(
      get(urlEqualTo("/prisons/id/$prisonId"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(prison)),
        ),
    )
  }

  fun stubSuccessfulGetPrisons(prisons: List<Prison> = listOf(PrisonFactory().produce())) {
    wiremock.stubFor(
      get(urlEqualTo("/prisons/names"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(prisons)),
        ),
    )
  }

  fun stubSuccessfulGetPrisonsByIds(prisonIds: List<String>, prisons: List<Prison> = listOf(PrisonFactory().produce())) {
    wiremock.stubFor(
      post(urlEqualTo("/prisons/prisonsByIds"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(prisons)),
        ),
    )
  }

  fun stubNotFoundGetPrison(prisonId: String) {
    wiremock.stubFor(
      get(urlEqualTo("/prisons/id/$prisonId"))
        .willReturn(
          aResponse()
            .withStatus(404),
        ),
    )
  }

  fun stubServerErrorGetPrison(prisonId: String) {
    wiremock.stubFor(
      get(urlEqualTo("/prisons/id/$prisonId"))
        .willReturn(
          aResponse()
            .withStatus(500),
        ),
    )
  }
}
