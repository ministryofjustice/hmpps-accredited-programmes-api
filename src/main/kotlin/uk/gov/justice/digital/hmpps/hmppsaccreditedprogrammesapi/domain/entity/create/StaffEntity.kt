package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "staff")
data class StaffEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @Column(name = "staff_id")
  var staffId: Int? = null,

  @Column(name = "first_name", nullable = false)
  var firstName: String,

  @Column(name = "last_name", nullable = false)
  var lastName: String,

  @Column(name = "primary_email", nullable = false)
  var primaryEmail: String,

  @Column(name = "username", nullable = false)
  var username: String,

  @Column(name = "accountType", nullable = true)
  var accountType: String,
)

enum class AccountType {
  GENERAL,
  ADMIN,
}