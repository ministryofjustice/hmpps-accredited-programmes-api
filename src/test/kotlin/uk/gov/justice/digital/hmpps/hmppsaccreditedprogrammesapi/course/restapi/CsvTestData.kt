package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.LoremIpsum
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseUpdate
import java.io.ByteArrayInputStream
import java.io.InputStream

private fun newCourse(name: String, identifier: String = "", audience: String, alternateName: String? = null) =
  CourseUpdate(name = name, identifier = identifier, alternateName = alternateName, audience = audience, description = LoremIpsum.words(1..10))

object CsvTestData {
  val newCourses: List<CourseUpdate> = listOf(
    newCourse(name = "Becoming New Me Plus", identifier = "BNM-SO", audience = "Sexual offence", alternateName = "BNM+"),
    newCourse(name = "Becoming New Me Plus", identifier = "BNM-IPVO", audience = "Intimate partner violence offence", alternateName = "BNM+"),
    newCourse(name = "Becoming New Me Plus", identifier = "BNM-VO", audience = "General violence offence", alternateName = "BNM+"),
    newCourse(name = "Building Better Relationships", identifier = "BBR-IPVO", audience = "Intimate partner violence offence", alternateName = "BBR"),
  )

  private fun newPrerequisiteRecord(name: String, description: String, identifier: String) = PrerequisiteRecord(
    name = name,
    description = description,
    identifier = identifier,
    course = LoremIpsum.words(1..3),
    comments = LoremIpsum.words(0..10),
  )

  val prerequisiteRecords: List<PrerequisiteRecord> = listOf(
    newPrerequisiteRecord(name = "gender", description = "Male", identifier = "BNM-SO, BNM-IPVO, BNM-VO"),
    newPrerequisiteRecord(name = "age", description = "18+", identifier = "BNM-SO, BNM-IPVO, BNM-VO"),
    newPrerequisiteRecord(name = "risk score", description = "High ESARA", identifier = "BNM-SO, BNM-IPVO, BNM-VO"),
    newPrerequisiteRecord(name = "risk score", description = "Very high/high OSP", identifier = "BNM-SO, BNM-IPVO, BNM-VO"),
    newPrerequisiteRecord(name = "setting", description = "Custody", identifier = "BNM-SO, BNM-IPVO, BNM-VO"),
    newPrerequisiteRecord(name = "responsivity requirements", description = "OASys Learning Screening Tool", identifier = "BNM-SO, BNM-IPVO, BNM-VO"),
    newPrerequisiteRecord(name = "need requirements", description = "High need on PNA in Attitudes, Relationships, and Self-management", identifier = "BNM-SO, BNM-IPVO, BNM-VO"),
    newPrerequisiteRecord(name = "criminogenic needs", description = "Relationships", identifier = "BNM-SO, BNM-IPVO, BNM-VO"),
    newPrerequisiteRecord(name = "criminogenic needs", description = "Attitudes", identifier = "BNM-SO, BNM-IPVO, BNM-VO"),
    newPrerequisiteRecord(name = "gender", description = "male", identifier = "BBR-IPVO"),
    newPrerequisiteRecord(name = "age", description = "18+", identifier = "BBR-IPVO"),
    newPrerequisiteRecord(name = "setting", description = "Custody", identifier = "BBR-IPVO"),
    newPrerequisiteRecord(name = "responsivity requirements", description = "Offences which arise out of intimate partner conflict against a non-partner", identifier = "BBR-IPVO"),
    newPrerequisiteRecord(name = "need requirements", description = "Relationship problems", identifier = "BBR-IPVO"),
    newPrerequisiteRecord(name = "need requirements", description = "Social skills deficits", identifier = "BBR-IPVO"),
    newPrerequisiteRecord(name = "criminogenic needs", description = "Relationships", identifier = "BBR-IPVO"),
    newPrerequisiteRecord(name = "criminogenic needs", description = "Drug misuse", identifier = "BBR-IPVO"),
  )

  val offeringsRecords: List<OfferingRecord> = listOf(
    OfferingRecord(course = "Becoming New Me Plus", identifier = "BNM-IPVO", organisation = "HMP Aylesbury", prisonId = "AYI"),
    OfferingRecord(course = "Becoming New Me Plus", identifier = "BNM-VO", organisation = "HMP Aylesbury", secondaryContactEmail = "test@second.com", prisonId = "AYI"),
    OfferingRecord(course = "Becoming New Me Plus", identifier = "BNM-IPVO", organisation = "HMP Brinsford", prisonId = "BSI"),
    OfferingRecord(course = "Becoming New Me Plus", identifier = "BNM-SO", organisation = "HMP Bure", prisonId = "BRI"),
    OfferingRecord(course = "Building Better Relationships", identifier = "BBR-IPVO", organisation = "HMP Dovegate", secondaryContactEmail = "test2@second.com", prisonId = "DGI"),
    OfferingRecord(course = "Building Better Relationships", identifier = "BBR-IPVO", organisation = "HMP Elmley", prisonId = "EYI"),
  ).map { it.copy(contactEmail = "${LoremIpsum.words(1..1)}@${LoremIpsum.words(1..1)}.com") }

  fun coursesCsvInputStream(): InputStream = ByteArrayInputStream(coursesCsvText.toByteArray())
  val coursesCsvText: String =
    newCourses
      .joinToString(
        prefix = COURSES_PREFIX,
        separator = "\n",
        transform = { """"${it.name}","${it.identifier}","${it.description}","${it.audience}","${it.alternateName}",${LoremIpsum.words(1..20)}""" },
        postfix = "\n",
      )

  val prerequisitesCsvText: String by lazy {
    prerequisiteRecords
      .joinToString(
        prefix = "name,description,course,identifier,comments,,,,\n",
        separator = "\n",
        transform = { """"${it.name}","${it.description}","${it.course}","${it.identifier}","${it.comments}",,,,""" },
        postfix = "\n",
      )
  }

  val offeringsCsvText: String by lazy {
    offeringsRecords
      .joinToString(
        prefix = OFFERINGS_PREFIX,
        separator = "\n",
        transform = { """"${it.course}","${it.identifier}","${it.organisation}","${it.contactEmail}",${asQuotedStringIfNotNull(it.secondaryContactEmail)},${it.prisonId}""" },
        postfix = "\n",
      )
  }

  val emptyCoursesCsvText: String = COURSES_PREFIX
  val emptyOfferingsCsvText: String = OFFERINGS_PREFIX

  private fun asQuotedStringIfNotNull(stringOrNull: String?) = stringOrNull?.let { "\"$it\"" } ?: ""
}

private const val COURSES_PREFIX = "name,identifier,description,audience,alternateName,comments\n"
private const val OFFERINGS_PREFIX = "course,identifier,organisation,contact email,secondary contact email,prisonId\n"
