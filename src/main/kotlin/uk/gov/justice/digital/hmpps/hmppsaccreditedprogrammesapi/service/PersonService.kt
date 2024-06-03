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
class PersonService(
  val prisonApiClient: PrisonApiClient,
) {

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
      .flatMap { it.sentences }
      .map { it.sentenceTypeDescription }
      .distinct()

    return when {
      activeSentences.isEmpty() -> "No active sentences"
      activeSentences.size == 1 -> activeSentences.first().toString()
      else -> "Multiple sentences"
    }
  }

  fun getOffenceDetails(prisonNumber: String): List<Pair<String?, LocalDate?>> {
    return getSentenceInformation(prisonNumber)?.latestPrisonTerm?.courtSentences
      ?.flatMap { it.sentences }
      ?.flatMap { it.offences.orEmpty() }
      ?.map { Pair(it.offenceCode, it.offenceStartDate) }
      ?.distinct()
      .orEmpty()
  }

  fun getSentenceDetails(prisonNumber: String): SentenceDetails? {
    val sentenceInformation = getSentenceInformation(prisonNumber)
      ?: throw NotFoundException("No sentence information found for person with id: $prisonNumber")

    val sentences = sentenceInformation.latestPrisonTerm.courtSentences
      .flatMap { it.sentences }
      .map { Sentence(it.sentenceTypeDescription, it.sentenceStartDate) }
    val keyDates = buildKeyDates(sentenceInformation)
    return SentenceDetails(sentences, keyDates)
  }

  private fun buildKeyDates(sentenceInformation: SentenceInformation): List<KeyDate> {
    val keyDates = ArrayList<KeyDate>()
    for (date in KeyDates::class.memberProperties) {
      val keyDateType = KeyDateType.fromMapping(date.name)
      if (date.get(sentenceInformation.latestPrisonTerm.keyDates) != null && keyDateType != null) {
        keyDates.add(
          createKeyDate(
            keyDateType,
            date.get(sentenceInformation.latestPrisonTerm.keyDates) as LocalDate,
          ),
        )
      }
    }

    // now find the earliest of these dates:
    val earliestReleaseDateCode =
      keyDates.filter { it.date != null }.minBy { it.date!! }.code
    val remappedKeyDates = keyDates.map { it.copy(earliestReleaseDate = (it.code == earliestReleaseDateCode)) }

    return remappedKeyDates
  }

  fun createKeyDate(releaseDateType: KeyDateType, date: LocalDate?): KeyDate {
    return KeyDate(
      type = releaseDateType.mapping,
      code = releaseDateType.code,
      description = releaseDateType.description,
      earliestReleaseDate = false,
      date = date,
      order = releaseDateType.order,
    )
  }

  enum class KeyDateType(val mapping: String, val code: String, val description: String, val order: Int = 1) {
    ACTUAL_PAROLE_DATE("actualParoleDate", "APD", "Approved parole date", 10),
    AUTOMATIC_RELEASE_DATE("automaticReleaseDate", "ARD", "Automatic release date", 20),
    CONDITIONAL_RELEASE_DATE("conditionalReleaseDate", "CRD", "Conditional release date", 30),
    EARLY_REMOVAL_SCHEME_ELIGIBILITY_DATE(
      "earlyRemovalSchemeEligibilityDate",
      "ERSED",
      "Early removal scheme eligibility date",
      40,
    ),
    HOME_DETENTION_CURFEW_ACTUAL_DATE(
      "homeDetentionCurfewActualDate",
      "HDCAD",
      "Home detention curfew approved date",
      50,
    ),
    HOME_DETENTION_CURFEW_ELIGIBILITY_DATE(
      "homeDetentionCurfewEligibilityDate",
      "HDCED",
      "Home detention curfew eligibility date",
      60,
    ),
    MID_TERM_DATE("midTermDate", "MTD", "Mid term date", 70),
    NON_PAROLE_DATE("nonParoleDate", "NPD", "Non-parole date", 80),
    TARIFF_EARLY_REMOVAL_SCHEME_ELIGIBILITY_DATE(
      "tariffEarlyRemovalSchemeEligibilityDate",
      "TERSED",
      "Tariff expired release scheme eligibility date",
      90,
    ),
    TARIFF_DATE("tariffDate", "TED", "Tariff Expiry Date", 100),
    PAROLE_ELIGIBILITY_DATE("paroleEligibilityDate", "PED", "Parole eligibility date", 110),
    POST_RECALL_RELEASE_DATE("postRecallReleaseDate", "PRRD", "Post recall release date", 120),
    RELEASE_DATE("releaseDate", "RD", "Release date", 130),
    ;

    companion object {
      private val mappingToEnum: Map<String, KeyDateType> = entries.associateBy { it.mapping }

      fun fromMapping(mapping: String): KeyDateType? {
        return mappingToEnum[mapping]
      }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
