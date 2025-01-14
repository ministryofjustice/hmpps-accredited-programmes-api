package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import jakarta.validation.ValidationException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseParticipationUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.projection.CourseParticipationProjection
import java.time.Year
import java.time.format.DateTimeFormatter
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipation as ApiCourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipationCreate as ApiCourseParticipationCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipationOutcome as ApiCourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipationSetting as ApiCourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipationSettingType as ApiCourseParticipationSettingType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseParticipationUpdate as ApiCourseParticipationUpdate

val referralProgramStartYear: Year = Year.of(1990)

fun ApiCourseParticipationCreate.toDomain() =
  CourseParticipationEntity(
    courseName = courseName,
    prisonNumber = prisonNumber,
    source = source,
    detail = detail,
    setting = setting?.toDomain(),
    outcome = outcome?.toDomain(),
    referralId = referralId,
    isDraft = isDraft,
  )

fun ApiCourseParticipationUpdate.toDomain() = CourseParticipationUpdate(
  courseName = courseName,
  source = source,
  detail = detail,
  setting = setting?.toDomain(),
  outcome = outcome?.toDomain(),
)

fun ApiCourseParticipationSettingType.toDomain() = when (this) {
  ApiCourseParticipationSettingType.COMMUNITY -> CourseSetting.COMMUNITY
  ApiCourseParticipationSettingType.CUSTODY -> CourseSetting.CUSTODY
}

fun ApiCourseParticipationSetting.toDomain() = CourseParticipationSetting(
  type = type.toDomain(),
  location = location,
)

fun ApiCourseParticipationOutcome.toDomain() =
  CourseParticipationOutcome(
    status = status.toDomain(),
    yearStarted = yearStarted?.let(Year::of)?.isValidYear("yearStarted"),
    yearCompleted = yearCompleted?.let(Year::of)?.isValidYear("yearCompleted"),
  )

fun Year.isValidYear(fieldName: String) = run {
  if (this.value < referralProgramStartYear.value) {
    throw ValidationException("$fieldName is not valid.")
  }
  this
}

fun ApiCourseParticipationOutcome.Status.toDomain() = when (this) {
  ApiCourseParticipationOutcome.Status.COMPLETE -> CourseStatus.COMPLETE
  ApiCourseParticipationOutcome.Status.INCOMPLETE -> CourseStatus.INCOMPLETE
}

fun CourseParticipationSetting.toApi() = ApiCourseParticipationSetting(
  type = type.toApi(),
  location = location,
)

fun CourseSetting.toApi() = when (this) {
  CourseSetting.CUSTODY -> ApiCourseParticipationSettingType.CUSTODY
  CourseSetting.COMMUNITY -> ApiCourseParticipationSettingType.COMMUNITY
}

fun CourseStatus.toApi() = when (this) {
  CourseStatus.INCOMPLETE -> ApiCourseParticipationOutcome.Status.INCOMPLETE
  CourseStatus.COMPLETE -> ApiCourseParticipationOutcome.Status.COMPLETE
}

fun CourseParticipationEntity.toApi() = ApiCourseParticipation(
  courseName = courseName,
  id = id!!,
  referralId = referralId,
  prisonNumber = prisonNumber,
  setting = setting?.toApi(),
  source = source,
  detail = detail,
  outcome = outcome?.let {
    ApiCourseParticipationOutcome(
      status = it.status.toApi(),
      yearStarted = it.yearStarted?.value,
      yearCompleted = it.yearCompleted?.value,
    )
  },
  isDraft = isDraft,
  addedBy = createdByUsername,
  createdAt = createdDateTime.format(DateTimeFormatter.ISO_DATE_TIME),
)

fun CourseParticipationProjection.toApi() = ApiCourseParticipation(
  courseName = getCourseName(),
  id = getId(),
  referralId = getReferralId(),
  prisonNumber = getPrisonNumber(),
  setting = ApiCourseParticipationSetting.from(getType(), getLocation()),
  source = getSource(),
  detail = getDetail(),
  outcome = ApiCourseParticipationOutcome.from(getOutcomeStatus(), getYearStarted(), getYearCompleted()),
  isDraft = getIsDraft(),
  addedBy = getAddedBy(),
  createdAt = getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME),
  referralStatus = getReferralStatus(),
)
