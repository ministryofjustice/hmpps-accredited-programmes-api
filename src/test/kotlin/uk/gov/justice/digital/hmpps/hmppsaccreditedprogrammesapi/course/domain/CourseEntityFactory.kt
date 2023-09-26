package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomStringLowerCase
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomStringUpperCase
import java.util.UUID

class CourseEntityFactory : Factory<CourseEntity> {
  private var id: Yielded<UUID> = { UUID.randomUUID() }
  private var name: Yielded<String> = { randomStringLowerCase(6) }
  private var identifier: Yielded<String> = { randomStringUpperCase(10) }
  private var description: Yielded<String?> = { null }
  private var alternateName: Yielded<String?> = { null }
  private var referable: Yielded<Boolean> = { true }
  private var prerequisites: Yielded<MutableSet<Prerequisite>> = { mutableSetOf() }
  private var mutableOfferings: Yielded<MutableSet<Offering>> = { mutableSetOf() }
  private var audiences: Yielded<MutableSet<Audience>> = { mutableSetOf() }

  fun withId(id: UUID) = apply {
    this.id = { id }
  }

  fun withName(name: String) = apply {
    this.name = { name }
  }

  fun withIdentifier(identifier: String) = apply {
    this.identifier = { identifier }
  }

  fun withDescription(description: String?) = apply {
    this.description = { description }
  }

  fun withAlternateName(alternateName: String?) = apply {
    this.alternateName = { alternateName }
  }

  fun withReferable(referable: Boolean) = apply {
    this.referable = { referable }
  }

  fun withPrerequisites(prerequisites: MutableSet<Prerequisite>) = apply {
    this.prerequisites = { prerequisites }
  }

  fun withMutableOfferings(mutableOfferingEntities: MutableSet<Offering>) = apply {
    this.mutableOfferings = { mutableOfferingEntities }
  }

  fun withAudiences(audienceEntities: MutableSet<Audience>) = apply {
    this.audiences = { audienceEntities }
  }

  override fun produce(): CourseEntity = CourseEntity(
    id = this.id(),
    name = this.name(),
    identifier = this.identifier(),
    description = this.description(),
    alternateName = this.alternateName(),
    referable = this.referable(),
    prerequisites = this.prerequisites(),
    mutableOfferings = this.mutableOfferings(),
    audiences = this.audiences(),
  )
}