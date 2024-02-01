package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "person")
data class PersonEntity(

  @Column
  var surname: String,
  @Column
  var forename: String,
  @Column(unique = true)
  val prisonNumber: String,
  @Column
  var conditionalReleaseDate: LocalDate?,
  @Column
  var paroleEligibilityDate: LocalDate?,
  @Column
  var tariffExpiryDate: LocalDate?,
  @Column
  var earliestReleaseDate: LocalDate?,
  @Column
  var earliestReleaseDateType: String?,
  @Column
  var indeterminateSentence: Boolean?,
  @Column
  var nonDtoReleaseDateType: String?,

  @Id
  @GeneratedValue
  @Column(name = "person_id")
  var id: UUID = UUID.randomUUID(),
)
