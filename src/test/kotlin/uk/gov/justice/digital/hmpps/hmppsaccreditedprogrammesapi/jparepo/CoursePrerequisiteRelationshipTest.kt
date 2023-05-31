package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContainAllIgnoringFields
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite

class CoursePrerequisiteRelationshipTest
@Autowired constructor(
  val coursesRepo: CourseEntityRepository,
  val prerequisiteRepo: PrerequisiteRepository,
  entityManager: EntityManager,
) : RepositoryTest(entityManager) {
  @Test
  fun `persists transient Prerequisites from Course prerequisites collection`() {
    prerequisiteRepo.count() shouldBe 0

    val course = CourseEntity(
      name = "A Course",
      type = "Approved Programme",
      description = "A representative Approved Programme for testing",
      prerequisites = mutableSetOf(
        Prerequisite(name = "PR1", description = "PR1 Description"),
        Prerequisite(name = "PR2", description = "PR2 Description"),
      ),
    )
    coursesRepo.save(course)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val persistentCourse = coursesRepo.findById(course.id!!).get()

    val prerequisites = persistentCourse.prerequisites
    prerequisites shouldHaveSize 2
    prerequisites.shouldContainAllIgnoringFields(course.prerequisites, Prerequisite::id)
    prerequisites.forAll { it.id.shouldNotBeNull() }

    coursesRepo.deleteAll()

    prerequisiteRepo.count() shouldBe 2
  }

  @Test
  fun `shares prerequisites across courses`() {
    val pr1 = Prerequisite(name = "PR1", description = "PR1 Description")
    val pr2 = Prerequisite(name = "PR2", description = "PR2 Description")
    val pr3 = Prerequisite(name = "PR3", description = "PR3 Description")

    val course1 = CourseEntity(
      name = "C1",
      type = "Approved Programme",
      description = "C1 Desc",
      prerequisites = mutableSetOf(pr1, pr2),
    )

    val course2 = CourseEntity(
      name = "C2",
      type = "Approved Programme",
      description = "C2 Desc",
      prerequisites = mutableSetOf(pr2, pr3),
    )

    coursesRepo.save(course1)
    coursesRepo.save(course2)
    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val courses = coursesRepo.findAll()

    courses.shouldHaveSize(2)
    val allPrerequisites = courses.flatMap { it.prerequisites }
    allPrerequisites shouldHaveSize 4
    allPrerequisites.toSet() shouldHaveSize 3

    prerequisiteRepo.count() shouldBe 3

    TestTransaction.flagForCommit()
    TestTransaction.end()
  }

  @Test
  fun `Does not duplicate a persistent Prerequisite when used by a new course`() {
    val pr = prerequisiteRepo.save(Prerequisite(name = "PR1", description = "PR1 Description"))

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    prerequisiteRepo.count() shouldBe 1

    val pr1 = prerequisiteRepo.findById(pr.id!!).get()
    val pr2 = Prerequisite(name = "PR2", description = "PR2 Description")

    val course = CourseEntity(
      name = "C1",
      type = "Approved Programme",
      description = "C1 Desc",
      prerequisites = mutableSetOf(pr1, pr2),
    )

    coursesRepo.save(course)

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    prerequisiteRepo.count() shouldBe 2
  }
}
