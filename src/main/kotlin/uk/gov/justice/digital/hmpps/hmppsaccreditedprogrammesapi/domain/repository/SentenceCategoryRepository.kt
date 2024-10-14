package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SentenceCategoryEntity

@Repository
interface SentenceCategoryRepository : JpaRepository<SentenceCategoryEntity, String> {

  fun findByDescription(description: String): SentenceCategoryEntity?
  fun findAllByDescriptionIn(description: List<String>?): List<SentenceCategoryEntity>
}
