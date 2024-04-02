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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.PrisonApiUnavailableException
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

private const val PRISON_API = "PRISON_API"

@Service
class PersonService(val prisonApiClient: PrisonApiClient) {

  private fun getSentenceInformation(prisonNumber: String): SentenceInformation? {
    return when (val response = prisonApiClient.getSentenceInformation(prisonNumber)) {
      is ClientResult.Success -> {
        log.debug("Retrieved sentence data for $prisonNumber")
        AuthorisableActionResult.Success(response.body).entity
      }
      is ClientResult.Failure -> {
        val exception = response.toException().cause
        log.error("Failure to retrieve or parse data for prisonNumber: $prisonNumber reason: $exception")
        when (response) {
          is ClientResult.Failure.StatusCode -> {
            if (response.status.is5xxServerError) {
              log.error("Failure to retrieve or parse data from $PRISON_API for prisonNumber: $prisonNumber reason: $exception")
              throw PrisonApiUnavailableException(
                "Failure to retrieve data from $PRISON_API with prisonNumber: $prisonNumber statusCode: ${response.status.value()} from $PRISON_API",
                response.toException(),
              )
            } else if (response.status.value() == 404) {
              log.warn("No data found in $PRISON_API for prisonNumber: $prisonNumber reason: $exception ")
              return null
            } else {
              log.error("Something went wrong whilst retrieving or parsing data from $PRISON_API for prisonNumber: $prisonNumber reason: $exception")
              throw response.toException()
            }
          }
          is ClientResult.Failure.Other -> throw response.toException()
        }
      }
    }
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
