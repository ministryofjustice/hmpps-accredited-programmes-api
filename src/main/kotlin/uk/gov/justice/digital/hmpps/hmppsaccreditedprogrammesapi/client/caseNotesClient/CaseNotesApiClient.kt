package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesClient

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesClient.model.CaseNoteCreatedResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesClient.model.CaseNoteRequest

private const val CASE_NOTES_API = "Case Notes API"

@Component
class CaseNotesApiClient(
  @Qualifier("caseNotesApiWebClient")
  private val webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun createCaseNote(caseNoteRequest: CaseNoteRequest, offenderIdentifier: String) =
    postRequest<CaseNoteCreatedResponse>(CASE_NOTES_API) {
      path = "/system-generated/case-notes/$offenderIdentifier"
      body = caseNoteRequest
    }
}
