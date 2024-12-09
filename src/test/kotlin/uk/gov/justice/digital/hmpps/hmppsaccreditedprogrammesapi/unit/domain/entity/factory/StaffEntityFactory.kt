package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AccountType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import java.util.UUID

class StaffEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var staffId: Int = 487505
  private var firstName: String = "John"
  private var lastName: String = "Smith"
  private var primaryEmail: String = "john.smith@digital.justice.gov.uk"
  private var username: String = "JSMITH_ADM"
  private var accountType: AccountType = AccountType.ADMIN

  fun withId(id: UUID?) = apply {
    this.id = id
  }

  fun withStaffId(staffId: Int) = apply {
    this.staffId = staffId
  }

  fun withFirstName(firstName: String) = apply {
    this.firstName = firstName
  }

  fun withLastName(lastName: String) = apply {
    this.lastName = lastName
  }

  fun withPrimaryEmail(primaryEmail: String) = apply {
    this.primaryEmail = primaryEmail
  }

  fun withUsername(username: String) = apply {
    this.username = username
  }

  fun withAccountType(accountType: AccountType) = apply {
    this.accountType = accountType
  }

  fun produce() = StaffEntity(
    id = this.id,
    staffId = this.staffId,
    firstName = this.firstName,
    lastName = this.lastName,
    primaryEmail = this.primaryEmail,
    username = this.username,
    accountType = this.accountType,
  )
}
