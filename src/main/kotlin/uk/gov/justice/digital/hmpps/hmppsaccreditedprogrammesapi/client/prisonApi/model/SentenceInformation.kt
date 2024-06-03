package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class SentenceInformation(
  val prisonerNumber: String,
  val latestPrisonTerm: PrisonTerm,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PrisonTerm(
  val courtSentences: List<CourtSentence>,
  val keyDates: KeyDates,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CourtSentence(
  val caseSeq: Int?,
  val beginDate: LocalDate?,
  val caseStatus: String?,
  val sentences: List<Sentence>,
  val issuingCourtDate: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Sentence(
  val sentenceStatus: String?,
  val sentenceCategory: String?,
  val sentenceCalculationType: String?,
  val sentenceTypeDescription: String?,
  val sentenceStartDate: LocalDate?,
  val lineSeq: Int?,
  val offences: List<Offence>?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeyDates(
  val sentenceStartDate: LocalDate?,
  val effectiveSentenceEndDate: LocalDate?,
  val confirmedReleaseDate: LocalDate?,
  val releaseDate: LocalDate?,
  val sentenceExpiryDate: LocalDate?,
  val automaticReleaseDate: LocalDate?,
  val conditionalReleaseDate: LocalDate?,
  val nonParoleDate: LocalDate?,
  val postRecallReleaseDate: LocalDate?,
  val licenceExpiryDate: LocalDate?,
  val homeDetentionCurfewEligibilityDate: LocalDate?,
  val paroleEligibilityDate: LocalDate?,
  val homeDetentionCurfewActualDate: LocalDate?,
  val actualParoleDate: LocalDate?,
  val releaseOnTemporaryLicenceDate: LocalDate?,
  val earlyRemovalSchemeEligibilityDate: LocalDate?,
  val earlyTermDate: LocalDate?,
  val midTermDate: LocalDate?,
  val lateTermDate: LocalDate?,
  val topupSupervisionExpiryDate: LocalDate?,
  val tariffDate: LocalDate?,
  val dtoPostRecallReleaseDate: LocalDate?,
  val tariffEarlyRemovalSchemeEligibilityDate: LocalDate?,
  val topupSupervisionStartDate: LocalDate?,
  val homeDetentionCurfewEndDate: LocalDate?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Offence(
  val offenceCode: String?,
  val offenceStartDate: LocalDate?,
)
