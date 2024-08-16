package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "person")
data class PersonEntity(
  var surname: String,
  var forename: String,
  @Column(unique = true)
  val prisonNumber: String,
  var conditionalReleaseDate: LocalDate?,
  var paroleEligibilityDate: LocalDate?,
  var tariffExpiryDate: LocalDate?,
  var earliestReleaseDate: LocalDate?,
  var earliestReleaseDateType: String?,
  var indeterminateSentence: Boolean?,
  var nonDtoReleaseDateType: String?,
  var sentenceType: String?,
  @Id
  @GeneratedValue
  @Column(name = "person_id")
  var id: UUID = UUID.randomUUID(),

  @Version
  @Column(name = "version", nullable = false)
  val version: Long = 0,

  var location: String?,
  var gender: String?,
)
