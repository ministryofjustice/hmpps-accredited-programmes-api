package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.PrisonApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.KeyDates
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.PrisonTerm
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.SentenceInformation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SentenceCategoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SentenceCategoryType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.SentenceCategoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil.SentenceInformationFactory
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val PRISON_NUMBER = "Prisoner123"

@ExtendWith(MockitoExtension::class)
class PersonServiceTest {

  @Mock
  lateinit var prisonApiClient: PrisonApiClient

  @Mock
  lateinit var peopleSearchApiService: PeopleSearchApiService

  @Mock
  lateinit var personRepository: PersonRepository

  @Mock
  lateinit var sentenceCategoryRepository: SentenceCategoryRepository

  @Captor
  lateinit var personEntityCaptor: ArgumentCaptor<PersonEntity>

  @InjectMocks
  lateinit var personService: PersonService

  private fun mockGetPrisoners() {
    val prisoner = Prisoner(
      prisonerNumber = PRISON_NUMBER,
      prisonName = "Prison 1",
      firstName = "John",
      lastName = "Doe",
      tariffDate = LocalDate.of(1985, 1, 1),
      gender = "Male",
    )
    whenever(peopleSearchApiService.getPrisoners(listOf(PRISON_NUMBER))).thenReturn(listOf(prisoner))
  }

  private fun mockGetSentenceInformation() {
    val sentenceInformation = SentenceInformationFactory().createSentenceInformationWithMultipleSentences(
      prisonerNumber = PRISON_NUMBER,
      sentenceTypes = listOf("Sentence Type 1", "Sentence Type 2", "Sentence Type 3"),
    )
    val response = ClientResult.Success<SentenceInformation>(HttpStatus.OK, sentenceInformation)
    whenever(prisonApiClient.getSentenceInformation(PRISON_NUMBER)).thenReturn(response)
  }

  private fun determinateAndIndeterminateSentenceCategories(): List<SentenceCategoryEntity> = listOf<SentenceCategoryEntity>(
    SentenceCategoryEntity(
      description = "Sentence Type 1",
      category = SentenceCategoryType.INDETERMINATE,
    ),
    SentenceCategoryEntity(
      description = "Sentence Type 2",
      category = SentenceCategoryType.DETERMINATE,
    ),
    SentenceCategoryEntity(
      description = "Sentence Type 3",
      category = SentenceCategoryType.INDETERMINATE,
    ),
  )

  private fun indeterminateSentenceCategories(): List<SentenceCategoryEntity> = listOf<SentenceCategoryEntity>(
    SentenceCategoryEntity(
      description = "Sentence Type 1",
      category = SentenceCategoryType.UNKNOWN,
    ),
    SentenceCategoryEntity(
      description = "Sentence Type 2",
      category = SentenceCategoryType.UNKNOWN,
    ),
    SentenceCategoryEntity(
      description = "Sentence Type 3",
      category = SentenceCategoryType.INDETERMINATE,
    ),
  )

  private fun determinateSentenceCategories(): List<SentenceCategoryEntity> = listOf<SentenceCategoryEntity>(
    SentenceCategoryEntity(
      description = "Sentence Type 1",
      category = SentenceCategoryType.DETERMINATE,
    ),
    SentenceCategoryEntity(
      description = "Sentence Type 2",
      category = SentenceCategoryType.DETERMINATE,
    ),
    SentenceCategoryEntity(
      description = "Sentence Type 3",
      category = SentenceCategoryType.DETERMINATE,
    ),
  )

  @Test
  fun `should correctly set DETERMINATE_AND_INDETERMINATE category sentence information during person create or update`() {
    // Given
    mockGetSentenceInformation()
    mockGetPrisoners()
    whenever(
      sentenceCategoryRepository.findAllByDescriptionIn(
        listOf(
          "Sentence Type 1",
          "Sentence Type 2",
          "Sentence Type 3",
        ),
      ),
    ).thenReturn(determinateAndIndeterminateSentenceCategories())

    // When
    personService.createOrUpdatePerson(PRISON_NUMBER)

    // Then
    verify(personRepository).save(personEntityCaptor.capture())
    val personEntity = personEntityCaptor.value
    personEntity.sentenceType shouldBe "Determinate and Indeterminate"
  }

  @Test
  fun `should correctly set INDETERMINATE category and true recall sentence information during person create or update`() {
    // Given
    mockGetSentenceInformation()
    mockGetPrisoners()
    whenever(
      sentenceCategoryRepository.findAllByDescriptionIn(
        listOf(
          "Sentence Type 1",
          "Sentence Type 2",
          "Sentence Type 3",
        ),
      ),
    ).thenReturn(indeterminateSentenceCategories())

    // When
    personService.createOrUpdatePerson(PRISON_NUMBER)

    // Then
    verify(personRepository).save(personEntityCaptor.capture())
    val personEntity = personEntityCaptor.value
    personEntity.sentenceType shouldBe "Indeterminate"
  }

  @Test
  fun `should correctly set DETERMINATE category and true recall sentence information during person create or update`() {
    // Given
    mockGetSentenceInformation()
    mockGetPrisoners()
    whenever(
      sentenceCategoryRepository.findAllByDescriptionIn(
        listOf(
          "Sentence Type 1",
          "Sentence Type 2",
          "Sentence Type 3",
        ),
      ),
    ).thenReturn(determinateSentenceCategories())

    // When
    personService.createOrUpdatePerson(PRISON_NUMBER)

    // Then
    verify(personRepository).save(personEntityCaptor.capture())
    val personEntity = personEntityCaptor.value
    personEntity.sentenceType shouldBe "Determinate"
  }

  @Test
  fun `should correctly set UNKNOWN category sentence information when no sentence information is available during person create or update`() {
    // Given
    mockGetSentenceInformation()
    mockGetPrisoners()
    whenever(
      sentenceCategoryRepository.findAllByDescriptionIn(
        listOf(
          "Sentence Type 1",
          "Sentence Type 2",
          "Sentence Type 3",
        ),
      ),
    ).thenReturn(emptyList<SentenceCategoryEntity>())

    // When
    personService.createOrUpdatePerson(PRISON_NUMBER)

    // Then
    verify(personRepository).save(personEntityCaptor.capture())
    val personEntity = personEntityCaptor.value
    personEntity.sentenceType shouldBe "Unknown"
  }

  @Test
  fun `should set No active sentences category when no sentence information is available`() {
    // Given
    mockGetPrisoners()
    val sentenceInformation = SentenceInformation(
      prisonerNumber = PRISON_NUMBER,
      latestPrisonTerm = PrisonTerm(
        courtSentences = emptyList(),
        keyDates = createKeyDates(),
      ),
    )
    val response = ClientResult.Success(HttpStatus.OK, sentenceInformation)
    whenever(prisonApiClient.getSentenceInformation(PRISON_NUMBER)).thenReturn(response)

    // When
    personService.createOrUpdatePerson(PRISON_NUMBER)

    // Then
    verify(personRepository).save(personEntityCaptor.capture())
    val personEntity = personEntityCaptor.value
    personEntity.sentenceType shouldBe "No active sentences"
  }

  @Test
  fun `should add sentence information when creating a person`() {
    // Given
    mockGetSentenceInformation()
    mockGetPrisoners()
    whenever(personRepository.findPersonEntityByPrisonNumber(PRISON_NUMBER)).thenReturn(null)
    whenever(
      sentenceCategoryRepository.findAllByDescriptionIn(
        listOf(
          "Sentence Type 1",
          "Sentence Type 2",
          "Sentence Type 3",
        ),
      ),
    ).thenReturn(determinateAndIndeterminateSentenceCategories())

    // When
    personService.createOrUpdatePerson(PRISON_NUMBER)

    // Then
    verify(personRepository).save(personEntityCaptor.capture())
    val personEntity = personEntityCaptor.value
    personEntity.sentenceType shouldBe "Determinate and Indeterminate"
  }

  @Test
  fun `buildKeyDates returns key dates with earliestReleaseDate as expected`() {
    val sentenceInformation = SentenceInformation(
      prisonerNumber = "A1234BC",
      latestPrisonTerm = PrisonTerm(
        courtSentences = emptyList(),
        keyDates = createKeyDates(),
      ),
    )

    val result = personService.buildKeyDates(sentenceInformation)

    val earliest = result.find { it.earliestReleaseDate!! }
    assertEquals("RD", earliest?.code)
  }

  @Test
  fun `buildKeyDates returns empty list when the required sentence date types are missing`() {
    val sentenceInformation = SentenceInformation(
      prisonerNumber = "A1234BC",
      latestPrisonTerm = PrisonTerm(
        courtSentences = emptyList(),
        keyDates = KeyDates(
          sentenceStartDate = LocalDate.now(),
          effectiveSentenceEndDate = null,
          confirmedReleaseDate = null,
          releaseDate = null,
          sentenceExpiryDate = null,
          automaticReleaseDate = null,
          conditionalReleaseDate = null,
          nonParoleDate = null,
          postRecallReleaseDate = null,
          licenceExpiryDate = null,
          homeDetentionCurfewEligibilityDate = null,
          paroleEligibilityDate = null,
          homeDetentionCurfewActualDate = null,
          actualParoleDate = null,
          releaseOnTemporaryLicenceDate = null,
          earlyRemovalSchemeEligibilityDate = null,
          earlyTermDate = null,
          midTermDate = null,
          lateTermDate = null,
          topupSupervisionExpiryDate = null,
          tariffDate = null,
          dtoPostRecallReleaseDate = null,
          tariffEarlyRemovalSchemeEligibilityDate = null,
          topupSupervisionStartDate = null,
          homeDetentionCurfewEndDate = null,
        ),
      ),
    )

    val result = personService.buildKeyDates(sentenceInformation)
    assertTrue { result.isEmpty() }

    val earliest = result.find { it.earliestReleaseDate!! }
    assertNull(earliest?.code)
  }

  @Test
  fun `buildKeyDates ignores earlier key dates when they are not in the relevant dates used specified in the case list`() {
    val sentenceInformation = SentenceInformation(
      prisonerNumber = "A1234BC",
      latestPrisonTerm = PrisonTerm(
        courtSentences = emptyList(),
        keyDates = createKeyDates(
          sentenceStartDate = LocalDate.now().minusDays(10),
          tariffDate = LocalDate.now(),
          conditionalReleaseDate = LocalDate.now().minusDays(1),
          releaseDate = LocalDate.now().plusDays(1),
          paroleEligibilityDate = LocalDate.now().minusDays(4),
          postRecallReleaseDate = LocalDate.now().minusDays(2),
        ),
      ),
    )

    val result = personService.buildKeyDates(sentenceInformation)
    val earliest = result.find { it.earliestReleaseDate!! }
    assertEquals("PED", earliest?.code)
  }

  private fun createKeyDates(
    sentenceStartDate: LocalDate = LocalDate.now(),
    effectiveSentenceEndDate: LocalDate = LocalDate.now().plusDays(1),
    confirmedReleaseDate: LocalDate = LocalDate.now().plusDays(2),
    releaseDate: LocalDate = LocalDate.now().plusDays(3),
    sentenceExpiryDate: LocalDate = LocalDate.now().plusDays(4),
    automaticReleaseDate: LocalDate = LocalDate.now().plusDays(5),
    conditionalReleaseDate: LocalDate = LocalDate.now().plusDays(6),
    nonParoleDate: LocalDate = LocalDate.now().plusDays(7),
    postRecallReleaseDate: LocalDate = LocalDate.now().plusDays(8),
    licenceExpiryDate: LocalDate = LocalDate.now().plusDays(9),
    homeDetentionCurfewEligibilityDate: LocalDate = LocalDate.now().plusDays(10),
    paroleEligibilityDate: LocalDate = LocalDate.now().plusDays(11),
    homeDetentionCurfewActualDate: LocalDate = LocalDate.now().plusDays(12),
    actualParoleDate: LocalDate = LocalDate.now().plusDays(13),
    releaseOnTemporaryLicenceDate: LocalDate = LocalDate.now().plusDays(14),
    earlyRemovalSchemeEligibilityDate: LocalDate = LocalDate.now().plusDays(15),
    earlyTermDate: LocalDate = LocalDate.now().plusDays(16),
    midTermDate: LocalDate = LocalDate.now().plusDays(17),
    lateTermDate: LocalDate = LocalDate.now().plusDays(18),
    topupSupervisionExpiryDate: LocalDate = LocalDate.now().plusDays(19),
    tariffDate: LocalDate = LocalDate.now().plusDays(20),
    dtoPostRecallReleaseDate: LocalDate = LocalDate.now().plusDays(21),
    tariffEarlyRemovalSchemeEligibilityDate: LocalDate = LocalDate.now().plusDays(22),
    topupSupervisionStartDate: LocalDate = LocalDate.now().plusDays(23),
    homeDetentionCurfewEndDate: LocalDate = LocalDate.now().plusDays(24),
  ): KeyDates = KeyDates(
    sentenceStartDate = sentenceStartDate,
    effectiveSentenceEndDate = effectiveSentenceEndDate,
    confirmedReleaseDate = confirmedReleaseDate,
    releaseDate = releaseDate,
    sentenceExpiryDate = sentenceExpiryDate,
    automaticReleaseDate = automaticReleaseDate,
    conditionalReleaseDate = conditionalReleaseDate,
    nonParoleDate = nonParoleDate,
    postRecallReleaseDate = postRecallReleaseDate,
    licenceExpiryDate = licenceExpiryDate,
    homeDetentionCurfewEligibilityDate = homeDetentionCurfewEligibilityDate,
    paroleEligibilityDate = paroleEligibilityDate,
    homeDetentionCurfewActualDate = homeDetentionCurfewActualDate,
    actualParoleDate = actualParoleDate,
    releaseOnTemporaryLicenceDate = releaseOnTemporaryLicenceDate,
    earlyRemovalSchemeEligibilityDate = earlyRemovalSchemeEligibilityDate,
    earlyTermDate = earlyTermDate,
    midTermDate = midTermDate,
    lateTermDate = lateTermDate,
    topupSupervisionExpiryDate = topupSupervisionExpiryDate,
    tariffDate = tariffDate,
    dtoPostRecallReleaseDate = dtoPostRecallReleaseDate,
    tariffEarlyRemovalSchemeEligibilityDate = tariffEarlyRemovalSchemeEligibilityDate,
    topupSupervisionStartDate = topupSupervisionStartDate,
    homeDetentionCurfewEndDate = homeDetentionCurfewEndDate,
  )
}
