package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.AudienceEntityFactory

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
class AudienceRepositoryTest {

  @Autowired
  private lateinit var entityManager: EntityManager

  @Test
  fun `AudienceRepository successfully saves and retrieves AudienceEntity objects`() {
    val audience = AudienceEntityFactory().withValue("A").produce()
    entityManager.merge(audience)

    val persistedAudience = entityManager.find(AudienceEntity::class.java, audience.id)
    persistedAudience shouldBe audience
  }

  @Test
  fun `AudienceRepository handles duplicate AudienceEntity objects on merge`() {
    val audienceValue = "A"
    val existingAudience = entityManager
      .createQuery("SELECT a FROM AudienceEntity a WHERE a.value = :value", AudienceEntity::class.java)
      .setParameter("value", audienceValue)
      .resultList
      .firstOrNull()

    if (existingAudience == null) {
      val newAudience = AudienceEntity(value = audienceValue)
      entityManager.persist(newAudience)
    }

    val persistedAudiences = entityManager
      .createQuery("SELECT a FROM AudienceEntity a WHERE a.value = :value", AudienceEntity::class.java)
      .setParameter("value", audienceValue)
      .resultList

    persistedAudiences.size shouldBe 1
    persistedAudiences[0].value shouldBe audienceValue
  }
}
