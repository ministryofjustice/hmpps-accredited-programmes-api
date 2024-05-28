package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "audit_record")
class AuditEntity(

  @Id
  @Column(name = "audit_record_id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: UUID? = null,
  var referralId: UUID? = null,
  var prisonNumber: String,
  var referrerUsername: String? = null,
  var referralStatusFrom: String? = null,
  var referralStatusTo: String? = null,
  var courseId: UUID? = null,
  var courseName: String? = null,
  var courseLocation: String? = null,
  var auditAction: String,
  var auditUsername: String,
  var auditDateTime: LocalDateTime = LocalDateTime.now(),
)

enum class AuditAction {
  CREATE_REFERRAL,
  VIEW_REFERRAL,
  UPDATE_REFERRAL,
  DELETE_REFERRAL,
  NOMIS_SEARCH_FOR_PERSON,
  OASYS_SEARCH_FOR_PERSON_OFFENCE_DETAIL,
  OASYS_SEARCH_FOR_PERSON_RELATIONSHIP,
  OASYS_SEARCH_FOR_PERSON_ROSH,
  OASYS_SEARCH_FOR_PERSON_LIFESTYLE,
  OASYS_SEARCH_FOR_PERSON_BEHAVIOUR,
  OASYS_SEARCH_FOR_PERSON_HEALTH,
  OASYS_SEARCH_FOR_PERSON_ATTITUDE,
  OASYS_SEARCH_FOR_PERSON_PSYCHIATRIC,
  OASYS_SEARCH_FOR_PERSON_LEARNING,
  OASYS_SEARCH_FOR_PERSON_RISKS,
  OASYS_SEARCH_FOR_PERSON_DRUG_ALCOHOL,
}
