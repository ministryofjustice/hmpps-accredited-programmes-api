package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigInteger
import java.util.UUID

@Entity
@Table(name = "staff")
data class StaffEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @Column(name = "staffId")
  var staffId: BigInteger? = null,

  @Column(name = "first_name", nullable = false)
  var firstName: String,

  @Column(name = "last_name", nullable = false)
  var lastName: String,

  @Column(name = "primary_email", nullable = false)
  var primaryEmail: String,

  @Column(name = "username", nullable = false)
  var username: String,

  @Column(name = "type", nullable = false)
  var pomType: PomType,

  @Column(name = "accountType", nullable = true)
  var accountType: AccountType,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "referral_id", referencedColumnName = "referral_id")
  var referral: ReferralEntity,
)

enum class PomType {
  PRIMARY,
  SECONDARY,
}

enum class AccountType {
  GENERAL,
  ADMIN,
}
