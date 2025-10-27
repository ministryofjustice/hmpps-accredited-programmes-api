package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.Offence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.Sentence
import java.time.LocalDate

/**
 * Factory class for creating Sentence objects with suitable default values for testing.
 */
class SentenceFactory {

  private var sentenceStatus: String? = "ACTIVE"
  private var sentenceCategory: String? = "Category A"
  private var sentenceCalculationType: String? = "STANDARD"
  private var sentenceTypeDescription: String? = "Standard Sentence"
  private var sentenceStartDate: LocalDate? = LocalDate.now().minusDays(30)
  private var sentenceEndDate: LocalDate? = LocalDate.now().plusDays(365)
  private var lineSeq: Int? = 1
  private var offences: List<Offence>? = emptyList()

  fun withSentenceStatus(status: String?) = apply { this.sentenceStatus = status }

  fun withSentenceCategory(category: String?) = apply { this.sentenceCategory = category }

  fun withSentenceCalculationType(calculationType: String?) = apply { this.sentenceCalculationType = calculationType }

  fun withSentenceTypeDescription(typeDescription: String?) = apply { this.sentenceTypeDescription = typeDescription }

  fun withSentenceStartDate(startDate: LocalDate?) = apply { this.sentenceStartDate = startDate }

  fun withSentenceEndDate(endDate: LocalDate?) = apply { this.sentenceEndDate = endDate }

  fun withLineSeq(lineSeq: Int?) = apply { this.lineSeq = lineSeq }

  fun withOffences(offences: List<Offence>?) = apply { this.offences = offences }

  fun withOffences(vararg offences: Offence) = apply { this.offences = offences.toList() }

  fun build(): Sentence = Sentence(
    sentenceStatus = sentenceStatus,
    sentenceCategory = sentenceCategory,
    sentenceCalculationType = sentenceCalculationType,
    sentenceTypeDescription = sentenceTypeDescription,
    sentenceStartDate = sentenceStartDate,
    sentenceEndDate = sentenceEndDate,
    lineSeq = lineSeq,
    offences = offences,
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

  fun createOffence(
    offenceCode: String? = "THEFT001",
    offenceStartDate: LocalDate? = LocalDate.now().minusDays(45),
  ): Offence = Offence(
    offenceCode = offenceCode,
    offenceStartDate = offenceStartDate,
  )

  fun createActiveSentence(
    typeDescription: String = "Standard Sentence",
  ): Sentence = createSentence(
    sentenceStatus = "ACTIVE",
    sentenceTypeDescription = typeDescription,
  )

  fun createCompletedSentence(
    typeDescription: String = "Standard Sentence",
  ): Sentence = createSentence(
    sentenceStatus = "COMPLETED",
    sentenceTypeDescription = typeDescription,
    sentenceEndDate = LocalDate.now().minusDays(10),
  )

  fun createSentenceWithOffences(
    typeDescription: String = "Standard Sentence",
    vararg offenceCodes: String,
  ): Sentence {
    val offences = offenceCodes.map { code ->
      createOffence(offenceCode = code)
    }
    return createSentence(
      sentenceTypeDescription = typeDescription,
      offences = offences,
    )
  }

  fun createIndeterminateSentence(
    typeDescription: String = "Life Sentence",
  ): Sentence = createSentence(
    sentenceTypeDescription = typeDescription,
    sentenceCalculationType = "INDETERMINATE",
    sentenceEndDate = null,
  )

  fun createDeterminateSentence(
    typeDescription: String = "Fixed Term",
    years: Long = 2,
  ): Sentence {
    val startDate = LocalDate.now().minusDays(30)
    val endDate = startDate.plusYears(years)
    return createSentence(
      sentenceTypeDescription = typeDescription,
      sentenceCalculationType = "DETERMINATE",
      sentenceStartDate = startDate,
      sentenceEndDate = endDate,
    )
  }
}
