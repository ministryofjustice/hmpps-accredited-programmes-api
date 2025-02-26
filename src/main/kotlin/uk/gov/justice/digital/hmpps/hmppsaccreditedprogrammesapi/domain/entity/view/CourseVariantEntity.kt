package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "course_variant")
class CourseVariantEntity(

  @Id
  val id: UUID? = null,

  @Column
  val courseId: UUID,

  @Column
  val variantCourseId: UUID,
)
