package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomSentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.NewPrerequisite
import java.util.UUID

// Refactor CourseEntityFactory
class CourseEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var name: String = randomLowercaseString()
  private var identifier: String = randomUppercaseString()
  private var description: String? = null
  private var alternateName: String? = null
  private var prerequisites: MutableSet<PrerequisiteEntity> = mutableSetOf()
  private var offerings: MutableSet<OfferingEntity> = mutableSetOf()
  private var audience: String = randomUppercaseString()
  private var audienceColour: String = randomUppercaseString()
  private var withdrawn: Boolean = false

  fun withId(id: UUID?) = apply {
    this.id = id
  }

  fun withName(name: String) = apply {
    this.name = name
  }

  fun withIdentifier(identifier: String) = apply {
    this.identifier = identifier
  }

  fun withDescription(description: String?) = apply {
    this.description = description
  }

  fun withAlternateName(alternateName: String?) = apply {
    this.alternateName = alternateName
  }

  fun withPrerequisites(prerequisites: MutableSet<PrerequisiteEntity>) = apply {
    this.prerequisites = prerequisites
  }

  fun withOfferings(offerings: MutableSet<OfferingEntity>) = apply {
    this.offerings = offerings
  }

  fun withWithdrawn(withdrawn: Boolean) = apply {
    this.withdrawn = withdrawn
  }

  fun produce() = CourseEntity(
    id = this.id,
    name = this.name,
    identifier = this.identifier,
    description = this.description,
    alternateName = this.alternateName,
    prerequisites = this.prerequisites,
    offerings = this.offerings,
    audience = this.audience,
    audienceColour = this.audienceColour,
    withdrawn = this.withdrawn,
  )
}

// Refactor CourseUpdateFactory
class CourseUpdateFactory {
  private var name: String = randomLowercaseString()
  private var identifier: String = randomUppercaseString()
  private var description: String = randomSentence()
  private var audience: String = randomUppercaseString()
  private var audienceColour: String = randomUppercaseString()
  private var alternateName: String? = null
  private var referable: Boolean = true

  fun withIdentifier(identifier: String) = apply {
    this.identifier = identifier
  }

  fun withAudience(audience: String) = apply {
    this.audience = audience
  }

  fun withAudienceColour(audienceColour: String) = apply {
    this.audienceColour = audienceColour
  }

  fun produce() = CourseUpdate(
    name = this.name,
    description = this.description,
    identifier = this.identifier,
    audience = this.audience,
    audienceColour = this.audienceColour,
    alternateName = this.alternateName,
  )
}

// Refactor PrerequisiteEntityFactory
class PrerequisiteEntityFactory {
  private var name: String = randomLowercaseString()
  private var description: String = randomSentence()

  fun withName(name: String) = apply {
    this.name = name
  }

  fun withDescription(description: String) = apply {
    this.description = description
  }

  fun produce() = PrerequisiteEntity(
    name = this.name,
    description = this.description,
  )
}

// Refactor NewPrerequisiteFactory
class NewPrerequisiteFactory {
  private var name: String = randomLowercaseString()
  private var description: String? = null
  private var identifier: String = randomUppercaseString()

  fun withName(name: String) = apply {
    this.name = name
  }

  fun withDescription(description: String?) = apply {
    this.description = description
  }

  fun withIdentifier(identifier: String) = apply {
    this.identifier = identifier
  }

  fun produce() = NewPrerequisite(
    name = this.name,
    description = this.description,
    identifier = this.identifier,
  )
}
