package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.KeyDates
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.SentenceInformation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.ServiceUnavailableException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SentenceCategoryType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.SentenceCategoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.KeyDate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Sentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SentenceDetails
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

@Service
class PersonService(
  val prisonApiClient: PrisonApiClient,
  private val peopleSearchApiService: PeopleSearchApiService,
  private val personRepository: PersonRepository,
  private val sentenceCategoryRepository: SentenceCategoryRepository,
) {

  fun createOrUpdatePerson(prisonNumber: String) {
    val sentenceInformation = getSentenceInformation(prisonNumber)
    val sentenceType = determineSentenceType(sentenceInformation)

    peopleSearchApiService.getPrisoners(listOf(prisonNumber)).firstOrNull()?.let {
      var personEntity = personRepository.findPersonEntityByPrisonNumber(prisonNumber)
      if (personEntity == null) {
        val earliestReleaseDateAndType = earliestReleaseDateAndType(it)
        personEntity = PersonEntity(
          surname = it.lastName,
          forename = it.firstName,
          prisonNumber = prisonNumber,
          conditionalReleaseDate = it.conditionalReleaseDate,
          paroleEligibilityDate = it.paroleEligibilityDate,
          tariffExpiryDate = it.tariffDate,
          earliestReleaseDate = earliestReleaseDateAndType.first,
          earliestReleaseDateType = earliestReleaseDateAndType.second,
          indeterminateSentence = it.indeterminateSentence,
          nonDtoReleaseDateType = it.nonDtoReleaseDateType,
          sentenceType = sentenceType,
          location = it.prisonName,
          gender = it.gender,
        )
      } else {
        updatePerson(it, personEntity, sentenceType)
      }
      personRepository.save(personEntity)
    }
  }

  fun getPerson(prisonNumber: String) = personRepository.findPersonEntityByPrisonNumber(prisonNumber)

  private fun earliestReleaseDateAndType(prisoner: Prisoner): Pair<LocalDate?, String?> = getSentenceDetails(prisoner.prisonerNumber)
    ?.keyDates
    ?.firstOrNull { it.earliestReleaseDate == true }
    ?.let { Pair(it.date, it.description) }
    ?: Pair(null, null)

  private fun updatePerson(
    prisoner: Prisoner,
    personEntity: PersonEntity,
    sentenceType: String,
  ) {
    val earliestReleaseDateAndType = earliestReleaseDateAndType(prisoner)
    personEntity.surname = prisoner.lastName
    personEntity.forename = prisoner.firstName
    personEntity.conditionalReleaseDate = prisoner.conditionalReleaseDate
    personEntity.paroleEligibilityDate = prisoner.paroleEligibilityDate
    personEntity.tariffExpiryDate = prisoner.tariffDate
    personEntity.earliestReleaseDate = earliestReleaseDateAndType.first
    personEntity.earliestReleaseDateType = earliestReleaseDateAndType.second
    personEntity.indeterminateSentence = prisoner.indeterminateSentence
    personEntity.nonDtoReleaseDateType = prisoner.nonDtoReleaseDateType
    personEntity.sentenceType = sentenceType
    personEntity.location = if (prisoner.prisonName == "Outside") "Released" else prisoner.prisonName
    personEntity.gender = prisoner.gender
  }

  fun getSentenceInformation(prisonNumber: String): SentenceInformation? {
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

  fun determineSentenceType(sentenceInformation: SentenceInformation?): String {
    sentenceInformation ?: return SentenceCategoryType.NO_ACTIVE_SENTENCES.description
    val distinctActiveSentences = sentenceInformation.latestPrisonTerm.courtSentences
      .flatMap { it.sentences }
      .mapNotNull { it.sentenceTypeDescription }
      .distinct()

    if (distinctActiveSentences.isEmpty()) {
      log.info("No court sentences exist for prisoner - setting sentence type to NO_ACTIVE_SENTENCES")
      return SentenceCategoryType.NO_ACTIVE_SENTENCES.description
    }

    distinctActiveSentences.forEach { sentence ->
      log.info("Distinct active sentence for prisoner: {}", sentence)
    }

    val matchingSentenceCategories = sentenceCategoryRepository.findAllByDescriptionIn(distinctActiveSentences)
      .map { it.category }

    return if (matchingSentenceCategories.isEmpty()) {
      log.info("No matching categories exist")
      SentenceCategoryType.UNKNOWN.description
    } else {
      val overallCategoryDescription = SentenceCategoryType.determineOverallCategory(matchingSentenceCategories).description
      log.info("Category matches found for active sentences: $overallCategoryDescription")
      overallCategoryDescription
    }
  }

  fun getOffenceDetails(prisonNumber: String): List<Pair<String?, LocalDate?>> = getSentenceInformation(prisonNumber)?.latestPrisonTerm?.courtSentences
    ?.flatMap { it.sentences }
    ?.flatMap { it.offences.orEmpty() }
    ?.map { Pair(it.offenceCode, it.offenceStartDate) }
    ?.distinct()
    .orEmpty()

  fun getSentenceDetails(prisonNumber: String): SentenceDetails? {
    val sentenceInformation = getSentenceInformation(prisonNumber)
      ?: throw NotFoundException("No sentence information found for person with id: $prisonNumber")

    val sentences = sentenceInformation.latestPrisonTerm.courtSentences
      .flatMap { it.sentences }
      .map { Sentence(it.sentenceTypeDescription, it.sentenceStartDate) }
    val keyDates = buildKeyDates(sentenceInformation)
    return SentenceDetails(sentences, keyDates)
  }

  fun updatePerson(prisonNumber: String, fromUpdateEndpoint: Boolean = false) {
    log.info("Attempting to update person with prison number: $prisonNumber")
    val personEntity = personRepository.findPersonEntityByPrisonNumber(prisonNumber)
    if (personEntity != null) {
      log.info("Prisoner is of interest to ACP - about to update: $prisonNumber fromUpdateEndpoint=$fromUpdateEndpoint")
      val sentenceInformation = getSentenceInformation(prisonNumber)
      val sentenceType = determineSentenceType(sentenceInformation)
      peopleSearchApiService.getPrisoners(listOf(prisonNumber)).firstOrNull()?.let {
        updatePerson(it, personEntity, sentenceType)
      }
      personRepository.save(personEntity)
      log.info("Prisoner $prisonNumber update successful")
    } else {
      log.info("Prisoner is not of interest to ACP: $prisonNumber fromUpdateEndpoint=$fromUpdateEndpoint")
    }
  }

  fun updateAllPeople() {
    log.info("Attempting to update all people in person cache.")
    val people = personRepository.findAll()
    people.forEach {
      updatePerson(it.prisonNumber, true)
    }
    log.info("Updated all people in person cache.")
  }

  fun updatePeople(prisonNumbers: List<String>) {
    log.info("Attempting to update $prisonNumbers in person cache.")
    prisonNumbers.forEach {
      updatePerson(it, true)
    }
    log.info("Updated $prisonNumbers  people in person cache.")
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
    if (keyDates.isNotEmpty()) {
      // now find the earliest of these dates:
      val earliestReleaseDateCode =
        keyDates.filter { it.date != null }.minBy { it.date!! }.code
      val remappedKeyDates = keyDates.map { it.copy(earliestReleaseDate = (it.code == earliestReleaseDateCode)) }
      return remappedKeyDates
    } else {
      return keyDates
    }
  }

  fun createKeyDate(releaseDateType: KeyDateType, date: LocalDate?): KeyDate = KeyDate(
    type = releaseDateType.mapping,
    code = releaseDateType.code,
    description = releaseDateType.description,
    earliestReleaseDate = false,
    date = date,
    order = releaseDateType.order,
  )

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
    LICENCE_END_DATE("licenceExpiryDate", "LED", "Licence end date", 65),
    MID_TERM_DATE("midTermDate", "MTD", "Mid term date", 70),
    NON_PAROLE_DATE("nonParoleDate", "NPD", "Non-parole date", 80),
    TARIFF_EARLY_REMOVAL_SCHEME_ELIGIBILITY_DATE(
      "tariffEarlyRemovalSchemeEligibilityDate",
      "TERSED",
      "Tariff expired release scheme eligibility date",
      90,
    ),
    TARIFF_DATE("tariffDate", "TED", "Tariff end date", 100),
    PAROLE_ELIGIBILITY_DATE("paroleEligibilityDate", "PED", "Parole eligibility date", 110),
    POST_RECALL_RELEASE_DATE("postRecallReleaseDate", "PRRD", "Post recall release date", 120),
    RELEASE_DATE("releaseDate", "RD", "Confirmed release date", 130),
    SENTENCE_END_DATE("sentenceExpiryDate", "SED", "Sentence end date", 140),
    ;

    companion object {
      private val mappingToEnum: Map<String, KeyDateType> = entries.associateBy { it.mapping }

      fun fromMapping(mapping: String): KeyDateType? = mappingToEnum[mapping]
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
