package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.KeyDateType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.KeyDateType.Companion.relevantDatesForEarliestReleaseDateCalculationCodesForCaseList
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
        log.info("Earliest release date and type for prisoner not in our database $prisonNumber ${earliestReleaseDateAndType.first} ${earliestReleaseDateAndType.second}")
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
    log.info("Earliest release date and type for prisoner in our database ${prisoner.prisonerNumber} ${earliestReleaseDateAndType.first} ${earliestReleaseDateAndType.second}")
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
      .filter { it.sentenceEndDate?.isAfter(LocalDate.now()) ?: true }
      .mapNotNull { it.sentenceTypeDescription }
      .distinct()

    if (distinctActiveSentences.isEmpty()) {
      log.info("No court sentences exist for prisoner ${sentenceInformation.prisonerNumber} - setting sentence type to NO_ACTIVE_SENTENCES")
      return SentenceCategoryType.NO_ACTIVE_SENTENCES.description
    }

    distinctActiveSentences.forEach { sentence ->
      log.info("Distinct active sentence for prisoner: {}", sentence)
    }

    val matchingSentenceCategories = sentenceCategoryRepository.findAllByDescriptionIn(distinctActiveSentences)
      .map { it.category }

    return if (matchingSentenceCategories.isEmpty()) {
      log.info("No matching categories exist for prisoner ${sentenceInformation.prisonerNumber}")
      SentenceCategoryType.UNKNOWN.description
    } else {
      val overallCategoryDescription =
        SentenceCategoryType.determineOverallCategory(matchingSentenceCategories).description
      log.info("Category matches found for prisoner ${sentenceInformation.prisonerNumber} active sentences: $overallCategoryDescription")
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
    try {
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
    } catch (ex: Exception) {
      log.warn("Failed to update person with prisonNumber $prisonNumber", ex)
    }
  }

  private val coroutineScope = CoroutineScope(Dispatchers.IO)

  fun updateAllPeople(batchSize: Int = 500) = runBlocking {
    val people = personRepository.findAll()

    val chunks = people.chunked(batchSize)

    log.info("Found ${chunks.size} of $batchSize each ")

    val jobs = chunks.mapIndexed { index, chunk ->
      coroutineScope.launch {
        log.info("Processing chunk ${index + 1}/${chunks.size} with ${chunk.size} people on ${Thread.currentThread().name}")
        chunk.forEach { person ->
          updatePerson(person.prisonNumber, true)
        }
        log.info("Processed chunk ${index + 1}/${chunks.size} with ${chunk.size} people on ${Thread.currentThread().name}")
      }
    }

    jobs.joinAll()
    log.info("Finished updating all people.")
  }

  fun updatePeople(prisonNumbers: List<String>) {
    log.info("Attempting to update $prisonNumbers in person cache.")
    prisonNumbers.forEach {
      updatePerson(it, true)
    }
    log.info("Updated $prisonNumbers  people in person cache.")
  }

  fun buildKeyDates(sentenceInformation: SentenceInformation): List<KeyDate> {
    val keyDates = ArrayList<KeyDate>()
    try {
      for (date in KeyDates::class.memberProperties) {
        val keyDateType = KeyDateType.fromMapping(date.name)
        if (date.get(sentenceInformation.latestPrisonTerm.keyDates) != null && keyDateType != null) {
          keyDates.add(
            createKeyDate(
              releaseDateType = keyDateType,
              date = date.get(sentenceInformation.latestPrisonTerm.keyDates) as LocalDate,
            ),
          )
        }
      }
      if (keyDates.isNotEmpty()) {
        // now find the earliest of these dates:
        val earliestReleaseDateCode =
          keyDates.filter { it.code in relevantDatesForEarliestReleaseDateCalculationCodesForCaseList }
            .takeIf { it.isNotEmpty() }
            ?.minBy { it.date!! }?.code
        val remappedKeyDates = keyDates.map { it.copy(earliestReleaseDate = (it.code == earliestReleaseDateCode)) }
        return remappedKeyDates
      } else {
        return keyDates
      }
    } catch (ex: Exception) {
      log.warn("Failed to build key dates for ${sentenceInformation.prisonerNumber}", ex)
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

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
