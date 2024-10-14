package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.CourtSentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.KeyDates
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.PrisonTerm
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.Sentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.SentenceInformation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SentenceCategoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SentenceCategoryType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.SentenceCategoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PersonEntityFactory
import java.time.LocalDate

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

  @BeforeEach
  fun setup() {
    val sentenceInformation = SentenceInformation(
      prisonerNumber = PRISON_NUMBER,
      latestPrisonTerm = PrisonTerm(
        courtSentences = listOf(
          CourtSentence(
            caseSeq = 3,
            beginDate = LocalDate.now(),
            caseStatus = "status",
            sentences = listOf(
              Sentence(
                sentenceTypeDescription = "Sentence Type 1",
                sentenceStartDate = LocalDate.now().minusDays(1),
                sentenceStatus = "status",
                sentenceCategory = "Category",
                sentenceCalculationType = "calculationType",
                lineSeq = 34,
                offences = emptyList(),
              ),
              Sentence(
                sentenceTypeDescription = "Sentence Type 2",
                sentenceStartDate = LocalDate.now().minusDays(1),
                sentenceStatus = "status",
                sentenceCategory = "Category",
                sentenceCalculationType = "calculationType",
                lineSeq = 34,
                offences = emptyList(),
              ),
              Sentence(
                sentenceTypeDescription = "Sentence Type 3",
                sentenceStartDate = LocalDate.now().minusDays(1),
                sentenceStatus = "status",
                sentenceCategory = "Category",
                sentenceCalculationType = "calculationType",
                lineSeq = 34,
                offences = emptyList(),
              ),
            ),
            issuingCourtDate = "firstIssuingCourtDate",
          ),
        ),
        keyDates = KeyDates(
          sentenceStartDate = LocalDate.now(),
          effectiveSentenceEndDate = LocalDate.now().plusDays(1),
          confirmedReleaseDate = LocalDate.now().plusDays(2),
          releaseDate = LocalDate.now().plusDays(3),
          sentenceExpiryDate = LocalDate.now().plusDays(4),
          automaticReleaseDate = LocalDate.now().plusDays(5),
          conditionalReleaseDate = LocalDate.now().plusDays(6),
          nonParoleDate = LocalDate.now().plusDays(7),
          postRecallReleaseDate = LocalDate.now().plusDays(8),
          licenceExpiryDate = LocalDate.now().plusDays(9),
          homeDetentionCurfewEligibilityDate = LocalDate.now().plusDays(10),
          paroleEligibilityDate = LocalDate.now().plusDays(11),
          homeDetentionCurfewActualDate = LocalDate.now().plusDays(12),
          actualParoleDate = LocalDate.now().plusDays(13),
          releaseOnTemporaryLicenceDate = LocalDate.now().plusDays(14),
          earlyRemovalSchemeEligibilityDate = LocalDate.now().plusDays(15),
          earlyTermDate = LocalDate.now().plusDays(16),
          midTermDate = LocalDate.now().plusDays(17),
          lateTermDate = LocalDate.now().plusDays(18),
          topupSupervisionExpiryDate = LocalDate.now().plusDays(19),
          tariffDate = LocalDate.now().plusDays(20),
          dtoPostRecallReleaseDate = LocalDate.now().plusDays(21),
          tariffEarlyRemovalSchemeEligibilityDate = LocalDate.now().plusDays(22),
          topupSupervisionStartDate = LocalDate.now().plusDays(23),
          homeDetentionCurfewEndDate = LocalDate.now().plusDays(24),
        ),
      ),
    )
    val response = ClientResult.Success(HttpStatus.OK, sentenceInformation)
    whenever(prisonApiClient.getSentenceInformation(PRISON_NUMBER)).thenReturn(response)

    val prisoner = Prisoner(
      prisonerNumber = PRISON_NUMBER,
      prisonName = "Prison 1",
      firstName = "John",
      lastName = "Doe",
      tariffDate = LocalDate.of(1985, 1, 1),
      gender = "Male",
    )

    whenever(peopleSearchApiService.getPrisoners(listOf(PRISON_NUMBER))).thenReturn(listOf(prisoner))

    whenever(personRepository.findPersonEntityByPrisonNumber(PRISON_NUMBER))
      .thenReturn(PersonEntityFactory().withPrisonNumber(PRISON_NUMBER).produce())
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
  fun `should add sentence information when creating a person`() {
    // Given
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
}
