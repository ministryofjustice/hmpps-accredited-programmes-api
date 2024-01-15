package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomSentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.NewPrerequisite
import java.util.UUID

class CourseEntityFactory : Factory<CourseEntity> {
  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var name: Yielded<String> = { randomLowercaseString() }
  private var identifier: Yielded<String> = { randomUppercaseString() }
  private var description: Yielded<String?> = { null }
  private var alternateName: Yielded<String?> = { null }
  private var referable: Yielded<Boolean> = { true }
  private var prerequisites: Yielded<MutableSet<PrerequisiteEntity>> = { mutableSetOf() }
  private var offerings: Yielded<MutableSet<OfferingEntity>> = { mutableSetOf() }
  private var audiences: Yielded<MutableSet<AudienceEntity>> = { mutableSetOf() }
  private var withdrawn: Yielded<Boolean> = { false }

  fun withId(id: UUID?) = apply {
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

  fun withPrerequisites(prerequisites: MutableSet<PrerequisiteEntity>) = apply {
    this.prerequisites = { prerequisites }
  }

  fun withOfferings(offerings: MutableSet<OfferingEntity>) = apply {
    this.offerings = { offerings }
  }

  fun withAudiences(audienceEntities: MutableSet<AudienceEntity>) = apply {
    this.audiences = { audienceEntities }
  }

  fun withWithdrawn(withdrawn: Boolean) = apply {
    this.withdrawn = { withdrawn }
  }

  override fun produce() = CourseEntity(
    id = this.id(),
    name = this.name(),
    identifier = this.identifier(),
    description = this.description(),
    alternateName = this.alternateName(),
    referable = this.referable(),
    prerequisites = this.prerequisites(),
    offerings = this.offerings(),
    audiences = this.audiences(),
    withdrawn = this.withdrawn(),
  )
}

class CourseUpdateFactory : Factory<CourseUpdate> {
  private var name: Yielded<String> = { randomLowercaseString() }
  private var identifier: Yielded<String> = { randomUppercaseString() }
  private var description: Yielded<String> = { randomSentence() }
  private var audience: Yielded<String> = { randomUppercaseString() }
  private var alternateName: Yielded<String?> = { null }
  private var referable: Yielded<Boolean> = { true }

  fun withIdentifier(identifier: String) = apply {
    this.identifier = { identifier }
  }

  fun withAudience(audience: String) = apply {
    this.audience = { audience }
  }

  override fun produce() = CourseUpdate(
    name = this.name(),
    description = this.description(),
    identifier = this.identifier(),
    audience = this.audience(),
    alternateName = this.alternateName(),
    referable = this.referable(),
  )
}

class PrerequisiteEntityFactory : Factory<PrerequisiteEntity> {
  private var name: Yielded<String> = { randomLowercaseString() }
  private var description: Yielded<String> = { randomSentence() }

  fun withName(name: String) = apply {
    this.name = { name }
  }

  fun withDescription(description: String) = apply {
    this.description = { description }
  }

  override fun produce() = PrerequisiteEntity(
    name = this.name(),
    description = this.description(),
  )
}

class NewPrerequisiteFactory : Factory<NewPrerequisite> {
  private var name: Yielded<String> = { randomLowercaseString() }
  private var description: Yielded<String?> = { null }
  private var identifier: Yielded<String> = { randomUppercaseString() }

  fun withName(name: String) = apply {
    this.name = { name }
  }

  fun withDescription(description: String?) = apply {
    this.description = { description }
  }

  fun withIdentifier(identifier: String) = apply {
    this.identifier = { identifier }
  }

  override fun produce() = NewPrerequisite(
    name = this.name(),
    description = this.description(),
    identifier = this.identifier(),
  )
}
