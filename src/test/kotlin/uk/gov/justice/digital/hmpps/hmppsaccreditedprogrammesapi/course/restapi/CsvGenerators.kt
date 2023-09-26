package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomSentence

const val COURSES_PREFIX = "name,identifier,description,audience,referable,alternateName,comments\n"
const val PREREQUISITES_PREFIX = "name,description,course,identifier,comments,,,,\n"
const val OFFERINGS_PREFIX = "course,identifier,organisation,contact email,secondary contact email,prisonId\n"

fun generateCourseRecords(count: Int): List<CourseRecord> {
  return (1..count).map {
    CourseRecord(
      name = "CourseName-$it",
      identifier = "Identifier-$it",
      description = "Description for Course-$it",
      audience = "Audience-$it",
      referable = true,
      alternateName = "AltName-$it",
      comments = "Comment-$it",
    )
  }
}

fun generatePrerequisiteRecords(count: Int): List<PrerequisiteRecord> {
  return (1..count).map {
    PrerequisiteRecord(
      name = "PrerequisiteName-$it",
      description = "Description-$it",
      course = "Course-$it",
      identifier = "Identifier-$it",
      comments = "Comments-$it",
    )
  }
}

fun generateOfferingRecords(count: Int): List<OfferingRecord> {
  return (1..count).map {
    OfferingRecord(
      course = "CourseName-$it",
      identifier = "Identifier-$it",
      organisation = "Organisation-$it",
      contactEmail = "email-$it@example.com",
      secondaryContactEmail = "secondaryEmail-$it@example.com",
      prisonId = "PrisonID-$it",
    )
  }
}

fun List<CourseRecord>.toCourseCsv(): String = if (isEmpty()) {
  COURSES_PREFIX
} else {
  joinToString(
    prefix = COURSES_PREFIX,
    separator = "\n",
    transform = { """"${it.name}","${it.identifier}","${it.description}","${it.audience}","${it.referable}","${it.alternateName}",${randomSentence(1..20)}""" },
    postfix = "\n",
  )
}

fun List<PrerequisiteRecord>.toPrerequisiteCsv(): String = if (isEmpty()) {
  PREREQUISITES_PREFIX
} else {
  joinToString(
    prefix = PREREQUISITES_PREFIX,
    separator = "\n",
    transform = { """"${it.name}","${it.description}","${it.course}","${it.identifier}","${it.comments}",,,,""" },
    postfix = "\n",
  )
}

fun List<OfferingRecord>.toOfferingCsv(): String = if (isEmpty()) {
  OFFERINGS_PREFIX
} else {
  joinToString(
    prefix = OFFERINGS_PREFIX,
    separator = "\n",
    transform = { """"${it.course}","${it.identifier}","${it.organisation}","${it.contactEmail}",${it.secondaryContactEmail?.let { secondaryContactEmail -> "\"$secondaryContactEmail\"" } ?: ""},${it.prisonId}""" },
    postfix = "\n",
  )
}

val MEDIA_TYPE_TEXT_CSV: MediaType = MediaType("text", "csv")
