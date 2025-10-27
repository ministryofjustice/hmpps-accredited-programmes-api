package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.CourtSentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.KeyDates
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.Offence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.PrisonTerm
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.Sentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.SentenceInformation
import java.time.LocalDate

/**
 * Factory class for creating SentenceInformation objects with suitable default values for testing.
 */
class SentenceInformationFactory {

  private var prisonerNumber: String = "A1234BC"
  private var sentenceTypes: List<String> = listOf("Standard Sentence")
  private var sentenceStartDate: LocalDate? = LocalDate.now().minusDays(30)
  private var sentenceEndDate: LocalDate? = LocalDate.now().plusDays(365)
  private var releaseDate: LocalDate? = LocalDate.now().plusDays(180)
  private var tariffDate: LocalDate? = null
  private var paroleEligibilityDate: LocalDate? = LocalDate.now().plusDays(90)
  private var conditionalReleaseDate: LocalDate? = LocalDate.now().plusDays(183)
  private var caseStatus: String? = "ACTIVE"
  private var sentenceStatus: String? = "ACTIVE"

  fun withPrisonerNumber(prisonerNumber: String) = apply { this.prisonerNumber = prisonerNumber }

  fun withSentenceTypes(vararg types: String) = apply { this.sentenceTypes = types.toList() }

  fun withSentenceTypes(types: List<String>) = apply { this.sentenceTypes = types }

  fun withSentenceStartDate(date: LocalDate?) = apply { this.sentenceStartDate = date }

  fun withSentenceEndDate(date: LocalDate?) = apply { this.sentenceEndDate = date }

  fun withReleaseDate(date: LocalDate?) = apply { this.releaseDate = date }

  fun withTariffDate(date: LocalDate?) = apply { this.tariffDate = date }

  fun withParoleEligibilityDate(date: LocalDate?) = apply { this.paroleEligibilityDate = date }

  fun withConditionalReleaseDate(date: LocalDate?) = apply { this.conditionalReleaseDate = date }

  fun withCaseStatus(status: String?) = apply { this.caseStatus = status }

  fun withSentenceStatus(status: String?) = apply { this.sentenceStatus = status }

  fun produce(): SentenceInformation {
    val sentences = sentenceTypes.map { type ->
      createSentence(
        sentenceTypeDescription = type,
        sentenceStartDate = sentenceStartDate,
        sentenceEndDate = sentenceEndDate,
        sentenceStatus = sentenceStatus,
      )
    }
    val courtSentence = createCourtSentence(
      sentences = sentences,
      caseStatus = caseStatus,
    )
    val keyDates = createKeyDates(
      sentenceStartDate = sentenceStartDate,
      releaseDate = releaseDate,
      tariffDate = tariffDate,
      paroleEligibilityDate = paroleEligibilityDate,
      conditionalReleaseDate = conditionalReleaseDate,
    )
    val prisonTerm = createPrisonTerm(
      courtSentences = listOf(courtSentence),
      keyDates = keyDates,
    )
    return createSentenceInformation(
      prisonerNumber = prisonerNumber,
      latestPrisonTerm = prisonTerm,
    )
  }

  fun createSentenceInformation(
    prisonerNumber: String = "A1234BC",
    latestPrisonTerm: PrisonTerm = createPrisonTerm(),
  ): SentenceInformation = SentenceInformation(
    prisonerNumber = prisonerNumber,
    latestPrisonTerm = latestPrisonTerm,
  )

  fun createPrisonTerm(
    courtSentences: List<CourtSentence> = listOf(createCourtSentence()),
    keyDates: KeyDates = createKeyDates(),
  ): PrisonTerm = PrisonTerm(
    courtSentences = courtSentences,
    keyDates = keyDates,
  )

  fun createCourtSentence(
    caseSeq: Int? = 1,
    beginDate: LocalDate? = LocalDate.now().minusDays(30),
    caseStatus: String? = "ACTIVE",
    sentences: List<Sentence> = listOf(createSentence()),
    issuingCourtDate: String? = "2023-01-15",
  ): CourtSentence = CourtSentence(
    caseSeq = caseSeq,
    beginDate = beginDate,
    caseStatus = caseStatus,
    sentences = sentences,
    issuingCourtDate = issuingCourtDate,
  )

  fun createSentence(
    sentenceStatus: String? = "ACTIVE",
    sentenceCategory: String? = "Category A",
    sentenceCalculationType: String? = "STANDARD",
    sentenceTypeDescription: String? = "Standard Sentence",
    sentenceStartDate: LocalDate? = LocalDate.now().minusDays(30),
    sentenceEndDate: LocalDate? = LocalDate.now().plusDays(365),
    lineSeq: Int? = 1,
    offences: List<Offence>? = emptyList(),
  ): Sentence = Sentence(
    sentenceStatus = sentenceStatus,
    sentenceCategory = sentenceCategory,
    sentenceCalculationType = sentenceCalculationType,
    sentenceTypeDescription = sentenceTypeDescription,
    sentenceStartDate = sentenceStartDate,
    sentenceEndDate = sentenceEndDate,
    lineSeq = lineSeq,
    offences = offences,
  )

  fun createKeyDates(
    sentenceStartDate: LocalDate? = LocalDate.now().minusDays(30),
    effectiveSentenceEndDate: LocalDate? = LocalDate.now().plusDays(365),
    confirmedReleaseDate: LocalDate? = LocalDate.now().plusDays(180),
    releaseDate: LocalDate? = LocalDate.now().plusDays(180),
    sentenceExpiryDate: LocalDate? = LocalDate.now().plusDays(365),
    automaticReleaseDate: LocalDate? = LocalDate.now().plusDays(182),
    conditionalReleaseDate: LocalDate? = LocalDate.now().plusDays(183),
    nonParoleDate: LocalDate? = null,
    postRecallReleaseDate: LocalDate? = null,
    licenceExpiryDate: LocalDate? = LocalDate.now().plusDays(395),
    homeDetentionCurfewEligibilityDate: LocalDate? = null,
    paroleEligibilityDate: LocalDate? = LocalDate.now().plusDays(90),
    homeDetentionCurfewActualDate: LocalDate? = null,
    actualParoleDate: LocalDate? = null,
    releaseOnTemporaryLicenceDate: LocalDate? = null,
    earlyRemovalSchemeEligibilityDate: LocalDate? = null,
    earlyTermDate: LocalDate? = null,
    midTermDate: LocalDate? = null,
    lateTermDate: LocalDate? = null,
    topupSupervisionExpiryDate: LocalDate? = null,
    tariffDate: LocalDate? = null,
    dtoPostRecallReleaseDate: LocalDate? = null,
    tariffEarlyRemovalSchemeEligibilityDate: LocalDate? = null,
    topupSupervisionStartDate: LocalDate? = null,
    homeDetentionCurfewEndDate: LocalDate? = null,
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

  fun createOffence(
    offenceCode: String? = "THEFT001",
    offenceStartDate: LocalDate? = LocalDate.now().minusDays(45),
  ): Offence = Offence(
    offenceCode = offenceCode,
    offenceStartDate = offenceStartDate,
  )

  // Convenience methods for common test scenarios
  fun createSentenceInformationWithMultipleSentences(
    prisonerNumber: String = "A1234BC",
    sentenceTypes: List<String> = listOf("Sentence Type 1", "Sentence Type 2", "Sentence Type 3"),
  ): SentenceInformation {
    val sentences = sentenceTypes.map { type ->
      createSentence(sentenceTypeDescription = type)
    }
    val courtSentence = createCourtSentence(sentences = sentences)
    val prisonTerm = createPrisonTerm(courtSentences = listOf(courtSentence))
    return createSentenceInformation(prisonerNumber = prisonerNumber, latestPrisonTerm = prisonTerm)
  }

  fun createSentenceInformationWithEmptySentences(
    prisonerNumber: String = "A1234BC",
  ): SentenceInformation {
    val courtSentence = createCourtSentence(sentences = emptyList())
    val prisonTerm = createPrisonTerm(courtSentences = listOf(courtSentence))
    return createSentenceInformation(prisonerNumber = prisonerNumber, latestPrisonTerm = prisonTerm)
  }

  fun createSentenceInformationWithNullKeyDates(
    prisonerNumber: String = "A1234BC",
  ): SentenceInformation {
    val keyDates = createKeyDates(
      sentenceStartDate = LocalDate.now(),
      effectiveSentenceEndDate = null,
      confirmedReleaseDate = null,
      releaseDate = null,
      sentenceExpiryDate = null,
      automaticReleaseDate = null,
      conditionalReleaseDate = null,
      paroleEligibilityDate = null,
    )
    val prisonTerm = createPrisonTerm(keyDates = keyDates)
    return createSentenceInformation(prisonerNumber = prisonerNumber, latestPrisonTerm = prisonTerm)
  }
}
