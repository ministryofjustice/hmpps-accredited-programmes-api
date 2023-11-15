package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.response.model.SentenceAndOffenceDetails

@Component
class PrisonApiClient(
  @Qualifier("prisonApiWebClient") webClient: WebClient,
  objectMapper: ObjectMapper,
) : BaseHMPPSClient(webClient, objectMapper) {

  fun getSentencesAndOffencesByBookingId(bookingID: Int) = getRequest<List<SentenceAndOffenceDetails>> {
    path = "/api/offender-sentences/booking/$bookingID/sentences-and-offences"
  }
}
