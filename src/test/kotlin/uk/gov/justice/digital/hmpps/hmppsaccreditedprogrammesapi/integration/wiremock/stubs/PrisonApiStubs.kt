package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.wiremock.stubs

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.SentenceInformation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil.SentenceInformationFactory

class PrisonApiStubs(
  val wiremock: WireMockExtension,
  val objectMapper: ObjectMapper,
) {

  fun stubSuccessfulGetSentenceInformation(prisonNumber: String, sentenceInformation: SentenceInformation = SentenceInformationFactory().produce()) {
    wiremock.stubFor(
      get(urlEqualTo("/api/offenders/$prisonNumber/booking/latest/sentence-summary"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(sentenceInformation)),
        ),
    )
  }
}
