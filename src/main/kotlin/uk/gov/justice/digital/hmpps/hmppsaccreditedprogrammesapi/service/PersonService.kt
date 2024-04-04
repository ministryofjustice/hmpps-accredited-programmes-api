package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.KeyDate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Sentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.SentenceDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.KeyDates
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.SentenceInformation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

@Service
class PersonService(val prisonApiClient: PrisonApiClient) {

  private fun getSentenceInformation(prisonNumber: String): SentenceInformation? {
    val sentenceInformation = when (val response = prisonApiClient.getSentenceInformation(prisonNumber)) {
      is ClientResult.Failure.Other -> throw ServiceUnavailableException(
        "Request to ${response.serviceName} failed. Reason ${response.toException().message} method ${response.method} path ${response.path}",
        response.toException(),
      )

      is ClientResult.Failure -> {
        log.error("Failure to retrieve or parse data for $prisonNumber  ${response.toException().cause}")
        AuthorisableActionResult.Success(null)
      }

      is ClientResult.Success -> {
        log.debug("Retrieved sentence data for $prisonNumber")
        AuthorisableActionResult.Success(response.body)
      }
    }
    return sentenceInformation.entity
  }

  fun getSentenceType(prisonNumber: String): String {
    val sentenceInformation = getSentenceInformation(prisonNumber) ?: return "No active sentences"
    val activeSentences = sentenceInformation.latestPrisonTerm.courtSentences
      .filter { it.caseStatus == "ACTIVE" }
      .flatMap { it.sentences }
      .map { it.sentenceTypeDescription }
      .distinct()

    return when {
      activeSentences.isEmpty() -> "No active sentences"
      activeSentences.size == 1 -> activeSentences.first().toString()
      else -> "Multiple sentences"
    }
  }

  fun getSentenceDetails(prisonNumber: String): SentenceDetails? {
    val sentenceInformation = getSentenceInformation(prisonNumber)
      ?: throw NotFoundException("No sentence information found for person with id: $prisonNumber")

    val sentences = sentenceInformation.latestPrisonTerm.courtSentences
      .filter { it.caseStatus == "ACTIVE" }
      .flatMap { it.sentences }
      .map { Sentence(it.sentenceTypeDescription, it.sentenceStartDate) }

    val keyDates = ArrayList<KeyDate>()
    for (date in KeyDates::class.memberProperties) {
      keyDates.add(KeyDate(date.name, date.get(sentenceInformation.latestPrisonTerm.keyDates) as LocalDate?))
    }

    return SentenceDetails(sentences, keyDates)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
